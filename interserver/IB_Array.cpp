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
#include "IB_Array.h"
#include "IB_Status.h"
#include "IB_Transaction.h"
#include "IB_Connection.h"
#include "IB_Statement.h"

IB_Array::IB_Array (const IB_Statement& statement,
		    const IB_STRING columnName,
		    const IB_STRING tableName,
		    const IB_ARRAYID arrayId) 

  : statement_ ((IB_Statement*) &statement),
    columnName_ (columnName),
    tableName_ (tableName),
    arrayId_ (arrayId)
{ }

IB_Array::IB_Array (const IB_Statement& statement,
		    const IB_STRING columnName,
		    const IB_STRING tableName)
  : statement_ ((IB_Statement*) &statement),
    columnName_ (columnName),
    tableName_ (tableName)
{ 
  // !!! how to zero out arrayId_
}

IB_Array::~IB_Array ()
{
  close ();
}

void
IB_Array::createArrayDesc ()
{
  if (isc_array_lookup_bounds (statement_->status_->vector(), 
			       statement_->connection_->dbHandleP(), 
			       statement_->transaction_->trHandleP(),
			       tableName_,
			       columnName_,
			       &arrayDesc_)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__, 
			       statement_->status_);
 
  statement_->addOpenArray (this);
}

IB_SLONG32
IB_Array::get (IB_BUFF_PTR arrayBuffer)
{
  if (isc_array_get_slice (statement_->status_->vector(), 
			   statement_->connection_->dbHandleP(), 
			   statement_->transaction_->trHandleP(),
			   &arrayId_,
			   &arrayDesc_,
			   arrayBuffer,
			   &maxSliceLength_)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__, 
			       statement_->status_); 
  // !!! ok, now return the length of data fetched.
  return 0;
}

void
IB_Array::put (const IB_SLONG32 arrayBufferLength,
	       const IB_BUFF_PTR arrayBuffer)
{
  if (isc_array_put_slice (statement_->status_->vector(), 
			   statement_->connection_->dbHandleP(), 
			   statement_->transaction_->trHandleP(),
			   &arrayId_,
			   &arrayDesc_,
			   arrayBuffer,
			   (IB_SLONG32*) &arrayBufferLength)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       statement_->status_); 
}

void
IB_Array::setUpperBound (IB_SSHORT16 row, IB_UBYTE value)
{
  //!!! arrayDesc_[row].array_bound_upper == value;
}

void
IB_Array::setLowerBound (IB_SSHORT16 row, IB_UBYTE value)
{
  //!!! arrayDesc_[row].array_lower_upper == value;
}

void
IB_Array::close ()
{
  statement_->remOpenArray (this);
}

