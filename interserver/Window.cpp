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
#include <windows.h>
#include <commctrl.h>
#include <shellapi.h>
#include <prsht.h>
#include <dbt.h>

#define BOOLEAN_DEFINED
#define MSG_DEFINED
#define ISCONFIG "isconfig.exe"
#define ISCONFIG_ENTRYPOINT  "LaunchISConfig"

#include "Property.rh"
#include "Property.h"

#include "Window.h"
#include "VersionInformation.h"

static  const char *const szClassName       = VersionInformation::APPLICATION_NAME__;
char szWindowText[WIN_TEXTLEN];
HWND hPSDlg = NULL;
static HINSTANCE hInstance = NULL;
static USHORT usServerFlags;


static HANDLE SRVR_main_init();
DWORD SRVR_main_thread (LPDWORD param);
extern int SRVR_main(int argc, HANDLE);
static void LaunchISConfig(HWND);

// Window Procedure
void __stdcall WINDOW_shutdown(HANDLE);
LRESULT CALLBACK WindowFunc(HWND, UINT, WPARAM, LPARAM);

/* 
   Input:  hThisInst - Handle to the current instance
           nWndMode - The mode to be used in the call ShowWindow()
           usServerFlagMask - The Server Mask specifying various server flags
   Return: (int) 0 if returning before entering message loop
           wParam if returning after WM_QUIT
   Description: This function registers the main window class, creates the
                window and it also contains the message loop. This func. is a
                substitute for the regular WinMain() function.
*/
int WIN_SRVR_main ( HINSTANCE hThisInst, int nWndMode, USHORT usServerFlagMask)
{
HWND hWnd = NULL;
MSG	msg;
WNDCLASS wcl;
HANDLE serverThread;

hInstance = hThisInst;
usServerFlags = usServerFlagMask;

InitCommonControls ();

// The following function will setup SRVR_main to run in
// it's own thread 
// The SRVR_main will do wait on connection
// and fork on receiving a connection request
// and go back to wait again
// The window property sheet and shutdown stuff will
// be handled in the main thread flow (as in this module)
// should return 0 for failure and thread handle on success

    if ((serverThread = SRVR_main_init( )) == FALSE)
	{
        // Currently this will almost never happen
	// so this is more a place holder.
	char szMsgString[TMP_STRINGLEN];
	LoadString(hInstance, IMM_HUNG_SERVER, szMsgString, TMP_STRINGLEN);
 	MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__,
 			MB_OK | MB_ICONHAND);
	return 0;
	}

/* initialize main window */
wcl.hInstance     = hInstance;
wcl.lpszClassName = szClassName;
wcl.lpfnWndProc   = WindowFunc;
wcl.style	  = 0;
wcl.hIcon         = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_IBSVR));
wcl.hCursor       = LoadCursor(NULL, IDC_ARROW);
wcl.lpszMenuName  = NULL;
wcl.cbClsExtra    = 0;
wcl.cbWndExtra    = 0;

wcl.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);

if(!RegisterClass(&wcl))
    {
    char szMsgString[MSG_STRINGLEN];
    strcpy (szMsgString, "Unable to register window class");
    MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK);
    return 0;
    }

hWnd = CreateWindowEx(
    0,
    szClassName,
    VersionInformation::APPLICATION_NAME__,
    WS_DLGFRAME | WS_SYSMENU | WS_MINIMIZEBOX,
    CW_USEDEFAULT,
    CW_USEDEFAULT,
    APP_HSIZE,
    APP_VSIZE,
    HWND_DESKTOP,
    NULL,
    hInstance,
    NULL );

// set up a handler, like the following - ofcourse
// only when it becomes an issue with MT
// RRK SVC_shutdown_init(WINDOW_shutdown, (UINT)hWnd);

// Do the proper ShowWindow depending on if the app is an icon on
// the desktop, or in the task bar.
SendMessage(hWnd, WM_COMMAND, IDM_CANCEL, 0);
UpdateWindow(hWnd);

while(GetMessage(&msg, NULL, 0, 0))
    {
    if (hPSDlg) // If property sheet dialog is open
	{
	// Check if the message is property sheet dialog specific
	BOOL bPSMsg = PropSheet_IsDialogMessage(hPSDlg, &msg);

	// Check if the property sheet dialog is still valid, if not destroy it
	if (!PropSheet_GetCurrentPageHwnd(hPSDlg))
   	    {
	    DestroyWindow(hPSDlg);
	    hPSDlg = NULL;
	    }
	if (bPSMsg)
	    continue;
	}
    TranslateMessage(&msg);
    DispatchMessage(&msg);
    }

