/*
 * The contents of this file are subject to the Interbase Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy
 * of the License at http://www.Inprise.com/IPL.html
 *
 * Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code was created by Inprise Corporation
 * and its predecessors. Portions created by Inprise Corporation are
 * Copyright (C) Inprise Corporation.
 * All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package interbase.interclient;

/** 
 * @author Paul Ostler
 **/
final class JDBCNet 
{
  // When a thread is finished reading a recvMsg, it returns it to jdbcNet
  // so that jdbcNet can set recvMessageOnWire_ = null and return buffer space
  // to cache.

  // When a blob stream is open in any thread, then the application must
  // take care that concurrent threads do not access the connection!
  // If a concurrent thread does make a jdbc request on the network connection, then
  // the blob stream will be abruptly closed.

  // static Driver.cache__ caches byte arrays, char arrays, and SendMessages

  // ****************
  // *** Tunables ***
  // ****************
  // This must be divisible by 3 and by 2
  final static private int blobPutSegmentSize__ = 30*1024;
  final static private int inputStreamBufferSize__ = 10*1024;

  // !! future enhancement - senderThread

  private java.net.Socket socket_;
  String server_;
  int socketTimeout_;
  java.io.InputStream inputStream_;
  private java.io.OutputStream outputStream_;
  private BlobOutput activeBlobOutputOnWire_;
  private RecvMessage activeRecvMsgOnWire_;

  private boolean streamedMessages_ = false; // !! make configurable
  private boolean byteswap_; 
  private java.util.Vector prefetchRequestors_ = new java.util.Vector (12, 12); 
  Crypter crypter_;

  private int interserverRemoteProtocolVersion_;
  private int interserverMessageCertificate_;
  ServerVersionInformation serverVersionInformation_;

  private sun.io.ByteToCharConverter btc_;
  private sun.io.CharToByteConverter ctb_;

  JDBCNet (int socketTimeoutMilliseconds,
           String server,
           int serverPort,
           sun.io.ByteToCharConverter btc,
	   sun.io.CharToByteConverter ctb) throws java.sql.SQLException
  {
    server_ = server;
    btc_ = btc;
    ctb_ = ctb;
    establishSocketStreams (socketTimeoutMilliseconds, serverPort, server);
    establishProtocol ();
    Globals.cache__.incrementConnectionCount ();
  }

  // !! check sources for SocketInputStream, it may be buffered.
  synchronized private void establishSocketStreams (int socketTimeoutMilliseconds,
                                                    int serverPort, 
						    String server) throws java.sql.SQLException
  {
    try {
      socket_ = new java.net.Socket (server, serverPort);
      socket_.setTcpNoDelay (true); // disable Nagle's algorithm
      socket_.setSoTimeout (socketTimeoutMilliseconds);
      inputStream_ = new java.io.BufferedInputStream (socket_.getInputStream (), inputStreamBufferSize__);
      outputStream_ = socket_.getOutputStream ();
    } 
    catch (java.net.SocketException e) {
      throw new CommunicationException (ErrorKey.communication__socket_exception_on_connect_01__,
					server,
					Utils.getMessage (e));
    }
    catch (java.net.UnknownHostException e) {
      throw new UnknownHostException (ErrorKey.unknownHost__0__,
				      server);
    }
    catch (java.io.IOException e) {
      if (socket_ != null) {
	try {
	  socket_.close ();
	} 
	catch (java.io.IOException ohWell) {}
      }
      throw new CommunicationException (ErrorKey.communication__io_exception_on_connect_01__,
					server,
					Utils.getMessage (e)); 
    }
    catch (SecurityException e) {
      throw new BadInstallationException (ErrorKey.badInstallation__security_check_on_socket_01__,
					  new Object[] { server, Utils.getMessage (e) });
    }
  }

