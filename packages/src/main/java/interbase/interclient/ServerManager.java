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

// The services-based API allows for requests to be batched as well, however
// there is a restriction:
// information and action requests can not be batched together.
//       
// For example, using the conventional API, you could specify:
//    isc_database_info (isc_info_dpb_sweep .... isc_info_dpb_ods_ver)
//
// To do this via the services API it would be (or something like this):
//    isc_service_query (isc_info_svc_ods_ver ...);
//    isc_service_start (isc_action_svc_fix, isc_info_spb_sweep...)
//
// You can only batch server queries (isc_info_svc) calls.  All server actions
// must be sent serially.

// Currently, the service based operations are centered around 2 areas:
//
// 1.  Retrieving information about a particular server (db connections, config
// settings, etc).
// 2.  Performing some operation (GBAK, GFIX, etc.)
//
// While there are some (not many) overlaps between the connection-based APIs
// and the service-based APIs, the intention (currently) is not to deprecate
// the connection-based APIs.
//
// As far as making it easier to retrieve database header (and other
// information that doesn't really need a connection), the conventional API and
// service-based API have about the same overhead.  In some cases, the
// services-based API has more overhead.
//
// Though it would be nice to have 1 entrypoint for retrieving particular
// pieces of information (which is where this seems to be heading), we need to
// keep the following in mind:  people are using the conventional API now, the
// services-based API is completely new.  If we add new items for both, then we
// increase the complexity (since the services manager is not part of the
// normal database subsystem) since the item will need to be maintained in a
// few places.

// Above comments from Mark Duquette

// In addition to the InterBase service which operates
// on InterBase databases, a guardian process
// known as the InterBase administration service exists
// for administrating the server as a whole.
// <p>
// The InterBase administration service can be used for starting
// and stopping the InterBase service, or for operating
// on database files directly.
// <p>
// Only sysdba can obtain a Server object, thereby administering
// an InterBase server; other users access database information
// thru a Connection or DatabaseMetaData and are not allowed
// Server access.

// The following service calls populate the response buffer retrieved
// by isc_service_query()
//    isc_action_svc_backup
//    isc_action_svc_restore
//    isc_action_svc_repair
//    isc_action_svc_db_stats
//    isc_action_svc_get_ib_log
//    isc_action_svc_display_user

/**
 * Provides a session for servicing an InterBase server.
 * <p>
 * A server manager object is obtained thru a {@link DataSource} using
 * {@link DataSource#getServerManager() DataSource.getServerManager()}.
 * The DataSource property named <code>serverManagerHost</code> must be
 * set using
 * {@link DataSource#setServerManagerHost(String) DataSource.setServerManagerHost(hostName)}.
 * See <a href="../../../help/icConnectionProperties.html">DataSource Properties</a>
 * for a list of other DataSource and Connection properties.
 * <p>
 * The ServerManager may be used for setting persistent database properties,
 * and for making service requests on databases and the server as a whole.
 * Use <a href="../../../help/icConnectionProperties.html">connection properties</a> for
 * configuring transient properties of a connection.
 * Use the {@link DatabaseMetaData DatabaseMetaData} class for
 * querying persistent database properties in general.
 * <p>
 * See the
 * <a href="../../../help/icExtensions.html">InterClient Extension API</a>
 * for a complete list of available InterBase extensions to JDBC.
 *
 * @since <font color=red>Extension, proposed for InterClient 3.0</font>
 **/
// CJL-IB6 changed reference to InterClient 2.0
final public class ServerManager
{
  // Server side session object reference
  // Not currently used.
  private int sessionRef_ = 0;

  private java.sql.SQLWarning sqlWarnings_ = null;
  private boolean open_ = true;
  private JDBCNet jdbcNet_;
  private String interserverHost_;
  private String interbaseHost_;
  private int port_;
  private String user_;
  private String role_;

  // for DataSource.getServerManager
  // The global role is used be used by gbak -role, and gsec -role
  ServerManager (int socketTimeoutMilliseconds,
                 String interserverHost,
                 String interbaseHost,
                 int port,
                 String user,
                 String password,
                 String role) throws java.sql.SQLException
  {
    interserverHost_ = interserverHost;
    interbaseHost_ = interbaseHost_;
    port_= port;
    user_ = user;
    role_ = role;

    connect (socketTimeoutMilliseconds, password);
  }

  private void checkForClosedConnection () throws java.sql.SQLException
  {
    if (!open_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__server_connection_closed__);
  }

