<!--
  收货地址管理页

  功能：查看、新增、编辑、删除收货地址，设置默认地址。
-->
<template>
  <view class="page">
    <!-- 地址列表 -->
    <view v-if="addressList.length > 0" class="address-list">
      <view v-for="item in addressList" :key="item.id" class="address-card">
        <view class="addr-info" @tap="handleSelect(item)">
          <view class="addr-top">
            <text class="addr-name">{{ item.receiverName }}</text>
            <text class="addr-phone">{{ item.receiverPhone }}</text>
            <view v-if="item.isDefault === 1" class="default-tag">默认</view>
          </view>
          <view class="addr-detail">
            {{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}
          </view>
        </view>
        <view class="addr-actions">
          <text class="action-btn" @tap="handleSetDefault(item)" v-if="item.isDefault !== 1">设为默认</text>
          <text class="action-btn" @tap="handleEdit(item)">编辑</text>
          <text class="action-btn delete" @tap="handleDelete(item)">删除</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="addressList.length === 0 && !loading" class="empty">
      <text class="empty-icon">📍</text>
      <text class="empty-text">暂无收货地址</text>
      <view class="empty-add-btn" @tap="handleAdd">
        <text>+ 添加收货地址</text>
      </view>
    </view>

    <!-- 底部添加按钮 -->
    <view class="add-btn" @tap="handleAdd">
      <text class="add-btn-icon">＋</text>
      <text>新增收货地址</text>
    </view>

    <!-- 新增/编辑弹窗 -->
    <view v-if="showDialog" class="dialog-mask" @tap.self="showDialog = false">
      <view class="dialog">
        <view class="dialog-title">{{ isEdit ? '编辑地址' : '新增地址' }}</view>
        <view class="form-item">
          <text class="label">收货人</text>
          <input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </view>
        <view class="form-item">
          <text class="label">手机号</text>
          <input v-model="form.receiverPhone" placeholder="请输入手机号" type="number" maxlength="11" />
        </view>
        <view class="form-item">
          <text class="label">所在地区</text>
          <picker mode="region" @change="onRegionChange" :value="[form.province, form.city, form.district]">
            <view class="region-picker">
              <text v-if="form.province" class="region-text">{{ form.province }} {{ form.city }} {{ form.district }}</text>
              <text v-else class="region-placeholder">请选择省市区</text>
              <text class="region-arrow">›</text>
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">详细地址</text>
          <input v-model="form.detailAddress" placeholder="请输入详细地址" />
        </view>
        <view class="dialog-actions">
          <view class="btn-cancel" @tap="showDialog = false">取消</view>
          <view class="btn-confirm" @tap="handleSubmit">确定</view>
        </view>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '../../api/address'
import { showToast, showModal } from '../../utils/nav'

const addressList = ref([])
const loading = ref(true)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})

// 加载地址列表
const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

onShow(loadAddresses)

// 地区选择
/** 原生地区选择器回调，更新省市区字段 */
const onRegionChange = (e) => {
  const [province, city, district] = e.detail.value
  form.value.province = province
  form.value.city = city
  form.value.district = district
}

// 新增地址
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' }
  showDialog.value = true
}

// 编辑地址
const handleEdit = (item) => {
  isEdit.value = true
  editId.value = item.id
  form.value = {
    receiverName: item.receiverName,
    receiverPhone: item.receiverPhone,
    province: item.province,
    city: item.city,
    district: item.district,
    detailAddress: item.detailAddress
  }
  showDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!form.value.receiverName) {
    showToast({ title: '请输入收货人', icon: 'none' })
    return
  }
  if (!form.value.receiverPhone || form.value.receiverPhone.length !== 11) {
    showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!form.value.detailAddress) {
    showToast({ title: '请输入详细地址', icon: 'none' })
    return
  }

  try {
    if (isEdit.value) {
      await updateAddress(editId.value, form.value)
      showToast({ title: '修改成功', icon: 'success' })
    } else {
      await addAddress(form.value)
      showToast({ title: '添加成功', icon: 'success' })
    }
    showDialog.value = false
    await loadAddresses()
  } catch (e) {}
}

