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
final class CommDiagListener implements java.awt.event.ActionListener
{
  private int actionId_;
  private CommDiagGUI gui_;

  CommDiagListener (int actionId,
                    CommDiagGUI gui)
  {
    actionId_ = actionId;
    gui_ = gui;
  }

  static final int TEST_BUTTON = 0;
  static final int EXIT_BUTTON = 1;

  public void actionPerformed (java.awt.event.ActionEvent event)
  {
    switch (actionId_) {
    case TEST_BUTTON:
      java.awt.Cursor waitCursor = new java.awt.Cursor (java.awt.Cursor.WAIT_CURSOR);
      java.awt.Cursor defaultCursor = new java.awt.Cursor (java.awt.Cursor.DEFAULT_CURSOR);
      gui_.frame_.setCursor (waitCursor);
      gui_.console_.setCursor (waitCursor);
      gui_.clearConsole ();
      gui_.writeln (CommDiag.getResource (ResourceKeys.pleaseWait));
      String display = gui_.app_.verifier_.verifyInstallation (gui_.getServer (),
				       gui_.getDatabase (),
				       gui_.getUser (),
				       gui_.getPassword (),
                                       gui_.getLoginTimeout (),
                                       false,  // gui_.getVerifyState ()
                                       false); // gui_.getFeaturesState ()
      gui_.clearConsole ();
      gui_.writeln (display);
      gui_.console_.setCursor (defaultCursor);
      gui_.frame_.setCursor (defaultCursor);
      break;

    case EXIT_BUTTON:
      if (gui_.isApplet_) {
        gui_.frame_.dispose ();
        gui_.app_.stop ();
        gui_.app_.destroy ();
      }
      else
        gui_.app_.exit ();
      break;
    }
  }

}
