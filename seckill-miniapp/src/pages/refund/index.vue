<!--
  退款申请页

  功能：提交退款申请。
  路径参数：?orderNo=订单号&amount=金额
-->
<template>
  <view class="page">
    <view class="form-card">
      <view class="form-title">退款申请</view>

      <view class="form-item">
        <text class="label">订单号</text>
        <text class="value">{{ orderNo }}</text>
      </view>

      <view class="form-item">
        <text class="label">退款金额</text>
        <text class="value price">¥{{ amount }}</text>
      </view>

      <view class="form-item">
        <text class="label">退款原因</text>
        <picker :range="reasons" @change="handleReasonChange">
          <view class="picker">
            <text>{{ selectedReason || '请选择退款原因' }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">补充说明</text>
        <textarea v-model="remark" placeholder="请输入补充说明（选填）" class="textarea" />
      </view>

      <view class="submit-btn" :class="{ disabled: !selectedReason }" @tap="handleSubmit">
        提交退款申请
      </view>
    </view>

    <!-- 退款记录 -->
    <view class="section" v-if="refundList.length > 0">
      <view class="section-title">退款记录</view>
      <view v-for="item in refundList" :key="item.id" class="refund-card">
        <view class="refund-top">
          <text class="refund-no">订单号: {{ item.orderNo }}</text>
          <text class="refund-status" :class="'status-' + item.status">
            {{ ['处理中', '已退款', '已拒绝'][item.status] || '未知' }}
          </text>
        </view>
        <view class="refund-info">
          <text class="refund-amount">¥{{ item.amount }}</text>
          <text class="refund-reason">{{ item.reason }}</text>
        </view>
        <text class="refund-time">{{ item.createTime }}</text>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { applyRefund, getUserRefunds } from '../../api/refund'
import { showToast, navigateBack } from '../../utils/nav'

const orderNo = ref('')
const amount = ref('')
const reasons = ['不想要了', '商品质量问题', '商品与描述不符', '收到商品损坏', '其他']
const selectedReason = ref('')
const remark = ref('')
const refundList = ref([])

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const options = page.$page?.options || page.options || {}
  orderNo.value = options.orderNo || ''
  amount.value = options.amount || ''

  // 加载退款记录
  try {
    const res = await getUserRefunds()
    refundList.value = res.data || []
  } catch (e) {}
})

const handleReasonChange = (e) => {
  selectedReason.value = reasons[e.detail.value]
}

const handleSubmit = async () => {
  if (!selectedReason.value) {
    showToast({ title: '请选择退款原因', icon: 'none' })
    return
  }

  try {
    await applyRefund({
      orderNo: orderNo.value,
      type: 1,
      amount: Number(amount.value),
      reason: selectedReason.value + (remark.value ? '：' + remark.value : '')
    })
    showToast({ title: '申请已提交', icon: 'success' })
    setTimeout(() => navigateBack(), 1500)
  } catch (e) {}
}
</script>

<style scoped>
.page {
  background: #f5f5f5; min-height: 100vh; padding: 20rpx;
}

.form-card {
  background: #fff; border-radius: var(--radius);
  padding: 32rpx; box-shadow: var(--shadow);
}
.form-title {
  font-size: 32rpx; font-weight: 700; color: var(--text-primary);
  margin-bottom: 32rpx;
}

.form-item { margin-bottom: 28rpx; }
.form-item .label {
  display: block; font-size: 26rpx; color: var(--text-secondary);
  margin-bottom: 12rpx;
}
.form-item .value {
  font-size: 28rpx; color: var(--text-primary);
}
.form-item .value.price {
  font-size: 36rpx; font-weight: 700; color: var(--danger);
}

.picker {
  display: flex; justify-content: space-between; align-items: center;
  height: 80rpx; border: 1rpx solid #e5e5e5; border-radius: 12rpx;
  padding: 0 20rpx; font-size: 28rpx; color: var(--text-primary);
}
.arrow { color: var(--text-hint); font-size: 32rpx; }

.textarea {
  width: 100%; height: 160rpx; border: 1rpx solid #e5e5e5;
  border-radius: 12rpx; padding: 20rpx; font-size: 28rpx;
}

.submit-btn {
  margin-top: 40rpx; height: 88rpx; background: var(--danger);
  color: #fff; display: flex; align-items: center;
  justify-content: center; border-radius: 12rpx;
  font-size: 30rpx; font-weight: 600;
}
.submit-btn.disabled { opacity: 0.5; }

.section { margin-top: 40rpx; }
.section-title {
  font-size: 28rpx; font-weight: 600; color: var(--text-primary);
  margin-bottom: 20rpx;
}

.refund-card {
  background: #fff; border-radius: var(--radius);
  padding: 24rpx; margin-bottom: 16rpx; box-shadow: var(--shadow);
}
.refund-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16rpx;
}
.refund-no { font-size: 24rpx; color: var(--text-secondary); }
.refund-status { font-size: 24rpx; font-weight: 600; }
.status-0 { color: #ff9800; }
.status-1 { color: #4caf50; }
.status-2 { color: var(--danger); }

.refund-info {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12rpx;
}
.refund-amount { font-size: 32rpx; font-weight: 700; color: var(--danger); }
.refund-reason { font-size: 24rpx; color: var(--text-secondary); }
.refund-time { font-size: 22rpx; color: var(--text-hint); }
</style>
