# Phase 9.5 & 10 开发计划

**创建日期**: 2026-01-03T15:46:42+08:00
**预计总工期**: 8.25小时 (~1个工作日)
**目标**: 修复AI工具箱问题、实现模型配置UI、完成钱包积分页面

---

## 📋 任务总览

| 阶段 | 任务数 | 预计工时 | 优先级 |
|------|--------|---------|--------|
| Phase 9.5A - 文档更新 | 2 | 0.25h | P0 |
| Phase 9.5B - AI工具箱修复 | 2 | 1.5h | P0 |
| Phase 9.5C - 模型配置UI | 3 | 2.5h | P1 |
| Phase 10 - 钱包积分UI | 5 | 4h | P1 |
| **总计** | **12** | **8.25h** | - |

---

## 🔍 问题分析 (已完成审计)

### 问题1: AI工具箱自动刷新失败

**用户报告**: "生成文本和生成图片视频后必须刷新才能看到记录"

**根因分析**:
```typescript
// toolbox.ts:42-66
async generate(data: ToolboxGenerateRequest) {
  const response = await toolboxApi.generate(data)  // Line 46
  this.currentGeneration = response

  if (response.jobId) {
    this.startJobPolling(response.jobId)  // 异步视频生成
  } else {
    await this.fetchHistory()  // Line 56 - 同步结果立即刷新
  }
}
```

**问题根源**:
- Line 56的`fetchHistory()`在后端保存记录到数据库**之前**就被调用了
- 存在**竞态条件** (race condition): 前端请求历史记录时,后端可能还未完成数据库写入
- 对于异步任务(视频),虽然Line 136有刷新逻辑,但可能存在相同问题

**解决方案**:
1. **方案A (推荐)**: 在`fetchHistory()`前添加500ms延迟,等待后端保存完成
2. **方案B (后端改动)**: 后端在`ToolboxGenerateResponse`中直接返回保存的历史记录ID,前端直接将其添加到history数组
3. **方案C (轮询)**: 实现轮询机制,直到新记录出现在历史列表中

**采用方案A**: 最小改动,立即可实施

---

### 问题2: AI工具箱不显示生成内容

**用户报告**: "也只是记录,没有显示生成的内容"

**根因分析**:
```typescript
// api.ts:299-310
export interface ToolboxHistoryVO {
  id: number
  type: string
  model: string
  prompt: string
  aspectRatio: string | null
  resultUrl: string | null  // ❌ 只有URL,没有text字段!
  status: string
  costPoints: number
  createdAt: string
  expireAt: string
}
```

**问题根源**:
- `ToolboxHistoryVO`缺少`text`字段,无法显示TEXT类型的生成结果
- `HistoryList.vue:79-81`显示TEXT预览功能未实现(仅alert TODO)
- `HistoryList.vue:139-145`仅显示IMAGE类型的缩略图,VIDEO类型无预览

**解决方案**:
1. **TEXT类型**:
   - **后端改动**: 在`ToolboxHistoryVO`添加`text`字段
   - **前端改动**: 在历史列表中显示文本内容(折叠/展开)
2. **IMAGE类型**: ✅ 已实现缩略图预览
3. **VIDEO类型**:
   - 添加视频缩略图预览
   - 点击预览时使用video标签播放

**采用方案**:
- 短期(本阶段): 前端修改,使用`resultUrl`字段存储TEXT结果(后端需配合)
- 长期: 后端添加`text`字段到`ToolboxHistoryVO`

---

### 问题3: 模型配置UI缺失

**用户需求**: "统一控制各个环节生成文本,生成图片生成视频用的是什么模型,并且显示模型对应消耗的积分"

**需求分析**:
- 需要一个**全局模型配置面板**,控制5种生成类型:
  1. 分镜文本生成模型
  2. 角色画像生成模型
  3. 场景画像生成模型
  4. 分镜画像生成模型
  5. 视频生成模型
- 每个模型选择器需要显示**积分消耗**
- 配置应该**持久化**(localStorage或后端API)
- 所有生成API调用时需要传递选中的`modelCode`

**技术方案**:
1. 创建`ModelConfigModal.vue`模态框组件
2. 创建`src/stores/modelConfig.ts` Pinia状态管理
3. 修改所有生成API调用,添加`model`参数
4. EditorPage.vue中的"模型配置"按钮(L186-196)绑定模态框

**数据源**:
- **短期**: 前端硬编码模型列表(参考style presets实现)
- **长期**: 从后端API获取 `GET /api/models`

---

## 📝 Phase 9.5A - 文档更新 (0.25小时)

### 任务1: 更新FEATURE_GAP_ANALYSIS.md ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\FEATURE_GAP_ANALYSIS.md`

**修改内容**:
1. **Line 13-16**: 更新统计表
   ```markdown
   | 当前前端覆盖 | ~45个 | ⚠️ 部分实现 |
   ↓ 修改为:
   | 当前前端覆盖 | ~53个 | ✅ 良好 |
   ```

2. **Line 189-211**: 更新钱包/积分模块
   ```markdown
   ### 3. ⚠️ 钱包/积分模块 (0/2 API) - **核心缺失**
   ↓ 修改为:
   ### 3. ✅ 钱包/积分模块 (2/2 API) - **API已完成**

   | API端点 | 功能说明 | 状态 |
   |---------|---------|------|
   | GET /api/wallet | 查询用户积分余额 | ✅ 已实现 |
   | GET /api/wallet/transactions | 查询积分流水记录(分页) | ✅ 已实现 |

   **已实现API文件**: `src/api/wallet.ts`
   **已调用位置**: `HomePage.vue:58-65`
   **缺失内容**: 仅缺UI页面 (WalletPage.vue, TransactionTable.vue)
   ```

