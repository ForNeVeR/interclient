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

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.*;
import java.sql.SQLException;

/**
 * Represents an SQL 3 Character Large Object (CLOb).
 * By default, a Clob is a transaction duration reference to a
 * character large object.
 *
 * @since <font color=red>JDBC 2, not yet supported</font>
 **/
final public class Clob implements java.sql.Clob
{

  /**
   * The length of the Character Large Object in characters.
   *
   * @return length of the Clob in characters
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long length () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Return copy of the substring of the Clob at the requested position.
   *
   * @param pos is the first character of the substring to be extracted.
   * @param length is the number of consecutive character to be copied.
   * @return a byte array containing a substring of the CLOB
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public String getSubString (long pos,
                                           int length) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Get the Clob contents as a Unicode stream.
   *
   * @return a Unicode stream containing the CLOB data
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public java.io.Reader getCharacterStream () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Get the Clob contents as an ascii stream.
   *
   * @return an ascii stream containing the Clob data
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public java.io.InputStream getAsciiStream () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /** 
   * Determine the character position at which the given substring 
   * searchstr appears in the CLOB.  Begin search at position start.
   * Return -1 if the substring does not appear in the CLOB.
   *
   * @param searchstr is the substring to search for.
   * @param start is the position at which to begin searching.
   * @return the position at which the substring appears, else -1.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long position (String searchstr,
                                     long start) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /** 
   * Determine the character position at which the given substring 
   * searchstr appears in the CLOB.  Begin search at position start.
   * Return -1 if the substring does not appear in the CLOB.
   *
   * @param searchstr is the substring to search for.
   * @param start is the position at which to begin searching.
   * @return the position at which the substring appears, else -1.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public long position (java.sql.Clob searchstr,
                                     long start) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

    @Override
    public int setString(long pos, String str) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void truncate(long len) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void free() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
