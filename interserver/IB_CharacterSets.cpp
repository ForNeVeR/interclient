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
#include "IB_CharacterSets.h"
#include "IB_SQLException.h"

// This function is only valid before sqllen is adjusted,
// but after sqlsubtype is adjusted.
IB_SSHORT16
IB_CharacterSets::getCharLength (const XSQLVAR* sqlvar)
{
  // modulo 128 because interbase seems to encode default character set in upper bytes
  long fieldCharSetCode = (long) (sqlvar->sqlsubtype % 128);

  if (isOneByteCharSet (fieldCharSetCode))
    return sqlvar->sqllen;
  else if (isTwoByteCharSet (fieldCharSetCode))
    return sqlvar->sqllen / 2;
  else if (isThreeByteCharSet (fieldCharSetCode)) 
      return sqlvar->sqllen / 3;

  throw new IB_SQLException (IB_SQLException::unsupportedCharacterSet__0__, 
			     fieldCharSetCode, 
			     IB_SQLException::unsupportedCharacterSetException__);
  return 0; // this is to make the MS VC++ compiler happy :-)
}

// This routine will not be needed in future releases of IB.
// Adjust improper sqllen values for CHAR and VARCHAR fields
// from #storage-bytes to #attachment-encoding-bytes 
// (workaround for InterBase bug #8770).
// Character set adjustments (sqlsubtype) must have already
// been performed prior to calling compute_sqllen.
IB_SSHORT16
IB_CharacterSets::compute_sqllen (const long attachmentCharSetCode,
				  const XSQLVAR* sqlvar)
{
  // modulo 128 because interbase seems to encode default character set in upper bytes
  long fieldCharSetCode = (long) (sqlvar->sqlsubtype % 128);
  IB_SSHORT16 improper_sqllen = sqlvar->sqllen;

  // NONE and ASCII have 1 byte attachment charset representations,
  // regardless of the attachment charset.  
  // This is because ASCII is a subset of all IB supported charsets.
  if ((fieldCharSetCode == NONE__) || 
      (fieldCharSetCode == ASCII__)) {
    return improper_sqllen;
  }

  if (attachmentCharSetCode == UNICODE_FSS__) {

    // 8859_1 is a 1 byte character set with a 2 byte FSS representation
    if (fieldCharSetCode == ISO8859_1__)
      return 2*improper_sqllen;

    // 1 byte character set with 3 byte FSS representation
    if (isOneByteCharSet (fieldCharSetCode))
      return 3*improper_sqllen;

    // 2 byte character sets with 3 byte FSS representations
    if (isTwoByteCharSet (fieldCharSetCode))
      return 3*improper_sqllen/2;

    // 3 byte character set with 3 byte FSS representations
    if (isThreeByteCharSet (fieldCharSetCode))
      return improper_sqllen;
  }
  else if (isTwoByteCharSet (attachmentCharSetCode)) {

    // 1 byte character set with 2 byte attachment charset representation
    if (isOneByteCharSet (fieldCharSetCode))
      return 2*improper_sqllen;

    // 2 byte character sets with 2 byte attachment charset representations
    if (isTwoByteCharSet (fieldCharSetCode))
      return improper_sqllen;

    // 3 byte character set with 2 byte attachment charset representations
    if (isThreeByteCharSet (fieldCharSetCode))
      return 2*improper_sqllen/3;
  }
  else if (isOneByteCharSet (attachmentCharSetCode)) {

    // 1 byte character set with 1 byte attachment charset representation
    if (isOneByteCharSet (fieldCharSetCode))
      return improper_sqllen;

    // 2 byte character sets with 1 byte attachment charset representations
    if (isTwoByteCharSet (fieldCharSetCode))
      return improper_sqllen/2;

    // 3 byte character set with 1 byte attachment charset representations
    if (isThreeByteCharSet (fieldCharSetCode))
      return improper_sqllen/3;
  }
  else {
    throw new IB_SQLException (IB_SQLException::unsupportedCharacterSet__0__, 
			       attachmentCharSetCode, 
			       IB_SQLException::unsupportedCharacterSetException__);
    return 0; // this is to make the MS VC++ compiler happy :-)
  }
  throw new IB_SQLException (IB_SQLException::unsupportedCharacterSet__0__, 
			     fieldCharSetCode, 
			     IB_SQLException::unsupportedCharacterSetException__);
  return 0; // this is to make the MS VC++ compiler happy :-)
}

