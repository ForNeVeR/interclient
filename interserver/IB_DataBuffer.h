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
#ifndef _IB_DATA_BUFFER_H_
#define _IB_DATA_BUFFER_H_

#include "IB_Defines.h"

class IB_SQLException;

class IB_DataBuffer {

protected:

  XSQLDA* sqlda_;

  IB_BUFF_PTR buffer_;

  IB_SLONG32 recordSize_;
 
  IB_SSHORT16* charLengths_;

public:

  IB_SLONG32 getRecordSize () 
  { return recordSize_; }

protected:

  IB_DataBuffer (const XSQLDA* sqlda, const long attachmentCharSetCode);

  // Non-virtual destructor should never be called.
  ~IB_DataBuffer ();

  // Direct SQLVAR data pointers into the buffer
  void align ();

  IB_SLONG32 computeRecordSize (const long attachmentCharSetCode);

  void validateIndex (IB_SSHORT16 columnIndex) const;

  static IB_SLONG32 alignedOffset (const IB_SLONG32 offset, 
				   const IB_SLONG32 sizeOfData);

  static IB_BOOLEAN isMetaDataField (const XSQLVAR* sqlvar);
};

inline
void 
IB_DataBuffer::validateIndex (IB_SSHORT16 columnIndex) const
{ }

inline
IB_SLONG32 
IB_DataBuffer::alignedOffset (const IB_SLONG32 offset, 
			      const IB_SLONG32 sizeOfData)
{
  return (offset+sizeOfData-1) & ~(sizeOfData-1);
}

#endif
