<!--
  管理后台 - 秒杀商品管理

  功能：
  1. 秒杀商品列表（ID、名称、秒杀价、原价、库存、时间、状态）
  2. 新增秒杀商品（弹窗表单：关联商品ID、秒杀价、库存、时间）
  3. 编辑秒杀商品（仅未开始状态可编辑）
  4. 删除秒杀商品（进行中不可删除）
  5. 补充库存（弹窗输入数量）

  权限：需管理员（role=1），onMounted 中校验
  表单校验：秒杀价>0、库存>0、时间非空、结束时间>开始时间
-->
<template>
  <view class="container">
    <view class="header">
      <text class="title">秒杀商品管理</text>
      <view class="add-btn" @tap="openAdd">+ 新增</view>
    </view>

    <view v-for="item in goodsList" :key="item.id" class="goods-item">
      <view class="item-info">
        <text class="item-name">{{ item.goodsName }}</text>
        <text class="item-price">¥{{ item.seckillPrice }} (原价¥{{ item.goodsPrice }})</text>
        <text class="item-stock">库存: {{ item.stockCount }}</text>
        <view class="item-tag" :class="'tag-' + item.status">{{ ['未开始','进行中','已结束'][item.status] }}</view>
      </view>
      <view class="item-actions">
        <view v-if="item.status === 0" class="act-btn" @tap="openEdit(item)">编辑</view>
        <view v-if="item.status === 1" class="act-btn" @tap="openStock(item)">补库存</view>
        <view v-if="item.status !== 1" class="act-btn danger" @tap="handleDelete(item)">删除</view>
      </view>
    </view>

    <view v-if="goodsList.length === 0" class="empty">暂无数据</view>

    <!-- 弹窗表单 -->
    <view v-if="formVisible" class="modal-overlay" @tap="formVisible = false">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">{{ isEdit ? '编辑' : '新增' }}秒杀商品</view>
        <view v-if="!isEdit" class="form-item">
          <text class="form-label">关联商品ID</text>
          <input v-model="form.goodsId" type="number" placeholder="普通商品ID" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">秒杀价</text>
          <input v-model="form.seckillPrice" type="digit" placeholder="秒杀价" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">秒杀库存</text>
          <input v-model="form.stockCount" type="number" placeholder="库存数量" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">开始时间</text>
          <input v-model="form.startTime" placeholder="YYYY-MM-DD HH:mm:ss" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">结束时间</text>
          <input v-model="form.endTime" placeholder="YYYY-MM-DD HH:mm:ss" class="form-input" />
        </view>
        <view class="modal-btns">
          <view class="modal-btn cancel" @tap="formVisible = false">取消</view>
          <view class="modal-btn confirm" @tap="handleSubmit">确定</view>
        </view>
      </view>
    </view>

    <!-- 补库存弹窗 -->
    <view v-if="stockVisible" class="modal-overlay" @tap="stockVisible = false">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">补充库存</view>
        <view class="form-item">
          <text class="form-label">增加数量</text>
          <input v-model="stockCount" type="number" placeholder="数量" class="form-input" />
        </view>
        <view class="modal-btns">
          <view class="modal-btn cancel" @tap="stockVisible = false">取消</view>
          <view class="modal-btn confirm" @tap="handleAddStock">确定</view>
        </view>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getAdminGoodsList, addGoods, updateGoods, deleteGoods, addStock } from '../../api/admin'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const goodsList = ref([])
const formVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
import { showToast, navigateBack, showModal } from '../../utils/nav'

const form = ref({})
const stockVisible = ref(false)
const stockGoodsId = ref(null)
const stockCount = ref(10)

const loadList = async () => {
  try { const res = await getAdminGoodsList(); goodsList.value = res.data || [] } catch (e) {}
}

onMounted(() => {
  if (!userStore.isAdmin) { showToast({ title: '无权限', icon: 'none' }); navigateBack(); return }
  loadList()
})

const openAdd = () => {
  isEdit.value = false; editId.value = null
  form.value = { goodsId: '', seckillPrice: '', stockCount: '', startTime: '', endTime: '' }
  formVisible.value = true
}

