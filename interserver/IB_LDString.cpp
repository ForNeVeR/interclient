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
#include "IB_LDString.h"

IB_LDString::IB_LDString ()
  : string_ (NULL),
    length_ (0)
{ }

IB_LDString::IB_LDString (const IB_USHORT16 length,
			  const IB_STRING string)
  : string_ ((IB_STRING) string)
, length_ ((IB_USHORT16) length)
{ }

// !!! Do we ned to call isc_vax_integer here?
IB_LDString::IB_LDString (const IB_LDSTRING string)
  : string_ ((IB_STRING) string + sizeof(IB_USHORT16))
, length_ (*(IB_USHORT16*) string)
{ }


