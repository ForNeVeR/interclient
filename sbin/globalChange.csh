set -x

for f in CallableStatement.java \
         Connection.java \
         DatabaseMetaData.java \
         PreparedStatement.java \
         ResultSet.java \
         ResultSetMetaData.java \
         Statement.java
do
  sed 's/**\/ \/\/ start-jdbc2/--\/ \/\/ start-jdbc2/g' $f > $f.new1
  sed 's/\/\/ end-jdbc2/**\/ \/\/ end-jdbc2/g' $f.new1 > $f.new2
  
  mv -f $f.new2 $f
  rm -f $f.new1
done




