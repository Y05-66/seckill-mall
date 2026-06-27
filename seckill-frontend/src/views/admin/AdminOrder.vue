<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>订单管理</h2>
        <p>查看和管理所有订单</p>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card style="margin-bottom: 20px" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="订单状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option :value="0" label="待支付" />
            <el-option :value="1" label="已支付" />
            <el-option :value="2" label="已取消" />
            <el-option :value="3" label="已超时" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="filters.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="orderList" stripe>
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="goodsName" label="商品名称" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
        <template #default="{ row }">¥{{ row.seckillPrice }}</template>
      </el-table-column>
      <el-table-column prop="statusDesc" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
            {{ row.statusDesc }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column prop="payTime" label="支付时间" width="180" />
    </el-table>
    <el-empty v-if="orderList.length === 0" description="暂无订单" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminOrderList } from '../../api/admin'

const orderList = ref([])
const filters = ref({ status: null, orderNo: '' })

const loadOrders = async () => {
  try {
    const params = {}
    if (filters.value.status !== null && filters.value.status !== '') params.status = filters.value.status
    if (filters.value.orderNo) params.orderNo = filters.value.orderNo
    const res = await getAdminOrderList(params)
    orderList.value = res.data
  } catch (e) { /* interceptor handles toast */ }
}

onMounted(loadOrders)
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
.filter-card { border-radius: var(--radius-md); }
</style>
