<template>
  <div class="page-shell">
    <div class="toolbar-row">
      <div class="filters">
        <el-input
          v-model="keyword"
          :placeholder="searchPlaceholder"
          clearable
          style="width: 240px"
          @keyup.enter="loadData"
        />
      </div>

      <div class="actions">
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" :label="nameLabel" min-width="180" />
      <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除这条记录吗？" @confirm="removeItem(row.id)">
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
      :page-sizes="[5, 10, 20, 30]"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="nameLabel" prop="name">
          <el-input v-model="form.name" :placeholder="searchPlaceholder" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { usePagedTable } from '@/composables/usePagedTable'

const props = defineProps({
  apiBase: {
    type: String,
    required: true,
  },
  pageTitle: {
    type: String,
    required: true,
  },
  nameLabel: {
    type: String,
    default: '名称',
  },
  searchPlaceholder: {
    type: String,
    default: '请输入名称',
  },
})

const keyword = ref('')
const dialogVisible = ref(false)
const formRef = ref()

const createDefaultForm = () => ({
  id: '',
  name: '',
  remark: '',
})

const form = reactive(createDefaultForm())

const rules = {
  name: [{ required: true, message: `请输入${props.nameLabel}`, trigger: 'blur' }],
}

const dialogTitle = computed(() => (form.id ? `编辑${props.pageTitle}` : `新增${props.pageTitle}`))

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post(`${props.apiBase}/listPage`, {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        name: keyword.value,
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || `获取${props.pageTitle}列表失败`)
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const resetFilters = async () => {
  keyword.value = ''
  pageNum.value = 1
  await loadData()
}

const resetForm = () => {
  Object.assign(form, createDefaultForm())
  formRef.value?.clearValidate()
}

const openCreate = async () => {
  dialogVisible.value = true
  resetForm()
  await nextTick()
  formRef.value?.clearValidate()
}

const openEdit = async (row) => {
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    remark: row.remark || '',
  })
  await nextTick()
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  await formRef.value.validate()

  const api = form.id ? `${props.apiBase}/update` : `${props.apiBase}/save`
  const result = await request.post(api, { ...form })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '保存失败')
    return
  }

  ElMessage.success(form.id ? '更新成功' : '创建成功')
  dialogVisible.value = false
  resetForm()
  await loadData()
}

const removeItem = async (id) => {
  const result = await request.get(`${props.apiBase}/del`, {
    params: { id },
  })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除失败')
    return
  }

  ElMessage.success('删除成功')
  await loadData()
}

onMounted(() => {
  loadData()
})
</script>
