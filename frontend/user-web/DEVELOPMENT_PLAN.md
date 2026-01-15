# 圆梦动画 Web Frontend - 完整开发计划

## 📋 项目概述

**项目名称**: 圆梦动画 AI Story Studio Web Frontend
**技术栈**: Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios + Naive UI + Tailwind CSS
**后端接口**: http://localhost:8080/api (77个API端点)
**设计风格**: Mochiani 暗黑玻璃态设计系统

---

## 🎯 核心需求

### 功能范围
- ✅ **首页**: 轮播图 + 最近项目卡片 + 项目表格 + 快速操作
- ✅ **AI工具箱**: 独立页面,支持文本/图片/视频生成,7天历史记录
- ✅ **邀请功能**: 邀请码展示 + 记录列表 + 统计数据 + 规则说明
- ✅ **编辑器**: 分镜表格 + 角色/场景管理 + 批量生成 + 实时任务轮询
- ✅ **登录认证**: 手机号验证码登录 + JWT鉴权

### 特殊要求
- **道具功能**: 隐藏UI但保留数据结构(Props字段保留在API调用中,但前端不显示相关组件)
- **品牌替换**: "麻薯动画" → "圆梦动画", "灵点" → "积分"
- **无需功能**: 创作空间、云空间、视频高清页面

---

## 🏗️ 项目结构

```
user-web/
├── public/                     # 静态资源
│   ├── favicon.ico
│   └── logo.png
├── src/
│   ├── api/                    # API服务层
│   │   ├── index.ts            # Axios实例配置
│   │   ├── auth.ts             # 认证API
│   │   ├── project.ts          # 项目API
│   │   ├── shot.ts             # 分镜API
│   │   ├── character.ts        # 角色API
│   │   ├── scene.ts            # 场景API
│   │   ├── asset.ts            # 资产API
│   │   ├── job.ts              # 任务API
│   │   ├── generate.ts         # 生成API
│   │   ├── toolbox.ts          # 工具箱API
│   │   ├── invite.ts           # 邀请API
│   │   └── wallet.ts           # 钱包API
│   ├── assets/                 # 静态资源
│   │   ├── images/
│   │   ├── fonts/
│   │   └── icons/
│   ├── components/             # 通用组件
│   │   ├── base/               # 基础组件
│   │   │   ├── GlassCard.vue
│   │   │   ├── NeonTag.vue
│   │   │   ├── PillButton.vue
│   │   │   └── FileUploader.vue
│   │   ├── common/             # 业务通用组件
│   │   │   ├── AssetThumbnail.vue
│   │   │   ├── JobProgressModal.vue
│   │   │   ├── ProjectCard.vue
│   │   │   └── InviteCodeCard.vue
│   │   └── layout/             # 布局组件
│   │       ├── NavSidebar.vue
│   │       └── Header.vue
│   ├── composables/            # 组合式函数
│   │   ├── useAuth.ts
│   │   ├── useJobPolling.ts
│   │   └── useClipboard.ts
│   ├── constants/              # 常量枚举
│   │   ├── enums.ts
│   │   └── config.ts
│   ├── layouts/                # 页面布局
│   │   ├── MainLayout.vue      # 主布局(带侧边栏)
│   │   └── AuthLayout.vue      # 登录布局
│   ├── router/                 # 路由配置
│   │   ├── index.ts
│   │   └── guards.ts
│   ├── stores/                 # Pinia状态管理
│   │   ├── user.ts             # 用户状态
│   │   ├── project.ts          # 项目状态
│   │   ├── editor.ts           # 编辑器状态
│   │   ├── toolbox.ts          # 工具箱状态
│   │   └── invite.ts           # 邀请状态
│   ├── styles/                 # 全局样式
│   │   ├── index.css           # Tailwind入口
│   │   ├── mochiani.css        # Mochiani设计token
│   │   └── scrollbar.css       # 滚动条样式
│   ├── types/                  # TypeScript类型
│   │   ├── api.ts              # API类型(镜像后端DTO)
│   │   ├── store.ts            # Store类型
│   │   └── global.d.ts         # 全局类型声明
│   ├── utils/                  # 工具函数
│   │   ├── jwt.ts              # JWT处理
│   │   ├── format.ts           # 格式化工具
│   │   └── validate.ts         # 表单验证
│   ├── views/                  # 页面组件
│   │   ├── auth/               # 认证页面
│   │   │   └── Login.vue
│   │   ├── home/               # 首页
│   │   │   ├── HomePage.vue
│   │   │   └── components/
│   │   │       ├── BannerCarousel.vue
│   │   │       ├── RecentProjects.vue
│   │   │       ├── ProjectTable.vue
│   │   │       └── CreateProjectModal.vue
│   │   ├── toolbox/            # AI工具箱
│   │   │   ├── ToolboxPage.vue
│   │   │   └── components/
│   │   │       ├── GenerationForm.vue
│   │   │       └── HistoryList.vue
│   │   ├── invite/             # 邀请页面
│   │   │   ├── InvitePage.vue
│   │   │   └── components/
│   │   │       ├── MyCodeCard.vue
│   │   │       ├── RecordsTable.vue
│   │   │       ├── StatsPanel.vue
│   │   │       └── RulesSection.vue
│   │   └── editor/             # 编辑器
│   │       ├── EditorPage.vue
│   │       └── components/
│   │           ├── StoryboardTable.vue
│   │           ├── StoryboardRow.vue
│   │           ├── CharacterPanel.vue
│   │           ├── ScenePanel.vue
│   │           ├── BatchToolbar.vue
│   │           └── AssetVersionModal.vue
│   ├── App.vue
│   └── main.ts
├── .env.development            # 开发环境配置
├── .env.production             # 生产环境配置
├── .eslintrc.cjs               # ESLint配置
├── .prettierrc.json            # Prettier配置
├── index.html
├── package.json
├── postcss.config.js
├── tailwind.config.js          # Tailwind配置
├── tsconfig.json               # TypeScript配置
├── vite.config.ts              # Vite配置
└── README.md
```

