{*
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
 *}
{*
 *	PROGRAM:	Registry Configuration Utility
 *	MODULE:		services.pas
 *	DESCRIPTION:	Wrapper for Windows NT services API
 *
 * copyright (c) 1996 by Borland International
 *}

unit services;

interface
uses
  SysUtils, Windows, Messages, Classes, Dialogs, Str_Ids;

type
  SC_HANDLE = THandle;

  svcStatus = record
    serverStatus: integer;
    serverStartup: integer;
  end;

  SERVICE_STATUS  = record
    dwServiceType: DWORD;
    dwCurrentState: DWORD;
    dwControlsAccepted: DWORD;
    dwWin32ExitCode: DWORD;
    dwServiceSpecificExitCode: DWORD;
    dwCheckPoint: DWORD;
    dwWaitHint: DWORD;
  end;

  QUERY_SERVICE_CONFIG = record
    dwServiceType,
    dwStartType,
    dwErrorControl: DWORD;
    lpBinaryPathName,
    lpLoadOrderGroup: PChar;
    dwTagId: DWORD;
    lpDependencies,
    lpServiceStartName,
    lpDisplayName: PChar;
  end;

  ENUM_SERVICE_STATUS = record
    lpServiceName,
    lpDisplayName: PChar;
    ServiceStatus: SERVICE_STATUS;
  end;

  LPENUM_SERVICE_STATUS = ^ENUM_SERVICE_STATUS;
  LPSERVICE_STATUS =  ^SERVICE_STATUS;
  LPQUERY_SERVICE_CONFIG = ^QUERY_SERVICE_CONFIG;

const
  RUNNING = 0;   // used to determine the service status in the radio group
  STOPPED = 1;
  PAUSED = 2;

  START_AUTOMATIC = 0;
  START_MANUAL = 1;
  
  NT_SERVICE_NAME: PChar = 'InterServer';
  
// Constants ported from WINSVC.H and WINNT.H

//
// Value to indicate no change to an optional parameter
//
  SERVICE_NO_CHANGE             = ($ffffffff);

//
//  The following are masks for the predefined standard access types
//
  DELETE                       = ($00010000);
  READ_CONTROL                  = ($00020000);
  WRITE_DAC                     = ($00040000);
  WRITE_OWNER                   = ($00080000);
  SYNCHRONIZE                   = ($00100000);
  STANDARD_RIGHTS_REQUIRED      = ($000F0000);
  STANDARD_RIGHTS_ALL           = ($001F0000);
  SPECIFIC_RIGHTS_ALL           = ($0000FFFF);

  STANDARD_RIGHTS_READ          = READ_CONTROL;
  STANDARD_RIGHTS_WRITE         = READ_CONTROL;
  STANDARD_RIGHTS_EXECUTE       = READ_CONTROL;
//
// Service object specific access type
//
  SERVICE_QUERY_CONFIG         = ($0001);
  SERVICE_CHANGE_CONFIG        = ($0002);
  SERVICE_QUERY_STATUS         = ($0004);
  SERVICE_ENUMERATE_DEPENDENTS = ($0008);
  SERVICE_START                = ($0010);
  SERVICE_STOP                 = ($0020);
  SERVICE_PAUSE_CONTINUE       = ($0040);
  SERVICE_INTERROGATE          = ($0080);
  SERVICE_USER_DEFINED_CONTROL = ($0100);
  SERVICE_ALL_ACCESS           = (STANDARD_RIGHTS_REQUIRED or
                                  SERVICE_QUERY_CONFIG or
                                  SERVICE_CHANGE_CONFIG or
                                  SERVICE_QUERY_STATUS or
                                  SERVICE_ENUMERATE_DEPENDENTS or
                                  SERVICE_START or
                                  SERVICE_STOP or
                                  SERVICE_PAUSE_CONTINUE or
                                  SERVICE_INTERROGATE or
                                  SERVICE_USER_DEFINED_CONTROL);
  SC_MANAGER_CONNECT            = ($0001);
  SC_MANAGER_CREATE_SERVICE     = ($0002);
  SC_MANAGER_ENUMERATE_SERVICE  = ($0004);
  SC_MANAGER_LOCK               = ($0008);
  SC_MANAGER_QUERY_LOCK_STATUS  = ($0010);
  SC_MANAGER_MODIFY_BOOT_CONFIG = ($0020);
  SC_MANAGER_ALL_ACCESS         = (STANDARD_RIGHTS_REQUIRED     or
                                   SC_MANAGER_CONNECT           or
                                   SC_MANAGER_CREATE_SERVICE    or
                                   SC_MANAGER_ENUMERATE_SERVICE or
                                   SC_MANAGER_LOCK              or
                                   SC_MANAGER_QUERY_LOCK_STATUS or
                                   SC_MANAGER_MODIFY_BOOT_CONFIG);
