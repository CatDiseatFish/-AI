# 圆梦动画 Frontend - 功能缺失分析报告

**生成日期**: 2026-01-03
**分析范围**: 对比后端77个API端点与当前前端开发计划
**目的**: 识别所有缺失的功能模块,制定完整开发计划

---

## 📊 总体概览

| 统计项 | 数量 | 状态 |
|--------|------|------|
| 后端API总数 | 77个 | ✅ 已完成 |
| 当前前端覆盖 | ~53个 | ✅ 良好 |
| 缺失功能模块 | 6大模块 | ⚠️ 部分开发 |
| 缺失API端点 | ~24个 | ⚠️ 持续对接 |

---

## ✅ 已实现功能模块

### 1. 认证模块 (5/5 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| POST /api/auth/send-code | Login.vue | ✅ |
| POST /api/auth/login | Login.vue | ✅ |
| POST /api/auth/logout | userStore | ✅ |
| GET /api/auth/profile | userStore | ✅ |
| PUT /api/auth/profile | - | ⚠️ 可能缺失 |

### 2. 文件夹模块 (6/6 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/folders | projectStore | ✅ |
| POST /api/folders | CreateFolderModal | ✅ |
| PUT /api/folders/{id} | - | ✅ |
| DELETE /api/folders/{id} | - | ✅ |
| POST /api/folders/{id}/move | - | ✅ |
| GET /api/folders/{id}/tree | NavSidebar | ✅ |

### 3. 项目模块 (5/5 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/projects | ProjectTable.vue | ✅ |
| POST /api/projects | CreateProjectModal.vue | ✅ |
| GET /api/projects/{id} | EditorPage.vue | ✅ |
| PUT /api/projects/{id} | - | ✅ |
| DELETE /api/projects/{id} | ProjectTable.vue | ✅ |

### 4. 分镜模块 (6/6 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/projects/{pid}/shots | StoryboardTable.vue | ✅ |
| POST /api/projects/{pid}/shots | StoryboardRow.vue | ✅ |
| PUT /api/projects/{pid}/shots/{id} | StoryboardRow.vue | ✅ |
| DELETE /api/projects/{pid}/shots/{id} | StoryboardRow.vue | ✅ |
| POST /api/projects/{pid}/shots/reorder | editorStore | ✅ |
| POST /api/projects/{pid}/shots/{sid}/bindings | CharacterPanel/ScenePanel | ✅ |

### 5. 工具箱模块 (4/4 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| POST /api/toolbox/generate | ToolboxPage.vue | ✅ |
| GET /api/toolbox/history | ToolboxPage.vue | ✅ |
| POST /api/toolbox/save | ToolboxPage.vue | ✅ |
| DELETE /api/toolbox/history/{id} | ToolboxPage.vue | ✅ |

### 6. 邀请模块 (5/5 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/invite/my-code | InvitePage.vue | ✅ |
| POST /api/invite/generate-code | InvitePage.vue | ✅ |
| POST /api/invite/apply | - | ✅ |
| GET /api/invite/records | InvitePage.vue | ✅ |
| GET /api/invite/stats | InvitePage.vue | ✅ |

### 7. 生成模块 (8/8 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| POST /api/projects/{pid}/parse | - | ✅ |
| POST /api/projects/{pid}/generate/shots | BatchToolbar.vue | ✅ |
| POST /api/projects/{pid}/generate/videos | BatchToolbar.vue | ✅ |
| POST /api/projects/{pid}/generate/characters | CharacterPanel.vue | ✅ |
| POST /api/projects/{pid}/generate/scenes | ScenePanel.vue | ✅ |
| POST /api/projects/{pid}/generate/character/{cid} | CharacterPanel.vue | ✅ |
| POST /api/projects/{pid}/generate/scene/{sid} | ScenePanel.vue | ✅ |
| - | - | - |

