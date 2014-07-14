## Start
unzip -j websql-1.0-SNAPSHOT.war *.sh
chmod +x *.sh
./startup.sh
./shutdown.sh

## Config

unzip -j websql-1.0-SNAPSHOT.war WEB-INF/classes/application.properties
unzip -j websql-1.0-SNAPSHOT.war WEB-INF/classes/logback.xml