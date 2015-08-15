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

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A ConnectionPoolDataSource object is a factory for PooledConnection objects.
 * An object that implements this interface will typically be
 * registered with a JNDI service.
 *
 * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
 * @version Std Ext 0.7
 **/
public class ConnectionPoolDataSource implements javax.sql.ConnectionPoolDataSource
{

  /**
   * Attempt to establish a database connection.
   *
   * @return a Connection to the database
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public javax.sql.PooledConnection getPooledConnection () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
      
  /**
   * Attempt to establish a database connection.
   *
   * @param user the database user on whose behalf the Connection is being made
   * @param password the user's password
   * @return  a Connection to the database
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public javax.sql.PooledConnection getPooledConnection (String user,
                                                         String password) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
      
  /**
   * Get the log writer for this data source.
   *
   * The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class.  When a data source object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @return the log writer for this data source, null if disabled
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public java.io.PrintWriter getLogWriter () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Set the log writer for this data source.
   * <p>
   * The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class. When a data source object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @param out the new log writer; to disable, set to null
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public void setLogWriter (java.io.PrintWriter out) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Sets the maximum time in seconds that this data source will wait
   * while attempting to connect to a database.  A value of zero
   * specifies that the timeout is the default system timeout 
   * if there is one; otherwise it specifies that there is no timeout.
   * When a data source object is created the login timeout is
   * initially zero.
   *
   * @param seconds the data source login time limit
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public void setLoginTimeout (int seconds) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
     
  /**
   * Gets the maximum time in seconds that this data source can wait
   * while attempting to connect to a database.  A value of zero
   * means that the timeout is the default system timeout 
   * if there is one; otherwise it means that there is no timeout.
   * When a data source object is created the login timeout is
   * initially zero.
   *
   * @return the data source login time limit
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
  synchronized public int getLoginTimeout () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