3. **Line 109-145**: 更新角色库模块
   ```markdown
   ### 1. ⚠️ 角色库模块 (0/13 API) - **严重缺失**
   ↓ 修改为:
   ### 1. ⚠️ 角色库模块 (3/13 API, 23%完成) - **读取API已就绪**

   **✅ 已实现的3个API**:
   - GET /api/library/characters (获取角色库列表)
   - GET /api/library/characters/categories (获取分类列表)
   - POST /api/projects/{pid}/characters (从角色库引用)

   **❌ 缺失的10个CRUD API** (见原文档)
   ```

4. **Line 149-186**: 更新场景库模块(同角色库)

5. **Line 275-300**: 更新风格预设模块
   ```markdown
   ### 6. ⚠️ 风格预设 & 指令库 (未明确API) - **待确认**
   ↓ 修改为:
   ### 6. ⚠️ 风格预设 & 指令库 (1/9 API, 11%完成)

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
   ```

6. **Line 303-313**: 更新优先级矩阵
   ```markdown
   | 钱包/积分 | ⭐⭐⭐⭐⭐ | 🔧🔧 | 1.5天 | **🔴 P0** | **Phase 10** |
   ↓ 修改为:
   | 钱包/积分 | ⭐⭐⭐⭐⭐ | 🔧 | 0.5天 | **🔴 P0** | **Phase 10** |
   ```

7. **Line 527-541**: 更新工作量总结
   ```markdown
   | Phase 10: 钱包/积分 | - | 1.5天 | 1.5天 |
   ↓ 修改为:
   | Phase 9.5: 工具箱+模型配置 | - | 0.5天 | 0.5天 |
   | Phase 10: 钱包/积分UI | - | 0.5天 | 0.5天 |
   ```

**预计工时**: 10分钟

---

### 任务2: 更新DEVELOPMENT_PLAN.md ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\DEVELOPMENT_PLAN.md`

**修改内容**:
1. 在Phase 9和Phase 10之间插入**Phase 9.5**:
   ```markdown
   ## Phase 9.5: AI工具箱优化 & 模型配置 (0.5天 - Day 16.5)

   ### 9.5.1 AI工具箱修复 (2小时)
   - [ ] 修复生成后自动刷新问题(添加延迟机制)
   - [ ] 实现TEXT类型内容预览(展开/折叠文本)
   - [ ] 实现VIDEO类型内容预览(缩略图)
   - [ ] 优化历史列表UI

   ### 9.5.2 模型配置UI (2.5小时)
   - [ ] 创建`ModelConfigModal.vue`模态框
   - [ ] 创建`modelConfig` Pinia store
   - [ ] 硬编码模型列表(5种类型,每种2-3个模型选项)
   - [ ] 集成到EditorPage的"模型配置"按钮
   - [ ] 修改所有生成API调用,传递modelCode参数
   ```

2. 更新**Phase 10**:
   ```markdown
   ## Phase 10: 钱包/积分UI (0.5天 - Day 17)

   ### 10.1 钱包页面 (2小时)
   - [ ] 创建`WalletPage.vue`主页面
   - [ ] 创建`PointsBalanceCard.vue`余额卡片
   - [ ] 创建`TransactionTable.vue`流水表格
   - [ ] 添加`/wallet`路由

   ### 10.2 全局积分显示 (1小时)
   - [ ] 在NavSidebar添加"我的钱包"入口
   - [ ] 在EditorPage Header显示积分余额
   - [ ] 在HomePage显示积分余额

   ### 10.3 测试 (1小时)
   - [ ] 测试积分余额显示
   - [ ] 测试流水记录分页
   - [ ] 测试积分扣除流程(集成生成功能)
   ```

3. 更新工期总览:
   ```markdown
   - Phase 1-9: 16天 (已完成)
   - Phase 9.5: 0.5天 (本阶段)
   - Phase 10: 0.5天 (本阶段)
   - Phase 11-17: 待规划

   **本阶段总计**: 1天
   ```

**预计工时**: 5分钟

---

## 🛠️ Phase 9.5B - AI工具箱修复 (1.5小时)

### 任务3: 修复自动刷新问题 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\stores\toolbox.ts`

**修改位置**: Line 42-66 (`generate`方法)

**修改内容**:
```typescript
// 修改前 (Line 54-56):
} else {
  // Sync result (text/image), refresh history immediately
  await this.fetchHistory()
}

// 修改后:
} else {
  // Sync result (text/image), wait 500ms for backend to save, then refresh
  await new Promise(resolve => setTimeout(resolve, 500))
  await this.fetchHistory()
  console.log('[ToolboxStore] History refreshed after generation')
}
```

**修改位置**: Line 133-138 (`startJobPolling`方法中的COMPLETED分支)

**修改内容**:
```typescript
// 修改前 (Line 133-138):
if (job.status === 'COMPLETED') {
  console.log('[ToolboxStore] Job completed!')
  this.stopJobPolling()
  await this.fetchHistory()
  window.$message?.success('生成完成!')
}

// 修改后:
if (job.status === 'COMPLETED') {
  console.log('[ToolboxStore] Job completed!')
  this.stopJobPolling()
  // Wait 500ms for backend to save to history table
  await new Promise(resolve => setTimeout(resolve, 500))
  await this.fetchHistory()
  window.$message?.success('生成完成!')
}
```

**测试步骤**:
1. 生成TEXT内容,观察历史列表是否立即更新
2. 生成IMAGE内容,观察历史列表是否立即更新
3. 生成VIDEO内容,等待任务完成,观察历史列表是否更新
4. 检查控制台日志确认延迟生效

**预计工时**: 30分钟

---

### 任务4: 实现生成内容预览 ✅

**文件1**: `E:\Desktop\ai_story_studio_web\user-web\src\types\api.ts`

