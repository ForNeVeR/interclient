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
  echo "You must supply a touch date"
  exit 1
fi

if [ ! $3 ]
then
  echo "You must supply a touch time."
  exit 1
fi

if [ ! $4 ]
then
  echo "You must supply an interclient version."
  exit 1
fi

# !!! this doesn't port to unix
CLASSPATH="$ROOT/packages;$ROOT/packages/javax.jar"
export CLASSPATH 

umask 022

echo "Cleaning up"
echo "===================================="
if rm -rf $ROOT/deliverables/* ;
then : ; else exit 1 ; fi

echo "Making a deliverable classes directory"
echo "===================================="
if chmod +x $ROOT/sbin/makeCreamedDeliverables.ksh ;
then : ; else exit 1 ; fi
if $ROOT/sbin/makeCreamedDeliverables.ksh $1 ;
then : ; else exit 1 ; fi

echo "Making deliverable jar files"
echo "========================================="
if chmod +x $ROOT/sbin/makeJarDeliverable.ksh ;
then : ; else exit 1 ; fi
if $ROOT/sbin/makeJarDeliverable.ksh $1 ;
then : ; else exit 1 ; fi

echo "Making a deliverable docs directory"
echo "================================="
if chmod +x $ROOT/sbin/makeDocDeliverables.ksh ;
then : ; else exit 1 ; fi
if $ROOT/sbin/makeDocDeliverables.ksh $1 $4 ;
then : ; else exit 1 ; fi

if cd $ROOT/deliverables
then : ; else exit 1 ; fi

#if cp ../packages/javax.jar . ;
#then : ; else exit 1 ; fi

echo "Making a deliverable examples directory"
echo "================================="
if mkdir examples ;
then : ; else exit 1 ; fi

if cp ../examples/*.* examples ;
then : ; else exit 1 ; fi

if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ]
then
  echo "Making deliverable License.txt install.sh, services.interclient, inetd.conf.interclient files"
  echo "================================================================================="
  if cp ../install/unix/install.sh . ;
  then : ; else exit 1 ; fi
  if chmod +x install.sh
  then : ; else exit 1 ; fi
  if cp ../install/unix/services.interclient . ;
  then : ; else exit 1 ; fi
  if cp ../install/unix/inetd.conf.interclient . ;
  then : ; else exit 1 ; fi
  if cp ../docs/readmes/License.txt . ;
  then : ; else exit 1 ; fi
  if cp ../docs/readmes/License.htm . ;
  then : ; else exit 1 ; fi
fi

echo "Making a deliverable bin directory file"
echo "=========================================================="
if mkdir bin ;
then : ; else exit 1 ; fi
if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ]
then
  if cp ../interserver/interserver bin ;
  then : ; else exit 1 ; fi
  if chmod +x bin/interserver
  then : ; else exit 1 ; fi
elif [ $IC_PLATFORM = "win32" ]
then
  if cp ../interserver/obj/interserver.exe bin
  then : ; else exit 1 ; fi
  if cp ../interserver/obj/isconfig.exe bin
  then : ; else exit 1 ; fi
# if cp ../interserver/isconfig.hlp bin
# then : ; else exit 1 ; fi
# if cp ../interserver/isconfig.cnt bin
# then : ; else exit 1 ; fi
else
  echo "IC_PLATFORM must be set to solaris, hpux, linux, or win32"
  exit 1
fi

#echo "Making deliverable CommDiag.html, and InterBaseSoftwareCorp.x509 files"
echo "Making deliverable CommDiag.html"
echo "================================================================================================"
#if cp ../packages/interbase/interclient/InterBaseSoftwareCorp.x509 . ;
#then : ; else exit 1 ; fi
if cp ../packages/interbase/interclient/utils/CommDiag.html . ;
then : ; else exit 1 ; fi

echo "Making all deliverables readable"
echo "================================"
if chmod -R go+r *
then : ; else exit 1 ; fi

echo "Touching all deliverables"
echo "========================="
if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ]
then
  if touch -t$2$3 * ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 bin/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 examples/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/images/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/examples/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/help/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/introduction* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/introduction/images/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/readmes/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/slide_show/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/slide_show/images/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/specifications/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/specifications/interbase/interclient/* ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 docs/specifications/interbase/interclient/utils/* ;
  then : ; else exit 1 ; fi
# no jbuilder_help dir on unix builds
#  if touch -t$2$3 jbuilder_help/* ;
#  then : ; else exit 1 ; fi
elif [ $IC_PLATFORM = "win32" ]
then
  if touch -d$2 -t$3 *.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 bin/* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 examples/* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/images/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/examples/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/help/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/introduction/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/introduction/images/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/readmes/* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/slide_show/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/slide_show/images/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/specifications/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/specifications/interbase/interclient/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 docs/specifications/interbase/interclient/utils/*.* ;
  then : ; else exit 1 ; fi
  if touch -d$2 -t$3 jbuilder_help/* ;
  then : ; else exit 1 ; fi
fi

if [ $IC_PLATFORM = "solaris" -o $IC_PLATFORM = "hpux" -o $IC_PLATFORM = "linux" ]
then
  echo "Tarring interclient driver classes, source and docs"
  echo "===================================================="
  /bin/rm -f icdriver.tar
  /bin/rm -f icdriver.tar.Z
  # removed InterBaseSoftwareCorp.x509 file
  if tar cvpf icdriver.tar interclient.jar interclient-core.jar interclient-res.jar interclient-utils.jar examples docs CommDiag.html ;
  then : ; else exit 1 ; fi
  if compress icdriver.tar ;
  then : ; else exit 1 ; fi
  if chmod 444 icdriver.tar.Z ;
  then : ; else exit 1 ; fi

  echo "Tarring interserver"
  echo "==================================================================="
  /bin/rm -f icserver.tar
  /bin/rm -f icserver.tar.Z
  if tar cvpf icserver.tar bin ;
  then : ; else exit 1 ; fi
  if compress icserver.tar ;
  then : ; else exit 1 ; fi
  if chmod 444 icserver.tar.Z ;
  then : ; else exit 1 ; fi

  echo "Tarring license, readmes, and all components"
  echo "==================================================================="
  /bin/rm -f interclient.tar
  if tar cvpf interclient.tar services.interclient inetd.conf.interclient icdriver.tar.Z icserver.tar.Z ;
  then : ; else exit 1 ; fi

  echo "Making installable tar file with install.sh and License.txt"
  echo "==================================================================="
  rm -rf interclient_install_temp_dir
  if mkdir interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if mv interclient.tar interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if mv install.sh interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if mv License.txt interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if mv License.htm interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if chmod 777 interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  /bin/rm -f InterClient.tar 
  if tar cvpf InterClient.tar interclient_install_temp_dir ;
  then : ; else exit 1 ; fi
  if chmod 444 InterClient.tar ;
  then : ; else exit 1 ; fi
 
  echo "Cleaning up"
  echo "==================================================================="
  /bin/rm -f *.tar.Z 
  /bin/rm -rf interclient_install_temp_dir 

  if mv InterClient.tar ../product ;
  then : ; else exit 1 ; fi
  if touch -t$2$3 ../product/InterClient.tar ;
  then : ; else exit 1 ; fi
  
elif [ $IC_PLATFORM = "win32" ]
then
  IS_DATA_DIR=$ROOT/install/windows/data/InterServer/x86
  if [ ! -d $IS_DATA_DIR ]
  then
    if mkdir -p $IS_DATA_DIR
	then : ; else exit 1 ; fi
  fi
  if cd $ROOT/deliverables
  then : ; else exit 1 ; fi

  if cp -mr bin $IS_DATA_DIR
  then : ; else exit 1 ; fi
  if cp -m CommDiag.html $IS_DATA_DIR
  then : ; else exit 1 ; fi
  if cp -m interclient.jar interclient-core.jar interclient-res.jar interclient-utils.jar $IS_DATA_DIR
  then : ; else exit 1 ; fi
  if cp -mr examples $IS_DATA_DIR
  then : ; else exit 1 ; fi
  if cp -mr docs $IS_DATA_DIR
  then : ; else exit 1 ; fi
  if cp -mr jbuilder_help $IS_DATA_DIR
  then : ; else exit 1 ; fi
fi
exit 0
