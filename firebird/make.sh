#! /bin/sh
#
# $Id$
#

set -x

if [ ! "$IC_PLATFORM" ]
then
  echo "IC_PLATFORM is not set."
  exit 1
fi

#if [ ! "$J2EE_HOME" ]
#then
#  echo "J2EE_HOME is not set."
#  exit 1
#fi

if [ ! $1 ]
then
  echo "You must supply either -build or -dev switch."
  exit 1
fi

if [ $1 = "-build" ]
then
	if [ ! "$IC_BUILD_DIR" ]
	then
  		echo "IC_BUILD_DIR is not set."
  		exit 1
	fi
  	ROOT=$IC_BUILD_DIR
elif [ $1 = "-dev" ]
then
	if [ ! "$IC_DEV_DIR" ]
	then
  		echo "IC_DEV_DIR is not set."
  		exit 1
	fi
  	ROOT=$IC_DEV_DIR
else
  echo "Bad switch, you must supply either -build or -dev switch."
  exit 1
fi

if [ $IC_PLATFORM = "solaris" ]
then
#  JAVAC=/usr/local/java/bin/javac
#  export JAVAC
#  CLASSPATH="$ROOT/packages:$ROOT/packages/javax.jar"
#  export CLASSPATH 
  PATH=$PATH:/opt/SUNWspro/bin:/usr/ccs/bin
  export PATH 
  LD_LIBRARY_PATH=/opt/SUNWspro/lib:/usr/local/interbase/lib
  export LD_LIBRARY_PATH 
elif [ $IC_PLATFORM = "hpux" ]
then
#  JAVAC=/usr/local/java/bin/javac
#  export JAVAC
#  CLASSPATH="$ROOT/packages:$ROOT/packages/javax.jar"
#  export CLASSPATH 
  PATH=$PATH:/opt/softbench/bin
  export PATH 
  LD_LIBRARY_PATH=/usr/local/interbase/lib
  export LD_LIBRARY_PATH 
elif [ $IC_PLATFORM = "linux" ]
then
	JAVAC=`which javac`
   export JAVAC
  CLASSPATH="$ROOT/packages:$ROOT/packages/javax.jar"
#
#  The j2ee license is restrictive, but there are deprecation errors using
#  the distributed javax.jar.  We should find a workaround so that j2ee.jar
#  is not required.
#
#  I've updated only those required classes in javax.jar to make it jdbc2
#  compliant.  
#
#	CLASSPATH="$ROOT/packages":$J2EE_HOME/lib/j2ee.jar

   export CLASSPATH 
   PATH=$PATH:/usr/bin
   export PATH 
   LD_LIBRARY_PATH=/usr/interbase/lib
   export LD_LIBRARY_PATH 
elif [ $IC_PLATFORM = "win32" ]
then
## JAVA_HOME is set only so that bcj can set its CLASSPATH
## JAVA_HOME=c:/jdk1.1.4 or d:/jdk1.2beta4
#  export JAVA_HOME
## JAVAC=c:/JBuilder2/bin/bmj
#  JAVAC=javac
#  export JAVAC
#  CLASSPATH="$ROOT/packages;$ROOT/packages/javax.jar"
#  export CLASSPATH 
  which make
  echo "Should be using borland make."
  which convert
  echo "Should be using borland convert."
  which echo
  which touch
else
  echo "IC_PLATFORM must be set to solaris, hpux, linux, or win32"
  exit 1
fi

umask 077

which $JAVAC
  
echo "Building InterServer"
echo "===================="
cd $ROOT/interserver
if [ $IC_PLATFORM = "solaris" ]
then
  rm -f Makefile
  if cat Makefile.solaris Makefile.unix > Makefile ;
  then : ; else exit 1 ; fi 
  if make clean ;
  then : ; else exit 1 ; fi 
  if make ;
  then : ; else exit 1 ; fi 
elif [ $IC_PLATFORM = "hpux" ]
then
  rm -f Makefile
  if cat Makefile.hp Makefile.unix > Makefile ;
  then : ; else exit 1 ; fi 
  if make clean ;
  then : ; else exit 1 ; fi 
  if make ;
  then : ; else exit 1 ; fi 
elif [ $IC_PLATFORM = "linux" ]
then
  rm -f Makefile
  if cat Makefile.linux Makefile.unix > Makefile ;
  then : ; else exit 1 ; fi 
  if make clean ;
  then : ; else exit 1 ; fi 
  if make ;
  then : ; else exit 1 ; fi 
elif [ $IC_PLATFORM = "win32" ]
then
  mkdir obj
  if make -fMakefile.w32 clean
  then : ; else exit 1 ; fi
  if make -fMakefile.w32 interserver.exe
  then : ; else exit 1 ; fi
  if [ ! $IC_SKIP_ISCONFIG ]
  then
    if make -DDELPHI -fMakefile.w32 isconfig.exe
    then : ; else exit 1 ; fi
  fi
else
  echo "IC_PLATFORM must be set to solaris, hpux, linux, or win32."
  exit 1
fi

# Old build code in which javac is invoked...
#if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "win32" ]
#then

#
# They must not have believed in keeping their unix JDK's current ;-)
#

echo "Building the borland.jdbc.SQLAdapter"
echo "================================="
cd $ROOT/packages/borland/jdbc
if rm -f *.class ;
then : ; else exit 1 ; fi
if $JAVAC -g:none -classpath $CLASSPATH *.java ;
then : ; else exit 1 ; fi
#

echo "Building the com.inprise.sql.SQLAdapter"
echo "================================="
cd $ROOT/packages/com/inprise/sql
if rm -f *.class ;
then : ; else exit 1 ; fi
if $JAVAC -g:none -classpath $CLASSPATH *.java ;
then : ; else exit 1 ; fi

echo "Building interbase.interclient"
echo "=============================="
cd $ROOT/packages/interbase/interclient
if rm -f *.class ;
then : ; else exit 1 ; fi
if $JAVAC -g:none -classpath $CLASSPATH *.java ;
then : ; else exit 1 ; fi

echo "Building interbase.interclient.utils"
echo "===================================="
cd $ROOT/packages/interbase/interclient/utils
if rm -f *.class ;
then : ; else exit 1 ; fi
if $JAVAC -g:none -classpath $CLASSPATH *.java ;
then : ; else exit 1 ; fi

#fi ended 'old' build

exit 0
