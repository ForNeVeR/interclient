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
//	Implements a subset of CDR
//
//	char				 8 bits (1 byte)
//	short, unsigned short		 16 bits (2 bytes)
//	long, unsigned long, float	 32 bits (4 bytes)
//	double				 64 bits (8 bytes)
//

// UINT_MAX
#include <limits.h>

// memcpy()
#include <string.h>

// assert()
#include <assert.h>

// malloc(), free()
#include <stdlib.h>

#include "CDR.h"

  //
  // Constructor ... buffer must be aligned for the strictest
  // CDR alignment requirement, since the algorithms used here
  // only maintain alignment with respect to &buffer [0].
  //
  // Yes, that complicates the grow() primitive.
  //
CDR::CDR (unsigned char *buf,
	  unsigned len,
	  int byte_order,
	  int consume_buf)
  : real_buffer (buf),
    do_free (consume_buf),
    do_byteswap (byte_order != MY_BYTE_SEX)
{
#ifdef TRACEON
  debugTraceAnInt ("Allocating new CDR buffer of length ", len);
#endif

  ptr_arith_t temp = (ptr_arith_t) buf;

  temp += MAX_ALIGNMENT - 1;
  temp &= ~((ptr_arith_t) MAX_ALIGNMENT - 1);

  buffer = (unsigned char *) temp;

  messageCertificate = buffer;
  messageLength = buffer+4;
  endOfStream = buffer+8;
  reserved = buffer+12;
  buffer += 16; // 16*8 = 128 bit header

  next = buffer;

  if (len > (unsigned) (buffer - real_buffer))
    length = remaining = len - (unsigned) (buffer - real_buffer);
  else
    length = remaining = 0;
}

//
// Grow the CDR buffer, either to a known size (incoming message) or
// by a standard increment (creating outgoing message).
//
// NOTE:  this code knows about what's involved in the constructor and
// destructor, as it needs to invoke the constructor and do what the
// destructor would do (and not in the normal order).  It also knows
// all other state that's significant.  Change with care!
//
//
Boolean
CDR::grow (size_t newsize)
{
  unsigned char	*old_realbuf, *oldMessageCertificate;
  size_t		offset;
  int			old_do_swap = do_byteswap;
  size_t oldLength;

#ifdef TRACEON
  debugTraceAnInt ("Growing CDR buffer to ", newsize);
#endif

  oldLength = length; // don't mempy if this is 0.

  //
  // Iff old buffer was heap allocated, it gets freed soon.  In any case,
  // we need to know which bytes that have been marshaled or read thus
  // far, so they'll also be in the newly grown buffer.
  //
  if (do_free)
    old_realbuf = real_buffer;
  else
    old_realbuf = 0;
  oldMessageCertificate = messageCertificate;
// fredt@users.sourceforge.net temporary check
#ifdef TRACEON
  debugTraceAPointer ("next ", next);
  debugTraceAPointer ("messageCertificate ", messageCertificate);
#endif
/* fredt@users.sourceforge.net
  ambiguous comparison of signed and unsigned ( UNIT_MAX is often an unsigned type )
  aborts program on first call with next = 16 and messageCertificate = 0

  assert ((next - messageCertificate) < UINT_MAX);
*/

  offset = (unsigned) (next - messageCertificate);

  //
  // Calculate the new buffer's length; if growing for encode, we
  // don't grow in "small" chunks because of the cost.
  //
  size_t new_len;

  if (newsize == 0) {
    if (length < 4096)
      new_len = length + 4096;
    else
      new_len = length * 2;
  } 
  else if (newsize <= length) {
    return CDR_TRUE;
  } 
  else if ((newsize-length) < 4096) { // grow by at least 4k, -po
    new_len = length + 4096;
  } 
  else
    new_len = newsize;

  //
  // Get a new buffer that's adequately aligned, and use it to
  // reinitialize ourselves with the "free this buffer later" flag.
  //
  unsigned char	*new_buffer;

  new_len += MAX_ALIGNMENT; // actually MAX_ALIGNMENT-1, but add 1 byte for good measure
  new_len += 16; // 16 byte header size
  if ((new_buffer = (unsigned char *) malloc (new_len)) == 0)
    return CDR_FALSE;

  (void) new (this) CDR (new_buffer, new_len, JAVA_BYTE_ORDER, 1);

  //
  // Now restore all the relevant old state that we saved earlier,
  // and free the original buffer if needed.  (The first buffer is
  // normally stack-allocated and so mustn't be freed this way.)
  //
  // !!!RRR !!! do_byteswap = old_do_swap;

  if (oldLength) { // don't memcpy if previous length was zero
    memcpy (messageCertificate, oldMessageCertificate, offset);
    skip_bytes (offset-16);
  }

  if (old_realbuf)
    free ((char *) old_realbuf);
    
  return CDR_TRUE;
}

