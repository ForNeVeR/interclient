// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)
//
// An example of using the Java Naming and Directory Interface (JNDI)
// for naming JDBC datasources.
// In a nutshell, a datasource represents a database and its connection
// properties, and is stored by a JNDI name server provider such as LDAP.
// Think of a datasource as a server-side registered database alias,
// in which the name server can manage any number of InterBase servers.
// Besides datasources, JNDI can name other objects as well.

public final class JNDIExample
{
  static public void main (String args[])
  {
    // Create an InterClient data source bean manually;
    // beans are normally manipulated by a GUI tool.
    // Bean properties are always set using the setXXX signature.
    javax.sql.DataSource dataSource = new interbase.interclient.DataSource ();

    // Set the standard properties
    ((interbase.interclient.DataSource) dataSource).setServerName ("perdy");
    ((interbase.interclient.DataSource) dataSource).setDatabaseName ("d:/databases/employee.gdb");
    ((interbase.interclient.DataSource) dataSource).setDataSourceName ("Employee");
    ((interbase.interclient.DataSource) dataSource).setDescription ("An example database of employees");
    ((interbase.interclient.DataSource) dataSource).setPortNumber (3060);
    ((interbase.interclient.DataSource) dataSource).setNetworkProtocol ("jdbc:interbase:");
    ((interbase.interclient.DataSource) dataSource).setRoleName (null);
    
    // Set the non-standard properties
    ((interbase.interclient.DataSource) dataSource).setServerManagerHost ("perdy");
    ((interbase.interclient.DataSource) dataSource).setCharSet (interbase.interclient.CharacterEncodings.NONE);
    ((interbase.interclient.DataSource) dataSource).setSuggestedCachePages (0);
    ((interbase.interclient.DataSource) dataSource).setSweepOnConnect (false);

    // Test our ability to manufacture a JNDI naming reference from the data source
    javax.naming.Reference ref = null;
    try {
      ref = ((javax.naming.Referenceable) dataSource).getReference ();
    }
    catch (javax.naming.NamingException e) {
      System.out.println ("naming exception: " + e.getMessage ());
    }

    // Now test our Object Factory's ability to regenerate the data source from the JNDI reference
    interbase.interclient.ObjectFactory factory = new interbase.interclient.ObjectFactory ();
    try {
      dataSource = (javax.sql.DataSource) factory.getObjectInstance (ref, null, null, null);
    }
    catch (java.lang.Exception e) {
      System.out.println ("factory exception: " + e.getMessage ());
    }

    // Set up an LDAP environment for LDAP server perdy, this is where references will be stored
    java.util.Hashtable ldapEnv = new java.util.Hashtable();
    ldapEnv.put (javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    ldapEnv.put (javax.naming.Context.PROVIDER_URL, "ldap://perdy:389");

    // Register an InterClient data source with an LDAP server
    try {
      javax.naming.Context context = new javax.naming.InitialContext (ldapEnv);
      System.out.println ("got context");
      context.bind ("jdbc/EmployeeDB", dataSource);
      System.out.println ("bound data source");
    }
    catch (javax.naming.NamingException e) {
      System.out.println ("naming exception: " + e.getMessage ());
    }

    // Lookup a registered InterClient data source from an LDAP server
    try {
      javax.naming.Context context = new javax.naming.InitialContext (ldapEnv);
      System.out.println ("got context");
      dataSource = (javax.sql.DataSource) context.lookup ("jdbc/EmployeeDB");
      System.out.println ("found data source");
    }
    catch (javax.naming.NamingException e) {
      System.out.println ("naming exception: " + e.getMessage ());
    }

    // Connect to the InterClient DataSource
    try {
      dataSource.setLoginTimeout (10);
      java.sql.Connection c = dataSource.getConnection ("sysdba", "masterkey");
      System.out.println ("got connection");
      c.close ();
    }
    catch (java.sql.SQLException e) {
      System.out.println ("sql exception: " + e.getMessage ());
    }
  }
}
			  
