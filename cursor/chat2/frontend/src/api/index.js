import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
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
  (err) => Promise.reject(err)
)

export const tutorApi = {
  list: () => http.get('/tutors'),
  listTakeover: () => http.get('/tutors/takeover'),
}

export const orgApi = {
  tree: () => http.get('/org/tree'),
}

export const wecomApi = {
  listAll: () => http.get('/wecom-accounts'),
  listByTutor: (tutorId) => http.get(`/wecom-accounts/by-tutor/${tutorId}`),
  query: (tutorIds, params = {}) =>
    http.get('/wecom-accounts/query', { params: { tutorIds: tutorIds.join(','), ...params } }),
  filterOptions: (tutorIds) =>
    http.get('/wecom-accounts/filter-options', { params: { tutorIds: tutorIds.join(',') } }),
}

export const takeoverApi = {
  list: () => http.get('/takeover-assignments'),
  save: (data) => http.post('/takeover-assignments', data),
  batchSave: (data) => http.post('/takeover-assignments/batch', data),
  remove: (id) => http.delete(`/takeover-assignments/${id}`),
}

/** 兼容分页对象 { list, total } 与旧版数组 */
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

export const conversationApi = {
  list: (params) => http.get('/conversations', { params }).then(normalizePage),
  detail: (id) => http.get(`/conversations/${id}`),
  unrepliedCounts: () => http.get('/conversations/unreplied-counts'),
}

export const messageApi = {
  list: (conversationId) => http.get('/messages', { params: { conversationId } }),
  reply: (data) => http.post('/messages/reply', data),
}
