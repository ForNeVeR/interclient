(*
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
 *)
(***********************************************************************
 *                       REGCFG.DLL Config program                     *
 *           copyright (c) 1996 by Borland International               *
 ***********************************************************************
 * DLL Notes:                                                          *
 *                                                                     *
 *    This driver is used to to allow the user to configure the        *
 *    startup parameters for the InterBase on their machine.           *
 *    This DLL can be called in three different ways and the look of   *
 *    the dialog is determined by the paramter passed in by the DLL.   *
 *                                                                     *
 * To call the DLL from a Delphi Installation program:                 *
 *    Call the function LaunchInstReg with the following paramters:    *
 *         lpInstallPath : PChar (Character pointer)                   *
 *         shStartFlag: DELPHI_INSTALL  (0)                            *
 *                                                                     *
 *    This will load the dialog with options that are applicable to    *
 *    users who are installing the product with Delphi32.              *
 *    It also displays the path where the user is going to be          *
 *    installing InterBase.                                            *
 *                                                                     *
 * To call the DLL from an InterBase Installation program:             *
 *    Call the function LaunchInstReg with the following paramters:    *
 *         lpInstallPath : PChar (Character pointer)                   *
 *         shStartFlag: IB_INSTALL  (1)                                *
 *                                                                     *
 *    This will load the dialog with options that are applicable to    *
 *    users who are installing the product with InterBase Server.      *
 *    It also displays the path where the user is going to be          *
 *    installing InterBase.                                            *
 *                                                                     *
 * To call the DLL from this driver program such that the user is      *
 * only sees the options that are valid for an installation with       *
 * Delphi32:                                                           *
 *    Call the function LaunchInstReg with the following paramters:    *
 *         lpInstallPath : nil                                         *
 *         shStartFlag: DELPHI_SHIP (2)                                *
 *                                                                     *
 *    This will load the dialog with options that are applicable to    *
 *    users who have installed the product with Delphi32.  The user    *
 *    will not see any options to run the server as a service under    *
 *    Windows NT.                                                      *
 *                                                                     *
 * To call the DLL from this driver program such that the user is      *
 * only sees the options that are valid for an installation with       *
 * InterBase Server:                                                   *
 *    Call the function LaunchInstReg with the following paramters:    *
 *         lpInstallPath : nil                                         *
 *         shStartFlag: IB_SHIP (3)                                    *
 *                                                                     *
 *    This will load the dialog with options that are applicable to    *
 *    users who have installed the product with InterBase Server.  The *
 *    user will see the options to run the server as a service under   *
 *    Windows NT.                                                      *
 ***********************************************************************)

program isconfig;

uses
  Forms,
  main in 'main.pas' {dlgMain},
  services in 'SERVICES.PAS';

{$R *.RES}

begin
  Application.Initialize;
  Application.Title := 'InterServer Configuration Utility';
  (** Application.HelpFile := 'isconfig.hlp'; **)
  Application.CreateForm(TdlgMain, dlgMain);
  Application.Run;
end.
