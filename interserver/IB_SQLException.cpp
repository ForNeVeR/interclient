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
#include "IB_SQLException.h"
#include "IB_Status.h"

IB_SQLException::IB_SQLException (const ErrorKey errorKey,
                                  const ExceptionCode exceptionCode)
  : errorKey_ (errorKey),
    reservedCode_ (0),
    errorCode_ (exceptionCode),
    ibSQLCode_ (0),
    ibErrorMessage_ (1, (IB_STRING) "X"), // !!!need to make sure we can handle zero length strings
    next_ (NULL)
{ }

IB_SQLException::IB_SQLException (const ErrorKey errorKey,
				  const int reservedCode,
                                  const ExceptionCode exceptionCode)
  : errorKey_ (errorKey),
    reservedCode_ (reservedCode),
    errorCode_ (exceptionCode),
    ibSQLCode_ (0),
    ibErrorMessage_ (1, (IB_STRING) "X"), // !!!need to make sure we can handle zero length strings
    next_ (NULL)
{ }

IB_SQLException::IB_SQLException (const ErrorKey engineErrorKey,
				  IB_Status* status)
  : errorKey_ (engineErrorKey),
    reservedCode_ (0),
    errorCode_ (status->getVendorCode ()),
    ibSQLCode_ (status->getSQLCode ()),
    ibErrorMessage_ (status->getISCErrorMessage ()),
    next_ (NULL)
{
  status->resetVector ();
}

IB_SQLException::~IB_SQLException ()
{ 
  if (next_)
    delete next_;
}

IB_SLONG32
IB_SQLException::getErrorKey () const
{
  return errorKey_;
}

IB_SLONG32
IB_SQLException::getErrorCode () const
{
  return errorCode_;
}

IB_SLONG32
IB_SQLException::getReservedCode () const
{
  return reservedCode_;
}

IB_SLONG32
IB_SQLException::getIBSQLCode () const
{
  return ibSQLCode_;
}

IB_LDString
IB_SQLException::getIBErrorMessage () const
{
  return ibErrorMessage_;
}

IB_SQLException*
IB_SQLException::getNextException () const
{
  return next_;
}

void
IB_SQLException::setNextException (const IB_SQLException* sqlException)
{
  next_ = (IB_SQLException*) sqlException;
}

