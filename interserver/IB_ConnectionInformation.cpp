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
#include "IB_ConnectionInformation.h"
#include "IB_SQLException.h"
#include "IB_Connection.h"
#include "IB_Status.h"
#include "IB_LDString.h"

IB_ConnectionInformation::IB_ConnectionInformation (const IB_Status& status,
						    const IB_Connection& connection)
  : IB_Information (status),
    connection_ ((IB_Connection*) &connection)
{ }

IB_ConnectionInformation::~IB_ConnectionInformation ()
{ }

void
IB_ConnectionInformation::issueRequest ()
{
  if (isc_database_info (status_->vector(),
			 connection_->dbHandleP(),
			 request_.length_,
			 request_.buffer_,
			 requestedInfo_.size_,
			 requestedInfo_.buffer_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

void
IB_ConnectionInformation::getVersion (IB_LDString& ibVersion) const
{
  IB_BUFF_PTR p = findInfoItem (isc_info_version);
  p = p + 2; // skip over cluster length indicator
  p = p + 1; // skip over a byte with fixed value 1
  ibVersion.length_ = (short) *p;
  ibVersion.string_ = p+1;
}

void
IB_ConnectionInformation::getODSMajorVersion (IB_SLONG32& majorVersionNumber) const
{
  majorVersionNumber = getInteger (findInfoItem (isc_info_ods_version));
}

void
IB_ConnectionInformation::getODSMinorVersion (IB_SLONG32& minorVersionNumber) const
{
  minorVersionNumber = getInteger (findInfoItem (isc_info_ods_minor_version));
}

void
IB_ConnectionInformation::getPageSize (IB_SLONG32& bytesPerPage) const
{
  bytesPerPage = getInteger (findInfoItem (isc_info_page_size));
}

void
IB_ConnectionInformation::getPageAllocation (IB_SLONG32& numDatabasePages) const
{
  numDatabasePages = getInteger (findInfoItem (isc_info_allocation));
}
// CJL added for IB6 functionality
void 
IB_ConnectionInformation::getDatabaseSQLDialect (IB_SLONG32& dbSQLDialect) const
{
  if ( getInteger (findInfoItem (isc_info_ods_version)) < 10 )
    dbSQLDialect = 1;  // not IB6 server, use default dialect.
  // !!!CJL-IB6 -- Should we produce a Dialect Adjustment Warning in this instance?
  // !!! on second thought, user may never have specified a dialect,
  // !!! warning might be alarmist.
  else
    dbSQLDialect = getInteger (findInfoItem (isc_info_db_SQL_dialect));
}

void 
IB_ConnectionInformation::getDatabaseReadOnly (IB_BOOLEAN& dbReadOnly) const
{
  if ( getInteger (findInfoItem (isc_info_ods_version)) < 10 )
    dbReadOnly = IB_FALSE;  // not IB6 server, db must be read-write.
  else
    dbReadOnly = TO_BOOLEAN( 
	   getInteger (findInfoItem (isc_info_db_read_only))
	   );
}

// CJL end
void
IB_ConnectionInformation::getBaseLevel (IB_SLONG32& versionNumber) const
{
  versionNumber = *(findInfoItem (isc_info_base_level) 
		    + 2   // skip over length indicator
		    + 1); // skip over a byte with fixed value 1
}

void
IB_ConnectionInformation::getDBId (IB_LDString& dbFileName, 
				   IB_LDString& siteName) const
{
  findInfoItem (isc_info_db_id);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getImplementation (IB_UBYTE& implementationNumber,
					     IB_UBYTE& classNumber) const
{
  findInfoItem (isc_info_implementation);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getNoReserve (IB_BOOLEAN& noReserveSpace) const
{
  findInfoItem (isc_info_no_reserve);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getCurrentMemory (IB_SLONG32& bytesServerMemInUse) const
{
  findInfoItem (isc_info_current_memory);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getForcedWrites (IB_BOOLEAN& synchronous) const
{
  findInfoItem (isc_info_forced_writes);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getMaxMemory (IB_SLONG32& bytesMaxUsedSoFar) const
{
  findInfoItem (isc_info_max_memory);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getNumBuffers (IB_UBYTE& memoryBuffers) const
{
  findInfoItem (isc_info_num_buffers);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getSweepInterval (IB_SLONG32& interval) const
{
  findInfoItem (isc_info_sweep_interval);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);

}

void
IB_ConnectionInformation::getFetches (IB_SLONG32& numFetches) const
{
  findInfoItem (isc_info_fetches);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getMarks (IB_SLONG32& numMarks) const
{
  findInfoItem (isc_info_marks);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getReads (IB_SLONG32& numReads) const
{
  findInfoItem (isc_info_reads);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getWrites (IB_SLONG32& numWrites) const
{
  findInfoItem (isc_info_writes);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getBackoutCount (IB_SSHORT16& tableId,
					   IB_SLONG32& numBackouts) const
{
  findInfoItem (isc_info_backout_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getDeleteCount (IB_SSHORT16& tableId,
					  IB_SLONG32& numDeletes) const
{
  findInfoItem (isc_info_delete_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getExpungeCount (IB_SSHORT16& tableId,
					   IB_SLONG32& numExpunges) const
{
  findInfoItem (isc_info_expunge_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getInsertCount (IB_SSHORT16& tableId,
					  IB_SLONG32& numInserts) const
{
  findInfoItem (isc_info_insert_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getPurgeCount (IB_SSHORT16& tableId,
					 IB_SLONG32& numPurges) const
{
  findInfoItem (isc_info_purge_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getIndexedReadCount (IB_SSHORT16& tableId,
					       IB_SLONG32& numIndexedReads) const
{
  findInfoItem (isc_info_read_idx_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getSequentialReadCount (IB_SSHORT16& tableId,
						  IB_SLONG32& numSequentialReads) const
{
  findInfoItem (isc_info_read_seq_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionInformation::getUpdateCount (IB_SSHORT16& tableId,
					  IB_SLONG32& numUpdates) const
{
  findInfoItem (isc_info_update_count);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}
