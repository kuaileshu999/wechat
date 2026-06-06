<template>
  <div class="chat-panel">
    <template v-if="conversationId">
      <header class="chat-panel-header">
        <div class="chat-title">
          <div class="name">{{ displayName }}</div>
          <div class="sub">{{ metaLine }}</div>
        </div>
        <div class="header-actions">
          <span class="tag" :class="stageTagClass">{{ conversation?.stageLabel }}</span>
          <span class="tag" :class="convTypeTagClass">{{ conversation?.convTypeLabel }}</span>
          <el-button size="small" @click="showTransfer = true">转接</el-button>
        </div>
      </header>

      <div ref="msgBox" v-loading="loading" class="chat-messages">
        <div v-for="msg in messages" :key="msg.id" class="msg-row">
          <div
            class="chat-bubble"
            :class="msg.senderType === 1 ? 'incoming' : 'outgoing'"
          >
            <div v-if="msg.senderType === 1" class="chat-sender">{{ msg.senderName }}</div>
            <div>{{ msg.content }}</div>
            <div class="chat-time">{{ formatTime(msg.sentAt) }}</div>
          </div>
        </div>
        <el-empty v-if="!loading && messages.length === 0" description="暂无消息" />
      </div>

      <footer class="chat-footer">
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="3"
          placeholder="输入回复内容，Ctrl+Enter 发送"
          resize="none"
          @keyup.ctrl.enter="sendReply"
        />
        <el-button type="primary" :loading="sending" :disabled="!canReply" @click="sendReply">
          发送
        </el-button>
      </footer>
    </template>
    <el-empty v-else class="chat-empty" description="请从左侧选择会话" />

    <el-dialog v-model="showTransfer" title="转接聊天" width="420px">
      <el-form label-width="88px">
        <el-form-item label="转给">
          <el-select v-model="transferToUserId" placeholder="选择处理老师" filterable style="width: 100%">
            <el-option
              v-for="item in handlerOptions"
              :key="item.userId"
              :label="item.label"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transferRemark" type="textarea" :rows="2" placeholder="转接说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTransfer = false">取消</el-button>
        <el-button type="primary" :loading="transferring" @click="submitTransfer">确认转接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { conversationApi, messageApi, takeoverManagerApi, tutorApi } from '../api'
import { getUser } from '../utils/auth'

const props = defineProps({
  conversationId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['replied', 'read', 'transferred'])

const loading = ref(false)
const sending = ref(false)
const transferring = ref(false)
const conversation = ref(null)
const messages = ref([])
const replyText = ref('')
const msgBox = ref(null)
const showTransfer = ref(false)
const transferToUserId = ref(null)
const transferRemark = ref('')
const handlerOptions = ref([])

const currentUser = computed(() => getUser())

const displayName = computed(() => {
  const c = conversation.value
  if (!c) return ''
  if (c.convType === 2) return c.groupName || c.studentName
  return c.studentName
})

const metaLine = computed(() => {
  const c = conversation.value
  if (!c) return ''
  return `${c.tutorName} · ${c.accountName} · ${c.subject || ''} ${c.grade || ''}`.trim()
})

const stageTagClass = computed(() => {
  const stage = conversation.value?.stage
  if (stage === 1) return 'tag-conversion'
  if (stage === 2) return 'tag-undertaking'
  return 'tag-completed'
})

const convTypeTagClass = computed(() =>
  conversation.value?.convType === 2 ? 'tag-group' : 'tag-private'
)

const canReply = computed(() => {
  if (!conversation.value || !currentUser.value) return false
  return conversation.value.handlerUserId === currentUser.value.userId
})

async function loadHandlers() {
  try {
    const [managers, tutors] = await Promise.all([
      takeoverManagerApi.list(),
      tutorApi.list(),
    ])
    const options = []
    managers.forEach((m) => {
      if (m.userId !== currentUser.value?.userId) {
        options.push({ userId: m.userId, label: `${m.name}（接管者）` })
      }
    })
    tutors.forEach((t) => {
      if (t.userId !== currentUser.value?.userId) {
        options.push({ userId: t.userId, label: `${t.name}（辅导老师）` })
      }
    })
    handlerOptions.value = options
  } catch {
    handlerOptions.value = []
  }
}

async function loadData(id) {
  if (!id) return
  loading.value = true
  try {
    const readerUserId = currentUser.value?.userId
    const [conv, msgs] = await Promise.all([
      conversationApi.detail(id, readerUserId),
      messageApi.list(id),
    ])
    conversation.value = conv
    messages.value = msgs
    emit('read', id)
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function sendReply() {
  const text = replyText.value.trim()
  if (!text) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!canReply.value) {
    ElMessage.warning('您不是当前会话处理人，无法回复')
    return
  }
  sending.value = true
  try {
    const msg = await messageApi.reply({
      conversationId: props.conversationId,
      senderUserId: currentUser.value.userId,
      content: text,
    })
    messages.value.push(msg)
    replyText.value = ''
    emit('replied')
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    sending.value = false
  }
}

async function submitTransfer() {
  if (!transferToUserId.value) {
    ElMessage.warning('请选择转接对象')
    return
  }
  transferring.value = true
  try {
    await conversationApi.transfer(props.conversationId, {
      toHandlerUserId: transferToUserId.value,
      operatorId: currentUser.value.userId,
      remark: transferRemark.value || undefined,
    })
    ElMessage.success('转接成功')
    showTransfer.value = false
    transferToUserId.value = null
    transferRemark.value = ''
    emit('transferred')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    transferring.value = false
  }
}

function scrollBottom() {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(
  () => props.conversationId,
  (id) => {
    conversation.value = null
    messages.value = []
    replyText.value = ''
    if (id) loadData(id)
  },
  { immediate: true }
)

watch(showTransfer, (v) => {
  if (v) loadHandlers()
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--card);
  border-radius: 8px;
  border: 1px solid var(--border);
  overflow: hidden;
}

.chat-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.chat-title .name {
  font-size: 16px;
  font-weight: 600;
}

.chat-title .sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

.msg-row {
  display: flex;
  flex-direction: column;
}

.chat-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
}

.chat-footer .el-textarea {
  flex: 1;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
