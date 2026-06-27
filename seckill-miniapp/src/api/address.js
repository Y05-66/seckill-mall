import { get, post, put, del } from '../utils/request'

export const addAddress = (data) => post('/address', data)
export const getAddressList = () => get('/address/list')
export const getAddress = (id) => get('/address/' + id)
export const updateAddress = (id, data) => put('/address/' + id, data)
export const deleteAddress = (id) => del('/address/' + id)
export const setDefaultAddress = (id) => put('/address/' + id + '/default')
export const getDefaultAddress = () => get('/address/default')
