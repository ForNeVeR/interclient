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
#include <stdlib.h> // malloc, free

#include "IB_ResultSet.h"
#include "IB_Types.h"
#include "IB_Status.h"
#include "IB_CharacterSets.h"

IB_ResultSet::~IB_ResultSet ()
{
#ifdef TRACEON
  debugTraceAnInt ("destroying result set: ", (IB_REF) this);
#endif
  // harmless to free a NULL pointer.
  free (buffer_);
}

// Called by both JIBSRemote.putResultData for CHARs, but not CHAR NONEs.
IB_SSHORT16
IB_ResultSet::getCharTrimmedByteLength (const IB_SSHORT16 columnIndex) const
{
  // !!! make sure I can read a string of all spaces, ie. zero length
  int length = (sqlda_->sqlvar+columnIndex)->sqllen;
  while ((length > 0) &&
         (*((sqlda_->sqlvar+columnIndex)->sqldata+length-1) == 32)) {
    length--;
  }

  if (maxFieldSize_) {
    return MIN (maxFieldSize_, length);
  }
  else
    return length;
}

// Called by JIBSRemote.putResultData only for CHAR NONEs (no right trimming).
IB_SSHORT16
IB_ResultSet::getCharByteLength (const IB_SSHORT16 columnIndex) const
{
  if (maxFieldSize_) // maxFieldSize is in bytes for NONE fields.
    return MIN (maxFieldSize_, (sqlda_->sqlvar+columnIndex)->sqllen);
  else
    return (sqlda_->sqlvar+columnIndex)->sqllen;
}

// Called by JIBSRemote.putResultData for VARCHARs, no trimming
IB_SSHORT16
IB_ResultSet::getVarCharByteLength (const IB_SSHORT16 columnIndex) const
{
  if (maxFieldSize_) {
    return MIN (maxFieldSize_, *(IB_SSHORT16*) (sqlda_->sqlvar+columnIndex)->sqldata);
  }
  else
    return *(IB_SSHORT16*) (sqlda_->sqlvar+columnIndex)->sqldata;
}

void
IB_ResultSet::open ()
{
#ifdef IB_USER_API
  if (open_)
    throw new IB_SQLException ("Attempt to open a result set which is already open");
#endif

  statement_->dsqlExecute ();

  open_ = IB_TRUE;
}

#ifdef IB_USER_API
IB_BOOLEAN
IB_ResultSet::previous ()
{
  throw new IB_SQLException ("previous() not yet implemented", 
                             SQLStates::_IM001, 
                             ErrorCodes::driverNotCapable);
}

IB_BOOLEAN
IB_ResultSet::relative (const IB_BOOLEAN previous,
			const IB_SLONG32 offset)
{
  throw new IB_SQLException ("relative() not yet implemented", 
                             SQLStates::_IM001, 
                             ErrorCodes::driverNotCapable);
}
#endif

IB_STRING 
IB_ResultSet::getChar (const IB_SSHORT16 columnIndex) const
{
  // !!! delete this and other type checks?
  IB_Types::typeCheck (IB_Types::CHAR_TYPE, sqlda_->sqlvar+columnIndex);
 
  return (IB_STRING) (sqlda_->sqlvar+columnIndex)->sqldata;
}

IB_STRING 
IB_ResultSet::getVarChar (const IB_SSHORT16 columnIndex) const
{
  IB_Types::typeCheck (IB_Types::VARCHAR_TYPE, sqlda_->sqlvar+columnIndex);

  return (IB_STRING) (sqlda_->sqlvar+columnIndex)->sqldata + sizeof (IB_SSHORT16);
}



