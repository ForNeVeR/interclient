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
 *	PROGRAM:	InterServer Configuration Utility
 *	MODULE:		main.pas
 *	DESCRIPTION:	Main module for ISCONFIG.DLL
 *
 * copyright (c) 1996 by Borland International
 *}

unit main;

interface

uses
  SysUtils, Windows, Messages, Classes, Graphics, Controls, Forms, Dialogs,
  StdCtrls, ComCtrls, ExtCtrls, Registry, Services, DdeMan, str_ids, FileCtrl,
  Buttons;

type

(* Application info is the structure created that holds the configuration
 * information once the user presses the OK button.
 *)
  ApplicationInfo = record
    close,                // FALSE if ErrorCondition
    startServer,          // Can only be set if called via install
    makeChanges: Bool;    // Update InterServer's startup configuration
    rootDir: string;      // InterServer root dir
    svrMode,              // Specifies service or app (used with startServer)
    svcStartUp,           // Service startup mode
    svrStartUp: short;    // the actual server startup mode
    install: bool;
    svrVersion: pChar;
    bRedClicked: bool;
    bGreenClicked: bool;
    bYellowClicked: bool;
  end;

  TmyImageList = array[0..2] of TBitmap;

  TdlgMain = class(TForm)
    PageControl1: TPageControl;
    AdvancedPage: TTabSheet;
    openDialog: TOpenDialog;
    btnOK: TButton;
    btnCancel: TButton;
    GeneralPage: TTabSheet;
    Image2: TImage;
    Label1: TLabel;
    Label3: TLabel;
    Label4: TLabel;
    Label5: TLabel;
    Bevel1: TBevel;
    ServerStartup: TComboBox;
    Label6: TLabel;
    Bevel2: TBevel;
    StartupMode: TComboBox;
    Image3: TImage;
    Label7: TLabel;
    Bevel4: TBevel;
    Label9: TLabel;
    Label10: TLabel;
    Label11: TLabel;
    Panel1: TPanel;
    greenLight: TImage;
    yellowLight: TImage;
    redLight: TImage;
    txtStatus: TPanel;
    txtVersion: TPanel;
    txtOS: TPanel;
    txtServices: TPanel;
    Label2: TLabel;
    AppPath: TEdit;
    btnBrowse: TBitBtn;
    Bevel3: TBevel;
    warningTxt: TLabel;
    btnRemove: TButton;
    Image1: TImage;
    txtStop: TLabel;
    txtPause: TLabel;
    txtStart: TLabel;
    procedure btnCancelClick(Sender: TObject);
    procedure btnOKClick(Sender: TObject);
    procedure btnRemoveClick(Sender: TObject);
    procedure ServerStartupClick(Sender: TObject);
    procedure greenLightClick(Sender: TObject);
    procedure redLightClick(Sender: TObject);
    procedure yellowLightClick(Sender: TObject);
    procedure AppPathChange(Sender: TObject);
    procedure StartupModeChange(Sender: TObject);
    procedure rbNormalClick(Sender: TObject);
    procedure rbHighClick(Sender: TObject);
    procedure SetupDialog(Sender: TObject);
    procedure btnBrowseClick(Sender: TObject);
  private
    { Private declarations }
    RedList: TmyImageList;
    GreenList: TmyImageList;
    YellowList: TmyImageList;

    procedure CreateLabels(startupFlag: short);
    procedure SetServiceControl(svcStatus: integer);
    procedure DisableServiceControl(bDisable: boolean);
    function MakeInitRegEntries(var svrPath: string; closeApp: boolean): Bool;
    procedure FillBitmapArray;
    procedure DestroyBitmapArray;    
  public
    { Public declarations }
  end;

{ Functions and Procedure Definitions }
procedure RemoveServerFromStartup(iconTitle: string);
function CheckStartupGroup(var iconTitle: string): integer;
procedure GetServerVersion(var lpServerVersion: pointer);
procedure MakeRequestedChanges(var AppInfo: ApplicationInfo);
procedure AddServerToStartup(lpBinPath: String);
function IsServerRunning: BOOL;
function GetStartStatus: integer;
function InstallService(svcStartType: DWORD; svcStartMode: short; lpBinPath: PChar): BOOL;
function IsNTAdministrator: BOOL;
function GetNTShell: string;

const
  IBSERVER = 4;

  CLOSE_APP = -1;
  SERVICE_AUTO = 0;
  SERVICE_MANUAL = 1;
  APPLICATION_AUTO = 2;
  APPLICATION_MANUAL = 3;

  ctrlRootDir = 0;
  ctrlSvrStartup = 1;
  ctrlStartMode = 2;
  ctrlSvcPriority = 3;

  appStartup = 0;    {  flags to stop events from happening during the }
  appRunning = 1;    {  startup of the application }

  RedLightOff = 1;
  RedLightOn = 2;
  GreenLightOff = 3;
  GreenLightOn = 4;
  YellowLightOff = 5;
  YellowLightOn = 6;
  LightDisabled = 7;

var
  dlgMain: TdlgMain;
  verInfo: TOSVersionInfo;
  AppFlag : short;
  AppInfo: ApplicationInfo;
  STARTUPGROUP: string;
  Startup : BOOL;
  arChanges: array[0..3] of boolean;

implementation

{$R *.DFM}
{$R isconfig32.res}
{$R version_w32.res}

