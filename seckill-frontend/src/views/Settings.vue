<!--
  设置页面
  整合个人信息编辑、密码修改、账号信息展示、退出登录功能。
  从 Profile.vue 中拆分出来，提供统一的设置入口。
-->
<template>
  <div class="settings-page container">
    <div class="page-bar">
      <span class="page-title">设置</span>
    </div>

    <!-- 个人信息 -->
    <div class="section">
      <div class="section-title">个人信息</div>
      <div class="setting-card">
        <div class="setting-item" @click="editField('avatar')">
          <span class="item-label">头像</span>
          <div class="item-right">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar-img" />
            <div v-else class="avatar-placeholder">{{ (userInfo.nickname || 'U').charAt(0) }}</div>
            <span class="item-arrow">›</span>
          </div>
        </div>
        <div class="setting-item" @click="editField('nickname')">
          <span class="item-label">昵称</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.nickname || '未设置' }}</span>
            <span class="item-arrow">›</span>
          </div>
        </div>
        <div class="setting-item" @click="editField('phone')">
          <span class="item-label">手机号</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.phone || '未绑定' }}</span>
            <span class="item-arrow">›</span>
          </div>
        </div>
        <div class="setting-item" @click="editField('email')">
          <span class="item-label">邮箱</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.email || '未绑定' }}</span>
            <span class="item-arrow">›</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 账号安全 -->
    <div class="section">
      <div class="section-title">账号安全</div>
      <div class="setting-card">
        <div class="setting-item" @click="showPasswordDialog = true">
          <span class="item-label">修改密码</span>
          <div class="item-right">
            <span class="item-arrow">›</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 账号信息 -->
    <div class="section">
      <div class="section-title">账号信息</div>
      <div class="setting-card">
        <div class="setting-item">
          <span class="item-label">用户ID</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.userId }}</span>
          </div>
        </div>
        <div class="setting-item">
          <span class="item-label">用户名</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.username }}</span>
          </div>
        </div>
        <div class="setting-item">
          <span class="item-label">角色</span>
          <div class="item-right">
            <el-tag :type="userInfo.role === 1 ? 'danger' : 'info'" size="small">
              {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>
        <div class="setting-item">
          <span class="item-label">注册时间</span>
          <div class="item-right">
            <span class="item-value">{{ userInfo.createTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEditDialog" :title="editTitle" width="400px">
      <div class="dialog-form">
        <div class="form-group" v-if="editFieldType !== 'avatar'">
          <label>{{ editLabel }}</label>
          <el-input v-model="editValue" :placeholder="editPlaceholder" />
        </div>
        <div class="form-group" v-else>
          <label>头像</label>
          <div class="avatar-upload" @click="triggerAvatarUpload">
            <img v-if="editValue" :src="editValue" class="avatar-preview" />
            <div v-else class="avatar-upload-placeholder">
              <span>📷</span>
              <span>点击上传头像</span>
            </div>
            <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveField" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <div class="dialog-form">
        <div class="form-group">
          <label>旧密码</label>
          <el-input v-model="passForm.oldPassword" type="password" placeholder="请输入旧密码" />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <el-input v-model="passForm.newPassword" type="password" placeholder="新密码（至少6位）" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changing">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 设置页面逻辑
 * - 用户信息加载与编辑（昵称、手机号、邮箱、头像）
 * - 密码修改
 * - 输入格式校验（手机号、邮箱）
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { getUserInfo, updateProfile, changePassword, uploadAvatar } from '../api/user'

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
const showEditDialog = ref(false)
const editFieldType = ref('')
const editTitle = ref('')
const editLabel = ref('')
const editPlaceholder = ref('')
const editValue = ref('')
const saving = ref(false)
const avatarInput = ref(null)

// 修改密码弹窗
const showPasswordDialog = ref(false)
const passForm = ref({ oldPassword: '', newPassword: '' })
const changing = ref(false)

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
const editFieldConfig = {
  nickname: { title: '修改昵称', label: '昵称', placeholder: '请输入昵称' },
  phone: { title: '修改手机号', label: '手机号', placeholder: '请输入手机号' },
  email: { title: '修改邮箱', label: '邮箱', placeholder: '请输入邮箱' },
  avatar: { title: '修改头像', label: '头像', placeholder: '' }
}

const editField = (field) => {
  editFieldType.value = field
  editTitle.value = editFieldConfig[field].title
  editLabel.value = editFieldConfig[field].label
  editPlaceholder.value = editFieldConfig[field].placeholder
  editValue.value = userInfo.value[field] || ''
  showEditDialog.value = true
}

// 保存字段
const handleSaveField = async () => {
  // 输入校验
  if (editFieldType.value === 'phone' && editValue.value) {
    if (!/^1[3-9]\d{9}$/.test(editValue.value)) {
      ElMessage.warning('请输入正确的手机号')
      return
    }
  }
  if (editFieldType.value === 'email' && editValue.value) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editValue.value)) {
      ElMessage.warning('请输入正确的邮箱地址')
      return
    }
  }
  saving.value = true
  try {
    const data = {}
    data[editFieldType.value] = editValue.value
    await updateProfile(data)
    userInfo.value[editFieldType.value] = editValue.value
    userStore.updateUserInfo(data)
    showEditDialog.value = false
    ElMessage.success('保存成功')
  } catch (e) {} finally {
    saving.value = false
  }
}

