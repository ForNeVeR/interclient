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
//-*-C++-*-
#ifndef _MESSAGE_CODES_H__
#define _MESSAGE_CODES_H__

class MessageCodes {

public:

  // Includes all codes used in messages from InterClient to InterServer.
  enum Opcode {
    TEST_ECHO = 1,
    ATTACH_SERVER_MANAGER = 5,
    DETACH_SERVER_MANAGER = 6,
    ATTACH_DATABASE = 8,
    DETACH_DATABASE = 9,
    SUSPEND_CONNECTION = 10,
    RESUME_CONNECTION = 11,
    PING = 12,
    COMMIT = 13,
    ROLLBACK = 14,

    PREPARE_STATEMENT = 16,
    PREPARE_CALL = 17,
    EXECUTE_STATEMENT = 18,
    EXECUTE_QUERY_STATEMENT = 19,
    EXECUTE_UPDATE_STATEMENT = 20,
    EXECUTE_PREPARED_STATEMENT = 21,
    EXECUTE_PREPARED_QUERY_STATEMENT = 22,
    EXECUTE_PREPARED_UPDATE_STATEMENT = 23,
    FETCH_ROWS = 24,
    CLOSE_CURSOR = 26,
    CLOSE_STATEMENT = 27,
    CANCEL_STATEMENT = 28,
    GET_RESULT_COLUMN_META_DATA = 29,

    OPEN_BLOB = 32,
    CREATE_BLOB = 33,
    GET_BLOB_SEGMENT = 34,
    CLOSE_BLOB = 36,

    // MMM - added new code
    GET_ARRAY_DESCRIPTOR = 38,
    GET_ARRAY_SLICE = 39,
    // MMM - end

    EXECUTE_CATALOG_QUERY = 40,

    SERVICE_REQUEST = 100,
    CREATE_DATABASE = 101
  };

  // Includes all codes used in messages from InterServer to InterClient
  enum Delimiter {
    SUCCESS = 1,
    FAILURE = 2,

    WARNING = 1,
    END_WARNINGS = 2, 

    EXCEPTION = 1,
    END_EXCEPTIONS = 2, 

    ROW_DATUM = 1,
    END_ROW_DATA = 2
  };

};

#endif


