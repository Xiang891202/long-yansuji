<template>
  <div class="employees-container">
    <div class="page-header">
      <h2>人資管理（員工管理）</h2>
      <el-button type="primary" @click="openCreateDialog" class="add-btn">新增員工</el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="employees" border>
        <el-table-column prop="name" label="姓名" />
        <el-table-column label="狀態">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">{{ row.isActive ? '啟用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">編輯</el-button>
            <el-button size="small" :loading="toggleLoading === row.id" @click="toggleActive(row)">
              {{ row.isActive ? '停用' : '啟用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" class="employee-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="請輸入姓名" />
        </el-form-item>
        <el-form-item label="身分證字號" required>
          <el-input v-model="form.identityNumber" placeholder="請輸入身分證字號" />
        </el-form-item>
        <el-form-item label="密碼" required>
          <el-input type="password" v-model="form.password" placeholder="請輸入密碼" show-password />
        </el-form-item>
        <el-form-item label="狀態">
          <el-switch v-model="form.isActive" active-text="啟用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="權限">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox value="inventory_access">點貨權限</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ isEdit ? '更新' : '新增' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import api from '@/api/axios';
import { useLoading } from '@/composables/useLoading';

const employees = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const form = ref({
  id: null,
  name: '',
  identityNumber: '',
  password: '',          // 新增密碼欄位
  isActive: true,
  permissions: [],
});

const dialogTitle = computed(() => (isEdit.value ? '編輯員工' : '新增員工'));

const { isLoading: submitLoading, withLoading: withSubmitLoading } = useLoading();
const toggleLoading = ref(null);

const loadEmployees = async () => {
  try {
    const res = await api.get('/admin/employees');
    employees.value = res.data;
  } catch {
    ElMessage.error('載入員工失敗');
  }
};

const openCreateDialog = () => {
  isEdit.value = false;
  form.value = {
    id: null,
    name: '',
    identityNumber: '',
    password: '',
    isActive: true,
    permissions: [],
  };
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  isEdit.value = true;
  // 注意：編輯時密碼不回填（安全考量），後端更新時若密碼為空則不修改密碼
  form.value = {
    ...row,
    password: '',   // 編輯時不清除原有密碼，但表單中密碼欄位留空表示不修改
  };
  dialogVisible.value = true;
};

const submitForm = () => withSubmitLoading(async () => {
  if (!form.value.name || !form.value.identityNumber) {
    ElMessage.warning('請填寫姓名與身分證字號');
    return;
  }
  if (!isEdit.value && !form.value.password) {
    ElMessage.warning('請設定密碼');
    return;
  }
  // 複製一份資料，避免直接修改原物件
  const payload = { ...form.value };
  if (isEdit.value && !payload.password) {
    delete payload.password; // 編輯時若不填密碼，則不更新密碼
  }
  if (isEdit.value) {
    await api.put(`/admin/employees/${form.value.id}`, payload);
    ElMessage.success('更新成功');
  } else {
    await api.post('/admin/employees', payload);
    ElMessage.success('新增成功');
  }
  dialogVisible.value = false;
  await loadEmployees();
});

const toggleActive = async (row) => {
  toggleLoading.value = row.id;
  try {
    const newStatus = !row.isActive;
    await api.patch(`/admin/employees/${row.id}/toggle-active?isActive=${newStatus}`);
    ElMessage.success(`${row.name} 已${newStatus ? '啟用' : '停用'}`);
    await loadEmployees();
  } catch {
    ElMessage.error('狀態更新失敗');
  } finally {
    toggleLoading.value = null;
  }
};

onMounted(() => {
  loadEmployees();
});
</script>

<style scoped>
/* 保持原样式 */
.employees-container {
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
.employee-dialog :deep(.el-dialog) {
  width: 90%;
  max-width: 500px;
  border-radius: 16px;
}
@media (max-width: 768px) {
  .employees-container {
    padding: 12px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>