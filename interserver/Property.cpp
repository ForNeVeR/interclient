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

#include "VersionInformation.h"
#include "property.rh"
#include "window.h"

#define WIN_TEXTLEN   128

static HINSTANCE hInstance = NULL;      // Handle to the current app. instance
static HWND hPSDlg = NULL;              // Handle to the parent prop. sheet window
HBRUSH hGrayBrush = NULL;               // Handle to a Gray Brush
static USHORT     usServerFlags;        // Server Flag Mask

// Array for help IDs
static const DWORD aMenuHelpIDs[] =
{
   IDC_LOCATION, 4000,
   IDC_LOCATION_TEXT, 4000,
   IDC_VERSION, 4005,
   IDC_VERSION_TEXT, 4005,
   IDC_CAPABILITIES, 4010,
   IDC_CAPABILITIES_TEXT, 4010,
   IDC_CLIENT, 4020,
   IDC_SERVER, 4030,
   IDC_INTERBASE, 4050,
   0,    0
};

// Window procedures
LRESULT APIENTRY GeneralPage(HWND, UINT, WPARAM, LPARAM);

// Static functions to be called from this file only.
static char *MakeVersionString(char *, int, USHORT);

 /* 
 *	Try to shorten a given file name to a specified length.  Put
 *	in '...' where anything was taken out.
 *	Both szName and szShort name can be the same pointer.
 *  Inputs: 
 *           szName - Null terminated input path/filename.
 *           szShortName - Output buffer for truncated path/filename.
 *           dwLen - Length to truncate to.
 *  Outputs:
 *			 pointer to a buffer (szShortname).
 */
#define PATHSEP(c) ((c) == '\\' || (c) == '/')


char * ChopFileName(
    char *szName,
    char *szShortName,
    ULONG dwLen)
{
char *pchLeft,
	*pchRight,
	*pchLastRight,
	*pchLastLeft,
	*pchEnd,
	*pchTmp;

BOOL bLeft = TRUE,
	bLeftFull = FALSE,
	bRightFull = FALSE;

/* Set pointers to the beginning and the end */
pchLeft = pchEnd = szName;

while (*pchEnd)
    pchEnd++;

/* Check that the path is already short enough */
if (((ULONG)(pchEnd - pchLeft)) <= dwLen)
    {
    pchTmp = szShortName;
    while (*pchTmp++ = *szName++);
    return szShortName;
    }

/* Subtract the room needed for the three dots */
dwLen -= 3;
pchRight = pchEnd;
pchLastLeft = pchLeft;
pchLastRight = pchRight;

while (!bLeftFull || !bRightFull)
{
    if (bLeft)
	{
	while (!bLeftFull && pchLeft++ && !PATHSEP(*pchLeft) && pchLeft < pchRight);
	if ((pchLeft - szName) + ((ULONG)(pchEnd - pchRight)) > dwLen)
	    {
	    bLeftFull = TRUE;
	    pchLeft = pchLastLeft;
	    }
	else
	    pchLastLeft = pchLeft;
	}
    else
	{
	while (!bRightFull && pchRight-- && !PATHSEP(*pchRight) && pchLeft < pchRight);
	if ((pchLeft - szName) + ((ULONG)(pchEnd - pchRight)) > dwLen)
	    {
	    bRightFull = TRUE;
	    pchRight = pchLastRight;
	    }
	else
	    pchLastRight = pchRight;
	}
    bLeft = bLeft ? FALSE : TRUE;
}

for (pchTmp = szShortName, pchLeft = szName;
	pchLeft <= pchLastLeft;
	*pchTmp++ = *pchLeft++);
	*pchTmp++ = '.';
	*pchTmp++ = '.';
	*pchTmp++ = '.';
for (; pchLastRight < pchEnd; *pchTmp++ = *pchLastRight++);
	*pchTmp++ = '\0';
return szShortName;
}


/*
 *  Input:  hParentWnd - Handle to the main window of this application
 *  Return: Handle to the Property sheet dialog if successful
 *          NULL if error in displaying property sheet
 *  Description: This function initializes the page(s) of the property sheet,
 *               and then calls the PropertySheet() function to display it.
 */
HWND DisplayProperties(HWND hParentWnd, 
		       HINSTANCE hInst, 
		       USHORT usServerFlagMask)
{
PROPSHEETHEADER PSHdr;
PROPSHEETPAGE PSPages[1];

hInstance = hInst;
usServerFlags = usServerFlagMask;

ZeroMemory (&PSPages, sizeof (PROPSHEETPAGE));

PSPages[0].dwSize = sizeof(PROPSHEETPAGE);
PSPages[0].dwFlags = PSP_USETITLE;
PSPages[0].hInstance = hInstance;
PSPages[0].pszTemplate = MAKEINTRESOURCE(GENERAL_DLG);
PSPages[0].pszTitle = "General";
PSPages[0].pfnDlgProc = (DLGPROC) GeneralPage;
PSPages[0].pfnCallback = NULL;

PSHdr.dwSize = sizeof(PROPSHEETHEADER);
PSHdr.dwFlags = PSH_PROPTITLE | PSH_PROPSHEETPAGE |
	        PSH_USEICONID | PSH_MODELESS | PSH_NOAPPLYNOW;

PSHdr.hwndParent = hParentWnd;
PSHdr.hInstance = hInstance;
PSHdr.pszIcon = MAKEINTRESOURCE(IDI_IBSVR);
PSHdr.pszCaption = (LPSTR) VersionInformation::APPLICATION_NAME__;
PSHdr.nPages = sizeof(PSPages)/sizeof(PROPSHEETPAGE);
PSHdr.nStartPage = 0;
PSHdr.ppsp = (LPCPROPSHEETPAGE) &PSPages;
PSHdr.pfnCallback = NULL;

// Initialize the gray brush to paint the background
// for all prop. sheet pages and their controls
hGrayBrush = CreateSolidBrush(GetSysColor(COLOR_BTNFACE));

hPSDlg = (HWND) PropertySheet(&PSHdr);
if (hPSDlg <= 0)
    hPSDlg = NULL;
return hPSDlg;
}

