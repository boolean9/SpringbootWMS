<template>
  <el-icon :style="{ fontSize: `${size}px` }">
    <component :is="resolvedIcon" />
  </el-icon>
</template>

<script setup>
import { computed } from 'vue'
import {
  ArrowDown,
  Bottom,
  Box,
  DataAnalysis,
  Delete,
  Document,
  Edit,
  Expand,
  Fold,
  HomeFilled,
  House,
  Key,
  Menu,
  Plus,
  Postcard,
  Refresh,
  Search,
  SwitchButton,
  Timer,
  Top,
  User,
  WarningFilled,
} from '@element-plus/icons-vue'

const props = defineProps({
  name: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 18,
  },
})

const iconMap = {
  'el-icon-arrow-down': ArrowDown,
  'el-icon-bottom': Bottom,
  'el-icon-box': Box,
  'el-icon-data-analysis': DataAnalysis,
  'el-icon-delete': Delete,
  'el-icon-document': Document,
  'el-icon-edit': Edit,
  'el-icon-goods': Box,
  'el-icon-house': House,
  'el-icon-key': Key,
  'el-icon-menu': Menu,
  'el-icon-plus': Plus,
  'el-icon-postcard': Postcard,
  'el-icon-refresh': Refresh,
  'el-icon-s-fold': Fold,
  'el-icon-s-home': HomeFilled,
  'el-icon-s-management': Menu,
  'el-icon-s-operation': Menu,
  'el-icon-s-unfold': Expand,
  'el-icon-search': Search,
  'el-icon-switch-button': SwitchButton,
  'el-icon-time': Timer,
  'el-icon-top': Top,
  'el-icon-user': User,
  'el-icon-warning': WarningFilled,
}

const resolvedIcon = computed(() => iconMap[props.name] || Menu)
</script>
