// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)

/**   
 * Portable performance test suite for InterClient or any other JDBC driver.
 * <p>
 * <b>IMPORTANT STEPS BEFORE RUNNING THIS TEST</b>
 * <ol>
 * <li> 
 * Create an empty test database file to be used for performance tests.
 * Metadata is created automatically by the test program,
 * but a test database must be created manually before running tests.
 * <p>                       
 * For InterBase, manually create test.gdb in the directory of your choice as follows:
 * <ul>      
 * <li> isql
 * <li> create database "test.gdb" [user "sysdba" password "masterkey"];
 * <li> quit;  
 * </ul>
 * The brackets ([]) indicate an optional user/password, only required
 * if system environment variables ISC_USER and ISC_PASSWORD are not set.
 * <p>
 * Some database products, like Oracle, manage their own filespace
 * so skip this step for such products.
 * <li>    Tailor configuration variables.
 *         This is done programmatically by setting the public configuration
 *         variables of this class programmatically in an application program
 *         (with a <code>main()</code>) external to this
 *         <code>PerformanceTests</code> library.
 *         Do <i>not</i> edit the default values in the
 *         <code>PerformanceTest</code> source.
 *         Rather, edit a copy of class <code>interbase.interclient.tests.ExampleTests</code>.
 *         or copy the <code>ExampleTests</code> program and modify its source.
 *         You may want to change the package name (interbase.interclient.tests)
 *         on the first line of your copy of the <code>ExampleTests</code>
 *         source file to the name
 *         of your choice, making sure
 *         the package name matches the new directory location of your test
 *         program relative to your class path,
 *         then compile the new program source.
 *         <p>
 *         Configure variables for the
 *         URLs, drivers, and test control switches in <code>ExampleTests</code> .
 *         <p>
 *         You must manually configure ODBC and BDE
 *         data-source names for test.gdb using the ODBC or BDE
 *         configuration utilities.  For consistency, use
 *         ODBC and BDE alias names as suggested by the defaults
 *         of the various configuration variables in <code>ExampleTests</code>.
 *         The ODBC configuration utiltity
 *         can usually be found in the NT control panel.
 *         The BDE Administrator can be found in the DataGateway program group.
 * <li>    Be sure to start InterServer, DataGateway, InterBase, Sybase Open Server,
 *         dbANYWHERE, or any other servers tested before running <code>ExampleTests</code>.
 *         Also, make sure DataGateway.zip, InterClient.jar,
 *         and any other driver to test are in a library class path for your project.
 * <li>    Discard the performance results of the first test run in order
 *         to factor out the overhead in initial class loading, and
 *         the initial database page allocation for an originally
 *         empty test.gdb.  In other words, run the test twice
 *         so that Java classes are pre-loaded, and take
 *         the results of the second test only.
 * <li>    Look for results and/or errors in the specified output file
 *         as configured.
 * </ol>
 **/
public class PerformanceTests
{
  // *******************************************
  // *** Default public configurables.
  // *** Do not modify the default values in this file.
  // *** Modify publics programmatically via an external class
  // *** such as ExampleTests rather than hardwiring
  // *** values in this class.
  // *******************************************

  /**
   * Output file to write the performance test results.
   * This is where you'll want to look after running a test.
   * <p>
   * The default value is null, which means output will be written to System.out.
   **/
  public static String outputFileName = null;

  /**
   * An array of database engines, usernames, passwords, URLs, and drivers you wish to test.
   * <p>
   * Each element in the array consists of a tuple
   * <code>{ engineName, username, password, URL, driverClassName }</code>.
   * The engine name is needed
   * to construct dbms-dependent SQL.  Valid engine names are
   * "interbase", "oracle", etc...
   * Some drivers, such as DataGateway, can be used against
   * different engines (known as category 3 drivers).
   * <p>
   * Here are some example client/server tuples that could be tested
   * The remote hostname may be set to localhost to test client/server in loopback mode.
   * <pre>
   * PerformanceTests.driversToTest = new String[][] {
   * // *** Client/server URLs ***
   * // JDBC-ODBC bridge used remotely (uses native dbms client api and dbms net protocol)
   *  { "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:odbc:clientServerTest",
   *    "sun.jdbc.odbc.JdbcOdbcDriver" }
   * // InterClient (uses custom client api and remote protocol (RP))
   * ,{ "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:interbase://hostname/d:/databases/test.gdb",
   *    "interbase.interclient.Driver" }
   * // Datagateway Broker  (uses custom client api and remote protocol (RP))
   * ,{ "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:BorlandBroker://hostname/localTest",
   *    "borland.jdbc.Broker.RemoteDriver" }
   * // Datagateway bridge used remotely (uses native dbms client api and RP)
   * ,{ "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:BorlandBridge:clientServerTest",
   *    "borland.jdbc.Bridge.LocalDriver" }
   * // Sybase jConnect (uses custom client api, but uses TDS remote protocol)
   * ,{ "sqlanywhere",
   *    "dba",
   *    "sql",
   *    "jdbc:sybase:Tds:hostname:4444/d:\\sqlany50\\sademo.db",
   *    "com.sybase.jdbc.SybDriver" }
   * // Symantec dbAnywhere
   * ,{ "sqlanywhere",
   *    "dba",
   *    "sql",
   *    "jdbc:dbaw://hostname:8889/Sybase_SQLANY/Sademo/Sademo",
   *    "symantec.itools.db.jdbc.Driver" }
   * // Oracle Thin Driver (uses custom client api, but uses Oracle remote protocol?)
   * ,{ "oracle",
   *    "user",
   *    "password",
   *    "jdbc:oracle:thin:@hostname:1521:ORCL",
   *    "oracle.jdbc.driver.OracleDriver" }
   * // Solid Driver uses Solid protocol
   * ,{ "solid",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:solid://hostname:1313/sysdba/masterkey",
   *    "solid.jdbc.SolidDriver" }
   * //
   * // *** Local URLs follow (not client/server) ***
   * // Datagateway bridge used locally
   * ,{ "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:BorlandBridge:localTest",
   *    "borland.jdbc.Bridge.LocalDriver" }
   * // JDBC-ODBC bridge used locally
   * ,{ "interbase",
   *    "sysdba",
   *    "masterkey",
   *    "jdbc:odbc:localTest"
   *    "sun.jdbc.odbc.JdbcOdbcDriver" }
   * };
   * </pre>
   **/
  public static String[][] driversToTest = null;