### 8. 任务模块 (3/3 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/jobs | editorStore | ✅ |
| GET /api/jobs/{id} | editorStore (polling) | ✅ |
| POST /api/jobs/{id}/cancel | JobProgressModal | ✅ |

### 9. 资产模块 (3/3 API)
| API端点 | 前端实现 | 状态 |
|---------|---------|------|
| GET /api/assets/{id}/versions | AssetVersionModal.vue | ✅ |
| POST /api/assets/{id}/versions/upload | AssetVersionModal.vue | ✅ |
| PUT /api/assets/{id}/current | AssetVersionModal.vue | ✅ |

**已实现功能小计**: 45个API端点

---

## ❌ 缺失功能模块 (重点关注)

### 1. ⚠️ 角色库模块 (3/13 API, 23%完成) - **读取API已就绪**

**功能说明**: 全局角色库系统,支持角色分类、创建通用角色、项目引用、替换等高级功能

**✅ 已实现的3个API**:
- GET /api/library/characters (获取角色库列表) - `src/api/character.ts`
- GET /api/library/characters/categories (获取分类列表) - `src/api/character.ts`
- POST /api/projects/{pid}/characters (从角色库引用) - `src/api/character.ts`

| API端点 | 功能说明 | 优先级 | 预计工作量 |
|---------|---------|--------|-----------|
| **角色分类管理** | | | |
| GET /api/character-categories | 获取角色分类列表 | ✅ 已实现 | - |
| POST /api/character-categories | 创建新分类 | 🔴 高 | 0.5天 |
| PUT /api/character-categories/{id} | 重命名分类 | 🟡 中 | 0.25天 |
| DELETE /api/character-categories/{id} | 删除分类 | 🟡 中 | 0.25天 |
| **全局角色库** | | | |
| GET /api/character-library | 获取角色库列表(带分页/筛选) | ✅ 已实现 | - |
| POST /api/character-library | 创建全局角色 | 🔴 高 | 1天 |
| PUT /api/character-library/{id} | 更新角色信息 | 🔴 高 | 0.5天 |
| DELETE /api/character-library/{id} | 删除角色 | 🟡 中 | 0.25天 |
| **项目角色管理** | | | |
| GET /api/projects/{pid}/characters | 获取项目角色列表 | 🔴 高 | 0.5天 |
| POST /api/projects/{pid}/characters | 从角色库引用角色到项目 | ✅ 已实现 | - |
| PUT /api/projects/{pid}/characters/{id} | 更新项目角色(覆盖全局) | 🟡 中 | 0.5天 |
| DELETE /api/projects/{pid}/characters/{id} | 移除项目角色引用 | 🟡 中 | 0.25天 |
| POST /api/projects/{pid}/characters/{id}/replace | 替换为角色库中的其他角色 | 🟢 低 | 0.5天 |

**缺失前端页面/组件**:
- ❌ **CharacterLibraryPage.vue** (角色库主页面)
- ❌ **CategoryManager.vue** (分类管理组件)
- ❌ **CharacterLibraryModal.vue** (角色选择/引用弹窗)
- ❌ **GlobalCharacterCard.vue** (全局角色卡片)
- ⚠️ **CharacterPanel.vue** (需大幅修改,支持"从角色库引用"功能)

**新增Store需求**:
- ❌ `src/stores/characterLibrary.ts` (角色库状态管理)

**新增API文件**:
- ❌ `src/api/characterLibrary.ts`

**预计总工作量**: 5-6天

---

### 2. ⚠️ 场景库模块 (3/13 API, 23%完成) - **读取API已就绪**

**功能说明**: 全局场景库系统,架构与角色库完全一致

**✅ 已实现的3个API**:
- GET /api/library/scenes (获取场景库列表) - `src/api/scene.ts`
- GET /api/library/scenes/categories (获取分类列表) - `src/api/scene.ts`
- POST /api/projects/{pid}/scenes (从场景库引用) - `src/api/scene.ts`

