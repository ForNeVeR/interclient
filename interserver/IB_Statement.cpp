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
#include <stdlib.h> // malloc, realloc, free
#include <math.h> // pow()
#include <string.h> // strncpy()

#include "IB_Connection.h"
#include "IB_Transaction.h"
#include "IB_Statement.h"
#include "IB_Status.h"
#include "IB_ResultSet.h"
#include "IB_InputBuffer.h"
#include "IB_Information.h"
#include "IB_Blob.h"
#include "IB_Array.h"

// must be exactly 18 characters for strncpy to work properly.
const char *const
IB_Statement::CALL_PREFIX_SELECT__  = "select * from     ";

const char *const
IB_Statement::CALL_PREFIX_EXECUTE__ = "execute procedure ";

const IB_BUFF_CHAR 
IB_Statement::stmtTypeInfoRequest__[] = { isc_info_sql_stmt_type };

const IB_BUFF_CHAR 
IB_Statement::rowCountsInfoRequest__[] = { isc_info_sql_records };

IB_Statement::IB_Statement (const IB_Status& status)
  : stmtHandle_ (NULL),
    connection_ (NULL),
    transaction_ (NULL),
    resultSet_ (NULL),
    inputBuffer_ (NULL),
    sqldaOut_ (NULL),
    sqldaIn_ (NULL),
    status_ ((IB_Status*) &status),
    cursorName_ (NULL),
    prepared_ (IB_FALSE),
#ifdef IB_USER_API
    sqlString_ (NULL),
#endif
    queryTimeout_ (0),
    openBlobs_ (),
    openArrays_ (),
    stmtType_ (0)
{
#ifdef TRACEON
  debugTraceAnInt ("constructing statement: ", (IB_REF) this);
#endif
 }

IB_Statement::~IB_Statement ()
{
#ifdef TRACEON
  debugTraceAnInt ("destroying statement: ", (IB_REF) this);
#endif
  close ();
  delete resultSet_;
  delete inputBuffer_;
  free (sqldaOut_);
  free (sqldaIn_);
}

void 
IB_Statement::setConnection (const IB_Connection& connection)
{
  if (stmtHandle_) // Attempt to set the connection context for an open statement
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10004,
			       IB_SQLException::bugCheckException__);

  connection_ = (IB_Connection*) &connection;
}

void
IB_Statement::setTransaction (const IB_Transaction& transaction)
{
  if (resultSet_)
    resultSet_->close ();

  transaction_ = (IB_Transaction*) &transaction;
}

void
IB_Statement::setStatus (const IB_Status& status)
{
  status_ = (IB_Status*) &status;
}

