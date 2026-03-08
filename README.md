# WMS 仓储管理系统

## 项目简介

这是一个前后端分离的仓储管理系统（WMS）示例项目，覆盖了用户、管理员、仓库、分类、货品、菜单权限以及出入库记录等核心业务。

项目当前已经完成一轮现代化升级：

- 前端从 `Vue 2 + Element UI + Vue CLI/Webpack` 升级到 `Vue 3 + Element Plus + Vite`
- 前端核心页面已迁移到 `Composition API`
- 后端从 `Spring Boot 2.x` 升级到 `Spring Boot 3.x`
- Java 运行环境升级到 `Java 17`
- MyBatis-Plus 适配到 Spring Boot 3 版本体系

适合用于：

- Java / Vue 全栈练手项目
- 仓储管理类课程设计 / 毕业设计
- 作为管理后台模板进行二次开发
- 作为 Vue 2 / Spring Boot 2 老项目升级参考

---

## 功能概览

### 业务模块

- 用户登录
- 账号注册
- 基于角色的菜单加载
- 用户管理
- 管理员管理
- 仓库管理
- 分类管理
- 货品管理
- 出入库记录管理
- 个人中心

### 业务特性

- 支持分页查询
- 支持条件筛选
- 支持动态菜单渲染
- 支持入库、出库联动库存数量变更
- 统一返回结构，便于前端统一处理
- 全局跨域配置已开启，便于前后端本地分离联调

---

## 技术栈

### 前端

- Vue 3
- Vue Router 4
- Vuex 4
- Element Plus
- Axios
- Vite

### 后端

- Spring Boot 3.2.x
- Java 17
- MyBatis-Plus 3.5.x
- MySQL 8
- Maven 3.6+
- Lombok

---

## 升级亮点

| 维度 | 升级前 | 升级后 |
| --- | --- | --- |
| 前端框架 | Vue 2 | Vue 3 |
| 页面组织方式 | Options API 为主 | Composition API 为主 |
| UI 组件库 | Element UI | Element Plus |
| 构建工具 | Vue CLI / Webpack | Vite |
| 路由 | vue-router 3 | vue-router 4 |
| 状态管理 | vuex 3 | vuex 4 |
| 后端框架 | Spring Boot 2.6.x | Spring Boot 3.2.x |
| Java 版本 | Java 8 | Java 17 |
| 注解体系 | `javax.*` | `jakarta.*` |

---

## 项目结构

```text
springboot_vue_wms
├── README.md                          # 项目说明文档
├── 项目实现思路与接口梳理.md             # 旧版接口与实现说明
├── wms                               # Spring Boot 后端
│   ├── pom.xml
│   ├── .mvn/settings.xml            # 项目级 Maven settings，优先走 Maven Central
│   └── src/main
│       ├── java/com/wms
│       │   ├── common               # 通用返回、分页参数、跨域、MyBatis-Plus 配置
│       │   ├── controller           # 控制器层
│       │   ├── entity               # 实体类
│       │   ├── mapper               # Mapper 接口
│       │   ├── service              # 服务接口与实现
│       │   └── WmsApplication.java
│       └── resources
│           ├── application.yml
│           └── mapper               # MyBatis XML
└── wms-web                           # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src
        ├── assets
        ├── components
        ├── composables              # 可复用逻辑
        ├── router
        ├── store
        └── utils
```

---

## 运行环境

建议使用以下版本：

- JDK 17+
- Maven 3.6+
- Node.js 18+
- npm 9+
- MySQL 8.x

本项目已在以下环境下完成构建验证：

- Java 17
- Maven 3.6.3
- Node.js 24.x
- npm 11.x

---

## 快速开始

### 1. 克隆项目

```bash
git clone <your-gitee-repository-url>
cd springboot_vue_wms
```

### 2. 准备数据库

1. 创建数据库：

```sql
CREATE DATABASE wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 修改后端数据库配置：

文件位置：

- `wms/src/main/resources/application.yml`

默认配置如下：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wms?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456
```

3. 导入数据库表结构和基础数据。

注意：

- 当前仓库中未提交 `.sql` 初始化脚本
- 如果你准备同步到 Gitee，建议一并补充 `wms.sql`
- 当前代码涉及的核心表通常包括：`user`、`menu`、`storage`、`goodsType`、`goods`、`record`

### 3. 启动后端

进入后端目录：

```bash
cd wms
```

推荐启动方式：

```bash
mvn -s .mvn/settings.xml spring-boot:run
```

说明：

- 项目内已提供 `.mvn/settings.xml`
- 如果你本机 Maven 配置了失效镜像，使用上面的命令更稳
- 默认后端端口为 `8090`

### 4. 启动前端

新开一个终端，进入前端目录：

```bash
cd wms-web
```

安装依赖：

```bash
npm install
```

启动开发环境：

```bash
npm run dev
```

默认前端地址：

- `http://localhost:5173`

### 5. 访问项目

- 前端开发地址：`http://localhost:5173`
- 后端接口地址：`http://localhost:8090`

---

## 打包构建

### 前端构建

```bash
cd wms-web
npm run build
```

构建产物目录：

- `wms-web/dist`

### 后端构建

```bash
cd wms
mvn -s .mvn/settings.xml clean package -DskipTests
```

构建产物目录：

- `wms/target`

---

## 配置说明

### 后端配置

配置文件：

- `wms/src/main/resources/application.yml`

当前主要配置项：

- 服务端口：`8090`
- 数据源：MySQL
- 日志级别：`com.wms: debug`

