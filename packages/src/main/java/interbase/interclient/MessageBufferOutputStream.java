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

// !!! maxFieldSize needs to be enforced for writes as well as reads.
// !!! perhaps it would be best to associate maxFieldSize with input
// !!! and output streams rather than with result set and prepared statement.

/** 
 * @author Paul Ostler
 **/
final class MessageBufferOutputStream extends java.io.ByteArrayOutputStream
{
  final static private int MIN_OUTPUT_BUFFER_SIZE__ = 1024;
  final static private int HEADER_LENGTH__ = 8;

  private sun.io.CharToByteConverter ctb_;
  private String encoding_; // !!!remove this as a member, we can always call getCharacterEncoding when needed.

  MessageBufferOutputStream ()
  {
    super (MIN_OUTPUT_BUFFER_SIZE__);
    count = HEADER_LENGTH__; // reserve space for two header ints
  }

  void setConverter (sun.io.CharToByteConverter ctb)
  {
    ctb_ = ctb;
    encoding_ = ctb_.getCharacterEncoding ();
  }

  // !!! for testing only
  String getOpcode ()
  {
    return null; // OpcodeTable.getString (buf[8]);
  }

  // overrides parent method
  public synchronized void reset ()
  {
    count = HEADER_LENGTH__;
  }

  void writeFirstInt (int v)
  {
    buf[3] = (byte) ((v >>> 24) & 0xFF);
    buf[2] = (byte) ((v >>> 16) & 0xFF);
    buf[1] = (byte) ((v >>>  8) & 0xFF);
    buf[0] = (byte) ((v >>>  0) & 0xFF);
  }

  void writeSecondInt (int v)
  {
    buf[7] = (byte) ((v >>> 24) & 0xFF);
    buf[6] = (byte) ((v >>> 16) & 0xFF);
    buf[5] = (byte) ((v >>>  8) & 0xFF);
    buf[4] = (byte) ((v >>>  0) & 0xFF);
  }

  int messageSize ()
  {
    return size () - HEADER_LENGTH__;
  }

  private void align (int by)
  {
    int x = count % by;  
    if (x != 0) {
      for (int i = 0; i < by - x; i++)
        write (0);
    }
  }

  void writeBoolean (boolean v)
  {
    write (v ? 1 : 0);
  }

  void writeByte (int v)
  {
    write (v);
  }

  void writeShort (int v)
  {
    align (2);
    write ((v >>> 8) & 0xFF);
    write ((v >>> 0) & 0xFF);
  }

  void writeChar (int v)
  {
    align (2);
    write ((v >>> 8) & 0xFF);
    write ((v >>> 0) & 0xFF);
  }

  void writeInt (int v)
  {
    align (4);
    write ((v >>> 24) & 0xFF);
    write ((v >>> 16) & 0xFF);
    write ((v >>>  8) & 0xFF);
    write ((v >>>  0) & 0xFF);
  }

  void writeLong (long v)
  {
    align (8);
    write ((int)(v >>> 56) & 0xFF);
    write ((int)(v >>> 48) & 0xFF);
    write ((int)(v >>> 40) & 0xFF);
    write ((int)(v >>> 32) & 0xFF);
    write ((int)(v >>> 24) & 0xFF);
    write ((int)(v >>> 16) & 0xFF);
    write ((int)(v >>>  8) & 0xFF);
    write ((int)(v >>>  0) & 0xFF);
  }

  void writeFloat (float v)
  {
    writeInt (Float.floatToIntBits (v));
  }

  void writeDouble (double v)
  {
    writeLong (Double.doubleToLongBits (v));
  }

  // not used.
  void writeUnicodeChars (String s)
  {
    int len = s.length ();
    for (int i = 0 ; i < len ; i++) {
      int v = s.charAt (i);
      write ((v >>> 8) & 0xFF);
      write ((v >>> 0) & 0xFF);
    }
  }

  // write a length delimited, and null terminated byte array
  void writeLDBytes (byte[] b)
  {
    writeLDBytes (b.length, b);
  }

  void writeLDBytes (int length, byte[] buf)
  {
    writeShort (length + 1); // length delimeter includes null terminator
    write (buf, 0, length);
    write (0); // null terminator
  }

  // Once we switch to JDK 1.2, remove cbuf argument,
  // and use ctb_.convert (s, encodingBuf)
  void writeLDChars (String s,
                     char[] cbuf,
                     byte[] encodingBuf) throws java.sql.SQLException
  {
    int slen = s.length ();
    try {
      //int length = ctb_.convert (s, encodingBuf);  Not in JDK 1.1
      for (int i=0; i<slen; i++)
        cbuf[i] = s.charAt (i);
      int nb = ctb_.convert (cbuf, 0, slen, encodingBuf, 0, encodingBuf.length);
      writeLDBytes (nb, encodingBuf);
    }
    catch (java.io.CharConversionException e) {
      throw new CharacterEncodingException (ErrorKey.characterEncoding__write_0__, Utils.getMessage (e)); 
    }
  }
			     
  void writeLDChars (String s) throws java.sql.SQLException
  {
    try {
      writeLDBytes (s.getBytes (encoding_));
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__, encoding_);
    }
  }
			     
  void writeLDSQLText (String s) throws java.sql.SQLException
  {
    // !!! for efficiency, convert into cached byte buffer
    try {
      writeLDBytes (s.getBytes ("8859_1")); // change to utf8 when ib supports sql ids
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__, "8859_1");
    }
  }
  void writeTimestampId (int[] timestampId)
  {
    writeInt (timestampId[0]);
    writeInt (timestampId[1]);
  }

  void writeBlobId (int[] blobId)
  {
    writeInt (blobId[0]);
    writeInt (blobId[1]);
  }

  // MMM - added writeArrayId().
  void writeArrayId (int[] arrayId)
  {
    writeInt (arrayId[0]);
    writeInt (arrayId[1]);
  }
  // MMM - added
}
