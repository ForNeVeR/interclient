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
#ifndef _IB_CONNECTION_H_
#define _IB_CONNECTION_H_

#include "Vector.h"

#include "IB_Defines.h"
#include "IB_SQLException.h"
#include "IB_LDString.h"
#include "IB_Transaction.h"

#ifdef SMART_COMPILER
class IB_Statement;
#else
#include "IB_Statement.h"
#endif

class IB_Status;
class IB_ConnectionConfiguration;

// A Connection represents a session with a specific database.
// Within the context of a Connection, SQL statements are
// executed and results are returned.

class IB_Connection {

private:

  // InterBase client database handle.
  // Also used as a flag to indicate open connection.
  isc_db_handle dbHandle_;

  // Input database name is shared.
  // Not deleted by destructor.
  // Up to 255 characters
  IB_LDString databaseName_;

  // Input status object is shared.
  // Not deleted by destructor.
  IB_Status* status_;

  // Users should never muck with this list
  // Friendly transactions will add themselves to this list when they're open,
  // and remove themselves when they're closed.
  Vector openTransactions_;

  // Users should never muck with this list
  // Friendly statements will add themselves to this list when they are opened,
  // and remove themselves when they're closed.
  Vector openStatements_;

public:

  long attachmentCharSetCode_;

// CJL-IB6 -- add SQL Dialect support

  IB_USHORT16 attachmentSQLDialect_;

// CJL-IB6 -- end add.

  IB_Connection (const IB_Status& status);

  // Does not copy databaseName string.
  // Database name up to 255 characters.
  IB_Connection (const IB_Status& status,
		 const IB_LDString& databaseName);

  // abort() connection.
  // Throws IB_SQLException if abort() fails.
  ~IB_Connection ();

  // Reassign the status object for this connection.
  void setStatus (const IB_Status& status);

  // If a connection is constructed without specifying a database name 
  // in the constructor, then the database name must be assigned with setDatabase().
  // A database name may not be reassigned while the connection is active.
  // databaseName is not copied.
  // databaseName up to 255 characters.
  // Throws IB_SQLException if connection is active.
  void setDatabase (const IB_LDString& databaseName);

  // Throws IB_SQLException if connection already open, 
  // or if server connection request fails.
  void open (const IB_ConnectionConfiguration& configuration,
	     const long attachmentCharSetCode);

#ifdef IB_USER_API
  void open ();

  // userName and password up to 255 characters.
  void open (const IB_LDString& userName,
	     const IB_LDString& password);
#endif

  // An exception will be raised if you attempt to close a connection with
  // open transactions.  In error recovery situations, use close(IB_Transaction::CloseOption)
  // to clean up all open transactions and statements on this connection.
  // No-op if connection is already closed.
  // Throw IB_SQLException if there are open transactions or statements,
  // or if server detach request fails.
  void close ();

  void close (const IB_Transaction::CloseOption);

  void closeStatements ();

  void closeStatementsExcept (IB_Statement* statement);

  void closeResultSets ();

  // This is a bogus method which just checks if the database handle
  // is non-zero.
  IB_BOOLEAN isOpen ();

#ifdef IB_USER_API
  // Warning! This will DELETE the database on this connection.
  // The connection must be established before dropping the database.
  // All supporting files, such as secondary database files, WALs, 
  // and shadow files, will also be deleted.
  // Throws IB_SQLException if the connection is not open, 
  // or if there the server drop request fails.
  void dropDatabase ();
#endif

  // Equality obeys reference semantics
  IB_BOOLEAN operator== (const IB_Connection& connection) const;

private:

  friend class IB_ConnectionInformation;
  friend class IB_TransactionInformation;
  friend class IB_Statement;
  friend class IB_Transaction;
  friend class IB_Blob;
  friend class IB_Array;
  // MMM - make friend with Session
  // To allow Session::getArraySlice()
  friend class Session;
  // MMM - end

  // Objects of this class are not to be copied
  IB_Connection (const IB_Connection&);
  IB_Connection& operator = (const IB_Connection& c);

  // Add transaction to list of open transaction on connection.
  void addOpenTransaction (const IB_Transaction& transaction);

  // Remove transactoin from list of active transactions.
  void remOpenTransaction (const IB_Transaction& transaction);

  // Add a statement to the list of prepared statements.
  void addOpenStatement (const IB_Statement& statement);

  // Remove a statement from the list of prepared statements.
  void remOpenStatement (const IB_Statement& statement);

  isc_db_handle* dbHandleP () const;

};

inline
IB_Connection::IB_Connection (const IB_Status& status)
  : dbHandle_ (NULL),
    databaseName_ (),
    status_ ((IB_Status*) &status),
    openTransactions_ (),
    openStatements_ ()
{ 
#ifdef TRACEON
  debugTraceAnInt ("constructing connection ", (IB_REF) this);
#endif
}

inline
IB_Connection::IB_Connection (const IB_Status& status,
			      const IB_LDString& databaseName)
  : dbHandle_ (NULL),
    databaseName_ (databaseName),
    status_ ((IB_Status*) &status),
    openTransactions_ (),
    openStatements_ ()
{ 
#ifdef TRACEON
  debugTraceAnInt ("constructing connection ", (IB_REF) this);
#endif
}

inline
void
IB_Connection::setStatus (const IB_Status& status)
{
  status_ = (IB_Status*) &status;
}

inline
void
IB_Connection::setDatabase (const IB_LDString& databaseName)
{
#ifdef IB_USER_API
  if (dbHandle_)
    throw new IB_SQLException ("Cannot reassign database name while connection is active");
#endif

  databaseName_ = databaseName;
}

inline
IB_BOOLEAN
IB_Connection::isOpen ()
{
  return TO_BOOLEAN (dbHandle_);
}

inline
void
IB_Connection::addOpenTransaction (const IB_Transaction& transaction)
{
  openTransactions_.addElement ((IB_Transaction*) &transaction);
}

inline
void
IB_Connection::remOpenTransaction (const IB_Transaction& transaction)
{
  openTransactions_.removeElement ((IB_Transaction*) &transaction);
}

inline
void
IB_Connection::addOpenStatement (const IB_Statement& statement)
{
  openStatements_.addElement ((IB_Statement*) &statement);
}

inline
void
IB_Connection::remOpenStatement (const IB_Statement& statement)
{
  openStatements_.removeElement ((IB_Statement*) &statement);
}

inline
IB_BOOLEAN 
IB_Connection::operator== (const IB_Connection& connection) const
{
  return (this == &connection);
}

inline
isc_db_handle* 
IB_Connection::dbHandleP () const
{
  return (isc_db_handle *)&dbHandle_;
}

#endif

