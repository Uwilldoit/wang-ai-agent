# AI恋爱军师

## 项目介绍

基于 Spring Boot 3 + Spring AI + RAG + Tool Caling + MCP 的企业级 AI 恋爱军师智能体，为用户提供情感指导服务。支持多轮对话、记忆持久化、RAG 知识库检索等能力，并且基于 ReAct 模式，能够自主思考并调用工具来完成复杂任务,比如利用网页搜索、资源下载和 PDF 生成工具制定完整的约会计划并生成文档。



## 项目功能

项目中，我们将开发一个AI恋爱军师应用、一个拥有自主规划能力的超级智能体，以及一系列工具和MCP服务。

具体功能如下：

- **AI 恋爱军师应用**:用户在恋爱过程中难免遇到各种难题，让AI为用户提供感指导。支持多轮对话、对话记忆持久化、RAG 知识库检索、工具调用、MCP服务调用。
- **AI超级智能体**:可以根据用户的需求，自主推理和行动，直到完成目标。
- **提供给 AI 的工具**:包括联网搜索、文件操作、网页抓取、资源下载、终端PDF 生成。
- **AI MCP 服务**:可以从特定菜谱中搜索做菜方法。



## 技术选型

项目以Spring AI 开发框架实战为核心，涉及到多种主流AI客户端和工具库的运用。

- Java 21 + Spring Boot 3 框架
- Spring AI
- RAG知识库
- PGVector 向量数据库
- Tool Calling 工具调用
- MCP 模型上下文协议
- ReAct Agent 智能体构建
- AI 大模型开发平台百炼
- Cursor AI 代码生成
- SSE异步推送
- 第三方接口：如SearchAPI
- 工具库如：Kryo 高性能序列化 +Jsoup 网页抓取 + iText PDF 生成 + Knife4j 接口文档 + Hutool 工具





## 项目运行配置

### 后端

必须启动的服务包含 `wang-howtocook-mcp-server`、`PostgreSQL`、`MySQL`

本地将`wang-howtocook-mcp-server`运行`maven`

中的`package`打包，并在`resources`目录下新建文件 `mcp-servers.json`，文件内容如下：

```json
{
  "mcpServers": {
    "amap-maps": {
      "command": "npx.cmd",(这里是Windows系统的运行命令，Linux或Mac去掉.cmd)
      "args": [
        "-y",
        "@amap/amap-maps-mcp-server"
      ],
      "env": {
        "AMAP_MAPS_API_KEY": "你的高德地图MCP的API"
      }
    },
    "wang-howtocook-mcp-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "wang-howtocook-mcp-server/target/wang-howtocook-mcp-server-0.0.1-SNAPSHOT.jar"
      ],
      "env": {}
    }
  }
}
```



`application-local.yml`文件内容如下：

```yaml
# 阿里云 AI 配置
spring:
  application:
    name: spring-ai-alibaba-qwq-chat-client-example
  ai:
    dashscope:
    #todo
      api-key: 你的API key
      chat:
        options:
        #todo 可以自己选择
          model: qwen-plus
    vectorstore:
      pgvector:
        index-type: HNSW
        dimensions: 1536
        distance-type: COSINE_DISTANCE
        max-document-batch-size: 10000 # Optional: Maximum number of documents per batch

  datasource:
    driver-class-name: org.postgresql.Driver
    #todo
    url: jdbc:postgresql://你的地址/wang_ai_agent
    username: 你的用户名
    password: 你的密码

#todo
search-api:
  api-key: 你的API key

```



### 前端

#### 安装与运行

1. **安装依赖**

```bash
npm install
```

2. **启动开发服务器**

```bash
npm run dev
```

3. **访问项目**

浏览器打开 [http://localhost:5173](http://localhost:5173)

4. **构建生产包**

```bash
npm run build
```

#### 目录结构

```
wang-ai-agent-frontend/
├─ public/
│  ├─ ai-love-avatar.png         # AI 恋爱大军师头像
│  └─ ai-manus-avatar.png        # AI 超级智能体头像
├─ src/
│  ├─ main.js                    # 入口文件
│  ├─ App.vue                    # 根组件
│  ├─ router/
│  │  └─ index.js                # 路由配置
│  └─ views/
│     ├─ Home.vue                # 主页
│     ├─ LoveChat.vue            # AI 恋爱军师
│     └─ ManusChat.vue           # AI 超级智能体
├─ index.html
├─ package.json
├─ vite.config.js
└─ README.md
```

#### 常见问题

- **图片无法显示？**
  - 请确保头像图片放在 `public` 目录下，重启开发服务器。
- **端口冲突？**
  - 前端默认端口为 5173，可在 `vite.config.js` 修改。
  - 后端默认端口为8123，前缀为`/api`。



# 交流与反馈

如有问题或建议，欢迎提 issue 或联系开发者。 
