<template>
  <div class="aside-container">
    <div class="logo-section" :class="{ collapsed: isCollapse }">
      <div class="logo-icon">W</div>
      <span v-if="!isCollapse" class="logo-text">WMS</span>
    </div>

    <el-scrollbar class="menu-scroll">
      <el-menu
        class="aside-menu"
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/Home">
          <AppIcon name="el-icon-s-home" />
          <template #title>首页</template>
        </el-menu-item>

        <el-menu-item
          v-for="item in menu"
          :key="item.id || item.menuclick"
          :index="`/${item.menuclick}`"
        >
          <AppIcon :name="item.menuicon" />
          <template #title>{{ item.menuname }}</template>
        </el-menu-item>

        <el-sub-menu v-for="group in builtinGroups" :key="group.label" :index="group.label">
          <template #title>
            <AppIcon :name="group.icon" />
            <span>{{ group.label }}</span>
          </template>

          <el-menu-item
            v-for="item in group.items"
            :key="item.path"
            :index="item.path"
          >
            <AppIcon :name="item.icon" />
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { getCurrentUser } from '@/utils/session'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false,
  },
})

const route = useRoute()
const store = useStore()

const menu = computed(() => store.getters.menu || [])
const currentUser = computed(() => getCurrentUser() || {})

const builtinGroups = computed(() => {
  const roleId = Number(currentUser.value.roleId ?? 1)
  const canAdmin = roleId !== 2

  return [
    {
      label: '感知中心',
      icon: 'el-icon-warning',
      items: [
        { path: '/Alerts', label: '库存预警', icon: 'el-icon-warning' },
        { path: '/Reports', label: '报表中心', icon: 'el-icon-data-analysis' },
      ],
    },
    {
      label: '协同作业',
      icon: 'el-icon-box',
      items: [
        { path: '/Transfers', label: '仓库调拨', icon: 'el-icon-refresh' },
        { path: '/Batches', label: '批次管理', icon: 'el-icon-box' },
        { path: '/Scanner', label: '扫码中心', icon: 'el-icon-postcard' },
      ],
    },
    {
      label: '治理审计',
      icon: 'el-icon-document',
      items: [
        { path: '/Suppliers', label: '供应商管理', icon: 'el-icon-user' },
        ...(canAdmin ? [{ path: '/AuditLogs', label: '操作日志', icon: 'el-icon-document' }] : []),
      ],
    },
  ].filter((group) => group.items.length > 0)
})
</script>

<style scoped>
.aside-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.logo-section.collapsed {
  justify-content: center;
  padding-inline: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #0f172a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 1px;
}

.menu-scroll {
  flex: 1;
}

.aside-menu {
  padding: 10px 8px 14px;
  border-right: none;
}

.aside-menu :deep(.el-menu-item),
.aside-menu :deep(.el-sub-menu__title) {
  margin-bottom: 6px;
  border-radius: 12px;
  height: 46px;
  line-height: 46px;
  gap: 10px;
}

.aside-menu :deep(.el-menu-item:hover),
.aside-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f1f5f9;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background-color: #eff6ff;
  color: var(--color-primary);
}

.aside-menu :deep(.el-sub-menu .el-menu-item) {
  margin-left: 10px;
}
</style>
