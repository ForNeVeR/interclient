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
#ifndef _IB_LD_STRING_H_
#define _IB_LD_STRING_H_

#include "IB_Defines.h"

// Maximum LD string length is 64K.

class IB_LDString {

public:

  // Construct a null ld string (not the empty string).
  IB_LDString ();

  // Input string is shared not copied.
  // Destructor does not reclaim string.
  IB_LDString (const IB_USHORT16 length,
	       const IB_STRING string);

  // Input string is shared not copied.
  // Destructor does not reclaim string.
  // Convert an IB_LDSTRING as returned by the engine
  // into an LDString object.
  IB_LDString (const IB_LDSTRING string);

  IB_STRING string_;
  IB_USHORT16 length_;
};

#endif
