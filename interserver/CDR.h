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
#ifndef	_JIBSNET_CDR_H_
#define	_JIBSNET_CDR_H_

#include <string.h>

#ifdef TRACEON
void debugTraceALine (char* where, char* what);
void debugTraceAnInt (char* where, int what);
#endif

#ifdef WIN32
#include <fcntl.h>
#include <io.h>
#include <process.h>
#include <signal.h>
#include <malloc.h>
#endif // WIN32

#ifdef UNIX
#define SIZEOF_LONG_LONG  8
#endif

// Note: Like Java, CDR specifies a fixed representation for 
// the underlying platform supported types.
// CDR is independent of database types.
// CDR does not depend on any other class in JIBSNet.

// Implements a subset of CDR for the following data types:
// 	char
//	byte
// 	short
//	long
//	float
//	double
//	
//	No unicode support  --- just ISO LATIN/1  
//	assumes IEEE float
//	assumes 2s complement integers
//
//	The CDR byte ordering scheme is not implemented on a per packet
//	basis. It is determined at connection establishment
//	all data "to" the wire uses native byte ordering 

#if SIZEOF_BOOL != 0
  typedef bool Boolean;
  static const Boolean CDR_FALSE = false;
  static const Boolean CDR_TRUE = true;
#else	// "bool" not builtin to this compiler
  typedef int Boolean;
  enum { CDR_FALSE = 0, CDR_TRUE = 1 };
#endif	// "bool" not builtin

typedef short Short;
typedef unsigned short UShort;

#if SIZEOF_LONG == 4
  typedef long Long;
  typedef unsigned long ULong;
#else
  // just assume "int" is 4 bytes long ...
  typedef int Long;
  typedef unsigned ULong;
#endif	// SIZEOF_LONG != 4

#if SIZEOF_LONG_LONG == 8
  typedef long long LongLong;
  typedef unsigned long long ULongLong;
#elif SIZEOF_LONG == 8
  typedef long LongLong;
  typedef unsigned long ULongLong;
#elif defined (_MSC_VER) && _MSC_VER >= 900
  typedef __int64 LongLong;
  typedef unsigned __int64 ULongLong;
#else
//
// If "long long" isn't native, programs can't use these
// data types in normal arithmetic expressions.  If any
// particular application can cope with the loss of range
// it can define conversion operators itself.
//
  #define NONNATIVE_LONGLONG
  #if defined (WORDS_BIGENDIAN)
    struct LongLong { Long h, l; };
    struct ULongLong { ULong h, l; };
  #else
    struct LongLong { Long l, h; };
    struct ULongLong { ULong l, h; };
  #endif	// !WORDS_BIGENDIAN
#endif	// no native 64 bit integer type

typedef float Float;
typedef double Double;

typedef unsigned char Char;
typedef Char *String;
typedef Char *Ptr;

// WORDS_BIGENDIAN is passed by make
// since byte order is determined at runtime in 
// JIBSRemote::establishProtocol .... can get rid of this except 
// .. for LongLong definition used by CDR
#if defined (WORDS_BIGENDIAN)
  #define BIG_ENDIAN
  #define MY_BYTE_SEX	0
#else
  #define LITTLE_ENDIAN
  #define MY_BYTE_SEX	1
#endif

// always BIG_ENDIAN
#define JAVA_BYTE_ORDER 0

//
// Type for doing arithmetic on pointers ... as elsewhere, we assume
// that "unsigned" versions of a type are the same size as the "signed"
// version of the same type.
//

#if SIZEOF_VOID_P == SIZEOF_INT
  typedef unsigned int ptr_arith_t;

#elif SIZEOF_VOID_P == SIZEOF_LONG
  typedef unsigned long ptr_arith_t;

#elif SIZEOF_VOID_P == SIZEOF_LONG_LONG
  typedef unsigned long long ptr_arith_t;

#else
#	error "Can't find a suitable type for doing pointer arithmetic."
#endif


//
// The core marshaling primitive:  a memory buffer, into which all the basic
// CDR datatypes can be placed ... or from which they can be retreived.
//
struct CDR {
  //
  // Define these constants as enums to ensure they get inlined
  // and to avoid pointless static memory allocations.
  //
  enum {
    //
    // Constants defined by the CDR protocol.  

    SHORT_SIZE = 2,
    LONG_SIZE = 4,
    LONGLONG_SIZE = 8,
    DOUBLE_SIZE = 8,

    MAX_ALIGNMENT = 8,		// actually 16, for strictest alignment
    // as specified by CDR
    DEFAULT_BUFSIZE = 2048      // 1430 if Ethernet MTU, less headers
    // need to be able to configure this
  };