void
IB_Statement::open ()
{
  if (stmtHandle_) // statement is already open
    throw new IB_SQLException (IB_SQLException::bugCheck__0__, 
			       10005,
			       IB_SQLException::bugCheckException__);

  if (!connection_ || !(connection_->dbHandleP()))// no open connection context has been set
    throw new IB_SQLException (IB_SQLException::bugCheck__0__, 
			       10006,
			       IB_SQLException::bugCheckException__);

  if (isc_dsql_allocate_statement (status_->vector(), 
				   connection_->dbHandleP(),
				   &stmtHandle_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  prepared_ = IB_FALSE;

  connection_->addOpenStatement (*this);
}

#ifdef IB_USER_API
void
IB_Statement::executeImmediate(const IB_STRING sqlString)
{
  if (stmtHandle_) // execute-immediate an open statement
    throw new IB_SQLException (IB_SQLException::bugCheck__0__, 
			       10007,
			       IB_SQLException::bugCheckException__);

  if (isc_dsql_execute_immediate (status_->vector(), 
				  connection_->dbHandleP(),
				  transaction_->trHandleP(),
				  0,
				  sqlString,
// CJL-IB6 add SQLDialect support, and obsolete sqldaVersion
				  connection_->attachmentSQLDialect_,
//			sqldaVersion__,
// CJL-IB6 end
				  NULL))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}
#endif

void
IB_Statement::prepareNoInputNoOutput (const IB_STRING sqlString)
{
  if (!stmtHandle_)
    open ();

  if (isc_dsql_prepare (status_->vector(),
			transaction_->trHandleP(),
			&stmtHandle_,
			0,
			sqlString,
// CJL-IB6 add SQLDialect support, and obsolete sqldaVersion
			connection_->attachmentSQLDialect_,
//			sqldaVersion__,
// CJL-IB6 end
			NULL))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  prepared_ = IB_TRUE;

  stmtType_ = statementType ();

  // Its harmless to delete a NULL pointer.
  delete resultSet_;
  resultSet_ = NULL;
}

void
IB_Statement::prepareNoOutput (const IB_STRING sqlString,
			       const IB_SSHORT16 suggestedInputCols)
{
  prepareNoInputNoOutput (sqlString);
  describeInput (suggestedInputCols);
}

IB_ResultSet*
IB_Statement::prepareNoInput (const IB_STRING sqlString,
			      const IB_SSHORT16 suggestedResultCols,
			      const IB_SSHORT16 maxFieldSize)
{
  if (!stmtHandle_)
    open ();

  // If sqldaOut_ is already allocated, reuse it.
  // Otherwise, allocate space for a suggestedResultCols item select list.
  // If more space is needed, allocate more later.
  if (!sqldaOut_)
    sqldaOut_ = (XSQLDA *) malloc (XSQLDA_LENGTH(suggestedResultCols));
  if (!sqldaOut_)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
			       IB_SQLException::outOfMemoryException__);
  sqldaOut_->version = sqldaVersion__;
  sqldaOut_->sqln = suggestedResultCols;


  if (isc_dsql_prepare (status_->vector(),
			transaction_->trHandleP(),
			&stmtHandle_,
			0,
			sqlString,
// CJL-IB6 add SQLDialect support, and obsolete sqldaVersion
            connection_->attachmentSQLDialect_,
//			sqldaVersion__,
// CJL-IB6 end
			sqldaOut_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  prepared_ = IB_TRUE;

  IB_SSHORT16 actualResultCols = sqldaOut_->sqld;

  stmtType_ = statementType ();

  // If its a non-select, just return.
  if (!actualResultCols) {
    return NULL;
  }

  // Check that actual number of result cols is not greater than allocated suggestedInputCols.
  // Realloc if necessary.
  if (sqldaOut_->sqln < actualResultCols) {
    sqldaOut_ = (XSQLDA *) realloc (sqldaOut_, 
				    XSQLDA_LENGTH (actualResultCols));
    sqldaOut_->version = sqldaVersion__;
    sqldaOut_->sqln = actualResultCols;
  }

  // !!! MOVE THIS UP INSIDE IF STATEMENT
  // Describe the output sqlvars
  if (isc_dsql_describe (status_->vector(),
			 &stmtHandle_,
// CJL-IB6 add SQLDialect support, and obsolete sqldaVersion
              connection_->attachmentSQLDialect_,
//			sqldaVersion__,
// CJL-IB6 end
			 sqldaOut_)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  // Its harmless to delete a NULL pointer.
  delete resultSet_;

  // Must always realloc since record size may change.
  // !!! future enhancement - make this a realloc
  resultSet_ = new IB_ResultSet (this, maxFieldSize);

  return resultSet_;
}

IB_ResultSet* 
IB_Statement::prepare (const IB_STRING sqlString,
		       const IB_SSHORT16 suggestedResultCols,
		       const IB_SSHORT16 suggestedInputCols,
		       const IB_SSHORT16 maxFieldSize) 
{
  IB_ResultSet* result;
  result = prepareNoInput (sqlString, suggestedResultCols, maxFieldSize);
  describeInput (suggestedInputCols);
  return result;
}

IB_ResultSet* 
IB_Statement::prepareCall (IB_STRING sqlString,
			   const IB_SSHORT16 suggestedResultCols,
			   const IB_SSHORT16 suggestedInputCols,
			   const IB_SSHORT16 maxFieldSize) 
{
  strncpy (sqlString, CALL_PREFIX_SELECT__, 18);
  try {
    return prepare (sqlString);
  }
  catch (IB_SQLException* e) {
    if (e->getErrorCode () == isc_dsql_error) { // procedure does not return any values
      strncpy (sqlString, CALL_PREFIX_EXECUTE__, 18);
      return prepare (sqlString);
    }
    else 
      throw e;
  }
}

void
IB_Statement::describeInput (const IB_SSHORT16 suggestedInputCols)
{
  // If sqldaIn_ is already allocated, reuse it.
  // Otherwise, allocate space for a suggestedInputCols parameter list.
  // If more space is needed, allocate more later.
  if (!sqldaIn_)
    sqldaIn_ = (XSQLDA *) malloc (XSQLDA_LENGTH (suggestedInputCols));
  if (!sqldaIn_)
    throw new IB_SQLException (IB_SQLException::outOfMemory__,
			       IB_SQLException::outOfMemoryException__);
  sqldaIn_->sqln = suggestedInputCols;
  sqldaIn_->version = sqldaVersion__;
    
  dsqlDescribeBind ();

  IB_SSHORT16 actualInputCols = sqldaIn_->sqld;
  if (actualInputCols > sqldaIn_->sqln) {
    sqldaIn_ = (XSQLDA *) realloc (sqldaIn_, 
				   XSQLDA_LENGTH (actualInputCols));
    sqldaIn_->version = sqldaVersion__;
    sqldaIn_->sqln = actualInputCols;
    dsqlDescribeBind ();
  }

  // Its harmless to delete a NULL pointer.
  // !!! future enhancement - make this a realloc
  delete inputBuffer_;
  inputBuffer_ = new IB_InputBuffer (this);
  
  // !!! do we really need to call clearParameters() here?
  clearParameters ();
}

void
IB_Statement::executeUpdate ()
{
  if (!prepared_) 
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10008,
			       IB_SQLException::bugCheckException__);
  if (isQuery())
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10009,
			       IB_SQLException::bugCheckException__);

  dsqlExecute ();
}

int
IB_Statement::isDDL ()
{
  return (stmtType_ == isc_info_sql_stmt_ddl);
}

IB_SLONG32
IB_Statement::getUpdateCount ()
{
  int countType = 0;

  // Get the statement type count indicator as it will appear
  // in the requested info buffer.
  switch (stmtType_) {
  case isc_info_sql_stmt_update:
    countType = isc_info_req_update_count;
    break;
  case isc_info_sql_stmt_delete:
    countType = isc_info_req_delete_count;
    break;
  case isc_info_sql_stmt_insert:
    countType = isc_info_req_insert_count;
    break;
  case isc_info_sql_stmt_select:
    countType = isc_info_req_select_count;
    break;
  default:
    return 0;
  }

  if (isc_dsql_sql_info (status_->vector(),
			 &stmtHandle_,
			 sizeof (rowCountsInfoRequest__),
			 (IB_BUFF_PTR) rowCountsInfoRequest__,
			 rowCountsRequestedInfoSize__,
			 rowCountsRequestedInfo_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  int rowCountLength;
  int countTypeIndicator=0;
  IB_SLONG32 count = 0;
  // skip over isc_info_sql_records, and overall 2 byte length field  
  IB_BUFF_PTR iterator = rowCountsRequestedInfo_ + 3; 
  while (*iterator != isc_info_end) {
    countTypeIndicator = *iterator++;
    // !!! should call IB_Information::getInteger() here.
    // HP-UX port (old CC): added type cast (int)
    rowCountLength = (int) isc_vax_integer (iterator, 2);
    iterator += 2; // skip over rowCountLength
    count = isc_vax_integer (iterator, rowCountLength);
    iterator += rowCountLength; // skip over rowCount
    if (countTypeIndicator == countType)  // found it
      break;
  }
  return count;
}

int
IB_Statement::getStatementType ()
{
  return stmtType_;
}

int
IB_Statement::statementType()
{
  if (isc_dsql_sql_info (status_->vector(),
			 &stmtHandle_,
			 sizeof (stmtTypeInfoRequest__),
			 (IB_BUFF_PTR) stmtTypeInfoRequest__,
			 stmtTypeRequestedInfoSize__,
			 stmtTypeRequestedInfo_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  return IB_Information::getInteger (stmtTypeRequestedInfo_ + 1);
}

void
IB_Statement::close ()
{
#ifdef TRACEON
  debugTraceAnInt ("closing statement: ", (IB_REF) this);
#endif
  if (!stmtHandle_)
    return;

  // Remove all blobs from open blobs list and collect memory.
  openBlobs_.removeAllElements ();
  openBlobs_.trimToSize ();
  openArrays_.removeAllElements ();
  openArrays_.trimToSize ();

  connection_->remOpenStatement (*this);

  if (resultSet_ != NULL)
    resultSet_->close ();

  dsqlDropStatement ();

  stmtHandle_ = NULL;

  prepared_ = IB_FALSE;
}

IB_SLONG32
IB_Statement::getQueryTimeout () const 
{
  return queryTimeout_;
}

void
IB_Statement::setQueryTimeout (const IB_SLONG32 seconds)
{
  queryTimeout_ = seconds;
}

void
IB_Statement::cancel ()
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__asynchronous_cancel__,
			     IB_SQLException::driverNotCapableException__);
}

void
IB_Statement::setCursorName (const IB_STRING cursorName)
{
#ifdef IB_USER_API
  if (!cursorName || (strlen (cursorName) == 0))
    throw new IB_SQLException ("Attempt to set cursor name to empty or null string");
#endif

  if (isc_dsql_set_cursor_name (status_->vector(), 
  			&stmtHandle_, 
  			cursorName, 
  			NULL))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);

  cursorName_ = cursorName;
}

IB_STRING
IB_Statement::getCursorName () const
{
  return cursorName_;
}

IB_ResultSet*
IB_Statement::getResultSet ()
{
  return resultSet_;
}

IB_BOOLEAN
IB_Statement::isQuery () const
{
  if (!stmtHandle_) // unprepared statement
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10010,
			       IB_SQLException::bugCheckException__);

  return (sqldaOut_ && (sqldaOut_->sqld > 0));
}

IB_BOOLEAN
IB_Statement::isParameterized () const
{
  if (!stmtHandle_) // unprepared statement
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10011,
			       IB_SQLException::bugCheckException__);

  return (sqldaIn_ && (sqldaIn_->sqld > 0));
}

IB_SSHORT16
IB_Statement::getParameterCount () const
{
  if (!sqldaIn_) // unprepared statement
    throw new IB_SQLException (IB_SQLException::bugCheck__0__,
			       10012,
			       IB_SQLException::bugCheckException__);
  
  return sqldaIn_->sqld;
}

IB_BOOLEAN 
IB_Statement::isParameterNullable (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::isTypeTokenNullable ((sqldaIn_->sqlvar+parameterIndex)->sqltype);
}

IB_Types::IBType 
IB_Statement::getParameterType (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::getIBType (sqldaIn_->sqlvar+parameterIndex);
}

#ifdef IB_USER_API
IB_LDString
IB_Statement::getParameterTypeName (const IB_SSHORT16 parameterIndex) const 
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::getIBTypeName (sqldaIn_->sqlvar+parameterIndex);
}
#endif

