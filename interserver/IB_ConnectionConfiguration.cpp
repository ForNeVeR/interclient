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
#include "IB_ConnectionConfiguration.h"
#include "IB_Status.h"

IB_ConnectionConfiguration::~IB_ConnectionConfiguration ()
{ }

void
IB_ConnectionConfiguration::initialAllocation ()
{
  parameterBlock_.allocate (dpbAllocationIncrement__);

  parameterBlock_.expandBy (1);
  *parameterBlock_.buffer_ = (IB_BUFF_CHAR) dpbVersion__;
  parameterBlock_.incrementLength (1);
}

void
IB_ConnectionConfiguration::addParameter (const IB_UBYTE iscParameter, 
					  const IB_LDString ldString)
{
  if (!parameterBlock_.isAllocated ())
    initialAllocation ();

  parameterBlock_.expandBy (ldString.length_ + 2);

  IB_BUFF_PTR argumentIterator = ldString.string_;

  parameterBlock_.nextAvailableMemory ();

  *parameterBlock_.next() = (IB_BUFF_CHAR) iscParameter;
  *parameterBlock_.next() = (IB_BUFF_CHAR) (IB_UBYTE) ldString.length_;
  for (IB_UBYTE i = (IB_UBYTE) ldString.length_; i; i--)
    *parameterBlock_.next() = *argumentIterator++;

  parameterBlock_.incrementLength (ldString.length_ + 2);
}

void
IB_ConnectionConfiguration::addParameter (const IB_UBYTE iscParameter,
					  const IB_UBYTE intSize,
					  const IB_INT intValue)
{
  if (!parameterBlock_.isAllocated ())
    initialAllocation ();

  // 1 for iscParameter, 1 for length, intSize for integer argument
  parameterBlock_.expandBy (2 + intSize);

  parameterBlock_.nextAvailableMemory();

  *parameterBlock_.next() = (IB_BUFF_CHAR) iscParameter;
  *parameterBlock_.next() = (IB_BUFF_CHAR) intSize;   // length of integer data
  parameterBlock_.writeInteger (intSize, intValue);

  parameterBlock_.incrementLength (2 + intSize);
}

void
IB_ConnectionConfiguration::setSweepInterval (const IB_SLONG32 transactions)
{
  addParameter ((IB_UBYTE) isc_dpb_sweep_interval,
		(IB_UBYTE) sizeof (IB_SLONG32),
		transactions);
}

void 
IB_ConnectionConfiguration::disableSweep ()
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void 
IB_ConnectionConfiguration::sweep ()
{
  addParameter ((IB_UBYTE) isc_dpb_sweep,
		1,
		(IB_UBYTE) 1);
}

void 
IB_ConnectionConfiguration::shutdown ()
{
  addParameter ((IB_UBYTE) isc_dpb_shutdown,
		1,
		(IB_UBYTE) 1);
}

void 
IB_ConnectionConfiguration::setVerify (const IB_LDString options)
{
  if (strcmp (options.string_, "none") == 0)
    return;

  // isc_dpb_verify if validate (gfix -validate)
  // isc_dpb_ignore if validate (gfix -validate)
  // isc_dpb_indices if validate (gfix -validate)
  // isc_dpb_transactions if validate (gfix -validate)
  // isc_dpb_pages if validate (gfix -validate)

  addParameter ((IB_UBYTE) isc_dpb_verify,
	        1,
		(IB_UBYTE) 1);
  addParameter ((IB_UBYTE) isc_dpb_ignore,
	        1,
		(IB_UBYTE) 1);
  addParameter ((IB_UBYTE) isc_dpb_indices,
	        1,
		(IB_UBYTE) 1);
  addParameter ((IB_UBYTE) isc_dpb_transactions,
	        1,
		(IB_UBYTE) 1);
  addParameter ((IB_UBYTE) isc_dpb_pages,
	        1,
		(IB_UBYTE) 1);

  // isc_dpb_records if validate & checkAllRecords (gfix -validate -full)
  // isc_dpb_repair if validate & mend (gfix -validate -mend)
  // isc_dpb_no_update if validate & !mend (gfix -validate -no_update)

  // "validate", no -mend and no -full
  if (strcmp (options.string_, "validate") == 0)
    return;
  // "validateMend", no -full and -mend
  if (strcmp (options.string_, "validateMend") == 0) {
    addParameter ((IB_UBYTE) isc_dpb_repair,
	          1,
		  (IB_UBYTE) 1);
  }
  // "validateFull", -full and -no_update
  else if (strcmp (options.string_, "validateFull") == 0) {
    addParameter ((IB_UBYTE) isc_dpb_records,
	          1,
		  (IB_UBYTE) 1);
    addParameter ((IB_UBYTE) isc_dpb_no_update,
	          1,
		  (IB_UBYTE) 1);
  }
  // "validateFullMend", -full and -mend
  else if (strcmp (options.string_, "validateFullMend") == 0) {
    addParameter ((IB_UBYTE) isc_dpb_records,
	          1,
		  (IB_UBYTE) 1);
    addParameter ((IB_UBYTE) isc_dpb_repair,
	          1,
		  (IB_UBYTE) 1);
  }
}

