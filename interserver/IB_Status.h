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
#ifndef _IB_STATUS_H_
#define _IB_STATUS_H_

#ifdef DEBUG
#include <iostream.h>
#endif

#include "IB_Defines.h"
#include "IB_LDString.h"

// Status class is used for holding errors from the InterBase subsystem,
// and warnings from InterServer.
// Whereas a SQLException is thrown from the InterServer JIBS subsystem.
// So a SQLException will encapsulate errors from InterBase (maintained
// in a status object) and errors from JIBS as well.

class IB_SQLException;

class IB_Status
{

private:

  // !!! check into exact sizes to use here
  // Static constants
  enum {
    vectorSize__ = 20, 
    errorMessageSize__ = 1024
  };

  ISC_STATUS vector_[vectorSize__];

  char errorMessage_[errorMessageSize__];

private:
  // In addition to encapsulating server status vector,
  // this class contains a global sqlWarnings_ chain.
  // Both the status vector and warnings chain can be set
  // during the normal course of execution.
  // As when InterBase sets the status vector, setting a warning
  // does not throw an exception and execution continues.
  // SQL warnings are shared.
  // Deleted by destructor.
  IB_SQLException* sqlWarnings_;

public:

  // The first warning reported is returned.  
  // Note: Subsequent warnings will be chained to this SQLWarning.
  // A client Statement's execute methods clear its SQLWarning chain.
  // Unlike the client API, the server shouldn't implicitly delete
  // SQLWarning chains.
  // Return the first SQLWarning or null.
  // The SQL warnings are deleted by the destructor.
  IB_SQLException* getWarnings() const;

  // After this call getWarnings returns null until a new warning is reported.
  // If you don't clear warnings periodically, there is a potential for
  // incremental memory loss.
  void clearWarnings();

  // shared warning object is created externally somewhere in JIBS or JIBSRemote,
  // but they are deleted by IB_Status's destructor.
  // We could value copy the warning here, but this way, we don't have to 
  // worry about the user forgetting to clean up his allocated warnings.
  void setWarning (const IB_SQLException* warning);

public:

  IB_Status ();

  ~IB_Status ();

  // !!! guessing on the return type here, check how interbase returns it.
  IB_SLONG32 getSQLCode (); // The standard SQL code.

  IB_SLONG32 getVendorCode (); // An InterBase generated error code.

  // An InterBase generated SQL error message
  // !!! include string length as output if it can be extracted from status vector?
  IB_STRING getSQLErrorMessage (); 

  // An InterBase generated error message.
  // !!! include string length as output if it can be extracted from status vector?
  IB_LDString getISCErrorMessage ();

#ifdef DEBUG
  void  dump (ostream& errStream);
#endif

  // zero out status vector
  void resetVector ();

  IB_BOOLEAN isVectorSet () const;

  IB_BOOLEAN isErrorVector () const;   // !!! Need to distinguish

  // InterBase does not yet support warnings yet, except for SQL code 100
  IB_BOOLEAN isWarningVector () const; // !!! Need to distinguish

private:

  friend class IB_ConnectionInformation;
  friend class IB_TransactionInformation;
  friend class IB_Connection;
  friend class IB_Transaction;
  friend class IB_Statement;
  friend class IB_Blob;
  friend class IB_Array;
  friend class Session;

  ISC_STATUS* vector () const;

  // Objects of this class are not to be copied
  IB_Status (const IB_Status&);
  IB_Status& operator = (const IB_Status&);
};

#endif
