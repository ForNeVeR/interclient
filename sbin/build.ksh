set -x

echo "build.ksh should only be run from IC_DEV_DIR, never IC_BUILD_DIR."

if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" -o ! "$IC_PLATFORM" ]
then
  echo "IC_BUILD_DIR, IC_DEV_DIR, or IC_PLATFORM is not set."
  exit 1
fi

if [ ! $1 ]
then
  echo "You must supply a -setup or -nosetup switch."
  exit 1
fi

if [ ! $2 ]
then
  echo "You must a touch date."
  exit 1
fi

if [ ! $3 ]
then
  echo "You must a touch time."
  exit 1
fi

if [ ! $4 ]
then
  echo "You must an interclient version."
  exit 1
fi

echo "Setting up build directory"
echo "==========================="
echo "The file setupBuildDir.ksh being used is from IC_DEV_DIR"
if $IC_DEV_DIR/sbin/setupBuildDir.ksh $1 ;
then 
  echo "Build Dir setup ok." 
else
  echo "Build Dir setup NOT ok."
  exit 1
fi

echo "Making objects from sources"
echo "====================="
if chmod +x $IC_BUILD_DIR/sbin/make.ksh ;
then : ; else exit 1 ; fi
if $IC_BUILD_DIR/sbin/make.ksh -build ;
then : ; else exit 1 ; fi

echo "Making deliverables"
echo "====================="
if chmod +x $IC_BUILD_DIR/sbin/makeDeliverables.ksh ;
then : ; else exit 1 ; fi
if $IC_BUILD_DIR/sbin/makeDeliverables.ksh -build $2 $3 $4 ;
then : ; else exit 1 ; fi
  
exit 0
