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
//-*-C++-*-
#ifndef _VERSION_INFORMATION_H_
#define _VERSION_INFORMATION_H_
 
#ifdef BORLAND_RESOURCE_COMPILER

#ifdef ISCONFIG
#define APP_DESC			"InterServer Configuration Utility\0"
#define INTERNAL_NAME			"ISCONFIG\0"
#else
#define APP_DESC			"InterServer for Windows\0"
#define INTERNAL_NAME			"INTERSERVER\0"
#endif 

// CJL-IB6  changes made  1.51 ->> 2.0, 151 ->> 200
#define DRIVER_VERSION			"2.01\0"
#define PROD_NAME			"InterClient for Windows\0"
#define APP_VERSION			"2.01\0"
#define FILE_VER_NUMBER 		0, 201, 0 ,0
// CJL-IB6 end

#else // BORLAND_RESOURCE_COMPILER

class VersionInformation
{
 
public:

  static const char *const APPLICATION_NAME__;
  static const char *const NT_SERVICE_NAME__;
  static const char *const NT_SERVICE_FILE_ENTRY__;

  static const char *const DRIVER_NAME__;
  static const char *const DRIVER_VERSION__;
  static const int DRIVER_MAJOR_VERSION__;
  static const int DRIVER_MINOR_VERSION__;

  // Used internally by JIBCNet to verify that
  // remote network messages received from the server
  // are signed with the proper remote protocol signature.
  static const int REMOTE_PROTOCOL_VERSION__;
  static const int REMOTE_MESSAGE_CERTIFICATE__; // !!! rename to SERVER_MESSAGE_CERTIFICATE__

  static const char *const VersionInformation::SERVER_NAME__;
  static const int SERVER_MAJOR_VERSION__;
  static const int SERVER_MINOR_VERSION__;
  static const int SERVER_BUILD_NUMBER__;
  static const int SERVER_BUILD_LEVEL__;
  static const int SERVER_EXPIRATION_YEAR__;
  static const int SERVER_EXPIRATION_MONTH__;
  static const int SERVER_EXPIRATION_DAY__;
};

#endif // BORLAND_RESOURCE_COMPILER
 
#endif
