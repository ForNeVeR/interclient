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

/**
 * Default resource bundle for InterClient error messages
 * and hardwired text within the driver.
 * <p>
 * This is the default (English) resource bundle.
 * You can provide your own locale-specific bundle
 * with an ISO language suffix appended to the class name for
 * your language.  Additional or replacement locale-specific
 * resource bundles must reside in the <code>interbase.interclient</code>
 * package.
 * <p>
 * The driver locates the appropriate resource bundle for your locale by using
 * <pre>
 * java.util.ResourceBundle.getBundle("interbase.interclient.Resources");
 * </pre>
 * Using the ClassLoader, this call in turn looks for the appropriate bundle
 * as a .class file in the following order based on your locale:
 * <ol>
 * <li>Resources_language_country_variant
 * <li>Resources_language_country
 * <li>Resources_language
 * <li>Resources (this is the default bundle shipped with InterClient)
 * </ol>
 * <p>
 * <b>Instructions for customizing resources for your locale:</b><br>
 * Follow these instructions only if a resource bundle for your locale is not already
 * included in the InterClient distribution.
 * Having the Resources.java source file will save you from having to call
 * <code>getContents()</code> as described below.
 * <ol>
 * <li> Get the contents from this Resources class
 *      by calling {@link #getContents getContents()}. Set aside for translation;
 *      write the contents, including keys and text, to a file or printer for translation.
 * <li> Create a Resources_*.java class for your locale with translated text.
 *      Be sure the class is public, extends <code>java.util.ListResourceBundle</code> and
 *      and is in the package <code>interbase.interclient</code>.  For example:
 * <pre>
 * // A French language resource bundle for InterClient.
 * package interbase.interclient;
 *
 * public class Resources_fr extends java.util.ListResourceBundle
 * {
 *   // Contains the translated text for each resource key from default Resources contents.
 *   static private final Object[][] contents =
 *   {
 *     {"1", "Mot de passe incorrect"}, // The password is incorrect
 *     {"2", "Saisie du mot de passe"}, // Enter your password
 *     ...
 *     {"98", "Quitter"} // Exit
 *   }
 *
 *   public Object[][] getContents()
 *   {
 *     return contents;
 *   }
 * }
 * </pre>
 * <li>Use <code>jar xvf interclient.jar</code> to extract the InterClient class files,
 * and include your new <code>Resources_fr.class</code> in the <code>interbase/interclient</code> directory.
 * Use <code>jar cvf0 interclient.jar interbase borland</code> to archive all the
 * files back into a new <code>interclient.jar</code> file.
 * Class directories <code>interbase/interclient</code>,
 * <code>interbase/interclient/utils</code>, <code>borland/jdbc</code>,
 * and <code>com/inprise/jdbc</code> should all be archived by this command.
 * <p>
 * Alternatively, modify the separate resource archive interclient-res.jar,
 * and include both <code>interclient-core.jar</code>, <code>interclient-res.jar</code>,
 * and optionally <code>interclient-utils.jar</code> in your
 * classpath.
 * For more information on jar, refer to the documentation for your Java Development Kit.
 * </ol>
 * <p>
 * A good article on using resources for localization may be found at
 * <a href="http://developer.java.sun.com/developer/technicalArticles/ResourceBundles.html">
 * http://developer.java.sun.com/developer/technicalArticles/ResourceBundles.html
 * </a>
 * <p>
 * <b>Note for bundle designers:</b>
 * All resource strings are processed by
 * {@link java.text.MessageFormat#format(String, Object[]) java.text.MessageFormat.format(String, Object[])},
 * so all occurrences of the {, } and ' characters in resource strings must
 * be delimited with quotes as '{', or '}' for the { and } characters,
 * and '' for the ' character.
 * For details see the Sun javadocs for class {@link java.text.MessageFormat java.text.MessageFormat}.
 *
 * @since <font color=red>Extension, since InterClient 1.50</font>
 * @see java.util.Locale
 * @see java.util.ResourceBundle
 * @author Paul Ostler
 **/
public class Resources extends java.util.ListResourceBundle
{
  // Note: This class must be loaded before any other class in
  //       the package, therefore do not reference VersionInformation class
  //       when building resource strings.

