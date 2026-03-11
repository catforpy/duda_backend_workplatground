#!/bin/bash

echo "开始打包用户模块..."

cd /Volumes/DudaDate/DudaNexus

mvn clean package -DskipTests \
  -pl duda-usercenter/duda-user-interface,duda-usercenter/duda-user-api,duda-usercenter/duda-user-provider \
  -am

echo "打包完成！"
echo ""
echo "JAR文件位置："
echo "- duda-user-interface/target/duda-user-interface-1.0.0-SNAPSHOT.jar"
echo "- duda-user-api/target/duda-user-api-1.0.0-SNAPSHOT.jar"
echo "- duda-user-provider/target/duda-user-provider-1.0.0-SNAPSHOT.jar"
