<!--
  设置页面
  整合个人信息编辑、密码修改、账号信息展示、退出登录功能。
  从"我的"页面跳转进入。
-->
<template>
  <view class="settings-page">
    <!-- 头像区域 -->
    <view class="section">
      <view class="section-title">个人信息</view>
      <view class="setting-item" @tap="chooseAvatar">
        <text class="item-label">头像</text>
        <view class="item-right">
          <image v-if="userInfo.avatar" :src="getFullImageUrl(userInfo.avatar)" class="avatar-img" mode="aspectFill" />
          <view v-else class="avatar-placeholder">
            <text class="avatar-text">{{ (userInfo.nickname || 'U').charAt(0) }}</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>
      <view class="setting-item" @tap="editField('nickname')">
        <text class="item-label">昵称</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.nickname || '未设置' }}</text>
          <text class="item-arrow">›</text>
        </view>
      </view>
      <view class="setting-item" @tap="editField('phone')">
        <text class="item-label">手机号</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.phone || '未绑定' }}</text>
          <text class="item-arrow">›</text>
        </view>
      </view>
      <view class="setting-item" @tap="editField('email')">
        <text class="item-label">邮箱</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.email || '未绑定' }}</text>
          <text class="item-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 账号安全 -->
    <view class="section">
      <view class="section-title">账号安全</view>
      <view class="setting-item" @tap="showPasswordModal = true">
        <text class="item-label">修改密码</text>
        <view class="item-right">
          <text class="item-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 权限信息 -->
    <view class="section">
      <view class="section-title">账号信息</view>
      <view class="setting-item">
        <text class="item-label">用户ID</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.userId }}</text>
        </view>
      </view>
      <view class="setting-item">
        <text class="item-label">用户名</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.username }}</text>
        </view>
      </view>
      <view class="setting-item">
        <text class="item-label">角色</text>
        <view class="item-right">
          <view class="role-tag" :class="userInfo.role === 1 ? 'role-admin' : 'role-user'">
            {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
          </view>
        </view>
      </view>
      <view class="setting-item">
        <text class="item-label">注册时间</text>
        <view class="item-right">
          <text class="item-value">{{ formatTime(userInfo.createTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="section" v-if="userStore.isLoggedIn">
      <view class="logout-btn" @tap="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showEditModal" @tap="showEditModal = false" catchtouchmove>
      <view class="modal" @tap.stop>
        <view class="modal-title">{{ editTitle }}</view>
        <input
          v-model="editValue"
          :placeholder="editPlaceholder"
          class="modal-input"
          :type="editFieldType === 'phone' ? 'number' : 'text'"
        />
        <view class="modal-actions">
          <view class="modal-btn cancel" @tap="showEditModal = false">取消</view>
          <view class="modal-btn confirm" @tap="handleSaveField">保存</view>
        </view>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view class="modal-mask" v-if="showPasswordModal" @tap="showPasswordModal = false" catchtouchmove>
      <view class="modal" @tap.stop>
        <view class="modal-title">修改密码</view>
        <input
          v-model="passForm.oldPassword"
          placeholder="请输入旧密码"
          class="modal-input"
          type="password"
        />
        <input
          v-model="passForm.newPassword"
          placeholder="新密码（至少6位）"
          class="modal-input"
          type="password"
        />
        <view class="modal-actions">
          <view class="modal-btn cancel" @tap="showPasswordModal = false">取消</view>
          <view class="modal-btn confirm" @tap="handleChangePassword">确认修改</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { getUserInfo, updateProfile, changePassword, uploadAvatar } from '../../api/user'
import { showToast, navigateTo } from '../../utils/nav'
import { getFullImageUrl } from '../../utils/image'

const userStore = useUserStore()

const userInfo = ref({
  userId: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  avatar: '',
  role: 0,
  createTime: ''
})

// 编辑弹窗
const showEditModal = ref(false)
const editFieldType = ref('')
const editTitle = ref('')
const editValue = ref('')
const editPlaceholder = ref('')

// 修改密码弹窗
const showPasswordModal = ref(false)
const passForm = ref({ oldPassword: '', newPassword: '' })

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    const info = res.data
    userInfo.value = {
      userId: info.userId || '',
      username: info.username || '',
      nickname: info.nickname || '',
      phone: info.phone || '',
      email: info.email || '',
      avatar: info.avatar || '',
      role: info.role ?? 0,
      createTime: info.createTime || ''
    }
  } catch (e) {}
}

onMounted(loadUserInfo)

// 编辑字段
const editField = (field) => {
  editFieldType.value = field
  const config = {
    nickname: { title: '修改昵称', placeholder: '请输入昵称' },
    phone: { title: '修改手机号', placeholder: '请输入手机号' },
    email: { title: '修改邮箱', placeholder: '请输入邮箱' }
  }
  editTitle.value = config[field].title
  editPlaceholder.value = config[field].placeholder
  editValue.value = userInfo.value[field] || ''
  showEditModal.value = true
}

// 保存字段
const handleSaveField = async () => {
  // 输入校验
  if (editFieldType.value === 'phone' && editValue.value) {
    if (!/^1[3-9]\d{9}$/.test(editValue.value)) {
      showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
  }
  if (editFieldType.value === 'email' && editValue.value) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editValue.value)) {
      showToast({ title: '请输入正确的邮箱地址', icon: 'none' })
      return
    }
  }
  try {
    const data = {}
    data[editFieldType.value] = editValue.value
    await updateProfile(data)
    userInfo.value[editFieldType.value] = editValue.value
    // 同步到 store
    userStore.updateUserInfo(data)
    showEditModal.value = false
    showToast({ title: '保存成功', icon: 'success' })
  } catch (e) {}
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      try {
        showToast({ title: '上传中...', icon: 'loading' })
        const result = await uploadAvatar(filePath)
        const avatarUrl = result.data.url
        await updateProfile({ avatar: avatarUrl })
        userInfo.value.avatar = avatarUrl
        userStore.updateUserInfo({ avatar: avatarUrl })
        showToast({ title: '头像已更新', icon: 'success' })
      } catch (e) {
        showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

// 修改密码
const handleChangePassword = async () => {
  if (!passForm.value.oldPassword || !passForm.value.newPassword) {
    showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (passForm.value.newPassword.length < 6) {
    showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }
  try {
    await changePassword(passForm.value)
    showPasswordModal.value = false
    passForm.value = { oldPassword: '', newPassword: '' }
    showToast({ title: '密码已修改', icon: 'success' })
  } catch (e) {}
}

// 退出登录
const handleLogout = async () => {
  const res = await uni.showModal({
    title: '确认退出',
    content: '退出后需要重新登录',
    confirmColor: '#e4393c'
  })
  if (res.confirm) {
    userStore.logout()
    showToast({ title: '已退出', icon: 'success' })
    uni.reLaunch({ url: '/pages/login/login' })
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知'
  const d = new Date(timeStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.settings-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.section {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 24rpx;
  color: #999;
  padding: 24rpx 32rpx 12rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #f5f5f5;
}

.item-label {
  font-size: 28rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-value {
  font-size: 28rpx;
  color: #999;
}

.item-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.avatar-img {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 700;
}

.role-tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}

.role-admin {
  background: #fff0f0;
  color: #e4393c;
}

.role-user {
  background: #f0f5ff;
  color: #1890ff;
}

.logout-btn {
  margin: 40rpx 32rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
  text-align: center;
}

.logout-text {
  font-size: 28rpx;
  color: #e4393c;
  font-weight: 600;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
  margin-bottom: 32rpx;
}

.modal-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 12rpx;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-btn.confirm {
  background: #e4393c;
  color: #fff;
}
</style>
