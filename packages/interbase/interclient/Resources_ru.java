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
// Строковые ресурсы InterClient включая сообщения об ошибках
// и жестко привязанные тексты, возвращаемые некоторыми методами драйвера
// <p>
// Вы можете создать свой собственный локализованный ресурс.
// Делая это, вы должны добавить к имени класса суффикс определения языка в формате ISO,
// соответствующий языку локализации. Все дополнительные или замещающие локальные
// ресурсы должны быть помещены в пакет interbase.interclient
// Общие ключи ресурсов представлены для удобства в классе
// interbase.interclient.ResourceKeys.
// <i>Не</i> заменяйте класс ResourceKeys; его переменные используются
// внутренними механизмами InterClient.
// <p>
// <b>Примечание для разработчиков ресурсов:</b>
// Все строки обрабатываются методом java.text.MessageFormat.format(),
// по этому все встречаемые в исходном тексте символы {, } и ' в строках ресурса
// должны быть разделены кавычками, например:'{', или '}' для символов { и } ,
// или '' для символа ' .
// Детали смотри в документации по Java компании Sun в разделе, посвященном
// java.util.MessageFormat class.
// <p>
// <b>Инструкции по настройке параметров локализации:</b>
// <ol>
// <li> Выделите содержимое существующего класса
//      <code>interbase.interclient.Resources</code> 
//      вызовом <code>getContents()</code>. 
//      сохраните содержание, включая ключи и текст, в файл или распечатайте для перевода.
// <li> Создайте класс Resources_*.java для вашего варианта локализации.
//      Удостоверьтесь в том, что класс является общедоступным, расширяя 
//      java.util.ListResourceBundle и находится в пакете interbase.interclient. Например:
// <pre>
// // Ресурс французского языка для InterClient.
// package interbase.interclient;
// 
// public class Resources_fr extends java.util.ListResourceBundle 
// {
//   // Содержит переведенный текст для каждого ключа из исходного содержания Resources.
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
// <li>Исполните <code>jar xvf interclient.jar</code> для выделения файлов классов InterClient
// и включите ваш новый Resources_fr.class в каталог interbase/interclient.
// Исполните <code>jar cvf0 interclient.jar interbase borland</code> для сборки файлов классов
// в новый файл interclient.jar. Каталоги классов interbase/interclient,
// interbase/interclient/utils и borland/jdbc будут архивированы по этой команде.
// Для дополнительной информации по jar смотрите документацию вашего Java Development Kit.
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
    // *** Различные текстовые ключи ***
    // *********************************
    {ResourceKeys.seeApi,
     "\nОписание исключения interbase.interclient см. в справочнике по API."},

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
     "Не удается найти ресурс для ключа {0} в бандле {1}."},

    // *********************************
    // *** SQLException              ***
    // ***   Всегда только от ядра   ***
    // *********************************
    {ResourceKeys.engine__default_0,
     "{0}"},

    // *********************************
    // *** InvalidOperationException ***
    // *********************************
    {ResourceKeys.invalidOperation__connection_closed,
     "Недопустимая операция на закрытом соединении."},

    // !!! since 1.50.37 needs translation
    {ResourceKeys.invalidOperation__server_connection_closed,
     "Invalid operation on a closed server connection."},

    {ResourceKeys.invalidOperation__result_set_closed,
     "Недопустимая операция на закрытом результирующем наборе."},

    {ResourceKeys.invalidOperation__statement_closed,
     "Недопустимая операция на закрытом операторе."},

    {ResourceKeys.invalidOperation__transaction_in_progress,
     "Недопустимая операция во время активной транзакции."},

    {ResourceKeys.invalidOperation__commit_or_rollback_under_autocommit,
     "Недопустимая операция фиксации или отката транзакции в режиме auto-commit."},

    {ResourceKeys.invalidOperation__execute_query_on_an_update_statement,
     "Недопустимая операция executeQuery на операторе обновления данных."},

    {ResourceKeys.invalidOperation__set_null_on_non_nullable_parameter, 
     "Недопустимая операция присвоения null необнуляемому входному параметру."},

    {ResourceKeys.invalidOperation__read_at_end_of_cursor,
     "Попытка чтения за нижней границей курсора."},

    {ResourceKeys.invalidOperation__read_at_invalid_cursor_position,
     "Недопустимая операция чтения на текущей позиции курсора."},

    {ResourceKeys.invalidOperation__was_null_with_no_data_retrieved,
     "Недопустимая операция проверки wasNull(): не получено данных из текущей строки курсора."},

    {ResourceKeys.invalidOperation__parameter_not_set,
     "Недопустимая операция исполнения подготовленного оператора с не установленным значением необнуляемого параметра." +
     "\nВсе входные параметры необнуяемых столбцов должны быть заполнены," +
     " незаполненные колонки приравниваются к null."},

    // *****************************************************************
    // *** DataConversionException extends InvalidOperationException ***
    // *****************************************************************

    // ********************************************************************
    // *** ParameterConversionException extends DataConversionException ***
    // ********************************************************************
    {ResourceKeys.parameterConversion__type_conversion,
     "Ошибка преобразования данных:" +
     " неверный тип параметра для запрошенного преобразования."},

    {ResourceKeys.parameterConversion__type_conversion__set_number_on_binary_blob,
     "Ошибка преобразования данных:" +
     " попытка присвоить числовое значение не символьному бинарному полю типа blob."},

    {ResourceKeys.parameterConversion__type_conversion__set_date_on_binary_blob,
     "Ошибка преобразования данных:" +
     " попытка присвоения значения даты, времени или даты+времени не символьному бинарному полю типа blob."},

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
     "Ошибка преобразования данных:" +
     " значение параметра {0} вне границ запрошенного преобразования."},

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
     "Ошибка преобразования данных:" +
     " неверный тип колонки результата для запрошенного преобразования."},

    {ResourceKeys.columnConversion__type_conversion__get_number_on_binary_blob,
     "Ошибка преобразования данных:" +
     " попытка получить число из не символьного бинарного поля типа blob."},

    {ResourceKeys.columnConversion__type_conversion__get_date_on_binary_blob,
     "Ошибка преобразования данных:" +
     " попытка получить значение даты, времени или даты+времени из не символьного бинарного поля типа blob."},

    {ResourceKeys.columnConversion__instance_conversion_0,
     "Ошибка преобразования данных:" +
     " результат столбца {0} вне границ запрошенного преобразования."},

    // ******************************************************************
    // *** InvalidArgumentException extends InvalidOperationException ***
    // ******************************************************************
    {ResourceKeys.invalidArgument__isolation_0,
     "Неверный аргумент:" +
     " неизвестный уровень изоляции транзакции {0} ."},

    {ResourceKeys.invalidArgument__connection_property__isolation,
     "Неверный аргумент:" +
     " неизвестный уровень изоляции транзакции в параметрах соединения."},

    {ResourceKeys.invalidArgument__connection_property__lock_resolution_mode,
     "Неверный аргумент:" +
     " неверное значение разрешения блокировки в параметрах соединения."},

    {ResourceKeys.invalidArgument__connection_property__unrecognized,
     "Неверный аргумент:" +
     " передано неизвестное свойство соединения."},

    {ResourceKeys.invalidArgument__connection_properties__no_user_or_password,
     "Неверный аргумент:" +
     " не имя пользователя (user) и пароль (password) для соединения."},

    {ResourceKeys.invalidArgument__connection_properties__null,
     "Неверный аргумент:" +
     " пустые значения свойств соединения."},