### 前端接口地址

前端请求封装位于：

- `wms-web/src/utils/request.js`

默认请求地址：

- `http://localhost:8090`

如果你想切换后端地址，可通过环境变量覆盖：

```env
VITE_API_BASE_URL=http://your-api-host:8090
```

可在 `wms-web` 目录下自行创建：

- `.env.development`
- `.env.production`

### 跨域配置

后端已开启全局 CORS，配置位置：

- `wms/src/main/java/com/wms/common/CorsConfig.java`

当前策略为：

- 允许所有来源
- 允许 `GET / POST / PUT / DELETE`
- 允许携带凭证

---

## 页面与模块说明

### 登录与注册

- 登录后，后端根据当前用户角色返回菜单数据
- 前端根据菜单数据动态注册路由并渲染侧边栏

### 用户与管理员

- 用户和管理员页面复用了统一的 CRUD 组件逻辑
- 可按姓名、性别、角色分页查询

### 仓库与分类

- 支持新增、编辑、删除、分页查询
- 为货品模块提供下拉数据源

### 货品管理

- 支持按货品名称、仓库、分类筛选
- 支持新增、编辑、删除
- 支持发起入库、出库操作

### 出入库记录

- 记录货品、申请人、操作人、数量、时间和备注
- 普通用户仅查看与自己相关的记录
- 管理员可查看全量记录

### 个人中心

- 查看当前登录账号信息
- 支持前端侧密码修改表单演示

---

## 后端接口概览

### 用户模块 `/user`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/user/list` | GET | 查询全部用户 |
| `/user/findByNo` | GET | 按账号查询用户 |
| `/user/save` | POST | 新增用户 |
| `/user/update` | POST | 更新用户 |
| `/user/del` | GET | 删除用户 |
| `/user/login` | POST | 登录并返回用户与菜单 |
| `/user/listPageC1` | POST | 多条件分页查询用户 |

### 菜单模块 `/menu`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/menu/list` | GET | 按角色查询菜单 |

### 仓库模块 `/storage`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/storage/save` | POST | 新增仓库 |
| `/storage/update` | POST | 更新仓库 |
| `/storage/del` | GET | 删除仓库 |
| `/storage/listPage` | POST | 分页查询仓库 |
| `/storage/list` | GET | 查询全部仓库 |

### 分类模块 `/goodstype`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/goodstype/save` | POST | 新增分类 |
| `/goodstype/update` | POST | 更新分类 |
| `/goodstype/del` | GET | 删除分类 |
| `/goodstype/listPage` | POST | 分页查询分类 |
| `/goodstype/list` | GET | 查询全部分类 |

### 货品模块 `/goods`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/goods/save` | POST | 新增货品 |
| `/goods/update` | POST | 更新货品 |
| `/goods/del` | GET | 删除货品 |
| `/goods/listPage` | POST | 分页查询货品 |

### 出入库模块 `/record`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/record/listPage` | POST | 分页查询出入库记录 |
| `/record/save` | POST | 新增出入库记录并同步更新库存 |

---

## 通用返回结构

后端接口统一返回：

```json
{
  "code": 200,
  "msg": "成功",
  "total": 0,
  "data": {}
}
```

字段说明：

- `code`：状态码，`200` 成功，`400` 失败
- `msg`：提示信息
- `total`：分页总数
- `data`：返回数据

分页请求参数使用 `QueryPageParam` 进行封装，典型结构如下：

```json
{
  "pageSize": 10,
  "pageNum": 1,
  "param": {
    "name": "测试",
    "sex": "1"
  }
}
```

---

## 关键业务说明

### 1. 登录与菜单权限

- 登录接口为 `/user/login`
- 登录成功后返回当前用户信息和菜单列表
- 前端根据 `menu` 表中的组件路径动态注册路由
- 侧边栏菜单与角色权限联动

### 2. 出入库逻辑

- 接口：`/record/save`
- 当 `action = 1` 时表示入库
- 当 `action = 2` 时表示出库
- 后端会在保存记录时同步修改 `goods.count`

### 3. 分页与筛选

- 用户、仓库、分类、货品、出入库记录模块都支持分页
- 多数模块支持按关键字段组合筛选

---

## 已知说明

以下几点建议在同步到 Gitee 前一并说明：

- 当前仓库未包含数据库初始化 SQL
- 登录页的注册逻辑默认提交 `roleId = 1`
- 出库逻辑当前未做“库存不足”拦截，如需上线使用建议补充校验
- 前端生产构建可通过，但当前仍有较大的 bundle 告警，后续可继续做按需拆包优化
- `wms-web/README.md` 仍是旧版 Vue CLI 模板说明，如有需要建议后续删除或同步更新

---

## 本地验证情况

当前版本已完成以下验证：

- 前端：`npm run build` 成功
- 后端：`mvn -s .mvn/settings.xml -DskipTests compile` 成功

---

## 后续可优化方向

- 补充数据库初始化脚本 `wms.sql`
- 增加 `Dockerfile` 和 `docker-compose.yml`
- 增加接口文档方案，如 SpringDoc OpenAPI
- 增加登录态持久化和权限守卫优化
- 增加入库/出库库存边界校验
- 增加单元测试与集成测试
- 前端继续做 Element Plus 按需引入和打包体积优化

---

## 声明

本项目更适合作为学习、演示和二次开发基础模板使用。

如果你准备将它同步到 Gitee，建议同时补充：

- 项目截图
- 数据库初始化脚本
- 默认演示账号说明
- 部署地址或演示视频

