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

// Multi-generational architecture derives it name from the process of updating a record.
// Each time a record is updated
// or deleted a new copy (generation) of the record is created.
// Its main benefit is that writers do not block readers.
// Which means you can run a single query for weeks while people are updating the database.
// The answer you get
// from the query will be consistent with the commited contents of the database when you started your query
// transaction.
// How does this work?
// <p>
// Every operation on the database, whether it is a read or a write,
// is time stamped with a transaction number.
// They are assigned sequentially in ascending order.
// This means that a user who has transaction number 20,
// started work with the database earlier in time than someone with transaction 21.
// How much earlier one cannot
// tell, all you know is that transaction 20 started before transaction 21.
// And you know what state transaction 20
// was in when you started transaction 21.
// It was either active, committed, rolled back, limbo, or dead.
// We're just
// concerned with active, committed, and rolled back for this discussion.
// <p>
// Every record in the database is stamped with the transaction number that inserted, updated, or deleted the
// record.
// This number is imbedded in the record header.
// When a record is changed the old version of the record is
// kept with the old transacation number and the new version gets the transcation number that changed it.
// The new
// version of the record has a pointer to the old version of the record.
// The old version of the record has a pointer to
// the prior version of it and so on. There is a mechanism in place
// to determine how many old versions need to be kept.
// If necessary it will keep every version that has been created.
// <p>
// When you update a record in the database,
// the old version is compared to the new version to create a <i>back
// difference record (BDR)</i>.
// The <i>BDR</i> is moved to a new locaton and the new version is written in same the
// location where the orignal version was. So even though we keep old versions or records around,
// the <i>BDR</i> will never be larger than it ancestor.
// Usually, it will be very small unless you're changing the whole record.
// With deleted versions it's even smaller.
// The version being deleted is kept intact as a <i>BDR</i>
// with the new version just
// having the current transaction number and a flag
// indicating that the record is deleted.
// <p>
// Now let's take a look at an added benefit,
// the ability to lock a record without taking out an explicit record lock.
// Let assume that transaction 21 (<i>t21</i>)
// wants to updated a record that you're viewing with transaction 20 (<i>t20</i>).
// If <i>t21</i> updates the record before you can issue the update,
// then they have effectively locked the record because the
// new version of the record will be stamped with transaction number 21.
// If <i>t20</i> tries later, or just a spit-second
// later, the system will immediate detect there is a new version of the record and not allow the update.
// There are
// just a couple of simple rules for dealing with transactions and record versions.
// <p>
//   <ul>
//     <li>If your transaction number is
//         <b>less than</b>
//         the record's transaction number,
//         then you <b>cannot</b> see or
//         update it.
//     <li>If your transaction number is <b>equal to</b> the record's transaction number,
//         then you <b>can</b> see and/or
//         update it.
//     <li>If your transaction number is <b>greater than</b>
//         the record's transaction number (<i>RTN</i>) <b>and</b> the <i>RTN</i>
//         was <b>committed</b>
//         <b>before</b> you started your transaction number then you <b>can</b> see and/or update it.
//   </ul>
//
// <p>
// If the commit is happening for a transaction across multiple databases then
// the two-phase commit protocol is invoked.
// This first phase sets the transaction to limbo in each of the
// databases then the second phase races around the
// network to just switch the transaction bit to committed.
// If it fails anywhere in the two phases then the transaction is
// considered in limbo and the transaction bit is left set at the limbo state.
//
// <p>The Oldest Interesting Transaction (OIT) is the first transaction in a state
// other than committed in the database's Transaction Inventory Pages (TIP).
// The TIP is a set of pages that log each transaction's information
// (transaction number and current state) in the database
// since the last time the database was created or last backed up and restored.
// <p>
// The Oldest Active Transaction (OAT) is the first transaction marked as active in the TIP pages.
// <p>
// The way to find out the values of the OIT and OAT is to run gstat -h locally against the database in question.
// <p>
// To create a transaction the start transaction call will
// first read the header page of the database,
// pull off the Next Transaction number, increment it,
// and write the header page back to the database.
// It also reads the OIT value from the
// header page and starts reading the TIP pages
// from that transaction number forward up to the OAT.
// If the OIT is now marked as
// committed, then the process continues checking
// the transactions until it comes to the first transaction in a
// state other than committed and
// records that in the process's own transaction header block.
// The process then starts from the OIT and reads forward until it finds the first
// active transaction and records that in it's transaction header block also.
// <p>
// If and only if the process starts another transaction,
// will the information from the process's
// transaction header block update the
// information on the header page when it is read to get
// the next transaction number. Of course if another
// process has already updated the
// header page with newer numbers, i.e. larger,
// then the information will not be written.
// <p>
// There are only two non-committed and non-active transaction states;
// limbo and rolled back.
// The only way to change a limbo transaction
// to committed is for the user to run gfix on the database
// to resolve the limbo transaction by rolling back or committing it.
// The only way to
// change a rolled back transaction to committed is to sweep the database.
// <p>
// The sweep can be executed by:
// <ul>
// <li>the user running a gfix -s process
// <li>programmatically attaching to the database with a database parameter block set to cause a sweep
// <li>have the automatic sweep kicked off
// </ul>
// <p>
// The automatic sweep interval is set by default to be 20,000.
// It can be changed by using the gfix -h [N] command to set the interval to
// [N]. If [N] is zero then the automatic sweep is completely turned
// off and the user will have to use options A or B from above to sweep the
// database.
// <p>
// Note, the automatic sweep is kicked off when the difference between
// the OAT and the OIT is greater then the sweep interval.
// The user's
// process that tried to start the transaction that
// exceed the sweep interval by one will sweep the
// entire database before actually starting
// the transaction they requested.
// <p>
// As you can see, if you ever rollback a transaction,
// have an active transaction abnormally terminate,
// or always use processes that use only
// one transaction and then exit,
// then you will have to sweep the database to update the OIT and OAT values.
// Of course, sweeping the
// database also provides the added benefit of
// removing any delete records from the database.
// @docauthor Paul McGee

