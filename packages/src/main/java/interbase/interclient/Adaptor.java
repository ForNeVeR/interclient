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
 * Provides a means to modify the standard behavior of an InterClient JDBC object.
 * <p>
 * The Adaptor interface is used to modify, or adapt, the behavior
 * of an InterClient JDBC object in order to improve 
 * performance for certain applications which do not require
 * the full implementation of JDBC standard behavior for that object.
 * <p>
 * A JDBC class which implements this interface is said to be adaptable.
 * Currently, only the InterClient {@link ResultSet} class implements this interface.
 * Hence InterClient result set objects are adaptable.
 * <p>
 * The Adaptor interface can be implemented by any JDBC class which
 * can be adapted for improved performance under certain assumptions.
 * An adaptation modifies the default behavior for a JDBC object
 * by compromising standard JDBC behavior in favor of performance.
 * <p>
 * Certain JDBC driver classes may choose to implement this interface
 * for enhanced performance with the JBuilder dataset components.
 * Currently, JBuilder only looks for ResultSet adaptations.  
 * Future versions of JBuilder and InterClient may allow
 * for the behavior of other driver objects to be adaptable as well.
 * <p>
 * Here's an example adaptation of a result set object which instructs
 * the driver to right trim <code>SQL CHAR</code> string instances
 * (<code>RIGHT_TRIM_STRINGS</code>),
 * and to avoid constructing multiple java.sql.Date instances by reusing the
 * first Date fetched on each subsequent fetch (<code>SINGLE_INSTANCE_TIME</code>).
 * By reusing a single java.sql.Date instance,
 * the <code>SINGLE_INSTANCE_TIME</code> adaptation avoids the overhead associated
 * with the driver calling the java.sql.Date constructor for each row selected.
 * The <code>RIGHT_TRIM_STRINGS</code> adaptation relieves an application from
 * having to right trim <code>CHAR</code> strings which are padded according the
 * JDBC standard.
 * <pre>
 * import interbase.interclient.Adaptor;
 *
 * java.sql.Statement s = c.createStatement ();
 * java.sql.ResultSet rs = s.executeQuery ("select NAME, BIRTHDAY from BIRTHDAYS");
 *
 * Adaptor rsAdaptor = (Adaptor) rs;
 * rsAdaptor.adapt (Adaptor.RIGHT_TRIM_STRINGS, null);
 * rsAdaptor.adapt (Adaptor.SINGLE_INSTANCE_TIME, null);
 * 
 * while (rs.next ()) {
 *   System.out.println (rs.getString ("NAME"));    // no need to call String.trim()
 *   System.out.println (rs.getDate ("BIRTHDAY"));  // The first Date object constructed is reused 
 * }                                                // and modified throughout the iterations.
 * </pre>
 * <p>
 * A third party tool built to run with any or multiple JDBC 
 * drivers could be compiled with this interface in its 
 * class path in order to leverage performance improvements
 * when using the InterClient driver as follows:
 * <pre>
 * java.sql.ResultSet resultSetToProcess = ...
 * try {
 *   Class.forName ("interbase.interclient.Adaptor");
 *   interbase.interclient.Adaptor adaptor = (interbase.interclient.Adaptor) resultSetToProcess;
 *   adaptor.adapt (SINGLE_INSTANCE_TIME, null);
 * }
 * catch (ClassNotFoundException e) {
 *   // no adaptor found, that's ok, just use default driver behavior
 * }
 * process (resultSetToProcess);
 * </pre>
 * The performance of the 3rd party application will be
 * improved when InterClient is loaded as the JDBC driver backend,
 * even if no other InterClient extensions are used, and the
 * application makes only portable java.sql method calls.
 * <p>
 * Notice that a 3rd party tool provider needs to compile
 * with the interbase.interclient.Adaptor interface in the class path, 
 * but does not need to compile with the complete interbase.interclient 
 * package unless it is using other InterClient extensions to
 * the JDBC API.  
 * <p>
 * Also notice that the Adaptor interface does NOT need to
 * be shipped along with the 3rd party product.
 * If the InterClient driver is loaded then the Adaptor interface
 * will be loaded as well.
 * If the InterClient Adaptor interface is not present
 * in the application class path then the adaptation will 
 * not be performed.  This allows for adaptations to be made
 * in JDBC applications which use only portable JDBC calls,
 * so that the application will still run against any driver,
 * but may have performance adaptations when running against InterClient.
 * <p>
 * InterClient VARs or those wishing to redistribute the Adaptor
 * interface with a 3rd party app may prefer the following code:
 * <pre>
 * import interbase.interclient.Adaptor;
 *
 * java.sql.ResultSet resultSetToProcess = ...
 * if (resultSetToProcess instanceof Adaptor) {
 *   Adaptor adaptor = (Adaptor) resultSetToProcess;
 *   adaptor.adapt (SINGLE_INSTANCE_TIME, null);
 * }
 * process (resultSetToProcess);
 * </pre>
 * <p>
 * Also see {@link borland.jdbc.SQLAdapter borland.jdbc.SQLAdapter} and
 * {@link com.inprise.sql.SQLAdapter com.inprise.sql.SQLAdapter}
 * interfaces which are all identical
 * to the InterClient Adaptor interface, and are all implemented by
 * the InterClient {@link ResultSet} class.
 *
 * @since <font color=red>Extension, since InterClient 1.0</font>
 * @author Paul Ostler 
 * @author Steve Shaughnessy
 * @docauthor Paul Ostler
 **/
