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
//-*-C++-*-
///////////////////////////////////////////////////////////////////////////////
//
//	There is no limit on the size of Statement results or inputs.
//	The length of the internal result buffer is not fixed but
//	but is calculated by looping thru all described sqlvars
//	and simulating their alignment into a not-yet allocated
//	buffer.  The result buffer is then allocated followed by
//	a second pass of actual data alignment.
//	In this way, the result buffer allocated is exactly what is needed.
//	For queries with unknown inputs and outputs, prepare
//	guesses and allocates memory for 2 input sqlvars and 20
//	output sqlvars.  This is more efficient than
//	allocating sqlvars one at a time.
//	These guesses are user configurable in the prepare calls.
//	More are re-allocated internally if needed.
//
///////////////////////////////////////////////////////////////////////////////
#ifndef _IB_STATEMENT_H_
#define _IB_STATEMENT_H_

#include "Vector.h"

#include "IB_Defines.h"
#include "IB_SQLException.h"
#include "IB_Types.h"
#include "IB_LDString.h"

class IB_Transaction;
class IB_Connection;
class IB_ResultSet;
class IB_InputBuffer;
class IB_Status;
#ifdef SMART_COMPILER
class IB_Blob;
class IB_Array;
#else
#include "IB_Blob.h"
#include "IB_Array.h"
#endif

class IB_Statement {

private:

  // Static constants
  enum {
    sqldaVersion__ = SQLDA_VERSION1,   
    defaultResultCols__ = 20,                // suggested result SQLVAR allocation
    defaultInputCols__ = 2,                  // suggested input SQLVAR allocation
    stmtTypeRequestedInfoSize__ = 8,
    rowCountsRequestedInfoSize__ = 37
  };

  static const char *const CALL_PREFIX_SELECT__;
  static const char *const CALL_PREFIX_EXECUTE__;

  // Information request buffer for statement type.
  static const IB_BUFF_CHAR stmtTypeInfoRequest__[];

  // Information request buffer for row counts.
  static const IB_BUFF_CHAR rowCountsInfoRequest__[];
  
  // Retrieving row counts requires that we query the statement-type
  // using isc_dsql_sql_info.
  // The first byte will contain isc_info_sql_stmt_type,
  // bytes 2 and 3 will contain the length of the integer which follows.
  // The integer which follows is one of isc_info_sql_stmt_* integer values
  // (of whose lengths I'm not sure).
  // Finally, a 1 byte isc_info_end is tacked on at the end.
  // Assuming these integer values are longs (4 bytes) requires a total
  // result buffer size of 1+2+4+1 bytes = 8 = requestedInfoSize__
  IB_BUFF_CHAR stmtTypeRequestedInfo_ [stmtTypeRequestedInfoSize__];

  // The first byte will contain isc_info_sql_records.
  // Followed by a repeating group of triples =
  // {1 byte stmt-type-indicator, 2 byte row count length, row count}
  // This group will contain 4 triples for each stmt-type-count indicator:
  //   isc_info_req_update_count
  //   isc_info_req_delete_count
  //   isc_info_req_insert_count
  //   isc_info_req_select_count
  // Finally, a 1 byte isc_info_end is tacked on at the end.
  // Hence the size of the results buffer is 1+4*(1+2+4)+1 = 30.
  // We'll add 7 in case InterBase someday puts more than these four (37).
  IB_BUFF_CHAR rowCountsRequestedInfo_ [rowCountsRequestedInfoSize__];

  // InterBase client statement handle.
  // Also used as a flag to indicate an allocated statement.
  isc_stmt_handle stmtHandle_;

  // Connection context for this statement.
  // Input connection is shared.
  // Not deleted by destructor.
  friend class IB_Blob;
  friend class IB_Array;
  friend class IB_InputBuffer;
  IB_Connection* connection_;

  // Transaction context for this statement.
  // Input connection is shared.
  // Not deleted by destructor.
  friend class IB_Blob;
  friend class IB_Array;
  IB_Transaction* transaction_;

  // Output result set is shared.
  // Deleted by destructor.
  IB_ResultSet* resultSet_;

  // For internal use only.
  // Deleted by destructor.
  IB_InputBuffer* inputBuffer_;

  // Internal SQL descriptor areas.
  // Deleted by destructor.
  friend class IB_ResultSet;
  friend class IB_InputBuffer;
  // MMM - JIBSRemote is now friend.
  // We need access to this pointers from JIBSRemote class
  friend class JIBSRemote;
  // MMM - end
  XSQLDA* sqldaOut_;
  XSQLDA* sqldaIn_;

  // Input status is shared.
  // Not deleted by destructor.
  IB_Status* status_;

