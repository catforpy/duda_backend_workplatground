#!/bin/bash

# 文件上传测试脚本
# 支持指定目录，与 object_metadata 表中的路径一致

BASE_URL="http://localhost:8085"
BUCKET_NAME="duda-java-backend-test"
USER_ID=1001

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}======================================${NC}"
echo -e "${YELLOW}  文件上传测试 - 支持目录${NC}"
echo -e "${YELLOW}======================================${NC}"
echo ""

# 创建测试文件
TEST_DIR="/tmp/duda_test_upload"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"

echo -e "${GREEN}1. 创建测试文件...${NC}"
echo "Hello DudaNexus! 测试文件上传功能。" > "$TEST_DIR/test.txt"
echo "测试图片内容" > "$TEST_DIR/image.jpg"
echo "测试PDF文档" > "$TEST_DIR/document.pdf"

echo -e "${GREEN}✓ 测试文件已创建${NC}"
echo ""

# 测试场景1: 上传到根目录
echo -e "${YELLOW}2. 测试场景1: 上传到根目录${NC}"
curl -X POST "$BASE_URL/api/object/upload" \
  -F "bucketName=$BUCKET_NAME" \
  -F "userId=$USER_ID" \
  -F "files=@$TEST_DIR/test.txt" \
  2>/dev/null | python3 -m json.tool

echo ""
echo ""

# 测试场景2: 上传到指定目录（documents/2026/03/）
echo -e "${YELLOW}3. 测试场景2: 上传到指定目录 (documents/2026/03/)${NC}"
DIRECTORY="documents/2026/03/"
curl -X POST "$BASE_URL/api/object/upload" \
  -F "bucketName=$BUCKET_NAME" \
  -F "userId=$USER_ID" \
  -F "prefix=$DIRECTORY" \
  -F "files=@$TEST_DIR/document.pdf" \
  2>/dev/null | python3 -m json.tool

echo ""
echo ""

# 测试场景3: 上传多个文件到同一目录
echo -e "${YELLOW}4. 测试场景3: 上传多个文件到 images/avatars/ 目录${NC}"
DIRECTORY="images/avatars/"
curl -X POST "$BASE_URL/api/object/upload" \
  -F "bucketName=$BUCKET_NAME" \
  -F "userId=$USER_ID" \
  -F "prefix=$DIRECTORY" \
  -F "files=@$TEST_DIR/image.jpg" \
  -F "files=@$TEST_DIR/test.txt" \
  2>/dev/null | python3 -m json.tool

echo ""
echo ""

# 测试场景4: 自定义对象键（完整路径）
echo -e "${YELLOW}5. 测试场景4: 自定义完整对象键 (reports/annual/2026报告.pdf)${NC}"
curl -X POST "$BASE_URL/api/object/upload" \
  -F "bucketName=$BUCKET_NAME" \
  -F "userId=$USER_ID" \
  -F "objectKey=reports/annual/2026报告.pdf" \
  -F "files=@$TEST_DIR/document.pdf" \
  2>/dev/null | python3 -m json.tool

echo ""
echo ""

# 验证上传结果
echo -e "${YELLOW}6. 验证上传结果 - 查询文件信息${NC}"
echo -e "${GREEN}查询根目录文件:${NC}"
curl -s "$BASE_URL/api/object/info?bucketName=$BUCKET_NAME&objectKey=test.txt" | python3 -m json.tool

echo ""
echo -e "${GREEN}查询目录文件:${NC}"
curl -s "$BASE_URL/api/object/info?bucketName=$BUCKET_NAME&objectKey=documents/2026/03/document.pdf" | python3 -m json.tool

echo ""
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}  测试完成！${NC}"
echo -e "${GREEN}======================================${NC}"

# 清理测试文件
# rm -rf "$TEST_DIR"
echo -e "${YELLOW}提示: 测试文件保留在 $TEST_DIR${NC}"
