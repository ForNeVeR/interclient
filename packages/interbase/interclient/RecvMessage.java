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
final class RecvMessage
{
  private boolean buffered_;
  private byte[] buf_;
  private int pos_;
  private int oldPos_; // saved when buffered out for alignment issues.
  private int count_;  // differs from messageLength only when buffered out.
  private int messageLength_;
  private java.io.InputStream in_;
  private boolean byteswap_;
  private int mark_;       

  boolean headerEndOfStream_;
  int headerReserved_;

  sun.io.ByteToCharConverter btc_;
  String encoding_;

  RecvMessage (int messageLength,
	       java.io.InputStream in,
	       boolean byteswap,
	       int endOfStream,
	       int reserved,
	       sun.io.ByteToCharConverter btc)
  {
    messageLength_ = messageLength;
    in_ = in;
    byteswap_ = byteswap;
    headerEndOfStream_ = (endOfStream != 0);
    headerReserved_ = reserved;
    btc_ = btc;
    encoding_ = btc.getCharacterEncoding ();

    buffered_ = false;
    pos_ = oldPos_ = 0;
    count_ = messageLength;
  }

  // *************************************
  // *** For random column access only ***
  // *************************************
  int getPosition ()
  {
    return pos_;
  }

  void setPosition (int newPos)
  {
    pos_ = newPos;
  }

  void mark ()
  {
    mark_ = pos_;
  }

  void reset ()
  {
    pos_ = mark_;
  }

  // *****************
  // *** Utilities ***
  // *****************

