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
 * Represents a driver implementation and a factory for connections.
 *
 * <p>The Java SQL framework allows for multiple database drivers.
 * Each driver should supply a class that implements
 * the Driver interface.
 * The DriverManager will try to load as many drivers as it can
 * find and then for any given connection request, it will ask each
 * driver in turn to try to connect to the target URL.
 *
 * <P>It is strongly recommended that each Driver class should be
 * small and standalone so that the Driver class can be loaded and
 * queried without bringing in vast quantities of supporting code.
 *
 * <P>When a Driver class is loaded, it should create an instance of
 * itself and register it with the DriverManager. This means that a
 * user can load and register a driver by doing
 * Class.forName("foo.bah.Driver").
 *
 * <p><b>InterClient Notes:</b>
 * <p>
 * There are various ways to load the InterClient driver.
 * See <a href="../../../examples/DriverExample.txt">DriverExample.java</a>
 * in the InterClient examples directory for details on loading the
 * InterClient driver under the JDBC 1 Driver API.
 * For details on loading the InterClient driver under the JDBC 2
 * {@link DataSource} API see
 * <a href="../../../examples/DataSourceExample.txt">DataSourceExample.java</a>.
 * <p>
 * The basic method for loading the driver is as follows:
 * <pre>
 * Class.forName ("interbase.interclient.Driver");
 * DriverManager.getConnection ("jdbc:interbase://server/c:/database-dir/atlas.gdb",
 *                              "sysdba", "masterkey");
 * </pre>
 * You can register a named driver instance with the driver manager in addition
 * to the automatically registered anonymous instance.
 * <pre>
 * java.sql.Driver driver = new interbase.interclient.Driver ();
 * java.sql.DriverManager.registerDriver (driver);
 * </pre>
 * However, registering multiple InterClient driver instances would only be necessary
 * if different instances recognized different URL prefixes.
 * For now, the InterClient driver only recognizes the JDBC protocol "jdbc:interbase:".
 * <p>
 * Information on using the InterClient driver with JBuilder can be found
 * in the distributed <a href="../../../readmes/JBuilderNotes.txt">JBuilderNotes.txt</a> file.
 * <p>
 * Although an InterClient driver may have an expiration date,
 * for most general releases InterClient itself will not expire.
 * Instead the InterServer component has an expiration date
 * defined in the InterServer license file (ic_license.dat).
 * Newer releases of InterServer may refuse to talk with
 * older releases of InterClient.
 * In this way, an older release of InterClient is effectively
 * expired when all compatible versions of InterServer expire.
 * A customer wishing to continue
 * using a older version of InterServer/InterClient,
 * may substitute in a new ic_license.dat file
 * with an extended license.
 * <p>
 * Also see the JDBC 2 Standard Extension {@link DataSource DataSource}
 * API for the preferred alternative to the Driver API for establishing
 * connections.
 *
 * @see Connection
 * @author Paul Ostler
 * @since <font color=red>JDBC 1</font>
 **/
final public class Driver implements java.sql.Driver
{
  // non-static because it depends on the point in time
  // when the Driver instance is instantiated.
  boolean expiredDriver_ = false; // for InstallationVerifier

  static private java.sql.SQLException exceptionsOnLoadDriver__ = null;

  static {
    // !!! Verify that the Globals static clause gets executed before the following line does
    // !!! this may hit the race-condition bug of java 1.1
    Utils.accumulateSQLExceptions (exceptionsOnLoadDriver__, Globals.exceptionsOnLoadResources__);
    try {
      java.sql.DriverManager.registerDriver (new Driver ());
    }
    catch (java.sql.SQLException e) {
      Utils.accumulateSQLExceptions (exceptionsOnLoadDriver__, e);
    }
  }