| API端点 | 功能说明 | 优先级 | 预计工作量 |
|---------|---------|--------|-----------|
| **场景分类管理** | | | |
| GET /api/scene-categories | 获取场景分类列表 | ✅ 已实现 | - |
| POST /api/scene-categories | 创建新分类 | 🔴 高 | 0.5天 |
| PUT /api/scene-categories/{id} | 重命名分类 | 🟡 中 | 0.25天 |
| DELETE /api/scene-categories/{id} | 删除分类 | 🟡 中 | 0.25天 |
| **全局场景库** | | | |
| GET /api/scene-library | 获取场景库列表(带分页/筛选) | ✅ 已实现 | - |
| POST /api/scene-library | 创建全局场景 | 🔴 高 | 1天 |
| PUT /api/scene-library/{id} | 更新场景信息 | 🔴 高 | 0.5天 |
| DELETE /api/scene-library/{id} | 删除场景 | 🟡 中 | 0.25天 |
| **项目场景管理** | | | |
| GET /api/projects/{pid}/scenes | 获取项目场景列表 | 🔴 高 | 0.5天 |
| POST /api/projects/{pid}/scenes | 从场景库引用场景到项目 | ✅ 已实现 | - |
| PUT /api/projects/{pid}/scenes/{id} | 更新项目场景(覆盖全局) | 🟡 中 | 0.5天 |
| DELETE /api/projects/{pid}/scenes/{id} | 移除项目场景引用 | 🟡 中 | 0.25天 |
| POST /api/projects/{pid}/scenes/{id}/replace | 替换为场景库中的其他场景 | 🟢 低 | 0.5天 |

**缺失前端页面/组件**:
- ❌ **SceneLibraryPage.vue** (场景库主页面)
- ❌ **SceneCategoryManager.vue** (分类管理组件)
- ❌ **SceneLibraryModal.vue** (场景选择/引用弹窗)
- ❌ **GlobalSceneCard.vue** (全局场景卡片)
- ⚠️ **ScenePanel.vue** (需大幅修改,支持"从场景库引用"功能)

**新增Store需求**:
- ❌ `src/stores/sceneLibrary.ts` (场景库状态管理)

**新增API文件**:
- ❌ `src/api/sceneLibrary.ts`

**预计总工作量**: 5-6天

---

### 3. ✅ 钱包/积分模块 (2/2 API) - **API已完成**

**功能说明**: 用户积分余额查询、消费流水记录

| API端点 | 功能说明 | 状态 |
|---------|---------|------|
| GET /api/wallet | 查询用户积分余额 | ✅ 已实现 |
| GET /api/wallet/transactions | 查询积分流水记录(分页) | ✅ 已实现 |

**✅ 已实现的API文件**: `src/api/wallet.ts`
**✅ 已调用位置**: `HomePage.vue:58-65`, `userStore.ts:44-46`

**❌ 缺失前端页面/组件** (仅UI部分):
- ❌ **WalletPage.vue** (积分钱包主页面)
- ❌ **TransactionTable.vue** (流水记录表格)
- ❌ **PointsBalanceCard.vue** (余额展示卡片)

**需要修改的现有组件**:
- ⚠️ **NavSidebar.vue** (添加"我的钱包"入口)
- ⚠️ **EditorPage.vue** (Header显示当前积分余额)
- ⚠️ **router/index.ts** (添加 `/wallet` 路由)

**预计总工作量**: 0.5天 (仅UI开发)

---

### 4. ⚠️ 充值/支付模块 (0/5 API) - **商业化关键**

**功能说明**: 积分充值套餐、订单创建、支付回调处理