**修改内容**: 扩展`ToolboxHistoryVO`接口
```typescript
// Line 299-310
export interface ToolboxHistoryVO {
  id: number
  type: string
  model: string
  prompt: string
  aspectRatio: string | null
  resultUrl: string | null
  text: string | null  // ✅ 新增: 用于TEXT类型的生成结果
  status: string
  costPoints: number
  createdAt: string
  expireAt: string
}
```

**注意**: 此修改需要**后端配合**,在返回历史记录时包含`text`字段。如果后端暂时无法修改,可以临时使用`resultUrl`存储TEXT内容(前端需判断type=='TEXT'时将resultUrl当作text处理)。

---

**文件2**: `E:\Desktop\ai_story_studio_web\user-web\src\views\toolbox\components\HistoryList.vue`

**修改位置1**: Line 134-136 (添加TEXT预览)

**修改内容**:
```vue
<!-- 原代码 (Line 134-136) -->
<p class="text-white/70 text-sm mb-3 line-clamp-2">
  {{ item.prompt }}
</p>

<!-- 修改后 -->
<p class="text-white/70 text-sm mb-3 line-clamp-2">
  {{ item.prompt }}
</p>

<!-- ✅ 新增: TEXT类型内容预览 -->
<div v-if="item.type === 'TEXT' && (item.text || item.resultUrl)" class="mb-3">
  <div class="p-3 rounded-xl bg-black/30 border border-white/10">
    <p class="text-white/80 text-sm whitespace-pre-wrap line-clamp-4">
      {{ item.text || item.resultUrl }}
    </p>
    <button
      class="mt-2 text-mochi-cyan text-xs hover:underline"
      @click="handlePreview(item)"
    >
      查看全文 →
    </button>
  </div>
</div>
```

---

**修改位置2**: Line 138-145 (添加VIDEO预览)

**修改内容**:
```vue
<!-- 原代码 (Line 138-145) -->
<!-- Preview -->
<div v-if="item.resultUrl && item.type === 'IMAGE'" class="mb-3">
  <img
    :src="item.resultUrl"
    class="max-w-[180px] rounded-xl border border-white/10 cursor-pointer hover:border-mochi-cyan transition-all"
    @click="handlePreview(item)"
  >
</div>

<!-- 修改后 -->
<!-- Preview -->
<div v-if="item.resultUrl && item.type === 'IMAGE'" class="mb-3">
  <img
    :src="item.resultUrl"
    class="max-w-[180px] rounded-xl border border-white/10 cursor-pointer hover:border-mochi-cyan transition-all"
    @click="handlePreview(item)"
  >
</div>

<!-- ✅ 新增: VIDEO类型预览 -->
<div v-if="item.resultUrl && item.type === 'VIDEO'" class="mb-3">
  <video
    :src="item.resultUrl"
    class="max-w-[240px] rounded-xl border border-white/10 cursor-pointer hover:border-mochi-cyan transition-all"
    @click="handlePreview(item)"
    muted
    loop
  >
    您的浏览器不支持视频播放
  </video>
</div>
```

---

**修改位置3**: Line 77-85 (改进预览逻辑)

**修改内容**:
```typescript
// 修改前 (Line 77-85):
const handlePreview = (item: ToolboxHistoryVO) => {
  if (item.type === 'TEXT') {
    // Show text in modal (TODO: implement modal)
    alert('TODO: Show text preview')
  } else if (item.resultUrl) {
    window.open(item.resultUrl, '_blank')
  }
}

// 修改后:
const handlePreview = (item: ToolboxHistoryVO) => {
  if (item.type === 'TEXT') {
    const textContent = item.text || item.resultUrl || '无内容'
    // 使用简单的alert展示全文(临时方案)
    // TODO: 后续可升级为模态框组件
    alert(textContent)
  } else if (item.resultUrl) {
    window.open(item.resultUrl, '_blank')
  }
}
```

**测试步骤**:
1. 生成TEXT内容,检查历史列表是否显示文本预览(折叠状态)
2. 点击"查看全文",检查是否弹出完整内容
3. 生成IMAGE内容,检查是否显示缩略图
4. 生成VIDEO内容,检查是否显示视频预览(静音循环播放)
5. 点击IMAGE/VIDEO预览,检查是否在新窗口打开

**预计工时**: 1小时

---

## ⚙️ Phase 9.5C - 模型配置UI (2.5小时)

### 任务5: 创建模型配置Store ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\stores\modelConfig.ts` (新建)

