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
 * Contributor(s): ______________________________________.
 */
package interbase.interclient;

/**
 * Describes column information for a result set.
 *
 * <p>A ResultSetMetaData object can be used to find out about the types 
 * and properties of the columns in a ResultSet.
 * <p>
 * <b>InterClient Notes:</b><br>
 * Here are the column names, labels, and base tables
 * to expect from some typical result sets:
 * <p>
 * <table border=1>
 * <tr>
 * <th>Query
 * <th>Column Name
 * <th>Column Label
 * <th>Table Name
 * </tr>
 * <tr>
 * <td><code>select F from T</code>
 * <td><code>"F"</code>
 * <td><code>"F"</code>
 * <td><code>"T"</code>
 * </tr>
 * <tr>
 * <td><code>select F as A from T</code>
 * <td><code>"F"</code>
 * <td><code>"A"</code>
 * <td><code>"T"</code>
 * </tr>
 * <tr>
 * <td><code>select F+1 from T</code>
 * <td><code>""</code>
 * <td><code>""</code>
 * <td><code>""</code>
 * </tr>
 * <tr>
 * <td><code>select MAX(F) from T</code>
 * <td><code>"MAX"</code>
 * <td><code>"MAX"</code>
 * <td><code>""</code>
 * </tr>
 * </table>
 * <p>
 * Notice that calculated result columns have no physical table name,
 * and may or may not have an ad hoc column label or name.
 *
 * @author Paul Ostler
 * @author Mikhail Melnikov
 * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
 **/
final public class ResultSetMetaData implements java.sql.ResultSetMetaData
{
  ResultSet resultSet_;

  JDBCNet jdbcNet_;

  // ResultMetaData, move these to ResultSetMetaData.
  boolean metaDataRetrieved_ = false;

  boolean writables_[] = null;

  ResultSetMetaData (ResultSet resultSet, 
		     JDBCNet jdbcNet)
  {
    resultSet_ = resultSet;
    jdbcNet_ = jdbcNet;
  }

  /**
   * Gets the number of columns in the ResultSet.
   *
   * @return the number
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int getColumnCount () throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    return resultSet_.resultCols_;
  }

  /**
   * Is the column automatically numbered, thus read-only.
   * <p>
   * <b>InterClient note:</b>
   * Always returns false for InterClient.
   * The capability does not currently exist to 
   * determine an InterBase generator field dynamically.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, subject to further functional refinements</font>
   **/
  public boolean isAutoIncrement (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    // !! Is there a way to detect generator fields in InterBase.
    return false;
  }

  /**
   * Does a column's case matters.
   *
   * <p><b>InterClient note:</b>
   * Always returns true for InterBase.
   * Text fields in InterBase are always case sensitive, even text blobs
   * when using the 'containing' operator.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isCaseSensitive (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    return true;
  }

  /**
   * Can a column be used in a where clause.
   *
   * <p><b>InterClient note:</b>
   * Always returns true for InterBase.
   * In InterBase, even a blob field can be used in a where clause,
   * eg. "WHERE blob-field CONTAINING foobar"
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isSearchable (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    return true;
  }

  /**
   * Is the column a cash value.
   *
   * <p><b>InterClient note:</b>
   * Always returns false for InterBase.
   * InterBase does not support a money type.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isCurrency (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    return false;
  }

  /**
   * Can you can put a NULL in this column.
   * <p>
   * <b>InterClient note:</b>
   * Unlike other RDBMS vendors, InterBase can always
   * determine column nullability dynamically,
   * so InterClient never returns columnNullableUnknown.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return {@link #columnNoNulls columnNoNulls}, {@link #columnNullable columnNullable} or {@link #columnNullableUnknown columnNullableUnknown}
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int isNullable (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      if (resultSet_.resultNullables_[column-1])
	return columnNullable;
      else
	return columnNoNulls;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * Does not allow NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNoNulls = 0;

  /**
   * Allows NULL values.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNullable = 1;

  /**
   * Nullability unknown.
   * @since <font color=red>JDBC 1</font>
   **/
  final static public int columnNullableUnknown = 2;

