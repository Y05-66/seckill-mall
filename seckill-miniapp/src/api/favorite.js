import { get, post, del } from '../utils/request'

export const addFavorite = (goodsId) => post('/favorite/' + goodsId)
export const removeFavorite = (goodsId) => del('/favorite/' + goodsId)
export const isFavorite = (goodsId) => get('/favorite/check/' + goodsId)
export const getFavoriteList = () => get('/favorite/list')
