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
#ifndef _IB_CONNECTION_CONFIGURATION_H_
#define _IB_CONNECTION_CONFIGURATION_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"
#include "IB_LDString.h"
#include "IB_Buffer.h"

// A connection is configured by passing a configuration on open.
// A configuration method may throw an IB_SQLException if 
// there is not enough memory on the heap for configuration data.
// All String arguments are copied into the parameter block.
// Adding parameters does not search for duplicates,
// so you shouldn't use the same configuration method twice on 
// a single configuration object.
// That is, you can't undo a configuration.

class IB_ConnectionConfiguration {

private:

  friend class IB_Connection;

  // holds dpb passed to isc_attach_database
  IB_Buffer parameterBlock_;

  // Static constants
  enum {
    dpbVersion__ = isc_dpb_version1,
    dpbAllocationIncrement__ = 64
  };

public:

  IB_ConnectionConfiguration();

  ~IB_ConnectionConfiguration();
  
  void setSweepInterval (const IB_SLONG32 transactions);

  void disableSweep ();

  void sweep ();

  void shutdown ();

  void setMaxNumUsers (const IB_SSHORT16 numUsers);

  // How long to wait before aborting connection request.
  // default is 180
  void setConnectTimeout (const IB_SLONG32 seconds);

  // Specify period for asking "Are you alive?"
  // default is 60
  void setDummyPacketInterval (const IB_SLONG32 seconds);

  // Activate a database shadow, which is a duplicate, in-sync copy of 
  // the database.
  void activateShadow ();

  // Delete the database shadow file.
  void deleteShadow ();

  // Marks the database as damaged.
  void markDamaged ();

  // By default the scope of dbkey context is limited to the
  // current transaction.
  // This method is used to extend the scope to the database session.
  void extendDBKeyScope ();

  // String encryption key, up to 255 characters.
  void setEncryptionKey (const IB_LDString key);

  // Force database writes to be synchronous.
  // By default writes are asynchronous. 
  void forceSynchronousWrites ();

  // Specify the character set to be utilized.
  void setCharacterSet (const IB_LDString ctype);

  // Specify language specific message file to be used.
  void setMessages (const IB_LDString languageSpecificMessageFile);

  // By default a small amount of space on each database page is
  // be reserved for holding backup versions of records when modifications
  // are made.  Keeping backup versions on on the same page as the primary
  // record optimizes update activity.
  // This method specifies that no such space be reserved.
  void noReserveSpaceForBackRecVersions ();

  // Number of database cache buffers to allocate for use with the database.
  // default is 75.
  // !!! this should really be a USHORT
  void setNumBuffers (const IB_UBYTE numCacheBuffers);

  // User name up to 255 characters.
  void setUser (const IB_LDString userName);

  // User password up to 255 characters.
  void setPassword (const IB_LDString password);

  // User role up to 255 characters.
  void setRole (const IB_LDString role);

  // Encrypted password up to 255 characters.
  void setEncryptedPassword (const IB_LDString encryptedPassword);

  // System DBA name up to 255 characters.
  void setSystemUserName (const IB_LDString sysDBAName);

  // Verify internal structures.
  void setVerify (const IB_LDString options);

  // CJL-IB6 add SQL Dialect support
  // IB6 SQLDialect values 1-3
  void setSQLDialect (const IB_USHORT16 sqlDialect);
  // CJL-IB6 end add.
private:

  // Throw IB_SQLException if there is not enough memory on the heap.
  void initialAllocation ();

  // isc_expand_dpb only handles 5 of 15 different dpb parameters.
  // Also there is a switch statement in isc_expand_dpb that is unnecessary
  // if a length argument is passed.
  // There is also an error in how isc_expand_dpb reallocs memory
  // in that it confused length with size in the code so that reallocs
  // and buffer copies always occurred on every call to isc_expand_dpb
  // even if sufficient unused buffer memory was available.
  // I haven't checked the code lately to see if the bug I logged is fixed.
  // The parameter block is allocated on an "as needed" basis in 
  // memoryAllocationIncrement byte blocks.
  // Parameter types per isc (char, char, char*)
  // Space used =  1 byte for iscParameter + 1 byte for length + length of argument
  // argumentLength = ldString.length_
  // Calls parameterBlock_.expandBy (argumentLength+2) and then 
  // increases parameterBlock_.length_ by argumentLength + 2.
  // Throws IB_SQLException if there is insufficient memory on the heap.
  void addParameter (const IB_UBYTE iscParameter, 
		     const IB_LDString ldString);

  // For integer arguments of lengths up to a word.
  // Throws IB_SQLException if there is insufficient memory on the heap.
  void addParameter (const IB_UBYTE iscParameter,
		     const IB_UBYTE intSize,
		     const IB_INT intValue);

};

inline
IB_ConnectionConfiguration::IB_ConnectionConfiguration ()
{ }

#endif
