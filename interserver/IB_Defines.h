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
#ifndef _IB_DEFINES_H_
#define _IB_DEFINES_H_

#include <ibase.h>

typedef int IB_REF;

//******************Character data**************************
typedef char IB_CHAR;
typedef char* IB_STRING; 
typedef char* IB_LDSTRING;        // first signed short is length delimiter

//******************ISC structured data*********************
// All structured data passed/returned from isc is char*
typedef char IB_BUFF_CHAR;
typedef IB_BUFF_CHAR * IB_BUFF_PTR;

//******************Timestamps, Blobs, and Arrays***********
typedef ISC_QUAD IB_TIMESTAMP;
typedef ISC_QUAD IB_BLOBID;
typedef ISC_QUAD IB_ARRAYID;

#define IS_NULL_ISC_QUAD(A) ((A.isc_quad_high == 0) && (A.isc_quad_low == 0))
#define IS_NULL_BLOBID IS_NULL_ISC_QUAD
   

// HP-UX port (old CC): does not know 'signed'
#ifdef hpux
#define signed
#else
#define signed        signed
#endif

//*****************Fixed Size Numeric data******************
#define IB_SBYTE      signed char   // java byte (8 bit signed integer)
#define IB_UBYTE      unsigned char
#define IB_SSHORT16   signed short  // java short (16 bit signed integer)
#define IB_USHORT16   unsigned short
#define IB_DOUBLE64   double        // java double
#define IB_FLOAT32    float         // java float

#ifdef ARCH_32
#define IB_SLONG32    signed long   // java int (32 bit signed integer)
#define IB_ULONG32    unsigned long
#define IB_SLONG64    signed long long // java long (64 bit signed integer)
#define IB_ULONG64    unsigned long long
#endif

#ifdef ARCH_64
#define IB_SLONG32    signed int    // java int (32 bit signed integer)
#define IB_ULONG32    unsigned int
#define IB_SLONG64    signed long   // java long (64 bit signed integer)
#define IB_ULONG64    unsigned long
#endif

// CJL-IB6 support for new types
//******** New InterBase 6.0 types ***********
#define IB_SINT64     ISC_INT64  

// For local numeric data that does not
// 1) go into/come from the network
// 2) go into/come from an isc call
// 3) go into/come from an IB system table
// You may as well use the native architecture's word size
// (to avoid masking).
#ifdef ARCH_32
#define IB_INT        signed int
#endif

#ifdef ARCH_64
#define IB_INT        signed long
#endif

//******************Non-fixed Numeric data*****************
// Note: IB_BOOLEAN must be a signed type or else IB_TRUE and IB_FALSE
// cannot be written to the wire using CDR
typedef unsigned char IB_BOOLEAN;
const IB_BOOLEAN IB_TRUE = 1;
const IB_BOOLEAN IB_FALSE = 0;
#define TO_BOOLEAN(x) ((x) ? 1 : 0)

//*****************Miscellaneous*******************************

#define NULL 0
#define NULL_CHAR '\0'

// HP-UX port (old CC): the following line produces compile time
//	error message - line 28: warning 2001: Redefinition of macro MI
#define MIN(x,y) ((x)<(y)) ? (x) : (y)

#include <string.h>

// preship: turn off TRACEON before ship!!!
//define TRACEON
#ifdef TRACEON
void debugTraceALine (char* where, char* what);
void debugTraceAnInt (char* where, int what);
#endif

void errorLog (char* what);

#endif
