#! /bin/sh
#
# $Id$
#

set -x

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

if cd $ROOT/deliverables
then : ; else exit 1 ; fi

if rm -rf classes ;
then : ; else exit 1 ; fi
if mkdir classes ;
then : ; else exit 1 ; fi

# Old crema obfuscation stuff...
# if rm -f $ROOT/product/crema.map ;
# then : ; else exit 1 ; fi
#if java crema.Obfuscator -map -v -o -d classes -e .class \
#	  ../packages/interbase/interclient/*.class \
#	  ../packages/interbase/interclient/utils/*.class \
#	  ../packages/borland/jdbc/*.class \
#	  ../packages/com/inprise/sql/*.class \
#	  > $ROOT/product/crema.map ;
#then : ; else exit 1 ; fi

if mkdir classes/interbase ;
then : ; else exit 1 ; fi
if mkdir classes/interbase/interclient ;
then : ; else exit 1 ; fi
if mkdir classes/interbase/interclient/utils ;
then : ; else exit 1 ; fi
if mkdir classes/borland ;
then : ; else exit 1 ; fi
if mkdir classes/borland/jdbc ;
then : ; else exit 1 ; fi
if mkdir classes/com ;
then : ; else exit 1 ; fi
if mkdir classes/com/inprise ;
then : ; else exit 1 ; fi
if mkdir classes/com/inprise/sql ;
then : ; else exit 1 ; fi

if cp ../packages/interbase/interclient/*.class classes/interbase/interclient
then : ; else exit 1 ; fi
if cp ../packages/interbase/interclient/utils/*.class classes/interbase/interclient/utils
then : ; else exit 1 ; fi
if cp ../packages/borland/jdbc/*.class classes/borland/jdbc
then : ; else exit 1 ; fi
if cp ../packages/com/inprise/sql/*.class classes/com/inprise/sql
then : ; else exit 1 ; fi

