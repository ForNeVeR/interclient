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
#ifndef _JIBSREMOTE_SESSION_H_
#define _JIBSREMOTE_SESSION_H_

// A JDBC session consists of a single connection, transaction pair.
// In JDBC, one may not have transactions over multiple connections.
// Also, one may not have multiple concurrent transactions on a
// connection.
// Both are restrictions of JDBC and not InterBase.
// JDBC does not distinguish between transaction configuration and
// connection configuration.
// A connection is a linear conduit for sequenced transactions.

#include "IB_Defines.h"
#include "IB_Connection.h"
#include "IB_Transaction.h"
#include "IB_ConnectionConfiguration.h"
#include "IB_TransactionConfiguration.h"
#include "IB_Status.h"
#include "IB_LDString.h"
#include "IB_Statement.h"
#include "IB_ResultSet.h"
#include "IB_ConnectionInformation.h"

class IB_SQLException;

class Session {

private:

  long attachmentCharSetCode_;

// CJL-IB6!!! add SQL Dialect support 
// CJL!!! redundant with connection_.attachmentSQLDialect?
  IB_USHORT16 attachmentSQLDialect_;
// CJL-IB6 end add.

  friend class JIBSRemote;
  IB_Status status_;

  IB_BOOLEAN autoClose_;

  IB_Connection connection_;

  IB_Transaction transaction_;

  IB_ConnectionConfiguration connectionConfiguration_;

  IB_TransactionConfiguration transactionConfiguration_;

  IB_ConnectionInformation connectionInformation_;

  char user_[32];
  IB_SLONG32 userLength_;

#ifdef FUTURE_CODE
  IB_ResultSetPool resultSetPool_;
#endif

  // Called by prepareStatement(), executeStatement(), executeQueryStatement(),
  // and executeUpdateStatement().
  IB_Statement* createStatement ();

  void commitDDL ();

public:

  Session ();

  ~Session ();

  // Called by JIBSRemote::putWarnings()
  IB_SQLException* getWarnings () const;

  // Called by JIBSRemote::putWarnings()
  void clearWarnings ();

  // !!! Potential performance improvement - pass integer tokens
  // !!! over the wire instead of string parameters?
  // !!! This would require mappings on the client. (A fatter client.)
  // keyword must be a null terminated string and
  // and value a null terminated length delimited string object.
  // value is copied not shared.
  // !!! what about ldstrings being null terminated
  // !!! I use strcmp on value.string_.
  // configure() must be called before open ().
  // Called by JIBSRemote::attach ()
  void configure (const IB_STRING keyword,
		  const IB_LDString value);

  // Called by JIBSRemote::attach ()
  void open (const IB_SLONG32 timeout,
	     const IB_LDString database,
             IB_LDString& ibVersion,
	     IB_SLONG32& ibMajorVersion,
             IB_SLONG32& odsMajorVersion,
             IB_SLONG32& odsMinorVersion,
             IB_SLONG32& pageSize,
             IB_SLONG32& pageAllocation,
			 IB_SLONG32& dbSQLDialect,
			 IB_BOOLEAN& dbReadOnly);

  // Called by JIBSRemote::detach ()
  void close ();

  // Called by JIBSRemote::ping ()
  IB_BOOLEAN ping ();

  // Called by JIBSRemote::commit ()
  void commit ();

  // Called by JIBSRemote::commit ()
  void commitRetain ();

  // Called by JIBSRemote::rollback ()
  void rollback ();

  // Called by JIBSRemote::getAndStartTransactionIfPending()
  IB_BOOLEAN transactionStarted ();

  // Called by JIBSRemote::getAndStartTransactionIfPending()
  void startTransaction (const IB_BOOLEAN readOnly, 
			 const IB_SBYTE isolation,
			 const IB_BOOLEAN versionAcknowledgement,
			 const IB_SBYTE lockResolution,
			 const IB_BOOLEAN enableAutoCommit,
			 const IB_BOOLEAN enableAutoClose);

  // Called by JIBSRemote::prepare_statement()
  IB_Statement* prepareStatement (const IB_STRING sql);
  
  // Called by JIBSRemote::prepare_statement()
  IB_Statement* prepareEscapedCall (const IB_STRING sql);
  
