set -x

if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" -o ! "$IC_PLATFORM" ]
then
  echo "IC_BUILD_DIR, IC_DEV_DIR, or IC_PLATFORM is not set."
  exit 1
fi

if [ ! $1 ]
then
  echo "You must supply either -build or -dev switch."
  exit 1
fi

if [ $1 = "-build" ]
then
  ROOT=$IC_BUILD_DIR
elif [ $1 = "-dev" ]
then
  ROOT=$IC_DEV_DIR
else
  echo "Bad switch, you must supply either -build or -dev switch."
  exit 1
fi

if [ ! $2 ]
then
  echo "You must supply a version number."
  exit 1
fi

# !!! this doesn't port to Unix
CLASSPATH="$ROOT/packages;$ROOT/packages/javax.jar"
export CLASSPATH

# dev/docs needs to have already been built AND cleaned before invoking this script!

if cd $ROOT/deliverables
then : ; else exit 1 ; fi

if rm -rf docs ;
then : ; else exit 1 ; fi

if cp -r ../docs .
then : ; else exit 1 ; fi

if rm -rf docs/javadoc_sources ;
then : ; else exit 1 ; fi


echo "Making documentation set index"
echo "======================================================"

if [ $IC_PLATFORM = "win32" ]
then
  echo "Making deliverable interclient.dat and interclient.zip for JBuilder Help"
  echo "================================================================================="
  if mkdir jbuilder_help ;
  then : ; else exit 1 ; fi
  if cp ../jbuilder_help/interclient/interclient.dat jbuilder_help ;
  then : ; else exit 1 ; fi
  if cp ../jbuilder_help/interclient.zip jbuilder_help ;
  then : ; else exit 1 ; fi
fi

