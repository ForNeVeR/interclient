set -x

if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" -o ! "$IC_PLATFORM" ]
then
  echo "IC_BUILD_DIR, IC_DEV_DIR, or IC_PLATFORM is not set."
  exit 1
fi

if [ ! "$1" ]
then
  echo "You must supply a -build or -dev switch."
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

echo "Cleaning up tree"
echo "===================================="
if cd $ROOT ;
then : ; else exit 1 ; fi
if rm -rf deliverables/* ;
then : ; else exit 1 ; fi
if rm -rf install/windows/data ;
then : ; else exit 1 ; fi
if rm -rf install/windows/interserver ;
then : ; else exit 1 ; fi
if rm -rf product/* ;
then : ; else exit 1 ; fi
if cd $ROOT/packages/interbase/interclient ;
then : ; else exit 1 ; fi
if rm -f *.class;
then : ; else exit 1 ; fi
if cd $ROOT/packages/interbase/interclient/utils ;
then : ; else exit 1 ; fi
if rm -f *.class;
then : ; else exit 1 ; fi
if cd $ROOT/packages/borland/jdbc ;
then : ; else exit 1 ; fi
if rm -f *.class;
then : ; else exit 1 ; fi
if cd $ROOT/packages/com/inprise/sql ;
then : ; else exit 1 ; fi
if rm -f *.class;
then : ; else exit 1 ; fi
if cd $ROOT/interserver ;
then : ; else exit 1 ; fi
if [ $IC_PLATFORM = "win32" ] ;
then
  make -fmakefile.w32 clean
elif [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ] ;
then
  make clean
  if rm -f interserver gen_license ;
  then : ; else exit 1 ; fi
else
  echo "IC_PLATFORM is not set to a valid platform."
  exit 1;
fi

exit 0
