import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：添加 Token
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = 'Bearer ' + userStore.token
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401 || res.code === 403) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg))
    }
    return res
  },
  error => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(error)
    }
    // 优先显示服务端返回的错误信息
    const serverMsg = error.response?.data?.msg
    ElMessage.error(serverMsg || '网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