// Efficiently align "value" up to "alignment", knowing that all such
// boundaries are binary powers and that we're using two's complement
// arithmetic.
//
static inline ptr_arith_t
align_binary (const ptr_arith_t value, 
	      size_t alignment)
{
  ptr_arith_t		temp = alignment - 1;

  return (value + temp) & ~temp;
}

// Efficiently round "ptr" up to an "alignment" boundary, knowing that
// all such boundaries are binary powers and that we're using two's
// complement arithmetic.
//
static inline unsigned char *
ptr_align_binary (const unsigned char *ptr, 
		  size_t alignment)
{
  return (unsigned char *) align_binary ((ptr_arith_t) ptr, alignment);
}

// ENCODING routines ... pad, store.  Never swap.  Padding uses
// whatever value is already in the buffer.
//
Boolean
CDR::put_byte (Char c)
{
  if (remaining < sizeof (char) && (grow (0) == CDR_FALSE))
    return CDR_FALSE;

  *next++ = (unsigned char) c;
  remaining--;
  return CDR_TRUE;
}


Boolean
CDR::put_short (Short s)
{
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes; maybe
  // grow the buffer if there's not enough left
  //
  tmp_next = ptr_align_binary (next, SHORT_SIZE);
  temp = SHORT_SIZE + (tmp_next - next);
  if (temp > remaining) {
    if (grow (0) == CDR_FALSE)
      return CDR_FALSE;
    tmp_next = next + temp - SHORT_SIZE;
  }
  remaining -= temp;

  //
  // copy the half word, native byte order
  //
  *(Short *)tmp_next = s;
  next = tmp_next + SHORT_SIZE;
  return CDR_TRUE;
}

Boolean
CDR::put_long (Long l)
{
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes; maybe
  // grow the buffer if there's not enough left
  //
  tmp_next = ptr_align_binary (next, LONG_SIZE);
  temp = LONG_SIZE + (tmp_next - next);
  if (temp > remaining) {
    if (grow (0) == CDR_FALSE)
      return CDR_FALSE;
    tmp_next = next + temp - LONG_SIZE;
  }
  remaining -= temp;

  //
  // copy the word, native byte order
  //
  *(Long *)tmp_next =  l;

  next = tmp_next + LONG_SIZE;
  return CDR_TRUE;
}

// added for problem with doubles in new jdk 1.1
Boolean
CDR::put_double (const Double& d)
{
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes; maybe
  // grow the buffer if there's not enough left
  //
  tmp_next = ptr_align_binary (next, DOUBLE_SIZE);
  temp = DOUBLE_SIZE + (tmp_next - next);
  if (temp > remaining) {
    if (grow (0) == CDR_FALSE)
      return CDR_FALSE;
    tmp_next = next + temp - DOUBLE_SIZE;
  }
  remaining -= temp;

  //
  // copy the word, IEEE 754
  //
  *(Double *) tmp_next =  d;

  next = tmp_next + DOUBLE_SIZE;
  return CDR_TRUE;
}

Boolean
CDR::put_longlong (const LongLong &ll)
{
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes; maybe
  // grow the buffer if there's not enough left
  //
  tmp_next = ptr_align_binary (next, LONGLONG_SIZE);
  temp = LONGLONG_SIZE + (tmp_next - next);
  if (temp > remaining) {
    if (grow (0) == CDR_FALSE)
      return CDR_FALSE;
    tmp_next = next + temp - LONGLONG_SIZE;
  }
  remaining -= temp;

  //
  // copy the double word in "native" byte order.
  //
  *(LongLong *) tmp_next = ll;
  next = tmp_next + LONGLONG_SIZE;
  return CDR_TRUE;
}

Boolean
CDR::put_string (const Short len, const String data)
{
    Char *tmp = (Char *)data;
    int i;
    for (i = 0; i < len; i++)
      put_char (*tmp++);
    // put_char ((Char) 0); !!! remove
    return CDR_TRUE;
}

Boolean
CDR::put_ldstring (const Short len, const String data)
{
    Char *tmp = (Char *)data;
    int i;
    put_short (len);
    for (i = 0; i < len; i++)
      put_char (*tmp++);
    // put_char ((Char) 0); !!! remove
    return CDR_TRUE;
}

Boolean
CDR::put_bytes (const Short len, const Ptr data)
{
    Char *tmp = (Char *)data;
    int i;
    put_short (len + 1);
    for (i = 0; i < len; i++)
      put_char (*tmp++);
    put_char ((Char) 0);
    return CDR_TRUE;
}

