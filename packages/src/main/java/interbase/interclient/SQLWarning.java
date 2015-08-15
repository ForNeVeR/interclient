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
 * @author Paul Ostler
 **/
class SQLWarning extends java.sql.SQLWarning
{
  // *** InterClient constructors (must have distinguished key) ****
  SQLWarning (String subClassName, ErrorKey errorKey, Object[] args)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey (), args) +
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  SQLWarning (String subClassName, ErrorKey errorKey, Object arg)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey (), new Object[] {arg}) +
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  SQLWarning (String subClassName, ErrorKey errorKey)
  {
    super (Globals.getResource (ResourceKeys.driverOriginationIndicator) +
           Globals.getResource (errorKey.getResourceKey ()) +
	   Globals.getResource (ResourceKeys.seeApi) + subClassName,
           errorKey.getSQLState (),
           errorKey.getErrorCode ());
  }

  // *** InterServer constructors (must have distinguished key) ***
  SQLWarning (String subClassName, int errorKeyIndex, Object[] args)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex], args);
  }

  SQLWarning (String subClassName, int errorKeyIndex, Object arg)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex], arg);
  }

  SQLWarning (String subClassName, int errorKeyIndex)
  {
    this (subClassName, ErrorKey.interserverErrorKeys__[errorKeyIndex]);
  }
}
 
