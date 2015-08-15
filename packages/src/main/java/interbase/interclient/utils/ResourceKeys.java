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
 * Resource keys used internally by CommDiag.
 * <p>
 * These keys are provided only as a convenience for managing
 * locale-specific resource strings.  These keys will change
 * with each release, some may be added and some may be deleted,
 * so do not use these keys for any purpose other than a version-
 * dependent resource bundle.
 * <p>
 * The resource for any key can be obtained as follows:
 * <pre>
 * java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("interbase.interclient.utils.Resources");
 * resource.getString(<i>key</i>);
 * </pre>
 *
 * @see Resources
 * @since <font color=red>1.50, not public in 1.50 final release</font>
 * @author Paul Ostler
 **/
class ResourceKeys
{
  // Define a private constructor to prevent default public constructor
  private ResourceKeys () {}

  // *** This is actually not used in the code, but
  // *** is useful to the developer for assigning new keys,
  // *** just keep incrementing it.  Avoid reusing
  // *** holes in case an old bundle is used with a new build.
  private final static int lastKeyValue__ = 59;

  final static public String icRelease_012 = "1";
  final static public String icClientServerEdition = "2";
  final static public String icLocalHostOnlyEdition = "3";
  final static public String icCompatibleJREVersions = "57";
  final static public String icCompatibleIBVersions = "58";
  final static public String icDriverName = "6";
  final static public String icProtocol_0 = "7";
  final static public String icProtocolVersion_0 = "8";
  final static public String icExpirationDate_0 = "9";
  final static public String icNoExpirationDate = "10";
  final static public String icExpirationDateClarification = "11";
  final static public String icDetectedExpiredInterClient = "12";

  final static public String testingURL_0 = "13";
  final static public String verifyingStructures = "14";
  final static public String connectionEstablished_0 = "15";
  final static public String connectionClosed = "16";

  final static public String ibProductName_0 = "17";
  final static public String ibProductVersion_0 = "18";
  final static public String ibODSVersion_01 = "19";
  final static public String ibPageSize_0 = "20";
  final static public String ibPageAllocation_0 = "21";
  final static public String ibFileSize_0 = "22";
// CJL-IB6 added for SQL Dialect support
  final static public String ibDBSQLDialect_0 = "59";
// CJL-IB6 end change  

  final static public String isProductName = "23";
  final static public String isProductVersion_0 = "24";
  final static public String isProtocolVersion_0 = "26";
  final static public String isExpirationDate_0 = "27";
  final static public String isNoExpirationDate = "28";
  final static public String isServerPort_0 = "29";
  final static public String isExpirationClarification = "31";

  final static public String installProblemDetected = "32";
  final static public String noInstallProblemDetected = "33";
  final static public String sqlWarning = "34";
  final static public String sqlException = "35";
  final static public String sqlState_0 = "36";
  final static public String errorCode_0 = "37";
  final static public String errorMessage_0 = "38";
  final static public String isc_sys_request = "39";
  final static public String classNotFound = "40";
  final static public String caughtThrowable = "41";

  final static public String driverRegistered = "42";
  final static public String exampleUsage = "43";
  final static public String notes = "44";
  final static public String noClassDefFoundError_0 = "45";

  final static public String commDiagFrameTitle = "46";
  final static public String testButtonText = "47";
  final static public String exitButtonText = "48";
  final static public String visitNewsgroupLabel = "49";
  final static public String mailBugsLabel = "50";
  final static public String interBaseServerLabel = "51";
  final static public String databaseFileLabel = "52";
  final static public String userLabel = "53";
  final static public String passwordLabel = "54";
  final static public String timeoutLabel = "55";
  final static public String pleaseWait = "56";
}
