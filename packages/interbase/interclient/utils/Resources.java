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

/**
 * Default resource bundle for hardwired text and GUI labels within the InterClient utilities package.
 * <p>
 * This is the default (English) resource bundle.
 * You can provide your own locale-specific bundle
 * with an ISO language suffix appended to the class name for
 * your language.  Additional or replacement locale-specific
 * resource bundles must reside in the interbase.interclient.utils
 * package.
 * <p>
 * The driver locates the appropriate resource bundle for your locale by using
 * <pre>
 * java.util.ResourceBundle.getBundle("interbase.interclient.utils.Resources");
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
 * You may also request the java source file for this Resources class
 * by mailing <a href="mailto:interclient@interbase.com"> interclient@interbase.com</a>.
 * Having the Resources.java source file will save you from having to call
 * <code>getContents()</code> as described below.
 * <ol>
 * <li> Get the contents from this Resources class
 *      by calling {@link #getContents getContents()}. Set aside for translation; 
 *      write the contents, including keys and text, to a file or printer for translation.
 * <li> Create a Resources_*.java class for your locale with translated text.
 *      Be sure the class is public, extends java.util.ListResourceBundle and
 *      and is in the package interbase.interclient.utils.  For example:
 * <pre>
 * // A French resource bundle for CommDiag.
 * package interbase.interclient.utils;
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
 * and include your new Resources_fr.class in the interbase/interclient/utils directory.
 * Use <code>jar cvf0 interclient.jar interbase borland</code> to archive all the
 * files back into a new interclient.jar file. Class directories interbase/interclient,
 * interbase/interclient/utils and borland/jdbc should all be archived by this command.
 * <p>
 * Alternatively, modify the separate resource archive interclient-res.jar,
 * and include both interclient-core.jar, interclient-res.jar, and optionally interclient-utils.jar in your
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
 * @since <font color=red>Extension, since 1.50</font>
 * @see java.util.Locale
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 * @author Paul Ostler
 **/
public class Resources extends java.util.ListResourceBundle
{
  // !!!Should I check for security exception here?
  final static private String lineSeparator__ = "\n";
  // java.lang.System.getProperty ("line.separator");

