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
 * An application is calling a method out of context,
 * or in a manner not allowed according to the JDBC specification.
 *
 * <p>This can occur if the program state is such that executing
 * an invoked method would not make sense.
 * For example, calling <code>Connection.setReadOnly()</code> while in 
 * the middle of a transaction, or attempting to operate on
 * a closed connection.
 * <p>
 * The error code associated with this exception is
 * {@link ErrorCodes#invalidOperation ErrorCodes.invalidOperation}.
 *
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
public class InvalidOperationException extends SQLException 
{
  final private static String className__ = "InvalidOperationException";

  // *** InterClient constructor ****
  InvalidOperationException (ErrorKey errorKey)
  {
    super (className__, errorKey);
  }

  // *** InterServer constructor ***
  InvalidOperationException (int errorKeyIndex)
  {
    super (className__, errorKeyIndex);
  }

  // *** For subclasses only ****
  InvalidOperationException (String subclassName, ErrorKey errorKey)
  {
    super (subclassName, errorKey);
  }

  InvalidOperationException (String subclassName, int errorKeyIndex)
  {
    super (subclassName, errorKeyIndex);
  }

// CJL-IB6 added for support of SQL Dialect Adjustment
  InvalidOperationException (String subclassName, int errorKeyIndex, Object arg)
  {
    super (subclassName, errorKeyIndex, arg);
  }
// CJL-IB6 end change

  InvalidOperationException (String subclassName, ErrorKey errorKey, Object arg)
  {
    super (subclassName, errorKey, arg);
  }
}
 