const openEdit = (item) => {
  isEdit.value = true; editId.value = item.id
  form.value = { seckillPrice: item.seckillPrice, stockCount: item.stockCount, startTime: item.startTime, endTime: item.endTime }
  formVisible.value = true
}

const openStock = (item) => {
  stockGoodsId.value = item.id; stockCount.value = 10; stockVisible.value = true
}

const handleSubmit = async () => {
  const data = { ...form.value }
  if (!isEdit.value && (!data.goodsId || Number(data.goodsId) <= 0)) {
    showToast({ title: '请输入有效的商品ID', icon: 'none' }); return
  }
  if (!data.seckillPrice || Number(data.seckillPrice) <= 0) {
    showToast({ title: '请输入有效的秒杀价', icon: 'none' }); return
  }
  if (!data.stockCount || Number(data.stockCount) <= 0) {
    showToast({ title: '请输入有效的库存数量', icon: 'none' }); return
  }
  if (!data.startTime || !data.endTime) {
    showToast({ title: '请填写开始和结束时间', icon: 'none' }); return
  }
  if (data.endTime <= data.startTime) {
    showToast({ title: '结束时间必须晚于开始时间', icon: 'none' }); return
  }
  try {
    if (data.goodsId) data.goodsId = Number(data.goodsId)
    data.seckillPrice = Number(data.seckillPrice)
    data.stockCount = Number(data.stockCount)
    if (isEdit.value) { await updateGoods(editId.value, data) }
    else { await addGoods(data) }
    showToast({ title: '操作成功', icon: 'success' })
    formVisible.value = false; loadList()
  } catch (e) {}
}

const handleDelete = async (item) => {
  const res = await showModal({ title: '确认删除', content: '确认删除该秒杀商品？' })
  if (res.confirm) {
    try { await deleteGoods(item.id); showToast({ title: '已删除', icon: 'success' }); loadList() } catch (e) {}
  }
}

const handleAddStock = async () => {
  try {
    await addStock(stockGoodsId.value, Number(stockCount.value))
    showToast({ title: '补充成功', icon: 'success' })
    stockVisible.value = false; loadList()
  } catch (e) {}
}
</script>

<style scoped>
.container { padding: 20rpx; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.title { font-size: 32rpx; font-weight: bold; }
.add-btn { background: #409eff; color: #fff; padding: 10rpx 24rpx; border-radius: 8rpx; font-size: 24rpx; }
.goods-item { background: #fff; border-radius: 12rpx; padding: 20rpx; margin-bottom: 16rpx; }
.item-name { font-size: 28rpx; font-weight: bold; display: block; }
.item-price { font-size: 24rpx; color: #f56c6c; display: block; margin-top: 8rpx; }
.item-stock { font-size: 22rpx; color: #666; display: block; margin-top: 4rpx; }
.item-tag { display: inline-block; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; margin-top: 8rpx; }
.tag-0 { background: #e6f7ff; color: #409eff; }
.tag-1 { background: #fff7e6; color: #e6a23c; }
.tag-2 { background: #f5f5f5; color: #999; }
.item-actions { display: flex; gap: 16rpx; margin-top: 12rpx; }
.act-btn { font-size: 22rpx; padding: 8rpx 20rpx; border-radius: 6rpx; background: #f0f0f0; color: #333; }
.act-btn.danger { color: #f56c6c; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 650rpx; background: #fff; border-radius: 16rpx; padding: 40rpx; }
.modal-title { font-size: 30rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; }
.form-item { margin-bottom: 20rpx; }
.form-label { font-size: 24rpx; color: #666; display: block; margin-bottom: 8rpx; }
.form-input { border: 1px solid #dcdfe6; border-radius: 8rpx; padding: 14rpx 16rpx; font-size: 26rpx; }
.modal-btns { display: flex; gap: 20rpx; margin-top: 30rpx; }
.modal-btn { flex: 1; text-align: center; padding: 16rpx; border-radius: 8rpx; font-size: 26rpx; }
.modal-btn.cancel { background: #f5f5f5; color: #666; }
.modal-btn.confirm { background: #409eff; color: #fff; }
</style>
