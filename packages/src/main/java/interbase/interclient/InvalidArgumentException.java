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
 * An application is calling a method with improper argument(s).
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#invalidOperation ErrorCodes.invalidOperation}.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
public class InvalidArgumentException extends InvalidOperationException 
{
  final private static String className__ = "InvalidArgumentException";

  // *** InterClient constructor ****
  InvalidArgumentException (ErrorKey errorKey) 
  {
    super (className__, errorKey);
  }

  // *** InterClient constructor ****
  InvalidArgumentException (ErrorKey errorKey, Object arg) 
  {
    super (className__, errorKey, arg);
  }

  // *** InterServer constructor ***
  InvalidArgumentException (int errorKeyIndex)
  {
    super (className__, errorKeyIndex);
  }

// CJL-IB6 Argument from InterServer (SQL Dialect adjusted )
  InvalidArgumentException (String subclassName, int errorKeyIndex, Object arg)
  {
    super (subclassName, errorKeyIndex, arg);
  }

// end change

  // *** For subclasses only ***
  InvalidArgumentException (String subclassName, ErrorKey errorKey, Object arg)
  {
    super (subclassName, errorKey, arg);
  }
}

 
