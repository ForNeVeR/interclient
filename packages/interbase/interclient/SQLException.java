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

/**
 * Constructors are distinguished from subclass constructors
 * in that all subclass constructors start with either ErrorKey
 * or int errorKeyIndex.
 * 
 * @author Paul Ostler
 **/
class SQLException extends java.sql.SQLException
{
  // *** InterClient constructors (must have distinguished key) ****
  SQLException (String subClassName, ErrorKey errorKey, Object[] args)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey (), args) +
           Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  SQLException (String subClassName, ErrorKey errorKey, Object arg)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey (), new Object[] {arg}) +
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  SQLException (String subClassName, ErrorKey errorKey)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey ()) +
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  // *** InterServer constructors (must have distinguished key) ***
  SQLException (String subClassName, int errorKeyIndex, Object[] args)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex], args);
  }

  SQLException (String subClassName, int errorKeyIndex, Object arg)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex], arg);
  }

  SQLException (String subClassName, int errorKeyIndex)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex]);
  }

  // *** InterBase constructors ***
  // Key may be distinguished, or may be default engine error key.
  // If distinguished key is used, SQL State is extracted from key
  // if not null, but error code always comes over the wire.
  SQLException (String subClassName,
		  int errorKeyIndex, 
		  int errorCode, 
		  int ibSQLCode,
		  String ibErrorMessage)
  {
    super (Globals.getResource (ResourceKeys.ibOriginationIndicator) +
           Globals.getResource (ErrorKey.interserverErrorKeys__[errorKeyIndex].getResourceKey (),
                               new Object[] {ibErrorMessage}) + 
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           ErrorKey.interserverErrorKeys__[errorKeyIndex].getSQLState (errorCode, ibSQLCode),
           errorCode);
  }

  SQLException (int errorKeyIndex,
		  int errorCode, 
		  int ibSQLCode,
		  String ibErrorMessage)
  {
    super (Globals.getResource (ResourceKeys.ibOriginationIndicator) +
           Globals.getResource (ErrorKey.interserverErrorKeys__[errorKeyIndex].getResourceKey (),
                               new Object[] {ibErrorMessage}),
           ErrorKey.interserverErrorKeys__[errorKeyIndex].getSQLState (errorCode, ibSQLCode),
           errorCode);
  }
}
 