(****************************************************************************
*                                                                           *
*                       F I L L  B I T M A P  A R R A Y                     *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Fills an array of bitmaps from the resource.  This is used to       *
*       workaround a pallete bug in Delphi 2.0                              *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.FillBitmapArray;
var
   x: integer;

begin
for x:=0 to 2 do
    redlist[x] := TBitmap.Create;

for x:=0 to 2 do
    yellowlist[x] := TBitmap.Create;

for x:=0 to 2 do
    greenlist[x] := TBitmap.Create;

 // Load the information into each array
 redlist[0].LoadFromResourceID(hInstance,RedLightOn);
 redlist[1].LoadFromResourceID(hInstance,RedLightOff);
 redlist[2].LoadFromResourceID(hInstance,LightDisabled);

 greenlist[0].LoadFromResourceID(hInstance,GreenLightOn);
 greenlist[1].LoadFromResourceID(hInstance,GreenLightOff);
 greenlist[2].LoadFromResourceID(hInstance,LightDisabled);

 yellowlist[0].LoadFromResourceID(hInstance,YellowLightOn);
 yellowlist[1].LoadFromResourceID(hInstance,YellowLightOff);
 yellowlist[2].LoadFromResourceID(hInstance,LightDisabled);
end;

(****************************************************************************
*                                                                           *
*                       D E S T R O Y  B I T M A P  A R R A Y               *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Destroys an array of bitmaps from the resource.  This is used to       *
*       workaround a pallete bug in Delphi 2.0                              *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.DestroyBitmapArray;
var
   x: integer;

begin
  for x:=0 to 2 do
    redlist[x].Free;

  for x:=0 to 2 do
    yellowlist[x].Free;

  for x:=0 to 2 do
    greenlist[x].Free;
end;

(****************************************************************************
*                                                                           *
*                       C R E A T E  L A B E L S                            *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Reads the label information out of the resource file and assigns    *
*       them to the labels on the form.                                     *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.CreateLabels(startupFlag: Short);
const
  startupKey: Pchar = 'Software\Microsoft\Windows NT\CurrentVersion\Program Manager\Settings';
var
  regHndl  : Hkey;
  regBuffer: Pchar;
  regBufferSize,
  valType  : DWord;
  retval   : LongInt;

begin

  // Get the name of the startup group
  retval := RegOpenKeyEx(HKEY_CURRENT_USER,startUpKey,
                         0,KEY_QUERY_VALUE,regHndl);
  if retval = ERROR_SUCCESS then begin
    regBufferSize := MAX_PATH;
    regBuffer := strAlloc(regBufferSize);
    retval := RegQueryValueEx(regHndl,'Startup',nil,
              @valType,Pbyte(regBuffer),@regBufferSize);
    RegCloseKey(regHndl);
    if retval = ERROR_SUCCESS then
      startupGroup := strPas(regBuffer)
    else
      startupGroup := 'STARTUP';
  end
  else
    startupGroup := 'STARTUP';

   if ( verinfo.dwPlatformId = VER_PLATFORM_WIN32_NT )then
      ServerStartup.items.add('Service');
   ServerStartup.Items.Add('Application');

end;  { CreateLabels }

(****************************************************************************
*                                                                           *
*                       C A N C E L  C L I C K                              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Closes the app and does nothing                                     *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.btnCancelClick(Sender: TObject);
begin
  DestroyBitmapArray;
  close;
end;

(****************************************************************************
*                                                                           *
*                       O K  C L I C K                                      *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Performs calls MakeRequestedChanges if the user changed any         *
*       information in the dialog                                           *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.btnOKClick(Sender: TObject);
var
  retval: DWord;

begin

  begin

    // make sure that the application path has a trailing \
    if not (AppPath.text[AppPath.GetTextLen] = '\') then
      AppPath.text := format('%s\',[AppPath.text]);

    // If the InterServer root directory has changed or the server priority has
    // changed, or the server startup has changed then set makeChanges to TRUE

    AppInfo.Close := TRUE;
    if ( arChanges[ctrlRootDir] ) or
       ( arChanges[ctrlSvrStartup] ) or
       ( arChanges[ctrlStartMode] ) then
    begin
        appInfo.makeChanges := TRUE;
        if not MakeInitRegEntries(AppInfo.rootDir,TRUE) then
          ShowError(errCantAddToRegistry,'MakeInitRegEntry');
    end else begin
      AppInfo.MakeChanges := FALSE;
    end;

    (* Get the startup mode for the server *)

    if verInfo.dwPlatformId = VER_PLATFORM_WIN32_NT then begin
      case serverStartup.ItemIndex of
        0 : if StartupMode.ItemIndex = 0 then
              appInfo.svrMode := SERVICE_MANUAL
            else
              appInfo.svrStartup := SERVICE_AUTO;
        1 : if StartupMode.ItemIndex = 0 then
              appInfo.svrMode := APPLICATION_MANUAL
            else
              appInfo.svrMode := APPLICATION_AUTO;
      end;
    end else
    begin
      if StartupMode.ItemIndex = 0 then
        appInfo.svrMode := APPLICATION_MANUAL
      else
        appInfo.svrMode := APPLICATION_AUTO;
    end;
  end;

  // if the app is not in an error state the allow it to close.  Otherwise
  // ensure that there are not going to be any changes in this error state.
  if AppInfo.Close then begin
    MakeRequestedChanges(appInfo);
    DestroyBitmapArray;
    close;
  end else
    appInfo.makeChanges := FALSE;
end;

(****************************************************************************
*                                                                           *
*                       R E M O V E  C L I C K                              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Removes the elements from the registry and the services db   *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.btnRemoveClick(Sender: TObject);
var
  retval : DWord;
  iconTitle,
  msgString: string;

begin
  msgString := format(LoadStr(errRemoveIB),[#13#10]);
  retval := MessageDlg(msgString,mtWarning,[mbYes,mbNo],0);
  if ( retval = mrYes ) then begin
    if verinfo.dwPlatformId = VER_PLATFORM_WIN32_NT then begin
      if GetServiceStatus(NT_SERVICE_NAME) = RUNNING then
        ShowError(errStopSvc,'GetserviceStatus')
      else begin
        if (verinfo.dwMajorVersion < 4) then
        begin
          CheckStartupGroup(iconTitle);
          RemoveServerFromStartup(iconTitle);
        end;
        DeleteServiceEntry;
        CleanupRegistry;
      end;
    end else
      CleanUpRegistry;
    close;
  end;
end;

(****************************************************************************
*                                                                           *
*                  A D D  S E R V E R  T O  S T A R T U P                   *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Adds the server to the startup group                                *
*                                                                           *
*****************************************************************************)
procedure AddServerToStartup(lpBinPath: String);
var
  errMsg,
  Macro: string;
  lpGrpInfo,Cmd: PChar;
  ClientAdd: TDdeClientConv;
  x: integer;
  done: bool;

begin
   { check the path for spaces.  if there are spaces, place quotes around
     the exe line }
  done := FALSE;
  x := 0;
  while not done do begin
   if lpBinPath[x] = ' ' then begin
     lpBinPath := format('"%s"',[lpBinPath]);
     done := TRUE;
   end;
   x := x+1
  end;
  errMsg := Format(LoadStr(errCantAddToStartup),[''])+#13#10;

  { Always add the server to the common group }
  Macro := Format('[ShowGroup(%s,1,1)]', [STARTUPGROUP]) + #13#10;
  Cmd := strAlloc(length(Macro)+1);
  StrPCopy (Cmd, Macro);

  (* check to see if the icon is already in the startup group.  If it is, then
     remove it before placing a new one there.  By removing the icon and replacing
     it with the new information, there is little chance of an invalid icon
     existing in the startup group.
  *)
  ClientAdd := TDdeClientConv.Create(nil);
  with ClientAdd do begin
    ConnectMode := ddeManual;
    ServiceApplication := GetNTShell;
    SetLink('PROGMAN','PROGMAN');
    OpenLink;

    { check to see if a startup group exists.  If it doesn't, create one }
    ExecuteMacro(Cmd,FALSE);
    lpGrpInfo := RequestData(STARTUPGROUP);

    { if lpGrpInfo is empty, then create a startup group }
    if (lpGrpInfo = nil) then begin
      strDispose(Cmd);
      Macro := Format('[CreateGroup(%s)]', [STARTUPGROUP]) + #13#10;
      Cmd := strAlloc(length(Macro)+1);
      StrPCopy (Cmd, Macro);
      ExecuteMacro(Cmd,FALSE);
      strDispose(cmd);
    end;

    Macro := Format('[ShowGroup(%s,1,1)]', [STARTUPGROUP]) + #13#10;
    Cmd := strAlloc(length(Macro)+1);
    StrPCopy (Cmd, Macro);
    if not ExecuteMacro(Cmd, False) then begin
      showError(errCantAddToStartup,'AddServerToStartup');
      strDispose(cmd);
    end else begin
      strDispose(cmd);
      Macro := Format('[AddItem(%s,%s)]', [lpBinPath,LoadStr(iconName)]) + #13#10;
      Cmd := strAlloc(length(Macro)+1);
      StrPCopy (Cmd, Macro);
      OpenLink;
      if not ExecuteMacro(Cmd, False) then
        showError(errCantAddToStartup,'AddServerToStartup');
      strDispose(cmd);
    end;
    CloseLink;
  end;
end; { AddServerToStartup }

(****************************************************************************
*                                                                           *
*         R E M O V E  S E R V E R  F R O M  S T A R T U P                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Removes the server from the startup group                           *
*                                                                           *
*****************************************************************************)
procedure RemoveServerFromStartup(iconTitle: string);
var
  errMsg,
  Macro: string;
  Cmd: PChar;
  ClientRemove : TDdeClientConv;
  groupFlag: integer;

begin
  errMsg := Format(LoadStr(errCantRemoveFromStartup),[''])+#13#10;

   { When group flag = 0, check the personal group
     When group flag = 1 check the common group }
  ClientRemove := TDdeClientConv.Create(nil);
  with ClientRemove do begin
    ConnectMode := ddeManual;
    SetLink('PROGMAN','PROGMAN');
    ServiceApplication := GetNTShell;
    OpenLink;
    for groupFlag := 0 to 1 do begin
      Macro := Format('[ShowGroup(%s,1,%d)]', [STARTUPGROUP,groupFlag]) + #13#10;
      Cmd := strAlloc(length(Macro)+1);
      StrPCopy (Cmd, Macro);
      if not ExecuteMacro(Cmd, False) then begin
          showError(errCantRemoveFromStartup,'RemoveServerFromStartup');
          strDispose(cmd);
      end else begin
        strDispose(cmd);
        Macro := Format('[DeleteItem(%s)]', [iconTitle]) + #13#10;
        Cmd := strAlloc(length(Macro)+1);
        StrPCopy (Cmd, Macro);
        if not ExecuteMacro(Cmd, False) then
          showError(errCantRemoveFromStartup,'RemoveServerFromStartup');
        strDispose(cmd);
      end;
    end;
    closeLink;
    Free;
  end;
end; { RemoveServerFromStartup }

(****************************************************************************
*                                                                           *
*              C H E C K  S T A R T U P  G R O U P                          *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Checks the startup group to see if the server is there              *
*                                                                           *
*****************************************************************************)
function CheckStartupGroup(var iconTitle: string): integer;
var
 ClientCheck: TDdeClientConv;
 //item: string;
 lpGrpInfo: PChar;
 groupFlag: integer;
 Macro: string;
 cmd: PChar;

(************************************************************)
function GetIconTitle(var lpGrpInfo: PChar): string;
var
  item: string;
  x,y: integer;
begin
   for x := 0 to length(lpGrpInfo) do begin
    // Get the information for the first half of the group and remove it
     if ( lpGrpInfo[x] = #13 ) and
        ( lpGrpInfo[x+1] = #10 ) then begin
       if ( pos(strPas(INTERSERVER_EXE_NAME),item ) > 0) then begin
        // I have found the icon associated with the server
        y := 1;
        // keep reading the string until reach a comma but do not
        // use quotes.  The entries are delimted by quotes, separated
        // by commas
        while not (item[y] = ',') do begin
          if not(item[y] = '"') then
            iconTitle := format('%s%s',[iconTitle,item[y]]);
          y := y + 1;
        end;
       end;
       item := '';
     end else
       item := format('%s%s',[item,lpGrpInfo[x]]);
   end;
   strDispose(lpGrpInfo);
   result := iconTitle;
end;
(************************************************************)

begin
  // see if the call is being made on NT 4.0.  If so, do
  // not check the startup group.
  if verinfo.dwMajorVersion >= 4 then begin
    result := APPLICATION_MANUAL;
    exit;
  end;
  ClientCheck := TDdeClientConv.Create(nil);
  with ClientCheck do begin
    ConnectMode := ddeManual;
    SetLink('PROGMAN','PROGMAN');
    ServiceApplication := GetNTShell;
    if OpenLink then begin
     // When groupFlag = 0 check the personal group
     // When groupFlag = 1 check the common group
     for GroupFlag := 0 to 1 do begin
       Macro := Format('[ShowGroup(%s,2,%d)]', [STARTUPGROUP,groupFlag]) + #13#10;
       Cmd := strAlloc(length(Macro)+1);
       StrPCopy (Cmd, Macro);
       ExecuteMacro(Cmd,FALSE);
       lpGrpInfo := RequestData(STARTUPGROUP);
       if ( pos(strPas(INTERSERVER_EXE_NAME),strPas(lpGrpInfo)) > 0) then begin
       // if the icon was found, just exit
         result := APPLICATION_AUTO;
         iconTitle := GetIconTitle(lpGrpInfo);
         exit;
       end
       else begin
         result := APPLICATION_MANUAL;
         iconTitle := '';
       end;
     end;
    end
    else begin
      ShowError(errCantQryStartup,'CheckStartupGroup');
      result := APPLICATION_MANUAL;
    end;
    CloseLink;
    Free;
  end;

end; { CheckStartupGroup }

(****************************************************************************
*                                                                           *
*            M A K E  R E Q U E S T E D  C H A N G E S                      *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Performs the changes requested by the user                          *
*                                                                           *
*****************************************************************************)
procedure MakeRequestedChanges(var AppInfo: ApplicationInfo);
var
  iconTitle,
  svrPath: string;
  lpBinPath: PChar;

begin
  // check to see if the server is located in the bin directory
  svrPath := AppInfo.rootDir;

 case verinfo.dwPlatformId of
   VER_PLATFORM_WIN32_NT:
   begin
      case AppInfo.svrMode of
        SERVICE_AUTO : { Service - Auto }
          begin
            if verinfo.dwMajorVersion < 4 then begin
              CheckStartupGroup(iconTitle);
              RemoveServerFromStartup(iconTitle);
            end else
              RemoveServerEntry;  // RemoveServer from registry          
            lpBinPath := strAlloc(Length(svrPath)+5);
            StrPCopy(lpBinPath,format('%s -s',[svrPath]));
            if not InstallService(SERVICE_AUTO_START,AppInfo.svcStartup,lpBinPath) then
              showError(errCantInstallService,'CreateService');
            strDispose(lpBinPath);
          end;
        SERVICE_MANUAL : { Service - Manual }
          begin
            if verinfo.dwMajorVersion < 4 then begin
              CheckStartupGroup(iconTitle);
              RemoveServerFromStartup(iconTitle);
            end else
              RemoveServerEntry;  // RemoveServer from registry          
            lpBinPath := strAlloc(Length(svrPath)+5);
            StrPCopy(lpBinPath,format('%s -s',[svrPath]));
            if not InstallService(SERVICE_DEMAND_START,AppInfo.svcStartup,lpBinPath) then
              showError(errCantInstallService,'CreateService');
            strDispose(lpBinPath);
          end;

        APPLICATION_AUTO : { Icon - Auto }
          begin
            if (GetServiceStatus(NT_SERVICE_NAME) = RUNNING) then
               ShowError(errStopSvc,'RemoveService')
            else begin
               if deleteServiceEntry then begin
                 if verinfo.dwMajorVersion < 4 then begin
                   CheckStartupGroup(iconTitle);
                   RemoveServerFromStartup(iconTitle);
                   AddServerToStartup(svrPath)
                 end else
                   MakeRegistryEntry('Software\Microsoft\Windows\CurrentVersion\Run',
                    'InterServer',svrPath);
               end;
              end;
          end;

        APPLICATION_MANUAL : { Icon - Demand }
          begin
            if (GetServiceStatus(NT_SERVICE_NAME) = RUNNING) then
               ShowError(errStopSvc,'RemoveService')
            else begin
              if deleteServiceEntry then begin
                if verinfo.dwMajorVersion < 4 then begin
                  CheckStartupGroup(iconTitle);
                  RemoveServerFromStartup(iconTitle);
                end else
                  RemoveServerEntry;
              end;
            end;
          end;
      end; { case }
   end;

   VER_PLATFORM_WIN32_WINDOWS:
     begin
       if appInfo.svrMode = APPLICATION_AUTO then
         MakeRegistryEntry('Software\Microsoft\Windows\CurrentVersion\Run',
                           'InterServer',svrPath)
       else
         RemoveServerEntry;
     end;
 end; { Case }
end;

(****************************************************************************
*                                                                           *
*                    I S  S E R V E R  R U N N I N G                        *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Checks to see if the server is running                              *
*                                                                           *
*****************************************************************************)
function IsServerRunning: BOOL;
var
  CLSID,
  WNDNAME: Pchar;

begin
  CLSID := StrAlloc(Length('InterServer')+1);
  strPCopy(CLSID,'InterServer');
  WNDNAME := StrAlloc(Length('InterServer')+1);
  strPCopy(WNDNAME,'InterServer');

  if FindWindow(CLSID,WNDNAME) = 0 then
    result := FALSE
  else
    result := TRUE;

  strDispose(CLSID);
  strDispose(WNDNAME);
end;

(****************************************************************************
*                                                                           *
*                    S E R V E R  S T A R T U P  C L I C K                  *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Disables the services information when the user clicks to use icons *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.ServerStartupClick(Sender: TObject);
begin
  arChanges[ctrlSvrStartup] := TRUE;
end;

(****************************************************************************
*                                                                           *
*                    G E T  S T A R T  S T A T U S                          *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Checks the startup information for the server                       *
*                                                                           *
*****************************************************************************)
function GetStartStatus: integer;
var
  hManager,
  hService: SC_HANDLE;
  lpqscServConfig: LPQUERY_SERVICE_CONFIG;
  retval,
  cbBuffSize: Dword;
  iconTitle: String;

  regHndl  : Hkey;
  regBuffer: Pchar;
  regBufferSize,
  valType : DWord;
  apType : integer;

begin
  case verInfo.dwPlatformId of
    VER_PLATFORM_WIN32_NT:
      begin
        {
           Check to see if we are on NT 4.0 and look for the registry key.
           If the registry key exists, then we don't need to do anything else.
        }
        if (verinfo.dwMajorVersion >= 4) then begin
          retval := RegOpenKeyEx(HKEY_LOCAL_MACHINE,STARTUP_ROOT_95,
                                 0,KEY_QUERY_VALUE,regHndl);
          if ( retval = ERROR_SUCCESS ) then begin
            regBufferSize := 100;
            regBuffer := strAlloc(regBufferSize);
            retval := RegQueryValueEx(regHndl,SERVER_NAME,nil,
                                      @valType,Pbyte(regBuffer), @regBufferSize);
            strDispose(regBuffer);
            RegCloseKey(regHndl);

            if ( retval = ERROR_SUCCESS ) then
              apType := APPLICATION_AUTO
            else
              apType := APPLICATION_MANUAL;
          end else
              apType := APPLICATION_MANUAL;
        end else begin
          { if we are not on NT 4.0, then check the startup groups for the
            server. }
          apType := CheckStartupGroup(iconTitle);
        end;

        { if we still haven't found the server, then check the services
          manager for an entry.  If after this, the server still is not found,
          then assume that the server is started manually by the user. }

        if not ( apType = APPLICATION_AUTO ) then begin
          hManager := OpenServicesManager(SC_MANAGER_ALL_ACCESS);
          if ( hManager = 0 ) then begin
             { 0 means that the services database could not be opened }
             MessageDlg(LoadStr(errMustBeAdmin),mtInformation,[mbOK],0);
             apType := CLOSE_APP;
          end
          else begin
            hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_QUERY_CONFIG);
            if not ( hService = 0 ) then begin
              cbBuffsize := 282;
              lpqscServConfig := memAlloc(cbBuffSize);
              if QueryServiceConfigA(hService,
                                     lpqscServConfig,
                                     cbBuffSize,
                                     @cbBuffSize) then begin
                retval := lpqscServConfig.dwStartType;
                dispose(lpqscServConfig);
                case retval of
                  SERVICE_AUTO_START : apType := SERVICE_AUTO;
                  SERVICE_DEMAND_START: apType := SERVICE_MANUAL;
                end; { case }
              end;
            end else
              { Could not retrieve the information from the services manager.
                so, the server is started as an application manually }
              apType := APPLICATION_MANUAL;
          end;
        end;
      end;

    VER_PLATFORM_WIN32_WINDOWS:
      begin
        retval := RegOpenKeyEx(HKEY_LOCAL_MACHINE,STARTUP_ROOT_95,
                               0,KEY_QUERY_VALUE,regHndl);
        if ( retval = ERROR_SUCCESS ) then begin
          regBufferSize := 100;
          regBuffer := strAlloc(regBufferSize);
          retval := RegQueryValueEx(regHndl,SERVER_NAME,nil,
                                    @valType,Pbyte(regBuffer),@regBufferSize);
          strDispose(regBuffer);
          RegCloseKey(regHndl);
          if ( retval = ERROR_SUCCESS ) then
            apType := APPLICATION_AUTO
          else
            apType := APPLICATION_MANUAL;
        end else
            apType := APPLICATION_MANUAL;
        RegCloseKey(regHndl);
      end;
    end; { case }
    
  result := apType;
end;


(****************************************************************************
*                                                                           *
*                    I N S T A L L  S E R V I C E                           *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Install the service as specified                                    *
*                                                                           *
*****************************************************************************)
function InstallService(svcStartType: DWORD;
         svcStartMode: short; lpBinPath: PChar): BOOL;
var
  retval: DWORD;
  iconTitle: string;
  hManager,
  hService: SC_HANDLE;

begin
  if ( CheckStartupGroup(iconTitle) = 0 ) then
    RemoveServerFromStartup(iconTitle);

  hManager := OpenServicesManager(SC_MANAGER_CREATE_SERVICE);
  hService := CreateService(hManager,NT_SERVICE_NAME,SERVER_NAME,
             lpBinPath,svcStartType);
  retval := GetLastError();
  if (retval = ERROR_SERVICE_EXISTS) or
     (retval = ERROR_DUP_NAME) then begin
       ChangeServiceStartup(svcStartType);
       result := TRUE;
  end else begin
    if not (retval = ERROR_SUCCESS) then
      result := FALSE
    else
      result := TRUE;
  end;
  CloseServiceHandle(hService);
  CloseServicesManager(hManager);
end;

(****************************************************************************
*                                                                           *
*                   I S N T A D M I N I S T R A T O R                       *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Determines if the user currently logged in is an NT administrator   *
*                                                                           *
*****************************************************************************)
function IsNTAdministrator: BOOL;
var
  hManager: SC_HANDLE;

begin
  hManager := OpenServicesManager(SC_MANAGER_ALL_ACCESS);
  if hManager = 0 then
    result := FALSE
  else
    result := TRUE;
end;

(****************************************************************************
*                                                                           *
*                        G E T  N T  S H E L L                              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Returns a string containing the current NT shell being used         *
*                                                                           *
*****************************************************************************)
function GetNTShell: string;
const
  shellKey: PChar = 'Software\Microsoft\Windows NT\CurrentVersion\Winlogon';

var
  regHndl  : Hkey;
  regBuffer: Pchar;
  regBufferSize,
  valType  : DWord;
  retval   : LongInt;
  CheckShell: TDdeClientConv;

begin
  retval := RegOpenKeyEx(HKEY_LOCAL_MACHINE,ShellKey,
                         0,KEY_QUERY_VALUE,regHndl);
  if retval = ERROR_SUCCESS then begin
    regBufferSize := MAX_PATH;
    regBuffer := strAlloc(regBufferSize);
    retval := RegQueryValueEx(regHndl,'Shell',nil,
              @valType,Pbyte(regBuffer),@regBufferSize);
    if retval = ERROR_SUCCESS then begin
      checkShell := TDdeClientConv.Create(nil);
      with checkShell do begin
       ConnectMode := ddeManual;
       SetLink('PROGMAN','PROGMAN');
       ServiceApplication := strPas(regBuffer);
       if OpenLink then
         result := strPas(regBuffer)
       else
         result := 'PROGMAN.EXE';
      end;
      CheckShell.Free;
    end else begin
      result := 'PROGMAN.EXE';
    end;
    strDispose(regBuffer);
    RegCloseKey(regHndl);
  end else begin
    result := 'PROGMAN.EXE';
  end;
end;

procedure GetServerVersion(var lpServerVersion: pointer);

var
  lpFileName: PChar;
  temp: Dword;
  versionSize: Dword;
  lpBuffer: Pointer;
  bufLen: UINT;

begin
    // Read the version string from the DLL to place in the registry
    lpFileName := strAlloc(MAX_PATH+255);
    strPCopy(lpFileName,'isconfig.exe');
    versionSize := GetFileVersionInfoSize(lpFileName,temp);
    GetMem(lpBuffer,versionSize);
    GetFileVersionInfo(lpFileName,temp,versionSize,lpBuffer);
    VerQueryValue(lpBuffer,'\StringFileInfo\040904E4\FileVersion',lpServerVersion,bufLen);
    FreeMem(lpBuffer,versionSize);
    strDispose(lpfileName);
end;

procedure TdlgMain.greenLightClick(Sender: TObject);
var
  retval: short;
  hManager,
  hService: SC_HANDLE;
  lpsServiceStatus: LPSERVICE_STATUS;  

begin
  if (CompareStr(ServerStartup.Items[ServerStartup.ItemIndex], 'Service') = 0) then begin
    if not appInfo.bGreenClicked then begin
      try
        Screen.cursor := crHourGlass;
        hManager := OpenServicesManager(SC_MANAGER_CONNECT);
        retval := GetServiceStatus(NT_SERVICE_NAME);
        case retval of
          PAUSED:
            begin
              hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_PAUSE_CONTINUE);
              if hService = 0 then
                ShowError(errCantOpenSvcDB,'OpenServiceManager')
              else begin
                lpsServiceStatus := memAlloc(200);
                if not ControlService(hService,SERVICE_CONTROL_CONTINUE,lpsServiceStatus) then
                 ShowError(errCantStartSvc,'ServiceNotStarted')
                else begin
                  btnRemove.enabled := FALSE;
                  btnBrowse.enabled := FALSE;
                  appPath.enabled := FALSE;
                end;
                Dispose(lpsServiceStatus);
              end;
            end;

          STOPPED:
            begin
              hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_START);
              if hManager = 0 then
                ShowError(errCantOpenSvcDB,'OpenServiceManager')
              else
               if not StartService(hService,0,nil) then begin
                 if not GetLastError() = ERROR_SERVICE_ALREADY_RUNNING then
                   ShowError(errCantStartSvc,'ServiceNotStarted');
               end else
                 btnRemove.enabled := FALSE;
                 btnBrowse.enabled := FALSE;
                 appPath.enabled := FALSE;
             end;
        end;
        CloseServicesManager(hManager);
        CloseServiceHandle(hService);
        SetServiceControl(GetServiceStatus(NT_SERVICE_NAME));
      finally
        Screen.cursor := crDefault;
      end;
    end;
  end;
end;

procedure TdlgMain.yellowLightClick(Sender: TObject);
var
  hManager,
  hService: SC_HANDLE;
  lpsServiceStatus: LPSERVICE_STATUS;

begin
  if (CompareStr(ServerStartup.Items[ServerStartup.ItemIndex], 'Service') = 0) then 
  begin
    if not appInfo.bYellowClicked then 
    begin
      try
	Screen.cursor := crHourGlass;
        hManager := OpenServicesManager(SC_MANAGER_CONNECT);
        hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_PAUSE_CONTINUE);
        if hService = 0 then
          ShowError(errCantOpenSvcDB,'OpenServiceManager')
        else begin
          lpsServiceStatus := memAlloc(200);
          if not ControlService(hService,SERVICE_CONTROL_PAUSE,lpsServiceStatus) then
           ShowError(errCantPauseSvc,'ServiceNotPaused')
          else
          begin
            Dispose(lpsServiceStatus);
            CloseServiceHandle(hService);
            CloseServicesManager(hManager);
  	    SetServiceControl(GetServiceStatus(NT_SERVICE_NAME));
          end;
        end;
      finally
        Screen.cursor := crDefault;
      end;
    end;
  end;
end;

procedure TdlgMain.redLightClick(Sender: TObject);
var
  hManager,
  hService: SC_HANDLE;
  lpsServiceStatus: LPSERVICE_STATUS;

begin
  if (CompareStr(ServerStartup.Items[ServerStartup.ItemIndex], 'Service') = 0) then begin
    if not appInfo.bRedClicked then begin
      Screen.cursor := crHourGlass;
      try
        hManager := OpenServicesManager(SC_MANAGER_CONNECT);
        hService := OpenService(hManager,NT_SERVICE_NAME,SERVICE_STOP);
        if hService = 0 then
          ShowError(errCantOpenSvcDB,'OpenServiceManager')
        else begin
          lpsServiceStatus := memAlloc(200);
          if not ControlService(hService,SERVICE_CONTROL_STOP,lpsServiceStatus) then
           ShowError(errCantStopSvc,'ServiceNotStopped')
          else begin
            Dispose(lpsServiceStatus);
            CloseServiceHandle(hService);
            CloseServicesManager(hManager);
  	    SetServiceControl(GetServiceStatus(NT_SERVICE_NAME));
            btnRemove.enabled := TRUE;
            btnBrowse.enabled := TRUE;
            appPath.enabled := TRUE;
          end;
        end;
      finally
        Screen.cursor := crDefault;
      end;
    end;
  end;
end;


procedure TdlgMain.SetServiceControl(svcStatus: integer);
begin
    case svcStatus of
      RUNNING :
        begin

(*        greenList.GetBitmap(0, greenLight.Picture.Bitmap);
          yellowList.GetBitmap(1, yellowLight.Picture.Bitmap);
          redList.GetBitmap(1, redLight.Picture.Bitmap);
*)
          greenLight.Picture.Bitmap := greenlist[0];
          yellowLight.Picture.Bitmap := yellowlist[1];
          redLight.Picture.Bitmap := redlist[1];

	  appInfo.bYellowClicked := FALSE;
	  appInfo.bRedClicked := FALSE;
	  appInfo.bGreenClicked := TRUE;

	  txtStart.caption := 'Started';
	  txtStop.caption := 'Stop';
 	  txtPause.caption := 'Pause';
	  txtStatus.Caption := 'Running';
          btnBrowse.enabled := FALSE;
          appPath.enabled := FALSE;
          btnRemove.enabled := FALSE;
          txtStart.Font.Style := [fsBold];
    	  txtStop.Font.Style := txtStart.Font.Style - [fsBold];
    	  txtPause.Font.Style := txtPause.Font.Style - [fsBold];
        end;

      STOPPED :
        begin
(*
          greenList.GetBitmap(1, greenLight.Picture.Bitmap);
          yellowList.GetBitmap(1, yellowLight.Picture.Bitmap);
          redList.GetBitmap(0, redLight.Picture.Bitmap);
*)
          greenLight.Picture.Bitmap := greenlist[1];
          yellowLight.Picture.Bitmap := yellowlist[1];
          redLight.Picture.Bitmap := redlist[0];

	  appInfo.bYellowClicked := FALSE;
	  appInfo.bRedClicked := TRUE;
	  appInfo.bGreenClicked := FALSE;

	  txtStart.caption := 'Start';
	  txtStop.caption := 'Stopped';
 	  txtPause.caption := 'Pause';
	  txtStatus.Caption := 'Stopped';
          btnBrowse.enabled := TRUE;
          appPath.enabled := TRUE;
          btnRemove.enabled := TRUE;
          txtStop.Font.Style := [fsBold];
    	  txtStart.Font.Style := txtStart.Font.Style - [fsBold];
    	  txtPause.Font.Style := txtPause.Font.Style - [fsBold];
        end;

      PAUSED  :
        begin
(*
          greenList.GetBitmap(1, greenLight.Picture.Bitmap);
          yellowList.GetBitmap(0, yellowLight.Picture.Bitmap);
          redList.GetBitmap(1, redLight.Picture.Bitmap);
*)
          greenLight.Picture.Bitmap := greenlist[1];
          yellowLight.Picture.Bitmap := yellowlist[0];
          redLight.Picture.Bitmap := redlist[1];

	  appInfo.bYellowClicked := TRUE;
	  appInfo.bRedClicked := FALSE;
	  appInfo.bGreenClicked := FALSE;

	  txtStart.caption := 'Continue';
	  txtStop.caption := 'Stop';
 	  txtPause.caption := 'Paused';
	  txtStatus.Caption := 'Paused';
          btnBrowse.enabled := FALSE;
          appPath.enabled := FALSE;
          btnRemove.enabled := FALSE;
          txtPause.Font.Style := [fsBold];
	  txtStart.Font.Style := txtStart.Font.Style - [fsBold];
	  txtStop.Font.Style := txtPause.Font.Style - [fsBold];
        end;
    end;
    redlight.refresh;
    yellowlight.refresh;
    greenlight.refresh;
end;

procedure TdlgMain.DisableServiceControl(bDisable: boolean);
begin
  if bDisable then begin
    txtStop.enabled := FALSE;
    txtStart.enabled := FALSE;
    txtPause.enabled := FALSE;

(*
    greenList.GetBitmap(2, greenLight.Picture.Bitmap);
    yellowList.GetBitmap(2, yellowLight.Picture.Bitmap);
    redList.GetBitmap(2, redLight.Picture.Bitmap);
*)
    greenLight.Picture.Bitmap := greenlist[2];
    yellowLight.Picture.Bitmap := yellowlist[2];
    redLight.Picture.Bitmap := redlist[2];

  end else
  begin
    txtStop.enabled := TRUE;
    txtStart.enabled := TRUE;
    txtPause.enabled := TRUE;
(*
    greenList.GetBitmap(1, greenLight.Picture.Bitmap);
    yellowList.GetBitmap(1, yellowLight.Picture.Bitmap);
    redList.GetBitmap(0, redLight.Picture.Bitmap);
*)
    greenLight.Picture.Bitmap := greenlist[1];
    yellowLight.Picture.Bitmap := yellowlist[1];
    redLight.Picture.Bitmap := redlist[0];
  end;
  redlight.refresh;
  yellowlight.refresh;
  greenlight.refresh;
end;

procedure TdlgMain.AppPathChange(Sender: TObject);
begin
  arChanges[ctrlRootDir] := TRUE;
end;

procedure TdlgMain.StartupModeChange(Sender: TObject);
begin
  arChanges[ctrlStartMode] := TRUE;
end;

procedure TdlgMain.rbNormalClick(Sender: TObject);
begin
  appInfo.svcStartup := 0;
end;

procedure TdlgMain.rbHighClick(Sender: TObject);
begin
  appInfo.svcStartup := 1;
end;

procedure TdlgMain.SetupDialog(Sender: TObject);
var
  lpSvrVersion: pointer;
  IBInit: StartupInfo;

begin
  verInfo.dwOSVersionInfoSize := sizeOf(verInfo);
  GetVersionEx(verInfo);
  GetServerVersion(lpSvrVersion);
  AppInfo.svrVersion := StrAlloc(strLen(lpSvrVersion)+1);
  strcopy(AppInfo.svrVersion, lpSvrVersion);
  try
    with dlgMain do begin
      if ( verinfo.dwPlatformId = VER_PLATFORM_WIN32_NT )then begin
        txtOS.caption := 'Microsoft Windows NT';
        txtServices.caption := 'InterServer can run as a service.';
      end else
      begin
        txtOS.caption := 'Microsoft Windows 95';
        txtServices.caption := 'InterServer can not run as a service.';
      end;

      pageControl1.ActivePage := GeneralPage;
      txtVersion.Caption := AppInfo.svrVersion;
      CreateLabels(AppFlag);
      GetStartupInfo(ibInit,verInfo.dwPlatformId);
      if ibInit.status then begin
        AppPath.Text := ibInit.ibDirectory;
        if IsServerRunning then begin
          txtStatus.Caption := 'Running';
          btnRemove.enabled := FALSE;
          btnBrowse.enabled := FALSE;
          appPath.enabled := FALSE;
        end
        else begin
          txtStatus.Caption := 'Stopped';
          btnRemove.enabled := TRUE;
          btnBrowse.enabled := TRUE;
          appPath.enabled := TRUE;
        end;
        { get the server startup information }
        // fill the stop light array
        FillBitmapArray;
        case ( GetStartStatus ) of
          SERVICE_AUTO : {Service: Auto}
            begin
              DisableServiceControl(FALSE);
              SetServiceControl(GetServiceStatus(NT_SERVICE_NAME));
              ServerStartup.ItemIndex := 0;
              StartupMode.ItemIndex := 1;
            end;

          SERVICE_MANUAL : {Service: Manual}
            begin
              DisableServiceControl(FALSE);
              SetServiceControl(GetServiceStatus(NT_SERVICE_NAME));
              ServerStartup.ItemIndex := 0;
              StartupMode.ItemIndex := 0;
            end;
          APPLICATION_AUTO : {Icon: Startup}
            begin
              DisableServiceControl(TRUE);
              ServerStartup.ItemIndex := ServerStartup.Items.Count - 1;
              StartupMode.ItemIndex := 1;
            end;
          APPLICATION_MANUAL : {Manual}
            begin
              DisableServiceControl(TRUE);
              ServerStartup.ItemIndex := ServerStartup.Items.Count - 1;
              StartupMode.ItemIndex := 0;
            end;
          CLOSE_APP:
            begin
              Free;
            end;
        end;

      end else begin
         MessageDlg(format(LoadStr(errUsingDefault),[#13#10]),mtInformation,[mbOK],0);
         ServerStartup.itemIndex := 0;
         StartupMode.ItemIndex := 1;
         txtStatus.Caption := 'Server not Configured';
         DisableServiceControl(TRUE);
       end;
      { reset the tag values for all the controls }
       serverStartup.tag := appRunning;
       startupmode.tag := appRunning;
    end; // with
  except on E: Exception do
    if not (IbInit.StartupMode = CLOSE_APP) then
      ShowError(errLaunchingMain,'LaunchInstReg');
  end;

end;

(****************************************************************************
*                                                                           *
*                        B R O W S E C L I C K                              *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*       Launches the browser and adds a \ to the end of the path            *
*                                                                           *
*****************************************************************************)
procedure TdlgMain.btnBrowseClick(Sender: TObject);
var
  strDirChosen,
  startdir: string;
  winDir: Pchar;

begin
 if AppPath.GetTextLen = 0 then
    GetDir(0,startDir)
  else begin
    if DirectoryExists(AppPath.text) then
          startDir := format('%s',[AppPath.text])
    else begin
      winDir := strAlloc(MAX_PATH);
      GetWindowsDirectory(winDir,MAX_PATH);
      startDir := strPas(winDir);
      strDispose(winDir);
    end;

  end;
  with OpenDialog do begin
    Title := LoadStr(BrowseDlgTitleLicense);
    InitialDir := startDir;
    FileName := LoadStr(feLicenseExt);
    DefaultExt := LoadStr(feLicenseExt);
    Filter := LoadStr(feLicenseFile);
    if Execute then begin
      strDirChosen := ExtractFilePath(FileName);
      AppPath.text := strDirChosen;
    end;
  end;
end;

(****************************************************************************
*                                                                           *
*                M A K E  I N I T  R E G  E N T R I E S                     *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*     Makes the initial registry entries for 95 and NT                      *
*                                                                           *
*****************************************************************************)
function TdlgMain.MakeInitRegEntries(var svrPath: string; closeApp: boolean): Bool;
var
  tmp,
  startDir: string;
  stdir : Pointer;

(**********************************************************)
procedure GetServerPath(var svrPath: string);
begin
 
 if not ( FileExists(format('%sbin\interserver.exe',[AppPath.Text]))) and
     not ( FileExists(format('%sinterserver.exe',[AppPath.Text]))) then
    begin
      tmp := format('%sbin',[AppPath.text]);
      messageDlg(format(LoadStr(errServerNotFound),[tmp]),mtInformation,[mbOK],0);

      { check to see if the directory that the user typed is valid.  If
        so, then use it, otherwise default to the system dir }

      if DirectoryExists(AppPath.text) then
         startDir := AppPath.text
      else begin
        stDir := StrAlloc(MAX_PATH+1);
        GetSystemDirectory(stDir,MAX_PATH);
        startDir := StrPas(stDir);
        strDispose(stDir);
      end; {if dir exists}

      // show a browser for the server
      with OpenDialog do begin
        Title := LoadStr(BrowseDlgTitleServer);
        InitialDir := startDir;
        FileName := LoadStr(feServerExt);
        DefaultExt := LoadStr(feserverExt);
        Filter := LoadStr(feServerFile);
        if Execute then
          svrPath := FileName
        else begin
          showError(errInvalidServer,'MakeInitEntries');
          ServerStartup.ItemIndex := ServerStartup.Items.Count;
          svrPath := '';
        end;
      end; {with}
    end else
      begin { not file }
        if fileExists(format('%sbin\interserver.exe',[AppPath.Text])) then
          svrPath := format('%sbin\interserver.exe',[AppPath.text])
        else
          svrPath := format('%sinterserver.exe',[AppPath.text]);
      end;
end;
(**********************************************************)

begin
  if Length(AppPath.text) = 0 then begin
    ShowError(errInvalidDirectory,'MakeRegEntries');
    result := FALSE;
    AppInfo.Close := FALSE;
  end
  else
  begin
    AppInfo.Close := closeApp;

    (* check for the server only if the user is starting the server automatically
     * If the server is being started manually,
     * then ServerStartup.ItemIndex = ServerStartup.Items.Count-1
     *)

    if (StartupMode.ItemIndex = 1) then
       GetServerPath(svrPath)
    else
      if ((ServerStartup.Items.Count = 2) and
          (ServerStartup.ItemIndex = 0)) then
       GetServerPath(svrPath);

    if not ( MakeRegistryEntry('Software\InterBase Corp\InterClient\CurrentVersion',
                               'RootDirectory',
                               AppPath.text)  and
             MakeRegistryEntry('Software\InterBase Corp\InterClient\CurrentVersion',
                               'Version',
                               AppInfo.svrVersion) ) then
      MessageDlg(LoadStr(errCantAddToRegistry),mtError,[mbOK],0);
    result := TRUE;
  end;
end;

end.
