package test;

import java.sql.*;

public class ReadOnly {

static public void RunReadOnlyTest() {

  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    props.put("sqlDialect", "1");
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3ro.gdb";
    Connection conn = DriverManager.getConnection(url, props);
    interbase.interclient.DatabaseMetaData dbmd =
       (interbase.interclient.DatabaseMetaData)
       ((interbase.interclient.Connection)conn).getMetaData();
    System.out.println(" Database read-only? : " + dbmd.isReadOnly() );
    System.out.println(" SQL Dialect " +  dbmd.getDatabaseSQLDialect() );
//    ResultSet rs = dbmd.getTypeInfo();
    PreparedStatement pstmt = conn.prepareStatement(
         "Select * from RDB$RELATIONS where RDB$RELATION_NAME like 'RDB$%'");
    ResultSet rs = pstmt.executeQuery();

    while (rs.next()) {
        System.out.println("RDB$RELATION_NAME: " + rs.getString("RDB$RELATION_NAME") );
    }
    pstmt.close();
    try {
      pstmt = conn.prepareStatement("insert into alltypes3 (i) values (1)");
      pstmt.executeUpdate();
    }
    catch (java.sql.SQLException eSQL) {
      System.out.println("--- error occured when executing insert statement");
      System.out.println("Message:  " + eSQL.getMessage());
      System.out.println("SQLState: " + eSQL.getSQLState());
      System.out.println("E-Code:   " + eSQL.getErrorCode());
      System.out.println("-----");
    }
    try {
      pstmt = conn.prepareStatement("update alltypes3  set i = 333 where 0=1");
      pstmt.executeUpdate();
      System.out.println("Bogus Update accepted");
    }
    catch (java.sql.SQLException eSQL) {
      System.out.println("--- error occured when executing bogus update statement");
      System.out.println("Message:  " + eSQL.getMessage());
      System.out.println("SQLState: " + eSQL.getSQLState());
      System.out.println("E-Code:   " + eSQL.getErrorCode());
      System.out.println("-----");
    }
    try {
      pstmt = conn.prepareStatement("update alltypes3 set i = 333");
      pstmt.executeUpdate();
      System.out.println("Real Update accepted");
    }
    catch (java.sql.SQLException eSQL) {
      System.out.println("--- error occured when executing real update statement");
      System.out.println("Message:  " + eSQL.getMessage());
      System.out.println("SQLState: " + eSQL.getSQLState());
      System.out.println("E-Code:   " + eSQL.getErrorCode());
      System.out.println("-----");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
}

}