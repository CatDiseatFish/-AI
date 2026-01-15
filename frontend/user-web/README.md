# 圆梦动画 Web Frontend

AI Story Studio 前端应用 - 基于 Vue 3 + TypeScript + Vite

## 🚀 快速开始

### 前置要求

- Node.js >= 18.0.0
- npm >= 9.0.0
- 后端API服务运行在 `http://localhost:8080`

### 安装

```bash
# 克隆项目
cd user-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:3000

### 可用命令

```bash
npm run dev          # 启动开发服务器
npm run build        # 生产环境构建
npm run preview      # 预览生产构建
npm run lint         # ESLint检查
npm run format       # Prettier格式化
npm run test         # 运行单元测试
npm run test:e2e     # 运行E2E测试
```

## 📁 项目结构

```
src/
├── api/           # API服务层
├── components/    # 可复用组件
├── layouts/       # 页面布局
├── router/        # 路由配置
├── stores/        # Pinia状态管理
├── views/         # 页面组件
│   ├── auth/      # 登录页面
│   ├── home/      # 首页
│   ├── toolbox/   # AI工具箱
│   ├── invite/    # 邀请页面
│   └── editor/    # 编辑器
└── main.ts
```

## 🎨 设计系统

本项目使用 **Mochiani 设计系统** (暗黑玻璃态风格)

### 主要颜色

- 霓虹青色: `#00FFCC` - 主要强调色
- 蓝色: `#409EFF` - 标准交互色
- 粉红: `#FF005E` - 危险操作
- 背景: `#0D0E12` - 全局背景

### 组件示例

```vue
<!-- 玻璃态卡片 -->
<div class="bg-white/5 border border-white/10 rounded-lg p-4">
  内容
</div>

<!-- 霓虹标签 -->
<span class="bg-[#00FFCC]/20 text-[#00FFCC] px-2 py-0.5 rounded text-xs">
  标签
</span>
```

## 🔌 API集成

所有API调用通过 `src/api/` 统一管理

### 示例

```typescript
import { projectApi } from '@/api/project'

// 获取项目列表
const projects = await projectApi.list({
  page: 1,
  size: 10,
})

// 创建项目
const newProject = await projectApi.create({
  name: '我的项目',
  aspectRatio: '16:9',
  styleCode: 'disney-3d',
})
```

### JWT认证

Token自动存储在 `localStorage`,所有请求自动添加 `Authorization` header

## 📝 开发规范

### 代码风格

- TypeScript 严格模式
- ESLint + Prettier 自动格式化
- 组件使用 `<script setup>` 语法
- Props 和 Emits 使用 TypeScript 定义

### 命名规范

- 组件: PascalCase (e.g., `GlassCard.vue`)
- 文件夹: kebab-case (e.g., `user-profile/`)
- 变量: camelCase (e.g., `userName`)
- 常量: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具配置
```

## 🔑 关键功能

### 1. 用户认证

- 手机号验证码登录
- JWT Token 自动管理
- 路由守卫保护

### 2. 项目管理

- 项目CRUD操作
- 文件夹分组
- 搜索/筛选/分页

### 3. 编辑器

- 分镜脚本编辑
- 角色/场景绑定
- 批量生成操作
- 实时任务轮询

### 4. AI工具箱

- 文本/图片/视频生成
- 7天历史记录
- 异步任务处理

### 5. 邀请系统

- 邀请码管理
- 一键复制邀请链接
- 邀请记录与统计

## ⚠️ 重要说明

### 道具功能处理

道具(Props)功能在前端完全隐藏,但后端数据结构保留:

- ❌ UI层: 表格列、右侧Tab、绑定组件全部移除
- ✅ API层: Request/Response 保留 `props` 字段
- ✅ Store层: State 保留 Props 数据

### 品牌替换

- "麻薯动画" → "圆梦动画"
- "灵点" → "积分"
- Logo/Favicon 已更新

## 🐛 常见问题

### Q: 登录后刷新页面丢失登录状态?

A: 检查 `localStorage` 中的 `token` 是否存在,确保 axios 拦截器正确添加 Authorization header

### Q: API 请求 CORS 错误?

A: 开发环境使用 Vite proxy 配置,确保 `vite.config.ts` 中 proxy 正确配置

### Q: 任务轮询不工作?

A: 检查组件卸载时是否调用 `stopAllPolling()` 清理定时器

### Q: Tailwind 样式不生效?

A: 确保组件文件在 `tailwind.config.js` 的 `content` 配置中

## 📚 文档

- [完整开发计划](./DEVELOPMENT_PLAN.md)
- [后端API文档](../user-api/docs/V1_DEVELOPMENT_GUIDE.md)
- [Mochiani设计系统](./docs/DESIGN_SYSTEM.md) _(待创建)_

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交代码 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 📄 License

Copyright © 2026 圆梦动画

---

**项目状态**: 🚧 开发中
**最后更新**: 2026-01-02
