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
 * maps the Date statment of the escape construct into the date statement of IB SQL.
 *
 * @author Madhukar Thakur
 **/
final class EscapeDateParser implements EscapeClauseParser
{

  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
    String token;
    StringBuffer nativeClause = new StringBuffer ();

    EscapeLexer escapeLexer = new EscapeLexer (escapeClause, " \n\t\r'-");
    if (!escapeLexer.hasMoreTokens())
      throw new BugCheckException (ErrorKey.bugCheck__0__,
				   119);

    try {
      token = escapeLexer.nextToken();
      if (!token.toUpperCase().equals (EscapeSymbols.dateOp__))
	throw new BugCheckException (ErrorKey.bugCheck__0__, 
				     120);

      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__d_0__,
					 escapeClause);

      String year = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.hyphenToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__d_0__,
					 escapeClause);

      String month = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.hyphenToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__d_0__,
					 escapeClause);

      String day = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__d_0__,
					 escapeClause);

      nativeClause.append (EscapeSymbols.quote__ + month + EscapeSymbols.space__ + day + EscapeSymbols.space__ + year + EscapeSymbols.quote__);
      return (nativeClause.toString());
    } 
    catch (java.util.NoSuchElementException e) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__d_0__,
				       escapeClause);
    }
  }
}