| API端点 | 功能说明 | 优先级 | 预计工作量 |
|---------|---------|--------|-----------|
| GET /api/recharge/products | 获取充值套餐列表 | 🔴 高 | 0.5天 |
| GET /api/recharge/exchange-rules | 获取兑换规则(人民币→积分比例) | 🟡 中 | 0.25天 |
| POST /api/recharge/orders | 创建充值订单 | 🔴 高 | 1.5天 |
| GET /api/recharge/orders/{orderNo} | 查询订单状态 | 🔴 高 | 0.5天 |
| POST /api/recharge/notify/wechat | 微信支付回调(后端处理,前端无需对接) | 🟢 低 | 0天 |

**缺失前端页面/组件**:
- ❌ **RechargePage.vue** (充值主页面)
- ❌ **ProductCard.vue** (充值套餐卡片)
- ❌ **PaymentModal.vue** (支付弹窗,显示二维码)
- ❌ **OrderStatusModal.vue** (订单状态轮询弹窗)

**新增Store需求**:
- ❌ `src/stores/recharge.ts` (充值状态管理)

**新增API文件**:
- ❌ `src/api/recharge.ts`

**技术难点**:
- 微信支付二维码展示
- 订单状态轮询机制 (类似job polling)
- 支付成功后刷新积分余额

**预计总工作量**: 2.5-3天

---

### 5. ⚠️ 导出模块 (0/2 API) - **用户体验关键**

**功能说明**: 项目资产导出为ZIP下载

| API端点 | 功能说明 | 优先级 | 预计工作量 |
|---------|---------|--------|-----------|
| POST /api/projects/{pid}/export | 提交导出任务(选择导出内容) | 🔴 高 | 1天 |
| GET /api/exports/{jobId}/download | 下载导出的ZIP文件 | 🔴 高 | 0.5天 |

**缺失前端组件**:
- ❌ **ExportModal.vue** (导出选项选择弹窗)
- ❌ **ExportProgressModal.vue** (导出进度展示)

**需要修改的现有组件**:
- ⚠️ **EditorPage.vue** (添加"导出项目"按钮)

**新增API文件**:
- ❌ `src/api/export.ts`

**技术难点**:
- 导出选项复杂(角色/场景/分镜图/视频,当前版本/全部版本)
- 导出任务轮询(job机制复用)
- 文件下载触发

**预计总工作量**: 1.5天

---

### 6. ⚠️ 风格预设 & 指令库 (1/9 API, 11%完成)

**功能说明**: 根据后端文档提到的 `StylePreset` 和 `PromptTemplate` 实体

**✅ 已实现的1个API**:
- GET /api/style-presets (获取风格预设列表) - `src/api/style.ts`

**❌ 缺失的8个API**:
- POST /api/style-presets (创建自定义风格)
- PUT /api/style-presets/{id} (更新风格)
- DELETE /api/style-presets/{id} (删除风格)
- GET /api/prompt-templates (指令库列表)
- POST /api/prompt-templates (保存常用指令)
- PUT /api/prompt-templates/{id} (更新指令)
- DELETE /api/prompt-templates/{id} (删除指令)
- POST /api/prompt-templates/{id}/publish (发布/禁用指令)

**缺失前端页面/组件**:
- ❌ **StylePresetPage.vue** (风格预设管理页面)
- ❌ **PromptLibraryPage.vue** (指令库页面)

**✅ 已集成组件**:
- ✅ **CreateProjectModal.vue** (使用 `styleApi.getStylePresets()` 加载风格)

**需要修改的现有组件**:
- ⚠️ **ToolboxPage.vue** (指令库快速插入功能)

**优先级**: 🟡 中 (依赖后端API规范)

**预计总工作量**: 2-3天

---

## 📈 功能实现优先级矩阵

