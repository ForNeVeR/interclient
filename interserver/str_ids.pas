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
 *	MODULE:		str_ids.pas
 *	DESCRIPTION:	string IDs for REGCFG.DLL
 *
 * copyright (c) 1996 by Borland International
 *}

unit str_ids;

interface

uses  Classes, SysUtils, dialogs, windows;

const
// String IDs for controls on the main dialog
 SvrConfigTitle = 38;
 MainDlgTitle = 1;
 SvcPriorityTitle = 2;
 SvcCtrlTitle = 3;
 AppPathTitle = 4;
 SvrStartupTitle = 5;
 startupGroupTitle = 59;

// String IDs for controls on the browser dialog
 browseDlgInfo = 100;
 pathEdit = 101;
 txtdirBox = 102;
 txtdriveBox = 103;
 browseDlgTitleLicense = 104;
 browseDlgTitleServer = 105;

 // String IDs for buttons being used on the main dialog and browser dialog
 txtbtnChange = 6;
 txtbtnRemove = 7;
 txtbtnOK = 8;
 txtbtnCancel = 9;
 txtbtnFinish = 10;
 txtWarning = 60;
 
// constants for the browser dialog
 feLicenseFile = 36;
 feLicenseExt = 37;
 feServerExt = 40;
 feServerFile = 41;

// Icon title
 iconName =  48;

// Error Message IDs for the main dialog and the browser dialog
 errStopSvc = 11;
 errRemoveIB = 12;
 errUsingDefault = 13;
 errLaunchingMain = 14;
 errLaunchingBrowser = 15;
 errCantStartSvc = 16;
 errCantStopSvc = 17;
 errCantPauseSvc = 18;
 errCantRemoveSvc = 19;
 errCantOpenSvcDb = 20;
 errSvcDeleted = 21;
 errSvcDisabled = 22;
 errSvcRunning = 23;
 errNeedToStartSvc = 24;
 errSvcPathNotFound = 25;
 errDriveInvalid = 26;
 errNoDirectoryGiven = 27;
 errCantAddToStartup = 28;
 errCantRemoveFromStartup = 29;
 errCantAddToRegistry = 30;
 errCantQryStartup = 31;
 errServerNotFound = 32;
 errInvalidDirectory = 35;
 errInvalidServer = 42;
 errInvalidParameter = 900;
 errCantStartApplication = 39;
 errCantInstallService = 44;
 errCantFindLicense = 45;
 errMustBeAdmin = 46;
 errNeedAdminRights = 47;

 // dialog messages
 msgStartServer = 43;

// String IDs for radio buttons
 winStartup = 50;
 winManual = 51;
 winSvcStartup = 52;
 winSvcDemand = 53;
 svcNormal = 54;
 svcHigh = 55;
 svcStart = 56;
 svcStop = 57;
 svcPause = 58;
 
procedure ShowError(ErrorConst: DWORD; strArea: string);

implementation

(****************************************************************************
*                                                                           *
*                       S H O W  E R R O R                                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Displays the appropriate error message to the user                     *
*                                                                           *
*****************************************************************************)
procedure ShowError(ErrorConst: DWORD; strArea: string);
var
  errString: String;

