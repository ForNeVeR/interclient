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
 * This implements the mapping from the CALL construct in the escape language
 * to a SELECT * FROM <proc_name> (arg1,..) or an EXECUTE PROCEDURE <proc_name> (arg1,..)
 *
 * @author Madhukar Thakur
 * @author Paul Ostler
 **/
final class EscapeProcedureCallParser implements EscapeClauseParser
{

  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
    EscapeLexer escapeLexer = new EscapeLexer (escapeClause);

    if (!escapeLexer.hasMoreTokens ())
      throw new BugCheckException (ErrorKey.bugCheck__0__, 117);

    try {
      String token = escapeLexer.nextToken ();

      if (!token.toUpperCase ().equals (EscapeSymbols.callOp__))
	throw new BugCheckException (ErrorKey.bugCheck__0__, 118);

      token = escapeLexer.nextToken ();

      StringBuffer nativeClause = new StringBuffer ();
      // there must be 18 spaces prefixed for overlay on server

      // !!! this must all be rewritten to not require "(" and ")"
      // !!! and should not really parse at all, just append the clause
      // CJL-- bug fix for defect 60498
      // A procedure with no parameters, i.e. {call myProc}, should be allowed
      // and {call myProc()} should not be allowed.

      nativeClause.append ("                  " + token );

      if (escapeLexer.hasMoreTokens ()) {

        token = escapeLexer.nextToken ();
        if (!token.equals (EscapeSymbols.leftParenToken__)) {
          throw new EscapeSyntaxException (ErrorKey.escapeSyntax__call_0__,
                                           escapeClause);
        }
        nativeClause.append(EscapeSymbols.leftParenToken__);

        token = escapeLexer.nextToken ();

        nativeClause.append(token);

        boolean foundClosedBracket = false;
        while (!foundClosedBracket) {
          token = escapeLexer.nextToken ();
          if (token.equals (EscapeSymbols.rightParenToken__)) {
            foundClosedBracket = true;
            nativeClause.append (EscapeSymbols.rightParenToken__);
          }
          else
            nativeClause.append (token);
        }
      }
      return (nativeClause.toString ());
    }
    catch (java.util.NoSuchElementException e) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__call_0__,
				       escapeClause);
    }
  }
}

