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
package interbase.interclient.utils;

// <meta http-equiv="Content-Type" content="text/html; charset=Windows-1251">
// Строковые ресурсы CommDiag.
// <p>
// Вы можете создать свой собственный локализованный ресурс.
// Делая это, вы должны добавить к имени класса суффикс определения языка в формате ISO,
// соответствующий языку локализации. Все дополнительные или замещающие локальные
// ресурсы должны быть помещены в пакет interbase.interclient
// Общие ключи ресурсов представлены для удобства в классе
// interbase.interclient.ResourceKeys.
// <i>Не</i> заменяйте класс ResourceKeys; его переменные используются
// внутренними механизмами CommDiag.
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
//      <code>interbase.interclient.utils.Resources</code>
//      вызовом <code>getContents()</code>.
//      сохраните содержание, включая ключи и текст, в файл или распечатайте для перевода.
// <li> Создайте класс Resources_*.java для вашего варианта локализации.
//      Удостоверьтесь в том, что класс является общедоступным, расширяя 
//      java.util.ListResourceBundle и находится в пакете interbase.interclient.utils. Например:
// <pre>
// // Ресурс французского языка для CommDiag.
// package interbase.interclient.utils;
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
// @extension since 1.50
// @see java.util.Locale
// @see java.util.ResourceBundle
// @see java.text.MessageFormat
// @see ResourceKeys

/**
 * Russian resource bundle for InterClient utilities.
 * @since <font color=red>Extension, since 1.50</font>
 * @see Resources
 **/
public class Resources_ru extends java.util.ListResourceBundle
{
  // Translations by Sergey Orlik
  
