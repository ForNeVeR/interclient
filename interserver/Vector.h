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
#ifndef _VECTOR_H_
#define _VECTOR_H_

/**
 * Vector class (a growable array).
 * 
 * Each vector tries to optimize storage management by maintaining
 * a capacity and a capacityIncrement. The capacity is always at
 * least as large as the vector size; it is usually larger because
 * as elements are added to the vector, the vector's
 * storage increases in chunks the size of capacityIncrement. Setting
 * the capacity to what you want before inserting a large number of
 * objects will reduce the amount of incremental reallocation.
 * You can safely ignore the capacity and the vector will still work
 * correctly.
 */

#define VECTOR_ELEMENT void*
#define NULL 0

class VectorEnumeration;

class Vector 
{

private:

  void fill (VECTOR_ELEMENT* anArray)
  {
    VECTOR_ELEMENT* p = elementData_;
    VECTOR_ELEMENT* q = anArray;

    for (int i=0; i < elementCount_; i++)
      *p++ = *q++;
  }

protected:

  friend class VectorEnumeration;

  /**
   * The buffer where elements are stored.
   */
  VECTOR_ELEMENT* elementData_;

  int capacity_;

  /**
   * The number of elements in the buffer.
   */
  int elementCount_;

  /**
   * The size of the increment. If it is 0 the size of the
   * the buffer is doubled everytime it needs to grow.
   */
  int capacityIncrement_;

public:

  /**
   * Constructs an empty vector with the specified storage
   * capacity and the specified capacityIncrement.
   * @param initialCapacity the initial storage capacity of the vector
   * @param capacityIncrement how much to increase the element's 
   * size by.
   */
  Vector (int initialCapacity, int capacityIncrement) 
    : capacityIncrement_ (capacityIncrement),
      capacity_ (initialCapacity),
      elementCount_ (0)
  {
    elementData_ = new VECTOR_ELEMENT[capacity_];
  }

  /**
   * Constructs an empty vector with the specified storage capacity.
   * @param initialCapacity the initial storage capacity of the vector
   */
  Vector (int initialCapacity)
    : capacityIncrement_ (0),
      capacity_ (initialCapacity),
      elementCount_ (0)
  {
    elementData_ = new VECTOR_ELEMENT[capacity_];
  }

  /**
   * Constructs an empty vector.
   */
  Vector ()
    : capacityIncrement_ (0),
      capacity_ (10),
      elementCount_ (0)
  {
    elementData_ = new VECTOR_ELEMENT[capacity_];
  }

  ~Vector ();

  /**
   * Copies the elements of this vector into the specified array.
   * @param anArray the array where elements get copied into
   */
  void copyInto (VECTOR_ELEMENT* anArray) 
  {
    int i = elementCount_;
    VECTOR_ELEMENT* p = anArray;
    VECTOR_ELEMENT* q = elementData_;
    while (i-- > 0) {
      *p++ = *q++;
    }
  }

  /**
   * Trims the vector's capacity down to size. Use this operation to
   * minimize the storage of a vector. Subsequent insertions will
   * cause reallocation.
   */
  void trimToSize () 
  {
    if (elementCount_ == 0) {
      delete elementData_;
      elementData_ = NULL;
      return;
    }

    if (elementCount_ < capacity_) {
      VECTOR_ELEMENT* oldData = elementData_;
      elementData_ = new VECTOR_ELEMENT[elementCount_];
      capacity_ = elementCount_;
      fill (oldData);
      delete oldData;
    }
  }

  /**
   * Ensures that the vector has at least the specified capacity.
   * @param minCapacity the desired minimum capacity
   */
  void ensureCapacity (int minCapacity) 
  {
    if (minCapacity > capacity_) {
      VECTOR_ELEMENT* oldData = elementData_;
      int newCapacity = (capacityIncrement_ > 0) ?
	(capacity_ + capacityIncrement_) : (capacity_ * 2);
      if (newCapacity < minCapacity) {
	newCapacity = minCapacity;
      }
      elementData_ = new VECTOR_ELEMENT[newCapacity];
      capacity_ = newCapacity;
      fill (oldData);
      delete oldData;
    }
  }

  /**
   * Sets the size of the vector. If the size shrinks, the extra elements
   * (at the end of the vector) are lost.
   * @param newSize the new size of the vector
   */
  void setSize (int newSize) 
  {
    if (newSize > elementCount_)
      ensureCapacity (newSize);
    elementCount_ = newSize;
  }

  /**
   * Returns the current capacity of the vector.
   */
  int capacity () 
  {
    return capacity_;
  }

  /**
   * Returns the number of elements in the vector.
   * Note that this is not the same as the vector's capacity.
   */
  int size () 
  {
    return elementCount_;
  }

  /**
   * Returns true if the collection contains no values.
   */
  int isEmpty () 
  {
    return elementCount_ == 0;
  }

  /**
   * Returns true if the specified object is a value of the 
   * collection.
   * @param elem the desired element
   */
  int contains (VECTOR_ELEMENT elem) 
  {
    return indexOf (elem, 0) >= 0;
  }

