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

/* maps the timestamp statement in the escape clause into a timestamp string in IB
 * SQL. Note, that since IB SQL does not support fcational seconds, use of frcational
 * seconds gives an SQL exception.
 *
 * @author Madhukar Thakur
 **/
final class EscapeTimestampParser implements EscapeClauseParser
{

  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
    String token;
    StringBuffer nativeClause = new StringBuffer ();

    EscapeLexer escapeLexer = new EscapeLexer (escapeClause, " \n\t\r'-:.");
    if (!escapeLexer.hasMoreTokens ())
      throw new BugCheckException (ErrorKey.bugCheck__0__, 
				   121);

    try {
      token = escapeLexer.nextToken ();
      if (!token.toUpperCase().equals (EscapeSymbols.timestampOp__))
	throw new BugCheckException (ErrorKey.bugCheck__0__, 
				     122);

      token = escapeLexer.nextToken ();
      if (!token.equals (EscapeSymbols.quoteToken__)) 
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      String year = escapeLexer.nextToken ();
      token = escapeLexer.nextToken ();
      if (!token.equals (EscapeSymbols.hyphenToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      String month = escapeLexer.nextToken ();
      token = escapeLexer.nextToken ();
      if (!token.equals (EscapeSymbols.hyphenToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      String day = escapeLexer.nextToken ();

      String hour = escapeLexer.nextToken ();
      token = escapeLexer.nextToken ();
      if (!token.equals (EscapeSymbols.colonToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      String min = escapeLexer.nextToken ();
      token = escapeLexer.nextToken ();
      if (!token.equals (EscapeSymbols.colonToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      String sec = escapeLexer.nextToken ();
      token = escapeLexer.nextToken ();
      if (token.equals (EscapeSymbols.dotToken__)) {
        // !!! set a warning here, or DataTruncationException?
        // throw new DriverNotCapableException (ErrorKey.driverNotCapable__escape__ts_fractionals__);
        String fractional = escapeLexer.nextToken (); // just ignore the value.
        token = escapeLexer.nextToken ();
      }

      if (!token.equals (EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
					 escapeClause);

      nativeClause.append (EscapeSymbols.quote__ +
                           month + EscapeSymbols.space__ +
                           day +   EscapeSymbols.space__ +
                           year +  EscapeSymbols.space__ +
                           hour +  EscapeSymbols.space__ +
                           min +   EscapeSymbols.space__ +
                           sec +
                           EscapeSymbols.quote__);
      return (nativeClause.toString ());
    } 
    catch (java.util.NoSuchElementException e) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__ts_0__,
				       escapeClause);
    }
  }
}

