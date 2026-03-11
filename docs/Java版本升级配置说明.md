# Java 版本升级配置说明

## 📊 升级概览

**升级时间**: 2026-03-10
**升级内容**: 系统全局 Java 版本从 Java 8 升级到 Java 17
**升级方式**: 使用 jenv 管理多个 Java 版本

---

## ✅ 已完成的配置

### 1. 多 Java 版本管理（使用 jenv）

系统中已安装以下 Java 版本：
- ✅ Java 8 (Zulu 1.8.0_472) - 用于老项目
- ✅ Java 11 (Eclipse Adoptium 11.0.29) - 中间版本
- ✅ Java 17 (Oracle 17.0.16) - **全局默认版本**
- ✅ Java 21 (Oracle 21.0.7) - 最新 LTS 版本

### 2. 全局配置

```bash
# 全局默认 Java 版本
jenv global 17

# 验证
java -version
# 输出: java version "17.0.16" 2025-07-15 LTS

mvn -version
# 输出: Java version: 17.0.16
```

### 3. 项目配置

#### DudaNexus 项目
```bash
# 项目级别的 Java 版本
cd /Volumes/DudaDate/DudaNexus
jenv local 17

# IDEA 配置
# .idea/misc.xml: languageLevel="JDK_17", project-jdk-name="17"
```

#### qiyu-live-app 项目
```bash
# 项目级别的 Java 版本
cd /Volumes/DudaDate/Live_app/Backend/qiyu-live-app
jenv local 17
```

### 4. Shell 配置（~/.zshrc）

```bash
# 使用 jenv 管理 JAVA_HOME（自动切换 Java 版本）
export PATH="$HOME/.jenv/bin:$PATH"
eval "$(jenv init -)"
jenv enable-plugin export
```

---

## 🔧 如何使用

### 新项目（使用 Java 17/21）

**在 IntelliJ IDEA 中：**
1. 打开项目
2. `File → Project Structure → Project`
3. SDK: 选择 `17` 或 `21`
4. Language level: 选择对应的 SDK 版本

**在命令行中：**
```bash
# 进入项目目录
cd /path/to/new-project

# 设置项目 Java 版本
jenv local 17  # 或 21

# 验证
java -version
mvn -version
```

### 老项目（继续使用 Java 8）

**方式 1: 在 IntelliJ IDEA 中配置（推荐）**

1. 打开老项目
2. `File → Project Structure → Project`
3. SDK: 点击 `Add SDK → JDK`
4. 选择 `/Library/Java/JavaVirtualMachines/zulu-8.jdk`
5. SDK: 选择 `1.8`
6. Language level: 选择 `8 - Lambdas, type annotations etc.`

**方式 2: 使用 jenv 配置项目级别版本**

```bash
# 进入老项目目录
cd /path/to/old-project

# 设置项目使用 Java 8
jenv local 1.8

# 验证
java -version
# 输出: openjdk version "1.8.0_472"
```

**方式 3: 临时切换到 Java 8**

```bash
# 临时切换当前 shell 到 Java 8
jenv shell 1.8

# 验证
java -version

# 退出后自动恢复到全局版本（Java 17）
exit
```

---

## 📝 版本切换命令速查

```bash
# 查看所有可用版本
jenv versions

# 设置全局默认版本
jenv global 17    # 或 1.8, 11, 21

# 设置当前目录（项目）版本
jenv local 17     # 在项目目录下执行

# 临时切换当前 shell 版本
jenv shell 1.8

# 查看当前版本
jenv version

# 查看全局版本
jenv global

# 查看项目版本
jenv local
```

---

## 🎯 常见场景

### 场景 1: 开发新项目（DudaNexus）

```bash
# 打开项目
cd /Volumes/DudaDate/DudaNexus

# 验证 Java 版本（应该是 17）
java -version

# 编译项目
mvn clean install

# 或在 IDEA 中直接运行
# IDEA 会自动使用项目配置的 JDK 17
```

### 场景 2: 维护老项目（Java 8）

```bash
# 进入老项目目录
cd /path/to/old-project

# 设置项目使用 Java 8
jenv local 1.8

# 验证
java -version
# 输出: openjdk version "1.8.0_472"

# 编译项目
mvn clean install

# 或在 IDEA 中配置项目 SDK 为 1.8
```

### 场景 3: 同时开发新旧项目

