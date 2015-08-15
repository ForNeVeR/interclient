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

// Unfold this class into JDBCNet, likewise for BlobOutput

/**
 * @author Paul Ostler
 **/
class BlobInput
{
  int a1;
  final static int a2 = 1;
  final static int a3 = 0;

  // For JDBCNet only, this should really be remote_CREATE_BLOB
  BlobInput (JDBCNet p1,
	     Statement p2,
             int p3,
	     java.io.InputStream p4,
	     int p5) throws java.sql.SQLException
  {
    RecvMessage l1 = null;

    try {
      MessageBufferOutputStream l2 = p1.createMessage ();
      l2.write (MessageCodes.CREATE_BLOB__);
      l2.writeInt (p2.statementRef_);
      l2.writeInt (p5);
      if (p3 == a2)
        l1 = p1.sendUnicodeStreamAndReceiveMessage (l2, p4, p5);
      else
        l1 = p1.sendStreamAndReceiveMessage (l2, p4, p5);
      if (!l1.get_SUCCESS ()) {
	throw l1.get_EXCEPTIONS ();
      }
      a1 = l1.readInt ();
    }
    finally {
      p1.destroyRecvMessage (l1);
    }
  }
}


