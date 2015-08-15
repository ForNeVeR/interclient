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
package interbase.interclient;

/**
 * @author Mikhail Melnikov
 **/ // MMM
final class ArrayDescriptor
{
  String columnName_ = null;  // name of the array column
  String tableName_ = null;   // name of the array table
  int elementDataType_ = 0;   // as defined in IBTypes.java. used everywhere in the client
  int elementIBDataType_ = 0; // original Interbase's array element data type code
  int elementScale_ = 0;      // scale if array's element is of numeric data type
  int elementPrecision_ = 0;  // calculated on the server and is sent over the wire
  int elementLength_ = 0;     // length of the array element in bytes
  int flags_ = 0;             // always zero. java arrays are row major order
  int dimensions_ = 0;        // number of the array dimensions

  // these arrays are created dynamically. The number of elements equal to dimentions_
  //int[] dimensionBoundLower_ = null;
  //int[] dimensionBoundUpper_ = null;

  // This array describes array's bounds information. It is created dynamically
  // when the array descriptors is received from InterServer as a part of
  // result set or prepared statement metadata. dimensionBounds_.length is
  // always equal to dimensions_. First index specifies the dimension, and
  // second specifies array's lower or upper bounds (the second dimension
  // index is always 0 or 1). For example, for InterBase array defined as
  // DATE[10, 2:8, 5:17] dimensionBounds_ array will look like:
  //       1  10
  //       2   8
  //       5  17
  // Element [0][0] of this array gives you lower bound for the first dimension,
  // element [0][1] - upper bound for the first dimension, element [1][0] -
  // lower bound for the second dimension, and so forth.
  int dimensionBounds_[][] = null;

  static final int MAX_DIMENSIONS = 16;

  /**
   * called by Connection.prepareStatement() and ResultSet.recv_ResultMetaData()
   **/
  ArrayDescriptor (RecvMessage recvMsg) throws java.sql.SQLException
  {
    recv (recvMsg);
  }

  /**
   * Used by Array class constuctor to create a temp descriptor
   **/
  ArrayDescriptor () throws SQLException
  {
    dimensionBounds_ = new int[MAX_DIMENSIONS][2];
  }

  /**
   * receive an array descriptor from the wire
   **/
  private void recv (RecvMessage recvMsg) throws java.sql.SQLException
  {
    elementDataType_ = recvMsg.readUnsignedByte ();
    elementScale_ = recvMsg.readUnsignedByte ();
    elementPrecision_ = recvMsg.readUnsignedShort ();
    elementLength_ = recvMsg.readUnsignedShort ();
    dimensions_ = recvMsg.readUnsignedShort ();

    dimensionBounds_ = new int[dimensions_][2];
    for (int dim = 0; dim < dimensions_; dim++) {
      dimensionBounds_[dim][0] = (int)recvMsg.readShort ();
      dimensionBounds_[dim][1] = (int)recvMsg.readShort ();
    }
    // The following threee lines receive information that is not used
    // by the client. This information is just saved so that original
    // interbase array descriptor could be send to the server (as a part
    // of Array.getArray()) method and be used to read an array slice
    // from InterBase.
    columnName_ = recvMsg.readLDSQLText();
    tableName_ = recvMsg.readLDSQLText();
    elementIBDataType_ = recvMsg.readUnsignedByte ();
  }

  /**
   * called by Array.remote_GET_ARRAY_SLICE
   * Send the descriptor to the server as a part of "get array slice"
   * operation. Second parameter describes slice bounds, in case where
   * the whole array is read slice bounds are the same as array
   * dimension bounds.
   **/
  void send (MessageBufferOutputStream sendMsg, int[][] sliceBounds) throws java.sql.SQLException
  {
    if (sliceBounds.length != dimensions_)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 137);

    sendMsg.writeByte (elementIBDataType_);
    sendMsg.writeByte (elementScale_);
    //sendMsg.writeShort (elementPrecision_);
    sendMsg.writeShort (elementLength_);
    sendMsg.writeShort (dimensions_);
    for (int dim = 0; dim < dimensions_; dim++) {
      sendMsg.writeShort (sliceBounds[dim][0]);
      sendMsg.writeShort (sliceBounds[dim][1]);
    }
    sendMsg.writeLDChars (columnName_);
    sendMsg.writeLDChars (tableName_);
  }

  /**
   * Returns 2D array describing array's dimensions and bounds
   * Called by ResultSetMetaData/ParameterMetaDate.getArrayDimensions()
   * INSQLDA_NONAMES - if the descriptor is empty, throw an exception
   **/
  int[][] getDimensions ()  throws java.sql.SQLException
  {
    int[][] dimDesc = new int[dimensions_][2];
    for (int dim = 0; dim < dimensions_; dim++){
      dimDesc[dim][0] = dimensionBounds_[dim][0];
      dimDesc[dim][1] = dimensionBounds_[dim][1];
    }
    return dimDesc;
  }

  /**
   * Called by Array.getArray() methods. Checks to see if the slice
   * described by sliceBounds is actually within array. As a byproduct
   * returns boolean which is set to true if slice equals whole array.
   **/
  boolean checkSliceBounds (int[][] sliceBounds) throws java.sql.SQLException
  {
    if (sliceBounds.length != dimensions_)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_slice__);

    boolean isWholeArray = true;

    for (int dim = 0; dim < sliceBounds.length; dim++){
      if ((sliceBounds[dim][0] < dimensionBounds_[dim][0]) ||
          (sliceBounds[dim][0] > dimensionBounds_[dim][1]) ||
          (sliceBounds[dim][1] > dimensionBounds_[dim][1]) ||
          (sliceBounds[dim][1] < dimensionBounds_[dim][0]) ||
          (sliceBounds[dim][0] > sliceBounds[dim][1]))
        throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_slice__);

      if ((sliceBounds[dim][0] != dimensionBounds_[dim][0]) ||
          (sliceBounds[dim][1] != dimensionBounds_[dim][1])) {
        isWholeArray = false;
      }
    }
    return isWholeArray;
  }
}
// MMM - end
