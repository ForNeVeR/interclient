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

// <meta http-equiv="Content-Type" content="text/html; charset=Windows-1251">
// ��������� ������� InterClient ������� ��������� �� �������
// � ������ ����������� ������, ������������ ���������� �������� ��������
// <p>
// �� ������ ������� ���� ����������� �������������� ������.
// ����� ���, �� ������ �������� � ����� ������ ������� ����������� ����� � ������� ISO,
// ��������������� ����� �����������. ��� �������������� ��� ���������� ���������
// ������� ������ ���� �������� � ����� interbase.interclient
// ����� ����� �������� ������������ ��� �������� � ������
// interbase.interclient.ResourceKeys.
// <i>��</i> ��������� ����� ResourceKeys; ��� ���������� ������������
// ����������� ����������� InterClient.
// <p>
// <b>���������� ��� ������������� ��������:</b>
// ��� ������ �������������� ������� java.text.MessageFormat.format(),
// �� ����� ��� ����������� � �������� ������ ������� {, } � ' � ������� �������
// ������ ���� ��������� ���������, ��������:'{', ��� '}' ��� �������� { � } ,
// ��� '' ��� ������� ' .
// ������ ������ � ������������ �� Java �������� Sun � �������, �����������
// java.util.MessageFormat class.
// <p>
// <b>���������� �� ��������� ���������� �����������:</b>
// <ol>
// <li> �������� ���������� ������������� ������
//      <code>interbase.interclient.Resources</code> 
//      ������� <code>getContents()</code>. 
//      ��������� ����������, ������� ����� � �����, � ���� ��� ������������ ��� ��������.
// <li> �������� ����� Resources_*.java ��� ������ �������� �����������.
//      �������������� � ���, ��� ����� �������� �������������, �������� 
//      java.util.ListResourceBundle � ��������� � ������ interbase.interclient. ��������:
// <pre>
// // ������ ������������ ����� ��� InterClient.
// package interbase.interclient;
// 
// public class Resources_fr extends java.util.ListResourceBundle 
// {
//   // �������� ������������ ����� ��� ������� ����� �� ��������� ���������� Resources.
//   static private final Object[][] contents = 
//   {
//     {"1", "Mot de passe incorrect"}, // The password is incorrect
//     {"2", "Saisie du mot de passe"}, // Enter your password
//     ...
//     {"98", "Quitter"} // Exit
//   }
//
//   public Object[][] getContents()
//   {
//     return contents;
//   }
// }
// </pre>
// <li>��������� <code>jar xvf interclient.jar</code> ��� ��������� ������ ������� InterClient
// � �������� ��� ����� Resources_fr.class � ������� interbase/interclient.
// ��������� <code>jar cvf0 interclient.jar interbase borland</code> ��� ������ ������ �������
// � ����� ���� interclient.jar. �������� ������� interbase/interclient,
// interbase/interclient/utils � borland/jdbc ����� ������������ �� ���� �������.
// ��� �������������� ���������� �� jar �������� ������������ ������ Java Development Kit.
// </ol>
//
// @since <font color=red>Extension, since 1.50</font>
// @see java.util.Locale
// @see java.util.ResourceBundle

/**
 * Russian resource bundle for InterClient.
 *
 * @since <font color=red>Extension, since InterClient 1.50</font>
 * @see Resources
 **/
public class Resources_ru extends java.util.ListResourceBundle
{
  final static private String lineSeparator__ = "\n";
  // java.lang.System.getProperty ("line.separator");

