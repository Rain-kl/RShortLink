import axios from 'axios'
import { getToken, getUsername } from '@/core/auth'
import { isNotEmpty } from '@/utils/plugins'

const publicHttp = axios.create({
  timeout: 15000
})

publicHttp.interceptors.request.use((config) => {
  config.headers.Token = isNotEmpty(getToken()) ? getToken() : ''
  config.headers.Username = isNotEmpty(getUsername()) ? getUsername() : ''
  return config
})

export default {
  create(data) {
    return publicHttp({
      url: '/api/short-link/v1/create',
      method: 'post',
      data
    })
  }
}