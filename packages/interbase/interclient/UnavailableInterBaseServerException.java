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
 * InterServer is unable to locate the InterBase application or service.
 * <p>
 * If you are running InterBase as an application and not a service,
 * then InterServer must also be running as an application and 
 * not a service.
 * <p>
 * From the InterServer configuration utility
 * in the InterClient program group, 
 * first stop the InterServer service then change the server startup mode
 * from "Service" to "Application".
 * Finally start the InterServer application from the InterServer icon
 * in the InterClient program group.
 * <p>
 * Make sure the InterBase Service is running and is using the 
 * correct version of gds32.dll.
 * The gds32.dll in your system32 directory must be from 
 * the installed version of InterBase,
 * and not a previously installed version.
 * <p>
 * The Delphi 1.0 version of InterBase will not work.
 * <p>
 * Local InterBase 4.1 will not run as a service on NT (this is ok,
 * but you will have to run InterServer as an application).
 * So, with Local InterBase 4.1, 
 * start the server by clicking on the icon in your 
 * program group.
 * Once you see the icon, use one of the 
 * InterBase tools to try and connect to it 
 * (ServerManager might be the easiest one).
 * <p>
 * If you can not connect to InterBase at this point, then make sure:
 * <ul>
 * <li> You have GDS32.DLL in your Windows System directory.
 * <li> You have the following in your services file:   gdb_tcp  3050/tcp
 * <li> Your registry keys are correct (to be sure of this, 
 *      use the instreg or 
 *      regcfg utilities to set them to your InterBase root directory).
 * </ul>
 * <b>Note:</b>
 * <ul>
 * <li> GDS32.DLL is the 32-bit client which comes with Delphi 2.x and 3.0.
        It is installed when you choose to install Local InterBase.
 * <li> WS-V4.0xx is the 16-bit local access server which will not
 *      work with InterClient since it can not load GDS32.DLL.
 * </ul>
 * <p>
 * The error code associated with this exception is
 * isc_unavailable from the InterBase ibase.h file.
 *
 * @docauthor Paul Ostler
 * @docauthor Mark Duquette
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class UnavailableInterBaseServerException extends SQLException 
{
  final private static String className__ = "UnavailableInterBaseServerException";

  // *** InterBase constructor ***
  UnavailableInterBaseServerException  (int errorKeyIndex,
                                        int errorCode,
                                        int ibSQLCode,
                                        String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
 
