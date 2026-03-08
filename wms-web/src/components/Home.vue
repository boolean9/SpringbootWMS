<template>
  <div class="ops-shell">
    <section class="section-card hero-panel">
      <div>
        <p class="hero-kicker">WMS CONTROL ROOM</p>
        <h1>欢迎回来，{{ currentUser.name || '管理员' }}</h1>
        <p class="hero-copy">
          这里汇总了库存预警、出入库趋势、通知回执和待执行调拨，你可以在一个视图里快速判断仓储运行状态。
        </p>
      </div>

      <div class="hero-actions">
        <el-button @click="loadDashboard">刷新概览</el-button>
        <el-button type="primary" @click="goReports">查看报表</el-button>
      </div>
    </section>

    <div class="ops-grid">
      <article class="metric-card">
        <span class="metric-label">货品 SKU</span>
        <strong class="metric-value">{{ overview.goodsSkuCount }}</strong>
        <span class="metric-hint">当前系统在管货品种类</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">总库存数量</span>
        <strong class="metric-value">{{ overview.totalStockCount }}</strong>
        <span class="metric-hint">所有仓库库存合计</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">低库存风险</span>
        <strong class="metric-value">{{ overview.lowStockCount }}</strong>
        <span class="metric-hint">需要补货的品项数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">待执行调拨</span>
        <strong class="metric-value">{{ overview.activeTransferCount }}</strong>
        <span class="metric-hint">待处理的跨仓协同任务</span>
      </article>
    </div>

    <div class="data-grid-2">
      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">近 7 天出入库趋势</h3>
            <p class="section-subtitle">蓝线为入库，橙线为出库</p>
          </div>
        </div>

        <div class="chart-surface">
          <svg viewBox="0 0 760 240" preserveAspectRatio="none">
            <polyline
              :points="inPoints"
              fill="none"
              stroke="#2563eb"
              stroke-width="4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <polyline
              :points="outPoints"
              fill="none"
              stroke="#f97316"
              stroke-width="4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>

        <div class="trend-axis">
          <span v-for="item in trendData" :key="item.day">{{ item.day }}</span>
        </div>
      </section>

      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">风险预警</h3>
            <p class="section-subtitle">最近触发的库存和批次告警</p>
          </div>
        </div>

        <div class="feed-list">
          <article v-for="item in alerts" :key="item.id" class="feed-item">
            <div class="feed-meta">
              <strong>{{ item.goodsName }}</strong>
              <el-tag size="small" :type="item.alertType === 'LOW_STOCK' ? 'danger' : 'warning'">
                {{ item.alertType }}
              </el-tag>
            </div>
            <p>{{ item.content }}</p>
            <div class="feed-foot">
              <span>{{ item.storageName || '-' }}</span>
              <span>{{ item.createdAt || '-' }}</span>
            </div>
          </article>
          <el-empty v-if="alerts.length === 0" description="暂无预警" />
        </div>
      </section>
    </div>

    <section class="section-card">
      <div class="section-header">
        <div>
          <h3 class="section-title">通知回执</h3>
          <p class="section-subtitle">系统消息与邮件通知状态</p>
        </div>
      </div>

      <el-table :data="notifications" border>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="channel" label="通道" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.channel === 'EMAIL' ? 'warning' : 'primary'">{{ row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recipient" label="接收方" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" align="center" />
        <el-table-column prop="createdAt" label="时间" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/session'

const router = useRouter()
const currentUser = computed(() => getCurrentUser() || {})

const overview = ref({
  goodsSkuCount: 0,
  totalStockCount: 0,
  lowStockCount: 0,
  highStockCount: 0,
  expiringBatchCount: 0,
  activeTransferCount: 0,
})
const trendData = ref([])
const alerts = ref([])
const notifications = ref([])

const maxTrendValue = computed(() => {
  const values = trendData.value.flatMap((item) => [Number(item.inboundCount || 0), Number(item.outboundCount || 0)])
  return Math.max(...values, 1)
})

const buildPoints = (key) => {
  if (!trendData.value.length) {
    return ''
  }

  return trendData.value
    .map((item, index) => {
      const x = trendData.value.length === 1 ? 380 : (760 / (trendData.value.length - 1)) * index
      const value = Number(item[key] || 0)
      const y = 220 - (value / maxTrendValue.value) * 180
      return `${x},${y}`
    })
    .join(' ')
}

const inPoints = computed(() => buildPoints('inboundCount'))
const outPoints = computed(() => buildPoints('outboundCount'))

const loadDashboard = async () => {
  const [overviewResult, trendResult, alertResult, notificationResult] = await Promise.all([
    request.get('/report/overview'),
    request.get('/report/trend', { params: { days: 7 } }),
    request.get('/alert/recent'),
    request.get('/notification/recent'),
  ])

  if (overviewResult.code === 200) {
    overview.value = overviewResult.data.overview || overview.value
  }
  trendData.value = trendResult.code === 200 ? trendResult.data : []
  alerts.value = alertResult.code === 200 ? alertResult.data : []
  notifications.value = notificationResult.code === 200 ? notificationResult.data : []
}

const goReports = () => {
  router.push('/Reports')
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.hero-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  background: linear-gradient(135deg, #0f172a, #1d4ed8 72%, #38bdf8);
  color: #fff;
}

.hero-kicker {
  font-size: 12px;
  letter-spacing: 0.18em;
  opacity: 0.78;
}

.hero-panel h1 {
  margin: 12px 0;
  font-size: 34px;
}

.hero-copy {
  max-width: 700px;
  color: rgba(255, 255, 255, 0.82);
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.trend-axis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  gap: 8px;
  margin-top: 12px;
  color: var(--color-text-muted);
  font-size: 12px;
}

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

@media (max-width: 900px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
