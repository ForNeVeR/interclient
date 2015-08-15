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
 * processes the Time statment of the escape construct. Since just Time is not
 * supported in IB SQL, this construct in the escape clause throws an SQL
 * exception
 *
 * The above is no longer true with InterBase 6, which does have a TIME type.
 * So, in InterClient 2.0 will be able to parse times that come in either of
 * of these formats:
 *
 * {t '12:24:45'}  Time without fractions of a second or
 * {t '12:24:45.3456'}  Time with 10000ths of a second
 *
 * @author Madhukar Thakur
 * @author Chris Levesque
 **/
final class EscapeTimeParser implements EscapeClauseParser
{
  public synchronized String parse (String escapeClause) throws java.sql.SQLException
  {
// CJL-IB6  abandoned with IC2.0
//    throw new DriverNotCapableException (ErrorKey.driverNotCapable__escape__t__);
// CJL-IB6 end comment
// CJL-IB6 added for CLI time support
    String token;
    StringBuffer nativeClause = new StringBuffer ();

    EscapeLexer escapeLexer = new EscapeLexer (escapeClause, " \n\t\r'-:.");
    if (!escapeLexer.hasMoreTokens())
      throw new BugCheckException (ErrorKey.bugCheck__0__,
				   139);

    try {
      token = escapeLexer.nextToken();
      if (!token.toUpperCase().equals (EscapeSymbols.timeOp__))
	throw new BugCheckException (ErrorKey.bugCheck__0__,
				     140);

      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.quoteToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
					 escapeClause);

      String hour = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.colonToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
					 escapeClause);

      String minute = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals (EscapeSymbols.colonToken__))
        throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
					 escapeClause);

      String second = escapeLexer.nextToken();
      token = escapeLexer.nextToken();
      if (!token.equals(EscapeSymbols.quoteToken__)) {
        // {t 'hh:mm:ss.ffff'}  ==> 'hh mm ss ffff'
        if (!token.equals (EscapeSymbols.dotToken__))
          throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
                                           escapeClause);
        String fsecond = escapeLexer.nextToken();

        token = escapeLexer.nextToken();
        if (!token.equals (EscapeSymbols.quoteToken__))
          throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
                                           escapeClause);
        nativeClause.append (
           EscapeSymbols.quote__ + hour + EscapeSymbols.space__ + minute +
           EscapeSymbols.space__ + second + EscapeSymbols.space__ + fsecond +
           EscapeSymbols.quote__);
      }
      else {
        // {t 'hh:mm:ss'}  ==> 'hh mm ss'
        nativeClause.append (
           EscapeSymbols.quote__ + hour + EscapeSymbols.space__ + minute +
           EscapeSymbols.space__ + second + EscapeSymbols.quote__);
      }
      return (nativeClause.toString());
    }
    catch (java.util.NoSuchElementException e) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__t_0__,
				       escapeClause);
    }
// CJL-IB6 end change
  }
}
