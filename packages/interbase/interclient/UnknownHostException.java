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
 * A TCP/IP socket connection cannot be established
 * to an unknown host.  Your local TCP/IP host table
 * configuration may not be able to recognize the
 * specified host name.
 * <p>
 * Did you specify a valid host in your database URL?
 * And, are you able to ping the host machine?
 * If the target server machine's host name is not recognized,
 * then try using it's IP address.
 * If you are attempting a localhost connection, and
 * 'localhost' is not recognized, then try using the loopback
 * TCP/IP address of 127.0.0.1.
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#unknownHost ErrorCodes.unknownHost}.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class UnknownHostException extends SQLException
{
  final private static String className__ = "UnknownHostException";

  // *** InterClient constructor ****
  UnknownHostException (ErrorKey errorKey, String host)
  {
    super (className__, errorKey, host);
  }
}
 
