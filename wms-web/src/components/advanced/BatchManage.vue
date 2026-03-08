<template>
  <div class="ops-shell">
    <div class="ops-grid">
      <article class="metric-card">
        <span class="metric-label">当前批次数</span>
        <strong class="metric-value">{{ total }}</strong>
        <span class="metric-hint">当前筛选条件下的批次总数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">30 天内到期</span>
        <strong class="metric-value">{{ expiringCount }}</strong>
        <span class="metric-hint">建议优先消耗或调价处理</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">已过期</span>
        <strong class="metric-value">{{ expiredCount }}</strong>
        <span class="metric-hint">需要尽快冻结和隔离</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">批次库存</span>
        <strong class="metric-value">{{ totalQuantity }}</strong>
        <span class="metric-hint">当前页面合计批次数量</span>
      </article>
    </div>

    <section class="page-shell">
      <div class="section-header">
        <div>
          <h3 class="section-title">批次追踪</h3>
          <p class="section-subtitle">按批次维护生产、效期、条码和 RFID 信息，支持按仓库精细追溯。</p>
        </div>

        <div class="actions">
          <el-button @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增批次</el-button>
        </div>
      </div>

      <div class="toolbar-row">
        <div class="filters">
          <el-input
            v-model="filters.batchNo"
            placeholder="搜索批次号"
            clearable
            style="width: 200px"
            @keyup.enter="loadData"
          />
          <el-select v-model="filters.goodsId" placeholder="货品" clearable style="width: 180px">
            <el-option v-for="item in goodsOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="filters.storageId" placeholder="仓库" clearable style="width: 160px">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 150px">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
          <el-switch v-model="filters.expiringOnly" inline-prompt active-text="临期" inactive-text="全部" />
        </div>

        <div class="actions">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="batchNo" label="批次号" min-width="150" />
        <el-table-column label="货品" min-width="160">
          <template #default="{ row }">
            {{ resolveGoodsName(row.goodsId) }}
          </template>
        </el-table-column>
        <el-table-column label="仓库" min-width="140">
          <template #default="{ row }">
            {{ resolveStorageName(row.storageId) }}
          </template>
        </el-table-column>
        <el-table-column label="供应商" min-width="150">
          <template #default="{ row }">
            {{ resolveSupplierName(row.supplierId) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" align="center" />
        <el-table-column prop="barcode" label="条码" min-width="140" />
        <el-table-column prop="rfidTag" label="RFID" min-width="140" />
        <el-table-column prop="productionDate" label="生产日期" width="120" />
        <el-table-column prop="expiryDate" label="到期日期" width="120">
          <template #default="{ row }">
            <span :class="resolveExpiryClass(row.expiryDate)">{{ row.expiryDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除批次？" @confirm="removeBatch(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑批次' : '新增批次'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="货品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="选择货品" style="width: 100%" @change="handleGoodsChange">
            <el-option v-for="item in goodsOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="storageId">
          <el-select v-model="form.storageId" placeholder="仓库" style="width: 100%">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="form.supplierId" placeholder="供应商" clearable style="width: 100%">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="0" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="form.barcode" />
        </el-form-item>
        <el-form-item label="RFID">
          <el-input v-model="form.rfidTag" />
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警天数">
          <el-input-number v-model="form.alertDays" :min="1" :max="365" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { usePagedTable } from '@/composables/usePagedTable'
import { useLookupData } from '@/composables/useLookupData'
import request from '@/utils/request'

const dialogVisible = ref(false)
const formRef = ref()

const filters = reactive({
  batchNo: '',
  goodsId: '',
  storageId: '',
  status: '',
  expiringOnly: false,
})

const createDefaultForm = () => ({
  id: '',
  goodsId: '',
  storageId: '',
  supplierId: '',
  batchNo: '',
  barcode: '',
  rfidTag: '',
  productionDate: '',
  expiryDate: '',
  quantity: 0,
  alertDays: 30,
  status: 'ACTIVE',
  remark: '',
})

const form = reactive(createDefaultForm())

const rules = {
  goodsId: [{ required: true, message: '请选择货品', trigger: 'change' }],
  storageId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
}

const { goodsOptions, storageOptions, supplierOptions, loadAllLookups } = useLookupData()

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/batch/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        batchNo: filters.batchNo,
        goodsId: filters.goodsId ? String(filters.goodsId) : '',
        storageId: filters.storageId ? String(filters.storageId) : '',
        status: filters.status,
        expiringOnly: String(filters.expiringOnly),
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取批次列表失败')
      return { data: [], total: 0 }
    }

    return result
  })

const expiringCount = computed(() =>
  tableData.value.filter((item) => item.expiryDate && new Date(item.expiryDate) <= addDays(30)).length,
)

const expiredCount = computed(() =>
  tableData.value.filter((item) => item.expiryDate && new Date(item.expiryDate) < addDays(0, true)).length,
)

const totalQuantity = computed(() =>
  tableData.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0),
)

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const resolveGoodsName = (id) => goodsOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'
const resolveStorageName = (id) => storageOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'
const resolveSupplierName = (id) => supplierOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'

const handleGoodsChange = (goodsId) => {
  const goods = goodsOptions.value.find((item) => Number(item.id) === Number(goodsId))
  if (goods) {
    form.storageId = goods.storage
    form.supplierId = goods.supplierId || ''
    form.barcode = form.barcode || goods.barcode || ''
    form.rfidTag = form.rfidTag || goods.rfidTag || ''
  }
}

const resetFilters = async () => {
  filters.batchNo = ''
  filters.goodsId = ''
  filters.storageId = ''
  filters.status = ''
  filters.expiringOnly = false
  pageNum.value = 1
  await loadData()
}

const openCreate = async () => {
  dialogVisible.value = true
  Object.assign(form, createDefaultForm())
  await nextTick()
  formRef.value?.clearValidate()
}

const openEdit = async (row) => {
  dialogVisible.value = true
  Object.assign(form, { ...createDefaultForm(), ...row })
  await nextTick()
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  await formRef.value.validate()
  const api = form.id ? '/batch/update' : '/batch/save'
  const result = await request.post(api, { ...form })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '保存批次失败')
    return
  }

  ElMessage.success(form.id ? '批次已更新' : '批次已创建')
  dialogVisible.value = false
  await loadData()
}

const removeBatch = async (id) => {
  const result = await request.get('/batch/del', { params: { id } })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除批次失败')
    return
  }

  ElMessage.success('批次已删除')
  await loadData()
}

const resolveExpiryClass = (expiryDate) => {
  if (!expiryDate) {
    return ''
  }

  const date = new Date(expiryDate)
  if (date < addDays(0, true)) {
    return 'is-expired'
  }
  if (date <= addDays(30)) {
    return 'is-expiring'
  }
  return ''
}

function addDays(days, startOfDay = false) {
  const base = new Date()
  if (startOfDay) {
    base.setHours(0, 0, 0, 0)
  }
  base.setDate(base.getDate() + days)
  return base
}

onMounted(async () => {
  await loadAllLookups()
  await loadData()
})
</script>

<style scoped>
.is-expiring {
  color: #d97706;
  font-weight: 600;
}

.is-expired {
  color: #dc2626;
  font-weight: 700;
}
</style>
