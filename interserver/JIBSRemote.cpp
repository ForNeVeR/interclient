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
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include <iostream.h> // for debug only

#include "JIBSRemote.h"
#include "MessageCodes.h"

#include "IB_SQLException.h"
#include "CommunicationSQLException.h"
#include "IB_Status.h"
#include "IB_CharacterSets.h"
#include "IB_Statement.h"
#include "IB_ResultSet.h"
#include "IB_Blob.h"
#include "event_msgs.h"

#include "IB_Defines.h" // for debug trace

#ifdef WIN32
#include <windows.h>
#else
typedef int     BOOL;
typedef int     BOOLEAN;
#define TRUE    1
#define FALSE   0
#endif


void
JIBSRemote::interserverMain ()
{
  IB_SBYTE opcode;	
  IB_SQLException* exceptions;

  try {
    establishProtocol ();
  }
  catch (IB_SQLException* e) {
    jibsNet_.resetMessage ();
    jibsNet_.put_code (MessageCodes::FAILURE);
    jibsNet_.sendProtocolMessage (determineByteswap ());
#ifdef WIN32
    // ExitThread (1);
  ExitProcess (1);
#else
    exit (1);
#endif
  }

  for (;;) {

      IB_BOOLEAN sendEndOfResultSetHeader = IB_FALSE;
      IB_BOOLEAN sendPrefixFor_BLOB_OPEN_OR_GET = IB_FALSE;
      
      IB_BOOLEAN header_endOfResultSet = IB_FALSE;

      IB_SLONG32 header_lastBlobSegment = 0;
      IB_SLONG32 header_actualSegmentSize = 0;

    try {
      jibsNet_.recvMessage ();

      opcode = jibsNet_.get_sbyte ();

      exceptions = NULL;

      switch (opcode) {
      case MessageCodes::TEST_ECHO:
	test_echo ();
	break;
      case MessageCodes::ATTACH_SERVER_MANAGER:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "ATTACH_SERVER_MANAGER");
#endif
	attach_server ();
	break;
      case MessageCodes::DETACH_SERVER_MANAGER:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "DETACH_SERVER_MANAGER");
#endif
	detach_server ();
        put_WARNINGS ();
        jibsNet_.sendMessage ();
	return;
      case MessageCodes::ATTACH_DATABASE:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "ATTACH_DATABASE");
#endif
	attach_database ();
	break;
      case MessageCodes::DETACH_DATABASE:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "DETACH_DATABASE");
#endif
	detach_database ();
        put_WARNINGS ();
        jibsNet_.sendMessage ();
	return;
      case MessageCodes::PING:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "PING");
#endif
	ping ();
	break;
      case MessageCodes::SUSPEND_CONNECTION:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "SUSPEND_CONNECTION");
#endif
	suspend_connection ();
	break;
      case MessageCodes::RESUME_CONNECTION:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "RESUME_CONNECTION");
#endif
	resume_connection ();
	break;
      case MessageCodes::COMMIT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "COMMIT");
#endif
	commit ();
	break;
      case MessageCodes::ROLLBACK:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "ROLLBACK");
#endif
	rollback ();
	break;
      case MessageCodes::EXECUTE_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_STATEMENT");
#endif
	sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = execute_statement ();
	break;
      case MessageCodes::EXECUTE_QUERY_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_QUERY_STATEMENT");
#endif
	sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = execute_query_statement ();
	break;
      case MessageCodes::EXECUTE_UPDATE_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_UPDATE_STATEMENT");
#endif
	execute_update_statement ();
	break;
      case MessageCodes::PREPARE_STATEMENT:
      case MessageCodes::PREPARE_CALL:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "PREPARE_STATEMENT");
#endif
	prepare_statement ();
	break;
      case MessageCodes::EXECUTE_PREPARED_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_PREPARED_STATEMENT");
#endif
	sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = execute_prepared_statement ();
	break;
      case MessageCodes::EXECUTE_PREPARED_QUERY_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_PREPARED_QUERY_STATEMENT");
#endif
        sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = execute_prepared_query_statement ();
	break;
      case MessageCodes::EXECUTE_PREPARED_UPDATE_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_PREPARED_UPDATE_STATEMENT");
#endif
	execute_prepared_update_statement ();
	break;
      case MessageCodes::CANCEL_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "CANCEL_STATEMENT");
#endif
	cancel_statement ();
	break;
      case MessageCodes::CLOSE_STATEMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "CLOSE_STATEMENT");
#endif
	close_statement ();
	break;
      case MessageCodes::FETCH_ROWS:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "FETCH_ROWS");
#endif
        sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = fetch_rows ();
	break;
      case MessageCodes::CLOSE_CURSOR:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "CLOSE_CURSOR");
#endif
	close_cursor ();
	break;
      case MessageCodes::OPEN_BLOB:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "OPEN_BLOB");
#endif
        sendPrefixFor_BLOB_OPEN_OR_GET = IB_TRUE;
	open_blob (header_actualSegmentSize, header_lastBlobSegment);
	break;
      case MessageCodes::GET_BLOB_SEGMENT:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "GET_BLOB_SEGMENT");
#endif
        sendPrefixFor_BLOB_OPEN_OR_GET = IB_TRUE;
	get_blob_segments (header_actualSegmentSize, header_lastBlobSegment);
	break;
      case MessageCodes::CREATE_BLOB:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "CREATE_BLOB");
#endif
	create_blob ();
	break;
      case MessageCodes::CLOSE_BLOB:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "CLOSE_BLOB");
#endif
	close_blob ();
	break;
      case MessageCodes::GET_RESULT_COLUMN_META_DATA:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "GET_RESULT_COLUMN_META_DATA");
#endif
	get_result_column_meta_data ();
	break;
      case MessageCodes::EXECUTE_CATALOG_QUERY:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "EXECUTE_CATALOG_QUERY");
#endif
        sendEndOfResultSetHeader = IB_TRUE;
	header_endOfResultSet = execute_catalog_query ();
        break;
      // MMM - added new opcode GET_ARRAY_DESCRIPTOR
      case MessageCodes::GET_ARRAY_DESCRIPTOR:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "GET_ARRAY_DESCRIPTOR");
#endif
	get_array_descriptor ();
	break;
      // MMM - end
      // MMM - added new opcode GET_ARRAY_SLICE
      case MessageCodes::GET_ARRAY_SLICE:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "GET_ARRAY_SLICE");
#endif
	get_array_slice ();
	break;
      // MMM - end
      case MessageCodes::SERVICE_REQUEST:
#ifdef TRACEON
debugTraceALine ("Received incoming message ", "SERVER_REQUEST");
#endif
        server_request ();
        break;
      default:
	throw new IB_SQLException (IB_SQLException::remoteProtocol__unexpected_token_from_client__,
                                   IB_SQLException::remoteProtocolException__);
      }

      if (sendEndOfResultSetHeader) {
        put_WARNINGS ();
        jibsNet_.sendMessage (header_endOfResultSet);
      }
      else if (sendPrefixFor_BLOB_OPEN_OR_GET) { // !! no warnings sent with blob fetches
        jibsNet_.sendMessage (header_actualSegmentSize, header_lastBlobSegment);
        jibsNet_.sendBlobData (header_actualSegmentSize);
      }
      else {
        put_WARNINGS ();
        jibsNet_.sendMessage ();
      }
    }
    catch (CommunicationSQLException* e) {
      // currently the only cause for a CommunicationSQLException is
      // when the message certificate doesn't match.
      session_.close ();
      jibsNet_.resetMessage ();
#ifdef TRACEON
debugTraceALine ("FAILURE", "");
#endif
      jibsNet_.put_code (MessageCodes::FAILURE);
      put_EXCEPTIONS (e);
      try {
      if (sendEndOfResultSetHeader)
        jibsNet_.sendMessage (header_endOfResultSet);
      else if (sendPrefixFor_BLOB_OPEN_OR_GET)
        jibsNet_.sendMessage (header_actualSegmentSize, header_lastBlobSegment);
      else
        jibsNet_.sendMessage ();
      }
      catch (IB_SQLException* e) {};
#ifdef WIN32
      // ExitThread (1);
      ExitProcess (1);
#else
      exit (1);
#endif
    }
    catch (IB_SQLException* e) {
      jibsNet_.resetMessage ();
#ifdef TRACEON
debugTraceALine ("FAILURE", "");
#endif
      jibsNet_.put_code (MessageCodes::FAILURE);
      put_EXCEPTIONS (e);
      if (sendEndOfResultSetHeader)
        jibsNet_.sendMessage (header_endOfResultSet);
      else if (sendPrefixFor_BLOB_OPEN_OR_GET)
        jibsNet_.sendMessage (header_actualSegmentSize, header_lastBlobSegment);
      else
        jibsNet_.sendMessage ();
      continue;
    }
  }
}

IB_SLONG32 
JIBSRemote::determineByteswap ()
{
  int i = 1;
  char *c = (char *) &i;
  if (c[3] == 1)
    return 0; // Big Endian
  else
    return 1; // Little Endian
}

void
JIBSRemote::establishProtocol ()
{
  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);

  // salt seed
  jibsNet_.put_slong32 ((IB_SLONG32) &session_);
  generateSalt ((IB_SLONG32) &session_);

  // server version information follows
  jibsNet_.put_slong32 (VersionInformation::REMOTE_MESSAGE_CERTIFICATE__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_MAJOR_VERSION__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_MINOR_VERSION__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_BUILD_NUMBER__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_BUILD_LEVEL__);

  // To send back license file expiration date
  //   jibsNet_.put_slong32 (lic_data.dat_year-1900);
  //   jibsNet_.put_slong32 (lic_data.dat_month-1);
  //   jibsNet_.put_slong32 (lic_data.dat_day);
  // To send back hard-coded interserver expiration date
  jibsNet_.put_slong32 (VersionInformation::SERVER_EXPIRATION_YEAR__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_EXPIRATION_MONTH__);
  jibsNet_.put_slong32 (VersionInformation::SERVER_EXPIRATION_DAY__);

  jibsNet_.put_ldstring (strlen (VersionInformation::SERVER_NAME__),
			 VersionInformation::SERVER_NAME__); // ASCII only

  jibsNet_.sendProtocolMessage (determineByteswap ());
}


void
JIBSRemote::generateSalt (IB_SLONG32 saltSeed) 
{
  // should generate salts for all types
  // start with byte .... and then short, int
  // Strings are just sequence of bytes
  // arrays are to be encrypted as a sequence of primitive types

  // get low byte, should be the most varying
  IB_SBYTE byteSaltSeed = (IB_SBYTE)saltSeed;
  int i;
  for (i = 0; i <10; i++)
    byteSalt_[i] =   (IB_SBYTE) ((byteSaltSeed + i ) ^ (byteSaltSeed -i));

  // hardcode salt for debug
  // for (i = 0; i <10; i++)
  //  byteSalt_[i] =   (IB_SBYTE) 123;

  // get low word, should be the most varying
  IB_SSHORT16 shortSaltSeed = (IB_SSHORT16)saltSeed;
  for (i = 0; i <10; i++)
    shortSalt_[i] =   (IB_SSHORT16) ((shortSaltSeed + i ) ^ (shortSaltSeed -i));

    // get low word, should be the most varying
  IB_SLONG32 intSaltSeed = saltSeed;
  for (i = 0; i <10; i++)
    intSalt_[i] =   (intSaltSeed + i ) ^ (intSaltSeed -i);
}

void JIBSRemote::stringCrypt (IB_LDBytes& from, IB_LDString& to)
{
  int i;
  for (i = 0; i< from.length_; i++) {
    from.value_[i] = (IB_SBYTE) (from.value_[i] ^ byteSalt_[i%9]);
  }
  // !!!RRK
  from.value_[i] = 0;
  to.string_ = (IB_STRING) from.value_;
  to.length_ = from.length_;
}

