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
#ifndef _IB_LD_BYTES_H
#define _IB_LD_BYTES_H

#include "IB_Defines.h"

// Maximum LD Byte length is 64K.

class IB_LDBytes {

public:

  // Construct a null ld bytes (no byte array).
  IB_LDBytes ();

  // Input bytes are shared not copied.
  // Destructor does not reclaim space.
  IB_LDBytes (const IB_USHORT16 length,
	       const IB_BUFF_PTR value);

  IB_BUFF_PTR value_;
  IB_SSHORT16 length_;
};

#endif
