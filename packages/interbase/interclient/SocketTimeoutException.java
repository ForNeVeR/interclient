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
 * IO was interrupted while waiting to read data from server,
 * probably due to a connection timeout.
 * <p>
 * The driver uses a connection timeout interval
 * as specified by DriverManager.setLoginTimeout ().
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#socketTimeout ErrorCodes.socketTimeout}.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class SocketTimeoutException extends SQLException
{
  final private static String className__ = "SocketTimeoutException";

  // *** InterClient constructor ****
  SocketTimeoutException (ErrorKey errorKey, Object[] args)
  {
    super (className__, errorKey, args);
  }
}
 
