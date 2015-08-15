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
 * Properties used to tailor an InterBase connection.
 * <p>
 * This class is used to
 * tailor connection properties before attaching
 * to an InterBase database.
 * Using this class is an alternative to configuring a
 * java.util.Properties object directly.
 * For example,
 * <pre>
 * java.util.Properties properties = new java.util.Properties ();
 * properties.put ("charSet", "SJIS")
 * </pre>
 * is identical to
 * <pre>
 * ConnectionProperties properties = new ConnectionProperties ();
 * properties.setCharacterEncoding (CharacterEncodings.SJIS)
 * </pre>
 * <p>
 * Also see
 * <a href="../../../help/icConnectionProperties.html">Connection Properties</a>
 * in the InterClient Help documentation for a list of valid properties.
 * <p><b>Note:</b>
 * These are transient properties of a connection.
 * Methods for setting persistent database properties may be found in the
 * {@link ServerManager ServerManager} class.
 * <p>
 * See the
 * <a href="../../../help/icExtensions.html">InterBase Extension API</a>
 * for a complete list of available InterBase extensions to JDBC.
 *
 * @deprecated deprecated since InterClient 2.0, replaced by {@link DataSource DataSource} API.
 * @see Driver#connect(java.lang.String,java.util.Properties)
 * @see Driver#getPropertyInfo(String, java.util.Properties)
 * @since <font color=red>Extension, since InterClient 1.0</font>
 **/
final public class ConnectionProperties extends java.util.Properties
{
  // DataSource-only Properties
  final static String databaseNameKey__ = "databaseName";
  final static String dataSourceNameKey__ = "dataSourceName";
  final static String descriptionKey__ = "description";
  final static String networkProtocolKey__ = "networkProtocol";
  final static String portNumberKey__ = "portNumber";
  final static String serverNameKey__ = "serverName";

  // DataSource and Driver API Properties
  final static String userKey__ = "user";
  final static String passwordKey__ = "password";
  final static String roleNameKey__ = "roleName";
  final static String charSetKey__ = "charSet";
  final static String sweepOnConnectKey__ = "sweepOnConnect";
  final static String suggestedCachePagesKey__ = "suggestedCachePages";
// CJL-IB6
  final static String sqlDialectKey__ = "sqlDialect";
// CJL-IB6 end change

  // **************
  // *** Values ***
  // **************
  final static String defaultCharSet__ = CharacterEncodings.NONE;
  final static String defaultPortNumber__ = "3060";
  final static String defaultNetworkProtocol__ = "jdbc:interbase:";
  final static String defaultSweepOnConnect__ = "false";
// CJL-IB6  This default means to speak the Database's native dialect.
  final static String defaultSQLDialect__ = "0";
// CJL-IB6

  /**
   * Construct a default InterBase connection properties object.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @deprecated To be deprecated in InterClient 2, replaced by {@link DataSource DataSource} API.
   **/
  public ConnectionProperties () {}

  //------------------- transient properties of a connection -------------------

  /**
   * Sets the character encoding for the pending connection.
   * All SQL and data will be presented to the database
   * using the specified character encoding.
   * <p>
   * The connection property key is "charSet".
   * The default value is {@link CharacterEncodings#NONE CharacterEncodings.NONE}.
   * <p>
   * Property values must match one of the
   * supported IANA character encoding names specified
   * in the {@link CharacterEncodings CharacterEncodings} class.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   * @deprecated To be deprecated in InterClient 2, replaced by {@link DataSource} API.
   **/
  public void setCharacterEncoding (String encoding)
  {
    put (charSetKey__, encoding);
  }

  /**
   * Sets the user and password for the pending connection.
   * <p>
   * The corresponding connection property keys are
   * "user" and "password".
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @deprecated To be deprecated in InterClient 2, replaced by {@link DataSource} API.
   **/
  public void setUser (String user, String password)
  {
    put (userKey__, user);
    put (passwordKey__, password);
  }
}
