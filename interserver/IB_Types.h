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
#ifndef _IB_TYPES_H_
#define _IB_TYPES_H_

#include <stdlib.h> // for abs()

#include "IB_Defines.h"
#include "IB_LDString.h"
#include "IB_SQLException.h"

// This class defines constants that are used to identify IB types.

class IB_Types {

public:

  // Warning: These values must match those in interclient/IBTypes.java
  
  enum IBType {
   NULL_TYPE = 0,

   SMALLINT_TYPE = 1,
   INTEGER_TYPE = 2,

   FLOAT_TYPE = 3,

   DOUBLE_TYPE = 4,

   NUMERIC_DOUBLE_TYPE = 5,
   NUMERIC_INTEGER_TYPE = 6,
   NUMERIC_SMALLINT_TYPE = 7,

   CHAR_TYPE = 8,
   VARCHAR_TYPE = 9,
   CLOB_TYPE = 10,

   DATE_TYPE = 11,

   BLOB_TYPE = 12,

   ARRAY_TYPE = 14,
// CJL-IB6 added new types
   SQL_DATE_TYPE = 15,
   TIME_TYPE = 16,

   NUMERIC_INT64_TYPE = 17,
   DECIMAL_INT64_TYPE = 18,
   DECIMAL_INTEGER_TYPE = 19
};

private:

  friend class JIBSRemote;
  friend class IB_Statement;
  friend class IB_ResultSet;

  // Called by IB_Statement::getResultColumnType () and getInputColumnType ()
  static IBType getIBType (const XSQLVAR* sqlvar);

  // MMM - 2 function declarations added
  // Get IB type from the blr_<type> form used in array descriptors
  // Called by JIBSRemote::putArrayDescriptor()
  static IBType getIBTypeOfArrayElement (ISC_ARRAY_DESC *descriptor);
  // Called by JIBSRemote::putArrayDescriptor()
  static IB_SSHORT16 getPrecisionOfArrayElement (ISC_ARRAY_DESC *descriptor);
  // MMM - end

#ifdef IB_USER_API
  static IB_LDString getIBTypeName (const XSQLVAR* sqlvar);
#endif

  // InterBase encodes nullability as part of the ISC type token as stored in 
  // descriptor areas.
  // !!! do a search for "& 1" and replace with this call.
  static IB_BOOLEAN isTypeTokenNullable (const IB_SSHORT16 iscTypeToken);

  static IB_BOOLEAN isIBTypeNumeric (const IBType ibType);

  static void typeCheck (const IBType ibType,
			 const XSQLVAR* sqlvar);

  static IB_SSHORT16 getPrecision (const XSQLVAR* sqlvar);

  static IB_SSHORT16 getScale (const XSQLVAR* sqlvar);

  static IB_SSHORT16 getSubType (const XSQLVAR* sqlvar);

};

inline
IB_BOOLEAN 
IB_Types::isTypeTokenNullable (const IB_SSHORT16 iscTypeToken)
{
  return iscTypeToken & 1;
}

inline
IB_BOOLEAN 
IB_Types::isIBTypeNumeric (const IB_Types::IBType ibType)
{
  return ((ibType == NUMERIC_DOUBLE_TYPE) || 
          (ibType == NUMERIC_INTEGER_TYPE) ||
          (ibType == NUMERIC_SMALLINT_TYPE) ||
		  (ibType == NUMERIC_INT64_TYPE) || 
          (ibType == DECIMAL_INTEGER_TYPE) ||
          (ibType == DECIMAL_INT64_TYPE));
/* CJL obsolete by IB 6.0
  return ((ibType == NUMERIC_DOUBLE_TYPE) || 
          (ibType == NUMERIC_INTEGER_TYPE) ||
          (ibType == NUMERIC_SMALLINT_TYPE));
*/
}

inline
void 
IB_Types::typeCheck (const IB_Types::IBType ibType,
		     const XSQLVAR* sqlvar)
{
#ifdef IB_USER_API
  // !!! put type check code here
#endif
}

inline
IB_SSHORT16 
IB_Types::getScale (const XSQLVAR* sqlvar)
{
  // !!! Make this work for DATEs. See ODBC Manual.
  return abs (sqlvar->sqlscale);
}

inline
IB_SSHORT16 
IB_Types::getSubType (const XSQLVAR* sqlvar)
{
  // modulo 128 because interbase seems to encode default character set in upper bytes
  return (sqlvar->sqlsubtype % 128);
}

#endif
