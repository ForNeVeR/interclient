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
 * A factory for Connection and ServerManager objects.
 * An object that implements the DataSource interface will typically be
 * registered with a JNDI service provider.  A JDBC driver that is 
 * accessed via the DataSource API does not automatically register 
 * itself with the DriverManager.
 * <p>
 * A DataSource class implements the javax.sql.DataSource interface.
 * The javax.sql.DataSource interface provides a pair of getConnection()
 * methods that can be invoked directly by user-level application code.
 * A DataSource class should also implement the javax.naming.Referenceable
 * and java.io.Serializable interfaces. These interfaces make it possible for instances
 * of the DataSource class to be bound in a JNDI namespace. JNDI supports two
 * separate mechanisms for storing objects that are bound in a namespace. The first is
 * based on the javax.naming.Referenceable interface. The second approach is simply
 * to store a serializable Java object as a sequence of bytes. For maximum portability a
 * data source implementation should support both styles.
 * <p>
 * This DataSource class provides a set of properties that need
 * to be given values so that a connection to a particular data source can be made. These
 * properties, which follow the design pattern specified for JavaBeans TM components, are
 * usually set when a data source object is deployed. Examples of data source properties
 * include the location of the database server, the name of the database, the network protocol
 * to use to communicate with the server, and so on.
 * <p>
 * The JDBC 2.0 Standard Extension specifies a simple policy for assigning JNDI names to data sources.
 * All JDBC data sources should be registered in the jdbc naming subcontext of a JNDI
 * namespace, or in one of its child subcontexts. The parent of the jdbc subcontext is the
 * root naming context.
 * <p>
 * As an implementation of the javax.naming.Referenceable interface,
 * a data source object can provide a Reference to itself.
 * A Reference represents a way of recording address information about
 * objects which themselves are not directly bound to the
 * naming system. Such objects can implement the Referenceable interface
 * as a way for programs that use that object to
 * determine what its Reference is.
 * For example, when binding an object, if an object implements the Referenceable interface,
 * getReference() can be invoked on the object to get its Reference to use for binding.
 * <p>
 * The javax.naming.Referenceable interface contains a single method, getReference(),
 * which is called by JNDI to obtain a Reference for a data source object when
 * that object is bound in a JNDI naming service. The Reference object contains all of the
 * information needed to reconstruct the data source object when it is later retrieved from
 * JNDI.
 * <p>
 * The DataSource facility provides an alternative to the JDBC DriverManager, essentially
 * duplicating all of the driver manager’s useful functionality. Although, both mechanisms
 * may be used by the same application if desired, JavaSoft encourages developers to
 * regard the DriverManager as a legacy feature of the JDBC API. Applications should
 * use the DataSource API whenever possible.
 * A JDBC implementation that is accessed via the DataSource API is not automatically
 * registered with the DriverManager. The DriverManager, Driver, and
 * DriverPropertyInfo interfaces may be deprecated in the future.
 * <p>
 * Note that the getter and setter methods for a property are defined on the implementation
 * class and not in the DataSource interface. This creates some separation between the
 * management API for DataSource objects and the API used by applications. Applications
 * shouldn't need to access/change properties, but management tools can get at them
 * using introspection.
 *
 * <p><b>InterClient Notes:</b>
 * See <a href="../../../help/icConnectionProperties.html">DataSource and Connection Properties</a>
 * for a specification of the InterClient and JDBC Standard Extension
 * data source properties.
 * <p>
 * The example below registers an InterClient data source object with a JNDI naming service.
 * <pre>
 * interbase.interclient.DataSource dataSource = new interbase.interclient.DataSource ();
 * dataSource.setServerName (“pongo”);
 * dataSource.setDatabaseName (“/databases/employee.gdb”);
 * javax.naming.Context context = new javax.naming.InitialContext();
 * context.bind (“jdbc/EmployeeDB”, dataSource);
 * </pre>
 * The first line of code in the example creates a data source object.
 * The next two lines initialize the data source’s properties.
 * Then a Java object that references the initial JNDI naming
 * context is created by calling the InitialContext() constructor, which is provided by
 * JNDI. System properties (not shown) are used to tell JNDI the service provider to use.
 * The JNDI name space is hierarchical, similar to the directory structure of many file
 * systems. The data source object is bound to a logical JNDI name by calling
 * Context.bind().
 * In this case the JNDI name identifies a subcontext, “jdbc”, of the root
 * naming context and a logical name, “EmployeeDB”, within the jdbc subcontext. This
 * is all of the code required to deploy a data source object within JNDI.
 * This example is provided mainly for illustrative purposes. We expect
 * that developers or system administrators will normally use a GUI tool to deploy a data
 * source object.
 * <p>
 * Once a data source has been registered with JNDI, it can then be used by a JDBC application,
 * as is shown in the following example.
 * <pre>
 * javax.naming.Context context = new javax.naming.InitialContext ();
 * javax.sql.DataSource dataSource = (javax.sql.DataSource) context.lookup (“jdbc/EmployeeDB”);
 * java.sql.Connection con = dataSource.getConnection (“sysdba”, “masterkey”);
 * </pre>
 * The first line in the example creates a Java object that references the initial JNDI naming
 * context. Next, the initial naming context is used to do a lookup operation using the
 * logical name of the data source. The Context.lookup() method returns a reference to
 * a Java Object, which is narrowed to a javax.sql.DataSource object. In the last line,
 * the DataSource.getConnection() method is called to produce a database connection.
 *
 * @see ObjectFactory
 * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
 * @version Std Ext 0.7, JNDI 1.1.1
 **/