//
// Service State -- for Enum Requests (Bit Mask)
//
  SERVICE_ACTIVE              =  ($00000001);
  SERVICE_INACTIVE            =  ($00000002);
  SERVICE_STATE_ALL           =  (SERVICE_ACTIVE  or
                                  SERVICE_INACTIVE);
//
// Controls
//
  SERVICE_CONTROL_STOP        =  ($00000001);
  SERVICE_CONTROL_PAUSE       =  ($00000002);
  SERVICE_CONTROL_CONTINUE    =  ($00000003);
  SERVICE_CONTROL_INTERROGATE =  ($00000004);
  SERVICE_CONTROL_SHUTDOWN    =  ($00000005);
//
// Service State -- for CurrentState
//
  SERVICE_STOPPED             =  ($00000001);
  SERVICE_START_PENDING       =  ($00000002);
  SERVICE_STOP_PENDING        =  ($00000003);
  SERVICE_RUNNING             =  ($00000004);
  SERVICE_CONTINUE_PENDING    =  ($00000005);
  SERVICE_PAUSE_PENDING       =  ($00000006);
  SERVICE_PAUSED              =  ($00000007);
//
// Controls Accepted  (Bit Mask)
//
  SERVICE_ACCEPT_STOP           = ($00000001);
  SERVICE_ACCEPT_PAUSE_CONTINUE = ($00000002);
  SERVICE_ACCEPT_SHUTDOWN       = ($00000004);
//
// Error control type
//
  SERVICE_ERROR_IGNORE         = ($00000000);
  SERVICE_ERROR_NORMAL         = ($00000001);
  SERVICE_ERROR_SEVERE         = ($00000002);
  SERVICE_ERROR_CRITICAL       = ($00000003);
  SERVICE_WIN32_OWN_PROCESS    = ($000010);
  SERVICE_WIN32_SHARE_PROCESS  = ($000020);
  SERVICE_WIN32                = (SERVICE_WIN32_OWN_PROCESS or
                                  SERVICE_WIN32_SHARE_PROCESS);
//
// Start Type
//
  SERVICE_BOOT_START           = ($00000000);
  SERVICE_SYSTEM_START         = ($00000001);
  SERVICE_AUTO_START           = ($00000002);
  SERVICE_DEMAND_START         = ($00000003);
  SERVICE_DISABLED             = ($00000004);
//
// Service Types (Bit Mask)
//
 SERVICE_KERNEL_DRIVER          = ($00000001);
 SERVICE_FILE_SYSTEM_DRIVER     = ($00000002);
 SERVICE_ADAPTER                = ($00000004);
 SERVICE_RECOGNIZER_DRIVER      = ($00000008);
 SERVICE_DRIVER                 = (SERVICE_KERNEL_DRIVER or
                                   SERVICE_FILE_SYSTEM_DRIVER or
                                   SERVICE_RECOGNIZER_DRIVER);

 SERVICE_INTERACTIVE_PROCESS    = ($00000100);
 SERVICE_TYPE_ALL               = (SERVICE_WIN32   or
                                   SERVICE_ADAPTER or
                                   SERVICE_DRIVER  or
                                   SERVICE_INTERACTIVE_PROCESS);

{ DLL Functions }
function OpenSCManagerA(lpMachineName,
                        lpServicesDBName: Pchar;
                        dwDesiredAccess: DWORD): SC_HANDLE; stdcall;
function CloseServiceHandle(svcHandle: SC_HANDLE): Bool;  stdcall;
function CreateServiceA(hManager: SC_HANDLE;
                        lpServiceName,
                        lpDisplayName: PChar;
                        dwDesiredAccess,
                        dwServiceType,
                        dwStartType,
                        dwErrorControl: DWORD;
                        lpBinaryPathName,
                        lpLoadOrderGroup: PChar;
                        pdwTagId: PDWORD;
                        lpDependencies,
                        lpServiceStartName,
                        lpPassword: PChar): SC_HANDLE; stdcall;
