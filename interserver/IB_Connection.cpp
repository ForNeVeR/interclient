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
#include "IB_Connection.h"
#include "IB_ConnectionConfiguration.h"
#include "IB_Transaction.h"
#include "IB_SQLException.h"
#include "IB_Status.h"
#include "IB_ResultSet.h"
#include "IB_Statement.h"

IB_Connection::~IB_Connection()
{
#ifdef TRACEON
  debugTraceAnInt ("destroying connection ", (IB_REF) this);
#endif
  close (IB_Transaction::ROLLBACK);
}

void
IB_Connection::open (const IB_ConnectionConfiguration& configuration,
		     const long attachmentCharSetCode)
{
#ifdef IB_USER_API
  if (dbHandle_)
    throw new IB_SQLException ("Attempt to open a connection which is already open");
#endif

  attachmentCharSetCode_ = attachmentCharSetCode;

  if (isc_attach_database (status_->vector(), 
			   databaseName_.length_, 
			   databaseName_.string_, 
			   &dbHandle_, 
			   configuration.parameterBlock_.length_, 
			   configuration.parameterBlock_.buffer_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       status_);
}

#ifdef IB_USER_API
void
IB_Connection::open ()
{
  if (dbHandle_) // attempt to  open a connection which is already open
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10001,
			       IB_SQLException::bugCheckException__);

  if (isc_attach_database (status_->vector(), 
			   databaseName_.length_,
			   databaseName_.string_, 
			   &dbHandle_, 
			   0, 
			   NULL))
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       status_);
}
#endif

#ifdef IB_USER_API
void
IB_Connection::open (const IB_LDString& userName,
		     const IB_LDString& password)
{ 
  IB_ConnectionConfiguration configuration;

  configuration.setUser (userName);
  configuration.setPassword (password);

  open (configuration);
}

void
IB_Connection::close ()
{
  // Check if the connection is already closed.
  if (!dbHandle_)
    return;

  // Check for active transactions
  if (!openTransactions_.isEmpty())
    throw new IB_SQLException ("Cannot close connection with open transactions", 
			       SQLStates::_25000,
                               ErrorCodes::invalidOperationError);

  if (isc_detach_database (status_->vector(), &dbHandle_))
    status_->setWarning (new IB_SQLException (status_, SQLStates::_01002));

  // This is used as a flag that connection is closed.
  dbHandle_ = NULL; // !!! This may be done by isc_detach_database
}
#endif

void
IB_Connection::close (const IB_Transaction::CloseOption closeOption)
{
  IB_SQLException* accumulatedExceptions = NULL;

  try {
    closeStatements ();
  }
  catch (IB_SQLException* e) {
    IB_SQLException::accumulate (accumulatedExceptions, e);
  }
    
  while (!openTransactions_.isEmpty ()) {
    IB_Transaction *t = (IB_Transaction*) openTransactions_.lastElement ();
    try {
      t->close (closeOption);
    }
    catch (IB_SQLException* e) {
      IB_SQLException::accumulate (accumulatedExceptions, e);
    }
  }

  try {
    if (isc_detach_database (status_->vector(), &dbHandle_))
      status_->setWarning (new IB_SQLException (IB_SQLException::engine__default_0__,
						status_));
  }
  catch (IB_SQLException* e) {
    IB_SQLException::accumulate (accumulatedExceptions, e);
  }

  // This is used as a flag that connection is closed.
  dbHandle_ = NULL; // !!! This may be done by isc_detach_database

  if (accumulatedExceptions)
    throw accumulatedExceptions;
}

void
IB_Connection::closeStatements ()
{
  IB_SQLException* accumulatedExceptions = NULL;

  while (!openStatements_.isEmpty ()) {
    IB_Statement *statement = (IB_Statement*) openStatements_.lastElement ();
    try {
      statement->close ();
    }
    catch (IB_SQLException* e) {
      IB_SQLException::accumulate (accumulatedExceptions, e);
    }
  }

  if (accumulatedExceptions)
    throw accumulatedExceptions;
}

void
IB_Connection::closeStatementsExcept (IB_Statement* statement)
{
  remOpenStatement (*statement);
  closeStatements ();
  addOpenStatement (*statement);
}

void
IB_Connection::closeResultSets ()
{
  IB_ResultSet* resultSet;

  IB_SQLException* accumulatedExceptions = NULL;

  VectorEnumeration openStatements (openStatements_);
  while (openStatements.hasMoreElements ()) {
    if (resultSet = ((IB_Statement*) openStatements.nextElement())->getResultSet()) {
      try {
	resultSet->close();
      }
      catch (IB_SQLException* e) {
	IB_SQLException::accumulate (accumulatedExceptions, e);
      }
    }
  }

  if (accumulatedExceptions)
    throw accumulatedExceptions;
}

#ifdef IB_USER_API
void
IB_Connection::dropDatabase ()
{
  // Check if the connection is closed.
  if (!dbHandle_)
    throw new IB_SQLException ("Connection must be open before dropping database",
			       SQLStates::_08003,
                               ErrorCodes::invalidOperationError);

  IB_SQLException* accumulatedExceptions = NULL;
  try {
    close (IB_Transaction::ROLLBACK);
  }
  catch (IB_SQLException* e) {
    IB_SQLException::accumulate (accumulatedExceptions, e);
  }

  try {
    if (isc_drop_database (status_->vector(), &dbHandle_))
      throw new IB_SQLException (status_);
  }
  catch (IB_SQLException* e) {
    IB_SQLException::accumulate (accumulatedExceptions, e);
  }

  // This is used as a flag that connection is closed.
  dbHandle_ = NULL; // !!! This may be done by isc_drop_database?

  if (accumulatedExceptions)
    throw accumulatedExceptions;
}
#endif


