set -x

while read BINARY FILE
do
  case $BINARY in
    "dir" )
        : ;;
    "text" )
      if ! diff /dev/$FILE /dev1117/$FILE 
      then echo "****************************"
      fi ;;
    "binary" )
      if ! diff /dev/$FILE /dev1117/$FILE 
      then echo "****************************"
      fi ;;
  esac ;
done

  