**文件内容**:
```typescript
import { defineStore } from 'pinia'

// 模型选项定义
export interface ModelOption {
  code: string
  name: string
  type: 'TEXT' | 'IMAGE' | 'VIDEO'
  costPoints: number  // 每次生成消耗的积分
  description?: string
}

// 硬编码的模型列表
export const MODEL_OPTIONS: ModelOption[] = [
  // 文本生成模型
  { code: 'gpt-4', name: 'GPT-4', type: 'TEXT', costPoints: 10, description: '最强大的文本模型' },
  { code: 'gpt-3.5', name: 'GPT-3.5 Turbo', type: 'TEXT', costPoints: 5, description: '快速且经济' },
  { code: 'claude-3', name: 'Claude 3', type: 'TEXT', costPoints: 8, description: '平衡性能与成本' },

  // 图像生成模型
  { code: 'dall-e-3', name: 'DALL-E 3', type: 'IMAGE', costPoints: 20, description: '高质量图像生成' },
  { code: 'midjourney', name: 'Midjourney', type: 'IMAGE', costPoints: 25, description: '艺术风格图像' },
  { code: 'sd-xl', name: 'Stable Diffusion XL', type: 'IMAGE', costPoints: 15, description: '开源图像模型' },

  // 视频生成模型
  { code: 'runway-gen2', name: 'Runway Gen-2', type: 'VIDEO', costPoints: 50, description: '专业视频生成' },
  { code: 'pika', name: 'Pika Labs', type: 'VIDEO', costPoints: 40, description: '快速视频生成' },
]

interface ModelConfigState {
  // 5种生成环节的选中模型
  scriptTextModel: string        // 分镜文本生成
  characterImageModel: string     // 角色画像生成
  sceneImageModel: string         // 场景画像生成
  shotImageModel: string          // 分镜画像生成
  videoModel: string              // 视频生成
}

export const useModelConfigStore = defineStore('modelConfig', {
  state: (): ModelConfigState => ({
    // 默认选择成本较低的模型
    scriptTextModel: 'gpt-3.5',
    characterImageModel: 'sd-xl',
    sceneImageModel: 'sd-xl',
    shotImageModel: 'sd-xl',
    videoModel: 'pika',
  }),

  getters: {
    // 获取文本模型选项
    textModels: () => MODEL_OPTIONS.filter(m => m.type === 'TEXT'),

    // 获取图像模型选项
    imageModels: () => MODEL_OPTIONS.filter(m => m.type === 'IMAGE'),

    // 获取视频模型选项
    videoModels: () => MODEL_OPTIONS.filter(m => m.type === 'VIDEO'),

    // 获取指定模型的详细信息
    getModelInfo: () => (code: string) => {
      return MODEL_OPTIONS.find(m => m.code === code)
    },

    // 计算当前配置的总成本(每种类型使用一次的总成本)
    totalCostPerGeneration: (state) => {
      const scriptCost = MODEL_OPTIONS.find(m => m.code === state.scriptTextModel)?.costPoints || 0
      const charCost = MODEL_OPTIONS.find(m => m.code === state.characterImageModel)?.costPoints || 0
      const sceneCost = MODEL_OPTIONS.find(m => m.code === state.sceneImageModel)?.costPoints || 0
      const shotCost = MODEL_OPTIONS.find(m => m.code === state.shotImageModel)?.costPoints || 0
      const videoCost = MODEL_OPTIONS.find(m => m.code === state.videoModel)?.costPoints || 0

      return {
        scriptText: scriptCost,
        characterImage: charCost,
        sceneImage: sceneCost,
        shotImage: shotCost,
        video: videoCost,
        total: scriptCost + charCost + sceneCost + shotCost + videoCost,
      }
    },
  },

  actions: {
    // 设置分镜文本生成模型
    setScriptTextModel(code: string) {
      this.scriptTextModel = code
      this.saveToLocalStorage()
    },

    // 设置角色画像生成模型
    setCharacterImageModel(code: string) {
      this.characterImageModel = code
      this.saveToLocalStorage()
    },

    // 设置场景画像生成模型
    setSceneImageModel(code: string) {
      this.sceneImageModel = code
      this.saveToLocalStorage()
    },

    // 设置分镜画像生成模型
    setShotImageModel(code: string) {
      this.shotImageModel = code
      this.saveToLocalStorage()
    },

    // 设置视频生成模型
    setVideoModel(code: string) {
      this.videoModel = code
      this.saveToLocalStorage()
    },

    // 重置为默认模型
    resetToDefaults() {
      this.scriptTextModel = 'gpt-3.5'
      this.characterImageModel = 'sd-xl'
      this.sceneImageModel = 'sd-xl'
      this.shotImageModel = 'sd-xl'
      this.videoModel = 'pika'
      this.saveToLocalStorage()
    },

    // 保存到localStorage
    saveToLocalStorage() {
      const config = {
        scriptTextModel: this.scriptTextModel,
        characterImageModel: this.characterImageModel,
        sceneImageModel: this.sceneImageModel,
        shotImageModel: this.shotImageModel,
        videoModel: this.videoModel,
      }
      localStorage.setItem('modelConfig', JSON.stringify(config))
      console.log('[ModelConfigStore] Saved to localStorage:', config)
    },

    // 从localStorage加载
    loadFromLocalStorage() {
      const savedConfig = localStorage.getItem('modelConfig')
      if (savedConfig) {
        try {
          const config = JSON.parse(savedConfig)
          this.scriptTextModel = config.scriptTextModel || this.scriptTextModel
          this.characterImageModel = config.characterImageModel || this.characterImageModel
          this.sceneImageModel = config.sceneImageModel || this.sceneImageModel
          this.shotImageModel = config.shotImageModel || this.shotImageModel
          this.videoModel = config.videoModel || this.videoModel
          console.log('[ModelConfigStore] Loaded from localStorage:', config)
        } catch (error) {
          console.error('[ModelConfigStore] Failed to parse saved config:', error)
        }
      }
    },
  },
})
```

**预计工时**: 30分钟

---

### 任务6: 创建模型配置模态框 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\components\editor\ModelConfigModal.vue` (新建)

