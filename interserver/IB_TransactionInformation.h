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
#ifndef _IB_TRANSACTION_INFORMATION_H_
#define _IB_TRANSACTION_INFORMATION_H_

#include "IB_Defines.h"
#include "IB_Information.h"

class IB_Transaction;
class IB_Status;

class IB_TransactionInformation: public IB_Information {

private:

  IB_Transaction* transaction_;

public:

  IB_TransactionInformation (const IB_Status& status, 
			     const IB_Transaction& transaction);

  ~IB_TransactionInformation ();

  void getTransactionId (int& transactionId) const;

private:
 
  virtual void issueRequest ();

};

#endif
