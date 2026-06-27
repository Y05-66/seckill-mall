<template>
  <div class="noti-page container">
    <div class="page-header">
      <h2>消息通知</h2>
      <button v-if="notifications.length > 0" class="read-all-btn" @click="handleMarkAllRead">
        全部已读
      </button>
    </div>

    <div v-if="notifications.length > 0" class="noti-list">
      <div v-for="item in notifications" :key="item.id"
        class="noti-item" :class="{ unread: item.isRead === 0 }"
        @click="handleRead(item)">
        <div class="noti-icon">
          {{ item.type === 'order' ? '📦' : item.type === 'promotion' ? '🎁' : '📢' }}
        </div>
        <div class="noti-content">
          <div class="noti-title">{{ item.title }}</div>
          <div class="noti-text">{{ item.content }}</div>
          <div class="noti-time">{{ item.createTime }}</div>
        </div>
        <div v-if="item.isRead === 0" class="noti-dot"></div>
      </div>
    </div>

    <div v-else class="empty">
      <p>暂无消息</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNotifications, markAsRead, markAllAsRead } from '../api/notification'

const notifications = ref([])

onMounted(loadNotifications)

async function loadNotifications() {
  try {
    const res = await getNotifications()
    notifications.value = res.data || []
  } catch (e) {}
}

const handleRead = async (item) => {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
    } catch (e) {}
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
  } catch (e) {}
}
</script>

<style scoped>
.noti-page {}

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 22px; font-weight: 700; }
.read-all-btn {
  padding: 6px 16px; background: none; border: 1px solid var(--border);
  border-radius: 6px; cursor: pointer; font-size: 13px;
}
.read-all-btn:hover { color: var(--red); border-color: var(--red); }

.noti-list { display: flex; flex-direction: column; gap: 8px; }
.noti-item {
  display: flex; align-items: flex-start; gap: 14px;
  background: var(--white); padding: 16px 20px;
  border-radius: var(--radius); box-shadow: var(--shadow);
  cursor: pointer; transition: background 0.2s;
  position: relative;
}
.noti-item:hover { background: #fafafa; }
.noti-item.unread { background: #fff8f8; }

.noti-icon { font-size: 28px; flex-shrink: 0; }
.noti-content { flex: 1; }
.noti-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.noti-text { font-size: 13px; color: var(--text-sub); margin-bottom: 6px; }
.noti-time { font-size: 12px; color: var(--text-light); }

.noti-dot {
  width: 8px; height: 8px; background: var(--red);
  border-radius: 50%; flex-shrink: 0; margin-top: 6px;
}

.empty {
  text-align: center; padding: 80px 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.empty p { font-size: 16px; color: var(--text-sub); }
</style>
