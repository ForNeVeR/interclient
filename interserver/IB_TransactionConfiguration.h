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
#ifndef _IB_TRANSACTION_CONFIGURATION_H_
#define _IB_TRANSACTION_CONFIGURATION_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"
#include "IB_LDString.h"
#include "IB_Buffer.h"

// A transaction is configured by passing a configuration 
// to the IB_Transaction::setConnection() method for each
// connection distributed by the transaction.
// A configuration method may throw an IB_SQLException if 
// there is not enough memory on the heap for configuration data.
// All String arguments are copied into the parameter block.
// A configuration cannot be "undone" or "toggled".
// This would require a higher level class with more overhead
// in that it would need to keep the configuration in member variables
// rather than a tpb.

class IB_TransactionConfiguration {

private:
  
  friend class IB_Transaction;

  // holds tpb passed to isc_start_transaction()
  IB_Buffer parameterBlock_;

  IB_BUFF_PTR readWriteToggle_;
  IB_BUFF_PTR autoCommitToggle_;
  IB_BUFF_PTR isolation1Toggle_;
  IB_BUFF_PTR isolation2Toggle_;
  IB_BUFF_PTR lockResolutionToggle_;

public:

  // Unlike a connection configuration,
  // buffer memory is allocated for a declared transaction configuration,
  // even before you add your first parameter (by the constructor).
  // The default configuration uses InterBase defaults.
  // The Session layer must explicitly configure to match JDBC semantics.
  // The beginning of the tpb is reserved for toggleable
  // parameters described below.  The remaining is a non-toggleable, expanding configuration
  // similar to ConnectionConfiguration (dpb).
  IB_TransactionConfiguration();

  ~IB_TransactionConfiguration();

  // Static constants
  enum {
    tpbVersion__ = isc_tpb_version3,
    tpbAllocationIncrement__ = 20
  };

  // These values must match the hardwired values used by interclient.
  enum IsolationLevel {
    NONE = 0,
    READ_UNCOMMITTED = 1, // not supported
    READ_COMMITTED = 2,   // read_committed
    REPEATABLE_READ = 4,  // concurrency
    SERIALIZABLE = 8,      // concurrency
    SNAPSHOT_TABLE_STABILITY = 16 // consistency
  };

  enum versionAcknowledgementMode {
    RECOGNIZE_UNCOMMITTED_RECORD_VERSIONS_ON_READ = 0, // default
    IGNORE_UNCOMMITTED_RECORD_VERSIONS_ON_READ = 1
  };

  enum LockResolutionMode {
    WAIT = 0, // default
    NO_WAIT = 1
  };

  // These values must match the value passed on the wire.
  enum AccessMode {
    READ_ONLY = IB_TRUE,
    READ_WRITE = IB_FALSE
  };

  enum TableLockMode {
    SHARED_WRITE = 0,
    SHARED_READ = 1,
    PROTECTED_WRITE = 2,
    PROTECTED_READ = 4
  };

  // WARNING: clobbers isolation mode!!!
  // Must be called after setIsolation!!!!!!!
  void setAutoCommit (const IB_BOOLEAN enableAutoCommit);

  // Default access mode is READ_WRITE.
  // READ_WRITE access mode allows a transaction to select, insert,
  // update, and delete table data.
  // READ_ONLY access mode allows a transaction only to select
  // data from tables.
  void setAccess (const AccessMode mode);

  // SNAPSHOT is the default isolation mode.
  // Also known as concurrency mode.
  // These transactions keep a snapshot of the database
  // when the transaction starts and do not 'see' the committed updates
  // of simultaneous transactions.
  // This provides a non-serializable form of consistency in which
  // writers don't block readers, and readers don't block writers.
  // This isolation level is possible because of InterBase's
  // multi-generational architecture.
  //
  // SNAPSHOT_TABLE_STABILITY ensure exclusive write-access 
  // to tables which are being modified.
  //
  // READ_MOST_RECENT_COMMITTED_VERSION reads the most recent 
  // committed updates of simultaneous transactions
  // are readable.
  // 
  // READ_LATEST_COMMITTED_VERSION means only the last committed updates of 
  // simultaneous transactions
  // are readable.
  // If an uncommitted update is more recent, then this transaction
  // will either wait on read or throw a LockConflictException,
  // depending on the lock resolution mode.
  void setIsolation (const IsolationLevel isolation);

  void setVersionAcknowledgement (const IB_BOOLEAN enableRecordVersion);

  // WAIT is the default lock resolution mode.
  // Lock resolution protocol in which a transaction is to wait until 
  // locked resources are released before resuming.
  // 
  // NO_WAIT is the lock resolution protocol in which a transaction is not to wait for
  // locks to be released, but instead, a LockConflictException is thrown.
  void setLockResolution (const LockResolutionMode mode);

  // Maximum tableName length is 255.
  // tableName string is copied into configuration object, not shared.
  void setTableLock (const TableLockMode mode,
		     const IB_LDString tableName);

  // Permit table writes by concurrency and read-committed mode 
  // transactions with write access;
  // Permit table reads by concurrency and read-committed mode
  // transactions with read access.
  // Maximum tableName length is 255.
  void setTableLock_SharedWrite (const IB_LDString tableName);

  // Permit table writes by any transaction with write access;
  // Permit table reads by any transaction.
  // Maximum tableName length is 255.
  void setTableLock_SharedRead (const IB_LDString tableName);

  // Permit exclusive table writes only;
  // Permit table reads by concurrency and read-committed mode
  // transactions with read access.
  // Maximum tableName length is 255.
  void setTableLock_ProtectedWrite (const IB_LDString tableName);
	
  // Disallow all table writes;
  // Permit table reads by all transactions.
  // Maximum tableName length is 255.
  void setTableLock_ProtectedRead (const IB_LDString tableName);

private:

  void initialAllocation ();

  // For configuration parameters with NO arguments.
  // This is used only for transaction configurations, connection configuration
  // parameters always have arguments.
  // Throws IB_SQLException if there is insufficient memory on the heap.
  void addParameter (const IB_UBYTE iscParameter);

  // tpb table name arguments are NOT preceded by a length indicator as dpb
  // string arguments are.
  // However, if we didn't pass the tableNameLength then a call to strlen()
  // would be necesary to get numBytes for call to expandParameterBlockBy().
  // String is copied into tpb.
  // Throws IB_SQLException if there is insufficient memory on the heap.
  void addTableName (const IB_LDString tableName);

  // The following method is used by IB_Transaction::setConnection() to 
  // test if the configuration passed to it specifies readOnly or readWrite.
  // We need a readOnly() method so that transaction can check on
  // stop() or refresh() that transaction is read-only.
  // Without this we'd have to search the tpb for the isc_tpb_read token.
  // See setAccess_ReadOnly() method.
  friend class IB_Transaction;
  IB_BOOLEAN readOnly () const;

};

#endif
