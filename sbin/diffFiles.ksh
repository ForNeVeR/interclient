set -x

if [ $1 ];
then
  case $1 in
    "-pongo" )
      REMOTE_DEV="pongo:/postler/dev"
      LOCAL_DEV="/dev" ;;
    "-gumby" )
      REMOTE_DEV="gumby:/parth/dev"
      LOCAL_DEV="/dev" ;;
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
        : ;;
    "text" )
      if rcp $REMOTE_DEV/$FILE /tmp/tmpfile ;
      then : ; else exit 1 ; fi
      if ! diff $LOCAL_DEV/$FILE /tmp/tmpfile 
      then echo "********************************"
      fi ;;
    "binary" )
      if rcp -b $REMOTE_DEV/$FILE /tmp/tmpfile ;
      then : ; else exit 1 ; fi
      if ! diff $LOCAL_DEV/$FILE /tmp/tmpfile 
      then echo "********************************"
      fi ;; 
  esac ;
done

  