IB_SSHORT16 
IB_Statement::getParameterPrecision (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::getPrecision (sqldaIn_->sqlvar+parameterIndex);
}

IB_SSHORT16 
IB_Statement::getParameterScale (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::getScale (sqldaIn_->sqlvar+parameterIndex);
}

IB_SSHORT16 
IB_Statement::getParameterSubType (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  return IB_Types::getSubType (sqldaIn_->sqlvar+parameterIndex);
}

IB_SSHORT16 
IB_Statement::getParameterCharSet (const IB_SSHORT16 parameterIndex) const
{
  inputBuffer_->validateIndex (parameterIndex);
  // !!! check for blob
  return IB_Types::getSubType (sqldaIn_->sqlvar+parameterIndex);
}

IB_SSHORT16 
IB_Statement::getParameterCharLength (const IB_SSHORT16 parameterIndex) const
{
  // !!! check for blob?
  return (inputBuffer_->getCharLength (parameterIndex));
}

void 
IB_Statement::setNull (const IB_SSHORT16 parameterIndex, const IB_BOOLEAN isNull)
{
  inputBuffer_->validateIndex (parameterIndex);
  if (isNull)
    *(sqldaIn_->sqlvar+parameterIndex)->sqlind = -1;
  else 
    *(sqldaIn_->sqlvar+parameterIndex)->sqlind = 0;
}

