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
 * The ConnectionCache class implements
 * the javax.sql.ConnectionEventListener interface so that it can be notified when
 * close and error events occur on a PooledConnection that is in use.
 * <P>
 * The ConnectionEventListener interface is implemented by a
 * connection pooling component.  A connection pooling component will
 * usually be provided by a JDBC driver vendor, or another software
 * vendor.  A ConnectionEventListener is notified by a JDBC driver when
 * an application is finished using a connection.  This event occurs
 * after the application calls close on its representation of the
 * connection.  A ConnectionEventListener is also notified when a
 * Connection error occurs due to the fact that the Connection is unfit
 * for future use---the server has crashed, for example.  The listener is
 * notified, by the JDBC driver, just before the driver throws an
 * SQLException to the application using the Connection.
 *
 * @since <font color=red>Extension, proposed for future release, not yet supported</font>
 * @version Std Ext 0.7
 **/
public class ConnectionCache implements javax.sql.ConnectionEventListener
{
  /**
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   **/
  synchronized public javax.sql.PooledConnection lookup ()
  {
    // flesh out details
    return null;
  }

  /**
   * Invoked when the application calls close() on its
   * representation of the connection.
   *
   * @param event an event object describing the source of the event
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   */
  synchronized public void connectionClosed (javax.sql.ConnectionEvent event)
  {
    // !!!
  }

  /**
   * Invoked when a fatal connection error occurs, just before
   * an SQLException is thrown to the application.
   *
   * @param event an event object describing the source of the event
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   */
  synchronized public void connectionErrorOccurred (javax.sql.ConnectionEvent event)
  {
    // !!!
  }

}