| 功能模块 | 用户价值 | 技术复杂度 | 开发工作量 | 优先级评分 | 推荐实施顺序 |
|---------|---------|-----------|-----------|-----------|------------|
| 角色库系统 | ⭐⭐⭐⭐⭐ | 🔧🔧🔧🔧 | 4-5天 | **🔴 P0** | **Phase 11** |
| 场景库系统 | ⭐⭐⭐⭐⭐ | 🔧🔧🔧🔧 | 4-5天 | **🔴 P0** | **Phase 12** |
| 钱包/积分 | ⭐⭐⭐⭐⭐ | 🔧 | 0.5天 | **🔴 P0** | **Phase 10** |
| 充值/支付 | ⭐⭐⭐⭐⭐ | 🔧🔧🔧 | 2.5-3天 | **🟡 P1** | **Phase 13** |
| 导出功能 | ⭐⭐⭐⭐ | 🔧🔧 | 1.5天 | **🟡 P1** | **Phase 14** |
| 风格/指令库 | ⭐⭐⭐ | 🔧🔧 | 2-3天 | **🟢 P2** | **Phase 15** |

**优先级说明**:
- **P0 (关键功能)**: 缺失会严重影响产品完整性,必须实现
- **P1 (重要功能)**: 影响用户体验和商业化,尽快实现
- **P2 (增强功能)**: 优化用户体验,可延后实现

---

## 🗺️ 更新后的完整开发计划

### 原计划 (Days 1-17)
- **Phase 1-9**: 已完成 (基础架构、首页、工具箱、邀请、编辑器核心、生成功能、资产管理、测试优化)
- **Phase 10**: 构建 & 部署

### 新增计划 (Days 18-32)

#### **Phase 10: 钱包/积分模块 (Days 18-19.5) - 1.5天**
- [ ] 创建 `src/api/wallet.ts`
- [ ] 创建 `WalletPage.vue` 主页面
- [ ] 实现 `PointsBalanceCard.vue` 组件
- [ ] 实现 `TransactionTable.vue` 流水记录表格
- [ ] 修改 `Header.vue` 显示积分余额
- [ ] 修改 `userStore.ts` 增加钱包相关方法
- [ ] 添加路由 `/wallet`
- [ ] 测试积分显示和流水记录

#### **Phase 11: 角色库系统 (Days 20-25) - 5-6天**
- [ ] **Day 20: 基础架构**
  - [ ] 创建 `src/api/characterLibrary.ts`
  - [ ] 创建 `src/stores/characterLibrary.ts`
  - [ ] 定义所有TypeScript类型 (CategoryVO, CharacterLibraryVO, etc.)
- [ ] **Day 21-22: 分类管理**
  - [ ] 实现 `CategoryManager.vue` 组件
  - [ ] CRUD操作 (创建/重命名/删除分类)
  - [ ] 分类树形展示
- [ ] **Day 23-24: 角色库主页面**
  - [ ] 创建 `CharacterLibraryPage.vue`
  - [ ] 实现 `GlobalCharacterCard.vue` 卡片组件
  - [ ] 分页、搜索、筛选功能
  - [ ] 创建/编辑/删除全局角色
- [ ] **Day 25: 项目引用功能**
  - [ ] 修改 `CharacterPanel.vue` 增加"从角色库引用"按钮
  - [ ] 实现 `CharacterLibraryModal.vue` 选择弹窗
  - [ ] 实现角色替换功能
  - [ ] 测试完整引用流程

#### **Phase 12: 场景库系统 (Days 26-31) - 5-6天**
- [ ] **Day 26: 基础架构** (复用角色库架构)
  - [ ] 创建 `src/api/sceneLibrary.ts`
  - [ ] 创建 `src/stores/sceneLibrary.ts`
  - [ ] 定义TypeScript类型
- [ ] **Day 27-28: 分类管理**
  - [ ] 实现 `SceneCategoryManager.vue`
  - [ ] CRUD操作
- [ ] **Day 29-30: 场景库主页面**
  - [ ] 创建 `SceneLibraryPage.vue`
  - [ ] 实现 `GlobalSceneCard.vue`
  - [ ] 分页、搜索、筛选
