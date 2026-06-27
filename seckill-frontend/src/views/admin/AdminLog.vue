<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>操作日志</h2>
        <p>查看管理员操作记录</p>
      </div>
      <div class="filter-bar">
        <input v-model="filters.username" placeholder="用户名" class="filter-input" />
        <input v-model="filters.operation" placeholder="操作类型" class="filter-input" />
        <button class="filter-btn" @click="loadLogs">查询</button>
      </div>
    </div>

    <el-table :data="logList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="操作人" width="100" />
      <el-table-column prop="operation" label="操作类型" width="150" />
      <el-table-column prop="method" label="方法" width="100" />
      <el-table-column prop="url" label="请求URL" />
      <el-table-column prop="ip" label="IP地址" width="130" />
      <el-table-column prop="result" label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.result === 1 ? 'success' : 'danger'">
            {{ row.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时" width="80">
        <template #default="{ row }">{{ row.costTime }}ms</template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="170" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const logList = ref([])
const filters = ref({ username: '', operation: '' })

onMounted(loadLogs)

async function loadLogs() {
  try {
    const params = {}
    if (filters.value.username) params.username = filters.value.username
    if (filters.value.operation) params.operation = filters.value.operation
    const r = await request.get('/log/list', { params })
    logList.value = r.data || []
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
.filter-bar { display: flex; gap: 8px; }
.filter-input {
  padding: 6px 12px; border: 1px solid #ddd; border-radius: 4px;
  font-size: 13px; width: 140px;
}
.filter-btn {
  padding: 6px 16px; background: var(--red); color: white;
  border: none; border-radius: 4px; cursor: pointer; font-size: 13px;
}
.admin-table { border-radius: var(--radius); overflow: hidden; box-shadow: var(--shadow); }
</style>