  // Called by JIBSRemote::execute_statement()
  IB_ResultSet* executeStatement (IB_Statement*& statement,
				  IB_SLONG32& updateCount,
				  const IB_STRING sql,
				  const IB_SLONG32 timeout,
				  const IB_SSHORT16 maxFieldSize,
				  const IB_SLONG32 fetchSize,
				  const IB_STRING cursorName);

  // Called by JIBSRemote::execute_query_statement()
  IB_ResultSet* executeQueryStatement (IB_Statement*& statement,
				       const IB_STRING sql,
				       const IB_SLONG32 timeout,
				       const IB_SSHORT16 maxFieldSize,
				       const IB_SLONG32 fetchSize,
				       const IB_STRING cursorName);

  // Called by JIBSRemote::execute_update_statement()
  void executeUpdateStatement (IB_Statement*& statement,
			       IB_SLONG32& updateCountOrSelectValue,
			       const IB_STRING sql,
			       const IB_SLONG32 timeout,
			       const IB_SSHORT16 maxFieldSize,
                               const IB_STRING cursorName);

  // Called by JIBSRemote::execute_prepared_statement()
  IB_ResultSet* executePreparedStatement (IB_Statement* statement,
					  IB_SLONG32& updateCount,
					  const IB_SLONG32 timeout,
					  const IB_SSHORT16 maxFieldSize,
					  const IB_SLONG32 fetchSize,
					  const IB_STRING cursorName);
 
  // Called by JIBSRemote::execute_prepared_query_statement()
  IB_ResultSet* executePreparedQueryStatement (IB_Statement* statement,
					       const IB_SLONG32 timeout,
					       const IB_SSHORT16 maxFieldSize,
					       const IB_SLONG32 fetchSize,
					       const IB_STRING cursorName);
    
  // Called by JIBSRemote::execute_prepared_update_statement()
  void executePreparedUpdateStatement (IB_Statement* statement,
				       IB_SLONG32& updateCountOrSelectValue,
				       const IB_SLONG32 timeout,
				       const IB_SSHORT16 maxFieldSize,
				       const IB_STRING cursorName);


  void createDatabase (const IB_STRING database);

// MMM - added method prototypes
  //  called by JIBSRemote::get_array_descriptor(),
  //            JIBDRemote::putResultMetaData(),
  //            JIBDRemote::putInputMetaData()
  void arrayLookupBounds (IB_STRING tableName,
			  IB_STRING columnName,
			  ISC_ARRAY_DESC* descriptor);

  // Called by JIBSRemote::get_array_slice()
  void arrayGetSlice (IB_ARRAYID* arrayId,
		      ISC_ARRAY_DESC* arrayDescriptor,
		      void* arrayData,
		      IB_SLONG32* arrayDataLength);

  // Called by JIBSRemote::getArray()
  void arrayPutSlice (IB_ARRAYID* arrayId,
		      ISC_ARRAY_DESC* arrayDescriptor,
		      void* arrayData,
		      IB_SLONG32* arrayDataLength);
// MMM - end
};

inline
Session::Session ()
  : status_ (),
    connection_ (status_),
    transaction_ (status_),
    connectionConfiguration_ (),
    transactionConfiguration_ (),
    connectionInformation_ (status_, connection_),
    autoClose_ (IB_TRUE)
{
  transaction_.setConnection (connection_);
    
  // Not really necessary to set default autocommit
  // because config info is always sent when transaction is pending.
  // transactionConfiguration_.setAutoCommit (IB_TRUE);
  // CJL - db sql dialect added for IB6 info
  // shouldn't hurt IB5 connections
  connectionInformation_.addRequest (isc_info_version);
  connectionInformation_.addRequest (isc_info_ods_version);
  connectionInformation_.addRequest (isc_info_ods_minor_version);
  connectionInformation_.addRequest (isc_info_page_size);
  connectionInformation_.addRequest (isc_info_allocation);
  connectionInformation_.addRequest (isc_info_base_level);
  connectionInformation_.addRequest (isc_info_db_SQL_dialect);
  connectionInformation_.addRequest (isc_info_db_read_only);
}

// Called by JIBSRemote::putWarnings()
inline
IB_SQLException* 
Session::getWarnings () const
{
  return status_.getWarnings ();
}

// Called by JIBSRemote::putWarnings()
inline
void 
Session::clearWarnings ()
{
  status_.clearWarnings ();
}

// Called by JIBSRemote::getAndStartTransactionIfPending()
inline
IB_BOOLEAN 
Session::transactionStarted ()
{
  return (transaction_.isOpen());
}

#endif

