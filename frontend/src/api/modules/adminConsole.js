import http from '../axios'

export default {
  getOverview() {
    return http({
      url: '/overview',
      method: 'get'
    })
  },
  getLinksPage(params) {
    return http({
      url: '/links/page',
      method: 'get',
      params
    })
  },
  getSettings() {
    return http({
      url: '/system-settings',
      method: 'get'
    })
  },
  updateSettings(data) {
    return http({
      url: '/system-settings',
      method: 'put',
      data
    })
  }
}