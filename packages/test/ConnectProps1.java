package test;

public class ConnectProps1 {

  public ConnectProps1() {
  }

  public static void main(String[] args) {
  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3test.gdb";
    String [] testDialects = {"1", "3", "-1", "5", "foo", "0", "2" };

    StringBuffer sb = new StringBuffer( testDialects[0] );
    for (int i = 1; i < testDialects.length; i++ ) {
      sb.append( ", ");
      sb.append(testDialects[i] );
    }
    System.out.println(" TESTING URL: " + url );
    System.out.println(" with these values: " + sb );

    for (int i = 0; i < testDialects.length; i++ ) {
      try {
        props.put ( "sqlDialect", testDialects[i] );
        System.out.println("Test for Dialect: " + testDialects[i] );
        java.sql.Connection conn = java.sql.DriverManager.getConnection(url, props);
        java.sql.SQLWarning warnings = conn.getWarnings();
        while (warnings !=null) {
          System.out.println( "Connection Warning --- " );
          System.out.println( " Message:   " + warnings.getMessage() );
          System.out.println( " SQLCode:   " + warnings.getSQLState() );
          System.out.println( " ErrorCode: " + warnings.getErrorCode() );
          System.out.println( " ---  " );
          warnings = warnings.getNextWarning();
        }
        try {
        java.sql.PreparedStatement d1PStmt1 =
           conn.prepareStatement("Select * from ALLTYPES1");
        System.out.println(" Dialect 1 statement succeeds ");
        }
        catch (java.sql.SQLException ohwell) {
          System.out.println( "Dialect 1 statement fails");
        }
        try {
        java.sql.PreparedStatement d1PStmt3 =
           conn.prepareStatement("Select * from \"ALLTYPES3\"");
        System.out.println(" Dialect 3 statement succeeds ");
        }
        catch (java.sql.SQLException eSQL) {
          System.out.println( "===== Dialect 3 statement fails ===== ");
          System.out.println( eSQL.getMessage() );
          System.out.println( "===================================== ");
        }
      }
      catch (java.sql.SQLException eSQL) {
        while (eSQL !=null) {
          System.out.println( "Exception thrown in Connect--- " );
          System.out.println( " Message:   " + eSQL.getMessage() );
          System.out.println( " SQLCode:   " + eSQL.getSQLState() );
          System.out.println( " ErrorCode: " + eSQL.getErrorCode() );
          System.out.println( " ---  " );
          eSQL = eSQL.getNextException();
        }
      }
      System.out.println( " ************** " );
    }  // for
    }
  catch (Exception e) {
    e.printStackTrace();
  }
}
  private boolean invokedStandalone = false;
}