# Java 版本切换速查卡

## 🎯 快速切换命令

```bash
# 查看所有版本
jenv versions

# 设置全局默认版本
jenv global 17

# 设置项目版本（在项目目录下执行）
jenv local 1.8

# 临时切换当前 shell
jenv shell 21

# 查看当前版本
jenv version
```

## 📁 项目配置

### DudaNexus 项目
```bash
cd /Volumes/DudaDate/DudaNexus
jenv local 17  # Java 17
java -version
```

### 老项目（继续使用 Java 8）
```bash
cd /path/to/old-project
jenv local 1.8  # Java 8
java -version
```

## 🔧 IntelliJ IDEA 配置

### 新项目（Java 17）
```
File → Project Structure → Project
  SDK: 17
  Language level: 17
```

### 老项目（Java 8）
```
File → Project Structure → Project
  SDK: 1.8
  Language level: 8
```

## 📍 JDK 安装路径

```bash
Java 8:   /Library/Java/JavaVirtualMachines/zulu-8.jdk
Java 11:  /Library/Java/JavaVirtualMachines/temurin-11.jdk
Java 17:  /Library/Java/JavaVirtualMachines/jdk-17.jdk
Java 21:  /Library/Java/JavaVirtualMachines/jdk-21.jdk
```

## ⚡ 常见问题

**Q: java -version 还是旧版本？**
```bash
source ~/.zshrc  # 重新加载配置
```

**Q: Maven 使用的 Java 不对？**
```bash
echo $JAVA_HOME  # 检查环境变量
export JAVA_HOME="$HOME/.jenv/versions/17"
```

**Q: IDEA 找不到 JDK？**
```
File → Project Structure → SDKs → + → Add JDK
选择对应的安装路径
```

## ✅ 验证命令

```bash
# 验证 Java 版本
java -version

# 验证 Maven 版本
mvn -version | grep "Java version"

# 验证项目配置
cat .java-version  # jenv 创建的文件
```

---

**快速参考 | 打印贴在显示器旁边** 📌