return msg.wParam;
}

/*
   Input:  hWnd - Handle to the window
           message - Message ID
           wParam - WPARAM parameter for the message
           lParam - LPARAM parameter for the message
   Return: FALSE indicates that the message has not been handled
           TRUE indicates the message has been handled 
    Description: This is main window procedure for the Interserver. This
                traps all significant messages and processes them.
*/ 

LRESULT CALLBACK WindowFunc (
    HWND	hWnd,
    UINT	message,
    WPARAM	wParam,
    LPARAM	lParam)
{

static ULONG        ulLastMask = 0L;
static BOOLEAN      bInTaskBar = FALSE;
static BOOLEAN      bStartup = FALSE;

ULONG ulInUseMask = 0L;

switch (message)
    {
    case WM_QUERYENDSESSION:
        // RRK take a look at this one more time
        // since our job is to dole out servers
        // we can anytime close down and there is not context
        // to worry about
	return TRUE;

    case WM_CLOSE:
        // Before we close down, make sure we kill our
        // server thread !!!RRK
        // if (serverHanlde)
        //   closeHandle (serverHandle);
	DestroyWindow(hWnd);
	break;    

    case WM_COMMAND:
 	switch (wParam)
	{
	    case IDM_CANCEL:
		ShowWindow(hWnd, bInTaskBar ? SW_HIDE : SW_MINIMIZE);
		return TRUE;

  	    case IDM_OPENPOPUP:	  
		{
	 	HMENU	hPopup = NULL;
		POINT	curPos;
   		char szMsgString[MSG_STRINGLEN];

		// The SetForegroundWindow() has to be called because our window
		// does not become the Foreground one (inspite of clicking on
		//the icon).  This is so because the icon is painted on the task
		//bar and is not the same as a minimized window.
		SetForegroundWindow(hWnd);

		hPopup = CreatePopupMenu();
   		LoadString(hInstance, IDS_CONFIGURE, szMsgString, MSG_STRINGLEN);
		AppendMenu(hPopup, MF_STRING, IDM_CONFIGURE, szMsgString);
   		LoadString(hInstance, IDS_SHUTDOWN, szMsgString, MSG_STRINGLEN);
		AppendMenu(hPopup, MF_STRING, IDM_SHUTDOWN, szMsgString);
		AppendMenu(hPopup, MF_SEPARATOR, IDM_SHUTDOWN, szMsgString);
    		LoadString(hInstance, IDS_PROPERTIES, szMsgString, MSG_STRINGLEN);
		AppendMenu(hPopup, MF_STRING, IDM_PROPERTIES, szMsgString);
		SetMenuDefaultItem(hPopup, IDM_PROPERTIES, FALSE);
		GetCursorPos(&curPos);
		TrackPopupMenu(hPopup, TPM_LEFTALIGN | TPM_RIGHTBUTTON, curPos.x, curPos.y, 0, hWnd, NULL);
		DestroyMenu(hPopup);
		return TRUE;
		}

	    case IDM_SHUTDOWN:
		SendMessage(hWnd, WM_CLOSE, 0, 0);
		return TRUE;

	    case IDM_CONFIGURE:
		bStartup = TRUE;
		LaunchISConfig(hWnd);
		bStartup = FALSE;
		return TRUE;

	    case IDM_PROPERTIES:
	        if (!hPSDlg)
		    hPSDlg = DisplayProperties(hWnd, hInstance, usServerFlags);
		else
		    SetForegroundWindow(hPSDlg);
		return TRUE;
	}

    	case ON_NOTIFYICON:
	    switch (lParam)
	    	{
	      	case WM_LBUTTONDOWN:
		    break;

	    	case WM_LBUTTONDBLCLK:
		    if (!bStartup)
		        PostMessage(hWnd, WM_COMMAND, (WPARAM) IDM_PROPERTIES, 0);
		    break;

	    	case WM_RBUTTONUP:
		    // The TrackPopupMenu() is inconsistant if called from here? 
		    // This is the only way I could make it work.
		    if (!bStartup)
		      PostMessage(hWnd, WM_COMMAND, (WPARAM) IDM_OPENPOPUP, 0);
		    
		    break;
	    	}
	    break;

    	case WM_CREATE:
	        HICON           hIcon;
	        NOTIFYICONDATA  nid;

           	hIcon = (HICON) LoadImage(hInstance, 
	 			MAKEINTRESOURCE(IDI_IBSVR_SMALL),
				IMAGE_ICON, 0, 0, LR_DEFAULTCOLOR);
   	    	nid.cbSize     = sizeof(NOTIFYICONDATA); 
	    	nid.hWnd       = hWnd; 
	    	nid.uID        = IDI_IBSVR;
	    	nid.uFlags     = NIF_TIP | NIF_ICON | NIF_MESSAGE; 
	    	nid.uCallbackMessage = ON_NOTIFYICON; 
	    	nid.hIcon      = hIcon;

	    	lstrcpy(nid.szTip, VersionInformation::APPLICATION_NAME__);
	        lstrcat(nid.szTip, "-");
	        lstrcat(nid.szTip, VersionInformation::DRIVER_VERSION__);
	
	    	// This should return true if it is succssfull 
		// and we are on Win95
	        bInTaskBar = Shell_NotifyIcon(NIM_ADD, &nid); 
	 
	        if (hIcon) 
		    DestroyIcon(hIcon);

	    	if (!bInTaskBar)  //To be replaced after (if) window is finalized 
	            {
    		    char szMsgString[MSG_STRINGLEN];

	    	    HMENU hSysMenu = GetSystemMenu(hWnd, FALSE);
	    	    DeleteMenu(hSysMenu, SC_RESTORE, MF_BYCOMMAND);
	    	    AppendMenu(hSysMenu, MF_SEPARATOR, 0, NULL);
	 	    LoadString(hInstance, IDS_CONFIGURE, szMsgString, MSG_STRINGLEN);
 		    AppendMenu(hSysMenu, MF_STRING, IDM_CONFIGURE, szMsgString);
    		    LoadString(hInstance, IDS_SHUTDOWN, szMsgString, MSG_STRINGLEN);
		    AppendMenu(hSysMenu, MF_STRING, IDM_SHUTDOWN, szMsgString);
	    	    AppendMenu(hSysMenu, MF_SEPARATOR, 0, NULL);
   		    LoadString(hInstance, IDS_PROPERTIES, szMsgString, MSG_STRINGLEN);
		    AppendMenu(hSysMenu, MF_STRING, IDM_PROPERTIES, szMsgString);
	  	    DestroyMenu(hSysMenu);
	   	    }
	    break;

    	case WM_QUERYOPEN:
	    if (!bInTaskBar)
	   	return FALSE;
	    return DefWindowProc(hWnd, message, wParam, lParam); 
	
	case WM_SYSCOMMAND:
	    if (!bInTaskBar)
	   	switch (wParam)
		    {
		    case SC_RESTORE:
		    	return TRUE;

		    case IDM_CONFIGURE:
			bStartup = TRUE;
			LaunchISConfig(hWnd);
			bStartup = FALSE;
			return TRUE;

		    case IDM_SHUTDOWN:
		   	PostMessage(hWnd, WM_CLOSE, 0, 0);
		    	return TRUE;

		    case IDM_PROPERTIES:
			if (!hPSDlg)
		    	    hPSDlg = DisplayProperties(hWnd, hInstance, usServerFlags);
			else
		    	    SetFocus(hPSDlg);
	    		return TRUE;
		    }
		return DefWindowProc(hWnd, message, wParam, lParam);

    	case WM_DESTROY:
	    if (bInTaskBar)
	        {
	        NOTIFYICONDATA	nid;
	    	nid.cbSize = sizeof(NOTIFYICONDATA); 
	    	nid.hWnd   = hWnd; 
	    	nid.uID    = IDI_IBSVR; 
	    	nid.uFlags = 0;
	    	Shell_NotifyIcon(NIM_DELETE, &nid); 
	    	}

	    PostQuitMessage(0);
	    break;

        default:
	    return DefWindowProc(hWnd, message, wParam, lParam);
    }
return FALSE;
}