  // *************************************************
  // *** Configurable test control switches follow ***
  // *** (ie. what to test and what not to test).  ***
  // *** Modify values thru an external application rather than ***
  // *** modifying the default values as set here. ***
  // *************************************************

  // *** Toggles for performance suites ***
  public static boolean testMetaData = false;   // exhaustive database metadata extraction
  public static boolean testStrings = false;    // various CHAR(n) tests
  public static boolean testBLObs = false;      // various BLOb tests
  public static boolean testNumbers = false;    // various numeric data
  public static boolean testDates = false;      // Dates, Times, and Timestamps
  public static boolean testProcedures = false; // Stored procedures

  public static boolean test1ByteBlobs = false;      // uses dataSize/1 rows
  public static boolean test10ByteBlobs = false;     // uses dataSize/10 rows
  public static boolean test100ByteBlobs = false;    // uses dataSize/100 rows
  public static boolean test1000ByteBlobs = false;   // uses dataSize/1000 rows
  public static boolean test10000ByteBlobs = false;  // uses dataSize/10000 rows
  public static boolean test100000ByteBlobs = false; // uses dataSize/100000 rows

  public static boolean test1CharStrings = false;  // most time consuming
  public static boolean test10CharStrings = false;
  public static boolean test100CharStrings = false;
  public static boolean test1000CharStrings = false;
  public static boolean test10000CharStrings = false; // least time consuming

  /**
   * Size of bulk data loads for testing.
   * <p>
   * <code>testDataSize</code> indicates how many bytes are inserted or selected for each test.
   * For example, if the <code>testDataSize</code> = 100,000, then the 1-byte blob test will
   * insert 100,00 rows of 1-byte blobs, but the 100,000-byte blob test will
   * insert only a single row.  Decreasing <code>dataSize</code> will decrease the time
   * required to run the performance tests.
   * <p>
   * Default value is 1,000.
   * It is recommended to always make dataSize divisible by 1,000.
   **/
  public static int testDataSize = 1000; // make it divisible by 1,000

  /**
   * See InterClient API reference for <code>Connection.setAutoCommit()</code>.
   * <p>
   * Default is false.
   * @see interbase.interclient.Connection#setAutoCommit
   **/
  public static boolean enableAutoCommit = false;

  /**
   * Direct the driver manager log stream to <code>ouputFileName</code>.
   * See JavaSoft API reference for <code>DriverManager.setLogStream()</code>.
   * <p>
   * Default is false.
   * @see java.sql.DriverManager#setLogStream
   **/
  public static boolean enableDriverManagerLogStream = false;

  /**
   * Character set name, see InterClient API reference for class
   * <code>interbase.interclient.ConnectionProperties</code> for mappings
   * to InterBase character sets.
   * <p>
   * Default is null which means to use the driver defaults.
   * @see interbase.interclient.ConnectionProperties
   **/
  public static String characterSet = null;

  /**
   * This is the character to use to construct test strings.
   * For example, if test100chars is enabled, then a test string
   * of 100 characters will be created using this character.
   * <p>
   * Default is 'X'.
   * @see #characterSet
   **/
  public static char character = 'X';
  
  // **********************************************
  // *** End of Configurables *********************
  // **********************************************

  // *** specifies the number of bytes per row in NUMBER_TABLE ***
  // *** set below with table declaration for number tests ***
  private static int numberBytesPerRecord__;

  // *** String data to be inserted and selected from test database ***
  private static String s1char__ = null;
  private static String s10chars__ = null;
  private static String s100chars__ = null;
  private static String s1000chars__ = null;
  private static String s10000chars__ = null;

  // *** Blob data to be inserted and selected from test database ***
  private static byte[] b1byte__ = null;
  private static byte[] b10bytes__ = null;
  private static byte[] b100bytes__ = null;
  private static byte[] b1000bytes__ = null;
  private static byte[] b10000bytes__ = null;
  private static byte[] b100000bytes__ = null;

  // *** Performance metric variables ***
  private static long startTime__, endTime__, elapsedTime__;