void 
IB_Statement::setSmallInt (const IB_SSHORT16 parameterIndex,
			   const IB_SSHORT16 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SSHORT16*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}

void 
IB_Statement::setInteger (const IB_SSHORT16 parameterIndex,
			  const IB_SLONG32 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SLONG32*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}

void 
IB_Statement::setFloat (const IB_SSHORT16 parameterIndex,
			const IB_FLOAT32 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_FLOAT32*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}

void 
IB_Statement::setDouble (const IB_SSHORT16 parameterIndex,
			 const IB_DOUBLE64 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_DOUBLE64*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}

/*
void 
IB_Statement::setNumericDouble (const IB_SSHORT16 parameterIndex,
				const IB_DOUBLE64 scaled,
				const IB_SSHORT16 scale)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_DOUBLE64*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = scaled/pow (10, scale);
  // InterBase is strange in that it seems to ignore
  // the scale of numeric doubles,
  // you must provide the value as input.
  // (sqldaIn_->sqlvar+parameterIndex)->sqlscale = -scale;
}
*/

void 
IB_Statement::setNumericDouble (const IB_SSHORT16 parameterIndex,
				const IB_DOUBLE64 value,
				const IB_SSHORT16 scale)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_DOUBLE64*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
  // InterBase is strange in that it seems to ignore
  // the scale of numeric doubles,
  // you must provide the value as input.
  // (sqldaIn_->sqlvar+parameterIndex)->sqlscale = -scale;
}

