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
#ifndef _IB_CONNECTION_INFORMATION_H_
#define _IB_CONNECTION_INFORMATION_H_

#include "IB_Defines.h"
#include "IB_Information.h"

class IB_Connection;
class IB_Status;
class IB_LDString;

class IB_ConnectionInformation: public IB_Information {

private:

  IB_Connection* connection_;

public:

  // Throw IB_SQLException if there is not enough memory on the heap.
  IB_ConnectionInformation (const IB_Status& status,
			    const IB_Connection& connection);

  ~IB_ConnectionInformation ();

  // Database characteristics

  // output ibVersion up to 255 characters.
  void getVersion (IB_LDString& ibVersion) const;

  void getODSMajorVersion (IB_SLONG32& majorVersionNumber) const;

  void getODSMinorVersion (IB_SLONG32& minorVersionNumber) const;

  void getPageSize (IB_SLONG32& bytesPerPage) const;

  void getPageAllocation (IB_SLONG32& numDatabasePages) const; 

// CJL added support for Database SQL Dialect and read-only status
  void getDatabaseSQLDialect(IB_SLONG32& dbSQLDialect) const;

  void getDatabaseReadOnly(IB_BOOLEAN& dbReadOnly) const;
// CJL end

  void getBaseLevel (IB_SLONG32& versionNumber) const;

  // output dbFileName up to 255 characters.
  // output siteName up to 255 characters.
  void getDBId (IB_LDString& dbFileName, 
		IB_LDString& siteName) const;

  void getImplementation (IB_UBYTE& implementationNumber,
			  IB_UBYTE& classNumber) const;

  void getNoReserve (IB_BOOLEAN& noReserveSpace) const;

  // Environmental characteristics
  void getCurrentMemory (IB_SLONG32& bytesServerMemInUse) const; // !!! guessing type

  void getForcedWrites (IB_BOOLEAN& synchronous) const;

  void getMaxMemory (IB_SLONG32& bytesMaxUsedSoFar) const; // !!! guessing type

  void getNumBuffers (IB_UBYTE& memoryBuffers) const; // !!! guessing type

  void getSweepInterval (IB_SLONG32& interval) const; // !!! guessing type

  // !!! void getUserNames (RWTPtrSlist<RWCString>& userNameList) const;

  // Performance statistics
  void getFetches (IB_SLONG32& numFetches) const;

  void getMarks (IB_SLONG32& numMarks) const;

  void getReads (IB_SLONG32& numReads) const;

  void getWrites (IB_SLONG32& numWrites) const;

  // Database operation counts
  void getBackoutCount (IB_SSHORT16& tableId,
			IB_SLONG32& numBackouts) const;

  void getDeleteCount (IB_SSHORT16& tableId,
		       IB_SLONG32& numDeletes) const;

  void getExpungeCount (IB_SSHORT16& tableId,
			IB_SLONG32& numExpunges) const;

  void getInsertCount (IB_SSHORT16& tableId,
		       IB_SLONG32& numInserts) const;

  void getPurgeCount (IB_SSHORT16& tableId,
		      IB_SLONG32& numPurges) const;

  void getIndexedReadCount (IB_SSHORT16& tableId,
			    IB_SLONG32& numIndexedReads) const;

  void getSequentialReadCount (IB_SSHORT16& tableId,
			       IB_SLONG32& numSequentialReads) const;

  void getUpdateCount (IB_SSHORT16& tableId,
		       IB_SLONG32& numUpdates) const;

private:

  virtual void issueRequest ();

};

#endif
