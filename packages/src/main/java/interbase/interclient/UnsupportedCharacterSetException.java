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
 * Attempted use of an unsupported character encoding.  
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#driverNotCapable ErrorCodes.driverNotCapable}.
 *
 * @see CharacterEncodings
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.50</font>
 **/
final public class UnsupportedCharacterSetException extends DriverNotCapableException
{
  final private static String className__ = "UnsupportedCharacterSetException";

  // *** InterClient constructor ****
  UnsupportedCharacterSetException (ErrorKey errorKey, String encoding)
  {
    super (className__, errorKey, encoding);
  }

  // *** InterServer constructor ***
  UnsupportedCharacterSetException (int errorKeyIndex, int characterCode)
  {
    super (className__, errorKeyIndex, String.valueOf (characterCode));
  }

}
 
