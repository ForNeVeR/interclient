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

final class ByteToUnicodeConverterStream extends java.io.InputStream
{
  java.io.InputStream in_;
  java.io.Reader reader_;
  boolean midCharacter_ = false;
  int loByte_;

  // For JDBCNet only
  ByteToUnicodeConverterStream (java.io.InputStream in,
                                java.io.InputStreamReader reader)
  {
    in_ = in;
    reader_ = reader;    
  }

  // For ResultSet.getUnicodeStream() on a CHAR/VARCHAR field
  ByteToUnicodeConverterStream (String s)
  {
    reader_ = new java.io.StringReader (s);
  }

  synchronized public void close () throws java.io.IOException
  {
    reader_.close ();
  }

  synchronized public int read () throws java.io.IOException
  {
    if (!midCharacter_) {
      int c = reader_.read ();
      int hiByte = (c >> 8) & 0xff;
      loByte_ = (c >> 0) & 0xff;
      midCharacter_ = true;
      return hiByte;
    }
    else {
      midCharacter_ = false;
      return loByte_;
    }
  }

  synchronized public int read (byte[] b) throws java.io.IOException
  {
    return read (b, 0, b.length);
  }

  synchronized public int read (byte[] b, int off, int len) throws java.io.IOException
  {
    if (len == 0) return 0;
    int charLen = Math.max (1, len/2);
    char[] cbuf = Globals.cache__.takeCharBuffer (charLen);
    try {
      int nc = reader_.read (cbuf, 0, charLen);
      int byteIndex = off;
      for (int i=0; i<nc; i++) {
        b[byteIndex++] = (byte) ((cbuf[i] >> 8) & 0xff);
        b[byteIndex++] = (byte) ((cbuf[i] >> 0) & 0xff);
      }
      return 2*nc;
    }
    finally {
      Globals.cache__.returnCharBuffer (cbuf);
    }
  }

  public long skip (long n) throws java.io.IOException 
  {
    long skippedChars = reader_.skip (n/2); // n/2 unicode characters is n bytes
    return 2*skippedChars;
  }

  public int available () throws java.io.IOException 
  {
    if (reader_ instanceof java.io.StringReader)
      return 0; // !!This is not right; oh well, cleanup later?
      
    // it may be too large or too small but at least
    // it syncs up at zero.
    return in_.available ();
  }

  public void mark (int readlimit)
  {
    // mark not supported, do nothing
  }

  public void reset () throws java.io.IOException 
  {
    throw new BlobIOException (ResourceKeys.blobIO__mark_not_supported);
  }

  public boolean markSupported () 
  {
    return false;
  }
}
