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
package interbase.interclient;

// Self-contained utilities.
// Don't reference any other driver classes from within this class.
final class Utils
{

  // This is necessary for java.text.MessageFormat.format to work
  // properly because arguments may not be null.
  static String getMessage (Exception e)
  {
    return (e.getMessage() == null) ? "" : e.getMessage();
  }

  static java.sql.SQLException accumulateSQLExceptions (java.sql.SQLException accumulatedExceptions,
			                                java.sql.SQLException nextException)
  {
    if (accumulatedExceptions == null)
      return nextException;
    else {
      accumulatedExceptions.setNextException (nextException);
      return accumulatedExceptions;
    }
  }

  static String getSQLTypeName (int sqlType)
  {
    switch (sqlType) {
    case java.sql.Types.BIGINT:
      return "BIGINT";
    case java.sql.Types.BINARY:
      return "BINARY";
    case java.sql.Types.BIT:
      return "BIT";
    case java.sql.Types.CHAR:
      return "CHAR";
    case java.sql.Types.DATE:
      return "DATE";
    case java.sql.Types.DECIMAL:
      return "DECIMAL";
    case java.sql.Types.DOUBLE:
      return "DOUBLE";
    case java.sql.Types.FLOAT:
      return "FLOAT";
    case java.sql.Types.INTEGER:
      return "INTEGER";
    case java.sql.Types.LONGVARBINARY:
      return "LONGVARBINARY";
    case java.sql.Types.LONGVARCHAR:
      return "LONGVARCHAR";
    case java.sql.Types.NULL:
      return "NULL";
    case java.sql.Types.NUMERIC:
      return "NUMERIC";
    case java.sql.Types.OTHER:
      return "OTHER";
    case java.sql.Types.REAL:
      return "REAL";
    case java.sql.Types.SMALLINT:
      return "SMALLINT";
    case java.sql.Types.TIME:
      return "TIME";
    case java.sql.Types.TIMESTAMP:
      return "TIMESTAMP";
    case java.sql.Types.TINYINT:
      return "TINYINT";
    case java.sql.Types.VARBINARY:
      return "VARBINARY";
    case java.sql.Types.VARCHAR:
      return "VARCHAR";
    default:
      return null;
    }
  }
}

