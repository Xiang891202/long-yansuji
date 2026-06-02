<template>
  <div class="statistics-container">
    <h2>📊 叫貨統計報表</h2>
    <div class="filter-bar">
      <div class="date-range-group">
        <div class="date-item">
          <label>開始日期</label>
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="選擇開始日期"
            value-format="YYYY-MM-DD"
            @change="loadStatistics"
            class="mobile-date-picker"
          />
        </div>
        <span class="date-separator">至</span>
        <div class="date-item">
          <label>結束日期</label>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="選擇結束日期"
            value-format="YYYY-MM-DD"
            @change="loadStatistics"
            class="mobile-date-picker"
          />
        </div>
      </div>
    </div>

    <!-- 表格展示（不帶圖表） -->
    <el-table :data="statistics" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="productName" label="商品名稱" min-width="120" />
      <el-table-column prop="totalQuantity" label="補貨總數量" sortable width="110" />
      <el-table-column prop="count" label="叫貨次數" sortable width="100" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import api from '@/api/axios';

const startDate = ref('');
const endDate = ref('');
const statistics = ref([]);
const loading = ref(false);

// 設定預設日期區間（近7天）
const getDefaultDateRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 7);
  return {
    start: start.toISOString().slice(0, 10),
    end: end.toISOString().slice(0, 10)
  };
};

// 載入統計資料
const loadStatistics = async () => {
  if (!startDate.value || !endDate.value) {
    ElMessage.warning('請選擇完整的日期區間');
    return;
  }
  if (startDate.value > endDate.value) {
    ElMessage.warning('開始日期不能晚於結束日期');
    return;
  }
  loading.value = true;
  try {
    const res = await api.get('/admin/statistics/replenishment', {
      params: { startDate: startDate.value, endDate: endDate.value }
    });
    statistics.value = res.data;
  } catch (err) {
    console.error(err);
    ElMessage.error('載入統計資料失敗');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const { start, end } = getDefaultDateRange();
  startDate.value = start;
  endDate.value = end;
  loadStatistics();
});
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background: #f9fafb;
  min-height: 100vh;
}

h2 {
  margin: 0 0 20px 0;
  font-size: 1.5rem;
  color: #2c3e50;
}

.filter-bar {
  background: white;
  padding: 16px;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.date-range-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.date-item {
  flex: 1;
  min-width: 130px;
}

.date-item label {
  display: block;
  font-size: 0.8rem;
  color: #6c757d;
  margin-bottom: 4px;
}

.mobile-date-picker {
  width: 100%;
}

.date-separator {
  color: #6c757d;
  font-weight: 500;
  padding: 0 4px;
}

/* 手机版优化 */
@media (max-width: 768px) {
  .statistics-container {
    padding: 12px;
  }
  .date-range-group {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  .date-item {
    width: 100%;
  }
  .date-separator {
    text-align: center;
    display: block;
  }
}
</style>