# Spring Cloud 微服务开发规范手册

## 1. 代码编写规范

### 1.1 代码风格
遵循《阿里巴巴Java开发手册》，并执行以下规范：

#### 1.1.1 命名规范
```java
// 类名：大驼峰
public class UserService { }

// 方法名：小驼峰
public void getUserById() { }

// 常量：全大写下划线分隔
public static final String MAX_SIZE = "100";

// 变量：小驼峰
private String userName;

// 包名：全小写
package com.duda.user.service;
```

#### 1.1.2 注释规范
```java
/**
 * 用户服务接口
 *
 * @author DudaNexus
 * @since 2024-03-07
 */
public interface UserService {

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     * @throws UserNotFoundException 用户不存在异常
     */
    User getUserById(Long userId) throws UserNotFoundException;
}
```

### 1.2 代码结构
```java
// Controller层 - 接口入口
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
}

// Service层 - 业务逻辑
public interface UserService {
    UserVO getUserById(Long userId);
}

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return UserConvert.toVO(user);
    }
}

// Mapper层 - 数据访问
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper获得基础CRUD方法
}
```

---

## 2. Spring Boot配置规范

### 2.1 配置文件组织
```yaml
# application.yml - 主配置
spring:
  application:
    name: duda-user-service
  profiles:
    active: ${ENV:dev}

# application-dev.yml - 开发环境
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dn_user
    username: root
    password: 123456

# application-prod.yml - 生产环境
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 2.2 配置属性管理
```java
// 自定义配置类
@ConfigurationProperties(prefix = "duda.user")
@Data
@Component
public class UserProperties {
    /**
     * 默认头像
     */
    private String defaultAvatar = "https://cdn.duda.com/default-avatar.png";

    /**
     * 用户名最大长度
     */
    private Integer maxUsernameLength = 20;

    /**
     * 密码最小长度
     */
    private Integer minPasswordLength = 6;
}
```

### 2.3 多环境配置
```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}
        group: DUDA_GROUP
        file-extension: yml
        shared-configs:
          - dataId: duda-common.yml
            group: DUDA_GROUP
            refresh: true
```

---

## 3. 服务注册与发现

### 3.1 Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}
        group: DUDA_GROUP
        register-enabled: true
        heart-beat-interval: 5000
        heart-beat-timeout: 15000
        ip-delete-timeout: 30000
```

### 3.2 服务调用
```java
// OpenFeign声明式调用
@FeignClient(
    name = "duda-user-service",
    path = "/api/v1/user",
    fallbackFactory = UserServiceFallbackFactory.class
)
public interface UserServiceClient {

    @GetMapping("/{id}")
    Result<UserVO> getUser(@PathVariable("id") Long id);

    @PostMapping
    Result<Long> createUser(@RequestBody CreateUserCommand cmd);
}

// 降级处理
@Component
@Slf4j
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("用户服务调用失败", cause);
        return new UserServiceClient() {
            @Override
            public Result<UserVO> getUser(Long id) {
                return Result.error("用户服务暂时不可用");
            }

            @Override
            public Result<Long> createUser(CreateUserCommand cmd) {
                return Result.error("创建用户失败");
            }
        };
    }
}
```

---

## 4. API网关配置

