<template>
  <div class="ops-shell">
    <section class="page-shell">
      <div class="section-header">
        <div>
          <h3 class="section-title">多仓库调拨</h3>
          <p class="section-subtitle">创建调拨单并在合适时机执行，系统会自动生成调入调出记录。</p>
        </div>

        <div class="actions">
          <el-button @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">新建调拨单</el-button>
        </div>
      </div>

      <div class="toolbar-row">
        <div class="filters">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索货品名称"
            clearable
            style="width: 220px"
            @keyup.enter="loadData"
          />
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 150px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
          <el-select v-model="filters.fromStorageId" placeholder="调出仓库" clearable style="width: 160px">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="filters.toStorageId" placeholder="调入仓库" clearable style="width: 160px">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </div>

        <div class="actions">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="goodsName" label="货品" min-width="160" />
        <el-table-column prop="batchNo" label="批次" min-width="120" />
        <el-table-column prop="fromStorageName" label="调出仓库" min-width="140" />
        <el-table-column prop="toStorageName" label="调入仓库" min-width="140" />
        <el-table-column prop="quantity" label="数量" width="90" align="center" />
        <el-table-column prop="operatorName" label="操作人" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="executeTime" label="执行时间" min-width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
              {{ row.status === 'COMPLETED' ? '已完成' : '待执行' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              text
              :disabled="row.status === 'COMPLETED'"
              @click="executeTransfer(row)"
            >
              执行
            </el-button>
            <el-popconfirm title="确认删除调拨单？" @confirm="removeTransfer(row.id)">
              <template #reference>
                <el-button size="small" type="danger" text :disabled="row.status === 'COMPLETED'">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建调拨单" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="货品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="选择货品" style="width: 100%" @change="handleGoodsChange">
            <el-option v-for="item in goodsOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次">
          <el-select v-model="form.batchId" placeholder="可选" clearable style="width: 100%">
            <el-option v-for="item in batchOptions" :key="item.id" :label="item.batchNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调出仓库" prop="fromStorageId">
          <el-select v-model="form.fromStorageId" placeholder="调出仓库" style="width: 100%">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入仓库" prop="toStorageId">
          <el-select v-model="form.toStorageId" placeholder="调入仓库" style="width: 100%">
            <el-option v-for="item in storageOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="调拨原因">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="例如：高频仓缺货，发起跨仓补货" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">提交</el-button>
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
const dialogVisible = ref(false)
const formRef = ref()
const batchOptions = ref([])

const filters = reactive({
  keyword: '',
  status: '',
  fromStorageId: '',
  toStorageId: '',
})

const createDefaultForm = () => ({
  goodsId: '',
  batchId: '',
  fromStorageId: '',
  toStorageId: '',
  quantity: 1,
  reason: '',
})

const form = reactive(createDefaultForm())

const rules = {
  goodsId: [{ required: true, message: '请选择货品', trigger: 'change' }],
  fromStorageId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toStorageId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入调拨数量', trigger: 'blur' }],
}

const { goodsOptions, storageOptions, loadGoods, loadStorages } = useLookupData()

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/transfer/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        keyword: filters.keyword,
        status: filters.status,
        fromStorageId: filters.fromStorageId ? String(filters.fromStorageId) : '',
        toStorageId: filters.toStorageId ? String(filters.toStorageId) : '',
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取调拨列表失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const handleGoodsChange = async (goodsId) => {
  const currentGoods = goodsOptions.value.find((item) => Number(item.id) === Number(goodsId))
  form.fromStorageId = currentGoods?.storage ?? ''
  form.batchId = ''
  const result = await request.get('/batch/list', {
    params: { goodsId },
  })
  batchOptions.value = result.code === 200 ? result.data : []
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.status = ''
  filters.fromStorageId = ''
  filters.toStorageId = ''
  pageNum.value = 1
  await loadData()
}

const openCreate = async () => {
  dialogVisible.value = true
  Object.assign(form, createDefaultForm())
  batchOptions.value = []
  await nextTick()
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  await formRef.value.validate()
  const result = await request.post('/transfer/save', {
    ...form,
    operatorId: currentUser.value.id,
    operatorName: currentUser.value.name,
  })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '创建调拨单失败')
    return
  }

  ElMessage.success('调拨单已创建')
  dialogVisible.value = false
  Object.assign(form, createDefaultForm())
  await loadData()
}

const executeTransfer = async (row) => {
  const result = await request.post('/transfer/execute', { id: row.id })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '执行调拨失败')
    return
  }

  ElMessage.success('调拨执行成功')
  await loadData()
}

const removeTransfer = async (id) => {
  const result = await request.get('/transfer/del', { params: { id } })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除调拨单失败')
    return
  }

  ElMessage.success('调拨单已删除')
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadGoods(), loadStorages()])
  await loadData()
})
</script>
