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
#include <string.h> // strcmp
#include <stdlib.h>  // atoi

#include "Session.h"
#include "IB_SQLException.h"
#include "IB_CharacterSets.h"

Session::~Session ()
{ }

// !!! fill this out and capitalize all property keys
void 
Session::configure (const IB_STRING keyword,
		    const IB_LDString value)
{
  if (strcmp (keyword, "user") == 0) { 
    strncpy (user_, value.string_, value.length_);
    userLength_ = value.length_;
    connectionConfiguration_.setUser (value);
  }
  else if (strcmp (keyword, "password") == 0) {
    connectionConfiguration_.setPassword (value);
  }
  else if (strcmp (keyword, "roleName") == 0) {
    connectionConfiguration_.setRole (value);
  }
  else if (strcmp (keyword, "charSet") == 0) { 
    connectionConfiguration_.setCharacterSet (value);
    attachmentCharSetCode_ = IB_CharacterSets::getCharSetCode (value);
  }
// CJL --  Added support for SQL Dialects on attachment
  else if (strcmp (keyword, "sqlDialect") == 0) {
     connectionConfiguration_.setSQLDialect ( atoi(value.string_) );
	 attachmentSQLDialect_ = atoi( value.string_ );
	 connection_.attachmentSQLDialect_ = atoi( value.string_ );
  }
// CJL-IB6 end
  else if (strcmp (keyword, "sweepOnConnect") == 0) {
    if (strcmp (value.string_, "true") == 0)
      connectionConfiguration_.sweep ();
  }
  else if (strcmp (keyword, "verifyOnConnect") == 0) {
    if (strcmp (value.string_, "none") != 0)
      connectionConfiguration_.setVerify (value);
  }
  else if (strcmp (keyword, "suggestedCachePages") == 0) {
    if (strcmp (value.string_, "0") == 0)
      return;
    if (atoi (value.string_) > 255)
      connectionConfiguration_.setNumBuffers (255);
    else
      connectionConfiguration_.setNumBuffers (atoi (value.string_));
  }
  else if (strcmp (keyword, "activateShadowOnConnect") == 0) {
    if (strcmp (value.string_, "true") == 0)
      connectionConfiguration_.activateShadow ();
  }

  // ------------nothing below here is surfaced in InterClient--------------------

  else if (strcmp (keyword, "dummy packet interval") == 0) {
    connectionConfiguration_.setDummyPacketInterval (atoi (value.string_));
  }
  else {
    status_.setWarning (new IB_SQLException (IB_SQLException::invalidArgument__connection_property__unrecognized__,
					     IB_SQLException::invalidArgumentException__));
  }
}

// Called by JIBSRemote::attach ()
// CJL-IB6 added dbSQLDialect and dbReadOnly to signature for IB6
void 
Session::open (const IB_SLONG32 timeout,
	       const IB_LDString database,
               IB_LDString& ibProductVersion,
	       IB_SLONG32& ibMajorVersion,
               IB_SLONG32& odsMajorVersion,
               IB_SLONG32& odsMinorVersion,
               IB_SLONG32& pageSize,
               IB_SLONG32& pageAllocation,
			   IB_SLONG32& dbSQLDialect,
			   IB_BOOLEAN& dbReadOnly)
{
  /**
  if (timeout) { // timeout is in milliseconds
    status_.setWarning (new IB_SQLException (IB_SQLException::driverNotCapable__connection_timeout__,
					     IB_SQLException::driverNotCapableException__));
#ifdef IB41
    connectionConfiguration_.setConnectTimeout (timeout);
#endif
  }
  **/

  connection_.setDatabase (database);
  connection_.open (connectionConfiguration_, 
	                attachmentCharSetCode_);

  connectionInformation_.requestInfo ();
  connectionInformation_.getVersion (ibProductVersion);
  connectionInformation_.getBaseLevel (ibMajorVersion);
  connectionInformation_.getODSMajorVersion (odsMajorVersion);
  connectionInformation_.getODSMinorVersion (odsMinorVersion);
  connectionInformation_.getPageSize (pageSize);
  connectionInformation_.getPageAllocation (pageAllocation);
// CJL-IB6  retrieval of SQL Dialect and Read-Only status (for the database)

  connectionInformation_.getDatabaseSQLDialect(dbSQLDialect);

// CJL-IB6  Set dialect to match database if not specified at attach
//  create Warning if attach dialect exceeds db dialect

  if (connection_.attachmentSQLDialect_ == 0) {
	  connection_.attachmentSQLDialect_ = (IB_USHORT16) dbSQLDialect;
  }
  else if (dbSQLDialect < connection_.attachmentSQLDialect_) {
	connection_.attachmentSQLDialect_ = (IB_USHORT16) dbSQLDialect;  
	status_.setWarning( new IB_SQLException (IB_SQLException::unsupportedSQLDialect__dialect_adjusted__,
      (int) connection_.attachmentSQLDialect_, 
      IB_SQLException::unsupportedSQLDialectException__));
  }
    
  connectionInformation_.getDatabaseReadOnly(dbReadOnly);
// CJL-IB6 end
}

