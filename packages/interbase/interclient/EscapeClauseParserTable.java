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
 * This is a hashtable with key being the keywords in the escape construct. 
 * The object in the table is the class that implements the parser for the corresponding
 * construct.
 *
 * @author Madhukar Thakur
 **/
final class EscapeClauseParserTable extends java.util.Hashtable
{
  public EscapeClauseParserTable ()  
  {
    super (8, (float) 1.0);
    put (EscapeSymbols.callOp__, new EscapeProcedureCallParser ());
    put (EscapeSymbols.questionMarkToken__, new EscapeProcedureCallWithResultParser ());
    put (EscapeSymbols.dateOp__, new EscapeDateParser ());
    put (EscapeSymbols.timeOp__, new EscapeTimeParser ());
    put (EscapeSymbols.timestampOp__, new EscapeTimestampParser ());
    put (EscapeSymbols.functionOp__, new EscapeFunctionParser ());
    put (EscapeSymbols.escapeOp__, new EscapeEscapeParser ());
    put (EscapeSymbols.outerJoinOp__, new EscapeOuterJoinParser ());
  }
}

