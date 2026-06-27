<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>秒杀商品管理</h2>
        <p>管理秒杀活动中的商品</p>
      </div>
      <el-button type="danger" @click="openAddDialog" round>
        <el-icon><Plus /></el-icon> 新增秒杀商品
      </el-button>
    </div>

    <el-table :data="goodsList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="goodsName" label="商品名称" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
        <template #default="{ row }">¥{{ row.seckillPrice }}</template>
      </el-table-column>
      <el-table-column prop="goodsPrice" label="原价" width="100">
        <template #default="{ row }">¥{{ row.goodsPrice }}</template>
      </el-table-column>
      <el-table-column prop="stockCount" label="秒杀库存" width="90" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="['info', 'danger', ''][row.status]">{{ ['未开始', '进行中', '已结束'][row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button v-if="row.status !== 1" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀商品' : '新增秒杀商品'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="!isEdit" label="关联商品ID">
          <el-input-number v-model="form.goodsId" :min="1" placeholder="普通商品ID" />
        </el-form-item>
        <el-form-item label="秒杀价">
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="秒杀库存">
          <el-input-number v-model="form.stockCount" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
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
import { getAdminGoodsList, addGoods, updateGoods, deleteGoods } from '../../api/admin'

const goodsList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const submitting = ref(false)

const loadGoods = async () => {
  try {
    const res = await getAdminGoodsList()
    goodsList.value = res.data
  } catch (e) { /* interceptor handles */ }
}

onMounted(loadGoods)

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { goodsId: null, seckillPrice: 0, stockCount: 10, startTime: '', endTime: '' }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { seckillPrice: row.seckillPrice, stockCount: row.stockCount, startTime: row.startTime, endTime: row.endTime }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateGoods(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await addGoods(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadGoods()
  } catch (e) { /* interceptor handles */ } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该秒杀商品？', '删除确认')
    await deleteGoods(row.id)
    ElMessage.success('删除成功')
    loadGoods()
  } catch (e) { /* interceptor or cancel handles */ }
}
</script>

<style scoped>
.admin-page { }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; padding: 20px 24px;
  background: white; border-radius: var(--radius-md); box-shadow: var(--shadow-card);
}
.page-header h2 { font-size: 20px; font-weight: 700; }
.page-header p { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
.admin-table { border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-card); }
</style>
