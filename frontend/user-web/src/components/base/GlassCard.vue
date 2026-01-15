<script setup lang="ts">
interface Props {
  padding?: string
  borderRadius?: string
  className?: string
}

withDefaults(defineProps<Props>(), {
  padding: 'p-4',
  borderRadius: 'rounded-lg',
  className: '',
})
</script>

<template>
  <div
    :class="[
      'bg-bg-secondary border border-border-default shadow-card',
      borderRadius,
      padding,
      className
    ]"
  >
    <slot />
  </div>
</template>

<style scoped>
/* 简洁白色卡片组件 */
</style>
