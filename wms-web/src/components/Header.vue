<template>
  <div class="header-container">
    <div class="header-left">
      <button class="collapse-btn" type="button" @click="emit('toggle-aside')">
        <AppIcon :name="icon" :size="20" />
      </button>

      <div class="header-title">
        <span class="system-name">仓储管理系统</span>
        <span class="system-subtitle">{{ currentDate }}</span>
      </div>
    </div>

    <el-dropdown class="user-dropdown" trigger="click" @command="handleCommand">
      <div class="user-info">
        <div class="avatar">{{ userInitial }}</div>
        <div class="user-meta">
          <span class="username">{{ user.name || '访客' }}</span>
          <span class="role">{{ roleLabel }}</span>
        </div>
        <AppIcon name="el-icon-arrow-down" :size="14" />
      </div>

      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">个人中心</el-dropdown-item>
          <el-dropdown-item command="password">修改密码</el-dropdown-item>
          <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getCurrentUser } from '@/utils/session'

defineProps({
  icon: {
    type: String,
    default: 'el-icon-s-fold',
  },
})

const emit = defineEmits(['toggle-aside'])

const router = useRouter()
const store = useStore()
const user = computed(() => getCurrentUser() || {})

const userInitial = computed(() => (user.value.name ? user.value.name.slice(0, 1).toUpperCase() : 'W'))

const roleLabel = computed(() => {
  switch (Number(user.value.roleId)) {
    case 0:
      return '超级管理员'
    case 1:
      return '管理员'
    default:
      return '用户'
  }
})

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
    date.getDate(),
  ).padStart(2, '0')}`
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    await router.push('/Profile')
    return
  }

  if (command === 'password') {
    try {
      const { value } = await ElMessageBox.prompt('请输入不少于 6 位的新密码', '修改密码', {
        inputPattern: /^.{6,}$/,
        inputErrorMessage: '密码长度不能少于 6 位',
      })

      if (value) {
        ElMessage.success('密码修改成功')
      }
    } catch (error) {
      // User cancelled the prompt.
    }
    return
  }

  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出当前登录吗？', '提示', {
        type: 'warning',
      })

      store.commit('clearSession')
      ElMessage.success('已退出登录')
      await router.replace('/')
    } catch (error) {
      // User cancelled the confirmation.
    }
  }
}
</script>

<style scoped>
.header-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 10px;
  background: #f8fafc;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.collapse-btn:hover {
  background: #eff6ff;
  color: var(--color-primary);
}

.header-title {
  display: flex;
  flex-direction: column;
}

.system-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.system-subtitle {
  font-size: 12px;
  color: var(--color-text-muted);
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 12px;
  transition: var(--transition);
}

.user-info:hover {
  background: #f8fafc;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #0f172a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.role {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
