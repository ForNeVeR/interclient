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
 * Resource bundle keys used internally by InterClient.
 * <p>
 * These keys are provided only as a convenience for managing
 * locale-specific resource strings.
 * These keys will change with each release, 
 * some may be added and some may be deleted,
 * so do not use these keys for any purpose other than a version-
 * dependent resource bundle.
 * <p>
 * The resource for any key can be obtained as follows:
 * <pre>
 * java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle ("interbase.interclient.Resources");
 * resources.getString (<i>key</i>);
 * </pre>
 *
 * @see Resources
 * @since <font color=red>Extension, since 1.50, not public in 1.50 final release</a>
 * @author Paul Ostler
 **/
class ResourceKeys
{
  // DEVELOPER NOTES:
  // Warning: See devCheckLists.html before editing this file!
  // Preship: Make sure all keys have an associated resource!!!

  // Define a private constructor to prevent default public constructor
  private ResourceKeys () {}
  
  // *** This is actually not used in the code, but
  // *** is useful to the developer for assigning new keys,
  // *** just keep incrementing it.  Try to avoid reusing
  // *** holes in case an old bundle is used with a new build.
// CJL-IB6 incremented from 127
  private final static int lastKeyValue__ = 131;
// CJL-IB6 end change

  // *** Miscellaneous text keys ***
  final static public String seeApi = "0";
  final static public String driverOriginationIndicator = "89";
  final static public String ibOriginationIndicator = "106";
  final static public String interclient = "94";
  final static public String interserver = "95";
  final static public String interbase = "96";
  final static public String companyName = "97";
  final static public String propertyDescription__user = "107";
  final static public String propertyDescription__password = "108";
  final static public String propertyDescription__charSet = "109";
  final static public String propertyDescription__roleName = "110";
  final static public String propertyDescription__sweepOnConnect = "112";
  final static public String propertyDescription__suggestedCachePages = "114";
// CJL-IB6 new descriptions for SQL Dialect support
  final static public String propertyDescription__sqlDialect = "128";
// CJL-IB6 end change

  // *** java.util.MissingResourceException ***
  final static public String missingResource__01 = "1";

  // *** SQLException ***
  // ***   Always from engine only ***
  final static public String engine__default_0 = "2";

  // *** InvalidOperationException ***
  final static public String invalidOperation__connection_closed  = "3";
  final static public String invalidOperation__server_connection_closed  = "103";
  final static public String invalidOperation__result_set_closed  = "4";
  final static public String invalidOperation__statement_closed  = "5";
  final static public String invalidOperation__transaction_in_progress  = "6";
  final static public String invalidOperation__commit_or_rollback_under_autocommit  = "7";

  // !!! perhaps executeQuery on an update statement should be legal
  final static public String invalidOperation__execute_query_on_an_update_statement  = "8"; //is/ic
  final static public String invalidOperation__set_null_on_non_nullable_parameter  = "9";
  final static public String invalidOperation__read_at_end_of_cursor  = "10";
  final static public String invalidOperation__read_at_invalid_cursor_position  = "11";
  final static public String invalidOperation__was_null_with_no_data_retrieved  = "12";
  final static public String invalidOperation__parameter_not_set  = "13";

  // *** DataConversionException extends InvalidOperationException ***

  // *** ParameterConversionException extends DataConversionException ***
  final static public String parameterConversion__set_object_on_stream = "104";
  final static public String parameterConversion__type_conversion  = "14";
  final static public String parameterConversion__type_conversion__set_number_on_binary_blob = "98";
  final static public String parameterConversion__type_conversion__set_date_on_binary_blob = "99";
  final static public String parameterConversion__instance_conversion_0  = "15";
  // MMM - added for array support    
  final static public String parameterConversion__array_element_type_conversion = "116";
  final static public String parameterConversion__array_element_instance_conversion_0 = "117";
  final static public String parameterConversion__array_element_instance_truncation_0 = "118";
  // MMM - end

  // *** ColumnConversionException extends DataConversionException ***
  final static public String columnConversion__type_conversion  = "16";
  final static public String columnConversion__type_conversion__get_number_on_binary_blob = "100";
  final static public String columnConversion__type_conversion__get_date_on_binary_blob = "101";
  final static public String columnConversion__instance_conversion_0  = "17";

  // *** InvalidArgumentException extends InvalidOperationException ***
  final static public String invalidArgument__isolation_0  = "18";
  final static public String invalidArgument__connection_property__isolation  = "19"; //is
  final static public String invalidArgument__connection_property__lock_resolution_mode  = "20"; //is
  final static public String invalidArgument__connection_property__unrecognized  = "21"; //is
  final static public String invalidArgument__connection_properties__no_user_or_password = "22";
  final static public String invalidArgument__connection_properties__null = "92";
// CJL-IB6
  final static public String invalidArgument__connection_properties__sqlDialect_0 = "129";
// CJL-IB6 end change
  final static public String invalidArgument__sql_empty_or_null  = "23";
  final static public String invalidArgument__column_name_0  = "24";
  final static public String invalidArgument__negative_row_fetch_size  = "25";
  final static public String invalidArgument__negative_max_rows  = "26";
  final static public String invalidArgument__fetch_size_exceeds_max_rows  = "27";
  final static public String invalidArgument__setUnicodeStream_odd_bytes  = "91";
  // MMM - added for array support   
  final static public String invalidArgument__invalid_array_dimensions = "120";
  final static public String invalidArgument__invalid_array_slice = "121";
  final static public String invalidArgument__not_array_parameter = "122";
  final static public String invalidArgument__not_array_column = "123";
  // MMM - end
  final static public String invalidArgument__lock_resolution = "125";
  final static public String invalidArgument__version_acknowledgement_mode = "126";
  final static public String invalidArgument__table_lock = "127";

