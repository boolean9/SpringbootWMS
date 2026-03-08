<template>
  <div class="ops-shell">
    <section class="page-shell">
      <div class="section-header">
        <div>
          <h3 class="section-title">货品主数据与库存作业</h3>
          <p class="section-subtitle">统一维护货品、供应商、条码和库存阈值，并支持按批次进行出入库。</p>
        </div>

        <div class="actions">
          <el-button type="primary" @click="loadData">刷新</el-button>
          <template v-if="canManage">
            <el-button @click="openCreate">新增货品</el-button>
            <el-button type="success" @click="openRecordDialog('INBOUND')">入库</el-button>
            <el-button type="warning" @click="openRecordDialog('OUTBOUND')">出库</el-button>
          </template>
        </div>
      </div>

      <div class="toolbar-row">
        <div class="filters">
          <el-input
            v-model="filters.name"
            placeholder="搜索货品名称"
            clearable
            style="width: 220px"
            @keyup.enter="loadData"
          />
          <el-select v-model="filters.storage" placeholder="仓库" clearable style="width: 160px">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="filters.goodstype" placeholder="分类" clearable style="width: 160px">
            <el-option v-for="item in goodstypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="filters.supplierId" placeholder="供应商" clearable style="width: 170px">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-input
            v-model="filters.code"
            placeholder="搜索条码或 RFID"
            clearable
            style="width: 220px"
            @keyup.enter="loadData"
          />
        </div>

        <div class="actions">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        highlight-current-row
        @current-change="handleCurrentRowChange"
      >
        <el-table-column prop="name" label="货品名称" min-width="160" />
        <el-table-column label="仓库" min-width="140">
          <template #default="{ row }">{{ resolveStorageName(row.storage) }}</template>
        </el-table-column>
        <el-table-column label="分类" min-width="140">
          <template #default="{ row }">{{ resolveGoodstypeName(row.goodstype) }}</template>
        </el-table-column>
        <el-table-column label="供应商" min-width="150">
          <template #default="{ row }">{{ resolveSupplierName(row.supplierId) }}</template>
        </el-table-column>
        <el-table-column prop="count" label="库存" width="100" align="center">
          <template #default="{ row }">
            <div class="stock-cell">
              <strong>{{ row.count }}</strong>
              <el-tag
                v-if="Number(row.minStock || 0) > 0 && Number(row.count || 0) < Number(row.minStock)"
                type="danger"
                size="small"
              >
                低
              </el-tag>
              <el-tag
                v-else-if="Number(row.maxStock || 0) > 0 && Number(row.count || 0) > Number(row.maxStock)"
                type="warning"
                size="small"
              >
                高
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="barcode" label="条码" min-width="140" />
        <el-table-column prop="rfidTag" label="RFID" min-width="140" />
        <el-table-column label="阈值" min-width="120" align="center">
          <template #default="{ row }">
            {{ row.minStock || 0 }} / {{ row.maxStock || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="canManage" label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除货品？" @confirm="removeGoods(row.id)">
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

    <el-dialog v-model="goodsDialogVisible" :title="goodsForm.id ? '编辑货品' : '新增货品'" width="700px" destroy-on-close>
      <el-form ref="goodsFormRef" :model="goodsForm" :rules="goodsRules" label-width="100px">
        <el-form-item label="货品名称" prop="name">
          <el-input v-model="goodsForm.name" />
        </el-form-item>
        <el-form-item label="仓库" prop="storage">
          <el-select v-model="goodsForm.storage" style="width: 100%">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="goodstype">
          <el-select v-model="goodsForm.goodstype" style="width: 100%">
            <el-option v-for="item in goodstypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="goodsForm.supplierId" clearable style="width: 100%">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="goodsForm.count" :min="0" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="goodsForm.barcode" />
        </el-form-item>
        <el-form-item label="RFID">
          <el-input v-model="goodsForm.rfidTag" />
        </el-form-item>
        <el-form-item label="最小库存">
          <el-input-number v-model="goodsForm.minStock" :min="0" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="最大库存">
          <el-input-number v-model="goodsForm.maxStock" :min="0" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="goodsForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="goodsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGoodsForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="recordDialogTitle" width="620px" destroy-on-close>
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="90px">
        <el-form-item label="货品">
          <el-input v-model="recordForm.goodsname" readonly />
        </el-form-item>
        <el-form-item label="申请人" prop="userid">
          <el-select v-model="recordForm.userid" filterable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="`${item.name} (${item.no})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次">
          <el-select v-model="recordForm.batchId" clearable style="width: 100%" @change="handleBatchChange">
            <el-option v-for="item in batchOptions" :key="item.id" :label="item.batchNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="recordForm.supplierId" clearable style="width: 100%">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="count">
          <el-input-number v-model="recordForm.count" :min="1" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="扫码值">
          <el-input v-model="recordForm.scanCode" placeholder="可选：记录本次扫码值" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="recordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRecordForm">提交</el-button>
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
import { getCurrentUser } from '@/utils/session'

const currentUser = computed(() => getCurrentUser() || {})
const canManage = computed(() => Number(currentUser.value.roleId) !== 2)

const filters = reactive({
  name: '',
  storage: '',
  goodstype: '',
  supplierId: '',
  code: '',
})

const currentRow = ref(null)
const goodsDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const goodsFormRef = ref()
const recordFormRef = ref()
const userOptions = ref([])
const batchOptions = ref([])

const createDefaultGoodsForm = () => ({
  id: '',
  name: '',
  storage: '',
  goodstype: '',
  supplierId: '',
  count: 0,
  barcode: '',
  rfidTag: '',
  minStock: 0,
  maxStock: 0,
  remark: '',
})

const createDefaultRecordForm = () => ({
  goods: '',
  goodsname: '',
  batchId: '',
  userid: '',
  adminId: '',
  supplierId: '',
  count: 1,
  remark: '',
  actionType: 'INBOUND',
  scanCode: '',
})

const goodsForm = reactive(createDefaultGoodsForm())
const recordForm = reactive(createDefaultRecordForm())

const goodsRules = {
  name: [{ required: true, message: '请输入货品名称', trigger: 'blur' }],
  storage: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  goodstype: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const recordRules = {
  userid: [{ required: true, message: '请选择申请人', trigger: 'change' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const recordDialogTitle = computed(() => (recordForm.actionType === 'INBOUND' ? '货品入库' : '货品出库'))

const {
  goodsOptions,
  storageOptions,
  goodstypeOptions,
  supplierOptions,
  loadBaseLookups,
} = useLookupData()

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/goods/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        name: filters.name,
        storage: filters.storage ? String(filters.storage) : '',
        goodstype: filters.goodstype ? String(filters.goodstype) : '',
        supplierId: filters.supplierId ? String(filters.supplierId) : '',
        code: filters.code,
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取货品列表失败')
      return { data: [], total: 0 }
    }

    goodsOptions.value = result.data || goodsOptions.value
    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const loadUsers = async () => {
  const result = await request.get('/user/list')
  userOptions.value = Array.isArray(result) ? result : result?.data || []
}

const loadBatches = async (goodsId) => {
  const result = await request.get('/batch/list', {
    params: { goodsId },
  })
  batchOptions.value = result.code === 200 ? result.data : []
}

const resolveStorageName = (id) => storageOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'
const resolveGoodstypeName = (id) => goodstypeOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'
const resolveSupplierName = (id) => supplierOptions.value.find((item) => Number(item.id) === Number(id))?.name || '-'

const handleCurrentRowChange = (row) => {
  currentRow.value = row
}

const resetFilters = async () => {
  filters.name = ''
  filters.storage = ''
  filters.goodstype = ''
  filters.supplierId = ''
  filters.code = ''
  pageNum.value = 1
  await loadData()
}

const openCreate = async () => {
  goodsDialogVisible.value = true
  Object.assign(goodsForm, createDefaultGoodsForm())
  await nextTick()
  goodsFormRef.value?.clearValidate()
}

const openEdit = async (row) => {
  goodsDialogVisible.value = true
  Object.assign(goodsForm, { ...createDefaultGoodsForm(), ...row })
  await nextTick()
  goodsFormRef.value?.clearValidate()
}

const submitGoodsForm = async () => {
  await goodsFormRef.value.validate()
  const api = goodsForm.id ? '/goods/update' : '/goods/save'
  const result = await request.post(api, { ...goodsForm })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '保存货品失败')
    return
  }

  ElMessage.success(goodsForm.id ? '货品已更新' : '货品已创建')
  goodsDialogVisible.value = false
  await loadData()
}

const removeGoods = async (id) => {
  const result = await request.get('/goods/del', { params: { id } })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除货品失败')
    return
  }

  ElMessage.success('货品已删除')
  await loadData()
}

const openRecordDialog = async (actionType) => {
  if (!currentRow.value?.id) {
    ElMessage.warning('请先选中一条货品记录')
    return
  }

  Object.assign(recordForm, createDefaultRecordForm(), {
    goods: currentRow.value.id,
    goodsname: currentRow.value.name,
    adminId: currentUser.value.id,
    supplierId: currentRow.value.supplierId || '',
    actionType,
  })
  await loadBatches(currentRow.value.id)
  recordDialogVisible.value = true
  await nextTick()
  recordFormRef.value?.clearValidate()
}

const handleBatchChange = (batchId) => {
  const batch = batchOptions.value.find((item) => Number(item.id) === Number(batchId))
  if (batch) {
    recordForm.supplierId = batch.supplierId || recordForm.supplierId
    recordForm.scanCode = recordForm.scanCode || batch.barcode || batch.rfidTag || ''
  }
}

const submitRecordForm = async () => {
  await recordFormRef.value.validate()
  const result = await request.post('/record/save', {
    ...recordForm,
    action: recordForm.actionType === 'INBOUND' ? '1' : '2',
  })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '提交出入库失败')
    return
  }

  ElMessage.success(recordForm.actionType === 'INBOUND' ? '入库成功' : '出库成功')
  recordDialogVisible.value = false
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadBaseLookups(), loadUsers()])
  await loadData()
})
</script>

<style scoped>
.stock-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
