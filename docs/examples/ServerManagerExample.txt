// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)
//
// An example of using a JDBC 2 Standard Extension DataSource
// for obtaining ServerManager connections.

public final class ServerManagerExample
{
  static public void main (String args[])
  {
    // Create an InterClient data source bean manually;
    // beans are normally manipulated by a GUI tool.
    // Bean properties are always set using the setXXX signature.
    interbase.interclient.DataSource dataSource = new interbase.interclient.DataSource ();
    
    // Set the standard properties
    dataSource.setServerName ("perdy");
    dataSource.setDataSourceName ("Employee");
    dataSource.setDescription ("An example database of employees");
    dataSource.setPortNumber (3060);
    dataSource.setNetworkProtocol ("jdbc:interbase:");
    dataSource.setRoleName (null);
    
    // Set the non-standard properties
    dataSource.setServerManagerHost ("perdy");

    // Obtain a ServerManager for the DataSource's Server Manager Host
    try {
      dataSource.setLoginTimeout (10);
      interbase.interclient.ServerManager service = dataSource.getServerManager ("sysdba", "masterkey");
      System.out.println ("got server manager");
      service.close ();
    }
    catch (java.sql.SQLException e) {
      System.out.println ("sql exception: " + e.getMessage ());
    }
  }
}
			  
