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
// unchar
#include <sys/param.h>

#ifdef SIGTSTP		/* true if BSD system */
#include <sys/file.h>
#include <sys/ioctl.h>
#endif

#ifdef BSD
#include <sys/wait.h>
#endif

#endif //def UNIX

// for exit()
#include <stdlib.h>

#include "Error.h"
#include "NetTCP.h"
#include "JIBSNet.h"
#include "IB_Blob.h"
#include "IB_Array.h"
#include "VersionInformation.h"
#include "CommunicationSQLException.h"

#include "IB_Defines.h" // for debug trace

// ***** interserver.log codes counters ****
// last read code used = 104
// last write code used = 205

void 
JIBSNet::get_BlobData (IB_Blob* blob, IB_SLONG32 blobSize)
{
  IB_SLONG32 segmentSize = MIN (IB_Blob::ibRequestedSegmentSize__, blobSize);

  cdr_.reset ();
#ifdef hpux
  // HP-UX port (old CC): added type cast (int)
  cdr_.grow ((int)segmentSize);
#else
  cdr_.grow (segmentSize);
#endif

  IB_SLONG32 bytesRead;
  IB_SLONG32 toRead;
  IB_SLONG32 written = 0;
  while (written < blobSize) {
    toRead = MIN (segmentSize, blobSize-written);
#ifdef hpux
    // HP-UX port (old CC): added type cast (int)
    bytesRead = NetTCP::readn (sockfd_, (char*) cdr_.next, (int)toRead);
#else
    bytesRead = NetTCP::readn (sockfd_, (char*) cdr_.next, toRead);
#endif
    if (bytesRead <= 0) {
      if (ERRNO == EINTR) {
        ERRNO = 0;		/* assume SIGCLD */
        continue;
      }
      giveup ("JDBC/Net [101]");
      //throw new CommunicationSQLException ();
    }
    else {
      blob->put ((IB_USHORT16) bytesRead, (IB_BUFF_PTR) cdr_.next);
      written += bytesRead;
#ifdef TRACEON
      debugTraceAnInt ("blob bytes read = ", bytesRead);
#endif
    }
  }
}
    
IB_SLONG32
JIBSNet::put_BlobData (IB_Blob* blob)
{
  cdr_.expandBy (IB_Blob::jdbcNetRequestedSegmentSize__);

  blob->get ((IB_BUFF_PTR) cdr_.next); 
  
  return blob->actualJDBCNetSegmentSize ();
}

void 
JIBSNet::sendBlobData (IB_SLONG32 actualSegmentSize)
{
  register int rc;

#ifdef hpux
  rc = NetTCP::writen (sockfd_, (char*) cdr_.next, (int) actualSegmentSize);
  if (rc != actualSegmentSize) 
    giveup ("JDBC/Net [201]");
  cdr_.incrementNext ((int)actualSegmentSize);
#else
  rc = NetTCP::writen (sockfd_, (char*) cdr_.next, actualSegmentSize);
  if (rc != actualSegmentSize) 
    giveup ("JDBC/Net [202]");
  cdr_.incrementNext (actualSegmentSize);
#endif
#ifdef TRACEON
  debugTraceAnInt ("blob bytes written = ", actualSegmentSize);
#endif
}

void
JIBSNet::sendMessage (IB_SLONG32 actualSegmentSize, IB_SLONG32 lastSegment)
{
  register int rc;
  int len = cdr_.next - cdr_.buffer;

  // Header bits must be a multiple of 64 so that subsequent doubles are aligned!
  cdr_.putNativeLongInHeader (cdr_.messageCertificate, (long)VersionInformation::REMOTE_MESSAGE_CERTIFICATE__);
  cdr_.putNativeLongInHeader (cdr_.messageLength, (long) len);
  cdr_.putNativeLongInHeader (cdr_.endOfStream, (long) lastSegment);
  cdr_.putNativeLongInHeader (cdr_.reserved, (long) actualSegmentSize);

  rc = NetTCP::writen (sockfd_, (char*) cdr_.messageCertificate, len+16);
  if (rc != len+16)
    giveup ("JDBC/Net [203]");
#ifdef TRACEON
debugTraceAnInt ("wrote blob message of length = ", len);
debugTraceAnInt ("encoded actual segment size to follow blob message = ", actualSegmentSize);
debugTraceAnInt ("encoded lastSegment = ", lastSegment);
#endif
}

void
JIBSNet::sendMessage (IB_BOOLEAN lastFetch)
{
  register int	rc;
  int len = cdr_.next - cdr_.buffer;

  // Header bits must be a multiple of 64 so that subsequent doubles are aligned!
  cdr_.putNativeLongInHeader (cdr_.messageCertificate, (long)VersionInformation::REMOTE_MESSAGE_CERTIFICATE__);
  cdr_.putNativeLongInHeader (cdr_.messageLength, (long) len);
  cdr_.putNativeLongInHeader (cdr_.endOfStream, (long) lastFetch);
  cdr_.putNativeLongInHeader (cdr_.reserved, (long) 0); 

  rc = NetTCP::writen (sockfd_, (char*) cdr_.messageCertificate, len+16);
  if (rc != len+16)
    giveup ("JDBC/Net [204]");
#ifdef TRACEON
debugTraceAnInt ("wrote message of length = ", len);
debugTraceAnInt ("encoded lastFetch = ", lastFetch);
#endif
}