---

## 🎨 设计系统 (Mochiani Tokens)

### 颜色系统

```javascript
// Tailwind配置
colors: {
  // 品牌色
  'mochi-cyan': '#00FFCC',      // 霓虹青色 - 主要强调色
  'mochi-blue': '#409EFF',      // 蓝色 - 标准交互色
  'mochi-pink': '#FF005E',      // 粉红 - 删除/危险操作
  'mochi-yellow': '#FFF600',    // 黄色 - Logo渐变

  // 背景
  'mochi-bg': '#0D0E12',        // 全局背景
  'mochi-surface-l1': '#1E2025', // Header/侧边栏
  'mochi-surface-l2': '#191A1E', // 内容容器

  // 文字
  'mochi-text': '#FBFBFB',      // 主要文字
}
```

### 组件样式模式

#### 玻璃态卡片 (Glassmorphism)
```html
<div class="bg-white/5 border border-white/10 rounded-lg p-4">
  <!-- 内容 -->
</div>
```

#### 霓虹青色标签
```html
<span class="bg-[#00FFCC]/20 text-[#00FFCC] px-2 py-0.5 rounded text-xs font-medium">
  标签
</span>
```

#### 活跃指示器(带发光)
```html
<div class="w-2.5 h-2.5 bg-[#00FFCC] rounded-full shadow-[0_0_6px_2px_rgba(0,255,204,0.7)]"></div>
```

#### 药丸按钮
```html
<button class="px-3 py-1 rounded-full border border-white/20 text-white/60 hover:bg-white/5 transition-colors text-xs">
  按钮
</button>
```

---

## 🔌 API 集成设计

### Axios 实例配置

```typescript
// src/api/index.ts
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
})

// 请求拦截器 - 添加JWT Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 处理Result包装 & 错误
api.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data
    if (code === 200) {
      return data // 直接返回data,剥离Result包装
    }
    // 业务错误
    window.$message?.error(message || '操作失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token失效,跳转登录
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    window.$message?.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api
```

