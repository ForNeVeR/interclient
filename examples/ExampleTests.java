// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)

/**
 * Example program using interbase.interclient.PerformanceTests class.
 * Copy and edit this program as desired.
 * See interbase.interclient.utils.PerformanceTests
 * for a description of configuration variables.
 *
 * @see PerformanceTests
 **/
public class ExampleTests
{
  // BDE aliases: localTest set to d:\databases\test.gdb
  //              clientServerTest set to hostname:d:\databases\test.gdb
  // ODBC aliases: localTest set to d:\databases\test.gdb
  //              clientServerTest set to hostname:d:\databases\test.gdb

  // You can set hostname to localhost to test client/server in loopback mode.

  /**
   * Configure PerformanceTests public variables and run some tests.
   * @throws java.sql.SQLException if a database access error occurs
   **/
  static public void main (String[] args) throws java.sql.SQLException
  {
    // This determines what drivers actually get tested.
    // Always point to the same dbFile when testing InterBase URLs
    PerformanceTests.driversToTest = new String[][] {
      // *** Client/server URLs ***
      // JDBC-ODBC bridge uses interbase protocol thru ODBC
       { "interbase",
         "sysdba",
         "masterkey",
         "jdbc:odbc:clientServerTest",
         "sun.jdbc.odbc.JdbcOdbcDriver" }
      // InterClient uses a JDBC protocol to ISC gateway
      ,{ "interbase",
         "sysdba",
         "masterkey",
         "jdbc:interbase://hostname/d:/databases/test.gdb",
         "interbase.interclient.Driver" }
      // Sybase jConnect uses TDS protocol to Open Server
      , { "sqlanywhere",
          "dba",
          "sql",
          "jdbc:sybase:Tds:hostname:4444/d:\\sqlany50\\sademo.db",
          "com.sybase.jdbc.SybDriver" }
      // Symantec dbAnywhere uses a JDBC protocol to ODBC gateway
      ,{ "sqlanywhere",
         "dba",
         "sql",
         "jdbc:dbaw://hostname:8889/Sybase_SQLANY/Sademo/Sademo",
         "symantec.itools.db.jdbc.Driver" }
      // Datagateway Broker uses a JDBC protocol to BDE gateway
      ,{ "interbase",
         "sysdba",
         "masterkey",
         "jdbc:BorlandBroker://hostname/localTest",
         "borland.jdbc.Broker.RemoteDriver" }
      // Datagateway Bridge uses interbase protocol thru BDE
      ,{ "interbase",
         "sysdba",
         "masterkey",
         "jdbc:BorlandBridge:clientServerTest",
         "borland.jdbc.Bridge.LocalDriver" }
      // Oracle Thin Driver uses Oracle protocol
      //, { "oracle",
      //    "user",
      //    "password",
      //    "jdbc:oracle:thin:@hostname:1521:ORCL",
      //    "oracle.jdbc.driver.OracleDriver" }
      // Solid Driver uses Solid protocol
      //,{ "solid",
      //   "sysdba",
      //   "masterkey",
      //   "jdbc:solid://hostname:1313/sysdba/masterkey",
      //   "solid.jdbc.SolidDriver" }
      //
      // *** Local URLs follow (not client/server) ***
      // Datagateway Bridge used locally thru BDE
      //, { "interbase",
      //    "sysdba",
      //    "masterkey",
      //    "jdbc:BorlandBridge:localTest",
      //    "borland.jdbc.Bridge.LocalDriver" }
      // JDBC-ODBC Bridge used locally thru ODBC
      //, { "interbase",
      //    "sysdba",
      //    "masterkey",
      //    "jdbc:odbc:localTest"
      //    "sun.jdbc.odbc.JdbcOdbcDriver" }
    };

    // *** All defaults are false
    PerformanceTests.testMetaData = false;
    PerformanceTests.testStrings = false;
    PerformanceTests.testBLObs = true;
    PerformanceTests.testNumbers = false;
    PerformanceTests.testDates = false;
    PerformanceTests.testProcedures = false;

    // *** All defaults are false
    PerformanceTests.test1ByteBlobs = false;  // excruciately slow
    PerformanceTests.test10ByteBlobs = false; // too slow
    PerformanceTests.test100ByteBlobs = false;  
    PerformanceTests.test1000ByteBlobs = false; 
    PerformanceTests.test10000ByteBlobs = true; 
    PerformanceTests.test100000ByteBlobs = false;

    // *** All defaults are false
    PerformanceTests.test1CharStrings = false; // too slow
    PerformanceTests.test10CharStrings = false; 
    PerformanceTests.test100CharStrings = false;
    PerformanceTests.test1000CharStrings = false;
    PerformanceTests.test10000CharStrings = false;

    // *** Default testDataSize is 1,000
    PerformanceTests.testDataSize = 100000;

    // *** All defaults are false
    PerformanceTests.enableAutoCommit = false;
    PerformanceTests.enableDriverManagerLogStream = false;

    PerformanceTests.characterSet = null;  // use driver defaults, ic uses ISO 8859-1, some use ASCII
    PerformanceTests.character = 'X'; 

    // ************************************************************************
    // *** Establish a print stream where performance test results are written.
    // ************************************************************************

    // Output file for test results.
    // If this is set to null then System.out is used.
    // A JBuilder Project or IDE setting controls whether or not System.out
    // goes to the Execution Log or to the DOS window.
    // Typical value on Windows is "d:\\Output.txt"
    PerformanceTests.outputFileName = "d:\\Output.txt";
    // PerformanceTests.outputFileName = null;

    // ************************* 
    // *** Ok, run the tests ***
    // *************************
    PerformanceTests.run ();
  }
}

