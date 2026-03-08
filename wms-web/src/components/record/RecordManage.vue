<template>
  <div class="page-shell">
    <div class="section-header">
      <div>
        <h3 class="section-title">出入库记录</h3>
        <p class="section-subtitle">支持按类型、批次、仓库和分类追踪库存流转明细。</p>
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
        <el-select v-model="filters.actionType" placeholder="类型" clearable style="width: 150px">
          <el-option label="入库" value="INBOUND" />
          <el-option label="出库" value="OUTBOUND" />
          <el-option label="调拨入" value="TRANSFER_IN" />
          <el-option label="调拨出" value="TRANSFER_OUT" />
        </el-select>
        <el-input
          v-model="filters.batchNo"
          placeholder="搜索批次号"
          clearable
          style="width: 180px"
          @keyup.enter="loadData"
        />
      </div>

      <div class="actions">
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="goodsname" label="货品" min-width="150" />
      <el-table-column prop="storagename" label="仓库" min-width="130" />
      <el-table-column prop="goodstypename" label="分类" min-width="130" />
      <el-table-column prop="batchNo" label="批次" min-width="120" />
      <el-table-column prop="supplierName" label="供应商" min-width="150" />
      <el-table-column prop="actionType" label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveActionTag(row.actionType)">{{ resolveActionLabel(row.actionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminname" label="操作人" min-width="120" />
      <el-table-column prop="username" label="申请人" min-width="120" />
      <el-table-column prop="count" label="数量" width="90" align="center" />
      <el-table-column prop="scanCode" label="扫码值" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createtime" label="时间" min-width="170" />
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
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
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { usePagedTable } from '@/composables/usePagedTable'
import { useLookupData } from '@/composables/useLookupData'
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/session'

const currentUser = computed(() => getCurrentUser() || {})

const filters = reactive({
  name: '',
  storage: '',
  goodstype: '',
  actionType: '',
  batchNo: '',
})

const { storageOptions, goodstypeOptions, loadBaseLookups } = useLookupData()

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/record/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        name: filters.name,
        storage: filters.storage ? String(filters.storage) : '',
        goodstype: filters.goodstype ? String(filters.goodstype) : '',
        actionType: filters.actionType,
        batchNo: filters.batchNo,
        roleId: String(currentUser.value.roleId || ''),
        userId: String(currentUser.value.id || ''),
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取记录失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const resetFilters = async () => {
  filters.name = ''
  filters.storage = ''
  filters.goodstype = ''
  filters.actionType = ''
  filters.batchNo = ''
  pageNum.value = 1
  await loadData()
}

const resolveActionLabel = (value) =>
  ({
    INBOUND: '入库',
    OUTBOUND: '出库',
    TRANSFER_IN: '调拨入',
    TRANSFER_OUT: '调拨出',
  }[value] || value)

const resolveActionTag = (value) =>
  ({
    INBOUND: 'success',
    OUTBOUND: 'warning',
    TRANSFER_IN: 'primary',
    TRANSFER_OUT: 'danger',
  }[value] || 'info')

onMounted(async () => {
  await loadBaseLookups()
  await loadData()
})
</script>