### 类型定义示例

```typescript
// src/types/api.ts

// 项目创建请求
export interface ProjectCreateRequest {
  name: string
  folderId?: number
  aspectRatio: '16:9' | '9:16' | '21:9'
  styleCode: string
  eraSetting?: string
  rawText?: string
}

// 分镜VO
export interface StoryboardShotVO {
  id: number
  shotNo: number
  scriptText: string
  characters: BoundCharacterVO[]
  scene: BoundSceneVO | null
  shotImage: AssetStatusVO
  video: AssetStatusVO
  createdAt: string
  updatedAt: string
}

// 资产状态
export interface AssetStatusVO {
  assetId: number | null
  currentVersionId: number | null
  currentUrl: string | null
  status: 'NONE' | 'GENERATING' | 'READY' | 'FAILED'
  totalVersions: number
}

// 工具箱生成请求
export interface ToolboxGenerateRequest {
  type: 'TEXT' | 'IMAGE' | 'VIDEO'
  prompt: string
  model?: string
  aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
  duration?: number
  referenceImageUrl?: string
}

// 邀请码VO
export interface InviteCodeVO {
  id: number
  code: string
  rewardPoints: number
  inviterRewardPoints: number
  usedCount: number
  maxUses: number | null
  expireAt: string | null
  enabled: number
  createdAt: string
}
```

---

## 🗂️ 状态管理 (Pinia Stores)

### 1. User Store

```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as UserVO | null,
    points: 0,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userName: (state) => state.user?.nickname || '用户',
  },

  actions: {
    async login(phone: string, code: string) {
      const data = await authApi.login(phone, code)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
    },

    logout() {
      this.token = ''
      this.user = null
      this.points = 0
      localStorage.removeItem('token')
    },

    async fetchProfile() {
      const data = await authApi.getProfile()
      this.user = data.user
      this.points = data.points
    },
  },
})
```

### 2. Editor Store (复杂)

```typescript
// src/stores/editor.ts
import { defineStore } from 'pinia'
import { shotApi, jobApi } from '@/api'

export const useEditorStore = defineStore('editor', {
  state: () => ({
    projectId: null as number | null,
    shots: [] as StoryboardShotVO[],
    characters: [] as ProjectCharacterVO[],
    scenes: [] as ProjectSceneVO[],
    selectedShotIds: [] as number[],
    pollingJobIds: new Set<number>(),
    pollingTimers: new Map<number, NodeJS.Timeout>(),
  }),

  actions: {
    async fetchShots(projectId: number) {
      this.projectId = projectId
      this.shots = await shotApi.list(projectId)
    },

    async batchGenerateShots(mode: 'ALL' | 'MISSING', count: number) {
      const jobId = await generateApi.batchShots(this.projectId!, {
        targetIds: this.selectedShotIds,
        mode,
        countPerItem: count,
        aspectRatio: '21:9',
      })
      this.startJobPolling(jobId)
    },

    startJobPolling(jobId: number) {
      if (this.pollingTimers.has(jobId)) return

      const timer = setInterval(async () => {
        const job = await jobApi.getJob(jobId)

        if (job.status === 'COMPLETED' || job.status === 'FAILED') {
          this.stopJobPolling(jobId)
          this.fetchShots(this.projectId!) // 刷新数据

          if (job.status === 'COMPLETED') {
            window.$message?.success('生成完成!')
          } else {
            window.$message?.error(`生成失败: ${job.errorMessage}`)
          }
        }
      }, 3000) // 每3秒轮询一次

      this.pollingTimers.set(jobId, timer)
      this.pollingJobIds.add(jobId)
    },

    stopJobPolling(jobId: number) {
      const timer = this.pollingTimers.get(jobId)
      if (timer) {
        clearInterval(timer)
        this.pollingTimers.delete(jobId)
        this.pollingJobIds.delete(jobId)
      }
    },

    stopAllPolling() {
      this.pollingTimers.forEach(timer => clearInterval(timer))
      this.pollingTimers.clear()
      this.pollingJobIds.clear()
    },
  },
})
```