  /**
   * Create an explicit driver instance.
   * See <a href="../../../examples/FirstExample.txt">FirstExample.java</a>
   * for example usage.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public Driver ()
  {
    if (Globals.interclientExpirationDate__ != null) {
      java.util.Date now = new java.util.Date ();
      if (now.after (Globals.interclientExpirationDate__))
        expiredDriver_ = true;
    }
  }

  /**
   * Try to make a database connection to the given URL.
   * The driver should return "null" if it realizes it is the wrong kind
   * of driver to connect to the given URL.  This will be common, as when
   * the JDBC driver manager is asked to connect to a given URL it passes
   * the URL to each loaded driver in turn.
   *
   * <P>The driver should raise a SQLException if it is the right
   * driver to connect to the given URL, but has trouble connecting to
   * the database.
   *
   * <P>The java.util.Properties argument can be used to passed arbitrary
   * string tag/value pairs as connection arguments.
   * Normally at least "user" and "password" properties should be
   * included in the Properties.
   *
   * <p><b>InterClient note:</b>
   * See
   * <a href="../../../help/icConnectionProperties.html">Connection Properties</a>
   * in the InterClient Help documentation.
   *
   * @param url The URL of the database to connect to
   * @param info a list of arbitrary string tag/value pairs as
   *   connection arguments; normally at least a "user" and
   *   "password" property should be included
   * @return a Connection to the URL
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   */
  synchronized public java.sql.Connection connect (String url,
			                           java.util.Properties properties) throws java.sql.SQLException
  {
    // !! need to do this for server connect as well.
    if (exceptionsOnLoadDriver__ != null)
      throw exceptionsOnLoadDriver__;

    java.util.StringTokenizer urlTokenizer = tokenizeInterBaseProtocol (url);

    if (urlTokenizer == null)
      return null;

    String serverAndPort = tokenizeServerNameAndPort (urlTokenizer, url);
    int indexOfColon = serverAndPort.indexOf (':');
    String server = (indexOfColon == -1) ? serverAndPort : serverAndPort.substring (0, indexOfColon);
    int port = (indexOfColon == -1) ?
               Globals.defaultServerPort__ :
               Integer.parseInt (serverAndPort.substring (indexOfColon+1));

    String database = tokenizeDatabase (urlTokenizer, url);

    if (properties == null ||
        properties.getProperty (ConnectionProperties.userKey__) == null ||
        properties.getProperty (ConnectionProperties.passwordKey__) == null ||
        properties.getProperty (ConnectionProperties.userKey__).equals ("") ||
        properties.getProperty (ConnectionProperties.passwordKey__).equals (""))
      throw new InvalidArgumentException (ErrorKey.invalidArgument__connection_properties__no_user_or_password__);

// CJL-IB6  ensure that specified sqlDialect is a non-negative integer

    String dialectEntry = properties.getProperty(ConnectionProperties.sqlDialectKey__);
    try {
      if (dialectEntry != null) {
        int dialectValue = Integer.parseInt(dialectEntry);
        if ( dialectValue < 0 ) {
          throw new SQLDialectException(
             ErrorKey.invalidArgument__connection_properties__sql_dialect__,
             dialectEntry);  // negative dialect?
        }
      }
    }
    catch (NumberFormatException nfx) {
      throw new SQLDialectException(
         ErrorKey.invalidArgument__connection_properties__sql_dialect__,
         dialectEntry); // non-integer dialect?
    }
// CJL-IB6  end change
    // java.sql.DriverManager.setLoginTimeout() is in seconds,
    // java.net.Socket.setSoTimeout() is in milliseconds
    int socketTimeoutMilliseconds = java.sql.DriverManager.getLoginTimeout ()*1000;
    Connection connection = new Connection (socketTimeoutMilliseconds,
                                            server,
                                            port,
                                            database,
                                            properties);


    return connection;
  }

