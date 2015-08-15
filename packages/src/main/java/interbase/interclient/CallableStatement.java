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

// The java.sql.CallableStatement interface presented two alternatives
// for an InterBase/InterClient implementation.  The values of an interbase
// stored procedure could be interpreted either as a "result set" or as "out
// parameters".  However, the ib implementation for result sets
// and out parameters is the same for both cases, so an interbase stored procedure
// can not return both a result set and out parameters separately at the same time.
// So, we had a choice, should we interprete the outputs of an
// interbase stored procedure as a result set or as out parameters.
// Now, if interbase was to add internal support for out parameters
// (distinct from a result set) so that a stored procedure could return
// a result set and orthogonal out parameters at the same time,
// then it would be good foresight for InterClient to adopt the "result set
// interpretation" currently, and later add out parameters as they are
// surfaced by the ib engine.
// On the other hand, if interbase was to never add internal support for out
// parameters, then it would be best for InterClient to adopt
// the "out parameter interpretation" of "execute procedure foo";
// afterall, if a result set is wanted, you can simply use "select * from foo".
// The drawback to this togglable interpretation is that result sets and out parameters
// are NOT orthogonal (they are the same data), so if distinct out parameter support was added
// to ib in the future, existing apps using the Callable Statement
// methods would behave very differently when the model changed.
// The advantage to the togglable interpretation is marketing;
// it would appear that we can have our cake and eat it to -
// "execute procedure foo" could be invoked from a CallableStatement and return out parameters,
// whereas  "select * from foo" could be invoked from a regular Statement and
// return a result set.  Although the data is the same, we can now "implement" all the CallableStatement
// methods dealing with out parameters; whereas in the stricter "result set only" interpretation,
// all these callable statement methods are defunct (in deferrence to the result set methods).

// So, after a lot of thought, we opted for the strict "result set only" interpretation.
// We are fortunate that InterBase supports generalized result sets from a stored
// procedure since I believe most vendors only support out parameters
// (but I'm not sure about this assertion).
// We're aware that DataGateway adopted the "togglable interpretation",
// but for the reasons outlined above, this may cause migration problems in the future.

// There are several methods for handling interbase stored procedures
// within the context of jdbc. None of these methods is "more correct" 
// than the other, rather its a matter of choosing which is most esthetically 
// pleasing, and which would customers most prefer. 
// The ODBC and CLI standards describe 5 different kinds of stored procedure 
// parameters - result sets, IN parameters, OUT parameters, INOUT parameters, 
// and a return value. 
// InterBase supports IN parameters. 
// InterBase does NOT support a return value. 
// InterBase does NOT support INOUT parameters. 
// In general, InterBase supports result sets, but a developer can 
// choose to interprete the output from an interbase stored procedure call 
// in either way, either as a result set or as OUT parameters. 
// The basic axiom to understand is that InterBase cannot support both 
// result sets and OUT parameters simultaneously. 
// InterBase returns "OUT parameters" in the same memory structure 
// as as result set, ie. the same output sqlda structure. 
// In fact, interbase itself does not know about OUT parameters. 
// There is no underlying implementation for OUT parameters, 
// rather only a syntactic construct in gpre. 
//
//   // returns a result set with two columns 
//   prepareCall ("select * from PROC_CALL (?)"); 
// OR 
//   // logically returns out parameters, but actually returns a result set with one row 
//   prepareCall ("execute procedure PROC_CALL (?) returning_values (?, ?)"); 
// 
// But wait, returning_values is a GPRE construct and is not understood by 
// dsql at all. So we must write 
// 
//   prepareCall ("execute procedure PROC_CALL (?)"); 
// 
// But wait again, the standard escape syntax is PROC_CALL (?, ?, ?)
//  where the last two parameters are out parameters! 
// 
// Now this is a problem, because when the user inputs PROC_CALL (?, ?, ?)
// we need to send "execute procedure PROC_CALL (?)" to the server to prepare. 
// But the client doesn't know how many parameters PROC_CALL takes until after its prepared. 
// This means InterServer would need to do a system table lookup on the server 
// before preparing so that it could determine the number of input parameters. 
// Then InterServer would need to parse the PROC_CALL (?, ?, ?) 
// to strip off the out parameters (since they're actually returned in a result set). 
// The parse is not completely trivial because the inputs themselves can contain anything. 
// All this extra processing is necessary only so that we can strip off 
// the out parameters since interbase procedures don't take out parameters. 

