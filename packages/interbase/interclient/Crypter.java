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
 * @author Ravi Kumar
 **/
final class Crypter 
{
  private byte[] byteSalt_;
  private short[] shortSalt_;
  private int[] intSalt_;

  Crypter (int seed)
  {
    // should generate salts for all types
    // start with byte .... and then short, int
    // Strings are just sequence of bytes
    // arrays are to be encrypted as a sequence of primitive types

    // get low byte, should be the most varying
    byte byteSaltSeed = (byte) seed;
    byteSalt_ = new byte[10];
    for (int i = 0; i <10; i++)
      byteSalt_[i] = (byte) ((byteSaltSeed + i ) ^ (byteSaltSeed -i));

    // harcode salt for debug
    // for (int i = 0; i <10; i++)
    //  byteSalt_[i] = (byte) 123;

    // get low word, should be the most varying
    short shortSaltSeed = (short) seed;
    shortSalt_ = new short[10];
    for (int i = 0; i <10; i++)
      shortSalt_[i] = (short) ((shortSaltSeed + i ) ^ (shortSaltSeed -i));

    // get low word, should be the most varying
    int intSaltSeed = seed;
    intSalt_ = new int[10];
    for (int i = 0; i <10; i++)
      intSalt_[i] = (intSaltSeed + i ) ^ (intSaltSeed -i);
  }

  byte[] stringCrypt (String value)
  {
    byte[] b = new byte [value.length()];
    value.getBytes (0, value.length(), b, 0);
    for (int i = 0; i< b.length; i++) {
      b[i] = (byte) (b[i] ^ byteSalt_[i%9]);
    }
    return b;
  }

  private String stringDecrypt (byte[] b)
  {
    for (int i = 0; i< b.length; i++) {
      b[i] = (byte) (b[i] ^ byteSalt_[i%9]);
    }
    return new String (b, 0);
  }

  private byte byteCrypt (byte value)
  {
    return (byte) (value ^ byteSalt_[1]);
  }

  private short shortCrypt (short value)
  {
    return (short) (value ^ shortSalt_[2]);
  }

  private int intCrypt (int value)
  {
    return (int) (value ^ intSalt_[4]);
  }

}
