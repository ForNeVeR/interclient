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
 * The component that actually reconstructs a data source object when it is retrieved
 * from JNDI. References are needed since many naming services
 * don’t have the ability to store Java objects in their serialized form. When a data
 * source object is bound in this type of naming service the Reference for that object is
 * actually stored by the JNDI implementation, not the data source object itself.
 * <p>
 * A JNDI administrator is responsible for making sure that both the object factory and
 * data source implementation classes provided by a JDBC driver vendor are accessible to
 * the JNDI service provider at runtime.
 * <p> 
 * An object factory implements the javax.naming.spi.ObjectFactory interface. This
 * interface contains a single method, getObjectInstance, which is called by a JNDI
 * service provider to reconstruct an object when that object is retrieved from JNDI. A
 * JDBC driver vendor should provide an object factory as part of their JDBC 2.0 product.
 *
 * @see DataSource
 * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
 * @version JNDI 1.1.1
 **/
// CJL-IB6 changed reference to InterClient 2.0
public class ObjectFactory implements javax.naming.spi.ObjectFactory
{
  /**                                     
   * Reconstructs an InterClient data source object from a JNDI data source reference.
   * <p>
   * The getObjectInstance() method is passed a reference that corresponds to the object
   * being retrieved as its first parameter. The other parameters are optional in the case of
   * JDBC data source objects. The object factory should use the information contained in
   * the reference to reconstruct the data source. If for some reason, a data source object cannot
   * be reconstructed from the reference, a value of null may be returned. This allows
   * other object factories that may be registered in JNDI to be tried. If an exception is
   * thrown then no other object factories are tried.
   *
   * @since <font color=red>JDBC 2 Standard Extension, proposed for InterClient 3.0</font>
   **/
// CJL-IB6 changed reference to InterClient 2.0
  public Object getObjectInstance (Object refObj,
                                   javax.naming.Name name,
                                   javax.naming.Context nameCtx,
                                   java.util.Hashtable env) throws Exception
  {
    javax.naming.Reference ref = (javax.naming.Reference) refObj;
    if (ref.getClassName ().equals ("interbase.interclient.DataSource")) {
      DataSource sds = new DataSource ();
      
      // standard properties
      sds.setDatabaseName ((String) ref.get ("databaseName").getContent ());
      sds.setDataSourceName ((String) ref.get ("dataSourceName").getContent ());
      sds.setDescription ((String) ref.get ("description").getContent ());
      sds.setNetworkProtocol ((String) ref.get ("networkProtocol").getContent ());
      sds.setPassword ((String) ref.get ("password").getContent ());
      sds.setPortNumber (Integer.parseInt ((String) ref.get ("portNumber").getContent ()));
      sds.setRoleName ((String) ref.get ("roleName").getContent ());
      sds.setServerName ((String) ref.get ("serverName").getContent ());
      sds.setUser ((String) ref.get ("user").getContent ());

      // non-standard properties
      sds.setCharSet ((String) ref.get ("charSet").getContent ());
      sds.setSuggestedCachePages (Integer.parseInt ((String) ref.get ("suggestedCachePages").getContent ()));
      sds.setSweepOnConnect (new Boolean ((String) ref.get ("sweepOnConnect").getContent ()).booleanValue ());
      sds.setServerManagerHost ((String) ref.get ("serverManagerHost").getContent ());

      return sds;
    }
    else {
      return null;
    }
  }
}
