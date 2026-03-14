#!/bin/bash

# =============================================
# 检查并修复MySQL字符集问题
# =============================================

MYSQL_HOST="120.26.170.213"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASS="duda2024"
MYSQL_DB="duda_file"

echo "╔════════════════════════════════════════╗"
echo "║     MySQL字符集检查工具                  ║"
echo "╚════════════════════════════════════════╝"
echo ""

# 1. 检查当前连接字符集
echo "步骤1: 检查MySQL连接字符集"
echo "========================================"
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB} <<EOF
SHOW VARIABLES LIKE 'character%';
SHOW VARIABLES LIKE 'collation%';
EOF

echo ""
echo "步骤2: 检查数据库字符集"
echo "========================================"
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB} <<EOF
SELECT
    DEFAULT_CHARACTER_SET_NAME,
    DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = '${MYSQL_DB}';
EOF

echo ""
echo "步骤3: 检查表字符集"
echo "========================================"
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB} <<EOF
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '${MYSQL_DB}'
ORDER BY TABLE_NAME;
EOF

echo ""
echo "步骤4: 修复数据库字符集"
echo "========================================"
echo "正在执行字符集修复..."
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB} <<EOF
-- 修复数据库字符集
ALTER DATABASE ${MYSQL_DB}
CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

-- 修复表字符集
ALTER TABLE bucket_config CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bucket_storage_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bucket_traffic_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bucket_billing_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bucket_billing_config CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

echo "✓ 字符集修复完成！"

echo ""
echo "步骤5: 验证修复结果"
echo "========================================"
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB} <<EOF
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '${MYSQL_DB}'
ORDER BY TABLE_NAME;
EOF

echo ""
echo "╔════════════════════════════════════════╗"
echo "║     字符集检查完成！                      ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "💡 说明："
echo "  1. 数据库字符集已修复为 utf8mb4"
echo "  2. 表字符集已修复为 utf8mb4_unicode_ci"
echo "  3. 如果注释还是乱码，请重启数据库连接"
echo ""
echo "🔗 Java连接字符串建议："
echo "  jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}"
echo "  ?useSSL=false"
echo "  &serverTimezone=Asia/Shanghai"
echo "  &useUnicode=true"
echo "  &characterEncoding=utf8"
echo ""