```bash
# 终端 1: 新项目
cd /Volumes/DudaDate/DudaNexus
jenv local 17
java -version  # Java 17

# 终端 2: 老项目
cd /path/to/old-project
jenv local 1.8
java -version  # Java 8

# IDEA 中可以为每个项目单独配置 SDK，互不影响
```

---

## ⚠️ 注意事项

### 1. 首次使用需要重启终端

升级后，**需要重启终端**才能生效：
```bash
# 关闭当前终端，重新打开
# 或者执行
source ~/.zshrc
```

### 2. IntelliJ IDEA 配置

**重要**: IDEA 不会自动使用 jenv 的配置，需要在 IDEA 中手动设置：

**新项目（DudaNexus）：**
- `File → Project Structure → Project → SDK: 17`
- `File → Project Structure → Project → Language level: 17`

**老项目（Java 8）：**
- `File → Project Structure → Project → SDK: 1.8`
- `File → Project Structure → Project → Language level: 8`

### 3. Maven 配置

Maven 会自动使用 `$JAVA_HOME` 环境变量配置的版本。由于我们配置了 jenv 的 export 插件，Maven 会自动使用当前项目设置的 Java 版本。

**验证：**
```bash
cd /Volumes/DudaDate/DudaNexus
mvn -version
# 应该显示: Java version: 17.0.16

cd /path/to/old-project
jenv local 1.8
mvn -version
# 应该显示: Java version: 1.8.0_472
```

### 4. 老项目不受影响

**老项目可以继续使用 Java 8，原因：**
1. ✅ jenv 支持项目级别的版本配置
2. ✅ IntelliJ IDEA 支持每个项目单独配置 SDK
3. ✅ 系统 Java 8 仍然保留，可以随时切换
4. ✅ Maven 会根据项目配置使用对应的 Java 版本

---

## 🔍 故障排查

### 问题 1: `java -version` 显示旧版本

**解决方法：**
```bash
# 重新加载 shell 配置
source ~/.zshrc

# 或者重启终端
```

### 问题 2: Maven 使用的 Java 版本不正确

**解决方法：**
```bash
# 确保 JAVA_HOME 正确设置
echo $JAVA_HOME

# 应该显示: $HOME/.jenv/versions/17 (或其他版本)

# 如果不对，手动设置
export JAVA_HOME="$HOME/.jenv/versions/17"
```

### 问题 3: IDEA 中无法识别 JDK

**解决方法：**
1. `File → Project Structure → SDKs`
2. 点击 `+` → `Add JDK`
3. 选择对应的 JDK 安装路径：
   - Java 8: `/Library/Java/JavaVirtualMachines/zulu-8.jdk`
   - Java 17: `/Library/Java/JavaVirtualMachines/jdk-17.jdk`
   - Java 21: `/Library/Java/JavaVirtualMachines/jdk-21.jdk`

### 问题 4: 老项目编译失败

**可能原因：**
- 项目配置的 SDK 不是 Java 8

**解决方法：**
```bash
# 检查项目 Java 版本
cd /path/to/old-project
jenv local  # 应该显示 1.8

# 如果不是，设置
jenv local 1.8

# 或在 IDEA 中检查项目 SDK 配置
```

---

## 📚 参考文档

- [jenv 官方文档](https://github.com/jenv/jenv)
- [Oracle JDK 17 发行说明](https://www.oracle.com/java/technologies/javase/17-relnotes.html)
- [Spring Boot 3.x JDK 要求](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/getting-started.html#getting-started.system-requirements)

---

## 🎉 总结

### 升级后的优势

1. ✅ **可以同时管理多个 Java 版本**
2. ✅ **新项目使用 Java 17，性能更好**
3. ✅ **老项目继续使用 Java 8，不受影响**
4. ✅ **项目级别自动切换，无需手动配置**
5. ✅ **符合 Spring Boot 3.x 的要求**

### 兼容性保证

- ✅ 老项目在 IDEA 中配置 SDK 为 1.8 即可
- ✅ 老项目目录下执行 `jenv local 1.8` 即可
- ✅ 多个项目可以同时使用不同 Java 版本
- ✅ 无需卸载 Java 8

### 下一步

1. ✅ 重启终端，让配置生效
2. ✅ 在 IDEA 中重新打开 DudaNexus 项目
3. ✅ 验证项目能正常编译运行
4. ✅ 老项目需要时配置 Java 8 即可

---

**配置完成时间**: 2026-03-10
**配置人员**: Claude (AI Assistant)
**状态**: ✅ 升级成功，老项目不受影响
