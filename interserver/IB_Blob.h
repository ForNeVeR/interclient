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
#ifndef _IB_BLOB_H_
#define _IB_BLOB_H_

#include "IB_Defines.h"
#include "IB_SQLException.h"

class IB_Statement;

class IB_Blob {


private:

  friend class IB_Statement;
  friend class JIBSRemote;

  enum {
    blobRequestedInfoSize__ = 48
  };

  // Information request buffer for isc_blob_info
  static const IB_BUFF_CHAR blobInfoRequest__[];

  // Result buffer is a repeating group of triples =
  // { 1 byte blob info item, 2 byte value length, value }
  IB_BUFF_CHAR blobRequestedInfo_ [blobRequestedInfoSize__];

  // Database blobs are identified by a 64 bit blobId.
  // This can be thought of as a pointer to disk.
  IB_BLOBID blobId_;

  // This is a pointer to an internal InterBase blob structure
  // for open blobs only.
  isc_blob_handle blobHandle_;

  // Statement context for this blob.
  IB_Statement *statement_;

  // How many bytes has get() returned so far.
  //IB_SLONG32 dataLength_;

  // data set by a call to get ()
  IB_BOOLEAN atEnd_;
  IB_SLONG32 actualJDBCNetSegmentSize_;

  // results of blob info call, set by a call to open ()
  IB_SLONG32 total_length_; // total size, in bytes, of blob
  IB_SLONG32 num_segments_; // total number of segments
  IB_SLONG32 max_segment_;  // length of longest segment
  IB_SLONG32 type_;         // 0 segmented, 1 streamed

  // Called by open ()
  void getInfo ();

public:

  // also see JDBCNet.BLOB_PUT_SEGMENT_SIZE__
  static const IB_SLONG32 jdbcNetRequestedSegmentSize__;
  static const IB_USHORT16 ibRequestedSegmentSize__;

  // Construct a blob object for an existing database blob.
  // The existing database blob is identified by blobId.
  // The database blob is NOT automaticaly opened by
  // constructing this blob object, rather you must
  // use the open() method.
  // Set the statement context for this blob.
  IB_Blob (const IB_Statement& statement,
	   const IB_BLOBID blobId);

  // Construct a blob object for which there is no existing database blob.
  // Subsequently, create() should be called to create a database
  // blob for this object.
  // segmentSize and numSegments are used for gets only, not puts.
  // Database blobs are identified by the blobId returned from create().
  // Set the statement context for this blob.
  IB_Blob (const IB_Statement& statement);

  // Close the database blob if open.
  // And remove this blob from the statements list of open blobs.
  // Throws IB_SQLException if InterBase fails to close the database blob.
  ~IB_Blob ();

  // This creates a database blob for this blob object, and
  // opens the blob as well.
  // Returns the blobId for the newly created database blob.
  // Throws IB_SQLException if InterBase fails to create the database blob.
  IB_BLOBID create ();

  // Open the database blob, and add this blob to the
  // statement's list of open blobs.
  // Throws IB_SQLException if InterBase fails to open the database blob.
  void open ();

  // Close the database blob, and remove this blob from the
  // statement's list of open blobs.
  // No-op if database blob is already closed.
  // Throws IB_SQLException if InterBase fails to close the database blob.
  void close ();

  // Get a segment of data from this blob into a segmentBuffer.
  // Blob must be open.
  // Return false if this is the last segment (eof), otherwise true.
  // Throws IB_SQLException if InterBase fails to get a blob segment.
  IB_BOOLEAN get (IB_BUFF_PTR segmentBuffer);

  // Put a segment of data into the database blob from a segmentBuffer.
  // This blob must be newly created.
  // Note: you cannot open an existing database blob and append
  // new data to the end of it using put().
  // If you want to append or modify existing blob data you must create a new blob,
  // and read the old blob data into the new blob
  // using oldBlob.fetch() and newBlob.put().
  // Note: you cannot read a segment written with put until you have closed
  // and reopened this blob.
  // Throws IB_SQLException if InterBase fails to put the segment in the blob.
  void put (IB_USHORT16 segmentBufferLength,
	    const IB_BUFF_PTR segmentBuffer);

  // Return the database blobId for this blob object.
  IB_BLOBID blobId () const;

  IB_BOOLEAN operator== (const IB_Blob& blob) const 
  {
    return &blob == this;
  }
  
   IB_BOOLEAN atEnd () const;

  // Return the total number of bytes read so far using get().
  //IB_SLONG32 dataLength () const;

  // Return the number of bytes read on the last call to get().
  IB_SLONG32 actualJDBCNetSegmentSize () const
  {
    return actualJDBCNetSegmentSize_;
  }

  // Return the total number of bytes in the blob.
  IB_SLONG32 size () const
  {
    return total_length_;
  }

};

inline
IB_Blob::IB_Blob (const IB_Statement& statement,
		  const IB_BLOBID blobId) 
  : statement_ ((IB_Statement*) &statement),
    blobHandle_ (NULL),
    blobId_ (blobId),
    atEnd_ (IB_FALSE)
{ }

inline
IB_Blob::IB_Blob (const IB_Statement& statement)
  : statement_ ((IB_Statement*) &statement),
    blobHandle_ (NULL),
    atEnd_ (IB_FALSE)
{ 
  // !!! how to zero out blobId_
}

inline
IB_BOOLEAN
IB_Blob::atEnd () const
{
  return atEnd_;
}

inline
IB_BLOBID
IB_Blob::blobId () const
{
  return blobId_;
}

#endif