// Called by JIBSRemote::detach ()
void 
Session::close ()
{
  connection_.close (IB_Transaction::ROLLBACK);
}

// Called by JIBSRemote::ping ()
IB_BOOLEAN 
Session::ping ()
{
  return TO_BOOLEAN (connection_.isOpen ());
}

// Called by JIBSRemote::commit ()
void 
Session::commit ()
{
  connection_.closeResultSets ();

  transaction_.close (IB_Transaction::COMMIT);
}

// Called by JIBSRemote::commit ()
void 
Session::commitRetain ()
{
  connection_.closeResultSets ();

  transaction_.commitRetain ();
}

void 
Session::commitDDL ()
{
  transaction_.commitRetain ();
}

// Called by JIBSRemote::rollback ()
void 
Session::rollback ()
{
  connection_.closeResultSets ();

  transaction_.close (IB_Transaction::ROLLBACK);
}

// Called by prepareStatement(), executeStatement(), executeQueryStatement(),
// and executeUpdateStatement().
IB_Statement* 
Session::createStatement ()
{
  IB_Statement* statement = new IB_Statement (status_);
  statement->setConnection (connection_);
  statement->open ();
  statement->setTransaction (transaction_);
  return statement;
}

// Called by JIBSRemote::execute_statement()
IB_ResultSet* 
Session::executeStatement (IB_Statement*& statement,
			   IB_SLONG32& updateCount,
			   const IB_STRING sql,
			   const IB_SLONG32 timeout,
			   const IB_SSHORT16 maxFieldSize,
			   const IB_SLONG32 fetchSize,
			   const IB_STRING cursorName)
{
  if (!statement) {
    statement = createStatement ();
  }
  // if the statement allocation is being reused,
  // be sure to close the previous cursor.
  else if (statement->isQuery ()) {
    statement->getResultSet ()->close ();
  }
    
  // if the statement is already open then we can reuse the allocation,
  // but the sql string may have changed so we must re-prepare.
  // Re-preparing automatically deletes old result sets.
  IB_ResultSet* resultSet = statement->prepareNoInput (sql);

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->setFetchSize (fetchSize);
    resultSet->setMaxFieldSize (maxFieldSize);
    if (statement->getStatementType () == isc_info_sql_stmt_exec_procedure)
      resultSet->singletonFetch ();
    else 
      resultSet->open ();
    updateCount = -1;
  }
  else {
    if (statement->isDDL ())
      connection_.closeStatementsExcept (statement);
    statement->executeUpdate ();
    updateCount = statement->getUpdateCount ();
    if (statement->isDDL ())
      commitDDL ();
  }

  return resultSet;
}

// Called by JIBSRemote::execute_query_statement()
IB_ResultSet* 
Session::executeQueryStatement (IB_Statement*& statement,
				const IB_STRING sql,
				const IB_SLONG32 timeout,
				const IB_SSHORT16 maxFieldSize,
				const IB_SLONG32 fetchSize,
				const IB_STRING cursorName)
{
  if (!statement) {
    statement = createStatement ();
  }
  // if the statement allocation is being reused,
  // be sure to close the previous cursor.
  else if (statement->isQuery ()) {
    statement->getResultSet ()->close ();
  }

  // if the statement is already open then we can reuse the allocation,
  // but the sql string may have changed so we must re-prepare.
  // Re-preparing automatically deletes old result sets.
  IB_ResultSet* resultSet = statement->prepareNoInput (sql);

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->setFetchSize (fetchSize);
    resultSet->setMaxFieldSize (maxFieldSize);
    if (statement->getStatementType () == isc_info_sql_stmt_exec_procedure)
      resultSet->singletonFetch ();
    else 
      resultSet->open ();
  }
  else {
    throw new IB_SQLException (IB_SQLException::invalidOperation__execute_query_on_an_update_statement__,
                               IB_SQLException::invalidOperationException__);
  }

  return resultSet;
}

