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

// If the database being accessed contains security classes created 
// using the InterBase V3.3 tool GDEF, certain methods which return 
// privilege related information may return incorrect results, e.g., 
// areAllTablesSelectable.  This is only an issue for InterBase 
// customers who have versions of InterBase prior to V4.0 and are 
// migrating to V4.0.  GDEF functionality was frozen in V3.3 and is 
// no longer supported in V4.0. 
//   
// Please note, in all cases, the security defined is enforced correctly, 
// i.e., if a user does not have appropriate privilege to perform a
// certain operation, the operation will not be performed. 

/**
 * Provides information about the database or connection, as well as
 * database capabilities.
 * <P>
 * Many of the methods here return lists of information in ResultSets.
 * You can use the normal ResultSet methods such as getString and getInt 
 * to retrieve the data from these ResultSets.  If a given form of
 * metadata is not available, these methods will throw a SQLException.
 * <P>
 * Some of these methods take arguments that are String patterns.  These
 * arguments all have names such as fooPattern.  Within a pattern String, "%"
 * means match any substring of 0 or more characters, and "_" means match
 * any one character. Only metadata entries matching the search pattern 
 * are returned. If a search pattern argument is set to a null ref, it means 
 * that argument's criteria should be dropped from the search.
 * <p>
 * A SQLException will be thrown if a driver does not support a meta
 * data method.  In the case of methods that return a ResultSet,
 * either a ResultSet (which may be empty) is returned or a
 * SQLException is thrown.
 *
 * @author Paul Ostler
 * @author Madhukar Thakur (some of the metadata SQL queries in interserver)
 * @author Bill Karwin (InterBase capability verifications)
 * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
 **/
final public class DatabaseMetaData implements java.sql.DatabaseMetaData
{
  private Connection connection_;

  private java.sql.Statement systemTableQuery_ = null;

  private JDBCNet jdbcNet_;

  String userName_ = null; // set by Connection constructor.

  String databaseProductVersion_ = null;
  int ibMajorVersion_;
  int odsMajorVersion_;
  int odsMinorVersion_;
  int pageSize_;
  int pageAllocation_;
// CJL-IB6 added for SQL Dialect and ReadOnly support
  int databaseSQLDialect_;   // new for IB6
  boolean databaseReadOnly_; // new for IB6
// CJL end

  // Called on first invocation of Connection.getMetaData()
  DatabaseMetaData (Connection connection,
                    JDBCNet jdbcNet)
  {
    connection_ = connection;
    jdbcNet_ = jdbcNet;
  }

  void checkForClosedConnection () throws java.sql.SQLException
  {
    if (!connection_.open_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__connection_closed__);
  }

  static final private int CATALOG_ALL_PROCEDURES_ARE_CALLABLE__ = 16;
  static final private int CATALOG_ALL_TABLES_ARE_SELECTABLE__ = 17;

  /**
   * Can all the procedures returned by getProcedures be called by the
   * current user.
   *
   * <p><b>InterClient note:</b>
   * This is always true for sysdba.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public boolean allProceduresAreCallable () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    if (userName_.equals (Globals.sysdba__))
      return true;

    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_ALL_PROCEDURES_ARE_CALLABLE__);

    RecvMessage recvMsg = null;

    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }
      return recvMsg.readBoolean ();
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  /**
   * Can all the tables returned by getTable be SELECTed by the
   * current user.
   *
   * <p><b>InterClient note:</b>
   * This is always true for sysdba.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public boolean allTablesAreSelectable () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    if (userName_.equals (Globals.sysdba__))
      return true;

    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_ALL_TABLES_ARE_SELECTABLE__);

    RecvMessage recvMsg = null;

    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }
      return recvMsg.readBoolean ();
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  /**
   * What's the url for this database.
   *
   * <p><b>InterClient note:</b>
   * For InterBase this is something like
   * "jdbc:interbase://host_name//databases/employee.gdb"
   * or "jdbc:interbase://host_name/d:/databases/employee.gdb"
   * for a database on server host_name.
   * InterServer and InterBase may reside on separate tiers;
   * If the interserver process is running on a server named interserver_host
   * and the database resides on a server named database_host, then the
   * url is something like
   * <pre>
   * jdbc:interbase://interserver_host/database_host:/databases/employee.gdb
   * </pre>
   *
   * @return the url or null if it can't be generated
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getURL() throws java.sql.SQLException
  {	
    checkForClosedConnection ();

    return ("jdbc:interbase://" + 
	    connection_.serverName_ + "/" + 
	    connection_.database_);
  }

  /**
   * What's our user name as known to the database.
   *
   * @return our database user name
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getUserName () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return userName_;
  }

  /**
   * Is the database in read-only mode.
   *
   * @see #isDatabaseReadWrite
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isReadOnly () throws java.sql.SQLException
  {
    checkForClosedConnection ();
// CJL-IB6  returns the status of the database, not the connection/transaction
//    return connection_.readOnly_;
    return databaseReadOnly_;
  }

  /**
   * Are NULL values sorted high.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean nullsAreSortedHigh () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Are NULL values sorted low.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean nullsAreSortedLow () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // According to Bill Karwin
    return true;
  }

  /**
   * Are NULL values sorted at the start regardless of sort order.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean nullsAreSortedAtStart () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Are NULL values sorted at the end regardless of sort order.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean nullsAreSortedAtEnd () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // According to Bill Karwin
    return false;
  }

  /**
   * Gets the name of the database product.
   *
   * <p><b>InterClient note:</b>
   * Returns "InterBase" usually, but may be locale dependent.
   *
   * @return database product name
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getDatabaseProductName () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return Globals.getResource (ResourceKeys.interbase);
  }

  /**
   * Gets the version of this database product.
   *
   * <p><b>InterClient note:</b>
   * Returns something like "SO-V5.0B" for an InterBase 5.0B server on Solaris.
   *
   * @return database version
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getDatabaseProductVersion () throws java.sql.SQLException
  {
    // isc_info_version
    checkForClosedConnection ();
    return databaseProductVersion_;
  }

  /**
   * Gets the SQL Dialect level of this database.
   *
   * The SQL Dialect of a database indicates which SQL features are available
   * to clients connecting at that SQL Dialect level.  As more features are
   * added to database server, such as new datatypes and syntax enhancements,
   * new SQL Dialect levels are introduced. Users can establish connections
   * that take advantage of the newest features (using a high SQL dialect) or
   * establish one that maintains compatability with older versions of the
   * server (using lower SQL Dialects).
   *
   * SQL Dialect levels available with InterBase 6.0:
   *
   * Dialect 1 maintains compatability with databases created with InterBase
   * versions 5.x.  With this dialect, the DATE datatype holds both date
   * and time values, NUMERIC and DECIMAL datatypes with a precision greater
   * than 9 are represented internally by double precision floating-point
   * values. String literals can be delimited by either single quotes (') or
   * double quotes (").  InterBase 5 databases are always dialect 1.
   *
   * Dialect 2 is a transition dialect, used to help you migrate the data in
   * a dialect 1 database to dialect 3.
   *
   * Dialect 3 introduces new features and is available only with InterBase
   * version 6.0 or higher.  With dialect 3, the DATE datatype represents only
   * the date portion of a timestamp, TIME represents only time, and TIMESTAMP
   * represents both date and time values (as did the DATE datatype in
   * dialect 1). NUMERIC and DECIMAL types with precisions between 10 and 18,
   * inclusive, are represented internally by 64-bit integers. Values appearing
   * within single quotes (') are presumed to be string literals; whereas
   * values appearing in double quotes (") are presumed to be identifiers
   * (identifying the names of tables, columns, views, procedures, roles,
   * triggers, and the like).
   *
   * @return SQL Dialect for the database
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>Extension InterClient 2.0</font>
   **/
  public int getDatabaseSQLDialect () throws java.sql.SQLException
  {
// CJL-IB6 new method in InterClient 2.0  
    // isc_info_db_SQL_dialect
    checkForClosedConnection ();
    return databaseSQLDialect_;
  }

  /**
   * Gets the name of this JDBC driver.
   *
   * <p><b>InterClient note:</b>
   * Returns "InterClient".
   *
   * @return JDBC driver name
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getDriverName () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return Globals.getResource (ResourceKeys.interclient);
  }

  /**
   * Gets the version of this JDBC driver.
   *
   * <p><b>InterClient note:</b>
   * This returns something like "1.50.36 Beta" for beta versions,
   * and something like "1.50.37" for product versions, where
   * 1 is the major version, 50 is the minor version, and 37 is the
   * build number.
   *
   * @see #getInterServerVersion()
   * @return JDBC driver version
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getDriverVersion () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return Globals.interclientVersionString__;
  }

  /**
   * Gets this JDBC driver's major version number.
   *
   * @see Driver#getMajorVersion()
   * @see #getInterServerMajorVersion
   * @return JDBC driver major version
   * @since <font color=red>JDBC 1</font>
   **/
  public int getDriverMajorVersion ()
  {
    return Globals.interclientMajorVersion__;
  }