---

## 🛣️ 路由配置

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { layout: 'auth', requiresAuth: false },
    },
    {
      path: '/',
      redirect: '/home',
      meta: { layout: 'main', requiresAuth: true },
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/home/HomePage.vue'),
      meta: { layout: 'main', requiresAuth: true },
    },
    {
      path: '/toolbox',
      name: 'Toolbox',
      component: () => import('@/views/toolbox/ToolboxPage.vue'),
      meta: { layout: 'main', requiresAuth: true },
    },
    {
      path: '/invite',
      name: 'Invite',
      component: () => import('@/views/invite/InvitePage.vue'),
      meta: { layout: 'main', requiresAuth: true },
    },
    {
      path: '/editor/:projectId',
      name: 'Editor',
      component: () => import('@/views/editor/EditorPage.vue'),
      meta: { layout: 'main', requiresAuth: true },
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && userStore.isAuthenticated) {
    next('/home')
  } else {
    next()
  }
})

export default router
```

---

## 📅 开发阶段 (18天)

### Phase 1: 项目初始化 (Day 1)
- [ ] 创建Vite + Vue 3 + TypeScript项目
- [ ] 安装依赖: pinia, vue-router, axios, naive-ui, tailwindcss
- [ ] 配置Tailwind (导入Mochiani tokens)
- [ ] 配置ESLint, Prettier, Husky
- [ ] 创建项目文件夹结构
- [ ] 配置Vite代理 (proxy到http://localhost:8080/api)
- [ ] 创建.env.development和.env.production

### Phase 2: 基础架构 (Days 2-3)
- [ ] 实现Axios实例 + JWT拦截器
- [ ] 定义所有TypeScript类型 (镜像后端DTO)
- [ ] 创建基础组件库:
  - [ ] GlassCard.vue
  - [ ] NeonTag.vue
  - [ ] PillButton.vue
  - [ ] FileUploader.vue
- [ ] 配置Router + Auth Guards
- [ ] 实现userStore (login/logout/fetchProfile)
- [ ] 创建AuthLayout和Login页面
- [ ] 测试完整登录流程

### Phase 3: 首页 & 导航 (Days 4-5)
- [ ] MainLayout组件 (含NavSidebar)
- [ ] NavSidebar实现 (首页/AI工具箱/邀请/软件设置)
- [ ] HomePage布局:
  - [ ] BannerCarousel (轮播图)
  - [ ] RecentProjects (最近项目卡片横向滚动)
  - [ ] ProjectTable (项目表格 + 搜索/筛选/分页)
  - [ ] CreateProjectModal (创建项目弹窗 + 风格选择器)
- [ ] projectStore实现
- [ ] 文件夹管理功能
- [ ] 项目CRUD操作

### Phase 4: AI工具箱 (Day 6)
- [ ] ToolboxPage布局
- [ ] GenerationForm组件:
  - [ ] 类型选择器 (文本/图片/视频)
  - [ ] 模型选择器 (含即梦反代选项)
  - [ ] 比例选择器
  - [ ] 参考图上传
  - [ ] 提示词输入框 (字数统计)
- [ ] HistoryList组件 (7天历史记录)
- [ ] toolboxStore实现
- [ ] 异步视频生成任务轮询
- [ ] 保存到资产库功能

### Phase 5: 邀请系统 (Day 7)
- [ ] InvitePage布局
- [ ] MyCodeCard (邀请码展示 + 一键复制链接)
- [ ] RecordsTable (邀请记录表格)
- [ ] StatsPanel (统计数据可视化)
- [ ] RulesSection (邀请规则说明)
- [ ] inviteStore实现
- [ ] 复制邀请链接功能 (格式化文本)

### Phase 6: 编辑器 - 核心 (Days 8-11)
- [ ] EditorPage布局
- [ ] StoryboardTable组件 (复杂表格):
  - [ ] 表头 (序号/剧本/出场人物/场景/道具-隐藏/分镜图/视频/操作)
  - [ ] 固定表头 (sticky)
  - [ ] 虚拟滚动 (优化性能)
- [ ] StoryboardRow组件:
  - [ ] 复选框
  - [ ] 序号显示
  - [ ] 剧本编辑 (双击放大)
  - [ ] 角色缩略图网格 (可绑定/解绑)
  - [ ] 场景缩略图
  - [ ] 道具列数据保留但UI隐藏
  - [ ] 分镜图状态 (NONE/GENERATING/READY/FAILED)
  - [ ] 视频状态 + 播放预览
  - [ ] 操作按钮 (上移/下移/删除)
- [ ] 右侧边栏Tabs:
  - [ ] CharacterPanel (角色Tab)
  - [ ] ScenePanel (场景Tab)
  - [ ] Props Tab完全禁用/隐藏
- [ ] AssetThumbnail组件 (带hover zoom + active dot)
- [ ] editorStore实现
- [ ] 分镜CRUD操作
- [ ] 角色/场景绑定功能

### Phase 7: 编辑器 - 生成功能 (Days 12-13)
- [ ] BatchToolbar组件 (勾选后显示):
  - [ ] 全选/反选
  - [ ] 批量生成分镜图下拉 (全部生成/缺失生成)
  - [ ] 批量生成视频下拉
  - [ ] 数量选择器
- [ ] CharacterPanel生成面板:
  - [ ] 一键生成所有角色
  - [ ] 单个角色生成
  - [ ] 上传本地图片
  - [ ] 比例选择 (默认21:9)
- [ ] ScenePanel生成面板 (同上)
- [ ] JobProgressModal (任务进度弹窗)
- [ ] 任务轮询机制 (editorStore)
- [ ] 进度指示器 (loading状态)
- [ ] 生成完成后自动刷新

### Phase 8: 资产管理 (Day 14)
- [ ] AssetVersionModal (版本历史弹窗)
- [ ] 版本列表展示
- [ ] 版本切换功能
- [ ] 本地文件上传 (OSS集成)
- [ ] 导出功能实现:
  - [ ] 导出选项选择 (角色/场景/分镜图/视频)
  - [ ] 导出模式选择 (当前版本/全部版本)
  - [ ] ZIP下载

### Phase 9: 测试 & 优化 (Days 15-16)
- [ ] 单元测试:
  - [ ] Stores测试
  - [ ] Utils测试
  - [ ] API Mock测试
- [ ] E2E测试:
  - [ ] 登录流程
  - [ ] 创建项目流程
  - [ ] 生成任务流程
- [ ] 性能优化:
  - [ ] 组件懒加载
  - [ ] 路由懒加载
  - [ ] 虚拟滚动 (大表格)
  - [ ] 图片懒加载
  - [ ] Debounce搜索输入
- [ ] 错误边界实现
- [ ] Loading状态优化
- [ ] 空状态设计

### Phase 9.5: 工具箱优化 & 模型配置 (Day 17, 0.5天)
- [ ] **9.5A: 文档更新 (0.25h)**
  - [ ] 更新FEATURE_GAP_ANALYSIS.md (API覆盖率统计)
  - [ ] 更新DEVELOPMENT_PLAN.md (添加Phase 9.5和Phase 10)
- [ ] **9.5B: 工具箱自动刷新修复 (0.5h)**
  - [ ] 修复toolbox.ts第56行即时刷新问题
  - [ ] 添加500ms延迟避免数据库写入竞态
  - [ ] 扩展ToolboxHistoryVO类型定义 (添加text字段)
  - [ ] 实现TEXT/VIDEO内容预览 (HistoryList.vue)
- [ ] **9.5C: 模型配置UI (3.5h)**
  - [ ] 创建modelConfig.ts store
  - [ ] 创建ModelConfigModal.vue组件:
    - [ ] 图片生成模型选择器
    - [ ] 视频生成模型选择器
    - [ ] 配置持久化(localStorage)
  - [ ] 集成到EditorPage.vue (连接"模型配置"按钮)

### Phase 10: 钱包/积分页面 (Day 17.5-18, 0.5天)
- [ ] **API层 (已完成✅)**
  - [x] wallet.ts API已实现 (getBalance, getTransactionHistory)
  - [x] TransactionVO类型已定义
  - [x] userStore已集成points字段和setPoints方法
- [ ] **路由配置 (0.5h)**
  - [ ] 添加/wallet路由到router/index.ts
  - [ ] 修改NavSidebar.vue添加"我的钱包"导航入口
- [ ] **页面与组件开发 (3.5h)**
  - [ ] 创建WalletPage.vue主页面:
    - [ ] 页面布局 (MainLayout)
    - [ ] 集成PointsBalanceCard和TransactionTable
  - [ ] 创建PointsBalanceCard.vue:
    - [ ] 显示当前积分余额
    - [ ] 显示冻结积分
    - [ ] "立即充值"按钮占位 (Phase 13实现)
  - [ ] 创建TransactionTable.vue:
    - [ ] 流水记录表格 (类型/金额/余额/描述/时间)
    - [ ] 分页功能
    - [ ] 类型筛选器 (全部/充值/消费/奖励)
  - [ ] 修改EditorPage.vue Header显示积分余额

### Phase 11: 构建 & 部署 (Day 18)
- [ ] 生产环境构建优化
- [ ] 环境变量配置
- [ ] 打包体积分析
- [ ] Docker镜像构建 (可选)
- [ ] 部署文档编写
- [ ] 用户手册编写

---

## 🔑 关键技术实现

### 1. JWT认证流程

```typescript
// 登录成功后
localStorage.setItem('token', token)

