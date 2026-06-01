<template>
  <div class="safe-stocks-container">
    <div class="page-header">
      <h2>安全庫存管理</h2>
    </div>
    <div class="filter-bar">
      <span>選擇星期：</span>
      <el-select v-model="selectedWeekday" @change="loadSafeStocks" placeholder="請選擇星期" style="width: 200px">
        <el-option v-for="item in weekdays" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="openAddDialog">新增商品</el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="safeStocks" border>
        <el-table-column prop="productName" label="商品名稱" />
        <el-table-column prop="safeQuantity" label="安全庫存量" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.safeQuantity" :min="0" size="small" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="updateStock(row)">儲存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增對話框 -->
    <el-dialog title="新增安全庫存" v-model="dialogVisible" class="stock-dialog">
      <el-form label-position="top">
        <el-form-item label="商品">
          <el-select v-model="newStock.productId" placeholder="請選擇商品" filterable style="width: 100%">
            <el-option v-for="p in allProducts" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="安全庫存量">
          <el-input-number v-model="newStock.safeQuantity" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addSafeStock">確定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '@/api/axios';

const weekdays = [
  { label: '星期一', value: 1 },
  { label: '星期二', value: 2 },
  { label: '星期三', value: 3 },
  { label: '星期四', value: 4 },
  { label: '星期五', value: 5 },
  { label: '星期六', value: 6 },
  { label: '星期日', value: 7 },
];

const selectedWeekday = ref(1);
const safeStocks = ref([]);
const allProducts = ref([]);
const dialogVisible = ref(false);
const newStock = ref({ productId: '', safeQuantity: 0 });

const loadAllProducts = async () => {
  try {
    const res = await api.get('/admin/products');
    allProducts.value = res.data;
  } catch {
    ElMessage.error('載入商品失敗');
  }
};

const loadSafeStocks = async () => {
  try {
    const res = await api.get('/admin/safe-stocks', { params: { dayOfWeek: selectedWeekday.value } });
    const enriched = res.data.map((stock) => {
      const product = allProducts.value.find((p) => p.id === stock.productId);
      return { ...stock, productName: product ? product.name : stock.productId };
    });
    safeStocks.value = enriched;
  } catch {
    ElMessage.error('載入安全庫存失敗');
  }
};

const updateStock = async (row) => {
  try {
    await api.put('/admin/safe-stocks', {
      productId: row.productId,
      dayOfWeek: selectedWeekday.value,
      safeQuantity: row.safeQuantity,
      version: row.version,
    });
    ElMessage.success('更新成功');
    await loadSafeStocks();
  } catch {
    ElMessage.error('更新失敗');
  }
};

const openAddDialog = () => {
  newStock.value = { productId: '', safeQuantity: 0 };
  dialogVisible.value = true;
};

const addSafeStock = async () => {
  if (!newStock.value.productId) {
    ElMessage.warning('請選擇商品');
    return;
  }
  if (safeStocks.value.some((s) => s.productId === newStock.value.productId)) {
    ElMessage.warning('該商品在本週已設定安全庫存，請直接修改數量並按「儲存」');
    return;
  }
  try {
    await api.put('/admin/safe-stocks', {
      productId: newStock.value.productId,
      dayOfWeek: selectedWeekday.value,
      safeQuantity: newStock.value.safeQuantity,
      version: 0,
    });
    ElMessage.success('新增成功');
    dialogVisible.value = false;
    await loadSafeStocks();
  } catch {
    ElMessage.error('新增失敗');
  }
};

onMounted(() => {
  loadAllProducts().then(() => loadSafeStocks());
});
</script>

<style scoped>
.safe-stocks-container {
  padding: 20px;
}
.page-header h2 {
  margin-top: 0;
  margin-bottom: 16px;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.table-wrapper {
  overflow-x: auto;
}
.stock-dialog :deep(.el-dialog) {
  width: 90%;
  max-width: 500px;
  border-radius: 16px;
}
@media (max-width: 768px) {
  .safe-stocks-container {
    padding: 12px;
  }
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>