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
package interbase.interclient;

/**
 * An invalid character encoding was encountered
 * in a string or character stream.
 * This can occur while reading or writing to a
 * CHAR, VARCHAR, or LONGVARCHAR (Blob) field which enforces
 * a specific character encoding.
 * <p>
 * For example, this could occur if a JDBC application
 * attempted to set a non-ASCII string to an ASCII field.
 * <p>
 * Although this exception generally indicates an application error,
 * it is possible for this exception to occur due to
 * some internal bug withing InterBase or InterServer
 * such that the character encoding of a fixed-format character
 * field becomes corrupted.  
 * </ul>
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#characterEncoding ErrorCodes.characterEncoding}.
 *
 * @docauthor Paul Ostler
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.50</font>
 **/
final public class CharacterEncodingException extends SQLException
{
  final private static String className__ = "CharacterEncodingException";

  // *** InterClient constructor ****
  CharacterEncodingException (ErrorKey errorKey, String message)
  {
    super (className__, errorKey, message);
  }

  // *** InterClient constructor ****
  CharacterEncodingException (ErrorKey errorKey, int internalCode)
  {
    super (className__, errorKey, String.valueOf (internalCode));
  }

  // *** InterClient constructor ****
  CharacterEncodingException (ErrorKey errorKey)
  {
    super (className__, errorKey);
  }
}

