set -x
  
echo "setupBuildDir.ksh should only be run from IC_DEV_DIR, never IC_BUILD_DIR."

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

if [ $1 = "-nosetup" ]
then
  echo "Using existing Build Dir."
  exit 0
elif [ "$1" != "-setup" ]
then
  echo "Bad switch, you must supply a -setup or -nosetup switch."
  exit 1
fi

echo "Deleting $IC_BUILD_DIR"
if rm -rf $IC_BUILD_DIR ;
then : ; else exit 1 ; fi

if mkdir $IC_BUILD_DIR ;
then : ; else exit 1 ; fi

echo "Note: The files copyFiles.ksh and files.txt being used are from IC_DEV_DIR"
if $IC_DEV_DIR/sbin/copyFiles.ksh -build < $IC_DEV_DIR/sbin/files.txt ;
then : ; else exit 1 ; fi

exit 0
