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
#include <stdlib.h> // malloc

#include "IB_DataBuffer.h"
#include "IB_SQLException.h"
#include "IB_Status.h"
#include "IB_CharacterSets.h"

// Child classes do all allocation and initialization.
IB_DataBuffer::IB_DataBuffer (const XSQLDA* sqlda, const long attachmentCharSetCode)
  : sqlda_ ((XSQLDA*) sqlda)
{ 
#ifdef TRACEON
  debugTraceAnInt ("constructing databuffer: ", (IB_REF) this);
#endif
  charLengths_ = (IB_SSHORT16*) calloc (sqlda_->sqld, sizeof (IB_SSHORT16));
  recordSize_ = computeRecordSize (attachmentCharSetCode);

  // !!! look in resultSetPool for memory.

  // !!! is this heap memory aligned for doubles?
  // HP-UX port (old CC): added type cast (size_t)
  buffer_ = (IB_BUFF_PTR) malloc ((size_t) recordSize_);
  if (!buffer_)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
			       IB_SQLException::outOfMemoryException__);

  align ();
}

// This is not virtual and will never be called.
// The buffer_ must be destroyed by child class destructors.
IB_DataBuffer::~IB_DataBuffer ()
{ 
#ifdef TRACEON
  debugTraceAnInt ("destroying databuffer: ", (IB_REF) this);
#endif
}

IB_SLONG32
IB_DataBuffer::computeRecordSize (const long attachmentCharSetCode) // and coerce sqllen
{
  IB_SLONG32 offset, alignment;
  IB_SSHORT16 length, dtype, i;
  XSQLVAR *var;

  // First compute the size of memory needed to hold a record.
  // Simultaneously, adjust improper sqllen values for CHAR and VARCHAR fields
  // from #storage-bytes to #attachment-encoding-bytes (workaround for InterBase bug #8770),
  // also adjust character set (sqlsubtype) of embedded literals from 127 to attachment character
  // set (workaround for InterBase bug #8857), finally adjust improper sqlsubtype
  // for metadata by looking for an RDB$ prefix in the field name
  // (workaround for InterBase bug #8856).
  // Note 1: InterBase will give a string truncation error for any transliteration
  // involving more than 512 bytes (no workaround for InterBase bug #8849).
  // Note 2: The workaround to InterBase bug #8770 (adjusting sqllen)
  // does not prevent IB string truncation errors (isc_arith_error),
  // the workaround merely prevents interserver from making bad memory
  // references using improper sqllen (ie. core dump).
  // Note 3: Various IB bugs in transliteration tables for which
  // no workarounds exist.
  // Note 4: Often times sqlsubtype is offset by a multiple of 256.
  // this may be because interbase encodes the default character set 
  // in the high bytes.  Therefore, sqlsubtype%128 is used.
  // Talk with Dave, because clobbering high order bytes while setting sqlsubtype 
  // could cause problems.
  for (i = 0, var = (XSQLVAR*) sqlda_->sqlvar, offset = 0; 
       i < sqlda_->sqld; 
       i++, var++) {
    dtype = var->sqltype & ~1; // type of data, drop indicator flag for now
    switch (dtype) {
    case SQL_VARYING:
      if ((var->sqlsubtype%128) == 127) var->sqlsubtype = (IB_SSHORT16) attachmentCharSetCode; // bug #8857
      if (isMetaDataField (var)) var->sqlsubtype = (IB_SSHORT16) IB_CharacterSets::NONE__; // bug #8856
      charLengths_[i] = IB_CharacterSets::getCharLength (var);
      var->sqllen = IB_CharacterSets::compute_sqllen (attachmentCharSetCode, var);
      length = sizeof (IB_SSHORT16) + var->sqllen;
      alignment = sizeof (IB_SSHORT16);
      break;
    case SQL_TEXT:
      if ((var->sqlsubtype%128) == 127) var->sqlsubtype = (IB_SSHORT16) attachmentCharSetCode; // bug #8857
      if (isMetaDataField (var)) var->sqlsubtype = (IB_SSHORT16) IB_CharacterSets::NONE__; // bug #8856
      charLengths_[i] = IB_CharacterSets::getCharLength (var);
      var->sqllen = IB_CharacterSets::compute_sqllen (attachmentCharSetCode, var);
      length = var->sqllen;
      alignment = sizeof (char);
      break;
    default:
      length = alignment = var->sqllen;
    }

    // compute offsets to aligned data
    offset = alignedOffset (offset, alignment);
    offset += length;

    // compute offsets to aligned indicator if indicator type
    if (var->sqltype & 1) {
      offset = alignedOffset (offset, sizeof (short));
      offset += sizeof (short);
    }
  }

  return offset;
}

// This routine will not be needed in future releases of IB.
// Check for an RDB$ prefix in the field name 
// (workaround for InterBase bug #8856).
IB_BOOLEAN
IB_DataBuffer::isMetaDataField (const XSQLVAR* sqlvar)
{
  if (sqlvar->sqlname_length <= 4)
    return IB_FALSE;
  else if (strncmp ("RDB$", sqlvar->sqlname, 4) == 0)
    return IB_TRUE;
  else 
    return IB_FALSE;
}

void
IB_DataBuffer::align ()
{
  IB_SLONG32 offset, alignment;
  IB_SSHORT16 i, length, dtype;
  XSQLVAR *var;

  for (i = 0, var = (XSQLVAR*) sqlda_->sqlvar, offset = 0; 
       i< sqlda_->sqld; 
       i++, var++) {
    dtype = var->sqltype & ~1; // type of data, drop indicator flag for now
    switch (dtype) {
    case SQL_VARYING:
      length = sizeof(IB_SSHORT16) + var->sqllen;
      alignment = sizeof (IB_SSHORT16);
      break;
    case SQL_TEXT:
      length = var->sqllen;
      alignment = sizeof (char);
      break;
    default:
      length = alignment = var->sqllen;	// length of data
    }

    // align data
    offset = alignedOffset (offset, alignment);
    var->sqldata = (char *) (buffer_ + offset);
    offset += length;

    // align indicator if indicator is used
    if (var->sqltype & 1) {
      offset = alignedOffset (offset, sizeof (short));
      var->sqlind = (short *) (buffer_ + offset);
      offset += sizeof (short);
    }
  }
}