  /**
   * Gets this JDBC driver's minor version number.
   *
   * @see Driver#getMinorVersion()
   * @see #getInterServerMinorVersion
   * @return JDBC driver minor version number.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getDriverMinorVersion ()
  {
    return Globals.interclientMinorVersion__;
  }

  /**
   * Does the database store tables in a local file.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   * InterBase databases are stored in .gdb files.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean usesLocalFiles () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Does the database use a file for each table.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if the database uses a local file for each table
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean usesLocalFilePerTable () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case unquoted SQL identifiers as
   * case sensitive and as a result store them in mixed case.
   *
   * <p><b>Compliance note:</b>
   * must return false.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsMixedCaseIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case unquoted SQL identifiers as
   * case insensitive and store them in upper case.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesUpperCaseIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Does the database treat mixed case unquoted SQL identifiers as
   * case insensitive and store them in lower case.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesLowerCaseIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case unquoted SQL identifiers as
   * case insensitive and store them in mixed case.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesMixedCaseIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case quoted SQL identifiers as
   * case sensitive and as a result store them in mixed case.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase 5 does not support quoted SQL identifiers.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsMixedCaseQuotedIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case quoted SQL identifiers as
   * case insensitive and store them in upper case.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase 5 does not support quoted SQL identifiers.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesUpperCaseQuotedIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case quoted SQL identifiers as
   * case insensitive and store them in lower case.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase 5 does not support quoted SQL identifiers.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesLowerCaseQuotedIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does the database treat mixed case quoted SQL identifiers as
   * case insensitive and store them in mixed case.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase 5 does not support quoted SQL identifiers.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean storesMixedCaseQuotedIdentifiers () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * What's the string used to quote SQL identifiers.
   * This returns a space " " if identifier quoting isn't supported.
   *
   * <p><b>Compliance note:</b>
   * must return the double quote character.
   *
   * <p><b>InterClient note:</b>
   * Returns " " for InterBase 4 and 5.
   * InterBase 4 and 5 do not support quoted SQL identifiers.
   *
   * @return the quoting string
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getIdentifierQuoteString () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // InterBase does not support quoted identifiers
    return " ";
  }

  /**
   * Get a comma separated list of all a database's SQL keywords
   * that are NOT also SQL92 keywords.
   *
   * @return the list
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getSQLKeywords () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    return
      "ACTIVE," +
      "AFTER," +
      "ASCENDING," +
      "BASE_NAME," +
      "BEFORE," +
      "BLOB," +
      "CACHE," +
      "CHECK_POINT_LENGTH," +
      "COMPUTED," +
      "CONDITIONAL," +
      "CONTAINING," +
      "CSTRING," +
      "DATABASE," +
      "RDB$DB_KEY," +
      "DEBUG," +
      "DESCENDING," +
      "DO," +
      "ENTRY_POINT," +
      "EXIT," +
      "FILE," +
      "FILTER," +
      "FUNCTION," +
      "GDSCODE," +
      "GENERATOR," +
      "GEN_ID," +
      "GROUP_COMMIT_WAIT_TIME," +
      "IF," +
      "INACTIVE," +
      "INPUT_TYPE," +
      "INDEX," +
      "LOGFILE," +
      "LOG_BUFFER_SIZE," +
      "MANUAL," +
      "MAXIMUM_SEGMENT," +
      "MERGE," +
      "MESSAGE," +
      "MODULE_NAME," +
      "NCHAR," +
      "NUM_LOG_BUFFERS," +
      "OUTPUT_TYPE," +
      "OVERFLOW," +
      "PAGE," +
      "PAGES," +
      "PAGE_SIZE," +
      "PARAMETER," +
      "PASSWORD," +
      "PLAN," +
      "POST_EVENT," +
      "PROTECTED," +
      "RAW_PARTITIONS," +
      "RESERV," +
      "RESERVING," +
      "RETAIN," +
      "RETURNING_VALUES," +
      "RETURNS," +
      "SEGMENT," +
      "SHADOW," +
      "SHARED," +
      "SINGULAR," +
      "SNAPSHOT," +
      "SORT," +
      "STABILITY," +
      "STARTS," +
      "STARTING," +
      "STATISTICS," +
      "SUB_TYPE," +
      "SUSPEND," +
      "TRIGGER," +
      "VARIABLE," +
      "RECORD_VERSION," +
      "WAIT," +
      "WHILE," +
      "WORK";
  }

  /**
   * Get a comma separated list of math functions.  These are the
   * X/Open CLI math function names used in the JDBC function escape
   * clause.
   *
   * <p><b>InterClient note:</b>
   * Returns "" for InterBase.
   * InterBase supports user-defined functions (UDFs) rather
   * than relying on built-in functions.
   *
   * @see #getUDFs()
   * @return the list
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getNumericFunctions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "";
  }

  /**
   * Get a comma separated list of string functions.  These are the
   * X/Open CLI string function names used in the JDBC function escape
   * clause.
   *
   * <p><b>InterClient note:</b>
   * Returns "" for InterBase.
   * InterBase supports user-defined functions (UDFs) rather
   * than relying on built-in functions.
   *
   * @see #getUDFs()
   * @return the list
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getStringFunctions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "";
  }

  /**
   * Get a comma separated list of system functions.  These are the
   * X/Open CLI system function names used in the JDBC function escape
   * clause.
   *
   * <p><b>InterClient note:</b>
   * Returns "" for InterBase.
   * InterBase supports user-defined functions (UDFs) rather
   * than relying on built-in functions.
   *
   * @see #getUDFs()
   * @return the list
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getSystemFunctions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "";
  }

  /**
   * Get a comma separated list of time and date functions.
   *
   * <p><b>InterClient note:</b>
   * Returns "" for InterBase.
   * InterBase supports user-defined functions (UDFs) rather
   * than relying on built-in functions.
   *
   * @see #getUDFs()
   * @return the list
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getTimeDateFunctions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "";
  }

  /**
   * This is the string that can be used to escape the wildcard
   * characters '_' or '%' in the string pattern style catalog search parameters.
   *
   * <P>The '_' character represents any single character.
   * <P>The '%' character represents any sequence of zero or 
   * more characters.
   *
   * <p><b>InterClient note:</b>
   * Returns "\" for InterBase.
   *
   * @return the string used to escape wildcard characters
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getSearchStringEscape () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "\\";
  }

  /**
   * Get all the "extra" characters that can be used in unquoted
   * identifier names (those beyond a-z, A-Z, 0-9 and _).
   *
   * <p><b>InterClient note:</b>
   * Returns "$" for InterBase.
   *
   * @return the string containing the extra characters
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getExtraNameCharacters () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "$";
  }

  // *** Functions describing which features are supported ***

  /**
   * Is "ALTER TABLE" with add column supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsAlterTableWithAddColumn () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is "ALTER TABLE" with drop column supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsAlterTableWithDropColumn () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is column aliasing supported.
   * <p>
   * If so, the SQL AS clause can be used to provide names for
   * computed columns or to provide alias names for columns as
   * required.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsColumnAliasing () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // Technically, this is a lie according to Dave Schnepper,
    // since SQL AS isn't supported everywhere in InterBase.
    return true;
  }

  /**
   * Are concatenations between NULL and non-NULL values NULL.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean nullPlusNonNullIsNull () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is the CONVERT function between SQL types supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase supports this function using the CAST operator.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsConvert () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // This is a shame since InterBase supports the CAST operator.
    return false;
  }

  /**
   * Is CONVERT between the given SQL types supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase supports this function using the CAST operator.
   *
   * @param fromType the type to convert from
   * @param toType the type to convert to     
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsConvert (int fromType, int toType) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    // This is a shame since InterBase supports the CAST operator.
    return false;
  }

  /**
   * Are table correlation names supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsTableCorrelationNames () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * If table correlation names are supported, are they restricted
   * to be different from the names of the tables.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsDifferentTableCorrelationNames () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Are expressions in "ORDER BY" lists supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsExpressionsInOrderBy () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can an "ORDER BY" clause use columns not in the SELECT.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOrderByUnrelated () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is some form of "GROUP BY" clause supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsGroupBy () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Can a "GROUP BY" clause use columns not in the SELECT.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsGroupByUnrelated () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Can a "GROUP BY" clause add columns not in the SELECT
   * provided it specifies all the columns in the SELECT.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsGroupByBeyondSelect () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is the escape character in "LIKE" clauses supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsLikeEscapeClause () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are multiple ResultSets from a single execute supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsMultipleResultSets () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can we have multiple transactions open at once (on different connections).
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsMultipleTransactions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Can columns be defined as non-nullable.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsNonNullableColumns () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is the ODBC Minimum SQL grammar supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsMinimumSQLGrammar () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // Technically, this is a lie for 4.0 since 4.0 doesn't
    // support DROP TABLE CASCADE/RESTRICT
    return true;
  }

  /**
   * Is the ODBC Core SQL grammar supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCoreSQLGrammar () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // Technically, this is a lie for 4.0 since we don't 
    // support DROP VIEW CASCADE/RESTRICT, or REVOKE CASCADE/RESTRICT
    // or qualifier names, or qualified base table identifiers,
    // or qualified table names.
    return true;
  }

  /**
   * Is the ODBC Extended SQL grammar supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsExtendedSQLGrammar () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Is the ANSI92 entry level SQL grammar supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsANSI92EntryLevelSQL () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // Technically, this is a lie ;-/
    return true;
  }

  /**
   * Is the ANSI92 intermediate SQL grammar supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsANSI92IntermediateSQL () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Is the ANSI92 full SQL grammar supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsANSI92FullSQL () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Is the SQL Integrity Enhancement Facility supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsIntegrityEnhancementFacility () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Is some form of outer join supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOuterJoins () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are full nested outer joins supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsFullOuterJoins () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is there limited support for outer joins.  (This will be true
   * if supportFullOuterJoins is true.)
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsLimitedOuterJoins () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * What's the database vendor's preferred term for "schema".
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support schemas.
   *
   * @return the vendor term
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getSchemaTerm () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__schemas__);
  }
 
  /**
   * What's the database vendor's preferred term for "procedure".
   *
   * <p><b>InterClient note:</b>
   * Returns "PROCEDURE" for InterBase.
   *
   * @return the vendor term
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public String getProcedureTerm () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "PROCEDURE";
  }
 
  /**
   * What's the database vendor's preferred term for "catalog".
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support catalogs.
   *
   * @return the vendor term
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getCatalogTerm () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  /**
   * Does a catalog appear at the start of a qualified table name.
   * (Otherwise it appears at the end)
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support catalogs.
   *
   * @return true if it appears at the start
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public boolean isCatalogAtStart () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  /**
   * What's the separator between catalog and table name.
   *
   * <p><b>InterClient note:</b>
   * Always returns "".
   * InterBase does not support catalogs.
   *
   * @return the separator string
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getCatalogSeparator () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return "";
    //throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  /**
   * Can a schema name be used in a data manipulation statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support schemas.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSchemasInDataManipulation () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a schema name be used in a procedure call statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support schemas.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSchemasInProcedureCalls () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a schema name be used in a table definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support schemas.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSchemasInTableDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a schema name be used in an index definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support schemas.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSchemasInIndexDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a schema name be used in a privilege definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support schemas.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSchemasInPrivilegeDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a catalog name be used in a data manipulation statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support catalogs.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCatalogsInDataManipulation () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a catalog name be used in a procedure call statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support catalogs.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCatalogsInProcedureCalls () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a catalog name be used in a table definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support catalogs.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCatalogsInTableDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a catalog name be used in an index definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support catalogs.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCatalogsInIndexDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can a catalog name be used in a privilege definition statement.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * InterBase does not support catalogs.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCatalogsInPrivilegeDefinitions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  // !!! Use a setFetchSize of 1 for positioned deletes to work
  /**
   * Is positioned DELETE supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase/InterClient.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsPositionedDelete () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  // !!! Use a setFetchSize of 1 for positioned deletes to work
  /**
   * Is positioned UPDATE supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase/InterClient.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsPositionedUpdate () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  // !!! don't we need to set fetch size = 1 for this to work?
  // !!! we may need to return false here since the driver doesn't look at SQL?
  /**
   * Is SELECT for UPDATE supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return true if so
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSelectForUpdate () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  // !!! shouldn't this be true??? this is a driver property, not a db property
  /**
   * Are stored procedure calls using the stored procedure escape syntax supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsStoredProcedures () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are subqueries in comparison expressions supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSubqueriesInComparisons () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are subqueries in 'exists' expressions supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSubqueriesInExists () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are subqueries in 'in' statements supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSubqueriesInIns () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are subqueries in quantified expressions supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsSubqueriesInQuantifieds () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are correlated subqueries supported.
   *
   * <p><b>Compliance note:</b>
   * must return true.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsCorrelatedSubqueries () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is SQL UNION supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsUnion () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is SQL UNION ALL supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsUnionAll () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Can cursors remain open across commits. 
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if cursors always remain open; false if they might not remain open
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOpenCursorsAcrossCommit () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can cursors remain open across rollbacks.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if cursors always remain open; false if they might not remain open
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOpenCursorsAcrossRollback () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Can statements remain open across commits.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if statements always remain open; false if they might not remain open
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOpenStatementsAcrossCommit () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Can statements remain open across rollbacks.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if statements always remain open; false if they might not remain open
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsOpenStatementsAcrossRollback () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  //----------------------------------------------------------------------
  // The following group of methods exposes various limitations
  // based on the target database with the current driver.
  // Unless otherwise specified, a result of zero means there is no
  // limit, or the limit is not known.

  /**
   * How many hex characters can you have in an inline binary literal.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support binary literals.
   *
   * @return max literal length
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public int getMaxBinaryLiteralLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__binary_literals__);
  }

  /**
   * What's the max length for a character literal.
   *
   * <p><b>InterClient note:</b>
   * Returns 1,024 (1K) for InterBase.
   *
   * @return max literal length
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxCharLiteralLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 1024; // See dsql/preparse.c line 91,540
  }

  /**
   * What's the limit on column name length.
   *
   * <p><b>InterClient note:</b>
   * Returns 31 for InterBase.
   *
   * @return max literal length
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnNameLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 31;
  }

  /**
   * What's the maximum number of columns in a "GROUP BY" clause.
   *
   * <p><b>InterClient note:</b>
   * Returns 16 for InterBase.
   *
   * @return max number of columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnsInGroupBy () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 16;
  }

  /**
   * What's the maximum number of columns allowed in an index.
   *
   * <p><b>InterClient note:</b>
   * Returns 16 for InterBase.
   *
   * @return max columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnsInIndex () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 16;
  }

  /**
   * What's the maximum number of columns in an "ORDER BY" clause.
   *
   * <p><b>InterClient note:</b>
   * Returns 16 for InterBase.
   *
   * @return max columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnsInOrderBy () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 16;
  }

  /**
   * What's the maximum number of columns in a "SELECT" list.
   *
   * <p><b>InterClient note:</b>
   * Returns 32,767 for InterBase.
   *
   * @return max columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnsInSelect () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    // Some say 65328, Dave S is more comfortable saying 32767
    return 32767;
  }

  /**
   * What's the maximum number of columns in a table.
   *
   * <p><b>InterClient note:</b>
   * Returns 32,767 for InterBase.
   *
   * @return max columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxColumnsInTable () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    // Some say 65328, Dave Schnepper is more comfortable saying 32767
    return 32767;
  }

  /**
   * How many active connections can we have at a time to this database.
   *
   * <p><b>InterClient note:</b>
   * Returns 0 (unlimited) for InterBase.
   * This depends mostly on the OS configuration (kernel, RAM, etc...).
   * Practical limits are around 150 using NT-V4.2.
   *
   * @return max connections
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxConnections () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 0;
  }

  /**
   * What's the maximum cursor name length.
   *
   * <p><b>InterClient note:</b>
   * Return 31 for InterBase.
   *
   * @return max cursor name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxCursorNameLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 31;
  }

  /**
   * What's the maximum length of an index (in bytes).	
   *
   * <p><b>InterClient notes:</b><br>
   * Returns 198 bytes, but see comments below.
   * <p>
   * Actually, there is no single hard-code value for this.
   * We can calculate the maximum size as follows.
   * We have 256 bytes to fill, but that has to include column indicators as well as any column padding.
   * If the 256 bytes were filled,  1/5 or 51 bytes would be column indicators.  There is then one
   * slop byte which is good for nothing; we could fill it with a column indicator, but it would
   * have nothing to indicate.  Then there's the padding: in the worst case we would have 3 bytes
   * of padding for each column.
   * <p>
   * So a calculation of the maximum amount of user data which could fit in an index key
   * is as follows:
   * <pre>
   * 256 available bytes - 1 slop byte - 51 column indicator bytes - 3 * number columns
   * </pre>
   * So the maximum length you can have for a multi-column index is as follows:
   * <pre>
   * 2 columns:  198 bytes
   * 3 columns:  195 bytes
   * 4 columns:  192 bytes
   * </pre>
   * and so on.  Note that your mileage may vary.  You may get up to an additional 3*n bytes
   * for an n-column index, if all columns happen to round to a multiple of 4 bytes long.  But
   * I wouldn't count on it.  Best to publish these as hard limits.
   * <p>
   * INTERNATIONAL ISSUES
   * <p>
   * The maximum number of characters you can fit in an index varies depending on the character
   * set.  The above calculations give the number of bytes, which is good for ASCII data.  Multibyte
   * character sets can get sticky.  Customers interested in internationalization will have to know
   * the length of their keys in bytes, not characters.
   * <p>
   * This information was supplied by David "Deej" Bredenberg.
   *
   * @return max index length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxIndexLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    return 198;

    // There has been so much confusion over the years about the length of
    // an index key, I hope this email will finally put it to rest.  The maximum
    // amount of data we can store in a single-column index is 256 bytes.  The
    // maximum length of a multi-column index varies.  It will take the rest
    // of this long email to explain why.
    //
    // Why the 256 byte limit?
    //
    // The 256 byte length is a hard limit imposed by the fact that we use a byte to
    // store the length of the key.  This length byte is stored once for every entry in
    // the index.  For indices which have very small keys, this is the most efficient
    // use of space.  The implementor of indices probably surmised that 256 bytes
    // was not a very efficient key anyway, which is true.  Users should be encouraged
    // to keep their keys as small as possible.  It limits the depth of indices, which
    // increases their efficiency.
    //
    // The tricky part comes in when talking about multi-column indices.  I
    // have heard 209 bytes bandied about as the hard limit on the length of
    // multicolumn keys.  I think this is apocryphal; I seem to remember Ann Harrison
    // decreeing it just after revising the implementation of multicolumn indices.
    //
    // I also remember customers and support engineers complaining that they couldn't
    // even get 209 bytes in certain cases.  R&D of course scoffed, since it was written
    // in stone that 209 bytes will fit.  Must be user error.  I suppose this is a lesson
    // about ignoring conventional wisdom.  Instead, listen to your friendly customer
    // support engineer.
    //
    // Here's the thing: there is no hard and fast limit, which may be the source
    // of the confusion.  In fact, the answer is actually different depending on how
    // many columns you have.  For the truly technical or just the trivially minded,
    // let's go through the calculation of how many bytes you can store in a multicolumn
    // index.  Those who don't care about such minutiae can skip to the table at the end.
    //
    // HISTORY
    //
    // Somewhere around the version 2.5 timeframe, various customers started
    // to complain that records were being retrieved that they didn't ask for, or maybe it
    // was that they weren't getting records they asked for, I don't remember which.  Senility
    // does that to you.  The culprit seemed to be when a multicolumn index was used
    // to "optimize" (obfuscate?) the retrieval.  Ann discovered that generating a multicolumn
    // key from a record was not a one-to-one relationship.  I.e., different column values
    // could generate the same key.  This was because we were just concatenating the values
    // of the fields together to form the key.
    //
    // As an example, take the following two records and their concatenated keys:
    //
    // TITLE      CRIME
    // -----------  --------
    // therapist   <null>
    // the            rapist
    //
    // The resultant multicolumn key on these two columns is "therapist".  So anyone searching
    // for a therapist would find "the rapist" as well.  Since rapists don't know much about
    // multiple-personality syndrome, obviously we needed to generate a key which
    // discriminates between these two records.
    //
    // IMPLEMENTATION
    //
    // (Note: I take no "credit" for this design.  Those who love it can send their fan mail to Ann.)
    //
    // In order to identify each segment in a key, a column indicator byte is used to tag every four
    // bytes of data.  This byte gives the segment number of the column that the following four bytes
    // belong in.  If a segment has a length which is not a multiple of four, it is padded up to the nearest
    // multiple of four.
    //
    // Now here's the really tricky part: for purposes of key generation, segments are numbered in
    // decreasing order.  So for a two-column index, the first column is segment number 2, and the
    // second column is segment number 1.  This is done so that the keys will sort correctly as is,
    // without having to extract the data.
    //
    // Using the above example, the two keys would be encoded as:
    //
    // 2ther2apis2t000
    // 2the01rapi1st00
    //
    // Now the resultant keys are clearly different, so they don't compute as equal anymore.  Just
    // as important, "the rapist" is now sorted before "therapist", which is what you want since "the"
    // comes before "therapist" in the dictionary.
    //
    // CALCULATION
    //
    // So now that we know the implementation, we can calculate the maximum size of the key.
    // We have 256 bytes to fill, but that has to include column indicators as well as any column padding.
    // If the 256 bytes were filled,  1/5 or 51 bytes would be column indicators.  There is then one
    // slop byte which is good for nothing; we could fill it with a column indicator, but it would
    // have nothing to indicate.  Then there's the padding: in the worst case we would have 3 bytes
    // of padding for each column.
    //
    // So a calculation of the maximum amount of user data which could fit in an index key
    // is as follows:
    //
    // 256 available bytes - 1 slop byte - 51 column indicator bytes - 3 * number columns
    //
    // So the maximum length you can have for a multi-column index is as follows:
    //
    // 2 columns:  198 bytes
    // 3 columns:  195 bytes
    // 4 columns:  192 bytes
    //
    // and so on.  Note that your mileage may vary.  You may get up to an additional 3*n bytes
    // for an n-column index, if all columns happen to round to a multiple of 4 bytes long.  But
    // I wouldn't count on it.  Best to publish these as hard limits.
    //
    // INTERNATIONAL ISSUES
    //
    // The maximum number of characters you can fit in an index varies depending on the character
    // set.  The above calculations give the number of bytes, which is good for ASCII data.  Multibyte
    // character sets can get sticky.  Customers interested in internationalization will have to know
    // the length of their keys in bytes, not characters.
    //
    // ALTERNATIVE SOLUTIONS
    //
    // This section is for any enterprising engineer who is interested in improving this design.
    // Here are some of the possible solutions:
    //
    // 1. Why not just put one column indicator byte at the beginning of each segment?
    //
    // Because we have no way of knowing what is an indicator and what is ordinary data, unless
    // we insert the indicator in a known location.
    //
    // 2. Why not have five or six or more bytes of data for each indicator?
    //
    // Because that would increase the amount of padding we have to do per key.
    //
    // 3. Why not have a single column indicator for each segment, followed by a length byte and
    // then the data for that column?
    //
    // This would work well for long columns, not so well for short columns.  In any case, Ann was
    // trying to retain a significant property of index keys:  that they are directly comparable by doing
    // a byte-by-byte comparison.  This makes key comparison fast and easy.
    //
    // David Bredenberg
  }

  /**
   * What's the maximum length allowed for a schema name.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support schemas.
   *
   * @return max name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public int getMaxSchemaNameLength() throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__schemas__);
  }

  /**
   * What's the maximum length of a procedure name.
   *
   * <p><b>InterClient note:</b>
   * Returns 27 for InterBase.
   *
   * @return max name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxProcedureNameLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // There is some bug which does not allow the usual 31
    return 27;
  }

  /**
   * What's the maximum length of a catalog name.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support catalogs.
   *
   * @return max name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public int getMaxCatalogNameLength() throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  /**
   * What's the maximum length of a single row.
   *
   * <p><b>InterClient note:</b>
   * Returns 32,664 bytes for InterBase.
   *
   * @return max row size in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxRowSize () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 32664;
  }

  /**
   * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY blobs.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   * There's virtually no limit to the size of a blob, other than
   * the practical limit the OS imposes on the size of a single file (eg. 2 gig
   * on 32-bit unixes).
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean doesMaxRowSizeIncludeBlobs () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * What's the maximum length of a SQL statement.
   *
   * <p><b>InterClient note:</b>
   * Returns 640 bytes for InterBase.
   *
   * @return max length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxStatementLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // !!! Is this really true, this is small!
    return 640;
  }

  /**
   * How many active statements can we have open at one time to this database.
   *
   * <p><b>InterClient note:</b>
   * Returns 0 (unlimited) for InterBase.
   *
   * @return the maximum
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxStatements () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 0;
  }

  /**
   * What's the maximum length of a table name.
   *
   * <p><b>InterClient note:</b>
   * Returns 31 for InterBase.
   *
   * @return max name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxTableNameLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 31;
  }

  /**
   * What's the maximum number of tables in a SELECT.
   *
   * <p><b>InterClient note:</b>
   * Returns 16 for InterBase.
   *
   * @return the maximum
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxTablesInSelect () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // According to Bill Karwin, it may be more 
    // practical to enforce a lower limit such as 12
    return 16;
  }

  /**
   * What's the maximum length of a user name.
   *
   * <p><b>InterClient note:</b>
   * Returns 31 for InterBase.
   *
   * @return max name length in bytes
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getMaxUserNameLength () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return 31;
  }

  /**
   * What's the database's default transaction isolation level.  The
   * values are defined in java.sql.Connection.
   *
   * <p><b>InterClient note:</b>
   * Returns Connection.TRANSACTION_SERIALIZABLE for InterBase.
   *
   * @see Connection
   * @return the default isolation level
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getDefaultTransactionIsolation () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return connection_.TRANSACTION_SERIALIZABLE;
  }

  /**
   * Are transactions supported. If not, commit is a noop and the
   * isolation level is TRANSACTION_NONE.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase.
   *
   * @return true if transactions are supported
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsTransactions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Does the database support the given transaction isolation level.
   *
   * <p><b>InterClient note:</b>
   * InterBase supports the JDBC isolation levels
   * {@link Connection#TRANSACTION_REPEATABLE_READ TRANSACTION_REPEATABLE_READ},
   * {@link Connection#TRANSACTION_READ_COMMITTED TRANSACTION_READ_COMMITTED}, and
   * {@link Connection#TRANSACTION_SERIALIZABLE TRANSACTION_SERIALIZABLE}.
   * <p>
   *
   * In addition, InterBase's native isolation modes will be supported as
   * extensions to the JDBC api
   * <ul>
   * <li> TRANSACTION_READ_COMMITTED_REC_VERSION (aka TRANSACTION_READ_COMMITTED)
   * <li> TRANSACTION_READ_COMMITTED_NO_REC_VERSION
   * <li> TRANSACTION_SNAPSHOT (aka TRANSACTION_SERIALIZABLE)
   * <li> TRANSACTION_SNAPSHOT_TABLE_STABILITY
   * </ul>
   *
   * @see Connection#setTransactionIsolation
   * @param level the values are defined in interbase.interclient.Connection
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsTransactionIsolationLevel (int level) throws java.sql.SQLException
  {
    checkForClosedConnection ();

    switch (level) {
    case Connection.TRANSACTION_REPEATABLE_READ:
    case Connection.TRANSACTION_READ_COMMITTED:
    case Connection.TRANSACTION_SERIALIZABLE: // aka TRANSACTION_SNAPSHOT
    case Connection.TRANSACTION_SNAPSHOT_TABLE_STABILITY:  // consistency
      return true;
    case Connection.TRANSACTION_NONE:
    case Connection.TRANSACTION_READ_UNCOMMITTED:
      return false;
    default:
      throw new InvalidArgumentException (ErrorKey.invalidArgument__isolation_0__,
					  String.valueOf (level));
    }
  }

  // Rejected design alternative -
  // Connection.setAutoCommitDDL (boolean enable) 
  // ********************************************
  // * Enable/disable Auto-commit for DDL statements only.
  // * <p>
  // * Even when the JDBC standard autocommit mode is disabled
  // * the default behaviour for InterClient (and InterBase isql) 
  // * is to autocommit DDL.
  // * <p>
  // * For example, consider
  // * <pre>
  // * s.executeUpdate ("create table foo (bar DATE)");
  // * s.executeUpdate ("insert into foo (bar) values ('now')");
  // * </pre>
  // * With both auto-commit and auto-commit DDL disabled, the SQL insert
  // * statement will fail with "table foo unknown".
  // * This is because InterBase delays the creation of table foo
  // * until the SQL create statement is committed.
  // * InterBase DDL does not follow the snapshot model.
  // * <p>
  // * Both isql and InterClient overcome this limitation by
  // * executing DDL statements, such as the SQL create table, 
  // * in a separate autocommitted transaction.
  // * In addition, the implicit server-side prepare of the SQL insert,
  // * must also be executed in this same autocommitted transaction so
  // * that table foo is visible in a new snapshot.
  // * However, the actual execution of the SQL insert is under
  // * control of the user's transaction.
  // * <p>
  // * For example, consider
  // * <pre>
  // * c.setAutoCommit (false);
  // * c.setAutoCommitDDL (true); // if this were false the insert would fail.
  // * s.executeUpdate ("create table foo (bar DATE)");
  // * s.executeUpdate ("insert into foo (bar) values ('now')");
  // * c.rollback ();
  // * </pre>
  // * In this case, the SQL insert is rolled back, 
  // * but the SQL create is committed.
  // * <p>
  // * <b>Note:</b> If an application does not mix DDL and DML, then server overhead
  // * can be reduced if the default auto-commit DDL mode is disabled.
  // * However, you cannot enable auto-commit and
  // * disable auto-commit DDL simulataneously.
  // * 
  // * @since <font color=red>Extension</font>
  // * @throws java.sql.SQLException if a database access error occurs.
  // * @docauthor po
  // ********************************************
  // 
  // po- Reason for rejection:
  // <ul>
  // <li> It's not natural, it requires 3 separate transactions to implement.
  //      See Ed Simon's proposal for isql.  The third transaction is required
  //      for catalog queries (SHOW TABLES in isql, DatabaseMetaData.getTables() in JDBC).  
  // <li> Unless there's a clear reason to do otherwise, keep it simple.
  //      In this case, it fixes one hole but creates another:
  //      Even with the fix, the DDL does not follow the interbase transaction model
  //      because the autocommitted DDL cannot be rolled back,
  //      and any future API or SQL extensions that reference metadata like
  //      DatabaseMetaData.getTables() would also have to be run in a separate transaction.
  //      Let's face it, InterBase does not keep DDL under transaction control
  //      so let's not try to fake it!
  // <li> JDBC has no support for the isql model.
  //      JDBC surfaces the method
  //      <ul>
  //      <li> DatabaseMetaData.dataDefinitionCausesTransactionCommit()
  //      </ul>
  //      to detect RDBMSs with our characteristics.
  //      Using the rejected alternative, 
  //      DDL "kind of" causes transaction commit (DDL committed),
  //      and "kind of" doesn't (DML not committed).
  //      JDBC has no model for this, nor should it.
  // </ul>

  /**
   * Are both data definition and data manipulation statements
   * within a transaction supported.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterBase, but DDL forces an implicit commit.
   *
   * @see #dataDefinitionCausesTransactionCommit()
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsDataDefinitionAndDataManipulationTransactions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Are only data manipulation statements within a transaction supported.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean supportsDataManipulationTransactionsOnly () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  /**
   * Does a data definition statement within a transaction force the
   * transaction to commit.
   *
   * <p><b>InterClient note:</b>
   * Returns true for InterClient.
   * InterClient does not follow the ISQL transaction model.
   * Under the ISQL transaction model, in a mixed DDL/DML transaction,
   * the DDL is autocommitted even if you're not in autocommit mode,
   * but the DML is not autocommitted.
   * So if a rollback occurs, only the DML is rollbacked, hence
   * a mixed DDL/DML transaction is not atomic.
   * Under the InterClient transaction
   * model for a mixed DDL/DML transaction, any DDL forces the entire
   * transaction to commit implicitly, this includes all DML up to
   * and including the DDL atomically.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean dataDefinitionCausesTransactionCommit () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return true;
  }

  /**
   * Is a data definition statement within a transaction ignored.
   *
   * <p><b>InterClient note:</b>
   * Returns false for InterBase.
   *
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean dataDefinitionIgnoredInTransactions () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    return false;
  }

  // *********************** System Table Queries *************************

  private void systemTableQueryPreamble (String catalog,
				         String schemaPattern) throws java.sql.SQLException
  {
    if ((catalog != null &&
         !catalog.equals ("")))
      throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);

    if ((schemaPattern != null &&
         !schemaPattern.equals ("") &&
         !schemaPattern.equals ("%")))
      throw new DriverNotCapableException (ErrorKey.driverNotCapable__schemas__);

    // systemTableQuery_ is put onto the connections list of open statement, 
    // so it will be closed automatically when the connection is closed.
    // !! This statement needs to be associated with a result set on each request.
    if (systemTableQuery_ == null)
      systemTableQuery_  = connection_.createStatement ();
  }

  private java.sql.ResultSet remoteCatalogQuery (MessageBufferOutputStream sendMsg,
						 int resultCols, 
						 String[] resultColumnNames,
						 boolean[] resultNullables,
						 int[] resultTypes, 
						 int[] resultPrecisions, 
						 int[] resultScales) throws java.sql.SQLException
  {
    ResultSet resultSet = null;
    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      int statementRef = recvMsg.readInt ();

      resultSet = new ResultSet (connection_,
				 statementRef,
				 recvMsg, 
				 jdbcNet_,
				 resultCols, 
				 resultColumnNames,
				 resultNullables,
				 resultTypes, 
				 resultPrecisions, 
				 resultScales);
      resultSet.numRows_ = recvMsg.readInt ();

      resultSet.setNumDataPositions (resultCols);
      resultSet.saveRowPosition ();
      // recvMsg remains associated with resultSet_

    }
    catch (java.sql.SQLException e) {
      if (resultSet != null)
        resultSet.local_Close ();
      jdbcNet_.destroyRecvMessage (recvMsg);
      throw e;
    }

    return resultSet;
  }

  // Note: The following catalog opcodes must match those in IB_Catalog.h
  static final private int CATALOG_GET_PROCEDURES__ = 1;
  static final private int CATALOG_GET_PROCEDURE_COLUMNS__ = 2;
  static final private int CATALOG_GET_TABLES__ = 3;
  static final private int CATALOG_GET_TABLE_TYPES__ = 14;
  static final private int CATALOG_GET_COLUMNS__ = 4;
  static final private int CATALOG_GET_COLUMN_PRIVILEGES__ = 5;
  static final private int CATALOG_GET_TABLE_PRIVILEGES__ = 6;
  static final private int CATALOG_GET_BEST_ROW_IDENTIFIER__ = 7;
  static final private int CATALOG_GET_VERSION_COLUMNS__ = 8;
  static final private int CATALOG_GET_PRIMARY_KEYS__ = 9;
  static final private int CATALOG_GET_IMPORTED_KEYS__ = 10;
  static final private int CATALOG_GET_EXPORTED_KEYS__ = 11;
  static final private int CATALOG_GET_CROSS_REFERENCE__ = 12;
  static final private int CATALOG_GET_INDEX_INFO__ = 13;
  static final private int CATALOG_GET_TYPE_INFO__ = 15;

  static final private int REMARKS_PRECISION__ = 31;

  // ************* Canned MetaData for each of the Catalog Result Sets ************

  // ********************** getProcedures *********************
  static final private int GET_PROCEDURES_RESULT_COLS__ = 9;

  static final private String[] GET_PROCEDURES_RESULT_COLUMN_NAMES__ 
  =  {"PROCEDURE_CAT",    // col 1
      "PROCEDURE_SCHEM",
      "PROCEDURE_NAME",
      "reserved",
      "reserved",         // col 5
      "reserved",
      "REMARKS",
      "PROCEDURE_TYPE",
      "PROCEDURE_OWNER"}; // col 9

  static final private boolean[] GET_PROCEDURES_RESULT_NULLABLES__
  = {true,   // col 1
     true, 
     false, 
     true, 
     true,  // col 5
     true, 
     true, 
     false, 
     false}; // col 9

  static final private int[] GET_PROCEDURES_RESULT_TYPES__
  = {IBTypes.VARCHAR__,       // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.NULL_TYPE__,
     IBTypes.NULL_TYPE__,  // col 5
     IBTypes.NULL_TYPE__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,
     IBTypes.VARCHAR__};      // col 9

  static final private int[] GET_PROCEDURES_RESULT_PRECISIONS__ 
  = {0,   // col 1
     0,
     31,
     0,
     0,   // col 5
     0,
     REMARKS_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     31}; // col 9

  static final private int[] GET_PROCEDURES_RESULT_SCALES__ 
  = {0,  // col 1
     0,
     0,
     0,
     0,  // col 5
     0,
     0,
     0,
     0}; // col 9

  /**
   * Get a description of stored procedures available in a catalog.
   *
   * <P>Only procedure descriptions matching the schema and
   * procedure name criteria are returned.  They are ordered by
   * PROCEDURE_SCHEM, and PROCEDURE_NAME.
   *
   * <P>Each procedure description has the the following columns:
   * <OL>
   * <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
   * <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
   * <LI><B>PROCEDURE_NAME</B> String => procedure name
   * <LI> reserved for future use
   * <LI> reserved for future use
   * <LI> reserved for future use
   * <LI><B>REMARKS</B> String => explanatory comment on the procedure
   * <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
   *      <UL>
   *      <LI> procedureResultUnknown - May return a result
   *      <LI> procedureNoResult - Does not return a result
   *      <LI> procedureReturnsResult - Returns a result
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * InterClient provides an additional 9th column as <b>PROCEDURE_OWNER</b>.
   * A SQLException is thrown if a catalog or schema pattern is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those without a schema
   * @param procedureNamePattern a procedure name pattern 
   * @return ResultSet - each row is a procedure description 
   * @see #getSearchStringEscape() 
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getProcedures (String catalog,
					                String schemaPattern,
					                String procedureNamePattern) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schemaPattern);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_PROCEDURES__);
    // !!! change remoteX and SQL to not pass pattern and use LIKE clause.
    // !!! and repeat this for all metadata functions that use patterns
    if (procedureNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(procedureNamePattern) );

    return remoteCatalogQuery (sendMsg, 
			       GET_PROCEDURES_RESULT_COLS__, 
			       GET_PROCEDURES_RESULT_COLUMN_NAMES__,
			       GET_PROCEDURES_RESULT_NULLABLES__,
			       GET_PROCEDURES_RESULT_TYPES__, 
			       GET_PROCEDURES_RESULT_PRECISIONS__, 
			       GET_PROCEDURES_RESULT_SCALES__);
  }

  /**
   * PROCEDURE_TYPE - May return a result.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureResultUnknown = 0;

  /**
   * PROCEDURE_TYPE - Does not return a result.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureNoResult = 1;
    
  /**
   * PROCEDURE_TYPE - Returns a result.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureReturnsResult = 2;

  // ********************** getProcedureColumns *********************
  static final private int GET_PROCEDURE_COLUMNS_RESULT_COLS__ = 13;

  static final private String[] GET_PROCEDURE_COLUMNS_RESULT_COLUMN_NAMES__ 
  =  {"PROCEDURE_CAT",    // col 1
      "PROCEDURE_SCHEM",
      "PROCEDURE_NAME",
      "COLUMN_NAME",
      "COLUMN_TYPE",      // col 5
      "DATA_TYPE",
      "TYPE_NAME",
      "PRECISION",
      "LENGTH",
      "SCALE",            // col 10
      "RADIX",
      "NULLABLE",
      "REMARKS"};         // col 13

  static final private boolean[] GET_PROCEDURE_COLUMNS_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     false,   // col 5
     false, 
     false, 
     false, 
     false, 
     false,   // col 10
     false, 
     false, 
     true};   // col 13

  static final private int[] GET_PROCEDURE_COLUMNS_RESULT_TYPES__
  = {IBTypes.VARCHAR__,      // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,  // col 5
     IBTypes.SMALLINT__,
     IBTypes.VARCHAR__,
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,
     IBTypes.SMALLINT__,  // col 10
     IBTypes.SMALLINT__,
     IBTypes.SMALLINT__,
     IBTypes.VARCHAR__};  // col 13

  static final private int[] GET_PROCEDURE_COLUMNS_RESULT_PRECISIONS__ 
  = {0,  // col 1
     0,
     31,
     31,
     IBTypes.SMALLINT_PRECISION__,  // col 5
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.TYPE_NAME_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,  // col 10
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     REMARKS_PRECISION__}; // col 13

  static final private int[] GET_PROCEDURE_COLUMNS_RESULT_SCALES__ 
  = {0,  // col 1
     0,
     0,
     0,
     0,  // col 5
     0,
     0,
     0,
     0,
     0,  // col 10
     0,
     0,
     0}; // col 13

  /**
   * Get a description of a catalog's stored procedure parameters and result columns.
   *
   * <P>Only descriptions matching the schema, procedure and
   * parameter name criteria are returned.  They are ordered by
   * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
   * if any, is first. Next are the parameter descriptions in call
   * order. The column descriptions follow in column number order.
   *
   * <P>Each row in the ResultSet is a parameter description or
   * column description with the following fields:
   *  <OL>
   *	<LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
   *	<LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
   *	<LI><B>PROCEDURE_NAME</B> String => procedure name
   *	<LI><B>COLUMN_NAME</B> String => column/parameter name
   *	<LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
   *      <UL>
   *      <LI> procedureColumnUnknown - nobody knows
   *      <LI> procedureColumnIn - IN parameter
   *      <LI> procedureColumnInOut - INOUT parameter
   *      <LI> procedureColumnOut - OUT parameter
   *      <LI> procedureColumnReturn - procedure return value
   *      <LI> procedureColumnResult - result column in ResultSet
   *      </UL>
   *    <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
   *	<LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
   *  type name is fully qualified
   *	<LI><B>PRECISION</B> int => precision
   *	<LI><B>LENGTH</B> int => length in bytes of data
   *	<LI><B>SCALE</B> short => scale
   *	<LI><B>RADIX</B> short => radix
   *	<LI><B>NULLABLE</B> short => can it contain NULL?
   *      <UL>
   *      <LI> procedureNoNulls - does not allow NULL values
   *      <LI> procedureNullable - allows NULL values
   *      <LI> procedureNullableUnknown - nullability unknown
   *      </UL>
   *	<LI><B>REMARKS</B> String => comment describing parameter/column
   *  </OL>
   *
   * <P><B>Note:</B> Some databases may not return the column
   * descriptions for a procedure. Additional columns beyond
   * REMARKS can be defined by the database.
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema pattern is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those without a schema
   * @param procedureNamePattern a procedure name pattern
   * @param columnNamePattern a column name pattern
   * @return ResultSet - each row is a stored procedure parameter or column description
   * @see #getSearchStringEscape()
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getProcedureColumns (String catalog,
					                      String schemaPattern,
					                      String procedureNamePattern,
					                      String columnNamePattern) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schemaPattern);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_PROCEDURE_COLUMNS__);
    if (procedureNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(procedureNamePattern) );
    if (columnNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(columnNamePattern) );

    return remoteCatalogQuery (sendMsg, 
			       GET_PROCEDURE_COLUMNS_RESULT_COLS__, 
			       GET_PROCEDURE_COLUMNS_RESULT_COLUMN_NAMES__,
			       GET_PROCEDURE_COLUMNS_RESULT_NULLABLES__,
			       GET_PROCEDURE_COLUMNS_RESULT_TYPES__, 
			       GET_PROCEDURE_COLUMNS_RESULT_PRECISIONS__, 
			       GET_PROCEDURE_COLUMNS_RESULT_SCALES__);
  }

  /**
   * COLUMN_TYPE - nobody knows.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnUnknown = 0;

  /**
   * COLUMN_TYPE - IN parameter.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnIn = 1;

  /**
   * COLUMN_TYPE - INOUT parameter.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnInOut = 2;

  /**
   * COLUMN_TYPE - OUT parameter.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnOut = 4;

  /**
   * COLUMN_TYPE - procedure return value.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnReturn = 5;

  /**
   * COLUMN_TYPE - result column in ResultSet.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureColumnResult = 3;

  /**
   * TYPE NULLABLE - does not allow NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureNoNulls = 0;

  /**
   * TYPE NULLABLE - allows NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureNullable = 1;

  /**
   * TYPE NULLABLE - nullability unknown.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int procedureNullableUnknown = 2;


  // ********************** getTables *********************
  static final private int GET_TABLES_RESULT_COLS__ = 6;

  static final private String[] GET_TABLES_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",    // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "TABLE_TYPE",
      "REMARKS",      // col 5
      "TABLE_OWNER"};    

  static final private boolean[] GET_TABLES_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     true,    // col 5
     false};  

  static final private int[] GET_TABLES_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__, // col 5
     IBTypes.VARCHAR__};

  static final private int[] GET_TABLES_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     12,
     REMARKS_PRECISION__, // col 5
     31};

  static final private int[] GET_TABLES_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0}; 

  /**
   * Get a description of tables available in a catalog.
   *
   * <P>Only table descriptions matching the catalog, schema, table
   * name and type criteria are returned.  They are ordered by
   * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
   *
   * <P>Each table description has the following columns:
   *  <OL>
   *    <LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
   *			"VIEW",	"SYSTEM TABLE", "GLOBAL TEMPORARY",
   *			"LOCAL TEMPORARY", "ALIAS", "SYNONYM".
   *	<LI><B>REMARKS</B> String => explanatory comment on the table
   *  </OL>
   *
   * <P><B>Note:</B> Some databases may not return information for all tables.
   *
   * <p><b>InterClient note:</b>
   * InterClient provides an additional 6th column as <b>TABLE_OWNER</b>.
   * A SQLException is thrown if a catalog or schema pattern is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those
   * without a schema
   * @param tableNamePattern a table name pattern
   * @param types a list of table types to include; null returns all types
   * @return ResultSet - each row is a table description
   * @see #getSearchStringEscape()
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getTables (String catalog,
						    String schemaPattern,
						    String tableNamePattern,
						    String types []) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schemaPattern);

    boolean[] typesVector = new boolean [3];

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_TABLES__);
    if (tableNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(tableNamePattern) );

    // Consolidate table types to the 3 possible InterBase table types, represent as vector set
    if (types == null) {
      typesVector[0] = typesVector[1] = typesVector[2] = true;
    }
    else {
      for (int i=0; i < types.length; i++) {
        if ("TABLE".equals (types[i]))
          typesVector[0] = true;
        else if ("SYSTEM TABLE".equals (types[i]))
          typesVector[1] = true;
        else if ("VIEW".equals (types[i]))
          typesVector[2] = true;
      }
    }

    sendMsg.writeBoolean (typesVector [0]);
    sendMsg.writeBoolean (typesVector [1]);
    sendMsg.writeBoolean (typesVector [2]);

    return remoteCatalogQuery (sendMsg, 
			       GET_TABLES_RESULT_COLS__, 
			       GET_TABLES_RESULT_COLUMN_NAMES__,
			       GET_TABLES_RESULT_NULLABLES__,
			       GET_TABLES_RESULT_TYPES__, 
			       GET_TABLES_RESULT_PRECISIONS__, 
			       GET_TABLES_RESULT_SCALES__);
  }

  // !!! perhaps this should return an empty result set?
  /**
   * Get the schema names available in this database.  The results
   * are ordered by schema name.
   *
   * <P>The schema column is:
   *  <OL>
   *	<LI><B>TABLE_SCHEM</B> String => schema name
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support schemas.
   *
   * @return ResultSet - each row has a single String column that is a schema name
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public java.sql.ResultSet getSchemas () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__schemas__);
  }

  /**
   * Get the catalog names available in this database.  The results
   * are ordered by catalog name.
   *
   * <P>The catalog column is:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => catalog name
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support catalogs.
   *
   * @return ResultSet - each row has a single String column that is a catalog name
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public java.sql.ResultSet getCatalogs () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  // ********************** getTableTypes *********************
  static final private int GET_TABLE_TYPES_RESULT_COLS__ = 1;

  static final private String[] GET_TABLE_TYPES_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_TYPE"};

  static final private boolean[] GET_TABLE_TYPES_RESULT_NULLABLES__
  = {false};

  static final private int[] GET_TABLE_TYPES_RESULT_TYPES__
  = {IBTypes.VARCHAR__};

  static final private int[] GET_TABLE_TYPES_RESULT_PRECISIONS__
  = {12};

  static final private int[] GET_TABLE_TYPES_RESULT_SCALES__
  = {0}; 

  /**
   * Get the table types available in this database.  The results
   * are ordered by table type.
   *
   * <P>The table type is:
   *  <OL>
   *	<LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
   *			"VIEW",	"SYSTEM TABLE", "GLOBAL TEMPORARY",
   *			"LOCAL TEMPORARY", "ALIAS", "SYNONYM".
   *  </OL>
   *
   * @return ResultSet - each row has a single String column that is a table type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getTableTypes () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (null, null);

    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_TABLE_TYPES__);

    return remoteCatalogQuery (sendMsg, 
			       GET_TABLE_TYPES_RESULT_COLS__, 
			       GET_TABLE_TYPES_RESULT_COLUMN_NAMES__,
			       GET_TABLE_TYPES_RESULT_NULLABLES__,
			       GET_TABLE_TYPES_RESULT_TYPES__, 
			       GET_TABLE_TYPES_RESULT_PRECISIONS__, 
			       GET_TABLE_TYPES_RESULT_SCALES__);
  }

  // ********************** getColumns *********************
  static final private int GET_COLUMNS_RESULT_COLS__ = 18;

  static final private String[] GET_COLUMNS_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",          // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "COLUMN_NAME",
      "DATA_TYPE",          // col 5
      "TYPE_NAME",
      "COLUMN_SIZE",
      "BUFFER_LENGTH",
      "DECIMAL_DIGITS",
      "NUM_PREC_RADIX",     // col 10
      "NULLABLE",
      "REMARKS",
      "COLUMN_DEF",
      "SQL_DATA_TYPE",
      "SQL_DATETIME_SUB",   // col 15
      "CHAR_OCTET_LENGTH",
      "ORDINAL_POSITION",
      "IS_NULLABLE"};       // col 18

  static final private boolean[] GET_COLUMNS_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     false,   // col 5
     false, 
     false, 
     true, 
     false, 
     false,   // col 10
     false, 
     true, 
     true, 
     true, 
     true,   // col 15
     false, 
     false, 
     false};  // col 18

  static final private int[] GET_COLUMNS_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,    // col 5
     IBTypes.VARCHAR__,
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,    // col 10
     IBTypes.INTEGER__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,    // col 15
     IBTypes.INTEGER__,
     IBTypes.INTEGER__,
     IBTypes.VARCHAR__}; // col 18

  static final private int[] GET_COLUMNS_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     31,
     IBTypes.SMALLINT_PRECISION__,   // col 5
     IBTypes.TYPE_NAME_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,   // col 10
     IBTypes.INTEGER_PRECISION__,
     REMARKS_PRECISION__,
     0,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,   // col 15
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     3}; // col 18

  static final private int[] GET_COLUMNS_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0,
     0,
     0,   // col 10
     0,
     0,
     0,
     0,
     0,   // col 15
     0,
     0,
     0};  // col 18

  /**
   * Get a description of table columns available in a catalog.
   *
   * <P>Only column descriptions matching the catalog, schema, table
   * and column name criteria are returned.  They are ordered by
   * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
   *
   * <P>Each column description has the following columns:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>COLUMN_NAME</B> String => column name
   *	<LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
   *	<LI><B>TYPE_NAME</B> String => Data source dependent type name,
   *  for a UDT the type name is fully qualified
   *	<LI><B>COLUMN_SIZE</B> int => column size.  For char or date
   *	    types this is the maximum number of characters, for numeric or
   *	    decimal types this is precision.
   *	<LI><B>BUFFER_LENGTH</B> is not used.
   *	<LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
   *	<LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
   *	<LI><B>NULLABLE</B> int => is NULL allowed?
   *      <UL>
   *      <LI> columnNoNulls - might not allow NULL values
   *      <LI> columnNullable - definitely allows NULL values
   *      <LI> columnNullableUnknown - nullability unknown
   *      </UL>
   *	<LI><B>REMARKS</B> String => comment describing column (may be null)
   * 	<LI><B>COLUMN_DEF</B> String => default value (may be null)
   *	<LI><B>SQL_DATA_TYPE</B> int => unused
   *	<LI><B>SQL_DATETIME_SUB</B> int => unused
   *	<LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
   *       maximum number of bytes in the column
   *	<LI><B>ORDINAL_POSITION</B> int	=> index of column in table
   *      (starting at 1)
   *	<LI><B>IS_NULLABLE</B> String => "NO" means column definitely
   *      does not allow NULL values; "YES" means the column might
   *      allow NULL values.  An empty string means nobody knows.
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema pattern is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those
   * without a schema
   * @param tableNamePattern a table name pattern
   * @param columnNamePattern a column name pattern
   * @return ResultSet - each row is a column description
   * @see #getSearchStringEscape()
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getColumns (String catalog,
						     String schemaPattern,
						     String tableNamePattern,
						     String columnNamePattern) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schemaPattern);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_COLUMNS__);
    if (tableNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(tableNamePattern) );
    if (columnNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 Convert identifier to System Table entry.
      sendMsg.writeLDSQLText ( systemTableValue(columnNamePattern) );

    return remoteCatalogQuery (sendMsg, 
			       GET_COLUMNS_RESULT_COLS__, 
			       GET_COLUMNS_RESULT_COLUMN_NAMES__,
			       GET_COLUMNS_RESULT_NULLABLES__,
			       GET_COLUMNS_RESULT_TYPES__, 
			       GET_COLUMNS_RESULT_PRECISIONS__, 
			       GET_COLUMNS_RESULT_SCALES__);
  }

  /**
   * COLUMN NULLABLE - might not allow NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNoNulls = 0;

  /**
   * COLUMN NULLABLE - definitely allows NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNullable = 1;

  /**
   * COLUMN NULLABLE - nullability unknown.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNullableUnknown = 2;


  // ********************** getColumnPrivileges *********************
  static final private int GET_COLUMN_PRIVILEGES_RESULT_COLS__ = 8;

  static final private String[] GET_COLUMN_PRIVILEGES_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",        // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "COLUMN_NAME",
      "GRANTOR",          // col 5
      "GRANTEE",
      "PRIVILEGE",
      "IS_GRANTABLE"};    // col 8

  static final private boolean[] GET_COLUMN_PRIVILEGES_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     true,   // col 5
     false, 
     false,
     true};  // col 8

  static final private int[] GET_COLUMN_PRIVILEGES_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,  // col 5
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__}; // col 8

  static final private int[] GET_COLUMN_PRIVILEGES_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     31,
     31,   // col 5
     31,
     10,
     3}; // col 8

  static final private int[] GET_COLUMN_PRIVILEGES_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0};  // col 8

  /**
   * Get a description of the access rights for a table's columns.
   *
   * <P>Only privileges matching the column name criteria are
   * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
   *
   * <P>Each privilige description has the following columns:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>COLUMN_NAME</B> String => column name
   *	<LI><B>GRANTOR</B> => grantor of access (may be null)
   *	<LI><B>GRANTEE</B> String => grantee of access
   *	<LI><B>PRIVILEGE</B> String => name of access (SELECT,
   *      INSERT, UPDATE, REFRENCES, ...)
   *	<LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
   *      to grant to others; "NO" if not; null if unknown
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @param columnNamePattern a column name pattern
   * @return ResultSet - each row is a column privilege description
   * @see #getSearchStringEscape()
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getColumnPrivileges (String catalog,
							      String schema,
							      String table,
							      String columnNamePattern) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_COLUMN_PRIVILEGES__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );
    if (columnNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
      sendMsg.writeLDSQLText ( systemTableValue(columnNamePattern) );

    return remoteCatalogQuery (sendMsg, 
			       GET_COLUMN_PRIVILEGES_RESULT_COLS__, 
			       GET_COLUMN_PRIVILEGES_RESULT_COLUMN_NAMES__,
			       GET_COLUMN_PRIVILEGES_RESULT_NULLABLES__,
			       GET_COLUMN_PRIVILEGES_RESULT_TYPES__, 
			       GET_COLUMN_PRIVILEGES_RESULT_PRECISIONS__, 
			       GET_COLUMN_PRIVILEGES_RESULT_SCALES__);
  }

  // ********************** getTablePrivileges *********************
  static final private int GET_TABLE_PRIVILEGES_RESULT_COLS__ = 7;

  static final private String[] GET_TABLE_PRIVILEGES_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",      // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "GRANTOR",          
      "GRANTEE",        // col 5
      "PRIVILEGE",
      "IS_GRANTABLE"};  // col 7

  static final private boolean[] GET_TABLE_PRIVILEGES_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     true, 
     false,   // col 5
     false, 
     false};  // col 7

  static final private int[] GET_TABLE_PRIVILEGES_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,  // col 5
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__}; // col 7

  static final private int[] GET_TABLE_PRIVILEGES_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     31,
     31,  // col 5
     10,  // RDB$PRIVILEGES is actually 6, but we need REFERENCES=10
     3}; // col 7

  static final private int[] GET_TABLE_PRIVILEGES_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0};  // col 7

  /**
   * Get a description of the access rights for each table available
   * in a catalog. Note that a table privilege applies to one or
   * more columns in the table. It would be wrong to assume that
   * this priviledge applies to all columns (this may be true for
   * some systems but is not true for all.)
   *
   * <P>Only privileges matching the schema and table name
   * criteria are returned.  They are ordered by TABLE_SCHEM,
   * TABLE_NAME, and PRIVILEGE.
   *
   * <P>Each privilige description has the following columns:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>GRANTOR</B> => grantor of access (may be null)
   *	<LI><B>GRANTEE</B> String => grantee of access
   *	<LI><B>PRIVILEGE</B> String => name of access (SELECT,
   *      INSERT, UPDATE, REFRENCES, ...)
   *	<LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
   *      to grant to others; "NO" if not; null if unknown
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema pattern is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those
   *    without a schema
   * @param tableNamePattern a table name pattern
   * @return ResultSet - each row is a table privilege description
   * @see #getSearchStringEscape()
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getTablePrivileges (String catalog,
							     String schemaPattern,
							     String tableNamePattern) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schemaPattern);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_TABLE_PRIVILEGES__);
    if (tableNamePattern == null)
      sendMsg.writeLDSQLText ("%");
    else
// CJL-IB6 convert identifier to System Table entry
      sendMsg.writeLDSQLText ( systemTableValue(tableNamePattern) );

    return remoteCatalogQuery (sendMsg, 
			       GET_TABLE_PRIVILEGES_RESULT_COLS__, 
			       GET_TABLE_PRIVILEGES_RESULT_COLUMN_NAMES__,
			       GET_TABLE_PRIVILEGES_RESULT_NULLABLES__,
			       GET_TABLE_PRIVILEGES_RESULT_TYPES__, 
			       GET_TABLE_PRIVILEGES_RESULT_PRECISIONS__, 
			       GET_TABLE_PRIVILEGES_RESULT_SCALES__);
  }

  // ********************** getBestRowIdentifier *********************
  static final private int GET_BEST_ROW_IDENTIFIER_RESULT_COLS__ = 8;

  static final private String[] GET_BEST_ROW_IDENTIFIER_RESULT_COLUMN_NAMES__ 
  =  {"SCOPE",      // col 1
      "COLUMN_NAME",
      "DATA_TYPE",
      "TYPE_NAME",          
      "COLUMN_SIZE",        // col 5
      "BUFFER_LENGTH",
      "DECIMAL_DIGITS",
      "PSEUDO_COLUMN"};  // col 8

  static final private boolean[] GET_BEST_ROW_IDENTIFIER_RESULT_NULLABLES__
  = {false,    // col 1
     false, 
     false, 
     false, 
     false,   // col 5
     false, 
     false, 
     false};  // col 8

  static final private int[] GET_BEST_ROW_IDENTIFIER_RESULT_TYPES__
  = {IBTypes.SMALLINT__,   // col 1
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,
     IBTypes.VARCHAR__,
     IBTypes.INTEGER__,    // col 5
     IBTypes.INTEGER__,
     IBTypes.SMALLINT__,
     IBTypes.SMALLINT__};  // col 8

  static final private int[] GET_BEST_ROW_IDENTIFIER_RESULT_PRECISIONS__
  = {IBTypes.SMALLINT_PRECISION__,   // col 1
     31,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.TYPE_NAME_PRECISION__,
     IBTypes.INTEGER_PRECISION__,  // col 5
     IBTypes.INTEGER_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__}; // col 8

  static final private int[] GET_BEST_ROW_IDENTIFIER_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0};  // col 8

  /**
   * Get a description of a table's optimal set of columns that
   * uniquely identifies a row. They are ordered by SCOPE.
   *
   * <P>Each column description has the following columns:
   *  <OL>
   *	<LI><B>SCOPE</B> short => actual scope of result
   *      <UL>
   *      <LI> bestRowTemporary - very temporary, while using row
   *      <LI> bestRowTransaction - valid for remainder of current transaction
   *      <LI> bestRowSession - valid for remainder of current session
   *      </UL>
   *	<LI><B>COLUMN_NAME</B> String => column name
   *	<LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
   *	<LI><B>TYPE_NAME</B> String => Data source dependent type name,
   *  for a UDT the type name is fully qualified
   *	<LI><B>COLUMN_SIZE</B> int => precision
   *	<LI><B>BUFFER_LENGTH</B> int => not used
   *	<LI><B>DECIMAL_DIGITS</B> short	 => scale
   *	<LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
   *      like an Oracle ROWID
   *      <UL>
   *      <LI> bestRowUnknown - may or may not be pseudo column
   *      <LI> bestRowNotPseudo - is NOT a pseudo column
   *      <LI> bestRowPseudo - is a pseudo column
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @param scope the scope of interest; use same values as SCOPE
   * @param nullable include columns that are nullable?
   * @return ResultSet - each row is a column description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getBestRowIdentifier (String catalog,
							       String schema,
							       String table,
							       int scope,
							       boolean nullable) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_BEST_ROW_IDENTIFIER__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );
    sendMsg.writeInt (scope);
    sendMsg.writeBoolean (nullable);

    return remoteCatalogQuery (sendMsg, 
			       GET_BEST_ROW_IDENTIFIER_RESULT_COLS__, 
			       GET_BEST_ROW_IDENTIFIER_RESULT_COLUMN_NAMES__,
			       GET_BEST_ROW_IDENTIFIER_RESULT_NULLABLES__,
			       GET_BEST_ROW_IDENTIFIER_RESULT_TYPES__, 
			       GET_BEST_ROW_IDENTIFIER_RESULT_PRECISIONS__, 
			       GET_BEST_ROW_IDENTIFIER_RESULT_SCALES__);
  }

  /**
   * BEST ROW SCOPE - very temporary, while using row.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowTemporary   = 0;

  /**
   * BEST ROW SCOPE - valid for remainder of current transaction.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowTransaction = 1;

  /**
   * BEST ROW SCOPE - valid for remainder of current session.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowSession     = 2;

  /**
   * BEST ROW PSEUDO_COLUMN - may or may not be pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowUnknown	= 0;

  /**
   * BEST ROW PSEUDO_COLUMN - is NOT a pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowNotPseudo	= 1;

  /**
   * BEST ROW PSEUDO_COLUMN - is a pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int bestRowPseudo	= 2;

  // ********************** getVersionColumns *********************
  static final private int GET_VERSION_COLUMNS_RESULT_COLS__ = GET_BEST_ROW_IDENTIFIER_RESULT_COLS__;

  static final private String[] GET_VERSION_COLUMNS_RESULT_COLUMN_NAMES__ 
  = GET_BEST_ROW_IDENTIFIER_RESULT_COLUMN_NAMES__;  // col 8

  static final private boolean[] GET_VERSION_COLUMNS_RESULT_NULLABLES__
  = GET_BEST_ROW_IDENTIFIER_RESULT_NULLABLES__;  // col 8

  static final private int[] GET_VERSION_COLUMNS_RESULT_TYPES__
  = GET_BEST_ROW_IDENTIFIER_RESULT_TYPES__;   // col 8

  static final private int[] GET_VERSION_COLUMNS_RESULT_PRECISIONS__
  = GET_BEST_ROW_IDENTIFIER_RESULT_PRECISIONS__; // col 8

  static final private int[] GET_VERSION_COLUMNS_RESULT_SCALES__
  = GET_BEST_ROW_IDENTIFIER_RESULT_SCALES__;  // col 8

  /**
   * Get a description of a table's columns that are automatically
   * updated when any value in a row is updated.  They are
   * unordered.
   *
   * <P>Each column description has the following columns:
   *  <OL>
   *	<LI><B>SCOPE</B> short => is not used
   *	<LI><B>COLUMN_NAME</B> String => column name
   *	<LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
   *	<LI><B>TYPE_NAME</B> String => Data source dependent type name
   *	<LI><B>COLUMN_SIZE</B> int => precision
   *	<LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
   *	<LI><B>DECIMAL_DIGITS</B> short	 => scale
   *	<LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
   *      like an Oracle ROWID
   *      <UL>
   *      <LI> versionColumnUnknown - may or may not be pseudo column
   *      <LI> versionColumnNotPseudo - is NOT a pseudo column
   *      <LI> versionColumnPseudo - is a pseudo column
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @return ResultSet - each row is a column description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getVersionColumns (String catalog,
							    String schema,
							    String table)  throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_VERSION_COLUMNS__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );

    return remoteCatalogQuery (sendMsg, 
			       GET_VERSION_COLUMNS_RESULT_COLS__, 
			       GET_VERSION_COLUMNS_RESULT_COLUMN_NAMES__,
			       GET_VERSION_COLUMNS_RESULT_NULLABLES__,
			       GET_VERSION_COLUMNS_RESULT_TYPES__, 
			       GET_VERSION_COLUMNS_RESULT_PRECISIONS__, 
			       GET_VERSION_COLUMNS_RESULT_SCALES__);
  }

  /**
   * VERSION COLUMNS PSEUDO_COLUMN - may or may not be pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int versionColumnUnknown	= 0;

  /**
   * VERSION COLUMNS PSEUDO_COLUMN - is NOT a pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int versionColumnNotPseudo	= 1;

  /**
   * VERSION COLUMNS PSEUDO_COLUMN - is a pseudo column.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int versionColumnPseudo	= 2;

  // ********************** getPrimaryKeys *********************
  static final private int GET_PRIMARY_KEYS_RESULT_COLS__ = 6;

  static final private String[] GET_PRIMARY_KEYS_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",      // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "COLUMN_NAME",          
      "KEY_SEQ",        // col 5
      "PK_NAME"};  

  static final private boolean[] GET_PRIMARY_KEYS_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     false,   // col 5
     true};  

  static final private int[] GET_PRIMARY_KEYS_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,    // col 5
     IBTypes.VARCHAR__};    

  static final private int[] GET_PRIMARY_KEYS_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     31,
     IBTypes.SMALLINT_PRECISION__,  // col 5
     31};

  static final private int[] GET_PRIMARY_KEYS_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0};  

  /**
   * Get a description of a table's primary key columns.  They
   * are ordered by COLUMN_NAME.
   *
   * <P>Each primary key column description has the following columns:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>COLUMN_NAME</B> String => column name
   *	<LI><B>KEY_SEQ</B> short => sequence number within primary key
   *	<LI><B>PK_NAME</B> String => primary key name (may be null)
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @return ResultSet - each row is a primary key column description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getPrimaryKeys (String catalog,
							 String schema,
							 String table) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_PRIMARY_KEYS__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );

    return remoteCatalogQuery (sendMsg, 
			       GET_PRIMARY_KEYS_RESULT_COLS__, 
			       GET_PRIMARY_KEYS_RESULT_COLUMN_NAMES__,
			       GET_PRIMARY_KEYS_RESULT_NULLABLES__,
			       GET_PRIMARY_KEYS_RESULT_TYPES__, 
			       GET_PRIMARY_KEYS_RESULT_PRECISIONS__, 
			       GET_PRIMARY_KEYS_RESULT_SCALES__);
  }

  // ********************** getImportedKeys *********************
  static final private int GET_IMPORTED_KEYS_RESULT_COLS__ = 14;

  static final private String[] GET_IMPORTED_KEYS_RESULT_COLUMN_NAMES__ 
  =  {"PKTABLE_CAT",     // col 1
      "PKTABLE_SCHEM",
      "PKTABLE_NAME",
      "PKCOLUMN_NAME",  
      "FKTABLE_CAT",     // col 5
      "FKTABLE_SCHEM",
      "FKTABLE_NAME",
      "FKCOLUMN_NAME",          
      "KEY_SEQ",        
      "UPDATE_RULE",     // col 10
      "DELETE_RULE",
      "FK_NAME",
      "PK_NAME",
      "DEFERRABILITY"};  // col 14

  static final private boolean[] GET_IMPORTED_KEYS_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     true,   // col 5
     true, 
     false, 
     false, 
     false, 
     false,   // col 10
     false, 
     true, 
     true,
     false};  // col 14

  static final private int[] GET_IMPORTED_KEYS_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,     // col 5
     IBTypes.VARCHAR__,  
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,     // col 10
     IBTypes.SMALLINT__,  
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__};     // col 14

  static final private int[] GET_IMPORTED_KEYS_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     31,
     0,   // col 5
     0,
     31,
     31,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,   // col 10
     IBTypes.SMALLINT_PRECISION__,
     31,
     31,
     IBTypes.SMALLINT_PRECISION__};  // col 14

  static final private int[] GET_IMPORTED_KEYS_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0,
     0,
     0,   // col 10
     0,
     0,
     0,
     0};  // col 14

  /**
   * Get a description of the primary key columns that are
   * referenced by a table's foreign key columns (the primary keys
   * imported by a table).  They are ordered by PKTABLE_CAT,
   * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
   *
   * <P>Each primary key column description has the following columns:
   *  <OL>
   *	<LI><B>PKTABLE_CAT</B> String => primary key table catalog
   *      being imported (may be null)
   *	<LI><B>PKTABLE_SCHEM</B> String => primary key table schema
   *      being imported (may be null)
   *	<LI><B>PKTABLE_NAME</B> String => primary key table name
   *      being imported
   *	<LI><B>PKCOLUMN_NAME</B> String => primary key column name
   *      being imported
   *	<LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
   *	<LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
   *	<LI><B>FKTABLE_NAME</B> String => foreign key table name
   *	<LI><B>FKCOLUMN_NAME</B> String => foreign key column name
   *	<LI><B>KEY_SEQ</B> short => sequence number within foreign key
   *	<LI><B>UPDATE_RULE</B> short => What happens to
   *       foreign key when primary is updated:
   *      <UL>
   *      <LI> importedNoAction - do not allow update of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - change imported key to agree
   *               with primary key update
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been updated
   *      <LI> importedKeySetDefault - change imported key to default values
   *               if its primary key has been updated
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      </UL>
   *	<LI><B>DELETE_RULE</B> short => What happens to
   *      the foreign key when primary is deleted.
   *      <UL>
   *      <LI> importedKeyNoAction - do not allow delete of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - delete rows that import a deleted key
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been deleted
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      <LI> importedKeySetDefault - change imported key to default if
   *               its primary key has been deleted
   *      </UL>
   *	<LI><B>FK_NAME</B> String => foreign key name (may be null)
   *	<LI><B>PK_NAME</B> String => primary key name (may be null)
   *	<LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
   *      constraints be deferred until commit
   *      <UL>
   *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
   *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
   *      <LI> importedKeyNotDeferrable - see SQL92 for definition
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those
   *    without a schema
   * @param table a table name
   * @return ResultSet - each row is a primary key column description
   * @see #getExportedKeys
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getImportedKeys (String catalog,
							  String schema,
							  String table) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_IMPORTED_KEYS__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );

    return remoteCatalogQuery (sendMsg, 
			       GET_IMPORTED_KEYS_RESULT_COLS__, 
			       GET_IMPORTED_KEYS_RESULT_COLUMN_NAMES__,
			       GET_IMPORTED_KEYS_RESULT_NULLABLES__,
			       GET_IMPORTED_KEYS_RESULT_TYPES__, 
			       GET_IMPORTED_KEYS_RESULT_PRECISIONS__, 
			       GET_IMPORTED_KEYS_RESULT_SCALES__);
  }

  /**
   * IMPORT KEY UPDATE_RULE and DELETE_RULE - for update, change
   * imported key to agree with primary key update; for delete,
   * delete rows that import a deleted key.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyCascade	= 0;

  /**
   * IMPORT KEY UPDATE_RULE and DELETE_RULE - do not allow update or
   * delete of primary key if it has been imported.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyRestrict = 1;

  /**
   * IMPORT KEY UPDATE_RULE and DELETE_RULE - change imported key to
   * NULL if its primary key has been updated or deleted.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeySetNull  = 2;

  /**
   * IMPORT KEY UPDATE_RULE and DELETE_RULE - do not allow update or
   * delete of primary key if it has been imported.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyNoAction = 3;

  /**
   * IMPORT KEY UPDATE_RULE and DELETE_RULE - change imported key to
   * default values if its primary key has been updated or deleted.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeySetDefault  = 4;

  /**
   * IMPORT KEY DEFERRABILITY - see SQL92 for definition
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyInitiallyDeferred  = 5;

  /**
   * IMPORT KEY DEFERRABILITY - see SQL92 for definition
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyInitiallyImmediate  = 6;

  /**
   * IMPORT KEY DEFERRABILITY - see SQL92 for definition
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int importedKeyNotDeferrable  = 7;

  // ********************** getExportedKeys *********************
  static final private int GET_EXPORTED_KEYS_RESULT_COLS__ = GET_IMPORTED_KEYS_RESULT_COLS__;

  static final private String[] GET_EXPORTED_KEYS_RESULT_COLUMN_NAMES__ = GET_IMPORTED_KEYS_RESULT_COLUMN_NAMES__;

  static final private boolean[] GET_EXPORTED_KEYS_RESULT_NULLABLES__ = GET_IMPORTED_KEYS_RESULT_NULLABLES__;

  static final private int[] GET_EXPORTED_KEYS_RESULT_TYPES__ = GET_IMPORTED_KEYS_RESULT_TYPES__;

  static final private int[] GET_EXPORTED_KEYS_RESULT_PRECISIONS__ = GET_IMPORTED_KEYS_RESULT_PRECISIONS__;

  static final private int[] GET_EXPORTED_KEYS_RESULT_SCALES__ = GET_IMPORTED_KEYS_RESULT_SCALES__;

  /**
   * Get a description of the foreign key columns that reference a
   * table's primary key columns (the foreign keys exported by a
   * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
   * FKTABLE_NAME, and KEY_SEQ.
   *
   * <P>Each foreign key column description has the following columns:
   *  <OL>
   *	<LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
   *	<LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
   *	<LI><B>PKTABLE_NAME</B> String => primary key table name
   *	<LI><B>PKCOLUMN_NAME</B> String => primary key column name
   *	<LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
   *      being exported (may be null)
   *	<LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
   *      being exported (may be null)
   *	<LI><B>FKTABLE_NAME</B> String => foreign key table name
   *      being exported
   *	<LI><B>FKCOLUMN_NAME</B> String => foreign key column name
   *      being exported
   *	<LI><B>KEY_SEQ</B> short => sequence number within foreign key
   *	<LI><B>UPDATE_RULE</B> short => What happens to
   *       foreign key when primary is updated:
   *      <UL>
   *      <LI> importedNoAction - do not allow update of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - change imported key to agree
   *               with primary key update
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been updated
   *      <LI> importedKeySetDefault - change imported key to default values
   *               if its primary key has been updated
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      </UL>
   *	<LI><B>DELETE_RULE</B> short => What happens to
   *      the foreign key when primary is deleted.
   *      <UL>
   *      <LI> importedKeyNoAction - do not allow delete of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - delete rows that import a deleted key
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been deleted
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      <LI> importedKeySetDefault - change imported key to default if
   *               its primary key has been deleted
   *      </UL>
   *	<LI><B>FK_NAME</B> String => foreign key name (may be null)
   *	<LI><B>PK_NAME</B> String => primary key name (may be null)
   *	<LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
   *      constraints be deferred until commit
   *      <UL>
   *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
   *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
   *      <LI> importedKeyNotDeferrable - see SQL92 for definition
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   *    catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @return ResultSet - each row is a foreign key column description
   * @see #getImportedKeys
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getExportedKeys (String catalog,
							  String schema,
							  String table) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_EXPORTED_KEYS__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );

    return remoteCatalogQuery (sendMsg, 
			       GET_EXPORTED_KEYS_RESULT_COLS__, 
			       GET_EXPORTED_KEYS_RESULT_COLUMN_NAMES__,
			       GET_EXPORTED_KEYS_RESULT_NULLABLES__,
			       GET_EXPORTED_KEYS_RESULT_TYPES__, 
			       GET_EXPORTED_KEYS_RESULT_PRECISIONS__, 
			       GET_EXPORTED_KEYS_RESULT_SCALES__);
  }

  // ********************** getCrossReference *********************
  static final private int GET_CROSS_REFERENCE_RESULT_COLS__ = GET_IMPORTED_KEYS_RESULT_COLS__;

  static final private String[] GET_CROSS_REFERENCE_RESULT_COLUMN_NAMES__ = GET_IMPORTED_KEYS_RESULT_COLUMN_NAMES__;

  static final private boolean[] GET_CROSS_REFERENCE_RESULT_NULLABLES__ = GET_IMPORTED_KEYS_RESULT_NULLABLES__;

  static final private int[] GET_CROSS_REFERENCE_RESULT_TYPES__ = GET_IMPORTED_KEYS_RESULT_TYPES__;

  static final private int[] GET_CROSS_REFERENCE_RESULT_PRECISIONS__ = GET_IMPORTED_KEYS_RESULT_PRECISIONS__;

  static final private int[] GET_CROSS_REFERENCE_RESULT_SCALES__ = GET_IMPORTED_KEYS_RESULT_SCALES__;

  /**
   * Get a description of the foreign key columns in the foreign key
   * table that reference the primary key columns of the primary key
   * table.  Viz. describe how one table imports another's key. This
   * should normally return a single foreign key/primary key pair
   * (most tables only import a foreign key from a table once).  They
   * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
   * KEY_SEQ.
   *
   * <P>Each foreign key column description has the following columns:
   *  <OL>
   *	<LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
   *	<LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
   *	<LI><B>PKTABLE_NAME</B> String => primary key table name
   *	<LI><B>PKCOLUMN_NAME</B> String => primary key column name
   *	<LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
   *      being exported (may be null)
   *	<LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
   *      being exported (may be null)
   *	<LI><B>FKTABLE_NAME</B> String => foreign key table name
   *      being exported
   *	<LI><B>FKCOLUMN_NAME</B> String => foreign key column name
   *      being exported
   *	<LI><B>KEY_SEQ</B> short => sequence number within foreign key
   *	<LI><B>UPDATE_RULE</B> short => What happens to
   *       foreign key when primary is updated:
   *      <UL>
   *      <LI> importedNoAction - do not allow update of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - change imported key to agree
   *               with primary key update
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been updated
   *      <LI> importedKeySetDefault - change imported key to default values
   *               if its primary key has been updated
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      </UL>
   *	<LI><B>DELETE_RULE</B> short => What happens to
   *      the foreign key when primary is deleted.
   *      <UL>
   *      <LI> importedKeyNoAction - do not allow delete of primary
   *               key if it has been imported
   *      <LI> importedKeyCascade - delete rows that import a deleted key
   *      <LI> importedKeySetNull - change imported key to NULL if
   *               its primary key has been deleted
   *      <LI> importedKeyRestrict - same as importedKeyNoAction
   *                                 (for ODBC 2.x compatibility)
   *      <LI> importedKeySetDefault - change imported key to default if
   *               its primary key has been deleted
   *      </UL>
   *	<LI><B>FK_NAME</B> String => foreign key name (may be null)
   *	<LI><B>PK_NAME</B> String => primary key name (may be null)
   *	<LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
   *      constraints be deferred until commit
   *      <UL>
   *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
   *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
   *      <LI> importedKeyNotDeferrable - see SQL92 for definition 
   *      </UL>
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param primaryCatalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param primarySchema a schema name; "" retrieves those
   * without a schema
   * @param primaryTable the table name that exports the key
   * @param foreignCatalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param foreignSchema a schema name; "" retrieves those
   * without a schema
   * @param foreignTable the table name that imports the key
   * @return ResultSet - each row is a foreign key column description
   * @see #getImportedKeys
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getCrossReference (String primaryCatalog,
							    String primarySchema,
							    String primaryTable,
							    String foreignCatalog,
							    String foreignSchema,
							    String foreignTable) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (primaryCatalog, primarySchema);
    systemTableQueryPreamble (foreignCatalog, foreignSchema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_CROSS_REFERENCE__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(primaryTable) );
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(foreignTable) );

    return remoteCatalogQuery (sendMsg, 
			       GET_CROSS_REFERENCE_RESULT_COLS__, 
			       GET_CROSS_REFERENCE_RESULT_COLUMN_NAMES__,
			       GET_CROSS_REFERENCE_RESULT_NULLABLES__,
			       GET_CROSS_REFERENCE_RESULT_TYPES__, 
			       GET_CROSS_REFERENCE_RESULT_PRECISIONS__, 
			       GET_CROSS_REFERENCE_RESULT_SCALES__);
  }

  // ********************** getTypeInfo *********************
  static final private int GET_TYPE_INFO_RESULT_COLS__ = 18;

  static final private String[] GET_TYPE_INFO_RESULT_COLUMN_NAMES__ 
  =  {"TYPE_NAME",           // col 1
      "DATA_TYPE",
      "PRECISION",
      "LITERAL_PREFIX",  
      "LITERAL_SUFFIX",      // col 5
      "CREATE_PARAMS",
      "NULLABLE",
      "CASE_SENSITIVE",          
      "SEARCHABLE",        
      "UNSIGNED_ATTRIBUTE",  // col 10
      "FIXED_PREC_SCALE",
      "AUTO_INCREMENT",
      "LOCAL_TYPE_NAME",
      "MINIMUM_SCALE",
      "MAXIMUM_SCALE",       // col 15
      "SQL_DATA_TYPE",
      "SQL_DATETIME_SUB",
      "NUM_PREC_RADIX"};     // col 18

  static final private boolean[] GET_TYPE_INFO_RESULT_NULLABLES__
  = {false,    // col 1
     false, 
     false, 
     true, 
     true,   // col 5
     true, 
     false, 
     false, 
     false, 
     false,   // col 10
     false, 
     false, 
     true, 
     false, 
     false,   // col 15
     true, 
     true, 
     false};  // col 18

  static final private int[] GET_TYPE_INFO_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.SMALLINT__,
     IBTypes.INTEGER__,
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,     // col 5
     IBTypes.VARCHAR__,  
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,     // col 10
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,  
     IBTypes.VARCHAR__,  
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,     // col 15
     IBTypes.INTEGER__,  
     IBTypes.INTEGER__,  
     IBTypes.INTEGER__};     // col 18

  static final private int[] GET_TYPE_INFO_RESULT_PRECISIONS__
  = {IBTypes.TYPE_NAME_PRECISION__,   // col 1
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     1,
     1,   // col 5
     0,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,   // col 10
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.TYPE_NAME_PRECISION__,  // col 13
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,   // col 15
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__};  // col 18

  static final private int[] GET_TYPE_INFO_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0,
     0,
     0,   // col 10
     0,
     0,
     0,
     0,
     0,   // col 15
     0,
     0,
     0};  // col 18

  /**
   * Get a description of all the standard SQL types supported by
   * this database. They are ordered by DATA_TYPE and then by how
   * closely the data type maps to the corresponding JDBC SQL type.
   *
   * <P>Each type description has the following columns:
   *  <OL>
   *	<LI><B>TYPE_NAME</B> String => Type name
   *	<LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
   *	<LI><B>PRECISION</B> int => maximum precision
   *	<LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
   *      (may be null)
   *	<LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
   *        (may be null)
   *	<LI><B>CREATE_PARAMS</B> String => parameters used in creating
   *      the type (may be null)
   *	<LI><B>NULLABLE</B> short => can you use NULL for this type?
   *      <UL>
   *      <LI> typeNoNulls - does not allow NULL values
   *      <LI> typeNullable - allows NULL values
   *      <LI> typeNullableUnknown - nullability unknown
   *      </UL>
   *	<LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive?
   *	<LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
   *      <UL>
   *      <LI> typePredNone - No support
   *      <LI> typePredChar - Only supported with WHERE .. LIKE
   *      <LI> typePredBasic - Supported except for WHERE .. LIKE
   *      <LI> typeSearchable - Supported for all WHERE ..
   *      </UL>
   *	<LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned?
   *	<LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value?
   *	<LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
   *      auto-increment value?
   *	<LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
   *      (may be null)
   *	<LI><B>MINIMUM_SCALE</B> short => minimum scale supported
   *	<LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
   *	<LI><B>SQL_DATA_TYPE</B> int => unused
   *	<LI><B>SQL_DATETIME_SUB</B> int => unused
   *	<LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
   *  </OL>
   *
   * @return ResultSet - each row is a SQL type description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getTypeInfo () throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (null, null);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_TYPE_INFO__);

    return remoteCatalogQuery (sendMsg, 
			       GET_TYPE_INFO_RESULT_COLS__, 
			       GET_TYPE_INFO_RESULT_COLUMN_NAMES__,
			       GET_TYPE_INFO_RESULT_NULLABLES__,
			       GET_TYPE_INFO_RESULT_TYPES__, 
			       GET_TYPE_INFO_RESULT_PRECISIONS__, 
			       GET_TYPE_INFO_RESULT_SCALES__);
  }

  /**
   * TYPE NULLABLE - does not allow NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typeNoNulls = 0;

  /**
   * TYPE NULLABLE - allows NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typeNullable = 1;

  /**
   * TYPE NULLABLE - nullability unknown.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typeNullableUnknown = 2;

  /**
   * TYPE INFO SEARCHABLE - No support.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typePredNone = 0;

  /**
   * TYPE INFO SEARCHABLE - Only supported with WHERE .. LIKE.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typePredChar = 1;

  /**
   * TYPE INFO SEARCHABLE -  Supported except for WHERE .. LIKE.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typePredBasic = 2;

  /**
   * TYPE INFO SEARCHABLE - Supported for all WHERE ...
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int typeSearchable  = 3;

  // ********************** getIndexInfo *********************
  static final private int GET_INDEX_INFO_RESULT_COLS__ = 13;

  static final private String[] GET_INDEX_INFO_RESULT_COLUMN_NAMES__ 
  =  {"TABLE_CAT",          // col 1
      "TABLE_SCHEM",
      "TABLE_NAME",
      "NON_UNIQUE",  
      "INDEX_QUALIFIER",    // col 5
      "INDEX_NAME",
      "TYPE",
      "ORDINAL_POSITION",          
      "COLUMN_NAME",        
      "ASC_OR_DESC",        // col 10
      "CARDINALITY",
      "PAGES",
      "FILTER_CONDITION"};  // col 13

  static final private boolean[] GET_INDEX_INFO_RESULT_NULLABLES__
  = {true,    // col 1
     true, 
     false, 
     false, 
     true,   // col 5
     false, 
     false, 
     false, 
     false, 
     true,   // col 10
     true,  // XXX was: false, 
     false, 
     true};  // col 13

  static final private int[] GET_INDEX_INFO_RESULT_TYPES__
  = {IBTypes.VARCHAR__,  // col 1
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,
     IBTypes.VARCHAR__,     // col 5
     IBTypes.VARCHAR__,
     IBTypes.SMALLINT__,  
     IBTypes.SMALLINT__,  
     IBTypes.VARCHAR__,
     IBTypes.VARCHAR__,     // col 10
     IBTypes.INTEGER__,  
     IBTypes.INTEGER__,  
     IBTypes.VARCHAR__};    // col 13

  static final private int[] GET_INDEX_INFO_RESULT_PRECISIONS__
  = {0,   // col 1
     0,
     31,
     IBTypes.SMALLINT_PRECISION__,
     0,   // col 5
     31,
     IBTypes.SMALLINT_PRECISION__,
     IBTypes.SMALLINT_PRECISION__,
     31,
     1,   // col 10
     IBTypes.INTEGER_PRECISION__,
     IBTypes.INTEGER_PRECISION__,
     0};  // col 13

  static final private int[] GET_INDEX_INFO_RESULT_SCALES__
  = {0,   // col 1
     0,
     0,
     0,
     0,   // col 5
     0,
     0,
     0,
     0,
     0,   // col 10
     0,
     0,
     0};  // col 13

  /**
   * Get a description of a table's indices and statistics. They are
   * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
   *
   * <P>Each index column description has the following columns:
   *  <OL>
   *	<LI><B>TABLE_CAT</B> String => table catalog (may be null)
   *	<LI><B>TABLE_SCHEM</B> String => table schema (may be null)
   *	<LI><B>TABLE_NAME</B> String => table name
   *	<LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique?
   *      false when TYPE is tableIndexStatistic
   *	<LI><B>INDEX_QUALIFIER</B> String => index catalog (may be null);
   *      null when TYPE is tableIndexStatistic
   *	<LI><B>INDEX_NAME</B> String => index name; null when TYPE is
   *      tableIndexStatistic
   *	<LI><B>TYPE</B> short => index type:
   *      <UL>
   *      <LI> tableIndexStatistic - this identifies table statistics that are
   *           returned in conjuction with a table's index descriptions
   *      <LI> tableIndexClustered - this is a clustered index
   *      <LI> tableIndexHashed - this is a hashed index
   *      <LI> tableIndexOther - this is some other style of index
   *      </UL>
   *	<LI><B>ORDINAL_POSITION</B> short => column sequence number
   *      within index; zero when TYPE is tableIndexStatistic
   *	<LI><B>COLUMN_NAME</B> String => column name; null when TYPE is
   *      tableIndexStatistic
   *	<LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending,
   *      "D" => descending, may be null if sort sequence is not supported;
   *      null when TYPE is tableIndexStatistic
   *	<LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then
   *      this is the number of rows in the table; otherwise, it is the
   *      number of unique values in the index.
   *	<LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then
   *      this is the number of pages used for the table, otherwise it
   *      is the number of pages used for the current index.
   *	<LI><B>FILTER_CONDITION</B> String => Filter condition, if any.
   *      (may be null)
   *  </OL>
   *
   * <p><b>InterClient note:</b>
   * A SQLException is thrown if a catalog or schema is given.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schema a schema name; "" retrieves those without a schema
   * @param table a table name
   * @param unique when true, return only indices for unique values;
   *     when false, return indices regardless of whether unique or not
   * @param approximate when true, result is allowed to reflect approximate
   *     or out of data values; when false, results are requested to be
   *     accurate
   * @return ResultSet - each row is an index column description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet getIndexInfo (String catalog, 
						       String schema, 
						       String table,
						       boolean unique, 
						       boolean approximate) throws java.sql.SQLException
  {
    checkForClosedConnection ();
    systemTableQueryPreamble (catalog, schema);

    MessageBufferOutputStream sendMsg;
    sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_CATALOG_QUERY__);
    sendMsg.writeByte (CATALOG_GET_INDEX_INFO__);
// CJL-IB6 Convert identifier to System Table entry.
    sendMsg.writeLDSQLText ( systemTableValue(table) );
    sendMsg.writeBoolean (unique);
    sendMsg.writeBoolean (approximate);

    return remoteCatalogQuery (sendMsg, 
			       GET_INDEX_INFO_RESULT_COLS__, 
			       GET_INDEX_INFO_RESULT_COLUMN_NAMES__,
			       GET_INDEX_INFO_RESULT_NULLABLES__,
			       GET_INDEX_INFO_RESULT_TYPES__, 
			       GET_INDEX_INFO_RESULT_PRECISIONS__, 
			       GET_INDEX_INFO_RESULT_SCALES__);
  }

  /**
   * INDEX INFO TYPE - this identifies table statistics that are
   * returned in conjuction with a table's index descriptions
   * @since <font color=red>JDBC 1</font>
   **/
  final static public short tableIndexStatistic = 0;

