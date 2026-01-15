<script setup lang="ts">
interface Props {
  label: string
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
})

const colorClasses = {
  default: 'bg-gray-100 text-text-secondary border border-border-default',
  primary: 'bg-accent/10 text-accent border border-accent/20',
  success: 'bg-success/10 text-success border border-success/20',
  warning: 'bg-warning/10 text-warning border border-warning/20',
  danger: 'bg-danger/10 text-danger border border-danger/20',
}
</script>

<template>
  <span
    :class="[
      'px-2 py-0.5 rounded text-xs font-medium inline-block',
      colorClasses[variant]
    ]"
  >
    {{ label }}
  </span>
</template>