// CJL-IB6 added for SQL dialect support !!! SQLCODE TBD!!!
// !!! needs translation !!!
    {ResourceKeys.invalidArgument__connection_properties__sqlDialect_0, // !!!
    "Invalid argument:" +
    " SQL Dialect \"{0}\" specified in connection properties is not valid " },
// CJL-IB6 end change

    {ResourceKeys.invalidArgument__sql_empty_or_null,
     "Неверный аргумент:" +
     " строка SQL не выделена (null) или пуста."},

    {ResourceKeys.invalidArgument__column_name_0,
     "Неверный аргумент:" +
     " неизвестное имя результирующего столбца {0} ."},

    {ResourceKeys.invalidArgument__negative_row_fetch_size,
     "Неверный аргумент:" +
     " передано отрицательное значение размера для fetch."},

    {ResourceKeys.invalidArgument__negative_max_rows,
     "Неверный аргумент:" +
     " передано отрицательное значение максимального числа строк результата."},

    {ResourceKeys.invalidArgument__fetch_size_exceeds_max_rows,
     "Неверный аргумент:" +
     " размер fetch не может превышать максимального числа строк результата."},

    {ResourceKeys.invalidArgument__setUnicodeStream_odd_bytes,
     "Неверный аргумент:" +
     " попытка определить поток ввода unicode с нечетным числом байт."},

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
     "Неверный аргумент:" +
     " индекс результирующей колонки {0} вне границ."},

    // ***************************************************************************
    // *** ParameterIndexOutOfBoundsException extends InvalidArgumentException ***
    // ***************************************************************************
    {ResourceKeys.parameterIndexOutOfBounds__0,
     "Неверный аргумент:" +
     " индекс входного параметра {0} вне границ."},

    // ***********************************************************
    // *** URLSyntaxException extends InvalidArgumentException ***
    // ***********************************************************
    {ResourceKeys.urlSyntax__bad_server_prefix_0,
     "Ошибка формата URL базы данных InterBase для JDBC: {0}" +
     "\nИмя файла должно начинаться с ''//<server>''" +
     "\n(например jdbc:interbase://hal//databases/employee.gdb," +
     " или jdbc:interbase://hal/C:/databases/employee.gdb)."},

    {ResourceKeys.urlSyntax__bad_server_suffix_0,
     "Ошибка формата URL базы данных InterBase для JDBC: {0}" +
     "\nИмя сервера должно продолжаться в форме ''/<полный путь к файлу>''" +
     "\n(например jdbc:interbase://hal//databases/employee.gdb," +
     " или jdbc:interbase://hal/C:/databases/employee.gdb)."},

    // **************************************************************
    // *** EscapeSyntaxException extends InvalidArgumentException ***
    // **************************************************************
    {ResourceKeys.escapeSyntax__no_closing_escape_delimeter_0,
     "Ошибка синтаксиса SQL escape: пропущен закрывающий разделитель '}'." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__unrecognized_keyword_0,
     "Ошибка синтаксиса SQL escape: неизвестное ключевое слово." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__d_0,
     "Ошибка синтаксиса SQL escape для выражения даты '{'d ''yyyy-mm-dd'''}' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__ts_0,
     "Ошибка синтаксиса SQL escape для выражения дата+время (timestamp) '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__escape_0,
     "Ошибка синтаксиса SQL escape для LIKE '{'escape ''escape-character'''}' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__escape__no_quote_0,
     "Ошибка синтаксиса SQL escape для LIKE '{'escape ''escape-character'''}' clause." +
     "\nПропущен символ '' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__fn_0,
     "Ошибка синтаксиса SQL escape для скалярной функции '{'fn ...'}' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

    {ResourceKeys.escapeSyntax__call_0,
     "Ошибка синтаксиса SQL escape для хранимой процедуры '{'call ...'}' ." +
     "\nИспользованный ошибочный синтаксис: {0}."},

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
     "Замечен баг." +
     "\nПожалуйста, отправьте e-mail по адресу interclient@interbase.com" +
     "\nсо ссылкой на код ошибки {0}."},

    // *************************************************
    // *** CharacterEncodingException                ***
    // ***   Как правило возникает из-за ошибки пользователя,             ***
    // ***  однако может быть вызвано багами в ic/is/ib ***
    // *************************************************
    {ResourceKeys.characterEncoding__read_0,
     "Ошибка в кодировке символа:" +
     " возникла исключительная ситуация при попытке декодировать строку в кодировке сервера." +
     "\nСообщение метода CharConversionException: \"{0}\"."},

    {ResourceKeys.characterEncoding__write_0,
     "Ошибка в кодировке символа:" +
     " возникла исключительная ситуация при попытке закодировать строку для посылки серверу." +
     "\nСообщение метода CharConversionException: \"{0}\"."},

    // *************************************************************************
    // *** RemoteProtocolException                                           ***
    // ***   Случается при появлении багов или в результате нарушений в сети ***
    // *************************************************************************
    {ResourceKeys.remoteProtocol__unexpected_token_from_server_0,
     "Ошибка протокола клиент/сервер:" +
     " InterServer получил неверный токен сообщения от InterServer." +
     "\nThe internal code is {0}."},

    {ResourceKeys.remoteProtocol__unexpected_token_from_client,
     "Ошибка протокола клиент/сервер:" +
     " InterServer получил неверный токен сообщения от InterClient."},

    {ResourceKeys.remoteProtocol__unable_to_establish_protocol,
     "Ошибка протокола клиент/сервер:" +
     " не удается установить соединение с сервером для обмена сообщениями."},

    {ResourceKeys.remoteProtocol__bad_message_certficate_from_server,
     "Ошибка протокола клиент/сервер:" +
     " получено нераспознаваемое сообщение от InterServer."},

    // ******************************
    // *** CommunicationException ***
    // ***   Сбои в сети          ***
    // ******************************
    {ResourceKeys.communication__user_stream__io_exception_on_read_0,
     "Ошибка связи:" +
     " исключение ввода-вывода при чтении входного потока, предоставленного пользователем." +
     "\nСообщение IOException: \"{0}\"."},

    {ResourceKeys.communication__user_stream__unexpected_eof,
     "Ошибка связи:" +
     " неожиданный конец потока при чтении входного потока, предоставленного пользователем."},

    {ResourceKeys.communication__socket_exception_on_connect_01,
     "Ошибка связи:" +
     " ошибка сокета при попытке установки соединения через сокет с сервером {0}." +
     "\nСообщение SocketException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_connect_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке установки соединения через сокет с сервером {0}." +
     "\nСообщение IOException: \"{1}\"." +
     "\nВозможно, interserver был неправильно сконфигурирован." +
     "\nИ вообще, запущен ли InterServer???"},

    {ResourceKeys.communication__io_exception_on_disconnect_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке закрыть соединение через сокет с сервером {0}." +
     "\nСообщение IOException: \"{1}\"." +
     "\nВозможно, interserver был остановлен."},

    {ResourceKeys.communication__io_exception_on_recv_protocol_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке установки протокола обмена с сервером {0}." +
     "\nСообщение IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_recv_message_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке получить данные с сервера {0}." +
     "\nСообщение IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_send_message_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке передать данные на сервер {0}." +
     "\nСообщение IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_read_0,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке прочитать данные с сервера {0}." +
     "\nСообщение IOException: \"{0}\"."},

    {ResourceKeys.communication__io_exception_on_blob_read_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке прочитать данные blob с сервера {0}." +
     "\nСообщение IOException: \"{1}\"."},

    {ResourceKeys.communication__io_exception_on_blob_close_01,
     "Ошибка связи:" +
     " исключение ввода-вывода при попытке закрыть поток данных blob на сервере {0}." +
     "\nСообщение IOException: \"{1}\"."},

    {ResourceKeys.communication__interserver,
     "Ошибка связи:" +
     " InterServer не может выполнить запрос ввода-вывода."},

    // *******************************************
    // *** BlobIOException extends IOException ***
    // *******************************************
    {ResourceKeys.blobIO__sqlException_on_read_0,
     "JDBC IOException:" +
     " исключение SQL при попытке чтения потока blob с сервер:" +
     "\n{0}"},

    {ResourceKeys.blobIO__sqlException_on_close_0,
     "JDBC IOException:" +
     " исключение SQL при попытке закрытия потока blob с сервера:" +
     "\n{0}"},

    {ResourceKeys.blobIO__sqlException_on_skip_0,
     "JDBC IOException:" +
     " исключение SQL при попытке пропуска данных в потоке blob с сервера:" +
     "\n{0}"},

    {ResourceKeys.blobIO__ioException_on_read_0,
     "JDBC IOException:" +
     " исключение ввода-вывода при попытке чтения потока blob с сервера:" +
     "\n{0}"},

    {ResourceKeys.blobIO__ioException_on_skip_0,
     "JDBC IOException:" +
     " исключение ввода-вывода при попытке пропуска данных в потоке blob с сервера:" +
     "\n{0}"},

    {ResourceKeys.blobIO__read_on_closed,
     "JDBC IOException:" +
     " недопустимая операция чтения закрытого потока blob."},

    {ResourceKeys.blobIO__skip_on_closed,
     "JDBC IOException:" +
     " недопустимая операция пропуска в закрытом потоке blob."},

    {ResourceKeys.blobIO__mark_not_supported,
     "JDBC IOException:" +
     " операция mark() не поддерживается на потоках blob."},

    // **********************************
    // *** ConnectionTimeoutException ***
    // **********************************
    {ResourceKeys.socketTimeout__012,
     "Тайм-аут сокета:" +
     " Ввод-вывод был прерван во время ожидания чтения данных с сервера {0}." +
     "\nВозможно, по причине тайм-аута сокета по прошествии {1} секунд(ы)." +
     "\nСообщение InterruptException: \"{2}\"."},

    // ****************************
    // *** UnknownHostException ***
    // ****************************
    {ResourceKeys.unknownHost__0, 
     "Неизвестное исключение хоста при попытке открыть сокет на сервере {0}."},

    // ********************************
    // *** BadInstallationException ***
    // ********************************
    {ResourceKeys.badInstallation__unsupported_jdk_version,
     "Клиент или сервер используют неподдерживаемую версию jdk."},

    {ResourceKeys.badInstallation__security_check_on_socket_01,
     "Ваша система безопасности не разрешает соединений с {0} по порту 3060." +
     "\nСообщение SecurityException: \"{1}\"."},

    {ResourceKeys.badInstallation__incompatible_remote_protocols,
     "Установленные версии InterClient и InterServer используют несовместимые версии протоколов клиент-сервер."},

    // *********************************
    // *** DriverNotCapableException ***
    // *********************************
    {ResourceKeys.driverNotCapable__out_parameters,
     "Неподдерживаемая возможность:" +
     " данная версия InterBase не поддерживает выходные параметра OUT отдельно от выходного набора." +
     "\nИспользуйте выходной набор." +
     "\nСмотри руководсво по API, раздел по interbase.interclient.CallableStatement."},

    {ResourceKeys.driverNotCapable__schemas,
     "Неподдерживаемая возможность:" +
     " InterBase не поддерживает схемы (schemas)."},

    {ResourceKeys.driverNotCapable__catalogs,
     "Неподдерживаемая возможность:" +
     " InterBase не поддерживает каталоги (catalogs)."},

    {ResourceKeys.driverNotCapable__isolation,
     "Неподдерживаемая возможность:" +
     " указанный уровень изоляции транзакций не поддерживается."},

    {ResourceKeys.driverNotCapable__binary_literals,
     "Неподдерживаемая возможность:" +
     " InterBase не поддерживает бинарные литералы."},

    {ResourceKeys.driverNotCapable__asynchronous_cancel,
     "Неподдерживаемая возможность:" +
     " данная версия InterBase не поддерживает асинхронное прерывание оператора."},

    {ResourceKeys.driverNotCapable__query_timeout,
     "Неподдерживаемая возможность:" +
     " данная версия InterBase не поддерживает тайм-ауты запросов."},

    {ResourceKeys.driverNotCapable__connection_timeout,
     "Неподдерживаемая возможность:" +
     " данная версия InterBase не поддерживает тайм-ауты соединений."},

    // !!! needs translation
    {ResourceKeys.driverNotCapable__extension_not_yet_supported,
     "Unsupported feature:" +
     " using a proposed InterClient driver extension to JDBC which is not yet supported."},

    // !!! needs translation
    {ResourceKeys.driverNotCapable__jdbc2_not_yet_supported,
     "Unsupported feature:" +
     " using a JDBC 2 method which is not yet supported."},

    {ResourceKeys.driverNotCapable__escape__t,
     "Неподдерживаемая возможность:" +
     " выражения SQL escape для времени '{'t ''hh:mm:ss'''}' не поддерживаются."},

    {ResourceKeys.driverNotCapable__escape__ts_fractionals, 
     "Неподдерживаемая возможность:" +
     " выражения SQL escape для даты+времени (timestamp) '{'ts ''yyyy-mm-dd hh:mm:ss.f...'''}' не поддерживают долей секунды."},

    {ResourceKeys.driverNotCapable__escape__call_with_result,
     "Неподдерживаемая возможность:" +
     " выражения SQL escape для процедурных вызовов '{'? = call ...'}' с результирующими параметрами не поддерживаются."},

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
     "Неподдерживаемая возможность:" +
     " драйвер не поддерживает указанную кодировку символов ({0})."},

    // ****************************
    // *** OutOfMemoryException ***
    // ****************************
    {ResourceKeys.outOfMemory,
     "Недостаточно памяти:" +
     " InterServer не хватает серверной памяти."}

  };

  // Выгрузка массива ключей и ресурсных пар для данного бандла.
  /**
   * Extracts an array of key, resource pairs for this bundle.
   * @since <font color=red>Extension, since InterClient 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
