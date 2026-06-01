<template>
  <div class="products-container">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openCreateDialog" class="add-btn">新增商品</el-button>
    </div>

    <!-- 按分類分組的商品列表 -->
    <div v-for="category in categories" :key="category.id" class="category-section">
      <h3 class="category-title">
        <span>{{ category.name }}</span>
        <span class="product-count">{{ groupedProducts[category.id]?.length || 0 }} 項商品</span>
      </h3>
      <div class="table-wrapper">
        <el-table :data="groupedProducts[category.id]" border>
          <el-table-column prop="name" label="商品名稱" min-width="120" />
          <el-table-column label="價格" min-width="150">
            <template #default="{ row }">
              <div v-for="p in row.prices" :key="p.label" class="price-item">
                {{ p.label }} : {{ p.price }}元
              </div>
            </template>
          </el-table-column>
          <el-table-column label="圖片" width="80">
            <template #default="{ row }">
              <img v-if="row.imageUrl" :src="row.imageUrl" class="product-thumb" />
              <span v-else class="no-image">無圖片</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">編輯</el-button>
              <el-button size="small" type="danger" @click="deleteProduct(row.id)">刪除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/編輯商品對話框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" class="product-dialog">
      <el-form :model="form" label-width="100px" label-position="top" :rules="formRules" ref="formRef">
        <el-form-item label="分類" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="請選擇分類" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入商品名稱" />
        </el-form-item>
        <el-form-item label="價格">
          <div v-for="(price, idx) in form.prices" :key="idx" class="price-row">
            <el-input v-model="price.label" placeholder="規格 (例:小份)" style="width: 120px" />
            <el-input-number v-model="price.price" :min="0" placeholder="價格" controls-position="right" />
            <el-button @click="removePrice(idx)" type="danger" circle size="small">−</el-button>
          </div>
          <el-button @click="addPrice" type="primary" plain size="small">＋ 新增價格</el-button>
        </el-form-item>
        <el-form-item label="商品圖片">
          <div class="upload-area">
            <input type="file" accept="image/*" @change="handleFileChange" ref="fileInput" class="file-input" />
            <el-button :loading="uploading" @click="uploadImage" size="small">上傳圖片</el-button>
          </div>
          <div v-if="form.imageUrl" class="image-preview">
            <img :src="form.imageUrl" />
            <p>圖片網址：{{ form.imageUrl }}</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '更新' : '新增' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '@/api/axios';

const products = ref([]);
const categories = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const form = ref({
  id: null,
  categoryId: '',
  name: '',
  unit: '個',
  prices: [{ label: '標準', price: 0 }],
  imageUrl: ''
});
const selectedFile = ref(null);
const uploading = ref(false);
const fileInput = ref(null);

const formRules = {
  categoryId: [{ required: true, message: '請選擇分類', trigger: 'change' }],
  name: [{ required: true, message: '請輸入商品名稱', trigger: 'blur' }]
};

// 按分類分組的商品
const groupedProducts = computed(() => {
  const group = {};
  for (const cat of categories.value) {
    group[cat.id] = products.value.filter(p => p.categoryId === cat.id);
  }
  return group;
});

const loadCategories = async () => {
  const res = await api.get('/admin/categories');
  categories.value = res.data;
};

const loadProducts = async () => {
  const res = await api.get('/admin/products');
  products.value = res.data;
};

const addPrice = () => {
  form.value.prices.push({ label: '', price: 0 });
};
const removePrice = (idx) => {
  form.value.prices.splice(idx, 1);
};

const openCreateDialog = () => {
  isEdit.value = false;
  form.value = {
    id: null,
    categoryId: categories.value.length ? categories.value[0].id : '',
    name: '',
    unit: '個',
    prices: [{ label: '標準', price: 0 }],
    imageUrl: ''
  };
  selectedFile.value = null;
  if (fileInput.value) fileInput.value.value = '';
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  selectedFile.value = null;
  if (fileInput.value) fileInput.value.value = '';
  dialogVisible.value = true;
};

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0];
};

const uploadImage = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('請先選擇圖片');
    return;
  }
  if (!form.value.id && !isEdit.value) {
    ElMessage.warning('請先儲存商品後再上傳圖片');
    return;
  }
  uploading.value = true;
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  try {
    const res = await api.post(`/admin/products/${form.value.id}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    form.value.imageUrl = res.data;
    ElMessage.success('圖片上傳成功');
  } catch (err) {
    console.error(err);
    ElMessage.error('上傳失敗');
  } finally {
    uploading.value = false;
  }
};

const submitForm = async () => {
  if (!form.value.categoryId) {
    ElMessage.warning('請選擇分類');
    return;
  }
  if (!form.value.name) {
    ElMessage.warning('請填寫商品名稱');
    return;
  }
  try {
    if (isEdit.value) {
      await api.put(`/admin/products/${form.value.id}`, form.value);
      ElMessage.success('更新成功');
    } else {
      const res = await api.post('/admin/products', form.value);
      form.value.id = res.data.id;
      ElMessage.success('新增成功，可繼續上傳圖片');
    }
    await loadProducts();
    dialogVisible.value = false;
  } catch (err) {
    console.error(err);
    ElMessage.error('操作失敗');
  }
};

const deleteProduct = async (id) => {
  await ElMessageBox.confirm('確定刪除此商品？', '提示', { type: 'warning' });
  await api.delete(`/admin/products/${id}`);
  ElMessage.success('刪除成功');
  await loadProducts();
};

onMounted(() => {
  loadCategories().then(() => loadProducts());
});
</script>

<style scoped>
.products-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}
.add-btn {
  border-radius: 24px;
  padding: 8px 20px;
}
.category-section {
  margin-top: 28px;
  background: #fff;
  border-radius: 12px;
  padding: 8px 12px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  transition: box-shadow 0.2s;
}
.category-section:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.category-title {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  background: #f8f9fa;
  padding: 10px 15px;
  border-left: 4px solid #42b983;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 1.2rem;
  font-weight: 600;
}
.product-count {
  font-size: 0.8rem;
  font-weight: normal;
  color: #666;
}
.table-wrapper {
  overflow-x: auto;
}
.product-thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
}
.no-image {
  font-size: 12px;
  color: #bbb;
}
.price-item {
  font-size: 0.85rem;
  line-height: 1.4;
}
/* 對話框自適應 */
.product-dialog :deep(.el-dialog) {
  width: 90%;
  max-width: 500px;
  border-radius: 16px;
}
.price-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.price-row .el-input-number {
  width: 140px;
}
.upload-area {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.file-input {
  flex: 1;
  min-width: 180px;
}
.image-preview {
  margin-top: 12px;
}
.image-preview img {
  max-width: 120px;
  max-height: 120px;
  border-radius: 8px;
  border: 1px solid #ddd;
}
.image-preview p {
  font-size: 12px;
  word-break: break-all;
  color: #666;
  margin-top: 8px;
}
/* 手機版響應式 */
@media (max-width: 768px) {
  .products-container {
    padding: 12px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .category-title {
    font-size: 1rem;
    padding: 8px 12px;
  }
  .product-count {
    font-size: 0.7rem;
  }
  .price-row {
    flex-direction: column;
    align-items: stretch;
  }
  .price-row .el-input-number {
    width: 100%;
  }
  .upload-area {
    flex-direction: column;
    align-items: stretch;
  }
  .product-dialog :deep(.el-dialog) {
    margin: 20px auto;
  }
}
</style>