// Called by JIBSRemote::execute_update_statement()
void 
Session::executeUpdateStatement (IB_Statement*& statement,
				 IB_SLONG32& updateCount,
				 const IB_STRING sql,
				 const IB_SLONG32 timeout,
				 const IB_SSHORT16 maxFieldSize,
                                 const IB_STRING cursorName)
{
  if (!statement) {
    statement = createStatement ();
  }
  // if the statement allocation is being reused,
  // be sure to close the previous cursor.
  else if (statement->isQuery ()) {
    statement->getResultSet ()->close ();
  }

  // if the statement is already open then we can reuse the allocation,
  // but the sql string may have changed so we must re-prepare.
  // Re-preparing automatically deletes old result sets.
  IB_ResultSet* resultSet = statement->prepareNoInput (sql);

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->setMaxFieldSize (maxFieldSize);
    resultSet->singletonFetch ();
    updateCount = -1;
  }
  else {
    if (statement->isDDL ())
// CJL-IB6 <!> uh-oh... what have we here?  This will eventually throw a bugcheck 10010
// when a previously opened statement is re-used!
      connection_.closeStatementsExcept (statement);
    statement->executeUpdate ();
    updateCount = statement->getUpdateCount();
    if (statement->isDDL ())
      commitDDL ();
  }
}

// Called by JIBSRemote::prepare_statement()
IB_Statement* 
Session::prepareStatement (const IB_STRING sql)
{
  IB_Statement* statement = createStatement ();
  statement->prepare (sql);
  return statement;
}
  
// Called by JIBSRemote::prepare_statement()
IB_Statement* 
Session::prepareEscapedCall (const IB_STRING sql)
{
  IB_Statement* statement = createStatement ();
  statement->prepareCall (sql);
  return statement;
}
  
// Called by JIBSRemote::execute_prepared_statement()
IB_ResultSet* 
Session::executePreparedStatement (IB_Statement* statement,
				   IB_SLONG32& updateCount,
				   const IB_SLONG32 timeout,
				   const IB_SSHORT16 maxFieldSize,
				   const IB_SLONG32 fetchSize,
				   const IB_STRING cursorName)
{
  IB_ResultSet* resultSet = statement->getResultSet ();

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->close ();
    resultSet->setFetchSize (fetchSize);
    resultSet->setMaxFieldSize (maxFieldSize);
    if (statement->getStatementType () == isc_info_sql_stmt_exec_procedure)
      resultSet->singletonFetch ();
    else 
      resultSet->open ();
    updateCount = -1;
  }
  else {
    if (statement->isDDL ())
      connection_.closeStatementsExcept (statement);
    statement->executeUpdate ();
    updateCount = statement->getUpdateCount();
    if (statement->isDDL ())
      commitDDL ();
  }

  return resultSet;
}
    

// Called by JIBSRemote::execute_prepared_query_statement()
IB_ResultSet* 
Session::executePreparedQueryStatement (IB_Statement* statement,
					const IB_SLONG32 timeout,
					const IB_SSHORT16 maxFieldSize,
					const IB_SLONG32 fetchSize,
					const IB_STRING cursorName)
{
  IB_ResultSet* resultSet = statement->getResultSet ();

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->close ();
    resultSet->setFetchSize (fetchSize);
    resultSet->setMaxFieldSize (maxFieldSize);
    if (statement->getStatementType () == isc_info_sql_stmt_exec_procedure)
      resultSet->singletonFetch ();
    else 
      resultSet->open ();
  }
  else {
    throw new IB_SQLException (IB_SQLException::invalidOperation__execute_query_on_an_update_statement__,
                               IB_SQLException::invalidOperationException__);
  }

  return resultSet;
}
    