IB_UBYTE JIBSRemote::byteCrypt (IB_UBYTE value)
{
  return (IB_UBYTE) (value ^ byteSalt_[sizeof(IB_UBYTE)]);
}

IB_SSHORT16 JIBSRemote::shortCrypt (IB_SSHORT16 value)
{
  return (IB_SSHORT16) (value ^ shortSalt_[sizeof(IB_SSHORT16)]);
}

IB_SLONG32 JIBSRemote::intCrypt (IB_SLONG32 value)
{
  return (IB_SLONG32) (value ^ intSalt_[sizeof(IB_SLONG32)]);
}

void
JIBSRemote::test_echo ()
{
  IB_SBYTE ibByte;
  IB_SSHORT16 ibShort;
  IB_SLONG32 ibLong;
  IB_STRING ibString;
  IB_REF ibRef;

  float f;
  double d;
  ibByte = jibsNet_.get_sbyte ();
  ibShort = jibsNet_.get_sshort16 ();
  ibLong = jibsNet_.get_slong32 ();
  ibRef = jibsNet_.get_ref();
  f = jibsNet_.get_float32();
  d = jibsNet_.get_double64();
  ibString = jibsNet_.get_string ();
  IB_LDBytes b;
  IB_LDString s;
  jibsNet_.get_ldbytes (b);
  stringCrypt (b, s);
  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_sbyte (ibByte);
  jibsNet_.put_sshort16 (ibShort);
  jibsNet_.put_slong32 (ibLong);
  jibsNet_.put_ref (ibRef);
  jibsNet_.put_float32 (f);
  jibsNet_.put_double64 (d);
  jibsNet_.put_ldstring (ibString);
  jibsNet_.put_ldstring (s);
}

void 
JIBSRemote::server_request ()
{
  IB_SBYTE subOpcode = jibsNet_.get_sbyte ();
  switch (subOpcode) {
  case MessageCodes::CREATE_DATABASE:
    create_database ();
    break;
  default:
    throw new IB_SQLException (IB_SQLException::remoteProtocol__unexpected_token_from_client__,
			       IB_SQLException::remoteProtocolException__);
  }
}

void 
JIBSRemote::create_database ()
{
  IB_STRING database = jibsNet_.get_string ();
  session_.createDatabase (database);
  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::attach_database ()
{
#ifdef WIN32OLDSTUFF
#ifdef LICENSING
  if (badLicense_)
    {
    /* The server is running as a service */
    if (is_window_service)
	SVC_PostEventLogMsg (IC_EVENTERR_NOT_LICENSED);
	{
	char szMsgString[TMP_STRINGLEN];
	LoadString (handle, IMM_NOT_LICENSED, szMsgString, TMP_STRINGLEN);
	MessageBox(NULL,
                   szMsgString, 
                   VersionInformation::APPLICATION_NAME__, 
                   MB_OK | MB_ICONHAND );
	}
    EXIT(0); 
    }
#endif /* LICENSING */
#endif

  // Get the properties list
  // A configuration parameter can be used to configure either
  // connectionConfiguration_ or transactionConfiguration_, both of which
  // are encapsulated by session_.
  int propertyCount;
  propertyCount = (IB_SBYTE) jibsNet_.get_sbyte ();
  IB_STRING keyword;
  IB_LDString value;
  IB_LDBytes cryptValue;
  for (int i = 0; i < propertyCount; i++) {
    keyword = jibsNet_.get_string ();
    if ((strcmp (keyword, "user") == 0) ||
       (strcmp (keyword, "password") == 0)) {
      jibsNet_.get_ldbytes (cryptValue);
      stringCrypt (cryptValue, value);
    } else
      jibsNet_.get_ldstring (value);
    session_.configure (keyword, value);
  }

  IB_SLONG32 timeout;
  IB_LDString database;
  timeout = (IB_SLONG32) jibsNet_.get_sshort16 ();
  jibsNet_.get_ldstring (database);

  IB_LDString ibProductVersion;
  IB_SLONG32 ibMajorVersion;
  IB_SLONG32 odsMajorVersion;
  IB_SLONG32 odsMinorVersion;
  IB_SLONG32 pageSize;
  IB_SLONG32 pageAllocation;
// CJL-IB6 sqlDialect and read-only support
  IB_SLONG32 dbSQLDialect;   // new for IB6
  IB_BOOLEAN dbReadOnly;     // new for IB6

  session_.open (timeout, 
		 database, 
		 ibProductVersion, 
		 ibMajorVersion,
		 odsMajorVersion, 
		 odsMinorVersion, 
		 pageSize, 
		 pageAllocation,
		 dbSQLDialect,     // new for IB6
		 dbReadOnly);      // new for IB6

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);

  // Write sessionRef
  jibsNet_.put_ref ((IB_REF) &session_);
  jibsNet_.put_ldstring (ibProductVersion); // ASCII or UNICODE
  jibsNet_.put_slong32 (ibMajorVersion);
  jibsNet_.put_slong32 (odsMajorVersion);
  jibsNet_.put_slong32 (odsMinorVersion);
  jibsNet_.put_slong32 (pageSize);
  jibsNet_.put_slong32 (pageAllocation);
  jibsNet_.put_slong32 (dbSQLDialect);  // new for IB6
  jibsNet_.put_slong32 ((IB_SLONG32) session_.connection_.attachmentSQLDialect_ ); // new for IB6
  jibsNet_.put_boolean (dbReadOnly);  // new for IB6
// CJL-IB6 end.
}

void
JIBSRemote::detach_database ()
{
  session_.close ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::attach_server ()
{
#ifdef WIN32OLDSTUFF
#ifdef LICENSING
  if (badLicense_)
    {
    /* The server is running as a service */
    if (is_window_service)
	SVC_PostEventLogMsg (IC_EVENTERR_NOT_LICENSED);
	{
	char szMsgString[TMP_STRINGLEN];
	LoadString (handle, IMM_NOT_LICENSED, szMsgString, TMP_STRINGLEN);
	MessageBox(NULL,
                   szMsgString, 
                   VersionInformation::APPLICATION_NAME__, 
                   MB_OK | MB_ICONHAND );
	}
    EXIT(0); 
    }
#endif /* LICENSING */
#endif

  // Get the properties list
  // A configuration parameter can be used to configure either
  // connectionConfiguration_ or transactionConfiguration_, both of which
  // are encapsulated by session_.
  int propertyCount;
  propertyCount = (IB_SBYTE) jibsNet_.get_sbyte ();
  IB_STRING keyword;
  IB_LDString value;
  IB_LDBytes cryptValue;
  for (int i = 0; i < propertyCount; i++) {
    keyword = jibsNet_.get_string ();
    if ((strcmp (keyword, "user") == 0) ||
       (strcmp (keyword, "password") == 0)) {
      jibsNet_.get_ldbytes (cryptValue);
      stringCrypt (cryptValue, value);
    } else
      jibsNet_.get_ldstring (value);
    session_.configure (keyword, value);
  }

  IB_SLONG32 timeout;
  timeout = (IB_SLONG32) jibsNet_.get_sshort16 ();

  // note: unlike attach_database, session is not opened

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);

  // Write sessionRef
  jibsNet_.put_ref ((IB_REF) &session_);
}

void
JIBSRemote::detach_server ()
{
  //session_.close ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::suspend_connection ()
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
JIBSRemote::resume_connection ()
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void
JIBSRemote::ping ()
{
  IB_BOOLEAN isOpen = session_.ping ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_boolean (isOpen);
}

void
JIBSRemote::commit ()
{
  IB_BOOLEAN retain = jibsNet_.get_boolean (); 

  if (retain) 
    session_.commitRetain ();
  else
    session_.commit ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::rollback ()
{
  session_.rollback ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::prepare_statement ()
{
  getAndStartTransactionIfPending ();

  IB_STRING sql = jibsNet_.get_string (); 
  IB_BOOLEAN isEscapedProcedureCall = jibsNet_.get_boolean (); 

  IB_Statement* statement;
  if (isEscapedProcedureCall)
    statement = session_.prepareEscapedCall (sql);
  else
    statement = session_.prepareStatement (sql);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) statement);
  putInputMetaData (statement);

  IB_ResultSet* resultSet = statement->getResultSet ();
  if (resultSet == NULL) {
    // jibsNet_.put_byte (statement->getResultSetCount());
    jibsNet_.put_sbyte (0);
  }
  else {
    // jibsNet_.put_byte (statement->getResultSetCount());
    jibsNet_.put_sbyte (1);
    putResultMetaData (statement->getResultSet());
  }
}

IB_BOOLEAN
JIBSRemote::execute_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_STRING sql;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;
  IB_BOOLEAN endOfResultSet = IB_TRUE;

  getStatementExecuteData (statement,
			   cursorName,
			   sql,
			   timeout,
			   maxFieldSize,
			   fetchSize);

  IB_SLONG32 updateCount;
  IB_ResultSet* resultSet = session_.executeStatement (statement, 
						       updateCount,
						       sql, 
						       (IB_SLONG32) timeout, 
						       maxFieldSize, 
						       fetchSize,
						       cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) statement);

  if (resultSet == NULL) {
    // jibsNet_.put_byte (statement->getResultSetCount());
    jibsNet_.put_sbyte (0);
    jibsNet_.put_slong32 (updateCount);
  }
  else {
    // jibsNet_.put_byte (statement->getResultSetCount());
    jibsNet_.put_sbyte (1);
    putResultMetaData (resultSet);
    jibsNet_.put_slong32 (0); // resultSet->getRowCount ();
    endOfResultSet = putResultData (resultSet);
  }
  return endOfResultSet;
}

IB_BOOLEAN
JIBSRemote::execute_query_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_STRING sql;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;
  IB_BOOLEAN endOfResultSet = IB_TRUE;

  getStatementExecuteData (statement,
			   cursorName,
			   sql,
			   timeout,
			   maxFieldSize,
			   fetchSize);

  IB_ResultSet* resultSet = session_.executeQueryStatement (statement, 
							    sql, 
							    (IB_SLONG32) timeout, 
							    maxFieldSize, 
							    fetchSize,
							    cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) statement);
  putResultMetaData (resultSet);
  jibsNet_.put_slong32 (0); // resultSet->getRowCount ();
  endOfResultSet = putResultData (resultSet);
  return endOfResultSet;
}

void
JIBSRemote::execute_update_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_STRING sql;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;

  getStatementExecuteData (statement,
			   cursorName,
			   sql,
			   timeout,
			   maxFieldSize,
			   fetchSize);

  IB_SLONG32 updateCount;
  session_.executeUpdateStatement (statement, 
				   updateCount,
				   sql, 
				   (IB_SLONG32) timeout, 
				   maxFieldSize,
                                   cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) statement);
  jibsNet_.put_slong32 (updateCount);
}

// Called by execute_*_statement()
void
JIBSRemote::getStatementExecuteData (IB_Statement*& statement,
				     IB_STRING& cursorName,
				     IB_STRING& sql, 
				     IB_SSHORT16& timeout, 
				     IB_SSHORT16& maxFieldSize,
				     IB_SLONG32& fetchSize)
{
  statement = (IB_Statement *) jibsNet_.get_ref ();
  cursorName = jibsNet_.get_string ();
  getAndStartTransactionIfPending ();
  sql = jibsNet_.get_string ();
  timeout = jibsNet_.get_sshort16 ();
  maxFieldSize = jibsNet_.get_sshort16 ();
  fetchSize = jibsNet_.get_slong32 ();
}

