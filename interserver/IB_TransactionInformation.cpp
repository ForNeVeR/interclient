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
#include "IB_TransactionInformation.h"
#include "IB_SQLException.h"
#include "IB_Transaction.h"
#include "IB_Status.h"

IB_TransactionInformation::IB_TransactionInformation (const IB_Status& status,
						      const IB_Transaction& transaction)
  : IB_Information (status),
    transaction_ ((IB_Transaction*) &transaction)
{ }

IB_TransactionInformation::~IB_TransactionInformation ()
{ }

void
IB_TransactionInformation::issueRequest ()
{
  if (isc_transaction_info (status_->vector(),
			    transaction_->trHandleP(),
			    request_.length_,
			    request_.buffer_,
			    requestedInfo_.size_,
			    requestedInfo_.buffer_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

void
IB_TransactionInformation::getTransactionId (int& transactionId) const
{
  transactionId = getInteger (findInfoItem (isc_info_tra_id));
}