// Called by JIBSRemote::execute_prepared_update_statement()
void 
Session::executePreparedUpdateStatement (IB_Statement* statement,
					 IB_SLONG32& updateCount,
					 const IB_SLONG32 timeout,
					 const IB_SSHORT16 maxFieldSize,
					 const IB_STRING cursorName)
{
  IB_ResultSet* resultSet = statement->getResultSet ();

  statement->setQueryTimeout (timeout);

  if (resultSet && (*cursorName != '\0'))
    statement->setCursorName (cursorName);

  if (resultSet) {
    resultSet->close ();
    resultSet->setMaxFieldSize (maxFieldSize);
    resultSet->singletonFetch ();
    updateCount = -1;
  }
  else {
    if (statement->isDDL ())
      connection_.closeStatementsExcept (statement);
    statement->executeUpdate ();
    updateCount = statement->getUpdateCount();
    if (statement->isDDL ())
      commitDDL ();
  }
}

void 
Session::startTransaction (const IB_BOOLEAN readOnly, 
			   const IB_SBYTE isolation,
			   const IB_BOOLEAN versionAcknowledgement,
			   const IB_SBYTE lockResolution,
			   const IB_BOOLEAN enableAutoCommit,
			   const IB_BOOLEAN enableAutoClose)
{
  // order of method calls here is pertinent
  transactionConfiguration_.setAccess ((IB_TransactionConfiguration::AccessMode) readOnly);
  transactionConfiguration_.setIsolation ((IB_TransactionConfiguration::IsolationLevel) isolation);
  transactionConfiguration_.setAutoCommit (enableAutoCommit);
  transactionConfiguration_.setVersionAcknowledgement (versionAcknowledgement);
  transactionConfiguration_.setLockResolution ((IB_TransactionConfiguration::LockResolutionMode) lockResolution);
  autoClose_ = enableAutoClose;
  transaction_.configure (transactionConfiguration_);
  transaction_.open ();
}

// MMM - added method 
// called by JIBSRemote::get_array_descriptor(),
//           JIBDRemote::putResultMetaData(),
//           JIBDRemote::putInputMetaData()
void
Session::arrayLookupBounds (IB_STRING tableName,
			    IB_STRING columnName,
			    ISC_ARRAY_DESC* descriptor)
{
  if (isc_array_lookup_bounds (status_.vector(),
		               connection_.dbHandleP(),
		               transaction_.trHandleP(),
                               tableName,
                               columnName,
                               descriptor))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, &status_);
}
// MMM - end

// MMM - added method
// called by JIBSRemote::get_array_slice()
void
Session::arrayGetSlice (IB_ARRAYID* arrayId,
			ISC_ARRAY_DESC* arrayDescriptor,
			void* arrayData,
			IB_SLONG32* arrayDataLength)
{
  if (isc_array_get_slice (status_.vector(),
		           connection_.dbHandleP(),
		           transaction_.trHandleP(),
			   arrayId,
			   arrayDescriptor,
			   arrayData,
			   arrayDataLength))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, &status_);
}
// MMM - end

// MMM - added method
// called by JIBSRemote::getArray()
void
Session::arrayPutSlice (IB_ARRAYID* arrayId,
			ISC_ARRAY_DESC* arrayDescriptor,
			void* arrayData,
			IB_SLONG32* arrayDataLength)
{
  if (isc_array_put_slice (status_.vector(),
		           connection_.dbHandleP(),
		           transaction_.trHandleP(),
			   arrayId,
			   arrayDescriptor,
			   arrayData,
			   arrayDataLength))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, &status_);
}
// MMM - end

// -----------------Server requests follow----------------

void
Session::createDatabase (const IB_STRING database)
{
  isc_db_handle ISC_FAR db_handle = 0;
  if (isc_create_database (status_.vector(), 
			   0, // short ?, probably means null terminated filename
			   database, 
			   &db_handle, // perhaps I should use connection_->dbHandleP()?
			   0, //short dpb_len, 
			   0, //char ISC_FAR *dpb, page size can be specified with isc_dpb_page_size
			   0)) //short ?, who knows but keep it zero
    throw new IB_SQLException (IB_SQLException::engine__default_0__, &status_);
}