IB_BOOLEAN
IB_CharacterSets::isOneByteCharSet (const long charSetCode)
{
  switch (charSetCode) {
  case NONE__: 
  case ASCII__: 
  case ISO8859_1__: 
  case CYRL__:
  case DOS437__:
  case DOS850__:
  case DOS852__:
  case DOS857__:
  case DOS860__:
  case DOS861__:
  case DOS863__:
  case DOS865__:
  case NEXT__:
  case OCTETS__:
  case WIN1250__:
  case WIN1251__:
  case WIN1252__:
  case WIN1253__:
  case WIN1254__:
    return IB_TRUE;
  default:
    return IB_FALSE;
  }
}

IB_BOOLEAN
IB_CharacterSets::isTwoByteCharSet (const long charSetCode)
{
  switch (charSetCode) {
  case BIG_5__:
  case EUCJ_0208__:
  case GB_2312__:
  case KSC_5601__:
  case SJIS_0208__:
    return IB_TRUE;
  default:
    return IB_FALSE;
  }
}

IB_BOOLEAN
IB_CharacterSets::isThreeByteCharSet (const long charSetCode)
{
  switch (charSetCode) {
  case UNICODE_FSS__:
    return IB_TRUE;
  default:
    return IB_FALSE;
  }
}

long 
IB_CharacterSets::getCharSetCode (const IB_LDString value)
{
  if (strncmp ("NONE", value.string_, value.length_) == 0)
    return NONE__;
  else if (strncmp ("ASCII", value.string_, value.length_) == 0)
    return ASCII__;
  else if (strncmp ("BIG_5", value.string_, value.length_) == 0)
    return BIG_5__;
  else if (strncmp ("CYRL", value.string_, value.length_) == 0)
    return CYRL__;
  else if (strncmp ("DOS437", value.string_, value.length_) == 0)
    return DOS437__;
  else if (strncmp ("DOS850", value.string_, value.length_) == 0)
    return DOS850__;
  else if (strncmp ("DOS852", value.string_, value.length_) == 0)
    return DOS852__;
  else if (strncmp ("DOS857", value.string_, value.length_) == 0)
    return DOS857__;
  else if (strncmp ("DOS860", value.string_, value.length_) == 0)
    return DOS860__;
  else if (strncmp ("DOS861", value.string_, value.length_) == 0)
    return DOS861__;
  else if (strncmp ("DOS863", value.string_, value.length_) == 0)
    return DOS863__;
  else if (strncmp ("DOS865", value.string_, value.length_) == 0)
    return DOS865__;
  else if (strncmp ("EUCJ_0208", value.string_, value.length_) == 0)
    return EUCJ_0208__;
  else if (strncmp ("GB_2312", value.string_, value.length_) == 0)
    return GB_2312__;
  else if (strncmp ("ISO8859_1", value.string_, value.length_) == 0)
    return ISO8859_1__;
  else if (strncmp ("KSC_5601", value.string_, value.length_) == 0)
    return KSC_5601__;
  else if (strncmp ("NEXT", value.string_, value.length_) == 0)
    return NEXT__;
  else if (strncmp ("OCTETS", value.string_, value.length_) == 0)
    return OCTETS__;
  else if (strncmp ("SJIS_0208", value.string_, value.length_) == 0)
    return SJIS_0208__;
  else if (strncmp ("UNICODE_FSS", value.string_, value.length_) == 0)
    return UNICODE_FSS__;
  else if (strncmp ("WIN1250", value.string_, value.length_) == 0)
    return WIN1250__;
  else if (strncmp ("WIN1251", value.string_, value.length_) == 0)
    return WIN1251__;
  else if (strncmp ("WIN1252", value.string_, value.length_) == 0)
    return WIN1252__;
  else if (strncmp ("WIN1253", value.string_, value.length_) == 0)
    return WIN1253__;
  else if (strncmp ("WIN1254", value.string_, value.length_) == 0)
    return WIN1254__;
  else 
    return NONE__;
}