  // Input cursor name is shared, not copied.
  // Not deleted by destructor.
  // !!! move this to result set??
  // !!! doublecheck when can isc_dsql_set_cursor_name be called?
  IB_STRING cursorName_;

  int stmtType_;

  // Keep track if statement is prepared.
  IB_BOOLEAN prepared_;

#ifdef IB_USER_API
  // Notice that InterClient has no need to maintain the sqlString.
  // If this were a user API we would want to.
  IB_STRING sqlString_;
#endif

  IB_SLONG32 queryTimeout_;

  // Users should never muck with this list
  // Friendly statements will add themselves to this list when they are prepared,
  // and remove themselves when they're closed.
  Vector openBlobs_;
  Vector openArrays_;

public:

  IB_Statement (const IB_Status& status);

  // Delete result set, and internal descriptor areas and input buffer.
  // Close the statement.
  // Throws IB_SQLException if the server close request fails.
  ~IB_Statement ();

  // Set or reset the connection context.
  // InterBase does not support distributed queries.
  // Therefore statements are associated with a single connection.
  // An open connection must be set before the statement is allocated.
  // A connection may be reset only after the statement is closed.
  // Throws IB_SQLException if the statement is already allocated.
  void setConnection (const IB_Connection& connection);

  // Set or reset a transaction context.
  // A statement is prepared and executed in the context of a transaction.
  // However, a statement may be allocated before setting a transaction.
  // Also, a new transaction may be set between statement executions, however, 
  // resetting the transaction will automatically close the associated result set.
  void setTransaction (const IB_Transaction& transaction);

  // Reset the status object for this statement.
  void setStatus (const IB_Status& status);

  // You can allocate a statement before a transaction context is set,
  // but the connection context must be set and open.
  // Throws IB_SQLException if statement is already open,
  // or if no open connection context has been set for this statement,
  // or if the server allocate request fails.
  void open ();

  // For executing a non-select non-parameterized SQL statement only once.
  // This api forbids the use of inputs to execute immediate strings
  // use prepareNoOutput (); execute(); if inputs are needed.
  // DO NOT allocate or prepare before making this call.
  // Null terminated sqlString is not copied, and it is not saved as
  // object state information.  
  // Throws IB_SQLException if statement is allocated, 
  // or if server execute-immediate request fails.
  void executeImmediate (const IB_STRING sqlString);

#ifdef IB_USER_API
  // All prepares will automatically allocate if necessary.
#endif
  // All prepares automatically close an open result set.
  // All prepares take a null terminated sql string that is not
  // saved as object state information.

  // For executing a non-select, non-parameterized SQL statement more than once
  // call execute as many times as you like after this call.
  // No descriptor areas are allocated when using this prepare call.
  // Throw IB_SQLException if optional allocate() fails,
  // or if server prepare request fails.
  void prepareNoInputNoOutput (const IB_STRING sqlString);

  // The parameters, numInputs and numOutputs, given in the following
  // set of prepare() calls are only guesses.  The prepare() methods
  // will allocate as many internal sqlvar structures as necessary 
  // for both input and output.  A good guess serves only to limit the
  // possibility of an internal realloc in case the defaults are not
  // sufficient.

  // Executing a non-select, possibly parameterized SQL statement.
  // requires calling prepareNoOutput 
  // and then calling execute as many times
  // as desired with different input values.
  // Only an input descriptor area is allocated with this prepare call.
  // The suggestedInputCols parameter is simply a suggestion for initial SQLVAR
  // allocation.  Once the statement is described, this call may
  // allocate for more than suggestedInputCols SQLVARs.
  // Throw IB_SQLException if optional allocate() fails,
  // or if there is not enough memory on the heap for an input descriptor area,
  // or if server describe_bind or prepare request fails.
  void prepareNoOutput (const IB_STRING sqlString,
			const IB_SSHORT16 suggestedInputCols = defaultInputCols__);

  // For executing a possible query, non-parameterized SQL statement.
  // If you want to execute the statement more than once then open a cursor
  // if its a select.
  // Only an output descriptor area is allocated with this prepare call.
  // The suggestedResultCols parameter is simply a suggestion for initial SQLVAR
  // allocation.  Once the statement is described, this call may
  // allocate for more than suggestedResultCols SQLVARs.
  // Throw IB_SQLException if optional allocate() fails,
  // or if there is not enough memory on the heap for an output descriptor area,
  // or if server describe or prepare request fails.
  IB_ResultSet* prepareNoInput (const IB_STRING sqlString,
				const IB_SSHORT16 suggestedResultCols = defaultResultCols__,
				const IB_SSHORT16 maxFieldSize = 0);

