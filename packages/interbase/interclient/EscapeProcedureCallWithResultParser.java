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
 * This implements the mapping from the ? = CALL construct in the escape language
 * Since, IB does not support procedures with return values, this raises
 * an "unsupported construct" exception.
 *
 * @author Madhukar Thakur
 **/
final class EscapeProcedureCallWithResultParser implements EscapeClauseParser
{
  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__escape__call_with_result__);
  }
}
