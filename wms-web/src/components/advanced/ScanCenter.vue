<template>
  <div class="ops-shell">
    <section class="section-card scan-hero">
      <div>
        <p class="hero-kicker">SCAN FLOW</p>
        <h3 class="section-title">条码 / RFID 快速作业</h3>
        <p class="section-subtitle">兼容扫码枪输入，先识别货品与批次，再一步提交入库或出库操作。</p>
      </div>

      <div class="scan-input-wrap">
        <el-input
          v-model="scanCode"
          placeholder="扫描条码或 RFID 后按回车"
          clearable
          size="large"
          @keyup.enter="resolveCode"
        >
          <template #append>
            <el-button type="primary" @click="resolveCode">识别</el-button>
          </template>
        </el-input>
      </div>
    </section>

    <div class="data-grid-2">
      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">识别结果</h3>
            <p class="section-subtitle">自动优先匹配批次码，其次匹配货品主条码或 RFID</p>
          </div>
        </div>

        <div v-if="resolvedGoods" class="resolved-card">
          <h4>{{ resolvedGoods.name }}</h4>
          <p>库存 {{ resolvedGoods.count }}，条码 {{ resolvedGoods.barcode || '-' }}，RFID {{ resolvedGoods.rfidTag || '-' }}</p>

          <div class="batch-list">
            <article v-for="item in resolvedBatches" :key="item.id" class="batch-item">
              <strong>{{ item.batchNo }}</strong>
              <span>库存 {{ item.quantity || 0 }}</span>
              <span>效期 {{ item.expiryDate || '-' }}</span>
            </article>
          </div>
        </div>
        <el-empty v-else description="等待扫码识别" />
      </section>

      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">快捷操作</h3>
            <p class="section-subtitle">支持直接提交入库 / 出库记录</p>
          </div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="操作类型" prop="actionType">
            <el-radio-group v-model="form.actionType">
              <el-radio label="INBOUND">入库</el-radio>
              <el-radio label="OUTBOUND">出库</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="form.quantity" :min="1" :max="999999" controls-position="right" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="例如：扫码复核出库" />
          </el-form-item>
        </el-form>

        <div class="actions">
          <el-button @click="resetAll">清空</el-button>
          <el-button type="primary" :disabled="!resolvedGoods" @click="submitOperation">提交作业</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/session'

const currentUser = computed(() => getCurrentUser() || {})
const scanCode = ref('')
const resolvedGoods = ref(null)
const resolvedBatches = ref([])
const formRef = ref()

const form = reactive({
  actionType: 'OUTBOUND',
  quantity: 1,
  remark: '',
})

const rules = {
  actionType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const resolveCode = async () => {
  if (!scanCode.value) {
    ElMessage.warning('请先输入条码或 RFID')
    return
  }

  const result = await request.get('/scan/resolve', {
    params: { code: scanCode.value },
  })

  if (result.code !== 200 || !result.data?.matched) {
    resolvedGoods.value = null
    resolvedBatches.value = []
    ElMessage.error(result.msg || '未识别到对应货品')
    return
  }

  resolvedGoods.value = result.data.goods
  resolvedBatches.value = result.data.batches || []
  ElMessage.success('识别成功')
}

const submitOperation = async () => {
  await formRef.value.validate()
  const result = await request.post('/scan/operate', {
    code: scanCode.value,
    quantity: form.quantity,
    userId: currentUser.value.id,
    adminId: currentUser.value.id,
    actionType: form.actionType,
    remark: form.remark,
  })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '扫码作业失败')
    return
  }

  ElMessage.success(form.actionType === 'INBOUND' ? '扫码入库成功' : '扫码出库成功')
  await resolveCode()
}

const resetAll = () => {
  scanCode.value = ''
  resolvedGoods.value = null
  resolvedBatches.value = []
  form.actionType = 'OUTBOUND'
  form.quantity = 1
  form.remark = ''
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.scan-hero {
  display: grid;
  gap: 18px;
  background: linear-gradient(135deg, #082f49, #0f766e 68%, #22c55e);
  color: #fff;
}

.hero-kicker {
  font-size: 12px;
  letter-spacing: 0.18em;
  opacity: 0.78;
}

.scan-input-wrap {
  max-width: 520px;
}

.resolved-card h4 {
  font-size: 24px;
  color: #0f172a;
}

.resolved-card p {
  margin-top: 8px;
  color: var(--color-text-secondary);
}

.batch-list {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.batch-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid var(--color-border-light);
}
</style>