### 4.1 Gateway路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 用户服务路由
        - id: duda-user-service
          uri: lb://duda-user-service
          predicates:
            - Path=/api/v1/user/**
          filters:
            - StripPrefix=2
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@ipKeyResolver}"

        # 认证服务路由
        - id: duda-auth-service
          uri: lb://duda-auth-service
          predicates:
            - Path=/api/v1/auth/**

        # IM服务路由（WebSocket）
        - id: duda-im-service
          uri: lb:ws://duda-im-service
          predicates:
            - Path=/ws/im/**

      # 全局配置
      default-filters:
        - name: Retry
        - name: RequestRateLimiter
        - name: CircuitBreaker
```

### 4.2 网关过滤器
```java
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 白名单路径
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 验证token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isEmpty(token)) {
            return unauthorized(exchange);
        }

        // 解析token
        Claims claims = JwtUtils.parseToken(token);
        if (claims == null) {
            return unauthorized(exchange);
        }

        // 设置用户信息到请求头
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", claims.get("userId", String.class))
                .header("X-User-Name", claims.get("userName", String.class))
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
```

---

## 5. 熔断降级

### 5.1 Sentinel配置
```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: ${SENTINEL_ADDR:localhost:8080}
        port: 8719
      datasource:
        flow:
          nacos:
            server-addr: ${NACOS_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-flow-rules
            rule-type: flow
        degrade:
          nacos:
            server-addr: ${NACOS_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-degrade-rules
            rule-type: degrade
```

### 5.2 资源定义
```java
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/{id}")
    @SentinelResource(
        value = "getUser",
        blockHandler = "handleBlock",
        fallback = "handleFallback"
    )
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    // 限流处理
    public Result<UserVO> handleBlock(Long id, BlockException ex) {
        log.warn("获取用户被限流: userId={}", id);
        return Result.error(429, "请求过于频繁");
    }

    // 降级处理
    public Result<UserVO> handleFallback(Long id, Throwable ex) {
        log.error("获取用户失败: userId={}", id, ex);
        return Result.error("服务暂时不可用，请稍后再试");
    }
}
```

---

## 6. 分布式事务

### 6.1 Seata配置
```yaml
seata:
  enabled: true
  application-id: duda-user-service
  tx-service-group: duda-tx-group
  registry:
    type: nacos
    nacos:
      server-addr: ${NACOS_ADDR:localhost:8848}
      namespace: ${NACOS_NAMESPACE:}
      group: SEATA_GROUP
  config:
    type: nacos
    nacos:
      server-addr: ${NACOS_ADDR:localhost:8848}
      namespace: ${NACOS_NAMESPACE:}
      group: SEATA_GROUP
```

### 6.2 分布式事务使用
```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private PaymentClient paymentClient;

    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    @Override
    public Long createOrder(CreateOrderCommand cmd) {
        // 1. 扣减库存
        productClient.deductStock(cmd.getProductId(), cmd.getCount());

        // 2. 创建订单
        Order order = buildOrder(cmd);
        orderMapper.insert(order);

        // 3. 创建支付
        paymentClient.createPayment(order.getId(), order.getAmount());

        return order.getId();
    }
}
```

---

## 7. 消息队列

### 7.1 RocketMQ配置
```yaml
rocketmq:
  name-server: ${ROCKETMQ_ADDR:localhost:9876}
  producer:
    group: duda-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

### 7.2 消息生产者
```java
@Component
@Slf4j
public class OrderMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送订单创建消息
     */
    public void sendOrderCreateMessage(Order order) {
        OrderCreateMessage message = OrderCreateMessage.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getAmount())
                .build();

        rocketMQTemplate.asyncSend(
                DNOrderConstant.ORDER_CREATE_TOPIC,
                message,
                new DefaultSendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("订单创建消息发送成功: orderId={}", order.getId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("订单创建消息发送失败: orderId={}", order.getId(), e);
                    }
                }
        );
    }
}
```

### 7.3 消息消费者
```java
@Component
@RocketMQMessageListener(
    consumerGroup = "duda-order-consumer-group",
    topic = DNOrderConstant.ORDER_CREATE_TOPIC
)
@Slf4j
public class OrderCreateConsumer implements RocketMQListener<OrderCreateMessage> {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onMessage(OrderCreateMessage message) {
        try {
            log.info("收到订单创建消息: {}", message);

            // 发送通知
            notificationService.sendOrderNotification(message);

        } catch (Exception e) {
            log.error("处理订单创建消息失败: {}", message, e);
            throw new RuntimeException("处理失败，触发重试");
        }
    }
}
```

---

## 8. 缓存使用规范

### 8.1 Redis配置
```yaml
spring:
  redis:
    host: ${REDIS_ADDR:localhost}
    port: 6379
    password: ${REDIS_PASSWORD:}
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### 8.2 缓存注解使用
```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户信息（缓存）
     */
    @Cacheable(
        value = "user",
        key = "'user:info:' + #userId",
        unless = "#result == null"
    )
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户信息（删除缓存）
     */
    @CacheEvict(
        value = "user",
        key = "'user:info:' + #user.id"
    )
    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    /**
     * 创建用户（不支持缓存）
     */
    @CachePut(
        value = "user",
        key = "'user:info:' + #result.id"
    )
    @Override
    public User createUser(User user) {
        userMapper.insert(user);
        return user;
    }
}
```

