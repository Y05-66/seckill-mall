import { get, post, put, del } from '../utils/request'

export const addToCart = (data) => post('/cart/add', data)
export const getCartList = () => get('/cart/list')
export const updateCartQuantity = (data) => put('/cart/update', data)
export const deleteCartItem = (cartId) => del('/cart/' + cartId)
export const clearCart = () => del('/cart/clear')
export const toggleCheck = (data) => put('/cart/check', data)
export const toggleCheckAll = (data) => put('/cart/check-all', data)
export const getCartCount = () => get('/cart/count')