// Axios请求拦截器自动添加
config.headers.Authorization = `Bearer ${token}`

// 401响应自动跳转登录
if (error.response?.status === 401) {
  userStore.logout()
  router.push('/login')
}
```

### 2. 任务轮询机制

```typescript
// editorStore.ts
async startJobPolling(jobId: number) {
  const timer = setInterval(async () => {
    const job = await jobApi.getJob(jobId)

    if (job.status === 'COMPLETED') {
      clearInterval(timer)
      this.fetchShots(this.projectId!) // 刷新数据
      window.$message?.success('生成完成!')
    }
  }, 3000) // 3秒轮询间隔

  this.pollingTimers.set(jobId, timer)
}

// 组件卸载时清理
onUnmounted(() => {
  editorStore.stopAllPolling()
})
```

### 3. 道具功能隐藏实现

```typescript
// API调用保留Props字段
interface ShotVO {
  // ... 其他字段
  props?: PropVO[]  // 保留数据结构
}

// 前端组件不渲染Props列
<template>
  <table>
    <thead>
      <th>角色</th>
      <th>场景</th>
      <!-- 道具列完全移除,不渲染 -->
      <th>分镜图</th>
    </thead>
  </table>
</template>

// 右侧边栏Props Tab禁用
<n-tabs>
  <n-tab-pane name="characters" tab="角色" />
  <n-tab-pane name="scenes" tab="场景" />
  <!-- Props Tab完全移除或disabled -->
