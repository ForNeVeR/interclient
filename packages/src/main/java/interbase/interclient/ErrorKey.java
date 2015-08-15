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
/**
 * contributors:
 *
 *  @author <a href="mailto:davidjencks@earthlink.net">David Jencks</a>
 *
 */
package interbase.interclient;

class ErrorKey  
{
  private String resourceKey_;
  private String sqlState_;
  private int errorCode_;

  private final static String sqlState__featureNotSupported__ = "0A000";
  private final static String sqlState__interbase_error__ = "ICI00";
  // Not used programmatically, convenience...
  private final static String lastUsedICJ = "00";

  private ErrorKey (String resourceKey, String sqlState, int errorCode)
  {
    resourceKey_ = resourceKey;
    sqlState_ = sqlState;
    errorCode_ = errorCode;
  }

  String getResourceKey ()
  {
    return resourceKey_;
  }

  // For ic/is driver states.
  String getSQLState ()
  {
    return sqlState_;
  }

  // For ib engine states.
  String getSQLState (int errorCode, int ibSQLCode)
  {
    // The error key sqlState always overrides the default mapping
    if (sqlState_ != null)
      return sqlState_;
    else
      return mapIBCodesToSQLState (errorCode, ibSQLCode);
  }

  // This is only called for unhandled interbase errors.
  // That is, if interserver traps the interbase error and throws
  // an error back with a distinguised errorKey other than 
  // defaultEngineError__ then the SQLState will be gotten directly
  // from that errorKey.
  private String mapIBCodesToSQLState (int errorCode, int ibSQLCode) 
  {
    switch (errorCode) {
    // OutOfMemoryException
    case ErrorCodes.isc_bufexh:
    case ErrorCodes.isc_virmemexh:
      return sqlState__interbase_error__;

    // BugCheckException
    case ErrorCodes.isc_bug_check:
      return sqlState__interbase_error__;

    // UnlicensedComponentException
    /*
    case ErrorCodes.isc_unlicensed:
      return sqlState__interbase_error__;
      */

    // UnavailableDatabaseFileException
    case ErrorCodes.isc_io_error: // !! this should be handled by interserver on connect
    case ErrorCodes.isc_unavailable:
      return sqlState__interbase_error__;

    case ErrorCodes.isc_deadlock:        // DeadlockException
    case ErrorCodes.isc_update_conflict: // UpdateConflictException
    case ErrorCodes.isc_lock_conflict:   // LockConflictException
      return sqlState__interbase_error__;

    // CorruptDatabaseException
    case ErrorCodes.isc_bad_checksum:
    case ErrorCodes.isc_badpage:
    case ErrorCodes.isc_db_corrupt:
      return sqlState__interbase_error__;

    // UnauthorizedUserException
    case ErrorCodes.isc_login:
      return sqlState__interbase_error__;
    }

    switch (ibSQLCode) {
    case 104: 
    case 204:
    case 804:
    case 842:
      return sqlState__interbase_error__;
    case 501:
      return sqlState__interbase_error__;
    case 607:
      return sqlState__interbase_error__;
    case 802:
      return sqlState__interbase_error__;
    case 902:
      return sqlState__interbase_error__;
    default:
      return sqlState__interbase_error__;
    }
  }

  // For ic/is driver states.
  // The ib engine exception constructor will use
  // the errorCode as directly read over the wire,
  // regardless if key is distinguished.
  int getErrorCode ()
  {
    return errorCode_;
  }

  // Once interserver is rewritten in java it will
  // maintain its own resource bundles and then
  // only send back Reason, ErrorCode, and SQLState.
  // But for now, interserver sends back -
  // errorKeyIndex, reservedCode, errorCode, ibSQLCode, ibErrorMessage 
  // The last three (errorCode, ibSQLCode, ibErrorMessage) are only
  // considered for engine errors, and therefore may be used to hold
  // message argument data for interserver driver errors.

  // final static ErrorKey temp__ = new ErrorKey ("temp", "S1000", 0);
  
  // *** SQLException ***
  // ***   Always from engine only ***
  final static ErrorKey engine__default_0__
  = new ErrorKey (ResourceKeys.engine__default_0, 
		  null, 
		  0);

