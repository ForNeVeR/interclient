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
#include <string.h>  // for strcpy on tableName

#include "IB_Status.h"
#include "IB_TransactionConfiguration.h"

IB_TransactionConfiguration::IB_TransactionConfiguration ()
{ 
  initialAllocation ();
}

IB_TransactionConfiguration::~IB_TransactionConfiguration ()
{ }

void
IB_TransactionConfiguration::initialAllocation ()
{
  // The 1st byte will contain the non-toggleable TPB version token,
  // followed by toggleable parameters in the next 4 bytes:
  // 2nd byte: isc_tpb_write/isc_tpb_read
  // 3rd byte: isc_tpb_autocommit (if this is not set fill it with whatever's in byte 2)
  // 4th byte: isc_tpb_consistency, concurrency, read_committed
  // 5th byte: isc_tpb_rec_version, isc_tpb_no_rec_version
  // The 5th byte will repeat whatever's in the 4th byte if this is not a read committed trans.

  parameterBlock_.allocate (tpbAllocationIncrement__);

  parameterBlock_.expandBy (6);

  parameterBlock_.nextAvailableMemory ();

  *parameterBlock_.next() = tpbVersion__;
  *(readWriteToggle_ = parameterBlock_.next()) = isc_tpb_write;
  *(autoCommitToggle_ = parameterBlock_.next()) = isc_tpb_write;
  *(isolation1Toggle_ = parameterBlock_.next()) = isc_tpb_concurrency;
  *(isolation2Toggle_ = parameterBlock_.next()) = isc_tpb_concurrency;
  *(lockResolutionToggle_ = parameterBlock_.next()) = isc_tpb_nowait;

  parameterBlock_.incrementLength (6);
}

void
IB_TransactionConfiguration::addParameter (const IB_UBYTE iscParameter)
{
  // 1 for iscParameter, no length indicator, no argument
  parameterBlock_.expandBy (1);

  *(parameterBlock_.nextAvailableMemory()) = (IB_BUFF_CHAR) iscParameter;

  parameterBlock_.incrementLength (1);
}

void
IB_TransactionConfiguration::addTableName (const IB_LDString tableName)
{
  parameterBlock_.expandBy (tableName.length_ + 1); // 1 for null terminator
  
  strcpy (parameterBlock_.nextAvailableMemory(), tableName.string_);

  parameterBlock_.incrementLength (tableName.length_ + 1);
}

// must be called after setAccess()
void
IB_TransactionConfiguration::setAutoCommit (const IB_BOOLEAN enableAutoCommit)
{
  if (enableAutoCommit) {
    *autoCommitToggle_ = isc_tpb_autocommit;
    *isolation1Toggle_ = isc_tpb_read_committed;
    *isolation2Toggle_ = isc_tpb_rec_version;
  }
  else
    *autoCommitToggle_ = *readWriteToggle_;
}

void
IB_TransactionConfiguration::setAccess (const AccessMode mode)
{
  switch (mode) {
  case READ_ONLY:
    *readWriteToggle_ = isc_tpb_read;
    break;
  case READ_WRITE:
    *readWriteToggle_ = isc_tpb_write;
    break;
  }
}

void
IB_TransactionConfiguration::setIsolation (const IsolationLevel isolation)
{
  switch (isolation) {
  case READ_COMMITTED: 
    *isolation1Toggle_ = isc_tpb_read_committed;
    *isolation2Toggle_ = isc_tpb_read_committed;
    break;
  case REPEATABLE_READ:
  case SERIALIZABLE:
    *isolation1Toggle_ = isc_tpb_concurrency;
    *isolation2Toggle_ = isc_tpb_concurrency;
    break;
  case SNAPSHOT_TABLE_STABILITY:
    *isolation1Toggle_ = isc_tpb_consistency;
    *isolation2Toggle_ = isc_tpb_consistency;
    break;
  case NONE:
  case READ_UNCOMMITTED:
    throw new IB_SQLException (IB_SQLException::driverNotCapable__isolation__,
			       IB_SQLException::driverNotCapableException__);
  }
}

// May only be called after setIsolation() has been called!
void
IB_TransactionConfiguration::setVersionAcknowledgement (const IB_BOOLEAN recordVersion)
{
  if (*isolation1Toggle_ != isc_tpb_read_committed) {
    *isolation2Toggle_ = *isolation1Toggle_;
    return;
  }

  if (recordVersion) // read most recent commited version
    *isolation2Toggle_ = isc_tpb_rec_version;
  else // read latest committed version
    *isolation2Toggle_ = isc_tpb_no_rec_version;
}

void
IB_TransactionConfiguration::setLockResolution (const LockResolutionMode mode)
{
  switch (mode) {
  case WAIT:
    *lockResolutionToggle_ = isc_tpb_wait;
    break;
  case NO_WAIT:
    *lockResolutionToggle_ = isc_tpb_nowait;
    break;
  }
}

void
IB_TransactionConfiguration::setTableLock (const TableLockMode mode,
					   const IB_LDString tableName)
{
  switch (mode) {
  case SHARED_WRITE:
    setTableLock_SharedWrite (tableName);
    break;
  case SHARED_READ:
    setTableLock_SharedRead (tableName);
    break;
  case PROTECTED_WRITE:
    setTableLock_ProtectedWrite (tableName);
    break;
  case PROTECTED_READ:
    setTableLock_ProtectedRead (tableName);
    break;
  }
}

void 
IB_TransactionConfiguration::setTableLock_SharedWrite (const IB_LDString tableName)
{
  addParameter ((IB_UBYTE) isc_tpb_shared);
  addParameter ((IB_UBYTE) isc_tpb_lock_write);
  addTableName (tableName);
}

void 
IB_TransactionConfiguration::setTableLock_SharedRead (const IB_LDString tableName)
{
  addParameter ((IB_UBYTE) isc_tpb_shared);
  addParameter ((IB_UBYTE) isc_tpb_lock_read);
  addTableName (tableName);
}

void 
IB_TransactionConfiguration::setTableLock_ProtectedWrite (const IB_LDString tableName)
{
  addParameter ((IB_UBYTE) isc_tpb_protected);
  addParameter ((IB_UBYTE) isc_tpb_lock_write);
  addTableName (tableName);
}

void 
IB_TransactionConfiguration::setTableLock_ProtectedRead (const IB_LDString tableName)
{
  addParameter ((IB_UBYTE) isc_tpb_protected);
  addParameter ((IB_UBYTE) isc_tpb_lock_read);
  addTableName (tableName);
}

IB_BOOLEAN
IB_TransactionConfiguration::readOnly () const
{
  return (*readWriteToggle_ == isc_tpb_read);
}




