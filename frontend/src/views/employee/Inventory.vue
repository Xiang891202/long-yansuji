<template>
  <div class="inventory-container">
    <h2>員工點貨回報</h2>

    <!-- 星期選擇器 -->
    <div class="weekday-selector">
      <span>選擇星期：</span>
      <el-select v-model="selectedWeekday" @change="loadProducts" placeholder="請選擇星期" style="width: 160px">
        <el-option v-for="item in weekdays" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <!-- 商品列表表格 -->
    <el-table :data="products" border style="margin-top: 20px">
      <el-table-column prop="name" label="商品名稱" />
      <el-table-column prop="safeQuantity" label="安全庫存" width="100" />
      <el-table-column label="目前庫存" width="160">
        <template #default="{ row }">
          <el-input-number v-model="quantities[row.id]" :min="0" size="small" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 蔬菜區 -->
    <div class="vegetable-section">
      <h3>蔬菜清單（點擊切換保留/移除）</h3>
      <div class="vegetable-tags">
        <el-tag
          v-for="veg in vegetableOptions"
          :key="veg"
          :type="removedVegetables.includes(veg) ? 'info' : 'success'"
          :class="{ 'removed-tag': removedVegetables.includes(veg) }"
          @click="toggleVegetable(veg)"
          effect="plain"
        >
          {{ veg }} {{ removedVegetables.includes(veg) ? '(移除)' : '(保留)' }}
        </el-tag>
      </div>
    </div>

    <!-- 提交按鈕 -->
    <el-button type="primary" @click="calculate" :loading="calculating" style="margin-top: 20px">
      計算並產出叫貨文字
    </el-button>

    <!-- 結果對話框 -->
    <el-dialog title="叫貨清單" v-model="resultDialogVisible" width="50%">
      <h4>廠商叫貨</h4>
      <pre class="result-text">{{ supplierText }}</pre>
      <el-button @click="copyText(supplierText)">📋 複製叫貨清單</el-button>

      <h4 style="margin-top: 20px">蔬菜清單</h4>
      <pre class="result-text">{{ vegetableText }}</pre>
      <el-button @click="copyText(vegetableText)">🥬 複製蔬菜清單</el-button>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '@/api/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const weekdays = [
  { label: '星期一', value: 1 },
  { label: '星期二', value: 2 },
  { label: '星期三', value: 3 },
  { label: '星期四', value: 4 },
  { label: '星期五', value: 5 },
  { label: '星期六', value: 6 },
  { label: '星期日', value: 7 },
];
const selectedWeekday = ref(new Date().getDay() || 7); // 星期日為7
const products = ref([]);
const quantities = ref({});
const vegetableOptions = ref([]);        // 改為響應式陣列
const removedVegetables = ref([]);       // 保留移除的蔬菜名稱
const calculating = ref(false);
const resultDialogVisible = ref(false);
const supplierText = ref('');
const vegetableText = ref('');

// 載入商品列表（有安全庫存且上架中的商品）
const loadProducts = async () => {
  try {
    const res = await api.get('/inventory/products', {
      params: { dayOfWeek: selectedWeekday.value }
    });
    products.value = res.data;
    // 初始化 quantities 物件
    const initQty = {};
    products.value.forEach(p => {
      initQty[p.id] = 0;
    });
    quantities.value = initQty;
  } catch (err) {
    console.error(err);
    ElMessage.error('載入商品失敗，請確認後端 /inventory/products 接口已實作');
    // 模擬測試資料（後端未完成時可暫時使用）
    // products.value = [
    //   { id: '1', name: '雞排', safeQuantity: 20, version: 1, unit: '包' },
    //   { id: '2', name: '甜不辣', safeQuantity: 15, version: 1, unit: '份' }
    // ];
  }
};

// 載入蔬菜清單
const loadVegetables = async () => {
  try {
    const res = await api.get('/public/vegetables');
    vegetableOptions.value = res.data.map(v => v.name);
    // 初始化 removedVegetables 為空（即全部保留）
    removedVegetables.value = [];
  } catch (err) {
    console.error('載入蔬菜清單失敗', err);
    // 降級使用預設清單
    vegetableOptions.value = ['玉米', '高麗菜', '花椰菜', '青椒', '洋蔥', '九層塔'];
  }
};

// 切換蔬菜保留/移除
const toggleVegetable = (veg) => {
  const idx = removedVegetables.value.indexOf(veg);
  if (idx === -1) {
    removedVegetables.value.push(veg);
  } else {
    removedVegetables.value.splice(idx, 1);
  }
};

// 計算叫貨清單
const calculate = async () => {
  // 檢查是否至少有一個商品被輸入數量（可選）
  const items = products.value.map(p => ({
    productId: p.id,
    currentQuantity: quantities.value[p.id] || 0,
    safeStockVersion: p.version
  }));
  const keptVegetables = vegetableOptions.filter(v => !removedVegetables.value.includes(v));

  if (items.length === 0) {
    ElMessage.warning('無商品資料');
    return;
  }

  calculating.value = true;
  const idempotencyKey = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString();
  try {
    const payload = {
      dayOfWeek: selectedWeekday.value,
      items,
      vegetables: keptVegetables
    };
    const res = await api.post('/inventory/calculate', payload, {
      headers: { 'Idempotency-Key': idempotencyKey }
    });
    supplierText.value = res.data.supplierText;
    vegetableText.value = res.data.vegetableText;
    resultDialogVisible.value = true;
  } catch (err) {
    console.error(err);
    ElMessage.error(err.response?.data?.message || '計算失敗，請稍後重試');
  } finally {
    calculating.value = false;
  }
};

// 複製文字到剪貼簿
const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success('已複製到剪貼簿');
  } catch (err) {
    ElMessage.error('複製失敗');
  }
};

// 權限檢查：若無 token 或無 inventory_access 權限，導回登入頁
onMounted(() => {
  const token = localStorage.getItem('token');
  if (!token) {
    router.push('/employee/login');
    return;
  }
  // 簡易解碼檢查權限（可選）
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (!payload.permissions?.includes('inventory_access')) {
      ElMessage.error('無點貨權限，請使用員工帳號登入');
      router.push('/employee/login');
      return;
    }
  } catch (e) {
    router.push('/employee/login');
  }
  loadProducts();
  loadVegetables();
});
</script>

<style scoped>
.inventory-container {
  padding: 20px;
}
.weekday-selector {
  margin-bottom: 16px;
}
.vegetable-section {
  margin-top: 24px;
}
.vegetable-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.removed-tag {
  text-decoration: line-through;
  opacity: 0.7;
}
.result-text {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  margin: 12px 0;
}

@media (max-width: 768px) {
  .inventory-container {
    padding: 12px;
  }
  .vegetable-tags .el-tag {
    font-size: 12px;
  }
}
</style>
