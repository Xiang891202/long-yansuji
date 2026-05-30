<template>
  <div class="products-container">
    <h2>商品管理</h2>
    <el-button type="primary" @click="openCreateDialog">新增商品</el-button>

    <!-- 商品列表 -->
    <el-table :data="products" border style="margin-top: 20px">
      <el-table-column prop="name" label="商品名稱" />
      <el-table-column prop="unit" label="單位" />
      <el-table-column label="價格">
        <template #default="{ row }">
          <div v-for="p in row.prices" :key="p.label">{{ p.label }} : {{ p.price }}元</div>
        </template>
      </el-table-column>
      <el-table-column label="圖片">
        <template #default="{ row }">
          <img v-if="row.imageUrl" :src="row.imageUrl" style="width: 50px; height: 50px; object-fit: cover" />
          <span v-else>無圖片</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="openEditDialog(row)">編輯</el-button>
          <el-button @click="deleteProduct(row.id)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/編輯商品對話框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="50%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分類">
          <el-select v-model="form.categoryId" placeholder="請選擇分類">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名稱">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="單位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="價格">
          <div v-for="(price, idx) in form.prices" :key="idx" style="display: flex; gap: 10px; margin-bottom: 8px">
            <el-input v-model="price.label" placeholder="規格(例:小份)" style="width: 120px" />
            <el-input-number v-model="price.price" :min="0" placeholder="價格" />
            <el-button @click="removePrice(idx)" type="danger" circle>−</el-button>
          </div>
          <el-button @click="addPrice" type="primary" plain>＋新增價格</el-button>
        </el-form-item>
        <el-form-item label="商品圖片">
          <input type="file" accept="image/*" @change="handleFileChange" />
          <el-button :loading="uploading" @click="uploadImage" style="margin-left: 10px">上傳圖片</el-button>
          <div v-if="form.imageUrl" style="margin-top: 10px">
            <img :src="form.imageUrl" style="width: 100px; height: 100px; object-fit: cover" />
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
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '@/api/axios';

const products = ref([]);
const categories = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
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

// 載入商品分類
const loadCategories = async () => {
  const res = await api.get('/admin/categories');
  categories.value = res.data;
};

// 載入商品列表
const loadProducts = async () => {
  const res = await api.get('/admin/products');
  products.value = res.data;
};

// 新增價格欄位
const addPrice = () => {
  form.value.prices.push({ label: '', price: 0 });
};
const removePrice = (idx) => {
  form.value.prices.splice(idx, 1);
};

// 開啟新增對話框
const openCreateDialog = () => {
  isEdit.value = false;
  form.value = {
    id: null,
    categoryId: '',
    name: '',
    unit: '個',
    prices: [{ label: '標準', price: 0 }],
    imageUrl: ''
  };
  selectedFile.value = null;
  dialogVisible.value = true;
};

// 開啟編輯對話框
const openEditDialog = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  selectedFile.value = null;
  dialogVisible.value = true;
};

// 檔案選擇
const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0];
};

// 上傳圖片
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
    form.value.imageUrl = res.data; // 更新圖片 URL
    ElMessage.success('圖片上傳成功');
  } catch (err) {
    console.error(err);
    ElMessage.error('上傳失敗');
  } finally {
    uploading.value = false;
  }
};

// 提交商品（新增或更新）
const submitForm = async () => {
  try {
    if (isEdit.value) {
      await api.put(`/admin/products/${form.value.id}`, form.value);
      ElMessage.success('更新成功');
    } else {
      const res = await api.post('/admin/products', form.value);
      form.value.id = res.data.id; // 保留 id 以便後續上傳圖片
      ElMessage.success('新增成功，可繼續上傳圖片');
    }
    // 重新載入列表
    await loadProducts();
    dialogVisible.value = false;
  } catch (err) {
    console.error(err);
    ElMessage.error('操作失敗');
  }
};

// 刪除商品
const deleteProduct = async (id) => {
  await ElMessageBox.confirm('確定刪除此商品？', '提示', { type: 'warning' });
  await api.delete(`/admin/products/${id}`);
  ElMessage.success('刪除成功');
  await loadProducts();
};

onMounted(() => {
  loadCategories();
  loadProducts();
});
</script>

<style scoped>
.products-container {
  padding: 20px;
}
</style>