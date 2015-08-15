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
 * An improper InterClient/InterServer installed configuration has been detected. 
 * <p>
 * Currently, this can occur for the following reasons:
 * <ul>
 * <li> Incompatible versions of InterClient and InterServer tried to communicate. 
 * <li> The Java SecurityManager does not allow socket connections to server on port 3060.
 * When running applets, a common reason for this exception is that interclient.jar 
 * is being picked up from your local classpath.  So the JVM is using local
 * classes to try to connect to a remote server, rather than using a
 * dynamically downloaded interclient.jar from the remote server.
 * See <a href="../../../help/icTroubleshooting.html">
 * Troubleshooting 
 * </a>
 * <li> Client or browser is using an unsupported jdk version. 
 * <li> The set of InterClient library class files (interbase/interclient/*.class)
 *      is incomplete.
 * </ul>
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#badInstallation ErrorCodes.badInstallation}.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font>
 **/
final public class BadInstallationException extends SQLException 
{
  final private static String className__ = "BadInstallationException";

  // *** InterClient constructor **** 
  BadInstallationException (ErrorKey errorKey, Object[] args)
  {
    super (className__, errorKey, args);
  }

  // *** InterClient constructor ****
  BadInstallationException (ErrorKey errorKey)
  {
    super (className__, errorKey);
  }
}


