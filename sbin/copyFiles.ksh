set -x

if [ $1 ];
then
  case $1 in
    "-build")
      if [ ! "$IC_BUILD_DIR"  -o ! "$IC_DEV_DIR" ]
      then
        echo "IC_BUILD_DIR, or IC_DEV_DIR is not set."
        exit 1
      fi
      REMOTE=""
      CP="cp"
      SRC=$IC_DEV_DIR
      TGT=$IC_BUILD_DIR
      B="" ;;
    "-perdy2pongo" )
      REMOTE=1
      CP="rcp"
      SRC="/dev"
      TGT="pongo:/postler/dev"
      B="-b" ;;
    "-pongo2perdy" )
      REMOTE=1
      CP="rcp"
      SRC="pongo:/postler/dev"
      TGT="/dev"
      B="-b" ;;
    "-gumby2perdy" )
      REMOTE=1
      CP="rcp"
      SRC="gumby:/parth/dev"
      TGT="/dev"
      B="-b" ;;
    "-perdy2gumby" )
      REMOTE=1
      CP="rcp"
      SRC="/dev"
      TGT="gumby:/parth/dev"
      B="-b" ;;
    *)
      echo "syntax error - bad switch"
      exit 1 ;;
  esac ;
else
  echo "syntax error - must provide switch"
  exit 1 ;
fi

while read BINARY FILE
do
  case $BINARY in
    "dir" )
      if [ ! "$REMOTE" -a ! -d $TGT/$FILE ];
      then
        if mkdir $TGT/$FILE ;
        then : ; else exit 1 ; fi ;
      fi;;
    "text" )
      if $CP $SRC/$FILE $TGT/$FILE ;
      then : ; else exit 1 ; fi ;;
    "binary" )
      if $CP $B $SRC/$FILE $TGT/$FILE ;
      then : ; else exit 1 ; fi ;;
  esac ;
done
exit 0

  