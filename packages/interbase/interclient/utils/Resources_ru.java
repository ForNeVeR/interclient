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
// ��������� ������� CommDiag.
// <p>
// �� ������ ������� ���� ����������� �������������� ������.
// ����� ���, �� ������ �������� � ����� ������ ������� ����������� ����� � ������� ISO,
// ��������������� ����� �����������. ��� �������������� ��� ���������� ���������
// ������� ������ ���� �������� � ����� interbase.interclient
// ����� ����� �������� ������������ ��� �������� � ������
// interbase.interclient.ResourceKeys.
// <i>��</i> ��������� ����� ResourceKeys; ��� ���������� ������������
// ����������� ����������� CommDiag.
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
//      <code>interbase.interclient.utils.Resources</code>
//      ������� <code>getContents()</code>.
//      ��������� ����������, ������� ����� � �����, � ���� ��� ������������ ��� ��������.
// <li> �������� ����� Resources_*.java ��� ������ �������� �����������.
//      �������������� � ���, ��� ����� �������� �������������, �������� 
//      java.util.ListResourceBundle � ��������� � ������ interbase.interclient.utils. ��������:
// <pre>
// // ������ ������������ ����� ��� CommDiag.
// package interbase.interclient.utils;
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
     "������ InterClient:                    {0}.{1}.{2}"}, // eg. 1.20.89

    {ResourceKeys.icClientServerEdition,
     ", ������� ������/������"},

    {ResourceKeys.icLocalHostOnlyEdition,
     ", ��������� �������"},

    // !!! needs translation
    {ResourceKeys.icCompatibleJREVersions,
     "InterClient compatible JRE versions:   "},

    // !!! needs translation
    {ResourceKeys.icCompatibleIBVersions,
     "InterClient compatible IB versions:    "},

    // Removed....
    //{ResourceKeys.icDevelopedBy_0,
    // "InterClient ����������:                {0}"}, // InterBase Software Corporation

    {ResourceKeys.icDriverName,
     "��� �������� InterClient:              interbase.interclient.Driver"},

    {ResourceKeys.icProtocol_0,
     "�������� InterClient JDBC:             {0}"}, // jdbc:interbase:

    {ResourceKeys.icProtocolVersion_0,
     "������ ��������� InterClient JDBC:     {0}"}, 

    {ResourceKeys.icExpirationDate_0,
     "���� ��������� �������� InterClient:   {0,date}"},

    {ResourceKeys.icNoExpirationDate,
     "���� ��������� �������� InterClient:   ���"},

    {ResourceKeys.icExpirationDateClarification,
     "������������ ������ InterClient �� ��������� ��������, ������\n" +
     "����������� �� �������� ����� ���������� � ���� �������������� SQL\n" +
     "��� ����������, �������������� �� ��������� ����� ������."},

    {ResourceKeys.icDetectedExpiredInterClient,
     "��������� ������������ ������� InterClient.\n" +
     "��������� ����� ������ � www.interbase.com."},

    {ResourceKeys.installProblemDetected,
     "***** ���������� �������� ��� ���������! *****"},

    {ResourceKeys.noInstallProblemDetected,
     "**** �� ���������� ������� ��� ���������! ****"},

    {ResourceKeys.testingURL_0,
     "�������� URL ���� ������{0}."},

    {ResourceKeys.verifyingStructures,
     "�������� ���������� ��������� ���� ������. "},

    {ResourceKeys.connectionEstablished_0,
     "����������� ���������� � {0}"}, 

    {ResourceKeys.connectionClosed,
     "�������� ���������� �������."},

    {ResourceKeys.ibProductName_0,
     "��� �������� ���� ������:     {0}"}, // InterBase

    {ResourceKeys.ibProductVersion_0,
     "������ �������� ���� ������:  {0}"}, // eg. SO-V5.1

    {ResourceKeys.ibODSVersion_01,
     "������ ODS :                  {0}.{1}"}, // eg. 9.0

    {ResourceKeys.ibPageSize_0,
     "������ �������� ���� ������:  {0} bytes"}, // eg. 1024

    {ResourceKeys.ibPageAllocation_0,
     "�������� ������� ���� ������: {0} pages"}, // number of pages

    {ResourceKeys.ibFileSize_0,
     "������ ���� ������:           {0} Kbytes"}, // database file size

// CJL-IB6  !!! added for SQL Dialect Support -- NEEDS TRANSLATION!!!
    {ResourceKeys.ibDBSQLDialect_0,
     "Database SQL Dialect:         {0}"}, // database SQL Dialect (IB 6.0)
