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

// This class contains version information which is surfaced via the api.
// Some are surfaced thru JDBC extended methods.
// It also contains license options for enabling driver features 
// such as "client/server edition" and "expiration date".
// In this way, the VersionInformation.class file acts in
// a similar capacity as the InterBase license.dat file;
// It can be used for extending the expiration date, or enabling other 
// optional features of an existing driver.
// <p>
// This license information is included in a class rather than
// a flat file such as license.dat because there are security
// restrictions for java applets reading local files. 
// Another essential difference between this class and license.dat 
// is that this class contains final statics that are essentially "macro-expanded"
// throughout the other classes in the interclient library
// which use this version information.
// That is, the final statics are "compiled into" the code statically;
// so you can't simply slide in a new version of this
// class to modify an existing interclient library.
// One way to get around this problem would be to wrap your static values
// with a static method to force runtime references to look at this class
// (eg. getExpirationDate ()).  
// However, there are several problems with this approach.
// <ul>
// <li> The approach requires that this class not be obfuscated, even though
//      it's a private class.
//      Therefore, since customers will have access to its members, nothing
//      prevents them from redefining the VersionInformation class to their
//      own liking.
// <li> The approach does not work if interclient is distributed as a jar file.
// </ul>
// So any changes to the InterClient "license" require shipping
// a complete class library.
// <p>
// InterServer has a traditional license file.

// Global version information for the InterClient driver as a whole.
// Prior to InterClient 2.0, version information was extracted using
// methods in the {@link Driver Driver} class.
// However, the JDBC 2 Standard Extension introduced an alternative
// to the Driver API via {@link DataSource DataSource}s.
// Under the Driver API, the first reference to the Driver class
// results in an implicit driver instance being registered with the
// DriverManager.  Under the DataSource API, driver instances are
// not implicitly registered with the driver manager.
// This class provides a common area accessible under either API.

final class Globals
{
  // --------------------------------version info-------------------------------
  
  final static int testBuild_ = 0;
  final static int betaBuild_ = 1;
  final static int finalBuild_ = 2;

  // for
  //   Driver.getMajorVersion ()
  //   Driver.getMinorVersion ()
  //   Driver.getBuildNumber ()
  //   Driver.getBuildCertificationLevel ()
  //   Driver.getCompatibileJREVersions ()
  //   DatabaseMetaData.getDriverMajorVersion ()
  //   DatabaseMetaData.getDriverMinorVersion ()
  //   DatabaseMetaData.getDriverVersion ()
  final static int interclientMajorVersion__ = 2;
  final static int interclientMinorVersion__ = 00;
  final static int interclientBuildNumber__ = 1;
  final static int interclientBuildCertification__ = testBuild_;
  final static String[] interclientCompatibleJREVersions__ = new String[] {"1.2"};
  final static int[] compatibleIBVersions__ = new int[] {5, 6};
  final static String interclientVersionString__ =
    interclientMajorVersion__ + "." +
    interclientMinorVersion__ + "." +
    interclientBuildNumber__ +
    ((interclientBuildCertification__ == testBuild_) ? " Test Build" :
    ((interclientBuildCertification__ == betaBuild_) ? " Beta" : "")) +
    " for JRE 1.2" +
    " and InterBase v5 and v6";

  // for
  //   Driver.getJDBCNetProtocol ()
  //   Driver.getJDBCNetProtocolVersion ()
  final static String jdbcNetProtocol__ = "jdbc:interbase:";
  final static int jdbcNetProtocolVersion__ = 20001;

  // If not client-server edition, then restrict connections to this machine
  final static String localHostOnlyRestrictedServer__ = "localhost";

  // User name for sysdba as used by InterBase
  final static String sysdba__ = "SYSDBA";

  // The driver will deny connection requests if
  // the current date is beyond this expiration date.
  // If null, the driver will not expire.
  // for Driver.getExpirationDate ()
  final static java.util.Date interclientExpirationDate__; 

  // Surfaced thru a JDBC extended method:
  //   Driver.clientServerEdition ()
  // Client/Server edition drivers may establish remote connections,
  // otherwise only localhost connections are allowed.
  final static boolean clientServerEdition__ = true;

  // TCP/IP port number for the interserver process
  final static int defaultServerPort__ = 3060;

  // --------------------------------------------------------------------------
  
  static private java.util.ResourceBundle resources__;
  static java.sql.SQLException exceptionsOnLoadResources__ = null;
  private final static Object[] emptyArgs__ = new Object[] {};

  final static boolean debug__ = false;  // preship: change to false!!!
  static long endTime__, startTime__;

  static BufferCache cache__ = new BufferCache ();

  static {
    java.util.GregorianCalendar cal = new java.util.GregorianCalendar ();
    cal.set (2005, java.util.Calendar.DECEMBER, 30); // preship: change!!!
    interclientExpirationDate__ = cal.getTime ();
    try {
      loadResources ();
    }
    catch (java.sql.SQLException e) {
      exceptionsOnLoadResources__ = e;
    }
  }

  static void loadResources () throws java.sql.SQLException
  {
    try {
      resources__ = java.util.ResourceBundle.getBundle ("interbase.interclient.Resources");
    }
    catch (java.util.MissingResourceException e) {
      throw new java.sql.SQLException ("[interclient] " +
				       "Missing resource bundle:" +
				       " an InterClient resource bundle could not be found" +
				       " in the interbase.interclient package.",
				       "ICJJ0",
				       ErrorCodes.missingResourceBundle);
    }
  }

  static String getResource (String key, Object[] args)
  {      
    try {
      return java.text.MessageFormat.format (resources__.getString (key), args);
    }
    catch (java.util.MissingResourceException e) {
      try {
	      return java.text.MessageFormat.format (resources__.getString (ResourceKeys.missingResource__01),
					                                     new Object[] {e.getKey(), e.getClassName ()});
      }
      catch (java.util.MissingResourceException e2) {
	      return java.text.MessageFormat.format ("No resource for key {0} could be found in resource bundle {1}.",
					                                     new Object[] {e.getKey(), e.getClassName ()});
      }
    }
  }

  static String getResource (String key)
  {
    return getResource (key, emptyArgs__);
  }

  static void trace (Object object)
  {
    // !!! remove this, only trace on a datasource
// CJL-IB6 !!! remove this
  // System.out.println( object );
  }
}
