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
#ifndef _JIBSNET_H_
#define _JIBSNET_H_

// strlen()
#include <string.h>
 
#include "IB_Defines.h"
#include "MessageCodes.h"
#include "IB_LDString.h"
#include "IB_LDBytes.h"
#include "CDR.h"

#ifdef WIN32
#define	MAXHOSTNAMELEN	  64	/* max size of a host name */
#endif

#if UNIX
// MAXHOSTNAMELEN for SYS5 systems
#include <netdb.h>
#endif

// This is the socket-aware layer for sending and receiving
// InterBase data.
// Notice that it is not JIBSRemote-aware.
// This layer is network aware.
// That is, it knows how to put data on the wire.
// It controls the data representations used on the wire, and 
// manages buffers used for transferring data to and from the
// JIBSRemote layer.
// This layer is message-unaware.
// That is, it is unaware of the content or format of remote messages.

class IB_Blob;
class IB_Array;

class JIBSNet {

private:

  // CDR contains buffers of platform-independent (fixed type) network data.
  // CDR has no knowledge of InterBase types.
  CDR cdr_;

  // Network-aware component.
  int sockfd_;
  
  // Note: this signature value must match that in interclient/JIBCNet.java
  // Used to verify that all messages received are interclient
  // messages using this remote interface version.
  enum {
    versionStringLength__ = 10
  };

  // For future use:
#ifndef SYS5
#define	MAXHOSTNAMELEN	  64	/* max size of a host name */
#endif
  char   client [MAXHOSTNAMELEN];
  char   version [versionStringLength__];
  // !!! what is unchar?
  unsigned char protocol;
  unsigned char architecture;
  unsigned char compression;
  unsigned char security;

public:

  JIBSNet (int sockfd);

  void recvMessage ();

  void resetMessage ();

  void sendMessage ();

  void sendTerminalMessage ();

  void sendProtocolMessage (IB_SLONG32 byteswap);

  void sendMessage (IB_BOOLEAN lastFetch);

  void sendMessage (IB_SLONG32 actualSegmentSize, IB_SLONG32 lastFetch);

  // IB_defines data type dependent put/get methods follow.
  // Notice that type mappings are usually accomplished
  // with a simple cast.  
  // See IB_Defines.h for InterBase types.

  // Never put/get unsigned data on the wire!!


  // *******************parameterized GET routines***********************

  void get_blobId (IB_BLOBID& value);

  void get_arrayId (IB_ARRAYID& value);

  void get_timestamp (IB_TIMESTAMP& value);

  void get_ldstring (IB_LDString& value);

  void get_ldbytes (IB_LDBytes& value);

  void get_BlobData (IB_Blob* blob, IB_SLONG32 actualSegmentSize);

  // HP-UX port (old CC): added 'inline'
  inline
  void get_quad (ISC_QUAD& quad);

  // *******************functional GET routines***********************
  ISC_QUAD get_quad ();

  IB_BLOBID get_blobId ();

  IB_ARRAYID get_arrayId ();

  IB_TIMESTAMP get_timestamp ();

  IB_BOOLEAN get_boolean ();

  IB_SBYTE get_sbyte ();

  IB_SSHORT16 get_sshort16 ();

  IB_SLONG32 get_slong32 ();

  IB_DOUBLE64 get_double64 ();

  IB_FLOAT32 get_float32 ();

  IB_LDString get_ldstring ();

  IB_LDBytes get_ldbytes ();

  IB_STRING get_string ();

  IB_REF get_ref ();

// CJL-IB6 support for new types
	IB_SINT64 get_sint64 ();

	IB_ULONG32 get_ulong32 ();
// CJL-IB6 end change

  // *******************PUT routines***********************

  void put_success ();

  void put_failure ();

  void put_code (const MessageCodes::Opcode opcode);

  void put_code (const MessageCodes::Delimiter delimeter);

  void put_blobId (const IB_BLOBID blobId); 

  void put_arrayId (const IB_ARRAYID arrayId);

  void put_timestamp (const IB_TIMESTAMP timestamp);

  void put_boolean (const IB_BOOLEAN value);

  void put_sbyte (const IB_SBYTE value);

  void put_sshort16 (const IB_SSHORT16 value);

  void put_slong32 (const IB_SLONG32 value);

  void put_double64 (const IB_DOUBLE64 value);

