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
#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#include <iostream.h>
#include <stdlib.h>

#include "window_p.h"
#include "Window.h"
#include "Property.rh"
#include "event_msgs.h"
#include "VersionInformation.h"

// Global variables
static HINSTANCE hInst;
const char *EventLogMessages[] = {
	IC_EVNT_MSG_NO_SERVICE,
	IC_EVNT_MSG_APP_RUNNING,
	NULL};

// The name of the service
const char *SERVICE_NAME = VersionInformation::NT_SERVICE_NAME__;

// Event used to hold ServiceMain from completing
HANDLE terminateEvent = NULL;

// Handle used to communicate status info with
// the SCM. Created by RegisterServiceCtrlHandler
SERVICE_STATUS_HANDLE serviceStatusHandle;

// Flags holding current state of service
BOOL pauseService = FALSE;
BOOL runningService = FALSE;

// Thread for the actual work
HANDLE threadHandle = 0;

// prototypes
static HANDLE parse_args(LPSTR, USHORT*);
extern int SRVR_main(int is_window_service, HANDLE handle);

void SVC_PostEventLogMsg(int ErrNo)
{
HANDLE hEventLog;
hEventLog = RegisterEventSource(NULL,VersionInformation::APPLICATION_NAME__);
ReportEvent(hEventLog,EVENTLOG_ERROR_TYPE,0,1000+ErrNo,NULL,1,
	    0,&EventLogMessages[ErrNo],NULL);
DeregisterEventSource(hEventLog);

}

void ErrorHandler(char *s, DWORD err)
{
	cout << s << endl;
	cout << "Error number: " << err << endl;
	ExitProcess(err);
}

DWORD ServiceThread(LPDWORD param)
{
        SRVR_main(1, 0);
	return 0;
}

// Initializes the service by starting its thread
BOOL InitService()
	{
	DWORD id, status;

	// Start the service's thread

	threadHandle = CreateThread(0, 0,
		(LPTHREAD_START_ROUTINE) ServiceThread,
		0, 0, &id);

	// This will work only if SRVR_main terminates and not exits
	if (WaitForSingleObject (threadHandle, 1000) != WAIT_TIMEOUT) 
	{
	  SVC_PostEventLogMsg(IC_EVENTERR_NO_SERVICE);
	  return FALSE;
	}
	GetExitCodeThread (threadHandle, &status);
	if (status != STILL_ACTIVE)
	{
	  SVC_PostEventLogMsg(IC_EVENTERR_NO_SERVICE);
	  return FALSE;
	}

	if (threadHandle==0)
		return FALSE;
	else
	{
		runningService = TRUE;
		return TRUE;
	}
}

// Resumes a paused service
VOID ResumeService()
{
	pauseService=FALSE;
	ResumeThread(threadHandle);
}

// Pauses the service
VOID PauseService()
{
	pauseService = TRUE;
	SuspendThread(threadHandle);
}

// Stops the service by allowing ServiceMain to
// complete
VOID StopService() 
{
	runningService=FALSE;
	// Set the event that is holding ServiceMain
	// so that ServiceMain can return
	SetEvent(terminateEvent);
}

// This function consolidates the activities of 
// updating the service status with
// SetServiceStatus
BOOL SendStatusToSCM (DWORD dwCurrentState,
	DWORD dwWin32ExitCode, 
	DWORD dwServiceSpecificExitCode,
	DWORD dwCheckPoint,
	DWORD dwWaitHint)
{
	BOOL success;
	SERVICE_STATUS serviceStatus;

	// Fill in all of the SERVICE_STATUS fields
	serviceStatus.dwServiceType =
		SERVICE_WIN32_OWN_PROCESS;
	serviceStatus.dwCurrentState = dwCurrentState;

	// If in the process of something, then accept
	// no control events, else accept anything
	if (dwCurrentState == SERVICE_START_PENDING)
		serviceStatus.dwControlsAccepted = 0;
	else
		serviceStatus.dwControlsAccepted = 
			SERVICE_ACCEPT_STOP |
			SERVICE_ACCEPT_PAUSE_CONTINUE |
			SERVICE_ACCEPT_SHUTDOWN;

	// if a specific exit code is defines, set up
	// the win32 exit code properly
	if (dwServiceSpecificExitCode == 0)
		serviceStatus.dwWin32ExitCode =
			dwWin32ExitCode;
	else
		serviceStatus.dwWin32ExitCode = 
			ERROR_SERVICE_SPECIFIC_ERROR;
	serviceStatus.dwServiceSpecificExitCode =
		dwServiceSpecificExitCode;

	serviceStatus.dwCheckPoint = dwCheckPoint;
	serviceStatus.dwWaitHint = dwWaitHint;

	// Pass the status record to the SCM
	success = SetServiceStatus (serviceStatusHandle,
		&serviceStatus);
	if (!success)
		StopService();

	return success;
}

