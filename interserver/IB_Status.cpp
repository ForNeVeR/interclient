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
#include "IB_Status.h"
#include "IB_SQLException.h"

#include <string.h>

IB_Status::IB_Status()
  : sqlWarnings_ (NULL)
{
  resetVector ();
}

IB_Status::~IB_Status()
{
  // deleting a null pointer is harmless.
  delete sqlWarnings_;
  sqlWarnings_ = NULL;
}

IB_SQLException*
IB_Status::getWarnings () const
{
  return sqlWarnings_;
}

void
IB_Status::clearWarnings ()
{
  // deleting a null pointer is harmless.
  delete sqlWarnings_;
  sqlWarnings_ = NULL;
}

void
IB_Status::setWarning (const IB_SQLException* warning)
{
  if (!sqlWarnings_)
    sqlWarnings_ = (IB_SQLException*) warning;
  else
    sqlWarnings_->setNextException (warning);
}

IB_SLONG32
IB_Status::getVendorCode ()
{
  return vector_[1];
}

ISC_STATUS*
IB_Status::vector() const
{
  return (ISC_STATUS*) vector_;
}

long
IB_Status::getSQLCode()
{
  return isc_sqlcode (vector_);
}

void
IB_Status::resetVector ()
{
  for (ISC_STATUS* p= vector_; p < vector_+vectorSize__; p++)
    *p = 0;
}

IB_BOOLEAN
IB_Status::isVectorSet () const
{
  return (vector_[0] == 1 && vector_[1] > 0);
}

IB_BOOLEAN
IB_Status::isErrorVector () const
{
  return isVectorSet ();
}

IB_BOOLEAN
IB_Status::isWarningVector () const
{
  return isVectorSet ();
}

char* 
IB_Status::getSQLErrorMessage ()
{
  if (!isVectorSet())
    return NULL;

  // !!! change signature for getSQLCode to return short
  // HP-UX port (old CC): added type cast (short)
  isc_sql_interprete ((short) getSQLCode(), errorMessage_, errorMessageSize__);
  return errorMessage_;
}

IB_LDString
IB_Status::getISCErrorMessage ()
{
  if (!isVectorSet())
    return NULL;

  ISC_STATUS* pVector = vector_;
  char* pErrorMessage = errorMessage_;
  int length = 0;
  int slen;

  // *** interbase bug, this segv if error message overflows an
  // *** internal interbase buffer "format[512]
  //isc_sql_interprete (getSQLCode(), pErrorMessage, errorMessageSize__);
  //slen = strlen (pErrorMessage);
  //length += slen + 1;
  //pErrorMessage += slen;
  //*pErrorMessage++ = '\n';

  while (isc_interprete (pErrorMessage, &pVector)) {
    slen = strlen (pErrorMessage);
    length += slen + 1;
    pErrorMessage += slen;
    *pErrorMessage++ = '\n';
  }
  return IB_LDString (length, errorMessage_);
}

#ifdef DEBUG
void
IB_Status::dump (ostream& errStream)
{
  if (!isVectorSet ())
    return;

  errStream << "InterBase Server Status..." << endl;
  errStream << "  SQLCODE = "   << getSQLCode() << endl;
  errStream << "  ISC Error Message: " << getISCErrorMessage ().string_ << endl;
  {
    ISC_STATUS* pvector = vector_;
    while (isc_interprete (errorMessage_, &pvector))
      errStream << "  ISC Status: " << errorMessage_ << endl;
  }
}
#endif

