<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>轮播图管理</h2>
        <p>管理首页轮播图展示</p>
      </div>
      <el-button type="danger" @click="openAddDialog" round>
        <el-icon><Plus /></el-icon> 新增轮播图
      </el-button>
    </div>

    <el-table :data="bannerList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <img :src="row.imageUrl" style="width:80px;height:40px;object-fit:cover;border-radius:4px" />
        </template>
      </el-table-column>
      <el-table-column prop="linkUrl" label="跳转链接" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'

const bannerList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const submitting = ref(false)

onMounted(loadList)

async function loadList() {
  try {
    const res = await request.get('/banner/admin/list')
    bannerList.value = res.data || []
  } catch (e) {}
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/banner/' + editId.value, form.value)
    } else {
      await request.post('/banner', form.value)
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {} finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该轮播图？', '删除确认')
    await request.delete('/banner/' + row.id)
    loadList()
  } catch (e) {}
}
</script>

<style scoped>
.admin-page {}
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; padding: 20px 24px;
  background: white; border-radius: var(--radius); box-shadow: var(--shadow);
}
.page-header h2 { font-size: 20px; font-weight: 700; }
.page-header p { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
.admin-table { border-radius: var(--radius); overflow: hidden; box-shadow: var(--shadow); }
</style>
