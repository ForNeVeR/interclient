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
#ifndef _IB_INPUT_BUFFER_H_
#define _IB_INPUT_BUFFER_H_

#include "IB_Defines.h"
#include "IB_DataBuffer.h"
#include "IB_SQLException.h"

class IB_InputBuffer : public IB_DataBuffer {

  // Note: recordSize_ is the buffer size.

public:

  ~IB_InputBuffer ();

  IB_SSHORT16 getCharLength (const IB_SSHORT16 columnIndex) const;

private:

  friend class IB_Statement;

  // The only way to create an input buffer is from statement prepare.
  // Throw SQLException if there is not enough memory on the heap.
  IB_InputBuffer (const IB_Statement* statement);

};

inline
IB_SSHORT16 
IB_InputBuffer::getCharLength (const IB_SSHORT16 columnIndex) const
{
  validateIndex (columnIndex);
  // !!! check for blob type
  return charLengths_[columnIndex];
}

#endif