begin
  case ErrorConst of
    ERROR_INSUFFICIENT_BUFFER: errString := 'ERROR_INSUFFICIENT_BUFFER';
    ERROR_CIRCULAR_DEPENDENCY: errString := 'ERROR_CIRCULAR_DEPENDENCY';
    ERROR_DUP_NAME: errString := 'ERROR_DUP_NAME';
    ERROR_INVALID_HANDLE: errString := 'ERROR_INVALID_HANDLE';
    ERROR_INVALID_NAME: errString := 'ERROR_INVALID_NAME';
    ERROR_INVALID_PARAMETER: errString := 'ERROR_INVALID_PARAMETER';
    ERROR_INVALID_SERVICE_ACCOUNT: errString := 'ERROR_INVALID_SERVICE_ACCOUNT';
    ERROR_SERVICE_EXISTS: errString := 'ERROR_SERVICE_EXISTS';
    ERROR_DEPENDENT_SERVICES_RUNNING: errString := 'ERROR_DEPENDENT_SERVICES_RUNNING';
    ERROR_INVALID_SERVICE_CONTROL: errString := 'ERROR_INVALID_SERVICE_CONTROL';
    ERROR_SERVICE_CANNOT_ACCEPT_CTRL: errString := 'ERROR_SERVICE_CANNOT_ACCEPT_CONTROL';
    ERROR_SERVICE_REQUEST_TIMEOUT: errString := 'ERROR_SERVICE_REQUEST_TIMEOUT';
    ERROR_SERVICE_DATABASE_LOCKED:errString := 'ERROR_SERVICE_DATABASE_LOCKED';
    ERROR_SERVICE_DEPENDENCY_DELETED:errString := 'ERROR_SERVICE_DEPENDENCY_DELETED';
    ERROR_SERVICE_DEPENDENCY_FAIL:errString := 'ERROR_SERVICE_DEPENDENCY_FAIL';
    ERROR_SERVICE_LOGON_FAILED:errString := 'ERROR_SERVICE_LOGON_FAILED';
    ERROR_SERVICE_NO_THREAD:errString := 'ERROR_SERVICE_NO_THREAD';

    ERROR_SERVICE_MARKED_FOR_DELETE:errString := loadStr(errSvcDeleted);
    ERROR_SERVICE_DISABLED:errString := LoadStr(errSvcDisabled);
    ERROR_PATH_NOT_FOUND: errString := LoadStr(errSvcPathNotFound);
    ERROR_SERVICE_ALREADY_RUNNING:errString := loadStr(errSvcRunning);
    ERROR_SERVICE_NOT_ACTIVE: errString := LoadStr(errNeedToStartSvc);
    ERROR_ACCESS_DENIED: errString := LoadStr(errCantOpenSvcDB);

    // Non-Windows API messages
    ErrInvalidParameter: errString := 'LaunchInstReg was called with an invalid startup parameter.';
    ErrStopSvc : errString := LoadStr(errStopSvc);
    ErrRemoveIB : errString := LoadStr(errRemoveIB);
    ErrCantStartSvc : errString := loadStr(errCantStartSvc);
    ErrCantStopSvc : errString := loadStr(errCantStopSvc);
    ErrCantOpenSvcDB : errString := LoadStr(errCantOpenSvcDB);
    ErrCantRemoveSvc : errString := LoadStr(errCantRemoveSvc);
    ErrLaunchingBrowser : errString := LoadStr(errLaunchingBrowser);
    ErrLaunchingMain : errString := LoadStr(errLaunchingMain);
    ErrUsingDefault : errString := LoadStr(errUsingDefault);
    ErrCantQryStartup: errString := LoadStr(errCantQryStartup);
    ErrInvalidDirectory: errString := LoadStr(errInvalidDirectory);
    ErrInvalidServer: errString := LoadStr(errInvalidServer);
    ErrCantPauseSvc: errString := LoadStr(errCantPauseSvc);
    ErrSvcDeleted: errString := LoadStr(ErrSvcDeleted);
    ErrSvcDisabled: errString := LoadStr(ErrSvcDisabled);
    ErrSvcRunning: errString := LoadStr(ErrSvcRunning);
    ErrNeedToStartSvc: errString := LoadStr(ErrNeedToStartSvc);
    ErrSvcPathNotFound: errString := LoadStr(ErrSvcPathNotFound);
    ErrDriveInvalid: errString := LoadStr(ErrDriveInvalid);
    ErrNoDirectoryGiven: errString := LoadStr(ErrNoDirectoryGiven);
    ErrCantAddToStartup: errString := LoadStr(ErrCantAddToStartup);
    ErrCantRemoveFromStartup: errString := LoadStr(ErrCantRemoveFromStartup);
    ErrCantAddToRegistry: errString := LoadStr(ErrCantAddToRegistry);
    ErrServerNotFound: errString := LoadStr(ErrServerNotFound);
    ErrCantStartApplication: errString := LoadStr(ErrCantStartApplication);
    ErrCantInstallService: errString := LoadStr(ErrCantInstallService);
    ErrCantFindLicense: errString := LoadStr(errCantFindLicense);
    else errString := format('Unknown error in module: %s',[StrArea]);
  end;
  MessageDlg(format(errString,[#13#10,#13#10,#13#10]),mtError,[mbOK],0);
end; { ShowError }
end.
