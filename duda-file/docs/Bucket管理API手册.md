# Bucket 管理 API 手册

## 目录
- [1. 创建 Bucket](#1-创建-bucket)
- [2. 查询 Bucket 列表](#2-查询-bucket-列表)
- [3. 查询 Bucket 详情](#3-查询-bucket-详情)
- [4. 删除 Bucket](#4-删除-bucket)
- [5. 错误码说明](#5-错误码说明)
- [6. 附录](#6-附录)

---

## 1. 创建 Bucket

### 接口信息
- **接口地址**: `/api/bucket/create`
- **请求方式**: `POST`
- **Content-Type**: `application/json`
- **是否需要认证**: 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| bucketName | String | 是 | Bucket名称（全局唯一，3-63字符，只能包含小写字母、数字、短横线） | `"my-bucket-123"` |
| displayName | String | 否 | Bucket显示名称（用户自定义友好名称） | `"我的存储桶"` |
| storageType | String | 否 | 存储类型，默认 `"OSS"` | `"OSS"` |
| region | String | 是 | 区域代码 | `"cn-hangzhou"` |
| storageClass | String | 否 | 存储类型，默认 `"STANDARD"` | `"STANDARD"` |
| dataRedundancyType | String | 否 | 数据冗余类型，默认 `"LRS"` | `"LRS"` |
| aclType | String | 否 | 权限类型，默认 `"PRIVATE"` | `"PRIVATE"` |
| versioningEnabled | Boolean | 否 | 是否开启版本控制，默认 `false` | `false` |
| userId | Long | 是 | 用户ID | `4943568288616448` |
| userType | String | 否 | 用户类型，默认 `"PERSONAL"` | `"PERSONAL"` |
| maxFileSize | Long | 否 | 最大文件大小（字节），默认 `10737418240`（10GB） | `10737418240` |
| maxFileCount | Integer | 否 | 最大文件数量，默认 `100000` | `100000` |
| category | String | 否 | Bucket类别 | `"documents"` |
| description | String | 否 | Bucket描述 | `"用于存储文档"` |
| tags | Object | 否 | Bucket标签（JSON对象） | `{"env": "dev"}` |
| keyName | String | 是 | API密钥名称（用于选择哪个密钥创建Bucket） | `"duda-file-sts-role"` |

### 枚举值说明

#### storageClass（存储类型）
| 值 | 说明 | 适用场景 |
|----|------|----------|
| `STANDARD` | 标准存储 | 频繁访问的数据 |
| `IA` | 低频访问存储 | 不常访问但需要快速获取的数据 |
| `ARCHIVE` | 归档存储 | 长期归档、很少访问的数据 |
| `COLD_ARCHIVE` | 冷归档存储 | 超长期归档的数据 |

#### dataRedundancyType（数据冗余类型）
| 值 | 说明 |
|----|------|
| `LRS` | 本地冗余存储 |
| `ZRS` | 同城冗余存储 |

#### aclType（权限类型）
| 值 | 说明 |
|----|------|
| `PRIVATE` | 私有读写（仅Bucket拥有者可访问） |
| `PUBLIC_READ` | 公共读（ anyone 可读，仅拥有者可写） |
| `PUBLIC_READ_WRITE` | 公共读写（ anyone 可读写，慎用） |

#### region（区域代码）
| 值 | 说明 |
|----|------|
| `cn-hangzhou` | 华东1（杭州） |
| `cn-beijing` | 华北2（北京） |
| `cn-shenzhen` | 华南1（深圳） |
| `cn-shanghai` | 华东2（上海） |
| `cn-qingdao` | 华北1（青岛） |

### 请求示例

#### 示例1：基础创建（最简化）
```json
POST /api/bucket/create
Content-Type: application/json

{
  "bucketName": "my-test-bucket",
  "region": "cn-hangzhou",
  "userId": 4943568288616448,
  "keyName": "duda-file-sts-role"
}
```

#### 示例2：标准创建（推荐）
```json
POST /api/bucket/create
Content-Type: application/json

{
  "bucketName": "my-documents-bucket",
  "displayName": "我的文档存储",
  "storageType": "OSS",
  "region": "cn-hangzhou",
  "storageClass": "STANDARD",
  "aclType": "PRIVATE",
  "versioningEnabled": false,
  "userId": 4943568288616448,
  "userType": "PERSONAL",
  "keyName": "duda-file-sts-role"
}
```

#### 示例3：完整创建（所有参数）
```json
POST /api/bucket/create
Content-Type: application/json

{
  "bucketName": "my-complete-bucket-20250318",
  "displayName": "完整配置的存储桶",
  "storageType": "OSS",
  "region": "cn-hangzhou",
  "storageClass": "STANDARD",
  "dataRedundancyType": "LRS",
  "aclType": "PRIVATE",
  "versioningEnabled": false,
  "userId": 4943568288616448,
  "userType": "PERSONAL",
  "maxFileSize": 10737418240,
  "maxFileCount": 100000,
  "category": "documents",
  "description": "这是一个用于存储文档的Bucket",
  "tags": {
    "environment": "production",
    "project": "duda-file",
    "team": "backend"
  },
  "keyName": "duda-file-sts-role"
}
```

### 响应结果

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bucketName": "my-test-bucket",
    "displayName": "我的文档存储",
    "storageType": null,
    "region": "cn-hangzhou",
    "regionName": null,
    "creationTime": "2026-03-18T12:46:18.770+00:00",
    "storageClass": "STANDARD",
    "dataRedundancyType": null,
    "acl": "PRIVATE",
    "extranetEndpoint": null,
    "intranetEndpoint": null,
    "fileCount": null,
    "storageSize": null,
    "storageQuota": null,
    "tags": null,
    "status": null,
    "versioningEnabled": null,
    "userId": null,
    "extra": null
  },
  "timestamp": 1773837978847,
  "success": true
}
```

#### 失败响应
```json
{
  "timestamp": "2026-03-18T12:43:02.747+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/bucket/create"
}
```

---

## 2. 查询 Bucket 列表

### 接口信息
- **接口地址**: `/api/bucket/list`
- **请求方式**: `GET`
- **是否需要认证**: 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| userId | Long | 是 | 用户ID | `4943568288616448` |
| keyName | String | 是 | API密钥名称 | `"duda-file-sts-role"` |

### 请求示例

```bash
GET /api/bucket/list?userId=4943568288616448&keyName=duda-file-sts-role
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "bucketName": "my-documents-bucket",
      "displayName": "我的文档存储",
      "storageType": null,
      "region": "cn-hangzhou",
      "regionName": null,
      "creationTime": null,
      "storageClass": "STANDARD",
      "dataRedundancyType": null,
      "acl": "PRIVATE",
      "extranetEndpoint": null,
      "intranetEndpoint": null,
      "fileCount": null,
      "storageSize": null,
      "storageQuota": null,
      "tags": null,
      "status": null,
      "versioningEnabled": null,
      "userId": null,
      "extra": null
    },
    {
      "bucketName": "my-images-bucket",
      "displayName": "图片存储",
      "storageType": null,
      "region": "cn-beijing",
      "regionName": null,
      "creationTime": null,
      "storageClass": "IA",
      "dataRedundancyType": null,
      "acl": "PUBLIC_READ",
      "extranetEndpoint": null,
      "intranetEndpoint": null,
      "fileCount": null,
      "storageSize": null,
      "storageQuota": null,
      "tags": null,
      "status": null,
      "versioningEnabled": null,
      "userId": null,
      "extra": null
    }
  ],
  "timestamp": 1773837980591,
  "success": true
}
```

---

## 3. 查询 Bucket 详情

### 接口信息
- **接口地址**: `/api/bucket/info`
- **请求方式**: `GET`
- **是否需要认证**: 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| bucketName | String | 是 | Bucket名称 | `"my-bucket"` |

### 请求示例

```bash
GET /api/bucket/info?bucketName=my-bucket
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bucketName": "my-bucket",
    "displayName": "我的存储桶",
    "storageType": "OSS",
    "region": "cn-hangzhou",
    "creationTime": "2026-03-18T12:00:00.000+00:00",
    "storageClass": "STANDARD",
    "acl": "PRIVATE",
    "fileCount": 100,
    "storageSize": 1073741824,
    "versioningEnabled": false
  },
  "timestamp": 1773837980591,
  "success": true
}
```

---

## 4. 删除 Bucket

### 接口信息
- **接口地址**: `/api/bucket/delete`
- **请求方式**: `DELETE`
- **是否需要认证**: 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| bucketName | String | 是 | Bucket名称 | `"my-bucket"` |
| userId | Long | 是 | 用户ID | `4943568288616448` |

### 请求示例

```bash
DELETE /api/bucket/delete?bucketName=my-bucket&userId=4943568288616448
```

### 响应结果

```json
{
  "code": 200,
  "message": "Bucket删除成功",
  "data": null,
  "timestamp": 1773837980591,
  "success": true
}
```

---

## 5. 错误码说明

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| `BUCKET_NAME_INVALID` | Bucket名称不符合规则 | 使用3-63字符的小写字母、数字、短横线 |
| `BUCKET_ALREADY_EXISTS` | Bucket已存在 | 使用其他Bucket名称 |
| `BUCKET_NOT_FOUND` | Bucket不存在 | 检查Bucket名称是否正确 |
| `USER_ID_REQUIRED` | 用户ID不能为空 | 提供有效的userId |
| `KEY_NAME_REQUIRED` | API密钥名称不能为空 | 提供有效的keyName |
| `API_KEY_NOT_FOUND` | API密钥不存在 | 检查keyName是否正确 |
| `STORAGE_CLASS_INVALID` | 存储类型无效 | 使用有效的存储类型值 |
| `REGION_INVALID` | 区域无效 | 使用有效的区域代码 |
| `ACL_TYPE_INVALID` | 权限类型无效 | 使用有效的权限类型值 |

### 错误响应示例

```json
{
  "code": 400,
  "message": "BUCKET_NAME_INVALID: Bucket名称只能包含小写字母、数字、短横线",
  "data": null,
  "timestamp": 1773837980591,
  "success": false
}
```

---

## 6. 附录

### 6.1 Bucket 命名规则

Bucket名称必须符合以下规则：
1. **长度**: 3-63个字符
2. **字符集**: 只能包含小写字母、数字、短横线（-）
3. **开头结尾**: 必须以字母或数字开头和结尾
4. **唯一性**: 全局唯一，不能与已有Bucket重复

#### 有效名称示例
- ✅ `my-bucket`
- ✅ `bucket-123`
- ✅ `testbucket`
- ✅ `my-bucket-20250318`

#### 无效名称示例
- ❌ `MyBucket` (包含大写字母)
- ❌ `my_bucket` (包含下划线)
- ❌ `-mybucket` (以短横线开头)
- ❌ `mybucket-` (以短横线结尾)
- ❌ `my` (长度小于3)

### 6.2 最佳实践

#### 选择合适的存储类型
- **STANDARD（标准存储）**: 适用于频繁访问的数据，如图片、视频、文档
- **IA（低频访问）**: 适用于不常访问但需要快速获取的数据，如归档日志、备份文件
- **ARCHIVE（归档）**: 适用于长期保存、很少访问的数据，如合规性归档
- **COLD_ARCHIVE（冷归档）**: 适用于超长期归档，如历史数据归档

#### 选择合适的权限类型
- **PRIVATE**: 默认选项，最安全，仅Bucket拥有者可访问
- **PUBLIC_READ**: 适用于静态网站、公开资源
- **PUBLIC_READ_WRITE**: 慎用，仅适用于特定场景（如公共上传目录）

#### 选择合适的区域
- 选择离用户最近的区域，降低访问延迟
- 考虑数据合规要求（如数据必须存储在特定区域）

### 6.3 前端集成示例

#### JavaScript/Fetch
```javascript
// 创建 Bucket
async function createBucket() {
  const response = await fetch('http://localhost:8085/api/bucket/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      bucketName: 'my-bucket-' + Date.now(),
      displayName: '我的存储桶',
      region: 'cn-hangzhou',
      storageClass: 'STANDARD',
      aclType: 'PRIVATE',
      userId: 4943568288616448,
      keyName: 'duda-file-sts-role'
    })
  });

  const result = await response.json();
  if (result.success) {
    console.log('Bucket创建成功:', result.data);
  } else {
    console.error('创建失败:', result.message);
  }
}

