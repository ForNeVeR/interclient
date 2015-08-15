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
 * Contributor(s): Friedrich von Never.
 */
package interbase.interclient;

import java.sql.*;
import java.sql.SQLException;

/**
 * Describes input column information for the parameters
 * of a prepared statement.
 *
 * <p>ParameterMetaData is an InterClient extension to JDBC 
 * for describing the parameters to a prepared statement dynamically.
 * <p>
 * ParameterMetaData is extracted from a {@link PreparedStatement}
 * using
 * {@link PreparedStatement#getParameterMetaData()
 *        PreparedStatement.getParameterMetaData()}.
 * This provides a means to get metadata describing
 * the input parameters to a dynamically prepared
 * statement (<code>SQL DESCRIBE INPUT</code> functionality).
 * <p>
 * This class was proposed for inclusion in JDBC 2.0, but
 * was rejected due to lack of underlying support of some RDBMS vendors.
 *
 * @author Paul Ostler
 * @author Mikhail Melnikov
 * @since <font color=red>Extension, since InterClient 1.0</font>
 **/ 
final public class ParameterMetaData implements java.sql.ParameterMetaData
{
  PreparedStatement preparedStatement_;

  ParameterMetaData (PreparedStatement preparedStatement)
  {
    preparedStatement_ = preparedStatement;
  }

  /**
   * Gets the number of input parameters of a dynamically prepared statement.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs
   */
  public int getParameterCount () throws java.sql.SQLException
  {
    // sqlda->sqln
    return preparedStatement_.inputCols_;
  }

    @Override
    public boolean isSigned(int param) throws SQLException {
        return false;
    }

    /**
   * Gets the parameter SQL type for an input column of a prepared statement.
   *
   * @return a sql type from {@link java.sql.Types java.sql.Types}
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs
   */
  public int getParameterType (int parameterIndex) throws java.sql.SQLException
  {
    // sqlvar->sqltype/sqlsubtype
    try {
      return IBTypes.getSQLType (preparedStatement_.inputTypes_[parameterIndex-1]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

  /**
   * Gets the InterBase type name for an input column.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs
   */
  public String getParameterTypeName (int parameterIndex) throws java.sql.SQLException
  {
    // sqlvar->sqltype/sqlsubtype
    try {
      return IBTypes.getIBTypeName (preparedStatement_.inputTypes_[parameterIndex-1]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

    @Override
    public String getParameterClassName(int param) throws java.sql.SQLException {
        return IBTypes.getClassName(preparedStatement_.inputTypes_[param-1]);
    }

  /**
   * Gets the number of decimal digits for an input column.
   *
   * @since <font color=red>Extension, since InterClient 1.0, behavior subject to further functional refinement</font>
   * @throws java.sql.SQLException if a database access error occurs
   **/
  public int getPrecision (int parameterIndex) throws java.sql.SQLException
  {
    // sqlvar->sqllen?, need precision sqlvar field
    try {
      switch (preparedStatement_.inputTypes_[parameterIndex-1]) {
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        return preparedStatement_.inputCharLengths_[parameterIndex-1];
      // MMM - if the parameter is of array type return element's precision
      case IBTypes.ARRAY__:
        // !!!SQLDA_NONAMES - versions of IB before IB6 could not provide
        // get array descriptor (metadata) during statement prepare.
        // Users were supposed to use an extesion method to get the
        // metadata. If the PreparedStatement.prepareArray() method
        // has not been called, then the array descriptor is still null.
        if (preparedStatement_.arrayDescriptors_[parameterIndex-1] != null)
          return preparedStatement_.arrayDescriptors_[parameterIndex-1].elementPrecision_;
        else
          throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
      // MMM - end
      default:
        return preparedStatement_.inputPrecisions_[parameterIndex-1];
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

  /**
   * Gets the number of digits to the right of the decimal for an input column.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs
   */
  public int getScale (int parameterIndex) throws java.sql.SQLException
  {
    // sqlvar->sqlscale
    // !!! I don't think I need to special case CHAR and VARCHAR anymore
    try {
      switch (preparedStatement_.inputTypes_[parameterIndex-1]) {
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        return 0;
      // MMM - if the parameter is of array type return element's precision
      case IBTypes.ARRAY__:
        // !!!SQLDA_NONAMES - versions of IB before IB6 could not provide
        // get array descriptor (metadata) during statement prepare.
        // Users were supposed to use an extesion method to get the
        // metadata. If the PreparedStatement.prepareArray() method
        // has not been called, then the array descriptor is still null.
        if (preparedStatement_.arrayDescriptors_[parameterIndex-1] != null)
          return preparedStatement_.arrayDescriptors_[parameterIndex-1].elementScale_;
        else
          throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
      // MMM - end
      default:
        return preparedStatement_.inputScales_[parameterIndex-1];
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

  /**
   * Can you put a null for this input column.
   *
   * @since <font color=red>Extension, since InterClient 1.0</font>
   * @throws java.sql.SQLException if a database access error occurs
   **/
  public int isNullable (int parameterIndex) throws java.sql.SQLException
  {
    // sqlvar->sqlind
    try {
      return preparedStatement_.inputNullables_[parameterIndex-1]
              ? ParameterMetaData.parameterNullable
              : ParameterMetaData.parameterNoNulls;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

    @Override
    public int getParameterMode(int param) throws SQLException {
        return ParameterMetaData.parameterModeUnknown;
    }

  /**
   * Gets an array parameter's base SQL type.
   * The input column must be of sql type
   * {@link java.sql.Types#ARRAY java.sql.Types.ARRAY}.
   *
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @throws java.sql.SQLException if a database access error occurs
   * @see java.sql.Types
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  public int getArrayBaseType (int parameterIndex) throws java.sql.SQLException
  {
    try {
      if (preparedStatement_.inputTypes_[parameterIndex-1] != IBTypes.ARRAY__)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__not_array_parameter__);

      // !!!INSQLDA_NONAMES - need the null check for IB < 6
      if (preparedStatement_.arrayDescriptors_[parameterIndex-1] != null)
        return IBTypes.getSQLType(
                preparedStatement_.arrayDescriptors_[parameterIndex - 1].elementDataType_);
      else
        throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }
  //*end jre12*
  // MMM - end

  // MMM -added method getArrayDimensions()
  /**
   * Gets an array parameters dimension and bounds.
   * The input column must be of sql type
   * {@link java.sql.Types#ARRAY java.sql.Types.ARRAY}.
   *
   * @return array of arrays of int describing array's dimensions and bounds for each dimensions.
   * @param parameterIndex the first parameter is 1, the second is 2, ...
   * @throws java.sql.SQLException if a database access error occurs
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   **/ //*start jre12*
// CJL-IB6 changed reference to InterClient 2.0
  public int[][] getArrayDimensions (int parameterIndex) throws java.sql.SQLException
  {
    try {
      if (preparedStatement_.inputTypes_[parameterIndex-1] != IBTypes.ARRAY__)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__not_array_parameter__);

      // !!!INSQLDA_NONAMES - need the null check for IB < 6
      if (preparedStatement_.arrayDescriptors_[parameterIndex-1] != null)
        return preparedStatement_.arrayDescriptors_[parameterIndex-1].getDimensions();
      else
        throw new DriverNotCapableException (ErrorKey.driverNotCapable__input_array_metadata__);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ParameterIndexOutOfBoundsException (ErrorKey.parameterIndexOutOfBounds__0__,
						    parameterIndex);
    }
  }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
