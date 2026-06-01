<template>
  <div class="categories-container">
    <div class="page-header">
      <h2>分類管理</h2>
      <el-button type="primary" @click="openCreateDialog" class="add-btn">新增分類</el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="categories" border>
        <el-table-column prop="name" label="分類名稱" />
        <!-- <el-table-column prop="code" label="代碼" />
        <el-table-column prop="sortOrder" label="排序" width="100" /> -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">編輯</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row.id)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/編輯對話框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" class="category-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="分類名稱" required>
          <el-input v-model="form.name" placeholder="請輸入分類名稱" />
        </el-form-item>
        <el-form-item label="代碼" required>
          <el-input v-model="form.code" placeholder="請輸入代碼 (如: signature, vegetable)" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width: 100%" />
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

const categories = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const form = ref({ id: null, name: '', code: '', sortOrder: 0 });

const dialogTitle = computed(() => (isEdit.value ? '編輯分類' : '新增分類'));

const loadCategories = async () => {
  try {
    const res = await api.get('/admin/categories');
    categories.value = res.data;
  } catch {
    ElMessage.error('載入失敗');
  }
};

const openCreateDialog = () => {
  isEdit.value = false;
  form.value = { id: null, name: '', code: '', sortOrder: 0 };
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!form.value.name || !form.value.code) {
    ElMessage.warning('請填寫分類名稱與代碼');
    return;
  }
  try {
    if (isEdit.value) {
      await api.put(`/admin/categories/${form.value.id}`, form.value);
      ElMessage.success('更新成功');
    } else {
      await api.post('/admin/categories', form.value);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    await loadCategories();
  } catch {
    ElMessage.error('操作失敗');
  }
};

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('確定刪除此分類？', '提示', { type: 'warning' });
    await api.delete(`/admin/categories/${id}`);
    ElMessage.success('刪除成功');
    await loadCategories();
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('刪除失敗');
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.categories-container {
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
}
.add-btn {
  border-radius: 24px;
  padding: 8px 20px;
}
.table-wrapper {
  overflow-x: auto;
}
.category-dialog :deep(.el-dialog) {
  width: 90%;
  max-width: 500px;
  border-radius: 16px;
}
@media (max-width: 768px) {
  .categories-container {
    padding: 12px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>