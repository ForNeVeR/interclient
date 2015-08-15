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
 * Contributor(s): Friedrich von Never.
 */
package interbase.interclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Represents an SQL 3 Binary Large Object.
 * By default, a Blob is a transaction duration reference to a
 * binary large object. By default, a Blob is implemented using a
 * LOCATOR(blob) internally.
 *
 * @since <font color=red>JDBC 2, not yet supported</font>
 **/
final public class Blob implements java.sql.Blob
{

  /**
   * The length of the Binary Large OBject in bytes.
   *
   * @return length of the BLOB in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long length () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Return a copy of the contents of the BLOB at the requested position.
   *
   * @param pos is the first byte of the blob to be extracted.
   * @param length is the number of consecutive bytes to be copied.
   * @return a byte array containing a portion of the BLOB
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public byte[] getBytes (long pos,
                                       int length) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Retrieve the entire BLOB as a stream.
   *
   * @return a stream containing the BLOB data
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public java.io.InputStream getBinaryStream () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /** 
   * Determine the byte position at which the given byte <code>pattern</code>
   * starts in the BLOB.  Begin search at <code>position</code> start.
   * Return -1 if the pattern does not appear in the BLOB.
   *
   * @param  pattern is the pattern to search for.
   * @param start is the position at which to begin searching.
   * @return the position at which the pattern appears, else -1.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long position (byte pattern[],
                                     long start) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /** 
   * Determine the byte position at which the given pattern 
   * <code>pattern</code> starts in the BLOB.  Begin search at position <code>start</code>.
   * Return -1 if the pattern does not appear in the BLOB.
   *
   * @param searchstr is the pattern to search for.
   * @param start is the position at which to begin searching.
   * @return the position at which the pattern appears, else -1.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long position (java.sql.Blob pattern,
                                     long start) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

    @Override
    public int setBytes(long pos, byte[] bytes) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void truncate(long len) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void free() throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
