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
 * A database lock conflict has occured.
 * <p>
 * The error code associated with this exception is
 * isc_lock_conflict from the InterBase ibase.h file.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
public class LockConflictException extends SQLException 
{
  final private static String className__ = "LockConflictException";

  // *** InterBase constructor ***
  LockConflictException (int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }

  // *** For subclasses only ***
  LockConflictException (String subclassName, int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (subclassName, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
 
