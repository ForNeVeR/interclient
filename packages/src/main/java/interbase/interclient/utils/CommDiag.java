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
 * A communication diagnostics utility for verifying basic
 * installation and connectivity of InterClient.
 * <p>
 * This class doubles as both an applet and an application.
 * To invoke as an application:
 * <pre>
 * java interbase.interclient.utils.CommDiag
 * </pre>
 * <p>
 * To invoke as an applet, point your browser to
 * <code>CommDiag.html</code>
 * <p>
 * To embedd CommDiag within an application, create a CommDiag object
 * and initialize it dynamically in the embedding application as follows
 * <pre>
 * new interbase.interclient.utils.CommDiag().init();
 * </pre>
 *
 * @since <font color=red>Extension</font>
 * @author Paul Ostler
 **/
public class CommDiag extends java.applet.Applet
{
  // Resources are shared by CommDiagGUI, CommDiagListener, InstallationVerifier
  static java.util.ResourceBundle resources__;
  InstallationVerifier verifier_ = new InstallationVerifier ();

  /**
   * The CommDiag application.
   *
   * @since <font color=red>Extension</font>
   * @throws java.sql.SQLException if there is no resource bundle available.
   **/
  public static void main (String args[]) throws java.sql.SQLException
  {
    loadResources ();
    // java.sql.DriverManager.setLogStream (System.out);
    new CommDiagGUI (new CommDiag (), false);
  }

  /**
   * Initialize CommDiag as an applet.
   * init() may also be used to open a CommDiag frame dynamically within
   * an embedding application.
   *
   * @since <font color=red>Extension</font>
   **/
  public void init ()
  {
    try {
      loadResources ();
      new CommDiagGUI (this, true);
    }
    catch (java.sql.SQLException e) {
      showStatus (e.getMessage ());
    }
  }

  synchronized void exit ()
  {
    System.exit (0);
  }

  static void loadResources () throws java.sql.SQLException
  {
    try {
      resources__ = java.util.ResourceBundle.getBundle ("interbase.interclient.utils.Resources");
    }
    catch (java.util.MissingResourceException e) {
      throw new java.sql.SQLException ("Missing resource bundle:" +
				       " a CommDiag resource bundle could not be found" +
				       " in the interbase.interclient.utils package.",
				       "ICJJ0",
				       interbase.interclient.ErrorCodes.missingResourceBundle);
    }
  }

  static String getResource (String key)
  {
    try {
      return java.text.MessageFormat.format (resources__.getString (key),
                                             new Object[] {});
    }
    catch (java.util.MissingResourceException e) {
      return "No resource for key " + e.getKey ();
    }
  }

  static String getResource (String key, Object[] args)
  {
    try {
      return java.text.MessageFormat.format (resources__.getString (key), args);
    }
    catch (java.util.MissingResourceException e) {
      return "No resource for key " + e.getKey ();
    }
  }

  static String getResource (String key, Object arg)
  {
    return getResource (key, new Object[] {arg});
  }

  static String getResource (String key, int i)
  {
    return getResource (key, new Integer (i));
  }

  static String getResource (String key, int i, int j)
  {
    return getResource (key, new Object[] { new Integer(i), new Integer(j) });
  }

    static String getResource (String key, int i, int j, int k)
  {
    return getResource (key, new Object[] { new Integer(i),
                                            new Integer(j),
                                            new Integer(k) });
  }
}
