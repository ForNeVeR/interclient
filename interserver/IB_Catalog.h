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
#ifndef _IB_CATALOG_H__
#define _IB_CATALOG_H__

#include "IB_Defines.h"
#include "IB_Transaction.h"
#include "IB_Types.h"

class IB_Statement;
class IB_SQLException;
class IB_Connection;
class IB_Status;

class IB_Catalog 
{

private:

  IB_Status* status_;

  IB_Connection* connection_;

  friend class JIBSRemote;

  // Note: The following catalog opcodes must match those in DatabaseMetaData.java
  enum {
    CATALOG_GET_PROCEDURES = 1,
    CATALOG_GET_PROCEDURE_COLUMNS = 2,
    CATALOG_GET_TABLES = 3,
    CATALOG_GET_TABLE_TYPES = 14,
    CATALOG_GET_COLUMNS = 4,
    CATALOG_GET_COLUMN_PRIVILEGES = 5,
    CATALOG_GET_TABLE_PRIVILEGES = 6,
    CATALOG_GET_BEST_ROW_IDENTIFIER = 7,
    CATALOG_GET_VERSION_COLUMNS = 8,
    CATALOG_GET_PRIMARY_KEYS = 9,
    CATALOG_GET_IMPORTED_KEYS = 10,
    CATALOG_GET_EXPORTED_KEYS = 11,
    CATALOG_GET_CROSS_REFERENCE = 12,
    CATALOG_GET_INDEX_INFO = 13,
    CATALOG_GET_TYPE_INFO = 15,
    CATALOG_ALL_PROCEDURES_ARE_CALLABLE = 16,
    CATALOG_ALL_TABLES_ARE_SELECTABLE = 17
  };

  // Table types supported by InterBase, see procedure getTables ()
  enum {
    TABLE_TYPE = 0,
    SYSTEM_TABLE_TYPE = 1,
    VIEW_TYPE = 2
  };

  enum {
    procedureResultUnknown__	= 0,
    procedureNoResult__		= 1,
    procedureReturnsResult__	= 2,

    procedureColumnUnknown__ = 0,
    procedureColumnIn__ = 1,
    procedureColumnInOut__ = 2,
    procedureColumnOut__ = 4,
    procedureColumnReturn__ = 5,
    procedureColumnResult__ = 3,

    procedureNoNulls__ = 0,
    procedureNullable__ = 1,
    procedureNullableUnknown__ = 2,

    columnNoNulls__ = 0,
    columnNullable__ = 1,
    columnNullableUnknown__ = 2,

    bestRowTemporary__   = 0,
    bestRowTransaction__ = 1,
    bestRowSession__     = 2,

    bestRowUnknown__	= 0,
    bestRowNotPseudo__	= 1,
    bestRowPseudo__	= 2,

    versionColumnUnknown__	= 0,
    versionColumnNotPseudo__	= 1,
    versionColumnPseudo__	= 2,

    importedKeyCascade__  = 0,
    importedKeyRestrict__ = 1,
    importedKeySetNull__  = 2,
    importedKeyNoAction__ = 3,
    importedKeySetDefault__ = 4,
    importedKeyInitiallyDeferred__  = 5,
    importedKeyInitiallyImmediate__  = 6,
    importedKeyNotDeferrable__  = 7,

    typeNoNulls__ = 0,
    typeNullable__ = 1,
    typeNullableUnknown__ = 2,

    typePredNone__ = 0,
    typePredChar__ = 1,
    typePredBasic__ = 2,
    typeSearchable__  = 3,

    tableIndexStatistic__ = 0,
    tableIndexClustered__ = 1,
    tableIndexHashed__    = 2,
    tableIndexOther__     = 3
  };

  // values for DATA_TYPE result column fields
  enum SQLType {
    BIT_SQLTYPE 	=  -7,
    TINYINT_SQLTYPE 	=  -6,
    SMALLINT_SQLTYPE	=   5,
    INTEGER_SQLTYPE 	=   4,
    BIGINT_SQLTYPE      =  -5,

    FLOAT_SQLTYPE       =   6,
    REAL_SQLTYPE 	=   7,
    DOUBLE_SQLTYPE 	=   8,

    NUMERIC_SQLTYPE 	=   2,
    DECIMAL_SQLTYPE	=   3,

    CHAR_SQLTYPE	=   1,
    VARCHAR_SQLTYPE 	=  12,
    LONGVARCHAR_SQLTYPE =  -1,

    DATE_SQLTYPE 	=  91,
    TIME_SQLTYPE 	=  92,
    TIMESTAMP_SQLTYPE 	=  93,

    BINARY_SQLTYPE	=  -2,
    VARBINARY_SQLTYPE 	=  -3,
    LONGVARBINARY_SQLTYPE =  -4,

