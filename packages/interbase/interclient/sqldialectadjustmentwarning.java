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
 * A warning that the value specified in the sqlDialect connection
 * property is not valid for the associated database and the
 * dialect value has been changed.
 * <p>
 * An attempt to set a connection SQL dialect to negative value or to
 * a value that exceeds the database's SQL dialect may throw this warning.
 * Connection SQL dialects that are too high are adjusted to match the
 * database's SQL dialect. Negative connection SQL dialects are adjusted
 * to dialect 1. A connection SQL dialect of 0 (zero), which is the
 * default, uses the SQL dialect of the database (but no warning is thrown).
 * <p>
// CJL-IB6 change the link to conform <!>
// CJL-IB6  <!!!> perhaps we should delete this class entirely.
 * The error code associated with this warning is
 * always {@link ErrorCodes#unlicensedComponent ErrorCodes.unlicensedComponent}.
 *
 * @author Chris Levesque
 * @docauthor Chris Levesque
 * @since <font color=red>Extension, since InterClient 2.00</font>
 **/

final public class SQLDialectAdjustmentWarning extends SQLWarning
{
  final private static String className__ = "SQLDialectAdjustmentWarning";
  // *** InterClient constructor ****
  SQLDialectAdjustmentWarning ( int errorKeyIndex, int newDialect)
  {
    super (className__, errorKeyIndex, (Object) new Integer(newDialect) );
  }
}
