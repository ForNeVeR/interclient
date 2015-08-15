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
 * Maps the outer join construct of the escape clause
 * into the outer join construct of IB SQL.
 *
 * @author Madhukar Thakur
 **/
final class EscapeOuterJoinParser implements EscapeClauseParser
{
  public synchronized String parse (String escapeClause) throws BugCheckException
  {
    String token;

    // now return the remaining string without the OJ at the start
    int pos = escapeClause.toUpperCase ().indexOf (EscapeSymbols.outerJoinOp__);
    if (pos == -1)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 127);

    return escapeClause.substring (pos+2);
  }

}