// Called by prepare_statement(), execute_*_statement(), execute_prepared_*_statement()
void
JIBSRemote::getAndStartTransactionIfPending ()
{
  IB_BOOLEAN configDataFollows = jibsNet_.get_boolean ();

  IB_BOOLEAN readOnly = IB_FALSE;
  IB_SBYTE isolation = 0;
  IB_BOOLEAN versionAcknowledgment = IB_FALSE;
  IB_SBYTE lockResolution = 0;
  IB_BOOLEAN enableAutoCommit = IB_FALSE;
  IB_BOOLEAN enableAutoClose = IB_FALSE;
  IB_SLONG32 numTableLocks;
  IB_LDString tableName;
  IB_SBYTE tableLock;

  if (configDataFollows) {
    readOnly = jibsNet_.get_boolean ();
    isolation = jibsNet_.get_sbyte ();
    versionAcknowledgment = jibsNet_.get_boolean ();
    lockResolution = jibsNet_.get_sbyte ();
    enableAutoCommit = jibsNet_.get_boolean ();
    enableAutoClose = jibsNet_.get_boolean ();
    numTableLocks = jibsNet_.get_slong32 ();
    for (int i=0; i<numTableLocks; i++) {
      tableName = jibsNet_.get_ldstring ();
      tableLock = jibsNet_.get_sbyte ();
      session_.transactionConfiguration_.setTableLock ((IB_TransactionConfiguration::TableLockMode) tableLock, 
						       tableName);
    }
  }

  // !!! A customer has reported this bug code 10019
  if (!session_.transactionStarted ()) {
    if (!configDataFollows) { // Transaction has been lost on server
      throw new IB_SQLException (IB_SQLException::bugCheck__0__,
				 10019,
				 IB_SQLException::bugCheckException__);
    }
    session_.startTransaction (readOnly, isolation, versionAcknowledgment, lockResolution,
			       enableAutoCommit, enableAutoClose);
  }
}

IB_BOOLEAN
JIBSRemote::execute_prepared_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;
  IB_BOOLEAN endOfResultSet = IB_TRUE;

  getPreparedStatementExecuteData (statement, cursorName, timeout, maxFieldSize, fetchSize);

  IB_SLONG32 updateCount;
  IB_ResultSet* resultSet = session_.executePreparedStatement (statement, 
							       updateCount, 
							       timeout, 
							       maxFieldSize, 
							       fetchSize,
							       cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  if (resultSet == NULL)
    jibsNet_.put_slong32 (updateCount);
  else {
    jibsNet_.put_slong32 (0); // resultSet->getRowCount ();
    endOfResultSet = putResultData (resultSet);
  }
  return endOfResultSet;
}

IB_BOOLEAN
JIBSRemote::execute_prepared_query_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;
  IB_BOOLEAN endOfResultSet;

  getPreparedStatementExecuteData (statement, cursorName, timeout, maxFieldSize, fetchSize);

  IB_ResultSet* resultSet = session_.executePreparedQueryStatement (statement, 
								    timeout, 
								    maxFieldSize,
								    fetchSize,
								    cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_slong32 (0); // resultSet->getRowCount (); 
  endOfResultSet = putResultData (resultSet);
  return endOfResultSet;
}

void
JIBSRemote::execute_prepared_update_statement ()
{
  IB_Statement *statement;
  IB_STRING cursorName;
  IB_SSHORT16 timeout;
  IB_SSHORT16 maxFieldSize;
  IB_SLONG32 fetchSize;

  getPreparedStatementExecuteData (statement, cursorName, timeout, maxFieldSize, fetchSize);

  IB_SLONG32 updateCount;
  session_.executePreparedUpdateStatement (statement, 
					   updateCount, 
					   timeout, 
					   maxFieldSize,
                                           cursorName);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_slong32 (updateCount);
}

// Called by execute_prepared_*_statement()
void
JIBSRemote::getPreparedStatementExecuteData (IB_Statement*& statement, 
					     IB_STRING& cursorName,
					     IB_SSHORT16& timeout, 
					     IB_SSHORT16& maxFieldSize,
					     IB_SLONG32& fetchSize)
{
  IB_Blob *blob;
  statement = (IB_Statement *) jibsNet_.get_ref ();
  cursorName = jibsNet_.get_string ();

  getAndStartTransactionIfPending ();

  timeout = jibsNet_.get_sshort16 ();
  maxFieldSize = jibsNet_.get_sshort16 ();
  fetchSize = jibsNet_.get_slong32 ();

  IB_SSHORT16 inputCols = statement->getParameterCount ();

  IB_SLONG32 tempLong;
  IB_DOUBLE64 tempDouble;
  IB_SSHORT16 tempShort;
  IB_SSHORT16 tempScale;
// CJL-IB6 added for Int64 support
	IB_SINT64 tempInt64;
// CJL-IB6 end change
    
  for (IB_SSHORT16 col=0; col < inputCols; col++) {
    if (statement->isParameterNullable (col) ) {
      if (jibsNet_.get_boolean ()) {
        statement->setNull (col, IB_TRUE);
	continue;
      }
      else {
	statement->setNull (col, IB_FALSE);
      }
    }

    switch (statement->getParameterType (col)) {

    case IB_Types::SMALLINT_TYPE:
      statement->setSmallInt (col, jibsNet_.get_sshort16 ()); 
      break;

    case IB_Types::INTEGER_TYPE:
      statement->setInteger (col, jibsNet_.get_slong32 ()); 
      break;

    case IB_Types::FLOAT_TYPE:
      statement->setFloat (col, jibsNet_.get_float32 ()); 
      break;

    case IB_Types::DOUBLE_TYPE:
      statement->setDouble (col, jibsNet_.get_double64 ()); 
      break;

    case IB_Types::NUMERIC_DOUBLE_TYPE:
      tempDouble = jibsNet_.get_double64 ();
      tempScale = jibsNet_.get_sbyte();
      statement->setNumericDouble (col, tempDouble, tempScale); 
      break;

    case IB_Types::NUMERIC_INTEGER_TYPE:
    case IB_Types::DECIMAL_INTEGER_TYPE:   // CJL-IB6 added for new type support
      tempLong = jibsNet_.get_slong32 ();
      tempScale = jibsNet_.get_sbyte();
      statement->setNumericInteger (col, tempLong, tempScale); 
      break;

    case IB_Types::NUMERIC_SMALLINT_TYPE:
      tempShort = jibsNet_.get_sshort16 ();
      tempScale = jibsNet_.get_sbyte();
      statement->setNumericSmallInt (col, tempShort, tempScale); 
      break;

    case IB_Types::CHAR_TYPE:
      statement->setChar (col, jibsNet_.get_ldstring ()); 
      break;

    case IB_Types::VARCHAR_TYPE:
      statement->setVarChar (col, jibsNet_.get_ldstring ()); 
      break;

    case IB_Types::DATE_TYPE:
      statement->setDate (col, jibsNet_.get_timestamp ()); 
      break;

    case IB_Types::CLOB_TYPE: // !!!
      blob = (IB_Blob *) jibsNet_.get_ref ();
      statement->setBinaryBlob (col, &(blob->blobId_)); 
      //statement->setAsciiBlob (col, &(blob->blobId_)); 
      break;

    case IB_Types::BLOB_TYPE:
      blob = (IB_Blob *) jibsNet_.get_ref ();
      statement->setBinaryBlob (col, &(blob->blobId_)); 
      break;

    case IB_Types::ARRAY_TYPE:
      // MMM - get and set array data
      // !!! it might be worth to rename the method setArrayId
      statement->setArray (col, jibsNet_.get_arrayId ());
      getArray (statement, col);
      // MMM - end
      break;

// CJL-IB6  support for new types
		case IB_Types::NUMERIC_INT64_TYPE:
		case IB_Types::DECIMAL_INT64_TYPE:    
      tempInt64 = jibsNet_.get_sint64 ();
      tempScale = jibsNet_.get_sbyte();
      statement->setNumericInt64 (col, tempInt64, tempScale); 
      break;

		case IB_Types::SQL_DATE_TYPE:    
      statement->setSQLDate (col, jibsNet_.get_slong32 () ); 
      break;

		case IB_Types::TIME_TYPE:    
      statement->setTime (col, jibsNet_.get_ulong32 ()); 
      break;
// CJL-IB6 end change

    default:
      break;
    }
  }
}

IB_BOOLEAN
JIBSRemote::fetch_rows ()
{
  IB_Statement* statement;
  IB_ResultSet* resultSet;
  IB_SLONG32 fetchSize;
  IB_BOOLEAN header_endOfResultSet;

  statement = (IB_Statement *) jibsNet_.get_ref ();
  fetchSize = jibsNet_.get_slong32 ();

  resultSet = statement->getResultSet ();
  resultSet->setFetchSize (fetchSize);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  header_endOfResultSet = putResultData (resultSet);
  return (header_endOfResultSet);
}


void
JIBSRemote::close_cursor ()
{
  IB_Statement* statement;
  IB_ResultSet* resultSet;

  statement = (IB_Statement *) jibsNet_.get_ref ();

  resultSet = statement->getResultSet ();

  resultSet->close ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::cancel_statement ()
{
  IB_Statement *statement;
  statement = (IB_Statement *) jibsNet_.get_ref ();

  statement->cancel ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::close_statement ()
{
  IB_Statement *statement;
  statement = (IB_Statement *) jibsNet_.get_ref ();

  delete statement;

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::open_blob (IB_SLONG32& actualSegmentSize, 
                       IB_SLONG32& lastSegment)
{
  IB_Statement *statement;
  IB_BLOBID blobId;
  
  statement = (IB_Statement *) jibsNet_.get_ref ();
  jibsNet_.get_blobId (blobId);

  IB_Blob *blob = new IB_Blob (*statement, blobId);
  blob->open ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) blob);
  jibsNet_.put_slong32 (blob->size ());
  jibsNet_.put_code (MessageCodes::END_WARNINGS); // !! use separate buffer for blobs

  actualSegmentSize = jibsNet_.put_BlobData (blob);

  if (blob->atEnd ()) {
    blob->close(); 
    lastSegment = 1;
  }
}

void
JIBSRemote::get_blob_segments (IB_SLONG32& actualSegmentSize, 
                               IB_SLONG32& lastSegment)
{
  IB_Blob *blob = (IB_Blob *) jibsNet_.get_ref ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_code (MessageCodes::END_WARNINGS); // !! use separate buffer for blobs

  actualSegmentSize = jibsNet_.put_BlobData (blob);

  if (blob->atEnd ()) {
    blob->close ();
    lastSegment = 1;
  }
}

void
JIBSRemote::create_blob ()
{
  IB_Statement *statement;
  
  statement = (IB_Statement *) jibsNet_.get_ref ();
  IB_SLONG32 blobSize = jibsNet_.get_slong32 ();

  IB_Blob *blob = new IB_Blob (*statement);
  blob->create();
  jibsNet_.get_BlobData (blob, blobSize);
  blob->close ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) blob);
}

void
JIBSRemote::close_blob ()
{
  IB_Blob *blob;
  blob = (IB_Blob *)jibsNet_.get_ref ();
  blob->close ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
}

