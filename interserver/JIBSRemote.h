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
#ifndef _JIBSREMOTE_H__
#define _JIBSREMOTE_H__

#include "IB_Defines.h"
#include "Session.h"
#include "JIBSNet.h"
#include "IB_Catalog.h"
#include "VersionInformation.h"

// The messaging layer.
// This is the remote interface layer for the interserver driver.
// It is not network-aware as the JIBSNet layer is.
// JIBSRemote makes no use of NetTCP utilities as does JIBSNet.
// This layer is message-aware (ie. it is responsible
// for sending and receiving remote messages).
// This layer is not responsible for when or why
// messages are being sent.
// This layer is not network-aware, ie. it does not care
// what data representation is used on the wire.
// This layer is responsible for sequencing message data.

class JIBSRemote {

private:

  Session session_;

  IB_Catalog catalog_;

  JIBSNet jibsNet_;

  IB_SBYTE byteSalt_[10];
  IB_SSHORT16 shortSalt_[10];
  IB_SLONG32 intSalt_[10];

  // Static constants
  enum {
    // Number of bytes of data to be sent for each FETCH_ROWS or EXECUTE_* request.
    bufferedDataSize__ = 128000
  };

public:

  JIBSRemote (int sockfd);

  ~JIBSRemote ();

  // This is the driver for the remote interface.
  // Called by interserver.cc
  void interserverMain ();

  enum {
    remoteProtocolVersion__ = 1
  };

private:

  IB_SLONG32 determineByteswap ();
  void establishProtocol (); 
  void test_echo ();
  void generateSalt (IB_SLONG32 saltSeed);
  void stringCrypt (IB_LDBytes& from, IB_LDString& to);
  IB_UBYTE byteCrypt (IB_UBYTE value);
  IB_SSHORT16 shortCrypt (IB_SSHORT16 value);
  IB_SLONG32 intCrypt (IB_SLONG32 value);

  void attach_database ();
  void detach_database ();

  void attach_server ();
  void detach_server ();

  void server_request ();
  void create_database ();

  void suspend_connection ();

  void resume_connection ();

  void ping ();

  void commit ();
  void rollback ();

  void prepare_statement ();

  IB_BOOLEAN execute_statement ();

  IB_BOOLEAN execute_query_statement ();

  void execute_update_statement ();

  IB_BOOLEAN execute_prepared_statement ();

  IB_BOOLEAN execute_prepared_query_statement ();

  void execute_prepared_update_statement ();

  IB_BOOLEAN fetch_rows ();

  void close_cursor ();

  void close_statement ();

  void cancel_statement ();

  void create_blob ();

  void open_blob (IB_SLONG32& actualSegmentSize, 
                  IB_SLONG32& lastSegment);

  void get_blob_segments (IB_SLONG32& actualSegmentSize, 
                          IB_SLONG32& lastSegment);

  void close_blob ();

  void get_result_column_meta_data ();

  IB_BOOLEAN execute_catalog_query ();

private:

  void put_WARNINGS ();

  void put_EXCEPTIONS (IB_SQLException* e);

  // Called by prepare_statement(), execute_*_statement(), execute_prepared_*_statement()
  void getAndStartTransactionIfPending ();

  // Called by execute_*_statement()
  void getStatementExecuteData (IB_Statement*& statement,
				IB_STRING& cursorName,
				IB_STRING& sql, 
				IB_SSHORT16& timeout, 
				IB_SSHORT16& maxFieldSize,
				IB_SLONG32& fetchSize);


  // Called by execute_prepared_*_statement()
  void getPreparedStatementExecuteData (IB_Statement*& statement, 
					IB_STRING& cursorName,
					IB_SSHORT16& timeout, 
					IB_SSHORT16& maxFieldSize,
					IB_SLONG32& fetchSize);


 
  // Called by execute_*_statement() and execute_prepared_*_statement()
  IB_BOOLEAN putResultData (IB_ResultSet* resultSet);


  // Called by prepare_statement() and execute_*_statement()
  void putResultMetaData (IB_ResultSet* resultSet);


  // Called by prepare_statement()  
  void putInputMetaData (IB_Statement* statement);

  // MMM - added methods prototypes
  // Called by putResultMetaData(), putInputMetaData()
  void putArrayDescriptor (ISC_ARRAY_DESC* descriptor);
  void getArrayDescriptor (ISC_ARRAY_DESC* descriptor);

  // Called by getPreparedStatementExecuteData()
  void getArray (IB_Statement*& statement, IB_SSHORT16 column);

  // Called by interserverMain() to get an array descriptor
  // from InterBase and send it back to the client
  void get_array_descriptor ();
  // Called by interserverMain() to get an array or an array slice.
  // Gets an array from a database and sends it back to the client.
  void get_array_slice ();
  // MMM - end

  void putCharColumnData (int col, IB_ResultSet* resultSet);

  IB_BOOLEAN putCatalogResultData (IB_ResultSet* resultSet);

  void putRowCatalogGetProcedures (IB_ResultSet* resultSet);
  void putRowCatalogGetProcedureColumns (IB_ResultSet* resultSet);
  void putRowCatalogGetTables (IB_ResultSet* resultSet);
  void putRowCatalogGetTableTypes (IB_ResultSet* resultSet);
  void putRowCatalogGetColumns (IB_ResultSet* resultSet);
  void putRowCatalogGetColumnPrivileges (IB_ResultSet* resultSet);
  void putRowCatalogGetTablePrivileges (IB_ResultSet* resultSet);
  void putRowCatalogGetBestRowIdentifier (IB_ResultSet* resultSet);
  void putRowCatalogGetVersionColumns (IB_ResultSet* resultSet);
  void putRowCatalogGetPrimaryKeys (IB_ResultSet* resultSet);
  void putRowCatalogGetExportedKeys (IB_ResultSet* resultSet);
  void putRowCatalogGetImportedKeys (IB_ResultSet* resultSet);
  void putRowCatalogGetCrossReference (IB_ResultSet* resultSet);
  void putRowCatalogGetIndexInfo (IB_ResultSet* resultSet);
  void putRowCatalogGetTypeInfo (IB_ResultSet* resultSet);

  static IB_LDString mapPrivilegeLiterals (IB_STRING privilegeLiteral);
  static IB_SSHORT16 mapRefConstraintRules (IB_STRING constraintRule);
};

inline
JIBSRemote::JIBSRemote (int sockfd)
    : jibsNet_ (sockfd),
      session_ (),
      catalog_ (session_.status_, session_.connection_)
{ }

inline
JIBSRemote::~JIBSRemote ()
{ }

#endif