  /**
   * Is the column a signed number.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public boolean isSigned (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      switch (resultSet_.resultTypes_[column-1]) {
      case IBTypes.SMALLINT__:
      case IBTypes.INTEGER__:
      case IBTypes.FLOAT__:
      case IBTypes.DOUBLE__:
      case IBTypes.NUMERIC_DOUBLE__:
      case IBTypes.NUMERIC_INTEGER__:
      case IBTypes.NUMERIC_SMALLINT__:
// CJL-IB6 add new datatypes
      case IBTypes.NUMERIC_INT64__:
      case IBTypes.DECIMAL_INT64__:
      case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end change
	return true;
      case IBTypes.NULL_TYPE__:
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
      case IBTypes.CLOB__:
      case IBTypes.DATE__:
      case IBTypes.BLOB__:
      case IBTypes.ARRAY__:
// CJL-IB6 add new datatype support
      case IBTypes.SQLDATE__:
      case IBTypes.TIME__:
// CJL-IB6 end change
      default:
	return false;
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's the column's normal max width in chars.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return max width
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int getColumnDisplaySize (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      switch (resultSet_.resultTypes_[column-1]) {
      case IBTypes.SMALLINT__:
	return 6;
      case IBTypes.NUMERIC_SMALLINT__:
	return 8;
      case IBTypes.INTEGER__:
	return 11;
      case IBTypes.NUMERIC_INTEGER__:
      case IBTypes.DECIMAL_INTEGER__:   /// CJL-IB6 add type support
	return 13;
      case IBTypes.NUMERIC_INT64__:     /// CJL-IB6 add type support
      case IBTypes.DECIMAL_INT64__:     /// CJL-IB6 add type support
      case IBTypes.FLOAT__:
      case IBTypes.DOUBLE__:
	return 22;
      case IBTypes.NUMERIC_DOUBLE__:
	return 24;
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        return resultSet_.resultCharLengths_[column-1];
      case IBTypes.NULL_TYPE__:
      case IBTypes.CLOB__:
      case IBTypes.BLOB__:
      case IBTypes.ARRAY__:
	return 0;
      case IBTypes.DATE__:
      case IBTypes.SQLDATE__:   /// CJL-IB6 add type support
      case IBTypes.TIME__:      /// CJL-IB6 add type support
	return resultSet_.resultPrecisions_[column-1];
      default:
        return 0;
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * Gets the suggested column title for use in printouts and displays.
   * <p>
   * <b>InterClient Notes:</b><br>
   * See the table in the {@link ResultSetMetaData} class header
   * documentation for the column labels associated with some typical SQL queries.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public String getColumnLabel (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      return resultSet_.resultColumnLabels_[column-1];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's a column's name.
   * <p>
   * <b>InterClient Notes:</b><br>
   * See the table in the {@link ResultSetMetaData} class header
   * documentation for the column names associated with some typical SQL queries.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return column name
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public String getColumnName (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      return resultSet_.resultColumnNames_[column-1];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's a column's table's schema.
   *
   * <p><b>InterClient note:</b>
   * Always returns "".
   * InterBase does not support schemas.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return schema name or "" if not applicable
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getSchemaName (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor (); // !!!should we really be checking this all the time?
    return "";
    //throw new DriverNotCapableException (ErrorKey.driverNotCapable__schemas__);
  }

  /**
   * What's a column's number of decimal digits.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return precision
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int getPrecision (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      switch (resultSet_.resultTypes_[column-1]) {
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        return resultSet_.resultCharLengths_[column-1];
      // MMM - if the column is of array type return element's precision
      case IBTypes.ARRAY__:
        return resultSet_.arrayDescriptors_[column-1].elementPrecision_;
      // MMM - end
      default:
        return resultSet_.resultPrecisions_[column-1];
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's a column's number of digits to right of the decimal point.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return scale
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int getScale (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      switch (resultSet_.resultTypes_[column-1]) {
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        return 0;
      // MMM - if the column is of array type return element's scale
      case IBTypes.ARRAY__:
        return resultSet_.arrayDescriptors_[column-1].elementScale_;
      // MMM - end
      default:
        return resultSet_.resultScales_[column-1];
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * Gets a column's origination table name.
   * <p>
   * <b>InterClient Notes:</b><br>
   * See the table in the {@link ResultSetMetaData} class header
   * documentation for the tables names associated with some typical SQL queries.
   *
   * @return table name or "" if not applicable
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public String getTableName (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      return resultSet_.resultTableNames_[column-1];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's a column's table's catalog name.
   *
   * <p><b>InterClient note:</b>
   * Always returns "".
   * InterBase does not support catalogs.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return column name or "" if not applicable.
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  public String getCatalogName (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();
    return "";
    //throw new DriverNotCapableException (ErrorKey.driverNotCapable__catalogs__);
  }

  /**
   * What's a column's SQL type.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return SQL type from {@link java.sql.Types java.sql.Types}
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public int getColumnType (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      return IBTypes.getSQLType (resultSet_.resultTypes_[column-1]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * What's a column's data source specific type name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return type name, if a UDT then a fully qualified type name is returned
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1</font>
   **/
  public String getColumnTypeName (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      return IBTypes.getIBTypeName (resultSet_.resultTypes_[column-1]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * Is a column definitely not writable.
   * <p><b>InterClient note:</b>
   * This returns true for a column which is a computed field,
   * or is a view field, or the current user does not have the
   * necessary SQL privileges to write to this field.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, behavior subject to further functional refinements</font>
   **/
  public boolean isReadOnly (int column) throws java.sql.SQLException
  {
    // See query in JIBSRemote::get_result_column_meta_data
    return !isWritable (column);
  }

  /**
   * Is it possible for a write on the column to succeed.
   * <p><b>InterClient note:</b>
   * This returns true for a column which is not a computed field,
   * and is not a view field, and the current user has the
   * necessary SQL privileges to write to this field.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, behavior subject to further functional refinements</font>
   **/
  synchronized public boolean isWritable (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    // See query in JIBSRemote::get_result_column_meta_data
    try {
      if (metaDataRetrieved_)
	return writables_[column-1];
      
      remote_GET_RESULT_COLUMN_META_DATA ();
      metaDataRetrieved_ = true;
  
      return writables_[column-1];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }

  /**
   * Will a write on the column definitely succeed.
   * <p><b>InterClient note:</b>
   * Always returns <code>false</code>.
   * We can never guarantee this for InterBase because there may
   * be triggers which could abort upon writing the column.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return true if so
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 1, behavior subject to further functional refinements</font>
   **/
  public boolean isDefinitelyWritable (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    // We can never guarantee this.
    // We can never guarantee this for InterBase because there may
    // be triggers which could abort upon writing the column.
    // Is this so?
    return false;
  }

  private void remote_GET_RESULT_COLUMN_META_DATA () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.GET_RESULT_COLUMN_META_DATA__);
    sendMsg.writeInt (resultSet_.statement_.statementRef_);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      int columnCount = getColumnCount ();
      writables_ = new boolean [columnCount];

      for (int i=0; i < columnCount; i++) {
	writables_[i] = recvMsg.readBoolean ();
      }

      resultSet_.setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  //--------------------------JDBC 2.0------------------------------------------

  /**
   * Return the fully qualified name of the Java class whose instances
   * are manufactured if ResultSet.getObject() is called to retrieve a value
   * from the column.  ResultSet.getObject() may return a subClass of the
   * class returned by this method.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public String getColumnClassName (int column) throws java.sql.SQLException
  {
      throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  //--------------------------InterClient Extensions----------------------------

  // MMM -added method getArrayBaseType()
  /**
   * Gets an array column's base SQL type from {@link java.sql.Types java.sql.Types}.
   * The result column must be of type {@link java.sql.Types#ARRAY java.sql.Types.ARRAY}.
   *
   * @param column the first column is 1, the second is 2, ...
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  public int getArrayBaseType (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      if (resultSet_.resultTypes_[column-1] != IBTypes.ARRAY__)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__not_array_column__);

      // !!!INSQLDA_NONAMES - need the null check for IB < 6
      if (resultSet_.arrayDescriptors_[column-1] != null)
        return IBTypes.getSQLType (resultSet_.arrayDescriptors_[column-1].elementDataType_);
      else
        throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }
  //*end jre12*
  // MMM - end

  // MMM -added method getArrayDimensions()
  /**
   * Gets an array column's dimensions and bounds.
   * The result column must be of type {@link java.sql.Types#ARRAY java.sql.Types.ARRAY}.
   *
   * @return an array of arrays describing dimensions and bounds for each dimension.
   * @param column the first column is 1, the second is 2, ...
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  public int[][] getArrayDimensions (int column) throws java.sql.SQLException
  {
    resultSet_.checkForClosedCursor ();

    try {
      if (resultSet_.resultTypes_[column-1] != IBTypes.ARRAY__)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__not_array_column__);

      if (resultSet_.arrayDescriptors_[column-1] != null)
        return resultSet_.arrayDescriptors_[column-1].getDimensions();
      else
        throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ColumnIndexOutOfBoundsException (ErrorKey.columnIndexOutOfBounds__0__,
						 column);
    }
  }
  //*end jre12*
  // MMM - end
}