  // *** The engine currently being tested, this is used to determine
  // *** the variant of SQL to use.
  // *** Engine names are passed in as part of the urlsToTest array.
  private static String engine__;

  // *** JDBC global object declarations ***
  private static java.sql.Driver d__ = null;
  private static java.sql.Connection c__ = null;
  private static java.sql.Statement s__ = null;
  private static java.sql.PreparedStatement ps__ = null;
  private static java.sql.ResultSet rs__ = null;
  private static java.util.Properties properties__ = null;
  private static String user__ = null;
  private static String password__ = null;
  private static String url__ = null;

  // *** Print stream for writing results ***
  private static java.io.PrintStream printStream__ = null;
  
  /**
   * Run the performance tests under the configuration as set
   * by the public configuration variables.
   * Results are written to <code>outputFileFile</code>.
   **/
  synchronized static public void run ()
  {
    // this forces jdbc objects to be closed before
    // this java application exits.
    System.runFinalizersOnExit (true);

    if (outputFileName == null) {
      printStream__ = System.out; // System.out is the default
    }
    else {
      try {
        printStream__ =
          new java.io.PrintStream (new java.io.FileOutputStream (outputFileName),
                                   true);  // auto-flush
      }
      catch (java.io.IOException e) {
        System.out.println  ("Couldn't open print stream to " + outputFileName +
                             "Aborting tests...");
        System.exit (1);
      }
    }

    showPerformanceTuningVariables ();

    try {
      int i;
      
      populateStaticData ();

      if (enableDriverManagerLogStream)
        java.sql.DriverManager.setLogStream (printStream__);

      // *** Load all available drivers from your class path ***
      for (i=0; i<driversToTest.length; i++) {
        try {
          Class.forName (driversToTest[i][4]);
        }
        catch (ClassNotFoundException e) {
	  printStream__.println ("Driver " +
                               driversToTest[i][4] +
                               " not found in classpath.");
        }
      }

      printStream__.println ("******************************************************************************");
      printStream__.println ("*** Beginning performance tests ***");
      printStream__.println ("*** The number of rows and bytes selected should match the number inserted ***");
      printStream__.println ("******************************************************************************");

      // *** Test all urls specified in driversToTest ***
      for (i=0; i<driversToTest.length; i++) {
        engine__ = driversToTest[i][0];
        user__ = driversToTest[i][1];
        password__ = driversToTest[i][2];
        url__ = driversToTest[i][3];
        printStream__.println ();
        printStream__.println ("*** Testing " + url__ + " ***");
        test ();
        printStream__.println ("*** Finished testing " + url__ + " ***");
      }

      printStream__.println ();
      printStream__.println ("*** End Of Tests ***");
      // If you're using the JBuilder debugger,
      // check for errors under JBuilder | View | Execution Log.
    }
    catch (java.sql.SQLException e) {
      showException (e);
    }
    finally {
      printStream__.close ();
    }
  }

  private static void showPerformanceTuningVariables ()
  {
    printStream__.println ("*** Running interbase.interclient.PerformanceTests.run() ***");
    printStream__.println ("driversToTest:");
    for (int i=0; i<driversToTest.length; i++) {
      printStream__.print (driversToTest[i][0]);
      printStream__.print (", ");
      printStream__.print (driversToTest[i][1]);
      printStream__.print (", ");
      printStream__.print (driversToTest[i][2]);
      printStream__.print (", ");
      printStream__.print (driversToTest[i][3]);
      printStream__.print (", ");
      printStream__.print (driversToTest[i][4]);
      printStream__.println ();
    }

    printStream__.println ("testMetaData: " + testMetaData);
    printStream__.println ("testStrings: " + testStrings);
    printStream__.println ("testBLObs: " + testBLObs);
    printStream__.println ("testNumbers: " + testNumbers);
    printStream__.println ("testDates: " + testDates);
    printStream__.println ("testProcedures: " + testProcedures);

    printStream__.println ("test1ByteBlobs: " + test1ByteBlobs);
    printStream__.println ("test10ByteBlobs: " + test10ByteBlobs);
    printStream__.println ("test100ByteBlobs: " + test100ByteBlobs);
    printStream__.println ("test1000ByteBlobs: " + test1000ByteBlobs);
    printStream__.println ("test10000ByteBlobs: " + test10000ByteBlobs);
    printStream__.println ("test100000ByteBlobs: " + test100000ByteBlobs);

    printStream__.println ("test1CharStrings: " + test1CharStrings);
    printStream__.println ("test10CharStrings: " + test10CharStrings);
    printStream__.println ("test100CharStrings: " + test100CharStrings);
    printStream__.println ("test1000CharStrings: " + test1000CharStrings);
    printStream__.println ("test10000CharStrings: " + test10000CharStrings);

    printStream__.println ("testDataSize: " + testDataSize);

    printStream__.println ("enableAutoCommit: " + enableAutoCommit);
    printStream__.println ("enableDriverManagerLogStream: " + enableDriverManagerLogStream);

    printStream__.println ("characterSet: " + characterSet);
    printStream__.println ("character: " + character);
  }

