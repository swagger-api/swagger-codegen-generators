#!/bin/bash

mvn clean package
if [ $? != 0 ]; then
    echo "err in swagger-codegen-generators mvn package"
    exit 0
fi


rm -fr ../swagger-codegen/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
rm -fr ../swagger-codegen/modules/swagger-codegen/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
rm -fr ../swagger-codegen/modules/swagger-codegen-cli/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
rm -fr ../swagger-codegen/modules/swagger-codegen-maven-plugin/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
rm -fr ../swagger-codegen/modules/swagger-generator/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar

cp target/swagger-codegen-generators-1.0.37-SNAPSHOT.jar  ../swagger-codegen/lib/  
cp target/swagger-codegen-generators-1.0.37-SNAPSHOT.jar  ../swagger-codegen/modules/swagger-codegen/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
cp target/swagger-codegen-generators-1.0.37-SNAPSHOT.jar  ../swagger-codegen/modules/swagger-codegen-cli/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
cp target/swagger-codegen-generators-1.0.37-SNAPSHOT.jar  ../swagger-codegen/modules/swagger-codegen-maven-plugin/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
cp target/swagger-codegen-generators-1.0.37-SNAPSHOT.jar  ../swagger-codegen/modules/swagger-generator/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar

old=`pwd`

# cd ../swagger-codegen/modules/swagger-codegen-cli 
cd ../swagger-codegen
mvn install:install-file -Dfile=lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar \
    -DgroupId=io.swagger.codegen.v3 -DartifactId=swagger-codegen-generators \
    -Dversion=1.0.37-SNAPSHOT -Dpackaging=jar

# cd ./modules/swagger-codegen-cli 
mvn clean install

if [ $? != 0 ]; then
    echo "err in swagger-codegen-cli mvn package"
    exit 0
fi


rm -fr ../zenkee/zfsgateway/build/swagger-codegen-cli.jar
cp ./modules/swagger-codegen-cli/target/swagger-codegen-cli.jar ../zenkee/zfsgateway/build/

cd ../zenkee/zfsgateway/build/
./code-gen-client.sh
if [ $? != 0 ]; then
    echo "err in mvn code-gen-client.sh"
    exit 0
fi


cd ../dist/

mv  ../../zfsclient/lib/dart_client/ ../../zfsclient/lib/dart_client.$(date +%Y-%m-%d-%H:%M:%S)
cp -r dart_client ../../zfsclient/lib/dart_client

cd $old

ls -l ../swagger-codegen/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
ls -l ../swagger-codegen/modules/swagger-codegen-cli/target/lib/swagger-codegen-generators-1.0.37-SNAPSHOT.jar
ls -l../zenkee/zfsgateway/build/swagger-codegen-cli.jar



