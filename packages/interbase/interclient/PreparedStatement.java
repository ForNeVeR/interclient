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
 * Represents a precompiled SQL statement, possibly parameterized.
 *
 * <P>A SQL statement is pre-compiled and stored in a
 * PreparedStatement object. This object can then be used to
 * efficiently execute this statement multiple times.
 *
 * <P><B>Note:</B> The setXXX methods for setting IN parameter values
 * must specify types that are compatible with the defined SQL type of
 * the input parameter. For instance, if the IN parameter has SQL type
 * Integer then setInt should be used.
 *
 * <p>If arbitrary parameter type conversions are required then the
 * setObject method should be used with a target SQL type.
 *
 * @author Paul Ostler
 * @author Mikhail Melnikov
 * @see Connection#prepareStatement
 * @see ResultSet
 * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
 **/
public class PreparedStatement extends Statement implements java.sql.PreparedStatement
{
  String sql_;
  private boolean isEscapedProcedureCall_ = false;
    
  boolean sendInputs_ = false;

  int inputCols_;
  boolean inputNullables_[];
  int inputTypes_[];
  int inputPrecisions_[];
  int inputScales_[];
  int inputCharSets_[];
  int inputCharLengths_[];
  // MMM - array of Descriptors
  ArrayDescriptor[] arrayDescriptors_;
  // MMM - end

  char[] cbuf_;
  byte[] encodingBuf_;

  ParameterMetaData parameterMetaData_ = null;

  Object inputs_[];

  private final static java.math.BigDecimal bdMaxShortValue = new java.math.BigDecimal (Short.MAX_VALUE);
  private final static java.math.BigDecimal bdMinShortValue = new java.math.BigDecimal (Short.MIN_VALUE);
  private final static java.math.BigDecimal bdMaxIntValue = new java.math.BigDecimal (Integer.MAX_VALUE);
  private final static java.math.BigDecimal bdMinIntValue = new java.math.BigDecimal (Integer.MIN_VALUE);
  private final static java.math.BigDecimal bdMaxFloatValue = new java.math.BigDecimal (Float.MAX_VALUE);
  private final static java.math.BigDecimal bdMinFloatValue = new java.math.BigDecimal (-Float.MAX_VALUE);
  private final static java.math.BigDecimal bdMaxDoubleValue = new java.math.BigDecimal (Double.MAX_VALUE);
  private final static java.math.BigDecimal bdMinDoubleValue = new java.math.BigDecimal (-Double.MAX_VALUE);
// CJL-IB6 add limits for new long types
  private final static java.math.BigDecimal bdMaxLongValue = new java.math.BigDecimal (Long.MAX_VALUE);
  private final static java.math.BigDecimal bdMinLongValue = new java.math.BigDecimal (Long.MIN_VALUE);
// CJL-IB6 end change

  // Called by Connection.close() to mark open statements as closed.
  // Called by commit() and rollback() for prepared statements when
  // auto close is enabled.
  void local_Close () throws java.sql.SQLException
  {
    if (resultSet_ != null)
      resultSet_.local_Close ();
    openOnClient_ = false;
    openOnServer_ = false;
    connection_.openPreparedStatements_.removeElement (this);
    Globals.cache__.returnBuffer (encodingBuf_);
    Globals.cache__.returnCharBuffer (cbuf_);
  }

  // for Connection.prepareStatement ()
  PreparedStatement (JDBCNet jdbcNet,
                     Connection connection, 
                     String sql) throws java.sql.SQLException
  {
    super (jdbcNet, connection);

    checkForEmptySQL (sql);

    if (escapeProcessingEnabled_) {
      EscapeProcessor escapeProcessor = new EscapeProcessor();
      sql_ = escapeProcessor.doEscapeProcessing (sql);
      isEscapedProcedureCall_ = escapeProcessor.isEscapedProcedureCall ();
    }
    else
      sql_ = sql;

    connection_.transactionStartedOnClient_ = true;
  
    remote_PREPARE_STATEMENT ();

    connection_.transactionStartedOnServer_ = true;
    openOnServer_ = true;
    inputs_ = new Object [inputCols_];
  }

