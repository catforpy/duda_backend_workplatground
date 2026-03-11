# =============================================
# 一键部署 MySQL 数据库
# 适用于: Ubuntu/Debian/CentOS
# =============================================

#!/bin/bash

set -e  # 遇到错误立即退出

echo "========================================="
echo "DudaNexus MySQL 一键部署脚本"
echo "========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
MYSQL_ROOT_PASSWORD="Duda@2025"
MYSQL_ADMIN_USER="duda_admin"
MYSQL_ADMIN_PASSWORD="Duda@2025"
DATABASE_NAME="duda_nexus"
BACKUP_DIR="/data/mysql/backup"
SCRIPT_DIR="/data/scripts"

# 检测操作系统
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    echo -e "${RED}无法检测操作系统类型${NC}"
    exit 1
fi

echo "检测到操作系统: $OS"

# 步骤1: 安装MySQL
echo ""
echo "========================================="
echo "步骤 1/7: 安装 MySQL"
echo "========================================="

if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    echo "使用 apt 安装 MySQL..."
    sudo apt update
    sudo apt install -y mysql-server mysql-client

elif [ "$OS" = "centos" ] || [ "$OS" = "rhel" ]; then
    echo "使用 yum 安装 MySQL..."
    sudo yum install -y mysql-server mysql-client
else
    echo -e "${RED}不支持的操作系统: $OS${NC}"
    exit 1
fi

echo -e "${GREEN}MySQL 安装完成${NC}"

# 步骤2: 启动MySQL服务
echo ""
echo "========================================="
echo "步骤 2/7: 启动 MySQL 服务"
echo "========================================="

if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo systemctl start mysql
    sudo systemctl enable mysql
elif [ "$OS" = "centos" ] || [ "$OS" = "rhel" ]; then
    sudo systemctl start mysqld
    sudo systemctl enable mysqld
fi

echo -e "${GREEN}MySQL 服务已启动${NC}"

# 步骤3: 创建目录
echo ""
echo "========================================="
echo "步骤 3/7: 创建数据目录"
echo "========================================="

sudo mkdir -p $BACKUP_DIR
sudo mkdir -p $SCRIPT_DIR
sudo mkdir -p /data/mysql/data

echo -e "${GREEN}目录创建完成${NC}"

# 步骤4: 获取临时root密码（MySQL 8.0）
echo ""
echo "========================================="
echo "步骤 4/7: 配置 MySQL"
echo "========================================="

# 检查是否为Ubuntu系统（MySQL 8.0会生成临时密码）
if [ "$OS" = "ubuntu" ]; then
    TEMP_PASSWORD=$(sudo grep 'temporary password' /var/log/mysql/error.log 2>/dev/null | tail -1 | awk '{print $NF}')

    if [ -n "$TEMP_PASSWORD" ]; then
        echo -e "${YELLOW}检测到临时密码，正在重置root密码...${NC}"
        # 使用临时密码登录并重置
        mysql -u root -p"$TEMP_PASSWORD" --connect-expired-password -e "
            ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';
        " 2>/dev/null || echo -e "${RED}密码重置失败，请手动重置${NC}"
    else
        echo "未检测到临时密码，尝试直接登录..."
    fi
fi

# 步骤5: 创建数据库和用户
echo ""
echo "========================================="
echo "步骤 5/7: 创建数据库和用户"
echo "========================================="

# 检查SQL文件是否存在
if [ ! -f "sql/init-schema.sql" ]; then
    echo -e "${RED}错误: sql/init-schema.sql 文件不存在！${NC}"
    echo "请确保在项目根目录执行此脚本"
    exit 1
fi

echo "请输入MySQL root密码（默认为空，直接回车）:"
read -s MYSQL_ROOT_INPUT

# 导入SQL文件
echo "正在导入数据库结构..."
mysql -u root -p"$MYSQL_ROOT_INPUT" < sql/init-schema.sql
echo -e "${GREEN}数据库结构导入完成${NC}"

echo "正在导入初始化数据..."
mysql -u root -p"$MYSQL_ROOT_INPUT" < sql/init-data.sql
echo -e "${GREEN}初始化数据导入完成${NC}"

# 创建应用用户
echo "创建应用用户..."
mysql -u root -p"$MYSQL_ROOT_INPUT" -e "
    CREATE USER IF NOT EXISTS '$MYSQL_ADMIN_USER'@'%' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ADMIN_PASSWORD';
    GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$MYSQL_ADMIN_USER'@'%';
    FLUSH PRIVILEGES;
"

echo -e "${GREEN}应用用户创建完成${NC}"

# 步骤6: 配置防火墙
echo ""
echo "========================================="
echo "步骤 6/7: 配置防火墙"
echo "========================================="

if command -v ufw >/dev/null 2>&1; then
    echo "检测到 ufw，配置防火墙规则..."
    sudo ufw allow 3306/tcp
    echo -e "${GREEN}防火墙规则已添加${NC}"
elif command -v firewall-cmd >/dev/null 2>&1; then
    echo "检测到 firewalld，配置防火墙规则..."
    sudo firewall-cmd --permanent --add-port=3306/tcp
    sudo firewall-cmd --reload
    echo -e "${GREEN}防火墙规则已添加${NC}"
else
    echo -e "${YELLOW}未检测到防火墙，跳过配置${NC}"
fi

# 步骤7: 验证安装
echo ""
echo "========================================="
echo "步骤 7/7: 验证安装"
echo "========================================="

mysql -u root -p"$MYSQL_ROOT_INPUT" -e "
    USE $DATABASE_NAME;
    SHOW TABLES;
    SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = '$DATABASE_NAME';
"

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}MySQL 部署完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "数据库信息:"
echo "  数据库名: $DATABASE_NAME"
echo "  root密码: $MYSQL_ROOT_PASSWORD (如果未设置则为空)"
echo "  应用用户: $MYSQL_ADMIN_USER"
echo "  应用密码: $MYSQL_ADMIN_PASSWORD"
echo ""
echo "连接命令:"
echo "  mysql -u $MYSQL_ADMIN_USER -p$MYSQL_ADMIN_PASSWORD $DATABASE_NAME"
echo ""
echo "远程连接:"
echo "  mysql -h <服务器IP> -u $MYSQL_ADMIN_USER -p$MYSQL_ADMIN_PASSWORD $DATABASE_NAME"
echo ""
echo "下一步:"
echo "  1. 修改应用配置文件中的数据库连接信息"
echo "  2. 启动应用服务"
echo "  3. 运行数据库备份脚本: $SCRIPT_DIR/mysql_backup.sh"
echo ""
