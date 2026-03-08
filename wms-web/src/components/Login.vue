<template>
  <div class="login-page">
    <div class="ambient-bg">
      <div class="orb orb-one"></div>
      <div class="orb orb-two"></div>
      <div class="grid-mask"></div>
    </div>

    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-badge">
          <span class="brand-icon">W</span>
          <span class="brand-name">Warehouse Matrix</span>
        </div>

        <div class="brand-copy">
          <h1>智能仓储协同中台</h1>
          <p>统一管理仓库、分类、货品、出入库记录和人员权限。</p>
        </div>

        <div class="brand-points">
          <div class="point-item">实时库存洞察</div>
          <div class="point-item">多角色协同流转</div>
          <div class="point-item">简洁稳定的管理后台</div>
        </div>
      </section>

      <section class="form-panel">
        <div class="auth-switcher">
          <button
            type="button"
            :class="['switch-item', { active: activeTab === 'login' }]"
            @click="activeTab = 'login'"
          >
            登录
          </button>
          <button
            type="button"
            :class="['switch-item', { active: activeTab === 'register' }]"
            @click="activeTab = 'register'"
          >
            注册
          </button>
          <div class="switch-track" :class="{ register: activeTab === 'register' }"></div>
        </div>

        <transition name="fade" mode="out-in">
          <div v-if="activeTab === 'login'" key="login">
            <div class="panel-heading">
              <h2>欢迎回来</h2>
              <p>请输入账号和密码继续访问系统。</p>
            </div>

            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="auth-form">
              <el-form-item prop="no">
                <el-input v-model="loginForm.no" placeholder="请输入账号" clearable @keyup.enter="handleLogin">
                  <template #prefix>
                    <AppIcon name="el-icon-user" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <AppIcon name="el-icon-key" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <div class="form-extra">
                <el-checkbox v-model="rememberMe">记住账号</el-checkbox>
                <span class="tip-text">默认连接本地后端 8090 端口</span>
              </div>

              <el-button class="submit-btn" type="primary" :loading="loginLoading" @click="handleLogin">
                {{ loginLoading ? '登录中...' : '登录系统' }}
              </el-button>
            </el-form>
          </div>

          <div v-else key="register">
            <div class="panel-heading">
              <h2>创建账号</h2>
              <p>注册后即可进入系统完成基础管理操作。</p>
            </div>

            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="auth-form">
              <el-form-item prop="name">
                <el-input v-model="registerForm.name" placeholder="请输入姓名" clearable>
                  <template #prefix>
                    <AppIcon name="el-icon-user" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="no">
                <el-input v-model="registerForm.no" placeholder="请输入账号" clearable>
                  <template #prefix>
                    <AppIcon name="el-icon-postcard" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码">
                  <template #prefix>
                    <AppIcon name="el-icon-key" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入密码"
                  @keyup.enter="handleRegister"
                >
                  <template #prefix>
                    <AppIcon name="el-icon-key" :size="16" />
                  </template>
                </el-input>
              </el-form-item>

              <el-button class="submit-btn" type="primary" :loading="registerLoading" @click="handleRegister">
                {{ registerLoading ? '注册中...' : '完成注册' }}
              </el-button>
            </el-form>
          </div>
        </transition>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import request from '@/utils/request'
import { setCurrentUser } from '@/utils/session'

const router = useRouter()
const store = useStore()

const activeTab = ref('login')
const rememberMe = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({
  no: '',
  password: '',
})

const registerForm = reactive({
  name: '',
  no: '',
  password: '',
  confirmPassword: '',
})

const loginRules = {
  no: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const registerRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  no: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const resetRegisterForm = () => {
  registerForm.name = ''
  registerForm.no = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerFormRef.value?.clearValidate()
}

const handleLogin = async () => {
  await loginFormRef.value.validate()
  loginLoading.value = true

  try {
    const result = await request.post('/user/login', { ...loginForm })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '账号或密码错误')
      return
    }

    setCurrentUser(result.data.user)
    store.commit('setMenu', result.data.menu || [])
    ElMessage.success('登录成功')
    await router.replace('/Home')
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  await registerFormRef.value.validate()
  registerLoading.value = true

  try {
    const result = await request.post('/user/save', {
      name: registerForm.name,
      no: registerForm.no,
      password: registerForm.password,
      roleId: 1,
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '注册失败')
      return
    }

    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.no = registerForm.no
    resetRegisterForm()
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 24px;
  background: radial-gradient(circle at top left, #1e3a8a 0%, #0f172a 35%, #020617 100%);
}

.ambient-bg {
  position: absolute;
  inset: 0;
}

.orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(40px);
  opacity: 0.4;
}

.orb-one {
  width: 240px;
  height: 240px;
  left: 10%;
  top: 15%;
  background: #38bdf8;
}

.orb-two {
  width: 300px;
  height: 300px;
  right: 12%;
  bottom: 10%;
  background: #f97316;
}

.grid-mask {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(circle at center, black, transparent 80%);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.2fr 0.95fr;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 25px 100px rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(24px);
}

.brand-panel {
  padding: 48px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(160deg, rgba(37, 99, 235, 0.32), rgba(15, 23, 42, 0.16));
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  width: fit-content;
}

.brand-icon {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
  font-weight: 700;
}

.brand-copy h1 {
  font-size: 42px;
  line-height: 1.08;
  margin: 28px 0 16px;
}

.brand-copy p {
  max-width: 420px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
}

.brand-points {
  display: grid;
  gap: 12px;
}

.point-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.form-panel {
  padding: 42px 36px;
  background: rgba(255, 255, 255, 0.94);
}

.auth-switcher {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  padding: 4px;
  background: #e2e8f0;
  border-radius: 14px;
  margin-bottom: 26px;
}

.switch-item {
  position: relative;
  z-index: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  color: #475569;
}

.switch-item.active {
  color: #0f172a;
}

.switch-track {
  position: absolute;
  left: 4px;
  top: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--shadow-sm);
  transition: transform 0.25s ease;
}

.switch-track.register {
  transform: translateX(100%);
}

.panel-heading {
  margin-bottom: 20px;
}

.panel-heading h2 {
  font-size: 28px;
  color: #0f172a;
}

.panel-heading p {
  color: #64748b;
  font-size: 14px;
  margin-top: 6px;
}

.auth-form {
  display: grid;
  gap: 6px;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 46px;
}

.form-extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.tip-text {
  font-size: 12px;
  color: #64748b;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 14px;
  margin-top: 6px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 920px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding-bottom: 28px;
  }
}
</style>
