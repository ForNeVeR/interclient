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
#ifndef _IB_INFORMATION_H_
#define _IB_INFORMATION_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"
#include "IB_Buffer.h"

class IB_Status;

class IB_Information {

protected:

  // Static constants
  enum {
    requestMemoryAllocationIncrement__ = 20,
    requestedInfoMemoryAllocationIncrement__ = 512
  };

  // Holds requested items
  IB_Buffer request_; 
  
  // Holds requested information returned from server
  // on a call to requestInfo ()
  IB_Buffer requestedInfo_;	

  // Information objects keep track of their own status.
  IB_Status* status_;

public:

  virtual ~IB_Information ();

  // reassign status object
  void setStatus (const IB_Status& status);

  // see isc_info_* items in ibase.h
  // Puts infoRequestItem into interal request buffer.
  // No request is made to the server until requestInfo()
  // is called.
  // Throws IB_SQLException if there is not enough memory on the heap
  // to expand internal request buffer.
  void addRequest (const IB_UBYTE infoRequestItem);

  // Send bundled request to server by calling virtual issueRequest().
  // Puts results into internal requestedInfo buffer.
  // Throws IB_SQLException if the server information request fails.
  // !!! Does this need to be virtual?
  void requestInfo ();

protected:

  // Allocate internal buffers.
  // Throw IB_SQLException if there is not enough memory on the heap.
  IB_Information (const IB_Status& status);

  // Called by requestInfo ()
  // This issues the request to the server.
  // Implemented differently for connection requests, and transaction requests.
  // Throws IB_SQLException if isc_database_info or isc_transaction_info fails.
  virtual void issueRequest () = 0;

  // A request is represented as a character array of isc_info_ parameters.
  // The character array representation must be terminated with the isc_info_end character.
  // Loop thru requested info buffer looking for requested item.
  // findInfoItem returns either a pointer to data in the
  // requestedInfo_ buffer starting with a length delimiter.
  // Throws IB_SQLException if the requestedItem is not found.
  IB_BUFF_PTR findInfoItem (const IB_UBYTE requestedItem) const;


  friend class IB_Statement;

  // Takes a pointer into the requested info buffer to a 2 byte length
  // indicator followed by an integer valued info result,
  // and returns that result as a long integer.
  // Only works for integer info items.
  // Called by IB_Statement::statementType() and getRowCount()
  static int getInteger (const IB_BUFF_PTR p);

};

#endif
