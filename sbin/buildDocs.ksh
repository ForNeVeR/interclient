set -x

if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" -o ! "$IC_PLATFORM" ]
then
  echo "IC_BUILD_DIR, IC_DEV_DIR, or IC_PLATFORM is not set."
  exit 1
fi

ROOT=$IC_DEV_DIR

if [ ! "$1" ]
then
  echo "You must supply a version string."
  exit 1
fi

CLASSPATH=$ROOT/packages
export CLASSPATH

if cd $ROOT
then : ; else exit 1 ; fi
if javadoc -classpath $CLASSPATH \
           -doctitle "InterClient $1" \
	   -header "$1" \
	   -footer "$1" \
	   -bottom "<font size=\"-1\">Send comments or suggestions to <a href=\"mailto:interclient@interbase.com\">interclient@interbase.com</a></font>" \
           -d docs/specifications \
	   -overview docs/javadoc_sources/overview.html \
           -linkoffline http://java.sun.com/products/jdk/1.2/docs/api docs/javadoc_sources \
	   -version \
           interbase.interclient interbase.interclient.utils ;
#          -linkoffline http://java.sun.com/products/jndi/javadoc ? \
#          -linkoffline http://java.sun.com/products/jdbc/html ? \
#          -link http://java.sun.com/products/jdk/1.2/docs/api \
#          -link http://java.sun.com/products/jndi/javadoc \
#          -link http://java.sun.com/products/jdbc/html \
#	   -author \
#	   -use \
then : ; else exit 1 ; fi

