import { get, post } from '../utils/request'

export const getAvailableCoupons = () => get('/coupon/available')
export const claimCoupon = (id) => post('/coupon/claim/' + id)
export const getMyCoupons = () => get('/coupon/my')