void
JIBSNet::sendProtocolMessage (IB_SLONG32 byteswap)
{
  register int	rc;
  int  len = cdr_.next - cdr_.buffer;

  // Header bits must be a multiple of 64 so that subsequent doubles are aligned!
  cdr_.putJavaLongInHeader (cdr_.messageCertificate, (long)VersionInformation::REMOTE_PROTOCOL_VERSION__);
  cdr_.putJavaLongInHeader (cdr_.messageLength, (long) len);
  cdr_.putJavaLongInHeader (cdr_.endOfStream, (long) 0);
  cdr_.putJavaLongInHeader (cdr_.reserved, (long) byteswap); 

  rc = NetTCP::writen (sockfd_, (char*) cdr_.messageCertificate, len+16);
  if (rc != len+16)
    giveup ("JDBC/Net [205]");
}

void
JIBSNet::giveup (char* error)
{
  errorLog (error);
#ifdef WIN32
  //ExitThread (1);
  ExitProcess (1);
#else
  exit (1);
#endif
}

void
JIBSNet::sendMessage ()
{
  register int	rc;
  int  len = cdr_.next - cdr_.buffer;

  // Header bits must be a multiple of 64 so that subsequent doubles are aligned!
  cdr_.putNativeLongInHeader (cdr_.messageCertificate, (long)VersionInformation::REMOTE_MESSAGE_CERTIFICATE__);
  cdr_.putNativeLongInHeader (cdr_.messageLength, (long) len);
  cdr_.putNativeLongInHeader (cdr_.endOfStream, (long) 0);
  cdr_.putNativeLongInHeader (cdr_.reserved, (long) 0); 

  rc = NetTCP::writen (sockfd_, (char*) cdr_.messageCertificate, len+16);
  if (rc != len+16)
    giveup ("JDBC/Net [200]");
#ifdef TRACEON
debugTraceAnInt ("wrote message of length = ", len);
#endif
}

// Read an integer from the wire
long
JIBSNet::getLong ()
{
  register int nbytes;
  char tempBuff[4];
  long value = 0;

again:
  if ( (nbytes = NetTCP::readn (sockfd_, (char *) tempBuff, sizeof (long))) <= 0) {
    if (ERRNO == EINTR) {
      ERRNO = 0;		/* assume SIGCLD */
      goto again;
    }
    // Note: this error is only valid as long as a Long is the first thing read for every message,
    // which should be the case for message certificate.
    giveup ("JDBC/Net [100]");
    //throw new CommunicationSQLException ();
  }
  if (nbytes != sizeof (long)) {
    giveup ("JDBC/Net [102]");
    //throw new CommunicationSQLException ();
  }

  value += (tempBuff[0] & 0xff);
  value += (tempBuff[1] & 0xff) << 8;
  value += (tempBuff[2] & 0xff) << 16;
  value += (tempBuff[3] & 0xff) << 24;
  return value;
}

void
JIBSNet::recvMessage () 
{
  register int nbytes;
  long templen = 0;	

  if (getLong () != VersionInformation::REMOTE_MESSAGE_CERTIFICATE__)
    throw new CommunicationSQLException ();

  templen = getLong ();

  cdr_.next = cdr_.buffer;
/* fredt@users.sourceforge.net
  if (!cdr_.buffer) // !!!what is this about?
*/
  if (cdr_.buffer == NULL)
    cdr_.grow (0);

  if (templen > cdr_.length)
#ifdef hpux
        // HP-UX port (old CC): added type cast (size_t)
	cdr_.grow ((size_t)templen );
#else
	cdr_.grow (templen );
#endif

  // The following line is to validate 
  // overflow of buffers on get sequence
  // should be the whole length and not what's after filling 
  cdr_.remaining = cdr_.length;  
  // !! need to rewrite as readFully
again:
#ifdef hpux
  // HP-UX port (old CC): added type cast (int)
  if ((nbytes = NetTCP::readn (sockfd_, (char*) cdr_.buffer, (int)templen)) <= 0) {
#else
  if ((nbytes = NetTCP::readn (sockfd_, (char*) cdr_.buffer, templen)) <= 0) {
#endif
    if (ERRNO == EINTR) {
      ERRNO = 0;		/* assume SIGCLD */
      goto again;
    }
    giveup ("JDBC/Net [103]");
    //throw new CommunicationSQLException ();
  }
  if (nbytes != templen) {
    giveup ("JDBC/Net [104]");
    //throw new CommunicationSQLException ();
  }

#ifdef TRACEON
debugTraceAnInt ("received a message of length = ", nbytes);
#endif
  // nbytes is the actual length of the message */
}