### 8.3 缓存工具类
```java
@Component
@Slf4j
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 分布式锁
     */
    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 释放锁
     */
    public Boolean unlock(String key, String value) {
        // 使用Lua脚本保证原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result == 1L;
    }
}
```

---

## 9. 数据库操作规范

### 9.1 MyBatis-Plus配置
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/**/*Mapper.xml
```

### 9.2 实体类定义
```java
@Data
@TableName("dn_user")
public class User {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 昵称
     */
    @TableField("user_nick")
    private String userNick;

    /**
     * 邮箱
     */
    @TableField("user_email")
    private String userEmail;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
```

### 9.3 分页查询
```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询用户
     */
    @Override
    public PageResult<UserVO> pageUsers(UserQuery query) {
        Page<User> page = new Page<>(query.getPageNo(), query.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(query.getUserName()),
                        User::getUserName, query.getUserName())
                .eq(query.getIsActive() != null,
                        User::getIsActive, query.getIsActive())
                .orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);

        return PageResult.of(result, UserConvert::toVO);
    }
}
```

---

## 10. 异常处理规范

### 10.1 统一异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
```

### 10.2 自定义异常
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(500, message);
    }
}

// 使用示例
if (user == null) {
    throw new BusinessException(10001, "用户不存在");
}
```

---

## 11. 日志规范

### 11.1 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>
    <property name="LOG_PATH" value="/data/logs/${APP_NAME}"/>

    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 11.2 日志使用
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    public User getUserById(Long userId) {
        log.info("查询用户: userId={}", userId);

        User user = userMapper.selectById(userId);

        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new UserNotFoundException();
        }

        log.info("查询用户成功: userId={}, userName={}", user.getId(), user.getUserName());
        return user;
    }
}
```

---

## 12. 参数校验

### 12.1 校验注解
```java
@Data
public class CreateUserCommand {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度3-20个字符")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度6-20个字符")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

### 12.2 Controller校验
```java
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody CreateUserCommand cmd) {
        return Result.success(userService.createUser(cmd));
    }
}
```

---

## 13. 接口文档

### 13.1 Swagger配置
```java
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("都达云台API文档")
                        .version("1.0.0")
                        .description("都达云台微服务API文档"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

### 13.2 接口注解
```java
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    public Result<UserVO> getUser(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Long> createUser(@RequestBody CreateUserCommand cmd) {
        // ...
    }
}
```

---

## 14. 性能优化

### 14.1 异步调用
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {

    @Async("taskExecutor")
    public void sendNotification(Long userId, String content) {
        // 异步发送通知
    }
}
```

### 14.2 批量操作
```java
@Service
public class UserServiceImpl implements UserService {

    /**
     * 批量插入用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertUsers(List<User> users) {
        // 分批插入，每批500条
        int batchSize = 500;
        for (int i = 0; i < users.size(); i += batchSize) {
            int end = Math.min(i + batchSize, users.size());
            List<User> batch = users.subList(i, end);
            userService.saveBatch(batch);
        }
    }
}
```

---

## 15. 测试规范

### 15.1 单元测试
```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testGetUserById() {
        User user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getUserName());
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUserName("test");
        user.setPassword("123456");

        Long userId = userService.createUser(user);
        assertNotNull(userId);
    }
}
```

### 15.2 接口测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userName").value("admin"));
    }
}
```

---

## 16. 部署配置

### 16.1 Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="DudaNexus"

WORKDIR /app

ADD target/duda-user-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "app.jar"]
```

### 16.2 docker-compose.yml
```yaml
version: '3.8'

services:
  duda-user-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_ADDR=nacos:8848
      - REDIS_ADDR=redis:6379
      - DB_URL=jdbc:mysql://mysql:3306/dn_user
    depends_on:
      - nacos
      - redis
      - mysql
```

---

## 17. 最佳实践总结

1. **遵循DDD设计**：按领域划分服务，保持服务独立性
2. **使用领域事件**：通过消息队列解耦服务
3. **幂等性设计**：所有写操作必须保证幂等
4. **优雅降级**：服务异常时要有降级方案
5. **监控告警**：关键指标必须监控和告警
6. **文档先行**：先设计API，再编写实现
7. **测试覆盖**：单元测试覆盖率>60%
8. **代码审查**：所有代码必须经过Code Review
9. **持续重构**：定期清理技术债务
10. **安全第一**：永远不要信任用户输入