  private void remote_PREPARE_STATEMENT () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.PREPARE_STATEMENT__);
    connection_.send_TransactionConfigData (sendMsg);
    sendMsg.writeLDChars (sql_);
    sendMsg.writeBoolean (isEscapedProcedureCall_);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      // Receive input meta data
      statementRef_ = recvMsg.readInt ();
      inputCols_ = recvMsg.readUnsignedShort ();
      inputNullables_ = new boolean [inputCols_];
      inputTypes_ = new int [inputCols_]; 
      inputPrecisions_ = new int [inputCols_];
      inputScales_= new int [inputCols_];
      inputCharSets_ = new int [inputCols_];
      inputCharLengths_ = new int [inputCols_];
      // MMM - create array of ArrayDescriptor
      arrayDescriptors_ = new ArrayDescriptor[inputCols_];
      // MMM - end

      if (Globals.debug__) {
         Globals.trace ("Prepared statement meta data:");
         Globals.trace ("statementRef = " + statementRef_);
         Globals.trace ("input cols = " + inputCols_);
      }

      for (int i = 0; i < inputCols_; i++) {
        inputNullables_[i] = recvMsg.readBoolean ();
	inputTypes_[i] = recvMsg.readUnsignedByte ();
	inputPrecisions_[i] = recvMsg.readUnsignedShort ();
        inputScales_[i] = recvMsg.readUnsignedByte ();
	inputCharSets_[i] = recvMsg.readUnsignedShort ();
	inputCharLengths_[i] = recvMsg.readUnsignedShort ();
        // MMM - get an array descriptor
        // If parameter is of ARRAY data type and the array descriptor
        // is available, get an array descriptor
        if (inputTypes_[i] == IBTypes.ARRAY__ && recvMsg.readBoolean()) {
          arrayDescriptors_[i] = new ArrayDescriptor(recvMsg);
        }
        // MMM - end
	if (Globals.debug__) {
	  Globals.trace ("input nullable " + inputNullables_[i]);
	  Globals.trace ("input type " + inputTypes_[i]);
	  Globals.trace ("input precision " + inputPrecisions_[i]);
	  Globals.trace ("input scale " + inputScales_[i]);
	  Globals.trace ("input char set " + inputCharSets_[i]);
	  Globals.trace ("input char length " + inputCharLengths_[i]);
          // MMM - should i add something here?
          // !!! Should we print Descriptor info here? Not to mention
          // that for IB with version less than 6 descriptor could be null.
          // MMM - end
        }
      }

      allocateEncodingBufs ();

      int count = recvMsg.readUnsignedByte ();

      if (count > 1) { // for future use, this is the number of result sets
	throw new RemoteProtocolException (ErrorKey.remoteProtocol__unexpected_token_from_server_0__, 105);
      }

      if (count == 0) { // update statement
         setWarning (recvMsg.get_WARNINGS ());
      }
    
      if (count == 1) { // query statement
        int resultCols = recvMsg.readUnsignedShort ();

        if (resultCols == 0)
          throw new BugCheckException (ErrorKey.bugCheck__0__, 111);

        // Notice that the result set is not yet associated with a recv buffer
        resultSet_ = new ResultSet (this, jdbcNet_, null, resultCols, false);
        resultSet_.recv_ResultMetaData (recvMsg);
        setWarning (recvMsg.get_WARNINGS ());
      }

      if (count < 0) 
	throw new BugCheckException (ErrorKey.bugCheck__0__, 112);
    }
    catch (java.sql.SQLException e) {
      if (resultSet_ != null) {
        resultSet_.local_Close ();
        resultSet_ = null;
      }
      throw e;
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  synchronized void allocateEncodingBufs ()
  {
    // !!! should only get precision for CHAR/VARCHAR fields, also for ResultSet.allocateCbuf()
    // get the maximum precision (byte length)
    // initialize to accomodate sending sql string in any encoding, 3-byte max.
    int maxPrecision = 0;
    int maxCharLength = 0;
    for (int i=0; i<inputCols_; i++) {
      if (inputPrecisions_[i] > maxPrecision)
	maxPrecision = inputPrecisions_[i];
      if (inputCharLengths_[i] > maxCharLength)
        maxCharLength = inputCharLengths_[i];
    }
    
    // allocate to accomodate largest column, +1 to accomodate null terminator
    encodingBuf_ = Globals.cache__.takeBuffer (maxPrecision+1);
    cbuf_ = Globals.cache__.takeCharBuffer (maxCharLength);
  }

  /**
   * A prepared SQL query is executed and its ResultSet is returned.
   *
   * @return a ResultSet that contains the data produced by the query; never null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public java.sql.ResultSet executeQuery () throws java.sql.SQLException
  {
    checkForClosedStatement ();

    clearWarnings ();

    if (!allNonNullablesAreSet ())
      throw new InvalidOperationException (ErrorKey.invalidOperation__parameter_not_set__);

    if (resultSet_ == null) {
      throw new InvalidOperationException (ErrorKey.invalidOperation__execute_query_on_an_update_statement__);
    }

    resultSet_.cursorName_ = cursorName_;

    if (resultSet_.openOnServer_) {
      resultSet_.local_Close ();
    }

    updateCountStack_ = null;
    connection_.transactionStartedOnClient_ = true;

    remote_EXECUTE_PREPARED_QUERY_STATEMENT ();

    connection_.transactionStartedOnServer_ = true;
    resultSetStack_ = null;

    return resultSet_;
  }

  private void remote_EXECUTE_PREPARED_QUERY_STATEMENT () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_PREPARED_QUERY_STATEMENT__);
    send_PreparedStatementExecuteData (sendMsg);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      if (!recvMsg.getHeaderEndOfStream ())
	remote_sendPrefetch ();

      resultSet_.local_open ();

      resultSet_.numRows_ = recvMsg.readInt ();

      // ResultData remains in jdbcNet buffer associated with this statement
      // awaiting subsequent ResultSet.next() and get*() calls.
      resultSet_.setRecvBuffer (recvMsg);
    }
    catch (java.sql.SQLException e) {
      if (resultSet_ != null)
        resultSet_.local_Close ();
      jdbcNet_.destroyRecvMessage (recvMsg);
      throw e;
    }
  }

  /**
   * Execute a SQL INSERT, UPDATE or DELETE statement. In addition,
   * SQL statements that return nothing such as SQL DDL statements
   * can be executed.
   *
   * @return either the row count for INSERT, UPDATE or DELETE; or 0
   *    for SQL statements that return nothing
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public int executeUpdate () throws java.sql.SQLException
  {
    checkForClosedStatement ();

    clearWarnings ();

    if (!allNonNullablesAreSet ())
      throw new InvalidOperationException (ErrorKey.invalidOperation__parameter_not_set__);

    if (resultSet_ != null && resultSet_.openOnServer_) {
      resultSet_.local_Close ();
    }

    updateCountStack_ = null;
    connection_.transactionStartedOnClient_ = true;

    int updateCountOrSelectValue = remote_EXECUTE_PREPARED_UPDATE_STATEMENT ();

    connection_.transactionStartedOnServer_ = true;
    resultSetStack_ = null;

    return (updateCountOrSelectValue);
  }

  private int remote_EXECUTE_PREPARED_UPDATE_STATEMENT () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_PREPARED_UPDATE_STATEMENT__);
    send_PreparedStatementExecuteData (sendMsg);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      int updateCountOrSelectValue = recvMsg.readInt ();

      setWarning (recvMsg.get_WARNINGS ());

      return updateCountOrSelectValue;
    }
    catch (java.sql.SQLException e) {
      if (resultSet_ != null)
        resultSet_.local_Close ();
      throw e;
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }

  private boolean allNonNullablesAreSet ()
  {
    for (int i = 0; i < inputCols_; i++) {
      if (!inputNullables_[i] && (inputs_[i] == null))
        return false;
    }
    return true;
  }

  /**
   * Set a parameter to SQL NULL.
   *
   * <P><B>Note:</B> You must specify the parameter's SQL type.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param sqlType SQL type code defined by java.sql.Types
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setNull (int parameterIndex, int sqlType) throws java.sql.SQLException
  {
    checkForClosedStatement ();

    try {
      if (!inputNullables_[parameterIndex-1])
	throw new InvalidOperationException (ErrorKey.invalidOperation__set_null_on_non_nullable_parameter__);

      inputs_[parameterIndex-1] = null;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

  private int getParameterIBType (int parameterIndex) throws java.sql.SQLException
  {
    try {
      return inputTypes_[parameterIndex-1];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

  /**
   * Set a parameter to a Java boolean value.  The driver converts this
   * to a SQL BIT value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setBoolean (int parameterIndex, boolean x) throws java.sql.SQLException
  {
    if (x)
      setInt (parameterIndex, 1);
    else
      setInt (parameterIndex, 0);
  }

  /**
   * Set a parameter to a Java byte value.  The driver converts this
   * to a SQL TINYINT value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setByte (int parameterIndex, byte x) throws java.sql.SQLException
  {
    setInt (parameterIndex, x);
  }

  /**
   * Set a parameter to a Java short value.  The driver converts this
   * to a SQL SMALLINT value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setShort (int parameterIndex, short x) throws java.sql.SQLException
  {
    setInt (parameterIndex, x);
  }

  /**
   * Set a parameter to a Java int value.  The driver converts this
   * to a SQL INTEGER value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setInt (int parameterIndex, int x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    java.math.BigDecimal scaledUpBd = null;
    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INTEGER__:
    case IBTypes.DECIMAL_INT64__:
// CJL-IB6 end change
      scaledUpBd = new java.math.BigDecimal (x);
      scaledUpBd = scaledUpBd.movePointRight (inputScales_[parameterIndex-1]);
      scaledUpBd = scaledUpBd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
    }

    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
      if (scaledUpBd.compareTo (bdMaxShortValue) == 1 || scaledUpBd.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (scaledUpBd.shortValue ());
      break;
    case IBTypes.SMALLINT__:
      if (x > Short.MAX_VALUE || x < Short.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short ((short) x);
      break;
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 add support for new types
    case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end changes
      if (scaledUpBd.compareTo (bdMaxIntValue) == 1 || scaledUpBd.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) scaledUpBd.intValue ());
      break;
    case IBTypes.INTEGER__:
      inputs_[parameterIndex-1] = new Integer (x);
      break;
// CJL-IB6 add support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INT64__:
      if (scaledUpBd.compareTo (bdMaxLongValue) == 1 || scaledUpBd.compareTo (bdMinLongValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Long ((long) scaledUpBd.longValue ());
      break;
// CJL-IB6 end changes
    case IBTypes.NUMERIC_DOUBLE__:
    case IBTypes.DOUBLE__:
      inputs_[parameterIndex-1] = new Double ((double) x);
      break;
    case IBTypes.FLOAT__:
      inputs_[parameterIndex-1] = new Float ((float) x);
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = String.valueOf (x);
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, String.valueOf (x))); 
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_number_on_binary_blob__);
    case IBTypes.ARRAY__:
    case IBTypes.DATE__:
// CJL-IB6 support for new date/time types
// !!! shouldn't there be a default value here?
    case IBTypes.SQLDATE__:
    case IBTypes.TIME__:
// CJL-IB6 end change
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a Java long value.  The driver converts this
   * to a SQL BIGINT value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setLong (int parameterIndex, long x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    java.math.BigDecimal scaledUpBd = null;
    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INTEGER__:
    case IBTypes.DECIMAL_INT64__:
// CJL-IB6 end change
      scaledUpBd = new java.math.BigDecimal (x);
      scaledUpBd = scaledUpBd.movePointRight (inputScales_[parameterIndex-1]);
      scaledUpBd = scaledUpBd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
    }

    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
      if (scaledUpBd.compareTo (bdMaxShortValue) == 1 || scaledUpBd.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (scaledUpBd.shortValue ());
      break;
    case IBTypes.SMALLINT__:
      if (x > Short.MAX_VALUE || x < Short.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short ((short) x);
      break;
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 add support for new types
    case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end changes
      if (scaledUpBd.compareTo (bdMaxIntValue) == 1 || scaledUpBd.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) scaledUpBd.intValue ());
      break;
    case IBTypes.INTEGER__:
      if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) x);
      break;
// CJL-IB6 add support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INT64__:
      if (scaledUpBd.compareTo (bdMaxLongValue) == 1 || scaledUpBd.compareTo (bdMinLongValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Long ((long) scaledUpBd.longValue ());
      break;
// CJL-IB6 end changes
    case IBTypes.NUMERIC_DOUBLE__:
    case IBTypes.DOUBLE__:
      inputs_[parameterIndex-1] = new Double ((double) x);
      break;
    case IBTypes.FLOAT__:
      inputs_[parameterIndex-1] = new Float ((float) x);
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = String.valueOf (x);
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, String.valueOf (x)));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_number_on_binary_blob__);
    case IBTypes.ARRAY__:
    case IBTypes.DATE__:
// CJL-IB6 support for new date/time types
// !!! shouldn't there be a default value here?
    case IBTypes.SQLDATE__:
    case IBTypes.TIME__:
// CJL-IB6 end change
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a Java float value.  The driver converts this
   * to a SQL FLOAT value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setFloat (int parameterIndex, float x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    java.math.BigDecimal scaledUpBd = null;
    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INTEGER__:
    case IBTypes.DECIMAL_INT64__:
// CJL-IB6 end change
      scaledUpBd = new java.math.BigDecimal (x);
      scaledUpBd = scaledUpBd.movePointRight (inputScales_[parameterIndex-1]);
      scaledUpBd = scaledUpBd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
    }

    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
      if (scaledUpBd.compareTo (bdMaxShortValue) == 1 || scaledUpBd.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (scaledUpBd.shortValue ());
      break;
    case IBTypes.SMALLINT__:
      if (x > Short.MAX_VALUE || x < Short.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short ((short) x);
      break;
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 add support for new types
    case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end changes
      if (scaledUpBd.compareTo (bdMaxIntValue) == 1 || scaledUpBd.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) scaledUpBd.intValue ());
      break;
    case IBTypes.INTEGER__:
      if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) x);
      break;
// CJL-IB6 add support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INT64__:
      if (scaledUpBd.compareTo (bdMaxLongValue) == 1 || scaledUpBd.compareTo (bdMinLongValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Long ((long) scaledUpBd.longValue ());
      break;
// CJL-IB6 end changes
    case IBTypes.NUMERIC_DOUBLE__:
    case IBTypes.DOUBLE__:
      inputs_[parameterIndex-1] = new Double ((double) x);
      break;
    case IBTypes.FLOAT__:
      inputs_[parameterIndex-1] = new Float (x);
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = String.valueOf (x);
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, String.valueOf (x)));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_number_on_binary_blob__);
    case IBTypes.ARRAY__:
    case IBTypes.DATE__:
// CJL-IB6 support for new date/time types
// !!! shouldn't there be a default value here?
    case IBTypes.SQLDATE__:
    case IBTypes.TIME__:
// CJL-IB6 end change
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a Java double value.  The driver converts this
   * to a SQL DOUBLE value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setDouble (int parameterIndex, double x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    java.math.BigDecimal scaledUpBd = null;
    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INTEGER__:
    case IBTypes.DECIMAL_INT64__:
// CJL-IB6 end change
      scaledUpBd = new java.math.BigDecimal (x);
      scaledUpBd = scaledUpBd.movePointRight (inputScales_[parameterIndex-1]);
      scaledUpBd = scaledUpBd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
    }

    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
      if (scaledUpBd.compareTo (bdMaxShortValue) == 1 || scaledUpBd.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (scaledUpBd.shortValue ());
      break;
    case IBTypes.SMALLINT__:
      if (x > Short.MAX_VALUE || x < Short.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
     inputs_[parameterIndex-1] = new Short ((short) x);
      break;
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 add support for new types
    case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end changes
      if (scaledUpBd.compareTo (bdMaxIntValue) == 1 || scaledUpBd.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) scaledUpBd.intValue ());
      break;
    case IBTypes.INTEGER__:
      if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) x);
      break;
// CJL-IB6 add support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INT64__:
      if (scaledUpBd.compareTo (bdMaxLongValue) == 1 || scaledUpBd.compareTo (bdMinLongValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Long ((long) scaledUpBd.longValue ());
      break;
// CJL-IB6 end changes
    case IBTypes.NUMERIC_DOUBLE__:
    case IBTypes.DOUBLE__:
      inputs_[parameterIndex-1] = new Double (x);
      break;
    case IBTypes.FLOAT__:
      if (x > Float.MAX_VALUE || x < Float.MIN_VALUE)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Float ((float) x);
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = String.valueOf (x);
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, String.valueOf (x)));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_number_on_binary_blob__);
    case IBTypes.ARRAY__:
    case IBTypes.DATE__:
// CJL-IB6 support for new date/time types
// !!! shouldn't there be a default value here?
    case IBTypes.SQLDATE__:
    case IBTypes.TIME__:
// CJL-IB6 end change
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a java.lang.BigDecimal value.
   * The driver converts this to a SQL NUMERIC value when
   * it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setBigDecimal (int parameterIndex, java.math.BigDecimal x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    java.math.BigDecimal scaledUpBd = null;
    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INTEGER__:
    case IBTypes.DECIMAL_INT64__:
// CJL-IB6 end change
      scaledUpBd = x.movePointRight (inputScales_[parameterIndex-1]);
      scaledUpBd = scaledUpBd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
    }

    switch (ibType) {
    case IBTypes.NUMERIC_SMALLINT__:
      if (scaledUpBd.compareTo (bdMaxShortValue) == 1 || scaledUpBd.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (scaledUpBd.shortValue ());
      break;
    case IBTypes.SMALLINT__:
      if (x.compareTo (bdMaxShortValue) == 1 || x.compareTo (bdMinShortValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Short (x.shortValue ());
      break;
    case IBTypes.NUMERIC_INTEGER__:
// CJL-IB6 add support for new types
    case IBTypes.DECIMAL_INTEGER__:
// CJL-IB6 end changes
      if (scaledUpBd.compareTo (bdMaxIntValue) == 1 || scaledUpBd.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) scaledUpBd.intValue ());
      break;
    case IBTypes.INTEGER__:
      if (x.compareTo (bdMaxIntValue) == 1 || x.compareTo (bdMinIntValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Integer ((int) x.intValue ());
      break;
// CJL-IB6 add support for new types
    case IBTypes.NUMERIC_INT64__:
    case IBTypes.DECIMAL_INT64__:
      if (scaledUpBd.compareTo (bdMaxLongValue) == 1 || scaledUpBd.compareTo (bdMinLongValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Long ((long) scaledUpBd.longValue ());
      break;
// CJL-IB6 end changes
    case IBTypes.NUMERIC_DOUBLE__:
    case IBTypes.DOUBLE__:
      if (x.compareTo (bdMaxDoubleValue) == 1 || x.compareTo (bdMinDoubleValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Double (x.doubleValue ());
      break;
    case IBTypes.FLOAT__:
      if (x.compareTo (bdMaxFloatValue) == 1 || x.compareTo (bdMinFloatValue) == -1)
        throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__,
                                                String.valueOf (x));
      inputs_[parameterIndex-1] = new Float (x.floatValue ());
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = x.toString ();
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, x.toString ()));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_number_on_binary_blob__);
    case IBTypes.ARRAY__:
    case IBTypes.DATE__:
// CJL-IB6 support for new date/time types
// !!! shouldn't there be a default value here?
    case IBTypes.SQLDATE__:
    case IBTypes.TIME__:
// CJL-IB6 end change
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  // !!! check for data truncation exception and
  // !!! check for invalidArgumentException if 2-byte character found.
  // !!! Comment on VARCHAR driver limits in javadoc interclient note
  /**
   * Set a parameter to a Java String value.  The driver converts this
   * to a SQL VARCHAR or LONGVARCHAR value (depending on the arguments
   * size relative to the driver's limits on VARCHARs) when it sends
   * it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setString (int parameterIndex, String x) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    try { 
      switch (ibType) {
      case IBTypes.SMALLINT__:
      case IBTypes.INTEGER__:
        setInt (parameterIndex, Integer.parseInt (x));
        break;
      case IBTypes.NUMERIC_INTEGER__:
      case IBTypes.NUMERIC_SMALLINT__:
        setBigDecimal (parameterIndex, new java.math.BigDecimal (x));
        break;
      case IBTypes.NUMERIC_DOUBLE__:
      case IBTypes.DOUBLE__:
        inputs_[parameterIndex-1] = new Double (x);
        break;
      case IBTypes.FLOAT__:
        inputs_[parameterIndex-1] = new Float (x);
        break;
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        inputs_[parameterIndex-1] = x;
        if (x.length() > inputCharLengths_[parameterIndex-1]) // !!! sets should not truncate
          throw new java.sql.DataTruncation (parameterIndex,
                                             true,        // is a parameter
                                             false,       // not a read
                                             x.length (), // original data size
                                             inputCharLengths_[parameterIndex-1]);
        break;
      // MMM - can not set string to array parameter
      // !!! ask Mikhail about this, setString() may need to work on every type.
      case IBTypes.ARRAY__:
        throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
      // MMM - end
      case IBTypes.CLOB__:
      case IBTypes.BLOB__:
        inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, x));
        break;
      case IBTypes.DATE__:
        // First convert the string in JDBC escape timestamp format to a timestamp
        try {
          setTimestamp (parameterIndex, java.sql.Timestamp.valueOf (x));
        }
        catch (java.lang.IllegalArgumentException e) {
          throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__, x);
        }
        break;
// CJL-IB6 adding support for new types
      case IBTypes.NUMERIC_INT64__:
      case IBTypes.DECIMAL_INT64__:
      case IBTypes.DECIMAL_INTEGER__:
        setBigDecimal (parameterIndex, new java.math.BigDecimal (x));
        break;
      case IBTypes.SQLDATE__:
        // First convert the string in JDBC escape date format to a timestamp
        try {
          setDate (parameterIndex, java.sql.Date.valueOf (x));
        }
        catch (java.lang.IllegalArgumentException e) {
          throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__, x);
        }
        break;
      case IBTypes.TIME__:
        // First convert the string in JDBC escape time format to a timestamp
        try {
          setTime (parameterIndex, java.sql.Time.valueOf (x));
        }
        catch (java.lang.IllegalArgumentException e) {
          throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__, x);
        }
        break;
// CJL-IB6 end change
      }
    }
    catch (java.lang.NumberFormatException e) {
      throw new ParameterConversionException (ErrorKey.parameterConversion__instance_conversion_0__, x);
    }
  }

  // !!! comment on driver limits
  /**
   * Set a parameter to a Java array of bytes.  The driver converts
   * this to a SQL VARBINARY or LONGVARBINARY (depending on the
   * argument's size relative to the driver's limits on VARBINARYs)
   * when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setBytes (int parameterIndex, byte[] x) throws java.sql.SQLException
  {
    checkForClosedStatement ();

    int ibType = getParameterIBType (parameterIndex);
    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = x;
      break;
    case IBTypes.CLOB__:
    case IBTypes.BLOB__:
    // MMM - Do not support array of bytes for array parameter
    // case IBTypes.ARRAY__:
    // MMM - end
      inputs_[parameterIndex-1] = 
	new Integer (jdbcNet_.setBlobBinaryStream (this, new java.io.ByteArrayInputStream (x), x.length));
      break;

    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a java.sql.Date value.  The driver converts this
   * to a SQL DATE value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setDate (int parameterIndex,
                                    java.sql.Date x) throws java.sql.SQLException
  {
    checkForClosedStatement ();

    int ibType = getParameterIBType (parameterIndex);
    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = x.toString ();
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, x.toString ()));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_date_on_binary_blob__);
    case IBTypes.DATE__:
// CJL-IB6 add support for sqldate
    case IBTypes.SQLDATE__:
// CJL-IB6 end change
      inputs_[parameterIndex-1] = new IBTimestamp (x.getYear (), x.getMonth (), x.getDate ());
      break;
    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a java.sql.Time value.  The driver converts this
   * to a SQL TIME value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setTime (int parameterIndex,
				    java.sql.Time x) throws java.sql.SQLException
  {
    checkForClosedStatement ();

    int ibType = getParameterIBType (parameterIndex);
    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = x.toString ();
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, x.toString ()));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_date_on_binary_blob__);
    case IBTypes.DATE__:
// CJL-IB6 add case for time
    case IBTypes.TIME__:
// CJL-IB6 end change
      // sets dummy year 1900, dummy month jan, dummy date the first
      inputs_[parameterIndex-1] = new IBTimestamp (0, 0, 1, x.getHours (), x.getMinutes (), x.getSeconds ());
      break;
    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * Set a parameter to a java.sql.Timestamp value.  The driver
   * converts this to a SQL TIMESTAMP value when it sends it to the
   * database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setTimestamp (int parameterIndex,
					 java.sql.Timestamp x) throws java.sql.SQLException
  {
    checkForClosedStatement ();

    int ibType = getParameterIBType (parameterIndex);
    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = x.toString ();
      break;
    case IBTypes.CLOB__:
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobString (this, x.toString ()));
      break;
    case IBTypes.BLOB__:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__set_date_on_binary_blob__);
    case IBTypes.DATE__:
      inputs_[parameterIndex-1] = new IBTimestamp (x.getYear (), x.getMonth (), x.getDate (),
						   x.getHours (), x.getMinutes (), x.getSeconds());
      break;
// CJL-IB6 added support for new Date and Time types
    // send Date at midnight
    case IBTypes.SQLDATE__:
      inputs_[parameterIndex-1] = new IBTimestamp (x.getYear (), x.getMonth (), x.getDate (),
						   0, 0, 0);
      break;
    // send Time with date of 1/1/1900
    case IBTypes.TIME__:
      inputs_[parameterIndex-1] = new IBTimestamp (0, 0, 1,
						   x.getHours (), x.getMinutes (), x.getSeconds());
      break;
// CJL-IB6 end change
    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  // Construct a byte array from the user supplied input stream in setBinaryStream()
  byte[] getBytesFromInputStream (java.io.InputStream in, int length) throws java.sql.SQLException
  {
    try {
      byte[] buf = new byte [length];
      int n = 0;
      while (n < length) {
	n += in.read (buf, n, length - n);
      }
      return buf;
    }
    catch (java.io.IOException e) {
      throw new CommunicationException (ErrorKey.communication__user_stream__io_exception_on_read_0__,
					Utils.getMessage (e));
    }
  }

  // Construct a string from the user supplied ascii input stream in setAsciiStream()
  String getStringFromAsciiInputStream (java.io.InputStream in, int length) throws java.sql.SQLException
  {
    byte[] buf = getBytesFromInputStream (in, length);
    return new String (buf,  // ascii byte array
		       0);   // hibyte - the top 8 bits of each 16 bit unicode character
  }

  // Construct a string from the user supplied unicode input stream in setUnicodeStream()
  String getStringFromUnicodeInputStream (java.io.InputStream in, int length) throws java.sql.SQLException
  {
    byte[] buf = getBytesFromInputStream (in, length);
    char[] cbuf = new char[length/2];
    int bufIndex = 0;
    for (int i=0; i<length/2; i++) {
      int hiByte = (buf[bufIndex++] & 0xff) << 8;
      int loByte = (buf[bufIndex++] & 0xff) << 0;
      cbuf[i] = (char)(hiByte + loByte);
    }
    return new String (cbuf);
  }

  /**
   * When a very large ASCII value is input to a LONGVARCHAR
   * parameter, it may be more practical to send it via a
   * java.io.InputStream. JDBC will read the data from the stream
   * as needed, until it reaches end-of-file.  The JDBC driver will
   * do any necessary conversion from ASCII to the database char format.
   *
   * <P><B>Note:</B> This stream object can either be a standard
   * Java stream object or your own subclass that implements the
   * standard interface.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the java input stream which contains the ASCII parameter value
   * @param length the number of bytes in the stream
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setAsciiStream (int parameterIndex,
					   java.io.InputStream x, 
					   int length) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = getStringFromAsciiInputStream (x, length); 
      break;
    case IBTypes.CLOB__:
    case IBTypes.BLOB__:
      // MMM - Cannot set an ascii stream for array parameter
      // case IBTypes.ARRAY__:
      // MMM - end
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobBinaryStream (this, x, length));
      break;
    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * When a very large UNICODE value is input to a LONGVARCHAR
   * parameter, it may be more practical to send it via a
   * java.io.InputStream. JDBC will read the data from the stream
   * as needed, until it reaches end-of-file.  The JDBC driver will
   * do any necessary conversion from UNICODE to the database char format.
   *
   * <P><B>Note:</B> This stream object can either be a standard
   * Java stream object or your own subclass that implements the
   * standard interface.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the java input stream which contains the UNICODE parameter value
   * @param length the number of bytes in the stream
   * @throws java.sql.SQLException if a database access error occurs.
   * @deprecated To be deprecated in InterClient 2, replaced by
   * {@link #setCharacterStream setCharacterStream(parameterIndex, reader, length)} in JDBC 2
   * @see #setCharacterStream
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setUnicodeStream (int parameterIndex,
					     java.io.InputStream x, 
					     int length) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    if ((length%2) != 0)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__setUnicodeStream_odd_bytes__);

    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = getStringFromUnicodeInputStream (x, length);
      break;
    case IBTypes.CLOB__:
    case IBTypes.BLOB__:
      // MMM - Cannot set an unicode stream for array parameter
      // case IBTypes.ARRAY__:
      // MMM - end
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobUnicodeStream (this, x, length));
      break;

    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * When a very large binary value is input to a LONGVARBINARY
   * parameter, it may be more practical to send it via a
   * java.io.InputStream. JDBC will read the data from the stream
   * as needed, until it reaches end-of-file.
   *
   * <P><B>Note:</B> This stream object can either be a standard
   * Java stream object or your own subclass that implements the
   * standard interface.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the java input stream which contains the binary parameter value
   * @param length the number of bytes in the stream
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void setBinaryStream (int parameterIndex,
					    java.io.InputStream x,
					    int length) throws java.sql.SQLException
  {
    checkForClosedStatement ();
    int ibType = getParameterIBType (parameterIndex);

    switch (ibType) {
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      inputs_[parameterIndex-1] = getBytesFromInputStream (x, length);
      break;
    case IBTypes.CLOB__:
    case IBTypes.BLOB__:
    // MMM - Cannot set an binary stream for array parameter
    // case IBTypes.ARRAY__:
    // MMM - end
      inputs_[parameterIndex-1] = new Integer (jdbcNet_.setBlobBinaryStream (this, x, length));
      break;

    default:
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
    }
  }

  /**
   * In general, parameter values remain in force for repeated use of a
   * Statement. Setting a parameter value automatically clears its
   * previous value.  However, in some cases it is useful to immediately
   * release the resources used by the current parameter values; this can
   * be done by calling clearParameters.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public void clearParameters () throws java.sql.SQLException
  {
    checkForClosedStatement ();

    for (int i = 0; i < inputCols_; i++)
      inputs_[i] = null;
  }

  //---------------------Advanced features--------------------------

  /**
   * Set the value of a parameter using an object; use the
   * java.lang equivalent objects for integral values.
   *
   * <p>The given Java object will be converted to the targetSqlType
   * before being sent to the database.
   *
   * <p><b>JDBC 2 note:</b>
   * <p>Note that this method may be used to pass database
   * specific abstract data types.
   *
   * <p>
   * If the object is of a class implementing SQLData,
   * the JDBC driver should call its method writeSQL() to write it
   * to the SQL data stream,
   * else
   * if the object is of a class implementing Ref, Blob, Clob, Struct,
   * or Array then pass it to the database as a value of the
   * corresponding SQL type.
   *
   * @param parameterIndex The first parameter is 1, the second is 2, ...
   * @param x The object containing the input parameter value
   * @param targetSqlType The SQL type (as defined in java.sql.Types) to be
   *     sent to the database. The scale argument may further qualify this type.
   * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
   *     this is the number of digits after the decimal.  For all other
   *     types this value will be ignored,
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
   **/
  synchronized public void setObject (int parameterIndex,
				      Object x, 
				      int targetSQLType, 
				      int scale) throws java.sql.SQLException
  {
    setObject (parameterIndex, x);
  }

  /**
   * This method is like setObject above, but assumes a scale of zero.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
   **/
  synchronized public void setObject (int parameterIndex,
				      Object x,
				      int targetSQLType) throws java.sql.SQLException
  {
    setObject (parameterIndex, x);
  }

  // !!! put comments here wrt using setObject on streams
  /**
   * <p>Set the value of a parameter using an object; use the
   * java.lang equivalent objects for integral values.
   *
   * <p>The JDBC specification specifies a standard mapping from
   * Java Object types to SQL types.  The given argument java object
   * will be converted to the corresponding SQL type before being
   * sent to the database.
   *
   * <p>Note that this method may be used to pass datatabase
   * specific abstract data types, by using a Driver specific Java
   * type.
   *
   * <p><b>JDBC 2 note:</b>
   * <p>If the object is of a class implementing SQLData,
   * the JDBC driver should call its method writeSQL() to write it
   * to the SQL data stream,
   * else
   * if the object is of a class implementing Ref, Blob, Clob, Struct,
   * or Array then pass it to the database as a value of the
   * corresponding SQL type.
   *
   * <p>Raise an exception if there is an ambiguity, for example, if the
   * object is of a class implementing more than one of those interfaces.
   *
   * @param parameterIndex The first parameter is 1, the second is 2, ...
   * @param x The object containing the input parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
   **/
  synchronized public void setObject (int parameterIndex, Object x) throws java.sql.SQLException
  {
    if (x instanceof Boolean)
      setBoolean (parameterIndex, ((Boolean) x).booleanValue ());
    else if (x instanceof Byte)
      setByte (parameterIndex, ((Byte) x).byteValue ());
    else if (x instanceof Short)
      setShort (parameterIndex, ((Short) x).shortValue());
    else if (x instanceof Integer)
      setInt (parameterIndex, ((Integer) x).intValue ());
    else if (x instanceof Long)
      setLong (parameterIndex, ((Long) x).longValue ());
    else if (x instanceof Double)
      setDouble (parameterIndex, ((Double) x).doubleValue ());
    else if (x instanceof Float)
      setFloat (parameterIndex, ((Float) x).floatValue ());
    else if (x instanceof java.math.BigDecimal)
      setBigDecimal (parameterIndex, (java.math.BigDecimal) x);
    else if (x instanceof String)
      setString (parameterIndex, (String) x);
    // MMM - added second part of condition
    else if ((x instanceof byte[]) &&
             (getParameterIBType (parameterIndex) != IBTypes.ARRAY__))
    // MMM - end
      setBytes (parameterIndex, (byte[]) x);
    else if (x instanceof java.sql.Date)
      setDate (parameterIndex, (java.sql.Date) x);
    else if (x instanceof java.sql.Time)
      setTime (parameterIndex, (java.sql.Time) x);
    else if (x instanceof java.sql.Timestamp)
      setTimestamp (parameterIndex, (java.sql.Timestamp) x);
    // MMM - added for array support
    else if (x instanceof java.sql.Array) 
      setArray (parameterIndex, (java.sql.Array) x);
    else if (x.getClass().isArray())
      setJavaArray (parameterIndex, x); 
    // MMM - end
    else if (x instanceof java.io.InputStream)
      // !!! setBinaryStream accodates JBuilder dataset bug
      // setBinaryStream (parameterIndex, x); // we need an extension with no length indicator
      // !!! The JDBC 1 Specification dictates the following...
      throw new ParameterConversionException (ErrorKey.parameterConversion__set_object_on_stream__);
    else
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__);
  }

  /**
   * Some prepared statements return multiple results; the execute
   * method handles these complex statements as well as the simpler
   * form of statements handled by executeQuery and executeUpdate.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @see Statement#execute
   * @since <font color=red>JDBC 1</font>
   **/
  synchronized public boolean execute () throws java.sql.SQLException
  {
    checkForClosedStatement ();

    clearWarnings ();

    if (!allNonNullablesAreSet ())
      throw new InvalidOperationException (ErrorKey.invalidOperation__parameter_not_set__);

    if (resultSet_ != null) {
      resultSet_.cursorName_ = cursorName_;
      if (resultSet_.openOnServer_)
        resultSet_.local_Close ();
    }

    updateCountStack_ = null;
    connection_.transactionStartedOnClient_ = true;

    remote_EXECUTE_PREPARED_STATEMENT ();

    connection_.transactionStartedOnServer_ = true;
    resultSetStack_ = resultSet_;

    return (resultSet_ != null);
  }

  private void remote_EXECUTE_PREPARED_STATEMENT () throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.EXECUTE_PREPARED_STATEMENT__);
    send_PreparedStatementExecuteData (sendMsg);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
	throw recvMsg.get_EXCEPTIONS ();
      }

      if (!recvMsg.getHeaderEndOfStream ())
	remote_sendPrefetch ();

      if (resultSet_ == null) {  // update statement
        updateCountStack_ = new Integer (recvMsg.readInt ());
        setWarning (recvMsg.get_WARNINGS ());
        jdbcNet_.destroyRecvMessage (recvMsg);
      }
      else { // query statement
        resultSet_.local_open ();
        resultSet_.numRows_ = recvMsg.readInt ();
        // ResultData and updateCounts remain in recvMsg associated
        // with resultSet awaiting subsequent ResultSet.next() and get*() calls.
        resultSet_.setRecvBuffer (recvMsg);
      }
    }
    catch (java.sql.SQLException e) {
      if (resultSet_ != null)
        resultSet_.local_Close ();
      updateCountStack_ = null;
      jdbcNet_.destroyRecvMessage (recvMsg);
      throw e;
    }
  }

  private void send_PreparedStatementExecuteData (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    sendMsg.writeInt (statementRef_);
    sendMsg.writeLDSQLText (cursorName_);
    connection_.send_TransactionConfigData (sendMsg);
    sendMsg.writeShort (timeout_);
    sendMsg.writeShort (maxFieldSize_);
    sendMsg.writeInt (fetchSize_);
    for (int i=0; i < inputCols_; i++) {
      send_Data (i, sendMsg);
    }
  }

  private void send_Data (int inputCol, MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    if (inputNullables_[inputCol]) {
      boolean isNull = (inputs_[inputCol] == null);
      sendMsg.writeBoolean (isNull);
      if (isNull) return;
    }

    switch (inputTypes_[inputCol]) {
    case IBTypes.SMALLINT__: 
      sendMsg.writeShort (((Short) inputs_[inputCol]).intValue());
      break;
    case IBTypes.INTEGER__:
      sendMsg.writeInt (((Integer) inputs_[inputCol]).intValue());
      break;
    case IBTypes.FLOAT__:
      sendMsg.writeFloat (((Float) inputs_[inputCol]).floatValue());
      break;
    case IBTypes.DOUBLE__:
      sendMsg.writeDouble (((Double) inputs_[inputCol]).doubleValue());
      break;
    case IBTypes.NUMERIC_DOUBLE__:
      sendMsg.writeDouble (((Double) inputs_[inputCol]).doubleValue());
      sendMsg.writeByte ((inputScales_[inputCol])); // Actually, this is never changed anymore.
      break;
    case IBTypes.NUMERIC_INTEGER__:
      sendMsg.writeInt (((Integer) inputs_[inputCol]).intValue());
      sendMsg.writeByte ((inputScales_[inputCol])); // Actually, this is never changed anymore.
      break;
    case IBTypes.NUMERIC_SMALLINT__:
      sendMsg.writeShort (((Short) inputs_[inputCol]).intValue());
      sendMsg.writeByte ((inputScales_[inputCol])); // Actually, this is never changed anymore.
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      if (inputs_[inputCol] instanceof String)
	sendMsg.writeLDChars ((String) inputs_[inputCol], cbuf_, encodingBuf_);
      else
	sendMsg.writeLDBytes ((byte[]) inputs_[inputCol]);
      break;
// CJL-IB6 support for new types
    case IBTypes.DECIMAL_INT64__:
    case IBTypes.NUMERIC_INT64__:
      sendMsg.writeLong (((Long) inputs_[inputCol]).longValue());
      sendMsg.writeByte ((inputScales_[inputCol])); // Actually, this is never changed anymore.
      break;
    case IBTypes.DECIMAL_INTEGER__:
      sendMsg.writeInt (((Integer) inputs_[inputCol]).intValue());
      sendMsg.writeByte ((inputScales_[inputCol])); // Actually, this is never changed anymore.
      break;
    case IBTypes.SQLDATE__:
      sendMsg.writeInt (((IBTimestamp) inputs_[inputCol]).encodedYearMonthDay_);
      break;
    case IBTypes.TIME__:
      sendMsg.writeInt (((IBTimestamp) inputs_[inputCol]).encodedHourMinuteSecond_);
      break;
// CJL-IB6 end change
    case IBTypes.DATE__:
      sendMsg.writeInt (((IBTimestamp) inputs_[inputCol]).encodedYearMonthDay_);
      sendMsg.writeInt (((IBTimestamp) inputs_[inputCol]).encodedHourMinuteSecond_);
      break;
    case IBTypes.ARRAY__:
      // MMM - send array ID and data
      ((Array) inputs_[inputCol]).send (sendMsg);
      break;
      // MMM - end
    case IBTypes.CLOB__:
    case IBTypes.BLOB__:
      int blobRef =  ((Integer) inputs_[inputCol]).intValue();
      sendMsg.writeInt (blobRef);
      break;
    default:
      throw new BugCheckException (ErrorKey.bugCheck__0__, 113);
    }
  }

  // MMM - added method remote_GET_ARRAY_DESCRIPTOR()
  // This method is only necessary when IB < 6
  private void remote_GET_ARRAY_DESCRIPTOR (int column,
                                            String tableName,
                                            String columnName) throws java.sql.SQLException
  {
    MessageBufferOutputStream sendMsg = jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.GET_ARRAY_DESCRIPTOR__);
    connection_.send_TransactionConfigData (sendMsg);
    sendMsg.writeLDChars (tableName);
    sendMsg.writeLDChars (columnName);

    RecvMessage recvMsg = null;
    try {
      recvMsg = jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
        throw recvMsg.get_EXCEPTIONS ();
      }
      if (recvMsg.readBoolean ())
        arrayDescriptors_[column] = new ArrayDescriptor(recvMsg);

      setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      jdbcNet_.destroyRecvMessage (recvMsg);
    }
  }
  // MMM - end

  //--------------------------JDBC 2.0-----------------------------

  /**
   * Add a set of parameters to the batch.
   *
   * @throws java.sql.SQLException if a database access error occurs.
   * @see Statement#addBatch
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void addBatch () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * When a very large UNICODE value is input to a LONGVARCHAR
   * parameter, it may be more practical to send it via a
   * java.io.Reader. JDBC will read the data from the stream
   * as needed, until it reaches end-of-file.  The JDBC driver will
   * do any necessary conversion from UNICODE to the database char format.
   *
   * <P><B>Note:</B> This stream object can either be a standard
   * Java stream object or your own subclass that implements the
   * standard interface.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the java reader which contains the UNICODE data
   * @param length the number of characters in the stream
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setCharacterStream (int parameterIndex,
       			                       java.io.Reader reader,
			                       int length) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Set a REF(&lt;structured-type&gt;) parameter.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @param x an object representing data of an SQL REF Type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setRef (int i, java.sql.Ref x) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Set a BLOB parameter.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @param x an object representing a BLOB
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setBlob (int i, java.sql.Blob x) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Set a CLOB parameter.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @param x an object representing a CLOB
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setClob (int i, java.sql.Clob x) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  // MMM - added setArray() method 
  /**
   * Sets an Array parameter.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x an object representing an SQL array
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, Proposed for InterClient 3.0</font>
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void setArray (int parameterIndex,
                                     java.sql.Array x) throws java.sql.SQLException 
  { 
    setJavaArray (parameterIndex, x.getArray ()); 
  } 
  // MMM - end

  // MMM - added private setJavaArray() method 
  private synchronized void setJavaArray (int parameterIndex, 
                                          Object javaArray) throws java.sql.SQLException 
  { 
    checkForClosedStatement (); 
    int ibType = getParameterIBType (parameterIndex); 

    if (ibType == IBTypes.ARRAY__) { 
      // !!!INSQLDA_NONAMES - need the null check for IB < 6 
      if (arrayDescriptors_[parameterIndex-1] != null) { 
        Array newArrayObject = new Array (javaArray); 
        newArrayObject.setDescriptor (arrayDescriptors_[parameterIndex-1]); 
        inputs_[parameterIndex-1] = newArrayObject; 
      } 
      else 
        throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__); 
    } 
    else 
      throw new ParameterConversionException (ErrorKey.parameterConversion__type_conversion__); 
  } 
  // MMM - end 

  /**
   * The number, types and properties of a ResultSet's columns
   * are provided by the getMetaData method.
   *
   * <p><b>InterClient note:</b>
   * InterClient provided the capability to get result set metadata
   * for a prepared statement before the advent of JDBC 2 using
   * {@link #getResultSetMetaData() getResultSetMetaData()}.
   * This provides a means to access result set metadata before the prepared statement
   * is executed (<code>SQL DESCRIBE</code> functionality).
   * Unfortunately, <code>SQL DESCRIBE INPUT</code> functionality does not exist
   * in JDBC 2, but does exist in InterClient using
   * {@link #getParameterMetaData() getParameterMetaData()}.
   *
   * @return the description of a ResultSet's columns
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
   * @see #getParameterMetaData
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public java.sql.ResultSetMetaData getMetaData () throws java.sql.SQLException
  {
    checkForClosedStatement ();
    return resultSet_.getMetaData ();
  }

  /**
   * Sets a parameter to a {@link java.sql.Date java.sql.Date} value.
   * The driver converts this to a SQL DATE value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setDate (int parameterIndex,
                                    java.sql.Date x,
                                    java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Sets a parameter to a {@link java.sql.Time java.sql.Time} value.
   * The driver converts this to a SQL TIME value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setTime (int parameterIndex,
                                    java.sql.Time x,
                                    java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Sets a parameter to a {@link java.sql.Timestamp java.sql.Timestamp} value.
   * The driver converts this to a SQL TIMESTAMP value when it sends it to the database.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setTimestamp (int parameterIndex,
                                         java.sql.Timestamp x,
                                         java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Sets the designated parameter to SQL NULL.  This version of setNull should
   * be used for user-named types and REF type parameters.  Examples
   * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
   * named array types.
   *
   * <P><B>Note:</B> To be portable, applications must give the
   * SQL type code and the fully-qualified SQL type name when specifying
   * a NULL user-defined or REF parameter.  In the case of a user-named type
   * the name is the type name of the parameter itself.  For a REF
   * parameter the name is the type name of the referenced type.  If
   * a JDBC driver does not need the type code or type name information,
   * it may ignore it.
   *
   * Although it is intended for user-named and Ref parameters,
   * this method may be used to set a null parameter of any JDBC type.
   * If the parameter does not have a user-named or REF type, the given
   * typeName is ignored.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param sqlType a value from java.sql.Types
   * @param typeName the fully-qualified name of an SQL user-named type,
   *   ignored if the parameter is not a user-named type or REF
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/ //*start jre12*
  synchronized public void setNull (int paramIndex, int sqlType, String typeName) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  // --------------------InterClient Extensions -----------------------

  /**
   * Get meta data describing the parameters to this prepared statement.
   * This represents <code>SQL DESCRIBE INPUT</code> functionality.
   * <p>
   * ParameterMetaData and this method were proposed for inclusion in JDBC 2.0,
   * but not accepted due to lack of underlying support by some database vendors.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs.
   **/
  synchronized public ParameterMetaData getParameterMetaData () throws java.sql.SQLException
  {
    checkForClosedStatement ();

    if (parameterMetaData_ == null)
      parameterMetaData_ = new ParameterMetaData (this);
    return parameterMetaData_;
  }

  /**
   * Get the result set metadata for this prepared statement.
   * <p>
   * This method has been deprecated since this functionality is now
   * provided by the JDBC 2 method PreparedStatement.getMetaData().
   *
   * @see #getMetaData
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @deprecated To be deprecated in InterClient 2, replaced by {@link #getMetaData getMetaData()} in JDBC 2.
   **/
  synchronized public java.sql.ResultSetMetaData getResultSetMetaData () throws java.sql.SQLException
  {
    return getMetaData ();
  }

  // !!!MMM - added this ugly extension method
  // !!!INSQLDA_NONAMES - because no array descriptor in IBs < 6 is
  // returned by Connection.prepareStatement for input array parameters,
  // the only way to write an array to InterBase is to make a user to
  // provide array's column name and table name.
  /**
   * "Prepares" an array to get the necessary metadata for an array parameter.
   * <p>
   * In versions of InterBase before v6, internal array descriptors
   * do not contain the proper values for the input array parameters
   * of a prepared statement.
   * This method acts as a workaround required only for pre-v6 versions of InterBase.
   * This call should only be used with versions of InterBase that do not
   * support table and column names for input array parameters.
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @param parameterIndex The first parameter is 1, the second is 2, ...
   * @param tableName name of a table where an array column is
   * @param columnName name of a column of an ARRAY data type
   * @throws java.sql.SQLException if a database access error occurs.
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public void prepareArray (int parameterIndex,
                                         String tableName,
                                         String columnName) throws java.sql.SQLException
  {
    if (getParameterIBType (parameterIndex) != IBTypes.ARRAY__)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__not_array_parameter__);

    // if the descriptor has already been received, do nothing
    if (arrayDescriptors_[parameterIndex-1] != null)
      return;

    checkForClosedStatement ();
    clearWarnings ();
    connection_.transactionStartedOnClient_ = true;
    remote_GET_ARRAY_DESCRIPTOR (parameterIndex-1, tableName, columnName);
    connection_.transactionStartedOnServer_ = true;
  }
  //*end jre12*
  // MMM - end
}