  void put_float32 (const IB_FLOAT32 value);

// CJL-IB6 support for new types
	void put_sint64 (const IB_SINT64 value);

  void put_ulong32 (const IB_ULONG32 value);
// CJL-IB6 end change

  // not null terminated
  void put_ldstring (const IB_LDString value);

  // void put_ldstring (const IB_LDSTRING value);

  // not null terminated
  void put_ldstring (const IB_SSHORT16 len, 
		     const char* string);

  // for null terminated strings, strlen is called
  void put_string (const char* string);

  // string is not null-terminated
  void put_string (const IB_SSHORT16 len, 
		   const char* string);

  void put_ldbytes (const IB_LDBytes value);

  void put_ref (const IB_REF ref);

  IB_SLONG32 put_BlobData (IB_Blob* blob);

  void sendBlobData (IB_SLONG32 actualSegmentSize);

  // HP-UX port (old CC): added 'inline'
  inline
  void put_quad (const ISC_QUAD quad);

private:

  // Get/Put a long integer directly from/onto the wire (bypass CDR buffer)
  void putLong (long value);
  void putNativeLong (long value);
  long getLong ();
  void giveup (char* errorLog);
};

inline
JIBSNet::JIBSNet (int sockfd)
  : sockfd_ (sockfd)
{ }

inline 
void
JIBSNet::resetMessage ()
{ cdr_.reset(); }

inline
void
JIBSNet::get_blobId (IB_BLOBID& value)
{ get_quad ((ISC_QUAD&) value); }

inline
void
JIBSNet::get_arrayId (IB_ARRAYID& value)
{ get_quad ((ISC_QUAD&) value); }

inline
void
JIBSNet::get_timestamp (IB_TIMESTAMP& value)
{ get_quad ((ISC_QUAD&) value); }

inline
void
JIBSNet::get_quad (ISC_QUAD& quad)
{ 
//  Char* p = (Char*) &quad;
//  for (int i=0; i<8; i++)
//    *p++ = cdr_.get_byte ();
    quad.isc_quad_high = cdr_.get_long ();
    quad.isc_quad_low = cdr_.get_long ();
}

inline
void
JIBSNet::get_ldstring (IB_LDString& value)
{ value.string_ = (IB_STRING)cdr_.get_string ((Short&) value.length_); }

inline
void
JIBSNet::get_ldbytes (IB_LDBytes& value)
{ value.value_ = (IB_BUFF_PTR)cdr_.get_bytes ((Short&) value.length_); }

// *******************functional GET routines***********************
inline
ISC_QUAD
JIBSNet::get_quad ()
{
  ISC_QUAD quad;
//   Char* p = (Char*) &quad;
//  for (int i=0; i<8; i++)
//    *p++ = cdr_.get_byte ();
  quad.isc_quad_high = cdr_.get_long ();
  quad.isc_quad_low = cdr_.get_long ();
  return quad;
}

inline
IB_BLOBID 
JIBSNet::get_blobId ()
{ 
  return (IB_BLOBID)get_quad();
}

inline
IB_ARRAYID 
JIBSNet::get_arrayId ()
{ 
  return (IB_ARRAYID)get_quad();
}

inline
IB_TIMESTAMP 
JIBSNet::get_timestamp ()
{ 
  return (IB_TIMESTAMP)get_quad();
}

inline
IB_BOOLEAN 
JIBSNet::get_boolean ()
{ 
  return (IB_BOOLEAN) cdr_.get_boolean ();
}

inline
IB_SBYTE 
JIBSNet::get_sbyte ()
{ 
  return (IB_SBYTE) cdr_.get_byte ();
}

inline
IB_SSHORT16 
JIBSNet::get_sshort16 ()
{ 
  return (IB_SSHORT16) cdr_.get_short ();
}

inline
IB_SLONG32 
JIBSNet::get_slong32 ()
{ 
  return (IB_SLONG32) cdr_.get_long ();
}

// CJL-IB6 support for new types
inline
ISC_INT64
JIBSNet::get_sint64 ()
{
  return (IB_SINT64) cdr_.get_longlong ();
}

inline
IB_ULONG32
JIBSNet::get_ulong32 ()
{ 
	return (IB_ULONG32) cdr_.get_ulong ();
}
// CJL-IB6 end change

inline
IB_DOUBLE64 
JIBSNet::get_double64 ()
{ 
  return (IB_DOUBLE64) cdr_.get_double ();
}