void 
IB_ConnectionConfiguration::setMaxNumUsers (const IB_SSHORT16 numUsers)
{
  addParameter ((IB_UBYTE) isc_dpb_number_of_users,
		(IB_UBYTE) sizeof (IB_SSHORT16),
		numUsers);
}

void
IB_ConnectionConfiguration::setConnectTimeout (const IB_SLONG32 seconds)
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionConfiguration::setDummyPacketInterval (const IB_SLONG32 seconds)
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_ConnectionConfiguration::activateShadow ()
{
  addParameter ((IB_UBYTE) isc_dpb_activate_shadow,
		1,
		(IB_UBYTE) 0);
};

void
IB_ConnectionConfiguration::deleteShadow ()
{
  addParameter ((IB_UBYTE) isc_dpb_delete_shadow,
		1,
		(IB_UBYTE) 0);
};

void
IB_ConnectionConfiguration::markDamaged ()
{
  addParameter ((IB_UBYTE) isc_dpb_damaged,
		1,
		(IB_UBYTE) 1);
};

void
IB_ConnectionConfiguration::extendDBKeyScope ()
{
  addParameter ((IB_UBYTE) isc_dpb_dbkey_scope,
		1,
		(IB_UBYTE) 1);
};

void
IB_ConnectionConfiguration::setEncryptionKey (const IB_LDString key)
{
  addParameter ((IB_UBYTE) isc_dpb_encrypt_key, 
		key);
};

void
IB_ConnectionConfiguration::forceSynchronousWrites ()
{
  addParameter ((IB_UBYTE) isc_dpb_force_write, 
		1,
		(IB_UBYTE) 1);
};

void
IB_ConnectionConfiguration::setCharacterSet (const IB_LDString ctype)
{
  addParameter ((IB_UBYTE) isc_dpb_lc_ctype, 
		ctype);
};

void
IB_ConnectionConfiguration::setMessages (const IB_LDString languageSpecificMessageFile)
{
  addParameter ((IB_UBYTE) isc_dpb_lc_messages, 
		 languageSpecificMessageFile);
};

void
IB_ConnectionConfiguration::noReserveSpaceForBackRecVersions ()
{
  addParameter ((IB_UBYTE) isc_dpb_no_reserve, 
		1,
		(IB_UBYTE) 1);
};

void
IB_ConnectionConfiguration::setNumBuffers (const IB_UBYTE numCacheBuffers)
{
  addParameter ((IB_UBYTE) isc_dpb_num_buffers,
		1,
		numCacheBuffers);
}

void
IB_ConnectionConfiguration::setPassword (const IB_LDString password)
{
  addParameter ((IB_UBYTE) isc_dpb_password, 
		password);
};

void
IB_ConnectionConfiguration::setRole (const IB_LDString role)
{
  addParameter ((IB_UBYTE) isc_dpb_sql_role_name, 
		role);
};

void
IB_ConnectionConfiguration::setEncryptedPassword (const IB_LDString encryptedPassword)
{
  addParameter ((IB_UBYTE) isc_dpb_password_enc, 
		encryptedPassword); 
};

void
IB_ConnectionConfiguration::setSystemUserName (const IB_LDString sysDBAName)
{
  addParameter ((IB_UBYTE) isc_dpb_sys_user_name,
		sysDBAName);
};

void
IB_ConnectionConfiguration::setUser (const IB_LDString userName)
{
  addParameter ((IB_UBYTE) isc_dpb_user_name, 
		userName);
};

// CJL added support for SQL Dialect
void
IB_ConnectionConfiguration::setSQLDialect (const IB_USHORT16 sqlDialect)
{
  addParameter ((IB_UBYTE) isc_dpb_SQL_dialect, 
		(IB_UBYTE) sizeof (IB_USHORT16),
		sqlDialect);
};
// CJL-IB6 end 