// 头像上传
const triggerAvatarUpload = () => {
  avatarInput.value.click()
}

/** 选择头像文件后上传 */
const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('只支持 JPG、PNG、GIF、WebP 格式')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 2MB')
    return
  }
  saving.value = true
  try {
    const res = await uploadAvatar(file)
    editValue.value = res.data.url
  } catch (e) {} finally {
    saving.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passForm.value.oldPassword || !passForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  changing.value = true
  try {
    await changePassword(passForm.value)
    showPasswordDialog.value = false
    passForm.value = { oldPassword: '', newPassword: '' }
    ElMessage.success('密码已修改')
  } catch (e) {} finally {
    changing.value = false
  }
}
</script>

<style scoped>
.settings-page { max-width: 600px; }

.page-bar {
  display: flex; align-items: center; gap: 8px;
  background: var(--white); padding: 14px 20px;
  border-radius: var(--radius); box-shadow: var(--shadow);
  margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 700; }

.section { margin-bottom: 16px; }
.section-title {
  font-size: 13px; color: var(--text-light);
  padding: 0 0 8px;
}

.setting-card {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
}

.setting-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f5f5f5;
  cursor: pointer; transition: background 0.2s;
}
.setting-item:last-child { border-bottom: none; }
.setting-item:hover { background: #fafafa; }

.item-label { font-size: 14px; color: #333; }

.item-right { display: flex; align-items: center; gap: 12px; }

.item-value { font-size: 14px; color: #999; }

.item-arrow { font-size: 16px; color: #ccc; }

.avatar-img {
  width: 48px; height: 48px; border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, var(--red), #ff6b81);
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 20px; font-weight: 700;
}

/* 弹窗表单 */
.dialog-form { padding: 10px 0; }
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block; font-size: 13px; color: #666;
  margin-bottom: 8px;
}

/* 头像上传 */
.avatar-upload {
  width: 100px; height: 100px; border-radius: 50%;
  cursor: pointer; overflow: hidden;
  border: 2px dashed var(--border); transition: border-color 0.2s;
}
.avatar-upload:hover { border-color: var(--red); }
.avatar-preview { width: 100%; height: 100%; object-fit: cover; }
.avatar-upload-placeholder {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  align-items: center; justify-content: center; background: #f8f8f8;
  font-size: 10px; color: var(--text-light); gap: 4px;
}
</style>
