Readme (Readme.txt)
InterClient 1.60 Final Build
Last modified June 22, 2000

Contents:
     I.   Where's the Documentation?
     II.  What is InterClient?
     III. How Does InterClient Differ from DataGateway and Other Drivers?
     IV.  Compatibilities
     V.   How to Obtain InterClient Updates
     VI.  Configuration
     VII. Testing Your Configuration

__________________________________________________________________
Where's the Documentation?

The complete HTML documentation can be accessed from the file
index.html in your InterClient docs directory.  On Windows, the
InterClient program group contains an icon for the InterClient
documentation.

__________________________________________________________________
What is InterClient?

InterClient is an all-Java JDBC driver for InterBase.

InterClient is a networked driver, meaning that it incorporates a
JDBC remote protocol for exchanging and caching data between
client and server.  This allows for a browser enabled client with
no preinstalled client libraries (such as ODBC) to access
InterBase data across the net.  This differs from a JDBC bridge
which maps from the JDBC API to some native RDBMS client API or
ODBC.  A JDBC bridge implementation relies on the RDBMS client
library for the actual exchange and caching of data between
server and client.

The advantage of a networked driver over a bridge implementation
is that the client is 100% pure Java, thereby providing for
cross-platform, robust, and secure applets.  Whereas a bridge
implementation requires some binary code to be pre-loaded on each
client machine, contrary to the notion of an applet.

The JDBC remote protocol employed by InterClient is streamlined
for JDBC data access, and is database independent except in cases
where InterBase access can be optimized or proprietary InterBase
features may be leveraged.

__________________________________________________________________
How Does InterClient Differ from DataGateway and Other Drivers

The DataGateway Broker was derived from InterClient.  But
DataGateway's server speaks BDE on the backend or middle tier.
InterServer speaks ISC on the server backend or middle tier.
Also, since InterClient/InterServer is middleware intended only
for InterBase, it can be, and is, streamlined for InterBase.
Being an all Java type 3 driver provides much more flexibility than
a type 4 jdbc driver (which is pinned to a legacy protocol).
These things give rise to a driver which we believe to be the fastest
driver on the market today, often being orders of magnitude faster
than drivers such as jConnect and dbANYWHERE.

__________________________________________________________________
Compatibilities

Neither InterBase nor InterServer need to be installed on
the client machine, nor even InterClient if running applets.

InterClient 1.60 is compatible with the
    Java Runtime Environment (JRE) 1.2
    InterServer 1.60 and InterBase v5 and v6

InterServer 1.51 has been tested with
    Windows 95/98, NT 4.0, and Windows2000
    Solaris 2.5.x
    HP-UX 10.x
    Linux Redhat

__________________________________________________________________
How to Obtain InterClient Updates

Periodic revisions are made available on the InterBase web site
at http://www.interbase.com.

__________________________________________________________________
Configuration

   On both Windows and Unix, the following
   entry must appear in the services file.

             interserver 3060/tcp

   The TCP/IP services file was modified as a part of the
   installation.  You can add the above line manually if you
   decided not to have setup make the modifications for you.

   The services file can be located as follows:
    *  Windows NT         <WINDOWS_DIR>\system32\drivers\etc\services
    *  Windows 95/98      <WINDOWS_DIR>\services
    *  Unix               /etc/services or an NIS services map
                
   For Unix only, an entry in the /etc/inetd.conf file is also required:

     interserver stream tcp nowait root /usr/interclient/bin/interserver 
interserver

   The CLASSPATH environment variable should be updated to include
           <INSTALL_DIR>\interclient.jar

   These configurations should be performed automatically by the
   Windows install program.  The class path configuration for
   Unix must be performed manually.

__________________________________________________________________
Testing Your Configuration

   See the Troubleshooting section of the InterClient Help html for
   detailed instructions on testing your machine configuration for
   running InterClient applications using

     java interbase.interclient.utils.CommDiag

   and for instructions on testing your applet configuration using

     CommDiag.html