  //
  // ENCODING SUPPORT ... adjust pointers as needed, then store in the
  // native byte order.
  //

  Boolean expandBy (int numBytes);

  void incrementNext (int numBytes);

  Boolean put_byte (Char c);
  Boolean put_short (Short s);
  Boolean put_long (Long l);
  Boolean put_longlong (const LongLong &ll);

  Boolean put_string (const Short len, const String s); // not null terminated
  Boolean put_ldstring (const Short len, const String data);

  Boolean put_bytes (const Short l, const Ptr b);

  inline Boolean put_char (const Char c)
  { return put_byte ((Char) c); }
    
  inline Boolean put_boolean (const Boolean b)
  { return put_byte ((Char) (b != CDR_FALSE)); }

  inline Boolean put_ushort (const UShort s)
  { return put_short ((Short) s); }

  inline Boolean put_ulong (const ULong l)
  { return put_long ((Long) l); }
				    
  inline Boolean put_float (const Float f)
  { return put_long (*(Long *) &f); }

  /*
  inline Boolean put_double (const Double &d)
  { return put_longlong (*(LongLong *) &d); }
  */

  Boolean put_double (const Double &d);

  //
  // DECODING SUPPORT ... same assumptions are made as above, but a
  // flag is tested to determine whether decode should byteswap or not.
  // It's cheaper to do it that way than to use virtual functions.
  //

  Char get_byte ();

  Short get_short ();

  Long get_long ();

  LongLong get_longlong ();

  String get_string ();
  String get_string (Short &l);

  Ptr get_bytes ();
  Ptr get_bytes (Short &l);

  inline Char get_char ()
  { return get_byte (); }

  inline Boolean get_boolean ()
  {
    Char c;

    //
    // Boolean is rarely 'char'
    //
    c = get_char ();
    Boolean b = (c == 1);
    return b;
  }

  inline UShort get_ushort ()
  { return (UShort)get_short (); }

  inline ULong get_ulong ()
  { return (ULong) get_long ();}

  inline Float get_float ()
  { 
    Float f;
    Long l = get_long ();
    memcpy ((void*) &f, (void*) &l, sizeof (Float));
    //!!!strncpy ((char *) &f, (char *) &l, sizeof (Float));
    return f;
  }

  inline Double get_double ()
  { 
    LongLong ll;
    Double d;
    ll = get_longlong ();
    memcpy ((void*) &d, (void*) &ll, sizeof (Double));
    //!!!strncpy ((char *)&d, (char *)&ll, sizeof(Double));
    return d; 
  }

  CDR (unsigned char *buf = 0,
       unsigned len = 0,
       int byte_order = JAVA_BYTE_ORDER,
       int consume_buf = 0);

  ~CDR ()
  { 
#ifdef TRACEON
    debugTraceALine ("Deallocating CDR buffer", "");
#endif
    if (do_free) delete real_buffer; 
  }

  void *operator new (size_t, void *p)
  { return p; }

  void *operator new (size_t s)
  { return ::operator new (s); }

  void operator delete (void *p)
  { ::operator delete (p); }

  void putJavaLongInHeader (unsigned char* headerSlot, long value);
  void putNativeLongInHeader (unsigned char* headerSlot, long value);

  //
  // Used mostly when interpreting typecodes.  These may change the
  // state of a CDR buffer even when errors are reported.
  //
  Boolean skip_string ();
  Boolean skip_bytes (unsigned nbytes)
  {
    if (remaining < nbytes)
      return CDR_FALSE;
    remaining -= nbytes;
    next += nbytes;
    return CDR_TRUE;
  }

  //
  // Grow the buffer to the identified size ... if it's zero, just
  // grow it by a standard quantum (e.g. when encoding we can't know
  // in advance how big it will need to become).
  //
  Boolean grow (size_t newlength);

  //
  // Some code needs to know how much is left on encode or decode
  //
  size_t bytes_remaining () 
  { return remaining; }

  //
  // reset buffer to start building a new unit of data
  //
  void reset ()
  {
    next = buffer;
    remaining = length;
  }

  // private:
  //
  //  DATA MEMBERS ...
  //
  unsigned char *next;		// next data goes here
  size_t remaining;	// space left

  unsigned char *real_buffer;	// maybe not aligned
  int do_free;

  unsigned char *buffer;
  size_t length;

  int do_byteswap;	// decode ONLY

  // 16-byte header, write-back areas
  unsigned char* messageCertificate;
  unsigned char* messageLength; // not including the 16-byte header
  unsigned char* endOfStream; 
  unsigned char* reserved;

};

#endif
