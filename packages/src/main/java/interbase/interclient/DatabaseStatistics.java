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
 * Statistics about database page reads and writes to memory and disk,
 * and table operation counts on the database.
 * <p>
 * Several connections may act on a single database, sharing
 * the same database page cache, and operating on the same tables.
 * So these statistics are database-wide, and include the operation counts
 * of all connections to the database collectively.
 * <p>
 * Counting begins at zero from the moment the call to <code>getStatistics</code>
 * is made.
 *
 * @see DatabaseMetaData#getStatistics()
 * @see ServerManager#getStatisticsText
 * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
 **/
// CJL-IB6 changed reference to InterClient 2.0
final public class DatabaseStatistics
{
  DatabaseStatistics () {}

  //-----------------Page reads/writes to memory or disk------------------------

  /**
   * Number of database page reads from disk.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getDiskReads () throws java.sql.SQLException
  {
    // isc_info_reads
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of database page writes to disk.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getDiskWrites () throws java.sql.SQLException
  {
    // isc_info_writes
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of memory reads from cache.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getMemoryReads () throws java.sql.SQLException
  {
    // isc_info_fetches
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of memory writes to cache.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getMemoryWrites () throws java.sql.SQLException
  {
    // isc_info_marks
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  //-----------------Table operation counts-------------------------------------

  /**
   * Number of removals of record versions from <code>table</code>.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getBackoutCount (String table) throws java.sql.SQLException
  {
    // isc_info_backout_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of database record deletes from <code>table</code>.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getDeleteCount (String table) throws java.sql.SQLException
  {
    // isc_info_delete_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of removals of a record and all of its ancestors from <code>table</code>,
   * for records whose deletions have been committed.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getExpungeCount (String table) throws java.sql.SQLException
  {
    // isc_info_expunge_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of table reads done via an index.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getIndexedReadCount (String table) throws java.sql.SQLException
  {
    // isc_info_read_idx_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of record inserts into <code>table</code>.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getInsertCount (String table) throws java.sql.SQLException
  {
    // isc_info_insert_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of removals of old versions of fully mature records
   * from <code>table</code> (records committed, resulting in older ancestor versions
   * no longer being needed).
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getPurgeCount (String table) throws java.sql.SQLException
  { 
    // isc_info_purge_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of sequential table reads.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getSequentialReadCount (String table) throws java.sql.SQLException
  { 
    // isc_info_read_seq_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }

  /**
   * Number of table updates.
   *
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0, not yet supported</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public int getUpdateCount (String table) throws java.sql.SQLException
  {
    // isc_info_update_count
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
}