// 查询 Bucket 列表
async function listBuckets() {
  const userId = 4943568288616448;
  const keyName = 'duda-file-sts-role';
  const response = await fetch(
    `http://localhost:8085/api/bucket/list?userId=${userId}&keyName=${keyName}`
  );

  const result = await response.json();
  if (result.success) {
    console.log('Bucket列表:', result.data);
  }
}
```

#### Axios
```javascript
import axios from 'axios';

// 创建 Bucket
async function createBucket() {
  try {
    const response = await axios.post('http://localhost:8085/api/bucket/create', {
      bucketName: 'my-bucket-' + Date.now(),
      displayName: '我的存储桶',
      region: 'cn-hangzhou',
      storageClass: 'STANDARD',
      aclType: 'PRIVATE',
      userId: 4943568288616448,
      keyName: 'duda-file-sts-role'
    });

    console.log('Bucket创建成功:', response.data);
  } catch (error) {
    console.error('创建失败:', error.response.data);
  }
}

// 查询 Bucket 列表
async function listBuckets() {
  try {
    const response = await axios.get('http://localhost:8085/api/bucket/list', {
      params: {
        userId: 4943568288616448,
        keyName: 'duda-file-sts-role'
      }
    });

    console.log('Bucket列表:', response.data.data);
  } catch (error) {
    console.error('查询失败:', error.response.data);
  }
}
```

### 6.4 Postman 集合

可以导入以下 Postman 集合进行快速测试：

```json
{
  "info": {
    "name": "Bucket管理API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "创建Bucket",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:8085/api/bucket/create",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8085",
          "path": ["api", "bucket", "create"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\n  \"bucketName\": \"my-bucket-{{$timestamp}}\",\n  \"displayName\": \"我的存储桶\",\n  \"region\": \"cn-hangzhou\",\n  \"storageClass\": \"STANDARD\",\n  \"aclType\": \"PRIVATE\",\n  \"userId\": 4943568288616448,\n  \"keyName\": \"duda-file-sts-role\"\n}"
        }
      }
    },
    {
      "name": "查询Bucket列表",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8085/api/bucket/list?userId=4943568288616448&keyName=duda-file-sts-role",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8085",
          "path": ["api", "bucket", "list"],
          "query": [
            {
              "key": "userId",
              "value": "4943568288616448"
            },
            {
              "key": "keyName",
              "value": "duda-file-sts-role"
            }
          ]
        }
      }
    }
  ]
}
```

---

## 联系方式

如有问题，请联系：
- 项目地址: `/Volumes/DudaDate/DudaNexus/duda-file`
- 文档位置: `/docs/Bucket管理API手册.md`
- 更新时间: 2026-03-18

---

**注意**:
1. 所有接口都需要认证，请在请求头中携带认证信息
2. 测试环境地址: `http://localhost:8085`
3. 生产环境地址根据实际部署情况调整
4. 建议在生产环境使用 HTTPS 协议