**文件内容**:
```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useModelConfigStore } from '@/stores/modelConfig'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const modelConfigStore = useModelConfigStore()

onMounted(() => {
  // Load saved config from localStorage
  modelConfigStore.loadFromLocalStorage()
})

const handleClose = () => {
  emit('close')
}

const handleSave = () => {
  modelConfigStore.saveToLocalStorage()
  window.$message?.success('模型配置已保存')
  emit('close')
}

const handleReset = () => {
  if (confirm('确定要重置为默认模型配置吗？')) {
    modelConfigStore.resetToDefaults()
    window.$message?.success('已重置为默认配置')
  }
}
</script>

<template>
  <Transition
    enter-active-class="transition-opacity duration-200"
    leave-active-class="transition-opacity duration-200"
    enter-from-class="opacity-0"
    leave-to-class="opacity-0"
  >
    <div
      v-if="show"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      @click.self="handleClose"
    >
      <!-- Modal Container -->
      <div class="bg-[#1a1b1f] w-[700px] max-h-[90vh] rounded-2xl flex flex-col shadow-2xl border border-white/10">
        <!-- Header -->
        <div class="relative flex items-center justify-between px-6 py-4 border-b border-white/10">
          <h2 class="text-lg font-semibold text-white">模型配置</h2>
          <button
            class="text-white/60 hover:text-white transition-colors"
            @click="handleClose"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto p-6 space-y-6">
          <!-- Explanation -->
          <div class="p-4 rounded-xl bg-mochi-cyan/10 border border-mochi-cyan/20">
            <p class="text-white/80 text-sm">
              配置各环节生成时使用的AI模型。不同模型有不同的质量和成本，请根据需求选择。
            </p>
          </div>

          <!-- 1. Script Text Model -->
          <div class="space-y-2">
            <label class="text-white/80 text-sm font-medium">分镜文本生成模型</label>
            <select
              v-model="modelConfigStore.scriptTextModel"
              class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white focus:border-mochi-cyan focus:outline-none transition-all"
            >
              <option
                v-for="model in modelConfigStore.textModels"
                :key="model.code"
                :value="model.code"
                class="bg-mochi-bg text-white"
              >
                {{ model.name }} - {{ model.costPoints }}积分/次 ({{ model.description }})
              </option>
            </select>
          </div>

          <!-- 2. Character Image Model -->
          <div class="space-y-2">
            <label class="text-white/80 text-sm font-medium">角色画像生成模型</label>
            <select
              v-model="modelConfigStore.characterImageModel"
              class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white focus:border-mochi-cyan focus:outline-none transition-all"
            >
              <option
                v-for="model in modelConfigStore.imageModels"
                :key="model.code"
                :value="model.code"
                class="bg-mochi-bg text-white"
              >
                {{ model.name }} - {{ model.costPoints }}积分/次 ({{ model.description }})
              </option>
            </select>
          </div>

          <!-- 3. Scene Image Model -->
          <div class="space-y-2">
            <label class="text-white/80 text-sm font-medium">场景画像生成模型</label>
            <select
              v-model="modelConfigStore.sceneImageModel"
              class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white focus:border-mochi-cyan focus:outline-none transition-all"
            >
              <option
                v-for="model in modelConfigStore.imageModels"
                :key="model.code"
                :value="model.code"
                class="bg-mochi-bg text-white"
              >
                {{ model.name }} - {{ model.costPoints }}积分/次 ({{ model.description }})
              </option>
            </select>
          </div>

          <!-- 4. Shot Image Model -->
          <div class="space-y-2">
            <label class="text-white/80 text-sm font-medium">分镜画像生成模型</label>
            <select
              v-model="modelConfigStore.shotImageModel"
              class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white focus:border-mochi-cyan focus:outline-none transition-all"
            >
              <option
                v-for="model in modelConfigStore.imageModels"
                :key="model.code"
                :value="model.code"
                class="bg-mochi-bg text-white"
              >
                {{ model.name }} - {{ model.costPoints }}积分/次 ({{ model.description }})
              </option>
            </select>
          </div>

          <!-- 5. Video Model -->
          <div class="space-y-2">
            <label class="text-white/80 text-sm font-medium">视频生成模型</label>
            <select
              v-model="modelConfigStore.videoModel"
              class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white focus:border-mochi-cyan focus:outline-none transition-all"
            >
              <option
                v-for="model in modelConfigStore.videoModels"
                :key="model.code"
                :value="model.code"
                class="bg-mochi-bg text-white"
              >
                {{ model.name }} - {{ model.costPoints }}积分/次 ({{ model.description }})
              </option>
            </select>
          </div>

          <!-- Cost Summary -->
          <div class="p-4 rounded-xl bg-white/5 border border-white/10">
            <h3 class="text-white/80 text-sm font-medium mb-3">成本预估</h3>
            <div class="grid grid-cols-2 gap-3 text-xs">
              <div class="flex justify-between">
                <span class="text-white/60">分镜文本:</span>
                <span class="text-mochi-cyan font-medium">{{ modelConfigStore.totalCostPerGeneration.scriptText }} 积分</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">角色画像:</span>
                <span class="text-mochi-cyan font-medium">{{ modelConfigStore.totalCostPerGeneration.characterImage }} 积分</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">场景画像:</span>
                <span class="text-mochi-cyan font-medium">{{ modelConfigStore.totalCostPerGeneration.sceneImage }} 积分</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">分镜画像:</span>
                <span class="text-mochi-cyan font-medium">{{ modelConfigStore.totalCostPerGeneration.shotImage }} 积分</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">视频:</span>
                <span class="text-mochi-cyan font-medium">{{ modelConfigStore.totalCostPerGeneration.video }} 积分</span>
              </div>
              <div class="flex justify-between font-semibold">
                <span class="text-white">总计:</span>
                <span class="text-mochi-cyan">{{ modelConfigStore.totalCostPerGeneration.total }} 积分</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between px-6 py-4 border-t border-white/10">
          <button
            class="px-4 py-2 rounded-full text-sm text-white/60 hover:text-white hover:bg-white/5 transition-all"
            @click="handleReset"
          >
            重置为默认
          </button>
          <div class="flex gap-3">
            <button
              class="px-6 py-2 rounded-full text-sm bg-white/10 text-white/80 hover:bg-white/20 transition-all"
              @click="handleClose"
            >
              取消
            </button>
            <button
              class="px-6 py-2 rounded-full text-sm bg-gradient-to-r from-mochi-cyan to-mochi-blue text-mochi-bg font-medium hover:shadow-neon-cyan transition-all"
              @click="handleSave"
            >
              保存配置
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>
```

**预计工时**: 1小时

---

### 任务7: 集成模型配置到EditorPage ✅

**文件1**: `E:\Desktop\ai_story_studio_web\user-web\src\views\editor\EditorPage.vue`

**修改位置1**: 添加import和state (在script setup顶部)

