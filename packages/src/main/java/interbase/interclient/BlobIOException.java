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
 * An exception has occurred while operating
 * on a blob stream.
 * <p>
 * The driver maintains the stream
 * by requesting network segments when needed,
 * managing blob requests against other JDBC requests,
 * converting stream data encodings, and flushing the blob
 * stream when necessary.
 * <p>
 * A variety of <code>SQLException</code>s, 
 * or <code>IOException</code>s
 * could occur within the driver during
 * a user-requested operation on a blob stream,
 * but all exceptions are surfaced to the user
 * of the stream as a java <code>IOException</code>,
 * because the user is operating on a
 * java.io.InputStream which is not distinguished
 * as a blob stream as part of the JDBC API.
 * <p>
 * Here are some typical reasons for a <code>BlobIOException</code>
 * <ul>
 * <li>Any <code>SQLException</code>, such as a
 *     <code>RemoteProtocolException</code> or 
 *     <code>CommunicationException</code> thrown during
 *     a <code>close()</code>, <code>read()</code>, or <code>skip()</code>
 *     on the stream would result in a BlobIOException being thrown for that operation.
 * <li>An attempt was made to <code>read()</code> or <code>skip()</code>
 *     on a stream which is closed.
 * <li>A call made to <code>reset</code> will result in a BlobIOException
 *     because <code>mark()</code> is not supported on blob streams.
 * <li>An improper blob data encoding was detected while reading from stream.
 * </ul>
 *
 * @docauthor Paul Ostler
 * @author Paul Ostler
 * @since <font color=red>Extension, since InterClient 1.50</font>
 **/
final public class BlobIOException extends java.io.IOException
{
  final private static String className__ = "BlobIOException";

  BlobIOException (String resourceKey,
		   String exceptionMessage)
  {
    super (Globals.getResource (resourceKey, new Object[] {exceptionMessage}) +
           Globals.getResource (ResourceKeys.seeApi) + className__);
  }

  BlobIOException (String resourceKey)
  {
    super (Globals.getResource (resourceKey) +
           Globals.getResource (ResourceKeys.seeApi) + className__);
  }
}
 
