set -x

IC_BUILD_DIR=/dev/build
export IC_BUILD_DIR

IC_DEV_DIR=/dev
export IC_DEV_DIR

IC_PLATFORM=win32
export IC_PLATFORM

echo "buildOnPerdy.ksh should only be run from IC_DEV_DIR, never IC_BUILD_DIR."

echo "The file build.sh being used is from IC_DEV_DIR."
if $IC_DEV_DIR/sbin/build.ksh $1 $2 $3 $4
then 
     echo "InterClient Build SUCCEEDED"
else 
     echo "InterClient Build FAILED"
fi

