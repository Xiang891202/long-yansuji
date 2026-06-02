<template>
  <div class="inventory-container">
    <div class="inventory-header">
      <h2>📋 員工點貨回報</h2>
      <p class="subtitle">請輸入各商品目前庫存，系統將自動計算叫貨建議</p>
    </div>

    <!-- 星期選擇器 -->
    <div class="weekday-selector">
      <span class="selector-label">選擇星期：</span>
      <el-select v-model="selectedWeekday" @change="loadProducts" placeholder="請選擇星期" style="width: 180px">
        <el-option v-for="item in weekdays" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <!-- 商品列表表格 -->
    <div class="product-table-wrapper">
      <el-table :data="products" border stripe style="width: 100%">
        <el-table-column prop="name" label="商品名稱" min-width="120" />
        <el-table-column prop="safeQuantity" label="安全庫存" width="100" align="center" />
        <el-table-column label="目前庫存" width="160" align="center">
          <template #default="{ row }">
            <el-input-number v-model="quantities[row.id]" :min="0" size="default" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 蔬菜區 -->
    <div class="vegetable-section">
      <h3>🥬 蔬菜清單（點擊切換保留 / 移除）</h3>
      <div class="vegetable-tags">
        <el-tag
          v-for="veg in vegetableOptions"
          :key="veg"
          :type="removedVegetables.includes(veg) ? 'info' : 'success'"
          :class="{ 'removed-tag': removedVegetables.includes(veg) }"
          @click="toggleVegetable(veg)"
          effect="plain"
          size="large"
        >
          {{ veg }} {{ removedVegetables.includes(veg) ? '✖ 移除' : '✔ 保留' }}
        </el-tag>
      </div>
      <div class="vegetable-tip" v-if="removedVegetables.length > 0">
        ⚠️ 已移除 {{ removedVegetables.join('、') }}，將不會出現在叫貨清單中
      </div>
    </div>

    <!-- 提交按鈕 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="calculate" :loading="calculating" class="calculate-btn">
        🧮 計算並產出叫貨文字
      </el-button>
    </div>

    <!-- 結果對話框 -->
    <el-dialog title="📦 叫貨清單" v-model="resultDialogVisible" width="90%" class="result-dialog">
      <div class="result-section">
        <h4>📞 廠商叫貨</h4>
        <pre class="result-text">{{ supplierText }}</pre>
        <el-button type="primary" plain @click="copyText(supplierText)">📋 複製叫貨清單</el-button>
      </div>
      <div class="result-section" style="margin-top: 20px">
        <h4>🥬 蔬菜清單</h4>
        <pre class="result-text">{{ vegetableText }}</pre>
        <el-button type="success" plain @click="copyText(vegetableText)">🥬 複製蔬菜清單</el-button>
      </div>
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
const selectedWeekday = ref(new Date().getDay() || 7);
const products = ref([]);
const quantities = ref({});
const vegetableOptions = ref([]);
const removedVegetables = ref([]);
const calculating = ref(false);
const resultDialogVisible = ref(false);
const supplierText = ref('');
const vegetableText = ref('');

const loadProducts = async () => {
  try {
    const res = await api.get('/inventory/products', {
      params: { dayOfWeek: selectedWeekday.value }
    });
    products.value = res.data;
    const initQty = {};
    products.value.forEach(p => {
      initQty[p.id] = 0;
    });
    quantities.value = initQty;
  } catch (err) {
    console.error(err);
    ElMessage.error('載入商品失敗，請確認後端 /inventory/products 接口已實作');
  }
};

const loadVegetables = async () => {
  try {
    const res = await api.get('/public/vegetables');
    vegetableOptions.value = res.data.map(v => v.name);
    removedVegetables.value = [];
  } catch (err) {
    console.error('載入蔬菜清單失敗', err);
    vegetableOptions.value = ['玉米', '高麗菜', '花椰菜', '青椒', '洋蔥', '九層塔'];
  }
};

const toggleVegetable = (veg) => {
  const idx = removedVegetables.value.indexOf(veg);
  if (idx === -1) {
    removedVegetables.value.push(veg);
  } else {
    removedVegetables.value.splice(idx, 1);
  }
};

const calculate = async () => {
  const items = products.value.map(p => ({
    productId: p.id,
    currentQuantity: quantities.value[p.id] || 0,
    safeStockVersion: p.version
  }));
  const keptVegetables = vegetableOptions.value.filter(v => !removedVegetables.value.includes(v));

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

const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success('已複製到剪貼簿');
  } catch (err) {
    ElMessage.error('複製失敗');
  }
};

onMounted(() => {
  const token = localStorage.getItem('token');
  if (!token) {
    router.push('/employee/login');
    return;
  }
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
  background: #f9fafb;
  min-height: 100vh;
}

.inventory-header {
  margin-bottom: 24px;
}

.inventory-header h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #6c757d;
  font-size: 0.9rem;
  margin: 0;
}

.weekday-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  background: white;
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.selector-label {
  font-weight: 500;
  color: #495057;
}

.product-table-wrapper {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  margin-bottom: 28px;
}

.vegetable-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 28px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.vegetable-section h3 {
  margin: 0 0 16px 0;
  font-size: 1.3rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.vegetable-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

/* 优化蔬菜标签字体大小和样式 */
.vegetable-tags .el-tag {
  font-size: 1rem !important;
  padding: 8px 16px !important;
  border-radius: 24px !important;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.vegetable-tags .el-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.removed-tag {
  text-decoration: line-through;
  opacity: 0.7;
}

.vegetable-tip {
  background: #fff3cd;
  color: #856404;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.calculate-btn {
  padding: 12px 32px;
  font-size: 1.1rem;
  border-radius: 40px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.result-dialog :deep(.el-dialog) {
  border-radius: 20px;
  width: 90%;
  max-width: 700px;
}

.result-section {
  margin-bottom: 20px;
}

.result-section h4 {
  font-size: 1.1rem;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.result-text {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 12px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9rem;
  margin: 12px 0;
  border: 1px solid #e9ecef;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .inventory-container {
    padding: 16px;
  }
  .inventory-header h2 {
    font-size: 1.5rem;
  }
  .vegetable-tags .el-tag {
    font-size: 0.9rem !important;
    padding: 6px 12px !important;
  }
  .calculate-btn {
    width: 100%;
  }
  .result-text {
    font-size: 0.8rem;
  }
}
</style>