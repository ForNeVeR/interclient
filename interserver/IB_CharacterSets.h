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
#ifndef _IB_CHARACTER_SETS_H_
#define _IB_CHARACTER_SETS_H_

#include "IB_Defines.h"
#include "IB_LDString.h"

class IB_CharacterSets {

public:

  enum IBCharacterSet {
    NONE__ = 0, // default
    ASCII__ = 2,
    BIG_5__ = 56,
    CYRL__ = 50,
    DOS437__ = 10,
    DOS850__ = 11,
    DOS852__ = 45,
    DOS857__ = 46,
    DOS860__ = 13,
    DOS861__ = 47,
    DOS863__ = 14,
    DOS865__ = 12,
    EUCJ_0208__ = 6,
    GB_2312__ = 57,
    ISO8859_1__ = 21, // aka LATIN1
    KSC_5601__ = 44,
    NEXT__ = 19,
    OCTETS__ = 1,
    SJIS_0208__ = 5,
    UNICODE_FSS__ = 3,
    WIN1250__ = 51,
    WIN1251__ = 52,
    WIN1252__ = 53,
    WIN1253__ = 54,
    WIN1254__ = 55
  };

  static IB_SSHORT16 compute_sqllen (const long attachmentCharSetCode,
				     const XSQLVAR* sqlvar);

  static IB_SSHORT16 getCharLength (const XSQLVAR* sqlvar);

  static long getCharSetCode (const IB_LDString value);

private:

  static IB_BOOLEAN isOneByteCharSet (const long charSetCode);
  static IB_BOOLEAN isTwoByteCharSet (const long charSetCode);
  static IB_BOOLEAN isThreeByteCharSet (const long charSetCode);
};

#endif
