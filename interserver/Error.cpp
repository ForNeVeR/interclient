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
// vsprintf(), sprintf()
#include <stdio.h>

// strlen(), strcat()
#include <string.h>

// exit(), abort()
#include <stdlib.h>

#include "Error.h"

#ifdef BSD
/*
 * Under BSD, these server routines use the syslog(3) facility.
 * They don't append a newline, for example.
 */

#include <syslog.h>

#else	/* not BSD */

#ifdef USE_INETD
#define	syslog(a,b)
#define	openlog(a,b,c)
#else
#define	syslog(a,b)	fprintf(stderr, "%s\n", (b)); fflush(stderr)
#define	openlog(a,b,c)	fprintf(stderr, "%s\n", (a)); fflush(stderr)
#endif

#endif	/* BSD */

/* !!! Oops, is this mt-safe */
char emesgstr[255] = {0};	/* used by all server routines */

const char Error::LOG_DAEMON[] = "/tmp/interserver.log";

void
Error::err_init(char *ident)
{
  openlog(ident, (LOG_PID | LOG_CONS), LOG_DAEMON);
}

void
/*VARARGS1*/
Error::err_quit(char *fmt, ...)
{
  va_list		args;
#ifdef UNIX
  // HP-UX port (old CC): added parameter 'fmt' to va_start macro
  va_start(args, fmt);
#endif
#ifdef WIN32
  va_start(args, fmt);
#endif
  // fmt = va_arg(args, char *);
  vsprintf(emesgstr, fmt, args);
  va_end(args);

  syslog(LOG_ERR, emesgstr);

#ifdef WIN32
  //ExitThread (1);
  ExitProcess (1);
#else
  exit(1);
#endif
}

void
/*VARARGS1*/
Error::err_sys(char *fmt, ...)
{
  va_list		args;
  //char		*fmt;

#ifdef UNIX
  // HP-UX port (old CC): added parameter 'fmt' to va_start macro
  va_start(args, fmt);
#endif
#ifdef WIN32
  va_start(args, fmt);
#endif
  //fmt = va_arg(args, char *);
  vsprintf(emesgstr, fmt, args);
  va_end(args);

  my_perror();
  syslog(LOG_ERR, emesgstr);

#ifdef WIN32
  //ExitThread (1);
  ExitProcess (1);
#else
  exit(1);
#endif
  
}

/*VARARGS1*/
void
Error::err_ret(char *fmt, ...)
{
  va_list		args;
  // char		*fmt;

#ifdef UNIX
  // HP-UX port (old CC): added parameter 'fmt' to va_start macro
  va_start(args, fmt);
#endif
#ifdef WIN32
  va_start(args, fmt);
#endif
  // fmt = va_arg(args, char *);
  vsprintf(emesgstr, fmt, args);
  va_end(args);

  my_perror();
  syslog(LOG_ERR, emesgstr);

}

/*VARARGS1*/
void
Error::err_dump(char *fmt, ...)
{
  va_list		args;
  // char		*fmt;

#ifdef UNIX
  // HP-UX port (old CC): added parameter 'fmt' to va_start macro
  va_start(args, fmt);
#endif
#ifdef WIN32
  va_start(args, fmt);
#endif
  // fmt = va_arg(args, char *);
  vsprintf(emesgstr, fmt, args);
  va_end(args);

  my_perror();
  syslog(LOG_ERR, emesgstr);

  abort();		/* dump core and terminate */
  exit(1);		/* shouldn't get here */
}

void
Error::my_perror()
{
  register int	len;

  len = strlen(emesgstr);
  sprintf(emesgstr + len, " %s", sys_err_str());
}

#ifdef	FUTURE
int	t_errno;	/* in case caller is using TLI, these are "tentative
			   definitions"; else they're "definitions" */
int	t_nerr;
char 	t_errlist[1];
#endif

#ifndef linux
//fredt@users.sourceforge.net
// commented out as declarations exist in stdlib.h (bcc32 v.5.5)
// better include with ifdef where necessary
//extern int	sys_nerr;	/* # of error message strings in sys table */
//extern char	*sys_errlist[];	/* the system error message table */
//end fredt@users.sourceforge.net 
#endif

char *
Error::sys_err_str ()
{
  static char	msgstr[200];

  if (errno != 0) {
    if (errno > 0 && errno < sys_nerr)
      sprintf(msgstr, "(%s)", sys_errlist[errno]);
    else
      sprintf(msgstr, "(errno = %d)", errno);
  } else {
    msgstr[0] = '\0';
  }

#ifdef	FUTURE
  if (t_errno != 0) {
    char	tmsgstr[100];

    if (t_errno > 0 && t_errno < sys_nerr)
      sprintf(tmsgstr, " (%s)", t_errlist[t_errno]);
    else
      sprintf(tmsgstr, ", (t_errno = %d)", t_errno);

    strcat(msgstr, tmsgstr);	/* catenate strings */
  }
#endif

  return(msgstr);
}
