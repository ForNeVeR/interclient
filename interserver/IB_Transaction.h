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
#ifndef _IB_TRANSACTION_H_
#define _IB_TRANSACTION_H_

#include "Vector.h" 

#include "IB_Defines.h"
#include "IB_SQLException.h"

class IB_Connection;
class IB_TransactionConfiguration;
class IB_Status;

class IB_Transaction {

public:

  enum CloseOption {
    COMMIT,
    ROLLBACK
  };

private:

  typedef struct {
    isc_db_handle *db_ptr;
    IB_SLONG32 tpb_len;
    IB_BUFF_PTR tpb_ptr;
  } TransactionExistenceBlock;

  // InterBase client transaction handle.
  // Also used as a flag to indicate an active transaction.
  isc_tr_handle trHandle_;

  // Internal structure for isc describing the transaction configuration
  // for each connection.
  // Deleted by destructor.
  TransactionExistenceBlock* tebVector_;

  // Number of connections spanned by this transaction.
  const IB_SSHORT16 numConnections_; 

  // This transaction is distributed across these connections.
  // Note: InterBase supports distributed transactions, but not distributed queries.
  Vector connections_;

  // !!! how to efficiently test for readOnly?
  // !!! can't require that every connectionIndex is configured?
  // A private flag intialized to true by constructor.
  // On every call to setConnection(), the transaction configuration
  // is checked, and if any one of them is read-write then
  // this flag is unset to false.
  // See IB_TransactionConfiguration::readOnly ()
  // IB_BOOLEAN readOnly_;

  // Input status is not shared.
  // Not deleted by destructor.
  IB_Status* status_;

public:

  // Specify the number of connections to be distributed by this transaction.
  // Default is 1.
  // Throws IB_SQLException if there is not enough memory on the heap to allocate
  // Transaction existence blocks for each connection.
  IB_Transaction (const IB_Status& status, 
		  const IB_SSHORT16 numConnections = 1);

  // close(ROLLBACK) and delete tebVector.
  // Throws IB_SQLException if close(ROLLBACK) fails.
  ~IB_Transaction ();

  // Specify a connection to be spanned by this transaction.
  // Throws IB_SQLException if called more than numConnections times.
  void setConnection (const IB_Connection& connection,
		      const int connectionIndex = 0);

  // Configure transaction state for each connection separately.
  // If not configured, then InterBase defaults are used.
  void configure (const IB_TransactionConfiguration& configuration,
		  const int connectionIndex = 0);

  // Reassign the status object for this connection.
  void setStatus (const IB_Status& status);

  // Transaction operations

  // Open the transaction.
  // Throws IB_SQLException if the transaction is already open, 
  // or if not all connections have been set,
  // or if the transaction start request fails.
  void open ();

  // Commits or rolls back this transaction and closes it.
  // No-op if transaction is not open.
  // Throws IB_SQLException if the server commit/rollback request fails.
  void close (const CloseOption closeOption = COMMIT);

  // Commits this transaction but retains the current data snapshot.
  // The transaction remains active.
  // Throws IB_SQLException if the transaction is not active,
  // or if the server commit retain request fails
  void commitRetain ();

  // Commits this transaction, stops it, and then restarts it.
  // The transaction remains active with a new snapshot of data.
  // Equivalent to commitClose() followed by open()
  // Throws IB_SQLException if the transaction is not active,
  // or if the server commit/start requests fail.  
  void commitRefresh ();

#ifdef IB_USER_API
  // For read only transactions.
  // Throws IB_SQLException if this transaction is read-write,
  // or if the server request fails.
  // !!! ask madhukar if this can be achieved without calling commitRefresh()
  void refresh ();

  // For manual two phase commits.
  // Throws IB_SQLException if the transaction is not active,
  // or if the server prepare request fails.
  void prepare ();
#endif

  IB_BOOLEAN isOpen () const;

  // Equality obeys reference semantics 
  IB_BOOLEAN operator== (const IB_Transaction& transaction) const;

private:

  friend class IB_Statement;
  friend class IB_TransactionInformation;
  friend class IB_Connection;
  friend class IB_Blob;
  friend class IB_Array;
  // MMM - make friend with Session
  // To allow Session::getArraySlice()
  friend class Session;
  // MMM - end

  isc_tr_handle* trHandleP () const;

  // Objects of this class are not to be copied
  IB_Transaction (const IB_Transaction&);
  IB_Transaction& operator = (const IB_Transaction&);

  // Called by constructor.
  // Allocates numConnections TransactionExistenceBlocks.
  // Throws IB_SQLException if there is not enough memory on the heap.
  void allocateTEBVector ();

  void zeroTEBVector ();

  // Remove this transaction from each connection's active transaction list.
  // Called by rollback(), and commit()
  void removeFromOpenTransactionLists ();

};

inline
isc_tr_handle* 
IB_Transaction::trHandleP () const
{
  return (isc_tr_handle *)&trHandle_;
}

inline
IB_BOOLEAN 
IB_Transaction::isOpen () const
{
  return (TO_BOOLEAN (trHandle_));
}

#endif
