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
 * An internal program state inconsistency has been detected
 * and implies a bug in InterClient, InterServer or InterBase.
 * <p>
 * Please contact <a href="mailto:interclient@interbase.com">interclient@interbase.com</a>
 * with the bug code and a small test case if possible.
 * <p>
 * The error codes associated with this exception are
 * {@link ErrorCodes#bugCheck ErrorCodes.bugCheck} for driver generated bug checks,
 * or isc_bug_check from the ibase.h file for an InterBase
 * generated license error.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class BugCheckException extends SQLException
{
  // DEVELOPER NOTE:
  // Client side bug codes start at 100.
  // Server side bug codes start at 10,000.
  // *** This is actually not used in the code, but
  // *** is useful to the developer for assigning new codes,
  // *** just keep incrementing it.  
  // *** Avoid reusing holes!
  // *** Also see server side IB_SQLException.lastBugCodeUsed__
  private final static int lastBugCodeUsed__ = 140;

  final private static String className__ = "BugCheckException";

  // *** InterClient constructor ****
  BugCheckException (ErrorKey errorKey, int bugCode) 
  {
    super (className__, errorKey, String.valueOf (bugCode));
  }

  // *** InterServer constructor ***
  BugCheckException (int errorKeyIndex, int bugCode)
  {
    super (className__, errorKeyIndex, String.valueOf (bugCode));
  }

  // *** InterBase constructor ***
  BugCheckException (int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
 