void
JIBSRemote::get_result_column_meta_data ()
{
  IB_Statement* statement;
  IB_ResultSet* resultSet;

  statement = (IB_Statement *) jibsNet_.get_ref ();

  resultSet = statement->getResultSet ();

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);

  // Construct an auxillary query to determine writables
  IB_Statement* writablesStatement = session_.createStatement ();

  // the input column name is returned if the column is writable.
  // otherwise an empty set is returned

  // inefficient for now, in java, use a hash table column name mapping
  // and concatencate string

  IB_ResultSet* writablesResultSet =
    writablesStatement->prepare ("select" 
				 " RF.RDB$FIELD_NAME "
				 "from"
				 " RDB$RELATIONS REL,"
				 " RDB$RELATION_FIELDS RF," 
				 " RDB$FIELDS FLD,"
				 " RDB$USER_PRIVILEGES PRV "
				 "where"
                                 " RF.RDB$FIELD_NAME = ? and"
				 " REL.RDB$RELATION_NAME = ? and"
				 " REL.RDB$RELATION_NAME = RF.RDB$RELATION_NAME and"
				 " RF.RDB$FIELD_SOURCE = FLD.RDB$FIELD_NAME and"
				 " REL.RDB$VIEW_BLR IS NULL and"      // table is not a view
				 " FLD.RDB$COMPUTED_BLR IS NULL and"  // not a computed field
				 " PRV.RDB$RELATION_NAME = REL.RDB$RELATION_NAME and"
				 " (PRV.RDB$FIELD_NAME = RF.RDB$FIELD_NAME or"
				 "  PRV.RDB$FIELD_NAME IS NULL) and"
				 " PRV.RDB$PRIVILEGE in ('A', 'I', 'U', 'D') and" 
				 " (PRV.RDB$USER = 'PUBLIC' or"
				 "  PRV.RDB$USER = ?)");
 
  (writablesStatement->sqldaIn_->sqlvar+0)->sqltype = SQL_VARYING; 
  (writablesStatement->sqldaIn_->sqlvar+1)->sqltype = SQL_VARYING; 
  (writablesStatement->sqldaIn_->sqlvar+2)->sqltype = SQL_VARYING; 
  (writablesStatement->sqldaIn_->sqlvar+0)->sqllen = 31; 
  (writablesStatement->sqldaIn_->sqlvar+1)->sqllen = 31; 
  (writablesStatement->sqldaIn_->sqlvar+2)->sqllen = 31; 
  // 31 bytes for characters, and 2 bytes for short varchar length indicator
  (writablesStatement->sqldaIn_->sqlvar+0)->sqldata = (IB_BUFF_PTR) malloc ((size_t) 33); 
  (writablesStatement->sqldaIn_->sqlvar+1)->sqldata = (IB_BUFF_PTR) malloc ((size_t) 33); 
  (writablesStatement->sqldaIn_->sqlvar+2)->sqldata = (IB_BUFF_PTR) malloc ((size_t) 33); 

  int resultCols = resultSet->getColumnCount ();
  for (int col=0; col < resultCols; col++) {
    IB_Statement::varcharCpy ((writablesStatement->sqldaIn_->sqlvar+0)->sqldata,
			      (resultSet->sqlda_->sqlvar+col)->sqlname_length,
			      (resultSet->sqlda_->sqlvar+col)->sqlname);
    IB_Statement::varcharCpy ((writablesStatement->sqldaIn_->sqlvar+1)->sqldata,
			      (resultSet->sqlda_->sqlvar+col)->relname_length,
			      (resultSet->sqlda_->sqlvar+col)->relname);
    IB_Statement::varcharCpy ((writablesStatement->sqldaIn_->sqlvar+2)->sqldata,
			      session_.userLength_,
			      session_.user_);

    writablesResultSet->open ();
    jibsNet_.put_boolean (writablesResultSet->next ()); // true if a row is returned
    writablesResultSet->close ();
  }

  writablesStatement->close ();
  free ((writablesStatement->sqldaIn_->sqlvar+0)->sqldata);
  free ((writablesStatement->sqldaIn_->sqlvar+1)->sqldata);
  free ((writablesStatement->sqldaIn_->sqlvar+2)->sqldata);
}

void
JIBSRemote::put_WARNINGS ()
{
  IB_SQLException* iterator = session_.getWarnings ();

  while (iterator) {
#ifdef TRACEON
debugTraceALine ("WARNING", "");
#endif
    jibsNet_.put_code (MessageCodes::WARNING);
    jibsNet_.put_slong32 (iterator->getErrorKey ());
    jibsNet_.put_slong32 (iterator->getReservedCode ());
    jibsNet_.put_slong32 (iterator->getErrorCode ());
    jibsNet_.put_slong32 (iterator->getIBSQLCode ());
    jibsNet_.put_ldstring (iterator->getIBErrorMessage ()); // UNICODE

    iterator = iterator->getNextException ();
  }

#ifdef TRACEON
debugTraceALine ("END_WARNINGS", "");
#endif
  jibsNet_.put_code (MessageCodes::END_WARNINGS);

  session_.clearWarnings ();
}

void
JIBSRemote::put_EXCEPTIONS (IB_SQLException* e)
{
  IB_SQLException* iterator = e;

  while (iterator) {
#ifdef TRACEON
debugTraceALine ("EXCEPTION", "");
#endif
    jibsNet_.put_code (MessageCodes::EXCEPTION);
    jibsNet_.put_slong32 (iterator->getErrorKey ());
    jibsNet_.put_slong32 (iterator->getReservedCode ());
    jibsNet_.put_slong32 (iterator->getErrorCode ());
    jibsNet_.put_slong32 (iterator->getIBSQLCode ());
    jibsNet_.put_ldstring (iterator->getIBErrorMessage ()); // UNICODE

    iterator = iterator->getNextException ();
  }

#ifdef TRACEON
debugTraceALine ("END_EXCEPTIONS", "");
#endif
  jibsNet_.put_code (MessageCodes::END_EXCEPTIONS);

  delete e;
  e = NULL;
}

// Called by execute_*_statement() and execute_prepared_*_statement()
IB_BOOLEAN
JIBSRemote::putResultData (IB_ResultSet* resultSet)
{
  if (resultSet->getCatalogFunction() != 0) {
    return putCatalogResultData (resultSet);
  }

  // The number of rows to be sent for each FETCH_ROWS or EXECUTE_* request.
  // !!! This could be computed once and for all when the statement is prepared
  // !!! but then the Statement/ResultSet classes are JDBC aware.
  // HP-UX port (old CC): added type cast (int)
  int bufferedRows = resultSet->getFetchSize ();
  if (!bufferedRows) {
    // Add one to ensure bufferedRows is never zero.
    // Add one to record size to avoid division by zero errors (eg. select '' from rdb$database).
    bufferedRows = 1 + (bufferedDataSize__ / (1 + (int) resultSet->getRecordSize ()));
  }

#ifdef TRACEON
debugTraceAnInt ("Fetch size = ", bufferedRows);
#endif

  IB_BOOLEAN endOfResultSet = IB_FALSE;
  IB_BOOLEAN isStoredProcCall = (resultSet->getStatementType () == isc_info_sql_stmt_exec_procedure);

  for (int row = 0; row < bufferedRows; row++) {

    if (!isStoredProcCall && !resultSet->next ()) {
      endOfResultSet = IB_TRUE;
      resultSet->close (); // auto-close cursor after last row is read
      break;
    }

#ifdef TRACEON
debugTraceALine ("ROW_DATUM", "");
#endif
    jibsNet_.put_code (MessageCodes::ROW_DATUM);
    for (int col = 0; col < resultSet->getColumnCount (); col++) {
      if (resultSet->isColumnNullable (col)) 
	jibsNet_.put_boolean (resultSet->isNull (col));
      if (!resultSet->isNull (col)) {
	switch (resultSet->getColumnType (col)) {
	case IB_Types::SMALLINT_TYPE:
	  jibsNet_.put_sshort16 (resultSet->getSmallInt (col)); 
	  break;
	case IB_Types::INTEGER_TYPE:
	  jibsNet_.put_slong32 (resultSet->getInteger (col)); 
	  break;
	case IB_Types::FLOAT_TYPE:
	  jibsNet_.put_float32 (resultSet->getFloat (col));
	  break;
	case IB_Types::DOUBLE_TYPE:
	  jibsNet_.put_double64 (resultSet->getDouble (col));
	  break;
// CJL-IB6 new type support
	case IB_Types::DECIMAL_INT64_TYPE:
	  jibsNet_.put_sint64 (resultSet->getInt64 (col));
	  break;
	case IB_Types::DECIMAL_INTEGER_TYPE:
	  jibsNet_.put_slong32 (resultSet->getInteger (col));
	  break;
// CJL-IB6 end change
	case IB_Types::NUMERIC_DOUBLE_TYPE:
	  jibsNet_.put_double64 (resultSet->getDouble (col));
	  break;
// CJL-IB6 new type support
	case IB_Types::NUMERIC_INT64_TYPE:
	  jibsNet_.put_sint64 (resultSet->getInt64 (col));
	  break;
// CJL-IB6 end change
	case IB_Types::NUMERIC_INTEGER_TYPE:
	  jibsNet_.put_slong32 (resultSet->getInteger (col));
	  break;
	case IB_Types::NUMERIC_SMALLINT_TYPE:
	  jibsNet_.put_sshort16 (resultSet->getSmallInt (col));
	  break;
	case IB_Types::CHAR_TYPE:
	  if (resultSet->getColumnCharSet (col) != IB_CharacterSets::OCTETS__)
	    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (col), 
				   resultSet->getChar (col));
	  else // OCTETS field
	     jibsNet_.put_ldstring (resultSet->getCharByteLength (col), 
				    resultSet->getChar (col));
	  break;
	case IB_Types::VARCHAR_TYPE:
	  jibsNet_.put_ldstring (resultSet->getVarCharByteLength (col),
				 resultSet->getVarChar (col));
	  break;
	case IB_Types::DATE_TYPE:
	  jibsNet_.put_timestamp (resultSet->getDate (col));
	  break;
// CJL-IB6 new type support
	case IB_Types::SQL_DATE_TYPE:
	  jibsNet_.put_slong32 (resultSet->getSQLDate (col));
	  break;
	case IB_Types::TIME_TYPE:
	  jibsNet_.put_ulong32 (resultSet->getTime (col));
	  break;
// CJL-IB6 end change
	case IB_Types::BLOB_TYPE:
	  jibsNet_.put_blobId (resultSet->getBinaryBlob (col));
	  break;
	case IB_Types::CLOB_TYPE:
	  jibsNet_.put_blobId (resultSet->getAsciiBlob (col));
	  // !!!jibsNet_.put_blobId (resultSet->getUnicodeBlob (col));
	  break;
	case IB_Types::ARRAY_TYPE:
	  jibsNet_.put_arrayId (resultSet->getArray (col));
	  break;
	default:
	  break;
	}
      }
    }
    if (isStoredProcCall) {
      endOfResultSet = IB_TRUE;
      resultSet->close (); // auto-close cursor after last row is read.
      break;
    }
  }
#ifdef TRACEON
debugTraceALine ("END_ROW_DATA", "");
debugTraceAnInt ("end of result set = ", endOfResultSet);
#endif
  jibsNet_.put_code (MessageCodes::END_ROW_DATA);
  return endOfResultSet;
}

// MMM - get array descriptor
// INSQLDA_NONAMES - this method is used when array descriptor can not
// be obtained from InterBase during prepare statement step. this would
// be the case if a version of InterBase in use does not provide table
// and column names in XSQLVAR structure for input parameters 
void
JIBSRemote::get_array_descriptor ()
{
  getAndStartTransactionIfPending ();

  IB_STRING tableName = jibsNet_.get_ldstring ().string_;
  IB_STRING columnName = jibsNet_.get_ldstring ().string_;

  ISC_ARRAY_DESC descriptor;
  session_.arrayLookupBounds (tableName, columnName, &descriptor);
  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_boolean (IB_TRUE);	// descriptor follows
  putArrayDescriptor(&descriptor);
}
// MMM - end

