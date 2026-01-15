<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#Recharge-Module]
//   Timestamp: 2026-01-04
//   Phase: [D-Develop]
//   Context-Analysis: "Creating ProductCard component for displaying recharge packages with pricing and points information."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics-User-Approved, DRY"
// }}
// {{START_MODIFICATIONS}}

import type { RechargeProductVO } from '@/types/api'
import { computed } from 'vue'

interface Props {
  product: RechargeProductVO
  selected?: boolean
}

interface Emits {
  (e: 'select', product: RechargeProductVO): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 计算价格（分转元）
const priceYuan = computed(() => (props.product.priceCents / 100).toFixed(2))

// 计算折扣（如果有）
const discount = computed(() => {
  // 假设标准比例是 10积分 = 1元
  const standardPrice = props.product.points / 10
  const actualPrice = props.product.priceCents / 100
  if (actualPrice < standardPrice) {
    return Math.round((actualPrice / standardPrice) * 100)
  }
  return null
})

const handleClick = () => {
  if (props.product.enabled) {
    emit('select', props.product)
  }
}
</script>

<template>
  <div
    class="relative bg-white/5 border rounded-2xl p-6 transition-all cursor-pointer group"
    :class="[
      selected
        ? 'border-[#00FFCC] bg-[#00FFCC]/10'
        : 'border-white/10 hover:border-[#00FFCC]/50 hover:bg-white/10',
      product.enabled ? '' : 'opacity-50 cursor-not-allowed'
    ]"
    @click="handleClick"
  >
    <!-- 折扣标签 -->
    <div
      v-if="discount && product.enabled"
      class="absolute top-4 right-4 bg-gradient-to-r from-[#FF6B6B] to-[#FF4444] px-3 py-1 rounded-full text-white text-xs font-bold"
    >
      {{ discount }}折
    </div>

    <!-- 套餐名称 -->
    <h3 class="text-xl font-bold text-white mb-4">
      {{ product.name }}
    </h3>

    <!-- 积分数量 -->
    <div class="mb-4">
      <div class="flex items-baseline">
        <span class="text-4xl font-bold bg-gradient-to-r from-[#00FFCC] to-[#00AAFF] bg-clip-text text-transparent">
          {{ product.points }}
        </span>
        <span class="text-white/60 ml-2">积分</span>
      </div>
    </div>

    <!-- 价格 -->
    <div class="flex items-baseline mb-4">
      <span class="text-white/60 text-sm">¥</span>
      <span class="text-2xl font-bold text-white">{{ priceYuan }}</span>
    </div>

    <!-- 描述（如果有） -->
    <p v-if="product.description" class="text-white/40 text-sm">
      {{ product.description }}
    </p>

    <!-- 选中指示器 -->
    <div
      v-if="selected"
      class="absolute bottom-4 right-4 w-6 h-6 rounded-full bg-[#00FFCC] flex items-center justify-center"
    >
      <svg class="w-4 h-4 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
      </svg>
    </div>
  </div>
</template>

// {{END_MODIFICATIONS}}
