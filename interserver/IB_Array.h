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
#ifndef _IB_ARRAY_H_
#define _IB_ARRAY_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"

class IB_Statement;

class IB_Array {

private:

  // Database arrays are identified by a 64 bit arrayId.
  // This can be thought of as a pointer to disk.
  IB_ARRAYID arrayId_;

  // This is a pointer to an InterBase array desc
  // created by createArrayDesc and used by get / put
  ISC_ARRAY_DESC arrayDesc_;

  // Statement context for this array.
  IB_Statement *statement_;

  // !!! use this to get Column Name 
  IB_SSHORT16 columnIndex_;

  IB_STRING tableName_;
  IB_STRING columnName_;

  // Maximum number of bytes in slice 
  // this is used to make space available on get 
  IB_SLONG32 maxSliceLength_;


public:

  // Construct a array object for an existing database array.
  // The existing database array is identified by arrayId.
  // Set the statement context 
  // !!! alternative for ColumnName and TableName accross wire
  // !!! could get a cloumnIndex in a result set and do a look
  // !!! up from within the scope of the server
  IB_Array (const IB_Statement& statement,
	    const IB_STRING columnName,
	    const IB_STRING tableName,
	    const IB_ARRAYID arrayId);

  // Construct a array object for which there is no existing database array.
  // Set the statement context for this array.
  IB_Array (const IB_Statement& statement,
	    const IB_STRING columnName,
	    const IB_STRING tableName);

  // And remove this array from the statements list of referenced arrays 
  ~IB_Array (); 

  // This creates a array desc and fills up slice information.....
  // i.e, array bounds
  // get the array bounds data from the client request packet.
  // desc used by both get and put calls and so must be created before.
  // Throws IB_SQLException if InterBase fails to create the desc 
  void createArrayDesc ();

  // Remove this array from the statement's list of referenced arrays 
  void close ();

  // Get a slice (or all) of array data from this array into array Buffer.
  // Array desc must be setup
  // Returns length of data fetched
  // Throws IB_SQLException if InterBase fails to get an array or slice 
  IB_SLONG32 get (IB_BUFF_PTR arrayBuffer);

  // Put a slice of data into the database array from a arrayBuffer.
  // Array desc must be setup
  // arrayId may or may not be valid to start with. In case, it is
  // NULL to start with, the put call will create a new array and
  // update arrayId_.   
  // Throws IB_SQLException if InterBase fails to put slice in array.
  void put (const IB_SLONG32 arrayBufferLength,
	    const IB_BUFF_PTR arrayBuffer);

  // The follwoing two determine the bounds of
  // an array slice
  void setUpperBound (IB_SSHORT16 row, IB_UBYTE value);
  void setLowerBound (IB_SSHORT16 row, IB_UBYTE value);

  // this call is required only if the Array object was created without
  // an existing arrayId..in case of a insert, or a over writing update.
  // In case of modifying update, an arrayId is already valid
  IB_ARRAYID arrayId () const;

  IB_SLONG32 sliceLength () const;

  IB_BOOLEAN operator== (const IB_Array& array) const ;

};

inline
IB_ARRAYID
IB_Array::arrayId () const
{
  return (arrayId_);
}

inline
IB_SLONG32 
IB_Array::sliceLength () const
{
  return (maxSliceLength_);
}

inline
IB_BOOLEAN 
IB_Array::operator== (const IB_Array& array) const 
{
  return &array == this;
}

#endif
