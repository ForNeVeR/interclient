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
#include "IB_Transaction.h"
#include "IB_Connection.h"
#include "IB_TransactionConfiguration.h"
#include "IB_Status.h"

IB_Transaction::IB_Transaction (const IB_Status& status,
				const IB_SSHORT16 numConnections)
  : trHandle_ (NULL),
    numConnections_ (numConnections),
    connections_ (),
    status_ ((IB_Status*) &status)
{
#ifdef TRACEON
  debugTraceAnInt ("constructing transaction: ", (IB_REF) this);
#endif
  allocateTEBVector ();
  // This is necessary so that unconfigured tpb's will be NULL pointers.
  zeroTEBVector (); 
}

void
IB_Transaction::allocateTEBVector ()
{
  // !!! rewrite using malloc
  tebVector_ = new TransactionExistenceBlock[numConnections_];

  if (!tebVector_)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
			       IB_SQLException::outOfMemoryException__);
}

void
IB_Transaction::zeroTEBVector ()
{
  for (int i=0; i<numConnections_; i++) {
    tebVector_[i].db_ptr = NULL;
    tebVector_[i].tpb_len = 0;
    tebVector_[i].tpb_ptr = NULL;
  }
}
    

IB_Transaction::~IB_Transaction ()
{
#ifdef TRACEON
  debugTraceAnInt ("destroying transaction: ", (IB_REF) this);
#endif
  close (ROLLBACK);
  delete tebVector_;
}

void
IB_Transaction::configure (const IB_TransactionConfiguration& configuration,
			   const int connectionIndex)
{
#ifdef IB_USER_API
  if ((connectionIndex >= numConnections_) ||
      (connectionIndex < 0))
    throw new IB_SQLException ("Attempt to configure transaction with"
			       "invalid connection index");
#endif

  tebVector_[connectionIndex].tpb_len = configuration.parameterBlock_.length_;
  tebVector_[connectionIndex].tpb_ptr = configuration.parameterBlock_.buffer_;

}

void
IB_Transaction::setConnection (const IB_Connection& connection,
			       const int connectionIndex)
{
#ifdef IB_USER_API
  if ((connectionIndex >= numConnections_) ||
      (connectionIndex < 0))
    throw new IB_SQLException ("Attempt to set the connection context for a transaction"
			       "using an invalid connection index");
#endif

  connections_.addElement ((IB_Connection*) &connection);

  tebVector_[connectionIndex].db_ptr = connection.dbHandleP();
}

void
IB_Transaction::setStatus (const IB_Status& status)
{
  status_ = (IB_Status*) &status;
}

void
IB_Transaction::open ()
{
  if (trHandle_) // Attempt to open a transaction which is already open
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10013,
			       IB_SQLException::bugCheckException__);

  if (isc_start_multiple (status_->vector(), 
			  &trHandle_, 
			  numConnections_, 
			  tebVector_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  // Add this transaction to each connection's active transaction list.
  VectorEnumeration connections (connections_);
  while (connections.hasMoreElements ()) {
    IB_Connection *c = (IB_Connection*) connections.nextElement ();
    c ->addOpenTransaction (*this);
  }
}

#ifdef IB_USER_API
void
IB_Transaction::prepare()
{
  if (!trHandle_)
    throw new IB_SQLException ("Attempt to prepare a transaction which is not open", 
                               SQLStates::_25000,
                               ErrorCodes::invalidOperationError);

  if (isc_prepare_transaction (status_->vector(), &trHandle_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}
#endif

void
IB_Transaction::commitRetain ()
{
  if (!trHandle_) // Attempt to commit retain a transaction which is not open 
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10014,
			       IB_SQLException::bugCheckException__);

  if (isc_commit_retaining (status_->vector(), &trHandle_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

}

void
IB_Transaction::commitRefresh ()
{
  if (!trHandle_) // Attempt to commit a transaction which is not open
     throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10015,
			       IB_SQLException::bugCheckException__);

  if (isc_commit_transaction (status_->vector(), &trHandle_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  trHandle_ = NULL;

  if (isc_start_multiple (status_->vector(), 
			  &trHandle_, 
			  numConnections_, 
			  tebVector_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

void
IB_Transaction::close (const CloseOption closeOption)
{
  if (!trHandle_)
    return;

  switch (closeOption) {
  case COMMIT:
    if (isc_commit_transaction (status_->vector(), &trHandle_))
      throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
    break;
  case ROLLBACK:
    if (isc_rollback_transaction (status_->vector(), &trHandle_))
      throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
    break;
  }

  removeFromOpenTransactionLists ();

  // Set a flag to indicate that the transaction is now closed.
  trHandle_ = NULL;
}

IB_BOOLEAN 
IB_Transaction::operator== (const IB_Transaction& transaction) const
{
  return (this == &transaction);
}

void
IB_Transaction::removeFromOpenTransactionLists ()
{
  VectorEnumeration connections (connections_);
  while (connections.hasMoreElements ()) {
    IB_Connection *connection = (IB_Connection*) connections.nextElement ();
    connection->remOpenTransaction (*this);
  }
}

