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
// CJL-IB6  need to examine the IB6 SQL_TYPES... 


//-*-C++-*-
#include "IB_Types.h"

#include <string.h>

IB_Types::IBType
IB_Types::getIBType (const XSQLVAR* sqlvar)
{
  switch (sqlvar->sqltype & ~1) { // drop indicator flag
  case SQL_TEXT:    
    return CHAR_TYPE;
  case SQL_VARYING: 
    return VARCHAR_TYPE;
  case SQL_LONG:    
    if (sqlvar->sqlscale == 0)
      return INTEGER_TYPE;
    else 
// CJL-IB6 expanded for IB6 DECIMAL types
			if (sqlvar->sqlsubtype == 2)
				return DECIMAL_INTEGER_TYPE;
			else
				return NUMERIC_INTEGER_TYPE; 
  case SQL_SHORT:
    if (sqlvar->sqlscale == 0)
      return SMALLINT_TYPE;
    else
      return NUMERIC_SMALLINT_TYPE;
  case SQL_FLOAT:   
    return FLOAT_TYPE;
  case SQL_DATE:    
    return DATE_TYPE;
  case SQL_DOUBLE:  
    if (sqlvar->sqlscale == 0)
      return DOUBLE_TYPE;
    else
      return NUMERIC_DOUBLE_TYPE;
  case SQL_D_FLOAT: 
    return DOUBLE_TYPE;
  case SQL_ARRAY:   
    return ARRAY_TYPE;
  case SQL_BLOB: 
    if (sqlvar->sqlsubtype == 1)      // plain text
      return CLOB_TYPE;
    else 
      return BLOB_TYPE;        // unstructured
// CJL-IB6 new IB6 types for the next three cases
	case SQL_INT64:
		if (sqlvar->sqlsubtype == 2) 
			return DECIMAL_INT64_TYPE;
		else
			return NUMERIC_INT64_TYPE;
  case SQL_TYPE_TIME:
		return TIME_TYPE;
  case SQL_TYPE_DATE:
    return SQL_DATE_TYPE;
  case SQL_QUAD:  
  default:  
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10017,
			       IB_SQLException::bugCheckException__);
  }
    // temporary for MSVC !!!RRK
    return NULL_TYPE;
}

IB_SSHORT16
IB_Types::getPrecision (const XSQLVAR* sqlvar)
{
  switch (sqlvar->sqltype & ~1) { // drop indicator flag
  case SQL_TEXT:    
  case SQL_VARYING: 
    return sqlvar->sqllen;
  case SQL_DATE:    
    return 19;
  case SQL_LONG:  
    return 10;  
  case SQL_SHORT:
    return 5;
  case SQL_FLOAT:   
    return 7;
  case SQL_DOUBLE:  
    return 15;
  case SQL_D_FLOAT: 
    return 15;
// CJL-IB6 Added three new types:
  case SQL_INT64:
		return 19;
  case SQL_TYPE_TIME:
		return 8;
	case SQL_TYPE_DATE:
		return 10;
// CJL-IB6 - end
  case SQL_ARRAY:   
  case SQL_BLOB: 
    return 0;
  case SQL_QUAD:    
  default:
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10018,
			       IB_SQLException::bugCheckException__);
  }
}

#ifdef IB_USER_API
IB_LDString 
IB_Types::getIBTypeName (const XSQLVAR* sqlvar)
{
  return getIBTypeName (getIBType (sqlvar));
}
#endif

// MMM - added function to get IB type from blr_<type> representation
// By some unknown reason base type of array element is 
// stored in the form blr_<type> not SQL_<TYPE>.
// CJL - There is no sqlsubtype member in the ISC_ARRAY_DESC
// struct. Therefore we can't distinguish between NUMERIC and DECIMAL
IB_Types::IBType
IB_Types::getIBTypeOfArrayElement (ISC_ARRAY_DESC *descriptor)
{
  switch (descriptor->array_desc_dtype) {
  case blr_text:
  case blr_text2:
    return CHAR_TYPE;
  case blr_varying: 
  case blr_varying2: 
    return VARCHAR_TYPE;
  case blr_long:
    if (descriptor->array_desc_scale == 0)
      return INTEGER_TYPE;
    else
      return NUMERIC_INTEGER_TYPE;
  case blr_short:
    if (descriptor->array_desc_scale == 0)
      return SMALLINT_TYPE;
    else
      return NUMERIC_SMALLINT_TYPE;
  case blr_float:
    return FLOAT_TYPE;
  case blr_date:
    return DATE_TYPE;
  case blr_double:
    if (descriptor->array_desc_scale == 0)
      return DOUBLE_TYPE;
    else
      return NUMERIC_DOUBLE_TYPE;
  case blr_d_float: 
    return DOUBLE_TYPE;
  case blr_blob_id: 
  case blr_quad:
  default:  
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10025,
			       IB_SQLException::bugCheckException__);
  }
    // temporary for MSVC !!!RRK
    return NULL_TYPE;
}
// MMM - end

// MMM - added function to get precision of an array element
// Called by JIBSRemote::putArrayDescriptor()
IB_SSHORT16
IB_Types::getPrecisionOfArrayElement (ISC_ARRAY_DESC *descriptor)
{
  switch (descriptor->array_desc_dtype) {
  case blr_text:
  case blr_text2:
  case blr_varying: 
  case blr_varying2: 
    return descriptor->array_desc_length;
  case blr_date:
    return 19;
  case blr_long:
    return 10;  
  case blr_short:
    return 5;
  case blr_float:
    return 7;
  case blr_double:
    return 15;
  case blr_d_float: 
    return 15;
// CJL-IB6 -- added three new types
	case blr_int64:
		return 19;
	case blr_sql_time:
		return 8;
	case blr_sql_date:
		return 10;
// CJL-IB6 -- end
  case blr_blob_id: 
  case blr_quad:
  default:
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10026,
			       IB_SQLException::bugCheckException__);
  }
}
// MMM - end
