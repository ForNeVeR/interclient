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
 * This is a simple lexical analyser that returns tokens separated by
 * one of the specified delimiter characaters. This is not general purpose and
 * is designed for particular use for escape processing.
 * It always skips over white space characters.
 *
 * @author Madhukar Thakur
 **/
final class EscapeLexer
{
  private int currentPos_;   //this is changed only in advance. No other method moves this
  private int maxPos_;
  private String  lexStr_;
  private String  delimiters_;

  private boolean inSingleQuotedString_;
  // keep track if currentChar_ is inside a singly quoted (') string. A singly quoted
  // string starts and ends with a '. Ofcourse, ' could be escaped using backslash, in
  // which case such an escaped ' will not start or end a quoted string.

  private boolean inDoubleQuotedString_;
  // keep track if currentChar_ is inside a doubly quoted (") string. Doubly quoted
  // strings start and end with a ". Of course we consider the fact that " may be
  // escaped using \ 

  private int curlyNestingDepth_ = 0;

  private char currentChar_;
  private char prevChar_;

  /** constructor, using the default delimiter set **/
  EscapeLexer (String str) 
  {
    this (str, " \n\t\r'\"{},()\\[]");
  }


  /**  constructor, using the specified delimiter set **/
  EscapeLexer (String str, String delim) 
  {
    currentPos_ = 0;
    maxPos_ = str.length()-1;
    lexStr_ = str;
    prevChar_ = 0;
    currentChar_ = str.charAt(currentPos_);
    delimiters_ = delim;
    if (maxPos_>= 0) {
      inSingleQuotedString_ = (currentChar_ == EscapeSymbols.quote__);
      inDoubleQuotedString_ = (currentChar_ == EscapeSymbols.doubleQuote__);
      if (currentChar_ == EscapeSymbols.curlyLeftBrace__)
        curlyNestingDepth_ = 1;
      else 
        curlyNestingDepth_ = 0;
    } else {
      inSingleQuotedString_ = false;
      inDoubleQuotedString_ = false;
      curlyNestingDepth_ = 0;
    }

  }

  /** 
   * returns true, if we could advance along the string that this tokenizer is meant for; 
   * false else 
   **/
  private synchronized boolean advance ()
  {
    currentPos_++;

    if (currentPos_ > maxPos_)
      return false;

    prevChar_    = currentChar_;
    currentChar_ = lexStr_.charAt(currentPos_);
  
    if ((currentChar_ == EscapeSymbols.quote__) && (prevChar_ != EscapeSymbols.backslash__))
      inSingleQuotedString_ = !inSingleQuotedString_;
    
    if ((currentChar_ == EscapeSymbols.doubleQuote__) && (prevChar_ != EscapeSymbols.backslash__))
      inDoubleQuotedString_ = !inDoubleQuotedString_;
    
    // go keep track of matching open and close curly braces.
    if (!inSingleQuotedString_ && ! inDoubleQuotedString_ ) {
      if ((currentChar_ == EscapeSymbols.curlyLeftBrace__) && (prevChar_ != EscapeSymbols.backslash__))
        curlyNestingDepth_ ++;
      if ((currentChar_ == EscapeSymbols.curlyRightBrace__)  && (prevChar_ != EscapeSymbols.backslash__) &&
          (curlyNestingDepth_ > 0) )
        curlyNestingDepth_ --;
      }

    return (true);
  }


  private boolean isWhiteSpace(char ch) 
  {
    return ((ch == ' ') || (ch == '\n') || (ch == '\t') ||
        (ch == '\r'));
  }


  private synchronized boolean skipWhiteSpaces()
  {
    while (isWhiteSpace(currentChar_))
      if (!advance()) 
        return (false);
    return (true);
  }


  private boolean isDelimChar(char ch) 
  {
    for (int i = 0; i < delimiters_.length(); i++) {
      if (delimiters_.charAt(i) == ch) 
        return (true);
    }
    return (false);
  }


  /** 
   * find the first occurrence of the character ch in the remaining String starting 
   * at current position. return index, if found. else return -1
   * 
   * leaves the currentPos_ pointing to the character AFTER the character found.
   **/
  private synchronized int findNext (char ch) 
  {
    do 
      if (currentChar_ == ch) {
        int saveCurrentPos_ = currentPos_;
        advance();
        return (saveCurrentPos_);
      }
    while (advance());
    return (-1);
  }