inline
IB_FLOAT32 
JIBSNet::get_float32 ()
{ 
  return (IB_FLOAT32) cdr_.get_float ();
}

inline
IB_LDString 
JIBSNet::get_ldstring ()
{ 
  Short length;
  IB_STRING str = (IB_STRING)cdr_.get_string ((Short&) length); 
  return IB_LDString (length, str);
}

inline
IB_STRING 
JIBSNet::get_string ()
{ 
  return (IB_STRING) cdr_.get_string ();
}

inline
IB_LDBytes 
JIBSNet::get_ldbytes ()
{ 
  IB_LDBytes value;
  value.value_ = (IB_BUFF_PTR)cdr_.get_bytes ((Short&) value.length_); 
  return (value);
}

inline
IB_REF 
JIBSNet::get_ref ()
{ 
  return (IB_REF) cdr_.get_long ();
}


// *******************PUT routines***********************

inline
void
JIBSNet::put_code (const MessageCodes::Opcode opcode)
{ cdr_.put_byte ((Char) opcode); }

inline
void
JIBSNet::put_code (const MessageCodes::Delimiter delimeter)
{ cdr_.put_byte ((Char) delimeter); }

inline
void
JIBSNet::put_blobId (const IB_BLOBID blobId) 
{ put_quad ((ISC_QUAD) blobId); }

inline
void
JIBSNet::put_arrayId (const IB_ARRAYID arrayId)
{ put_quad ((ISC_QUAD) arrayId); }

inline
void
JIBSNet::put_timestamp (const IB_TIMESTAMP timestamp)
{ put_quad ((ISC_QUAD) timestamp); }

inline
void
JIBSNet::put_boolean (const IB_BOOLEAN value)
{ cdr_.put_boolean ((Boolean) value); }

inline
void
JIBSNet::put_sbyte (const IB_SBYTE value)
{ cdr_.put_byte ((Char) value); }

inline
void
JIBSNet::put_sshort16 (const IB_SSHORT16 value)
{ cdr_.put_short ((Short) value); }

inline
void
JIBSNet::put_slong32 (const IB_SLONG32 value)
{ cdr_.put_long ((Long) value); }

// CJL-IB6 support for new type
inline
void
JIBSNet::put_ulong32 (const IB_ULONG32 value)
{ cdr_.put_ulong ((ULong) value); }

inline
void
JIBSNet::put_sint64 (const IB_SINT64 value)
{ cdr_.put_longlong ((LongLong) value); }
// CJL-IB6 end change

inline
void
JIBSNet::put_double64 (const IB_DOUBLE64 value)
{ cdr_.put_double ((Double) value); }

inline
void
JIBSNet::put_float32 (const IB_FLOAT32 value)
{ cdr_.put_float ((Float) value); }

inline
void
JIBSNet::put_ldstring (const IB_LDString value)
{ cdr_.put_ldstring ((Short) value.length_,
		     (String) value.string_); }

/*
inline
void
JIBSNet::put_ldstring (const IB_LDSTRING value)
{ cdr_.put_ldstring (*(*IB_SSHORT16) value,
		     value + sizeof (IB_SSHORT16)); }
*/

inline
void
JIBSNet::put_ldstring (const IB_SSHORT16 len, 
		       const char* string)
{ cdr_.put_ldstring (len, (String) string); }

// !!! is this ever called, delete if not
inline
void
JIBSNet::put_string (const char* string)
{ cdr_.put_string (strlen (string), (String) string); }

// !!! is this ever called, delete if not
inline
void
JIBSNet::put_string (const IB_SSHORT16 len, 
		     const char* string)
{ cdr_.put_string (len, (String) string); }

inline
void
JIBSNet::put_ldbytes (const IB_LDBytes value)
{ cdr_.put_bytes ((Short) value.length_,
		   (Ptr) value.value_); }

inline
void
JIBSNet::put_ref (const IB_REF ref)
{ cdr_.put_long ((Long) ref); }

inline
void
JIBSNet::put_quad (const ISC_QUAD quad)
{ 
//  Char* p = (Char*) &quad;
//  for (int i=0; i<8; i++)
//    cdr_.put_byte (*p++);

  // HP-UX port (old CC): added two type casts to Long
  cdr_.put_long ((Long) quad.isc_quad_high);
  cdr_.put_long ((Long) quad.isc_quad_low);
}

#endif