import java.sql.*;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Represents a database session and
 * provides a factory for SQL statements.
 * <p>
 * Within the context of a Connection, SQL statements are
 * executed and results are returned.
 *
 * <P>A Connection's database is able to provide information
 * describing its tables, its supported SQL grammar, its stored
 * procedures, the capabilities of this connection, etc. This
 * information is obtained with the getMetaData method.
 *
 * <P><B>Note:</B> By default the Connection automatically commits
 * changes after executing each statement. If auto commit has been
 * disabled, an explicit commit must be done or database changes will
 * not be saved.
 *
 * <p><b>InterClient note:</b>
 * One of InterBase's biggest strengths is the
 * row-level locking provided by our versioning engine.
 * This versioning of records is of great importance for applications
 * in which there is a lot of
 * updating going on, and a high level of concurrency is desired.
 * The default InterBase snapshot transaction mode allows for writers
 * not to block readers, and readers not to block writers.
 * Other DBMSs will serialize a lot of operations that InterBase would allow to
 * operate concurrently.
 *
 * @docauthor Bill Karwin
 * @docauthor Paul Ostler
 * @author Paul Ostler
 * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
 **/
final public class Connection implements java.sql.Connection
{
  // Server side session object reference
  // Not currently used.
  private int sessionRef_ = 0;

  //int defaultCursorNameSuffix_ = 1;

  // Target server and database for this connection
  // Both needed for DatabaseMetaData::getURL()
  String database_;
  String serverName_;
  int port_;
// CJL-IB6 added to record the adjusted SQL Dialect for the connection.
  int attachmentSQLDialect_;
// CJL-IB6 end change

  // Proprietary connection & transaction properties used on attach_database.
  private java.util.Properties properties_;

  boolean open_ = true;

  // Transaction state
  boolean readOnly_ = false;
  private int isolation_ = TRANSACTION_SERIALIZABLE;
  private boolean enableRecVersion_ = true;
  private int lockResolution_ = LOCK_RESOLUTION_WAIT;
  private boolean enableAutoCommit_ = true;
  private java.util.Hashtable tableLocks_ = null;

  boolean transactionStartedOnClient_ = false; // server side transaction may or may not have started.
  boolean transactionStartedOnServer_ = false;

  DatabaseMetaData databaseMetaData_;

  // for friendly statements on this connection.
  java.util.Vector openStatements_;
  java.util.Vector openPreparedStatements_;

  private java.sql.SQLWarning sqlWarnings_ = null;

  // MMM - jdbcNet_ is no longer private for it is also referenced
  /// from Array.remote_GET_ARRAY_SLICE
  //private JDBCNet jdbcNet_;
  // MMM - end
  JDBCNet jdbcNet_;

  String ianaCharacterEncoding_;
  private final static String defaultEncoding__ = CharacterEncodings._8859_1;

  //synchronized int getNextCursorNameSuffix ()
  //{
  //  return defaultCursorNameSuffix_++;
  //}

  // for Driver.connect()
  Connection (int socketTimeoutMilliseconds,
              String serverName,
              int port,
              String database,
              java.util.Properties properties) throws java.sql.SQLException
  {
    serverName_ = serverName;
    port_ = port;
    database_ = database;
    properties_ = (java.util.Properties) properties.clone ();

    if (properties_ == null)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__connection_properties__null__);

    addRequiredPropertiesAndSetConverters ();

    openStatements_ = new java.util.Vector ();
    openPreparedStatements_ = new java.util.Vector ();

    connect (socketTimeoutMilliseconds);
  }

  // Add any properties that need to be sent on the wire but were
  // not specified by the user, and determine byte code converters.
// CJL-IB6 <!> Should SQL Dialect also send a default (0) value on the wire?
// or is the InterServer code good enough?
// CJL-IB6 end.
  private void addRequiredPropertiesAndSetConverters ()
  {
    ianaCharacterEncoding_ = (String) properties_.get (ConnectionProperties.charSetKey__);
    if (ianaCharacterEncoding_ == null ||
        ianaCharacterEncoding_.equals (ConnectionProperties.defaultCharSet__)) {
      ianaCharacterEncoding_ = defaultEncoding__;
      properties_.put (ConnectionProperties.charSetKey__,
		       ConnectionProperties.defaultCharSet__);
    }
  }

  // MMM - checkForClosedConnection() is no longer private
  // Is also called from Array.remote_GET_ARRAY_SLICE
  //private void checkForClosedConnection () throws java.sql.SQLException
  // MMM - end
  void checkForClosedConnection () throws java.sql.SQLException
  {
    if (!open_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__connection_closed__);
  }

  private void connect (int socketTimeoutMilliseconds) throws java.sql.SQLException
  {
    try {
      jdbcNet_ = new JDBCNet (socketTimeoutMilliseconds,
                              serverName_,
                              port_,
			      sun.io.ByteToCharConverter.getConverter (ianaCharacterEncoding_),
			      sun.io.CharToByteConverter.getConverter (ianaCharacterEncoding_));
    }
    catch (java.io.UnsupportedEncodingException e) {
      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__,
                                                  ianaCharacterEncoding_);
    }

    databaseMetaData_ = new DatabaseMetaData (this, jdbcNet_);
    java.util.Date now = new java.util.Date ();
    try {
      remote_ATTACH_DATABASE ();
    }
    catch (java.sql.SQLException e) {
      try { jdbcNet_.disconnectSocket (); } catch (java.sql.SQLException e2) {}
      throw e;
    }
  }

  // Multi-purpose function performs the following:
  // 1) Sends properties
  // 2) Maps user-specified property values to on-the-wire property values;
  //    Notice that on-the-wire values differ, eg. ianaEncoding vs. IBCharSet names
  // 3) Verify the consistency of property values, especially datasource property values
  private void send_properties (MessageBufferOutputStream sendMsg,
				java.util.Properties properties) throws java.sql.SQLException
  {
    sendMsg.writeByte (properties.size ());
    java.util.Enumeration propertyNames = properties.propertyNames ();
    while (propertyNames.hasMoreElements ()) {
      String propertyName = (String) propertyNames.nextElement ();
      if (propertyName.equals (ConnectionProperties.userKey__)) {
        databaseMetaData_.userName_ = properties.getProperty (propertyName).toUpperCase ();
        sendMsg.writeLDSQLText (ConnectionProperties.userKey__); 
        byte[] encryptedUserName = jdbcNet_.crypter_.stringCrypt (databaseMetaData_.userName_);
        sendMsg.writeLDBytes (encryptedUserName);
      }
      else if (propertyName.equals (ConnectionProperties.passwordKey__)) {
	sendMsg.writeLDSQLText (propertyName);
        byte[] encryptedPassword = jdbcNet_.crypter_.stringCrypt (properties.getProperty (propertyName));
        sendMsg.writeLDBytes (encryptedPassword);
      }
      else if (propertyName.equals (ConnectionProperties.roleNameKey__)) {
	sendMsg.writeLDSQLText (propertyName);
	sendMsg.writeLDSQLText (properties.getProperty (propertyName).toUpperCase ());
      }
      else if (propertyName.equals (ConnectionProperties.charSetKey__)) {
	sendMsg.writeLDSQLText (propertyName);
	sendMsg.writeLDSQLText (CharacterEncodings.getInterBaseCharacterSetName (properties.getProperty (propertyName)));
      }
      else {
	sendMsg.writeLDSQLText (propertyName);
	sendMsg.writeLDSQLText (properties.getProperty (propertyName));
      }
    }
  }

  private void remote_ATTACH_DATABASE () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.ATTACH_DATABASE__);
    send_properties (sendMsg, properties_);
    sendMsg.writeShort (jdbcNet_.socketTimeout_);  // in milliseconds
    sendMsg.writeLDSQLText (database_);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      sessionRef_ = recvMsg.readInt ();
      databaseMetaData_.databaseProductVersion_ = recvMsg.readLDSQLText ();
      databaseMetaData_.ibMajorVersion_ = recvMsg.readInt (); // Note: this is set to 4 for v5
      databaseMetaData_.odsMajorVersion_ = recvMsg.readInt ();
      databaseMetaData_.odsMinorVersion_ = recvMsg.readInt ();
      databaseMetaData_.pageSize_ = recvMsg.readInt ();
      databaseMetaData_.pageAllocation_ = recvMsg.readInt ();
