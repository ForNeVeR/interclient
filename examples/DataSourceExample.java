// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)
//
// An example of using a JDBC 2 Standard Extension DataSource.
// The DataSource facility provides an alternative to the JDBC DriverManager,
// essentially duplicating all of the driver manager’s useful functionality.
// Although, both mechanisms may be used by the same application if desired,
// JavaSoft encourages developers to regard the DriverManager as a legacy
// feature of the JDBC API.
// Applications should use the DataSource API whenever possible.
// A JDBC implementation that is accessed via the DataSource API is not
// automatically registered with the DriverManager.
// The DriverManager, Driver, and DriverPropertyInfo interfaces
// may be deprecated in the future.

public final class DataSourceExample
{
  static public void main (String args[])
  {
    // Create an InterClient data source bean manually;
    // beans are normally manipulated by a GUI tool.
    // Bean properties are always set using the setXXX signature.
    interbase.interclient.DataSource dataSource = new interbase.interclient.DataSource ();

    // Set the standard properties
    dataSource.setServerName ("perdy");
    dataSource.setDatabaseName ("d:/databases/employee.gdb");
    dataSource.setDataSourceName ("Employee");
    dataSource.setDescription ("An example database of employees");
    dataSource.setPortNumber (3060);
    dataSource.setNetworkProtocol ("jdbc:interbase:");
    dataSource.setRoleName (null);
    
    // Set the non-standard properties
    dataSource.setCharSet (interbase.interclient.CharacterEncodings.NONE);
    dataSource.setSuggestedCachePages (0);
    dataSource.setSweepOnConnect (false);

    // Connect to the InterClient DataSource
    try {
      dataSource.setLoginTimeout (10);
      java.sql.Connection c = dataSource.getConnection ("sysdba", "masterkey");
      // At this point, there is no implicit driver instance
      // registered with the driver manager!
      System.out.println ("got connection");
      c.close ();
    }
    catch (java.sql.SQLException e) {
      System.out.println ("sql exception: " + e.getMessage ());
    }
  }
}
			  