/*
 *  Input:  hDlg - Handle to the page dialog
 *          unMsg - Message ID
 *          wParam - WPARAM message parameter
 *          lParam - LPARAM message parameter
 *
 *  Return: FALSE if message is not processed
 *          TRUE if message is processed here
 *
 *  Description: This is the window procedure for the "General" page dialog
 *               of the property sheet dialog box. All the Property Sheet
 *               related events are passed as WM_NOTIFY messages and they
 *               are identified within the LPARAM which will be pointer to 
 *               the NMDR structure
 */
LRESULT CALLBACK GeneralPage(HWND hDlg, UINT unMsg, WPARAM wParam, LPARAM lParam)
{
switch (unMsg)
    {   
    case WM_INITDIALOG:
	{
	char  *pszPtr;
	char  szText[MSG_STRINGLEN];
	char  szWindowText[WIN_TEXTLEN];
	lstrcpy(szText, VersionInformation::DRIVER_VERSION__);
	SetDlgItemText(hDlg, IDC_VERSION, szText);
	LoadString(hInstance, IDS_SERVERPROD_NAME, szText, MSG_STRINGLEN);
	SetDlgItemText(hDlg, IDC_PRODNAME, szText);
	LoadString(hInstance, IDS_JDBC, szText, MSG_STRINGLEN);
	SetDlgItemText(hDlg, IDC_CAPABILITIES, szText);
	GetModuleFileName(hInstance, szWindowText, WIN_TEXTLEN);
	pszPtr = strrchr (szWindowText, '\\'); 
	*(pszPtr+1) = 0x00; 
	ChopFileName(szWindowText, szWindowText, 34);
	SetDlgItemText(hDlg, IDC_LOCATION, szWindowText);
	}
	break;
    case WM_CTLCOLORDLG:
    case WM_CTLCOLORSTATIC:
    case WM_CTLCOLORLISTBOX:
    case WM_CTLCOLORMSGBOX:
    case WM_CTLCOLORSCROLLBAR:
    case WM_CTLCOLORBTN:
	{
	OSVERSIONINFO   OsVersionInfo;
	OsVersionInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
	if (GetVersionEx ((LPOSVERSIONINFO) &OsVersionInfo) &&
			OsVersionInfo.dwMajorVersion < 4)
	    {
	     SetBkMode((HDC) wParam, TRANSPARENT);
	     return (LRESULT) hGrayBrush;
	    }
	}
	break;

    case WM_COMMAND:
	break;

    case WM_HELP:
	break;
	/*******************
	{
	LPHELPINFO lphi;

	lphi = (LPHELPINFO)lParam;
	if (lphi->iContextType == HELPINFO_WINDOW)   // must be for a control
	    {
	    WinHelp (lphi->hItemHandle,
		     "ISCONFIG.HLP",
		     HELP_WM_HELP,
		     (DWORD)(LPVOID)aMenuHelpIDs);
	    }
	}
	return TRUE;
	**********************/

    case WM_CONTEXTMENU:
	break;
	/************************!!! ask mark about this
	{
	WinHelp ((HWND)wParam,
		  "ISCONFIG.HLP",
		  HELP_CONTEXTMENU,
		  (DWORD)(LPVOID)aMenuHelpIDs);
	}
	return TRUE;
	**********************************/


    case WM_NOTIFY:
	switch(((LPNMHDR)lParam)->code)
	    {
	    case PSN_KILLACTIVE:
		SetWindowLong(hDlg, DWL_MSGRESULT, FALSE);
		break;
	    }
	break;
    }
return FALSE;
}

/*
 *  Input:  pchBuf - Buffer to be filled into
 *          nLen - Length of the buffer
 *          usServerFlagMask - Bit flag mask encoding the server flags
 *  Return: Buffer containing the version string
 *  Description: This method is called to get the Version String. This string
 *               is based on the flag set in usServerFlagMask.
 */

static char *MakeVersionString(char *pchBuf, int nLen, USHORT usServerFlagMask)
{
char* p = pchBuf;
char* end = p + nLen;

p += LoadString(hInstance, IDS_VERSION, p, end - p);

if (p < end)
	 *p++ = '\0';
return pchBuf;
}