function OpenServiceA(hManager: SC_HANDLE;
                      lpServiceName: PChar;
                      dwDesiredAccess: DWORD): SC_HANDLE; stdcall;
function DeleteService(hService: SC_HANDLE): BOOL; stdcall;
function EnumServicesStatusA(hManager: SC_HANDLE;
                            dwServiceType,
                            dwServiceState: DWORD;
                            lpServiceStatus: LPENUM_SERVICE_STATUS;
                            cbBufSize: DWORD;
                            pcbBytesNeeded,
                            lpServicesReturned,
                            lpResumeHandle: PDWord): BOOL; stdcall;
function ControlService(hService: SC_HANDLE;
                        dwControl: DWORD;
                        lpServiceStatus: LPSERVICE_STATUS): BOOL; stdcall;
function StartServiceA(hService: SC_HANDLE;
                      dwNumServiceArgs: DWORD;
                      lpszServiceArgs: PChar): BOOL; stdcall;
function ChangeServiceConfigA(hservice: SC_HANDLE;
                              dwServiceType,
                              dwStartType,
                              dwErrorControl: DWORD;
                              lpBinaryPathName,
                              lpLoadOrderGroup : PChar;
                              lpdwTagId: PDWORD;
                              lpDependencies,
                              lpServiceStartName,
                              lpPassword,
                              lpDisplayName: PChar): BOOL; stdcall;
function QueryServiceConfigA(hService: SC_HANDLE;
                            lpqscServConfig: LPQUERY_SERVICE_CONFIG;
                            cbBufSize: Dword;
                            lpcbBytesNeeded: LPDWORD): BOOL; stdcall;
function QueryServiceStatus(hService: SC_HANDLE;
                            lpServiceStatus: LPSERVICE_STATUS): BOOL; stdcall;

{ Local Functions }
function OpenServicesManager(dwDesiredAccess: DWORD): SC_HANDLE;
function CloseServicesManager(hManager: SC_HANDLE): BOOL;
function CreateService(hManager: SC_HANDLE;
                       lpServiceName,
                       lpDisplayName,
                       lpBinaryPathName: PChar;
                       dwStartType: DWORD): SC_HANDLE;
function OpenService(hManager: SC_HANDLE;
                     lpServiceName: PChar;
                     dwDesiredAccess: DWORD): SC_HANDLE;

function EnumServicesStatus(hManager: SC_HANDLE;
                            dwServiceType,
                            dwServiceState: DWORD;
                            lpServiceStatus: LPENUM_SERVICE_STATUS;
                            cbBufSize: DWORD;
                            pcbBytesNeeded,
                            lpServicesReturned,
                            lpResumeHandle: PDWord): BOOL;
function StartService(hService: SC_HANDLE;
                      dwNumServiceArgs: DWORD;
                      lpszServiceArgs: PChar): BOOL;
function ChangeServiceConfig(hservice: SC_HANDLE;
                              dwServiceType,
                              dwStartType,
                              dwErrorControl: DWORD;
                              lpBinaryPathName,
                              lpLoadOrderGroup : PChar;
                              lpdwTagId: PDWORD;
                              lpDependencies,
                              lpServiceStartName,
                              lpPassword,
                              lpDisplayName: PChar): BOOL;

function QueryServiceConfig(hService: SC_HANDLE;
                            lpqscServConfig: LPQUERY_SERVICE_CONFIG;
                            cbBufSize: Dword;
                            lpcbBytesNeeded: PDWORD): BOOL;
function GetServiceStatus(lpServiceName: PChar): short;
procedure ChangeServiceStartup(dwStartupMode: DWord);
function DeleteServiceEntry: BOOL;

implementation

{ DLL Function Calls }
function OpenSCManagerA; external 'ADVAPI32.DLL';
function CloseServiceHandle; external 'ADVAPI32.DLL';
function CreateServiceA; external 'ADVAPI32.DLL';
function OpenServiceA; external 'ADVAPI32.DLL';
function DeleteService; external 'ADVAPI32.DLL';
function EnumServicesStatusA; external 'ADVAPI32.DLL';
function ControlService; external 'ADVAPI32.DLL';
function StartServiceA; external 'ADVAPI32.DLL';
function ChangeServiceConfigA; external 'ADVAPI32.DLL';
function QueryServiceConfigA; external 'ADVAPI32.DLL';
function QueryServiceStatus; external 'ADVAPI32.DLL';

