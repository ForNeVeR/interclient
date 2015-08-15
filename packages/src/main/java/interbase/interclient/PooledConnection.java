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
 * A PooledConnection object is a connection object that provides
 * hooks for connection pool management.
 *
 * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
 * @version Std Ext 0.7
 **/
public class PooledConnection implements javax.sql.PooledConnection
{

  /**
   * Create an object handle for a database connection.
   *
   * @return  a Connection object
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public java.sql.Connection getConnection () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
      
  /**
   * Close the database connection.
   *
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void close () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
      
  /**
   * Add an event listener.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void addConnectionEventListener (javax.sql.ConnectionEventListener listener)
  {
    // !!!
  }

  /**
   * Remove an event listener.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void removeConnectionEventListener (javax.sql.ConnectionEventListener listener)
  {
    // !!!
  }
}





