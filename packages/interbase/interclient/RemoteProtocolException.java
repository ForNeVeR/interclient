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
 * InterClient remote client/server communication protocol exception.
 * <p>
 * This exception is thrown if InterClient
 * receives an unexpected or unintelligable message
 * from InterServer (or vice-versa).
 * <p>
 * This exception should not occur under normal operation.
 * If a remote protocol exception does occur, it
 * indicates either a bug in the client/server communication protocol
 * between InterClient and InterServer, or a loss of network
 * integrity.
 * <p>
 * This can occur for the following reasons:
 * <ul>
 * <li> An internal bug occurred in the remote protocol between InterClient and InterServer.
 * <li> InterServer terminated abnormally, disabling communication with InterClient.
 * <li> The network between InterClient and InterServer disappears.
 * </ul>
 * On Unix, if you suspect a bug, check for a core file in the root directory 
 * and if there is one get a stack trace using a command such as 
 * <pre>
 * % dbx /usr/interclient/bin/interserver core
 * dbx> where
 * </pre>
 * On NT or Win95, check if InterServer has GPF'ed.
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#remoteProtocol ErrorCodes.remoteProtocol}.
 *
 * @docauthor Paul Ostler
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class RemoteProtocolException extends SQLException
{
  // DEVELOPER NOTE:
  // *** This is actually not used in the code, but
  // *** is useful to the developer for assigning new codes,
  // *** just keep incrementing it.  
  // *** Avoid reusing holes!
  private final static int lastCodeUsed__ = 105;

  final private static String className__ = "RemoteProtocolException";

  // *** InterClient constructor ****
  RemoteProtocolException (ErrorKey errorKey, int internalCode) 
  {
    super (className__, errorKey, String.valueOf (internalCode));
  }

  // *** InterClient constructor ****
  RemoteProtocolException (ErrorKey errorKey) 
  {
    super (className__, errorKey);
  }

  // *** InterServer constructor ***
  RemoteProtocolException (int errorKeyIndex)
  {
    super (className__, errorKeyIndex);
  }
  
    //david jencks 2-5-2001
    RemoteProtocolException (ErrorKey errorKey, Object[] args)
    {
        super(className__, errorKey, args);
    }

}
 
