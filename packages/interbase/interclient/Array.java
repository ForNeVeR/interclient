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
 * The Java mapping for an SQL Array.
 * By default, an Array is a transaction duration reference to an SQL array.
 * By default, an Array is implemented using an SQL LOCATOR(array) internally.
 *
 * <p><b>InterClient Notes:</b><br>
 * JDBC 2.0 array support is based on the SQL3 standard and
 * does not require support for multidimensional arrays (or array of arrays).
 * InterClient supports the array data type as specified by the JDBC 2 specification,
 * but in addition to this support InterClient provides for multi-dimensional arrays as
 * supported by InterBase.
 * InterBase, supports multi-dimensional arrays up to 16 dimensions.
 * Because of these differences InterClient provides extension methods
 * to allow a users to work with multi-dimensional arrays.
 * <p>
 * In accordance with the JDBC 2 specification,
 * Array instances are created using
 * {@link ResultSet#getArray(int) ResultSet.getArray</code>}.
 * So, in accordance with JDBC 2, there is no public constructor for this class.
 * <p>
 * An InterBase array may be referenced by an <code>Array</code> instance
 * for the duration of the transaction in which it was created.
 * Although it is not good programming practice,
 * an Array could be used even after its transaction has finished,
 * but its associated InterBase array may be garbage collected without warning
 * at any point in time after its transaction completes.
 * So, you may still be able get a Java array within the transaction context
 * of the completed transaction that created it, even after the InterBase array has
 * been modified by another transaction, but you will get an error from the engine if
 * the InterBase array has been garbage collected.
 * <p>
 * <P><B>Note:</B> Because of a current InterBase limitation only the
 * "8859_1" character encoding is supported for arrays of Strings.
 * <p>
 * For further information on InterClient's support of InterBase arrays
 * see <a href="../../../help/icArray.html">InterClient Array Support</a>
 * in the InterClient help directory.
 *
 * @author Mikhail Melnikov
 * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
 **/  // MMM
 // CJL-IB6  removed reference to "proposed for InterClient 2.0
final public class Array implements java.sql.Array
{

  Object data_ = null;
  int[] id_ = {0, 0};
  ArrayDescriptor descriptor_ = null;
  Connection connection_ = null;

  // !!!MMM - Or may be i just need to reference the same definition in PreparedStatement?
  private final static java.math.BigDecimal bdMaxShortValue = new java.math.BigDecimal (Short.MAX_VALUE);
  private final static java.math.BigDecimal bdMinShortValue = new java.math.BigDecimal (Short.MIN_VALUE);
  private final static java.math.BigDecimal bdMaxIntValue = new java.math.BigDecimal (Integer.MAX_VALUE);
  private final static java.math.BigDecimal bdMinIntValue = new java.math.BigDecimal (Integer.MIN_VALUE);
  private final static java.math.BigDecimal bdMaxFloatValue = new java.math.BigDecimal (Float.MAX_VALUE);
  private final static java.math.BigDecimal bdMinFloatValue = new java.math.BigDecimal (Float.MIN_VALUE);
  private final static java.math.BigDecimal bdMaxDoubleValue = new java.math.BigDecimal (Double.MAX_VALUE);
  private final static java.math.BigDecimal bdMinDoubleValue = new java.math.BigDecimal (Double.MIN_VALUE);

  /**
   * Called by PreparedStatement.setArray().
   **/
  Array (Object data) throws java.sql.SQLException
  {
    if (!data.getClass().isArray())
      // If this constructor were public, then we could use
      // InvalidArgumentException(ErrorKey.invalidArgument__not_array_object);
      throw new BugCheckException (ErrorKey.bugCheck__0__, 131);
    data_ = data;
  }

  /**
   * Not being used currently
   **/
  Array (Object data, ArrayDescriptor descriptor) throws java.sql.SQLException
  {
    if (!data.getClass().isArray())
      // If this constructor were public, then we could use
      // InvalidArgumentException(ErrorKey.invalidArgument__not_array_object);
      throw new BugCheckException (ErrorKey.bugCheck__0__, 132);
    setDescriptor (descriptor);
    data_ = data;
  }

  /**
   * Called by ResultSet.getArray().
   **/
  Array (int[] id,
         ArrayDescriptor descriptor,
         Connection connection) throws java.sql.SQLException
  {
    setDescriptor (descriptor);
    id_[0] = id[0];
    id_[1] = id[1];
    connection_ = connection;
  }

  /**
   * Called by constuctor to associate given descriptor with this object
   * and by PreparedStatement.setArray().
   **/
  void setDescriptor (ArrayDescriptor descriptor) throws java.sql.SQLException
  {
    if (descriptor == null)
      throw new BugCheckException (ErrorKey.bugCheck__0__, 133);

    // During read operation object of this class is already created
    // but no data has been received from the server yet.
    if (data_ != null)
      checkDimensions (data_, descriptor, 0);
    descriptor_ = descriptor;
  }

  /**
   * Called by constuctor to get the dimension information
   * from the array. This information is stored in tmp array
   * descriptor and is compared later with the descriptor that
   * has been received from a database.
   * This method uses recursive algorithm. If the 1st dimension
   * is array of arrays it calls itself recursively to process
   * the second dimension, and so on, until it comes down to
   * the last dimension - an array of primitive types or objects.
   * for each dimension, dimension size is stored.
   **/
  private void checkDimensions (Object array,
                                 ArrayDescriptor descriptor,
                                 int dim) throws java.sql.SQLException
  {
    if (array == null)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_dimensions__);

    int length = java.lang.reflect.Array.getLength (array);
    if ((descriptor.dimensionBounds_[dim][1] -
         descriptor.dimensionBounds_[dim][0] + 1) != length)
      throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_dimensions__);

    if (array.getClass().getComponentType().isArray()) {
      if (++dim >= descriptor.dimensions_)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_dimensions__);
      for (int i = 0; i < length; i++)
        checkDimensions (java.lang.reflect.Array.get (array, i), descriptor, dim);
    }
    else
      if (dim != descriptor.dimensions_ - 1)
        throw new InvalidArgumentException (ErrorKey.invalidArgument__invalid_array_dimensions__);
  }

  /**
   * called by PreparedStatement.send_Data
   **/
  void send (MessageBufferOutputStream sendMsg) throws java.sql.SQLException
  {
    // Send array Id
    sendMsg.writeArrayId (id_);
    // Send array descriptor
    descriptor_.send (sendMsg, descriptor_.dimensionBounds_);
    // Send array data
    send_Data (sendMsg, data_);
  }

  /**
   * Called by send() method during PreparedStatement.executeUpdate()
   * If array is contains more than one dimension goes recursively
   * through all dimensions and puts data on the wire.
   * If by some reason conversion of an array element represented by
   * primitive data type or object to base element type defined by
   * InterBase is not possible, exception is thrown.
   **/
  private void send_Data (MessageBufferOutputStream sendMsg,
                          Object array) throws java.sql.SQLException
  {
    Class elementClass = array.getClass().getComponentType();

    if (elementClass.isArray())
      for (int i = 0; i < ((Object[])array).length; i++)
        send_Data (sendMsg, ((Object[])array)[i] );
    else {
      switch (descriptor_.elementDataType_) {
      case IBTypes.SMALLINT__:
        send_SMALLINT (sendMsg, array, elementClass);
        break;
      case IBTypes.INTEGER__:
        send_INTEGER (sendMsg, array, elementClass);
        break;
      case IBTypes.FLOAT__:
        send_FLOAT (sendMsg, array, elementClass);
        break;
      case IBTypes.DOUBLE__:
        send_DOUBLE (sendMsg, array, elementClass);
        break;
      case IBTypes.NUMERIC_SMALLINT__:
      case IBTypes.NUMERIC_INTEGER__:
        send_NUMERIC_INTEGER (sendMsg, array, elementClass);
        break;
      case IBTypes.NUMERIC_DOUBLE__:
        send_NUMERIC_DOUBLE (sendMsg, array, elementClass);
        break;
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        send_CHAR (sendMsg, array, elementClass);
        break;
      case IBTypes.DATE__:
        send_DATE (sendMsg, array, elementClass);
        break;
      default:
        throw new BugCheckException (ErrorKey.bugCheck__0__, 138);
      } // switch
    } // isArray

  } // send_Data ()

  // The following is a comment to all send_<IB_DATA_TYPE> methods.
  // This comment describes a general approach that was used in
  // coding of send_<IB_DATA_TYPE> methods.
  //
  // Whenever possible the code is optimized for performance, sometimes
  // at the expense of elegance.
  //
  // If array elements are of primitive data types (which means they are
  // all the same then the pattern is (assuming we write in IB array of INT):
  //
  //  if (elementClass.isPrimitive()) {
  //    if (elementClass.equals (int.class))
  //      for (int i = 0; i < length; i++)
  //        sendMsg.writeInt (((int[])array)[i]);
  //    else if (elementClass.equals (byte.class))
  //      for (int i = 0; i < length; i++)
  //        sendMsg.writeInt (((byte[])array)[i]);
  //    else if (elementClass.equals (short.class))
  //      for (int i = 0; i < length; i++)
  //        sendMsg.writeInt (((short[])array)[i]);
  //
  // This fragment could have been coded in more compact way. The following
  // fragment does the same thing but it is shorter, may be more elegant, but
  // it is also slower than the previous one (by 5%).
  //
  //  if (elementClass.equals (byte.class)  ||
  //      elementClass.equals (short.class) ||
  //      elementClass.equals (int.class))
  //    for (int i = 0; i < length; i++)
  //      sendMsg.writeInt (java.lang.reflect.Array.getInt (array, i));
  //
  // There are, of course, exclusions from this approach. Particular when
  // there is more than one line of code necessary to process an array
  // element (see send_NUMERIC_* methods). In this cases performance is
  // sacrified to make code more general and the approach reminds the code
  // which deals with arrays of some objects. Read on.
  //
  // If array elements are objects then
  //
  //  else // it is array of some objects
  //    for (int i = 0; i < length; i++) {
  //      Object element = ((Object[])array)[i];
  //      if (element instanceof Integer)
  //        tmpElem = ((Integer)element).intValue();
  //      else if (element instanceof Byte)
  //        tmpElem = ((Byte)element).intValue();
  //      else if (element instanceof Short)
  //        tmpElem = ((Short)element).intValue();
  //
  //      sendMsg.writeInt (tmpElem);
  //    }
  //
  // This approach takes cake of the situations where an array of objects
  // of different classes is used to write into the IB array.

  /**
   * Called by Array.send_Data().
   **/
  private void send_SMALLINT (MessageBufferOutputStream sendMsg,
                              Object array,
                              Class elementClass) throws java.sql.SQLException
  {
    short tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      if (elementClass.equals (short.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeShort (((short[])array)[i]);
      else if (elementClass.equals (byte.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeShort (((byte[])array)[i]);
      else if (elementClass.equals (int.class))
        for (int i = 0; i < length; i++) {
          int tmp = ((int[])array)[i];
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeShort ((short)tmp);
        }
      else if (elementClass.equals (long.class))
        for (int i = 0; i < length; i++) {
          long tmp = ((long[])array)[i];
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeShort ((short)tmp);
        }
      else if (elementClass.equals (float.class))
        for (int i = 0; i < length; i++) {
          float tmp = ((float[])array)[i];
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeShort ((short)tmp);
        }
      else if (elementClass.equals (double.class))
        for (int i = 0; i < length; i++) {
          double tmp = ((double[])array)[i];
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeShort ((short)tmp);
        }
      else if (elementClass.equals (boolean.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeShort (((boolean[])array)[i] ? 1 : 0);
      else
        throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof Short)
          tmpElem = ((Short)element).shortValue();
        else if (element instanceof Byte)
          tmpElem = ((Byte)element).shortValue();
        else if (element instanceof Integer)
          tmpElem = ((Integer)element).shortValue();
        else if (element instanceof Long) {
          long tmp = ((Long)element).longValue();
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Long)element).shortValue();
        }
        else if (element instanceof Float) {
          float tmp = ((Float)element).floatValue();
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Float)element).shortValue();
        }
        else if (element instanceof Double) {
          double tmp = ((Double)element).doubleValue();
          if (tmp > Short.MAX_VALUE || tmp < Short.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Double)element).shortValue();
        }
        else if (element instanceof java.math.BigDecimal) {
          if (((java.math.BigDecimal)element).compareTo (bdMaxShortValue) == 1 ||
              ((java.math.BigDecimal)element).compareTo (bdMinShortValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (element));
          tmpElem = ((java.math.BigDecimal)element).shortValue();
        }
        else if (element instanceof String) {
          try {
            tmpElem = Short.parseShort ((String)element);
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String) element));
          }
        }
        else if (element instanceof Boolean)
          tmpElem = (short)(((Boolean)element).booleanValue() ? 1 : 0);
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeShort (tmpElem);
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_INTEGER (MessageBufferOutputStream sendMsg,
                             Object array,
                             Class elementClass) throws java.sql.SQLException
  {
    int tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      if (elementClass.equals (int.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeInt (((int[])array)[i]);
      else if (elementClass.equals (byte.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeInt (((byte[])array)[i]);
      else if (elementClass.equals (short.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeInt (((short[])array)[i]);
      else if (elementClass.equals (long.class))
        for (int i = 0; i < length; i++) {
          // I was hoping that something simplier like
          // int tmp = java.lang.reflect.Array.getInt (array, i);
          // could be used here letting the above call to make a
          // proper conversion and throw an exception if it can't.
          // But, it throws an exception anyway.
          long tmp = ((long[])array)[i];
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeInt ((int)tmp);
        }
      else if (elementClass.equals (float.class))
        for (int i = 0; i < length; i++) {
          float tmp = ((float[])array)[i];
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeInt ((int)tmp);
        }
      else if (elementClass.equals (double.class))
        for (int i = 0; i < length; i++) {
          double tmp = ((double[])array)[i];
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeInt ((int)tmp);
        }
      else if (elementClass.equals (boolean.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeInt (((boolean[])array)[i] ? 1 : 0);
      else
        throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof Integer)
          tmpElem = ((Integer)element).intValue();
        else if (element instanceof Byte)
          tmpElem = ((Byte)element).intValue();
        else if (element instanceof Short)
          tmpElem = ((Short)element).intValue();
        else if (element instanceof Long) {
          long tmp = ((Long)element).longValue();
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Long)element).intValue();
        }
        else if (element instanceof Float) {
          float tmp = ((Float)element).floatValue();
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Float)element).intValue();
        }
        else if (element instanceof Double) {
          double tmp = ((Double)element).doubleValue();
          if (tmp > Integer.MAX_VALUE || tmp < Integer.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Double)element).intValue();
        }
        else if (element instanceof java.math.BigDecimal) {
          if (((java.math.BigDecimal)element).compareTo (bdMaxIntValue) == 1 ||
              ((java.math.BigDecimal)element).compareTo (bdMinIntValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (element));
          tmpElem = ((java.math.BigDecimal)element).intValue();
        }
        else if (element instanceof String) {
          try {
            tmpElem = Integer.parseInt ((String)element);
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String)element));
          }
        }
        else if (element instanceof Boolean)
          tmpElem = ((Boolean)element).booleanValue() ? 1 : 0;
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeInt (tmpElem);
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_FLOAT (MessageBufferOutputStream sendMsg,
                           Object array,
                           Class elementClass) throws java.sql.SQLException
  {
    float tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      if (elementClass.equals (float.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((float[])array)[i]);
      else if (elementClass.equals (byte.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((byte[])array)[i]);
      else if (elementClass.equals (short.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((short[])array)[i]);
      else if (elementClass.equals (int.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((int[])array)[i]);
      else if (elementClass.equals (long.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((long[])array)[i]);
      else if (elementClass.equals (double.class))
        for (int i = 0; i < length; i++) {
          double tmp = ((double[])array)[i];
          if (tmp > Float.MAX_VALUE || tmp < Float.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          sendMsg.writeFloat ((float)tmp);
        }
      else if (elementClass.equals (boolean.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeFloat (((boolean[])array)[i] ? 1 : 0);
      else
        throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof Float)
          tmpElem = ((Float)element).floatValue();
        else if (element instanceof Byte)
          tmpElem = ((Byte)element).floatValue();
        else if (element instanceof Short)
          tmpElem = ((Short)element).floatValue();
        else if (element instanceof Integer)
          tmpElem = ((Integer)element).floatValue();
        else if (element instanceof Long)
          tmpElem = ((Long)element).floatValue();
        else if (element instanceof Double) {
          double tmp = ((Double)element).doubleValue();
          if (tmp > Float.MAX_VALUE || tmp < Float.MIN_VALUE)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (tmp));
          tmpElem = ((Double)element).floatValue();
        }
        else if (element instanceof java.math.BigDecimal) {
          if (((java.math.BigDecimal)element).compareTo (bdMaxFloatValue) == 1 ||
              ((java.math.BigDecimal)element).compareTo (bdMinFloatValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (element));
          tmpElem = ((java.math.BigDecimal)element).floatValue();
        }
        else if (element instanceof String) {
          try {
            tmpElem = (Float.valueOf ((String)element)).floatValue();
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String)element));
          }
        }
        else if (element instanceof Boolean)
          tmpElem = ((Boolean)element).booleanValue() ? 1 : 0;
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeFloat (tmpElem);
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_DOUBLE (MessageBufferOutputStream sendMsg,
                            Object array,
                            Class elementClass) throws java.sql.SQLException
  {
    double tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      if (elementClass.equals (double.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((double[])array)[i]);
      else if (elementClass.equals (byte.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((byte[])array)[i]);
      else if (elementClass.equals (short.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((short[])array)[i]);
      else if (elementClass.equals (int.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((int[])array)[i]);
      else if (elementClass.equals (long.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((long[])array)[i]);
      else if (elementClass.equals (float.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((float[])array)[i]);
      else if (elementClass.equals (boolean.class))
        for (int i = 0; i < length; i++)
          sendMsg.writeDouble (((boolean[])array)[i] ? 1 : 0);
      else
        throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof Double)
          tmpElem = ((Double)element).doubleValue();
        else if (element instanceof Byte)
          tmpElem = ((Byte)element).doubleValue();
        else if (element instanceof Short)
          tmpElem = ((Short)element).doubleValue();
        else if (element instanceof Integer)
          tmpElem = ((Integer)element).doubleValue();
        else if (element instanceof Long)
          tmpElem = ((Long)element).doubleValue();
        else if (element instanceof Float)
          tmpElem = ((Float)element).doubleValue();
        else if (element instanceof java.math.BigDecimal) {
          if (((java.math.BigDecimal)element).compareTo (bdMaxDoubleValue) == 1 ||
              ((java.math.BigDecimal)element).compareTo (bdMinDoubleValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (element));
          tmpElem = ((java.math.BigDecimal)element).doubleValue();
        }
        else if (element instanceof String) {
          try {
            tmpElem = (Double.valueOf ((String)element)).doubleValue();
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String)element));
          }
        }
        else if (element instanceof Boolean)
          tmpElem = ((Boolean)element).booleanValue() ? 1 : 0;
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeDouble (tmpElem);
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_NUMERIC_INTEGER (MessageBufferOutputStream sendMsg,
                                     Object array,
                                     Class elementClass) throws java.sql.SQLException
  {
    java.math.BigDecimal bd = null;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      for (int i = 0; i < length; i++) {
        if (elementClass.equals (short.class)  ||
            elementClass.equals (int.class)    ||
            elementClass.equals (long.class)   ||
            elementClass.equals (float.class)  ||
            elementClass.equals (double.class) ||
            elementClass.equals (byte.class))
          bd = new java.math.BigDecimal (java.lang.reflect.Array.getDouble (array, i));
        else if (elementClass.equals (boolean.class))
          bd = new java.math.BigDecimal (((boolean[])array)[i] ? 1 : 0);
        else
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);

        bd = bd.movePointRight (descriptor_.elementScale_);
        bd = bd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
        if (descriptor_.elementDataType_ == IBTypes.NUMERIC_SMALLINT__) {
          if (bd.compareTo (bdMaxShortValue) == 1 || bd.compareTo (bdMinShortValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (bd.doubleValue()));
          sendMsg.writeShort (bd.shortValue());
        }
        else { // IBTypes.NUMERIC_INTEGER__
          if (bd.compareTo (bdMaxIntValue) == 1 || bd.compareTo (bdMinIntValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (bd.doubleValue()));
          sendMsg.writeInt (bd.intValue());
        }
      }
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof java.math.BigDecimal)
          bd = (java.math.BigDecimal)element;
        else if (element instanceof Number)
          bd = new java.math.BigDecimal (((Number)element).doubleValue());
        else if (element instanceof String) {
          try {
            bd = new java.math.BigDecimal ((String)element);
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String)element));
          }
        }
        else if (element instanceof Boolean)
          bd = new java.math.BigDecimal (((Boolean)element).booleanValue() ? 1 : 0);
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);

        bd = bd.movePointRight (descriptor_.elementScale_);
        bd = bd.setScale (0, java.math.BigDecimal.ROUND_HALF_DOWN);
        if (descriptor_.elementDataType_ == IBTypes.NUMERIC_SMALLINT__) {
          if (bd.compareTo (bdMaxShortValue) == 1 || bd.compareTo (bdMinShortValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (bd.doubleValue()));
          sendMsg.writeShort (bd.shortValue());
        }
        else { // IBTypes.NUMERIC_INTEGER__
          if (bd.compareTo (bdMaxIntValue) == 1 || bd.compareTo (bdMinIntValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (bd.doubleValue()));
          sendMsg.writeInt (bd.intValue());
        }
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_NUMERIC_DOUBLE (MessageBufferOutputStream sendMsg,
                                    Object array,
                                    Class elementClass) throws java.sql.SQLException
  {
    double tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      for (int i = 0; i < length; i++) {
        if (elementClass.equals (double.class) ||
            elementClass.equals (float.class)  ||
            elementClass.equals (long.class)   ||
            elementClass.equals (int.class)    ||
            elementClass.equals (short.class)  ||
            elementClass.equals (byte.class))
          tmpElem = java.lang.reflect.Array.getDouble (array, i);
        else if (elementClass.equals (boolean.class))
          tmpElem = (double)(((boolean[])array)[i] ? 1 : 0);
        else
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeDouble (tmpElem);
      }
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof java.math.BigDecimal) {
          if (((java.math.BigDecimal)element).compareTo (bdMaxDoubleValue) == 1 ||
              ((java.math.BigDecimal)element).compareTo (bdMinDoubleValue) == -1)
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    String.valueOf (element));
          tmpElem = ((java.math.BigDecimal)element).doubleValue();
        }
        else if (element instanceof Number)
          tmpElem = ((Number)element).doubleValue();
        else if (element instanceof String) {
          try {
            tmpElem = (new java.math.BigDecimal ((String)element)).doubleValue();
          }
          catch (java.lang.NumberFormatException e) {
            throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                    ((String)element));
          }
        }
        else if (element instanceof Boolean)
          tmpElem = (double)(((Boolean)element).booleanValue() ? 1 : 0);
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        sendMsg.writeDouble (tmpElem);
      }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_DATE (MessageBufferOutputStream sendMsg,
                          Object array,
                          Class elementClass) throws java.sql.SQLException
  {
    IBTimestamp tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive())
      throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
    for (int i = 0; i < length; i++) {
      Object element = ((Object[])array)[i];
      if (element instanceof java.sql.Date) {
        tmpElem = new IBTimestamp (((java.sql.Date)element).getYear (),
                                   ((java.sql.Date)element).getMonth (),
                                   ((java.sql.Date)element).getDate ());
      }
      else if (element instanceof java.sql.Timestamp) {
        tmpElem = new IBTimestamp (((java.sql.Timestamp)element).getYear (),
                                   ((java.sql.Timestamp)element).getMonth (),
                                   ((java.sql.Timestamp)element).getDate (),
                                   ((java.sql.Timestamp)element).getHours (),
                                   ((java.sql.Timestamp)element).getMinutes (),
                                   ((java.sql.Timestamp)element).getSeconds ());
      }
      else if (element instanceof String) {
        try {
          java.sql.Timestamp tmpTimestamp = java.sql.Timestamp.valueOf ((String)element);
          tmpElem = new IBTimestamp (tmpTimestamp.getYear (),
                                     tmpTimestamp.getMonth (),
                                     tmpTimestamp.getDate (),
                                     tmpTimestamp.getHours (),
                                     tmpTimestamp.getMinutes (),
                                     tmpTimestamp.getSeconds ());
        }
        catch (java.lang.IllegalArgumentException e) {
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_conversion_0__,
                                                  ((String)element));
        }
      }
      else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
      sendMsg.writeInt (tmpElem.encodedYearMonthDay_);
      sendMsg.writeInt (tmpElem.encodedHourMinuteSecond_);
    }
  }

  /**
   * Called by Array.send_Data().
   **/
  private void send_CHAR (MessageBufferOutputStream sendMsg,
                          Object array,
                          Class elementClass) throws java.sql.SQLException
  {
    String tmpElem;
    int length = java.lang.reflect.Array.getLength (array);
    if (elementClass.isPrimitive()) {
      for (int i = 0; i < length; i++) {
        if (elementClass.equals (double.class) ||
            elementClass.equals (float.class))
          tmpElem = String.valueOf (java.lang.reflect.Array.getDouble (array, i));
        else if (elementClass.equals (long.class)   ||
                 elementClass.equals (int.class)    ||
                 elementClass.equals (short.class)  ||
                 elementClass.equals (byte.class))
          tmpElem = String.valueOf (java.lang.reflect.Array.getLong (array, i));
        else if (elementClass.equals (boolean.class))
          tmpElem = String.valueOf (((boolean[])array)[i]);
        else
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        if (tmpElem.length() > descriptor_.elementPrecision_)
          // instead of throw new java.sql.DataTruncation (...)
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_truncation_0__,
                                                  ((String)tmpElem));
        // !!!IB bug# Only "8859_1" encoding is currently supported
        sendMsg.writeLDSQLText (tmpElem);
      }
    }
    else // it is array of objects
      // The order of "if" clauses is important from the performance
      // point of view with most probable conversions being first.
      for (int i = 0; i < length; i++) {
        Object element = ((Object[])array)[i];
        if (element instanceof String)
          tmpElem = ((String)element);
        else if ((element instanceof Byte)    ||
                 (element instanceof Short)   ||
                 (element instanceof Integer) ||
                 (element instanceof Long)    ||
                 (element instanceof Float)   ||
                 (element instanceof Double)  ||
                 (element instanceof java.math.BigDecimal) ||
                 (element instanceof java.sql.Date)        ||
                 (element instanceof java.sql.Timestamp)   ||
                 (element instanceof Boolean))
          tmpElem = element.toString();
        else throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_type_conversion__);
        if (tmpElem.length() > descriptor_.elementPrecision_)
          // instead of throw new java.sql.DataTruncation (...)
          throw new ParameterConversionException (ErrorKey.parameterConversion__array_element_instance_truncation_0__,
                                                  ((String)tmpElem));
        // !!!IB bug# Only "8859_1" encoding is currently supported
        sendMsg.writeLDSQLText (tmpElem);
      }
  }

  /**
   * Called by Array.getArray().
   **/
  private Object remote_GET_ARRAY_SLICE (int[][] sliceBounds) throws java.sql.SQLException
  {
    Object slice;

    connection_.checkForClosedConnection ();
    connection_.clearWarnings ();

    MessageBufferOutputStream sendMsg = connection_.jdbcNet_.createMessage ();

    sendMsg.writeByte (MessageCodes.GET_ARRAY_SLICE__);
    connection_.send_TransactionConfigData (sendMsg);
    sendMsg.writeArrayId (id_);
    descriptor_.send (sendMsg, sliceBounds);
    slice = createJavaArray (sliceBounds);

    RecvMessage recvMsg = null;
    try {
      recvMsg = connection_.jdbcNet_.sendAndReceiveMessage (sendMsg);

      if (!recvMsg.get_SUCCESS ()) {
        throw recvMsg.get_EXCEPTIONS ();
      }
      recv_Data (recvMsg, slice);

      connection_.setWarning (recvMsg.get_WARNINGS ());
    }
    finally {
      connection_.jdbcNet_.destroyRecvMessage (recvMsg);
    }
    return slice;
  }

  private Object createJavaArray (int[][] dimensionBounds) throws java.sql.SQLException
  {
    Class elementClass = null;

    int[] dimensions = new int[descriptor_.dimensions_];
    for (int i = 0; i < descriptor_.dimensions_; i++)
      dimensions[i] = dimensionBounds[i][1] - dimensionBounds[i][0] + 1;

    switch (descriptor_.elementDataType_) {
    case IBTypes.SMALLINT__:
      elementClass = short.class;
      break;
    case IBTypes.INTEGER__:
      elementClass = int.class;
      break;
    case IBTypes.FLOAT__:
      elementClass = float.class;
      break;
    case IBTypes.DOUBLE__:
      elementClass = double.class;
      break;
    case IBTypes.NUMERIC_SMALLINT__:
    case IBTypes.NUMERIC_INTEGER__:
    case IBTypes.NUMERIC_DOUBLE__:
      elementClass = java.math.BigDecimal.class;
      break;
    case IBTypes.CHAR__:
    case IBTypes.VARCHAR__:
      elementClass = String.class;
      break;
    case IBTypes.DATE__:
      elementClass = java.sql.Timestamp.class;
      break;
    default:
      throw new BugCheckException (ErrorKey.bugCheck__0__, 135);
      }
    return java.lang.reflect.Array.newInstance (elementClass, dimensions);
  }

  private void recv_Data (RecvMessage recvMsg, Object array) throws java.sql.SQLException
  {
    Class elementClass = array.getClass().getComponentType();
    int length = java.lang.reflect.Array.getLength (array);

    if (elementClass.isArray())
    {
      for (int i = 0; i < length; i++)
        recv_Data (recvMsg, ((Object[])array)[i]);
      return;
    }
    else
    {
      // Last dimension
      switch (descriptor_.elementDataType_) {
      case IBTypes.SMALLINT__:
        for (int i = 0; i < length; i++)
          ((short[])array)[i] = recvMsg.readShort ();
        return;
      case IBTypes.INTEGER__:
        for (int i = 0; i < length; i++)
          ((int[])array)[i] = recvMsg.readInt ();
        return;
      case IBTypes.FLOAT__:
        for (int i = 0; i < length; i++)
          ((float[])array)[i] = recvMsg.readFloat ();
        return;
      case IBTypes.DOUBLE__:
        for (int i = 0; i < length; i++)
          ((double[])array)[i] = recvMsg.readDouble ();
        return;
      case IBTypes.NUMERIC_SMALLINT__:
        for (int i = 0; i < length; i++)
          ((java.math.BigDecimal[])array)[i] =
                java.math.BigDecimal.valueOf((long)recvMsg.readShort (),
                                             descriptor_.elementScale_);
        return;
      case IBTypes.NUMERIC_INTEGER__:
        for (int i = 0; i < length; i++)
          ((java.math.BigDecimal[])array)[i] =
                java.math.BigDecimal.valueOf((long)recvMsg.readInt (),
                                             descriptor_.elementScale_);
        return;
      case IBTypes.NUMERIC_DOUBLE__:
        for (int i = 0; i < length; i++)
          ((java.math.BigDecimal[])array)[i] =
            (new java.math.BigDecimal(recvMsg.readDouble ())).setScale (descriptor_.elementScale_,
                                                                        java.math.BigDecimal.ROUND_HALF_EVEN);;
        return;
      case IBTypes.CHAR__:
      case IBTypes.VARCHAR__:
        for (int i = 0; i < length; i++)
          // !!!IB bug# Only "8859_1" encoding is currently supported
          ((String[])array)[i] = new String(recvMsg.readLDSQLText ());
        return;
      case IBTypes.DATE__:
        for (int i = 0; i < length; i++) {
          int timestampId[] = recvMsg.readTimestampId ();
          IBTimestamp ibTimestamp = new IBTimestamp (IBTimestamp.DATETIME,
                                                     timestampId);
	        ((java.sql.Timestamp[])array)[i] = new java.sql.Timestamp (
                                             ibTimestamp.getYear (),
                                             ibTimestamp.getMonth (),
                                             ibTimestamp.getDate (),
                                             ibTimestamp.getHours (),
                                             ibTimestamp.getMinutes (),
                                             ibTimestamp.getSeconds (),
                                             ibTimestamp.getNanos ());
        }
        return;
      default:
        throw new BugCheckException (ErrorKey.bugCheck__0__, 136);
      }
    }
  }

  //--------------------------JDBC 2.0 -----------------------------------------

  /**
   * Returns the SQL type name of the elements in this array.
   * If the elements are a built-in type, it returns
   * the database-specific type name of the elements. 
   * If the elements are a user-defined type (UDT),
   * this method returns the fully-qualified SQL type name.
   *
   * @return the database-specific name for a built-in base type or
   *   the fully-qualified SQL type name for a base type that is a UDT
   * @throws java.sql.SQLException if an error occurs while attempting to access the type name
   * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public String getBaseTypeName () throws java.sql.SQLException
  {
    return IBTypes.getIBTypeName (descriptor_.elementDataType_);
  }

  /**
   * Returns the JDBC type of the elements in this array.
   *
   * @return a constant from the class java.sql.Types that is
   *   the type code for the elements in this array.
   * @throws java.sql.SQLException if an error occurs while attempting to access the the base type 
   * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public int getBaseType () throws java.sql.SQLException
  {
    return IBTypes.getSQLType (descriptor_.elementDataType_);
  }

  /**
   * Retrieve the contents of this SQL array as a Java <code>Array</code> object.
   * This version of the method <code>getArray</code>
   * uses the type map associated with the connection for customizations of
   * the type mappings.
   * <p>
   * Conceptually, this method calls <code>getObject()</code> on each element of the
   * array and returns a Java array containing the result. Except when
   * the array element type maps to a Java primitive type, such as int,
   * boolean, etc. In this case, an array of primitive type values, 
   * i.e. an array of int, is returned, not an array of Integer.  This 
   * exception for primitive types should improve performance as well as 
   * usability.
   *
   * @return a Java array containing the ordered elements of the SQL
   *    array designated by this object.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public Object getArray () throws java.sql.SQLException
  {
    // This is the only form of optimization so far.
    // If the whole array has already been read, return it.
    if (data_ != null)
      return data_;
    connection_.transactionStartedOnClient_ = true;
    data_ = remote_GET_ARRAY_SLICE (descriptor_.dimensionBounds_);
    connection_.transactionStartedOnServer_ = true;
    return (data_);
  }

  /**
   * Retrieve the contents of this SQL array as a Java array object using
   * <code>map</code> for type-map customization.
   * If the base type of the array does not
   * match a user-defined type in <code>map</code>, the standard
   * mapping is used instead.
   * <p>
   * Conceptually, this method calls <code>getObject()</code> on each element of the
   * array and returns a Java array containing the result. Except when
   * the array element type maps to a Java primitive type, such as int,
   * boolean, etc. In this case, an array of primitive type values, 
   * i.e. an array of int, is returned, not an array of Integer.  This 
   * exception for primitive types should improve performance as well as 
   * usability.
   *
   * @param map a <code>java.util.Map</code> object that contains mappings
   *            of SQL type names to classes in the Java programming language
   * @return an array in the Java programming language that contains the ordered
   *         elements of the SQL array designated by this object
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public Object getArray (java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Gets a Java array object containing a slice of this SQL array.
   * The array slice begins at the given <code>index</code> and
   * contains up to <code>count</code> successive elements of the SQL array.
   * This method uses the type-map
   * associated with the connection for customizations of the type-mappings.
   *
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @return an array containing up to <code>count</code> consecutive elements
   *   of the SQL array, beginning with element <code>index</code>
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public Object getArray (long index,
                                       int count) throws java.sql.SQLException
  {
    // No, i do not want to create one-dimensional array with 2 elements
    int[][] sliceBounds = descriptor_.getDimensions ();
    sliceBounds[0][0] = (int)index;
    sliceBounds[0][1] = (int)(index + count -1);
    boolean isWholeArray = descriptor_.checkSliceBounds (sliceBounds);
    // We remember the array data only if we read the whole array
    Object array = remote_GET_ARRAY_SLICE (sliceBounds);
    if (isWholeArray)
      data_ = array;
    return array;
  }

  /**
   * Gets a Java array object containing a slice of this SQL array using
   * a type-map customization.
   * The array slice begins at the given <code>index</code> and
   * contains up to <code>count</code> successive elements of the SQL array.
   * This method uses
   * the specified <code>map</code> for type-map customizations
   * unless the base type of the array does not match a user-
   * defined type in <code>map</code>, in which case it
   * uses the standard mapping.
   *
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @param map a <code>java.util.Map</code> object
   *   that contains SQL type names and the classes in
   *   the Java programming language to which they are mapped
   * @return an array containing up to <code>count</code>
   *   consecutive elements of the SQL array designated by this
   *   <code>Array</code> object, beginning with element <code>index</code>.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public Object getArray (long index,
                                       int count,
                                       java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Gets a result set that contains the elements of this array.
   * If appropriate,
   * the elements of the array are mapped using the connection's type
   * map; otherwise, the standard mapping is used.
   * <p>
   * The result set contains one row for each array element, with
   * two columns in each row.  The second column stores the element
   * value; the first column stores the index into the array for
   * that element (with the first array element being at index 1).
   * The rows are in ascending order corresponding to
   * the order of the indices.
   *
   * @return a {@link ResultSet} object containing one row for each
   *   of the elements in the array designated by this <code>Array</code>
   *   object, with the rows in ascending order based on the indices.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, proposed for future release, not yet supported</font>
   **/
  synchronized public java.sql.ResultSet getResultSet () throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Gets a result set that contains the elements of this array
   * using
   * <code>map</code> to map the array elements.  If the base
   * type of the array does not match a user-defined type in
   * <code>map</code>, the standard mapping is used instead.
   * <p>
   * The result set contains one row for each array element, with
   * two columns in each row.  The second column stores the element
   * value; the first column stores the index into the array for
   * that element (with the first array element being at index 1).
   * The rows are in ascending order corresponding to
   * the order of the indices.
   *
   * @param map contains mapping of SQL user-defined types to
   *   classes in the Java(tm) programming language
   * @return a <code>ResultSet</code> object containing one row for each
   *   of the elements in the array designated by this <code>Array</code>
   *   object, with the rows in ascending order based on the indices.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public java.sql.ResultSet getResultSet (java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Gets a result set holding the elements of the subarray that
   * starts at index <code>index</code> and contains up to
   * <code>count</code> successive elements.  This method uses
   * the connection's type map to map the elements of the array if
   * the map contains an entry for the base type. Otherwise, the
   * standard mapping is used.
   * <P>
   * The result set has one row for each element of the SQL array
   * designated by this object, with the first row containing the
   * element at index <code>index</code>.  The result set has
   * up to <code>count</code> rows in ascending order based on the
   * indices.  Each row has two columns:  The second column stores
   * the element value; the first column stroes the index into the
   * array for that element.
   *
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @return a <code>ResultSet</code> object containing up to
   *   <code>count</code> consecutive elements of the SQL array
   *   designated by this <code>Array</code> object, starting at
   *   index <code>index</code>.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, proposed for future release, not yet supported</font>
   **/
  synchronized public java.sql.ResultSet getResultSet (long index,
                                                       int count) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  /**
   * Returns a result set holding the elements of the subarray that
   * starts at index <code>index</code> and contains up to
   * <code>count</code> successive elements.  This method uses
   * the <code>Map</code> object <code>map</code> to map the elements
   * of the array unless the base type of the array does not match
   * a user-defined type in <code>map</code>, in which case it uses
   * the standard mapping.
   * <P>
   * The result set has one row for each element of the SQL array
   * designated by this object, with the first row containing the
   * element at index <code>index</code>.  The result set has
   * up to <code>count</code> rows in ascending order based on the
   * indices.  Each row has two columns:  The second column stores
   * the element value; the first column stroes the index into the
   * array for that element.
   *
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @param map the <code>Map</code> object that contains the mapping
   *   of SQL type names to classes in the Java(tm) programming language
   *   @return a <code>ResultSet</code> object containing up to
   *   <code>count</code> consecutive elements of the SQL array
   *   designated by this <code>Array</code> object, starting at
   *   index <code>index</code>.
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   * @since <font color=red>JDBC 2, not yet supported</font>
   **/
  synchronized public java.sql.ResultSet getResultSet (long index,
                                                       int count,
                                                       java.util.Map map) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__jdbc2_not_yet_supported__);
  }

  //--------------------------InterClient Extensions----------------------------

  /**
   * Gets a sub-array described by <code>sliceBounds</code> of this SQL array.
   * The slice is specified by a two-dimensional array <code>sliceBounds</code>.
   * The number of rows in this array must be  equal to the number of SQL array dimensions
   * and the number of columns is always two - lower and upper bounds for
   * each dimension.
   * For example, an array defined in InterBase as
   * <code>INTEGER[2:5][3:10][100:200]</code> is described by the following array of pairs:
   * <pre>
   * sliceBounds[0] = {2, 5}
   * sliceBounds[1] = {3, 10}
   * sliceBounds[2] = {100, 200}
   * </pre>
   *
   * @since <font color=red>Extension, proposed for InterClient 3.0</font>
   * @sliceBounds two dimensional array describing bounds of array slice
   * @return an array described by <code>sliceBounds</code>
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   */
// CJL-IB6 changed reference to InterClient 2.0
  synchronized public Object getArray (int[][] sliceBounds) throws java.sql.SQLException
  {
    boolean isWholeArray = descriptor_.checkSliceBounds (sliceBounds);
    Object array = remote_GET_ARRAY_SLICE (sliceBounds);
    // We remember the array data only if we read the whole array
    if (isWholeArray)
      data_ = array;
    return array;
  }

  /**
   * Gets a result set holding the elements of a sub-array, or slice,
   * described by <code>sliceBounds</code>.
   * The slice is specified by a two-dimensional array <code>sliceBounds</code>.
   * The number of rows in this array must be  equal to the number of SQL array dimensions
   * and the number of columns is always two - lower and upper bounds for
   * each dimension.
   * For example, an array defined in InterBase as
   * <code>INTEGER[2:5][3:10][100:200]</code> is described by the following array of pairs:
   * <pre>
   * sliceBounds[0] = {2, 5}
   * sliceBounds[1] = {3, 10}
   * sliceBounds[2] = {100, 200}
   * </pre>
   *
   * @since <font color=red>Extension, proposed for future release, not yet supported</font>
   * @sliceBounds two dimensional array describing bounds of array slice
   * @return a result set for a subarray described by <code>sliceBounds</code>
   * @throws java.sql.SQLException if an error occurs while attempting to access the array
   **/
  synchronized public java.sql.ResultSet getResultSet (int[][] sliceBounds) throws java.sql.SQLException
  {
    throw new DriverNotCapableException (ErrorKey.driverNotCapable__extension_not_yet_supported__);
  }
}
// MMM - end

