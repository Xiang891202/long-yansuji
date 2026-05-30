<template>
  <div class="safe-stocks-container">
    <h2>安全庫存管理</h2>
    <div style="margin-bottom: 16px">
      <span>選擇星期：</span>
      <el-select v-model="selectedWeekday" placeholder="請選擇星期" @change="loadSafeStocks" style="width: 200px">
        <el-option v-for="item in weekdays" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="openAddDialog" style="margin-left: 16px">新增商品</el-button>
    </div>

    <el-table :data="safeStocks" border style="width: 100%">
      <el-table-column prop="productName" label="商品名稱" />
      <el-table-column prop="safeQuantity" label="安全庫存量" width="180">
        <template #default="{ row }">
          <el-input-number v-model="row.safeQuantity" :min="0" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="updateStock(row)">儲存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增商品對話框 -->
    <el-dialog title="新增安全庫存" v-model="dialogVisible" width="30%">
      <el-form label-width="100px">
        <el-form-item label="商品">
          <el-select v-model="newStock.productId" placeholder="請選擇商品" filterable>
            <el-option v-for="p in allProducts" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="安全庫存量">
          <el-input-number v-model="newStock.safeQuantity" :min="0" />
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
const safeStocks = ref([]);         // 當前星期安全庫存列表，每項包含 productId, productName, safeQuantity, version
const allProducts = ref([]);        // 所有商品（用於新增對話框）
const dialogVisible = ref(false);
const newStock = ref({ productId: '', safeQuantity: 0 });

// 載入所有商品（用於新增時選擇）
const loadAllProducts = async () => {
  try {
    const res = await api.get('/admin/products');
    allProducts.value = res.data;
  } catch (err) {
    console.error(err);
    ElMessage.error('載入商品失敗');
  }
};

// 載入指定星期的安全庫存
const loadSafeStocks = async () => {
  try {
    const res = await api.get('/admin/safe-stocks', { params: { dayOfWeek: selectedWeekday.value } });
    // 後端回傳的陣列包含 productId, safeQuantity, version, 但沒有商品名稱，需補上名稱
    const stocks = res.data;
    // 從商品列表補上名稱 (假設 allProducts 已載入)
    const enriched = stocks.map(stock => {
      const product = allProducts.value.find(p => p.id === stock.productId);
      return {
        ...stock,
        productName: product ? product.name : stock.productId,
      };
    });
    safeStocks.value = enriched;
  } catch (err) {
    console.error(err);
    ElMessage.error('載入安全庫存失敗');
  }
};

// 更新單一商品的庫存
const updateStock = async (row) => {
  try {
    const payload = {
      productId: row.productId,
      dayOfWeek: selectedWeekday.value,
      safeQuantity: row.safeQuantity,
      version: row.version,
    };
    await api.put('/admin/safe-stocks', payload);
    ElMessage.success('更新成功');
    // 重新載入以取得最新版本
    await loadSafeStocks();
  } catch (err) {
    console.error(err);
    ElMessage.error(err.response?.data?.message || '更新失敗');
  }
};

// 開啟新增對話框
const openAddDialog = () => {
  newStock.value = { productId: '', safeQuantity: 0 };
  dialogVisible.value = true;
};

// 新增安全庫存（若已有該星期該商品的記錄，後端會自動覆蓋？根據設計是 PUT 時若無則新增）
const addSafeStock = async () => {
  if (!newStock.value.productId) {
    ElMessage.warning('請選擇商品');
    return;
  }
  // 檢查是否已存在
  const exists = safeStocks.value.some(s => s.productId === newStock.value.productId);
  if (exists) {
    ElMessage.warning('該商品在本週已設定安全庫存，請直接修改數量並按「儲存」');
    return;
  }
  try {
    const payload = {
      productId: newStock.value.productId,
      dayOfWeek: selectedWeekday.value,
      safeQuantity: newStock.value.safeQuantity,
      version: 0,
    };
    await api.put('/admin/safe-stocks', payload);
    ElMessage.success('新增成功');
    dialogVisible.value = false;
    await loadSafeStocks();
  } catch (err) {
    console.error(err);
    ElMessage.error(err.response?.data?.message || '新增失敗');
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
</style>