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
 * This maps the construct FN <function_name> (arg1, arg2,...)
 * from the escape language into a call to a udf on the server. Most functions are
 * assumed to be udfs, and we pass them as such.
 *
 * @author Madhukar Thakur
 **/
final class EscapeFunctionParser implements EscapeClauseParser
{

  // returns true if str is a syntactically correct function call.
  // The syntactic correctness is determined by the array tokensToMatch.
  // Further, if matchExactly is true, then the tokensToMatch is the exact number of tokens
  // to be present in str. If this argument is false, then the tokensToMatch must 
  // be a prefix of str, (not necessarily the entire string.  
  private synchronized boolean matchStringToTokens (String str, 
						    String[] tokensToMatch,
						    boolean matchExactly) throws java.sql.SQLException
  {
    String token;

    EscapeLexer escapeLexer = new EscapeLexer (str);

    if (!escapeLexer.hasMoreTokens())
      throw new BugCheckException (ErrorKey.bugCheck__0__, 
				   123);

    for (int i=0; i<tokensToMatch.length; i++) {
      try  {
        token = escapeLexer.nextToken();
      }
      catch (java.util.NoSuchElementException excp) {
        return false;
      }

      if (token.toUpperCase().compareTo (tokensToMatch[i]) != 0) 
        return false;
    }

    if (escapeLexer.hasMoreTokens() && matchExactly) 
      return false;

    return true;
  }
      
  public synchronized String parse (String escapeClause) throws java.sql.SQLException 
  {
    String token;

    String userTokens[] = {"FN", "USER", "(", ")"};
    String nowTokens[] = {"FN", "NOW", "(", ")"};
    String curdateTokens[] = {"FN", "CURDATE", "(", ")"};
    String ucaseTokens[] = {"FN", "UCASE", "("};

    // check escapeClause against some functions that we know of.
    if (matchStringToTokens (escapeClause, userTokens, true)) 
      return "USER";
    else if (matchStringToTokens (escapeClause, nowTokens, true)) 
      return "\"NOW\"";
    else if (matchStringToTokens (escapeClause, curdateTokens, true)) 
      return "\"TODAY\"";
    else if (matchStringToTokens (escapeClause, ucaseTokens, false)) {
      int pos =  escapeClause.indexOf ("(");
      if (pos == -1)
	throw new BugCheckException (ErrorKey.bugCheck__0__, 
				     124);
      return "UPPER " + escapeClause.substring (pos);
    }

    // now return the remaining string without the FN at the start
    int pos = escapeClause.toUpperCase ().indexOf ("FN");
    if (pos == -1) {
      throw new EscapeSyntaxException (ErrorKey.escapeSyntax__fn_0__,
				       escapeClause);
    }
    return escapeClause.substring (pos+2);  //+2 allows to skip the keyword FN
  }
}

