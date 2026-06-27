<template>
  <div class="profile-page container">
    <!-- 用户信息卡片 -->
    <div class="profile-card">
      <div class="pc-header">
        <div class="pc-avatar" @click="$router.push('/settings')">
          <img v-if="userAvatar" :src="userAvatar" class="avatar-img" @error="userAvatar = ''" />
          <span v-else>{{ (userStore.nickname || userStore.username || 'U').charAt(0) }}</span>
          <div class="avatar-edit">✏️</div>
        </div>
        <div class="pc-meta">
          <h2>{{ userStore.nickname || userStore.username }}</h2>
          <p>ID: {{ userStore.userId }} · {{ userStore.role === 1 ? '管理员' : '普通用户' }}</p>
        </div>
        <button class="edit-btn" @click="$router.push('/settings')">设置</button>
      </div>
    </div>

    <!-- 订单快捷入口 -->
    <div class="quick-row">
      <div class="quick-item" @click="$router.push('/orders')">
        <span class="qi-icon">📋</span>
        <span>全部订单</span>
      </div>
      <div class="quick-item" @click="$router.push('/orders?status=0')">
        <span class="qi-icon">💳</span>
        <span>待支付</span>
      </div>
      <div class="quick-item" @click="$router.push('/orders?status=1')">
        <span class="qi-icon">✅</span>
        <span>已支付</span>
      </div>
      <div class="quick-item" @click="$router.push('/orders?status=2')">
        <span class="qi-icon">❌</span>
        <span>已取消</span>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card">
      <div class="menu-item" @click="$router.push('/settings')">
        <span class="mi-icon">⚙️</span>
        <span class="mi-text">设置</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/address')">
        <span class="mi-icon">📍</span>
        <span class="mi-text">收货地址</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/favorites')">
        <span class="mi-icon">❤️</span>
        <span class="mi-text">我的收藏</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/notifications')">
        <span class="mi-icon">🔔</span>
        <span class="mi-text">消息通知</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/coupons')">
        <span class="mi-icon">🎫</span>
        <span class="mi-text">优惠券</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/points')">
        <span class="mi-icon">⭐</span>
        <span class="mi-text">我的积分</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/seckill')">
        <span class="mi-icon">🔥</span>
        <span class="mi-text">限时秒杀</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/goods')">
        <span class="mi-icon">🛒</span>
        <span class="mi-text">全部商品</span>
        <span class="mi-arrow">›</span>
      </div>
      <div class="menu-item" v-if="userStore.role === 1" @click="$router.push('/admin')">
        <span class="mi-icon">⚙️</span>
        <span class="mi-text">管理后台</span>
        <span class="mi-arrow">›</span>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-wrap">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { getUserInfo } from '../api/user'

const router = useRouter()

const userStore = useUserStore()
const userAvatar = ref('')

onMounted(async () => {
  try {
    const res = await getUserInfo()
    userAvatar.value = res.data.avatar || ''
  } catch (e) {}
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    // 用户点击取消
  }
}
</script>

<style scoped>
.profile-page { max-width: 600px; }

.profile-card {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden; margin-bottom: 16px;
}

.pc-header {
  display: flex; align-items: center; gap: 16px;
  background: linear-gradient(135deg, var(--red) 0%, #ff6b81 100%);
  padding: 24px; color: white; position: relative;
}
.pc-avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.2); display: flex;
  align-items: center; justify-content: center;
  font-size: 24px; font-weight: 800; cursor: pointer;
  position: relative; overflow: hidden;
}
.avatar-img {
  width: 100%; height: 100%; object-fit: cover;
}
.avatar-edit {
  position: absolute; bottom: -2px; right: -2px;
  font-size: 14px; background: white; border-radius: 50%;
  width: 20px; height: 20px; display: flex;
  align-items: center; justify-content: center;
}
.pc-meta { flex: 1; }
.pc-meta h2 { font-size: 18px; margin-bottom: 2px; }
.pc-meta p { font-size: 12px; opacity: 0.9; }
.edit-btn {
  background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4);
  color: white; padding: 6px 14px; border-radius: 16px;
  font-size: 12px; cursor: pointer;
}

.quick-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  margin-bottom: 16px;
}
.quick-item {
  background: var(--white); border-radius: var(--radius);
  padding: 16px 8px; text-align: center; cursor: pointer;
  box-shadow: var(--shadow); transition: all 0.2s;
}
.quick-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); }
.qi-icon { display: block; font-size: 24px; margin-bottom: 4px; }
.quick-item span:last-child { font-size: 12px; color: var(--text-sub); }

.menu-card {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
}
.menu-item {
  display: flex; align-items: center; padding: 14px 20px;
  cursor: pointer; border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:hover { background: #fafafa; }
.mi-icon { font-size: 18px; margin-right: 10px; }
.mi-text { flex: 1; font-size: 14px; }
.mi-arrow { font-size: 16px; color: var(--text-light); }

/* 退出登录 */
.logout-wrap {
  margin-top: 16px;
}
.logout-btn {
  width: 100%;
  padding: 12px;
  background: var(--white);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--red);
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow);
  transition: all 0.2s;
}
.logout-btn:hover {
  background: #fff0f0;
  border-color: var(--red);
}
</style>
