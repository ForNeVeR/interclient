package test;

import java.sql.*;

public class NewTypes1 {

  public NewTypes1() {
  }

  public static void main(String[] args) {
    NewTypes1 newTypes1 = new NewTypes1();
    newTypes1.invokedStandalone = true;
    TestSetDates();
  }
  private boolean invokedStandalone = false;

  public static void TestGetDates() {
  // This tests the all getter methods for a date column.
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (dt, i ) values (?, ?)" );
    pstmt.setDate(1, new java.sql.Date(41, 11, 7));
    byte[] b = null;
    java.io.ByteArrayInputStream in = (java.io.ByteArrayInputStream) new java.io.ByteArrayInputStream(b);
    pstmt.setBinaryStream(1, in, in.available());
    pstmt.setInt(2, 12345678);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select i, dt from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      try {
        System.out.println("-------Getting value as String");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getString(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as DOUBLE");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDouble(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as BigDecimal");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBigDecimal(2, 5));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Integer");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getInt(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Short");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getShort(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Float");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getFloat(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Date");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDate(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Time");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTime(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as TimeStamp");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTimestamp(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Boolean");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBoolean(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Long");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getLong(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Byte");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getByte(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Object");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getObject(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
    System.out.println("--END TEST--");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }


  public static void TestGetTimes() {
  // This tests the all getter methods for a time column.
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (ti, i ) values (?, ?)" );
    pstmt.setTime(1, new java.sql.Time(12, 35, 15));
    pstmt.setInt(2, 12345678);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select i, ti from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      try {
        System.out.println("-------Getting value as String");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getString(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as DOUBLE");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDouble(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as BigDecimal");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBigDecimal(2, 5));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Integer");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getInt(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Short");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getShort(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Float");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getFloat(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Date");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDate(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Time");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTime(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as TimeStamp");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTimestamp(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Boolean");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBoolean(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Long");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getLong(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Byte");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getByte(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Object");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getObject(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
    System.out.println("--END TEST--");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }


