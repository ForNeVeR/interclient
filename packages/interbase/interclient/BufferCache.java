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
 * Cache used for saving output message buffers and byte arrays
 * for reuse, thereby avoiding frequent calls to java's new allocator
 * when receiving or sending messages.
 * !! Future enhancement - replace Vector of buffers with a btree
 * !! for fast retrieval of minimum sized buffer.
 *
 * @author Paul Ostler
 **/
final class BufferCache
{
  static private final int minBufferLength__ = 1024;
  static private final int minCharBufferLength__ = 256;

  // An upper limit on the number of bytes of cached memory.
  // This value is increased as more connections are added.
  static private int maxMemoryCached__ = 500000; // initial 1/2 meg

  static private int maxMemoryCachedPerConnection__ = 500000;

  // Actually this is 1 greater than the number of connections.
  private int numConnections_ = 1;

  // We start out with no cached memory but this is easily tweaked.
  private int memoryCached_ = 0;

  // A vector of cached buffers, 
  // sorted in increasing size, initially empty.
  private java.util.Vector cachedBuffers_ = new java.util.Vector (12, 12);

  private java.util.Vector cachedOutputBuffers_ = new java.util.Vector (12, 12);

  private java.util.Vector cachedCharBuffers_ = new java.util.Vector (12, 12);

  BufferCache ()
  {  }

  synchronized void incrementConnectionCount ()
  {
    numConnections_++;
    maxMemoryCached__ += maxMemoryCachedPerConnection__;
  }

  synchronized void decrementConnectionCount ()
  {
    numConnections_--;
    maxMemoryCached__ -= maxMemoryCachedPerConnection__;
  }

  // take the first available cached output buffer
  synchronized MessageBufferOutputStream takeOutputBuffer ()
  {
    if (cachedOutputBuffers_.isEmpty ()) {
      if (Globals.debug__) Globals.trace ("mallocing new message buffer for outgoing messages");
      return new MessageBufferOutputStream ();
    }
    else {
      MessageBufferOutputStream outputBuffer = (MessageBufferOutputStream) cachedOutputBuffers_.firstElement ();
      cachedOutputBuffers_.removeElementAt (0);
      return outputBuffer;
    }
  }

  synchronized void returnOutputBuffer (MessageBufferOutputStream outputBuffer)
  {
    outputBuffer.reset ();
    cachedOutputBuffers_.addElement (outputBuffer);
  }

  synchronized byte[] takeBuffer (int length)
  {
    byte[] buffer;

    // Find the smallest cached buffer that accomodates requested length (ie. >= length).
    for (int i=0; i < cachedBuffers_.size (); i++) {
      buffer = (byte[]) cachedBuffers_.elementAt (i);
      if (buffer.length >= length) {
        cachedBuffers_.removeElementAt (i);
        memoryCached_ -= buffer.length;
        return buffer;
      }
    }

    // Ok, there's no cached buffer large enough so ask O/S for memory.
    if (Globals.debug__) Globals.trace ("mallocing new byte array");
    return new byte [Math.max (length, minBufferLength__)];
  }

  synchronized void returnBuffer (byte[] buffer)
  {
    if (buffer == null)
      return;

    // Insert msg buffer into cache in ascending order (WRT to buffer length).
    boolean inserted = false;
    memoryCached_ += buffer.length;
    for (int i=0; i < cachedBuffers_.size (); i++) {
      if (((byte[]) cachedBuffers_.elementAt (i)).length > buffer.length) {
        cachedBuffers_.insertElementAt (buffer, i);
        inserted = true;
        break;
      }
    }
    if (!inserted) // largest buffer so far
      cachedBuffers_.addElement (buffer);  // insert at end

    // If maxMemoryCached__ is exceeded, then remove smallest buffers first.
    // Its best to keep the larger buffers around.
    // Always keep at least numConnections_ buffers around even if maxMemoryCached__ is exceeded.
    //while ((memoryCached_ > maxMemoryCached__) && 
    //       (cachedBuffers_.size() > numConnections_)) {
    //  memoryCached_ -= ((byte[]) cachedBuffers_.firstElement()).length;
    //  cachedBuffers_.removeElementAt (0);
      // The removed buffer will be garbage collected if, after return,
      // the calling routine sets buffer = null
    //}
  }

  synchronized char[] takeCharBuffer (int length)
  {
    char[] buffer;

    // Find the smallest cached buffer that accomodates requested length (ie. >= length).
    for (int i=0; i < cachedCharBuffers_.size (); i++) {
      buffer = (char[]) cachedCharBuffers_.elementAt (i);
      if (buffer.length >= length) {
        cachedCharBuffers_.removeElementAt (i);
        return buffer;
      }
    }

    // Ok, there's no cached buffer large enough so ask O/S for memory.
    if (Globals.debug__) Globals.trace ("mallocing new char array");
    return new char [Math.max (length, minCharBufferLength__)];
  }

  // Eventually, we need to worry about clutter and a maxMemoryCached for cbufs
  synchronized void returnCharBuffer (char[] buffer)
  {
    if (buffer == null)
      return;

    // Insert msg buffer into cache in ascending order (WRT to buffer length).
    boolean inserted = false;
    for (int i=0; i < cachedCharBuffers_.size (); i++) {
      if (((char[]) cachedCharBuffers_.elementAt (i)).length > buffer.length) {
        cachedCharBuffers_.insertElementAt (buffer, i);
        inserted = true;
        break;
      }
    }
    if (!inserted) // largest buffer so far
      cachedCharBuffers_.addElement (buffer);  // insert at end
  }
}