// Dispatches events received from the service 
// control manager
VOID ServiceCtrlHandler (DWORD controlCode) 
{
	DWORD  currentState = 0;
	BOOL success;

	switch(controlCode)
	{
		// There is no START option because
		// ServiceMain gets called on a start

		// Stop the service
		case SERVICE_CONTROL_STOP:
			currentState = SERVICE_STOP_PENDING;
			// Tell the SCM what's happening
			success = SendStatusToSCM(
				SERVICE_STOP_PENDING,
				NO_ERROR, 0, 1, 5000);
			// Not much to do if not successful

			// Stop the service
			StopService();
			return;

		// Pause the service
		case SERVICE_CONTROL_PAUSE:
			if (runningService && !pauseService)
			{
				// Tell the SCM what's happening
				success = SendStatusToSCM(
					SERVICE_PAUSE_PENDING,
					NO_ERROR, 0, 1, 1000);
				PauseService();
			currentState = SERVICE_PAUSED;
			}
			break;

		// Resume from a pause
		case SERVICE_CONTROL_CONTINUE:
			if (runningService && pauseService)
			{
				// Tell the SCM what's happening
				success = SendStatusToSCM(
					SERVICE_CONTINUE_PENDING,
					NO_ERROR, 0, 1, 1000);
					ResumeService();
					currentState = SERVICE_RUNNING;
			}
			break;

		// Update current status
		case SERVICE_CONTROL_INTERROGATE:
			// it will fall to bottom and send status
			break;

		// Do nothing in a shutdown. Could do cleanup
		// here but it must be very quick.
		case SERVICE_CONTROL_SHUTDOWN:
			// Do nothing on shutdown
			return;
		default:
 			break;
	}
	SendStatusToSCM(currentState, NO_ERROR,
		0, 0, 0);
}

// Handle an error from ServiceMain by cleaning up
// and telling SCM that the service didn't start.
VOID terminate(DWORD error)
{
	// if terminateEvent has been created, close it.
	if (terminateEvent)
		CloseHandle(terminateEvent);

	// Send a message to the scm to tell about
	// stopage
	if (serviceStatusHandle)
		SendStatusToSCM(SERVICE_STOPPED, error,
			0, 0, 0);

	// If the thread has started kill it off
	if (threadHandle)
		CloseHandle(threadHandle);

	// Do not need to close serviceStatusHandle
}

// ServiceMain is called when the SCM wants to
// start the service. When it returns, the service
// has stopped. It therefore waits on an event
// just before the end of the function, and
// that event gets set when it is time to stop. 
// It also returns on any error because the
// service cannot start if there is an eror.
VOID ServiceMain(DWORD argc, LPTSTR *argv) 
{
	BOOL success;

	// immediately call Registration function
	serviceStatusHandle =
		RegisterServiceCtrlHandler(
			SERVICE_NAME,
			(LPHANDLER_FUNCTION) ServiceCtrlHandler);
	if (!serviceStatusHandle)
	{
		terminate(GetLastError());
		return;
	}

	// Notify SCM of progress
	success = SendStatusToSCM(
		SERVICE_START_PENDING,
		NO_ERROR, 0, 1, 5000);
	if (!success)
	{
		terminate(GetLastError()); 
		return;
	}

	// create the termination event
	terminateEvent = CreateEvent (0, TRUE, FALSE,
		0);
	if (!terminateEvent)
	{
		terminate(GetLastError());
		return;
	}

	// Notify SCM of progress
	success = SendStatusToSCM(
		SERVICE_START_PENDING,
		NO_ERROR, 0, 2, 1000);
	if (!success)
	{
		terminate(GetLastError()); 
		return;
	}

	// Check for startup params off argc, argv
	// It will be tricky to depend on it as passing
	// of parameters at startup is possible only
	// for manual start (mot automatic)

	// Notify SCM of progress
	success = SendStatusToSCM(
		SERVICE_START_PENDING,
		NO_ERROR, 0, 3, 5000);
	if (!success)
	{
		terminate(GetLastError()); 
		return;
	}

	// Start the service itself
	success = InitService();
	if (!success)
	{
		terminate(GetLastError());
		return;
	}

	// The service is now running. 
	// Notify SCM of progress
	success = SendStatusToSCM(
		SERVICE_RUNNING,
		NO_ERROR, 0, 0, 0);
	if (!success)
	{
		terminate(GetLastError()); 
		return;
	}

	// Wait for stop signal, and then terminate
	WaitForSingleObject (terminateEvent, INFINITE);

	terminate(0);
}

