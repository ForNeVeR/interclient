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
#ifndef _JIBSNET_NETTCP_H_
#define _JIBSNET_NETTCP_H_

// NetTCP provides low-level TCP/IP utilities.
// Note: NetTCP is not CDR-aware.

class NetTCP {

public:

  // Initiate the server's end.
  // We are passed a flag that says whether or not we were started by inetd
  // If we weren't started by inetd, wait for a client's request to arrive.
  // else, connection is already setup
  static int netOpen (int sockfd, 
		      int* newSockfd, 
		      int inetdflag);

  // Close the network connection.
  static void netClose (int sockfd);

  // Initialize the network connection for the server, 
  // when it has *not* been invoked by inetd.
  static int netInit (const char* const service, 
		      int port);

#ifdef WIN32
  static int fork ( int parent_handle);
#endif

  // Read "n" bytes from a descriptor.
  // Use in place of read() when fd is a stream socket.
  static int readn (int fd, 
		    char* ptr, 
		    int nbytes);

  // Write "n" bytes to a descriptor.
  // Use in place of write() when fd is a stream socket.
  static int writen (int fd, 
		     char* ptr, 
		     int nbytes);

};

#endif
