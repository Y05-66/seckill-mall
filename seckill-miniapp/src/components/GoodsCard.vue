<!--
  GoodsCard.vue - 普通商品卡片组件

  功能：展示商品图片、名称、价格、库存预警。
  点击整张卡片触发 click 事件。

  Props:
    - item {Object} - 商品数据 { id, goodsName, goodsTitle, goodsImg, goodsPrice, goodsStock }

  Events:
    - click(item) - 点击卡片时触发

  用法：<GoodsCard :item="goods" @click="goDetail" />
-->
<template>
  <view class="goods-card" @tap="$emit('click', item)">
    <view class="img-wrap">
      <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="card-img" @error="handleImageError" />
      <view v-if="item.goodsStock <= 10 && item.goodsStock > 0" class="stock-badge">
        仅剩{{ item.goodsStock }}件
      </view>
    </view>
    <view class="card-body">
      <view class="card-name">{{ item.goodsName }}</view>
      <view class="card-title">{{ item.goodsTitle }}</view>
      <view class="card-bottom">
        <view class="card-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ item.goodsPrice }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { handleImageError, getFullImageUrl } from '../utils/image'

defineProps({ item: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style scoped>
.goods-card {
  width: calc(50% - 8rpx);
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: transform 0.2s;
}
.goods-card:active { transform: scale(0.98); }

.img-wrap { position: relative; }
.card-img { width: 100%; height: 340rpx; display: block; }
.stock-badge {
  position: absolute; top: 12rpx; left: 12rpx;
  padding: 4rpx 14rpx;
  background: rgba(239, 71, 111, 0.9);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
}

.card-body { padding: 20rpx; }
.card-name {
  font-size: 26rpx; font-weight: 600; color: var(--text-primary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.card-title {
  font-size: 22rpx; color: var(--text-hint); margin-top: 6rpx;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.card-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 14rpx; }
.card-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 22rpx; color: var(--danger); font-weight: 600; }
.price-value { font-size: 34rpx; color: var(--danger); font-weight: 700; }
</style>
