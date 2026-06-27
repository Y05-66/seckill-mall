<template>
  <div class="compare-page container">
    <div class="page-header">
      <h2>商品对比</h2>
      <button v-if="compareList.length > 0" class="clear-btn" @click="handleClear">清空对比</button>
    </div>

    <div v-if="compareList.length > 0" class="compare-table">
      <table>
        <thead>
          <tr>
            <th class="label-col">属性</th>
            <th v-for="item in compareList" :key="item.id">
              <div class="compare-header">
                <img :src="item.goodsImg" :alt="item.goodsName" @error="handleImageError" />
                <div>{{ item.goodsName }}</div>
                <button class="remove-btn" @click="handleRemove(item)">×</button>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="label">价格</td>
            <td v-for="item in compareList" :key="item.id" class="price">
              ¥{{ item.goodsPrice }}
            </td>
          </tr>
          <tr>
            <td class="label">库存</td>
            <td v-for="item in compareList" :key="item.id">
              {{ item.goodsStock }} 件
            </td>
          </tr>
          <tr>
            <td class="label">商品描述</td>
            <td v-for="item in compareList" :key="item.id">
              {{ item.goodsDesc || '-' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty">
      <p>暂无对比商品</p>
      <button class="go-btn" @click="$router.push('/goods')">去添加</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { handleImageError } from '../utils/image'

const compareList = ref([])

onMounted(() => {
  try {
    const saved = localStorage.getItem('compareList')
    if (saved) {
      compareList.value = JSON.parse(saved)
    }
  } catch (e) {
    compareList.value = []
  }
})

const handleRemove = (item) => {
  compareList.value = compareList.value.filter(i => i.id !== item.id)
  localStorage.setItem('compareList', JSON.stringify(compareList.value))
}

const handleClear = () => {
  compareList.value = []
  localStorage.removeItem('compareList')
}
</script>

<style scoped>
.compare-page {}

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 22px; font-weight: 700; }
.clear-btn {
  padding: 6px 16px; background: none; border: 1px solid var(--border);
  border-radius: 6px; cursor: pointer; font-size: 13px;
}

.compare-table {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
}
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 16px; border-bottom: 1px solid #f0f0f0; text-align: center; }
th { background: #fafafa; font-weight: 600; }
.label-col { width: 120px; text-align: left; }
.label { text-align: left; font-weight: 500; color: var(--text-sub); }
.price { color: var(--red); font-weight: 700; font-size: 16px; }

.compare-header { display: flex; flex-direction: column; align-items: center; gap: 8px; position: relative; }
.compare-header img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; }
.remove-btn {
  position: absolute; top: -4px; right: -4px;
  width: 20px; height: 20px; border-radius: 50%;
  background: var(--red); color: white; border: none;
  cursor: pointer; font-size: 14px; display: flex;
  align-items: center; justify-content: center;
}

.empty {
  text-align: center; padding: 80px 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.empty p { font-size: 16px; color: var(--text-sub); margin-bottom: 20px; }
.go-btn {
  padding: 10px 24px; background: var(--red); color: white;
  border: none; border-radius: 8px; cursor: pointer;
}
</style>