{ Local Function Implementation }
(****************************************************************************
*                                                                           *
*                   O P E N  S E R V I C E S M A N A G E R                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Opens the Windows NT services manager and returns the handle           *
*                                                                           *
*****************************************************************************)
function OpenServicesManager(dwDesiredAccess: DWORD): SC_HANDLE;
var
  hManager: SC_HANDLE;

begin
  hManager := OpenSCManagerA(nil,nil,dwDesiredAccess);
  if not (hManager = 0) then
    result := hManager
  else
   result := 0;
end; { OpenServicesManager }

(****************************************************************************
*                                                                           *
*           C L O S E  S E R V I C E S  M A N A G E R                       *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Closes the Windows NT servics manager and returns TRUE or FALSE        *
*                                                                           *
*****************************************************************************)
function CloseServicesManager(hManager: SC_HANDLE): BOOL;

begin
  if not closeServiceHandle(hManager) then begin
    result := FALSE;
  end else
    result :=  TRUE;
end; { CloseServicesManager }

(****************************************************************************
*                                                                           *
*                       C R E A T E  S E R V I C E                          *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Creates the desired service and returns the handle to it               *
*                                                                           *
*****************************************************************************)
function CreateService(hManager: SC_HANDLE;
                       lpServiceName,
                       lpDisplayName,
                       lpBinaryPathName: PChar;
                       dwStartType: DWORD): SC_HANDLE;
var
  hService: SC_HANDLE;

begin
  hService := CreateServiceA(
       hManager,                  // SCManager database
       lpServiceName,             // name of service
       lpDisplayName,             // service name to display
       SERVICE_ALL_ACCESS,        // desired access
       (SERVICE_WIN32_OWN_PROCESS or SERVICE_INTERACTIVE_PROCESS), // service type
       dwStartType,      // start type
       SERVICE_ERROR_NORMAL,      // error control type
       lpBinaryPathName,          // service binary path
       nil,                      // no load ordering group
       nil,                      // no tag identifier
       nil,                      // no dependencies
       nil,                      // LocalSystem account
       nil);                     // no password
  result := hService;
end; { CreateService }

(****************************************************************************
*                                                                           *
*                       O P E N  S E R V I C E                              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Opens the desired service and returns the handle to it.                *
*                                                                           *
*****************************************************************************)
function OpenService(hManager: SC_HANDLE;
                     lpServiceName: PChar;
                     dwDesiredAccess: DWORD): SC_HANDLE;
var
  hService: SC_HANDLE;

begin
  hService := OpenServiceA(hManager,lpServiceName,dwDesiredAccess);
  result := hService;
end; { OpenService }

(****************************************************************************
*                                                                           *
*                    E N U M  S E R V I C E S  S T A T U S                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Enumerates the status for a given service and passes the information   *
*    back in the lpServiceStatus structure.                                 *
*                                                                           *
*****************************************************************************)
function EnumServicesStatus(hManager: SC_HANDLE;
                            dwServiceType,
                            dwServiceState: DWORD;
                            lpServiceStatus: LPENUM_SERVICE_STATUS;
                            cbBufSize: DWORD;
                            pcbBytesNeeded,
                            lpServicesReturned,
                            lpResumeHandle: PDWord): BOOL;
var
  retval: BOOL;

begin
  retval := EnumServicesStatus( hManager,dwServiceType,dwServiceState,
                                lpServiceStatus,cbBufSize,pcbBytesNeeded,
                                lpServicesReturned,lpResumeHandle);
  result := retval;
end; { EnumServicesStatus }

(****************************************************************************
*                                                                           *
*                       S T A R T  S E R V I C E                            *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Starts the desired service                                             *
*                                                                           *
*****************************************************************************)
function StartService(hService: SC_HANDLE;
                      dwNumServiceArgs: DWORD;
                      lpszServiceArgs: PChar): BOOL;
var
  retval: BOOL;

begin
  retval := StartServiceA(hService,dwNumServiceArgs,@lpszServiceArgs);
  result := retval;
end; { StartService }

(****************************************************************************
*                                                                           *
*                 C H A N G E  S E R V I C E  C O N F I G                   *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Changes the configuration for a service in the services database       *
*                                                                           *
*****************************************************************************)
function ChangeServiceConfig(hservice: SC_HANDLE;
                              dwServiceType,
                              dwStartType,
                              dwErrorControl: DWORD;
                              lpBinaryPathName,
                              lpLoadOrderGroup : PChar;
                              lpdwTagId: PDWORD;
                              lpDependencies,
                              lpServiceStartName,
                              lpPassword,
                              lpDisplayName: PChar): BOOL;

