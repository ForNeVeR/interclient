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

echo "Note: makeCreamedDeliverables should be called before makeJarDeliverable."

# Make a temporary resources directory
if cd $ROOT/deliverables
then : ; else exit 1 ; fi
if rm -rf resources ;
then : ; else exit 1 ; fi
if mkdir resources ;
then : ; else exit 1 ; fi
if mkdir resources/interbase ;
then : ; else exit 1 ; fi
if mkdir resources/interbase/interclient ;
then : ; else exit 1 ; fi
if mkdir resources/interbase/interclient/utils ;
then : ; else exit 1 ; fi

# make interclient.jar
if cd $ROOT/deliverables/classes ;
then : ; else exit 1 ; fi
if jar -0cvf interclient.jar interbase/interclient/*.class interbase/interclient/utils/*.class borland/jdbc/*.class com/inprise/sql/*.class ;
then : ; else exit 1 ; fi
if mv interclient.jar .. ;
then : ; else exit 1 ; fi

# temporarily move the resources classes into the resources directory
if cd $ROOT/deliverables
then : ; else exit 1 ; fi
if mv classes/interbase/interclient/Resources_*.class resources/interbase/interclient
then : ; else exit 1 ; fi
if mv classes/interbase/interclient/utils/Resources_*.class resources/interbase/interclient/utils
then : ; else exit 1 ; fi

# make interclient-core.jar and interclient-utils.jar
if cd $ROOT/deliverables/classes ;
then : ; else exit 1 ; fi
if jar -0cvf interclient-core.jar interbase/interclient/*.class borland/jdbc/*.class com/inprise/sql/*.class ;
then : ; else exit 1 ; fi
if jar -0cvf interclient-utils.jar interbase/interclient/utils/*.class ;
then : ; else exit 1 ; fi
if mv interclient-core.jar interclient-utils.jar .. ;
then : ; else exit 1 ; fi

# make interclient-res.jar
if cd $ROOT/deliverables/resources ;
then : ; else exit 1 ; fi
if jar -0cvf interclient-res.jar interbase/interclient/*.class interbase/interclient/utils/*.class ;
then : ; else exit 1 ; fi
if mv interclient-res.jar .. ;
then : ; else exit 1 ; fi

# move the resources back to the classes directory
if cd $ROOT/deliverables ;
then : ; else exit 1 ; fi
if mv resources/interbase/interclient/Resources_*.class classes/interbase/interclient
then : ; else exit 1 ; fi
if mv resources/interbase/interclient/utils/Resources_*.class classes/interbase/interclient/utils
then : ; else exit 1 ; fi

# delete the temporary resources directory
if cd $ROOT/deliverables ;
then : ; else exit 1 ; fi
if rm -rf resources ;
then : ; else exit 1 ; fi

# do not sign interclient.jar. This code is being left commented as
# reminder of how to do it, in case we need it later.
#if javakey -gs ../packages/interbase/interclient/InterBaseSoftwareCorp_sign_directive interclient.jar ;
#then : ; else exit 1 ; fi
#if mv -f interclient.jar.sig interclient.jar ;
#then : ; else exit 1 ; fi


