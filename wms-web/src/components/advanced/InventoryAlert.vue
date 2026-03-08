<template>
  <div class="ops-shell">
    <div class="ops-grid">
      <article class="metric-card">
        <span class="metric-label">低库存预警</span>
        <strong class="metric-value">{{ overview.lowStockCount }}</strong>
        <span class="metric-hint">低于安全库存的 SKU 数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">高库存预警</span>
        <strong class="metric-value">{{ overview.highStockCount }}</strong>
        <span class="metric-hint">高于库存上限的 SKU 数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">批次过期风险</span>
        <strong class="metric-value">{{ overview.expiringBatchCount }}</strong>
        <span class="metric-hint">需要优先处理的批次数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">在途调拨单</span>
        <strong class="metric-value">{{ overview.activeTransferCount }}</strong>
        <span class="metric-hint">尚未执行完成的仓间流转</span>
      </article>
    </div>

    <div class="data-grid-2">
      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">预警工作台</h3>
            <p class="section-subtitle">集中查看库存不足、库存过多和批次过期风险</p>
          </div>

          <div class="actions">
            <el-button @click="loadPageData">刷新列表</el-button>
            <el-button type="primary" @click="refreshAlerts">重新扫描预警</el-button>
          </div>
        </div>

        <div class="toolbar-row">
          <div class="filters">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索货品、批次或预警内容"
              clearable
              style="width: 260px"
              @keyup.enter="loadPageData"
            />
            <el-select v-model="filters.alertType" placeholder="预警类型" clearable style="width: 160px">
              <el-option label="低库存" value="LOW_STOCK" />
              <el-option label="高库存" value="HIGH_STOCK" />
              <el-option label="即将过期" value="EXPIRY" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
            <el-select v-model="filters.status" placeholder="处理状态" clearable style="width: 160px">
              <el-option label="未读" value="UNREAD" />
              <el-option label="已处理" value="PROCESSED" />
              <el-option label="已解除" value="RESOLVED" />
            </el-select>
          </div>

          <div class="actions">
            <el-button type="primary" @click="loadPageData">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </div>

        <el-table v-loading="loading" :data="tableData" border>
          <el-table-column prop="alertType" label="类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="resolveAlertTag(row.alertType)">{{ resolveAlertLabel(row.alertType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="goodsName" label="货品" min-width="150" />
          <el-table-column prop="storageName" label="仓库" min-width="120" />
          <el-table-column prop="batchNo" label="批次号" min-width="130" />
          <el-table-column prop="content" label="预警内容" min-width="220" show-overflow-tooltip />
          <el-table-column label="当前值 / 阈值" min-width="150" align="center">
            <template #default="{ row }">
              {{ row.currentValue ?? '-' }} / {{ row.thresholdValue ?? '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="170" />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="resolveStatusTag(row.status)">{{ resolveStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                text
                :disabled="row.status === 'PROCESSED' || row.status === 'RESOLVED'"
                @click="markProcessed(row.id)"
              >
                标记处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </section>

      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">通知回执</h3>
            <p class="section-subtitle">系统通知和邮件回执都会记录在这里</p>
          </div>
        </div>

        <div class="feed-list">
          <article v-for="item in notifications" :key="item.id" class="feed-item">
            <div class="feed-meta">
              <strong>{{ item.title }}</strong>
              <el-tag size="small" :type="item.channel === 'EMAIL' ? 'warning' : 'primary'">
                {{ item.channel }}
              </el-tag>
            </div>
            <p>{{ item.content }}</p>
            <div class="feed-foot">
              <span>{{ item.recipient || '系统通知' }}</span>
              <span>{{ item.createdAt || '-' }}</span>
            </div>
          </article>
          <el-empty v-if="notifications.length === 0" description="暂无通知回执" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { usePagedTable } from '@/composables/usePagedTable'
import request from '@/utils/request'

const overview = ref({
  lowStockCount: 0,
  highStockCount: 0,
  expiringBatchCount: 0,
  activeTransferCount: 0,
})

const notifications = ref([])

const filters = reactive({
  keyword: '',
  alertType: '',
  status: '',
})

const { loading, tableData, total, pageNum, pageSize, loadData, handleSizeChange, handleCurrentChange } =
  usePagedTable(async ({ pageNum: currentPage, pageSize: currentSize }) => {
    const result = await request.post('/alert/listPage', {
      pageSize: currentSize,
      pageNum: currentPage,
      param: {
        keyword: filters.keyword,
        alertType: filters.alertType,
        status: filters.status,
      },
    })

    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取预警列表失败')
      return { data: [], total: 0 }
    }

    return result
  })

const handlePageChange = async (value) => {
  await handleCurrentChange(value)
}

const loadOverview = async () => {
  const result = await request.get('/report/overview')
  if (result.code === 200) {
    overview.value = result.data.overview || overview.value
  }
}

const loadNotifications = async () => {
  const result = await request.get('/notification/recent')
  notifications.value = result.code === 200 ? result.data : []
}

const loadPageData = async () => {
  await loadData()
  await Promise.all([loadOverview(), loadNotifications()])
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.alertType = ''
  filters.status = ''
  pageNum.value = 1
  await loadPageData()
}

const refreshAlerts = async () => {
  const result = await request.get('/alert/refresh')
  if (result.code !== 200) {
    ElMessage.error(result.msg || '刷新预警失败')
    return
  }

  ElMessage.success('预警扫描完成')
  await loadPageData()
}

const markProcessed = async (id) => {
  const result = await request.get('/alert/markRead', { params: { id } })
  if (result.code !== 200) {
    ElMessage.error(result.msg || '更新预警失败')
    return
  }

  ElMessage.success('预警已标记为处理中')
  await loadData()
}

const resolveAlertLabel = (value) =>
  ({
    LOW_STOCK: '低库存',
    HIGH_STOCK: '高库存',
    EXPIRY: '即将过期',
    EXPIRED: '已过期',
  }[value] || value)

const resolveAlertTag = (value) =>
  ({
    LOW_STOCK: 'danger',
    HIGH_STOCK: 'warning',
    EXPIRY: '',
    EXPIRED: 'danger',
  }[value] || 'info')

const resolveStatusLabel = (value) =>
  ({
    UNREAD: '未读',
    PROCESSED: '已处理',
    RESOLVED: '已解除',
  }[value] || value)

const resolveStatusTag = (value) =>
  ({
    UNREAD: 'danger',
    PROCESSED: 'success',
    RESOLVED: 'info',
  }[value] || 'info')

onMounted(() => {
  loadPageData()
})
</script>

<style scoped>
.feed-list {
  display: grid;
  gap: 14px;
}

.feed-item {
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid var(--color-border-light);
}

.feed-item p {
  margin: 10px 0 12px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.feed-meta,
.feed-foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.feed-foot {
  color: var(--color-text-muted);
  font-size: 12px;
}
</style>