</n-tabs>
```

### 4. 品牌替换策略

```typescript
// constants/branding.ts
export const BRANDING = {
  appName: '圆梦动画',  // 替换 "麻薯动画"
  currency: '积分',     // 替换 "灵点"
  currencyIcon: '✨',
}

// 全局搜索替换
// "麻薯动画" → "圆梦动画"
// "灵点" → "积分"
// "MochiAni" → "YuanMeng" (代码中)
```

### 5. 图片懒加载

```vue
<template>
  <img
    v-lazy="imageUrl"
    class="transition-opacity duration-300"
    @load="onImageLoad"
  />
</template>

<script setup>
// 或使用Intersection Observer
import { useIntersectionObserver } from '@vueuse/core'

const { stop } = useIntersectionObserver(
  imageRef,
  ([{ isIntersecting }]) => {
    if (isIntersecting) {
      loadImage()
      stop()
    }
  }
)
</script>
```

### 6. 虚拟滚动 (大表格优化)

```vue
<template>
  <n-virtual-list
    :items="shots"
    :item-size="120"
    style="max-height: 600px"
  >
    <template #default="{ item }">
      <StoryboardRow :shot="item" />
    </template>
  </n-virtual-list>
</template>
```

---

## 📦 依赖清单

### package.json

```json
{
  "name": "yuanmeng-web",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --fix",
    "format": "prettier --write src/",
    "test": "vitest",
    "test:e2e": "playwright test"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "naive-ui": "^2.38.0",
    "@vueuse/core": "^10.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "@vue/test-utils": "^2.4.0",
    "typescript": "^5.3.0",
    "vue-tsc": "^1.8.0",
    "vite": "^5.0.0",
    "tailwindcss": "^3.4.0",
    "autoprefixer": "^10.4.0",
    "postcss": "^8.4.0",
    "eslint": "^8.56.0",
    "eslint-plugin-vue": "^9.19.0",
    "@typescript-eslint/parser": "^6.0.0",
    "prettier": "^3.1.0",
    "vitest": "^1.1.0",
    "@playwright/test": "^1.40.0",
    "husky": "^8.0.0",
    "lint-staged": "^15.2.0"
  }
}
```

---

## ⚙️ 配置文件

### vite.config.ts

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'naive-ui': ['naive-ui'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
        },
      },
    },
  },
})
```

