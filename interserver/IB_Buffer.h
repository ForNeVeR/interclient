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
#ifndef _IB_BUFFER_H_
#define _IB_BUFFER_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"

class IB_Buffer
{

public:

  IB_BUFF_PTR buffer_;     
  IB_SSHORT16 size_;	// buffer allocated size
  IB_SSHORT16 length_;	// buffer in use length
  IB_BUFF_PTR position_; // current pointer into buffer
  IB_SSHORT16 memoryAllocationIncrement_; // set by call to allocate ()

public:

  IB_Buffer ();

  ~IB_Buffer ();

  // Initial allocation of raw buffer memory.
  // Throw IB_SQLException if there is not enough memory on the heap.
  void allocate (const IB_SSHORT16 memoryAllocationIncrement);

  IB_BOOLEAN isAllocated ();

  // Called by IB_Configuration::addParameter()
  // and IB_Information::addRequest().
  // Memory is only reallocated if enough memory has not already been allocated.
  // Throw IB_SQLException if there is not enough memory on the heap.
  // Increase bufferSize_ if memory needs to be allocated to accomodate
  // numBytes more data.
  // Does not increase bufferLength_.
  void expandBy (const IB_SSHORT16 numBytes);

  IB_BUFF_PTR next ();

  IB_BUFF_PTR nextAvailableMemory ();

  IB_BUFF_PTR buffer () const;

  void incrementLength (IB_SSHORT16 delta);

  // !!! more efficient to have writeByte(), writeShort(), writeLong() routines
  // !!! used by ?
  void writeInteger (const IB_UBYTE intSize,
		     const IB_INT intValue);

};

inline
IB_Buffer::IB_Buffer ()
: length_ (0),
  memoryAllocationIncrement_ (0),
  buffer_ (NULL)
{ }

inline
IB_BOOLEAN 
IB_Buffer::isAllocated ()
{
  return TO_BOOLEAN (buffer_);
}

inline
IB_BUFF_PTR 
IB_Buffer::next ()
{
  return position_++;
}

inline
IB_BUFF_PTR 
IB_Buffer::nextAvailableMemory ()
{
  return (position_ = buffer_ + length_);
}

inline
IB_BUFF_PTR 
IB_Buffer::buffer () const
{
  return (buffer_);
}

inline
void 
IB_Buffer::incrementLength (IB_SSHORT16 delta)
{
  length_ += delta;
}

#endif