  final static private Object[][] resources__ =
  {
    // ****************************
    // *** InstallationVerifier ***
    // ****************************

    {ResourceKeys.icRelease_012,
     "InterClient Release:                   {0}.{1}.{2}"}, // eg. 1.50.30

    {ResourceKeys.icClientServerEdition,
     ", Client/Server Edition"},

    {ResourceKeys.icLocalHostOnlyEdition,
     ", Local Host Only Edition"},

    {ResourceKeys.icCompatibleJREVersions,
     "InterClient compatible JRE versions:   "},

    {ResourceKeys.icCompatibleIBVersions,
     "InterClient compatible IB versions:    "},

    {ResourceKeys.icDriverName,
     "InterClient driver name:               interbase.interclient.Driver"},

    {ResourceKeys.icProtocol_0,
     "InterClient JDBC protocol:             {0}"}, // jdbc:interbase:

    {ResourceKeys.icProtocolVersion_0,
     "InterClient JDBC protocol version:     {0}"}, 

    {ResourceKeys.icExpirationDate_0,
     "InterClient expiration date:           {0,date}"},

    {ResourceKeys.icNoExpirationDate,
     "InterClient expiration date:           no expiration date"},

    {ResourceKeys.icExpirationDateClarification,
     "An expired InterClient will not cease to function, rather" +
     lineSeparator__ +
     "a suggestion to upgrade will be posted as a SQLWarning" +
     lineSeparator__ +
     "for connections established after the expiration date."},

    {ResourceKeys.icDetectedExpiredInterClient,
     "Detected an expired InterClient driver." +
     lineSeparator__ +
     "Download a newer version from www.interbase.com."},

    {ResourceKeys.installProblemDetected,
     "***** Installation problem detected! *****"},

    {ResourceKeys.noInstallProblemDetected,
     "***** NO Installation problems detected! *****"},

    {ResourceKeys.testingURL_0,
     "Testing database URL {0}."},

    {ResourceKeys.verifyingStructures,
     "Verifying internal database structures. "},

    {ResourceKeys.connectionEstablished_0,
     "Connection established to {0}"}, 

    {ResourceKeys.connectionClosed,
     "Test connection closed."},

    {ResourceKeys.ibProductName_0,
     "Database product name:      {0}"}, // InterBase

    {ResourceKeys.ibProductVersion_0,
     "Database product version:   {0}"}, // eg. SO-V5.1

    {ResourceKeys.ibODSVersion_01,
     "Database ODS version:       {0}.{1}"}, // eg. 9.0

    {ResourceKeys.ibPageSize_0,
     "Database Page Size:         {0} bytes"}, // eg. 1024

    {ResourceKeys.ibPageAllocation_0,
     "Database Page Allocation:   {0} pages"}, // number of pages

    {ResourceKeys.ibFileSize_0,
     "Database Size:              {0} Kbytes"}, // database file size

// CJL-IB6 added for SQL Dialect Support
    {ResourceKeys.ibDBSQLDialect_0,
     "Database SQL Dialect:       {0}"}, // database SQL Dialect (IB 6.0)
// CJL-IB6 end change

    {ResourceKeys.isProductName,
     "Middleware JDBC/Net server name:             InterServer"}, 

    {ResourceKeys.isProductVersion_0,
     "Middleware JDBC/Net server version:          {0}"},

    {ResourceKeys.isProtocolVersion_0,
     "Middleware JDBC/Net server protocol version: {0}"}, // jdbc:interbase: version

    {ResourceKeys.isExpirationDate_0,
     "Middleware JDBC/Net server expiration date:  {0,date}"},

    {ResourceKeys.isNoExpirationDate,
     "Middleware JDBC/Net server expiration date:  no expiration date"},

    {ResourceKeys.isServerPort_0,
     "Middleware JDBC/Net server port:             {0}"}, // 3060

    {ResourceKeys.isExpirationClarification,
     "An expired InterServer will not cease to function, rather" +
     lineSeparator__ +
     "a suggestion to upgrade will be posted as a SQLWarning" +
     lineSeparator__ +
     "for connections established after the expiration date."},

    {ResourceKeys.sqlWarning,
     "***** SQLWarning *****"},

    {ResourceKeys.sqlException,
     "***** SQLException *****"},

    {ResourceKeys.sqlState_0,
     "SQL State: {0}"},

    {ResourceKeys.errorCode_0,
     "Error Code: {0}"},

    {ResourceKeys.errorMessage_0,
     "Message: {0}"},

    {ResourceKeys.isc_sys_request,
     "You may need to prefix your database with a drive letter." +
     lineSeparator__ +
     "For example, jdbc:interbase://localhost/c:/databases/employee.gdb"},

    {ResourceKeys.caughtThrowable,
     "***** Error or Exception Occurred *****"},

    {ResourceKeys.driverRegistered,
     "interbase.interclient.Driver registered."},

    {ResourceKeys.exampleUsage,
     "Example Usage:" +
     lineSeparator__ +
     "    InterServer Host: localhost" +
     lineSeparator__ +
     "    Database File:    c:/databases/atlas.gdb" +
     lineSeparator__ +
     "    User:             sysdba" +
     lineSeparator__ +
     "    Password:         masterkey"},

    {ResourceKeys.notes,
     "Note 1: If the database file is remote to the InterServer host," +
     lineSeparator__ +
     "        then use a remote database file specification syntax." +
     lineSeparator__ +
     "Note 2: If localhost is not in your tcp/ip host configuration," +
     lineSeparator__ +
     "        then try using your machine''s host name or loopback IP address." +
     lineSeparator__ +
     "Note 3: Getting UnknownHostException?" +
     lineSeparator__ +
     "        Try pinging the InterServer host.  Try using an IP address."},

    {ResourceKeys.noClassDefFoundError_0,
     "NoClassDefFoundError: {0}" +
     lineSeparator__ +
     "interbase.interclient.Driver not found in class path." +
     lineSeparator__ +
     lineSeparator__ +
     "Please exit and modify your system''s CLASSPATH environment variable" +
     lineSeparator__ +
     "to include <interclient-install-dir>\\classes" +
     lineSeparator__ +
     "For JBuilder users, modify your project property class path setting" +
     lineSeparator__ +
     "to include <interclient-install-dir>\\classes" +
     lineSeparator__ +
     "Finally, restart CommDiag after class paths are modified." +
     lineSeparator__},

    // *******************
    // *** CommDiagGUI ***
    // *******************

    {ResourceKeys.commDiagFrameTitle,
     "InterClient Communication Diagnostics"},

    {ResourceKeys.testButtonText,
     "Test"},

    {ResourceKeys.exitButtonText,
     "Exit"},

    {ResourceKeys.visitNewsgroupLabel,
     "Visit newsgroup forums.inprise.com/interbase.public.general"},

    {ResourceKeys.mailBugsLabel,
     "Mail bugs to interclient@interbase.com"},
 
    {ResourceKeys.interBaseServerLabel,
     "InterServer Host: "},

    {ResourceKeys.databaseFileLabel,
     "Database File: "},

    {ResourceKeys.userLabel,
     "User: "},

    {ResourceKeys.passwordLabel,
     "Password: "},

    {ResourceKeys.timeoutLabel,
     "Timeout (seconds): "},

    {ResourceKeys.pleaseWait,
     "Please wait..."}

  };

  /**
   * Extract an array of key, resource pairs for this bundle.
   * @since <font color=red>Extension, since 1.50</font>
   **/
  public Object[][] getContents()
  {
    return resources__;
  }
}
