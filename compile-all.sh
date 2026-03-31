#!/bin/bash

echo "========================================="
echo "  DudaNexus 用户中台编译脚本"
echo "========================================="
echo ""

# 1. 清理并安装所有common模块
echo "[1/5] 安装duda-common模块..."
cd /Volumes/DudaDate/DudaNexus/duda-common
for module in duda-common-core duda-common-redis duda-common-rocketmq duda-common-database duda-common-web; do
    echo "  -> 安装 $module"
    cd /Volumes/DudaDate/DudaNexus/duda-common/$module
    mvn clean install -DskipTests -q || exit 1
done

echo ""
echo "[2/5] 安装duda-user-interface..."
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-interface
mvn clean install -DskipTests -q || exit 1

echo ""
echo "[3/5] 编译duda-user-provider..."
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn clean compile -DskipTests -q || exit 1

echo ""
echo "[4/5] 编译duda-user-api..."
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-api
mvn clean compile -DskipTests -q || exit 1

echo ""
echo "[5/5] 打包所有模块..."
cd /Volumes/DudaDate/DudaNexus/duda-usercenter
mvn clean package -DskipTests -q || exit 1

echo ""
echo "========================================="
echo "  ✅ 编译成功！"
echo "========================================="
echo ""
echo "编译产物位置："
echo "  - duda-user-interface/target/*.jar"
echo "  - duda-user-provider/target/*.jar"
echo "  - duda-user-api/target/*.jar"
echo ""