  // *** ColumnIndexOutOfBoundsException extends InvalidArgumentException ***
  final static public String columnIndexOutOfBounds__0  = "28";

  // *** ParameterIndexOutOfBoundsException extends InvalidArgumentException ***
  final static public String parameterIndexOutOfBounds__0  = "29";

  // *** URLSyntaxException extends InvalidArgumentException ***
  final static public String urlSyntax__bad_server_prefix_0  = "30";
  final static public String urlSyntax__bad_server_suffix_0  = "31";

  // *** EscapeSyntaxException extends invalidArgumentException ***
  final static public String escapeSyntax__no_closing_escape_delimeter_0  = "32";
  final static public String escapeSyntax__unrecognized_keyword_0  = "33";
  final static public String escapeSyntax__d_0  = "34";
  final static public String escapeSyntax__ts_0  = "35";
  final static public String escapeSyntax__escape_0  = "36";
  final static public String escapeSyntax__escape__no_quote_0  = "37";
  final static public String escapeSyntax__fn_0  = "38";
  final static public String escapeSyntax__call_0  = "39";
  final static public String escapeSyntax__t_0 = "131";
  
  // *** BugCheckException ***
  // ***   This should never happen, but does not rely on network integrity.
  final static public String bugCheck__0  = "44"; //is/ic

  // *** CharacterEncodingException ***
  // ***   Normally due to user-error, but can also be caused by ic/is/ib bugs.
  final static public String characterEncoding__read_0  = "45";
  final static public String characterEncoding__write_0  = "46";

  // *** RemoteProtocolException ***
  // ***   These should never happen unless bug or network integrity is violated ***
  final static public String remoteProtocol__unexpected_token_from_server_0  = "47";
  final static public String remoteProtocol__unexpected_token_from_client  = "48"; //is
  final static public String remoteProtocol__unable_to_establish_protocol  = "49";
  final static public String remoteProtocol__bad_message_certficate_from_server  = "50";

  // *** CommunicationException ***
  // ***   Network subsystem failures, IOException and SocketException ***
  final static public String communication__user_stream__io_exception_on_read_0  = "51";
  final static public String communication__user_stream__unexpected_eof  = "52";
  final static public String communication__socket_exception_on_connect_01  = "53";
  final static public String communication__io_exception_on_connect_01  = "54";
  final static public String communication__io_exception_on_disconnect_01  = "55";
  final static public String communication__io_exception_on_recv_protocol_01  = "56";
  final static public String communication__io_exception_on_recv_message_01  = "57";
  final static public String communication__io_exception_on_send_message_01  = "58";
  final static public String communication__io_exception_on_read_0  = "59";
  final static public String communication__io_exception_on_blob_read_01  = "60";
  final static public String communication__io_exception_on_blob_close_01  = "90";
  final static public String communication__interserver  = "61"; //is

  // *** BlobIOException extends IOException ***
  // ***   thrown only by BlobInputStream operations ***
  final static public String blobIO__sqlException_on_read_0  = "62";
  final static public String blobIO__sqlException_on_close_0  = "63";
  final static public String blobIO__sqlException_on_skip_0  = "64";
  final static public String blobIO__ioException_on_read_0  = "65";
  final static public String blobIO__ioException_on_skip_0  = "66";
  final static public String blobIO__read_on_closed  = "67";
  final static public String blobIO__skip_on_closed  = "68";
  final static public String blobIO__mark_not_supported  = "69";

  // *** SocketTimeoutException ***
  final static public String socketTimeout__012  = "70";

  // *** UnknownHostException ***
  final static public String unknownHost__0  = "71";

  // *** BadInstallationException ***
  // !!! this not currently used, but should be eventually.
  final static public String badInstallation__unsupported_jdk_version  = "72";
  final static public String badInstallation__security_check_on_socket_01  = "73";
  final static public String badInstallation__incompatible_remote_protocols  = "74";

  // *** DriverNotCapableException ***
  final static public String driverNotCapable__out_parameters  = "75";
  final static public String driverNotCapable__schemas  = "76";
  final static public String driverNotCapable__catalogs  = "77";
  final static public String driverNotCapable__isolation  = "78"; //is/ic
  final static public String driverNotCapable__binary_literals = "79";
  final static public String driverNotCapable__asynchronous_cancel = "80"; //is/ic
  final static public String driverNotCapable__query_timeout = "81";
  final static public String driverNotCapable__connection_timeout = "82"; //is
  final static public String driverNotCapable__extension_not_yet_supported = "83"; //is/ic
  final static public String driverNotCapable__jdbc2_not_yet_supported = "105"; 
  final static public String driverNotCapable__escape__t  = "84";
  final static public String driverNotCapable__escape__ts_fractionals  = "85";
  final static public String driverNotCapable__escape__call_with_result  = "86";
  // MMM - added for array support  
  final static public String driverNotCapable__input_array_metadata = "124"; //ib/is/ic
  // MMM - end

  // *** UnsupportedCharacterSetException extends DriverNotCapableException ***
  final static public String unsupportedCharacterSet__0  = "87"; //is/ic

// CJL-IB6
  // *** SQLDialectAdjustmentWarning ***
  final static public String sqlDialectAdjustmentWarning__0 = "130";  //is
// CJL-IB6 end changes

  // *** OutOfMemoryException ***
  final static public String outOfMemory  = "88"; //is
}
