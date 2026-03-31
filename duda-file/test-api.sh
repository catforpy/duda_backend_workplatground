#!/bin/bash

# duda-file API 功能测试脚本

BASE_URL="http://localhost:8085"
BUCKET_NAME="duda-aliyun-backend"
USER_ID=1

echo "=== 1. 测试获取Bucket列表 ==="
curl -s "$BASE_URL/api/bucket/list?userId=$USER_ID&keyName=test-key" | python3 -m json.tool

echo -e "\n=== 2. 测试列出Bucket对象 ==="
curl -s "$BASE_URL/api/object/list?bucketName=$BUCKET_NAME&maxKeys=10" | python3 -m json.tool

echo -e "\n=== 3. 测试获取对象信息 ==="
curl -s "$BASE_URL/api/object/info?bucketName=$BUCKET_NAME&objectKey=case-banner.png" | python3 -m json.tool

echo -e "\n=== 4. 测试检查对象是否存在 ==="
curl -s "$BASE_URL/api/object/exists?bucketName=$BUCKET_NAME&objectKey=case-banner.png&userId=$USER_ID" | python3 -m json.tool

echo -e "\n=== 5. 测试获取STS临时凭证 ==="
curl -s -X POST "$BASE_URL/api/object/sts-token" \
  -H "Content-Type: application/json" \
  -d "{\"bucketName\":\"$BUCKET_NAME\",\"userId\":$USER_ID}" \
  | python3 -m json.tool

echo -e "\n=== 测试完成 ==="