// MMM - get array or array slice
void
JIBSRemote::get_array_slice ()
{
  getAndStartTransactionIfPending ();

  ISC_ARRAY_DESC tmpdesc, *descriptor = &tmpdesc;

  IB_ARRAYID arrayId = jibsNet_.get_arrayId ();
  getArrayDescriptor (descriptor);

  // First, calculate and allocate space to hold the data
  IB_SLONG32 elements = descriptor->array_desc_bounds[0].array_bound_upper - 
                       descriptor->array_desc_bounds[0].array_bound_lower + 1;
  for (int dim = 1; dim < descriptor->array_desc_dimensions; dim++)
    elements *= (descriptor->array_desc_bounds[dim].array_bound_upper - 
                 descriptor->array_desc_bounds[dim].array_bound_lower + 1);

  // We are going to read an array of VARCHAR (blr_varying/blr_varying2)
  // as array of blr_cstring(s). Thus, we need to add a byte per each element
  // to hold the zero terminator.  
  char elementBlrType;
  int elementIBType = IB_Types::getIBTypeOfArrayElement (descriptor);
  if (elementIBType == IB_Types::VARCHAR_TYPE) {
    elementBlrType = descriptor->array_desc_dtype;
    descriptor->array_desc_dtype = blr_cstring;
    descriptor->array_desc_length++;
  }
  void *arrayData;
  IB_SLONG32 arrayDataLength = elements*descriptor->array_desc_length;

  if ((arrayData = (void *) malloc (arrayDataLength)) == NULL)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
                               IB_SQLException::outOfMemoryException__);

  session_.arrayGetSlice (&arrayId, descriptor, arrayData, &arrayDataLength);

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);

  // Put array data on the wire.
  // It might be a good idea to put this code in a separate function
  // like putArray (we already have getArray). It might improve the
  // code structure, but wont give better code reuse and will be a 
  // little less efficient.
  int i;
  switch (elementIBType) {

  // There is no need to get scale for each numeric data type.
  // It was received as a part of descriptor for all array elements.
  case IB_Types::SMALLINT_TYPE:
  case IB_Types::NUMERIC_SMALLINT_TYPE:
    for (i = 0; i < elements; i++)
      jibsNet_.put_sshort16 (((IB_SSHORT16*) arrayData)[i]); 
    break;

  case IB_Types::INTEGER_TYPE:
  case IB_Types::NUMERIC_INTEGER_TYPE:
    for (i = 0; i < elements; i++)
      jibsNet_.put_slong32 (((IB_SLONG32*) arrayData)[i]); 
    break;

  case IB_Types::FLOAT_TYPE:
    for (i = 0; i < elements; i++)
      jibsNet_.put_float32 (((IB_FLOAT32*) arrayData)[i]); 
    break;

  case IB_Types::DOUBLE_TYPE:
  case IB_Types::NUMERIC_DOUBLE_TYPE:
    for (i = 0; i < elements; i++)
      jibsNet_.put_double64 (((IB_DOUBLE64*) arrayData)[i]); 
    break;

  case IB_Types::CHAR_TYPE:
  {
    //IB_BUFF_PTR bufferIterator = (IB_BUFF_PTR)arrayData;
    //for (i = 0; i < elements; i++) {
    for (IB_BUFF_PTR bufferIterator = (IB_BUFF_PTR)arrayData;
         bufferIterator < (IB_BUFF_PTR)arrayData + arrayDataLength;
         bufferIterator += descriptor->array_desc_length) {
      jibsNet_.put_ldstring (descriptor->array_desc_length, bufferIterator);
      //bufferIterator += descriptor->array_desc_length;
    }
    break;
  }
  case IB_Types::VARCHAR_TYPE:
  {
    for (IB_BUFF_PTR bufferIterator = (IB_BUFF_PTR)arrayData;
         bufferIterator < (IB_BUFF_PTR)arrayData + arrayDataLength;
         bufferIterator += descriptor->array_desc_length) {
      jibsNet_.put_ldstring (strlen (bufferIterator), bufferIterator);
    }
    break;
  }
  case IB_Types::DATE_TYPE:
    for (i = 0; i < elements; i++)
      jibsNet_.put_timestamp (((ISC_QUAD*) arrayData)[i]); 
    break;

  default:
    break;
  }

  // Restore the element data type to blr_varying/blr_varying2
  // !!!MMM - well, we do not have to do it if we store descriptor on IC only
  // if (elementIBType == IB_Types::VARCHAR_TYPE) {
  //   descriptor->array_desc_dtype = elementBlrType;
  //   descriptor->array_desc_length--;
  // }

  free (arrayData);
}
// MMM - end

// MMM - getArray() get array from the wire and store it in a database
void
JIBSRemote::getArray (IB_Statement*& statement, IB_SSHORT16 column)
{
  ISC_ARRAY_DESC tmpdesc, *descriptor = &tmpdesc;

  getArrayDescriptor (descriptor);

  // First, calculate and allocate space to hold the data
  IB_SLONG32 elements = descriptor->array_desc_bounds[0].array_bound_upper - 
                       descriptor->array_desc_bounds[0].array_bound_lower + 1;
  for (int dim = 1; dim < descriptor->array_desc_dimensions; dim++)
    elements *= (descriptor->array_desc_bounds[dim].array_bound_upper - 
                 descriptor->array_desc_bounds[dim].array_bound_lower + 1);

  // We are going to write an array of VARCHAR (blr_varying/blr_varying2)
  // as array of blr_cstring(s). Thus, we need to add a byte per each element
  // to hold the zero terminator.  
  char elementBlrType;
  int elementIBType = IB_Types::getIBTypeOfArrayElement (descriptor);
  if (elementIBType == IB_Types::VARCHAR_TYPE) {
    elementBlrType = descriptor->array_desc_dtype;
    descriptor->array_desc_dtype = blr_cstring;
    descriptor->array_desc_length++;
  }
  void *arrayData;
  IB_SLONG32 arrayDataLength = elements*descriptor->array_desc_length;

  if ((arrayData = (void *) malloc (arrayDataLength)) == NULL)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
                               IB_SQLException::outOfMemoryException__);

  int i;
  switch (elementIBType) {

  // There is no need to get scale for each numeric data type.
  // It was received as a part of descriptor for all array elements.
  case IB_Types::SMALLINT_TYPE:
  case IB_Types::NUMERIC_SMALLINT_TYPE:
    for (i = 0; i < elements; i++)
      ((IB_SSHORT16*) arrayData)[i] = jibsNet_.get_sshort16 (); 
    break;

  case IB_Types::INTEGER_TYPE:
  case IB_Types::NUMERIC_INTEGER_TYPE:
    for (i = 0; i < elements; i++)
      ((IB_SLONG32*) arrayData)[i] = jibsNet_.get_slong32 (); 
    break;

  case IB_Types::FLOAT_TYPE:
    for (i = 0; i < elements; i++)
      ((IB_FLOAT32*) arrayData)[i] = jibsNet_.get_float32 (); 
    break;

  case IB_Types::DOUBLE_TYPE:
  case IB_Types::NUMERIC_DOUBLE_TYPE:
    for (i = 0; i < elements; i++)
      ((IB_DOUBLE64*) arrayData)[i] = jibsNet_.get_double64 (); 
    break;

  case IB_Types::CHAR_TYPE:
  {
    IB_BUFF_PTR bufferIterator = (IB_BUFF_PTR)arrayData;
    for (i = 0; i < elements; i++) {
      IB_LDString ldstring = jibsNet_.get_ldstring ();
      statement->charCpy (bufferIterator,
                          descriptor->array_desc_length,
                          (descriptor->array_desc_length < ldstring.length_) ?
                             descriptor->array_desc_length : ldstring.length_,
                          ldstring.string_);
      bufferIterator += descriptor->array_desc_length;
    }
    break;
  }
  case IB_Types::VARCHAR_TYPE:
  {
    IB_BUFF_PTR bufferIterator = (IB_BUFF_PTR)arrayData;
    for (i = 0; i < elements; i++) {
      IB_LDString ldstring = jibsNet_.get_ldstring ();
      strcpy (bufferIterator, ldstring.string_);
      bufferIterator += descriptor->array_desc_length;
    }
    break;
  }
  case IB_Types::DATE_TYPE:
    for (i = 0; i < elements; i++)
      ((ISC_QUAD*) arrayData)[i] = jibsNet_.get_timestamp (); 
    break;

  default:
    break;
  }

  // Put array into a database
  session_.arrayPutSlice ((IB_ARRAYID*) (statement->sqldaIn_->sqlvar+column)->sqldata,
                          descriptor, arrayData, &arrayDataLength);

  // we do not need to restore the descriptor's info since it is a
  // local variable anyway and the original is stored on the client
  // if (elementIBType == IB_Types::VARCHAR_TYPE) {
  //   descriptor->array_desc_dtype = elementBlrType;
  //   descriptor->array_desc_length--;
  // }

  free (arrayData);
}
// MMM - end

// MMM - get array descriptor from the wire.
// Called by get_array_slice.
void
JIBSRemote::getArrayDescriptor (ISC_ARRAY_DESC *descriptor)
{
  descriptor->array_desc_dtype = jibsNet_.get_sbyte ();
  descriptor->array_desc_scale = jibsNet_.get_sbyte ();
  descriptor->array_desc_scale = -descriptor->array_desc_scale;
  descriptor->array_desc_length = jibsNet_.get_sshort16 ();
  descriptor->array_desc_dimensions = jibsNet_.get_sshort16 ();

  for (int dim = 0; dim < descriptor->array_desc_dimensions; dim++) {
    descriptor->array_desc_bounds[dim].array_bound_lower = jibsNet_.get_sshort16 ();
    descriptor->array_desc_bounds[dim].array_bound_upper = jibsNet_.get_sshort16 ();
  }
  
  strcpy (descriptor->array_desc_field_name, jibsNet_.get_ldstring ().string_); 
  strcpy (descriptor->array_desc_relation_name, jibsNet_.get_ldstring ().string_);
}
// MMM - end

// MMM - put array descriptor info on the wire.
// Called by putResultMetaData(), putInputMetaData()
void
JIBSRemote::putArrayDescriptor (ISC_ARRAY_DESC *descriptor)
{
  jibsNet_.put_sbyte (IB_Types::getIBTypeOfArrayElement (descriptor));
  jibsNet_.put_sbyte (abs (descriptor->array_desc_scale));
  jibsNet_.put_sshort16 (IB_Types::getPrecisionOfArrayElement (descriptor));
  jibsNet_.put_sshort16 (descriptor->array_desc_length);
  jibsNet_.put_sshort16 (descriptor->array_desc_dimensions);

  for (int dim = 0; dim < descriptor->array_desc_dimensions; dim++) {
    jibsNet_.put_sshort16 (descriptor->array_desc_bounds[dim].array_bound_lower);
    jibsNet_.put_sshort16 (descriptor->array_desc_bounds[dim].array_bound_upper);
  }
  // There are no length fields for column and table names in
  // array descriptor, these names are null terminated.
  jibsNet_.put_ldstring (strlen (descriptor->array_desc_field_name),
                         descriptor->array_desc_field_name);
  jibsNet_.put_ldstring (strlen (descriptor->array_desc_relation_name),
                         descriptor->array_desc_relation_name);
  jibsNet_.put_sbyte (descriptor->array_desc_dtype);
}
// MMM - end

// Called by prepare_statement() and execute_*_statement()
void
JIBSRemote::putResultMetaData (IB_ResultSet* resultSet)
{
  IB_SSHORT16 resultCols = resultSet->getColumnCount ();
  jibsNet_.put_sshort16 (resultCols);

  IB_SSHORT16 col;
    
  IB_LDString columnTableName;
  IB_LDString columnName;
  IB_LDString columnLabel;

  for (col=0; col < resultCols; col++) {
    columnTableName = resultSet->getColumnTableName (col);
    columnName = resultSet->getColumnName (col);
    columnLabel = resultSet->getColumnLabel (col);
    
    jibsNet_.put_ldstring (columnTableName); // unicode
    jibsNet_.put_ldstring (columnName); // unicode

    if ((columnName.length_ == columnLabel.length_) &&
        !strncmp (columnName.string_, columnLabel.string_, columnName.length_)) {
      jibsNet_.put_boolean (IB_TRUE);
    }
    else {
      jibsNet_.put_boolean (IB_FALSE);
      jibsNet_.put_ldstring (columnLabel); // unicode
    }

    jibsNet_.put_sbyte (resultSet->isColumnNullable (col));
    jibsNet_.put_sbyte (resultSet->getColumnType (col));
    jibsNet_.put_sshort16 (resultSet->getColumnPrecision (col));
    jibsNet_.put_sbyte ((const signed char) resultSet->getColumnScale (col));
    jibsNet_.put_sshort16 (resultSet->getColumnCharSet (col)); //sqlsubtype 
    jibsNet_.put_sshort16 (resultSet->getColumnCharLength (col));

    // MMM - send array descriptor
    if (resultSet->getColumnType (col) == IB_Types::ARRAY_TYPE) {
      ISC_ARRAY_DESC descriptor;
      session_.arrayLookupBounds (resultSet->statement_->sqldaOut_->sqlvar[col].relname,
                                  resultSet->statement_->sqldaOut_->sqlvar[col].sqlname,
                                  &descriptor);
      jibsNet_.put_boolean (IB_TRUE);	// descriptor follows
      putArrayDescriptor (&descriptor);
    }
    // MMM - end
  }
}

