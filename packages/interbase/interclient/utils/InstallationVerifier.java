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

final class InstallationVerifier
{
  final static private int isc_sys_request = 335544373;
  final static private int initialStringBufferSize__ = 8000; // #chars
  // !!! this needs to be lineSeparator__
  final static private char newline__ = '\n';

  interbase.interclient.Driver driver_;

  InstallationVerifier () {}

  String loadDriver ()
  {
    StringBuffer result = new StringBuffer ();
    try {
      driver_ = new interbase.interclient.Driver ();
      result.append (CommDiag.getResource (ResourceKeys.driverRegistered));
      result.append (newline__);
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.exampleUsage));
      result.append (newline__);
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.notes));
      result.append (newline__);
      return result.toString ();
    }
    catch (NoClassDefFoundError e) { // IC Driver not in path.
      result.append (CommDiag.getResource (ResourceKeys.noClassDefFoundError_0, e.getMessage ()));
      return problemDetected (result);
    }
    catch (Throwable e) {
      result.append (CommDiag.getResource (ResourceKeys.caughtThrowable));
      result.append (newline__);
      result.append (e.getClass ().getName ());
      result.append (": ");
      result.append (e.getMessage ());
      return problemDetected (result);
    }
  }

  String verifyInstallation (String server,
			     String database,
			     String user,
			     String password,
			     int loginTimeout, // milliseconds
			     boolean enableStructureVerification,
			     boolean showDatabaseFeatures)
  {
    StringBuffer result = new StringBuffer (initialStringBufferSize__);
    try {
      result.append (CommDiag.getResource (ResourceKeys.icRelease_012,
                                           driver_.getMajorVersion (),
                                           driver_.getMinorVersion (),
                                           driver_.getBuildNumber ()));
      if (driver_.getBuildCertificationLevel () == interbase.interclient.Driver.testBuild)
        result.append (" Test Build");
      if (driver_.getBuildCertificationLevel () == interbase.interclient.Driver.betaBuild)
        result.append (" Beta");

      if (driver_.clientServerEdition ())
	result.append (CommDiag.getResource (ResourceKeys.icClientServerEdition));
      else
	result.append (CommDiag.getResource (ResourceKeys.icLocalHostOnlyEdition));
      result.append (newline__);

      result.append (CommDiag.getResource (ResourceKeys.icCompatibleJREVersions));
      String[] compatibleJREVersions = driver_.getCompatibleJREVersions ();
      result.append (compatibleJREVersions[0]);
      for (int i=1; i<compatibleJREVersions.length; i++)
        result.append (", " + compatibleJREVersions[i]);
      result.append (newline__);

      result.append (CommDiag.getResource (ResourceKeys.icCompatibleIBVersions));
      int[] compatibleIBVersions = driver_.getCompatibleIBVersions ();
      result.append (compatibleIBVersions[0]);
      for (int i=1; i<compatibleIBVersions.length; i++)
        result.append (", " + compatibleIBVersions[i]);
      result.append (newline__);

      result.append (CommDiag.getResource (ResourceKeys.icDriverName));
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.icProtocol_0, driver_.getJDBCNetProtocol ()));
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.icProtocolVersion_0, String.valueOf (driver_.getJDBCNetProtocolVersion ())));
      result.append (newline__);

	result.append (CommDiag.getResource (ResourceKeys.icNoExpirationDate));

      result.append (newline__);
      result.append (newline__);

      /*******
       * SecurityManager security = System.getSecurityManager ();
       * if (security == null)
       * 	result.append (CommDiag.getResource (Resources.icDetectedNoSecurityManager));
       * else
       * 	result.append (CommDiag.getResource (Resources.icDetectedSecurityManager));
       * result.append (newline__);
       *
       * try {
       * 	if (security != null)
       * 	  security.checkPropertyAccess ("java.version");
       * 	String javaVersion = System.getProperty ("java.version");
       * 	result.append (CommDiag.getResource (Resources.icDetectedJREVersion, javaVersion));
       * 	result.append (newline__);
       * }
       * catch (SecurityException e) {
       * 	result.append (CommDiag.getResource (Resources.icSecurityExceptionOnJREVersionCheck));
       * 	result.append (newline__);
       * }
       *
       * try {
       * 	if (security != null)
       * 	  security.checkPropertyAccess ("jdbc.drivers");
       * 	String jdbcDrivers = System.getProperty ("jdbc.drivers");
       * 	if (jdbcDrivers == null)
       * 	  result.append (CommDiag.getResource (Resources.icDetectedNoSystemProperty));
       * 	else
       * 	  result.append (CommDiag.getResource (Resources.icDetectedSystemProperty, jdbcDrivers));
       * 	result.append (newline__);
       * }
       * catch (SecurityException e) {}
       **/

      // getLocalHost() will hang HotJava with a high security manager.
      // I don't think this test is really necessary.
      /****************************************************************************
      * if (security == null) {
      *   try {
      *     result.append ("Detected your localhost: ");
      *     result.append (java.net.InetAddress.getLocalHost ().toString ());
      *     result.append (newline__);
      *   }
      *   catch (java.net.UnknownHostException e) {
      *     result.append ("Detected an improper TCP/IP client configuration.\n");
      *     result.append ("Unable to determine the IP address of your local host: ");
      *     result.append (e.getMessage ());
      *     result.append ("\nMake sure your client machine has a properly configured IP address.\n");
      *     badInstall = true;
      *   }
      * }
      ****************************************************************************/

      // Ok, if we've made it this far then lets try a connection.

      String databaseURL = "jdbc:interbase://" + server + "/" + database;

      result.append (CommDiag.getResource (ResourceKeys.testingURL_0, databaseURL));
      result.append (newline__);

      interbase.interclient.Connection connection = null;
      try {
	interbase.interclient.ConnectionProperties properties = 
          new interbase.interclient.ConnectionProperties ();
	properties.setUser (user, password);
        //properties.enableStructureVerification (enableStructureVerification);
    
        java.sql.DriverManager.setLoginTimeout (loginTimeout);
	connection = (interbase.interclient.Connection) driver_.connect (databaseURL, properties);

        //if (enableStructureVerification) {
	//   result.append (CommDiag.getResource (Resources.verifyingStructures));
	//  result.append (newline__);
        //}

	result.append (CommDiag.getResource (ResourceKeys.connectionEstablished_0, databaseURL));
        result.append (newline__);
        result.append (newline__);

	interbase.interclient.DatabaseMetaData md =
          (interbase.interclient.DatabaseMetaData) connection.getMetaData ();
	result.append (CommDiag.getResource (ResourceKeys.ibProductName_0, md.getDatabaseProductName ()));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.ibProductVersion_0, md.getDatabaseProductVersion ()));
        result.append (newline__);
        // the following is just to test when this is fixed.
	//result.append ("ib major version = " + md.getDatabaseMajorVersion ());
        //result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.ibODSVersion_01, md.getODSMajorVersion (), md.getODSMinorVersion ()));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.ibPageSize_0, md.getPageSize ()));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.ibPageAllocation_0, md.getPageAllocation ()));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.ibFileSize_0, (int) (md.getPageAllocation () * md.getPageSize ()/1024)));
