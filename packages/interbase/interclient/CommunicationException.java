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
 * Networked I/O subsystem communication failure.
 * <p>
 * InterServer must be running, either as a service or an application.
 * If you are using Local InterBase, you must run InterServer
 * as an application so it can talk to the Local InterBase application.
 * <p>
 * InterServer can be run either as an application (on Windows 95/98 and NT),
 * or as a service (NT and Unix only).
 * On Windows NT the InterServer service may be started using
 * the InterServer Configuration Utility in the InterClient program group.
 * Alternatively, the InterServer application may be started by
 * selecting the InterServer icon in the InterClient program group
 * <p>
 * Check the InterServer service configuration, and verify
 * that the port number for InterServer is 3060.
 * <p>
 * On UNIX, inetd must be running.
 * For both Windows and UNIX, interserver must be configured
 * as a TCP service, listening on port 3060.
 * <ul>
 * <li> On NT, check your WINDIR\system32\drivers\etc\services file.
 * <li> On 95, check your WINDIR\services file.
 * <li> On UNIX, check your /etc/services and /etc/inetd.conf files.
 * </ul>
 * <p>
 * In general, this exception represents an error of the
 * underlying I/O subsystem, and can occur for the following reasons.
 * <ul>
 * <li> An IO or socket exception occurred 
 *      while trying to open or close a TCP/IP socket connection to 
 *      the server.
 * <li> An IO exception occurred during a normal socket read or write,
 *      as InterClient tries to communicate across a TCP/IP socket.
 * <li> An IO exception occurred while reading from a user supplied input stream.
 * <li> An unexpected EOF occurred while reading from a user supplied input stream. 
 *      A user supplied input stream to a prepared statement must provide
 *      at least as many bytes as are specified in the length parameter of
 *      methods PreparedStatement.setBinaryStream() and setAsciiStream().
 * </ul>
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#communication ErrorCodes.communication}.
 *
 * @docauthor Paul Ostler
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class CommunicationException extends SQLException
{
  final private static String className__ = "CommunicationException";

  // *** InterClient constructor ****
  CommunicationException (ErrorKey errorKey, String subsystemError, String server)
  {
    super (className__, errorKey, new Object[] { subsystemError, server });
  }

  // *** InterClient constructor ****
  CommunicationException (ErrorKey errorKey, Object arg)
  {
    super (className__, errorKey, arg);
  }

  // *** InterClient constructor ****
  CommunicationException (ErrorKey errorKey)
  {
    super (className__, errorKey);
  }

  // *** InterServer constructor ***
  CommunicationException (int errorKeyIndex)
  {
    super (className__, errorKeyIndex);
  }
}
 
