<template>
  <div class="chat-container">
    <div class="sidebar">
      <div class="user-info">
        <el-avatar :size="40">{{ user?.nickname?.charAt(0) }}</el-avatar>
        <div class="user-detail">
          <div class="nickname">{{ user?.nickname }}</div>
          <div class="user-id">ID: {{ user?.userId }}</div>
        </div>
        <el-button type="text" @click="logout">退出</el-button>
      </div>
      
      <div class="add-friend">
        <el-input v-model="addFriendInput" placeholder="输入用户ID添加好友" style="flex: 1; margin-right: 10px;" />
        <el-button type="primary" @click="addFriend">添加</el-button>
      </div>

      <div class="friend-list">
        <div class="list-title">好友列表</div>
        <div
          v-for="friend in friends"
          :key="friend.userId"
          class="friend-item"
          :class="{ active: currentSessionId === getSessionId(friend.userId) }"
          @click="selectFriend(friend)"
        >
          <el-avatar :size="36">{{ friend.nickname?.charAt(0) }}</el-avatar>
          <span>{{ friend.nickname }}</span>
        </div>
      </div>

      <div class="public-chat">
        <div
          class="friend-item"
          :class="{ active: currentSessionId === 'public' }"
          @click="selectPublic()"
        >
          <el-avatar :size="36" style="background: #409eff;">公</el-avatar>
          <span>公共聊天室</span>
        </div>
      </div>
    </div>

    <div class="chat-main">
      <div class="chat-header">{{ currentChatName }}</div>
      
      <div class="messages" ref="messagesRef">
        <div v-for="msg in messages" :key="msg.id" class="message" :class="{ 'self': msg.senderId === user?.userId }">
          <div v-if="msg.senderId !== user?.userId" class="avatar">
            <el-avatar :size="32">{{ getUserNickname(msg.senderId)?.charAt(0) }}</el-avatar>
          </div>
          <div class="content-wrapper">
            <div v-if="msg.senderId !== user?.userId" class="sender-name">{{ getUserNickname(msg.senderId) }}</div>
            <div class="message-content">
              <div v-if="msg.messageType === 0" class="text-message">{{ msg.content }}</div>
              <div v-else class="file-message">
                <el-link :href="msg.fileUrl" target="_blank">
                  <el-icon><Document /></el-icon>
                  {{ msg.fileUrl.split('_').pop() }}
                </el-link>
              </div>
            </div>
            <div class="time">{{ formatTime(msg.createTime) }}</div>
          </div>
          <div v-if="msg.senderId === user?.userId" class="avatar">
            <el-avatar :size="32">{{ user?.nickname?.charAt(0) }}</el-avatar>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="tools">
          <el-upload
            :show-file-list="false"
            :before-upload="handleFileUpload"
            style="display: inline-block;"
          >
            <el-button type="text">
              <el-icon><Folder /></el-icon> 上传文件
            </el-button>
          </el-upload>
        </div>
        <el-input
          v-model="messageInput"
          type="textarea"
          :rows="3"
          placeholder="输入消息（禁止表情）..."
          @keydown.ctrl.enter="sendMessage"
        />
        <div class="send-btn">
          <el-button type="primary" @click="sendMessage">发送 (Ctrl+Enter)</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import { friendApi, chatApi } from '../api'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user')))
const friends = ref([])
const messages = ref([])
const currentSessionId = ref('public')
const currentChatName = ref('公共聊天室')
const messageInput = ref('')
const addFriendInput = ref('')
const messagesRef = ref(null)
let stompClient = null

const getSessionId = (friendId) => {
  const ids = [user.value.userId, friendId].sort()
  return `${ids[0]}_${ids[1]}`
}

const getUserNickname = (userId) => {
  if (userId === user.value.userId) return user.value.nickname
  const f = friends.value.find(x => x.userId === userId)
  return f?.nickname || userId
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const loadFriends = async () => {
  const res = await friendApi.listFriends(user.value.userId)
  if (res.data.code === 200) {
    friends.value = res.data.data
  }
}

const loadMessages = async () => {
  const res = await chatApi.getMessages(currentSessionId.value)
  if (res.data.code === 200) {
    messages.value = res.data.data
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const selectFriend = (friend) => {
  currentSessionId.value = getSessionId(friend.userId)
  currentChatName.value = friend.nickname
  loadMessages()
  subscribeToSession()
}

const selectPublic = () => {
  currentSessionId.value = 'public'
  currentChatName.value = '公共聊天室'
  loadMessages()
  subscribeToSession()
}

const addFriend = async () => {
  if (!addFriendInput.value) return
  const res = await friendApi.sendFriendRequest(user.value.userId, addFriendInput.value)
  if (res.data.code === 200) {
    ElMessage.success(res.data.data)
    addFriendInput.value = ''
  } else {
    ElMessage.error(res.data.message)
  }
}

const sendMessage = async () => {
  if (!messageInput.value.trim()) return
  const formData = new FormData()
  formData.append('senderId', user.value.userId)
  formData.append('sessionId', currentSessionId.value)
  formData.append('messageType', 0)
  formData.append('content', messageInput.value)
  
  const res = await chatApi.sendMessage(formData)
  if (res.data.code === 200) {
    messageInput.value = ''
  } else {
    ElMessage.error(res.data.message)
  }
}

const handleFileUpload = async (file) => {
  const formData = new FormData()
  formData.append('senderId', user.value.userId)
  formData.append('sessionId', currentSessionId.value)
  formData.append('messageType', 1)
  formData.append('file', file)
  
  const res = await chatApi.sendMessage(formData)
  if (res.data.code !== 200) {
    ElMessage.error(res.data.message)
  }
  return false
}

const connectWebSocket = () => {
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    subscribeToSession()
  })
}

const subscribeToSession = () => {
  if (stompClient && stompClient.connected) {
    stompClient.subscribe(`/topic/chat/${currentSessionId.value}`, (msg) => {
      const message = JSON.parse(msg.body)
      messages.value.push(message)
      scrollToBottom()
    })
  }
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  if (!user.value) {
    router.push('/login')
    return
  }
  loadFriends()
  loadMessages()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.disconnect()
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100%;
}

.sidebar {
  width: 280px;
  background: #f5f5f5;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.user-info {
  padding: 20px;
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.user-detail {
  flex: 1;
  margin-left: 12px;
}

.nickname {
  font-weight: 600;
  color: #333;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.add-friend {
  padding: 15px;
  display: flex;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.list-title {
  padding: 10px 15px;
  font-size: 14px;
  color: #666;
  background: #ebebeb;
}

.friend-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.friend-item:hover {
  background: #f0f0f0;
}

.friend-item.active {
  background: #e6f7ff;
  border-left: 3px solid #409eff;
}

.friend-item span {
  margin-left: 12px;
}

.public-chat {
  border-top: 1px solid #e0e0e0;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f9f9f9;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.self {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.content-wrapper {
  max-width: 60%;
  margin: 0 12px;
}

.sender-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-content {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.self .message-content {
  background: #d1edff;
}

.file-message {
  display: flex;
  align-items: center;
}

.time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

.self .time {
  text-align: right;
}

.input-area {
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  background: #fff;
}

.tools {
  margin-bottom: 10px;
}

.send-btn {
  margin-top: 10px;
  text-align: right;
}
</style>
