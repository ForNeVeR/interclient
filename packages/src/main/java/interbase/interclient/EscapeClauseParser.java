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
 * This interface defines a function escMapping that maps from a given
 * escape_string to a corresponding SQL construct. In fact, this interface is
 * defined, so that it can be mapping function escMapping can be defined
 * differently for different types of escape_strings. Different classes are defiend
 * to implement this interface for different escape_strings.
 *
 * @author Madhukar Thakur
 **/
interface EscapeClauseParser
{
  String parse (String escapeClause) throws java.sql.SQLException;
}