void 
IB_Statement::setNumericInteger (const IB_SSHORT16 parameterIndex,
				 const IB_SLONG32 scaled, 
				 const IB_SSHORT16 scale)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SLONG32*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = scaled;
  (sqldaIn_->sqlvar+parameterIndex)->sqlscale = -scale;
}

void 
IB_Statement::setNumericSmallInt (const IB_SSHORT16 parameterIndex,
				  const IB_SSHORT16 scaled, 
				  const IB_SSHORT16 scale)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SSHORT16*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = scaled;
  (sqldaIn_->sqlvar+parameterIndex)->sqlscale = -scale;
}

void 
IB_Statement::setChar (const IB_SSHORT16 parameterIndex,
			  const IB_SSHORT16 stringLen,
			  const IB_STRING string)
{
  inputBuffer_->validateIndex (parameterIndex);
  charCpy ((sqldaIn_->sqlvar+parameterIndex)->sqldata,
	   (sqldaIn_->sqlvar+parameterIndex)->sqllen,
	   stringLen,
	   string);
}

void 
IB_Statement::setChar (const IB_SSHORT16 parameterIndex,
		       const IB_LDString ldstring)
{
  inputBuffer_->validateIndex (parameterIndex);
  charCpy ((sqldaIn_->sqlvar+parameterIndex)->sqldata,
	   (sqldaIn_->sqlvar+parameterIndex)->sqllen,
	   ldstring.length_,
	   ldstring.string_);
}

void 
IB_Statement::setVarChar (const IB_SSHORT16 parameterIndex,
			  const IB_LDSTRING ldString)
{
  inputBuffer_->validateIndex (parameterIndex);
  // !!! no need to coerce sqllen here
  // !!! what if length of string exceeds sqllen? check for this on client
  //(sqldaIn_->sqlvar+parameterIndex)->sqllen = 
  //		(*(IB_SSHORT16 *)ldString) + sizeof (IB_SSHORT16);
  varcharCpy ((sqldaIn_->sqlvar+parameterIndex)->sqldata,
	      *(IB_SSHORT16 *) ldString,
	      ldString + sizeof(IB_SSHORT16));
}

void 
IB_Statement::setVarChar (const IB_SSHORT16 parameterIndex,
			  const IB_LDString ldString)
{
  inputBuffer_->validateIndex (parameterIndex);

  //(sqldaIn_->sqlvar+parameterIndex)->sqllen = 
  //		ldString.length_ + sizeof (IB_SSHORT16);
  varcharCpy ((sqldaIn_->sqlvar+parameterIndex)->sqldata,
	      ldString.length_,
	      ldString.string_);
}

