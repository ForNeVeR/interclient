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
#include "IB_LDBytes.h"

IB_LDBytes::IB_LDBytes ()
  : value_ (NULL),
    length_ (0)
{ }

IB_LDBytes::IB_LDBytes (const IB_USHORT16 length,
			  const IB_BUFF_PTR value)
  : value_ ((IB_BUFF_PTR) value)
, length_ ((IB_USHORT16) length)
{ }

