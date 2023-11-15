#!/bin/zsh
DIR=$(pwd)

export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
DB_CREATION_SQL_DIR="docker/provision/mysql/init"
DB_CREATION_SQL_PATH="$DB_CREATION_SQL_DIR/01-database.sql"

if [ -d "$DB_CREATION_SQL_DIR" ]; then
  echo "$DB_CREATION_SQL_DIR exists"
else
  echo "creating dir $DB_CREATION_SQL_DIR"
  mkdir -pv $DB_CREATION_SQL_DIR
fi

echo "" > $DB_CREATION_SQL_PATH
echo "CREATE USER \`$MYSQL_USERNAME\`@\`localhost\` IDENTIFIED BY 'local';" >> $DB_CREATION_SQL_PATH
for db in $(cat databases-list.txt); do
echo "CREATE DATABASE IF NOT EXISTS \`$db\`;" >> $DB_CREATION_SQL_PATH
echo "GRANT ALL PRIVILEGES ON \`$db\`.* TO \`$MYSQL_USERNAME\`@\`%\`;" >> $DB_CREATION_SQL_PATH
done

docker compose up -d
sleep 10
rm -rf $DB_CREATION_SQL_PATH