  /**
   * INDEX INFO TYPE - this identifies a clustered index
   * @since <font color=red>JDBC 1</font>
   **/
  final static public short tableIndexClustered = 1;

  /**
   * INDEX INFO TYPE - this identifies a hashed index
   * @since <font color=red>JDBC 1</font>
   **/
  final static public short tableIndexHashed    = 2;

  /**
   * INDEX INFO TYPE - this identifies some other form of index
   * @since <font color=red>JDBC 1</font>
   **/
  final static public short tableIndexOther     = 3;

  //--------------------------JDBC 2.0-----------------------------

  /**
   * Does the database support the given result set type.
   *
   * @param type defined in java.sql.ResultSet
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @see Connection
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean supportsResultSetType (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Does the database support the concurrency type in combination
   * with the given result set type.
   *
   * @param type defined in java.sql.ResultSet
   * @param concurrency type defined in java.sql.ResultSet
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @see Connection
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean supportsResultSetConcurrency (int type,
                                                            int concurrency) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether a result set's own updates visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean ownUpdatesAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether a result set's own deletes visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean ownDeletesAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether a result set's own inserts visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean ownInsertsAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether updates made by others are visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean othersUpdatesAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether deletes made by others are visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean othersDeletesAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether inserts made by others are visible.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are visible for the result set type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean othersInsertsAreVisible (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether or not a visible row update can be detected by
   * calling ResultSet.rowUpdated().
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are detected by the resultset type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean updatesAreDetected (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether or not a visible row delete can be detected by
   * calling ResultSet.rowDeleted().  If deletesAreDetected()
   * returns false, then deleted rows are removed from the result set.
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are detected by the resultset type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean deletesAreDetected (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Determine whether or not a visible row insert can be detected
   * by calling ResultSet.rowInserted().
   *
   * @param result set type, i.e. ResultSet.TYPE_XXX
   * @return true if changes are detected by the resultset type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean insertsAreDetected (int type) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Return true if the driver supports batch updates, else return false.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public boolean supportsBatchUpdates () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get a description of the user-defined types defined in a particular
   * schema.  Schema specific UDTs may have type JAVA_OBJECT, STRUCT,
   * or DISTINCT.
   *
   * <P>Only types matching the catalog, schema, type name and type
   * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM
   * and TYPE_NAME.  The type name parameter may be a fully qualified
   * name.  In this case, the catalog and schemaPattern parameters are
   * ignored.
   *
   * <P>Each type description has the following columns:
   *  <OL>
   *	<LI><B>TYPE_CAT</B> String => the type's catalog (may be null)
   *	<LI><B>TYPE_SCHEM</B> String => type's schema (may be null)
   *	<LI><B>TYPE_NAME</B> String => type name
   *  <LI><B>CLASS_NAME</B> String => Java class name
   *	<LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.
   *  One of JAVA_OBJECT, STRUCT, or DISTINCT
   *	<LI><B>REMARKS</B> String => explanatory comment on the type
   *  </OL>
   *
   * <P><B>Note:</B> If the driver does not support UDTs then an empty
   * result set is returned.
   *
   * @param catalog a catalog name; "" retrieves those without a
   * catalog; null means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those
   * without a schema
   * @param typeNamePattern a type name pattern; may be a fully qualified
   * name
   * @param types a list of user-named types to include (JAVA_OBJECT,
   * STRUCT, or DISTINCT); null returns all types
   * @return ResultSet - each row is a type description
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public java.sql.ResultSet getUDTs (String catalog,
                                                  String schemaPattern,
                                                  String typeNamePattern,
                                                  int[] types) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Return the connection that produced this metadata object.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, since 1.50</font>
   **/ 
  synchronized public java.sql.Connection getConnection() throws java.sql.SQLException
  {
    return connection_;
  }