  public static void TestGetInt64s() {
  // This tests the all viable setter methods for a int64 column.
  // The String getter is used to retrieve information
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (n64, i ) values (?, ?)" );
    pstmt.setBigDecimal(1, new java.math.BigDecimal("1234567890123.12345"));
    pstmt.setInt(2, 12345678);
//    pstmt.setString(2, "1234567890123.12345 BigDecimal");
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select i, n64 from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      try {
        System.out.println("-------Getting value as String");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getString(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as DOUBLE");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDouble(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as BigDecimal");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBigDecimal(2, 5));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Integer");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getInt(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Short");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getShort(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Float");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getFloat(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Date");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getDate(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Time");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTime(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as TimeStamp");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getTimestamp(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Boolean");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getBoolean(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Long");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getLong(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Byte");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getByte(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
      try {
        System.out.println("-------Getting value as Object");
        System.out.println(rsmd.getColumnLabel(2) + ": " + rs.getObject(2));
      }
      catch (SQLException eSQL) {
        System.out.println("Message:  " + eSQL.getMessage());
        System.out.println("SQLState: " + eSQL.getSQLState());
        System.out.println("E-Code:   " + eSQL.getErrorCode());
      }
    System.out.println("--END TEST--");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }
  public static void TestSetTimes() {
  // This tests the all viable setter methods for a time column.
  // The String getter is used to retrieve information
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (ti, ch30 ) values (?, ?)" );
    pstmt.setTime(1, new java.sql.Time(13,8,11));
    pstmt.setString(2, "13:08:11 Time");
    pstmt.executeUpdate();
    pstmt.setString(1, "13:08:11");
    pstmt.setString(2, "13:08:11 String");
    pstmt.executeUpdate();
    pstmt.setTime(1, new java.sql.Time((new Long("45296000")).longValue()));
    pstmt.setString(2, "45296000 Long");
    pstmt.executeUpdate();
    pstmt.setTimestamp(1, new java.sql.Timestamp(89, 9, 17, 17, 6, 15, 0));
    pstmt.setString(2, "10/17/89 5:06:15 pm Timestamp");
    pstmt.executeUpdate();
    try {
      pstmt.setDate(1, new java.sql.Date(27,8,11));
      pstmt.setString(2, "9/11/1927 Date");
      pstmt.executeUpdate();
    }
    catch (java.sql.SQLException eSQL) {
      System.out.println("--------Exception on setting Date");
      System.out.println("Message:  " + eSQL.getMessage());
      System.out.println("SQLState: " + eSQL.getSQLState());
      System.out.println("E-Code:   " + eSQL.getErrorCode());
    }
    pstmt = conn.prepareStatement(
       "select ch30, ti from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnLabel(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }

  public static void TestSetDates() {
  // This tests the all viable setter methods for a time column.
  // The String getter is used to retrieve information
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (dt, ch30 ) values (?, ?)" );
    pstmt.setDate(1, new java.sql.Date(27, 8, 11));
    pstmt.setString(2, "1927-9-11 DATE");
    pstmt.executeUpdate();
    pstmt.setString(1, "1927-09-11");
    pstmt.setString(2, "1927-09-11 String");
    pstmt.executeUpdate();
    pstmt.setDate(1, new java.sql.Date((new Long("0")).longValue()));
    pstmt.setString(2, "0 Long");
    pstmt.executeUpdate();
    pstmt.setTimestamp(1, new java.sql.Timestamp(89, 9, 17, 17, 6, 15, 0));
    pstmt.setString(2, "10/17/89 5:06:15 pm Timestamp");
    pstmt.executeUpdate();
    try {
      pstmt.setTime(1, new java.sql.Time(17,6,15));
      pstmt.setString(2, "5:06:15 pm TIME");
      pstmt.executeUpdate();
    }
    catch (java.sql.SQLException eSQL) {
      System.out.println("--------Exception on setting TIME");
      System.out.println("Message:  " + eSQL.getMessage());
      System.out.println("SQLState: " + eSQL.getSQLState());
      System.out.println("E-Code:   " + eSQL.getErrorCode());
    }
    pstmt = conn.prepareStatement(
       "select ch30, dt from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnLabel(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }

  public static void TestSetInt64s() {
  // This tests the all viable setter methods for a int64 column.
  // The String getter is used to retrieve information
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
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (n64, ch30 ) values (?, ?)" );
    pstmt.setBigDecimal(1, new java.math.BigDecimal("1234567890123.12345"));
    pstmt.setString(2, "1234567890123.12345 BigDecimal");
    pstmt.executeUpdate();
    pstmt.setString(1, "1234567890123.12345");
    pstmt.setString(2, "1234567890123.12345 String");
    pstmt.executeUpdate();
    pstmt.setLong(1, (new Long("1234567890123")).longValue());
    pstmt.setString(2, "1234567890123.00000 Long");
    pstmt.executeUpdate();
    pstmt.setInt(1, -123456789);
    pstmt.setString(2, "-123456789.00000 Int");
    pstmt.executeUpdate();
    pstmt.setShort(1, (short) -1234);
    pstmt.setString(2, "-1234.00000  Short");
    pstmt.executeUpdate();
    pstmt.setDouble(1, 1234567890123.975317);
    pstmt.setString(2, "1234567890123.975317 Double");
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select ch30, n64 from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnLabel(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }
  public static void TestColumnLabels() {
  // This is a customer test.  This feature has not been changed from 1.51
  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    props.put("sqlDialect", "3");
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3test.gdb";
    Connection conn = DriverManager.getConnection(url, props);
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (n64, ch30 ) values (?, ?)" );
    pstmt.setBigDecimal(1, new java.math.BigDecimal("1234567890123.12345"));
    pstmt.setString(2, "1234567890123.12345 BigDecimal");
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select ch30, n64 from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(
           rsmd.getColumnName(i) + " AS " +
           rsmd.getColumnLabel(i) + ": " + rs.getString(i)
           );
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }
  public static void TestCLITime() {
  // This tests support for the CLI time syntax:
  // {t '12:35:47'} is supported
  // {t '12:35:47.1234'} is supported
  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    props.put("sqlDialect", "3");
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3test.gdb";
    Connection conn = DriverManager.getConnection(url, props);
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    Statement stmt = conn.createStatement();
    stmt.executeUpdate("insert into alltypes3 (si, ti ) values (1, {t '12:30:47.1230'} )" );
    stmt.executeUpdate("insert into alltypes3 (si, ti ) values (1, {t '12:30:48'} )" );
    stmt.executeUpdate("insert into alltypes3 (si, ti ) values (1, '12:30:49' )" );
    stmt.executeUpdate("insert into alltypes3 (si, ti ) values (1, time '12:30:50.1230' )" );
    stmt.executeUpdate("insert into alltypes3 (si, ti ) values (1, time '12:30:51' )" );
    stmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (si, ti ) values (?, {t 12:30:47.1230} )" );
    pstmt.setShort(1, (short) 2);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select si, ti from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnLabel(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }

  public static void TestBug60498() {
  // This tests the CLI call syntax with procedures that have no
  // parameters -- tests fix for bug 60498
  // {call MyProc} is supported
  // {call MyProc1 ( parm1 ) } is supported
  // {call MyProcX ( parm1, parm2, ... )} is supported
  // {call MyProc() } is NOT supported.
  try {
    Class.forName("interbase.interclient.Driver");
    java.util.Properties props = new java.util.Properties();
    props.put("user", "sysdba");
    props.put("password", "masterkey");
    props.put("sqlDialect", "3");
    String url = "jdbc:interbase://venture/d:/ic/tests/gdb/d3test.gdb";
    Connection conn = DriverManager.getConnection(url, props);
    PreparedStatement pstmt = conn.prepareStatement(
       "delete from alltypes3" );
    pstmt.executeUpdate();
    pstmt.close();
    CallableStatement cstmt = conn.prepareCall("{call AddCurrentTime}");
    cstmt.executeUpdate( );
    cstmt = conn.prepareCall("{call AddTime( {t '12:30:15'})}");
    cstmt.executeUpdate( );
    cstmt = conn.prepareCall("{call AddIntTime( 1, '12:30:15.1234' )}");
    cstmt.executeUpdate( );
    cstmt.close();
    pstmt = conn.prepareStatement(
       "insert into alltypes3 (si, ti ) values (?, {t '12:30:47.1230'} )" );
    pstmt.setShort(1, (short) 2);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = conn.prepareStatement(
       "select si, ti from alltypes3" );
    ResultSet rs = pstmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int rsColumnCount = rsmd.getColumnCount();

    while (rs.next()) {
      for (int i = 1; i <= rsColumnCount; i++ ) {
        System.out.println(rsmd.getColumnLabel(i) + ": " + rs.getString(i));
      }
    System.out.println("---");
    }
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  }


}