// Called by prepare_statement()  
void
JIBSRemote::putInputMetaData (IB_Statement* statement)
{
  IB_SSHORT16 inputCols = statement->getParameterCount ();
  jibsNet_.put_sshort16 (inputCols);
    
  IB_SSHORT16 col;

  for (col=0; col < inputCols; col++) {
    jibsNet_.put_sbyte (statement->isParameterNullable (col));
    jibsNet_.put_sbyte (statement->getParameterType (col));
    jibsNet_.put_sshort16 (statement->getParameterPrecision (col));
    jibsNet_.put_sbyte ((const signed char) statement->getParameterScale (col));
    jibsNet_.put_sshort16 (statement->getParameterCharSet (col)); //sqlsubtype 
    jibsNet_.put_sshort16 (statement->getParameterCharLength (col)); 
    // MMM - send array descriptor
    if (statement->getParameterType (col) == IB_Types::ARRAY_TYPE) {
      ISC_ARRAY_DESC descriptor;
      if (statement->sqldaIn_->sqlvar[col].relname_length &&
          statement->sqldaIn_->sqlvar[col].sqlname_length) {
        session_.arrayLookupBounds (statement->sqldaIn_->sqlvar[col].relname,
                                    statement->sqldaIn_->sqlvar[col].sqlname,
                                    &descriptor);
        jibsNet_.put_boolean (IB_TRUE);	// descriptor follows
        putArrayDescriptor (&descriptor);
      }
      else 
        jibsNet_.put_boolean (IB_FALSE);// no descriptor follows
    }
    // MMM - end
  }
}

