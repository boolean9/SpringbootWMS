<template>
  <div class="profile-container">
    <section class="profile-hero">
      <div class="profile-avatar">{{ userInitial }}</div>
      <div class="profile-meta">
        <h1>{{ user.name || '未登录用户' }}</h1>
        <p>{{ roleLabel }}</p>
      </div>
    </section>

    <section class="profile-grid">
      <article class="profile-card">
        <header class="card-header">
          <h3>基础信息</h3>
        </header>

        <div class="info-list">
          <div class="info-item">
            <span class="label">姓名</span>
            <strong>{{ user.name || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="label">账号</span>
            <strong>{{ user.no || '-' }}</strong>
          </div>
          <div class="info-item">
            <span class="label">角色</span>
            <strong>{{ roleLabel }}</strong>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <el-tag type="success">正常</el-tag>
          </div>
        </div>
      </article>

      <article class="profile-card">
        <header class="card-header">
          <h3>安全设置</h3>
        </header>

        <div class="security-row">
          <div>
            <strong>登录密码</strong>
            <p>建议定期更新密码，避免多人共用同一账号。</p>
          </div>
          <el-button type="primary" plain @click="showPasswordDialog = true">修改密码</el-button>
        </div>
      </article>
    </section>

    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="460px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPassword">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser } from '@/utils/session'

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

const showPasswordDialog = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateNewPassword = (rule, value, callback) => {
  if (!value || value.length < 6) {
    callback(new Error('新密码不能少于 6 位'))
    return
  }

  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const submitPassword = async () => {
  await passwordFormRef.value.validate()
  ElMessage.success('密码修改成功')
  showPasswordDialog.value = false
  resetPasswordForm()
}
</script>

<style scoped>
.profile-container {
  display: grid;
  gap: 20px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 28px;
  border-radius: 24px;
  color: #fff;
  background: linear-gradient(135deg, #0f172a 0%, #2563eb 100%);
}

.profile-avatar {
  width: 82px;
  height: 82px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
}

.profile-meta h1 {
  font-size: 28px;
}

.profile-meta p {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.78);
}

.profile-grid {
  display: grid;
  gap: 20px;
}

.profile-card {
  background: #fff;
  border-radius: 20px;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.card-header {
  padding: 18px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.card-header h3 {
  font-size: 18px;
  color: #0f172a;
}

.info-list {
  padding: 8px 20px 18px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f8fafc;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: var(--color-text-secondary);
}

.security-row {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.security-row p {
  margin-top: 6px;
  color: var(--color-text-muted);
  font-size: 13px;
}

@media (max-width: 768px) {
  .profile-hero,
  .security-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