// CJL-IB6  report the database SQL Dialect
        result.append (newline__);
        result.append (CommDiag.getResource (ResourceKeys.ibDBSQLDialect_0, md.getDatabaseSQLDialect() ));
// CJL-IB6 end change
        result.append (newline__);
        result.append (newline__);

	result.append (CommDiag.getResource (ResourceKeys.isProductName));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.isProductVersion_0, md.getInterServerVersion ()));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.isProtocolVersion_0, String.valueOf (md.getInterServerJDBCNetProtocolVersion ())));
        result.append (newline__);
	  result.append (CommDiag.getResource (ResourceKeys.isNoExpirationDate));
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.isServerPort_0, String.valueOf (md.getInterServerPort ())));
        result.append (newline__);
        result.append (newline__);

        //if (showDatabaseFeatures) {
	//  result.append (newline__);
        //  getSupportedDatabaseFeatures (md, properties, result);
	//  result.append (newline__);
	//}

        java.sql.SQLWarning warnings = connection.getWarnings ();
	if (warnings != null) {
	  result.append (newline__);
	  result.append (CommDiag.getResource (ResourceKeys.sqlWarning));
	  result.append (newline__);
	  result.append (formatSQLException (warnings));
	}
        
	connection.close ();
	result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.connectionClosed));
	result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.noInstallProblemDetected));
	result.append (newline__);
	return result.toString ();
      }
      catch (java.sql.SQLException e) {
        result.append (newline__);
	result.append (CommDiag.getResource (ResourceKeys.sqlException));
	result.append (newline__);
	result.append (formatSQLException (e));
        if (e.getErrorCode () == isc_sys_request) {
          result.append (CommDiag.getResource (ResourceKeys.isc_sys_request));
	  result.append (newline__);
	}
        if (e.getErrorCode () == 335544721) {} // !!! new IB error, ask Bill Karwin
	return problemDetected (result);
      }
    }
    catch (Throwable e) {
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.caughtThrowable));
      result.append (newline__);
      result.append (e.getClass ().getName ());
      result.append (": ");
      result.append (e.getMessage ());
      return problemDetected (result);
    }
  }

  private String problemDetected (StringBuffer result)
  {
    result.append (newline__);
    result.append (CommDiag.getResource (ResourceKeys.installProblemDetected));
    result.append (newline__);
    return result.toString ();
  }

  private String formatSQLException (java.sql.SQLException e) 
  {
    StringBuffer result = new StringBuffer ();
    while (e != null) {
      // I should really check to make sure SQLState and message are not null
      result.append (CommDiag.getResource (ResourceKeys.sqlState_0, e.getSQLState ()));
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.errorCode_0, String.valueOf (e.getErrorCode ())));
      result.append (newline__);
      result.append (CommDiag.getResource (ResourceKeys.errorMessage_0, e.getMessage ()));
      result.append (newline__);

      e = e.getNextException ();
    }
    return result.toString ();
  }
}