  private static void populateStaticData ()
  {
    int i;
    
    // *** Populate java strings for inserting into database CHAR fields ***
    // !!! had to lower this to please JBuilder, up to 10,000 later.
    StringBuffer tempBuffer = new StringBuffer (10000); //!!!1,000 when debugging
    for (i=0; i<10000; i++) // !!!10,000 or  1,000 when debugging
      tempBuffer.append (character);
    // !!! comment out next two when debugging
    s10000chars__ = tempBuffer.toString ();
    tempBuffer.setLength (1000);
    s1000chars__ = tempBuffer.toString ();
    tempBuffer.setLength (100);
    s100chars__ = tempBuffer.toString ();
    tempBuffer.setLength (10);
    s10chars__ = tempBuffer.toString ();
    tempBuffer.setLength (1);
    s1char__ = tempBuffer.toString ();

    // !!! comment out next two when debugging
    b100000bytes__ = new byte [100000];
    b10000bytes__ = new byte [10000];
    b1000bytes__ = new byte [1000];
    b100bytes__ = new byte [100];
    b10bytes__ = new byte [10];
    b1byte__ = new byte [1];
    for (i=0; i<100000; i++) // !!! up to 100,000, 1,000 when debugging
      b100000bytes__[i] = 56; // !!! up to 100,000, 1,000 when debugging
    // !!!comment out next two when debugging
    System.arraycopy (b100000bytes__, 0, b10000bytes__, 0, 10000);
    System.arraycopy (b10000bytes__, 0, b1000bytes__, 0, 1000);
    System.arraycopy (b1000bytes__, 0, b100bytes__, 0, 100);
    System.arraycopy (b100bytes__, 0, b10bytes__, 0, 10);
    System.arraycopy (b10bytes__, 0, b1byte__, 0, 1);
  }

  // url__ and engine__ must be previously set.
  private static void test () throws java.sql.SQLException
  {
    try {
      // *** Get a connection to the url ***
      d__ = java.sql.DriverManager.getDriver (url__);
      printStream__.println ("*** using driver version: " + d__.getMajorVersion() + "." + d__.getMinorVersion() + " ***");
      
      properties__ = new java.util.Properties ();
      properties__.put ("user", user__);
      properties__.put ("password", password__);
      if (characterSet != null)
        properties__.put ("charset", characterSet); // need to resource "charSet"
      c__ = d__.connect (url__, properties__);

      // *** Create the metadata needed ***
      c__.setAutoCommit (false);  // Run DDL with autocommit off
      dropTestTables ();
      createTestTables ();
      c__.commit (); // Commit the DDL
      c__.setAutoCommit (enableAutoCommit);

      if (testMetaData)
        testMetaData ();

      if (testStrings) {
        printStream__.println ("** Beginning string tests **");
	testStringInsertPerformance ();
	testStringSelectPerformance ();
      }
      if (testBLObs) {
        printStream__.println ("** Beginning BLOb tests **");
	testBLObInsertPerformance ();
	testBLObSelectPerformance ();
      }
      if (testNumbers) {
        printStream__.println ("** Beginning number tests **");
	testNumberInsertPerformance ();
	testNumberSelectPerformance ();
      }
      if (testDates) {
        printStream__.println ("** Beginning date tests **");
	testDateInsertPerformance ();
	testDateSelectPerformance ();
      }
      if (testProcedures) {
        printStream__.println ("** Beginning procedure tests **");
	testProcedureInsertPerformance ();
	testProcedureSelectPerformance ();
      }
    }
    catch (java.sql.SQLException e) {
      showException (e);
      throw e;
    }
    finally {
      // The jdbc/odbc bridge needs the commit before close can be called
      if ((c__ != null) && !c__.isClosed()) {
        c__.commit();
        c__.close ();
      }
      c__ = null;
    }
  }

  private static void showException (java.sql.SQLException e)
  {
    java.sql.SQLException next = e;
    while (next != null) {
      printStream__.println ("SQL State: " + next.getSQLState () + "");
      printStream__.println ("Error Code: " + next.getErrorCode () + "");
      printStream__.println ("Message: " + next.getMessage () + "");
      next = next.getNextException ();
    }
  }