  // For executing a parameterized SQL query, 
  // or any unknown SQL statement, possibly parameterized.
  // If you want to execute the statement more than once then open a cursor
  // if its a select.
  // Both input and output descriptor areas are allocated with this prepare call.
  // The suggestedResultCols+suggestedInputCols parameters are simply a 
  // suggestion for initial SQLVAR
  // allocation.  Once the statement is described, this call may
  // allocate for more than suggestedResultCols+suggestedInputCols SQLVARs.
  // When preparing a completely unknown sql statement, it may be best
  // to use the default argument values for suggestedResultCols and suggestedInputCols.
  // Throw IB_SQLException if optional allocate() fails,
  // or if there is not enough memory on the heap for an input/output descriptor areas,
  // or if server describe, describe_bind or prepare request fails.
  IB_ResultSet* prepare (const IB_STRING sqlString,
			 const IB_SSHORT16 suggestedResultCols = defaultResultCols__,
			 const IB_SSHORT16 suggestedInputCols = defaultInputCols__,
			 const IB_SSHORT16 maxFieldSize = 0);

  IB_ResultSet* prepareCall (IB_STRING sqlString,
			     const IB_SSHORT16 suggestedResultCols = defaultResultCols__,
			     const IB_SSHORT16 suggestedInputCols = defaultInputCols__,
			     const IB_SSHORT16 maxFieldSize = 0);

  // **********************JDBC-style Statement methods***********************

  // Execute a non-query SQL statement.
  // Typically this is used for SQL UPDATE, INSERT, and DELETE statements.
  // In addition, it can be used for DDL and DML statements.
  // Note: execute query statements using ResultSet::open() or singletonFetch()
  // Throw a SQLException if sql statement is a query,
  // or if server execute request fails.
  void executeUpdate ();

  // Close cursor and deallocate statement.
  // Remove this statement from connection's open statement list.
  // When a statement is closed, it's current result set, if one exists, is also closed.
  // No-op if statement is already closed.
  // Throws IB_SQLException if server drop request, or cursor close request fails.
  void close ();

  IB_BOOLEAN isOpen () const;

  // The queryTimeout limit is the number of seconds the driver will
  // wait for a Statement to execute. If the limit is exceeded a
  // SQLException is thrown from the execute() method.
  // return the current query timeout limit in seconds; zero means unlimited 
  IB_SLONG32 getQueryTimeout () const;

  // The queryTimeout limit is the number of seconds the driver will
  // wait for a Statement to execute. If the limit is exceeded a
  // SQLException is thrown from the execute() method.
  // seconds is the new query timeout limit in seconds; zero means unlimited 
  void setQueryTimeout (const IB_SLONG32 seconds);

  // Cancel can be used by one thread to cancel a statement that
  // is being executed by another thread.
  void cancel();

  // setCursor name defines the SQL cursor name that will be used by
  // subsequent Statement execute methods. This name can then be
  // used in SQL positioned update/delete statements to identify the
  // current row in the ResultSet generated by this statement.
  //
  // Note: By definition, positioned update/delete
  // execution must be done by a different Statement than the one
  // which generated the ResultSet being used for positioning. Also,
  // cursor names must be unique within a Connection.
  // cursorName is a null terminated string.
  // Input cursorName is shared, not copied.
  // Throw IB_SQLException if server set cursor name request fails.
  void setCursorName (const IB_STRING cursorName);

  // Get the name of the SQL cursor used by the current ResultSet.
  // In SQL, a result table is retrieved through a cursor that is
  // named. The current row of a result can be updated or deleted
  // using a positioned update/delete statement that references the
  // cursor name.
  // Return a shared null terminated string.
  IB_STRING getCursorName () const;

  // Returns the current result set after having called prepare.
  // getResultSet returns the current result as a ResultSet.
  IB_ResultSet* getResultSet ();

  // Request server for row count information for
  // UPDATE, INSERT or DELETE statements.  
  // For SELECT statements use ResultSet::getRowCount()
  // Throw IB_SQLException if server information request fails.
  IB_SLONG32 getUpdateCount ();

  int isDDL ();

  //******************Parameter Meta Data Methods***************

  // Note that JDBC can only get result set metadata from a result set 
  // (therefore only after execution).

  // Check if the prepared statement returns data or has input
  IB_BOOLEAN isQuery () const;

  IB_BOOLEAN isParameterized () const;

  IB_SSHORT16 getParameterCount () const;

  IB_BOOLEAN isParameterNullable (const IB_SSHORT16 columnIndex) const;

