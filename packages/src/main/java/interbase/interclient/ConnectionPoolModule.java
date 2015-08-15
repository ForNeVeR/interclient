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


import java.sql.*;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * The ConnectionPoolModule class implements the javax.sql.DataSource interface
 * so that an instance of ConnectionPoolModule can be registered with JNDI as a JDBC
 * DataSource.  Instances of the ConnectionPoolModule class will be used directly by
 * applications to create database connections.
 * ConnectionPoolModule supports one standard data source property, DataSourceName,
 * which is used to locate a connection pool data source when one is needed. A
 * ConnectionPoolDataSource is only used when there is a miss in the connection
 * cache. In this case, a second JNDI lookup() operation is done to produce a ConnectionPoolDataSource
 * object. The ConnectionPoolDataSource object is used to create
 * a new PooledConnection object.
 *
 * @since <font color=red>Extension, proposed for future release, not yet supported</font>
 * @version Std Ext 0.7
 **/
public class ConnectionPoolModule implements javax.sql.DataSource,
                                             javax.naming.Referenceable,
                                             java.io.Serializable
{
  // The private ConnectionCache field is declared static so that connections created with
  // different DataSource objects can be cached in the same physical connection pool.
  private static ConnectionCache cache = null;
  private javax.sql.ConnectionPoolDataSource cpds = null;
  private String dataSourceName = null;

  static {
    // Code to initialize the connection cache goes here.
    // flesh out details
  }
  // flesh out details

  /**
   * Attempt to establish a database connection.
   *
   * @return  a Connection to the database
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public java.sql.Connection getConnection () throws java.sql.SQLException
  {
    // vendor specific code to create a JDBC Connection goes here
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   *
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public java.sql.Connection getConnection (String user,
                                            String password) throws java.sql.SQLException
  {
    // flesh out details
    javax.sql.PooledConnection pc = cache.lookup ();
    if (pc == null) {
      // There was a miss in the cache, so do a JNDI lookup to
      // get a connection pool data source.
      try {
        javax.naming.Context ctx = new javax.naming.InitialContext ();
        cpds = (javax.sql.ConnectionPoolDataSource) ctx.lookup (getDataSourceName ());
      }
      catch (javax.naming.NamingException e) {
        throw new java.sql.SQLException ("!!!");
      }
      pc = cpds.getPooledConnection (user, password);
    }
    pc.addConnectionEventListener (cache);
    // flesh out details
    return pc.getConnection ();
  }
  
  /**
   *
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public String getDataSourceName() { return dataSourceName; }

  /**
   *
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void setDataSourceName (String s)
  {
    // flesh out details
  }

  /**
   * Get the log writer for this data source.
   * <p>
   * The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class.  When a DataSource object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @return the log writer for this data source, null if disabled
   * @throws java.sql.SQLException if a database-access error occurs.  
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public java.io.PrintWriter getLogWriter() throws java.sql.SQLException
  {
    // vendor specific code goes here
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Set the log writer for this data source.
   * <p>
   * This method provides a way to register a character
   * stream to which tracing and error logging information will be written by a JDBC implementation.
   * This allows for DataSource specific tracing. If one wants all Data-Sources
   * to use the same log stream, one must register the stream with each
   * DataSource object individually. Log messages written to a DataSource specific log
   * stream are not written to the log stream maintained by the DriverManager. When a
   * DataSource object is created the log writer is initially null, in other words, logging is
   * disabled.
   * <p>
   * The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class. When a DataSource object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @param out the new log writer; to disable, set to null
   * @throws java.sql.SQLException if a database-access error occurs.  
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void setLogWriter (java.io.PrintWriter out) throws java.sql.SQLException
  {
    // vendor specific code goes here
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  
  /**
   * Sets the maximum time in seconds that this data source will wait
   * while attempting to connect to a database.  A value of zero
   * specifies that the timeout is the default system timeout 
   * if there is one; otherwise it specifies that there is no timeout.
   * When a DataSource object is created the login timeout is
   * initially zero.
   *
   * @param seconds the data source login time limit
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void setLoginTimeout(int seconds) throws java.sql.SQLException
  {
    // vendor specific code goes here
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Gets the maximum time in seconds that this data source can wait
   * while attempting to connect to a database.  A value of zero
   * means that the timeout is the default system timeout 
   * if there is one; otherwise it means that there is no timeout.
   * When a DataSource object is created the login timeout is
   * initially zero.
   *
   * @return the data source login time limit
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public int getLoginTimeout () throws java.sql.SQLException
  {
    // vendor specific code goes here
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Creates a JNDI naming Reference of this data source.
   *
   * @return the non-null reference of this object
   * @throws javax.naming.NamingException if a naming exception was encountered while retrieving the reference
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public javax.naming.Reference getReference () throws javax.naming.NamingException
  {
    // This method creates a new Reference object to represent this data source.
    // The class name of the data source object is
    // saved in the Reference, so that an object factory will know that it should create an instance
    // of that class when a lookup operation is performed. The class
    // name of the object factory, interbase.interclient.ObjectFactory,
    // is also stored in the reference.
    // This is not required by JNDI, but is recommend in
    // practice. JNDI will always use the object factory class specified in the reference when
    // reconstructing an object, if a class name has been specified. See the JNDI SPI documentation
    // for further details on this topic, and for a complete description of the Reference
    // and StringRefAddr classes.
    //
    // This DataSource class provides several standard JDBC properties.
    // The names and values of the data source properties are also stored
    // in the reference using the StringRefAddr class.
    // This is all the information needed to reconstruct a DataSource object.

    javax.naming.Reference ref = new javax.naming.Reference (getClass ().getName (),
                                                             "interbase.interclient.ObjectFactory",
                                                             null);
    ref.add (new javax.naming.StringRefAddr ("dataSourceName", getDataSourceName ()));
    // !!! complete this...
    return ref;
  }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