  // !!! think about getting some code-reuse with the Connection class
  private void connect (int socketTimeoutMilliseconds, String password) throws java.sql.SQLException
  {
    try {
      jdbcNet_ = new JDBCNet (socketTimeoutMilliseconds,
                              interserverHost_,
                              port_,
                              sun.io.ByteToCharConverter.getConverter ("8859_1"),
                              sun.io.CharToByteConverter.getConverter ("8859_1"));
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__, 
						  "8859_1");
    }
    java.util.Date now = new java.util.Date ();
    try {
      remote_ATTACH_SERVER_MANAGER (password);
    }
    catch (java.sql.SQLException e) {
      try { jdbcNet_.disconnectSocket (); } catch (java.sql.SQLException e2) {}
      throw e;
    }
  }

  private void send_properties (MessageBufferOutputStream sendMsg,
				String user,
                                String password) throws java.sql.SQLException
  {
    sendMsg.writeByte (2); // properties.size ()
    sendMsg.writeLDSQLText ("user");
    byte[] encryptedUserName = jdbcNet_.crypter_.stringCrypt (user.toUpperCase ());
    sendMsg.writeLDBytes (encryptedUserName);
    sendMsg.writeLDSQLText ("password");
    byte[] encryptedPassword = jdbcNet_.crypter_.stringCrypt (password);
    sendMsg.writeLDBytes (encryptedPassword);
  }

  private void remote_ATTACH_SERVER_MANAGER (String password) throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.ATTACH_SERVER_MANAGER__);
    send_properties (sendMsg, user_, password);
    sendMsg.writeShort (jdbcNet_.socketTimeout_);  // in milliseconds, not used

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      sessionRef_ = recvMsg.readInt ();
      setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  //---------------- Getting InterServer Version Information--------------------
  
  /**
   * What's the major version number for InterServer.
   *
   * @see DatabaseMetaData#getInterServerMajorVersion
   * @see Driver#getMajorVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer major version.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerMajorVersion ()
  {
    return jdbcNet_.serverVersionInformation_.majorVersion_;
  }

  /**
   * What's the minor version number for InterServer.
   *
   * @see DatabaseMetaData#getInterServerMinorVersion
   * @see Driver#getMinorVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer minor version.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerMinorVersion ()
  {
    return jdbcNet_.serverVersionInformation_.minorVersion_;
  }

  /**
   * What's the build number for InterServer.
   *
   * @see Driver#getBuildNumber
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer build number.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerBuildNumber ()
  {
    return jdbcNet_.serverVersionInformation_.buildNumber_;
  }

  /**
   * Gets the build certification level for this version
   * of InterServer.
   *
   * The InterServer certification levels are as follows:
   * <ul>
   * <li><b>{@link Driver#testBuild Driver.testBuild}</b>
   * Test builds are experimental and uncertified with
   * no level of quality assurance whatsoever.
   * <li><b>{@link Driver#betaBuild Driver.betaBuild}</b>
   * Beta builds are certified for quality assurance,
   * but remain experimental.
   * <li><b>{@link Driver#finalBuild Driver.finalBuild}</b>
   * The highest level of build certification.
   * A final build is certified for quality assurance,
   * but is not experimental.
   * </ul>
   * These are not JDBC certification levels, rather they
   * represent a level of quality assurance for a particular
   * build of InterServer.  So, for example, an InterServer
   * version 2.0.1 may be a test build, and 2.0.21 may be a beta build,
   * and 2.0.41 may be the final build.
   * <p>
   * The progression from test, beta, to final build is
   * not static functionally.  That is, new functionality
   * may be added to a beta build that does not exist in a
   * test build, and likewise, there may be new functionality
   * in a final build which does not exist in a beta build
   * of the same version.
   *
   * @see Driver#getBuildCertificationLevel
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return the build certification level
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerBuildCertificationLevel ()
  {
    return jdbcNet_.serverVersionInformation_.buildLevel_;
  }

  /**
   * Get the JDBC/Net protocol used by InterServer.
   *
   * @see DatabaseMetaData#getInterServerJDBCNetProtocolVersion
   * @see Driver#getJDBCNetProtocolVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerJDBCNetProtocolVersion ()
  {
    return jdbcNet_.serverVersionInformation_.jdbcNetProtocolVersion_;
  }

  /**
   * Get the expiration date for the server side JDBC middleware server (InterServer).
   *
   * @see DatabaseMetaData#getInterServerExpirationDate
   * @see Driver#getExpirationDate
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public java.util.Date getInterServerExpirationDate ()
  {
    return jdbcNet_.serverVersionInformation_.expirationDate_;
  }

  /**
   * Get the JDBC/Net middleware server (InterServer) port.
   *
   * @see DatabaseMetaData#getInterServerPort()
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerPort ()
  {
    return port_;
  }

  //----------------Getting server-wide information-----------------------------

  /**
   * What's the version string for the InterBase server.
   *
   * @see DatabaseMetaData#getDatabaseProductVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @return InterBase version string
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String getInterBaseVersion () throws java.sql.SQLException
  {
    // isc_service_query()/isc_info_svc_server_version
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets the names of databases with current connections.
   * This includes both C and Java client connections.
   *
   * @return an array of database names
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String[] getDatabasesInUse () throws java.sql.SQLException
  {
    // isc_service_query()/isc_info_svc_svr_db_info
    //   isc_spb_num_db
    //   isc_spb_dbname
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Returns the total number of database connections to the InterBase server.
   * This includes both C and Java client connections.
   *
   * @return the total number of InterBase connections on server.
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getNumberOfInterBaseConnectionsInUse () throws java.sql.SQLException
  {
    // isc_service_query()/isc_info_svc_svr_db_info
    //   isc_spb_num_att
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Returns registered user information as a map from username to a userInformation array object.
   * Users are registered in the InterBase <code>isc4.gdb</code>.
   * This represents <code>gsec display</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * The user information array object for each user in the map is a 5-tuple containing
   * <p>
   * <ul>
   * <li>firstName
   * <li>middleName
   * <li>lastName
   * <li>unixUserId
   * <li>unixGroupId
   * </ul>
   * <p>
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for for InterClient 2.0, not yet supported</font>
   **/
  synchronized public java.util.Map getRegisteredUsers () throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_display_users/*
    // followed by
    // isc_service_query()/isc_info_svc_get_users
    //   isc_spb_username
    //   isc_spb_firstname
    //   isc_spb_middlename
    //   isc_spb_lastname
    //   isc_spb_userid
    //   isc_spb_groupid
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets a continuous stream of InterBase lock activity from the server.
   * This represents <code>iblockpr</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Parameter <code>options</code> maps the following string-based keys:
   * <i>TBD</i>.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @return iblockpr info as an ascii stream.
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   **/
  synchronized public java.io.InputStream getLockActivityStream (java.util.Map options) throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_lock_stats
    //   isc_spb_lck_sample
    //   isc_spb_lck_secs
    //   isc_spb_options
    //     isc_spb_lck_contents
    //     isc_spb_lck_summary
    //     isc_spb_lck_wait
    //     isc_spb_lck_stats
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  //------------------- server-wide service requests ---------------------------

  /**
   * Start the InterBase service to allow for database connections.
   * This represents <code>ibmgr -start</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * <code>defaultPageSize</code> is the page size that will be used
   * by the InterBase server when a new database is created.
   * This affects the page size of a database created with the
   * {@link #createDatabase(String, java.util.Map) createDatabase} method.
   * <p>
   * <code>defaultCachedPages</code>
   * suggests the number of cached pages the InterBase server will use for
   * database attachments.
   * <p>
   * Database cache is the number of memory pages reserved for each attached
   * database. If the figure is set high enough to accommodate the page requirements 
   * for all attached databases, overall performance is maximized because all
   * database activity can be handled in physical RAM rather than having it swapped
   * to disk. If too many pages are reserved, however, and you have many databases
   * running simultaneously, your request may exceed the amount of physical RAM
   * available to the system. If that happens, some of your operations would be
   * swapped to disk as the operating system tries to manage the excessive demands of
   * your databases and the needs of other running applications.
   * <p>
   * On Classic, the default number of pages cached is 75 for each connection.
   * The minimum is 50 pages.
   * <p>
   * On SuperServer, a single page buffer cache is allocated per database,
   * and shared between connections.  The default allocation is 256 cached pages.
   * <p>
   * There are 3 ways to programmatically specify the number of cache pages:
   * <ul>
   * <li> <b>Server-wide default:</b>
   *      The default number of cache buffers to use for all database attachments.
   *      <br><b>Note:</b> This value is NOT stamped onto the database header page
   *      by {@link #createDatabase(String, java.util.Map) createDatabase}.
   *      If this default is not set, the default is 256 for SuperServer and 75
   *      for Classic.
   * <li> <b>Database-wide:</b>
   *      The number of cache buffers to use for attachments to a particular database
   *      may be stamped onto the database header page using
   *      {@link #setDatabaseCachePages(String, int) setDatabaseCachePages}.
   *      By default, {@link #createDatabase(String, java.util.Map) createDatabase}
   *      stamps a zero onto the database header page.
   *      If this value is non-zero, it takes precedence over both the server-wide and
   *      connection-wide settings.
   * <li> <b>Connection-wide:</b>
   *      The number of cache pages may be set at connection-time using
   *      the <code>suggestedCachePages</code> connection property, or
   *      {@link DataSource#setSuggestedCachePages DataSource.setSuggestedCachePages},
   *      but this value is transient and not stamped onto the database header page.
   *      This transient setting takes precedence over the server-wide default, 
   *      but not any database-wide setting.
   * </ul>
   * <p>
   * The number of cached database pages may also be set manually in the
   * server's <code>ibconfig</code> file by
   * assigning the configuration parameter <code>DATABASE_CACHE_PAGES</code>.
   * This manual server-wide setting takes precedence over any programmatic server-wide
   * setting, but is overriden by the database-wide and connection-wide programmatic
   * settings.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @docauthor Charlie Caro
   * @docauthor Balasubramanian Sriram
   * @docauthor Paul Ostler
   * @see DatabaseMetaData#getActualCachePagesInUse
   * @see DatabaseMetaData#getPersistentDatabaseCachePages
   * @see #stopInterBase
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
  synchronized public void startInterBase (int defaultCachedPages,
                                           int defaultPageSize) throws java.sql.SQLException
  {
    // No method for getting this yet, but
    //   Mark D. says it will probably be added eventually.
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Stop the InterBase service.
   * This represents <code>ibmgr -shut</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * This command rolls back all active transactions and forces immediate
   * shutdown of the server.  Use with caution.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @see #startInterBase
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
  synchronized public void stopInterBase () throws java.sql.SQLException
  { 
    // No method for getting this yet, but
    //   Mark D. says it will probably be added eventually.
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Registers a user with the InterBase server.
   * Registered users are stored in <code>isc4.gdb</code>.
   * This represents <code>gsec add</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * <code>userInformation</code> maps any of the following keys
   * to String or Integer objects:
   * <p>
   * <dl>
   * <dt> <code>firstName</code>
   * <dd> This represents  <code>gsec add -fname</code> functionality.
   *      The first name of the user being added.
   * <dt> <code>middleName</code>
   * <dd> This represents  <code>gsec add -mname</code> functionality.
   *      The middle name of the user being added.
   * <dt> <code>lastName</code>
   * <dd> This represents  <code>gsec add -lname</code> functionality.
   *      The last name of the user being added.
   * <dt> <code>unixUserId</code>
   * <dd> An Integer switch representing <code>gsec add -uid</code> functionality.
   * <dt> <code>unixGroupId</code>
   * <dd> An Integer switch representing <code>gsec add -gid</code> functionality.
   * </dl>
   * <p>
   * All such properties are optional.
   * Furthermore, the <code>userInformation</code> map itself may be null.
   * <p>
   * When adding users to the security database, the only information
   * required is the username and password and are restricted to ASCII characters.
   * The information for first, middle, and last names are stored in Unicode.
   * <p>
   * By default, only SYSDBA is allowed to add user information in the security database,
   * however, no checks are made by this API to enforce this.
   * Since this is strictly enforced at the database level,
   * it is possible for SYSDBA to grant update priviledges to those fields
   * that can be updatable by end users.
   * <p>
   * The SQL role name used for this operation is the <code>roleName</code> property
   * inherited from the {@link DataSource} that produced this ServerManager.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void addUser (String user,
				    String password,
                                    java.util.Map userInformation) throws java.sql.SQLException
  {
    // isc_add_user()
    // isc_service_start()/isc_action_svc_add_user
    //   isc_spb_sql_role_name
    //   isc_spb_sec_userid
    //   isc_spb_sec_groupid
    //   isc_spb_sec_username
    //   isc_spb_sec_password
    //   isc_spb_sec_groupname
    //   isc_spb_sec_firstname
    //   isc_spb_sec_middlename
    //   isc_spb_sec_lastname
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Removes a registered user.
   * Registered users are stored in <code>isc4.gdb</code>.
   * This represents <code>gsec delete</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Once a user has been removed from the security database,
   * you will need to create a new user record using {@link #addUser addUser}
   * before that user can connect to the server.
   * <p>
   * By default, only SYSDBA is allowed to remove user information in the security
   * database, however, no checks are made by this API to enforce this.
   * This is strictly enforced at the database level.
   * This would allow the SYSDBA to grant update priviledges to those fields
   * that can be updatable by end-users.
   * <p>
   * The SQL role name used for this operation is the <code>roleName</code> property
   * inherited from the {@link DataSource} that produced this ServerManager.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void deleteUser (String user) throws java.sql.SQLException
  {
    // isc_delete_user()
    // isc_service_start()/isc_action_svc_delete_user
    //   isc_spb_sql_role_name
    //   isc_spb_sec_username
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Change the user information for a registered user.
   * Registered users are stored in <code>isc4.gdb</code>.
   * This represents <code>gsec modify</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * <code>newUserInformation</code> maps any of the following keys
   * to String or Integer objects:
   * <p>
   * <dl>
   * <dt> <code>password</code>
   * <dd> This represents  <code>gsec modify -pw</code> functionality.
   *      The new password of the user being modified.
   * <dt> <code>firstName</code>
   * <dd> This represents  <code>gsec modify -fname</code> functionality.
   *      The new first name of the user being modified.
   * <dt> <code>middleName</code>
   * <dd> This represents  <code>gsec modify -mname</code> functionality.
   *      The new middle name of the user being modified.
   * <dt> <code>lastName</code>
   * <dd> This represents  <code>gsec modify -lname</code> functionality.
   *      The new last name of the user being modified.
   * <dt> <code>unixUserId</code>
   * <dd> An Integer switch representing <code>gsec modify -uid</code> functionality.
   * <dt> <code>unixGroupId</code>
   * <dd> An Integer switch representing <code>gsec modify -gid</code> functionality.
   * </dl>
   * <p>
   * Only information that has a corresponding parameter will be modified.
   * To change the user name, that user must be removed from the security
   * database and then re-added.
   * <p>
   * By default only SYSDBA is allowed to modify user information in the security database.
   * However, no checks are made by this API to enforce this.
   * This is strictly enforced at the database level.
   * This would allow SYSDBA to grant update priviledges to those fields that can
   * be updatable by end-users.
   * <p>
   * The SQL role name used for this operation is the <code>roleName</code> property
   * inherited from the {@link DataSource} that produced this ServerManager.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void modifyUser (String user,
				       java.util.Map newUserInformation) throws java.sql.SQLException
  {
    // isc_modify_user()
    // isc_service_start()/isc_action_svc_modify_user
    //   isc_spb_sql_role_name
    //   isc_spb_sec_userid
    //   isc_spb_sec_groupid
    //   isc_spb_sec_password
    //   isc_spb_sec_groupname
    //   isc_spb_sec_firstname
    //   isc_spb_sec_middlename
    //   isc_spb_sec_lastname
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Change the password for a registered user.
   * <p>
   * This method is less general than
   * {@link #modifyUser(String,java.util.Map) modifyUser(user, newUserInformation)},
   * but is convenient for the common task of changing a user's password.
   * 
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void modifyUser (String user,
				       String newPassword) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  //------------------------setting persistent database properties--------------
  
  /**
   * Specify whether or not space is reserved on each newly created database page
   * for anticipated record versions.  True by default.
   * <p>
   * This represents <code>gfix -use full</code>, and
   * <code>gfix -use reserve</code> functionality.
   * <p>
   * By default, an 80% fill ratio is used for newly created pages.
   * If space reservation is disabled, then a 100% fill ratio is used.
   * <p>
   * The default is to reserve space for back versions in a database. 
   * It's only if the database is mostly archival 
   * (read only loosely-speaking) that no 
   * reserve space is recommended.
   *
   * @see DatabaseMetaData#reservingSpaceForVersioning
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void reserveSpaceForVersioning (String database, boolean reserveSpace) throws java.sql.SQLException
  {
    // isc_dpb_no_reserve
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_prp_reserve_space
    //   isc_spb_prp_use_all_space
    //   isc_spb_dbname
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Sets the number of cache page buffers to use for all connections to the
   * specified <code>database</code>.
   * This is a persistent property of the database and is stamped
   * onto the database header page.
   * This represents <code>gfix -buffers</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * This persistent property of the database overrides
   * the <code>suggestedCachePages</code> connection property (see
   * <a href="../../../help/icConnectionProperties.html">Connection Properties</a>),
   * and also overrides the server-wide default set by
   * <code>DATABASE_CACHE_PAGES</code> in the InterBase
   * <code>ibconfig</code> startup file or by
   * {@link #startInterBase(int,int) startInterBase(defaultCachePages, defaultPageSize)}.
   * <p>
   * Upon connection, if a persistent database-wide cache pages property is not set,
   * then the connection-wide setting is used if specified on connect,
   * otherwise the server-wide default is used.
   * <p>
   * By default,
   * {@link #createDatabase(String, java.util.Map) createDatabase}
   * stamps a zero onto the database header page.
   * If this value is non-zero, it takes precedence over both the server-wide and
   * connection-wide settings.  A connection-wide setting may be established using
   * the <code>suggestedCachePages</code> connection property, see
   * <a href="../../../help/icConnectionProperties.html">Connection Properties</a>,
   * or by {@link DataSource#setSuggestedCachePages DataSource.setSuggestedCachePages}.
   * A server-wide setting may be established using
   * <code>DATABASE_CACHE_PAGES</code> in the InterBase
   * <code>ibconfig</code> startup file or by
   * {@link #startInterBase(int,int) startInterBase(defaultCachedPages, defaultPageSize)}.
   * <p>
   * The SuperServer architecture allocates a separate page buffer cache per database.
   * <p>
   * There is an absolute limitation of
   * MAX_PAGE_BUFFERS (65535) pages imposed by InterBase code.
   * So the cache memory size for a database cannot go beyond a maximum of
   * MAX_PAGE_BUFFERS*PageSize bytes which amounts to 512MB for an 8K page size.
   * 8K here is the maxiumum database page size currently allowed.
   * If this property is 0 or unspecified, and there is no server-wide or database-wide default set, the default
   * pages used is 256 cache pages.
   *
   * @see DatabaseMetaData#getActualCachePagesInUse
   * @see DatabaseMetaData#getPersistentDatabaseCachePages
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setDatabaseCachePages (String database, int numCachePages) throws java.sql.SQLException
  {
    // isc_dpb_set_page_buffers
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    //   isc_spb_prp_page_buffers
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Configure the database as read/write or read-only.
   * True by default.
   * This is a persistent property of the database and is stamped
   * onto the database header page.
   * <code>gfix</code> flag for this functionality to be determined.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * By default, a database is read/write.
   *
   * @see DatabaseMetaData#isDatabaseReadWrite
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setDatabaseReadWrite (String database, boolean readWrite) throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    // isc_spb_prp_writemode
    //       isc_spb_prp_wm_async
    //       isc_spb_prp_wm_sync
    // isc_spb_prp_accessmode
    //       isc_spb_prp_am_readonly
    //       isc_spb_prp_am_readwrite
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Sets the periodicity of automatic garbage collection for the database.
   * This represents <code>gfix -housekeeping interval</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   * <p>
   * The default sweep interval is 20,000.
   * <p>
   * To turn off automatic sweeping altogether, set the sweep interval to 0.
   *
   * @see DatabaseMetaData#getSweepInterval
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setSweepInterval (String database, int interval) throws java.sql.SQLException
  {
    // isc_dpb_sweep_interval
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    //   isc_spb_prp_sweep_interval
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Enable or disable synchronous writes to disk on commit.
   * This represents <code>gfix -write {sync | async}</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   * <p>
   * If writes are asynchronous then data may not be physically written to disk
   * on a database write, since operating systems buffer disk writes.
   * If there is a system failure before the data is written to disk,
   * then information can be lost.
   * <p>
   * Performing synchronous writes ensures data integrity and safety,
   * but slower performance.  In particular, operations which involve
   * data modification are slower.
   * <p>
   * There are two significant shortcomings of using asynchronous writes:
   * <ul>
   * <li> When a transaction is committed, pages will be written 
   * to the OS cache, but we will not wait for them to be 
   * written to disk before returning from the commit with a 
   * success status. 
   * 
   * <li> The "careful write" strategy is defeated.  We write 
   * pages to disk in such a way that what's on disk is always 
   * logically consistent.  If asynchronous writes are specified, 
   * we still write pages out to the OS cache in this order, but 
   * the OS will probably write them to disk in a different order
   * to optimize disk seeks. 
   * </ul>
   * <p>
   * So there are risks for database corruption if async writes 
   * are being used.  The window of risk is the amount of time it takes 
   * your OS to write pages to disk once we have written them out to the 
   * OS cache.  While this may seem like a small window, imagine a system 
   * under full load, pages being constantly updated.  At any given time 
   * you may lose power, and certain pages will not be written out, or 
   * even worse pages may be written which expect the presence of other 
   * pages.
   * <p>
   * Sometimes the loss of a page is easily recoverable, such as when a 
   * new record is added to a data page.  You will merely lose that new 
   * record.  Other times the damage could be more severe.  Some things 
   * can be fixed by gfix -mend.  If that doesn't work, sometimes data 
   * can be recovered from a damaged database by copying it to another 
   * database one table at a time.  But you do have to face the possibility 
   * of complete non-recovery of the database, though this is relatively 
   * rare. 
   * <p>
   * So to be safe, those using asynchronous write should use a UPS, or 
   * at the very least take a backup once a day and be prepared, in the 
   * worst case, to lose that day's changes. 
   *
   * @docauthor Deej Bredenberg 
   * @see DatabaseMetaData#usingSynchronousWrites()
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void useSynchronousWrites (String database, boolean synchronous) throws java.sql.SQLException
  {
    // isc_dpb_force_write
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    //   isc_spb_options/isc_spb_prp_wm_async/isc_spb_prp_wm_sync
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
        
  //---------------------------service requests on a database-------------------
  
  /**
   * Prevent further database connections or transactions,
   * or force the shutdown of existing ones.
   * This incorporates
   * <code>gfix -shut -force timeout</code>,
   * <code>gfix -shut -attach timeout</code>,
   * and
   * <code>gfix -shut -tran timeout</code>
   * functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * During the timeout period, new connections and/or transactions may
   * be prevented from starting.
   * If the <code>forceOffline</code> option is disabled,
   * then at the end of the specified <code>timeout</code> period,
   * if there are still open connections, this method is aborted and the
   * database is <i>not</i> taken offline.
   * <p>
   * If the <code>forceOffline</code> option is enabled,
   * then at the end of the timeout period any open transactions
   * are rolled back, connections are disconnected, and the
   * database is taken offline.
   * This option should only be used as a last resort.
   * <p>
   * To force an immediate database shutdown,
   * specify <code>forceOffline</code>
   * with a <code>timeout</code> period of 0.
   * <p>
   * Only the SYSDBA user or the database owner may invoke this method.
   * Once a database is offline, sysdba or the database owner may
   * connect to the offline database and operate on it in the normal fashion.
   * Other users will be denied access.
   * <p>
   * Periodically you will want to shut down a database for backups and restores.
   * Backup saves only the current committed version of each record while also
   * putting all the data for each relation on contiguous pages in the database.
   * Restore will rebuild all the indices and reset the statistics for each.
   * This usually will increase performance significantly.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void takeOffline (String database,
                                        boolean forceOffline,
                                        boolean blockNewConnections,
                                        boolean blockNewTransactions,
                                        int timeout) throws java.sql.SQLException
  {
    // isc_dpb_shutdown and isc_dpb_shutdown_delay
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    //   isc_spb_prp_shutdown_db            gfix -shut -force n
    //   isc_spb_prp_deny_new_attachments   gfix -shut -attach n
    //   isc_spb_prp_deny_new_transactions  gfix -shut -trans n
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Brings an offline database back online.
   * This represents <code>gfix -online</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a server access error occurs
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void bringOnline (String database) throws java.sql.SQLException
  {
    // isc_dpb_online
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_dbname
    //   isc_spb_options/isc_spb_prp_db_online
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Backs up a database to multiple backup files.
   * This represents <code>gbak -backup_database</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Parameter <code>backupOptions</code> maps any of the following String-based keys
   * to String, Boolean, or Integer objects:
   * <p>
   * <dl>
   * <dt> <code>tapeDeviceBlockingFactor</code>
   * <dd> An Integer switch for <code>gbak -backup_database -factor n</code>.
   *      Use blocking factor n for a tape device.
   * <dt> <code>nonTransportable</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -nt</code>.
   *      Create a non-transportable backup which may not be restored on another
   *      machine architecture.
   *      By default, <code>gbak -backup_database -transportable</code> is used to produce
   *      an architecture independent backup file in XDR format.
   * <dt> <code>ignoreLimboTransactions</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -limbo</code>.
   *      Ignore limbo transactions during backup.
   * <dt> <code>ignoreChecksums</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -ignore</code>.
   *      Ignore checksums during backup.
   * <dt> <code>metaDataOnly</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -metadata</code>.
   *      Backup metadata only, no data.
   * <dt> <code>inhibitGarbageCollection</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -garbage_collect</code>.
   *      Do not garbage collect during backup.
   * <dt> <code>convertExternalFilesToInternalTables</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -convert</code>.
   *      Convert external files as internal tables.
   * <dt> <code>verbose</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -verify</code>.
   *      Report details of the backup process.
   *      This is the return value of the backup method.
   * <dt> <code>oldDescriptions</code>
   * <dd> A Boolean switch for <code>gbak -backup_database -old_descriptions</code>.
   *      Backup metadata in old-style format.
   * </dl>
   * <p>
   * All such configuration information is optional.
   * Furthermore, the <code>backupOptions</code> map itself may be null.
   * <p>
   * All boolean options are <code>false</code> by default.
   * <p>
   * The user name and password used to obtain the ServerManager will be used
   * to connect to the database for backup.
   * This helps add some degree of security for this operation.
   * Only SYSDBA or the database owner will be able to backup a database.
   * <p>
   * The SQL role name used for this operation is the <code>roleName</code> property
   * inherited from the {@link DataSource} that produced this ServerManager.
   * <p>
   * Under normal circumstances, when backing up a database, the backup file
   * will always be located on the InterBase server machine since the backup
   * service cannot open a file over a network connection.
   * The backup service can create a remote file if the following conditions are true:
   * <ul>
   * <li> The InterBase server is running on Windows NT.
   * <li> The path to the backup file is specified as a UNC name.
   * <li> The destination for the file is another Windows NT machine or
   *      a machine which can be connected to via UNC naming conventions.
   * </ul>
   * <b>OR</b> the destination drive is mounted via NFS (or some equivalent)
   * on the machine running the InterBase server.
   *
   * @return textual information concerning the restore
   * @see #backup(String,String,java.util.Map)
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String backup (String        database,
				     String[]      backupFiles,
                                     int[]         backupFileLengths,
                                     java.util.Map backupOptions) throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_backup/
    //   isc_spb_dbname                    for database
    //   isc_spb_role_name                 gbak -role, use roleName property from DataSource
    //   isc_spb_verbose                   verbose
    //   isc_spb_bkp_file                  backupFiles[]
    //   isc_spb_bkp_factor                tapeDeviceBlockingFactor
    //   isc_spb_bkp_length                backupFileLengths[]
    //   isc_spb_options/
    //     isc_spb_bkp_ignore_checksums    ignoreChecksums
    //     isc_spb_bkp_ignore_limbo        ignoreLimbo
    //     isc_spb_bkp_metadata_only       metaDataOnly
    //     isc_spb_bkp_no_garbage_collect  inhibitGarbageCollection
    //     isc_spb_bkp_old_descriptions    oldDescriptions
    //     isc_spb_bkp_non_transportable   nonTransportable
    //     isc_spb_bkp_convert             convertExternalFilesToInternalTables
    // followed by
    // isc_service_query()/isc_info_svc_line or isc_info_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Backs up a database to a single backup file.
   * <p>
   * See the more general
   * {@link #backup(String,String[],int[],java.util.Map)
   *        backup(database, backupFiles[], fileLengths[], infoMap)}
   * for details.
   *
   * @return textual information concerning the restore
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String backup (String        database,
				     String        backupFile,
                                     java.util.Map backupOptions) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Restores a backup to multiple database files.
   * This represents <code>gbak -c[reate_database]</code> or
   * <code>gbak -r[eplace_database]</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Parameter <code>restoreOptions</code> maps any of the following String-based keys
   * to String, Boolean, or Integer objects:
   * <p>
   * <dl>
   * <dt> <code>replaceDatabase</code>
   * <dd> A Boolean switch for <code>gbak -r[eplace_database]</code>.
   *      Restore database to new file or replace existing file.
   *      By default <code>gbak -c[reate_database]</code> functionality is used
   *      so that you can restore to a new file only;
   *      an existing database file is not replaced.
   * <dt> <code>pageSize</code>
   * <dd> An Integer switch for <code>gbak {-r|-c} -page_size</code>.
   *      The default is 1024 (1K).
   * <dt> <code>cachePages</code>
   * <dd> An Integer switch for <code>gbak {-r|-c} -buffers</code>.
   *      See {@link #setDatabaseCachePages setDatabaseCachePages()}.
   * <dt> <code>deactivateIndices</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -inactive</code>.
   * <dt> <code>dropValidityConstraints</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -novalidity</code>.
   *      Do not restore validity constraints when restoring metadata;
   *      allows restoration of data that would otherwise not meet
   *      validity constraints.
   * <dt> <code>noShadow</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -kill</code>.
   *      Do not restore metadata references to shadows that were previously defined.
   *      Subsequent database operations will not be shadowed.
   * <dt> <code>oneTableAtATime</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -one_at_a_time</code>.
   *      Restore one table at a time.
   *      This is useful for partial recovery of a database that contains corrupt data.
   * <dt> <code>verbose</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -verify</code>.
   *      Report details of the restore.
   *      This is the return value of the restore method.
   * <dt> <code>reserveNoSpaceForVersioning</code>
   * <dd> A Boolean switch for <code>gbak {-r|-c} -use_all_space</code>.
   *      See {@link #reserveSpaceForVersioning reserveSpaceForVersioning()}.
   * </dl>
   * <p>
   * All such configuration information is optional.
   * Furthermore, the <code>restoreOptions</code> map itself may be null.
   * <p>
   * All boolean options are <code>false</code> by default.
   * <p>
   * The user name and password inherited from the {@link DataSource}
   * used to obtain this ServerManager is used to connect to the database
   * for restore.  This adds some degree of security for this operation.
   * <p>
   * The SQL role name used for this operation is the <code>roleName</code> property
   * inherited from the {@link DataSource} that produced this ServerManager.
   *
   * @return textual information concerning the restore
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String restore (String        backupFile,
				      String[]      databaseFiles,
                                      int[]         databaseFileLengths,
                                      java.util.Map restoreOptions) throws java.sql.SQLException
  { 
    // isc_service_start()/isc_action_svc_restore/
    //   isc_spb_dbname            databaseFiles[]
    //   isc_spb_role_name         gbak -role, use roleName from dataSource
    //   isc_spb_res_buffers       cachePages
    //   isc_spb_res_page_size     pageSize
    //   isc_spb_res_length        databaseFileLengths[]
    //   isc_spb_bkp_file          backupFile
    //   isc_spb_verbose           verbose
    //   isc_spb_options/
    //     isc_spb_res_deactivate_idx    deactivateIndices
    //     isc_spb_res_no_shadow         noShadow
    //     isc_spb_res_no_validity       dropValidityConstraints
    //     isc_spb_res_one_at_a_time     oneTableAtATime
    //     isc_spb_res_replace           replaceDatabase
    //     isc_spb_res_create            default behavior, but *must* be specified
    //     isc_spb_res_reserve_space     reserveSpaceForVersioning
    // followed by
    // isc_service_query()/isc_svc_info_line or isc_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Restores a backup to a single working database file.
   * <p>
   * See the more general
   * {@link #restore(String,String[],int[],java.util.Map)
   *        restore(backupFile, databaseFiles[], fileLengths[], infoMap)}
   * for details.
   *
   * @return textual information concerning the restore
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String restore (String backupFile,
				      String database,
                                      java.util.Map restoreOptions) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Manually sweep the database of old record versions.
   * This represents <code>gfix -sweep</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Sweep does not require exclusive access, but if there
   * are no active transactions on the database,
   * then sweep can update certain data and transaction
   * state information.
   *
   * @return textual information about the sweep
   * @see #setSweepInterval
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String sweep (String database) throws java.sql.SQLException
  {
    // isc_dpb_sweep
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_options/
    //     isc_spb_rpr_sweep_db
    // followed by
    // isc_service_query()/isc_svc_info_line or isc_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Verifies the database's internal page structures.
   * Also checks the database for checksum errors, and unassigned or corrupt pages.
   * This represents <code>gfix -validate -no_update</code> functionality, 
   * with other switches specified in the <code>verifyOptions</code> map.
   * No database structures are updated.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Parameter <code>verifyOptions</code> maps any of the following String-based keys
   * to Boolean objects:
   * <p>
   * <dl>
   * <dt> <code>full</code>
   * <dd> A Boolean switch for <code>gfix -validate -noupdate -full</code>.
   *      This validate all records.
   *      Without full validation, only page structures will be validated, which does
   *      involve some limited checking of records. 
   * <dt> <code>ignoreChecksums</code>
   * <dd> A Boolean switch for <code>gfix -validate -noupdate -ignore</code>
   * </dl>
   * <p>
   * All such configuration information is optional.
   * Furthermore, the <code>verifyOptions</code> map itself may be null.
   * <p>
   * This method will attempt to obtain exclusive access to the database.
   * Verification requires exclusive access to ensure that
   * database structures are not modified during validation.
   * If there are other connections to the database, a SQLException will be
   * thrown and the connection refused.
   *
   * @return textual information detailing the verification
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String verify (String database,
                                     java.util.Map verifyOptions) throws java.sql.SQLException
  {
    // isc_dpb_verify if validate (gfix -validate)
    // isc_dpb_records if validate & checkAllRecords (gfix -validate -full)
    // isc_dpb_repair if validate & mend (gfix -validate -mend)
    // isc_dpb_no_update if validate & !mend (gfix -validate -no_update)
    // isc_dpb_ignore if validate (gfix -validate)
    // isc_dpb_indices if validate (gfix -validate)
    // isc_dpb_transactions if validate (gfix -validate)
    // isc_dpb_pages if validate (gfix -validate)

    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_options/
    //     isc_spb_rpr_validate_db      -validate, on for verify() and repair()
    //     isc_spb_rpr_check_db         -noupdate, off for repair()
    //     isc_spb_rpr_full             -full
    //     isc_spb_mend_db              -mend, off for verify()
    //     isc_spb_rpr_ignore_checksums -ignore
    //     isc_spb_rpr_list_limbo_trans ideally we should show this too in output
    //     isc_spb_rpr_kill_shadows     off
    //     isc_spb_rpr_sweep            off
    // followed by
    // isc_service_query()/isc_svc_info_line or isc_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
      
  /**
   * Validate and mend the database where possible.
   * Orphaned pages will be released, and allocated pages not in use will be freed.
   * In addition, if any inconsistencies are found then an attempt is made to mend
   * the database to make it viable for reading, but is not guaranteed to retain data.
   * This represents <code>gfix -validate -mend</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * <b>Note:</b> Clearly this cannot repair all corrupt databases in general.
   * <p>
   * Parameter <code>repairOptions</code> maps any of the following String-based keys
   * to Boolean objects:
   * <p>
   * <dl>
   * <dt> <code>full</code>
   * <dd> A Boolean switch for <code>gfix -validate -mend -full</code>.
   *      Validate and mend record fragments.
   *      Without full validation, only page structures will be validated,
   *      which does involve some limited checking of records.
   * <dt> <code>ignoreChecksums</code>
   * <dd> A Boolean switch for <code>gfix -validate -mend -ignore</code>.
   * <dt> <code>killShadows</code>
   * <dd> A Boolean switch for <code>gfix -validate -mend -kill</code>..
   *      Drops any shadow files associated with the database.
   * <dt> <code>sweep</code>
   * <dd> A Boolean switch for <code>gfix -validate -mend -sweep</code>.
   *      Sweeps the database.
   * </dl>
   * <p>
   * All such configuration information is optional.
   * Furthermore, the <code>repairOptions</code> map itself may be null.
   * <p>
   * This method will attempt to obtain exclusive access to the database. 
   * Verification and repair requires exclusive access to ensure that
   * database structures are not modified during validation.
   * If there are other connections to the database, a SQLException will be
   * thrown and the connection refused.
   *
   * @return textual information detailing the reparations
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String repair (String database,
                                     java.util.Map repairOptions) throws java.sql.SQLException
  {
    // isc_dpb_verify
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_options/
    //     isc_spb_rpr_validate_db      -validate, on
    //     isc_spb_rpr_check_db         -noupdate, off
    //     isc_spb_rpr_full             -full
    //     isc_spb_mend_db              -mend, on
    //     isc_spb_rpr_ignore_checksums -ignore
    //     isc_spb_rpr_kill_shadows     -kill
    //     isc_spb_rpr_sweep_db         -sweep
    //     isc_spb_list_limbo_trans     ??should we display these as well?? !!!
    // followed by
    // isc_service_query()/isc_svc_info_line or isc_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Creates a multi-file shadow for a database.
   * <p>
   * Shadows are carbon copies of the database, and may be used
   * as a protection against hardware failures by setting up
   * a shadow file on a separate disk partition or even a separate machine
   * from the database file.
   * If your primary disk or server fails, the shadow file may be
   * activated for use as a regular database file by using
   * {@link #activateShadow(String) activateShadow(shadowFile)}.
   * On activation, the shadow is no longer a shadow but becomes
   * a regular database file.
   * Users may then reconnect to the activated shadow file as a regular
   * database file.
   * Recovering in this way from a shadow file is quicker than restoring from a backup.
   * <p>
   * Shadows can be composed of multiple files just like normal databases.
   * <p>
   * A drawback to using shadows is that it doubles the number of
   * writes the database server must perform.
   * <p>
   * Parameter <code>shadowProperties</code> maps any of the following String-based keys
   * to Integer objects:
   * <p>
   * <dl>
   * <dt> <code>pageSize</code>
   * <dd> The shadow file page size.
   * </dl>
   * <p>
   * All such properties are optional.
   * Furthermore, the <code>shadowProperties</code> map itself may be null.
   * <p>
   * See the <i>InterBase Operations Guide</i> for further details on shadows.
   * <p>
   * This method is a placeholder for possible future API functionality.
   * Currently a shadow file may only be created by executing the SQL command
   * <code>CREATE SHADOW</code> on a connection to the database.
   * <p>
   * <b>Reviewers Note:</b>
   * Are there other shadow properties that should be specified?
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   **/
  synchronized public void createShadow (String database,
                                         String[] shadowFiles,
                                         int[] shadowFileLengths,
                                         java.util.Map shadowProperties) throws java.sql.SQLException
  {
    // no dpb parameter
    // no svc parameter
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
      
  /**
   * Creates a single shadow file for a database.
   * <p>
   * See the more general
   * {@link #createShadow(String,String[],int[],java.util.Map)
   *        createShadow(database, shadowFiles[], lengths[], shadowProperties)}
   * for details.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   **/
  synchronized public void createShadow (String database,
					 String shadow,
                                         java.util.Map shadowProperties) throws java.sql.SQLException
  { 
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Activate a database shadow file.
   * Once activated, the shadow file becomes a regular database file.
   * This represents <code>gfix -activate</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * <b>Warning:</b>
   * Be sure the main database is offline with no attachments before
   * activating its shadow.
   * If a shadow is activated while the main database is online
   * the shadow can be corrupted by existing attachments to the main database.
   * <p>
   * See the InterBase Operations Guide for further details on shadows.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void activateShadow (String shadow) throws java.sql.SQLException
  {
    // gfix -a functionality
    // isc_dpb_activate_shadow
    // isc_service_start()/isc_action_svc_properties/
    //   isc_spb_options/
    //     isc_spb_prp_activate
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Deletes the shadow file for a database.
   * This represents <code>gfix -kill</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * A shadow file may also be deleted by executing the SQL command
   * <code>DROP SHADOW</code> on a connection to the database.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void deleteShadow (String database) throws java.sql.SQLException
  {
    // gfix -kill functionality
    // isc_dpb_delete_shadow
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_options/
    //     isc_spb_rpr_kill_shadows
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Creates a single file database using the specified persistent database properties.
   * <p>
   * Parameter <code>databaseProperties</code> maps any of the following String-based keys
   * to Boolean or Integer objects:
   * <p>
   * <dl>
   * <dt> <code>pageSize</code>
   * <dd> The page size of the new database.
   * <dt> <code>reserveSpaceForVersioning</code>
   * <dd> See {@link #reserveSpaceForVersioning(String, boolean) reserveSpaceForVersioning(database, boolean)}.
   * <dt> <code>useSynchronousWrites</code>
   * <dd> See {@link #useSynchronousWrites(String, boolean) useSynchronousWrites(database, boolean)}.
   * <dt> <code>cachePages</code>
   * <dd> See {@link #setDatabaseCachePages(String, int) setDatabaseCachePages(database, int)}.
   * <dt> <code>sweepInterval</code>
   * <dd> See {@link #setSweepInterval(String, int) setSweepInterval(database, int)}.
   * </dl>
   * <p>
   * All such properties are optional.
   * Furthermore, the <code>databaseProperties</code> map itself may be null.
   * All boolean configurables default to <code>true</code>,
   * the page size defaults to 1024 (1K), the sweep interval defaults
   * to 20,000, and the <code>cachePages</code> default stamped onto the database header page is 0.
   * The server-wide defaults for page size and cache pages can be configured using
   * <code>DATABASE_CACHE_PAGES</code> in the InterBase <code>ibconfig</code> startup file or
   * when starting the server dynamically with
   * {@link ServerManager#startInterBase(int,int) ServerManager.startInterBase(defaultCachePages, defaultPageSize)}.
   * <p>
   * <b>Note:</b>
   * The SQL string <code>CREATE DATABASE ...</code> cannot be executed with InterClient
   * because SQL is executed within the context of a Connection in JDBC.
   * Connectionless SQL is not allowed.
   * <p>
   * <b>Reviewers Note:</b>
   * Do we need sqlDialect as an option as well?
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void createDatabase (String database,
                                           java.util.Map databaseProperties) throws java.sql.SQLException
  {
    // isc_db_handle ISC_FAR db_handle = 0;
    // if (isc_create_database (status_.vector(),
    // 			        0, // short ?, probably means null terminated filename
    // 			        database,
    // 			        &db_handle, // perhaps I should use connection_->dbHandleP()?
    // 			        0, //short dpb_len,
    // 			        0, //char ISC_FAR *dpb, page size can be specified with isc_dpb_page_size
    // 			        0)) //short dbType, not used
    //     throw new IB_SQLException (IB_SQLException::engine__default_0__, &status_);

    // isc_dpb_page_size
    // isc_dpb_read_write - not meaningful for createDatabase()
    // isc_dpb_no_reserve
    // isc_dpb_force_write
    // isc_dpb_set_page_buffers and/or sc_dpb_num_buffers
    // isc_dpb_sweep_interval            

    checkForClosedConnection ();

    // uses isc_create_dabase()
    remote_CREATE_DATABASE (database);
  }

  private void remote_CREATE_DATABASE (String database) throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.writeByte (MessageCodes.SERVICE_REQUEST__);
    sendMsg.writeByte (MessageCodes.CREATE_DATABASE__);
    sendMsg.writeLDSQLText (database);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }
  
  /**
   * Remove the database file.
   * This method will fail if there are any connections to the database.
   * 
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void deleteDatabase (String database) throws java.sql.SQLException
  {
    // isc_attach_database(dbHandle) then isc_drop_database(dbHandle)
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets limbo transaction information for the database.
   * This represents <code>gfix -list</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   * <p>
   * The <code>object[]</code> array returned contains the following information:
   * <i>TBD</i>.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public Object[] getLimboTransactions (String database) throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_tra_*
    //   isc_spb_options/
    //     isc_spb_list_limbo_trans
    // followed by isc_service_query()/isc_info_svc_limbo_trans/*
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Rollback limbo transactions.
   * This represents <code>gfix -rollback</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void rollbackTransactions (String database,
                                                 int[] transactionIds) throws java.sql.SQLException
  { 
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_tra_*
    //   isc_spb_rpr_rollback_trans
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Attempt to commit limbo transactions.
   * This represents <code>gfix -commit</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void commitTransactions (String database,
                                               int[] transactionIds) throws java.sql.SQLException
  { 
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_tra_*
    //   isc_spb_rpr_commit_trans
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Attempt to resolve limbo transactions using the two-phase commit protocol.
   * This represents <code>gfix -two_phase</code> functionality.
   * See the <i>InterBase Operations Guide</i> for more details.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public void twoPhaseCommitTransactions (String database,
                                                       int[] transactionIds) throws java.sql.SQLException
  { 
    // isc_service_start()/isc_action_svc_repair/
    //   isc_spb_dbname
    //   isc_spb_tra_*
    //   isc_spb_rpr_recover_two_phase
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
       
  //-----------------------getting database-wide information--------------------

  /**
   * Gets database statistics as a text string.
   * This represents <code>gstat</code> functionality.
   * See the <i>InterBase Operations Guide</i> for details.
   * <p>
   * Parameter <code>options</code> maps the following string-based keys to Boolean objects:
   * <p>
   * <dl>
   * <dt> <code>header</code>
   * <dd> A Boolean switch representing <code>gstat -header</code> functionality.
   *      Stop reporting statistics after reporting the information on the header page.
   *      False by default.
   * <dt> <code>log</code>
   * <dd> A Boolean switch representing <code>gstat -log</code> functionality.
   *      Stop reporting statistics after reporting the information on the log pages.
   *      False by default.
   * <dt> <code>index</code>
   * <dd> A Boolean switch representing <code>gstat -index</code> functionality.
   *      Retrieve and display statistics on indexes in the database.
   *      True by default.
   * <dt> <code>data</code>
   * <dd> A Boolean switch representing <code>gstat -data</code> functionality.
   *      Retrieve and display statistics on data tables in the database.
   *      True by default.
   * <dt> <code>system</code>
   * <dd> A Boolean switch representing <code>gstat -system</code> functionality.
   *      Retrieve statistics on system tables and indexes in addition to user tables
   *      and indices.
   *      False by default.
   * </dl>
   * <p>
   * Reading header information would normally not require that the server be running.
   * However, in order to read the header information using this API,
   * the server must be running as the logic for doing this is not in the client.
   * <p>
   * Only the SYSDBA user or the database owner may run this service.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @return statistical information as flat text
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public String getStatisticsText (String database,
                                                java.util.Map options) throws java.sql.SQLException
  {
    // isc_service_start()/isc_action_svc_db_stats
    //   isc_spb_dbname
    //   isc_spb_options/
    //     isc_spb_sts_data_pages     dataPageStats
    //     isc_spb_sts_db_log         write ahead log stuff
    //     isc_spb_sts_hdr_pages      headerPageStats
    //     isc_spb_sts_idx_pages      indexPageStats
    //     isc_spb_sts_sys_rels       include system relations
    // followed by isc_service_query()/isc_svc_info_line or isc_svc_to_eof
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  //-------------------------Miscellaneous methods------------------------------
  
  /**
   * The first warning reported by calls on this ServerManager is
   * returned.  
   * Subsequent warnings are chained to this SQLWarning.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @return the first SQLWarning or null
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public java.sql.SQLWarning getWarnings () throws java.sql.SQLException
  {
    return sqlWarnings_;
  }

  /**
   * After this call, getWarnings returns null until a new warning is
   * reported for this ServerManager.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void clearWarnings () throws java.sql.SQLException
  {
    sqlWarnings_ = null;
  }

  synchronized void setWarning (java.sql.SQLWarning e)
  {
    if (sqlWarnings_ == null)
      sqlWarnings_ = e;
    else
      sqlWarnings_.setNextException (e);
  }

  /**
   * A server manager connection will be closed when its finalizer is called
   * by the garbage collector.  However, there is no guarantee
   * that the garbage collector will ever run, and in general
   * will not run when an application terminates abruptly
   * without closing its server connections.
   * <p>
   * Therefore, it is recommended that server manager connections be
   * explicitly closed even if your application throws an exception.
   * This can be achieved by placing a call to close() in a finally
   * clause of your application as follows
   * <pre>
   * try {
   *   ...
   * }
   * finally {
   *   if (serverManager != null)
   *     try { serverManager.close (); } catch (SQLException e) {}
   * }
   * </pre>
   * <p>
   * Or alternatively, use the
   * {@link System#runFinalizersOnExit(boolean) System.runFinalizersOnExit(boolean)} method.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  protected void finalize () throws java.lang.Throwable
  {
    if (open_)
      close ();

    super.finalize ();
  }

  /**
   * Close the server manager connection and release server manager resources.
   * <p>
   * In most cases, it is desirable to immediately release a
   * ServerManager connection instead of waiting for
   * it to be automatically released; the <code>close</code> method provides this
   * immediate release. 
   *
   * <P><B>Note:</B> A ServerManager connection is automatically closed when it is
   * garbage collected. Certain fatal errors also result in a closed
   * ServerManager.
   *
   * @throws java.sql.SQLException if a server access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void close () throws java.sql.SQLException
  {
    if (!open_)
      return;
  
    java.sql.SQLException accumulatedExceptions = null;

    try {
      remote_DETACH_SERVER_MANAGER ();
    }
    catch (java.sql.SQLException e) {
      accumulatedExceptions = Utils.accumulateSQLExceptions (accumulatedExceptions, e);
    }

    open_ = false;

    try {
      jdbcNet_.disconnectSocket ();
    }
    catch (java.sql.SQLException e) {
      accumulatedExceptions = Utils.accumulateSQLExceptions (accumulatedExceptions, e);
    }

    if (accumulatedExceptions != null) {
      throw accumulatedExceptions;
    }
  }

  private void remote_DETACH_SERVER_MANAGER () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.writeByte (MessageCodes.DETACH_SERVER_MANAGER__);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }
  
      setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }
}