IB_BOOLEAN
JIBSRemote::execute_catalog_query ()
{
  IB_Statement *statement = NULL;
  IB_BOOLEAN endOfResultSet = IB_TRUE;

  int catalogFunction = (int) jibsNet_.get_sbyte ();

  switch (catalogFunction) {
  case IB_Catalog::CATALOG_ALL_PROCEDURES_ARE_CALLABLE: {
    IB_BOOLEAN allProceduresAreCallable = 
      catalog_.allProceduresAreCallable (session_.user_);
 
    jibsNet_.resetMessage ();
    jibsNet_.put_code (MessageCodes::SUCCESS);
    jibsNet_.put_boolean (allProceduresAreCallable);
    return IB_TRUE;
  }
  case IB_Catalog::CATALOG_ALL_TABLES_ARE_SELECTABLE: {
    IB_BOOLEAN allTablesAreSelectable = 
      catalog_.allTablesAreSelectable (session_.user_);
 
    jibsNet_.resetMessage ();
    jibsNet_.put_code (MessageCodes::SUCCESS);
    jibsNet_.put_boolean (allTablesAreSelectable);
    return IB_TRUE;
  }
  case IB_Catalog::CATALOG_GET_PROCEDURES: {
    IB_STRING procedureNamePattern = jibsNet_.get_string ();
    statement = catalog_.getProcedures (procedureNamePattern);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_PROCEDURE_COLUMNS: {
    IB_STRING procedureNamePattern = jibsNet_.get_string ();
    IB_STRING columnNamePattern = jibsNet_.get_string ();
    statement = catalog_.getProcedureColumns (procedureNamePattern, columnNamePattern);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_TABLES: {
    IB_STRING tableNamePattern = jibsNet_.get_string ();

    IB_BOOLEAN types[3];
    types [0] = jibsNet_.get_boolean ();
    types [1] = jibsNet_.get_boolean ();
    types [2] = jibsNet_.get_boolean ();

    statement = catalog_.getTables (tableNamePattern, types);
    break;
  }
  case IB_Catalog::CATALOG_GET_TABLE_TYPES: {
    statement = catalog_.getTableTypes ();
    break;
  }
  case IB_Catalog::CATALOG_GET_COLUMNS: {
    IB_STRING tableNamePattern = jibsNet_.get_string ();
    IB_STRING columnNamePattern = jibsNet_.get_string ();
    statement = catalog_.getColumns (tableNamePattern, columnNamePattern);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_COLUMN_PRIVILEGES: {
    IB_STRING table = jibsNet_.get_string ();
    IB_STRING columnNamePattern = jibsNet_.get_string ();
    statement = catalog_.getColumnPrivileges (table, columnNamePattern);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_TABLE_PRIVILEGES: {
    IB_STRING tableNamePattern = jibsNet_.get_string ();
    statement = catalog_.getTablePrivileges (tableNamePattern);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_BEST_ROW_IDENTIFIER: {
    IB_STRING table = jibsNet_.get_string ();
    // HP-UX port (old CC): added type cast (int)
    int scope = (int) jibsNet_.get_slong32 ();
    IB_BOOLEAN nullable = jibsNet_.get_boolean ();
    statement = catalog_.getBestRowIdentifier (table, scope, nullable);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_VERSION_COLUMNS: {
    IB_STRING table = jibsNet_.get_string ();
    statement = catalog_.getVersionColumns (table);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_PRIMARY_KEYS: {
    IB_STRING table = jibsNet_.get_string ();
    statement = catalog_.getPrimaryKeys (table);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_IMPORTED_KEYS: {
    IB_STRING table = jibsNet_.get_string ();
    statement = catalog_.getImportedKeys (table);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_EXPORTED_KEYS: {
    IB_STRING table = jibsNet_.get_string ();
    statement = catalog_.getExportedKeys (table);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_CROSS_REFERENCE: {
    IB_STRING primaryTable = jibsNet_.get_string ();
    IB_STRING foreignTable = jibsNet_.get_string ();
    statement = catalog_.getCrossReference (primaryTable, foreignTable);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_INDEX_INFO: {
    IB_STRING table = jibsNet_.get_string ();
    IB_BOOLEAN unique = jibsNet_.get_boolean ();
    IB_BOOLEAN approximate = jibsNet_.get_boolean ();
    statement = catalog_.getIndexInfo (table, unique, approximate);
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  case IB_Catalog::CATALOG_GET_TYPE_INFO: {
    statement = catalog_.getTypeInfo ();
    // this also creates the result set with member variable CATALOG_GET_PROCEDURES
    // make sure it does the same things that session_.executeQueryStatement does
    break;
  }
  }

  jibsNet_.resetMessage ();
  jibsNet_.put_code (MessageCodes::SUCCESS);
  jibsNet_.put_ref ((IB_REF) statement);
  jibsNet_.put_slong32 (0); // statement->getUpdateCount ()) or statement->getResultSet()->getRowCount()
  endOfResultSet = putResultData (statement->getResultSet ());
  return endOfResultSet;
}

IB_BOOLEAN
JIBSRemote::putCatalogResultData (IB_ResultSet* resultSet)
{
  // The number of rows to be sent for each FETCH_ROWS or EXECUTE_* request.
  // !!! This could be computed once and for all when the statement is prepared
  // !!! but then the Statement/ResultSet classes are JDBC aware.
  // HP-UX port (old CC): added type cast (int)
  int bufferedRows = resultSet->getFetchSize ();
  if (!bufferedRows) {
    // Add one to ensure bufferedRows is never zero.
    // Add one to record size to avoid division by zero errors (eg. select '' from rdb$database).
    bufferedRows = 1 + (bufferedDataSize__ / (1 + (int) resultSet->getRecordSize ()));
  }

  IB_BOOLEAN endOfResultSet = IB_FALSE;

  for (int row = 0; row < bufferedRows; row++) {
    if (!resultSet->next ()) {
      endOfResultSet = IB_TRUE;
      resultSet->close (); // auto-close cursor after last row is read
      break;
    }

    jibsNet_.put_code (MessageCodes::ROW_DATUM);

    switch (resultSet->getCatalogFunction()) {
    case IB_Catalog::CATALOG_GET_PROCEDURES:
      putRowCatalogGetProcedures (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_PROCEDURE_COLUMNS:
      putRowCatalogGetProcedureColumns (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_TABLES:
      putRowCatalogGetTables (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_TABLE_TYPES:
      putRowCatalogGetTableTypes (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_COLUMNS:
      putRowCatalogGetColumns (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_COLUMN_PRIVILEGES:
      putRowCatalogGetColumnPrivileges (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_TABLE_PRIVILEGES:
      putRowCatalogGetTablePrivileges (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_BEST_ROW_IDENTIFIER:
      putRowCatalogGetBestRowIdentifier (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_VERSION_COLUMNS:
      putRowCatalogGetVersionColumns (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_PRIMARY_KEYS:
      putRowCatalogGetPrimaryKeys (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_EXPORTED_KEYS:
      putRowCatalogGetExportedKeys (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_IMPORTED_KEYS:
      putRowCatalogGetImportedKeys (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_CROSS_REFERENCE:
      putRowCatalogGetCrossReference (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_INDEX_INFO:
      putRowCatalogGetIndexInfo (resultSet);
      break;
    case IB_Catalog::CATALOG_GET_TYPE_INFO:
      putRowCatalogGetTypeInfo (resultSet);
      break;
    }

  }
  jibsNet_.put_code (MessageCodes::END_ROW_DATA);
  return endOfResultSet;
}

void
JIBSRemote::putRowCatalogGetProcedures (IB_ResultSet* resultSet)
{
  // Column 1 - CATALOG
  jibsNet_.put_sbyte (1); // NULL

  // Column 2 - SCHEMA
  jibsNet_.put_sbyte (1); // NULL

  // Column 3 - PROCEDURE_NAME   (RDB$PROCEDURES.RDB$PROCEDURE_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
			 resultSet->getChar (0)); // CHAR

  // Col 4 - 6 - reserved for future use
  jibsNet_.put_sbyte (1); // NULL
  jibsNet_.put_sbyte (1); // NULL
  jibsNet_.put_sbyte (1); // NULL

  // Column 7 - REMARKS (based on RDB$PROCEDURES.RDB$DESCRIPTION)
  jibsNet_.put_sbyte (0); // NOT NULL
  jibsNet_.put_ldstring (0, ""); // VARCHAR

  // Column 8 - PROCEDURE_TYPE (based on RDB$PROCEDURES.RDB$PROCEDURE_OUTPUTS)
  IB_SSHORT16 procedureOutputs = resultSet->getSmallInt (2);
  if (procedureOutputs)
    jibsNet_.put_sshort16 (IB_Catalog::procedureReturnsResult__);
  else 
    jibsNet_.put_sshort16 (IB_Catalog::procedureNoResult__);

  // Column 9 - PROCEDURE_OWNER (interclient extension, RDB$PROCEDURES.RDB$OWNER_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (3),
		       resultSet->getChar (3));
}

void
JIBSRemote::putRowCatalogGetProcedureColumns (IB_ResultSet* resultSet)
{
  // Column 1 - CATALOG
  jibsNet_.put_sbyte (1); // NULL

  // Column 2 - SCHEMA
  jibsNet_.put_sbyte (1); // NULL

  // Column 3 - PROCEDURE_NAME (RDB$PROCEDURES.RDB$PROCEDURE_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		       resultSet->getChar (0));

  // Column 4 - COLUMN_NAME (RDB$PROCEDURE_PARAMETERS.RDB$PARAMETER_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
		       resultSet->getChar (1));

  // Column 5 - COLUMN_TYPE (based on RDB$PROCEDURE_PARAMETERS.RDB$PARAMETER_TYPE)
  IB_SSHORT16 parameterType = resultSet->getSmallInt (2);
  if (parameterType)
    jibsNet_.put_sshort16 (IB_Catalog::procedureColumnResult__);
  else
    jibsNet_.put_sshort16 (IB_Catalog::procedureColumnIn__);

  // Column 6 - DATA_TYPE (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (IB_Catalog::getSQLType (resultSet->getSmallInt (3), 
					         resultSet->getSmallInt (4),
					         resultSet->getSmallInt (5)));

  // Column 7 - TYPE_NAME (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  IB_Types::IBType ibType = IB_Catalog::getIBType (resultSet->getSmallInt (3), 
					           resultSet->getSmallInt (4),
					           resultSet->getSmallInt (5));
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR ASCII

  // Column 8 - PRECISION (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getPrecision (ibType, resultSet->getSmallInt (6)));

  // Column 9 - LENGTH (RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (resultSet->getSmallInt (6));

  // Column 10 - SCALE (RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (abs (resultSet->getSmallInt (5)));

  // Column 11 - RADIX   
  jibsNet_.put_sshort16 (10);

  // Column 12 - NULLABLE (based on RDB$FIELDS.RDB$NULL_FLAG)
  if (resultSet->getSmallInt (7) == 1)
    jibsNet_.put_sshort16 (IB_Catalog::procedureNoNulls__);
  else
    jibsNet_.put_sshort16 (IB_Catalog::procedureNullable__);

  // Column 13 - REMARKS (based on RDB$PROCEDURE_PARAMETERS.RDB$DESCRIPTION)
  jibsNet_.put_sbyte (0); // NOT NULL
  jibsNet_.put_ldstring (0, ""); // VARCHAR
}

void
JIBSRemote::putRowCatalogGetTables (IB_ResultSet* resultSet)
{
  // Column 1 - CATALOG
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - SCHEMA
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME   (RDB$RELATION_NAME)
   jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		        resultSet->getChar (0));

  // Column 4 - TABLE_TYPE (TABLE, SYSTEM TABLE, or VIEW)
  // literals returned by SELECT (T, S, V) must be of the same length,
  // must be substituted for here.
  IB_STRING selectedLiteral = resultSet->getChar (1);
  if (*selectedLiteral == 'T')
    jibsNet_.put_ldstring (5, "TABLE"); // VARCHAR ASCII
  else if (*selectedLiteral == 'V')
    jibsNet_.put_ldstring (4, "VIEW"); // VARCHAR ASCII
  else if (*selectedLiteral == 'S')
    jibsNet_.put_ldstring (12, "SYSTEM TABLE"); // VARCHAR ASCII
  else
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10020,
			       IB_SQLException::bugCheckException__);
  
  // Column 5 - REMARKS
  jibsNet_.put_sbyte (0); // NOT NULL
  jibsNet_.put_ldstring (0, ""); // VARCHAR
  // !!! need to extract string from rdb$description blob

  // Column 6 - TABLE_OWNER (interclient extension, RDB$OWNER)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (3),
		       resultSet->getChar (3)); //
}


void
JIBSRemote::putRowCatalogGetTableTypes (IB_ResultSet* resultSet)
{
  // Column 1 - TABLE_TYPE (TABLE, SYSTEM TABLE, or VIEW)
  // literals returned by SELECT (T, S, V) must be of the same length,
  // must be substituted for here.
  IB_STRING selectedLiteral = resultSet->getChar (0);
  if (*selectedLiteral == 'T')
    jibsNet_.put_ldstring (5, "TABLE"); // VARCHAR
  else if (*selectedLiteral == 'V')
    jibsNet_.put_ldstring (4, "VIEW");
  else if (*selectedLiteral == 'S')
    jibsNet_.put_ldstring (12, "SYSTEM TABLE");
  else
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10021,
			       IB_SQLException::bugCheckException__);
}

void
JIBSRemote::putRowCatalogGetColumns (IB_ResultSet* resultSet)
{
  // Column 1 - CATALOG
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - SCHEMA
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME (RDB$RELATION_FIELDS.RDB$TABLE_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		       resultSet->getChar (0));

  // Column 4 - COLUMN_NAME (RDB$RELATION_FIELDS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
		       resultSet->getChar (1));

  // Column 5 - DATA_TYPE (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (IB_Catalog::getSQLType (resultSet->getSmallInt (2),
						 resultSet->getSmallInt (3),
						 resultSet->getSmallInt (4)));

  // Column 6 - TYPE_NAME (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  IB_Types::IBType ibType = IB_Catalog::getIBType (resultSet->getSmallInt (2),
						   resultSet->getSmallInt (3),
						   resultSet->getSmallInt (4));
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR

  // Column 7 - COLUMN_SIZE (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getColumnSize (ibType, resultSet->getSmallInt (5)));

  // Column 8 - BUFFER_LENGTH (not used)
  jibsNet_.put_sbyte (1); // NULL

  // Column 9 - DECIMAL_DIGITS (RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_slong32 (abs (resultSet->getSmallInt (4)));

  // Column 10 - NUM_PREC_RADIX   
  jibsNet_.put_slong32 (10);

  // Column 11 - NULLABLE (based on RDB$FIELDS.RDB$NULL_FLAG and RDB$RELATION_FIELDS.RDB$NULL_FLAG)
  if  ((resultSet->getSmallInt (6) == 1) || (resultSet->getSmallInt (10) == 1))
    jibsNet_.put_slong32 (IB_Catalog::columnNoNulls__);
  else
    jibsNet_.put_slong32 (IB_Catalog::columnNullable__);

  // Column 12 - REMARKS (based on RDB$RELATION_FIELDS.RDB$DESCRIPTION)
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 13 - COLUMN_DEF (based on RDB$RELATION_FIELDS.RDB$DEFAULT_VALUE)
  /***!!!
  IB_BLOBID columnDefault = resultSet->getAsciiBlob(8);
  IB_BLOBID domainDefault = resultSet->getAsciiBlob(11);
  if (!IS_NULL_BLOBID(columnDefault)) {
    jibsNet_.put_sbyte (0); // NOT NULL
    IB_STRING defaultValue = ""; // catalog_.getDefaultValueFromBlob (columnDefault);
    jibsNet_.put_ldstring (0, defaultValue); // CHAR(0)
  }
  else if (!IS_NULL_BLOBID(domainDefault)) {
    jibsNet_.put_sbyte (0); // NOT NULL
    IB_STRING defaultValue = ""; // catalog_.getDefaultValueFromBlob (domainDefault);
    jibsNet_.put_ldstring (0, defaultValue); // CHAR(0)
  }
  else
  ***/
    jibsNet_.put_sbyte (1); // NULL
 

  // Column 14 - SQL_DATA_TYPE (unused)
  jibsNet_.put_sbyte (1); // NULL

  // Column 15 - SQL_DATETIME_SUB (unused)
  jibsNet_.put_sbyte (1); // NULL

  // Column 16 - CHAR_OCTET_LENGTH (based on RDB$RELATION_FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (resultSet->getSmallInt (5)); // NULL

  // Column 17 - ORDINAL_POSITION (based on RDB$RELATION_FIELDS.RDB$FIELD_POSITION)
  jibsNet_.put_slong32 (resultSet->getSmallInt(9) + 1);

  // Column 18 - IS_NULLABLE (based on RDB$FIELDS.RDB$NULL_FLAG and RDB$RELATION_FIELDS.RDB$NULL_FLAG)
  if ((resultSet->getSmallInt (6) == 1) || (resultSet->getSmallInt (10) == 1))
    jibsNet_.put_ldstring (2, "NO");  // VARCHAR
  else
    jibsNet_.put_ldstring (3, "YES"); // VARCHAR
  
}

void
JIBSRemote::putRowCatalogGetColumnPrivileges (IB_ResultSet* resultSet)
{
  // Column 1 - TABLE_CAT
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - TABLE_SCHEM
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME (RDB$USER_PRIVILEGES.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
			 resultSet->getChar (0));

  // Column 4 - COLUMN_NAME (RDB$USER_PRIVILEGES.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
			 resultSet->getChar (1));

  // Column 5 - GRANTOR (RDB$USER_PRIVILEGES.RDB$GRANTOR)
  if (resultSet->isNull (2))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (2),
			   resultSet->getChar (2));
  }

  // Column 6 - GRANTEE (RDB$USER_PRIVILEGES.RDB$USER)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (3),
			 resultSet->getChar (3));

  // Column 7 - PRIVILEGE (RDB$USER_PRIVILEGES.RDB$PRIVILEGE)
  jibsNet_.put_ldstring (mapPrivilegeLiterals (resultSet->getChar (4))); // VARCHAR

  // Column 8 - IS_GRANTABLE (based on RDB$USER_PRIVILEGES.RDB$GRANT_OPTION)
  if (resultSet->isNull (5))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    if (resultSet->getSmallInt (5))
      jibsNet_.put_ldstring (3, "YES"); // VARCHAR
    else
      jibsNet_.put_ldstring (2, "NO"); // VARCHAR
  }
}

void
JIBSRemote::putRowCatalogGetTablePrivileges (IB_ResultSet* resultSet)
{
  // Column 1 - CATALOG
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - SCHEMA
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME (RDB$USER_PRIVILEGES.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
			 resultSet->getChar (0));

  // Column 4 - GRANTOR (RDB$USER_PRIVILEGES.RDB$GRANTOR)
  if (resultSet->isNull (1))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
			   resultSet->getChar (1));
  }

  // Column 5 - GRANTEE (RDB$USER_PRIVILEGES.RDB$USER)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (2),
			 resultSet->getChar (2));

  // Column 6 - PRIVILEGE (RDB$USER_PRIVILEGES.RDB$PRIVILEGE)
  jibsNet_.put_ldstring (mapPrivilegeLiterals (resultSet->getChar (3))); // VARCHAR

  // Column 7 - IS_GRANTABLE (based on RDB$USER_PRIVILEGES.RDB$GRANT_OPTION)
  if (resultSet->getSmallInt (4))
    jibsNet_.put_ldstring (3, "YES"); // VARCHAR
  else
    jibsNet_.put_ldstring (2, "NO");
}

void
JIBSRemote::putRowCatalogGetBestRowIdentifier (IB_ResultSet* resultSet)
{
  // Column 1 - SCOPE
  jibsNet_.put_sshort16 (IB_Catalog::bestRowSession__);

  // Column 2 - COLUMN_NAME (RDB$RELATION_FIELDS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		       resultSet->getChar (0));

  // Column 3 - DATA_TYPE (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (IB_Catalog::getSQLType (resultSet->getSmallInt (1), 
					         resultSet->getSmallInt (2),
					         resultSet->getSmallInt (3)));

  // Column 4 - TYPE_NAME (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  IB_Types::IBType ibType = IB_Catalog::getIBType (resultSet->getSmallInt (1), 
					           resultSet->getSmallInt (2),
					           resultSet->getSmallInt (3));
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR

  // Column 5 - COLUMN_SIZE (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getColumnSize (ibType, resultSet->getSmallInt (4)));

  // Column 6 - BUFFER_LENGTH (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getColumnSize (ibType, resultSet->getSmallInt (4)));

  // Column 7 - DECIMAL_DIGITS (RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (abs (resultSet->getSmallInt (3)));

  // Column 8 - PSEUDO_COLUMN   
  jibsNet_.put_sshort16 (IB_Catalog::bestRowUnknown__);
}

