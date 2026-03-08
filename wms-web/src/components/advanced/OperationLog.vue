<template>
  <div class="page-shell">
    <div class="section-header">
      <div>
        <h3 class="section-title">用户行为日志</h3>
        <p class="section-subtitle">自动记录关键接口访问，支持审计、追责和异常回放。</p>
      </div>

      <div class="actions">
        <el-button @click="loadData">刷新</el-button>
      </div>
    </div>

    <div class="toolbar-row">
      <div class="filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索操作人、接口路径或返回信息"
          clearable
          style="width: 260px"
          @keyup.enter="loadData"
        />
        <el-select v-model="filters.moduleName" placeholder="模块" clearable style="width: 150px">
          <el-option v-for="item in moduleOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.success" placeholder="结果" clearable style="width: 140px">
          <el-option label="成功" value="true" />
          <el-option label="失败" value="false" />
        </el-select>
      </div>

      <div class="actions">
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="operatorName" label="操作人" min-width="120" />
      <el-table-column prop="operatorNo" label="账号" min-width="120" />
      <el-table-column prop="moduleName" label="模块" min-width="110" />
      <el-table-column prop="actionName" label="动作" min-width="110" />
      <el-table-column prop="requestMethod" label="方法" width="90" align="center" />
      <el-table-column prop="requestPath" label="接口路径" min-width="220" show-overflow-tooltip />
      <el-table-column prop="message" label="结果信息" min-width="170" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP" min-width="120" />
      <el-table-column prop="createTime" label="时间" min-width="170" />
      <el-table-column prop="success" label="结果" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.success ? 'success' : 'danger'">
            {{ row.success ? '成功' : '失败' }}
          </el-tag>
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
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { usePagedTable } from '@/composables/usePagedTable'
import request from '@/utils/request'

const filters = reactive({
  keyword: '',
  moduleName: '',
  success: '',
})

const moduleOptions = [
  'User',
  'Goods',
  'Record',
  'Alert',
  'Transfer',
  'Batch',
  'Supplier',
  'Report',
  'Scan',
  'OperationLog',
]

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/operationLog/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        keyword: filters.keyword,
        moduleName: filters.moduleName,
        success: filters.success,
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取日志失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.moduleName = ''
  filters.success = ''
  pageNum.value = 1
  await loadData()
}

onMounted(() => {
  loadData()
})
</script>