- [ ] **Day 31: 项目引用功能**
  - [ ] 修改 `ScenePanel.vue`
  - [ ] 实现 `SceneLibraryModal.vue`
  - [ ] 测试完整流程

#### **Phase 13: 充值/支付模块 (Days 32-34.5) - 2.5-3天**
- [ ] **Day 32: 套餐展示**
  - [ ] 创建 `src/api/recharge.ts`
  - [ ] 创建 `RechargePage.vue`
  - [ ] 实现 `ProductCard.vue` 套餐卡片
  - [ ] 获取充值套餐列表
- [ ] **Day 33-34: 支付流程**
  - [ ] 实现 `PaymentModal.vue` 支付弹窗
  - [ ] 微信支付二维码展示
  - [ ] 实现 `OrderStatusModal.vue` 订单轮询
  - [ ] 订单状态实时更新
  - [ ] 支付成功后刷新积分
- [ ] **Day 34.5: 测试**
  - [ ] 完整支付流程测试
  - [ ] 异常处理(超时、取消、失败)

#### **Phase 14: 导出功能 (Days 35-36.5) - 1.5天**
- [ ] **Day 35: 导出选项**
  - [ ] 创建 `src/api/export.ts`
  - [ ] 实现 `ExportModal.vue` 选项弹窗
  - [ ] 选择导出内容(角色/场景/分镜/视频)
  - [ ] 选择导出模式(当前版本/全部版本)
- [ ] **Day 36-36.5: 导出执行**
  - [ ] 实现 `ExportProgressModal.vue` 进度展示
  - [ ] 复用job轮询机制
  - [ ] ZIP文件下载触发
  - [ ] 测试完整导出流程

#### **Phase 15: 风格预设 & 指令库 (Days 37-39) - 2-3天** (待后端API确认)
- [ ] **Day 37: 风格预设**
  - [ ] 创建 `StylePresetPage.vue`
  - [ ] 风格列表展示
  - [ ] 自定义风格保存
  - [ ] 修改 `CreateProjectModal.vue` 集成风格选择器
- [ ] **Day 38-39: 指令库**
  - [ ] 创建 `PromptLibraryPage.vue`
  - [ ] 指令列表展示
  - [ ] 保存常用指令
  - [ ] 修改 `ToolboxPage.vue` 集成指令快速插入

#### **Phase 16: 测试 & 优化 (Days 40-41)**
- [ ] 全模块集成测试
- [ ] 性能优化
- [ ] 错误边界处理
- [ ] 用户体验优化

#### **Phase 17: 构建 & 部署 (Day 42)**
- [ ] 生产环境构建
- [ ] 部署上线
- [ ] 文档完善

---

## 🎯 关键架构改动

### 1. 导航侧边栏更新

**当前侧边栏** (NavSidebar.vue):
```
- 首页
- AI工具箱
- 邀请好友
- 软件设置
```

**新增侧边栏**:
```
- 首页
- AI工具箱
- 邀请好友
+ 角色库          # 新增
+ 场景库          # 新增
+ 我的钱包        # 新增
- 软件设置
```

### 2. 路由新增

```typescript
// src/router/index.ts
{
  path: '/character-library',
  name: 'CharacterLibrary',
  component: () => import('@/views/character-library/CharacterLibraryPage.vue'),
  meta: { layout: 'main', requiresAuth: true },
},
{
  path: '/scene-library',
  name: 'SceneLibrary',
  component: () => import('@/views/scene-library/SceneLibraryPage.vue'),
  meta: { layout: 'main', requiresAuth: true },
},
{
  path: '/wallet',
  name: 'Wallet',
  component: () => import('@/views/wallet/WalletPage.vue'),
  meta: { layout: 'main', requiresAuth: true },
},
{
  path: '/recharge',
  name: 'Recharge',
  component: () => import('@/views/recharge/RechargePage.vue'),
  meta: { layout: 'main', requiresAuth: true },
},
```

### 3. 新增目录结构

