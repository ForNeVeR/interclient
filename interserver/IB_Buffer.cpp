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
#include "IB_Status.h"
#include "IB_Buffer.h"

IB_Buffer::~IB_Buffer ()
{
  delete buffer_;
}

// !!! more efficient to have writeByte(), writeShort(), writeLong() routines
// !!! used by ?
void 
IB_Buffer::writeInteger (const IB_UBYTE intSize,
			 const IB_INT intValue)
{
  if (intSize == 1)   // IB_BYTE
    *next() = (IB_BUFF_CHAR) intValue;
  if (intSize == 2) { // IB_SHORT
    *next() = (IB_BUFF_CHAR) intValue & 0xFF;
    *next() = (IB_BUFF_CHAR) (intValue>>8) & 0xFF;
  }
  if (intSize == 4) { // IB_LONG
    *next() = (IB_BUFF_CHAR) intValue & 0xFF;
    *next() = (IB_BUFF_CHAR) (intValue>>8) & 0xFF;
    *next() = (IB_BUFF_CHAR) (intValue>>16) & 0xFF;
    *next() = (IB_BUFF_CHAR) (intValue>>24) & 0xFF;
  }
}

void
IB_Buffer::expandBy (const IB_SSHORT16 numBytes)
{
  // Save a pointer to the old buffer in case we
  // need to allocate a new one.
  IB_BUFF_PTR oldBuffer = buffer_;

  // allocate a larger buffer if needed and copy over old buffer
  // !!! this may be more efficient using realloc?
  if ((length_ + numBytes) > size_) {
    buffer_ = new IB_BUFF_CHAR [size_ + memoryAllocationIncrement_];
    if (!buffer_)
      throw new IB_SQLException (IB_SQLException::outOfMemory__,
				 IB_SQLException::outOfMemoryException__);
    size_ += memoryAllocationIncrement_;

    // copy old parameter block into new parameter block
    IB_BUFF_PTR newPB = buffer_;
    IB_BUFF_PTR oldPB = oldBuffer;
    while (newPB < buffer_ + length_)
      *newPB++ = *oldPB++;

    delete oldBuffer;
  }
}

void
IB_Buffer::allocate (const IB_SSHORT16 memoryAllocationIncrement)
{
  memoryAllocationIncrement_ = memoryAllocationIncrement;

  // !!! Rewrite using malloc
  buffer_ =  new IB_BUFF_CHAR [memoryAllocationIncrement_];

  if (!buffer_)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
			       IB_SQLException::outOfMemoryException__);

  length_ = 0;
  size_ = memoryAllocationIncrement_;
  position_ = buffer_;
}