/*
 *  currently unused -- and so till MT days
 *  This is a callback function which is called at shutdown time.
 *  This function post the WM_DESTROY message in appl. queue.
 */
void __stdcall WIN_shutdown(HANDLE hWnd)
{
PostMessage((HWND) hWnd, WM_DESTROY, 0, 0);
}


HANDLE SRVR_main_init( )
{
DWORD id, status;
HANDLE serverThread;
LPDWORD is_window_service = 0;

// set the arguments to mean a continous loop
// of waits and forks.....is_window_service = 0
serverThread = CreateThread (0, 0, 
		(LPTHREAD_START_ROUTINE)SRVR_main_thread, 
		&is_window_service, 0, &id);
// This will work only if SRVR_main terminates and not exits
if (WaitForSingleObject (serverThread, 1000) != WAIT_TIMEOUT) 
{
  char szMsgString[TMP_STRINGLEN];
  LoadString(hInstance, IMM_NO_SERVICE, szMsgString, TMP_STRINGLEN);
  MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK | MB_ICONHAND);
  ExitProcess (0);
}
GetExitCodeThread (serverThread, &status);
if (status != STILL_ACTIVE)
{
  char szMsgString[TMP_STRINGLEN];
  LoadString(hInstance, IMM_NO_SERVICE, szMsgString, TMP_STRINGLEN);
  MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK | MB_ICONHAND );
  ExitProcess (0);
}
if (!serverThread)
  return FALSE;
