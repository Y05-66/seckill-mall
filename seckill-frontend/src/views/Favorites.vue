<template>
  <div class="favorites-page container">
    <div class="page-header">
      <h2>我的收藏</h2>
      <span class="count">共 {{ favorites.length }} 件商品</span>
    </div>

    <div v-if="favorites.length > 0" class="goods-grid">
      <div v-for="item in favorites" :key="item.id" class="goods-card" @click="$router.push('/goods/' + item.id)">
        <div class="gc-img">
          <img :src="item.goodsImg" :alt="item.goodsName" loading="lazy" @error="handleImageError" />
          <button class="remove-btn" @click.stop="handleRemove(item)">取消收藏</button>
        </div>
        <div class="gc-body">
          <div class="gc-name">{{ item.goodsName }}</div>
          <div class="gc-price">
            <span class="gc-yen">¥</span>
            <span class="gc-num">{{ item.goodsPrice }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <p>暂无收藏商品</p>
      <button class="go-btn" @click="$router.push('/goods')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoriteList, removeFavorite } from '../api/favorite'
import { handleImageError } from '../utils/image'

const favorites = ref([])

onMounted(loadFavorites)

async function loadFavorites() {
  try {
    const res = await getFavoriteList()
    favorites.value = res.data || []
  } catch (e) {}
}

const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm('确认取消收藏？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await removeFavorite(item.id)
    await loadFavorites()
    ElMessage.success('已取消收藏')
  } catch (e) {}
}
</script>

<style scoped>
.favorites-page {}

.page-header {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 22px; font-weight: 700; }
.count { font-size: 14px; color: var(--text-light); }

.goods-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
.goods-card {
  background: var(--white); border-radius: var(--radius);
  overflow: hidden; cursor: pointer;
  box-shadow: var(--shadow); transition: all 0.3s;
}
.goods-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-hover); }

.gc-img {
  position: relative; width: 100%; aspect-ratio: 1; overflow: hidden;
}
.gc-img img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn {
  position: absolute; bottom: 8px; right: 8px;
  padding: 4px 10px; background: rgba(0,0,0,0.6); color: white;
  border: none; border-radius: 4px; font-size: 11px;
  cursor: pointer; opacity: 0; transition: opacity 0.2s;
}
.goods-card:hover .remove-btn { opacity: 1; }

.gc-body { padding: 10px; }
.gc-name {
  font-size: 13px; font-weight: 500; margin-bottom: 6px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.gc-price { color: var(--red); }
.gc-yen { font-size: 12px; font-weight: 600; }
.gc-num { font-size: 18px; font-weight: 800; }

.empty {
  text-align: center; padding: 80px 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.empty p { font-size: 16px; color: var(--text-sub); margin-bottom: 20px; }
.go-btn {
  padding: 10px 24px; background: var(--red); color: white;
  border: none; border-radius: 8px; font-size: 14px;
  font-weight: 600; cursor: pointer;
}
</style>