  // If we're the active recv message in jdbcNet,
  // then another thread may want to buffer us out while we're
  // in the middle of a read, so its important this is synchronized.
  // Other threads will always buffer us out before attempting to
  // use the input stream.
  synchronized void bufferOut () throws java.sql.SQLException
  {
    try {
      if (!buffered_) {
	int remaining = messageLength_ - pos_;
	buf_ = Globals.cache__.takeBuffer (remaining);// !!!enforce a minimum buffer size from cache
	if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis ();}
	int n = 0;
	int bytesRead;
	while (n < remaining) {
	  bytesRead = in_.read (buf_, n, remaining - n);
	  if (Globals.debug__) Globals.trace ("Network bytes read = " + bytesRead);
	  n += bytesRead;
	}
	oldPos_ = pos_;
	pos_ = 0;
	count_ = remaining;
	buffered_ = true;
	in_ = null;
	if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis ();}
	if (Globals.debug__) { Globals.trace ("Time spent reading " + remaining + " message bytes = " +
					    (Globals.endTime__ - Globals.startTime__));}
      }
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__, Utils.getMessage (e));
    }
  }

  void destroy () throws java.sql.SQLException
  {
    if (buffered_) {
      Globals.cache__.returnBuffer (buf_);
      buf_ = null;
    }
    else {
      int remaining = count_ - pos_;
      skip (remaining);
      in_ = null;
    }
  }

  boolean getHeaderEndOfStream ()
  {
    return headerEndOfStream_;
  }

  int getHeaderReserved ()
  {
    return headerReserved_;
  }

  private void align (int by) throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 101);

    int x = (pos_ + oldPos_) % by;
    if (x != 0) 
      skip (by - x);
  }

  private void skip (int n) throws java.sql.SQLException
  {
    pos_ += n;
    if (!buffered_) {
      try {
	int skipped = 0;
	while (skipped < n) {
	  skipped += in_.skip (n-skipped);
	}
      }
      catch (java.io.IOException e) {
	// cleanup
	throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					  Utils.getMessage (e));
      }
    }
  }

  // *********************
  // *** Read routines ***
  // *********************

  boolean readBoolean () throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 102);

    if (buffered_)
      return (buf_[pos_++] != 0);

    try {
      pos_++;
      return (in_.read () != 0);
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }
      
  byte readByte () throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 103);

    if (buffered_)
      return buf_[pos_++];

    try {
      pos_++;
      return (byte) in_.read ();
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  int readUnsignedByte () throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 104);

    if (buffered_)
      return (buf_[pos_++] & 0xff);

    try {
      pos_++;
      return in_.read ();
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  short readShort () throws java.sql.SQLException
  {
    try {
      align (2);

      int b1, b2;
      if (buffered_) {
	b1 = buf_[pos_++] & 0xff;
	b2 = buf_[pos_++] & 0xff;
      }
      else {
	pos_ += 2;
	b1 = in_.read ();
	b2 = in_.read ();
      }

      if (byteswap_)
	return (short) ((b1 << 0) + (b2 << 8));
      else
	return (short) ((b1 << 8) + (b2 << 0));
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  int readUnsignedShort () throws java.sql.SQLException
  {
    try {
      align (2);

      int b1, b2;
      if (buffered_) {
	b1 = buf_[pos_++] & 0xff;
	b2 = buf_[pos_++] & 0xff;
      }
      else {
	pos_ += 2;
	b1 = in_.read ();
	b2 = in_.read ();
      }

      if (byteswap_)
	return ((b1 << 0) + (b2 << 8));
      else
	return ((b1 << 8) + (b2 << 0));
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  int readInt () throws java.sql.SQLException
  {
    try {
      align (4);

      int b1, b2, b3, b4;
      if (buffered_) {
	b1 = buf_[pos_++] & 0xff;
	b2 = buf_[pos_++] & 0xff;
	b3 = buf_[pos_++] & 0xff;
	b4 = buf_[pos_++] & 0xff;
      }
      else {
	pos_ += 4;
	b1 = in_.read ();
	b2 = in_.read ();
	b3 = in_.read ();
	b4 = in_.read ();
      }

      if (byteswap_)
	return (b1 << 0) + (b2 << 8) + (b3 << 16) + (b4 << 24);
      else
	return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
    }
    catch (java.io.IOException e) {
      // cleanup
      throw new CommunicationException (ErrorKey.communication__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  long readLong () throws java.sql.SQLException
  {
    align (8);
    if (byteswap_)
      return (readInt () & 0xFFFFFFFFL) + ((long) (readInt ()) << 32);
    else
      return ((long) (readInt ()) << 32) + (readInt () & 0xFFFFFFFFL);
  }

  float readFloat () throws java.sql.SQLException
  {
    return Float.intBitsToFloat (readInt ());
  }

  double readDouble () throws java.sql.SQLException
  {
    return Double.longBitsToDouble (readLong ());
  }

  byte[] readLDBytes (int maxByteLength) throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 105);

    int len = readUnsignedShort ();
    int newLen = (maxByteLength == 0) ? len : Math.min (len, maxByteLength);
    byte[] result = new byte[newLen];
    if (buffered_) {
      System.arraycopy (buf_, pos_, result, 0, newLen);
      pos_ += len;
      return result;
    }
    else { 
      // !!! implement streamed messages later
      throw new BugCheckException (ErrorKey.bugCheck__0__, 106);
    }
  }

  // returns the number of unicode chars read
  int readLDChars (char[] cbuf,
		   int maxCharLength) throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 107);

    try {
      int len = readUnsignedShort ();
      if (buffered_) {
        int nc = btc_.convert (buf_, pos_, pos_ + len, cbuf, 0, maxCharLength);
	pos_ += len;
	return nc;
      }
      else { 
	// !!! implement streamed messages later
	// read directly from input stream buffer
	// expand internal input stream buffer length to accomodate bytes,
	// or rely on stateful conversion capability
	throw new BugCheckException (ErrorKey.bugCheck__0__, 108);
      }
    }
    catch (java.io.CharConversionException e) {
      throw new CharacterEncodingException (ErrorKey.characterEncoding__read_0__,
                                            Utils.getMessage (e));
    }
  }  

  String readLDString (String encoding) throws java.sql.SQLException
  {
    try {
      int len = readUnsignedShort ();
      if (buffered_) {
	String result = new String (buf_, pos_, len, encoding);
        pos_ += len;
        return result;
      }
      else {
	// !!! implement streamed messages later
	// read directly from input stream buffer
	// expand internal input stream buffer length to accomodate bytes,
	// or rely on stateful conversion capability
	throw new BugCheckException (ErrorKey.bugCheck__0__, 109);
      }
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__,
                                                  encoding);
    }
  }

  String readLDString () throws java.sql.SQLException
  {
    return readLDString (encoding_);
  }

  String readLDSQLText () throws java.sql.SQLException
  {
    // This will change to UTF8 once IB supports sql identifiers
    return readLDString ("8859_1");
  }

  // *** special types *** !!! replace these with calls to readLong

  int[] readTimestampId () throws java.sql.SQLException
  {
    int encodedTimeStamp[] = new int[2];
    encodedTimeStamp[0] = readInt ();
    encodedTimeStamp[1] = readInt ();
    return encodedTimeStamp;
  }

  int[] readBlobId () throws java.sql.SQLException
  {
    int blobId[] = new int[2];
    blobId[0] = readInt ();
    blobId[1] = readInt ();
    return blobId;
  }

  // MMM - added readArrayId ().
  int[] readArrayId () throws java.sql.SQLException
  {
    int arrayId[] = new int[2];
    arrayId[0] = readInt ();
    arrayId[1] = readInt ();
    return arrayId;
  }
  // MMM - end

  // *********************
  // *** skip routines ***
  // *********************

  void skipShort () throws java.sql.SQLException
  {
    align (2);
    skip (2);
  }

  void skipInt () throws java.sql.SQLException
  {
    align (4);
    skip (4);
  }

  void skipFloat () throws java.sql.SQLException
  {
    align (4);
    skip (4);
  }

  void skipDouble () throws java.sql.SQLException
  {
    align (8);
    skip (8);
  }

  // logically equivalent to two sucessive skipInt's
  void skipBlobId () throws java.sql.SQLException
  { 
    align (4);
    skip (8);
  }

  // logically equivalent to two sucessive skipInt's
  void skipTimestampId () throws java.sql.SQLException
  { 
    align (4);
    skip (8);
  }

  void skipLDBytes () throws java.sql.SQLException
  {
    if (pos_ >= count_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 110);

    skip (readUnsignedShort ());
  }

  // CJL-IB6 added for int64 support
  void skipLong () throws java.sql.SQLException
  {
    align (8);
    skip (8);
  }
  // CJL-IB6 end change

  // ******************************************
  // *** Common receive message data groups ***
  // ******************************************

  boolean get_SUCCESS () throws java.sql.SQLException
  {
    switch (readUnsignedByte ()) {
    case MessageCodes.SUCCESS__:
      if (Globals.debug__) Globals.trace ("SUCCESS");
      return true;
    case MessageCodes.FAILURE__:
      if (Globals.debug__) Globals.trace ("FAILURE");
      return false;
    default:
      throw new RemoteProtocolException (ErrorKey.remoteProtocol__unexpected_token_from_server_0__,
					 100);
    }
  }

  java.sql.SQLWarning get_WARNINGS () throws java.sql.SQLException
  {
    java.sql.SQLWarning accumulatedWarnings = null;

    int code;
    while ((code = readUnsignedByte ()) == MessageCodes.WARNING__) {
      if (Globals.debug__) Globals.trace ("WARNING");
      accumulatedWarnings = 
	(java.sql.SQLWarning) Utils.accumulateSQLExceptions (accumulatedWarnings, 
							     makeSQLWarning ());
    }
    
    if (code != MessageCodes.END_WARNINGS__)
      throw new RemoteProtocolException (ErrorKey.remoteProtocol__unexpected_token_from_server_0__,
					 101);
    if (Globals.debug__) Globals.trace ("END_WARNINGS");

    return accumulatedWarnings;
  }

  java.sql.SQLException get_EXCEPTIONS () throws java.sql.SQLException
  {
    java.sql.SQLException accumulatedExceptions = null;

    int code;
    while ((code = readUnsignedByte ()) == MessageCodes.EXCEPTION__) {
      if (Globals.debug__) Globals.trace ("EXCEPTION");
      accumulatedExceptions =
	Utils.accumulateSQLExceptions (accumulatedExceptions, 
				       makeSQLException ());
    }

    if (code != MessageCodes.END_EXCEPTIONS__) {
      accumulatedExceptions = 
        Utils.accumulateSQLExceptions (accumulatedExceptions, 
			               new RemoteProtocolException (ErrorKey.remoteProtocol__unexpected_token_from_server_0__,
					                            102));
    }
    if (Globals.debug__) Globals.trace ("END_EXCEPTIONS");

    return accumulatedExceptions;
  }

  java.sql.SQLWarning makeSQLWarning () throws java.sql.SQLException
  {
    int errorKeyIndex = readInt ();
    int reservedCode = readInt ();
    int errorCode = readInt ();
    int ibSQLCode = readInt ();
    String ibErrorMessage = readLDString ();

    return createSQLWarning (errorKeyIndex, 
			     reservedCode, 
			     errorCode, 
			     ibSQLCode, 
			     ibErrorMessage);
  }

  java.sql.SQLException makeSQLException () throws java.sql.SQLException
  {
    int errorKeyIndex = readInt ();
    int reservedCode = readInt ();
    int errorCode = readInt ();
    int ibSQLCode = readInt ();
    String ibErrorMessage = readLDString ();

    return createSQLException (errorKeyIndex, 
			       reservedCode, 
			       errorCode, 
			       ibSQLCode, 
			       ibErrorMessage);
  }

  boolean get_ROW_DATUM () throws java.sql.SQLException
  {
    switch (readUnsignedByte ()) {
    case MessageCodes.ROW_DATUM__:
      if (Globals.debug__) Globals.trace ("ROW_DATUM");
      return true;
    case MessageCodes.END_ROW_DATA__:
      if (Globals.debug__) Globals.trace ("END_ROW_DATA");
      return false;
    default:
      throw new RemoteProtocolException (ErrorKey.remoteProtocol__unexpected_token_from_server_0__,
					 103);
    }
  }

  SQLException createSQLException (int errorKeyIndex,
				     int reservedCode,
				     int errorCode,
				     int ibSQLCode,
				     String ibErrorMessage)
  {
    // InterBase codes, they may or may not have been handled
    // by interserver, so errorKeyIndex may or may not be 
    // unhandledEngineError_0__

    switch (errorCode) {

    // ******************************************************************
    // *** InterBase constructors, all using default engine error key ***
    // ******************************************************************
    case ErrorCodes.isc_bufexh:
    case ErrorCodes.isc_virmemexh:
      return new OutOfMemoryException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage); 

    case ErrorCodes.isc_bug_check:
      return new BugCheckException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);

    case ErrorCodes.isc_io_error: // !!!this should be trapped by interserver on attach only
      return new UnavailableDatabaseFileException  (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage); 

    case ErrorCodes.isc_unavailable:
      return new UnavailableInterBaseServerException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage); 

    case ErrorCodes.isc_lock_conflict:
      return new LockConflictException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);

    case ErrorCodes.isc_update_conflict:
      return new UpdateConflictException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage); 

    case ErrorCodes.isc_deadlock:
      return new DeadlockException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);

    case ErrorCodes.isc_bad_checksum:
    case ErrorCodes.isc_badpage:
    case ErrorCodes.isc_db_corrupt:
      return new CorruptDatabaseException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);

    case ErrorCodes.isc_login:
      return new UnauthorizedUserException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);

    // ********************************
    // *** InterServer constructors ***
    // ********************************
    case ErrorCodes.outOfMemory:
      return new OutOfMemoryException (errorKeyIndex);

    case ErrorCodes.bugCheck:
      return new BugCheckException (errorKeyIndex, reservedCode); // bugCode

    case ErrorCodes.remoteProtocol:
      return new RemoteProtocolException (errorKeyIndex);

    case ErrorCodes.driverNotCapable:
      return new DriverNotCapableException (errorKeyIndex);

    case ErrorCodes.invalidOperation:
      return new InvalidOperationException (errorKeyIndex);

    case ErrorCodes.invalidArgument__:
      return new InvalidArgumentException (errorKeyIndex);

    case ErrorCodes.communication:
      return new CommunicationException (errorKeyIndex);

    case ErrorCodes.unsupportedCharacterSet__:
      return new UnsupportedCharacterSetException (errorKeyIndex, reservedCode); // characterCode

// <!>   get to the bottom of this --- CJL-IB6 added for SQL Dialect support
    case ErrorCodes.unsupportedSQLDialectException__:
      return new SQLDialectException (errorKeyIndex, reservedCode); // adjusted SQL Dialect

// CJL-IB6 end change

    default:  // This may or may not have a special errorKey assigned by interserver (usually the default key)
      return new SQLException (errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
    }
  }

  java.sql.SQLWarning createSQLWarning (int errorKeyIndex,
					int reservedCode,
					int errorCode,
					int ibSQLCode,
					String ibErrorMessage)
  {
    java.sql.SQLException e = createSQLException (errorKeyIndex, 
						  reservedCode, 
						  errorCode, 
						  ibSQLCode, 
						  ibErrorMessage);
    return new java.sql.SQLWarning (e.getMessage (),
				    e.getSQLState (),
				    e.getErrorCode ());
  }
}


