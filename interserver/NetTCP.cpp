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
//-*-C++-*-
#ifdef UNIX
// Please don't erase these dependency comments.

// sockaddr_in(local), htonl(), INADDR_ANY, htons()
#include <netinet/in.h>

// TCP_NODELAY
#include <netinet/tcp.h>

// close(), read(), nread(), write()
#include <unistd.h> 

// AF_INET, socket(), SOCK_STREAM, SOL_SOCKET, 
// SO_REUSEADDR, bind(), listen(), accept()
#include <sys/socket.h> 

// servent(local), getservbyname()
#include <netdb.h> 
#endif // UNIX

#ifdef WIN32
#include <winsock.h>
#include <stdio.h>
#include <fcntl.h>
#include <io.h>
#include <process.h>
#include <signal.h>
// !!! get it to the exact number
static char commandLine[200];
#endif

#include "NetTCP.h"
#include "Error.h"
#include "IB_Defines.h" // for debug trace

void
NetTCP::netClose (int sockfd)
{
  close (sockfd);
}

int
NetTCP::netInit (const char * const service, // the name of the service we provide
		 int port) // if nonzero, this is the port to listen on;
                           // overrides the standard port for the service
{
  struct servent *sp;
  int sockfd;
  struct sockaddr_in tcpSrvAddr;
  struct servent tcpServInfo;	
  int optValue;

  /*
   * We have to create a socket ourselves and bind our well-known
   * address to it. use either port# or get it thro 
   * service
   */
  // !!! bzero ((void *) &tcpSrvAddr, (size_t) sizeof (tcpSrvAddr));
  tcpSrvAddr.sin_family      = AF_INET;
  tcpSrvAddr.sin_addr.s_addr = htonl (INADDR_ANY);

  if (service != NULL) {
    if ( (sp = getservbyname(service, "tcp")) == NULL)
      Error::err_sys ("netInit: unknown service: %s/tcp", service);
    tcpServInfo = *sp;			/* structure copy */

    if (port > 0)
      tcpSrvAddr.sin_port = htons(port);
    /* caller's value */
    else
      tcpSrvAddr.sin_port = sp->s_port;
    /* service's value */
  } else {
    if (port <= 0) {
      Error::err_ret ("netInit: must specify either service or port");
      return(-1);
    }
    tcpSrvAddr.sin_port = htons (port);
  }

  /*
   * Create the socket and Bind our local address 
   * so that any client can send to us.
   * if there is a need to allow only a bunch of 
   * incoming address, say service subscribers, handle after
   * accept.....!!
   */

  if ( (sockfd = socket (AF_INET, SOCK_STREAM, 0)) < 0)
    Error::err_sys ("netInit: can't create stream socket");

  // Some systems are painfully long before they allow a port
  // to be reused, so specify REUSEADDR.
  optValue = 1;
  setsockopt (sockfd, 
	      SOL_SOCKET, 
	      SO_REUSEADDR, 
	      (char *) &optValue, 
	      sizeof (int));

  // Setting TCP_NODELAY guarantees that data is sent
  // as soon as it is written.
  setsockopt (sockfd,
	      IPPROTO_TCP,
	      TCP_NODELAY,
	      (char*) &optValue, // Non zero value disables Nagle Algorithm
	      sizeof (int));

  if (bind(sockfd, 
	   (struct sockaddr *) &tcpSrvAddr,
	   sizeof (tcpSrvAddr)) < 0)
    Error::err_sys ("netInit: can't bind local address");

  /*
   * And set the listen parameter, telling the system that we're
   * ready  to accept incoming connection requests.
   */

  listen (sockfd, 5);
  return (sockfd);
}