void 
IB_Statement::setAsciiBlob (const IB_SSHORT16 parameterIndex,
			    const IB_BLOBID *blobId)
{
  inputBuffer_->validateIndex (parameterIndex);
  (sqldaIn_->sqlvar+parameterIndex)->sqldata = (char *)blobId;
  (sqldaIn_->sqlvar+parameterIndex)->sqllen = sizeof (ISC_QUAD);
}

void 
IB_Statement::setAsciiBlob (const IB_SSHORT16 parameterIndex,
			    const IB_LDString string)
{
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void 
IB_Statement::setBinaryBlob (const IB_SSHORT16 parameterIndex,
			     const IB_BLOBID *blobId)
{
  inputBuffer_->validateIndex (parameterIndex);
  (sqldaIn_->sqlvar+parameterIndex)->sqldata = (char *)blobId;
  (sqldaIn_->sqlvar+parameterIndex)->sqllen = sizeof (ISC_QUAD);
}

void 
IB_Statement::setBinaryBlob (const IB_SSHORT16 parameterIndex,
			     const IB_SSHORT16 numBytes,
			     const IB_STRING bytes)
{
  inputBuffer_->validateIndex (parameterIndex);
  throw new IB_SQLException (IB_SQLException::driverNotCapable__extension_not_yet_implemented__,
			     IB_SQLException::driverNotCapableException__);
}

void 
IB_Statement::setUnicodeBlob (const IB_SSHORT16 parameterIndex,
			      const IB_BLOBID *blobId)
{
  inputBuffer_->validateIndex (parameterIndex);
  (sqldaIn_->sqlvar+parameterIndex)->sqldata = (char *)blobId;
  // !!! don't need to set sqllen, describe_bind does this
  (sqldaIn_->sqlvar+parameterIndex)->sqllen = sizeof (ISC_QUAD);
}

void 
IB_Statement::setUnicodeBlob (const IB_SSHORT16 parameterIndex,
			      const IB_STRING bytes)
{
}

void 
IB_Statement::setDate (const IB_SSHORT16 parameterIndex,
		       const IB_TIMESTAMP timestamp)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(ISC_QUAD*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = (ISC_QUAD) timestamp;
}

void 
IB_Statement::setArray (const IB_SSHORT16 parameterIndex,
			const IB_ARRAYID arrayId)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(ISC_QUAD*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = (ISC_QUAD) arrayId;
}

// CJL-IB6 added for new type support
void 
IB_Statement::setNumericInt64 (const IB_SSHORT16 parameterIndex,
				 const IB_SINT64 scaled, 
				 const IB_SSHORT16 scale)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SINT64*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = scaled;
  (sqldaIn_->sqlvar+parameterIndex)->sqlscale = -scale;
}

void 
IB_Statement::setSQLDate (const IB_SSHORT16 parameterIndex,
			  const IB_SLONG32 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_SLONG32*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}

void 
IB_Statement::setTime (const IB_SSHORT16 parameterIndex,
			  const IB_ULONG32 value)
{
  inputBuffer_->validateIndex (parameterIndex);
  *(IB_ULONG32*) (sqldaIn_->sqlvar+parameterIndex)->sqldata = value;
}
// CJL-IB6 end changes

void 
IB_Statement::clearParameters () 
{
  IB_BUFF_PTR iterator;
  IB_BUFF_PTR endOfBufferPtr = inputBuffer_->buffer_ + inputBuffer_->recordSize_;

  // !!! probably a quicker way to zero out buffer in C
  for (iterator = inputBuffer_->buffer_;
       iterator < endOfBufferPtr;
       iterator++)
    *iterator = 0;
}

IB_BOOLEAN
IB_Statement::operator== (const IB_Statement& statement) const
{
  return (this == &statement);
}