else
  return serverThread;
}

DWORD SRVR_main_thread(LPDWORD param)
{
  int x = SRVR_main (0, 0); // from interserver.cpp
  return 0;
}

///////////////////////////////
void LaunchISConfig(HWND hWnd)
{
    LPCTSTR lpApplicationName = NULL;
    LPTSTR lpCommandLine = (LPTSTR) ISCONFIG;
    BOOL bInheritHandles = TRUE;
    DWORD dwCreationFlags = DETACHED_PROCESS | NORMAL_PRIORITY_CLASS,
	  dwRetval;
    LPVOID lpEnvironment = NULL;
    LPCTSTR lpCurrentDirectory = NULL;
    STARTUPINFO lpStartupInfo;
    PROCESS_INFORMATION lpProcessInformation;

    // Startup parameters
    lpStartupInfo.cb = sizeof(lpStartupInfo);
    lpStartupInfo.lpReserved = NULL;
    lpStartupInfo.lpDesktop = NULL;
    lpStartupInfo.lpTitle = NULL;
    lpStartupInfo.dwFlags = STARTF_USESHOWWINDOW;
    lpStartupInfo.wShowWindow = SW_SHOWNORMAL;
    lpStartupInfo.cbReserved2 = NULL;
    lpStartupInfo.lpReserved2 = NULL;

    if (! CreateProcess(lpApplicationName,
			lpCommandLine,
			NULL,
                        NULL,
			bInheritHandles,
			dwCreationFlags,
			lpEnvironment,
			lpCurrentDirectory,
			&lpStartupInfo,
			&lpProcessInformation))
        {
	char szMsgString[TMP_STRINGLEN];
	LoadString(hInstance, IMM_NOCONFIGURE, szMsgString, TMP_STRINGLEN);
	MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, 
			MB_OK | MB_ICONHAND );
        }
    else
        {
	// Wait until the app has actually launched
        dwRetval = WaitForInputIdle(lpProcessInformation.hProcess, INFINITE);
        switch (dwRetval)
 	    {
	    case 0 :
		{
		MSG lpMsg;
	        // Once the app has launched, do not return until the process has exited.
		CloseHandle(lpProcessInformation.hThread);
		while (WaitForSingleObject(lpProcessInformation.hProcess, 1000) == WAIT_TIMEOUT)
		    {
                    // Eat up and process any messages so that they are not duplicated later
		    // if this is not done, then all of the messages are queued up and processed
		    // once LaunchISConfig returns.  The side effects of this are the popup menu
                    // being displayed at the cursor and/or the property sheet being displayed.
                    if (PeekMessage(&lpMsg, hWnd, 0, 0, PM_REMOVE))
		        {
  		        if (lpMsg.message != WM_QUIT)
			    {
			    TranslateMessage(&lpMsg);
			    DispatchMessage(&lpMsg);
			    }
                        }
		    }
		break;
		}

	    default :
	        {
		char szMsgString[TMP_STRINGLEN];
		LoadString(hInstance, IMM_NOINIT, szMsgString, TMP_STRINGLEN);
		MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, 
				MB_OK | MB_ICONHAND );
		break;
	        }
	    }
        }
}
