set -x

if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" -o ! "$IC_PLATFORM" ]
then
  echo "IC_BUILD_DIR, IC_DEV_DIR, or IC_PLATFORM is not set."
  exit 1
fi

if [ ! "$1" ]
then
  echo "You must supply a touch date."
  exit 1
fi

if [ ! "$2" ]
then
  echo "You must supply a touch time."
  exit 1
fi

if [ ! "$3" ]
then
  echo "You must supply a build number."
  exit 1
fi

if [ ! "$4" ]
then
  echo "You must supply a destination directory."
  exit 1
fi

if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ] ;
then
  if [ ! "$5" ]
  then
    echo "You must supply a crypt password."
    exit 1
  fi
fi

ROOT=$IC_BUILD_DIR
# !!! this doesn't port to Unix
CLASSPATH="$IC_BUILD_DIR/packages;$IC_BUILD_DIR/packages/javax.jar"
export CLASSPATH 

umask 022

if cd $ROOT ;
then : ; else exit 1 ; fi

echo "Building product tar"
echo "===================================="
if [ $IC_PLATFORM = "win32" ] ;
then
  if mv $ROOT/install/windows/interserver/x86/cut/cd/setupex.exe $ROOT/product/IC$3.exe
  then : ; else exit 1 ; fi
  if mv $ROOT/install/windows/interserver/x86/cut/cd $ROOT/product
  then : ; else exit 1 ; fi
fi
if mkdir -p $4/IC$3-$IC_PLATFORM-product ;
then : ; else exit 1 ; fi
if cp -r product/* $4/IC$3-$IC_PLATFORM-product
then : ; else exit 1 ; fi
if cd $4/IC$3-$IC_PLATFORM-product ;
then : ; else exit 1 ; fi

if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ] ;
then
  if touch -t$1$2 *.*
  then : ; else exit 1 ; fi
elif [ $IC_PLATFORM = "win32" ] ;
then
  if touch -d$1 -t$2 *.*
  then : ; else exit 1 ; fi
  if cd cd ;
  then : ; else exit 1 ; fi
  if touch -d$1 -t$2 *
  then : ; else exit 1 ; fi
fi
if cd $4 ;
then : ; else exit 1 ; fi
if tar cvf IC$3-$IC_PLATFORM-product.tar IC$3-$IC_PLATFORM-product
then : ; else exit 1 ; fi

#echo "Cleaning up tree"
#echo "===================================="
if $ROOT/sbin/cleanTree.ksh -build ;
then : ; else exit 1 ; fi

echo "Building source tar"
echo "===================================="
if cd $IC_BUILD_DIR/.. ;
then : ; else exit 1 ; fi
if tar cvf IC$3-$IC_PLATFORM-src.tar build
then : ; else exit 1 ; fi
if mv IC$3-$IC_PLATFORM-src.tar $4
then : ; else exit 1 ; fi

echo "Compressing source tar file"
echo "===================================="
if cd $4 ;
then : ; else exit 1 ; fi
if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ] ;
then
  if compress IC$3-$IC_PLATFORM-src.tar ;
  then : ; else exit 1 ; fi
  if crypt $5 < IC$3-$IC_PLATFORM-src.tar.Z > IC$3-$IC_PLATFORM-src.tar.Z.crypt
  then : ; else exit 1 ; fi
elif [ $IC_PLATFORM = "win32" ] ;
then
  if gzip IC$3-$IC_PLATFORM-src.tar ;
  then : ; else exit 1 ; fi
else 
  echo "IC_PLATFORM is not set to a valid platform."
  exit 1;
fi

exit 0
