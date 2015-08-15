import java.sql.*;

public class Catalog1 {

public static void main (String args[]) {
  runGetColumnTest();
}

public static void runGetColumnTest() {
  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    props.put("sqlDialect", "3");
//    String url = "jdbc:interbase://venture/linux10:/usr/interbase/isc4.gdb";
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3test.gdb";
//    String url = "jdbc:interbase://venture/d:/interbase/examples/database/types.gdb";
    Connection conn = DriverManager.getConnection(url, props);
    interbase.interclient.DatabaseMetaData dbmd =
       (interbase.interclient.DatabaseMetaData)
       ((interbase.interclient.Connection)conn).getMetaData();
    System.out.println(" Database read-only? : " + dbmd.isReadOnly() );
    System.out.println(" SQL Dialect " +  dbmd.getDatabaseSQLDialect() );
    ResultSet rs = dbmd.getColumns(null, null, "alltypes3", "%");
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnName(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
}

} 