  private static void dropTestTables () throws java.sql.SQLException
  {
    s__ = c__.createStatement();
    try { s__.executeUpdate ("drop table CHAR1_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table CHAR10_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table CHAR100_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table CHAR1000_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table CHAR10000_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB1_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB10_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB100_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB1000_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB10000_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table BLOB100000_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table NUMBER_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table DATE_TABLE"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop procedure INSERT_PROC"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop procedure SELECT_PROC"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop procedure EXECUTE_PROC"); } catch (java.sql.SQLException e) {}
    try { s__.executeUpdate ("drop table PROC_CHAR100_TABLE"); } catch (java.sql.SQLException e) {}
    s__.close();
    c__.commit (); // Some drivers don't force commit on DDL
  }

  private static void createTestTables () throws java.sql.SQLException
  {
    s__ = c__.createStatement();
    // Could provide SQL in a resource bundle for each engine__
    String charset = (engine__.equals ("interbase") && (characterSet != null))
                     ? (" character set " +
                        interbase.interclient.CharacterEncodings.getInterBaseCharacterSetName (characterSet))
                     : "";
    if (testStrings) {
      if (test1CharStrings)
	s__.executeUpdate ("create table CHAR1_TABLE (S char(1)" + charset + ")");
      if (test10CharStrings)
	s__.executeUpdate ("create table CHAR10_TABLE (S char(10)" + charset + ")");
      if (test100CharStrings)
	s__.executeUpdate ("create table CHAR100_TABLE (S char(100)" + charset + ")");
      if (test1000CharStrings)
	s__.executeUpdate ("create table CHAR1000_TABLE (S char(1000)" + charset + ")");
      if (test10000CharStrings)
	s__.executeUpdate ("create table CHAR10000_TABLE (S char(10000)" + charset + ")");
    }
    if (testBLObs) {
      String longVarBinary;
      if (engine__.equals ("solid"))
	longVarBinary = "long varbinary";
      else if (engine__.equals ("sqlanywhere"))
        longVarBinary = "Image";
      else
	longVarBinary = "blob";

      if (test1ByteBlobs)
	s__.executeUpdate ("create table BLOB1_TABLE (B " + longVarBinary + ")");
      if (test10ByteBlobs)
	s__.executeUpdate ("create table BLOB10_TABLE (B " + longVarBinary + ")");
      if (test100ByteBlobs)
	s__.executeUpdate ("create table BLOB100_TABLE (B " + longVarBinary + ")");
      if (test1000ByteBlobs)
	s__.executeUpdate ("create table BLOB1000_TABLE (B " + longVarBinary + ")");
      if (test10000ByteBlobs)
	s__.executeUpdate ("create table BLOB10000_TABLE (B " + longVarBinary + ")");
      if (test100000ByteBlobs)
	s__.executeUpdate ("create table BLOB100000_TABLE (B " + longVarBinary + ")");
    }
    if (testNumbers) {
      numberBytesPerRecord__ = 32; // number of bytes per row in NUMBER_TABLE
      s__.executeUpdate ("create table NUMBER_TABLE (aSmallint        SMALLINT," +
			 "                           anInteger        INTEGER," +
			 "                           aDouble          DOUBLE PRECISION," +
			 "                           aFloat           FLOAT," +
			 "                           aNumericSmallint NUMERIC(4,2)," + // SMALLINT
			 "                           aNumericInteger  NUMERIC(9,3)," + // INTEGER
			 "                           aNumericDouble   NUMERIC(10,4))");// DOUBLE
    }
    if (testDates) {
      s__.executeUpdate ("create table DATE_TABLE (aDate      DATE," +
			 "                         aTime      DATE," +
			 "                         aTimestamp DATE)");
    }
    if (testProcedures) {
      s__.executeUpdate ("create table PROC_CHAR100_TABLE (S char(100)" + charset + ")");
      s__.executeUpdate ("create procedure INSERT_PROC" +
                         " as begin insert into PROC_CHAR100_TABLE (S) values ('" +
                         s100chars__ + "'); end");
      s__.executeUpdate ("create procedure SELECT_PROC" +
                         " returns (RETURN_VALUE char(100)" + charset + ")" +
                         " as begin for select S from PROC_CHAR100_TABLE into :RETURN_VALUE" +
                         " do suspend; end");
      s__.executeUpdate ("create procedure EXECUTE_PROC" +
                         " returns (RETURN_VALUE char(100)" + charset + ")" +
                         " as begin RETURN_VALUE = USER; end");
    }
    s__.close();
    c__.commit (); // Some drivers don't force commit on DDL
  }

  private static void testMetaData () throws java.sql.SQLException
  {
    java.util.Vector tableVector = new java.util.Vector ();
    java.util.Enumeration tableEnum;
    String table;

    startTime__ = System.currentTimeMillis ();
    java.sql.DatabaseMetaData dbmd = c__.getMetaData ();

    // First collect a vector of table names
    java.sql.ResultSet tempRS =
      dbmd.getTables ("", "", "%", new String[] {"TABLE", "SYSTEM TABLE", "VIEW"});
    while (tempRS.next ()) {
      tableVector.addElement (tempRS.getString ("TABLE_NAME"));
    }

    // Tables
    printStream__.println ("*** Tables ***");
    showResultSet (dbmd.getTables ("", "", "%", new String[] {"TABLE", "SYSTEM TABLE", "VIEW"}));

    // Table Privileges
    printStream__.println ("*** Table Privileges ***");
    showResultSet (dbmd.getTablePrivileges ("", "", "%"));

    // Columns
    printStream__.println ("*** Columns ***");
    showResultSet (dbmd.getColumns ("", "", "%", "%"));

    // Column Privileges
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Column Privileges for table " + table + " ***");
      showResultSet (dbmd.getColumnPrivileges ("", "", table, "%"));
    }

    // Best Row Identifier
    int scope = java.sql.DatabaseMetaData.bestRowSession;
    boolean nullable = false;
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Best Row Identifier for table " + table + " ***");
      showResultSet (dbmd.getBestRowIdentifier ("", "", table, scope, nullable));
    }

    // Version Columns
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Version Columns for table " + table + " ***");
      showResultSet (dbmd.getVersionColumns ("", "", table));
    }

