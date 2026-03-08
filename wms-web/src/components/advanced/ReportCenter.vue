<template>
  <div class="ops-shell">
    <div class="section-card report-hero">
      <div>
        <p class="hero-kicker">ANALYTICS HUB</p>
        <h2 class="hero-title">仓储指标洞察中心</h2>
        <p class="hero-copy">统一查看库存体量、周转效率、销售出库以及调拨运行状态，并支持 Excel / PDF 导出。</p>
      </div>

      <div class="actions">
        <el-select v-model="days" style="width: 140px" @change="loadTrend">
          <el-option label="近 7 天" :value="7" />
          <el-option label="近 14 天" :value="14" />
          <el-option label="近 30 天" :value="30" />
        </el-select>
        <el-button @click="loadDashboard">刷新报表</el-button>
        <el-button type="primary" @click="exportReport('excel')">导出 Excel</el-button>
        <el-button type="success" @click="exportReport('pdf')">导出 PDF</el-button>
      </div>
    </div>

    <div class="ops-grid">
      <article class="metric-card">
        <span class="metric-label">货品 SKU</span>
        <strong class="metric-value">{{ overview.goodsSkuCount }}</strong>
        <span class="metric-hint">当前系统在管货品种类数</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">总库存数量</span>
        <strong class="metric-value">{{ overview.totalStockCount }}</strong>
        <span class="metric-hint">所有仓库合计库存量</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">低库存风险</span>
        <strong class="metric-value">{{ overview.lowStockCount }}</strong>
        <span class="metric-hint">需要优先补货的品项</span>
      </article>
      <article class="metric-card">
        <span class="metric-label">在途调拨</span>
        <strong class="metric-value">{{ overview.activeTransferCount }}</strong>
        <span class="metric-hint">等待执行或处理中调拨单</span>
      </article>
    </div>

    <div class="data-grid-2">
      <section class="section-card">
        <div class="section-header">
          <div>
            <h3 class="section-title">出入库趋势</h3>
            <p class="section-subtitle">蓝线代表入库，橙线代表出库</p>
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
            <h3 class="section-title">销售出库排行</h3>
            <p class="section-subtitle">按普通出库量统计的货品排名</p>
          </div>
        </div>

        <div class="ranking-list">
          <article v-for="(item, index) in salesData.slice(0, 8)" :key="item.goodsId" class="ranking-item">
            <span class="ranking-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <div class="ranking-meta">
              <strong>{{ item.goodsName }}</strong>
              <span>出库量 {{ item.saleCount }}</span>
            </div>
          </article>
          <el-empty v-if="salesData.length === 0" description="暂无销售数据" />
        </div>
      </section>
    </div>

    <section class="section-card">
      <div class="section-header">
        <div>
          <h3 class="section-title">库存周转率</h3>
          <p class="section-subtitle">根据出库量与当前库存估算周转效率</p>
        </div>
      </div>

      <el-table :data="turnoverData" border>
        <el-table-column prop="goodsName" label="货品名称" min-width="180" />
        <el-table-column prop="outboundCount" label="出库量" width="120" align="center" />
        <el-table-column prop="currentStock" label="当前库存" width="120" align="center" />
        <el-table-column prop="turnoverRate" label="周转率" width="120" align="center" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiBaseURL } from '@/utils/request'
import request from '@/utils/request'

const days = ref(14)
const overview = ref({
  goodsSkuCount: 0,
  totalStockCount: 0,
  lowStockCount: 0,
  highStockCount: 0,
  expiringBatchCount: 0,
  activeTransferCount: 0,
})
const trendData = ref([])
const turnoverData = ref([])
const salesData = ref([])

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
  const result = await request.get('/report/overview')
  if (result.code !== 200) {
    ElMessage.error(result.msg || '获取报表概览失败')
    return
  }

  overview.value = result.data.overview || overview.value
  turnoverData.value = result.data.turnover || []
  salesData.value = result.data.sales || []

  if (days.value === 7) {
    trendData.value = result.data.trend || []
  } else {
    await loadTrend()
  }
}

const loadTrend = async () => {
  const result = await request.get('/report/trend', {
    params: { days: days.value },
  })

  trendData.value = result.code === 200 ? result.data : []
}

const exportReport = (type) => {
  window.open(`${apiBaseURL}/report/export/${type}`, '_blank')
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.report-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  background: linear-gradient(135deg, #0f172a, #1d4ed8 72%, #38bdf8);
  color: #fff;
}

.hero-kicker {
  font-size: 12px;
  letter-spacing: 0.18em;
  opacity: 0.75;
}

.hero-title {
  font-size: 34px;
  margin: 12px 0 10px;
}

.hero-copy {
  max-width: 640px;
  color: rgba(255, 255, 255, 0.82);
}

.trend-axis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  gap: 8px;
  margin-top: 10px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.ranking-list {
  display: grid;
  gap: 12px;
}

.ranking-item {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid var(--color-border-light);
}

.ranking-index {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #e0f2fe;
  color: #0369a1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.ranking-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ranking-meta span {
  color: var(--color-text-muted);
  font-size: 12px;
}

@media (max-width: 900px) {
  .report-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