### tailwind.config.js

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // 品牌色
        'mochi-cyan': '#00FFCC',
        'mochi-blue': '#409EFF',
        'mochi-pink': '#FF005E',
        'mochi-yellow': '#FFF600',
        'mochi-electric': '#21FFF3',

        // 背景
        'mochi-bg': '#0D0E12',
        'mochi-surface-l1': '#1E2025',
        'mochi-surface-l2': '#191A1E',

        // 文字
        'mochi-text': '#FBFBFB',
        'mochi-text-alt': '#D9D9D9',
      },
      boxShadow: {
        'neon-cyan': '0 0 6px 2px rgba(0, 255, 204, 0.7)',
      },
      fontSize: {
        'xs': ['12px', { lineHeight: '1rem' }],
        'sm': ['14px', { lineHeight: '1.25rem' }],
        'base': ['16px', { lineHeight: '1.4' }],
        'lg': ['18px', { lineHeight: '1.4' }],
      },
    },
  },
  plugins: [],
}
```

### tsconfig.json

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

---

## 🚀 开发流程

### 启动项目

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 启动后端API (另一个终端)
cd ../user-api
./mvnw spring-boot:run
```

### 访问地址

- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api
- Swagger文档: http://localhost:8080/swagger-ui.html

### Git工作流

```bash
# 功能分支开发
git checkout -b feature/home-page
# 提交代码
git add .
git commit -m "feat: implement home page layout"
# 推送并创建PR
git push origin feature/home-page
```

---

## 📝 关键注意事项

### 1. 道具功能处理
- ✅ **API层**: 保留所有Props相关字段在Request/Response中
- ✅ **Store层**: 保留Props数据结构在State中
- ❌ **UI层**: 完全移除Props相关组件(表格列、右侧Tab、绑定UI)
- 📌 **代码注释**: 在隐藏处添加注释 `// Props功能隐藏但保留数据结构`

### 2. 品牌一致性
- 全局搜索 "麻薯动画" 替换为 "圆梦动画"
- 全局搜索 "灵点" 替换为 "积分"
- Logo图片更新为圆梦动画Logo
- Favicon更新

