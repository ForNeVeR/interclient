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
 * A deadly embrace between two transactions has occurred in which
 * both transactions are waiting for each other to terminate.
 * <p>
 * In the standard <EM>concurrency wait</EM> transaction model,
 * if transaction A writes to a record and then transaction B
 * attempts to write to the same record, transaction B will wait
 * indefinitely until transaction A completes with either a <EM>commit</EM>
 * or a <EM>rollback</EM>.  For example, in two qli sessions type
 * "<EM>for states modify using area = 12</EM>"
 * <p>
 * If transaction A commits, then transaction B will fail
 * with an update conflict.  In such case, transaction B
 * should always have an error handler which handles the failure
 * by either rolling back and restarting the transaction, or committing
 * and then exiting.
 * <p>
 * If transaction B's error handler commits in the error handler
 * of an update conflict then the transaction is not atomic since
 * the conflicting updates will not be committed but other updates will.
 * This approach is not recommended.  Rather it is better to rollback
 * in error handlers.
 * <p>
 * Deadly embrace occurs when transaction A is waiting for transaction B
 * to complete and transaction B is waiting for transaction A to complete.
 * This can be produced in 2 qli sessions with
 * <PRE>
 *     transaction A: "for states modify using area = 100", then
 *     transaction B: "for cities modify using altitude = 1001", then
 *     transaction A: "for cities modify using altitude = 1000", then
 *     transaction B: "for states modify using area = 101"
 * </PRE>
 * <p>
 * At this point both transactions are waiting for each other to complete.
 * This will be detected by a deadlock scan which is performed automatically
 * about every 10 seconds.  One of the transactions will fail with a deadlock,
 * the other keeps waiting until the error failing transaction's error
 * handler either commits or rolls back.  If the failing transaction commits
 * then the other transaction will fail as well with update conflicts.
 * If the failing transaction rolls back then the other transaction resumes.
 * <p>
 * In summary, update conflicts can wait indefinitely.
 * Real deadlocks are detected by deadlock scans, but unless the process which
 * receives the error rolls back, one process may hang indefinitely.
 * <p>
 * The error code associated with this exception is
 * isc_deadlock from the InterBase ibase.h file.
 *
 * @author Paul Ostler
 * @docauthor Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.0</font> 
 **/
final public class DeadlockException extends SQLException 
{
  final private static String className__ = "DeadlockException";

  // *** InterBase constructor ***
  DeadlockException (int errorKeyIndex, int errorCode, int ibSQLCode, String ibErrorMessage)
  {
    super (className__, errorKeyIndex, errorCode, ibSQLCode, ibErrorMessage);
  }
}
 