  synchronized void disconnectSocket () throws java.sql.SQLException
  {
    Globals.cache__.decrementConnectionCount ();
    try {
      socket_.close ();
    } 
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_disconnect_01__,
					server_,
					Utils.getMessage (e));
    }
  }

  private void establishProtocol () throws java.sql.SQLException
  {
    RecvMessage recvMsg = receiveProtocolMessage ();

    try {
      if (recvMsg.readUnsignedByte () != MessageCodes.SUCCESS__) {
	// This should only happen if interserver refuses the connection for some
	// unknown reason, the current version of interserver will always say success
	// but in the future, we'll need to add a way to extract an exception from
	// the message.
	throw new RemoteProtocolException (ErrorKey.remoteProtocol__unable_to_establish_protocol__);
      }

      int seed = recvMsg.readInt ();
      crypter_ = new Crypter (seed);
 
      serverVersionInformation_ = new ServerVersionInformation ();
      serverVersionInformation_.jdbcNetProtocolVersion_ = interserverRemoteProtocolVersion_;
      interserverMessageCertificate_ = recvMsg.readInt ();
      serverVersionInformation_.majorVersion_ = recvMsg.readInt ();
      serverVersionInformation_.minorVersion_ = recvMsg.readInt ();
      serverVersionInformation_.buildNumber_ = recvMsg.readInt ();
      serverVersionInformation_.buildLevel_ = recvMsg.readInt (); 
      // !!! get date as a long
      int year = recvMsg.readInt ();  // the year minus 1900. 
      int month = recvMsg.readInt (); // the month between 0-11. 
      int date = recvMsg.readInt ();  // the day of the month between 1-31. 
      if (date == 0)
	serverVersionInformation_.expirationDate_ = null;
      else {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar ();
        cal.set (1900 + year, month, date);
	serverVersionInformation_.expirationDate_ = cal.getTime ();
      }
      serverVersionInformation_.name_ = recvMsg.readLDSQLText ();
    }
    finally {
      destroyRecvMessage (recvMsg);
    }
  }               

  // Initial message received on stream must be special cased
  // since byteswap and protocol version needs to be established.
  synchronized private RecvMessage receiveProtocolMessage () throws java.sql.SQLException
  {
    try {
      interserverRemoteProtocolVersion_ = readJavaInt ();
      if (interserverRemoteProtocolVersion_ != Globals.jdbcNetProtocolVersion__)
	throw new BadInstallationException (ErrorKey.badInstallation__incompatible_remote_protocols__);
      int messageLength = readJavaInt ();
      readJavaInt ();                    // dummy endOfStream
      byteswap_ = (readJavaInt () != 0); // reserved
      RecvMessage recvMsg = new RecvMessage (messageLength, inputStream_, byteswap_, 0, 0, btc_);
      recvMsg.bufferOut ();
      return recvMsg;
    }
    catch (java.io.InterruptedIOException e) {
      throw new SocketTimeoutException (ErrorKey.socketTimeout__012__,
					new Object[] { server_,
						       String.valueOf (socketTimeout_),
						       Utils.getMessage (e) });
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_recv_protocol_01__,
					server_,
					Utils.getMessage (e));
    }
  }

  synchronized private RecvMessage receiveMessage () throws java.sql.SQLException
  {
    try {
      if (readNativeInt () != interserverMessageCertificate_) 
	throw new RemoteProtocolException (ErrorKey.remoteProtocol__bad_message_certficate_from_server__);
      int messageLength = readNativeInt ();
      if (Globals.debug__) Globals.trace ("incoming message length = " + messageLength);
      int endOfStream = readNativeInt ();
      int reserved = readNativeInt ();
      RecvMessage recvMsg = new RecvMessage (messageLength, inputStream_, byteswap_, endOfStream, reserved, btc_);
      if (streamedMessages_)
	activeRecvMsgOnWire_ = recvMsg;
      else
	recvMsg.bufferOut ();
      return recvMsg;
    }
    catch (java.io.InterruptedIOException e) {
      throw new SocketTimeoutException (ErrorKey.socketTimeout__012__,
					new Object[] { server_,
						       String.valueOf (socketTimeout_),
						       Utils.getMessage (e) });
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_recv_message_01__,
					server_,
					Utils.getMessage (e));
    }
  }

  synchronized RecvMessage receivePrefetchMessage (Statement requestor) throws java.sql.SQLException
  {
    if (requestor.prefetchedRecvMsg_ != null) { // its been buffered-out
      RecvMessage recvMsg = requestor.prefetchedRecvMsg_;
      requestor.prefetchedRecvMsg_ = null;
      return recvMsg;
    }
    if (activeBlobOutputOnWire_ != null) {
      activeBlobOutputOnWire_.close ();
      // !!! switch back to default buffer
      activeRecvMsgOnWire_ = null;
    }
    else if (activeRecvMsgOnWire_ != null) {
      activeRecvMsgOnWire_.bufferOut ();
      activeRecvMsgOnWire_ = null;
    }
    while (!prefetchRequestors_.isEmpty ()) {
      Statement s = (Statement) prefetchRequestors_.firstElement ();
      prefetchRequestors_.removeElementAt (0);
      RecvMessage recvMsg = receiveMessage ();
      if (requestor == s) {
	if (streamedMessages_) {
	  activeRecvMsgOnWire_ = recvMsg;
	}
	else {
	  recvMsg.bufferOut ();
	}
	return recvMsg;
      }
      else {
	recvMsg.bufferOut ();
	s.prefetchedRecvMsg_ = recvMsg;
      }
    }
    throw new BugCheckException (ErrorKey.bugCheck__0__, 100);
  }

  // flush unread bytes and recycle memory
  synchronized void destroyRecvMessage (RecvMessage recvMsg) throws java.sql.SQLException
  {
    if (recvMsg == activeRecvMsgOnWire_) {
      activeRecvMsgOnWire_ = null;
    }
    if (recvMsg != null) {
      recvMsg.destroy ();
    }
  }

  // Under the current design, sendMessages are completely buffered
  // before going onto the wire.  Once interserver is in java,
  // rework this so that sendMsg writes can go directly onto the
  // wire.
  MessageBufferOutputStream createMessage ()
  {
    MessageBufferOutputStream result = Globals.cache__.takeOutputBuffer ();
    result.setConverter (ctb_);
    return result;
  }

  void sendMessage (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    // !! try spawning a thread here.
    if (Globals.debug__) Globals.trace (sendMsg.getOpcode ());
    if (Globals.debug__) Globals.trace ("outgoing message length = " + sendMsg.messageSize ());
    try {
    synchronized (outputStream_) {
      sendMsg.writeFirstInt (interserverMessageCertificate_);
      sendMsg.writeSecondInt (sendMsg.messageSize ());
      if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis ();}
      sendMsg.writeTo (outputStream_);
      if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis ();}
      if (Globals.debug__) { Globals.trace ("Time spent writing send message = " +
					        (Globals.endTime__ - Globals.startTime__));}
    }
    if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis ();}
    outputStream_.flush ();
    if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis ();}
    if (Globals.debug__) { Globals.trace ("Time spent flushing send message = " +
					      (Globals.endTime__ - Globals.startTime__));}
    Globals.cache__.returnOutputBuffer (sendMsg);
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_send_message_01__,
					server_,
					Utils.getMessage (e));
    }
  }

  // This should be called from a different thread after
  // a call to receivePrefetchMessage ()
  synchronized void sendPrefetchMessage (Statement requestor,
                                         MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    prefetchRequestors_.addElement (requestor);
    sendMessage (sendMsg);
  }

  synchronized RecvMessage sendAndReceiveMessage (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    sendMessage (sendMsg);
    clearAllPendingMessages ();
    return receiveMessage ();
  }

  private void clearAllPendingMessages () throws java.sql.SQLException
  {
    if (activeBlobOutputOnWire_ != null) {
      activeBlobOutputOnWire_.close ();
      // !!! switch back to default buffer size
      activeBlobOutputOnWire_ = null;
    }
    else if (activeRecvMsgOnWire_ != null) {
      activeRecvMsgOnWire_.bufferOut ();
      activeRecvMsgOnWire_ = null;
    }
    while (!prefetchRequestors_.isEmpty ()) {
      Statement s = (Statement) prefetchRequestors_.firstElement ();
      prefetchRequestors_.removeElementAt (0);
      RecvMessage recvMsg = receiveMessage ();
      recvMsg.bufferOut ();
      s.prefetchedRecvMsg_ = recvMsg;
    }
  }

  synchronized RecvMessage sendAndReceiveBlobMessage (MessageBufferOutputStream sendMsg,
						      BlobOutput blobOutput) throws java.sql.SQLException
  {
    sendMessage (sendMsg);
    clearAllPendingMessages ();
    // !!! swith to 1k buffer here to ensure pass thru
    activeBlobOutputOnWire_ = blobOutput;
    return receiveMessage ();
  }

  synchronized void sendPrefetchBlobMessage (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    sendMessage (sendMsg);
  }

  synchronized RecvMessage receivePrefetchBlobMessage () throws java.sql.SQLException
  {
    // blob messages never get buffered-out.
    return receiveMessage ();
  }

  // Read a big endian integer, independent of byte-swap and alignment.
  private int readJavaInt () throws java.io.IOException
  {
    if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis (); }
    int j, value = 0;
    j = inputStream_.read (); value += (j&0xff);
    j = inputStream_.read (); value += (j&0xff)<<8;
    j = inputStream_.read (); value += (j&0xff)<<16;
    j = inputStream_.read (); value += (j&0xff)<<24;
    if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis (); }
    if (Globals.debug__) { Globals.trace ("readJavaInt(): " +
                                        (Globals.endTime__ - Globals.startTime__));}
    return (value);
  }

  // byteswap, but no alignment
  int readNativeInt () throws java.io.IOException
  {
    if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis (); }
    int j, value = 0;
    if (byteswap_) {
      j = inputStream_.read (); value += (j&0xff);
      j = inputStream_.read (); value += (j&0xff)<<8;
      j = inputStream_.read (); value += (j&0xff)<<16;
      j = inputStream_.read (); value += (j&0xff)<<24;
    }
    else {
      j = inputStream_.read (); value += (j&0xff)<<24;
      j = inputStream_.read (); value += (j&0xff)<<16;
      j = inputStream_.read (); value += (j&0xff)<<8 ;
      j = inputStream_.read (); value += (j&0xff);
    }
    if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis (); }
    if (Globals.debug__) { Globals.trace ("readNativeInt(): " +
                                        (Globals.endTime__ - Globals.startTime__));}
    return value;
  }

  synchronized RecvMessage sendStreamAndReceiveMessage (MessageBufferOutputStream sendMsg,
                                                        java.io.InputStream userIn,
                                                        int blobSize) throws java.sql.SQLException
  {
    sendMessageWithByteStream (sendMsg, userIn, blobSize);
    return receiveMessage ();
  }

  synchronized RecvMessage sendUnicodeStreamAndReceiveMessage (MessageBufferOutputStream sendMsg,
                                                        java.io.InputStream userIn,
                                                        int blobSize) throws java.sql.SQLException
  {
    sendMessageWithUnicodeStream (sendMsg, userIn, blobSize);
    return receiveMessage ();
  }

  private void sendMessageWithByteStream (MessageBufferOutputStream sendMsg,
                            java.io.InputStream userIn,
                            int blobSize) throws java.sql.SQLException
  {
    synchronized (outputStream_) {
      sendMessage (sendMsg);
      writeBytes (userIn, blobSize);
    }
  }

  private void sendMessageWithUnicodeStream (MessageBufferOutputStream sendMsg,
                            java.io.InputStream userIn,
                            int unicodeSize) throws java.sql.SQLException
  {
    synchronized (outputStream_) {
      sendMessage (sendMsg);
      writeUnicodeToBytes (userIn, unicodeSize);
    }
  }

  void writeBytes (java.io.InputStream in,
                   int blobSize) throws java.sql.SQLException
  {
    if (blobSize == 0) return;
    int segmentSize = Math.min (blobPutSegmentSize__, blobSize);
    byte[] buffer = Globals.cache__.takeBuffer (segmentSize);

    int written = 0;
    int bytesRead;
    int toRead;
    try {
      if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis ();}
      while (written < blobSize) {
        toRead = Math.min (segmentSize, blobSize - written);
        bytesRead = in.read (buffer, 0, toRead);
        if (bytesRead == -1) // EOF
          throw new CommunicationException (ErrorKey.communication__user_stream__unexpected_eof__);
        else {
          outputStream_.write (buffer, 0, bytesRead);
          written += bytesRead;
          if (Globals.debug__) Globals.trace ("blob bytes written = " + bytesRead);
	}
        outputStream_.flush ();  // !!! when should i flush
      }
      if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis ();}
      if (Globals.debug__) { Globals.trace ("Time spent writing " + blobSize + " blob bytes = " +
                                          (Globals.endTime__ - Globals.startTime__));}
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__user_stream__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
    finally {
      Globals.cache__.returnBuffer (buffer);
    }
  }

  // !!! comment what this does
  void writeUnicodeToBytes (java.io.InputStream in,
                            int unicodeSize) throws java.sql.SQLException
  {
    if (unicodeSize == 0) return;
    if ((unicodeSize%2) != 0)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__setUnicodeStream_odd_bytes__);
    // ensure that segment size can accomodate any encoding up to 3 bytes
    int segmentSize = Math.min (blobPutSegmentSize__, (unicodeSize/2)*3);
    int charSize = segmentSize/3;

    int unicodeBytesWritten = 0;
    byte[] byteBuf = Globals.cache__.takeBuffer (segmentSize);
    byte[] unicodeBuf = Globals.cache__.takeBuffer (2*charSize);
    char[] cbuf = Globals.cache__.takeCharBuffer (charSize);
    try {
      while (unicodeBytesWritten < unicodeSize) {
        int toRead = Math.min (2*charSize, unicodeSize - unicodeBytesWritten);
        int bytesRead = 0;
        while (bytesRead < toRead) {
          int n = in.read (unicodeBuf, 0+bytesRead, toRead-bytesRead);
          if (n == -1) // EOF
            throw new CommunicationException (ErrorKey.communication__user_stream__unexpected_eof__);
          bytesRead += n;
        }
        int unicodeBufIndex = 0;
        int charsRead = bytesRead/2;
        for (int i=0; i<charsRead; i++) {
          int hiByte = (unicodeBuf[unicodeBufIndex++] & 0xff) << 8;
          int loByte = (unicodeBuf[unicodeBufIndex++] & 0xff) << 0;
          cbuf[i] = (char)(hiByte + loByte);
        }
        int nb = ctb_.convert (cbuf, 0, charsRead, byteBuf, 0, byteBuf.length);
        outputStream_.write (byteBuf, 0, nb);
        unicodeBytesWritten += bytesRead;
        outputStream_.flush ();  // !!! when should i flush
      }
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__user_stream__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
    finally {
      Globals.cache__.returnBuffer (byteBuf);
      Globals.cache__.returnCharBuffer (cbuf);
      Globals.cache__.returnBuffer (unicodeBuf);
    }
  }

  BlobOutput openBlobOutput (Statement statement,
	                     int[] blobId) throws java.sql.SQLException
  {
    return new BlobOutput (this, statement, blobId, btc_);
  }

  // returns server side blobRef
  int setBlobBinaryStream (Statement statement,
                           java.io.InputStream userStream,
                           int length) throws java.sql.SQLException
  {
    return (new BlobInput (this, statement, BlobInput.a3, userStream, length)).a1;
  }

  // returns server side blobRef
  int setBlobUnicodeStream (Statement statement,
                            java.io.InputStream userStream,
                            int length) throws java.sql.SQLException
  {
    return (new BlobInput (this, statement, BlobInput.a2, userStream, length)).a1;
  }

  // returns server side blobRef
  int setBlobString (Statement statement,
                     String s) throws java.sql.SQLException
  {
    try {
      byte[] buf = s.getBytes (ctb_.getCharacterEncoding ()); // !!! performance problem here.
      return (new BlobInput (this, statement, BlobInput.a3,
	 	          new java.io.ByteArrayInputStream (buf),
		          buf.length)).a1;
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new BugCheckException (ErrorKey.bugCheck__0__, 128);
    }
  }
}
