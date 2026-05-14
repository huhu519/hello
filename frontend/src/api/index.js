import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const userApi = {
  login: (account, password) => api.post('/user/login', null, { params: { account, password } }),
  register: (phone, password, nickname, avatar, inviteCode) =>
    api.post('/user/register', null, { params: { phone, password, nickname, avatar, inviteCode } }),
  getInfo: (userId) => api.get(`/user/info/${userId}`)
}

export const friendApi = {
  sendRequest: (userId, friendId) => api.post('/friend/request', null, { params: { userId, friendId } }),
  handleRequest: (id, accept) => api.post('/friend/handle', null, { params: { id, accept } }),
  listFriends: (userId) => api.get(`/friend/list/${userId}`)
}

export const chatApi = {
  sendMessage: (formData) => api.post('/chat/send', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getMessages: (sessionId) => api.get(`/chat/messages/${sessionId}`)
}

export const inviteApi = {
  create: (remark, maxUseCount) => api.post('/invite/create', null, { params: { remark, maxUseCount } }),
  list: () => api.get('/invite/list')
}

export default api
