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
#include "IB_Information.h"
#include "IB_Status.h"

IB_Information::IB_Information (const IB_Status& status)
: status_ ((IB_Status*) &status)
{ 
  // !!! Should we delay allocation until first call to addRequest?
  request_.allocate (requestMemoryAllocationIncrement__);
  // !!! Should we delay allocation until first call to requestInfo?
  requestedInfo_.allocate (requestedInfoMemoryAllocationIncrement__);
}

IB_Information::~IB_Information ()
{ }

void
IB_Information::setStatus (const IB_Status& status)
{
  status_ = (IB_Status*) &status;
}

void
IB_Information::addRequest (const IB_UBYTE infoRequestItem)
{
  request_.expandBy (1);
  *(request_.nextAvailableMemory()) = (IB_BUFF_CHAR) infoRequestItem;
  request_.incrementLength (1);
}

// !!! recursion is not working - look at this
void
IB_Information::requestInfo ()
{
  issueRequest ();

  if (*requestedInfo_.buffer() == isc_info_error)
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10002,
			       IB_SQLException::bugCheckException__);

  // Check if requestedInfo_ buffer is too small for results.
  // If it is, then make it bigger and try this call again recursively.
  if (*requestedInfo_.buffer() == isc_info_truncated) {
    requestedInfo_.expandBy (requestedInfoMemoryAllocationIncrement__);
    requestInfo ();
  }
}

IB_BUFF_PTR
IB_Information::findInfoItem (const IB_UBYTE requestedItem) const
{
  IB_BUFF_PTR iterator = (IB_BUFF_PTR) requestedInfo_.buffer();

  while (*iterator != isc_info_end) {
    if (*iterator == requestedItem)
      return ++iterator;
    iterator++;
    // Increment iterator over length indicator and over length bytes
    iterator += sizeof(IB_SSHORT16) + isc_vax_integer (iterator, sizeof (IB_SSHORT16));
  }

  throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			     10003,
			     IB_SQLException::bugCheckException__);
  // This is just to please MSVC
  return NULL;
}

int
IB_Information::getInteger (const IB_BUFF_PTR p)
{
  return isc_vax_integer (p+2, (short) isc_vax_integer (p, 2));
}

