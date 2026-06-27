<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>秒杀活动管理</h2>
        <p>创建和管理秒杀活动</p>
      </div>
      <el-button type="danger" @click="openCreateDialog" round>
        <el-icon><Plus /></el-icon> 创建秒杀活动
      </el-button>
    </div>

    <el-table :data="seckillList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="goodsName" label="商品名称" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
        <template #default="{ row }">¥{{ row.seckillPrice }}</template>
      </el-table-column>
      <el-table-column prop="stockCount" label="库存" width="80" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['info', 'danger', ''][row.status]">{{ ['未开始', '进行中', '已结束'][row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleStart(row)">开始</el-button>
          <el-button v-if="row.status === 1" size="small" type="warning" @click="handleEnd(row)">结束</el-button>
          <el-button v-if="row.status === 0" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button v-if="row.status !== 1" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          <el-button v-if="row.status === 1" size="small" @click="openStockDialog(row)">补库存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '创建秒杀活动'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="!isEdit" label="关联商品ID">
          <el-input-number v-model="form.goodsId" :min="1" />
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
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 补库存弹窗 -->
    <el-dialog v-model="stockDialogVisible" title="补充库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="增加数量">
          <el-input-number v-model="stockCount" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminSeckillList, createSeckillActivity, updateSeckillActivity, deleteSeckillActivity, updateSeckillStatus, addStock } from '../../api/admin'

const seckillList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const stockDialogVisible = ref(false)
const stockGoodsId = ref(null)
const stockCount = ref(10)

const loadList = async () => {
  try {
    const res = await getAdminSeckillList()
    seckillList.value = res.data
  } catch (e) { /* interceptor handles toast */ }
}

onMounted(loadList)

const openCreateDialog = () => {
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

const openStockDialog = (row) => {
  stockGoodsId.value = row.id  // 使用秒杀商品ID，不是普通商品ID
  stockCount.value = 10
  stockDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateSeckillActivity(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createSeckillActivity(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) { /* interceptor handles */ }
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确认开始该秒杀活动？', '操作确认')
    await updateSeckillStatus(row.id, 1)
    ElMessage.success('秒杀已开始')
    loadList()
  } catch (e) { /* interceptor or cancel handles */ }
}

const handleEnd = async (row) => {
  try {
    await ElMessageBox.confirm('确认结束该秒杀活动？', '操作确认')
    await updateSeckillStatus(row.id, 2)
    ElMessage.success('秒杀已结束')
    loadList()
  } catch (e) { /* interceptor or cancel handles */ }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该秒杀活动？', '删除确认')
    await deleteSeckillActivity(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { /* interceptor or cancel handles */ }
}

const handleAddStock = async () => {
  try {
    await addStock(stockGoodsId.value, stockCount.value)
    ElMessage.success('库存补充成功')
    stockDialogVisible.value = false
    loadList()
  } catch (e) { /* interceptor handles */ }
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
