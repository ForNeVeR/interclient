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
 * This maps the escape symbol (used to escape the SQL character used in a wildcard
 * string seqrch using like ...) in the JDBC's escape language in the right format
 * for IB SQL
 *
 * @author Madhukar Thakur
 **/
final class EscapeEscapeParser implements EscapeClauseParser
{

  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
    String token;

    EscapeLexer escapeLexer = new EscapeLexer (escapeClause);
    if (!escapeLexer.hasMoreTokens ())
      throw new BugCheckException (ErrorKey.bugCheck__0__, 
				   125);

    try {
      token = escapeLexer.nextToken();
      if (!token.toUpperCase().equals (EscapeSymbols.escapeOp__))
	      throw new BugCheckException (ErrorKey.bugCheck__0__, 126);

      token = escapeLexer.nextToken();
      if (!token.equals(EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__escape__no_quote_0__,
					 escapeClause);
                
      token = escapeLexer.nextToken();
      String nativeClause = EscapeSymbols.escapeOp__ + EscapeSymbols.space__ +
                            EscapeSymbols.doubleQuoteToken__ + token + EscapeSymbols.doubleQuoteToken__;

      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__escape__no_quote_0__,
					 escapeClause);
                
      return (nativeClause);
    }
    catch (java.util.NoSuchElementException e) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__escape_0__,
				       escapeClause);
    }
  }
}

