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
#ifndef _JIBSNET_ERROR_H_
#define _JIBSNET_ERROR_H_


#ifdef UNIX 
// HP-UX port (old CC): we should include "stdarg.h".
//       InterBase source uses:
//
//       #ifdef _ANSI_PROTOTYPES_
//       #include <stdarg.h>
//       #else
//       #include <varargs.h>
//       #endif
//
//       for ALL platforms (Windows included)
//
//#include <varargs.h>
#include <stdarg.h>

#include <sys/errno.h>
extern int errno;
#define ERRNO errno
#endif // UNIX

#ifdef WIN32
#include <errno.h>
#include <fcntl.h>
#include <io.h>
#include <process.h>
#include <signal.h>
#define ERRNO errno
#include <winsock.h>
#define NO_ITIMER
// #define ERRNO		WSAGetLastError()
#define H_ERRNO		WSAGetLastError()
#define SOCLOSE		closesocket
#define SYS_ERR		gds_arg_win32
#endif // WIN32


/*
 * Error handling routines.
 * These functions are generic and re-usable as a whole
 *
 * use err_quit, err_sys, err_dump, err_ret appropriately
 * the names are suggestive, use ret for recoverable condition
 * and use the others based on source of error and what needs
 * to be done after
 * 
 * In case of SYS5, these functions are simply a bunch of fprints
 */

// The Error class does not depend on any other classes in JIBSNet.

class Error {

public:

  /* log file for daemon tracing */
  static const char LOG_DAEMON[];

  /*
   * Identify ourself, for syslog() messages.
   *
   * LOG_PID is an option that says prepend each message with our pid.
   * LOG_CONS is an option that says write to console if unable to send
   * the message to syslogd.
   * LOG_DAEMON is our facility.
   */
  static void err_init (char *ident);

  /*
   * Fatal error.  Print a message and terminate.
   * Don't print the system's errno value.
   *
   *	err_quit (str, arg1, arg2, ...)
   *
   * The string "str" must specify the conversion specification for any args.
   */
  static void err_quit (char *, ...);

  /*
   * Fatal error related to a system call.  Print a message and terminate.
   * Don't dump core, but do print the system's errno value and its
   * associated message.
   *
   *	err_sys (str, arg1, arg2, ...)
   *
   * The string "str" must specify the conversion specification for any args.
   */
  static void err_sys (char*, ...);

  /*
   * Recoverable error.  Print a message, and return to caller.
   *
   *	err_ret (str, arg1, arg2, ...)
   *
   * The string "str" must specify the conversion specification for any args.
   */
  static void err_ret (char*, ...);

  /*
   * Fatal error.  Print a message, dump core (for debugging) and terminate.
   *
   *	err_dump (str, arg1, arg2, ...)
   *
   * The string "str" must specify the conversion specification for any args.
   */
  static void err_dump (char*, ...);

  /*
   * Print the UNIX errno value.
   * We just append it to the end of the emesgstr[] array.
   */
  static void my_perror ();

private:

  /*
   * Return a string containing some additional operating-system
   * dependent information.
   * Note that different versions of UNIX assign different meanings
   * to the same value of "errno" 
   */
  static char* sys_err_str ();

};

#endif