begin
  result := ChangeServiceConfigA(hservice,
                              dwServiceType,
                              dwStartType,
                              dwErrorControl,
                              lpBinaryPathName,
                              lpLoadOrderGroup,
                              lpdwTagId,
                              lpDependencies,
                              lpServiceStartName,
                              lpPassword,
                              lpDisplayName);
end;  { ChangeServiceConfig }

(****************************************************************************
*                                                                           *
*                       G E T  S E R V I C E  S T A T U S                   *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Queries a service for its current status and passes the information    *
*    back in the lpServiceStatus structure.                                 *
*                                                                           *
*****************************************************************************)
function GetServiceStatus(lpServiceName: PChar): short;
var
  hManager,
  hService : SC_HANDLE;
  lpsServiceStatus: SERVICE_STATUS;
  retval: Dword;

begin
  hManager := OpenServicesManager(SC_MANAGER_CONNECT);
  hService := OpenService(hManager,lpServiceName,SERVICE_QUERY_STATUS);
  if QueryServiceStatus(hService, @lpsServiceStatus) then begin
    retval := lpsServiceStatus.dwCurrentState;
    case retval of
      SERVICE_STOPPED: result := STOPPED;
      SERVICE_STOP_PENDING: result := STOPPED;
      SERVICE_RUNNING: result := RUNNING;
      SERVICE_START_PENDING: result := RUNNING;
      SERVICE_CONTINUE_PENDING: result := PAUSED;
      SERVICE_PAUSE_PENDING: result := PAUSED;
      SERVICE_PAUSED: result := PAUSED
      else
        result := RUNNING;
    end;
  end else
    result := STOPPED;
  CloseServiceHandle(hService);
  CloseServicesManager(hManager);
end; { GetServiceStatus }

(****************************************************************************
*                                                                           *
*                    Q U E R Y  S E R V I C E  C O N F I G                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Queries the services database for information about a particular       *
*    service                                                                *
*                                                                           *
*****************************************************************************)
function QueryServiceConfig(hService: SC_HANDLE;
                            lpqscServConfig: LPQUERY_SERVICE_CONFIG;
                            cbBufSize: Dword;
                            lpcbBytesNeeded: PDWORD): BOOL;
begin
  result := QueryServiceConfigA(hService,lpqscServConfig,cbBufSize,lpcbBytesNeeded);
end; { QueryServiceConfig }

(****************************************************************************
*                                                                           *
*                    C H A N G E  S E R V I C E  S T A R T U P              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Changes the how InterBase will startup when the user starts NT         *
*                                                                           *
*****************************************************************************)
procedure ChangeServiceStartup(dwStartupMode: DWord);
var
  hManager,
  hService : SC_HANDLE;

begin
  hManager := OpenServicesManager(SC_MANAGER_CONNECT);
  hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_CHANGE_CONFIG);
  if hService = 0 then
    ShowError(GetLastError(),'OpenServiceManager');
  if not ChangeServiceConfig(hService,SERVICE_NO_CHANGE,dwStartupMode,
                         SERVICE_NO_CHANGE,nil,nil,nil,nil,nil,nil,nil) then
     showError(GetLastError(),'ConfigService');
  CloseServiceHandle(hService);
  CloseServicesManager(hManager);
end; { ChangeServiceStartup }

(****************************************************************************
*                                                                           *
*                   D E L E T E  S E R V I C E  E N T R Y                   *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Removes the interbase service from the registry                        *
*                                                                           *
*****************************************************************************)
function DeleteServiceEntry: BOOL;
var
 hManager,
 hService: SC_HANDLE;
begin
  hManager := OpenServicesManager(SC_MANAGER_All_ACCESS);
  if hManager = 0 then begin
    MessageDlg(LoadStr(errNeedAdminRights),mtInformation,[mbOK],0);
    result := FALSE;
  end
  else begin
    hService := OpenService(hManager,NT_SERVICE_NAME,DELETE);
    DeleteService(hService);
    CloseServicesManager(hManager);
    Result := TRUE;
  end;
end; { DeleteServiceEntry }

end. { UNIT }
