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
#ifndef _IB_SQL_EXCEPTION_H_
#define _IB_SQL_EXCEPTION_H_

// The IB_SQLException class provides information on a database access
// error.
//
// Each IB_SQLException provides several kinds of information: 
//       A string describing the error.  This is used as the Java Exception
//       message, and is available via the getMesage() method
//
//       A "SQLstate" string which follows the XOPEN SQLstate conventions.
//       The values of the SQLState string as described in the XOPEN SQL spec.
//
//       An integer error code that is vendor specific.  Normally this will
//	 be the actual error code returned by the underlying database.
//
//       A chain to a next Exception.  This can be used to provided additional
// 	 error information.

#include "IB_Defines.h"
#include "IB_LDString.h"

class IB_Status;

class IB_SQLException {

public:

  // WARNING - these values must match the indices of 
  // interclient's ErrorKey.interserverErrorKeys__ array.
  enum ErrorKey {
    engine__default_0__ = 0,
    bugCheck__0__ = 1,
    outOfMemory__ = 2,
    unsupportedCharacterSet__0__ = 3,
    driverNotCapable__extension_not_yet_implemented__ = 4,
    driverNotCapable__asynchronous_cancel__ = 5,
    driverNotCapable__isolation__ = 6,
    driverNotCapable__connection_timeout__ = 7,
    invalidArgument__connection_property__lock_resolution_mode__ = 9,
    invalidArgument__connection_property__isolation__ = 10, 
    invalidArgument__connection_property__unrecognized__ = 11,
    invalidOperation__execute_query_on_an_update_statement__ = 12, 
    remoteProtocol__unexpected_token_from_client__ = 14,
    communication__interserver__ = 15,
// CJL-IB6 added for SQL dialect support
	unsupportedSQLDialect__dialect_adjusted__ = 16
// CJL-IB6 end
  };

  // Exception codes
  enum ExceptionCode {
    communicationException__ = 1,
    invalidOperationException__ = 2,
    driverNotCapableException__ = 7,
    bugCheckException__ = 9,
    remoteProtocolException__ = 10,
    outOfMemoryException__ = 11,
    unsupportedCharacterSetException__ = 128,
    invalidArgumentException__ = 129,
// CJL-IB6 Added for Dialect support
    unsupportedSQLDialectException__ = 130
// CJL-IB6 end    
  };

  // Client side bug codes start at 100.
  // Server side bug codes start at 10,000.
  // *** This is actually not used in the code, but
  // *** is useful to the developer for assigning new codes,
  // *** just keep incrementing it.  
  // *** Avoid reusing holes!
  enum { 
    lastBugCodeUsed__ = 10024
  };

  // InterServer errorKey, with exception code
  IB_SQLException (const ErrorKey errorKey,
                   const ExceptionCode exceptionCode);

  // This will result in a subclass of SQLException
  IB_SQLException (const ErrorKey errorKey,
		   const int reservedCode,
                   const ExceptionCode exceptionCode);

  // This may or may not result in a subclassed exception,
  // it depends on the vendor code.
  IB_SQLException (const ErrorKey engineErrorKey, 
		   IB_Status* status);

  virtual ~IB_SQLException ();

  IB_SLONG32 getErrorKey () const;
  IB_SLONG32 getErrorCode () const;
  IB_SLONG32 getReservedCode () const;
  IB_SLONG32 getIBSQLCode () const;
  IB_LDString getIBErrorMessage () const;

  // Get the exception chained to this one. 
  //
  // return the next IB_SQLException in the chain
  IB_SQLException* getNextException () const;

  // Add an IB_SQLException to the end of the chain.
  // The input exception must exist on the heap even though
  // "this" exception may be on the stack.
  // sqlException is the new end of the IB_SQLException chain
  void setNextException (const IB_SQLException* sqlException);

  static void accumulate (IB_SQLException*& accumulatedExceptions,
			  IB_SQLException* nextException);

private:

  IB_SLONG32 errorKey_;
  IB_SLONG32 errorCode_;
  IB_SLONG32 reservedCode_;
  IB_SLONG32 ibSQLCode_;
  IB_LDString ibErrorMessage_;
  IB_SQLException* next_;

};

inline
void
IB_SQLException::accumulate (IB_SQLException*& accumulatedExceptions,
			     IB_SQLException* nextException)
{
  if (accumulatedExceptions)
    accumulatedExceptions->setNextException (nextException);
  else
    accumulatedExceptions = nextException;
}

#endif