  /**
   * Searches for the specified object, starting from the first position
   * and returns an index to it.
   * @param elem the desired element
   * @return the index of the element, or -1 if it was not found.
   */
  int indexOf (VECTOR_ELEMENT elem) 
  {
    return indexOf (elem, 0);
  }

  /**
   * Searches for the specified object, starting at the specified 
   * position and returns an index to it.
   * @param elem the desired element
   * @param index the index where to start searching
   * @return the index of the element, or -1 if it was not found.
   */
  int indexOf (VECTOR_ELEMENT elem, int index) 
  {
    for (int i = index ; i < elementCount_ ; i++) {
      if (elem == *(elementData_+i)) {
	return i;
      }
    }
    return -1;
  }

  /**
   * Searches backwards for the specified object, starting from the last
   * position and returns an index to it. 
   * @param elem the desired element
   * @return the index of the element, or -1 if it was not found.
   */
  int lastIndexOf (VECTOR_ELEMENT elem) 
  {
    return lastIndexOf (elem, elementCount_);
  }

  /**
   * Searches backwards for the specified object, starting from the specified
   * position and returns an index to it. 
   * @param elem the desired element
   * @param index the index where to start searching
   * @return the index of the element, or -1 if it was not found.
   */
  int lastIndexOf (VECTOR_ELEMENT elem, int index) 
  {
    for (int i = index ; --i >= 0 ; ) {
      if (elem == *(elementData_+i)) {
	return i;
      }
    }
    return -1;
  }

  /**
   * Returns the element at the specified index.
   * @param index the index of the desired element
   * @exception ArrayIndexOutOfBoundsException If an invalid 
   * index was given.
   */
  VECTOR_ELEMENT elementAt (int index) 
  {
    return *(elementData_+index);
  }

  /**
   * Returns the first element of the sequence.
   * @exception NoSuchElementException If the sequence is empty.
   */
  VECTOR_ELEMENT firstElement () 
  {
    return *elementData_;
  }

  /**
   * Returns the last element of the sequence.
   * @exception NoSuchElementException If the sequence is empty.
   */
  VECTOR_ELEMENT lastElement () 
  {
    return *(elementData_ + elementCount_ - 1);
  }

  /**
   * Sets the element at the specified index to be the specified object.
   * The previous element at that position is discarded.
   * @param obj what the element is to be set to
   * @param index the specified index
   * @exception ArrayIndexOutOfBoundsException If the index was 
   * invalid.
   */
  void setElementAt (VECTOR_ELEMENT obj, int index) 
  {
    *(elementData_ + index) = obj;
  }

  /**
   * Deletes the element at the specified index. Elements with an index
   * greater than the current index are moved down.
   * @param index the element to remove
   * @exception ArrayIndexOutOfBoundsException If the index was invalid.
   */
  void removeElementAt (int index) 
  {
    int loopCount = elementCount_ - index - 1;
    VECTOR_ELEMENT* p = elementData_ + index;
    VECTOR_ELEMENT* q = elementData_ + index + 1;

    while (loopCount-- > 0)
      *p++ = *q++;
    elementCount_--;
  }

  void removeFirstElement ()
  {
    removeElementAt (0);
  }

  void removeLastElement ()
  {
    elementCount_--;
  }

  /**
   * Inserts the specified object as an element at the specified index.
   * Elements with an index greater or equal to the current index 
   * are shifted up.
   * @param obj the element to insert
   * @param index where to insert the new element
   */
  void insertElementAt (VECTOR_ELEMENT obj, int index)
  {
    ensureCapacity (elementCount_ + 1);
    int loopCount = elementCount_ - index;
    VECTOR_ELEMENT* p = elementData_ + elementCount_ - 1;
    VECTOR_ELEMENT* q = elementData_ + elementCount_;
    while (loopCount-- > 0)
      *q-- = *p--;
    *q = obj;
    elementCount_++;
  }

  /**
   * Adds the specified object as the last element of the vector.
   * @param obj the element to be added
   */
  void addElement (VECTOR_ELEMENT obj)
  {
    ensureCapacity (elementCount_ + 1);
    *(elementData_ + elementCount_++) = obj;
  }

  /**
   * Removes the element from the vector. If the object occurs more
   * than once, only the first is removed. If the object is not an
   * element, returns false.
   * @param obj the element to be removed
   * @return true if the element was actually removed; false otherwise.
   */
  int removeElement (VECTOR_ELEMENT obj) 
  {
    int i = indexOf (obj);
    if (i >= 0) {
      removeElementAt (i);
      return 1;
    }
    return 0;
  }

  /**
   * Removes all elements of the vector. The vector becomes empty.
   */
  void removeAllElements ()
  {
    elementCount_ = 0;
  }

};

class VectorEnumeration 
{

private:

  Vector& vector_;
  int count_;

public:

  VectorEnumeration (Vector& vector) 
    : vector_ (vector),
      count_ (0)
  { }

  int hasMoreElements()
  {
    return count_ < vector_.elementCount_;
  }

  VECTOR_ELEMENT nextElement()
  {
    if (count_ < vector_.elementCount_)
      return *(vector_.elementData_ + count_++);
    else
      return NULL;
  }

};

#endif