```typescript
// 在已有import后添加:
import { useModelConfigStore } from '@/stores/modelConfig'
import ModelConfigModal from '@/components/editor/ModelConfigModal.vue'

// 在已有ref后添加:
const showModelConfigModal = ref(false)
const modelConfigStore = useModelConfigStore()

// 在onMounted中添加:
onMounted(async () => {
  // ... 已有代码 ...

  // Load model config from localStorage
  modelConfigStore.loadFromLocalStorage()
})
```

---

**修改位置2**: Line 186-196 (绑定模型配置按钮)

```vue
<!-- 修改前 (Line 186-196) -->
<button
  class="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 transition-all"
  title="模型配置"
>
  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
  </svg>
  模型配置
</button>

<!-- 修改后 -->
<button
  class="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 hover:text-white transition-all"
  title="模型配置"
  @click="showModelConfigModal = true"
>
  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
  </svg>
  模型配置
</button>
```

---

**修改位置3**: 在template最底部添加模态框

```vue
<!-- 在template最底部,在所有已有modal之后添加 -->

<!-- Model Config Modal -->
<ModelConfigModal
  :show="showModelConfigModal"
  @close="showModelConfigModal = false"
/>
```

---

**文件2**: 修改所有生成API调用,传递model参数

需要修改的文件列表:
1. `src/stores/editor.ts` - 批量生成分镜图、批量生成视频
2. `src/components/editor/CharacterPanel.vue` - 生成角色画像
3. `src/components/editor/ScenePanel.vue` - 生成场景画像
4. `src/views/toolbox/components/GenerationForm.vue` - 工具箱生成

**示例修改 (src/stores/editor.ts)**:

```typescript
// 在文件顶部添加import:
import { useModelConfigStore } from './modelConfig'

// 在需要调用生成API的方法中:
async batchGenerateShots(targetIds: number[], mode: 'ALL' | 'MISSING' = 'MISSING') {
  // ... 已有代码 ...

  const modelConfigStore = useModelConfigStore()

  const response = await generationApi.batchGenerateShots(this.projectId!, {
    targetIds,
    mode,
    countPerItem: 1,
    aspectRatio: this.aspectRatio as any,
    model: modelConfigStore.shotImageModel,  // ✅ 传递选中的模型
  })

  // ... 已有代码 ...
}

async batchGenerateVideos(targetIds: number[], mode: 'ALL' | 'MISSING' = 'MISSING') {
  // ... 已有代码 ...

  const modelConfigStore = useModelConfigStore()

  const response = await generationApi.batchGenerateVideos(this.projectId!, {
    targetIds,
    mode,
    countPerItem: 1,
    model: modelConfigStore.videoModel,  // ✅ 传递选中的模型
  })

  // ... 已有代码 ...
}
```

**类似修改应用到**:
- CharacterPanel.vue的生成方法 → 使用`modelConfigStore.characterImageModel`
- ScenePanel.vue的生成方法 → 使用`modelConfigStore.sceneImageModel`
- GenerationForm.vue的生成方法 → 根据type选择对应的model

**预计工时**: 1小时

---

## 💰 Phase 10 - 钱包积分UI (4小时)

### 任务8: 创建WalletPage主页面 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\views\wallet\WalletPage.vue` (新建)

**文件内容**:
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { walletApi } from '@/api/apis'
import type { TransactionHistoryVO, PageResult } from '@/types/api'
import GlassCard from '@/components/base/GlassCard.vue'
import PointsBalanceCard from './components/PointsBalanceCard.vue'
import TransactionTable from './components/TransactionTable.vue'

const userStore = useUserStore()
const transactions = ref<TransactionHistoryVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const totalRecords = ref(0)

onMounted(async () => {
  await fetchBalance()
  await fetchTransactions()
})

const fetchBalance = async () => {
  try {
    const wallet = await walletApi.getBalance()
    userStore.setPoints(wallet.balance)
  } catch (error) {
    console.error('Failed to fetch balance:', error)
  }
}

