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
#include "IB_Blob.h"
#include "IB_Status.h"
#include "IB_Transaction.h"
#include "IB_Connection.h"
#include "IB_Statement.h"

const IB_SLONG32 IB_Blob::jdbcNetRequestedSegmentSize__ = 300 * 1024;
const IB_USHORT16 IB_Blob::ibRequestedSegmentSize__ = 65535;

const IB_BUFF_CHAR
IB_Blob::blobInfoRequest__[] = { isc_info_blob_total_length, 
                                 isc_info_blob_max_segment,
                                 isc_info_blob_num_segments,
                                 isc_info_blob_type
                               };

void 
IB_Blob::getInfo()
{
  if (isc_blob_info (statement_->status_->vector(), 
                     &blobHandle_,
                     sizeof (blobInfoRequest__),
                     (IB_BUFF_PTR) blobInfoRequest__,
                     blobRequestedInfoSize__,
                     blobRequestedInfo_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       statement_->status_);

  IB_BUFF_PTR iterator;
  char item;
  int valueLength;

  for (iterator = blobRequestedInfo_; *iterator != isc_info_end; ) {
    item = *iterator++; 
    // HP-UX port (old CC): added type cast (int)
    valueLength = (int) isc_vax_integer (iterator, 2);
    iterator += 2; // skip over value length field

    switch (item) {
    case isc_info_blob_total_length:
      total_length_ = isc_vax_integer (iterator, valueLength);
      break;
    case isc_info_blob_max_segment:
      max_segment_ = isc_vax_integer (iterator, valueLength);
      break;
    case isc_info_blob_num_segments:
      num_segments_ = isc_vax_integer (iterator, valueLength);
      break;
    case isc_info_blob_type:
      type_ = isc_vax_integer (iterator, valueLength);
      break;
    case isc_info_truncated:
    default:
      throw new IB_SQLException (IB_SQLException::bugCheck__0__,
				 10000,
				 IB_SQLException::bugCheckException__);
    } 
    iterator += valueLength;
  }

}

IB_Blob::~IB_Blob ()
{
  close ();
  statement_->remOpenBlob (this);
}

IB_BLOBID
IB_Blob::create ()
{
#ifdef IB_USER_API
  //  if (blobId_)
  //    throw new IB_SQLException ("Attempt to create a blob which is already created");
#endif

  if (isc_create_blob2 (statement_->status_->vector(), 
			statement_->connection_->dbHandleP(), 
			statement_->transaction_->trHandleP(),
			&blobHandle_,
			&blobId_,
			0,
			NULL)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__, statement_->status_);
 
  statement_->addOpenBlob (this);

  return blobId_;
}

void
IB_Blob::open ()
{
  if (isc_open_blob2 (statement_->status_->vector(), 
		      statement_->connection_->dbHandleP(), 
		      statement_->transaction_->trHandleP(),
		      &blobHandle_,
		      &blobId_,
		      0,
		      NULL)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       statement_->status_);

  statement_->addOpenBlob (this);

  getInfo ();
}

IB_BOOLEAN
IB_Blob::get (IB_BUFF_PTR segmentBuffer)
{
  IB_SLONG32 code;
  IB_USHORT16 fetchedBytes = 0;
  IB_BUFF_PTR buffPtr = segmentBuffer;
  IB_SLONG32 remainingBytes = jdbcNetRequestedSegmentSize__;

  actualJDBCNetSegmentSize_ = 0;
  for (;;) {
    isc_get_segment (statement_->status_->vector(),
		     &blobHandle_, 
		     &fetchedBytes,  
		     MIN (ibRequestedSegmentSize__, 
			  remainingBytes), // this must be an unsigned short!!!
		     buffPtr);

    remainingBytes -= fetchedBytes;
    buffPtr += fetchedBytes;
    actualJDBCNetSegmentSize_ += fetchedBytes;

    code = statement_->status_->getVendorCode();

    if (code == isc_segstr_eof)  {
      atEnd_ = IB_TRUE;
      return IB_FALSE;
    }
    // else if (code == isc_segment)  // can't fit ib segment into remaining buffer
    //  return IB_TRUE;
    else if (code && (code != isc_segment))
      throw new IB_SQLException (IB_SQLException::engine__default_0__,
				 statement_->status_); 
    else if (actualJDBCNetSegmentSize_ >= jdbcNetRequestedSegmentSize__)
      return IB_TRUE; // !! actually > should never happen, bug check
  }

  return IB_TRUE;
}

void
IB_Blob::put (IB_USHORT16 dataLength,
	      const IB_BUFF_PTR segmentBuffer)
{
  // !!! make sure dataLength is never greater than 32K
  // HP-UX port (old CC): added type cast (unsigned short)
  if (isc_put_segment (statement_->status_->vector(),
		       &blobHandle_, 
		       dataLength,
		       segmentBuffer))
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       statement_->status_); 
}

void
IB_Blob::close ()
{
  if (!blobHandle_)
    return;

  if (isc_close_blob (statement_->status_->vector(), 
		      &blobHandle_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__,
			       statement_->status_); 

}


