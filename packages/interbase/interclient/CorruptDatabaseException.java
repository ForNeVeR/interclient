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

/**
 * A database inconsistency has been detected which 
 * may indicate a corrupt database.
 * <p>
 * The error codes associated with this exception are
 * isc_bad_checksum, isc_badpage, or isc_db_corrupt
 * from the InterBase ibase.h file.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class CorruptDatabaseException extends SQLException 
{
  final private static String className__ = "CorruptDatabaseException";

  // *** InterBase constructor ***
  CorruptDatabaseException (int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