    // Primary Keys
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Primary Keys for table " + table + " ***");
      showResultSet (dbmd.getPrimaryKeys ("", "", table));
    }

    // Imported Keys
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Imported Keys for table " + table + " ***");
      showResultSet (dbmd.getImportedKeys ("", "", table));
    }

    // Exported Keys
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Exported Keys for table " + table + " ***");
      showResultSet (dbmd.getExportedKeys ("", "", table));
    }

    // Cross Reference
    java.util.Enumeration primaryTableEnum = tableVector.elements ();
    java.util.Enumeration foreignTableEnum = tableVector.elements ();
    while (primaryTableEnum.hasMoreElements ()) {
      String primaryTable = (String) primaryTableEnum.nextElement ();
      while (foreignTableEnum.hasMoreElements ()) {
        String foreignTable = (String) foreignTableEnum.nextElement ();
        printStream__.println ("*** Cross Reference for primary table " + primaryTable + " and foreign table " + foreignTable + " ***");
        showResultSet (dbmd.getCrossReference ("", "", primaryTable, "", "", foreignTable));
      }
    }

    // Procedures
    printStream__.println ("*** Procedures ***");
    showResultSet (dbmd.getProcedures ("", "", "%"));

    // Procedure Columns
    printStream__.println ("*** Procedure Columns ***");
    showResultSet (dbmd.getProcedureColumns ("", "", "%", "%"));

    // Table Types
    printStream__.println ("*** Table Types ***");
    showResultSet (dbmd.getTableTypes ());

    // Type Info
    printStream__.println ("*** Type Info ***");
    showResultSet (dbmd.getTypeInfo ());

    // Index Info
    boolean unique = false;
    boolean approximate = true;
    tableEnum = tableVector.elements ();
    while (tableEnum.hasMoreElements ()) {
      table = (String) tableEnum.nextElement ();
      printStream__.println ("*** Index Info for table " + table + " ***");
      showResultSet (dbmd.getIndexInfo ("", "", table, unique, approximate));
    }

    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("database metadata extracted in: " + elapsedTime__ + " milliseconds");
  }

  private static void showResultSet (java.sql.ResultSet resultSet) throws java.sql.SQLException
  {
    java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
    int[] displaySizes = new int[metaData.getColumnCount ()];
    for (int col=1; col <= metaData.getColumnCount (); col++) {
      displaySizes[col-1] = Math.max (metaData.getColumnName (col).length(),
                                      metaData.getColumnDisplaySize (col)) + 1;
    }

    for (int col=1; col <= metaData.getColumnCount (); col++) {
      writeColumnData (metaData.getColumnName (col), col, displaySizes);
    }
    printStream__.println ();
    for (int col=1; col <= metaData.getColumnCount (); col++) {
      writeColumnData (metaData.getTableName (col), col, displaySizes);
    }
    printStream__.println ();
    for (int col=1; col <= metaData.getColumnCount (); col++) {
      writeColumnData (metaData.getColumnLabel (col), col, displaySizes);
    }
    printStream__.println ();
    for (int col=1; col <= metaData.getColumnCount (); col++) {
      writeColumnData (metaData.getColumnTypeName (col), col, displaySizes);
    }
    printStream__.println ();
    for (int col=1; col <= metaData.getColumnCount (); col++) {
      writeColumnData (String.valueOf (metaData.isWritable (col)), col, displaySizes);
    }
    printStream__.println ();
    while (resultSet.next ()) {
      for (int col=1; col <= metaData.getColumnCount (); col++) {
	writeColumnData (resultSet.getString (col), col, displaySizes);
      }
      printStream__.println ();
    }
    printStream__.println ();
    resultSet.close ();
  }

  private static void writeColumnData (String string,
                                       int column,
                                       int[] displaySizes)
  {
     printStream__.print (string);

     int strLength;
     if (string == null)
       strLength = 4;
     else
       strLength = string.length ();

     for (int i=strLength; i < displaySizes[column-1]; i++)
       printStream__.print (' ');
  }

  private static void testStringInsertPerformance () throws java.sql.SQLException
  {
    if (test1CharStrings)
      try { insertStrings (s1char__); } catch (java.sql.SQLException e) { showException (e); }

    if (test10CharStrings)
      try { insertStrings (s10chars__); } catch (java.sql.SQLException e) { showException (e); }

    if (test100CharStrings)
      try { insertStrings (s100chars__); } catch (java.sql.SQLException e) { showException (e); }

    if (test1000CharStrings)
      try { insertStrings (s1000chars__); } catch (java.sql.SQLException e) { showException (e); }

    if (test10000CharStrings)
      try { insertStrings (s10000chars__); } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void insertStrings (String str) throws java.sql.SQLException
  {
    int numRows = Math.max (1, testDataSize/str.length ());
    int numChars = 0;

    startTime__ = System.currentTimeMillis ();
    ps__ = c__.prepareStatement ("insert into CHAR" + str.length() + "_TABLE (S) values (?)"); // Network
    for (int row = 0; row < numRows; row++) {
      numChars += str.length();
      ps__.setString (1, str);
      ps__.executeUpdate (); // Network
    }
    ps__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("inserted " + numRows + " rows of " + str.length() + " char strings in: " +
		         elapsedTime__ + " milliseconds (" + numChars + " chars inserted)");
  }

  private static void testStringSelectPerformance () throws java.sql.SQLException
  {
    if (test1CharStrings)
      try { selectStrings (s1char__.length()); } catch (java.sql.SQLException e) { showException (e); }

    if (test10CharStrings)
      try { selectStrings (s10chars__.length ()); } catch (java.sql.SQLException e) { showException (e); }

    if (test100CharStrings)
      try { selectStrings (s100chars__.length ()); } catch (java.sql.SQLException e) { showException (e); }

    if (test1000CharStrings)
      try { selectStrings (s1000chars__.length ()); } catch (java.sql.SQLException e) { showException (e); }

    if (test10000CharStrings)
      try { selectStrings (s10000chars__.length ()); } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void selectStrings (int length) throws java.sql.SQLException
  {
    int numRows = 0;
    int numChars = 0;
    String temp;

    startTime__ = System.currentTimeMillis ();
    s__ = c__.createStatement ();
    rs__ = s__.executeQuery ("select S from CHAR" + length + "_TABLE"); // Network
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      temp = rs__.getString(1);
      numRows++;
      numChars += temp.length();
    }
    s__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("selected " + numRows + " rows of " + length + " char strings in: " +
		         elapsedTime__ + " milliseconds (" + numChars  + " chars selected)");
  }

  private static void testBLObInsertPerformance () throws java.sql.SQLException
  {
    if (test1ByteBlobs)
      try { insertBLObs (b1byte__); } catch (java.sql.SQLException e) { showException (e); }

    if (test10ByteBlobs)
      try { insertBLObs (b10bytes__);  } catch (java.sql.SQLException e) { showException (e); }

    if (test100ByteBlobs)
      try { insertBLObs (b100bytes__);  } catch (java.sql.SQLException e) { showException (e); }

    if (test1000ByteBlobs)
      try { insertBLObs (b1000bytes__); } catch (java.sql.SQLException e) { showException (e); }

    if (test10000ByteBlobs)
      try { insertBLObs (b10000bytes__);  } catch (java.sql.SQLException e) { showException (e); }

    if (test100000ByteBlobs)
      try { insertBLObs (b100000bytes__);  } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void insertBLObs (byte[] bytes) throws java.sql.SQLException
  {
    java.io.ByteArrayInputStream stream = null;
    int numRows = Math.max (1, testDataSize/bytes.length);
    int numBytes = 0;

    startTime__ = System.currentTimeMillis ();
    ps__ = c__.prepareStatement ("insert into BLOB" + bytes.length + "_TABLE (B) values (?)"); // Network
    for (int row = 0; row < numRows; row++) {
      numBytes += bytes.length;
      stream = new java.io.ByteArrayInputStream (bytes);
      ps__.setBinaryStream (1, stream, bytes.length);
      ps__.executeUpdate (); // Network
    }
    ps__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("inserted " + numRows + " rows of " + bytes.length + " byte blobs in: " +
		         elapsedTime__ + " milliseconds (" + numBytes + " bytes inserted)");
  }

  private static void testBLObSelectPerformance () throws java.sql.SQLException
  {
    if (test1ByteBlobs)
      try { selectBLObs (b1byte__.length);  } catch (java.sql.SQLException e) { showException (e); }

    if (test10ByteBlobs)
      try { selectBLObs (b10bytes__.length); } catch (java.sql.SQLException e) { showException (e); }

    if (test100ByteBlobs)
      try { selectBLObs (b100bytes__.length);  } catch (java.sql.SQLException e) { showException (e); }

    if (test1000ByteBlobs)
      try { selectBLObs (b1000bytes__.length); } catch (java.sql.SQLException e) { showException (e); }

    if (test10000ByteBlobs)
      try { selectBLObs (b10000bytes__.length); } catch (java.sql.SQLException e) { showException (e); }

    if (test100000ByteBlobs)
      try { selectBLObs (b100000bytes__.length); } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void selectBLObs (int length) throws java.sql.SQLException
  {
    java.io.InputStream stream;
    byte[] readBuffer = new byte[testDataSize]; // More than adequate
    int numRows = 0;
    int bytesRead;
    int numBytes = 0;

    try {
      startTime__ = System.currentTimeMillis ();
      s__ = c__.createStatement ();
      rs__ = s__.executeQuery ("select B from BLOB" + length + "_TABLE"); // Network
      while (rs__.next ()) {
	stream = rs__.getBinaryStream (1);
        numRows++;
        while ((bytesRead = stream.read (readBuffer)) != -1) { // Network
          numBytes += bytesRead;
        }
      }
      s__.close ();
      endTime__ = System.currentTimeMillis ();
      elapsedTime__ = endTime__ - startTime__;
      printStream__.println ("selected " + numRows + " rows of " + length + " byte blobs in: " +
		     elapsedTime__ + " milliseconds (" + numBytes + " bytes selected)");
    }
    catch (java.io.IOException e) {
      throw new java.sql.SQLException ("IOException: " + e.getMessage ());
    }
  }

  private static void testNumberInsertPerformance () throws java.sql.SQLException
  {
    try { insertNumbers (); } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void insertNumbers () throws java.sql.SQLException
  {
    int numRows = Math.max (1, testDataSize/numberBytesPerRecord__);

    startTime__ = System.currentTimeMillis ();
    ps__ = c__.prepareStatement ("insert into NUMBER_TABLE (aSmallint," +
				 "                          anInteger," +
				 "                          aDouble," +
				 "                          aFloat," +
				 "                          aNumericSmallint," +
				 "                          aNumericInteger," +
				 "                          aNumericDouble)" +
				 " values (?, ?, ?, ?, ?, ?, ?)"); // Network
    for (int row = 0; row < numRows; row++) {
      ps__.setShort (1, (short) 1);
      ps__.setInt (2, 2);
      ps__.setDouble (3, (double) 3);
      ps__.setFloat (4, (float) 4);
      ps__.setInt (5, 5);
      ps__.setInt (6, 6);
      ps__.setDouble (7, (double) 7);
      ps__.executeUpdate (); // Network
    }
    ps__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("inserted " + numRows + " rows of " + numberBytesPerRecord__ + " byte number records in: " +
		   elapsedTime__ + " milliseconds (" + numRows*numberBytesPerRecord__ + " bytes inserted)");
  }

  private static void testNumberSelectPerformance () throws java.sql.SQLException
  {
    try { selectNumbers ();  } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void selectNumbers () throws java.sql.SQLException
  {
    int numRows = 0;

    s__ = c__.createStatement ();
    startTime__ = System.currentTimeMillis ();
    rs__ = s__.executeQuery ("select aSmallint," +
			     "       anInteger," +
			     "       aDouble," +
			     "       aFloat," +
			     "       aNumericSmallint," +
			     "       aNumericInteger," +
			     "       aNumericDouble" +
			     " from NUMBER_TABLE"); // Network
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
      rs__.getString (2);
      rs__.getString (3);
      rs__.getString (4);
      rs__.getString (5);
      rs__.getString (6);
      rs__.getString (7);
    }
    s__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("selected " + numRows + " rows of " + numberBytesPerRecord__ + " byte number records in: " +
		   elapsedTime__ + " milliseconds (" + numRows*numberBytesPerRecord__  + " bytes selected)");
  }

  private static void testDateInsertPerformance () throws java.sql.SQLException
  {
    try { insertDates ();  } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void insertDates () throws java.sql.SQLException
  {
    int numRows = Math.max (1, testDataSize/(3*8)); // 3 64-bit fields

    startTime__ = System.currentTimeMillis ();
    ps__ = c__.prepareStatement ("insert into DATE_TABLE (aDate," +
				 "                        aTime," +
				 "                        aTimestamp)" +
				 " values (?, ?, ?)"); // Network
    for (int row = 0; row < numRows; row++) {
      ps__.setDate (1, new java.sql.Date (98, 1, 3));
      ps__.setTime (2, new java.sql.Time (0, 0, 0));
      ps__.setTimestamp (3, new java.sql.Timestamp (98, 1, 3, 0, 0, 0, 0));
      ps__.executeUpdate (); // Network
    }
    ps__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("inserted " + numRows + " rows of 3 field (date, time, timestamp) records in: " +
		         elapsedTime__ + " milliseconds (" + numRows*3*8 + " bytes inserted)");
  }

  private static void testDateSelectPerformance () throws java.sql.SQLException
  {
    try { selectDates (); } catch (java.sql.SQLException e) { showException (e); }
  }

  private static void selectDates () throws java.sql.SQLException
  {
    int numRows = 0;

    s__ = c__.createStatement ();
    startTime__ = System.currentTimeMillis ();
    rs__ = s__.executeQuery ("select aDate," +
			     "       aTime," +
			     "       aTimestamp" +
			     " from DATE_TABLE"); // Network

    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
      rs__.getString (2);
      rs__.getString (3);
    }
    s__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("selected " + numRows + " rows of 3 field (date, time, timestamp) records in: " +
		         elapsedTime__ + " milliseconds (" + numRows*3*8 + " bytes selected)");
  }

  private static void testProcedureInsertPerformance () throws java.sql.SQLException
  {
    startTime__ = System.currentTimeMillis ();
    s__ = c__.createStatement ();
    s__.executeUpdate ("execute procedure INSERT_PROC"); // Network
    s__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("executed procedure INSERT_PROC in: " +
		         elapsedTime__ + " milliseconds");
  }

  private static void testProcedureSelectPerformance () throws java.sql.SQLException
  {
    int numRows = 0;

    startTime__ = System.currentTimeMillis ();
    s__ = c__.createStatement ();
    s__.execute ("execute procedure SELECT_PROC"); // Network
    rs__ = s__.getResultSet ();
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
    }

    rs__ = s__.executeQuery ("execute procedure EXECUTE_PROC"); // Network
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
    }

    rs__ = s__.executeQuery ("select * from SELECT_PROC"); // Network
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
    }

    rs__ = s__.executeQuery ("select * from EXECUTE_PROC"); // Network
    while (rs__.next ()) { // Networks only when local row cache is exhausted
      numRows++;
      rs__.getString (1);
    }

    s__.close ();
    endTime__ = System.currentTimeMillis ();
    elapsedTime__ = endTime__ - startTime__;
    printStream__.println ("executed various procedures in: " +
		         elapsedTime__ + " milliseconds");
  }
}
