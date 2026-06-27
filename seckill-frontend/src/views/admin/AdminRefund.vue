<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>退款管理</h2>
        <p>处理用户退款申请</p>
      </div>
    </div>

    <el-table :data="refundList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          {{ row.type === 1 ? '仅退款' : '退货退款' }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="退款金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="退款原因" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['warning', 'success', 'danger', 'info'][row.status]">
            {{ ['待审核', '已同意', '已拒绝', '已完成'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="200" v-if="refundList.some(r => r.status === 0)">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row)">同意</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllRefunds, approveRefund, rejectRefund } from '../../api/refund'

const refundList = ref([])

onMounted(loadList)

async function loadList() {
  try { const r = await getAllRefunds(); refundList.value = r.data || [] } catch (e) {}
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认同意该退款申请？', '退款审核')
    await approveRefund(row.id, '同意退款')
    ElMessage.success('已同意')
    loadList()
  } catch (e) {}
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '退款审核', { inputType: 'textarea' })
    await rejectRefund(row.id, value || '拒绝退款')
    ElMessage.success('已拒绝')
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