  IB_Types::IBType getParameterType (const IB_SSHORT16 columnIndex) const;

#ifdef IB_USER_API
  IB_LDString getParameterTypeName (const IB_SSHORT16 columnIndex) const;
#endif

  IB_SSHORT16 getParameterPrecision (const IB_SSHORT16 columnIndex) const;

  IB_SSHORT16 getParameterScale (const IB_SSHORT16 columnIndex) const;

  IB_SSHORT16 getParameterSubType (const IB_SSHORT16 columnIndex) const;

  IB_SSHORT16 getParameterCharSet (const IB_SSHORT16 columnIndex) const;

  IB_SSHORT16 getParameterCharLength (const IB_SSHORT16 columnIndex) const;

  //*****************Parameterized Statement methods***************************

  // Set a parameter to SQL NULL.
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setNull (const IB_SSHORT16 parameterIndex, const IB_BOOLEAN isNull);

  // Set a parameter to a SMALLINT value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setSmallInt (const IB_SSHORT16 parameterIndex,
		    const IB_SSHORT16 value);

  // Set a parameter to a INTEGER value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setInteger (const IB_SSHORT16 parameterIndex,
		   const IB_SLONG32 value);

  // Set a parameter to a FLOAT or REAL value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setFloat (const IB_SSHORT16 parameterIndex,
		 const IB_FLOAT32 value);

  // Set a parameter to a DOUBLE value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setDouble (const IB_SSHORT16 parameterIndex,
		  const IB_DOUBLE64 value);

  // Set a parameter to a NUMERIC or DECIMAL value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  // scaled = value*(10**scale), scale is typically 2 for dollars.cents
  void setNumericDouble (const IB_SSHORT16 parameterIndex,
			 const IB_DOUBLE64 scaled,
			 const IB_SSHORT16 scale);

  // Set a parameter to a NUMERIC or DECIMAL value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  // scaled = value*(10**scale), scale is typically 2 for dollars.cents
  void setNumericInteger (const IB_SSHORT16 parameterIndex,
			  const IB_SLONG32 scaled, 
			  const IB_SSHORT16 scale);

  // Set a parameter to a NUMERIC or DECIMAL value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  // scaled = value*(10**scale), scale is typically 2 for dollars.cents
  void setNumericSmallInt (const IB_SSHORT16 parameterIndex,
			   const IB_SSHORT16 scaled,
			   const IB_SSHORT16 scale);

  // Set a parameter to a CHAR value.
  // Input string is copied into internal buffer.
  // string must be null terminated, and it will be padded internally
  // to match column length.
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setChar (const IB_SSHORT16 parameterIndex,
		const IB_SSHORT16 stringLen,
		const IB_STRING string);

  // Set a parameter to a CHAR value.
  // Input string is copied into internal buffer, and it will be padded internally
  // to match column length.
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setChar (const IB_SSHORT16 parameterIndex,
		const IB_LDString ldstring);

  // Set a parameter to a VARCHAR value
  // Input ldstring is copied into internal buffer.
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setVarChar (const IB_SSHORT16 parameterIndex,
		   const IB_LDSTRING ldString);

  // Input ldstring is copied into internal buffer.
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setVarChar (const IB_SSHORT16 parameterIndex,
		   const IB_LDString ldString);

  // Set a parameter to a LONGVARCHAR value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setAsciiBlob (const IB_SSHORT16 parameterIndex,
		     const IB_BLOBID *blobId);

  // Set a parameter to a LONGVARCHAR value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setAsciiBlob (const IB_SSHORT16 parameterIndex,
		     const IB_LDString string);

  // Set a parameter to a BINARY, VARBINARY or LONGVARBINARY value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setBinaryBlob (const IB_SSHORT16 parameterIndex,
		      const IB_BLOBID *blobId);

  // Set a parameter to a BINARY, VARBINARY or LONGVARBINARY value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setBinaryBlob (const IB_SSHORT16 parameterIndex,
		      const IB_SSHORT16 numBytes,
		      const IB_STRING bytes);

  // Set a parameter to a LONGVARCHAR value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setUnicodeBlob (const IB_SSHORT16 parameterIndex,
		       const IB_BLOBID *blobId);

  // Set a parameter to a LONGVARCHAR value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setUnicodeBlob (const IB_SSHORT16 parameterIndex,
		       const IB_STRING bytes);

  // Set a parameter to a DATE, TIME or TIMESTAMP value
  // Throw IB_SQLException ifdef IB_USER_API and parameter index is invalid.
  void setDate (const IB_SSHORT16 parameterIndex,
		const IB_TIMESTAMP timestamp);

  void setArray (const IB_SSHORT16 parameterIndex,
		 const IB_ARRAYID arrayId);

