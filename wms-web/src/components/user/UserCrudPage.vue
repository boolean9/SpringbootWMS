<template>
  <div class="page-shell">
    <div class="toolbar-row">
      <div class="filters">
        <el-input
          v-model="filters.name"
          placeholder="请输入姓名"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <el-select
          v-model="filters.sex"
          placeholder="请选择性别"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="option in sexOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="actions">
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button v-if="showActions" type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      highlight-current-row
      @current-change="handleCurrentChangeRow"
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="no" label="账号" min-width="140" />
      <el-table-column prop="name" label="姓名" min-width="140" />
      <el-table-column prop="age" label="年龄" width="90" align="center" />
      <el-table-column prop="sex" label="性别" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="Number(row.sex) === 1 ? 'primary' : 'success'">
            {{ Number(row.sex) === 1 ? '男' : '女' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roleId" label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveRoleType(row.roleId)">
            {{ resolveRoleName(row.roleId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" min-width="150" />
      <el-table-column v-if="showActions" label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除这条记录吗？" @confirm="removeUser(row.id)">
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

    <el-dialog
      v-if="showActions"
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="no">
          <el-input v-model="form.no" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="form.age" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
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
  pageTitle: {
    type: String,
    default: '用户管理',
  },
  roleId: {
    type: [String, Number],
    default: '2',
  },
  selectMode: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['doSelectUser'])

const sexOptions = [
  { label: '男', value: '1' },
  { label: '女', value: '0' },
]

const filters = reactive({
  name: '',
  sex: '',
})

const dialogVisible = ref(false)
const formRef = ref()

const createDefaultForm = () => ({
  id: '',
  no: '',
  name: '',
  password: '',
  age: '',
  phone: '',
  sex: '0',
  roleId: String(props.roleId),
})

const form = reactive(createDefaultForm())

const showActions = computed(() => !props.selectMode)
const dialogTitle = computed(() => (form.id ? `编辑${props.pageTitle}` : `新增${props.pageTitle}`))

const validateAge = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }

  if (Number(value) > 150) {
    callback(new Error('年龄不能大于 150'))
    return
  }

  callback()
}

const validateDuplicate = async (rule, value, callback) => {
  if (!value || form.id) {
    callback()
    return
  }

  try {
    const result = await request.get('/user/findByNo', {
      params: {
        no: value,
      },
    })

    if (result.code === 200) {
      callback(new Error('账号已经存在'))
      return
    }

    callback()
  } catch (error) {
    callback()
  }
}

const rules = {
  no: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 8, message: '账号长度需要在 3 到 8 位之间', trigger: 'blur' },
    { validator: validateDuplicate, trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 8, message: '密码长度需要在 3 到 8 位之间', trigger: 'blur' },
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { pattern: /^([1-9][0-9]*){1,3}$/, message: '年龄必须为正整数', trigger: 'blur' },
    { validator: validateAge, trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9][0-9]\d{8}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/user/listPageC1', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        name: filters.name,
        sex: filters.sex,
        roleId: String(props.roleId),
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取用户列表失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const handleCurrentChangeRow = (row) => {
  if (props.selectMode && row) {
    emit('doSelectUser', row)
  }
}

const resetForm = () => {
  Object.assign(form, createDefaultForm())
  formRef.value?.clearValidate()
}

const resetFilters = async () => {
  filters.name = ''
  filters.sex = ''
  pageNum.value = 1
  await loadData()
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
    no: row.no,
    name: row.name,
    password: '',
    age: String(row.age ?? ''),
    phone: row.phone,
    sex: String(row.sex ?? '0'),
    roleId: String(row.roleId ?? props.roleId),
  })
  await nextTick()
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  await formRef.value.validate()

  const api = form.id ? '/user/update' : '/user/save'
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

const removeUser = async (id) => {
  const result = await request.get('/user/del', {
    params: { id },
  })

  if (result.code !== 200) {
    ElMessage.error(result.msg || '删除失败')
    return
  }

  ElMessage.success('删除成功')
  await loadData()
}

const resolveRoleName = (roleId) => {
  switch (Number(roleId)) {
    case 0:
      return '超级管理员'
    case 1:
      return '管理员'
    default:
      return '用户'
  }
}

const resolveRoleType = (roleId) => {
  switch (Number(roleId)) {
    case 0:
      return 'danger'
    case 1:
      return 'warning'
    default:
      return 'success'
  }
}

onMounted(() => {
  loadData()
})
</script>
