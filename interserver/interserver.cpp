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
/*
 * INTERSERVER main programme: both standalone and inetd driven
 * 			       and in the future multi-client, threaded stuff
 *
 *	-i		says we were *not* started by inted.
 *	-p port#	specifies a different port# to listen on.
 *                      default port is specified in /etc/services
 */

#include <stdio.h> 

// atoi(), exit()
#include <stdlib.h>

#ifdef WIN32

#include "Window.h"
#include "interservice_proto.h"
#include "event_msgs.h"
#include "Property.rh"
#define EXIT(a)		ExitProcess(a)

#else

#define EXIT(a)		exit(a)

#endif

#include "Error.h"
#include "JIBSRemote.h"
#include "NetTCP.h"
#include "VersionInformation.h"

// This is not thread safe.
// Just hope that this doesn't happen too often.
void errorLog (char* what)
{
  // For an InterServer application, this log file goes
  // to the proper directory, but for a service on NT,
  // the log file goes to win32 or some such system directory.
  // !!! When we get time, extract the root directory from
  // !!! the registry to prefix interserver.log.
  // !!! See ISC_get_registry_var and GetProfileString as used
  // !!! in gds.c::gds__prefix and gds.c::gds__log
#ifdef UNIX
  FILE* f = fopen ("/usr/interclient/interserver.log", "a");
#else
  FILE* f = fopen ("interserver.log", "a");
#endif
  if (f) {
    fprintf (f, "%s\n", what);
    fflush (f);
    fclose (f);
  }
}

// Not thread safe.
// This tracing will not work for multiple connections!
#ifdef TRACEON
FILE *traceStream__ = NULL;

void debugTraceALine (char* where, char* what)
{
  if (!traceStream__)
    traceStream__ = fopen ("debug.log", "w");
  if (traceStream__) {
    fprintf (traceStream__, "%s %s\n", where, what);
    fflush (traceStream__);
  }
}
void debugTraceAnInt (char* where, int what)
{
  if (!traceStream__)
    traceStream__ = fopen ("debug.log", "w");
  if (traceStream__) {
    fprintf (traceStream__, "%s %d\n", where, what);
    fflush (traceStream__);
  }
}
//fredt@users.sourceforge.net added function
void debugTraceAPointer (char* where, void* what)
{
  if (!traceStream__)
    traceStream__ = fopen ("debug.log", "w");
  if (traceStream__) {
    fprintf (traceStream__, "%s %p\n", where, what);
    fflush (traceStream__);
  }
}
#endif

// Notice we do not depend on JIBSNet
// !!! In the future, interserver should NOT depend on NetTCP.
// !!! Only JIBSNet should depend on NetTCP.

#ifdef WIN32
int SRVR_main(int is_window_service, HANDLE handle)
#else
int main(int argc, char *argv[])
#endif
{
  int sockfd, newSockfd;
  int		childpid;
  int port = 0;

#ifdef WIN32
  WSADATA wsdata;
  int winStatus;
  int inetdflag = 0; /* FALSE since on windows there is no inetd */

  newSockfd = (int)handle;
  winStatus = WSAStartup (MAKEWORD (1, 1), &wsdata);
  if (winStatus != 0)
     Error::err_sys ("winsock dll initialization failure");

  // forked by services manager or by user initiated daemon
  if (newSockfd != 0)
    goto fork_start;

#else
  register char	*s;

  int inetdflag = 1; // TRUE if started by inetd

  // Creates error log file and writes text "INTERSERVER" to it.
  Error::err_init("INTERSERVER");

  while (--argc > 0 && (*++argv)[0] == '-')
    for (s = argv[0]+1; *s != '\0'; s++)
      switch (*s) {
      case 'i':
	inetdflag = 0;	/* turns OFF the flag */
	/* (it defaults to 1) */
	break;

      case 'p':		/* specify server's port# */
	if (--argc <= 0)
	  Error::err_quit("-p requires another argument");
	port = atoi(*++argv);
	break;

      case 'h':
        if (--argc) 
          newSockfd = atol (*++argv); 
        else
	  Error::err_quit("-h requires another argument");
        goto fork_start;
        break;

      case 'z':
	// !!! have a proper version define
	fprintf (stderr, "interserver version: %s\n", VersionInformation::DRIVER_VERSION__);
        fprintf (stderr, "remote protocol version: %s\n", VersionInformation::DRIVER_VERSION__);
	fflush (stderr);
	EXIT(0);

      default:
	Error::err_quit ("unknown command line option: %c", *s);
      }
#endif


  if (inetdflag == 0) {
    /*
     * Start us up as a daemon process (in the background).
     * Also initialize the network connection - create the socket
     * and bind our well-known address to it.
     */
    sockfd = NetTCP::netInit (VersionInformation::NT_SERVICE_FILE_ENTRY__, port);
    if (sockfd < 1)
      EXIT(1);
  }

  /*
   * The child created by netOpen() handles the client's request.
   * The parent waits for another request -- leave this in place
   * for multi-client and mt.  
   * In the inetd case, the parent from netOpen() never returns.
   */

  for (;;) {
    // !!! Do something about these debug prints.
    // !!! In the future, netOpen and netClose should be replaced by 
    // !!! jibsNet_.connect() and jibsNet_.disconnect()
    // !!! as done by interclient.

    if ( (childpid = NetTCP::netOpen(sockfd, &newSockfd, inetdflag)) == 0) {
fork_start:
      // JIBSRemote controls a remote JDBC session.
      JIBSRemote jibsRemote (newSockfd);
      jibsRemote.interserverMain ();
      // child processes client's request
      NetTCP::netClose (newSockfd);
#ifdef WIN32
      winStatus = WSACleanup ();
      if (winStatus == SOCKET_ERROR)
        Error::err_sys ("winsock dll cleanup failure");
#endif
      EXIT(0);
    }
    else {
      if (inetdflag == 0)
        continue;
    }

    /* parent waits for another client's request */
  }
  /* NOTREACHED */
  return 0;
}