  /**
   * Returns true if the driver thinks that it can open a connection
   * to the given URL.  Typically drivers will return true if they
   * understand the subprotocol specified in the URL and false if
   * they don't.
   *
   * <p><b>InterClient note:</b>
   * InterClient only accepts URLs which begin with "jdbc:interbase:".
   *
   * @param url The URL of the database.
   * @return True if this driver can connect to the given URL.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   */
  public boolean acceptsURL (String url) throws java.sql.SQLException
  {
    return (tokenizeInterBaseProtocol (url) != null);
  }

  /**
   * The getPropertyInfo method is intended to allow a generic GUI tool to
   * discover what properties it should prompt a human for in order to get
   * enough information to connect to a database.  Note that depending on
   * the values the human has supplied so far, additional values may become
   * necessary, so it may be necessary to iterate though several calls
   * to getPropertyInfo.
   *
   * @param url The URL of the database to connect to.
   * @param info A proposed list of tag/value pairs that will be sent on connect open.
   * @return An array of DriverPropertyInfo objects describing possible
   *          properties.  This array may be an empty array if no properties
   *          are required.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.DriverPropertyInfo[] getPropertyInfo (String url,
                                                                     java.util.Properties properties)
  {
    // We can ignore url since we only handle interbase subprotocol.
    // Note: All string values must match those in interserver/Session.cc

// CJL-IB6 incremented size to add support for sqlDialect property
    java.sql.DriverPropertyInfo driverPropertyInfo[] = new java.sql.DriverPropertyInfo[8];
// CJL-IB6 end change

    // If there are no properties set already,
    // then create a dummy properties just to make the calls go thru.
    if (properties == null)
      properties = new java.util.Properties ();

    driverPropertyInfo[0] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.userKey__,
                                       properties.getProperty (ConnectionProperties.userKey__));

    driverPropertyInfo[1] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.passwordKey__,
                                       properties.getProperty (ConnectionProperties.passwordKey__));

    driverPropertyInfo[2] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.charSetKey__,
                                       properties.getProperty (ConnectionProperties.charSetKey__,
                                                               ConnectionProperties.defaultCharSet__));

    driverPropertyInfo[3] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.roleNameKey__,
                                       properties.getProperty (ConnectionProperties.roleNameKey__));

    driverPropertyInfo[5] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.sweepOnConnectKey__,
                                       properties.getProperty (ConnectionProperties.sweepOnConnectKey__,
                                                               ConnectionProperties.defaultSweepOnConnect__));

    driverPropertyInfo[6] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.suggestedCachePagesKey__,
                                       properties.getProperty (ConnectionProperties.suggestedCachePagesKey__));

// CJL-IB6 new property
// <!>  check links in ConnectionProperties
// <!>  Should this be defaulted to 0 (zero)?
    driverPropertyInfo[7] =
      new java.sql.DriverPropertyInfo (ConnectionProperties.sqlDialectKey__,
                                       properties.getProperty (ConnectionProperties.sqlDialectKey__));
// CJL-IB6 end change

    driverPropertyInfo[0].description =
      Globals.getResource (ResourceKeys.propertyDescription__user);
    driverPropertyInfo[1].description =
      Globals.getResource (ResourceKeys.propertyDescription__password);
    driverPropertyInfo[2].description =
      Globals.getResource (ResourceKeys.propertyDescription__charSet);
    driverPropertyInfo[3].description =
      Globals.getResource (ResourceKeys.propertyDescription__roleName);
    driverPropertyInfo[5].description =
      Globals.getResource (ResourceKeys.propertyDescription__sweepOnConnect);
    driverPropertyInfo[6].description =
      Globals.getResource (ResourceKeys.propertyDescription__suggestedCachePages);
// CJL-IB6
// <OK> check resource keys and EN_US resources
    driverPropertyInfo[7].description =
      Globals.getResource (ResourceKeys.propertyDescription__sqlDialect);
// CJL-IB6 end change

    driverPropertyInfo[0].required = true;
    driverPropertyInfo[1].required = true;
    driverPropertyInfo[2].required = false;
    driverPropertyInfo[3].required = false;
    driverPropertyInfo[4].required = false;
    driverPropertyInfo[5].required = false;
    driverPropertyInfo[6].required = false;
// CJL-IB6
    driverPropertyInfo[7].required = false;
// CJL-IB6 end change

    driverPropertyInfo[2].choices = CharacterEncodings.getSupportedEncodings ();
    driverPropertyInfo[5].choices = new String[] { "false", "true" };
// CJL-IB6
    driverPropertyInfo[7].choices = new String[] { "1", "2", "3" };
// CJL-IB6 end change

    return driverPropertyInfo;
  }

  private java.util.StringTokenizer tokenizeInterBaseProtocol (String databaseURL)
  {
    if (databaseURL == null)
      return null;

    java.util.StringTokenizer urlTokenizer = new java.util.StringTokenizer (databaseURL, "/ \t\n\r", true);

    if (!urlTokenizer.hasMoreTokens ())
      return null;

    String subprotocol = urlTokenizer.nextToken();
    if (!subprotocol.equals (Globals.jdbcNetProtocol__))
      return null;

    return urlTokenizer;
  }

  private String tokenizeServerNameAndPort (java.util.StringTokenizer urlTokenizer,
			                    String databaseURL) throws java.sql.SQLException
  {
    if (!urlTokenizer.hasMoreTokens ())
      throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_prefix_0__,
				    databaseURL);

    if (!urlTokenizer.nextToken().equals ("/"))
      throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_prefix_0__,
				    databaseURL);

    if (!urlTokenizer.nextToken().equals ("/"))
      throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_prefix_0__,
				    databaseURL);

    try {
      return urlTokenizer.nextToken ();
    }
    catch (java.util.NoSuchElementException e) {
      throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_prefix_0__,
				    databaseURL);
    }
  }

  private String tokenizeDatabase (java.util.StringTokenizer urlTokenizer,
			           String databaseURL) throws java.sql.SQLException
  {
    try {
      if (!urlTokenizer.nextToken().equals ("/"))
        throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_suffix_0__,
				      databaseURL);
      return urlTokenizer.nextToken ("\t\n\r");
    }
    catch (java.util.NoSuchElementException e) {
      throw new URLSyntaxException (ErrorKey.urlSyntax__bad_server_suffix_0__,
				    databaseURL);
    }
  }

  /**
   * Get the driver's major version number. Initially this should be 1.
   *
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMajorVersion ()
  {
    return Globals.interclientMajorVersion__;
  }

  /**
   * Get the driver's minor version number. Initially this should be 0.
   *
   * <p><b>InterClient note:</b>
   * In addition to major and minor version numbers,
   * InterClient provides build numbers to further qualify driver versions.
   * Build numbers may be extracted from an InterClient driver object
   * via an extension to the JDBC API, {@link #getBuildNumber getBuildNumber()}.
   *
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMinorVersion ()
  {
    return Globals.interclientMinorVersion__;
  }

  /**
   * Report whether the Driver is a genuine JDBC COMPLIANT (tm) driver.
   * A driver may only report "true" here if it passes the JDBC compliance
   * tests, otherwise it is required to return false.
   *
   * JDBC compliance requires full support for the JDBC API and full support
   * for SQL 92 Entry Level.  It is expected that JDBC compliant drivers will
   * be available for all the major commercial databases.
   *
   * This method is not intended to encourage the development of non-JDBC
   * compliant drivers, but is a recognition of the fact that some vendors
   * are interested in using the JDBC API and framework for lightweight
   * databases that do not support full database functionality, or for
   * special databases such as document information retrieval where a SQL
   * implementation may not be feasible.
   *
   * <p><b>InterClient note:</b>
   * InterBase is only two features shy of compliance:
   * <ul>
   * <li> Quoted SQL identifiers.
   * <li> Asynchronous cancel and statement timeout.
   * </ul>
   *
   * @since <font color=red>JDBC 1</font>
   * @return true if so
   */
  public boolean jdbcCompliant ()
  {
    return false;
  }

