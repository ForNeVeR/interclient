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
 * @author Paul Ostler
 **/
final class MessageCodes
{
  // WARNING: do not change any of these values without also changing
  // the corresponding constant in the interserver.

  // ************Opcodes used in messages from InterClient to InterServer************
  static final int ATTACH_SERVER_MANAGER__ = 5;
  static final int DETACH_SERVER_MANAGER__ = 6;
  static final int ATTACH_DATABASE__ = 8;
  static final int DETACH_DATABASE__ = 9;
  static final int SUSPEND_CONNECTION__ = 10;
  static final int RESUME_CONNECTION__ = 11;
  static final int PING__ = 12;
  static final int COMMIT__ = 13;
  static final int ROLLBACK__ = 14;

  static final int PREPARE_STATEMENT__ = 16;
  static final int PREPARE_CALL__ = 17;
  static final int EXECUTE_STATEMENT__ = 18;
  static final int EXECUTE_QUERY_STATEMENT__ = 19;
  static final int EXECUTE_UPDATE_STATEMENT__ = 20;
  static final int EXECUTE_PREPARED_STATEMENT__ = 21;
  static final int EXECUTE_PREPARED_QUERY_STATEMENT__ = 22;
  static final int EXECUTE_PREPARED_UPDATE_STATEMENT__ = 23;
  static final int FETCH_ROWS__ = 24;
  static final int CLOSE_CURSOR__ = 26;
  static final int CLOSE_STATEMENT__ = 27;
  static final int CANCEL_STATEMENT__ = 28;
  static final int GET_RESULT_COLUMN_META_DATA__ = 29;

  static final int OPEN_BLOB__ = 32;
  static final int CREATE_BLOB__ = 33;
  static final int GET_BLOB_SEGMENT__ = 34;
  static final int CLOSE_BLOB__ = 36;

  // MMM - added new code GET_ARRAY_DESCRIPTOR__
  static final int GET_ARRAY_DESCRIPTOR__ = 38;
  static final int GET_ARRAY_SLICE__ = 39;
  // MMM - end

  static final int EXECUTE_CATALOG_QUERY__ = 40;

  static final int SERVICE_REQUEST__ = 100;
  static final int CREATE_DATABASE__ = 101;

  // ************Delimeters used in messages from InterServer to InterClient************
  static final int SUCCESS__ = 1;
  static final int FAILURE__ = 2;

  static final int WARNING__ = 1;
  static final int END_WARNINGS__ = 2; 

  static final int EXCEPTION__ = 1;
  static final int END_EXCEPTIONS__ = 2; 

  static final int ROW_DATUM__ = 1;
  static final int END_ROW_DATA__ = 2;
}