public interface Adaptor
{

  /**
   * This modifier instructs an adaptable result set object to create String instances
   * for <code>SQL CHAR</code> fields with white space already trimmed from the end of the String.
   * This adaptation affects the behavior of
   * {@link ResultSet#getString(int) ResultSet.getString(column)}.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public final static int RIGHT_TRIM_STRINGS = 1;

  /**
   * This modifier instructs an adaptable result set object to reuse a single instance
   * of a Java {@link java.sql.Time Time}, {@link java.sql.Date Date}, or
   * {@link java.sql.Timestamp Timestamp} object
   * for the life of the result set.
   * This adaptation affects the behavior of
   * {@link ResultSet#getDate(int) ResultSet.getDate(column)},
   * {@link ResultSet#getTime(int) ResultSet.getTime(column)} and
   * {@link ResultSet#getTimestamp(int) ResultSet.getTimestamp(column)}.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public final static int SINGLE_INSTANCE_TIME = 2;

  /**
   * Adapt a JDBC object as described by one of the Adaptor modifiers.
   * The <code>extraInfo</code> parameter is currently unused.
   * 
   * @param modifier is either
   *   {@link #RIGHT_TRIM_STRINGS RIGHT_TRIM_STRINGS} or
   *   {@link #SINGLE_INSTANCE_TIME SINGLE_INSTANCE_TIME}
   * @param extraInfo any extra information that needs to be specified along with the modifier,
   *    currently unused.
   * @return true if the modifier is supported, false otherwise.
   * @throws java.sql.SQLException if the driver is unable to adapt to the modification
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public boolean adapt (int modifier, 
                        Object extraInfo) throws java.sql.SQLException;

  /**
   * Revert back to the default JDBC behavior for this previously adapted object.
   * 
   * @param modifier is either
   *   {@link #RIGHT_TRIM_STRINGS RIGHT_TRIM_STRINGS} or
   *   {@link #SINGLE_INSTANCE_TIME SINGLE_INSTANCE_TIME}
   * @throws java.sql.SQLException if the driver is unable to revert the modification back
   *   to the standard JDBC behavior.
   * @since <font color=red>Extension, since InterClient 1.0</font>
   **/
  public void revert (int modifier) throws java.sql.SQLException;

}

