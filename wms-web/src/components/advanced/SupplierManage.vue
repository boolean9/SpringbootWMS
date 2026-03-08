<template>
  <div class="page-shell">
    <div class="section-header">
      <div>
        <h3 class="section-title">供应商管理</h3>
        <p class="section-subtitle">维护供应商档案，形成采购、入库和库存预警的闭环数据基础。</p>
      </div>

      <div class="actions">
        <el-button @click="loadData">刷新</el-button>
        <el-button type="primary" @click="openCreate">新增供应商</el-button>
      </div>
    </div>

    <div class="toolbar-row">
      <div class="filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索编码、名称或联系人"
          clearable
          style="width: 260px"
          @keyup.enter="loadData"
        />
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 150px">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
      </div>

      <div class="actions">
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="code" label="编码" min-width="120" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="contactPerson" label="联系人" min-width="120" />
      <el-table-column prop="phone" label="电话" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="level" label="等级" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除供应商？" @confirm="removeSupplier(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑供应商' : '新增供应商'" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="例如 SUP-001" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" clearable style="width: 100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { usePagedTable } from '@/composables/usePagedTable'
import request from '@/utils/request'

const dialogVisible = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  status: '',
})

const createDefaultForm = () => ({
  id: '',
  code: '',
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  level: 'B',
  status: 'ACTIVE',
  remark: '',
})

const form = reactive(createDefaultForm())

const rules = {
  code: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
}

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/supplier/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        keyword: filters.keyword,
        status: filters.status,
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取供应商列表失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.status = ''
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
  const api = form.id ? '/supplier/update' : '/supplier/save'
  const result = await request.post(api, { ...form })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '保存供应商失败')
    return
  }

  ElMessage.success(form.id ? '供应商已更新' : '供应商已创建')
  dialogVisible.value = false
  await loadData()
}

const removeSupplier = async (id) => {
  const result = await request.get('/supplier/del', { params: { id } })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除供应商失败')
    return
  }

  ElMessage.success('供应商已删除')
  await loadData()
}

onMounted(() => {
  loadData()
})
</script>
