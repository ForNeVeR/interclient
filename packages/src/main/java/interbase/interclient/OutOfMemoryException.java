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
 * InterBase or InterServer host memory has been exhausted.
 * <p>
 * The error codes associated with this exception are
 * {@link ErrorCodes#outOfMemory ErrorCodes.outOfMemory} for driver generated bug checks,
 * or isc_bufexh, isc_virmemexh from the ibase.h file for 
 * an InterBase generated license error.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class OutOfMemoryException extends SQLException
{
  final private static String className__ = "OutOfMemoryException";

  // *** InterServer constructor ***
  OutOfMemoryException (int errorKeyIndex)
  {
    super (className__, errorKeyIndex);
  }

  // *** InterBase constructor ***
  OutOfMemoryException (int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