// CJL-IB6 changed reference to InterClient 2.0
public class DataSource implements javax.sql.DataSource,
                                   javax.naming.Referenceable,
                                   java.io.Serializable
{
  /**
   * @serial The time in seconds to wait for connections on this data source.
   *    This is not stored as part of datasource under JNDI.
   **/
  private int loginTimeout = 0;

  private java.io.PrintWriter logWriter_ = null;


  // ---------------------standard properties-----------------------------------
  /**
   * @serial The target database for this data source.
   **/
  private String databaseName = null;


  /**
   * @serial A data source name; used to name an
   *         underlying XADataSource, or ConnectionPoolDataSource
   *         when pooling of connections is done.
   **/
  private String dataSourceName = null;

  /**
   * @serial A description of this data source.
   **/
  private String description = null;

  /**
   * @serial The database protocol to use for a connection to this data source.
   **/
  private String networkProtocol = null;

  /**
   * @serial The user password to use for a connection to this data source.
   **/
  private String password = null;  // isc_dpb_password

  /**
   * @serial The port number where InterServer listens for connection requests to this data source.
   **/
  private int portNumber = 3060;

  /**
   * @serial The user's SQL role to use for a connection to this data source.
   **/
  private String roleName = null; // isc_dpb_sql_role_name

  /**
   * @serial The server where InterServer manages requests to this data source.
   **/
  private String serverName = null;

  /**
   * @serial The user name to use for a connection to this data source.
   **/
  private String user = null;  // isc_dpb_user_name

// CJL-IB6 add SQL Dialect to the mix
  /**
   * @serial The SQL Dialect to use for a connection to this data source.
   **/
  private int sqlDialect = 0;

// CJL-IB6 end change

  // -----------------non-standard properties-----------------------------------
  
  /**
   * @serial The character encoding to use for a connection to this data source.
   **/
  private String charSet = CharacterEncodings.NONE;  // isc_dpb_lc_ctype

  /**
   * @serial The InterBase server to be serviced by a ServerManager on this data source.
   *         If this property is null, <code>serverName</code> is used.
   **/
  private String serverManagerHost = null;

  /**
   * @serial The suggested number of cache pages to use for a connection to this data source.
   **/
  private int suggestedCachePages = 0;   // isc_dpb_num_buffers

  /**
   * @serial Should the database be swept clean of old record versions upon connection to this data source.
   **/
  private boolean sweepOnConnect = false; // isc_dpb_sweep

  /**
   * Create a data source with default property values.
   * No particular DatabaseName or other properties are associated with the data source.
   * <p>
   * Every Java Bean should provide a constructor with no arguments
   * since many beanboxes attempt to instantiate a bean by invoking
   * its no-argument constructor.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public DataSource () {} // constructor is used by the object factory

  /**
   * Gets the JDBC Standard Extension <code>databaseName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getDatabaseName () { return databaseName; }

  /**
   * Sets the JDBC Standard Extension <code>databaseName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setDatabaseName (String databaseName) { this.databaseName = databaseName; }

  /**
   * Gets the JDBC Standard Extension <code>dataSourceName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getDataSourceName () { return dataSourceName; }

  /**
   * Sets the JDBC Standard Extension <code>dataSourceName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setDataSourceName (String dataSourceName) { this.dataSourceName = dataSourceName; }

  /**
   * Gets the JDBC Standard Extension <code>description</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getDescription () { return description; }

  /**
   * Sets the JDBC Standard Extension <code>description</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setDescription (String description) { this.description = description; }

  /**
   * Gets the JDBC Standard Extension <code>networkProtocol</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getNetworkProtocol () { return networkProtocol; }

  /**
   * Sets the JDBC Standard Extension <code>networkProtocol</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setNetworkProtocol (String networkProtocol) { this.networkProtocol = networkProtocol; }

  /**
   * Gets the JDBC Standard Extension <code>password</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getPassword () { return password; }

  /**
   * Sets the JDBC Standard Extension <code>password</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setPassword (String password) { this.password = password; }

  /**
   * Gets the JDBC Standard Extension <code>portNumber</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getPortNumber () { return portNumber; }

  /**
   * Sets the JDBC Standard Extension <code>portNumber</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setPortNumber (int portNumber) { this.portNumber = portNumber; }

  /**
   * Gets the JDBC Standard Extension <code>roleName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getRoleName () { return roleName; }

  /**
   * Sets the JDBC Standard Extension <code>roleName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setRoleName (String roleName) { this.roleName = roleName; }

  /**
   * Gets the JDBC Standard Extension <code>serverName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getServerName () { return serverName; }

  /**
   * Sets the JDBC Standard Extension <code>serverName</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setServerName (String serverName) { this.serverName = serverName; }

  /**
   * Gets the JDBC Standard Extension <code>user</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getUser () { return user; }

  /**
   * Sets the JDBC Standard Extension <code>user</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setUser (String user) { this.user = user; }

  /**
   * Gets the <code>charSet</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getCharSet () { return charSet; }

  /**
   * Sets the <code>charSet</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setCharSet (String charSet) { this.charSet = charSet; }

  /**
   * Gets the <code>serverManagerHost</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getServerManagerHost () { return serverManagerHost; }

  /**
   * Sets the <code>serverManagerHost</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setServerManagerHost (String serverManagerHost) { this.serverManagerHost = serverManagerHost; }

  /**
   * Gets the <code>suggestedCachePages</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getSuggestedCachePages () { return suggestedCachePages; }

  /**
   * Sets the <code>suggestedCachePages</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setSuggestedCachePages (int suggestedCachePages) { this.suggestedCachePages = suggestedCachePages; }

  /**
   * Gets the <code>sweepOnConnect</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public boolean getSweepOnConnect () { return sweepOnConnect; }

  /**
   * Sets the <code>sweepOnConnect</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setSweepOnConnect (boolean sweepOnConnect) { this.sweepOnConnect = sweepOnConnect; }

  /**
   * Attempt to establish a database connection.
   *
   * @return a Connection to the database
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/

// CJL-IB6 SQL DIALECT support
  /**
   * Gets the <code>sqlDialect</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 2.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getSQLDialect () { return sqlDialect; }

  /**
   * Sets the <code>sqlDialect</code> property value.
   * See <a href="../../../help/icConnectionProperties.html">DataSource</a> properties.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setSQLDialect (int sqlDialect) { this.sqlDialect = sqlDialect; }

// CJL-IB6 end change

  public java.sql.Connection getConnection () throws java.sql.SQLException
  {
    return getConnection (user, password);
  }

  /**
   * Attempt to establish a database connection.
   *
   * @param user the database user on whose behalf the Connection is being made
   * @param password the user's password
   * @return  a Connection to the database
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public java.sql.Connection getConnection (String user,
                                            String password) throws java.sql.SQLException
  {
    if (networkProtocol != null &&
        !networkProtocol.equals ("jdbc:interbase:"))
      throw new java.sql.SQLException ("invalid network protocol");

    java.util.Properties properties = new java.util.Properties ();
    if (user != null)
      properties.put (ConnectionProperties.userKey__, user);
    if (password != null)
      properties.put (ConnectionProperties.passwordKey__, password);
    if (roleName != null)
      properties.put (ConnectionProperties.roleNameKey__, roleName);
    if (charSet != null)
      properties.put (ConnectionProperties.charSetKey__, charSet);
    if (sweepOnConnect)
      properties.put (ConnectionProperties.sweepOnConnectKey__, String.valueOf (sweepOnConnect));
    if (suggestedCachePages != 0)
      properties.put (ConnectionProperties.suggestedCachePagesKey__, String.valueOf (suggestedCachePages));

    return new Connection (loginTimeout*1000,
                           serverName,
                           portNumber,
                           databaseName,
                           properties);
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
    logWriter_ = out;
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
  public java.io.PrintWriter getLogWriter () throws java.sql.SQLException
  {
    return logWriter_;
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
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setLoginTimeout (int seconds) throws java.sql.SQLException
  {
    loginTimeout = seconds;
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
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getLoginTimeout () throws java.sql.SQLException
  {
    return loginTimeout;
  }

  /**
   * Creates a JNDI naming Reference of this data source.
   *
   * @return the non-null reference of this object
   * @throws javax.naming.NamingException if a naming exception was encountered while retrieving the reference
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public javax.naming.Reference getReference () throws javax.naming.NamingException
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

    javax.naming.Reference ref =
      new javax.naming.Reference (getClass ().getName (),
                                  "interbase.interclient.ObjectFactory",
                                  null);
    ref.add (new javax.naming.StringRefAddr ("databaseName", getDatabaseName ()));
    ref.add (new javax.naming.StringRefAddr ("dataSourceName", getDataSourceName ()));
    ref.add (new javax.naming.StringRefAddr ("description", getDescription ()));
    ref.add (new javax.naming.StringRefAddr ("networkProtocol", getNetworkProtocol ()));
    ref.add (new javax.naming.StringRefAddr ("portNumber", String.valueOf (getPortNumber ())));
    ref.add (new javax.naming.StringRefAddr ("serverName", getServerName ()));
    ref.add (new javax.naming.StringRefAddr ("user", getUser ()));  
    ref.add (new javax.naming.StringRefAddr ("password", getPassword ()));
    ref.add (new javax.naming.StringRefAddr ("roleName", getRoleName ()));
    ref.add (new javax.naming.StringRefAddr ("charSet", getCharSet ()));
    ref.add (new javax.naming.StringRefAddr ("sweepOnConnect", String.valueOf (getSweepOnConnect ())));
    ref.add (new javax.naming.StringRefAddr ("suggestedCachePages", String.valueOf (getSuggestedCachePages ())));
    ref.add (new javax.naming.StringRefAddr ("serverManagerHost", String.valueOf (getServerManagerHost ())));

    return ref;
  }

  synchronized void log (Object object)
  {
    if (logWriter_ != null)
      logWriter_.print (object);
  }

  synchronized void logln (Object object)
  {
    if (logWriter_ != null)
      logWriter_.println (object);
  }

  // ------------------- InterClient Extensions -------------------------

  /**
   * Creates a special attachment to the datasource for the purpose
   * of managing an InterBase server.
   * <p>
   * The server manager attachment is to an InterServer host,
   * but a target InterBase host may optionally be specified using the
   * <code>serverManagerHost</code> property.
   * If this property is null, then it is assumed that the InterBase host
   * to be serviced is the same as the InterServer host.
   * <p>
   * Many server manager operations may require you to pass the SYSDBA user and password.
   * <p>
   * <b>Note:</b> This does not start the InterBase server.
   * The InterBase server must be started manually, or by using
   * {@link ServerManager#startInterBase(int,int) ServerManager.startInterBase(defaultCachePages, defaultPageSize)}.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a server access error occurs
   * @return a service connection to the interserver host in the form of a ServerManager object
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public ServerManager getServerManager () throws java.sql.SQLException
  {
    return getServerManager (user, password);
  }

  /**
   * Attempts to create a special attachment to the datasource for the purpose
   * of managing an InterBase server.
   * The user and password to be used while servicing the InterBase server are given.
   * See {@link #getServerManager() getServerManager()} for more details.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a server access error occurs
   * @return a service connection to the interserver host in the form of a ServerManager object
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public ServerManager getServerManager (String user,
                                         String password) throws java.sql.SQLException
  {
    // java.net.Socket.setSoTimeout() is in milliseconds
    ServerManager serverManager = new ServerManager (loginTimeout*1000,
                                                     serverName,        // interserver host
                                                     serverManagerHost, // interbase server
                                                     portNumber,
                                                     user,
                                                     password,
                                                     roleName);

    return serverManager;
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