int
NetTCP::netOpen (int sockfd, int *newSockfd, int inetdflag)
{
  register int tmpSockfd, childpid;
// fredt@users.sourceforge.net changed socklen_t type to int
  //david jencks 1-19-2001 begin
  //  int clilen, on;
#ifndef socklen_t //problem on ms compiler
#define socklen_t int  //problem on rhlinux 7, socklen_t is uint, size_t is int
#endif
  int on;
  socklen_t clilen;
  //david jencks 1-19-2001 end
  struct sockaddr_in tcpCliAddr;

  on = 1;

  if (inetdflag) {
    /*	
     * When we're fired up by inetd, file
     * descriptors 0, 1 and 2 are sockets to the client.
     */
    *newSockfd = 0;	/* descriptor to read from */
    return (0);	/* done */
  }

  /*
   * For the multi-client server that's not initiated by inetd,
   * we have to wait for a connection request to arrive,
   * then fork a child to handle the client's request.
   */

again:
  clilen = sizeof (tcpCliAddr);
  tmpSockfd = accept (sockfd, (struct sockaddr *) &tcpCliAddr, &clilen);
  if (tmpSockfd < 0) {
    if (errno == EINTR) {
      errno = 0;
      goto again;	/* probably a SIGCLD that was caught */
    }
    Error::err_sys ("accept error");
  }

  /*
   * Fork a child process to handle the client's request.
   * The parent returns the child pid to the caller, which is
   * a multi-client server to wait for the next client request 
   */
#ifdef WIN32
  if ( (childpid = fork(tmpSockfd)) < 0)
    Error::err_sys ("server can't fork");
  else if (childpid > 0) 
  { /* parent */
    close (tmpSockfd);	/* close new connection */
    return (childpid);	/* and return */
  }
#endif

  /*
   * Child process continues here.
   * First close the original socket so that the parent
   * can accept any further requests that arrive there.
   * Then set "sockfd" in our process to be the descriptor that
   * we are going to process.
   */

  close(sockfd);
  *newSockfd = tmpSockfd;

  return(0);		/* return to process the connection */
}

#ifdef WIN32
int
NetTCP::fork (int parent_handle)
{
  char *ptr;
  HANDLE child_handle;
  STARTUPINFO		start_crud;
  PROCESS_INFORMATION	pi;

  DuplicateHandle (GetCurrentProcess(), (HANDLE) parent_handle,
		   GetCurrentProcess(), &child_handle, 0, TRUE, DUPLICATE_SAME_ACCESS);

  strcpy (commandLine, "interserver");
  ptr = commandLine + strlen (commandLine);
  sprintf (ptr, "  -h %d", (long) child_handle);

  start_crud.cb = sizeof (STARTUPINFO);
  start_crud.lpReserved = NULL;
  start_crud.lpReserved2 = NULL;
  start_crud.cbReserved2 = 0;
  start_crud.lpDesktop = NULL;
  start_crud.lpTitle = NULL;
  start_crud.dwFlags = 0;
  if (CreateProcess (
		     NULL, 
		     commandLine, 
		     NULL, NULL, TRUE, NULL, NULL, NULL,
		     &start_crud,
		     &pi))
    {
      CloseHandle (pi.hThread);
      CloseHandle (pi.hProcess);
    }
  CloseHandle (child_handle);
  return 1;
}
#endif

// Read n bytes from a descriptor.
int
NetTCP::readn (register int fd, 
	       register char *ptr, 
	       register int nbytes)
{
  int	nleft, nread;

  nleft = nbytes;
  while (nleft > 0) {
#ifdef WIN32
    nread = recv (fd, ptr, nleft, 0);
#else
    nread = read (fd, ptr, nleft);
#endif
#ifdef TRACEON
    debugTraceAnInt ("readn: ", nread);
#endif
    if (nread < 0)
      return (nread);		/* error, return < 0 */
    else if (nread == 0)
      break;			/* EOF */

    nleft -= nread;
    ptr   += nread;
  }
  return (nbytes - nleft);		/* return >= 0 */
}

// po - lifted from Stevens, p. 279
// If the connection has been dropped, write will generate
// a SIGPIPE which will cause interserver to exit.  This is ok.
// Writes n bytes to a descriptor.
int
NetTCP::writen (register int fd, 
		register char *ptr, 
		register int nbytes)
{
  int	nleft, nwritten;

  nleft = nbytes;
  while (nleft > 0) {
#ifdef WIN32
    nwritten = send (fd, ptr, nleft, 0);
#else
    nwritten = write (fd, ptr, nleft);
#endif
#ifdef TRACEON
    debugTraceAnInt ("NetTCP::writen: ", nwritten);
#endif
    if (nwritten <= 0)
      return (nwritten); // error

    nleft -= nwritten;
    ptr   += nwritten;
  }
  return (nbytes - nleft);
}

