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
#include "VersionInformation.h"

const char *const VersionInformation::APPLICATION_NAME__ = "InterServer";
const char *const VersionInformation::NT_SERVICE_NAME__ = "InterServer";
const char *const VersionInformation::NT_SERVICE_FILE_ENTRY__ = "interserver";

const char *const VersionInformation::DRIVER_NAME__ = "InterClient";
const char *const VersionInformation::DRIVER_VERSION__ = "2.01"; // string
const int VersionInformation::DRIVER_MAJOR_VERSION__ = 2;
const int VersionInformation::DRIVER_MINOR_VERSION__ = 0;

const int VersionInformation::REMOTE_PROTOCOL_VERSION__ = 20001;
const int VersionInformation::REMOTE_MESSAGE_CERTIFICATE__ = 20001;

const char *const VersionInformation::SERVER_NAME__ = "InterServer";
const int VersionInformation::SERVER_MAJOR_VERSION__ = 2; // must be int
const int VersionInformation::SERVER_MINOR_VERSION__ = 0; // must be int
const int VersionInformation::SERVER_BUILD_NUMBER__ = 1; // int > 0
const int VersionInformation::SERVER_BUILD_LEVEL__ = 0; // 0 for test build, 1 for beta, and 2 for final
// The following expiration issues warning only...
const int VersionInformation::SERVER_EXPIRATION_YEAR__ = 105; // -1900
const int VersionInformation::SERVER_EXPIRATION_MONTH__ = 11; // 0=JAN
const int VersionInformation::SERVER_EXPIRATION_DAY__ = 30; 