// Ok, now that that's solved ;-/ we must return the 
// isc_info_sql statement type back to the client 
// after preparing to distinguish betwen select procedure calls and 
// exec procedure calls, in the later case keep the result set private 
// and only return its values on calls to get out parameters, in the former case 
// return the result set and don't return any out parameters. 
// This means changing the remote protocol for PREPARE_STATEMENT, 
// EXECUTE_STATEMENT, EXECUTE_QUERY_STATEMENT, AND EXECUTE_UPDATE_STATEMENT, 
// and introducing 2 new opcodes to the remote protocol 
//  EXECUTE_CALLABLE_STATEMENT, and PREPARE_AND_EXECUTE_CALLABLE_STATEMENT 
// I don't mind modifying the remote protocol as such, but there is now 
// going to be a lot of special case code in PreparedStatement to handle 
// the case where we want to hide the result set from the outside world 
// and only expose it thru out parameters. 

// From an implementors point of view, the much cleaner solution is 
// to always return a result set and this satisfies the JDBC requirements. 
// However, a JDBC programmer wants to use the standard notation 
//   { call PROC_CALL (?, ?, ?) } with IN and OUT parameters. 
// On the other hand, the interbase syntactic support for OUT parameters is not supported 
// in dsql so maybe its best to just be honest 
// and say that interbase cannot support out parameters rather than trying to fake 
// it as we do with gpre.  We still support the escape syntax, but 
// there there are no out parameters, so that we have
//  cs = c.prepareCall ("{ call PROC_CALL (?) }");
//  rs = cs.executeQuery ();

// Level 1 InterBase support - extend dsql so that IB prepare can handle
//   "call foo (?, ?)" WITH out parameters.
// note 1: notice that we must introduce a new form of ib procedure call ("call") and
//            cannot reuse "execute procedure foo" since this would break 
//            existing interbase dsql apps.
// note2: do not use returning_values (?, ?) syntax, rather inlude out parameters
//           in the parameter list!
// note3: the out parameters themselves could be returned in the output sqlda,
//           but this prevents the stored procedure to return a result set AND out parameters.
//           level 2 support would require a separate memory area for out parameters.
// note 4: interbase system tables would need to be extended
// note 5: when level 1 support is provided, the interclient callable statement
//           semantics will change so as to ONLY return out parameters and never
//           return a result set (the opposite of current semantics) 
// note 6: We could get around this upward incompatibility 
//           with a lot of extra work in interserver, but not before this release.
//           The work logically belongs in dsql, not interserver.

// Level 2 InterBase support - Level 1 + allow
//   "call foo (?, ?)" WITH out parameters to return a result set.
//   this means the out parameters cannot be returned in the output sqlda

//  InterBase may, sometime in the future,
//  extend its native support for stored procedures to include
//  out parameters in its native dsql syntax.
//  At such time,
//  the InterClient semantics for {call PROC_CALL (?,...)}
//  would be modified such that OUT parameters would appear in the
//  parameter list and a result set may no longer be returned.

// IB will have to add level 1 support if we are to be compliant
// because {call foo} has no native sql.

/**
 * Represents an SQL stored procedure.
 * 
 * <P>JDBC provides a stored procedure SQL escape that allows stored
 * procedures to be called in a standard way for all RDBMS's. This
 * escape syntax has one form that includes a result parameter and one
 * that does not. If used, the result parameter must be registered as
 * an OUT parameter. The other parameters may be used for input,
 * output or both. Parameters are refered to sequentially, by
 * number. The first parameter is 1.
 *
 * <P><CODE>
 * {?= call procedure-name [(arg1, arg2, ...)]}<BR>
 * {call procedure-name [(arg1, arg2, ...)]}
 * </CODE>
 *
 * <P>IN parameter values are set using the set methods inherited from
 * PreparedStatement. The type of all OUT parameters must be
 * registered prior to executing the stored procedure; their values
 * are retrieved after execution via the get methods provided here.
 *
 * <P>A Callable statement may return a ResultSet or multiple
 * ResultSets. Multiple ResultSets are handled using operations
 * inherited from Statement.
 *
 * <P>For maximum portability, a call's ResultSets and update counts
 * should be processed prior to getting the values of output
 * parameters.

 * <P>A call's ResultSets and update counts
 * should be processed prior to getting the values of output
 * parameters.
 *
 * <p>
 * <b>InterClient notes:</b>
 * <p>
 * InterBase does not distinguish between stored procedures which
 * return result sets and those which return "out" parameters.
 * InterBase embedded SQL (supported by the preprocessor gpre)
 * allows for a syntax in which the singleton result set columns from a 
 * procedure call may be interpreted as out parameters if the procedure
 * is called using "execute procedure PROC_CALL" rather than "select * from PROC_CALL".
 * But the procedure definition itself is not distinguished as either
 * a "select procedure" or an "executable procedure", it is up to the caller
 * to choose.
 * <p>
 * Because of this dual semantics for a procedure call, 
 * we had to choose which behavior to bestow on the escape syntax
 * {call PROC_CALL (...)}.  In order to support InterBase procedures in their
 * generality and to conform to JDBC standards, 
 * InterBase stored procedures return all outputs
 * in a result set, never as OUT parameters.  
 * So effectively, a CallableStatement is
 * executed in the same way as a PreparedStatement for InterBase.
 * <p>
 * The following example demonstrates how to prepare and execute an
 * InterBase stored procedure call which returns multiple rows.
 * <pre>
 * CallableStatement cs = c.prepareCall ("select * from PROC_CALL (?, ...)");
 * ResultSet rs = cs.executeQuery ();
 * </pre>
 * If a stored procedure call is known to return only a single row
 * or no rows at all, then it should be prepared as follows
 * <pre>
 * CallableStatement cs = c.prepareCall ("execute procedure PROC_CALL (?, ...)");
 * cs.execute ();  // may or may not spawn a singleton result set
 * ResultSet rs = cs.getResultSet ();
 * </pre>
 * Finally, the generalized escape syntax
 * <pre>
 * CallableStatement cs = c.prepareCall ("{call PROC_CALL (?, ...)}");
 * cs.execute ();  // may or may not spawn a result set
 * ResultSet rs = cs.getResultSet ();
 * </pre>
 * is a standard notation which may be used for both kinds
 * of procedures, that is, whether or not results are returned.
 * If results are returned, they are returned as a result set.
 * <p>
 * In summary, InterBase does not support out parameters <i>distinct</i> from
 * a result set.  Although InterClient could interprete a singleton result set as
 * out parameters as done by gpre, it is the intention of the SQL standard that result
 * sets and out parameters be distinct entities, so that a stored procedure
 * could return both a result set and out parameters independently.
 *
 * @see Connection#prepareCall(java.lang.String)
 * @see ResultSet
 * @author Paul Ostler
 * @docauthor Paul Ostler
 * @since <font color=red>JDBC 1, with extended behavior in JDBC 2</font>
 **/
