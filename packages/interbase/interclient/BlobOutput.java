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
 * The constructor for this class should only be invoked by subclasses.
 *
 * @author Paul Ostler
 **/
class BlobOutput
{
  // !! This could be cleaned up so that jdbcnet is more in control,
  // !! this class should be analagous to recvmessage.
  // !! jdbcNet should control blobInputStream_ and open and close blobs
  BlobInputStream blobInputStream_ = null;
  java.io.InputStreamReader reader_ = null;

  private JDBCNet jdbcNet_;
  private int blobId_[];
  private Statement statement_;

  int actualSegmentSize_;
  boolean lastSegment_ = false;
  int blobSize_;
  int blobRef_;

  sun.io.ByteToCharConverter btc_;
  String encoding_;

  // Used only by JDBCNet
  BlobOutput (JDBCNet jdbcNet,
  	      Statement statement,
  	      int[] blobId,
              sun.io.ByteToCharConverter btc) throws java.sql.SQLException
  {
    jdbcNet_ = jdbcNet;
    statement_ = statement;
    blobId_ = blobId;
    btc_ = btc;
    encoding_ = btc_.getCharacterEncoding ();

    remote_OPEN_BLOB ();
    blobInputStream_ = new BlobInputStream (this, jdbcNet_.inputStream_);
    try {
      reader_ = new java.io.InputStreamReader (blobInputStream_, encoding_);
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new BugCheckException (ErrorKey.bugCheck__0__, 129);
    }
  }

  void close () throws java.sql.SQLException
  {
    try {
      blobInputStream_.close ();
    }
    catch (BlobIOException e) { 
      throw new CommunicationException (ErrorKey.communication__io_exception_on_blob_close_01__,
					jdbcNet_.server_,
                                        Utils.getMessage (e));
    }
  }

  void remote_sendPrefetch () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.write (MessageCodes.GET_BLOB_SEGMENT__);
    sendMsg.writeInt (blobRef_);
    jdbcNet_.sendPrefetchBlobMessage (sendMsg);
  }

  void remote_recvPrefetch () throws java.sql.SQLException
  {
    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.receivePrefetchBlobMessage ();
      if (!recvMsg.get_SUCCESS ())
	throw recvMsg.get_EXCEPTIONS ();
      lastSegment_ = recvMsg.getHeaderEndOfStream ();
      actualSegmentSize_ = recvMsg.getHeaderReserved ();
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  void remote_OPEN_BLOB () throws java.sql.SQLException
  {
    RecvMessage recvMsg = null;

    try {
      MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
      sendMsg.write (MessageCodes.OPEN_BLOB__);
      sendMsg.writeInt (statement_.statementRef_);
      sendMsg.writeBlobId (blobId_);
      recvMsg = jdbcNet_.sendAndReceiveBlobMessage (sendMsg, this);
      if (!recvMsg.get_SUCCESS ())
	throw recvMsg.get_EXCEPTIONS ();
      lastSegment_ = recvMsg.getHeaderEndOfStream ();
      actualSegmentSize_ = recvMsg.getHeaderReserved ();
      // blobRef_ must be set before calling prefetch!
      blobRef_ = recvMsg.readInt ();
      blobSize_ = recvMsg.readInt ();
      if (!lastSegment_)
	remote_sendPrefetch ();
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }
    
  void remote_GET_BLOB_SEGMENT () throws java.sql.SQLException
  {
    remote_recvPrefetch ();
    if (!lastSegment_)
      remote_sendPrefetch ();
  }

  void remote_CLOSE_BLOB () throws java.sql.SQLException
  {
    RecvMessage recvMsg = null;

    try {
      MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
      sendMsg.write (MessageCodes.CLOSE_BLOB__);
      sendMsg.writeInt (blobRef_);
      recvMsg = jdbcNet_.sendAndReceiveBlobMessage (sendMsg, this);
      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  // ************************************
  // *** Stream methods for ResultSet ***
  // ************************************
  java.io.InputStream getInputStream ()
  {
    return blobInputStream_;
  }

  java.io.InputStream getUnicodeInputStream ()
  {
    return new ByteToUnicodeConverterStream (blobInputStream_, reader_);
  }

  byte[] getBytes () throws java.sql.SQLException
  {
    byte[] buf = new byte[blobSize_];
    getBytes (buf, 0, blobSize_);
    return buf;
  }

  private void getBytes (byte[] buf, int off, int len) throws java.sql.SQLException
  {
    try {
      int n = 0;
      while (n < len) {
	n += blobInputStream_.read (buf, off + n, len - n);
      }
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_blob_read_01__,
					jdbcNet_.server_,
					Utils.getMessage (e));
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new BugCheckException (ErrorKey.bugCheck__0__, 130);
    }
  }

  String getString () throws java.sql.SQLException
  {
    // #chars <= #bytes (always)
    char[] cbuf = Globals.cache__.takeCharBuffer (blobSize_);
    byte[] buf = Globals.cache__.takeBuffer (blobSize_);

    try {
      getBytes (buf, 0, blobSize_);
      try {
        int nc = btc_.convert (buf, 0, blobSize_, cbuf, 0, blobSize_);
        int new_nc = (statement_.maxFieldSize_ == 0) ? nc : Math.min (nc, statement_.maxFieldSize_);
        return new String (cbuf, 0, new_nc);
      }
      catch (java.io.CharConversionException e) {
        throw new CharacterEncodingException (ErrorKey.characterEncoding__read_0__, Utils.getMessage (e));
      }
    }
    finally {
      Globals.cache__.returnCharBuffer (cbuf);
      Globals.cache__.returnBuffer (buf);
    }
  }
}      