    NULL_SQLTYPE	=   0,

    /**
     * OTHER indicates that the SQL type is database specific and
     * gets mapped to a Java object which can be accessed via
     * getObject and setObject.
     */
    OTHER_SQLTYPE	= 1111
  };


  static const char* const smallintName__;
  static const char* const integerName__;
  static const char* const floatName__;
  static const char* const doubleName__;
  static const char* const numericName__;
  static const char* const charName__;
  static const char* const varCharName__;
  static const char* const dateName__;
  static const char* const blobName__;
  static const char* const arrayName__;
// CJL-IB6 declare four new constants for new datatypes
  static const char* const timeName__;
  static const char* const sqlDateName__;
  static const char* const timestampName__;
  static const char* const decimalName__;
// CJL-IB6 end change

  // May need to up the size of this array if system table queries
  // get big enough to overflow.
  // getCrossReference is one of the largest queries and exceeds 1024
// CJL-IB6 -- investigate the growth of catalog queries
  char queryStringBuffer_[4096]; 

public:

  IB_Catalog (IB_Status& status, 
              IB_Connection& connection)
    : status_ (&status),
      connection_ (&connection)
  { }

  ~IB_Catalog ()
  { }

  IB_Statement* getProcedures (const IB_STRING procedureNamePattern);

  IB_Statement* getProcedureColumns (const IB_STRING procedureNamePattern,
				     const IB_STRING columnNamePattern);

  IB_Statement* getTables (const IB_STRING tableNamePattern, 
                           const IB_BOOLEAN* types);

  IB_Statement* getTableTypes ();

  IB_Statement* getColumns (const IB_STRING tableNamePattern,
			    const IB_STRING columnNamePattern);

  IB_Statement* getColumnPrivileges (const IB_STRING table,
				     const IB_STRING columnNamePattern);

  IB_Statement* getTablePrivileges (const IB_STRING tableNamePattern);

  IB_Statement* getBestRowIdentifier (const IB_STRING table, int scope, IB_BOOLEAN nullable);

  IB_Statement* getVersionColumns (const IB_STRING table);
  
  IB_Statement* getPrimaryKeys (const IB_STRING table);

  IB_Statement* getExportedKeys (const IB_STRING table);

  IB_Statement* getImportedKeys (const IB_STRING table);

  IB_Statement* getCrossReference (const IB_STRING primaryTable, 
				   const IB_STRING foreignTable);

  IB_Statement* getIndexInfo (const IB_STRING table, 
			      const IB_BOOLEAN unique,
			      const IB_BOOLEAN approximate);

  IB_Statement* getTypeInfo ();

  IB_BOOLEAN allProceduresAreCallable (const IB_STRING user);

  IB_BOOLEAN allTablesAreSelectable (const IB_STRING user);

  static SQLType getSQLType (const int fieldType, 
			     const int fieldSubType, 
			     const int fieldScale);

  static IB_Types::IBType getIBType (const int fieldType, 
				     const int fieldSubType, 
				     const int fieldScale);

  static int getPrecision (const IB_Types::IBType ibType, 
			   const int fieldLength);

  static int getColumnSize (const IB_Types::IBType ibType, 
                            const int fieldLength);

  static IB_LDString getIBTypeName (const IB_Types::IBType ibType);

  // The following are used only by getTypeInfo ()
  static SQLType getSQLType (const IB_Types::IBType ibType);

  static int getMaxPrecision (const IB_Types::IBType ibType);

  static IB_STRING getLiteralPrefix (const IB_Types::IBType ibType);

  static IB_STRING getLiteralSuffix (const IB_Types::IBType ibType);

  static IB_BOOLEAN isCaseSensitive (const IB_Types::IBType ibType);

  static IB_BOOLEAN maybeMoneyValue (const IB_Types::IBType ibType);

  static short getMinScale (const IB_Types::IBType ibType);

  static short getMaxScale (const IB_Types::IBType ibType);
 
  static IB_BOOLEAN isUnsigned (const IB_Types::IBType ibType);

  IB_STRING getDefaultValueFromBlob (const IB_BLOBID blobId);

private:

  //david jencks 1-24-2001
  static const char* const spaces = "                               ";

  IB_Statement* createStatement ();

  //david jencks 1-25-2001
  //not unicode aware
  IB_BOOLEAN hasNoWildcards(IB_STRING pattern);

  //david jencks 1-25-2001
  //not unicode aware
  void stripEscape(IB_STRING pattern, IB_STRING stripped);
  
  //david jencks 1-25-2001
  //not unicode aware
  void ConstructNameCondition(IB_STRING pattern, IB_STRING target,
    IB_STRING column);
  //david jencks 1-26-2001 new method
  IB_Statement* runCatalogQuery(const IB_STRING queryString, 
    const int catalogFunction);
};

#endif