final public class CallableStatement extends PreparedStatement implements java.sql.CallableStatement
{
  private Object outputs_[];

  CallableStatement (JDBCNet jdbcNet, 
		     Connection connection, 
		     String sql) throws java.sql.SQLException
  {
    super (jdbcNet, connection, sql);
  }
 
  /**
   * Register an out parameter to a stored procedure.
   * <p>
   * Before executing a stored procedure call, you must explicitly
   * call registerOutParameter to register the java.sql.Type of each
   * out parameter.
   *
   * <P><B>Note:</B> When reading the value of an out parameter, you
   * must use the getXXX method whose Java type XXX corresponds to the
   * parameter's registered SQL type.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2,...
   * @param sqlType SQL type code defined by java.sql.Types;
   *   for parameters of type Numeric or Decimal use the version of
   *   registerOutParameter that accepts a scale value.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   */
  synchronized public void registerOutParameter (int parameterIndex,
				                 int sqlType) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Register a Numeric or Decimal out parameter to a stored procedure.
   * <p>
   * Use this version of registerOutParameter for registering
   * Numeric or Decimal out parameters.
   *
   * <P><B>Note:</B> When reading the value of an out parameter, you
   * must use the getXXX method whose Java type XXX corresponds to the
   * parameter's registered SQL type.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param sqlType use either java.sql.Type.NUMERIC or java.sql.Type.DECIMAL
   * @param scale a value greater than or equal to zero representing the 
   *              desired number of digits to the right of the decimal point.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public void registerOutParameter (int parameterIndex,
				                 int sqlType,
				                 int scale) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * An OUT parameter may have the value of SQL NULL; wasNull reports
   * whether the last value read has this special value.
   *
   * <P><B>Note:</B> You must first call getXXX on a parameter to
   * read its value and then call wasNull() to see if the value was
   * SQL NULL.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @return true if the last parameter read was SQL NULL
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public boolean wasNull () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a CHAR, VARCHAR, or LONGVARCHAR parameter as a Java String.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public String getString (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a BIT parameter as a Java boolean.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is false.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public boolean getBoolean (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a TINYINT parameter as a Java byte.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public byte getByte (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a SMALLINT parameter as a Java short.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public short getShort (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of an INTEGER parameter as a Java int.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public int getInt (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a BIGINT parameter as a Java long.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public long getLong (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a FLOAT parameter as a Java float.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public float getFloat (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a DOUBLE parameter as a Java double.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is 0.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public double getDouble (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a NUMERIC parameter as a java.math.BigDecimal object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @param scale a value greater than or equal to zero representing the
   *   desired number of digits to the right of the decimal point.
   * @return the parameter value; if the value is SQL NULL, the result is null.
   * @throws java.sql.SQLException if a database access error occurs.
   * @deprecated To be deprecated in InterClient 2, replaced by
   * {@link #getBigDecimal(int) getBigDecimal(parameterIndex)} without scale in JDBC 2
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public java.math.BigDecimal getBigDecimal (int parameterIndex, int scale) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a SQL BINARY or VARBINARY parameter as a Java byte[].
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public byte[] getBytes (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a SQL DATE parameter as a java.sql.Date object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public java.sql.Date getDate (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a SQL TIME parameter as a java.sql.Time object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   **/
  synchronized public java.sql.Time getTime (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a SQL TIMESTAMP parameter as a java.sql.Timestamp object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, not supported</font>
   */
  synchronized public java.sql.Timestamp getTimestamp (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  /**
   * Get the value of a parameter as a Java object.
   *
   * <p>This method returns a Java object whose type coresponds to the SQL
   * type that was registered for this parameter using registerOutParameter.
   *
   * <p>Note that this method may be used to read
   * datatabase-specific, abstract data types. This is done by
   * specifying a targetSqlType of java.sql.types.OTHER, which
   * allows the driver to return a database-specific Java type.
   *
   * <p><b>JDBC 2 note:</b>
   * The behavior of method getObject() is extended to materialize data
   * of SQL user-defined types.  When the OUT parameter is a UDT
   * value, the behavior of this method is as if it were a call to:
   * <code>getObject(parameterIndex, this.getConnection().getTypeMap())</code>
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex The first parameter is 1, the second is 2, ...
   * @return A java.lang.Object holding the OUT parameter value.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 1, with extended behavior in JDBC 2, not supported</font>
   **/
  synchronized public Object getObject (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__out_parameters__);
  }

  // ------------------- JDBC 2 --------------------------------------------
  
  /**
   * Get the value of a NUMERIC parameter as a java.math.BigDecimal object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value (full precision); if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.math.BigDecimal getBigDecimal (int parameterIndex) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Returns an object representing the value of OUT parameter <code>i</code>.
   * Use the <code>map</code> to determine the class from which to construct
   * data of SQL structured and distinct types.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @param map the mapping from SQL type names to Java classes
   * @return a java.lang.Object holding the OUT parameter value.
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public Object getObject (int i, java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get a REF(&lt;structured-type&gt;) OUT parameter.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @return an object representing data of an SQL REF Type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Ref getRef (int i) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get a BLOB OUT parameter.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @return an object representing a BLOB
   * @throws java.sql.SQLException if a database-access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Blob getBlob (int i) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get a CLOB OUT parameter.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @return an object representing a CLOB
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Clob getClob (int i) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get an Array OUT parameter.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param i the first parameter is 1, the second is 2, ...
   * @return an object representing an SQL array
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Array getArray (int i) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get the value of an SQL DATE parameter as a java.sql.Date object
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Date getDate (int parameterIndex,
                                             java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get the value of an SQL TIME parameter as a java.sql.Time object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Time getTime (int parameterIndex,
                                             java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Get the value of a SQL TIMESTAMP parameter as a java.sql.Timestamp object.
   *
   * <p><b>InterClient note:</b>
   * Throws a DriverNotCapableException.
   * InterBase does not support out parameters distinct from a result set.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @return the parameter value; if the value is SQL NULL, the result is null
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public java.sql.Timestamp getTimestamp (int parameterIndex,
                                                       java.util.Calendar cal) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*

  /**
   * Registers the designated output parameter.  This version of
   * the method <code>registerOutParameter</code>
   * should be used for a user-named or REF output parameter.  Examples
   * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
   * named array types.
   * <p>
   * Before executing a stored procedure call, you must explicitly
   * call <code>registerOutParameter</code> to register the type from
   * <code>java.sql.Types</code> for each
   * OUT parameter.  For a user-named parameter the fully-qualified SQL
   * type name of the parameter should also be given, while a REF
   * parameter requires that the fully-qualified type name of the
   * referenced type be given.  A JDBC driver that does not need the
   * type code and type name information may ignore it.   To be portable,
   * however, applications should always provide these values for
   * user-named and REF parameters.
   * <p>
   * Although it is intended for user-named and REF parameters,
   * this method may be used to register a parameter of any JDBC type.
   * If the parameter does not have a user-named or REF type, the
   * typeName parameter is ignored.
   *
   * <P><B>Note:</B> When reading the value of an out parameter, you
   * must use the <code>getXXX</code> method whose Java type XXX corresponds to the
   * parameter's registered SQL type.
   *
   * @param parameterIndex the first parameter is 1, the second is 2,...
   * @param sqlType a value from java.sql.Types
   * @param typeName the fully-qualified name of an SQL structured type
   * @throws java.sql.SQLException if a database access error occurs.
   * @since <font color=red>JDBC 2, not supported</font>
   **/ //*start jre12*
  synchronized public void registerOutParameter (int paramIndex,
                                                 int sqlType,
                                                 String typeName) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }
  //*end jre12*
}