// CJL-IB6 support for new types
  // Set a parameter to a DECIMAL or NUMERIC 64-bit value -- SQL Dialect 3 only
  void setNumericInt64 (const IB_SSHORT16 parameterIndex,
		 const IB_SINT64 scaled, 
		 const IB_SSHORT16 scale);

  // Set a parameter to a SQL DATE value -- SQL Dialect 3 only
  void setSQLDate (const IB_SSHORT16 parameterIndex,
    	 const IB_SLONG32 value);

  // Set a parameter to a TIME value -- SQL Dialect 3 only
  void setTime (const IB_SSHORT16 parameterIndex, 
		 const IB_ULONG32 value);
// CJL-IB6 end changes  

  // Zero out the internal input buffer.
  void clearParameters ();

  //*****************end of Parameterized Statement methods************************

  // Equality obeys reference semantics
  IB_BOOLEAN operator== (const IB_Statement& statement) const;

  int getStatementType ();

private:

  // Allocate input descriptor area and internal input buffer.
  // Throw IB_SQLException there is not enough memory on the heap,
  // or if server describe bind request fails.
  void describeInput (const IB_SSHORT16 numInputs = 2);

  isc_stmt_handle* stmtHandleP () const;

  friend class IB_Blob;
  friend class IB_Array;

  void addOpenBlob (IB_Blob* blob);

  void remOpenBlob (const IB_Blob* blob); 

  void addOpenArray (IB_Array* array);

  void remOpenArray (const IB_Array* array);


  // Returns:
  //   isc_info_sql_stmt_select
  //   isc_info_sql_stmt_insert
  //   isc_info_sql_stmt_update
  //   isc_info_sql_stmt_delete
  //   isc_info_sql_stmt_ddl
  //   isc_info_sql_stmt_get_segment
  //   isc_info_sql_stmt_put_segment
  //   isc_info_sql_stmt_exec_procedure
  //   isc_info_sql_stmt_start_trans
  //   isc_info_sql_stmt_commit
  //   isc_info_sql_stmt_rollback
  //   isc_info_sql_stmt_select_for_upd
  //   isc_info_sql_stmt_set_generator
  int statementType();

  // Copy string into buffer padding to paddedStringLength.
  // Actual buffer length is one greater than paddedStringLength to accomodate null terminator.
  // Called by setChar() and setRawChar()
  static void charCpy (const IB_BUFF_PTR buffer,
		       const IB_SSHORT16 paddedStringLength,
		       const IB_SSHORT16 stringLen,
		       const IB_STRING string);

  // Copy a non-null terminated, length delimited string into a buffer
  // and null terminate it.
  // buffer length must be at least 1 greater than stringLength to accomodate null terminator.
  // !!! this call can be replaced by strcpy if we're assured varchars on the wire
  // !!! are null terminated.
  static void varcharCpy (const IB_BUFF_PTR buffer,
			  const IB_SSHORT16 stringLength,
			  const IB_STRING string);

private:
  
  friend class IB_ResultSet;

  // This opens a cursor for query statements, and simply executes non-queries.
  // Called by IB_ResultSet::openCursor ()
  void dsqlExecute ();

  // Called by IB_ResultSet::singletonFetch ()
  void dsqlSingletonFetch ();

  // Called by IB_ResultSet::next ()
  IB_BOOLEAN dsqlFetch ();

  // Called by IB_ResultSet::close ()
  void dsqlCloseCursor ();

#ifdef IB_USER_API
  IB_BOOLEAN dsqlFetchRelative (const IB_BOOLEAN previous,
				const IB_SLONG32 offset);
#endif

  // Called by close ()
  void dsqlDropStatement ();

  // Called by describeInput()
  void dsqlDescribeBind ();

};

inline
isc_stmt_handle* 
IB_Statement::stmtHandleP () const
{
  return (isc_stmt_handle *)&stmtHandle_;
}

inline
IB_BOOLEAN 
IB_Statement::isOpen () const
{
  return (TO_BOOLEAN (stmtHandle_));
}

inline
void 
IB_Statement::addOpenBlob (IB_Blob* blob) 
{
  openBlobs_.addElement (blob);
}

inline
void 
IB_Statement::remOpenBlob (const IB_Blob* blob) 
{
  openBlobs_.removeElement ((IB_Blob*) blob);
}

inline
void 
IB_Statement::addOpenArray (IB_Array* array)
{
  openArrays_.addElement (array);
}

inline
void 
IB_Statement::remOpenArray (const IB_Array* array) 
{
  openArrays_.removeElement ((IB_Array*) array);
}

#endif