void 
IB_Statement::charCpy (const IB_BUFF_PTR buffer,
		       const IB_SSHORT16 paddedStringLength,
		       const IB_SSHORT16 stringLen,
		       const IB_STRING string)
{
  IB_BUFF_PTR bufferIterator = buffer;
  IB_BUFF_PTR endOfStringPtr = buffer + stringLen;
  IB_BUFF_PTR endOfBufferPtr = buffer + paddedStringLength;
  IB_BUFF_PTR stringIterator = string;

  while (bufferIterator < endOfStringPtr)
    *bufferIterator++ = *stringIterator++;
  
  while (bufferIterator < endOfBufferPtr)
    *bufferIterator++ = ' ';
}

void 
IB_Statement::varcharCpy (const IB_BUFF_PTR buffer,
			  const IB_SSHORT16 stringLength,
			  const IB_STRING string)
{
  IB_BUFF_PTR bufferIterator = buffer;
  IB_BUFF_PTR endOfStringPtr;
  IB_BUFF_PTR stringIterator = string;

  *(IB_SSHORT16 *) bufferIterator = stringLength;
  bufferIterator += sizeof (IB_SSHORT16);
  endOfStringPtr = bufferIterator + stringLength;
  while (bufferIterator < endOfStringPtr)
    *bufferIterator++ = *stringIterator++;
}


// Called by IB_ResultSet::singletonFetch()
void
IB_Statement::dsqlSingletonFetch ()
{
  // No isc_dsql_fetch call is required since sqldaOut_ is passed to isc_dsql_execute2
  if (isc_dsql_execute2 (status_->vector(), 
			 transaction_->trHandleP(), 
			 &stmtHandle_, 
// CJL-IB6 add support for SQLDialect, obsolete sqldaVersion__
			 connection_->attachmentSQLDialect_, 
//			 sqldaVersion__, 
// CJL-IB6 end
			 sqldaIn_,
			 sqldaOut_)) 
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

// Called by executeUpdate() and IB_ResultSet::open()
void
IB_Statement::dsqlExecute ()
{
  if (isc_dsql_execute (status_->vector(), 
			transaction_->trHandleP(),
			&stmtHandle_,
// CJL-IB6 add support for SQLDialect, obsolete sqldaVersion__
			 connection_->attachmentSQLDialect_, 
//			 sqldaVersion__, 
// CJL-IB6 end
			sqldaIn_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

// Called by IB_ResultSet::next()
IB_BOOLEAN
IB_Statement::dsqlFetch ()
{
  ISC_STATUS returnCode;
  returnCode = isc_dsql_fetch (status_->vector(), 
			       &stmtHandle_, 
// CJL-IB6 add support for SQLDialect, obsolete sqldaVersion__
				   connection_->attachmentSQLDialect_, 
//			 sqldaVersion__, 
// CJL-IB6 end
			       sqldaOut_);
  if (returnCode == 100L)
    return 0;
  else if (returnCode == 0)
    return 1;

  throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
  return 0; // just to please compiler
}

// Called by IB_ResultSet::close()
void
IB_Statement::dsqlCloseCursor ()
{
  if (isc_dsql_free_statement (status_->vector(), 
			       &stmtHandle_, 
			       DSQL_close))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

#ifdef IB_USER_API
IB_BOOLEAN
IB_Statement::dsqlFetchRelative (const IB_BOOLEAN previous,
				 const IB_SLONG32 offset)
{
  //  return (!isc_dsql_fetch2 (status_->vector(), 
  //			    &stmtHandle_, 
  //			    connection_->attachmentSQLDialect_, 
  //			    sqldaOut_,
  //			    (IB_USHORT16) previous,
  //			    offset));
  return IB_FALSE;
}
#endif

// Called by close()
void
IB_Statement::dsqlDropStatement ()
{
  if (isc_dsql_free_statement (status_->vector(), 
			       &stmtHandle_, 
			       DSQL_drop))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}

// Called by describeInput()
void
IB_Statement::dsqlDescribeBind ()
{
  if (isc_dsql_describe_bind (status_->vector(),
			      &stmtHandle_,
// CJL-IB6 add support for SQLDialect, obsolete sqldaVersion__
				   connection_->attachmentSQLDialect_, 
//			 sqldaVersion__, 
// CJL-IB6 end
			      sqldaIn_))
    throw new IB_SQLException (IB_SQLException::engine__default_0__, status_);
}