// CJL-IB6 end change

    {ResourceKeys.isProductName,
     "��� ������� �������� ������ JDBC/Net:            InterServer"},

    {ResourceKeys.isProductVersion_0,
     "������ ������� �������� ������ JDBC/Net:         {0}"},

    {ResourceKeys.isProtocolVersion_0,
     "������ ��������� �������� ������ JDBC/Net:       {0}"}, // jdbc:interbase: version

    {ResourceKeys.isExpirationDate_0,
     "���� �������� ������� �������� ������ JDBC/Net:  {0,date}"},

    {ResourceKeys.isNoExpirationDate,
     "���� �������� ������� �������� ������ JDBC/Net:  �� ���������"},

    {ResourceKeys.isServerPort_0,
     "���� ������� �������� ������ JDBC/Net         :  {0}"}, // 3060

    // Removed....
    //{ResourceKeys.isDevelopedBy_0,
    // "������ �������� ������ JDBC/Net ����������    :  {0}"}, // InterBase Software Corporation

    {ResourceKeys.isExpirationClarification,
     "������������ ������ InterServer �� ��������� ��������, ������\n" +
     "����������� �� �������� ����� ���������� � ���� �������������� SQL\n" +
     "��� ����������, �������������� �� ��������� ����� ������."},

    {ResourceKeys.sqlWarning,
     "** �������������� SQL **"},

    {ResourceKeys.sqlException,
     "**** ���������� SQL ****"},

    {ResourceKeys.sqlState_0,
     "��������� SQL: {0}"},

    {ResourceKeys.errorCode_0,
     "��� ������: {0}"},

    {ResourceKeys.errorMessage_0,
     "���������: {0}"},

    {ResourceKeys.isc_sys_request,
     "��� ����� ������������� ���������� ��� ���� ������ ������ ����� �����.\n" +
     "��������: jdbc:interbase://localhost/c:/databases/employee.gdb"},

    {ResourceKeys.caughtThrowable,
     "** ��������� ������ ��� �������� ���������� **"},

    {ResourceKeys.driverRegistered,
     "interbase.interclient.Driver ���������������."},

    {ResourceKeys.exampleUsage,
     "������ �������������:\n" +
     "    InterServer ����: localhost\n" +
     "    ���� ���� ������: c:/databases/atlas.gdb\n" +
     "    ������������:     sysdba\n" +
     "    ������:           masterkey"},

    {ResourceKeys.notes,
     "���������� 1: If the database file is remote to the InterServer host," +
     "\n" +
     "        then use a remote database file specification syntax.\n" +
     "���������� 2: ���� localhost ��� � ����� ������������ tcp/ip,\n" +
     "        ���������� ��������������� ��������� ������� IP ����� ������.\n" +
     "���������� 3: �������� UnknownHostException?\n" +
     "        ���������� ����������� ���� InterServer.\n"},

    {ResourceKeys.noClassDefFoundError_0,
     "NoClassDefFoundError: {0}" +
     "\ninterbase.interclient.Driver �� ������ � ���� �������.\n\n" +
     "����������, ������� � �������� ���������� ����� CLASSPATH ���,\n" +
     "����� ��� �������� ������� <interclient-install-dir>\\classes\n" +
     "������������� Jbuilder: �������� �������� ������ ������� \"class path\" ���,\n" +
     "����� ��� �������� ������� <interclient-install-dir>\\classes\n" +
     "����� ����� ������������� CommDiag.\n"},

    // *******************
    // *** CommDiagGUI ***
    // *******************

    {ResourceKeys.commDiagFrameTitle,
     "����������� ����� InterClient"},

    {ResourceKeys.testButtonText,
     "����"},

    {ResourceKeys.exitButtonText,
     "�����"},

    {ResourceKeys.visitNewsgroupLabel,
     "�������� ������ �������� forums.inprise.com/interbase.public.general"},

    {ResourceKeys.mailBugsLabel,
     "� ��������� ������� ��������� �� e-mail: interclient@interbase.com"},
 
    {ResourceKeys.interBaseServerLabel,
     "������ InterServer: "},

    {ResourceKeys.databaseFileLabel,
     "���� ���� ������: "},

    {ResourceKeys.userLabel,
     "������������: "},

    {ResourceKeys.passwordLabel,
     "������: "},

    {ResourceKeys.timeoutLabel,
     "����-��� (������): "},

    {ResourceKeys.pleaseWait,
     "�����..."}

  };

  // ��������� ��� ������ � �������� ��� ������� ������
  /**
   * Extract an array of key, resource pairs for this bundle.
   * @since <font color=red>Extension, since 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