// 删除地址
const handleDelete = async (item) => {
  try {
    const res = await showModal({ title: '提示', content: '确认删除该地址？' })
    if (!res.confirm) return
    await deleteAddress(item.id)
    showToast({ title: '已删除', icon: 'success' })
    await loadAddresses()
  } catch (e) {}
}

// 设为默认
const handleSetDefault = async (item) => {
  try {
    await setDefaultAddress(item.id)
    showToast({ title: '已设为默认', icon: 'success' })
    await loadAddresses()
  } catch (e) {}
}

// 选择地址（从订单页面跳转来时）
const handleSelect = (item) => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.$emit('selectAddress', item)
    uni.navigateBack()
  }
}
</script>

<style scoped>
.page {
  background: #f5f5f5; min-height: 100vh;
  padding: 20rpx; padding-bottom: 120rpx;
}

.address-list { display: flex; flex-direction: column; gap: 20rpx; }
.address-card {
  background: #fff; border-radius: 16rpx;
  padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.addr-info { padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0; }
.addr-top { display: flex; align-items: center; gap: 16rpx; margin-bottom: 12rpx; }
.addr-name { font-size: 30rpx; font-weight: 600; color: var(--text-primary); }
.addr-phone { font-size: 26rpx; color: var(--text-secondary); }
.default-tag {
  padding: 2rpx 12rpx; background: var(--danger-light);
  color: var(--danger); font-size: 20rpx; border-radius: 6rpx;
}
.addr-detail { font-size: 26rpx; color: var(--text-secondary); line-height: 1.5; }

.addr-actions {
  display: flex; justify-content: flex-end; gap: 32rpx;
  padding-top: 20rpx;
}
.action-btn { font-size: 24rpx; color: var(--text-secondary); }
.action-btn.delete { color: var(--danger); }

.empty {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 80rpx 0 40rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: var(--text-hint); margin-bottom: 32rpx; }
.empty-add-btn {
  padding: 20rpx 40rpx; background: var(--danger); color: #fff;
  border-radius: 12rpx; font-size: 28rpx; font-weight: 600;
}

.add-btn {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: linear-gradient(135deg, #e4393c, #ff6b81); color: #fff;
  height: 100rpx; display: flex; align-items: center;
  justify-content: center; gap: 12rpx;
  font-size: 30rpx; font-weight: 600;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.1);
}
.add-btn-icon { font-size: 36rpx; }

/* 弹窗 */
.dialog-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex;
  align-items: center; justify-content: center; z-index: 999;
}
.dialog {
  background: #fff; border-radius: 24rpx; padding: 36rpx;
  width: 85%; max-height: 80vh; overflow-y: auto;
}
.dialog-title {
  font-size: 32rpx; font-weight: 700; color: var(--text-primary);
  text-align: center; margin-bottom: 32rpx;
}
.form-item { margin-bottom: 24rpx; }
.form-item .label {
  display: block; font-size: 26rpx; color: var(--text-secondary);
  margin-bottom: 12rpx;
}
.form-item input {
  width: 100%; height: 80rpx; border: 1rpx solid #e5e5e5;
  border-radius: 12rpx; padding: 0 20rpx; font-size: 28rpx;
}
.region-picker {
  display: flex; align-items: center; justify-content: space-between;
  width: 100%; height: 80rpx; border: 1rpx solid #e5e5e5;
  border-radius: 12rpx; padding: 0 20rpx;
}
.region-text { font-size: 28rpx; color: var(--text-primary); }
.region-placeholder { font-size: 28rpx; color: var(--text-hint); }
.region-arrow { font-size: 32rpx; color: var(--text-hint); }
.dialog-actions {
  display: flex; gap: 20rpx; margin-top: 32rpx;
}
.btn-cancel, .btn-confirm {
  flex: 1; height: 80rpx; display: flex;
  align-items: center; justify-content: center;
  border-radius: 12rpx; font-size: 28rpx; font-weight: 600;
}
.btn-cancel { background: #f5f5f5; color: var(--text-secondary); }
.btn-confirm { background: var(--danger); color: #fff; }
</style>
