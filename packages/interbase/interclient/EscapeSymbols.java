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
 * @author Paul Ostler
 **/
final class EscapeSymbols
{
  static final char quote__ = '\'';
  static final char doubleQuote__ = '"';
  static final char backslash__ = '\\';
  static final char curlyLeftBrace__ = '{';
  static final char curlyRightBrace__ = '}';
  static final char space__ = ' ';
  static final char newline__ = '\n';
  static final char tab__ = '\t';
  static final char return__ = '\r';

  static final String quoteToken__ = "'";
  static final String doubleQuoteToken__ = "\"";
  static final String hyphenToken__ = "-";
  static final String leftParenToken__ = "(";
  static final String rightParenToken__ = ")";
  static final String colonToken__ = ":";
  static final String dotToken__ = ".";
  static final String questionMarkToken__ = "?";

  static final String callOp__ = "CALL";
  static final String dateOp__ = "D";
  static final String timeOp__ = "T";
  static final String timestampOp__ = "TS";
  static final String escapeOp__ = "ESCAPE";
  static final String outerJoinOp__ = "OJ";
  static final String functionOp__ = "FN";

}