  // ------------------- InterClient Extensions -------------------------

  // --------------------additional version information-------------------------
  /**
   * Gets the build number for this InterClient driver.
   *
   * @see #getBuildCertificationLevel
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public int getBuildNumber ()
  {
    return Globals.interclientBuildNumber__;
  }

  /**
   * Gets the build certification level for this version
   * of the InterClient driver.
   *
   * The InterClient certification levels are as follows:
   * <ul>
   * <li><b>{@link #testBuild testBuild}</b>
   * Test builds are experimental and uncertified with
   * no level of quality assurance whatsoever.
   * <li><b>{@link #betaBuild betaBuild}</b>
   * Beta builds are certified for quality assurance,
   * but remain experimental.
   * <li><b>{@link #finalBuild finalBuild}</b>
   * The highest level of InterClient build certification.
   * A final build is certified for quality assurance,
   * but is not experimental.
   * </ul>
   * These are not JDBC certification levels, rather they
   * represent a level of quality assurance for a particular
   * build of an InterClient driver.  So, for example, an InterClient
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
   * @since <font color=red>Extension, since InterClient 1.50</font>
   * @return the build certification level
   **/
  public int getBuildCertificationLevel ()
  {
    return Globals.interclientBuildCertification__;
  }

  /**
   * The lowest level of InterClient build certification.
   * Test builds are experimental and uncertified with
   * no level of quality assurance.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public final static int testBuild = Globals.testBuild_;

  /**
   * An intermediate level of InterClient build certification.
   * Beta builds are certified for quality assurance,
   * but remain experimental.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public final static int betaBuild = Globals.betaBuild_;

  /**
   * The highest level of InterClient build certification.
   * A final build is certified for quality assurance,
   * but is not experimental.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public final static int finalBuild = Globals.finalBuild_;

  /**
   * Are remote connections allowed.
   * <p>
   * A non-client/server edition would only allow "localhost" connections.
   *
   * @return true if remote connections are allowed, false for localhost only.
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public boolean clientServerEdition ()
  {
    return Globals.clientServerEdition__;
  }

  /**
   * Has the InterClient driver expired.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public boolean expiredDriver ()
  {
    return expiredDriver_;
  }

  /**
   * Gets the name of the company that developed this driver.
   *
   * @return The company that produced this driver.
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public String getCompanyName ()
  {
    return Globals.getResource (ResourceKeys.companyName);
  }

  /**
   * Gets the JRE versions which are compatible with this version of the InterClient driver.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   * @return an array of JRE versions, eg. {"1.2", "1.3"}.
   **/
  public String[] getCompatibleJREVersions ()
  {
    return Globals.interclientCompatibleJREVersions__;
  }

  /**
   * Gets the InterBase major versions which are compatible with this version of the InterClient driver.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   * @return an array of InterBase major versions as integers, eg. {5, 6}.
   **/
  public int[] getCompatibleIBVersions ()
  {
    return Globals.compatibleIBVersions__;
  }

  /**
   * Gets the expiration date for this driver.
   * Returns null if the InterClient driver does not expire.
   * 
   * <p><b>Note:</b> Although InterClient may not expire, InterServer may.
   *
   * @see ExpiredDriverException
   * @see DatabaseMetaData#getInterServerExpirationDate
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public java.util.Date getExpirationDate ()
  {
    return Globals.interclientExpirationDate__;
  }

  /**
   * Gets the database URL prefix to use for InterBase connections.
   *
   * @return jdbc:interbase:
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public String getJDBCNetProtocol ()
  {
    return Globals.jdbcNetProtocol__;
  }

  /**
   * Gets the version of the messaging protocol between InterClient
   * and InterServer.
   *
   * @see DatabaseMetaData#getInterServerJDBCNetProtocolVersion
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public int getJDBCNetProtocolVersion ()
  {
    return Globals.jdbcNetProtocolVersion__;
  }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
