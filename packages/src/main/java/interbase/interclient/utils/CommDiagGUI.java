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
 * @author Paul Ostler
 **/
class CommDiagGUI
{
  // !!! need to catch exit event (upper right x)
  
  final static private char echoChar__ = '*'; // used when entering passwords

  CommDiag app_;
  boolean isApplet_;

  java.awt.Frame frame_ = new java.awt.Frame (CommDiag.getResource (ResourceKeys.commDiagFrameTitle));
  java.awt.TextArea console_ = new java.awt.TextArea (35, 80);
  private java.awt.TextField server_ = new java.awt.TextField (50);
  private java.awt.TextField database_ = new java.awt.TextField (50);
  private java.awt.TextField user_ = new java.awt.TextField (50);
  private java.awt.TextField password_ = new java.awt.TextField (50);
  private java.awt.TextField loginTimeout_ = new java.awt.TextField (50);
  private java.awt.Button testButton_ = new java.awt.Button (CommDiag.getResource (ResourceKeys.testButtonText));
  private java.awt.Button exitButton_ = new java.awt.Button (CommDiag.getResource (ResourceKeys.exitButtonText));

  //private Checkbox verifyToggle_ = new Checkbox ("Verify Database Structures");
  //private Checkbox featuresToggle_ = new Checkbox ("Show Database Features");

  final static private java.awt.Font CONSOLE_FONT__ = new java.awt.Font ("Courier", java.awt.Font.PLAIN, 12);
  final static private java.awt.Font LABEL_FONT__ = new java.awt.Font ("Helvetica", java.awt.Font.ITALIC, 12);
  
  CommDiagGUI (CommDiag app, boolean isApplet) 
  {
    app_ = app;
    isApplet_ = isApplet;

    // ***************************************
    // *** First build and display the GUI ***
    // ***************************************

    frame_.setBackground (java.awt.Color.gray);
    console_.setBackground (java.awt.Color.lightGray);
    //verifyToggle_.setBackground (Color.yellow);
    //featuresToggle_.setBackground (Color.yellow);

    testButton_.addActionListener (
      new CommDiagListener (CommDiagListener.TEST_BUTTON, 
                            this));
    exitButton_.addActionListener (
      new CommDiagListener (CommDiagListener.EXIT_BUTTON, 
                            this));
    testButton_.setBackground (java.awt.Color.green);
    exitButton_.setBackground (java.awt.Color.red);

    password_.setEchoChar (echoChar__);
    console_.setEditable (false);
    console_.setFont (CONSOLE_FONT__);

    java.awt.Panel inputPanel = new java.awt.Panel ();
    inputPanel.setLayout (new java.awt.BorderLayout ());

    java.awt.Panel inputLabels = new java.awt.Panel ();
    inputLabels.setLayout (new java.awt.GridLayout (5, 1));
    inputLabels.add (new java.awt.Label (CommDiag.getResource (ResourceKeys.interBaseServerLabel)));
    inputLabels.add (new java.awt.Label (CommDiag.getResource (ResourceKeys.databaseFileLabel)));
    inputLabels.add (new java.awt.Label (CommDiag.getResource (ResourceKeys.userLabel)));
    inputLabels.add (new java.awt.Label (CommDiag.getResource (ResourceKeys.passwordLabel)));
    inputLabels.add (new java.awt.Label (CommDiag.getResource (ResourceKeys.timeoutLabel)));

    java.awt.Panel inputFields = new java.awt.Panel ();
    inputFields.setLayout (new java.awt.GridLayout (5, 1));
    inputFields.add (server_);    
    inputFields.add (database_);
    inputFields.add (user_);
    inputFields.add (password_);
    inputFields.add (loginTimeout_);

    java.awt.Panel buttonPanel = new java.awt.Panel ();
    buttonPanel.setLayout (new java.awt.FlowLayout (java.awt.FlowLayout.CENTER));
    buttonPanel.add (testButton_);
    buttonPanel.add (exitButton_);
    //buttonPanel.add (verifyToggle_);
    //buttonPanel.add (featuresToggle_);

    inputPanel.add (java.awt.BorderLayout.WEST, inputLabels);
    inputPanel.add (java.awt.BorderLayout.CENTER, inputFields);
    inputPanel.add (java.awt.BorderLayout.SOUTH, buttonPanel);

    java.awt.Panel labelPanel = new java.awt.Panel ();
    labelPanel.setLayout (new java.awt.BorderLayout ());
    labelPanel.setForeground (java.awt.Color.blue);
    
    java.awt.Label newsgroupLabel = new java.awt.Label (CommDiag.getResource (ResourceKeys.visitNewsgroupLabel),
							java.awt.Label.CENTER);
    java.awt.Label icsupportLabel = new java.awt.Label (CommDiag.getResource (ResourceKeys.mailBugsLabel),
							java.awt.Label.CENTER);
    icsupportLabel.setFont (LABEL_FONT__); 
    newsgroupLabel.setFont (LABEL_FONT__); 

    labelPanel.add (java.awt.BorderLayout.NORTH, newsgroupLabel);
    labelPanel.add (java.awt.BorderLayout.SOUTH, icsupportLabel);

    java.awt.Panel upperPanel = new java.awt.Panel ();
    upperPanel.setLayout (new java.awt.BorderLayout ());
    upperPanel.add (java.awt.BorderLayout.NORTH, inputPanel);
    upperPanel.add (java.awt.BorderLayout.SOUTH, labelPanel);

    frame_.add (java.awt.BorderLayout.NORTH, upperPanel);
    frame_.add (java.awt.BorderLayout.CENTER, console_);

    frame_.pack ();
    frame_.show ();

    // ************************************
    // *** Ok, now that the GUIs built, ***
    // *** load the driver and test it  ***
    // ************************************

    writeln (app_.verifier_.loadDriver ());
  }

  synchronized void write (String s)
  {
    console_.append (s);
  }

  synchronized void writeln (String s)
  {
    console_.append (s);
    console_.append ("\n");
  }

  synchronized void clearConsole ()
  {
    console_.setText ("");
  }

  String getUser ()
  {
    return user_.getText ();
  }

  String getPassword ()
  {
    return password_.getText ();
  }

  String getDatabase ()
  {
    return database_.getText ();
  }

  String getServer ()
  {
    return server_.getText ();
  }

  int getLoginTimeout ()
  {
    String loginTimeout = loginTimeout_.getText ();
    if ("".equals (loginTimeout) || loginTimeout == null) 
      return 0;
    else
      try {
        return Integer.parseInt (loginTimeout);
      }
    catch (NumberFormatException e) {
      loginTimeout_.setText ("");
      return 0;
    }
  }

  //boolean getVerifyState ()
  //{
  //   return verifyToggle_.getState ();
  //}

  //boolean getFeaturesState ()
  // {
  //  return featuresToggle_.getState ();
  // }

}