  // *** InvalidOperationException ***
  final static ErrorKey invalidOperation__connection_closed__ 
  = new ErrorKey (ResourceKeys.invalidOperation__connection_closed, 
		  "ICJ01",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__server_connection_closed__ 
  = new ErrorKey (ResourceKeys.invalidOperation__server_connection_closed, 
		  "ICJ02",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__result_set_closed__
  = new ErrorKey (ResourceKeys.invalidOperation__result_set_closed, 
		  "ICJ03",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__statement_closed__                     
  = new ErrorKey (ResourceKeys.invalidOperation__statement_closed, 
		  "ICJ04",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__transaction_in_progress__              
  = new ErrorKey (ResourceKeys.invalidOperation__transaction_in_progress, 
		  "ICJ05",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__commit_or_rollback_under_autocommit__  
  = new ErrorKey (ResourceKeys.invalidOperation__commit_or_rollback_under_autocommit, 
		  "ICJ06",
		  ErrorCodes.invalidOperation);

  // interclient and interserver
  // !! perhaps executeQuery on an update statement should be legal
  final static ErrorKey invalidOperation__execute_query_on_an_update_statement__ 
  = new ErrorKey (ResourceKeys.invalidOperation__execute_query_on_an_update_statement, 
		  "ICJ07",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__set_null_on_non_nullable_parameter__   
  = new ErrorKey (ResourceKeys.invalidOperation__set_null_on_non_nullable_parameter, 
		  "ICJ08",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__read_at_end_of_cursor__                
  = new ErrorKey (ResourceKeys.invalidOperation__read_at_end_of_cursor, 
		  "ICJ09",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__read_at_invalid_cursor_position__      
  = new ErrorKey (ResourceKeys.invalidOperation__read_at_invalid_cursor_position, 
		  "ICJ0A",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__was_null_with_no_data_retrieved__      
  = new ErrorKey (ResourceKeys.invalidOperation__was_null_with_no_data_retrieved, 
		  "ICJ0B",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidOperation__parameter_not_set__                    
  = new ErrorKey (ResourceKeys.invalidOperation__parameter_not_set, 
		  "ICJ0C",
		  ErrorCodes.invalidOperation);

  // *** DataConversionException extends InvalidOperationException ***

  // *** ParameterConversionException extends DataConversionException ***
  final static ErrorKey parameterConversion__type_conversion__     
  = new ErrorKey (ResourceKeys.parameterConversion__type_conversion, 
		  "ICJ10",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__type_conversion__set_number_on_binary_blob__
  = new ErrorKey (ResourceKeys.parameterConversion__type_conversion__set_number_on_binary_blob,
		  "ICJ11",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__type_conversion__set_date_on_binary_blob__
  = new ErrorKey (ResourceKeys.parameterConversion__type_conversion__set_date_on_binary_blob,
		  "ICJ12",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__set_object_on_stream__
  = new ErrorKey (ResourceKeys.parameterConversion__set_object_on_stream,
		  "ICJ13",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__instance_conversion_0__
  = new ErrorKey (ResourceKeys.parameterConversion__instance_conversion_0,
		  "ICJ14",
		  ErrorCodes.invalidOperation);

  // !!! look into this.
  // MMM - added for array support
  final static ErrorKey parameterConversion__array_element_type_conversion__
  = new ErrorKey (ResourceKeys.parameterConversion__array_element_type_conversion,
		  "ICJ15",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__array_element_instance_conversion_0__
  = new ErrorKey (ResourceKeys.parameterConversion__array_element_instance_conversion_0,
		  "ICJ16",
		  ErrorCodes.invalidOperation);

  final static ErrorKey parameterConversion__array_element_instance_truncation_0__
  = new ErrorKey (ResourceKeys.parameterConversion__array_element_instance_truncation_0,
		  "ICJ17",
		  ErrorCodes.invalidOperation);
  // MMM - end

  // *** ColumnConversionException extends DataConversionException ***
  final static ErrorKey columnConversion__type_conversion__        
  = new ErrorKey (ResourceKeys.columnConversion__type_conversion, 
		  "ICJ20",
		  ErrorCodes.invalidOperation);

  final static ErrorKey columnConversion__type_conversion__get_number_on_binary_blob__
  = new ErrorKey (ResourceKeys.columnConversion__type_conversion__get_number_on_binary_blob, 
		  "ICJ21",
		  ErrorCodes.invalidOperation);

  final static ErrorKey columnConversion__type_conversion__get_date_on_binary_blob__
  = new ErrorKey (ResourceKeys.columnConversion__type_conversion__get_date_on_binary_blob,
		  "ICJ22",
		  ErrorCodes.invalidOperation);

  final static ErrorKey columnConversion__instance_conversion_0__
  = new ErrorKey (ResourceKeys.columnConversion__instance_conversion_0, 
		  "ICJ23",
		  ErrorCodes.invalidOperation);

  // *** InvalidArgumentException extends InvalidOperationException ***
  final static ErrorKey invalidArgument__isolation_0__                               
  = new ErrorKey (ResourceKeys.invalidArgument__isolation_0, 
		  "ICJ30",
		  ErrorCodes.invalidOperation);

  // interserver only
  final static ErrorKey invalidArgument__connection_property__isolation__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_property__isolation,
		  "ICJ31",
		  ErrorCodes.invalidOperation);

  // interserver only
  final static ErrorKey invalidArgument__connection_property__lock_resolution_mode__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_property__lock_resolution_mode,
		  "ICJ32",
		  ErrorCodes.invalidOperation);

  // interserver only
  final static ErrorKey invalidArgument__connection_property__unrecognized__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_property__unrecognized,
		  "ICJ33",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__connection_properties__no_user_or_password__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_properties__no_user_or_password,
		  "ICJ34",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__connection_properties__null__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_properties__null,
		  "ICJ35",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__sql_empty_or_null__
  = new ErrorKey (ResourceKeys.invalidArgument__sql_empty_or_null,
		  "ICJ37",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__column_name_0__
  = new ErrorKey (ResourceKeys.invalidArgument__column_name_0,
		  "ICJ38",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__negative_row_fetch_size__
  = new ErrorKey (ResourceKeys.invalidArgument__negative_row_fetch_size,
		  "ICJ39",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__negative_max_rows__
  = new ErrorKey (ResourceKeys.invalidArgument__negative_max_rows,
		  "ICJ3A",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__fetch_size_exceeds_max_rows__
  = new ErrorKey (ResourceKeys.invalidArgument__fetch_size_exceeds_max_rows,
		  "ICJ3B",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__setUnicodeStream_odd_bytes__
  = new ErrorKey (ResourceKeys.invalidArgument__setUnicodeStream_odd_bytes,
		  "ICJ3C",
		  ErrorCodes.invalidOperation);

  // MMM - added for array support
  final static ErrorKey invalidArgument__not_array_column__
  = new ErrorKey (ResourceKeys.invalidArgument__not_array_column,
		  "ICJ3D",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__not_array_parameter__
  = new ErrorKey (ResourceKeys.invalidArgument__not_array_parameter,
		  "ICJ3E",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__invalid_array_slice__
  = new ErrorKey (ResourceKeys.invalidArgument__invalid_array_slice,
		  "ICJ3F",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__invalid_array_dimensions__
  = new ErrorKey (ResourceKeys.invalidArgument__invalid_array_dimensions,
		  "ICJ3G",
		  ErrorCodes.invalidOperation);
  // MMM - end

  final static ErrorKey invalidArgument__lock_resolution__
  = new ErrorKey (ResourceKeys.invalidArgument__lock_resolution,
		  "ICJ3H",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__version_acknowledgement_mode__
  = new ErrorKey (ResourceKeys.invalidArgument__version_acknowledgement_mode,
		  "ICJ3I",
		  ErrorCodes.invalidOperation);

  final static ErrorKey invalidArgument__table_lock__
  = new ErrorKey (ResourceKeys.invalidArgument__table_lock,
		  "ICJ3J",
		  ErrorCodes.invalidOperation);

// CJL-IB6
  final static ErrorKey invalidArgument__connection_properties__sql_dialect__
  = new ErrorKey (ResourceKeys.invalidArgument__connection_properties__sqlDialect_0,
		  "ICJ3K",
		  ErrorCodes.invalidOperation);
// CJL-IB6 end change

  // *** ColumnIndexOutOfBoundsException extends InvalidArgumentException ***
  final static ErrorKey columnIndexOutOfBounds__0__    
  = new ErrorKey (ResourceKeys.columnIndexOutOfBounds__0, 
		  "ICJ40",
		  ErrorCodes.invalidOperation);

  // *** ParameterIndexOutOfBoundsException extends InvalidArgumentException ***
  final static ErrorKey parameterIndexOutOfBounds__0__ 
  = new ErrorKey (ResourceKeys.parameterIndexOutOfBounds__0, 
		  "ICJ50",
		  ErrorCodes.invalidOperation);

  // *** URLSyntaxException extends InvalidArgumentException ***
  final static ErrorKey urlSyntax__bad_server_prefix_0__
  = new ErrorKey (ResourceKeys.urlSyntax__bad_server_prefix_0, 
		  "ICJ60",
		  ErrorCodes.invalidOperation);

  final static ErrorKey urlSyntax__bad_server_suffix_0__
  = new ErrorKey (ResourceKeys.urlSyntax__bad_server_suffix_0, 
		  "ICJ61",
		  ErrorCodes.invalidOperation);

  // *** EscapeSyntaxException extends invalidArgumentException ***
  final static ErrorKey escapeSyntax__no_closing_escape_delimeter_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__no_closing_escape_delimeter_0, 
		  "ICJ70",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__unrecognized_keyword_0__        
  = new ErrorKey (ResourceKeys.escapeSyntax__unrecognized_keyword_0, 
		  "ICJ71",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__d_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__d_0,
		  "ICJ72",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__ts_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__ts_0,
		  "ICJ73",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__escape_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__escape_0,
		  "ICJ74",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__escape__no_quote_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__escape__no_quote_0,
		  "ICJ75",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__fn_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__fn_0,
		  "ICJ76",
		  ErrorCodes.invalidOperation);

  final static ErrorKey escapeSyntax__call_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__call_0,
		  "ICJ77",
		  ErrorCodes.invalidOperation);

// CJL-IB6 time support
  final static ErrorKey escapeSyntax__t_0__
  = new ErrorKey (ResourceKeys.escapeSyntax__t_0,
		  "ICJ78",
		  ErrorCodes.invalidOperation);
// CJL-IB6 end change

  // *** BugCheckException ***
  // ***   This should never happen, but does not rely on network integrity.
  final static ErrorKey bugCheck__0__ // interclient and interserver
  = new ErrorKey (ResourceKeys.bugCheck__0, 
		  "ICJB0",
		  ErrorCodes.bugCheck);

  // *** CharacterEncodingException ***
  // ***   Normally due to user-error, but can also be caused by ic/is/ib bugs.
  final static ErrorKey characterEncoding__read_0__
  = new ErrorKey (ResourceKeys.characterEncoding__read_0, 
		  "ICJC0",
		  ErrorCodes.characterEncoding);

  final static ErrorKey characterEncoding__write_0__
  = new ErrorKey (ResourceKeys.characterEncoding__write_0, 
		  "ICJC1",
		  ErrorCodes.characterEncoding);

  // *** RemoteProtocolException ***
  // ***   These should never happen unless bug or network integrity is violated ***
  final static ErrorKey remoteProtocol__unexpected_token_from_server_0__          
  = new ErrorKey (ResourceKeys.remoteProtocol__unexpected_token_from_server_0, 
		  "ICJD0",
		  ErrorCodes.remoteProtocol);

  // interserver only
  final static ErrorKey remoteProtocol__unexpected_token_from_client__         
  = new ErrorKey (ResourceKeys.remoteProtocol__unexpected_token_from_client, 
		  "ICJD1",
		  ErrorCodes.remoteProtocol);

  final static ErrorKey remoteProtocol__unable_to_establish_protocol__          
  = new ErrorKey (ResourceKeys.remoteProtocol__unable_to_establish_protocol, 
		  "ICJD2",
		  ErrorCodes.remoteProtocol);

  final static ErrorKey remoteProtocol__bad_message_certficate_from_server__    
  = new ErrorKey (ResourceKeys.remoteProtocol__bad_message_certficate_from_server, 
		  "ICJD3",
		  ErrorCodes.remoteProtocol);

  // *** CommunicationException ***
  // ***   Network subsystem failures, IOException and SocketException ***
  final static ErrorKey communication__user_stream__io_exception_on_read_0__ 
  = new ErrorKey (ResourceKeys.communication__user_stream__io_exception_on_read_0, 
		  "ICJE0",
		  ErrorCodes.communication);

  final static ErrorKey communication__user_stream__unexpected_eof__     
  = new ErrorKey (ResourceKeys.communication__user_stream__unexpected_eof, 
		  "ICJE1",
		  ErrorCodes.communication);

  final static ErrorKey communication__socket_exception_on_connect_01__   
  = new ErrorKey (ResourceKeys.communication__socket_exception_on_connect_01, 
		  "ICJE2",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_connect_01__
  = new ErrorKey (ResourceKeys.communication__io_exception_on_connect_01, 
		  "ICJE3",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_disconnect_01__   
  = new ErrorKey (ResourceKeys.communication__io_exception_on_disconnect_01, 
		  "ICJE4",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_recv_protocol_01__ 
  = new ErrorKey (ResourceKeys.communication__io_exception_on_recv_protocol_01, 
		  "ICJE5",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_recv_message_01__ 
  = new ErrorKey (ResourceKeys.communication__io_exception_on_recv_message_01, 
		  "ICJE6",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_send_message_01__ 
  = new ErrorKey (ResourceKeys.communication__io_exception_on_send_message_01, 
		  "ICJE7",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_read_0__ 
  = new ErrorKey (ResourceKeys.communication__io_exception_on_read_0, 
		  "ICJE8",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_blob_read_01__    
  = new ErrorKey (ResourceKeys.communication__io_exception_on_blob_read_01, 
		  "ICJE9",
		  ErrorCodes.communication);

  final static ErrorKey communication__io_exception_on_blob_close_01__
  = new ErrorKey (ResourceKeys.communication__io_exception_on_blob_close_01, 
		  "ICJEA",
		  ErrorCodes.communication);

  // interserver only
  final static ErrorKey communication__interserver__
  = new ErrorKey (ResourceKeys.communication__interserver, 
		  "ICJEB",
		  ErrorCodes.communication);

  // *** SocketTimeoutException ***
  final static ErrorKey socketTimeout__012__
  = new ErrorKey (ResourceKeys.socketTimeout__012, 
		  "ICJF0",
		  ErrorCodes.socketTimeout);

  // *** UnknownHostException ***
  final static ErrorKey unknownHost__0__                            
  = new ErrorKey (ResourceKeys.unknownHost__0,
		  "ICJG0",
		  ErrorCodes.unknownHost);

  // *** BadInstallationException ***
  // !! this not currently used, but should be eventually.
  final static ErrorKey badInstallation__unsupported_jdk_version__       
  = new ErrorKey (ResourceKeys.badInstallation__unsupported_jdk_version, 
		  "ICJH0",
		  ErrorCodes.badInstallation);

  final static ErrorKey badInstallation__security_check_on_socket_01__    
  = new ErrorKey (ResourceKeys.badInstallation__security_check_on_socket_01, 
		  "ICJH1",
		  ErrorCodes.badInstallation);

  final static ErrorKey badInstallation__incompatible_remote_protocols__ 
  = new ErrorKey (ResourceKeys.badInstallation__incompatible_remote_protocols, 
		  "ICJH2",
		  ErrorCodes.badInstallation);

  // *** DriverNotCapableException ***
  final static ErrorKey driverNotCapable__out_parameters__                
  = new ErrorKey (ResourceKeys.driverNotCapable__out_parameters, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__schemas__                       
  = new ErrorKey (ResourceKeys.driverNotCapable__schemas, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__catalogs__                      
  = new ErrorKey (ResourceKeys.driverNotCapable__catalogs, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  // interclient and interserver
  final static ErrorKey driverNotCapable__isolation__                     
  = new ErrorKey (ResourceKeys.driverNotCapable__isolation, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__binary_literals__               
  = new ErrorKey (ResourceKeys.driverNotCapable__binary_literals, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  // interclient and interserver
  final static ErrorKey driverNotCapable__asynchronous_cancel__           
  = new ErrorKey (ResourceKeys.driverNotCapable__asynchronous_cancel,
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__query_timeout__                 
  = new ErrorKey (ResourceKeys.driverNotCapable__query_timeout, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  // interserver only
  final static ErrorKey driverNotCapable__connection_timeout__                 
  = new ErrorKey (ResourceKeys.driverNotCapable__connection_timeout, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  // interclient and interserver
  final static ErrorKey driverNotCapable__extension_not_yet_supported__
  = new ErrorKey (ResourceKeys.driverNotCapable__extension_not_yet_supported,
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__jdbc2_not_yet_supported__
  = new ErrorKey (ResourceKeys.driverNotCapable__jdbc2_not_yet_supported,
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__escape__t__
  = new ErrorKey (ResourceKeys.driverNotCapable__escape__t, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable); 

  final static ErrorKey driverNotCapable__escape__ts_fractionals__        
  = new ErrorKey (ResourceKeys.driverNotCapable__escape__ts_fractionals,
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  final static ErrorKey driverNotCapable__escape__call_with_result__      
  = new ErrorKey (ResourceKeys.driverNotCapable__escape__call_with_result, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

  // MMM - added for array support
  final static ErrorKey driverNotCapable__input_array_metadata__
  = new ErrorKey (ResourceKeys.driverNotCapable__input_array_metadata,
		  "0A000",
		  ErrorCodes.driverNotCapable);
  // MMM - end

  // *** UnsupportedCharacterSetException extends DriverNotCapableException ***
  final static ErrorKey unsupportedCharacterSet__0__ // interclient and interserver
  = new ErrorKey (ResourceKeys.unsupportedCharacterSet__0,
		  sqlState__featureNotSupported__,
		  ErrorCodes.driverNotCapable);

// CJL-IB6!!! fix this so it has its own resources
// <!> register SQL Code 01JB0 in docs
  final static ErrorKey unsupportedSQLDialect__dialect_adjusted__ // interserver
  = new ErrorKey (ResourceKeys.sqlDialectAdjustmentWarning__0,
		  "01JB0",
		  ErrorCodes.unsupportedSQLDialectException__);

  // *** OutOfMemoryException ***
  final static ErrorKey outOfMemory__  // interserver only                        
  = new ErrorKey (ResourceKeys.outOfMemory, 
		  "ICJI0", 
		  ErrorCodes.outOfMemory);
          
  //david jencks 1-21-2001
  // invalid table type in databaseMetaData.getTables()
  final static ErrorKey dbmd_getTables_invalid_table_type__  // interserver only                        
  = new ErrorKey (ResourceKeys.dbmd_getTables_invalid_table_type, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.invalidArgument__);

  //david jencks 1-26-2001
  // table name must at least be "" in databaseMetaData.getColumnPrivileges()
  final static ErrorKey dbmd_getColumnPrivileges_table_name_required__  // interserver only                        
  = new ErrorKey (ResourceKeys.dbmd_getColumnPrivileges_table_name_required, 
		  sqlState__featureNotSupported__,
		  ErrorCodes.invalidArgument__);

  // SQLStates ICJJ* are reserved for missing resource bundles, 
  // see Driver class, and ib.ic.utils.CommDiag class.
  
  // these must match IB_SQLException::ErrorKey indices
  
// CJL-IB6  !!! New ErrorKey added, but we must do
//    something about the missing errorKey (13)
  static final ErrorKey[] interserverErrorKeys__ =
  {
    engine__default_0__,                               // 0
    bugCheck__0__,                                     // 1
    outOfMemory__,                                     // 2
    unsupportedCharacterSet__0__,                      // 3
    driverNotCapable__extension_not_yet_supported__,   // 4   !!! change name in IS to match
    driverNotCapable__asynchronous_cancel__,           // 5
    driverNotCapable__isolation__,                     // 6
    driverNotCapable__connection_timeout__,            // 7
    communication__interserver__,                      // 8 -- this is a place holder!!!
    invalidArgument__connection_property__lock_resolution_mode__, // 9
    invalidArgument__connection_property__isolation__,        // 10 
    invalidArgument__connection_property__unrecognized__,     // 11
    invalidOperation__execute_query_on_an_update_statement__, // 12
    communication__interserver__,                      // 13 -- this is a place holder!!!
    remoteProtocol__unexpected_token_from_client__,    // 14
    communication__interserver__,                      // 15
    unsupportedSQLDialect__dialect_adjusted__          // 16
  };

}