### 3. 性能优化
- 使用虚拟滚动处理大列表(>100项)
- 图片懒加载(使用Intersection Observer)
- 路由和组件懒加载
- Debounce搜索输入(300ms延迟)
- 避免在循环中创建响应式对象

### 4. 错误处理
- 所有API调用使用try-catch包裹
- 统一使用Naive UI的message组件显示错误
- 登录失败显示具体错误原因
- 生成失败显示job.errorMessage

### 5. 类型安全
- 所有API响应都定义TypeScript类型
- 避免使用`any`,使用`unknown`替代
- Pinia Store的State都有明确类型
- Props和Emits都使用TypeScript定义

### 6. 任务轮询注意事项
- 组件卸载时必须清理定时器
- 避免创建重复的轮询定时器(检查jobId是否已存在)
- 轮询间隔建议: 图片3秒,视频5秒
- 失败后停止轮询,不要无限重试

### 7. 文件上传
- 使用FormData上传文件
- 显示上传进度条
- 限制文件大小(图片<10MB,视频<100MB)
- 限制文件类型(jpg/png/webp)

### 8. 邀请链接格式
```
您的好友邀请你加入圆梦动画免费创作AI漫剧:
https://www.yuanmeng.com/invite/GMJrwP9AnT
```

---

## 🎯 验收标准

### 功能完整性
- [ ] 所有77个API端点都正确对接
- [ ] 用户可以登录并保持登录状态
- [ ] 首页显示项目列表,支持搜索/筛选/分页
- [ ] 可以创建项目并跳转到编辑器
- [ ] 编辑器可以编辑分镜脚本
- [ ] 可以绑定角色和场景到分镜
- [ ] 批量生成功能正常工作
- [ ] 任务轮询正确更新状态
- [ ] AI工具箱可以生成文本/图片/视频
- [ ] 邀请功能可以生成邀请码并复制链接
- [ ] 所有"道具"相关UI已移除但数据保留

### UI/UX标准
- [ ] 严格遵循Mochiani设计系统
- [ ] 所有背景色为#0D0E12
- [ ] 霓虹青色(#00FFCC)用于强调元素
- [ ] 玻璃态效果(bg-white/5 + border-white/10)
- [ ] 所有交互都有平滑过渡动画
- [ ] Hover状态明显(bg-white/5或bg-white/10)
- [ ] Loading状态友好(骨架屏或Spinner)
- [ ] 空状态有引导文案

### 性能标准
- [ ] 首屏加载时间<2秒
- [ ] 路由切换流畅(<300ms)
- [ ] 大表格滚动流畅(使用虚拟滚动)
- [ ] 图片懒加载生效
- [ ] 打包后总体积<2MB (gzip前)

### 代码质量
- [ ] TypeScript覆盖率100%
- [ ] 无ESLint错误
- [ ] 所有组件都有明确的Props类型定义
- [ ] 关键函数有注释说明
- [ ] 无Console.log残留
- [ ] Git提交信息规范(feat/fix/docs/style/refactor)

---

## 📚 参考文档

- [Vue 3 官方文档](https://vuejs.org/)
- [Pinia 官方文档](https://pinia.vuejs.org/)
- [Naive UI 组件库](https://www.naiveui.com/)
- [Tailwind CSS 文档](https://tailwindcss.com/)
- [VueUse 工具库](https://vueuse.org/)
- [后端API文档](../user-api/docs/V1_DEVELOPMENT_GUIDE.md)

---

## 🤝 团队协作

### 前端开发
- 负责Vue组件开发
- UI实现(严格遵循Mochiani设计)
- API对接与状态管理
- 单元测试与E2E测试

### 后端开发
- 提供API接口
- 维护Swagger文档
- 处理业务逻辑
- 数据库设计

### 协作要点
- 使用Swagger自动生成TypeScript类型
- API变更及时通知前端
- 前端Mock数据开发不阻塞
- 定期联调测试

---

**最后更新**: 2026-01-03
**版本**: v1.1 (已添加Phase 9.5和Phase 10)
**作者**: AI Development Team