// CJL-IB6 protocol now return db sql dialect and read-only status
// Also, the attachment SQL Dialect corrected by InterServer
// CJL-IB6 <!> create method Connection.getSQLDialect() to obtain the adjusted dialect
      databaseMetaData_.databaseSQLDialect_ = recvMsg.readInt ();
      attachmentSQLDialect_ = recvMsg.readInt ();
      databaseMetaData_.databaseReadOnly_ = recvMsg.readBoolean ();
// CJL end.

      setWarning (recvMsg.get_WARNINGS ());

    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  private boolean isCompatibleIBVersion (String ibVersionString)
  {
    for (int i=0; i < Globals.compatibleIBVersions__.length; i++) {
      if (databaseMetaData_.ibMajorVersion_ == Globals.compatibleIBVersions__[i])
        return true;
    }
    // isc_info_base_level is still set to 4 for the IB 5 release,
    // workaround:
    int hypenIndex = ibVersionString.indexOf ('-');
    for (int i=0; i < Globals.compatibleIBVersions__.length; i++) {
      if (ibVersionString.charAt (hypenIndex+2) ==
          String.valueOf (Globals.compatibleIBVersions__[i]).charAt (0))
        return true;
    }
    return false;
  }

  /**
   * A connection will be closed when its finalizer is called
   * by the garbage collector.
   * There is no guarantee
   * that the garbage collector will ever run, and in general
   * will not run when an application terminates abruptly
   * without closing its connections.
   * <p>
   * Therefore, it is recommended that connections be
   * explicitly closed even if your application throws an exception.
   * This can be achieved by placing a call to close() in a finally
   * clause of your application as follows
   * <pre>
   * try {
   *   ...
   * }
   * finally {
   *   if (connection != null)
   *     try { connection.close (); } catch (SQLException ohWell) {}
   * }
   * </pre>
   * <p>
   * Or alternatively, use the System.runFinalizersOnExit () method.
   * @since <font color=red>Extension</font>
   **/
  protected void finalize () throws java.lang.Throwable
  {
    if (open_)
      close ();

    super.finalize ();
  }

  /**
   * Create a Statement as a container for executing
   * SQL on this connection.
   *
   * SQL statements without parameters are normally
   * executed using Statement objects. If the same SQL statement
   * is executed many times, it is more efficient to use a
   * PreparedStatement.
   *
   * <p><b>JDBC 2 note:</b>
   * Result sets created using the returned Statement will have
   * forward-only type, and read-only concurrency, by default.
   *
   * @return a new Statement object
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.Statement createStatement () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    Statement statement = new Statement (jdbcNet_, this);
    openStatements_.addElement (statement);
    return statement;
  }

  /**
   * Prepare SQL as a {@link PreparedStatement PreparedStatement}
   * for subsequent use on this connection.
   *
   * A SQL statement with or without IN parameters can be
   * pre-compiled and stored in a PreparedStatement object. This
   * object can then be used to efficiently execute this statement
   * multiple times.
   * <p>
   * <B>Note:</B> This method is optimized for handling
   * parametric SQL statements that benefit from precompilation. If
   * the driver supports precompilation, prepareStatement will send
   * the statement to the database for precompilation. Some drivers
   * may not support precompilation. In this case, the statement may
   * not be sent to the database until the PreparedStatement is
   * executed.  This has no direct affect on users; however, it does
   * affect which method throws certain SQLExceptions.
   *
   * <p><b>JDBC 2 note:</b>
   * Result sets created using the returned PreparedStatement will have
   * forward-only type, and read-only concurrency, by default.
   *
   * <p><b>InterClient note:</b>
   * InterBase supports statement precompilation of prepared statements,
   * allowing for efficient execution of repeated statement executions.
   * Moreover, unlike many other vendors, InterBase allows prepared
   * statements to remain open across transaction commits and rollbacks.
   *
   * @param sql a SQL statement that may contain one or more '?' IN parameter placeholders.
   * @return a new PreparedStatement object containing the pre-compiled statement.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   */
  synchronized public java.sql.PreparedStatement prepareStatement (String sql) throws java.sql.SQLException
  {
    checkForClosedConnection ();

    PreparedStatement statement = new PreparedStatement (jdbcNet_, this, sql);
    openPreparedStatements_.addElement (statement);
    return statement;
  }

  /**
   * Prepare a CallableStatement for
   * subsequent use on this connection.
   *
   * A SQL stored procedure call statement is handled by creating a
   * CallableStatement for it. The CallableStatement provides
   * methods for setting up its IN and OUT parameters, and
   * methods for executing it.
   *
   * <P><B>Note:</B> This method is optimized for handling stored
   * procedure call statements. Some drivers may send the call
   * statement to the database when the prepareCall is done; others
   * may wait until the CallableStatement is executed. This has no
   * direct affect on users; however, it does affect which method
   * throws certain SQLExceptions.
   *
   * <p><b>JDBC 2 note:</b>
   * Result sets created using the returned CallableStatement will have
   * forward-only type, and read-only concurrency, by default.
   *
   * <p><b>InterClient note:</b>
   * InterBase supports statement precompilation of callable statements,
   * allowing for efficient execution of repeated stored procedure calls.
   * Moreover, unlike many other vendors, InterBase allows callable
   * statements to remain open across transaction commits and rollbacks.
   *
   * @param sql a SQL statement that may contain one or more '?'
   *    parameter placeholders. Typically this  statement is a JDBC
   *    function call escape string.
   * @return a new CallableStatement object containing the pre-compiled SQL statement.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.CallableStatement prepareCall (String sql) throws java.sql.SQLException
  {
    checkForClosedConnection ();

    return new CallableStatement (jdbcNet_, this, sql);
  }

  /**
   * Gets the native SQL recognized by the underlying DBMS for the given
   * JDBC sql string.
   *
   * A driver may convert the JDBC sql grammar into its system's
   * native SQL grammar prior to sending it; nativeSQL returns the
   * native form of the statement that the driver would have sent.
   *
   * @param sql a SQL statement that may contain one or more '?' parameter placeholders
   * @return the native form of this statement
   * @throws java.sql.SQLException if a database access error occurs.
   * @author Madhukar Thakur
   * @since <font color=red>JDBC 1</font>
   */
  public String nativeSQL (String sql) throws java.sql.SQLException
  {
    EscapeProcessor escapeProcessor = new EscapeProcessor ();
    return (escapeProcessor.doEscapeProcessing (sql));

  }

  /**
   * Enable or disable auto-commit on this connection.
   *
   * If a connection is in auto-commit mode, then all its SQL
   * statements will be executed and committed as individual
   * transactions.  Otherwise, its SQL statements are grouped into
   * transactions that are terminated by either commit() or
   * rollback().  By default, new connections are in auto-commit
   * mode.
   * <p>
   * The commit occurs when the statement completes or the next
   * execute occurs, whichever comes first. In the case of
   * statements returning a ResultSet, the statement completes when
   * the last row of the ResultSet has been retrieved or the
   * ResultSet has been closed. In advanced cases, a single
   * statement may return multiple results as well as output
   * parameter values. Here the commit occurs when all results and
   * output param values have been retrieved.
   *
   * <p><b>InterClient note:</b>
   * Although a sequence of auto-committed statements is in general
   * less efficient than a manually committed sequence, InterClient
   * efficiently auto-commits using a native auto-commit mode.
   *
   * @param enableAutoCommit true enables auto-commit; false disables auto-commit.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   */
  synchronized public void setAutoCommit (boolean enableAutoCommit) throws java.sql.SQLException
  {
    checkForClosedConnection ();

    enableAutoCommit_ = enableAutoCommit;

    if (!transactionStartedOnClient_)
      return;

    remote_COMMIT (false);

    local_CloseResultSets (openStatements_);
    local_CloseResultSets (openPreparedStatements_);

    transactionStartedOnClient_ = false;
    transactionStartedOnServer_ = false;
  }

  /**
   * Get the current auto-commit state.
   *
   * @return Current state of auto-commit mode.
   * @throws java.sql.SQLException if a database-access error occurs.
   * @see #setAutoCommit(boolean)
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean getAutoCommit () throws java.sql.SQLException
  {
    return enableAutoCommit_;
  }

  private void local_CloseStatements (java.util.Vector statements) throws java.sql.SQLException
  {
    while (statements.size () != 0) {
      ((Statement) statements.lastElement ()).local_Close ();
    }
  }

  private void local_CloseResultSets (java.util.Vector statements) throws java.sql.SQLException
  {
    for (java.util.Enumeration e = statements.elements ();
	 e.hasMoreElements (); ) {
      ResultSet resultSet = ((Statement) e.nextElement ()).resultSet_;
      if (resultSet != null)
        resultSet.local_Close ();
    }
  }

  // called by Connection.close and by Server.close
  void local_Close () throws java.sql.SQLException
  {
    local_CloseStatements (openStatements_);
    local_CloseStatements (openPreparedStatements_);
    open_ = false;
  }

  /**
   * Commit makes all changes made since the previous
   * commit/rollback permanent and releases any database locks
   * currently held by the Connection. This method should only be
   * used when auto commit has been disabled.
   *
   * @see #setAutoCommit(boolean)
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void commit () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    if (enableAutoCommit_)
      return;
      // JDBCTestWin wants an exception here!!! but JBuilder doesn't
      // throw new InvalidOperationException (ErrorKey.invalidOperation__commit_or_rollback_under_autocommit__);

    if (!transactionStartedOnClient_)
      return;

    remote_COMMIT (false);

    local_CloseResultSets (openStatements_);
    local_CloseResultSets (openPreparedStatements_);

    transactionStartedOnClient_ = false;
    transactionStartedOnServer_ = false;
  }

  private void remote_COMMIT (boolean retain) throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.writeByte (MessageCodes.COMMIT__);
    sendMsg.writeBoolean (retain);

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
   * Rollback drops all changes made since the previous
   * commit/rollback and releases any database locks currently held
   * by the Connection. This method should only be used when auto
   * commit has been disabled.
   *
   * @see #setAutoCommit(boolean)
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void rollback () throws java.sql.SQLException
  {
    checkForClosedConnection ();

    if (enableAutoCommit_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__commit_or_rollback_under_autocommit__);

    if (!transactionStartedOnClient_)
      return;

    remote_ROLLBACK ();

    local_CloseResultSets (openStatements_);
    local_CloseResultSets (openPreparedStatements_);

    transactionStartedOnClient_ = false;
    transactionStartedOnServer_ = false;
  }

  private void remote_ROLLBACK () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.writeByte (MessageCodes.ROLLBACK__);

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
   * In some cases, it is desirable to immediately release a
   * Connection's database and JDBC resources instead of waiting for
   * them to be automatically released; the close method provides this
   * immediate release.
   *
   * <P><B>Note:</B> A Connection is automatically closed when it is
   * garbage collected. Certain fatal errors also result in a closed
   * Connection.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void close () throws java.sql.SQLException
  {
    if (!open_)
      return;

    java.sql.SQLException accumulatedExceptions = null;

    try {
      remote_DETACH_DATABASE ();
    }
    catch (java.sql.SQLException e) {
      accumulatedExceptions = Utils.accumulateSQLExceptions (accumulatedExceptions, e);
    }

    local_Close ();

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

  private void remote_DETACH_DATABASE () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();
    sendMsg.writeByte (MessageCodes.DETACH_DATABASE__);

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
   * Tests to see if a Connection is closed.
   *
   * @return true if the connection is closed; false if it's still open.
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isClosed () throws java.sql.SQLException
  {
    return !open_;
  }

  /**
   * A Connection's database is able to provide information
   * describing its tables, its supported SQL grammar, its stored
   * procedures, the capabilities of this connection, etc. This
   * information is made available through a DatabaseMetaData object.
   *
   * @return a DatabaseMetaData object for this Connection.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.DatabaseMetaData getMetaData () throws java.sql.SQLException
  {
    return databaseMetaData_;
  }

  /**
   * You can put a connection in read-only mode as a hint to enable database optimizations.
   *
   * <P><B>Note:</B> setReadOnly cannot be called while in the middle of a transaction.
   *
   * <p><b>InterClient note:</b>
   * A read-only transaction may only read data, and has no write access to tables.
   *
   * @param readOnly true enables read-only mode; false disables read-only mode.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setReadOnly (boolean readOnly) throws java.sql.SQLException
  {
    // isc_tpb_read, isc_tpb_write
    checkForClosedConnection ();

    if (transactionStartedOnClient_) {
      throw new InvalidOperationException (ErrorKey.invalidOperation__transaction_in_progress__);
    }

    readOnly_ = readOnly;
  }

  /**
   * Tests to see if the connection is in read-only mode.
   *
   * @return true if connection is read-only
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isReadOnly () throws java.sql.SQLException
  {
    return readOnly_;
  }

  /**
   * A sub-space of this Connection's database may be selected by setting a
   * catalog name. If the driver does not support catalogs it will
   * silently ignore this request.
   *
   * <p><b>InterClient note:</b>
   * InterBase does not support catalogs so this request
   * is ignored and is effectively a no-op.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  public void setCatalog (String catalog) throws java.sql.SQLException
  { 
    //checkForClosedConnection ();
  }

  /**
   * Return the Connection's current catalog name.
   *
   * <p><b>InterClient note:</b>
   * Always returns <code>null</code>.
   * InterBase does not support catalogs.
   * 
   * @return the current catalog name or null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getCatalog () throws java.sql.SQLException
  {
    //checkForClosedConnection ();
    //throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
    return null;
  }

  /**
   * You can call this method to try to change the transaction
   * isolation level using one of the TRANSACTION_* values.
   *
   * <P><B>Note:</B> setTransactionIsolation cannot be called while
   * in the middle of a transaction.
   *
   * <p><b>InterClient note:</b>
   * InterBase supports the JDBC isolation levels
   * {@link #TRANSACTION_REPEATABLE_READ TRANSACTION_REPEATABLE_READ},
   * {@link #TRANSACTION_READ_COMMITTED TRANSACTION_READ_COMMITTED},
   * {@link #TRANSACTION_SERIALIZABLE TRANSACTION_SERIALIZABLE}
   * as well as an additional native isolation level
   * {@link #TRANSACTION_SNAPSHOT_TABLE_STABILITY TRANSACTION_SNAPSHOT_TABLE_STABILITY}
   * surfaced as extension to the base JDBC API.
   * The default isolation level is
   * {@link #TRANSACTION_SERIALIZABLE TRANSACTION_SERIALIZABLE}.
   *
   * @param level one of the TRANSACTION_* isolation values with the
   * exception of TRANSACTION_NONE; some databases may not support other values.
   * @see interbase.interclient.DatabaseMetaData#supportsTransactionIsolationLevel
   * @see #setLockResolution(int)
   * @see #setTableLock(java.lang.String,int)
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setTransactionIsolation (int level) throws java.sql.SQLException
  {
    checkForClosedConnection ();

    if (transactionStartedOnClient_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__transaction_in_progress__);

    switch (level) {
    case TRANSACTION_REPEATABLE_READ:
    case TRANSACTION_READ_COMMITTED:
    case TRANSACTION_SERIALIZABLE:  // aka TRANSACTION_SNAPSHOT
    case TRANSACTION_SNAPSHOT_TABLE_STABILITY:
      isolation_ = level;
      break;
    case TRANSACTION_READ_UNCOMMITTED:
      throw new DriverNotCapableException (ErrorKey.driverNotCapable__isolation__);

    case TRANSACTION_NONE: 
    default:
      throw new InvalidArgumentException (ErrorKey.invalidArgument__isolation_0__,
					  String.valueOf (level));
    }
  }

  /**
   * Get this Connection's current transaction isolation mode.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return the current transaction mode TRANSACTION_* value.
   * @since <font color=red>JDBC 1</font>
   **/
  public int getTransactionIsolation () throws java.sql.SQLException
  {
    return isolation_;
  }

  /**
   * The first warning reported by calls on this Connection is
   * returned.
   *
   * <P><B>Note:</B> Subsequent warnings will be chained to this SQLWarning.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @return the first SQLWarning or null
   * @since <font color=red>JDBC 1</font>
   **/
  public java.sql.SQLWarning getWarnings () throws java.sql.SQLException
  {
    return sqlWarnings_;
  }

  /**
   * After this call, getWarnings returns null until a new warning is
   * reported for this Connection.
   *
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void clearWarnings () throws java.sql.SQLException
  {
    sqlWarnings_ = null;
  }

  // Called by Driver on connect when driver is expired.
  synchronized void setWarning (java.sql.SQLWarning e)
  {
    if (sqlWarnings_ == null)
      sqlWarnings_ = e;
    else
      sqlWarnings_.setNextException (e);
  }

  // Called on Statement prepare and execute.
  void send_TransactionConfigData (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    if (!transactionStartedOnServer_) {
      sendMsg.writeBoolean (true); // config data follows
      sendMsg.writeBoolean (readOnly_);
      sendMsg.writeByte (isolation_);
      sendMsg.writeBoolean (enableRecVersion_);
      sendMsg.writeByte (lockResolution_);
      sendMsg.writeBoolean (enableAutoCommit_);
      sendMsg.writeBoolean (false); // antiquated "enableAutoClose_"
      if (tableLocks_ == null)
        sendMsg.writeInt (0);
      else {
        sendMsg.writeInt (tableLocks_.size());
        java.util.Enumeration tableEnum = tableLocks_.keys ();
        while (tableEnum.hasMoreElements ()) {
          String tableName = (String) tableEnum.nextElement ();
          sendMsg.writeLDSQLText (tableName);
          sendMsg.writeByte (((Integer) tableLocks_.get (tableName)).intValue ());
        }
      }
    }
    else
      sendMsg.writeBoolean (false); // no config data to follow
  }

  /**
   * JDBC Isolation level in which transactions are not supported.
   *
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int TRANSACTION_NONE	      = 0;

  /**
   * JDBC Isolation level in which dirty reads, non-repeatable reads and phantom reads can occur.
   * This level allows a row changed by one transaction to be read
   * by another transaction before any changes in that row have been
   * committed (a "dirty read").  If any of the changes are rolled back,
   * the second transaction will have retrieved an invalid row.
   *
   * <p><b>InterClient note:</b>
   * InterBase does not support such an unrestricted and dangerous isolation level.
   *
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int TRANSACTION_READ_UNCOMMITTED = 1;

  /**
   * JDBC Isolation level in which dirty reads are prevented; non-repeatable reads and phantom
   * reads can occur.  This level only prohibits a transaction
   * from reading a row with uncommitted changes in it.
   *
   * <p><b>InterClient notes:</b>
   * InterBase supports two kinds of read-committed transactions.
   * The first kind of read-committed transaction, known as <i>read-committed record-versions</i>,
   * can read a committed record even while it's being modified by another
   * transaction, that is the record may have an uncommitted version.
   * The second kind of read-committed transaction, known as
   * <i>read-committed no-record-versions</i>,
   * will either block or abort while trying to read a record version which
   * has an uncommitted version
   * (depending on the lock resolution mode of <i>wait</i> or <i>no-wait</i>).
   * The two modes may be configured using
   * {@link #setVersionAcknowledgement(int) setVersionAcknowledgement(int mode)}
   * with either mode
   * {@link #IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}
   * or
   * {@link #RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}.
   * The default mode for a read-committed transaction is
   * {@link #RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}.
   *
   * @since <font color=red>JDBC 1</font>
   * @docauthor Paul Ostler
   **/
  final static public int TRANSACTION_READ_COMMITTED   = 2; // isc_tpb_read_committed

  /**
   * JDBC Isolation level in which dirty reads and non-repeatable reads are prevented; phantom
   * reads can occur.  This level prohibits a transaction from
   * reading a row with uncommitted changes in it, and it also
   * prohibits the situation where one transaction reads a row,
   * a second transaction alters the row, and the first transaction
   * rereads the row, getting different values the second time
   * (a "non-repeatable read").
   *
   * <p><b>InterClient note:</b>
   * Repeatable reads are achieved using InterBase's snapshot model of concurrency,
   * which was known as <i>Concurrency</i> mode prior to v4.
   *
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int TRANSACTION_REPEATABLE_READ  = 4; // isc_tpb_concurrency

  /**
   * JDBC Isolation level in which dirty reads, non-repeatable reads and phantom reads are prevented.
   * This level includes the prohibitions in
   * TRANSACTION_REPEATABLE_READ and further prohibits the
   * situation where one transaction reads all rows that satisfy
   * a WHERE condition, a second transaction inserts a row that
   * satisfies that WHERE condition, and the first transaction
   * rereads for the same condition, retrieving the additional
   * "phantom" row in the second read.
   *
   * <p><b>InterClient note:</b>
   * InterBase's multi-generational architecture, aka versioning engine,
   * provides the superb implementation of serializability
   * through the snapshot model of concurrency.
   * Under the snapshot model, once a transaction starts,
   * a snapshot of committed records is taken when the transaction starts,
   * the snapshot is insensitive to the record changes of other transactions.
   * Only changes made within the local transaction itself are visible to the
   * localized snapshot.  Writers in another transaction will not block
   * readers within the local transaction because the local transaction reads
   * from the local snapshot only.  Similarly Readers in another transaction
   * will not block writes within the local transaction.
   * However, serializability and effective record locking is achieved
   * because a write within the local transaction will block or abort
   * (depending on the lock resolution mode of wait or no-wait) if another
   * transaction has an uncommitted modification (or version) of that record.
   * In other words, an uncommitted record version provides an effective write-lock
   * on a record.
   * <p>
   * This native isolation was known as <i>Concurrency</i> mode prior to v4,
   * and is also described as the <i>Snapshot</i> model of concurrency.
   *
   * @since <font color=red>JDBC 1</font>
   * @docauthor Paul Ostler
   **/
  final static public int TRANSACTION_SERIALIZABLE     = 8;  // isc_tpb_concurrency

  // ------------------------ JDBC 2 -----------------------------
  
  /**
   * Same as <code>createStatement()</code> above, but allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type, see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
   * @return a new Statement object
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Statement createStatement (int resultSetType,
                                                          int resultSetConcurrency) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Same as <code>prepareStatement()</code> above, but allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type, see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
   * @return a new PreparedStatement object containing the pre-compiled SQL statement
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public java.sql.PreparedStatement prepareStatement (String sql,
                                                                   int resultSetType,
                                                                   int resultSetConcurrency) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Same as the JDBC 1 {@link #prepareCall(String) prepareCall(sql)},
   * but allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type, see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
   * @return a new CallableStatement object containing the pre-compiled SQL statement
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public java.sql.CallableStatement prepareCall (String sql,
                                                              int resultSetType,
				                              int resultSetConcurrency) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get the type-map object associated with this connection.
   * By default, the map returned is empty.
   *
   * @since <font color=red>JDBC 2, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/ //*start jre12*
  synchronized public java.util.Map getTypeMap () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Install a type-map object as the default type-map for
   * this connection.
   *
   * @since <font color=red>JDBC 2, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/ //*start jre12*
  synchronized public void setTypeMap (java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  // --------------- InterClient extensions ----------------------

  // IB supports 4 transaction isolation levels:
  //
  // 	Snapshot
  // 	Read_Committed	Record_Version
  // 	Read_Committed	No_Record_Version	Wait
  // 	Read_Committed	No_Record_Version	No_Wait
  //
  // i.e. you can take a Snapshot at a moment in time, or read the next
  // committed version even if it happened after your transaction started. If
  // you set Read_Committed, you can ignore a newer version of a row that has
  // not been committed (Record_Version), or request it (No_Record_Version)
  // and the behavior depends on the Wait setting. With Wait, IB waits to see
  // if the newer version commits or rolls back; if the former, an error is
  // returned. If the latter the record is read. With No_Wait, an error is
  // returned immediately.
  //
  // IB's default is Snapshot. You can set this transaction isolation level
  // by setting the TransIsolation property of tiRepeatableRead for the
  // TDatabase component in Delphi, or by setting DRIVER FLAGS=512 for the IB
  // driver in the BDE Config program.
  //
  // The BDE default is "Read_Committed | No_Record_Version | No_Wait." You
  // can set this transaction isolation level by setting the TransIsolation
  // property of tiReadCommitted for the TDatabase. This setting goes against
  // everything that is "good" (tm) in IB.

  /**
   * A synonym for
   * {@link #TRANSACTION_SERIALIZABLE TRANSACTION_SERIALIZABLE}.
   * <p>
   * This is the native default isolation mode for InterBase,
   * and was formerly known as <i>Concurrency</i> mode prior to v4.
   * <p>
   * SNAPSHOT transactions keep a snapshot of the database
   * when the transaction starts and do not <i>see</i> the committed updates
   * of simultaneous transactions.
   * This versioning of records provides for serializability in which
   * writers don't block readers, and readers don't block writers.
   * Thereby allowing for high transaction throughput.
   * This isolation level is possible because of InterBase's
   * multi-generational architecture.
   * Although rare, update side effects can occur with this isolation level,
   * but these can be avoided through the use of triggers or validity
   * constraints.
   * <p>
   * This variable is identical to the JDBC isolation level
   * <code>TRANSACTION_SERIALIZABLE</code>
   * and exists only for the sake of promoting InterBase terminology.
   *
   * @see #setTransactionIsolation(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TRANSACTION_SNAPSHOT = TRANSACTION_SERIALIZABLE; // isc_tpb_concurrency

  /**
   * A native InterBase isolation which ensures exclusive write-access
   * to tables which are being modified.
   * This is the most restrictive InterBase isolation mode in which a transaction
   * is prevented from accessing tables if they are being written by
   * other transactions; it also prevents other transactions from writing
   * to a table once this transaction writes to it.
   * This isolation level is designed to guarantee that if a transaction
   * writes to a table before other simultaneous transactions,
   * then only it can change a table's data.
   * <p>
   * This isolation level was formerly known as <i>Consistency</i> mode
   * prior to v4.
   *
   * @see #setTransactionIsolation(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TRANSACTION_SNAPSHOT_TABLE_STABILITY = 16; // isc_tpb_consistency


  /**
   * Configures the transaction lock resolution protocol.
   * The two possible modes are {@link #LOCK_RESOLUTION_WAIT LOCK_RESOLUTION_WAIT}
   * or {@link #LOCK_RESOLUTION_NO_WAIT LOCK_RESOLUTION_NO_WAIT}.
   * The default resolution mode is {@link #LOCK_RESOLUTION_WAIT LOCK_RESOLUTION_WAIT}.
   *
   * <P><B>Note:</B> This method may not be called while
   * in the middle of a transaction.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setLockResolution (int mode) throws java.sql.SQLException
  { 
    checkForClosedConnection ();

    if (transactionStartedOnClient_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__transaction_in_progress__);

    switch (mode) {
    case LOCK_RESOLUTION_WAIT:
    case LOCK_RESOLUTION_NO_WAIT:
      lockResolution_ = mode;
      break;

    default:
      throw new InvalidArgumentException (ErrorKey.invalidArgument__lock_resolution__);
    }
  }

  /**
   * Gets this Connection's current lock resolution mode.
   * This can be either
   * {@link #LOCK_RESOLUTION_WAIT LOCK_RESOLUTION_WAIT}
   * or
   * {@link #LOCK_RESOLUTION_NO_WAIT LOCK_RESOLUTION_NO_WAIT}.
   *
   * @see #setLockResolution(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return LOCK_RESOLUTION_WAIT or LOCK_RESOLUTION_NOWAIT
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getLockResolution () throws java.sql.SQLException
  {
    return lockResolution_;
  }

  /**
   * The default lock resolution mode for InterBase in which a transaction
   * will wait for a database lock.
   * <p>
   * This is the lock resolution protocol in which a transaction will wait 
   * for conflicting transactions to release locks.
   * <p>
   * <b>Note:</b> This native InterBase resolution is also
   * known as <i>WAIT</i> mode.
   *
   * @see #setLockResolution(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int LOCK_RESOLUTION_WAIT = 0; // isc_tpb_wait

  /**
   * The InterBase lock resolution protocol in which a transaction will not wait for
   * locks to be released, but instead, a LockConflictException is thrown immediately.
   * <p>
   * <b>Note:</b> This native InterBase resolution is also
   * known as <i>NO WAIT</i> mode.
   *
   * @see #setLockResolution(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int LOCK_RESOLUTION_NO_WAIT = 1; // isc_tpb_nowait

  /**
   * Configures a read-committed transaction to either recognize or ignore
   * uncommitted record versions of a concurrent transaction on read.
   * The two possible modes are
   * {@link #RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}
   * or
   * {@link #IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}.
   * The default version-acknowledgement mode is
   * {@link #RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}.
   * <p>
   * This method may not be called while in the middle of a transaction.
   * Furthermore, this method is only applicable to read-committed transactions.
   * <p>
   * If uncommitted record versions are acknowledged, then a transaction
   * may only read the latest version of a row.
   * If an uncommitted row update is pending, then the transaction
   * will either wait on read or throw a
   * {@link LockConflictException LockConflictException},
   * depending on the lock resolution mode.
   * <p>
   * If uncommitted record versions are ignored, then the transaction
   * can immediately read the latest committed version of a row,
   * even if a more recent uncommitted version is pending.
   * So uncommitted record versions of concurrent transactions are ignored on read.
   *
   * @see #setLockResolution(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setVersionAcknowledgement (int mode) throws java.sql.SQLException
  {
    // isc_tpb_rec_version, isc_tpb_no_rec_version
    checkForClosedConnection ();

    if (transactionStartedOnClient_) {
      throw new InvalidOperationException (ErrorKey.invalidOperation__transaction_in_progress__);
    }

    if (mode == IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ)
      enableRecVersion_ = true;
    else if (mode == RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ)
      enableRecVersion_ = false;
    else
      throw new InvalidArgumentException (ErrorKey.invalidArgument__version_acknowledgement_mode__);
  }

  /**
   * Gets the current version-acknowledgement mode for a read-committed transaction.
   * This can be either
   * {@link #RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}
   * or
   * {@link #IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ}.
   *
   * @see #setVersionAcknowledgement(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @return RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ or IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getVersionAcknowledgement () throws java.sql.SQLException
  {
    if (enableRecVersion_)
      return IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ;
    else
      return RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ;
  }

  /**
   * A read-committed version-acknowledgement mode in which
   * transactions read the most recent
   * committed updates of concurrent transactions.
   * Uncommitted record versions of concurrent transactions are ignored on read.
   * <p>
   * This is the default behavior for read-committed transactions
   * in InterClient and the BDE.  
   * This version-acknowlegement mode is only meaningful for
   * read-committed transactions.
   * <p>
   * <b>Note:</b> This native InterBase read-committed mode is also
   * known as <i>RECORD_VERSION</i>.
   *
   * @see #TRANSACTION_READ_COMMITTED
   * @see #setVersionAcknowledgement(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ = 1; // isc_tpb_rec_version

  /**
   * A read-committed version-acknowledgement mode in which
   * transactions read the last committed
   * update but not if an uncommitted update is pending.
   * <p>
   * If an uncommitted update is pending, then this transaction
   * will either wait on read or throw a
   * {@link LockConflictException LockConflictException},
   * depending on the lock resolution mode.
   * <p>
   * This version-acknowlegement mode is only meaningful for
   * read-committed transactions.
   * <p>
   * <b>Note:</b> This native InterBase read-committed mode is also
   * known as <code>NO RECORD_VERSION</code>.
   *
   * @see #TRANSACTION_READ_COMMITTED
   * @see #setVersionAcknowledgement(int)
   * @see #setLockResolution(int)
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ = 0; // isc_tpb_no_rec_version

  /**
   * Specify an InterBase lock on a table.
   * Table lock modes are given by the <code>TABLELOCK_*</code> variables.
   *
   * <P><B>Note:</B> This method may not be called while
   * in the middle of a transaction.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setTableLock (String table, int tableLock) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
    /**
    checkForClosedConnection ();

    if (transactionStartedOnClient_)
      throw new InvalidOperationException (ErrorKey.invalidOperation__transaction_in_progress__);

    switch (tableLock) {
    case TABLELOCK_PROTECTED_READ:
    case TABLELOCK_PROTECTED_WRITE:
    case TABLELOCK_SHARED_READ:
    case TABLELOCK_SHARED_WRITE:
      if (tableLocks_ == null)
        tableLocks_ = new java.util.Hashtable ();
      tableLocks_.put (table, new Integer (tableLock));
      break;

    default:
      throw new InvalidArgumentException (ErrorKey.invalidArgument__table_lock__);
    }
    **/
  }

  /**
   * Gets the current table lock mode on a table for this transaction.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @return Table lock mode TABLELOCK_* value.
   * @throws java.sql.SQLException if a database access error occurs.
   */
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getTableLock (String table) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
    /**
    if (tableLocks_ == null)
      return TABLELOCK_SHARED_WRITE;
    else if (tableLocks_.get (table) == null)
      return TABLELOCK_SHARED_WRITE;
    return ((Integer) tableLocks_.get (table)).intValue ();
    **/
  }

  /**
   * The default table lock for snapshot and read-committed transactions
   * in which a table may be shared by concurrent transactions for writes.
   * So concurrent transactions may both write to the same tables.
   * <p>
   * Permits table writes by concurrency and read-committed mode
   * transactions with write access;
   * Permits table reads by concurrency and read-committed mode
   * transactions with read access.
   * <p>
   * This table lock mode is disallowed for
   * {@link #TRANSACTION_SNAPSHOT_TABLE_STABILITY TRANSACTION_SNAPSHOT_TABLE_STABILITY}
   * transactions, which are inherently protected.
   *
   * @see #setTableLock(String,int)
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TABLELOCK_SHARED_WRITE = 0; // isc_tpb_shared, isc_tpb_lock_write

  /**
   * A table lock in which a table may be accessed read-only,
   * and may also be shared by concurrent transactions.
   * Table writes for this transaction are disallowed.
   * <p>
   * Permits table writes by other transactions with write access;
   * Permits table reads by any transaction.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @see #setTableLock(String,int)
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TABLELOCK_SHARED_READ = 1; // isc_tpb_shared, isc_tpb_lock_read

  /**
   * A table lock in which this transaction reserves exclusive access
   * to a table for writes.  Other transactions may not write
   * to the locked table.
   * <p>
   * Permits exclusive table writes only;
   * Permits table reads by snapshot and read-committed mode
   * transactions with read access.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @see #setTableLock(String,int)
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TABLELOCK_PROTECTED_WRITE = 2; // isc_tpb_protected, isc_tpb_lock_write

  /**
   * A table lock in which this transaction reserves exclusive access
   * to a table for reads.  No other transaction may write to the
   * locked table.
   * <p>
   * Disallows all table writes;
   * Permits table reads by all transactions.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   * @see #setTableLock(String,int)
   **/
