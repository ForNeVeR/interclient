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
 * Contributor(s): Friedrich von Never.
 */

package interbase.interclient;

/**
 * An XAConnection object provides support for distributed
 * transactions.  An XAConnection may be enlisted in a distributed
 * transaction by means of an XAResource object.
 *
 * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
 * @version Std Ext 0.7, pre JTA 0.9
 */
public class XAConnection extends PooledConnection implements javax.sql.XAConnection
{
  /**
   * Return an XA resource to the caller.
   *
   * @return the XAResource
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>JDBC 2 Standard Extension, proposed for future release, not yet supported</font>
   */
    synchronized public javax.transaction.xa.XAResource getXAResource () throws java.sql.SQLException {
        throw new DriverNotCapableException(ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
    }
}