  // Translations by Sergey Orlik
  // Russian language bundle for InterClient
  final static private Object[][] resources__ =
  {
    // *********************************
    // *** ��������� ��������� ����� ***
    // *********************************
    {ResourceKeys.seeApi,
     "\n�������� ���������� interbase.interclient ��. � ����������� �� API."},

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

    {ResourceKeys.companyName,
     "InterBase Software Corporation"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__user,
     "The user name for the connection"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__password,
     "The user''s password for the connection"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__charSet,
     "The character encoding for the connection"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__roleName,
     "The user''s SQL role name for the connection"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__sweepOnConnect,
     "Force garbage collection of outdated record versions upon connection"},

    // !!! Needs translation
    {ResourceKeys.propertyDescription__suggestedCachePages,
     "The suggested number of cache page buffers to use for the connection"},

// CJL-IB6 referenced by Driver.getPropertyInfo()
// !!! Needs Translation
    {ResourceKeys.propertyDescription__sqlDialect,
     "The SQL Dialect for the connection"},
// CJL-IB6 end change

    // ********************************
    // *** MissingResourceException ***
    // ********************************
    {ResourceKeys.missingResource__01,
     "�� ������� ����� ������ ��� ����� {0} � ������ {1}."},

    // *********************************
    // *** SQLException              ***
    // ***   ������ ������ �� ����   ***
    // *********************************
    {ResourceKeys.engine__default_0,
     "{0}"},

    // *********************************
    // *** InvalidOperationException ***
    // *********************************
    {ResourceKeys.invalidOperation__connection_closed,
     "������������ �������� �� �������� ����������."},

    // !!! since 1.50.37 needs translation
    {ResourceKeys.invalidOperation__server_connection_closed,
     "Invalid operation on a closed server connection."},

    {ResourceKeys.invalidOperation__result_set_closed,
     "������������ �������� �� �������� �������������� ������."},

    {ResourceKeys.invalidOperation__statement_closed,
     "������������ �������� �� �������� ���������."},

    {ResourceKeys.invalidOperation__transaction_in_progress,
     "������������ �������� �� ����� �������� ����������."},

    {ResourceKeys.invalidOperation__commit_or_rollback_under_autocommit,
     "������������ �������� �������� ��� ������ ���������� � ������ auto-commit."},

    {ResourceKeys.invalidOperation__execute_query_on_an_update_statement,
     "������������ �������� executeQuery �� ��������� ���������� ������."},

    {ResourceKeys.invalidOperation__set_null_on_non_nullable_parameter, 
     "������������ �������� ���������� null ������������� �������� ���������."},

    {ResourceKeys.invalidOperation__read_at_end_of_cursor,
     "������� ������ �� ������ �������� �������."},

    {ResourceKeys.invalidOperation__read_at_invalid_cursor_position,
     "������������ �������� ������ �� ������� ������� �������."},

    {ResourceKeys.invalidOperation__was_null_with_no_data_retrieved,
     "������������ �������� �������� wasNull(): �� �������� ������ �� ������� ������ �������."},

    {ResourceKeys.invalidOperation__parameter_not_set,
     "������������ �������� ���������� ��������������� ��������� � �� ������������� ��������� ������������� ���������." +
     "\n��� ������� ��������� ����������� �������� ������ ���� ���������," +
     " ������������� ������� �������������� � null."},

    // *****************************************************************
    // *** DataConversionException extends InvalidOperationException ***
    // *****************************************************************

    // ********************************************************************
    // *** ParameterConversionException extends DataConversionException ***
    // ********************************************************************
    {ResourceKeys.parameterConversion__type_conversion,
     "������ �������������� ������:" +
     " �������� ��� ��������� ��� ������������ ��������������."},

    {ResourceKeys.parameterConversion__type_conversion__set_number_on_binary_blob,
     "������ �������������� ������:" +
     " ������� ��������� �������� �������� �� ����������� ��������� ���� ���� blob."},

    {ResourceKeys.parameterConversion__type_conversion__set_date_on_binary_blob,
     "������ �������������� ������:" +
     " ������� ���������� �������� ����, ������� ��� ����+������� �� ����������� ��������� ���� ���� blob."},

    // !!! needs translation
    {ResourceKeys.parameterConversion__set_object_on_stream,
     "Invalid data conversion:" +
     " It is an invalid operation to send a Java input stream" +
     " using the setObject method." +
     lineSeparator__ +
     " You must explicitly use PreparedStatement.setXXXStream" +
     " to transfer a value as a stream per the JDBC specification." +
     lineSeparator__ +
     " Refer to the JDBC 1.20 specification pg. 43 for details."},

    {ResourceKeys.parameterConversion__instance_conversion_0,
     "������ �������������� ������:" +
     " �������� ��������� {0} ��� ������ ������������ ��������������."},

    // !!! needs translation
     // MMM - added for the array support
    {ResourceKeys.parameterConversion__array_element_type_conversion,
     "Invalid data conversion:" +
     " Array parameter element has wrong type for requested conversion."},

    {ResourceKeys.parameterConversion__array_element_instance_conversion_0,
     "Invalid data conversion:" +
     " Array parameter element value {0} is out of range for requested conversion."},

    {ResourceKeys.parameterConversion__array_element_instance_truncation_0,
     "Invalid data conversion:" +
     " Array parameter element value {0} cannot be stored without truncation."},
     // MMM - end

    // *****************************************************************
    // *** ColumnConversionException extends DataConversionException ***
    // *****************************************************************
    {ResourceKeys.columnConversion__type_conversion,
     "������ �������������� ������:" +
     " �������� ��� ������� ���������� ��� ������������ ��������������."},

    {ResourceKeys.columnConversion__type_conversion__get_number_on_binary_blob,
     "������ �������������� ������:" +
     " ������� �������� ����� �� �� ����������� ��������� ���� ���� blob."},

    {ResourceKeys.columnConversion__type_conversion__get_date_on_binary_blob,
     "������ �������������� ������:" +
     " ������� �������� �������� ����, ������� ��� ����+������� �� �� ����������� ��������� ���� ���� blob."},

    {ResourceKeys.columnConversion__instance_conversion_0,
     "������ �������������� ������:" +
     " ��������� ������� {0} ��� ������ ������������ ��������������."},

    // ******************************************************************
    // *** InvalidArgumentException extends InvalidOperationException ***
    // ******************************************************************
    {ResourceKeys.invalidArgument__isolation_0,
     "�������� ��������:" +
     " ����������� ������� �������� ���������� {0} ."},

    {ResourceKeys.invalidArgument__connection_property__isolation,
     "�������� ��������:" +
     " ����������� ������� �������� ���������� � ���������� ����������."},

    {ResourceKeys.invalidArgument__connection_property__lock_resolution_mode,
     "�������� ��������:" +
     " �������� �������� ���������� ���������� � ���������� ����������."},

    {ResourceKeys.invalidArgument__connection_property__unrecognized,
     "�������� ��������:" +
     " �������� ����������� �������� ����������."},

    {ResourceKeys.invalidArgument__connection_properties__no_user_or_password,
     "�������� ��������:" +
     " �� ��� ������������ (user) � ������ (password) ��� ����������."},

    {ResourceKeys.invalidArgument__connection_properties__null,
     "�������� ��������:" +
     " ������ �������� ������� ����������."},

// CJL-IB6 added for SQL dialect support !!! SQLCODE TBD!!!
// !!! needs translation !!!
    {ResourceKeys.invalidArgument__connection_properties__sqlDialect_0, // !!!
    "Invalid argument:" +
    " SQL Dialect \"{0}\" specified in connection properties is not valid " },
// CJL-IB6 end change

    {ResourceKeys.invalidArgument__sql_empty_or_null,
     "�������� ��������:" +
     " ������ SQL �� �������� (null) ��� �����."},

    {ResourceKeys.invalidArgument__column_name_0,
     "�������� ��������:" +
     " ����������� ��� ��������������� ������� {0} ."},

    {ResourceKeys.invalidArgument__negative_row_fetch_size,
     "�������� ��������:" +
     " �������� ������������� �������� ������� ��� fetch."},

    {ResourceKeys.invalidArgument__negative_max_rows,
     "�������� ��������:" +
     " �������� ������������� �������� ������������� ����� ����� ����������."},

    {ResourceKeys.invalidArgument__fetch_size_exceeds_max_rows,
     "�������� ��������:" +
     " ������ fetch �� ����� ��������� ������������� ����� ����� ����������."},

    {ResourceKeys.invalidArgument__setUnicodeStream_odd_bytes,
     "�������� ��������:" +
     " ������� ���������� ����� ����� unicode � �������� ������ ����."},

    // !!! needs translation
    // MMM - added for array support
    {ResourceKeys.invalidArgument__not_array_column,
     "Invalid argument:" +
     " Result column specified is not of array data type."},

    {ResourceKeys.invalidArgument__not_array_parameter,
     "Invalid argument:" +
     " Input column specified is not of array data type."},

    {ResourceKeys.invalidArgument__invalid_array_slice,
     "Invalid argument:" +
     " Specified array slice is out of bounds."},

    {ResourceKeys.invalidArgument__invalid_array_dimensions,
     "Invalid argument:" +
     " Array dimensions do not match array metadata."},
    // MMM - end

    // !!! needs translation
    {ResourceKeys.invalidArgument__lock_resolution, // ICJ3H
     "Invalid argument:" +
     " Attempt to set an invalid lock resolution mode for the transaction."},

    // !!! needs translation
    {ResourceKeys.invalidArgument__version_acknowledgement_mode, // ICJ3I
     "Invalid argument:" +
     " Attempt to set an invalid version acknowledgement mode for the transaction."},

    // !!! needs translation
    {ResourceKeys.invalidArgument__table_lock, // ICJ3J
     "Invalid argument:" +
     " Attempt to set an invalid table lock for the transaction."},

    // ************************************************************************
    // *** ColumnIndexOutOfBoundsException extends InvalidArgumentException ***
    // ************************************************************************
    {ResourceKeys.columnIndexOutOfBounds__0,
     "�������� ��������:" +
     " ������ �������������� ������� {0} ��� ������."},

    // ***************************************************************************
    // *** ParameterIndexOutOfBoundsException extends InvalidArgumentException ***
    // ***************************************************************************
    {ResourceKeys.parameterIndexOutOfBounds__0,
     "�������� ��������:" +
     " ������ �������� ��������� {0} ��� ������."},

    // ***********************************************************
    // *** URLSyntaxException extends InvalidArgumentException ***
    // ***********************************************************
    {ResourceKeys.urlSyntax__bad_server_prefix_0,
     "������ ������� URL ���� ������ InterBase ��� JDBC: {0}" +
     "\n��� ����� ������ ���������� � ''//<server>''" +
     "\n(�������� jdbc:interbase://hal//databases/employee.gdb," +
     " ��� jdbc:interbase://hal/C:/databases/employee.gdb)."},

    {ResourceKeys.urlSyntax__bad_server_suffix_0,
     "������ ������� URL ���� ������ InterBase ��� JDBC: {0}" +
     "\n��� ������� ������ ������������ � ����� ''/<������ ���� � �����>''" +
     "\n(�������� jdbc:interbase://hal//databases/employee.gdb," +
     " ��� jdbc:interbase://hal/C:/databases/employee.gdb)."},

    // **************************************************************
    // *** EscapeSyntaxException extends InvalidArgumentException ***
    // **************************************************************
    {ResourceKeys.escapeSyntax__no_closing_escape_delimeter_0,
     "������ ���������� SQL escape: �������� ����������� ����������� '}'." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__unrecognized_keyword_0,
     "������ ���������� SQL escape: ����������� �������� �����." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__d_0,
     "������ ���������� SQL escape ��� ��������� ���� '{'d ''yyyy-mm-dd'''}' ." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__ts_0,
     "������ ���������� SQL escape ��� ��������� ����+����� (timestamp) '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' ." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__escape_0,
     "������ ���������� SQL escape ��� LIKE '{'escape ''escape-character'''}' ." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__escape__no_quote_0,
     "������ ���������� SQL escape ��� LIKE '{'escape ''escape-character'''}' clause." +
     "\n�������� ������ '' ." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__fn_0,
     "������ ���������� SQL escape ��� ��������� ������� '{'fn ...'}' ." +
     "\n�������������� ��������� ���������: {0}."},

    {ResourceKeys.escapeSyntax__call_0,
     "������ ���������� SQL escape ��� �������� ��������� '{'call ...'}' ." +
     "\n�������������� ��������� ���������: {0}."},

// CJL-IB6 added for escape t support
    {ResourceKeys.escapeSyntax__t_0, // ICJ78
     "Malformed SQL escape syntax for time '{'t ''hh:mm:ss.f...'''}' clause." +
     lineSeparator__ +
     "The malformed syntax used was {0}."},
// CJL-IB6 end change

// CJL-IB6 --- needs translation !!!
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
    {ResourceKeys.bugCheck__0,
     "������� ���." +
     "\n����������, ��������� e-mail �� ������ interclient@interbase.com" +
     "\n�� ������� �� ��� ������ {0}."},

    // *************************************************
    // *** CharacterEncodingException                ***
    // ***   ��� ������� ��������� ��-�� ������ ������������,             ***
    // ***  ������ ����� ���� ������� ������ � ic/is/ib ***
    // *************************************************
    {ResourceKeys.characterEncoding__read_0,
     "������ � ��������� �������:" +
     " �������� �������������� �������� ��� ������� ������������ ������ � ��������� �������." +
     "\n��������� ������ CharConversionException: \"{0}\"."},

    {ResourceKeys.characterEncoding__write_0,
     "������ � ��������� �������:" +
     " �������� �������������� �������� ��� ������� ������������ ������ ��� ������� �������." +
     "\n��������� ������ CharConversionException: \"{0}\"."},

    // *************************************************************************
    // *** RemoteProtocolException                                           ***
    // ***   ��������� ��� ��������� ����� ��� � ���������� ��������� � ���� ***
    // *************************************************************************
    {ResourceKeys.remoteProtocol__unexpected_token_from_server_0,
     "������ ��������� ������/������:" +
     " InterServer ������� �������� ����� ��������� �� InterServer." +
     "\nThe internal code is {0}."},

    {ResourceKeys.remoteProtocol__unexpected_token_from_client,
     "������ ��������� ������/������:" +
     " InterServer ������� �������� ����� ��������� �� InterClient."},

    {ResourceKeys.remoteProtocol__unable_to_establish_protocol,
     "������ ��������� ������/������:" +
     " �� ������� ���������� ���������� � �������� ��� ������ �����������."},

    {ResourceKeys.remoteProtocol__bad_message_certficate_from_server,
     "������ ��������� ������/������:" +
     " �������� ���������������� ��������� �� InterServer."},

    // ******************************
    // *** CommunicationException ***
    // ***   ���� � ����          ***
    // ******************************
    {ResourceKeys.communication__user_stream__io_exception_on_read_0,
     "������ �����:" +
     " ���������� �����-������ ��� ������ �������� ������, ���������������� �������������." +
     "\n��������� IOException: \"{0}\"."},

    {ResourceKeys.communication__user_stream__unexpected_eof,
     "������ �����:" +
     " ����������� ����� ������ ��� ������ �������� ������, ���������������� �������������."},

    {ResourceKeys.communication__socket_exception_on_connect_01,
     "������ �����:" +
     " ������ ������ ��� ������� ��������� ���������� ����� ����� � �������� {0}." +
     "\n��������� SocketException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_connect_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ��������� ���������� ����� ����� � �������� {0}." +
     "\n��������� IOException: \"{1}\"." +
     "\n��������, interserver ��� ����������� ���������������." +
     "\n� ������, ������� �� InterServer???"},

    {ResourceKeys.communication__io_exception_on_disconnect_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ������� ���������� ����� ����� � �������� {0}." +
     "\n��������� IOException: \"{1}\"." +
     "\n��������, interserver ��� ����������."},

    {ResourceKeys.communication__io_exception_on_recv_protocol_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ��������� ��������� ������ � �������� {0}." +
     "\n��������� IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_recv_message_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� �������� ������ � ������� {0}." +
     "\n��������� IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_send_message_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� �������� ������ �� ������ {0}." +
     "\n��������� IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_read_0,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ��������� ������ � ������� {0}." +
     "\n��������� IOException: \"{0}\"."},

    {ResourceKeys.communication__io_exception_on_blob_read_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ��������� ������ blob � ������� {0}." +
     "\n��������� IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_blob_close_01,
     "������ �����:" +
     " ���������� �����-������ ��� ������� ������� ����� ������ blob �� ������� {0}." +
     "\n��������� IOException: \"{1}\"."},

    {ResourceKeys.communication__interserver,
     "������ �����:" +
     " InterServer �� ����� ��������� ������ �����-������."},

    // *******************************************
    // *** BlobIOException extends IOException ***
    // *******************************************
    {ResourceKeys.blobIO__sqlException_on_read_0,
     "JDBC IOException:" +
     " ���������� SQL ��� ������� ������ ������ blob � ������:" +
     "\n{0}"},

    {ResourceKeys.blobIO__sqlException_on_close_0,
     "JDBC IOException:" +
     " ���������� SQL ��� ������� �������� ������ blob � �������:" +
     "\n{0}"},

    {ResourceKeys.blobIO__sqlException_on_skip_0,
     "JDBC IOException:" +
     " ���������� SQL ��� ������� �������� ������ � ������ blob � �������:" +
     "\n{0}"},

    {ResourceKeys.blobIO__ioException_on_read_0,
     "JDBC IOException:" +
     " ���������� �����-������ ��� ������� ������ ������ blob � �������:" +
     "\n{0}"},

    {ResourceKeys.blobIO__ioException_on_skip_0,
     "JDBC IOException:" +
     " ���������� �����-������ ��� ������� �������� ������ � ������ blob � �������:" +
     "\n{0}"},

    {ResourceKeys.blobIO__read_on_closed,
     "JDBC IOException:" +
     " ������������ �������� ������ ��������� ������ blob."},

    {ResourceKeys.blobIO__skip_on_closed,
     "JDBC IOException:" +
     " ������������ �������� �������� � �������� ������ blob."},

    {ResourceKeys.blobIO__mark_not_supported,
     "JDBC IOException:" +
     " �������� mark() �� �������������� �� ������� blob."},

    // **********************************
    // *** ConnectionTimeoutException ***
    // **********************************
    {ResourceKeys.socketTimeout__012,
     "����-��� ������:" +
     " ����-����� ��� ������� �� ����� �������� ������ ������ � ������� {0}." +
     "\n��������, �� ������� ����-���� ������ �� ���������� {1} ������(�)." +
     "\n��������� InterruptException: \"{2}\"."},

    // ****************************
    // *** UnknownHostException ***
    // ****************************
    {ResourceKeys.unknownHost__0, 
     "����������� ���������� ����� ��� ������� ������� ����� �� ������� {0}."},

    // ********************************
    // *** BadInstallationException ***
    // ********************************
    {ResourceKeys.badInstallation__unsupported_jdk_version,
     "������ ��� ������ ���������� ���������������� ������ jdk."},

    {ResourceKeys.badInstallation__security_check_on_socket_01,
     "���� ������� ������������ �� ��������� ���������� � {0} �� ����� 3060." +
     "\n��������� SecurityException: \"{1}\"."},

    {ResourceKeys.badInstallation__incompatible_remote_protocols,
     "������������� ������ InterClient � InterServer ���������� ������������� ������ ���������� ������-������."},

    // *********************************
    // *** DriverNotCapableException ***
    // *********************************
    {ResourceKeys.driverNotCapable__out_parameters,
     "���������������� �����������:" +
     " ������ ������ InterBase �� ������������ �������� ��������� OUT �������� �� ��������� ������." +
     "\n����������� �������� �����." +
     "\n������ ���������� �� API, ������ �� interbase.interclient.CallableStatement."},

    {ResourceKeys.driverNotCapable__schemas,
     "���������������� �����������:" +
     " InterBase �� ������������ ����� (schemas)."},

    {ResourceKeys.driverNotCapable__catalogs,
     "���������������� �����������:" +
     " InterBase �� ������������ �������� (catalogs)."},

    {ResourceKeys.driverNotCapable__isolation,
     "���������������� �����������:" +
     " ��������� ������� �������� ���������� �� ��������������."},

    {ResourceKeys.driverNotCapable__binary_literals,
     "���������������� �����������:" +
     " InterBase �� ������������ �������� ��������."},

    {ResourceKeys.driverNotCapable__asynchronous_cancel,
     "���������������� �����������:" +
     " ������ ������ InterBase �� ������������ ����������� ���������� ���������."},

    {ResourceKeys.driverNotCapable__query_timeout,
     "���������������� �����������:" +
     " ������ ������ InterBase �� ������������ ����-���� ��������."},

    {ResourceKeys.driverNotCapable__connection_timeout,
     "���������������� �����������:" +
     " ������ ������ InterBase �� ������������ ����-���� ����������."},

    // !!! needs translation
    {ResourceKeys.driverNotCapable__extension_not_yet_supported,
     "Unsupported feature:" +
     " using a proposed InterClient driver extension to JDBC which is not yet supported."},

    // !!! needs translation
    {ResourceKeys.driverNotCapable__jdbc2_not_yet_supported,
     "Unsupported feature:" +
     " using a JDBC 2 method which is not yet supported."},

    {ResourceKeys.driverNotCapable__escape__t,
     "���������������� �����������:" +
     " ��������� SQL escape ��� ������� '{'t ''hh:mm:ss'''}' �� ��������������."},

    {ResourceKeys.driverNotCapable__escape__ts_fractionals, 
     "���������������� �����������:" +
     " ��������� SQL escape ��� ����+������� (timestamp) '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' �� ������������ ����� �������."},

    {ResourceKeys.driverNotCapable__escape__call_with_result,
     "���������������� �����������:" +
     " ��������� SQL escape ��� ����������� ������� '{'? = call ...'}' � ��������������� ����������� �� ��������������."},

    // !!! needs translation
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
    {ResourceKeys.unsupportedCharacterSet__0,
     "���������������� �����������:" +
     " ������� �� ������������ ��������� ��������� �������� ({0})."},

    // ****************************
    // *** OutOfMemoryException ***
    // ****************************
    {ResourceKeys.outOfMemory,
     "������������ ������:" +
     " InterServer �� ������� ��������� ������."}

  };

  // �������� ������� ������ � ��������� ��� ��� ������� ������.
  /**
   * Extracts an array of key, resource pairs for this bundle.
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