  /** 
   * find the first occurrence of the char ch in the remaining String starting at
   * the current position, the character found must not be in any quoted
   * string (not single, not double quoted). If found, return index, else return -1
   *
   * leaves the currentPos_ pointing to the character AFTER the character found.
   **/
  synchronized int findNextNotInQuotedString (char ch)
  {
    do 
      if ((currentChar_ == ch) && !inSingleQuotedString_ && !inDoubleQuotedString_) {
        int saveCurrentPos_ = currentPos_;
        advance();
        return (saveCurrentPos_);
      }
    while (advance());
    return (-1);
  }


  /** 
   * find the next matching occurrence of the character ch in the string, 
   * starting at (including) the current position. With this search starts, if we are in 
   * a singly quoted string, the matching occurrence of the character ch must also
   * be in the SAME singly quoted string, if we are not in such a string at start, then
   * the matching char must also not be in a single quoted string. Similarly for
   * doubly quoted strings. 
   * The matching character must also be in the SAME nested curly braces context.
   * This can be used to search for closing curly brace matching a given open curly brace.
   * 
   * at the end, leaves currentPos_ pointing to the character AFTER the matched char.
   **/
  synchronized int findNextMatching (char ch)
  {
    boolean initiallyInSingleQuotedString = inSingleQuotedString_;
    boolean initiallyInDoubleQuotedString = inDoubleQuotedString_;
    int initialCurlyNestingDepth = curlyNestingDepth_;

    do {
      if ((currentChar_ == ch) &&
          (initiallyInSingleQuotedString == inSingleQuotedString_) &&
          (initiallyInDoubleQuotedString == inDoubleQuotedString_) &&
          (initialCurlyNestingDepth == curlyNestingDepth_+1) && (ch == EscapeSymbols.curlyRightBrace__)) {
        int saveCurrentPos_ = currentPos_;
        advance();
        return (saveCurrentPos_);
        } 

      if (!advance()) 
        return (-1);

      if ((initiallyInSingleQuotedString && !inSingleQuotedString_) ||
          (initiallyInDoubleQuotedString && !inDoubleQuotedString_)  ||
          ((initialCurlyNestingDepth == curlyNestingDepth_+1) && 
           (currentChar_ == EscapeSymbols.curlyRightBrace__) && (ch != EscapeSymbols.curlyRightBrace__) ))
          // We started this func. in a quoted string and that quoted string now got over.
          // or we started in a curly brace context and that context is now over
        return (-1);
    } while (true);

  }


  /** 
   * find the next token starting at the current position. the token ends with a
   * delimiter character. Consecutive White spaces are skipped over. Delimiter
   * characters are also returned as a token (with a single character)
   * 
   * at end, leaves the currentPos_ pointing to the first delimiter character after
   * the token.
   **/
  synchronized String nextToken () throws java.util.NoSuchElementException 
  {
    StringBuffer token = new StringBuffer();

    if (currentPos_ > maxPos_)
      throw new java.util.NoSuchElementException();

    if (!skipWhiteSpaces())
      throw new java.util.NoSuchElementException();

    token.append(currentChar_);
    if (isDelimChar(currentChar_)) {
      advance();
      return (token.toString());
      }

    while (true) {
      if (!advance()) 
        return (token.toString());
      if (!isDelimChar(currentChar_))
        token.append(currentChar_);
      else
        return (token.toString());
    } 


  }


  /** 
   * return a substring of the string that the EscapeLexer is for.
   * the substring starts at the startPos and ends at position endPos
   **/
  synchronized String subString (int startPos, int endPos) throws StringIndexOutOfBoundsException
  {
    return (lexStr_.substring (startPos, endPos));
  }   

  /** 
   * return a substring of the string that the EscapeLexer is for.
   * the substring starts at the startPos and ends at the end of the string.
   **/
  synchronized String subString (int startPos) throws StringIndexOutOfBoundsException
  {
    return (lexStr_.substring (startPos));
  }   
    
  /** 
   * return true, if there are more tokens to be returned. White spaces are never
   * returned as tokens.
   * 
   * leaves the currentPos_ pointing to the first nonwhite character
   **/
  synchronized boolean hasMoreTokens () 
  {
    if ( (currentPos_ > maxPos_) || (!skipWhiteSpaces()) )
      return (false);
    else
      return (true);
  }



}
