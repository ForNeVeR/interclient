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

// All methods should throw BlobIOException and not IOException.

/**
 * This class represents the input streams
 * returned to users of ResultSet.getAsciiStream (),
 * getUnicodeStream(), and getBinaryStream ().
 *
 * @author Paul Ostler
 **/
final class BlobInputStream extends java.io.InputStream
{
  BlobOutput a1;
  boolean a2 = false;
  int a3 = 0;
  java.io.InputStream a4;

  BlobInputStream (BlobOutput p1, java.io.InputStream p2)
  {
    a1 = p1;
    a4 = p2;
  }

  void m1 () throws BlobIOException
  {
    int skip = available ();
    while (skip > 0) {
      skip -= skip (skip);
    }
  }

  synchronized public void close () throws BlobIOException
  {
    if (a2)
      return;

    // We could optimize this by first calling send_CLOSE_BLOB,
    // then only at the very end, after flushing prefetched segments,
    // call recv_CLOSE_BLOB.
    try {
      m1 ();
      if (!a1.lastSegment_) {
	a3 = 0;
	a1.remote_recvPrefetch ();
	m1 ();
	if (!a1.lastSegment_)
	  a1.remote_CLOSE_BLOB ();
      }
    }
    catch (java.sql.SQLException e) {
      throw new BlobIOException (ResourceKeys.blobIO__sqlException_on_close_0,
				 Utils.getMessage (e));
    }
    finally {
      a2 = true;
    }
  }

  synchronized public int read () throws BlobIOException 
  {
    if (a2)
      throw new BlobIOException (ResourceKeys.blobIO__read_on_closed);

    try {
      if (endOfStream ())
	return -1; // EOF

      if (available () == 0) {
	a3 = 0;
	a1.remote_GET_BLOB_SEGMENT ();
      }

      int value = a4.read ();
      a3++;
      return value;
    }
    catch (java.sql.SQLException e) {
      throw new BlobIOException (ResourceKeys.blobIO__sqlException_on_read_0,
				 Utils.getMessage (e));
    }
    catch (java.io.IOException e) {
      throw new BlobIOException (ResourceKeys.blobIO__ioException_on_read_0,
                                 Utils.getMessage (e));
    }
  }

  synchronized public int read (byte[] b) throws BlobIOException 
  {
    return read (b, 0, b.length);
  }

  synchronized public int read (byte[] b, int off, int len) throws BlobIOException
  {
    if (a2)
      throw new BlobIOException (ResourceKeys.blobIO__read_on_closed);

    try {
      if (endOfStream ())
	return -1; // EOF

      if (available () == 0) {
	a3 = 0;
	a1.remote_GET_BLOB_SEGMENT ();
      }

      if (Globals.debug__) { Globals.startTime__ = System.currentTimeMillis (); }
      int lenToRead = Math.min (available (), len);      
      int actuallyRead = a4.read (b, off, lenToRead);
      a3 += actuallyRead;
      if (Globals.debug__) { Globals.endTime__ = System.currentTimeMillis (); }
      if (Globals.debug__) { Globals.trace ("Time spent reading " + actuallyRead +
						" byte blob segment = " + 
	       				        (Globals.endTime__ - Globals.startTime__));}
      return actuallyRead;
    }
    catch (java.sql.SQLException e) {
      throw new BlobIOException (ResourceKeys.blobIO__sqlException_on_read_0,
				 Utils.getMessage (e));
    }
    catch (java.io.IOException e) {
      throw new BlobIOException (ResourceKeys.blobIO__ioException_on_read_0,
                                 Utils.getMessage (e));
    }
  }

  public long skip (long n) throws BlobIOException 
  {
    if (a2)
      throw new BlobIOException (ResourceKeys.blobIO__skip_on_closed);

    try {
      if (endOfStream ())
	return 0; // EOF

      if (available () == 0) {
	a3 = 0;
	a1.remote_GET_BLOB_SEGMENT ();
      }

      long nToSkip = Math.min (available (), (int) n);      
      long actuallySkipped = a4.skip (nToSkip);
      a3 += actuallySkipped;
      return actuallySkipped;
    }
    catch (java.sql.SQLException e) {
      throw new BlobIOException (ResourceKeys.blobIO__sqlException_on_skip_0,
				 Utils.getMessage (e));
    }
    catch (java.io.IOException e) {
      throw new BlobIOException (ResourceKeys.blobIO__ioException_on_skip_0,
                                 Utils.getMessage (e));
    }
  }

  public int available () throws BlobIOException 
  {
    return (a1.actualSegmentSize_ - a3);
  }

  boolean endOfStream () throws BlobIOException
  {
    return a1.lastSegment_ && (available () == 0);
  }

  public void mark (int readlimit) 
  { 
    // mark not supported, do nothing
  }

  public void reset () throws BlobIOException 
  {
    throw new BlobIOException (ResourceKeys.blobIO__mark_not_supported);
  }

  public boolean markSupported () 
  {
    return false;   
  }
 
}