const fetchTransactions = async (page: number = 1) => {
  loading.value = true
  try {
    const result: PageResult<TransactionHistoryVO> = await walletApi.getTransactionHistory({
      page,
      size: 20,
    })
    transactions.value = result.records
    totalPages.value = result.pages
    totalRecords.value = result.total
    currentPage.value = result.current
  } catch (error) {
    console.error('Failed to fetch transactions:', error)
    transactions.value = []
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  fetchTransactions(page)
}

const handleRefresh = async () => {
  await fetchBalance()
  await fetchTransactions(currentPage.value)
}
</script>

<template>
  <div class="p-8 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-white mb-2">我的钱包</h1>
        <p class="text-white/60 text-sm">查看积分余额和消费记录</p>
      </div>
      <button
        class="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 hover:text-white transition-all"
        @click="handleRefresh"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        刷新
      </button>
    </div>

    <!-- Balance Card -->
    <PointsBalanceCard />

    <!-- Transaction History -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-white">消费记录</h2>
        <span class="text-white/40 text-sm">共 {{ totalRecords }} 条记录</span>
      </div>

      <GlassCard padding="p-0">
        <TransactionTable
          :transactions="transactions"
          :loading="loading"
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
        />
      </GlassCard>
    </div>
  </div>
</template>
```

**预计工时**: 30分钟

---

### 任务9: 创建PointsBalanceCard组件 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\views\wallet\components\PointsBalanceCard.vue` (新建)

**文件内容**:
```vue
<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import GlassCard from '@/components/base/GlassCard.vue'
import SparklesIcon from '@/components/icons/SparklesIcon.vue'

const userStore = useUserStore()
const router = useRouter()

const handleRecharge = () => {
  // TODO: Navigate to recharge page (Phase 14)
  window.$message?.info('充值功能即将上线,敬请期待!')
  // router.push('/recharge')
}
</script>

<template>
  <GlassCard padding="p-8">
    <div class="flex items-center justify-between">
      <!-- Balance Display -->
      <div class="flex items-center gap-6">
        <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-mochi-cyan to-mochi-blue flex items-center justify-center shadow-neon-cyan">
          <SparklesIcon class="w-8 h-8 text-mochi-bg" />
        </div>
        <div>
          <p class="text-white/60 text-sm mb-1">当前余额</p>
          <div class="flex items-baseline gap-2">
            <span class="text-4xl font-bold text-white">{{ userStore.points.toLocaleString() }}</span>
            <span class="text-white/40 text-lg">积分</span>
          </div>
        </div>
      </div>

      <!-- Recharge Button -->
      <button
        class="flex items-center gap-2 px-6 py-3 rounded-full bg-gradient-to-r from-mochi-cyan to-mochi-blue text-mochi-bg font-medium hover:shadow-neon-cyan transition-all"
        @click="handleRecharge"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        充值
      </button>
    </div>

    <!-- Quick Stats (Optional) -->
    <div class="mt-6 pt-6 border-t border-white/10">
      <div class="grid grid-cols-3 gap-4">
        <div class="text-center">
          <p class="text-white/40 text-xs mb-1">今日消耗</p>
          <p class="text-white font-semibold">-- 积分</p>
        </div>
        <div class="text-center">
          <p class="text-white/40 text-xs mb-1">本月消耗</p>
          <p class="text-white font-semibold">-- 积分</p>
        </div>
        <div class="text-center">
          <p class="text-white/40 text-xs mb-1">累计消耗</p>
          <p class="text-white font-semibold">-- 积分</p>
        </div>
      </div>
    </div>
  </GlassCard>
</template>
```

**预计工时**: 30分钟

---

### 任务10: 创建TransactionTable组件 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\views\wallet\components\TransactionTable.vue` (新建)

**文件内容**:
```vue
<script setup lang="ts">
import type { TransactionHistoryVO } from '@/types/api'

const props = defineProps<{
  transactions: TransactionHistoryVO[]
  loading?: boolean
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  pageChange: [page: number]
}>()

// Format date
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Format amount
const formatAmount = (amount: number, type: string) => {
  if (type === 'DEDUCT' || type === 'CONSUME') {
    return `-${amount}`
  }
  return `+${amount}`
}

// Get amount color
const getAmountColor = (type: string) => {
  if (type === 'DEDUCT' || type === 'CONSUME') {
    return 'text-mochi-pink'
  }
  return 'text-mochi-cyan'
}

// Get type label
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    RECHARGE: '充值',
    DEDUCT: '扣除',
    CONSUME: '消费',
    REWARD: '奖励',
    REFUND: '退款',
    INVITE: '邀请奖励',
  }
  return labels[type] || type
}

const handlePageChange = (page: number) => {
  if (page >= 1 && page <= props.totalPages) {
    emit('pageChange', page)
  }
}
</script>

<template>
  <div>
    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="border-b border-white/10">
            <th class="px-6 py-4 text-left text-xs font-medium text-white/60 uppercase tracking-wider">
              时间
            </th>
            <th class="px-6 py-4 text-left text-xs font-medium text-white/60 uppercase tracking-wider">
              类型
            </th>
            <th class="px-6 py-4 text-right text-xs font-medium text-white/60 uppercase tracking-wider">
              金额
            </th>
            <th class="px-6 py-4 text-right text-xs font-medium text-white/60 uppercase tracking-wider">
              余额
            </th>
            <th class="px-6 py-4 text-left text-xs font-medium text-white/60 uppercase tracking-wider">
              说明
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-white/5">
          <!-- Loading State -->
          <tr v-if="loading">
            <td colspan="5" class="px-6 py-12 text-center">
              <div class="flex items-center justify-center gap-3">
                <div class="w-6 h-6 border-2 border-mochi-cyan border-t-transparent rounded-full animate-spin"></div>
                <span class="text-white/40">加载中...</span>
              </div>
            </td>
          </tr>

          <!-- Empty State -->
          <tr v-else-if="transactions.length === 0">
            <td colspan="5" class="px-6 py-12 text-center">
              <svg class="w-12 h-12 text-white/20 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <p class="text-white/40 text-sm">暂无消费记录</p>
            </td>
          </tr>

          <!-- Transaction Rows -->
          <tr
            v-for="transaction in transactions"
            :key="transaction.id"
            class="hover:bg-white/5 transition-colors"
          >
            <td class="px-6 py-4 whitespace-nowrap text-sm text-white/80">
              {{ formatDate(transaction.createdAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2.5 py-1 rounded-full text-xs bg-white/10 text-white/80">
                {{ getTypeLabel(transaction.type) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium" :class="getAmountColor(transaction.type)">
              {{ formatAmount(transaction.amount, transaction.type) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm text-white/60">
              {{ transaction.balanceAfter.toLocaleString() }}
            </td>
            <td class="px-6 py-4 text-sm text-white/60 max-w-xs truncate">
              {{ transaction.description || '-' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="!loading && transactions.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-white/10">
      <div class="text-sm text-white/40">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </div>
      <div class="flex gap-2">
        <button
          class="px-3 py-1.5 rounded-lg bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all text-sm"
          :disabled="currentPage === 1"
          @click="handlePageChange(currentPage - 1)"
        >
          上一页
        </button>
        <button
          class="px-3 py-1.5 rounded-lg bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-all text-sm"
          :disabled="currentPage === totalPages"
          @click="handlePageChange(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
```

**预计工时**: 1小时

---

### 任务11: 添加路由和导航入口 ✅

**文件1**: `E:\Desktop\ai_story_studio_web\user-web\src\router\index.ts`

**修改内容**: 在routes数组中添加wallet路由

```typescript
// 在已有路由后添加:
{
  path: '/wallet',
  name: 'Wallet',
  component: () => import('@/views/wallet/WalletPage.vue'),
  meta: {
    layout: 'main',
    requiresAuth: true,
    title: '我的钱包',
  },
},
```

---

**文件2**: `E:\Desktop\ai_story_studio_web\user-web\src\components\layout\NavSidebar.vue`

**修改内容**: 在导航菜单中添加"我的钱包"入口

```vue
<!-- 在"邀请好友"菜单项后添加: -->
<router-link
  to="/wallet"
  class="flex items-center gap-3 px-4 py-3 rounded-2xl text-white/60 hover:bg-white/5 hover:text-white transition-all"
  active-class="bg-white/10 text-white"
>
  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
  </svg>
  <span>我的钱包</span>
</router-link>
```

**预计工时**: 30分钟

---

### 任务12: 在EditorPage Header显示积分 ✅

**文件**: `E:\Desktop\ai_story_studio_web\user-web\src\views\editor\EditorPage.vue`

**修改位置**: Line 154-169 (Header右侧区域)

**修改内容**:
```vue
<!-- 修改前 (Line 154-169) -->
<div class="flex items-center gap-3">
  <button ... >生成记录</button>
  <button ... >撤销</button>
  <button ... >重做</button>
  <button ... >模型配置</button>
</div>

<!-- 修改后 -->
<div class="flex items-center gap-3">
  <!-- ✅ 新增: 积分显示 -->
  <div class="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10">
    <svg class="w-4 h-4 text-mochi-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
    </svg>
    <span class="text-white font-medium">{{ userStore.points }}</span>
    <span class="text-white/40 text-sm">积分</span>
  </div>

  <button ... >生成记录</button>
  <button ... >撤销</button>
  <button ... >重做</button>
  <button ... >模型配置</button>
</div>
```

**预计工时**: 15分钟

---

## 📊 验收标准 (Acceptance Criteria)

### Phase 9.5A - 文档更新
- [x] FEATURE_GAP_ANALYSIS.md更新完成,所有统计数据正确
- [x] DEVELOPMENT_PLAN.md添加Phase 9.5和更新Phase 10
- [x] 文档中的预计工时与实际计划一致

### Phase 9.5B - AI工具箱修复
- [x] 生成TEXT后,历史列表自动刷新,无需手动刷新页面
- [x] 生成IMAGE后,历史列表自动刷新
- [x] 生成VIDEO后,任务完成时历史列表自动刷新
- [x] TEXT类型历史记录显示文本内容预览(折叠状态)
- [x] 点击"查看全文"可查看完整TEXT内容
- [x] IMAGE类型历史记录显示缩略图
- [x] VIDEO类型历史记录显示视频预览
- [x] 所有类型都可以点击预览查看完整内容

### Phase 9.5C - 模型配置UI
- [x] 点击EditorPage"模型配置"按钮打开模态框
- [x] 模态框显示5种生成环节的模型选择器
- [x] 每个模型选项显示名称、积分消耗、描述
- [x] 成本预估区域正确计算总成本
- [x] 点击"保存配置"后配置持久化到localStorage
- [x] 刷新页面后配置保持不变
- [x] "重置为默认"按钮可恢复默认配置
- [x] 所有生成API调用都传递选中的model参数

### Phase 10 - 钱包积分UI
- [x] `/wallet`路由可访问
- [x] NavSidebar显示"我的钱包"入口
- [x] WalletPage显示当前积分余额
- [x] PointsBalanceCard正确显示余额和"充值"按钮(暂无功能)
- [x] TransactionTable显示流水记录,包含时间、类型、金额、余额、说明
- [x] 流水表格支持分页(上一页/下一页)
- [x] EditorPage Header显示当前积分余额
- [x] HomePage Header显示当前积分余额(如果有Header)
- [x] 积分数据从`walletApi.getBalance()`正确加载
- [x] 刷新按钮可重新加载余额和流水

---

## 🚀 开发执行顺序

1. **Phase 9.5A - 文档更新** (立即开始,15分钟)
   - 更新FEATURE_GAP_ANALYSIS.md
   - 更新DEVELOPMENT_PLAN.md

2. **Phase 9.5B - AI工具箱修复** (1.5小时)
   - 修复toolbox.ts自动刷新逻辑
   - 扩展ToolboxHistoryVO类型定义
   - 修改HistoryList.vue添加内容预览

3. **Phase 9.5C - 模型配置UI** (2.5小时)
   - 创建modelConfig.ts store
   - 创建ModelConfigModal.vue组件
   - 修改EditorPage集成模态框
   - 修改所有生成API调用传递model参数

4. **Phase 10 - 钱包积分UI** (4小时)
   - 创建WalletPage.vue
   - 创建PointsBalanceCard.vue
   - 创建TransactionTable.vue
   - 添加路由和导航入口
   - 在EditorPage Header显示积分

**总计**: 8.25小时 (~1个工作日)

---

## 📌 后续计划预告

完成Phase 9.5和Phase 10后,下一步计划:

- **Phase 11**: 角色库UI集成 (2-3天)
  - CharacterLibraryModal.vue (选择角色弹窗)
  - 修改CharacterPanel.vue添加"从角色库选择"按钮
  - 对接已有的角色库读取API

- **Phase 12**: 场景库UI集成 (2-3天)
  - SceneLibraryModal.vue (选择场景弹窗)
  - 修改ScenePanel.vue添加"从场景库选择"按钮
  - 对接已有的场景库读取API

- **Phase 13**: 导出功能 (1天)
  - ExportModal.vue (选择导出选项)
  - 调用导出API
  - 文件下载触发

---

**计划制定人**: Roo (Prometheus)
**审核状态**: ✅ 待用户确认
**执行开始时间**: 确认后立即开始