  // ----------------- InterClient Extensions ------------------------

  // ------------------ InterServer Version Information ------------------------

  /**
   * Gets the version of the server side JDBC middleware server (InterServer).
   *
   * @see #getDriverVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer version.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public String getInterServerVersion ()
  {
    String result =
      String.valueOf (jdbcNet_.serverVersionInformation_.majorVersion_) +  "." +
      String.valueOf (jdbcNet_.serverVersionInformation_.minorVersion_) +  "." +
      String.valueOf (jdbcNet_.serverVersionInformation_.buildNumber_) +
      ((jdbcNet_.serverVersionInformation_.buildLevel_ == Globals.testBuild_) ? " Test Build" :
      ((jdbcNet_.serverVersionInformation_.buildLevel_ == Globals.betaBuild_) ? " Beta" : ""));
      
    return result;
  }

  /**
   * Gets the major version number for InterServer.
   *
   * @see #getDriverMajorVersion
   * @see ServerManager#getInterServerMajorVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer major version.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerMajorVersion ()
  {
    return jdbcNet_.serverVersionInformation_.majorVersion_;
  }

  /**
   * Gets the minor version number for InterServer.
   *
   * @see #getDriverMinorVersion
   * @see ServerManager#getInterServerMinorVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return InterServer minor version.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerMinorVersion ()
  {
    return jdbcNet_.serverVersionInformation_.minorVersion_;
  }

  /**
   * Gets the JDBC/Net protocol version used by InterServer.
   *
   * @see #getDriverJDBCNetProtocolVersion
   * @see ServerManager#getInterServerJDBCNetProtocolVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerJDBCNetProtocolVersion ()
  {
    return jdbcNet_.serverVersionInformation_.jdbcNetProtocolVersion_;
  }

  /**
   * Gets the expiration date for the server side JDBC middleware server (InterServer).
   *
   * @see #getDriverExpirationDate
   * @see ServerManager#getInterServerExpirationDate
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public java.util.Date getInterServerExpirationDate ()
  {
    return jdbcNet_.serverVersionInformation_.expirationDate_;
  }

  /**
   * Gets the JDBC/Net middleware server (InterServer) port.
   *
   * @see ServerManager#getInterServerPort()
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInterServerPort ()
  {
    return connection_.port_;
  }

  // -----------------additional InterClient driver information ----------------
  
  /**
   * Gets the JDBC/Net protocol version used by InterClient.
   *
   * @see #getInterServerJDBCNetProtocolVersion
   * @see Driver#getJDBCNetProtocolVersion
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getDriverJDBCNetProtocolVersion ()
  {
    return Globals.jdbcNetProtocolVersion__;
  }

  /**
   * Gets the expiration date for InterClient.
   *
   * @see #getInterServerExpirationDate
   * @see Driver#getExpirationDate
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public java.util.Date getDriverExpirationDate ()
  {
    return Globals.interclientExpirationDate__;
  }

  //------------------------additional database version information-------------
  
  /**
   * Gets the major version for the InterBase product.
   * <p>
   * <b>InterClient Note:</b>
   * This value is based on <code>isc_info_base_level</code>.
   * Due to an internal bug, this setting is still 4 for
   * InterBase 5 releases.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getDatabaseMajorVersion () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    // !! this uses base_level which is still 4 for IB 5.
    // isc_info_base_level
    return ibMajorVersion_;
  }

  /**
   * Gets the On Disk Structure (ODS) major version for the database.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getODSMajorVersion () throws java.sql.SQLException
  { 
    checkForClosedConnection ();

    // isc_info_ods_version
    return odsMajorVersion_;
  }

  /**
   * Gets the On Disk Structure (ODS) minor version for the database.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getODSMinorVersion () throws java.sql.SQLException
  { 
    checkForClosedConnection ();

    // isc_info_ods_minor_version
    return odsMinorVersion_;
  }

  // ------------------------additional database information--------------------
  
  /**
   * Gets the actual number of cached page buffers currently
   * being used for this connection.
   * <p>
   * See description of <code>suggestedCachePages</code> in
   * <a href="../../../help/icConnectionProperties.html">Connection Properties</a>.
   * 
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getActualCachePagesInUse () throws java.sql.SQLException
  {
    // isc_info_num_buffers
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets the number of cache page buffers as a persistent property of the database.
   * This is the database-wide default stamped onto the header page with
   * {@link ServerManager#setDatabaseCachePages(String, int) ServerManager.setDatabaseCachePages(database,cachePages)}.
   * 
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getPersistentDatabaseCachePages () throws java.sql.SQLException
  {
    // isc_info_set_page_buffers
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets the number of pages currently allocated for this database.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getPageAllocation () throws java.sql.SQLException
  { 
    checkForClosedConnection ();

    // isc_info_allocation
    return pageAllocation_;
  }
     
  /**
   * Gets the page size for this database.
   * This is the number of bytes per database page.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getPageSize () throws java.sql.SQLException
  { 
    checkForClosedConnection ();

    // isc_info_page_size
    return pageSize_;
  }

  /**
   * Gets the sweep interval for automatic housekeeping.
   * See the <i>InterBase Operation Guide</i> for the meaning of sweep intervals.
   *
   * @see ServerManager#setSweepInterval
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public boolean getSweepInterval () throws java.sql.SQLException
  {
    // isc_info_sweep_interval
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Is the database itself a read-write database.
   * This is independent of the connection mode, which may be read-only
   * even for a read-write database.
   *
   * @see #isReadOnly
   * @see ServerManager#setDatabaseReadWrite
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
// CJL-IB6 <!> look into this... Isn't this a duplicate method 
  synchronized public boolean isDatabaseReadWrite () throws java.sql.SQLException
  {
    // !!! isc_info_??; need to get this from Sriram
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Is space currently being reserved on each newly created 
   * database page for anticipated record versions.
   * <p>
   * See {@link ServerManager#reserveSpaceForVersioning ServerManager.reserveSpaceForVersioning}
   * for details.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public boolean reservingSpaceForVersioning () throws java.sql.SQLException
  {
    // isc_info_no_reserve
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Are disk writes synchronous for this connection.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @see ServerManager#useSynchronousWrites
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public boolean usingSynchronousWrites () throws java.sql.SQLException
  {
    // isc_info_forced_writes
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets statistics for the database as a whole.
   * Statistical counting starts at 0 from the moment this method is called.
   * <p>
   * This method is a placeholder for future functionality.
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
  synchronized public DatabaseStatistics getStatistics () throws java.sql.SQLException
  { 
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets a list of users currently connected to this database.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   **/
  synchronized public String[] getUsersConnected () throws java.sql.SQLException
  {
    // isc_info_user_names
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Gets a list of the User-Defined Functions on this database.
   * <p>
   * This method is a placeholder for possible future functionality.
   *
   * @return a comma separated list of udf names, eg. "foo, bar, bah"
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs
   **/
  synchronized public String getUDFs () throws java.sql.SQLException
  {
    // !!! how to get this?
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
  /*
   * This method converts identifers from the method parameter form to the
   * form which is stored in InterBase system tables.  Values like these:
   *   "employee"   "\"emp no"\""   "EMP_NO"
   * are converted to the following:
   *   "EMPLOYEE"   "emp no"    "EMP_NO"
   * This should work for all attachment SQL Dialects
  */
  private String systemTableValue( String name )
  {
  if ( name.startsWith("\"") &&
       name.endsWith("\"") )
    return name.substring( 1, name.length() - 1 );
  else
    return name.toUpperCase();
  }
}