```
src/
├── views/
│   ├── character-library/          # 新增
│   │   ├── CharacterLibraryPage.vue
│   │   └── components/
│   │       ├── CategoryManager.vue
│   │       ├── GlobalCharacterCard.vue
│   │       └── CharacterLibraryModal.vue
│   ├── scene-library/              # 新增
│   │   ├── SceneLibraryPage.vue
│   │   └── components/
│   │       ├── SceneCategoryManager.vue
│   │       ├── GlobalSceneCard.vue
│   │       └── SceneLibraryModal.vue
│   ├── wallet/                     # 新增
│   │   ├── WalletPage.vue
│   │   └── components/
│   │       ├── PointsBalanceCard.vue
│   │       └── TransactionTable.vue
│   ├── recharge/                   # 新增
│   │   ├── RechargePage.vue
│   │   └── components/
│   │       ├── ProductCard.vue
│   │       ├── PaymentModal.vue
│   │       └── OrderStatusModal.vue
│   └── style-presets/              # 新增
│       └── StylePresetPage.vue
├── stores/
│   ├── characterLibrary.ts         # 新增
│   ├── sceneLibrary.ts             # 新增
│   └── recharge.ts                 # 新增
├── api/
│   ├── characterLibrary.ts         # 新增
│   ├── sceneLibrary.ts             # 新增
│   ├── wallet.ts                   # 新增
│   ├── recharge.ts                 # 新增
│   └── export.ts                   # 新增
```

---

## 📊 工作量总结

| 模块 | 原计划天数 | 新增天数 | 总计天数 |
|------|-----------|---------|---------|
| 原Phase 1-9 | 16天 | - | 16天 |
| Phase 9.5: 工具箱+模型配置 | - | 0.5天 | 0.5天 |
| Phase 10: 钱包/积分UI | - | 0.5天 | 0.5天 |
| Phase 11: 角色库 | - | 4-5天 | 4-5天 |
| Phase 12: 场景库 | - | 4-5天 | 4-5天 |
| Phase 13: 充值/支付 | - | 2.5-3天 | 2.5-3天 |
| Phase 14: 导出 | - | 1.5天 | 1.5天 |
| Phase 15: 风格/指令库 | - | 2-3天 | 2-3天 |
| Phase 16-17: 测试部署 | 1天 | 2天 | 3天 |
| **总计** | **17天** | **18-23天** | **35-40天** |

**工作量增加**: +117% ~ +147%

---

## 🚨 关键风险与依赖

### 1. 后端API完成度
- **风险**: 角色库/场景库后端API可能未完成
- **缓解**: 优先与后端沟通,确认API开发进度
- **替代方案**: 前端先用Mock数据开发UI,后续对接真实API

### 2. 支付功能复杂度
- **风险**: 微信支付集成可能遇到跨域、回调处理等问题
- **缓解**: 提前测试支付沙箱环境
- **替代方案**: 先实现支付宝支付,微信支付延后

### 3. 风格预设/指令库API规范
- **风险**: 后端API文档未明确这两个模块的接口
- **缓解**: 立即与后端确认API设计
- **替代方案**: 降低优先级,Phase 15可选实施

---

## ✅ 下一步行动

1. **立即确认** (今天):
   - [ ] 与后端确认角色库/场景库API是否已实现
   - [ ] 与后端确认支付模块集成方案(微信/支付宝)
   - [ ] 与后端确认风格预设/指令库API规范

2. **优先实施** (本周):
   - [ ] Phase 10: 钱包/积分模块 (1.5天)
   - [ ] Phase 11: 角色库系统 (5-6天)

3. **并行开发** (可选):
   - 如果团队有多人,可以并行开发:
     - 人员A: 角色库系统
     - 人员B: 场景库系统
     - 人员C: 钱包/充值系统

---

**报告生成人**: Claude Code AI Assistant
**报告审核**: 待用户确认
**下次更新**: 完成Phase 10后
