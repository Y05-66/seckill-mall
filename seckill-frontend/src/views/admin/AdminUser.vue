<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>管理系统用户和权限</p>
      </div>
    </div>

    <el-table :data="userList" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : ''">{{ row.role === 1 ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="handleRoleChange(row)">
            {{ row.role === 1 ? '设为用户' : '设为管理员' }}
          </el-button>
          <el-button
            size="small"
            :type="row.status === 0 ? 'warning' : 'success'"
            @click="handleStatusChange(row)"
          >
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserList, updateUserRole, updateUserStatus } from '../../api/admin'

const userList = ref([])

const loadUsers = async () => {
  try {
    const res = await getAdminUserList()
    userList.value = res.data
  } catch (e) { /* interceptor handles toast */ }
}

onMounted(loadUsers)

const handleRoleChange = async (row) => {
  try {
    const newRole = row.role === 1 ? 0 : 1
    const label = newRole === 1 ? '管理员' : '普通用户'
    await ElMessageBox.confirm(`确认将用户 ${row.username} 设为${label}？`, '角色变更')
    await updateUserRole(row.id, newRole)
    ElMessage.success('角色变更成功')
    loadUsers()
  } catch (e) { /* interceptor or cancel handles */ }
}

const handleStatusChange = async (row) => {
  try {
    const newStatus = row.status === 0 ? 1 : 0
    const label = newStatus === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确认${label}用户 ${row.username}？`, '状态变更')
    await updateUserStatus(row.id, newStatus)
    ElMessage.success('状态变更成功')
    loadUsers()
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
