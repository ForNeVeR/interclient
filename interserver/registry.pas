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
 *	MODULE:		registry.pas
 *	DESCRIPTION:	Windows registry wrapper functions
 *
 * copyright (c) 1996 by Borland International
 *}

unit registry;

interface

uses SysUtils, Windows, Messages, Classes, Dialogs, Controls;
type
 StartupInfo = record
   ibDirectory: String;
   StartupMode: short;
   ServicePriority: short;
   status: bool;
   serviceStartup: short;
 end;

const
// Service and Registry String Constants
  BORLAND_REG_ROOT: Pchar = 'Software\InterBase Corp';
  SERVER_NAME: PChar = 'InterServer';
  INTERBASE_KEY : PChar = 'Software\InterBase Corp\InterClient\CurrentVersion';
  REG_ROOT_DIR : PChar = 'RootDirectory';
  STARTUP_ROOT_95 : PChar = 'Software\Microsoft\Windows\CurrentVersion\Run';
  SERVICES_ROOT: PChar = 'System\ControlSet001\Services\InterServer';
  INTERSERVER_EXE_NAME : PChar = 'interserver.exe';
  INTERSERVER_SERVICE_EXE_NAME: String= 'interserver.exe -s';

  APPLICATION_AUTO = 0;
  APPLICATION_MANUAL = 1;

//functions and procedures used
  procedure RemoveServerEntry;
  function  MakeRegistryEntry(lpNode, lpKey: PChar; strValue: String): bool;
  procedure GetStartupInfo(var IbInfo: StartupInfo; osVersion: Dword);
  procedure CleanUpRegistry;

implementation

(****************************************************************************
*                                                                           *
*                       G E T  S T A R T U P  I N F O                       *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Queries the registry information to find out where InterBase was       *
*    installed to.                                                          *
*                                                                           *
*                                                                           *
*****************************************************************************)
procedure GetStartupInfo(var IbInfo: StartupInfo; osVersion: Dword);
var
  regHndl  : Hkey;
  regBuffer: Pchar;
  regBufferSize,
  valType  : DWord;
  retval   : LongInt;

begin
  ibinfo.status := TRUE;
  retval := RegOpenKeyEx(HKEY_LOCAL_MACHINE,INTERBASE_KEY,
                         0,KEY_QUERY_VALUE,regHndl);
  if retval = ERROR_SUCCESS then
  begin
    regBufferSize := MAX_PATH;
    regBuffer := strAlloc(regBufferSize);
    retval := RegQueryValueEx(regHndl,'RootDirectory',nil,
              @valType,Pbyte(regBuffer),@regBufferSize);

    if not ( retval = ERROR_SUCCESS ) then
       IbInfo.status := FALSE
    else
      IbInfo.ibDirectory := strPas(regBuffer);
    strDispose(regBuffer);
    RegCloseKey(regHndl);    
  end else begin
    Ibinfo.status := FALSE;
  end;
end; { GetStartupInfo }

(****************************************************************************
*                                                                           *
*                  M A K E  R E G I S T R Y  E N T R Y                      *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Adds an entry to the registry                                          *
*                                                                           *
*                                                                           *
*****************************************************************************)
function MakeRegistryEntry(lpNode, lpKey: PChar; strValue: string): bool;
var
  retval   : longint;
  regHndl  : HKey;
  security : REGSAM;
  KeyMade  : Dword;

begin
  security := KEY_ALL_ACCESS;
  retval := RegOpenKeyEx(HKEY_LOCAL_MACHINE,lpNode,0,security,regHndl);
  if not (retval = ERROR_SUCCESS) then
    retval := RegCreateKeyEx(HKEY_LOCAL_MACHINE,lpNode,0,'',
              REG_OPTION_NON_VOLATILE,KEY_WRITE,nil,regHndl,@KeyMade);
  if (retval = ERROR_SUCCESS) or (KeyMade = REG_CREATED_NEW_KEY) then
    begin
     retval := RegSetValueEx(regHndl,lpKey,0,REG_SZ,PChar(strValue),length(strValue)+1);
     if (retval = ERROR_SUCCESS) then
       result := TRUE
     else
       result := FALSE;
    end
  else
    result := FALSE;
  RegCloseKey(regHndl);
end; { MakeRegistryEntry }

(****************************************************************************
*                                                                           *
*                   R E M O V E  S E R V E R  E N T R Y                     *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Removes the server registry entry under Win95                          *
*      Key = Software\Microsoft\Windows\CurrentVersion\Run                  *
*                                                                           *
*                                                                           *
*****************************************************************************)
Procedure RemoveServerEntry;
var
  regHndl : Hkey;

begin
  RegOpenKeyEx(HKEY_LOCAL_MACHINE,STARTUP_ROOT_95,0,KEY_ALL_ACCESS,regHndl);
  RegDeleteValue(regHndl,SERVER_NAME);
  RegCloseKey(regHndl);
end; { RemoveServerEntry }

(****************************************************************************
*                                                                           *
*                       C L E A N  U P  R E G I S T R Y                     *
*****************************************************************************
*                                                                           *
*  Functional Description:                                                  *
*    Removes all of the InterBase registry information                      *
*                                                                           *
*                                                                           *
*****************************************************************************)
procedure CleanUpRegistry;
var
  regHndl : Hkey;

begin
  RegOpenKeyEx(HKEY_LOCAL_MACHINE,INTERBASE_KEY,0,KEY_SET_VALUE,regHndl);
  RegDeleteValue(regHndl,'RootDirectory');
  RegDeleteValue(regHndl,'Version');
  RegCloseKey(regHndl);
  RegOpenKeyEx(HKEY_LOCAL_MACHINE,'Software\InterBase Corp\InterServer',0,KEY_SET_VALUE,regHndl);
  RegDeleteKey(regHndl,'CurrentVersion');
  RegCloseKey(regHndl);
  RegOpenKeyEx(HKEY_LOCAL_MACHINE,'Software\InterBase Corp',0,KEY_SET_VALUE,regHndl);
  RegDeleteKey(regHndl,'InterServer');
  RegOpenKeyEx(HKEY_LOCAL_MACHINE,STARTUP_ROOT_95,0,KEY_SET_VALUE,regHndl);
  RegDeleteValue(regHndl,SERVER_NAME);
  RegCloseKey(regHndl);
end;  { CleanUpRegistry }

end.
