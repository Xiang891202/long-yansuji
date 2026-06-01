<template>
  <div class="statistics-container">
    <h2>叫貨統計報表</h2>
    <div class="filter-bar">
      <span>日期區間：</span>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="開始日期"
        end-placeholder="結束日期"
        value-format="YYYY-MM-DD"
        @change="loadStatistics"
      />
    </div>

    <!-- 表格展示 -->
    <el-table :data="statistics" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="productName" label="商品名稱" />
      <el-table-column prop="totalQuantity" label="補貨總數量" sortable />
      <el-table-column prop="count" label="叫貨次數" sortable />
    </el-table>

    <!-- 圖表展示（使用 ECharts） -->
    <div v-if="statistics.length" style="margin-top: 30px">
      <h3>補貨數量排行（長條圖）</h3>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import api from '@/api/axios';

const dateRange = ref([]);
const statistics = ref([]);
const loading = ref(false);
const chartRef = ref(null);
let chart = null;

// 設定預設日期區間（近7天）
const getDefaultDateRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 7);
  return [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)];
};

// 載入統計資料
const loadStatistics = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('請選擇日期區間');
    return;
  }
  loading.value = true;
  try {
    const [startDate, endDate] = dateRange.value;
    const res = await api.get('/admin/statistics/replenishment', {
      params: { startDate, endDate }
    });
    statistics.value = res.data;
    await nextTick();
    renderChart();
  } catch (err) {
    console.error(err);
    ElMessage.error('載入統計資料失敗');
  } finally {
    loading.value = false;
  }
};

// 渲染 ECharts 圖表
const renderChart = () => {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  const products = statistics.value.map(item => item.productName);
  const quantities = statistics.value.map(item => item.totalQuantity);
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: products, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '補貨總數量' },
    series: [{ type: 'bar', data: quantities, itemStyle: { color: '#42b983' } }]
  });
};

// 視窗大小改變時重新調整圖表大小
window.addEventListener('resize', () => {
  if (chart) chart.resize();
});

onMounted(() => {
  // 若未設定日期範圍，使用預設值
  if (!dateRange.value.length) {
    const [start, end] = getDefaultDateRange();
    dateRange.value = [start, end];
    loadStatistics();
  } else {
    loadStatistics();
  }
});
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 12px;
  }
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>