  final static private String lineSeparator__ = "\n";
  // java.lang.System.getProperty ("line.separator");

  final static private Object[][] resources__ =
  {
    // *******************************
    // *** Miscellaneous text keys ***
    // *******************************
    {ResourceKeys.seeApi,
     lineSeparator__ + "See API reference for exception interbase.interclient."},

    {ResourceKeys.driverOriginationIndicator,
     "[interclient] "},

    {ResourceKeys.ibOriginationIndicator,
     "[interclient][interbase] "},

    {ResourceKeys.interclient,
     "InterClient"},

    {ResourceKeys.interserver,
     "InterServer"},

    {ResourceKeys.interbase,
     "InterBase"},

// CJL-IB6  This really should have been changed for 1.51..
//    {ResourceKeys.companyName,
//     "Inprise Corporation"},

    {ResourceKeys.companyName,
     "InterBase Software Corporation"},
//  CJL end

    {ResourceKeys.propertyDescription__user,
     "The user name for the connection"},

    {ResourceKeys.propertyDescription__password,
     "The user''s password for the connection"},

    {ResourceKeys.propertyDescription__charSet,
     "The character encoding for the connection"},

    {ResourceKeys.propertyDescription__roleName,
     "The user''s SQL role name for the connection"},

    {ResourceKeys.propertyDescription__sweepOnConnect,
     "Force garbage collection of outdated record versions upon connection"},

    {ResourceKeys.propertyDescription__suggestedCachePages,
     "The suggested number of cache page buffers to use for the connection"},

// CJL-IB6 referenced by Driver.getPropertyInfo()
    {ResourceKeys.propertyDescription__sqlDialect,
     "The SQL Dialect for the connection"},
// CJL-IB6 end change

    // ********************************
    // *** MissingResourceException ***
    // ********************************
    {ResourceKeys.missingResource__01,
     "No resource for key {0} could be found in resource bundle {1}."},

    // *********************************
    // *** SQLException              ***
    // ***   Always from engine only ***
    // *********************************
    {ResourceKeys.engine__default_0,
     "{0}"},

    // *********************************
    // *** InvalidOperationException ***
    // *********************************
    {ResourceKeys.invalidOperation__connection_closed, // ICJ01
     "Invalid operation on a closed connection."},

    {ResourceKeys.invalidOperation__server_connection_closed, // ICJ02
     "Invalid operation on a closed server connection."},

    {ResourceKeys.invalidOperation__result_set_closed, // ICJ03
     "Invalid operation on a closed result set."},

    {ResourceKeys.invalidOperation__statement_closed, // ICJ04
     "Invalid operation on a closed statement."},

    {ResourceKeys.invalidOperation__transaction_in_progress, // ICJ05
     "Invalid operation when transaction is in progress."},

    {ResourceKeys.invalidOperation__commit_or_rollback_under_autocommit, // ICJ06
     "Invalid operation to commit or rollback a transaction while in auto-commit mode."},

    {ResourceKeys.invalidOperation__execute_query_on_an_update_statement, // ICJ07
     "Invalid operation to executeQuery on an update statement."},

    {ResourceKeys.invalidOperation__set_null_on_non_nullable_parameter, // ICJ08
     "Invalid operation to set a non-nullable input parameter to null."},

    {ResourceKeys.invalidOperation__read_at_end_of_cursor, // ICJ09
     "Invalid operation to read past end of cursor."},

    {ResourceKeys.invalidOperation__read_at_invalid_cursor_position, // ICJ0A
     "Invalid operation to read at current cursor position."},

    {ResourceKeys.invalidOperation__was_null_with_no_data_retrieved, // ICJ0B
     "Invalid operation to check wasNull() when no column data has been retrieved for the current row."},

    {ResourceKeys.invalidOperation__parameter_not_set, // ICJ0C
     "Invalid operation to execute a prepared statement with an unset non-nullable input parameter." +
     lineSeparator__ +
     "All input parameters for non-nullable columns must be set," +
     " unset nullable columns are assumed null."},

    // *****************************************************************
    // *** DataConversionException extends InvalidOperationException ***
    // *****************************************************************

    // ********************************************************************
    // *** ParameterConversionException extends DataConversionException ***
    // ********************************************************************
    {ResourceKeys.parameterConversion__type_conversion, // ICJ10
     "Invalid data conversion:" +
     " Wrong parameter type for requested conversion."},

    {ResourceKeys.parameterConversion__type_conversion__set_number_on_binary_blob, // ICJ11
     "Invalid data conversion:" +
     " Attempt to set a number to a non-character binary blob field."},

    {ResourceKeys.parameterConversion__type_conversion__set_date_on_binary_blob, // ICJ12
     "Invalid data conversion:" +
     " Attempt to set a date, time, or timestamp to a non-character binary blob field."},

    {ResourceKeys.parameterConversion__set_object_on_stream, // ICJ13
     "Invalid data conversion:" +
     " It is an invalid operation to send a Java input stream" +
     " using the setObject method." +
     lineSeparator__ +
     " You must explicitly use PreparedStatement.setXXXStream" +
     " to transfer a value as a stream per the JDBC specification." +
     lineSeparator__ +
     " Refer to the JDBC 1.20 specification pg. 43 for details."},

    {ResourceKeys.parameterConversion__instance_conversion_0, // ICJ14
     "Invalid data conversion:" +
     " Parameter instance {0} is out of range for requested conversion."},

     // MMM - added for the array support
    {ResourceKeys.parameterConversion__array_element_type_conversion,  // ICJ15
     "Invalid data conversion:" +
     " Array parameter element has wrong type for requested conversion."},

    {ResourceKeys.parameterConversion__array_element_instance_conversion_0,  // ICJ16
     "Invalid data conversion:" +
     " Array parameter element value {0} is out of range for requested conversion."},

    {ResourceKeys.parameterConversion__array_element_instance_truncation_0, // ICJ17
     "Invalid data conversion:" +
     " Array parameter element value {0} cannot be stored without truncation."},
     // MMM - end

    // *****************************************************************
    // *** ColumnConversionException extends DataConversionException ***
    // *****************************************************************
    {ResourceKeys.columnConversion__type_conversion, // ICJ20
     "Invalid data conversion:" +
     " Wrong result column type for requested conversion."},

    {ResourceKeys.columnConversion__type_conversion__get_number_on_binary_blob, // ICJ21
     "Invalid data conversion:" +
     " Attempt to get a number from a non-character binary blob field."},

    {ResourceKeys.columnConversion__type_conversion__get_date_on_binary_blob, // ICJ22
     "Invalid data conversion:" +
     " Attempt to get a date, time, or timestamp from a non-character binary blob field."},

    {ResourceKeys.columnConversion__instance_conversion_0, // ICJ23
     "Invalid data conversion:" +
     " Result column instance {0} is out of range for requested conversion."},

    // ******************************************************************
    // *** InvalidArgumentException extends InvalidOperationException ***
    // ******************************************************************
    {ResourceKeys.invalidArgument__isolation_0, // ICJ30
     "Invalid argument:" +
     " Transaction isolation level {0} is invalid."},

    {ResourceKeys.invalidArgument__connection_property__isolation, // ICJ31
     "Invalid argument:" +
     " Transaction isolation specified in connection properties is not known."},

    {ResourceKeys.invalidArgument__connection_property__lock_resolution_mode, // ICJ32
     "Invalid argument:" +
     " Lock resolution specified in connection properties is not valid."},

    {ResourceKeys.invalidArgument__connection_property__unrecognized, // ICJ33
     "Invalid argument:" +
     " Unrecognized connection property specified."},

    {ResourceKeys.invalidArgument__connection_properties__no_user_or_password, // ICJ34
     "Invalid argument:" +
     " User and password connection properties are not set."},

    {ResourceKeys.invalidArgument__connection_properties__null, // ICJ35
     "Invalid argument:" +
     " Connection properties is null."},

// CJL-IB6 added for SQL dialect support !!! SQLCODE TBD!!!
    {ResourceKeys.invalidArgument__connection_properties__sqlDialect_0, // !!!
    "Invalid argument:" +
    " SQL Dialect \"{0}\" specified in connection properties is not valid " },
// CJL-IB6 end change

    // ICJ36 no longer used

    {ResourceKeys.invalidArgument__sql_empty_or_null, // ICJ37
     "Invalid argument:" +
     " SQL string is null or empty."},

    {ResourceKeys.invalidArgument__column_name_0, // ICJ38
     "Invalid argument:" +
     " Result column name {0} is not known."},

    {ResourceKeys.invalidArgument__negative_row_fetch_size, // ICJ39
     "Invalid argument:" +
     " Negative row fetch size specified."},

    {ResourceKeys.invalidArgument__negative_max_rows, // ICJ3A
     "Invalid argument:" +
     " Negative number specified for maximum rows for result set."},

    {ResourceKeys.invalidArgument__fetch_size_exceeds_max_rows, // ICJ3B
     "Invalid argument:" +
     " Row fetch size may not exceed maximum rows for result set."},

    {ResourceKeys.invalidArgument__setUnicodeStream_odd_bytes, // ICJ3C
     "Invalid argument:" +
     " Attempt to set a unicode input stream with an odd number of bytes."},

    // MMM - added for array support
    {ResourceKeys.invalidArgument__not_array_column,    // ICJ3D
     "Invalid argument:" +
     " Result column specified is not of array data type."},

    {ResourceKeys.invalidArgument__not_array_parameter,  // ICJ3E
     "Invalid argument:" +
     " Input column specified is not of array data type."},

    {ResourceKeys.invalidArgument__invalid_array_slice,   // ICJ3F
     "Invalid argument:" +
     " Specified array slice is out of bounds."},

    {ResourceKeys.invalidArgument__invalid_array_dimensions, // ICJ3G
     "Invalid argument:" +
     " Array dimensions do not match array metadata."},
    // MMM - end

    {ResourceKeys.invalidArgument__lock_resolution, // ICJ3H
     "Invalid argument:" +
     " Attempt to set an invalid lock resolution mode for the transaction."},

    {ResourceKeys.invalidArgument__version_acknowledgement_mode, // ICJ3I
     "Invalid argument:" +
     " Attempt to set an invalid version acknowledgement mode for the transaction."},

    {ResourceKeys.invalidArgument__table_lock, // ICJ3J
     "Invalid argument:" +
     " Attempt to set an invalid table lock for the transaction."},

    // ************************************************************************
    // *** ColumnIndexOutOfBoundsException extends InvalidArgumentException ***
    // ************************************************************************
    {ResourceKeys.columnIndexOutOfBounds__0, // ICJ40
     "Invalid argument:" +
     " Result column index {0} is out of range."},

    // ***************************************************************************
    // *** ParameterIndexOutOfBoundsException extends InvalidArgumentException ***
    // ***************************************************************************
    {ResourceKeys.parameterIndexOutOfBounds__0, // ICJ50
     "Invalid argument:" +
     " Input parameter index {0} is out of range."},

    // ***********************************************************
    // *** URLSyntaxException extends InvalidArgumentException ***
    // ***********************************************************
    {ResourceKeys.urlSyntax__bad_server_prefix_0, // ICJ60
     "Malformed InterBase database JDBC URL: {0}" +
     lineSeparator__ +
     "Server specification must begin with ''//<server>''" +
     lineSeparator__ +
     "(eg. jdbc:interbase://hal//databases/employee.gdb," +
     " or jdbc:interbase://hal/C:/databases/employee.gdb)."},

    {ResourceKeys.urlSyntax__bad_server_suffix_0, // ICJ61
     "Malformed InterBase database JDBC URL: {0}" +
     lineSeparator__ +
     "Server name must be followed by ''/<absolute path to file>''" +
     lineSeparator__ +
     "(eg. jdbc:interbase://hal//databases/employee.gdb," +
     " or jdbc:interbase://hal/C:/databases/employee.gdb)."},

    // **************************************************************
    // *** EscapeSyntaxException extends InvalidArgumentException ***
    // **************************************************************
    {ResourceKeys.escapeSyntax__no_closing_escape_delimeter_0, // ICJ70
     "Malformed SQL escape syntax: Missing closing escape delimeter '}'." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__unrecognized_keyword_0, // ICJ71
     "Malformed SQL escape syntax: Unrecognized escape keyword." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__d_0, // ICJ72
     "Malformed SQL escape syntax for date '{'d ''yyyy-mm-dd'''}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__ts_0, // ICJ73
     "Malformed SQL escape syntax for timestamp '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__escape_0, // ICJ74
     "Malformed SQL escape syntax for LIKE '{'escape ''escape-character'''}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__escape__no_quote_0, // ICJ75
     "Malformed SQL escape syntax for LIKE '{'escape ''escape-character'''}' clause." +
     lineSeparator__ +
     "Missing '' in escape syntax." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__fn_0, // ICJ76
     "Malformed SQL escape syntax for scalar function '{'fn ...'}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

    {ResourceKeys.escapeSyntax__call_0, // ICJ77
     "Malformed SQL escape syntax for stored procedure '{'call ...'}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},

// CJL-IB6 added for escape t support
    {ResourceKeys.escapeSyntax__t_0, // ICJ78
     "Malformed SQL escape syntax for time '{'t ''hh:mm:ss.f...'''}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},
// CJL-IB6 end change

// CJL-IB6
    // ***********************************
    // *** SQLDialectAdjustmentWarning ***
    // ***********************************
    {ResourceKeys.sqlDialectAdjustmentWarning__0, // 01JB0
     "Specified dialect not supported:" +
     " The SQL Dialect has been changed to {0}."},
// CJL-IB6 end change

    // *************************
    // *** BugCheckException ***
    // *************************
    {ResourceKeys.bugCheck__0, // ICJB0
     "Bug detected." +
     lineSeparator__ +
     "Please send mail to interclient@interbase.com" +
     lineSeparator__ +
     "Refer to bug code {0}."},

    // *************************************************
    // *** CharacterEncodingException                ***
    // ***   Normally due to user-error,             ***
    // ***   but can also be caused by ic/is/ib bugs ***
    // *************************************************
    {ResourceKeys.characterEncoding__read_0, // ICJC0
     "Character encoding error:" +
     " A character conversion exception occurred while trying to decode a String encoding from server." +
     lineSeparator__ +
     "The message of the CharConversionException is \"{0}\"."},

    {ResourceKeys.characterEncoding__write_0, // ICJC1
     "Character encoding error:" +
     " A character conversion exception occurred while trying to encode a String to send to server." +
     lineSeparator__ +
     "The message of the CharConversionException is \"{0}\"."},

    // ***************************************************************
    // *** RemoteProtocolException                                 ***
    // ***   Either indicates bug or network integrity is violated ***
    // ***************************************************************
    {ResourceKeys.remoteProtocol__unexpected_token_from_server_0, // ICJD0
     "Client/Server protocol error:" +
     " Unexpected token in network message received from InterServer." +
     lineSeparator__ +
     "The internal code is {0}."},

    {ResourceKeys.remoteProtocol__unexpected_token_from_client, // ICJD1
     "Client/Server protocol error:" +
     " InterServer received an unexpected token in network message from InterClient."},

    {ResourceKeys.remoteProtocol__unable_to_establish_protocol, // ICJD2
     "Client/Server protocol error:" +
     " Unable to establish a protocol with server for remote messaging."},

     //david jencks 2-5-2001
    {ResourceKeys.remoteProtocol__bad_message_certficate_from_server, // ICJD3
     "Client/Server protocol error:" +
     " Unrecognized network message received from InterServer:" +
     "Incorrect interserver message certificate." +
     "Expecting: {0} Received: {1}"},

    // ************************************
    // *** CommunicationException       ***
    // ***   Network subsystem failures ***
    // ************************************
    {ResourceKeys.communication__user_stream__io_exception_on_read_0, // ICJE0
     "Communication error:" +
     " An IO exception occurred while reading from a user supplied input stream." +
     lineSeparator__ +
     "The message of the IOException is \"{0}\"."},

    {ResourceKeys.communication__user_stream__unexpected_eof, // ICJE1
     "Communication error:" +
     " Unexpected end of stream while reading from a user supplied input stream."},

    {ResourceKeys.communication__socket_exception_on_connect_01, // ICJE2
     "Communication error:" +
     " A socket exception occurred while trying to establish a socket connection to server {0}." +
     lineSeparator__ +
     "The message of the SocketException is \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_connect_01, // ICJE3
     "Communication error:" +
     " An IO exception occurred while trying to establish a socket connection to server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"." +
     lineSeparator__ +
     "This is probably due to an interserver misconfiguration." +
     lineSeparator__ +
     "Is InterServer running?"},

    {ResourceKeys.communication__io_exception_on_disconnect_01, // ICJE4
     "Communication error:" +
     " An IO exception occurred while trying to close a socket connection to server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"." +
     lineSeparator__ +
     "This is probably due to an interserver shutdown."},

    {ResourceKeys.communication__io_exception_on_recv_protocol_01, // ICJE5
     "Communication error:" +
     " An IO exception occurred while trying to establish a connection protocol to server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_recv_message_01, // ICJE6
     "Communication error:" +
     " An IO exception occurred while trying to receive data from server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_send_message_01, // ICJE7
     "Communication error:" +
     " An IO exception occurred while trying to send data to server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_read_0, // ICJE8
     "Communication error:" +
     " An IO exception occurred while trying to read data from server." +
     lineSeparator__ +
     "The message of the IOException is \"{0}\"."},

    {ResourceKeys.communication__io_exception_on_blob_read_01, // ICJE9
     "Communication error:" +
     " An IO exception occurred while trying to read a blob stream from server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_blob_close_01, // ICJEA
     "Communication error:" +
     " An IO exception occurred while trying to close a blob stream from server {0}." +
     lineSeparator__ +
     "The message of the IOException is \"{1}\"."},

    {ResourceKeys.communication__interserver, // ICJEB
     "Communication error:" +
     " InterServer was unable to complete an IO request."},

    // *******************************************
    // *** BlobIOException extends IOException ***
    // *******************************************
    {ResourceKeys.blobIO__sqlException_on_read_0,
     "JDBC IOException:" +
     " An SQL exception occurred while trying to read a blob stream from server as follows:" +
     lineSeparator__ +
     "{0}"},

    {ResourceKeys.blobIO__sqlException_on_close_0,
     "JDBC IOException:" +
     " An SQL exception occurred while trying to close a blob stream from server as follows:" +
     lineSeparator__ +
     "{0}"},

    {ResourceKeys.blobIO__sqlException_on_skip_0,
     "JDBC IOException:" +
     " An SQL exception occurred while trying to skip on a blob stream from server as follows" +
     lineSeparator__ +
     "{0}"},

    {ResourceKeys.blobIO__ioException_on_read_0,
     "JDBC IOException:" +
     " An IO exception occurred while trying to read on a blob stream from server as follows:" +
     lineSeparator__ +
     "{0}"},

    {ResourceKeys.blobIO__ioException_on_skip_0,
     "JDBC IOException:" +
     " An IO exception occurred while trying to skip on a blob stream from server as follows:" +
     lineSeparator__ +
     "{0}"},

    {ResourceKeys.blobIO__read_on_closed,
     "JDBC IOException:" +
     " Invalid operation to read on closed blob stream."},

    {ResourceKeys.blobIO__skip_on_closed,
     "JDBC IOException:" +
     " Invalid operation to skip on closed blob stream."},

    {ResourceKeys.blobIO__mark_not_supported,
     "JDBC IOException:" +
     " mark() operation is not supported on blob streams."},

    // *****************************
    // *** SocketTimeoutException ***
    // *****************************
    {ResourceKeys.socketTimeout__012, // ICJF0
     "Socket timeout:" +
     " IO was interrupted while waiting to read data from server {0}." +
     lineSeparator__ +
     "This is probably due to a timeout on the socket after {1} seconds." +
     lineSeparator__ +
     "The message of the InterruptException is \"{2}\"."},

    // ****************************
    // *** UnknownHostException ***
    // ****************************
    {ResourceKeys.unknownHost__0,  // ICJG0
     "An unknown host exception occurred while trying to open a socket connection to server {0}."},

    // ********************************
    // *** BadInstallationException ***
    // ********************************
    {ResourceKeys.badInstallation__unsupported_jdk_version, // ICJH0
     "Client or browser is using an unsupported jdk version."},

    {ResourceKeys.badInstallation__security_check_on_socket_01, // ICJH1
     "Your security manager does not allow socket connections to {0} on interserver port." +
     lineSeparator__ +
     "The message of the SecurityException is \"{1}\"."},

    {ResourceKeys.badInstallation__incompatible_remote_protocols, // ICJH2
     "Installed versions of InterClient and InterServer use incompatible client/server protocol versions."},

    // *********************************
    // *** DriverNotCapableException ***
    // *********************************
    {ResourceKeys.driverNotCapable__out_parameters, // 0A000
     "Unsupported feature:" +
     " This version of InterBase does not support OUT parameters distinct from a result set." +
     lineSeparator__ +
     "Use a result set." +
     lineSeparator__ +
     "See API reference for interbase.interclient.CallableStatement."},

    {ResourceKeys.driverNotCapable__schemas, // 0A000
     "Unsupported feature:" +
     " InterBase does not support schemas."},

    {ResourceKeys.driverNotCapable__catalogs, // 0A000
     "Unsupported feature:" +
     " InterBase does not support catalogs."},

    {ResourceKeys.driverNotCapable__isolation, // 0A000
     "Unsupported feature:" +
     " Specified transaction isolation level is not supported."},

    {ResourceKeys.driverNotCapable__binary_literals, // 0A000
     "Unsupported feature:" +
     " InterBase does not support binary literals."},

    {ResourceKeys.driverNotCapable__asynchronous_cancel, // 0A000
     "Unsupported feature:" +
     " This version of InterBase does not support asynchronous statement cancel."},

    {ResourceKeys.driverNotCapable__query_timeout, // 0A000
     "Unsupported feature:" +
     " This version of InterBase does not support query timeout."},

    {ResourceKeys.driverNotCapable__connection_timeout, // 0A000
     "Unsupported feature:" +
     " This version of InterBase does not support connection timeout."},

    {ResourceKeys.driverNotCapable__extension_not_yet_supported, // 0A000
     "Unsupported feature:" +
     " Using a proposed InterClient driver extension to JDBC which is not yet supported."},

    {ResourceKeys.driverNotCapable__jdbc2_not_yet_supported, // 0A000
     "Unsupported feature:" +
     " Using a JDBC 2 method which is not yet supported."},

    {ResourceKeys.driverNotCapable__escape__t, // 0A000
     "Unsupported feature:" +
     " SQL escape clauses for time '{'t ''hh:mm:ss'''}' are not supported."},

    {ResourceKeys.driverNotCapable__escape__ts_fractionals, // 0A000
     "Unsupported feature:" +
     " SQL escape clauses for timestamp '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' do not support fractional seconds."},

    {ResourceKeys.driverNotCapable__escape__call_with_result, // 0A000
     "Unsupported feature:" +
     " SQL escape clauses for procedure calls '{'? = call ...'}' with result parameters are not supported."},

    // MMM - added for array support
    // INSQLDA_NONAMES - this is not necessary for IB6 and higher
    {ResourceKeys.driverNotCapable__input_array_metadata,     // 0A000
     "Unsupported feature:" +
     " This version of InterBase does not support input array metadata." +
     lineSeparator__ +
     "Use the extension method PreparedStatement.prepareArray()." +
     lineSeparator__ +
     "See API reference for interbase.interclient.PreparedStatement."},
    // MMM - end

    // **************************************************************************
    // *** UnsupportedCharacterSetException extends DriverNotCapableException ***
    // **************************************************************************
    {ResourceKeys.unsupportedCharacterSet__0, // 0A000
     "Unsupported feature:" +
     " Driver does not support specified character encoding ({0})."},

    // ****************************
    // *** OutOfMemoryException ***
    // ****************************
    {ResourceKeys.outOfMemory, // ICJI0
     "Out of memory:" +
     " InterServer exhausted server memory."},

    //david jencks 1-21-2001
    // ****************************
    // *** database meta data call method parameter errors ***
    // ****************************
    {ResourceKeys.dbmd_getTables_invalid_table_type,
     "Invalid or unsupported table type."},

     {ResourceKeys.dbmd_getColumnPrivileges_table_name_required,
     "Table Name must be a string, not null."}

     //end david jencks 1-21-2001
  };

  /**
   * Extracts an array of key, resource pairs for this bundle.
   *
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