int WINAPI WinMain (
	HINSTANCE       hThisInst,
	HINSTANCE       hPrevInst,
	LPSTR           lpszArgs,
	int             nWndMode)
{

USHORT is_window_service = 0;

HWND hWnd = NULL;
hInst = hThisInst;

HANDLE connection_handle = parse_args(lpszArgs, &is_window_service);

// this loop will be executed on fork both in standalone 
// and windows NT services manager fork

if (connection_handle != INVALID_HANDLE_VALUE) {
  is_window_service = 0;
  SRVR_main (is_window_service, connection_handle);
  return 1;
  }

// If we are not starting with an s param (inetd_falg) or
// we are on Win 95 then we will start as an
// application.

OSVERSIONINFO OSVerInfo;
OSVerInfo.dwOSVersionInfoSize=sizeof(OSVERSIONINFO);
GetVersionEx(&OSVerInfo);
if ((!is_window_service) || (OSVerInfo.dwPlatformId == VER_PLATFORM_WIN32_WINDOWS))
    {
    // If we are on NT then make sure that the
    // service is not already started, before allowing 
    // the application to start.
    if (OSVerInfo.dwPlatformId == VER_PLATFORM_WIN32_NT)
	{
	char szMsgString[TMP_STRINGLEN];
	SC_HANDLE hManager, hService ;
	SERVICE_STATUS lpsServiceStatus;
	BOOLEAN ServiceRunning = FALSE;
	hManager = OpenSCManager(NULL,NULL,SC_MANAGER_CONNECT);
	hService = OpenService(hManager,SERVICE_NAME,SERVICE_QUERY_STATUS);
	if (QueryServiceStatus(hService, &lpsServiceStatus))
	    { 
	    switch (lpsServiceStatus.dwCurrentState)
		{
		case SERVICE_STOP_PENDING: 
		case SERVICE_RUNNING: 
		case SERVICE_START_PENDING: 
		case SERVICE_CONTINUE_PENDING: 
		    LoadString(hThisInst, IMM_SERVICE_RUNNING, szMsgString, TMP_STRINGLEN);
		    MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK | MB_ICONHAND);
		    ServiceRunning = TRUE;
		    break;
		case SERVICE_PAUSE_PENDING: 
		case SERVICE_PAUSED: 
		    LoadString(hThisInst, IMM_SERVICE_PAUSED, szMsgString, TMP_STRINGLEN);
		    MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK | MB_ICONHAND);
		    ServiceRunning = TRUE;
		    break;
		}
	    }
	CloseServiceHandle(hService);
	CloseServiceHandle(hManager);
	if (ServiceRunning)
	    return (0);
	}
    // Check to see if another instance of the application is already
    // running.  If so display an error message.
    hWnd = FindWindow(VersionInformation::APPLICATION_NAME__, VersionInformation::APPLICATION_NAME__);
    if (hWnd) 
	{
	char szMsgString[TMP_STRINGLEN];
	LoadString(hThisInst, IMM_ALREADY_RUNNING, szMsgString, TMP_STRINGLEN);
	MessageBox(NULL, szMsgString, VersionInformation::APPLICATION_NAME__, MB_OK | MB_ICONHAND);
	return (0);
	}
    WIN_SRVR_main(hThisInst, nWndMode, is_window_service);
    return (0);
    }

// Now we know that we are starting a service. So we
// first have to make sure that we are not already
// running as an application.  If the application
// is already running then DO NOT start the service
// and exit.  Thus letting SCM display an error.

hWnd = FindWindow(VersionInformation::APPLICATION_NAME__, VersionInformation::APPLICATION_NAME__);
if (hWnd) 
    {
    SVC_PostEventLogMsg(IC_EVENTERR_APP_RUNNING);
    return 1;
    }

SERVICE_TABLE_ENTRY serviceTable[] = { 
{ (LPTSTR) SERVICE_NAME, 	(LPSERVICE_MAIN_FUNCTION) ServiceMain},
{ NULL, 		NULL } };
BOOL success;

// Register with the SCM
success = StartServiceCtrlDispatcher(serviceTable);
if (!success)
    ErrorHandler("In StartServiceCtrlDispatcher", GetLastError());
return 1;
}

static HANDLE parse_args(
	LPSTR   lpszArgs,
	USHORT  *is_window_service)
{
char    *p, c;
HANDLE  connection_handle;

connection_handle = INVALID_HANDLE_VALUE;

for (p = lpszArgs; *p; p++)
    {
    if (*p++ == '-')
	while (c = *p++)
	    switch (c)
		{
		case 's':       /* Run as a service */
                    *is_window_service = 1;
		    break;

                case 'h':
                    connection_handle = (HANDLE)atol (++p);
                    break;
		case 'z':
            // RRK
            // print server version and quit
            // Not relevant for Windows as one should
			// use property sheet
			ExitProcess (0);

		default:
		    p++;
		}
    }
return connection_handle;
}
#endif