  final static private Object[][] resources__ =
  {
    // ****************************
    // *** InstallationVerifier ***
    // ****************************

    {ResourceKeys.icRelease_012,
     "Версия InterClient:                    {0}.{1}.{2}"}, // eg. 1.20.89

    {ResourceKeys.icClientServerEdition,
     ", вариант Клиент/Сервер"},

    {ResourceKeys.icLocalHostOnlyEdition,
     ", локальный вариант"},

    // !!! needs translation
    {ResourceKeys.icCompatibleJREVersions,
     "InterClient compatible JRE versions:   "},

    // !!! needs translation
    {ResourceKeys.icCompatibleIBVersions,
     "InterClient compatible IB versions:    "},

    // Removed....
    //{ResourceKeys.icDevelopedBy_0,
    // "InterClient разработан:                {0}"}, // InterBase Software Corporation

    {ResourceKeys.icDriverName,
     "Имя драйвера InterClient:              interbase.interclient.Driver"},

    {ResourceKeys.icProtocol_0,
     "Протокол InterClient JDBC:             {0}"}, // jdbc:interbase:

    {ResourceKeys.icProtocolVersion_0,
     "Версия протокола InterClient JDBC:     {0}"}, 

    {ResourceKeys.icExpirationDate_0,
     "Дата окончания действия InterClient:   {0,date}"},

    {ResourceKeys.icNoExpirationDate,
     "Дата окончания действия InterClient:   нет"},

    {ResourceKeys.icExpirationDateClarification,
     "Просроченная версия InterClient не прекратит работать, однако\n" +
     "предложения об апгрейде будут выдаваться в виде Предупреждения SQL\n" +
     "для соединений, осуществляемых по истечении срока работы."},

    {ResourceKeys.icDetectedExpiredInterClient,
     "Обнаружен просроченный драйвер InterClient.\n" +
     "Загрузите новую версию с www.interbase.com."},

    {ResourceKeys.installProblemDetected,
     "***** Обнаружена проблема при установке! *****"},

    {ResourceKeys.noInstallProblemDetected,
     "**** НЕ обнаружено проблем при установке! ****"},

    {ResourceKeys.testingURL_0,
     "Проверка URL базы данных{0}."},

    {ResourceKeys.verifyingStructures,
     "Проверка внутренней структуры базы данных. "},

    {ResourceKeys.connectionEstablished_0,
     "Установлено соединение с {0}"}, 

    {ResourceKeys.connectionClosed,
     "Тестовое соединение закрыто."},

    {ResourceKeys.ibProductName_0,
     "Имя продукта базы данных:     {0}"}, // InterBase

    {ResourceKeys.ibProductVersion_0,
     "Версия продукта базы данных:  {0}"}, // eg. SO-V5.1

    {ResourceKeys.ibODSVersion_01,
     "Версия ODS :                  {0}.{1}"}, // eg. 9.0

    {ResourceKeys.ibPageSize_0,
     "Размер страницы базы данных:  {0} bytes"}, // eg. 1024

    {ResourceKeys.ibPageAllocation_0,
     "Выделено страниц базы данных: {0} pages"}, // number of pages

    {ResourceKeys.ibFileSize_0,
     "Размер базы данных:           {0} Kbytes"}, // database file size

// CJL-IB6  !!! added for SQL Dialect Support -- NEEDS TRANSLATION!!!
    {ResourceKeys.ibDBSQLDialect_0,
     "Database SQL Dialect:         {0}"}, // database SQL Dialect (IB 6.0)
// CJL-IB6 end change

    {ResourceKeys.isProductName,
     "Имя сервера среднего уровня JDBC/Net:            InterServer"},

    {ResourceKeys.isProductVersion_0,
     "Версия сервера среднего уровня JDBC/Net:         {0}"},

    {ResourceKeys.isProtocolVersion_0,
     "Версия протокола среднего уровня JDBC/Net:       {0}"}, // jdbc:interbase: version

    {ResourceKeys.isExpirationDate_0,
     "Срок действия сервера среднего уровня JDBC/Net:  {0,date}"},

    {ResourceKeys.isNoExpirationDate,
     "Срок действия сервера среднего уровня JDBC/Net:  не ограничен"},

    {ResourceKeys.isServerPort_0,
     "Порт сервера среднего уровня JDBC/Net         :  {0}"}, // 3060

    // Removed....
    //{ResourceKeys.isDevelopedBy_0,
    // "Сервер среднего уровня JDBC/Net разработан    :  {0}"}, // InterBase Software Corporation

    {ResourceKeys.isExpirationClarification,
     "Просроченная версия InterServer не прекратит работать, однако\n" +
     "предложения об апгрейде будут выдаваться в виде Предупреждение SQL\n" +
     "для соединений, осуществляемых по истечении срока работы."},

    {ResourceKeys.sqlWarning,
     "** Предупреждение SQL **"},

    {ResourceKeys.sqlException,
     "**** Исключение SQL ****"},

    {ResourceKeys.sqlState_0,
     "Состояние SQL: {0}"},

    {ResourceKeys.errorCode_0,
     "Код ошибки: {0}"},

    {ResourceKeys.errorMessage_0,
     "Сообщение: {0}"},

    {ResourceKeys.isc_sys_request,
     "Вам может потребоваться предварить имя базы данных буквой имени диска.\n" +
     "Например: jdbc:interbase://localhost/c:/databases/employee.gdb"},

    {ResourceKeys.caughtThrowable,
     "** Произошла ошибка или возникло исключение **"},

    {ResourceKeys.driverRegistered,
     "interbase.interclient.Driver зарегистрирован."},

    {ResourceKeys.exampleUsage,
     "Пример использования:\n" +
     "    InterServer хост: localhost\n" +
     "    Файл базы данных: c:/databases/atlas.gdb\n" +
     "    Пользователь:     sysdba\n" +
     "    Пароль:           masterkey"},

    {ResourceKeys.notes,
     "Примечание 1: If the database file is remote to the InterServer host," +
     "\n" +
     "        then use a remote database file specification syntax.\n" +
     "Примечание 2: Если localhost нет в вашей конфигурации tcp/ip,\n" +
     "        попробуйте воспользоваться локальным адресом IP вашей машины.\n" +
     "Примечание 3: Получили UnknownHostException?\n" +
     "        Попробуйте попинговать хост InterServer.\n"},

    {ResourceKeys.noClassDefFoundError_0,
     "NoClassDefFoundError: {0}" +
     "\ninterbase.interclient.Driver не найден в пути классов.\n\n" +
     "Пожалуйста, выйдите и измените переменную среды CLASSPATH так,\n" +
     "чтобы она включала каталог <interclient-install-dir>\\classes\n" +
     "Пользователям Jbuilder: измените свойство вашего проекта \"class path\" так,\n" +
     "чтобы оно включало каталог <interclient-install-dir>\\classes\n" +
     "После этого перезапустите CommDiag.\n"},

    // *******************
    // *** CommDiagGUI ***
    // *******************

    {ResourceKeys.commDiagFrameTitle,
     "Диагностика связи InterClient"},

    {ResourceKeys.testButtonText,
     "Тест"},

    {ResourceKeys.exitButtonText,
     "Выход"},

    {ResourceKeys.visitNewsgroupLabel,
     "Посетите группу новостей forums.inprise.com/interbase.public.general"},

    {ResourceKeys.mailBugsLabel,
     "О найденных ошибках сообщайте по e-mail: interclient@interbase.com"},
 
    {ResourceKeys.interBaseServerLabel,
     "Сервер InterServer: "},

    {ResourceKeys.databaseFileLabel,
     "Файл базы данных: "},

    {ResourceKeys.userLabel,
     "Пользователь: "},

    {ResourceKeys.passwordLabel,
     "Пароль: "},

    {ResourceKeys.timeoutLabel,
     "Тайм-аут (секунд): "},

    {ResourceKeys.pleaseWait,
     "Ждите..."}

  };

  // Выделение пар ключей и ресурсов для данного бандла
  /**
   * Extract an array of key, resource pairs for this bundle.
   * @since <font color=red>Extension, since 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
