import axios from 'axios'

const http = axios.create({
  baseURL: '/wechat/api',
  timeout: 60000,
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body.data
  },
  (err) => Promise.reject(err.response?.data?.message ? new Error(err.response.data.message) : err)
)

export function normalizePage(data) {
  if (Array.isArray(data)) {
    return { list: data, total: data.length, page: 1, pageSize: data.length }
  }
  return {
    list: data?.list ?? [],
    total: data?.total ?? 0,
    page: data?.page ?? 1,
    pageSize: data?.pageSize ?? 20,
  }
}

export const authApi = {
  login: (data) => http.post('/auth/login', data),
}

export function serializeParams(params) {
  const search = new URLSearchParams()
  Object.entries(params || {}).forEach(([key, val]) => {
    if (val === undefined || val === null || val === '') return
    if (Array.isArray(val)) {
      val.forEach((item) => search.append(key, item))
    } else {
      search.append(key, val)
    }
  })
  return search.toString()
}

export const teachingGroupApi = {
  list: () => http.get('/teaching-groups'),
  tree: () => http.get('/teaching-groups/tree'),
}

export const tutorApi = {
  list: (params) => http.get('/tutors', { params }),
  detail: (id) => http.get(`/tutors/${id}`),
}

export const takeoverManagerApi = {
  list: () => http.get('/takeover-managers'),
}

export const hostingConfigApi = {
  list: () => http.get('/hosting-configs'),
  detail: (id) => http.get(`/hosting-configs/${id}`),
  preview: (params) =>
    http.get('/hosting-configs/preview', {
      params,
      paramsSerializer: { serialize: serializeParams },
    }),
  create: (data) => http.post('/hosting-configs', data),
  activate: (id, operatorId) => http.post(`/hosting-configs/${id}/activate`, { operatorId }),
  end: (id, operatorId) => http.post(`/hosting-configs/${id}/end`, { operatorId }),
}

export const hostingAssignmentApi = {
  list: (params) => http.get('/hosting-assignments', { params }).then(normalizePage),
  stats: () => http.get('/hosting-assignments/stats'),
  release: (id, operatorId) => http.post(`/hosting-assignments/${id}/release`, { operatorId }),
  reactivate: (id, operatorId) => http.post(`/hosting-assignments/${id}/reactivate`, { operatorId }),
}

export const conversationApi = {
  list: (params) => http.get('/conversations', { params }).then(normalizePage),
  stats: (handlerUserId) => http.get('/conversations/stats', { params: { handlerUserId } }),
  detail: (id, readerUserId) =>
    http.get(`/conversations/${id}`, { params: { readerUserId } }),
  transfer: (id, data) => http.post(`/conversations/${id}/transfer`, data),
}

export const messageApi = {
  list: (conversationId) => http.get('/messages', { params: { conversationId } }),
  reply: (data) => http.post('/messages/reply', data),
}

export const transferLogApi = {
  list: (params) => http.get('/transfer-logs', { params }),
}
