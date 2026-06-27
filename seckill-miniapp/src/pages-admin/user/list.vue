<!--
  管理后台 - 用户管理

  功能：
  1. 用户列表（ID、用户名、昵称、手机号、角色、状态）
  2. 修改用户角色（普通用户 ↔ 管理员，不可修改自己）
  3. 启用/禁用用户（不可禁用管理员，不可禁用自己）

  权限：需管理员（role=1），onMounted 中校验
  安全策略：禁止修改自身角色、禁止禁用管理员账号
-->
<template>
  <view class="container">
    <view class="title">用户管理</view>

    <view v-for="user in userList" :key="user.id" class="user-item">
      <view class="user-info">
        <text class="user-name">{{ user.username }}</text>
        <text class="user-nickname">{{ user.nickname || '-' }}</text>
        <view class="user-tags">
          <view class="tag" :class="user.role === 1 ? 'tag-admin' : 'tag-user'">{{ user.role === 1 ? '管理员' : '用户' }}</view>
          <view class="tag" :class="user.status === 0 ? 'tag-active' : 'tag-disabled'">{{ user.status === 0 ? '正常' : '禁用' }}</view>
        </view>
      </view>
      <view class="user-actions">
        <view v-if="user.id !== userStore.userInfo.userId" class="act-btn" @tap="handleRole(user)">
          {{ user.role === 1 ? '设为用户' : '设为管理员' }}
        </view>
        <view v-if="user.id !== userStore.userInfo.userId && user.role !== 1" class="act-btn" :class="user.status === 0 ? 'warning' : 'success'" @tap="handleStatus(user)">
          {{ user.status === 0 ? '禁用' : '启用' }}
        </view>
      </view>
    </view>

    <view v-if="userList.length === 0" class="empty">暂无数据</view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getAdminUserList, updateUserRole, updateUserStatus } from '../../api/admin'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
import { showToast, navigateBack, showModal } from '../../utils/nav'

const userList = ref([])

const loadUsers = async () => {
  try { const res = await getAdminUserList(); userList.value = res.data || [] } catch (e) {}
}

onMounted(() => {
  if (!userStore.isAdmin) { showToast({ title: '无权限', icon: 'none' }); navigateBack(); return }
  loadUsers()
})

const handleRole = async (user) => {
  const newRole = user.role === 1 ? 0 : 1
  const label = newRole === 1 ? '管理员' : '普通用户'
  const res = await showModal({ title: '角色变更', content: `确认将 ${user.username} 设为${label}？` })
  if (res.confirm) {
    try { await updateUserRole(user.id, newRole); showToast({ title: '已变更', icon: 'success' }); loadUsers() } catch (e) {}
  }
}

const handleStatus = async (user) => {
  const newStatus = user.status === 0 ? 1 : 0
  const label = newStatus === 1 ? '禁用' : '启用'
  const res = await showModal({ title: '状态变更', content: `确认${label}用户 ${user.username}？` })
  if (res.confirm) {
    try { await updateUserStatus(user.id, newStatus); showToast({ title: '已变更', icon: 'success' }); loadUsers() } catch (e) {}
  }
}
</script>

<style scoped>
.container { padding: 20rpx; }
.title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.user-item { background: #fff; border-radius: 12rpx; padding: 20rpx; margin-bottom: 12rpx; }
.user-name { font-size: 28rpx; font-weight: bold; display: block; }
.user-nickname { font-size: 24rpx; color: #666; display: block; margin-top: 4rpx; }
.user-tags { display: flex; gap: 10rpx; margin-top: 8rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.tag-admin { background: #fff1f0; color: #f56c6c; }
.tag-user { background: #e6f7ff; color: #409eff; }
.tag-active { background: #e6fffb; color: #67c23a; }
.tag-disabled { background: #f5f5f5; color: #999; }
.user-actions { display: flex; gap: 16rpx; margin-top: 12rpx; }
.act-btn { font-size: 22rpx; padding: 8rpx 20rpx; border-radius: 6rpx; background: #f0f0f0; color: #333; }
.act-btn.warning { background: #fff7e6; color: #e6a23c; }
.act-btn.success { background: #e6fffb; color: #67c23a; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