void
JIBSRemote::putRowCatalogGetVersionColumns (IB_ResultSet* resultSet)
{
  // Column 1 - SCOPE (not used)
  jibsNet_.put_sshort16 (0); // just any old bogus value

  // Column 2 - COLUMN_NAME (RDB$FIELDS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
			 resultSet->getChar (0));

  // Column 3 - DATA_TYPE (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (IB_Catalog::getSQLType (resultSet->getSmallInt (1), 
					         resultSet->getSmallInt (2),
					         resultSet->getSmallInt (3)));

  // Column 4 - TYPE_NAME (based on RDB$FIELDS.RDB$FIELD_TYPE, 
  //                                RDB$FIELDS.RDB$FIELD_SUB_TYPE,
  //                                RDB$FIELDS.RDB$FIELD_SCALE)
  IB_Types::IBType ibType = IB_Catalog::getIBType (resultSet->getSmallInt (1), 
					           resultSet->getSmallInt (2),
					           resultSet->getSmallInt (3));
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR

  // Column 5 - COLUMN_SIZE (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getColumnSize (ibType, resultSet->getSmallInt (4)));

  // Column 6 - BUFFER_LENGTH (based on ibType, RDB$FIELDS.RDB$FIELD_LENGTH)
  jibsNet_.put_slong32 (IB_Catalog::getColumnSize (ibType, resultSet->getSmallInt (4)));

  // Column 7 - DECIMAL_DIGITS (RDB$FIELDS.RDB$FIELD_SCALE)
  jibsNet_.put_sshort16 (abs (resultSet->getSmallInt (3)));

  // Column 8 - PSEUDO_COLUMN   
  jibsNet_.put_sshort16 (IB_Catalog::versionColumnUnknown__);
}

void
JIBSRemote::putRowCatalogGetPrimaryKeys (IB_ResultSet* resultSet)
{
  // Column 1 - TABLE_CAT
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - TABLE_SCHEM
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME (RDB$RELATION_CONSTRAINTS.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		         resultSet->getChar (0));

  // Column 4 - COLUMN_NAME (RDB$INDEX_SEGMENT.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
		         resultSet->getChar (1));

  // Column 5 - KEY_SEQ (1 + RDB$INDEX_SEGMENT.RDB$FIELD_POSITION)
  jibsNet_.put_sshort16 (resultSet->getSmallInt (2) + 1);

  // Column 6 - PK_NAME (RDB$RELATION_CONSTRAINTS.RDB$CONSTRAINT_NAME)
  if (resultSet->isNull (3))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (3),
			   resultSet->getChar (3));
  }
}

void
JIBSRemote::putRowCatalogGetImportedKeys (IB_ResultSet* resultSet)
{
  // Column 1 - PKTABLE_CAT
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - PKTABLE_SCHEM
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - PKTABLE_NAME (RDB$RELATION_CONSTRAINTS.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
			 resultSet->getChar (0));

  // Column 4 - PKCOLUMN_NAME (RDB$INDEX_SEGMENTS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (1),
			 resultSet->getChar (1));

  // Column 5 - FKTABLE_CAT
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 6 - FKTABLE_SCHEM
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 7 - FKTABLE_NAME (RDB$RELATION_CONSTRAINTS.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (2),
			 resultSet->getChar (2));

  // Column 8 - FKCOLUMN_NAME (RDB$INDEX_SEGMENTS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (3),
			 resultSet->getChar (3));

  // Column 9 - KEY_SEQ (1+RDB$INDEX_SEGMENTS.RDB$FIELD_POSITION)
  jibsNet_.put_sshort16 (resultSet->getSmallInt (4) + 1);

  // Column 10 - UPDATE_RULE
  if (resultSet->isNull (5))
    jibsNet_.put_sshort16 (IB_Catalog::importedKeyRestrict__);
  else 
    jibsNet_.put_sshort16 (mapRefConstraintRules (resultSet->getChar (5))); // !!! ascii?

  // Column 11- DELETE_RULE
  if (resultSet->isNull (6))
    jibsNet_.put_sshort16 (IB_Catalog::importedKeyRestrict__);
  else 
    jibsNet_.put_sshort16 (mapRefConstraintRules (resultSet->getChar (6))); // !!! ascii?

  // Column 12 - FK_NAME (RDB$INDEX_SEGMENTS.INDEX_NAME)
  if (resultSet->isNull (7))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (7),
			   resultSet->getChar (7));
  }

  // Column 13 - PK_NAME (RDB$INDEX_SEGMENTS.INDEX_NAME)
  if (resultSet->isNull (8))
    jibsNet_.put_sbyte (1); // NULL
  else {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (8),
			   resultSet->getChar (8));
  }

  // Column 14 - DEFERRABILITY (DatabaseMetaData.importedKeyNotDeferrable)
  jibsNet_.put_sshort16 (IB_Catalog::importedKeyNotDeferrable__);
}

void
JIBSRemote::putRowCatalogGetExportedKeys (IB_ResultSet* resultSet)
{
    putRowCatalogGetImportedKeys (resultSet);
}

void
JIBSRemote::putRowCatalogGetCrossReference (IB_ResultSet* resultSet)
{
    putRowCatalogGetImportedKeys (resultSet);
}

void
JIBSRemote::putRowCatalogGetIndexInfo (IB_ResultSet* resultSet)
{
  // Column 1 - TABLE_CAT
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 2 - TABLE_SCHEM
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 3 - TABLE_NAME (RDB$INDICES.RDB$RELATION_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (0),
		         resultSet->getChar (0));

  // Column 4 - NON_UNIQUE (based on RDB$INDICES.RDB$UNIQUE_FLAG)
  jibsNet_.put_sshort16 (!resultSet->getSmallInt (1));

  // Column 5 - INDEX_QUALIFIER 
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 6 - INDEX_NAME (RDB$INDICES.RDB$INDEX_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (2),
		         resultSet->getChar (2));

  // Column 7 - TYPE !!!
  jibsNet_.put_sshort16 (IB_Catalog::tableIndexOther__);

  // Column 8 - ORDINAL_POSITION (RDB$INDEX_SEGMENTS.RDB$FIELD_POSITION)
  jibsNet_.put_sshort16 (resultSet->getSmallInt (3) + 1);

  // Column 9 - COLUMN_NAME (RDB$INDEX_SEGMENTS.RDB$FIELD_NAME)
  jibsNet_.put_ldstring (resultSet->getCharTrimmedByteLength (4),
		       resultSet->getChar (4));

  // Column 10 - ASC_OR_DESC (based on RDB$INDICES.RDB$INDEX_TYPE)
  if (resultSet->isNull (5)) {
    jibsNet_.put_sbyte (0); // NOT NULL CHAR(1)
    jibsNet_.put_ldstring (1, "A");
  }
  else if (resultSet->getSmallInt (5) == 1) {
    jibsNet_.put_sbyte (0); // NOT NULL CHAR(1)
    jibsNet_.put_ldstring (1, "D");
  }
  else
    jibsNet_.put_sbyte (1); // NULL

  // Column 11- CARDINALITY (not supported)
  jibsNet_.put_sbyte (1); // NULL

  // Column 12 - PAGES (COUNT (DISTINCT RDB$PAGES.RDB$PAGE_NUMBER))
  jibsNet_.put_slong32 (resultSet->getInteger (7));

  // Column 13 - FILTER_CONDITION
  jibsNet_.put_sbyte (1); // NULL
}

void
JIBSRemote::putRowCatalogGetTypeInfo (IB_ResultSet* resultSet)
{
  IB_Types::IBType ibType = (IB_Types::IBType) resultSet->getInteger (0);
  IB_Catalog::SQLType sqlType = (IB_Catalog::SQLType) resultSet->getInteger (1);

  // Column 1 - TYPE_NAME
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR

  // Column 2 - DATA_TYPE
  jibsNet_.put_sshort16 (sqlType);

  // Column 3 - PRECISION 
  jibsNet_.put_slong32 (IB_Catalog::getMaxPrecision (ibType));

  // Column 4 - LITERAL_PREFIX
  IB_STRING literalPrefix = IB_Catalog::getLiteralPrefix (ibType);
  if (literalPrefix) {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (strlen (literalPrefix), 
			   literalPrefix); // VARCHAR ASCII
  }
  else
    jibsNet_.put_sbyte (1); // NULL

  // Column 5 - LITERAL_SUFFIX
  IB_STRING literalSuffix = IB_Catalog::getLiteralSuffix (ibType);
  if (literalSuffix) {
    jibsNet_.put_sbyte (0); // NOT NULL
    jibsNet_.put_ldstring (strlen (literalPrefix),
			   literalSuffix); // VARCHAR ASCII
  }
  else
    jibsNet_.put_sbyte (1); // NULL


  // Column 6 - CREATE_PARAMS
  jibsNet_.put_sbyte (1); // NULL VARCHAR

  // Column 7 - NULLABLE
  jibsNet_.put_sshort16 (IB_Catalog::typeNullable__);

  // Column 8 - CASE_SENSITIVE
  jibsNet_.put_sshort16 (IB_Catalog::isCaseSensitive (ibType));

  // Column 9- SEARCHABLE
  jibsNet_.put_sshort16 (IB_TRUE);

  // Column 10 - UNSIGNED_ATTRIBUTE
  jibsNet_.put_sshort16 (IB_Catalog::isUnsigned (ibType));

  // Column 11 - FIXED_PREC_SCALE
  jibsNet_.put_sshort16 (IB_Catalog::maybeMoneyValue (ibType));

  // Column 12 - AUTO_INCREMENT
  jibsNet_.put_sshort16 (IB_FALSE);

  // Column 13 - LOCAL_TYPE_NAME
  jibsNet_.put_sbyte (0); // NOT NULL
  jibsNet_.put_ldstring (IB_Catalog::getIBTypeName (ibType)); // VARCHAR

  // Column 14 - MINIMUM SCALE
  jibsNet_.put_sshort16 (IB_Catalog::getMinScale (ibType));

  // Column 15 - MAXIMUM_SCALE
  jibsNet_.put_sshort16 (IB_Catalog::getMaxScale (ibType));

  // Column 16 - SQL_DATA_TYPE
  jibsNet_.put_sbyte (1); // NULL

  // Column 17 - SQL_DATETIME_SUB
  jibsNet_.put_sbyte (1); // NULL

  // Column 18 - NUM_PREC_RADIX
  jibsNet_.put_slong32 (10);
}


IB_LDString
JIBSRemote::mapPrivilegeLiterals (IB_STRING privilegeLiteral)
{
  // literals in RDB$USER_PRIVILEGES.RDB$PRIVILEGE are 'I', 'S', 'U', 'D', 'R'
  // must be substituted here with the full literals, such as INSERT for I, etc.
 
  // !!! do these still return ascii when fss is enabled?
  if (*privilegeLiteral == 'I')
    return IB_LDString (6, "INSERT");
  else if (*privilegeLiteral == 'S')
    return IB_LDString (6, "SELECT");
  else if (*privilegeLiteral == 'U')
    return IB_LDString (6, "UPDATE");
  else if (*privilegeLiteral == 'D')
    return IB_LDString (6, "DELETE");
  else if (*privilegeLiteral == 'R')
    return IB_LDString (10, "REFERENCES");
  else
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10022,
			       IB_SQLException::bugCheckException__);
  return(NULL);
}


IB_SSHORT16
JIBSRemote::mapRefConstraintRules (IB_STRING constraintRule)
{
// Map strings such as RESTRICT, CASCADE, SET NULL, SET DEFAULT, NO ACTION into
// appropriate shorts.
  if (!strncmp (constraintRule, "RESTRICT", 8))
    return (IB_Catalog::importedKeyRestrict__);
  else if (!strncmp (constraintRule, "CASCADE", 7))
    return (IB_Catalog::importedKeyCascade__);
  else if (!strncmp (constraintRule, "SET NULL", 8))
    return (IB_Catalog::importedKeySetNull__);
  else if (!strncmp (constraintRule, "NO ACTION", 9))
    return (IB_Catalog::importedKeyNoAction__);
  else if (!strncmp (constraintRule, "SET DEFAULT", 11))
    return (IB_Catalog::importedKeySetDefault__);
  else
    return (IB_Catalog::importedKeyRestrict__);
}

