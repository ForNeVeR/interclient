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
 * to do escape processing 
 * call the function doEscapeProcessing () to do all the escape processing required 
 * on a string before preparing or executing it.
 *
 * @author Madhukar Thakur
 **/
final class EscapeProcessor
{
  private static final String EMPTY_STR__ = "";

  private boolean isEscapedProcedureCall_ = false;

  // map the escape string given as str into a SQL construct and
  // return the SQL string.
  synchronized private String mapOneEscapeClause (
    boolean firstLevel,
    String escapeClause,
    EscapeClauseParserTable escapeClauseParserTable) throws java.sql.SQLException
  {
  
    EscapeLexer escapeLexer = new EscapeLexer (escapeClause);
    if (escapeClause.length () > 0) {
      String firstToken = escapeLexer.nextToken ();
      String upperFirstToken = firstToken.toUpperCase ();

      if (firstLevel) {
        isEscapedProcedureCall_ = upperFirstToken.equals (EscapeSymbols.callOp__);
      }

      // get the mapping function corresponding to this keyword from the 
      // escapeClauseParserTable hash table and call it to do the mapping

      EscapeClauseParser escapeStatementParser =
        (EscapeClauseParser) escapeClauseParserTable.get (upperFirstToken);
      if (escapeStatementParser == null) {
	throw new EscapeSyntaxException (ErrorKey.escapeSyntax__unrecognized_keyword_0__,
					 escapeClause);
      }

      return escapeStatementParser.parse (escapeClause);
    }
    else 
      return EMPTY_STR__;
  }

  // This is called to do excape processing on a string.
  // Locate an escape string in the str and replace it by the
  // appropriate SQL string and return the resulting string. The escape string is
  // the one enclosed between {...}. However, note that there may be more than one
  // escape strings in the string str and that open brace maybe within single (or double) 
  // quoted strings. A curly brace within quoted string is not cosnidered to be a
  // delimiter for the escape procesing. Also, to note quoted strings, we take into
  // account \" (does not start or end a quoted string).
  synchronized String doEscapeProcessing (String sqlSubstring) throws java.sql.SQLException 
  {
    int openCurlyPos = -1;
    int closeCurlyPos = -1;
    int currentPos = 0;
    boolean found = false;

    String strForNextNestingLevel = sqlSubstring;
    EscapeClauseParserTable escapeClauseParserTable = new EscapeClauseParserTable ();

    // make one pass over strForNextNestingLevel for every level of nested curly braces.
    // break out of the while loop and return the string if no open curly brace found.
    boolean firstLevel = true;
    while (true) {

      openCurlyPos = -1;
      closeCurlyPos = -1;
      currentPos = 0;
      found = false;

      StringBuffer finalStr = new StringBuffer ();

      EscapeLexer escapeLexer = new EscapeLexer (strForNextNestingLevel);

      openCurlyPos = escapeLexer.findNextNotInQuotedString (EscapeSymbols.curlyLeftBrace__);
      found = (openCurlyPos > -1);
      if (!found) break;

      // this while loop makes one pass over strForNextNestingLevel and finds all matching curly braces 
      // that are not nested. This reoves decreases the depth of nested curly braces by 1.
      while (found) {
	closeCurlyPos = escapeLexer.findNextMatching (EscapeSymbols.curlyRightBrace__);
	if (closeCurlyPos == -1) {
	  throw new EscapeSyntaxException (ErrorKey.escapeSyntax__no_closing_escape_delimeter_0__,
					   strForNextNestingLevel);
	}
  
	finalStr.append (strForNextNestingLevel.substring(currentPos, openCurlyPos));
	finalStr.append (' ');     // be nice and add an extra space
   
	String escString =
          new String (strForNextNestingLevel.substring (openCurlyPos+1, closeCurlyPos));
	finalStr.append (mapOneEscapeClause (firstLevel, escString, escapeClauseParserTable));
	finalStr.append (' ');     // be nice and add an extra space
        firstLevel = false;
	currentPos = closeCurlyPos+1;
   
	openCurlyPos = escapeLexer.findNextNotInQuotedString (EscapeSymbols.curlyLeftBrace__);
	found = (openCurlyPos > -1); 
      }

      finalStr.append (strForNextNestingLevel.substring (currentPos));
      strForNextNestingLevel = finalStr.toString ();
    }

    return (strForNextNestingLevel);
  }

  boolean isEscapedProcedureCall ()
  {
    return isEscapedProcedureCall_;
  }

}