// CJL-IB6 changed reference to InterClient 2.0
  final static public int TABLELOCK_PROTECTED_READ = 4; // isc_tpb_protected, isc_tpb_lock_read


  /**
   * Like commit but keeps your snapshot and retains your transaction context.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void commitRetain () throws java.sql.SQLException
  {
    // isc_commit_retain()
    checkForClosedConnection ();

    if (enableAutoCommit_)
      return;
      // JDBCTestWin wants an exception here!!! but JBuilder doesn't
      // throw new InvalidOperationException (ErrorKey.invalidOperation__commit_or_rollback_under_autocommit__);

    if (!transactionStartedOnClient_)
      return;

    remote_COMMIT (true);

    local_CloseResultSets (openStatements_);
    local_CloseResultSets (openPreparedStatements_);
  }

  /**
   * Get the InterBase transaction id for the current transaction.
   * <p>
   * This is a placeholder for proposed future functionality.
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
  synchronized public int getTransactionId () throws java.sql.SQLException
  {
    // !!!
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
      
  /**
   * Is a transaction currently active on this connection.
   * <p>
   * This is a placeholder for proposed possible future functionality.
   *
   * @since <font color=red>Extension, proposed for possible future release, not yet supported</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
  synchronized public boolean inTransaction () throws java.sql.SQLException
  {
    // !!! should we surface this???
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
// CJL-IB6 added to SQL Dialect support
  /**
   * Gets this Connection's current SQL dialect.
   * With InterBase 5 databases, this value is always 1.
   * With InterBase 6 databases, this can be either
   * <p>
   * 1: indicating InterBase 5 compatibilty
   * 2: indicating a transition mode
   * 3: indicating a mode that supports all SQL features of InterBase 6
   * <p>
   * @see DatabaseMetaData#getDatabaseSQLDialect()
   * @since <font color=red>Extension since InterClient 2.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
  synchronized public int getAttachmentSQLDialect () throws java.sql.SQLException
  {
    return attachmentSQLDialect_;
  }

    @Override
    public void setHoldability(int holdability) throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        throw new SQLClientInfoException("Not supported", new HashMap<String, ClientInfoStatus>() {{
            put(name, ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
        }});
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        HashMap<String, ClientInfoStatus> failed = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            failed.put(entry.toString(), ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
        }

        throw new SQLClientInfoException("Not supported", failed);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public String getSchema() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
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