//
// DECODING routines ... adjust pointer, then byteswap as needed.
//

Char
CDR::get_byte ()
{
  Char c;
  if (remaining < sizeof (char))
    return CDR_FALSE;

  c = (char) *next++;
  remaining--;
  return c;
}


Short
CDR::get_short ()
{
  Short s;
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes
  //
  tmp_next = ptr_align_binary (next, SHORT_SIZE);
  temp = SHORT_SIZE + (tmp_next - next);
  if (temp > remaining)
    return CDR_FALSE;
  remaining -= temp;

  //
  // decode halfword, swapping as needed
  //
  if (!do_byteswap) {
    s = *(Short *)tmp_next;
    next = tmp_next + SHORT_SIZE;
  } else {
    register unsigned char	*sp = (unsigned char *) &s;

    sp [1] = *tmp_next++;
    sp [0] = *tmp_next++;
    next = tmp_next;
  }
  return s;
}

Long
CDR::get_long (
	       )
{
  Long l;
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes
  //
  tmp_next = ptr_align_binary (next, LONG_SIZE);
  temp = LONG_SIZE + (tmp_next - next);
  if (temp > remaining)
    return CDR_FALSE;
  remaining -= temp;

  //
  // decode word, swapping as needed
  //
  if (!do_byteswap) {
    l =  *(Long *)tmp_next;
    next = tmp_next + LONG_SIZE;
  } else {
    register unsigned char	*lp = (unsigned char *) &l;

    //
    // NOTE:  environment-specific speedups abound for this kind
    // of stuff.  This generic code takes advanage of none of them.
    //
    lp [3] = *tmp_next++;
    lp [2] = *tmp_next++;
    lp [1] = *tmp_next++;
    lp [0] = *tmp_next++;
    next = tmp_next;
  }
  return l;
}

LongLong
CDR::get_longlong ()
{
  LongLong ll;
  register unsigned char	*tmp_next;
  register unsigned		temp;

  //
  // Adjust pointer and count of remaining bytes
  //
  tmp_next = ptr_align_binary (next, LONGLONG_SIZE);
  temp = LONGLONG_SIZE + (tmp_next - next);
  remaining -= temp;

  //
  // decode doubleword, swapping as needed
  //
  if (!do_byteswap) {
    ll = *(LongLong *)tmp_next;
    next = tmp_next + LONGLONG_SIZE;
  } else {
    register unsigned char	*llp = (unsigned char *) &ll;

    //
    // NOTE:  environment-specific speedups abound for this kind
    // of stuff.  This generic code takes advanage of none of them.
    //
    llp [7] = *tmp_next++;
    llp [6] = *tmp_next++;
    llp [5] = *tmp_next++;
    llp [4] = *tmp_next++;
    llp [3] = *tmp_next++;
    llp [2] = *tmp_next++;
    llp [1] = *tmp_next++;
    llp [0] = *tmp_next++;
    next = tmp_next;
  }
  return ll;
}

String
CDR::get_string ()
{
  Short len;
  return (get_string (len));
}

String
CDR::get_string (Short& len)
{
  String str;
  len = get_short ();
  str = next;
  next += len;
  remaining -= len;
  len--; // don't count null terminator
  return str;
}

Ptr
CDR::get_bytes ()
{
  Short len;
  return (get_bytes (len));
}

Ptr
CDR::get_bytes (Short& len)
{
  Ptr ptr; 
  len = get_short ();
  ptr = next;
  next += len;
  remaining -= len;
  len--;
  return ptr;
}

Boolean
CDR::skip_string ()			// ISO/1 or octet string
{
  ULong	len = get_ulong ();

  if ( len > remaining)
    return CDR_FALSE;		// buffer's changed

  next += (unsigned) len;
  remaining -= (unsigned) len;
  return CDR_TRUE;
}


Boolean
CDR::expandBy (int numBytes)
{
  return (grow (numBytes + (length - remaining)));
}

void
CDR::incrementNext (int numBytes)
{
  next += numBytes;
  remaining -= numBytes;
}

// Put an integer in the header in java byte order (bigendian).
// Not necessarily in native byte order.
void
CDR::putJavaLongInHeader (unsigned char* headerSlot, long value)
{
  long temp = value;
  *headerSlot = (unsigned char)(temp&0xff); temp >>=8;
  *(headerSlot+1) = (unsigned char)(temp&0xff); temp >>=8;
  *(headerSlot+2) = (unsigned char)(temp&0xff); temp >>=8;
  *(headerSlot+3) = (unsigned char)(temp&0xff); 
}

void
CDR::putNativeLongInHeader (unsigned char* headerSlot, long value)
{
  *(long*) headerSlot = value;
}
