<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消课管理</h2>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待消课列表" name="pending">
        <div class="search-bar">
          <el-input v-model="pendingKeyword" placeholder="学员姓名或手机号" clearable style="width: 220px"
                    @keyup.enter="loadPending" />
          <el-button type="primary" @click="loadPending">查询</el-button>
        </div>
        <el-table :data="pendingOrders" v-loading="pendingLoading" border stripe>
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="studentName" label="学员姓名" />
          <el-table-column prop="studentPhone" label="手机号" width="130" />
          <el-table-column label="校区">
            <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="收款金额" />
          <el-table-column prop="consumedAmount" label="已消金额" />
          <el-table-column prop="totalHours" label="总课时" />
          <el-table-column prop="consumedHours" label="已消课时" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="userStore.hasPermission('consumption:execute')" link type="primary"
                         @click="openConsume(row)">消课</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="已消课列表" name="completed">
        <div class="search-bar">
          <el-input v-model="completedKeyword" placeholder="学员姓名或手机号" clearable style="width: 220px"
                    @keyup.enter="searchCompleted" />
          <el-button type="primary" @click="searchCompleted">查询</el-button>
        </div>
        <el-table :data="completedList" v-loading="completedLoading" border stripe>
          <el-table-column prop="orderId" label="订单ID" width="90" />
          <el-table-column prop="studentName" label="学员姓名" />
          <el-table-column prop="studentPhone" label="手机号" width="130" />
          <el-table-column label="校区">
            <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
          </el-table-column>
          <el-table-column prop="teacherName" label="上课老师">
            <template #default="{ row }">{{ row.teacherName || '-' }}</template>
          </el-table-column>
          <el-table-column label="消课方式">
            <template #default="{ row }">{{ labelOf(CONSUMPTION_MODES, row.consumptionMode) }}</template>
          </el-table-column>
          <el-table-column label="消课金额">
            <template #default="{ row }">
              {{ row.consumptionMode === 'AMOUNT' ? row.consumedAmount : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="消课课时">
            <template #default="{ row }">
              {{ row.consumptionMode === 'HOURS' ? row.consumedHours : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="上课时间" width="170">
            <template #default="{ row }">{{ formatTime(row.classTime) }}</template>
          </el-table-column>
          <el-table-column label="结课时间" width="170">
            <template #default="{ row }">{{ formatTime(row.classEndTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-16" background layout="total, prev, pager, next"
                       :total="completedTotal" :current-page="completedPage"
                       @current-change="p => { completedPage = p; loadCompleted() }" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="consumeVisible" title="执行消课" width="960px">
      <el-form label-width="100px" v-if="orderContext">
        <el-form-item label="订单号">
          <span>{{ orderContext.orderNo }}</span>
        </el-form-item>
        <el-form-item label="课程">
          <span>{{ orderContext.courseName }}</span>
        </el-form-item>
        <el-form-item label="消课方式">
          <span>{{ labelOf(CONSUMPTION_MODES, orderContext.consumptionMode) }}</span>
        </el-form-item>
        <el-form-item label="订单老师">
          <span>{{ orderContext.teacherName || '未指定' }}</span>
        </el-form-item>
        <el-form-item label="待消">
          <span v-if="orderContext.consumptionMode === 'AMOUNT'">
            金额 {{ orderContext.pendingAmount }}
          </span>
          <span v-else>课时 {{ orderContext.pendingHours }}</span>
        </el-form-item>
        <el-form-item label="消课明细">
          <el-button size="small" @click="addSession">添加一次</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="sessions" border stripe max-height="280">
        <el-table-column label="上课老师" width="140">
          <template #default="{ row }">
            <el-select v-model="row.teacherId" filterable clearable size="small" style="width: 100%">
              <el-option v-for="e in campusTeachers" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="上课时间" width="360">
          <template #default="{ row }">
            <ClassTimeRangePicker
              v-model:start="row.classTime"
              v-model:end="row.classEndTime"
              size="small"
              step="00:15"
              :default-duration-minutes="orderContext?.sessionMinutes || 60"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="orderContext?.consumptionMode === 'AMOUNT'" label="消课金额" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.consumedAmount" :min="0.01" :precision="2" size="small" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column v-else label="消课课时" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.consumedHours" :min="0.5" :step="0.5" :precision="1" size="small" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="sessions.length <= 1" @click="removeSession($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="completedRecords.length" class="history-block">
        <div class="history-title">已消课记录</div>
        <el-table :data="completedRecords" border stripe max-height="240" size="small">
          <el-table-column prop="teacherName" label="上课老师" width="100">
            <template #default="{ row }">{{ row.teacherName || '-' }}</template>
          </el-table-column>
          <el-table-column label="上课时间" width="280">
            <template #default="{ row }">{{ formatTimeRange(row.classTime, row.classEndTime) }}</template>
          </el-table-column>
          <el-table-column label="消课金额" width="90">
            <template #default="{ row }">
              {{ row.consumptionMode === 'AMOUNT' ? row.consumedAmount : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="消课课时" width="90">
            <template #default="{ row }">
              {{ row.consumptionMode === 'HOURS' ? row.consumedHours : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="consumeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitConsume">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="修改消课记录" width="520px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="上课老师">
          <el-select v-model="editForm.teacherId" filterable clearable style="width: 100%">
            <el-option v-for="e in editTeachers" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课时间" required>
          <ClassTimeRangePicker
            v-model:start="editForm.classTime"
            v-model:end="editForm.classEndTime"
            step="00:15"
            :default-duration-minutes="editForm.sessionMinutes || 60"
          />
        </el-form-item>
        <el-form-item v-if="editForm.consumptionMode === 'AMOUNT'" label="消课金额" required>
          <el-input-number v-model="editForm.consumedAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="消课课时" required>
          <el-input-number v-model="editForm.consumedHours" :min="0.5" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, showSuccess } from '@/composables/useCommon'
import { CONSUMPTION_MODES, labelOf } from '@/constants'
import ClassTimeRangePicker from '@/components/ClassTimeRangePicker.vue'

const userStore = useUserStore()
const { campusMap } = useCampuses()
const activeTab = ref('pending')
const pendingOrders = ref([])
const pendingLoading = ref(false)
const pendingKeyword = ref('')
const completedList = ref([])
const completedLoading = ref(false)
const completedKeyword = ref('')
const completedTotal = ref(0)
const completedPage = ref(1)
const consumeVisible = ref(false)
const editVisible = ref(false)
const submitting = ref(false)
const currentOrderId = ref(null)
const currentRecordId = ref(null)
const orderContext = ref(null)
const completedRecords = ref([])
const campusTeachers = ref([])
const editTeachers = ref([])
const sessions = ref([])
const editForm = reactive({
  teacherId: null,
  classTime: '',
  classEndTime: '',
  consumptionMode: 'HOURS',
  consumedAmount: 0,
  consumedHours: 0,
  sessionMinutes: 60,
  remark: ''
})

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-'
}

function formatTimeRange(start, end) {
  if (!start) return '-'
  const s = formatTime(start)
  const e = end ? formatTime(end) : '-'
  return `${s} 至 ${e}`
}

function roundToFifteenMinutes(d) {
  const total = d.hour() * 60 + d.minute()
  const rounded = Math.round(total / 15) * 15
  return d.startOf('day').add(rounded, 'minute').format('YYYY-MM-DD HH:mm')
}

function createSession(ctx) {
  const defaultTeacher = ctx?.teacherId || null
  const defaultAmount = ctx?.unitAmount ? Number(ctx.unitAmount) : null
  const defaultHours = ctx?.unitHours ? Number(ctx.unitHours) : 1
  const minutes = ctx?.sessionMinutes || 60
  const classTime = roundToFifteenMinutes(dayjs())
  const classEndTime = dayjs(classTime).add(minutes, 'minute').format('YYYY-MM-DD HH:mm')
  return {
    teacherId: defaultTeacher,
    classTime,
    classEndTime,
    consumedAmount: defaultAmount,
    consumedHours: defaultHours
  }
}

async function loadPending() {
  pendingLoading.value = true
  try {
    pendingOrders.value = await request.get('/consumptions/pending-orders', {
      params: { keyword: pendingKeyword.value || undefined }
    })
  } finally {
    pendingLoading.value = false
  }
}

async function loadCompleted() {
  completedLoading.value = true
  try {
    const data = await request.get('/consumptions/completed', {
      params: {
        page: completedPage.value,
        size: 10,
        keyword: completedKeyword.value || undefined
      }
    })
    completedList.value = data.list
    completedTotal.value = data.total
  } finally {
    completedLoading.value = false
  }
}

function searchCompleted() {
  completedPage.value = 1
  loadCompleted()
}

async function openConsume(row) {
  currentOrderId.value = row.id
  try {
    orderContext.value = await request.get(`/consumptions/order-context/${row.id}`)
    completedRecords.value = orderContext.value.completedRecords || []
  } catch {
    const detail = await request.get(`/orders/${row.id}`)
    const courses = await request.get(`/courses/enabled/${detail.order.campusId}`)
    const course = courses.find(c => c.id === detail.order.courseId)
    if (!course) {
      ElMessage.error('无法加载课程信息，请重启后端: cd backend && mvn spring-boot:run')
      return
    }
    try {
      completedRecords.value = await request.get(`/consumptions/order/${row.id}/records`)
    } catch {
      completedRecords.value = []
    }
    orderContext.value = {
      orderId: detail.order.id,
      orderNo: detail.order.orderNo,
      campusId: detail.order.campusId,
      teacherId: detail.order.teacherId || null,
      teacherName: detail.teacherName || null,
      courseName: course.name,
      consumptionMode: course.consumptionMode,
      unitAmount: course.unitAmount,
      unitHours: course.unitHours,
      sessionMinutes: course.sessionMinutes || 60,
      pendingAmount: detail.pendingAmount,
      pendingHours: detail.pendingHours
    }
  }
  campusTeachers.value = await request.get('/employees/active', { params: { campusId: orderContext.value.campusId } })
  sessions.value = [createSession(orderContext.value)]
  consumeVisible.value = true
}

function addSession() {
  sessions.value.push(createSession(orderContext.value))
}

function removeSession(index) {
  sessions.value.splice(index, 1)
}

function toClassTimePayload(classTime) {
  if (!classTime) return null
  return classTime.includes('T') ? classTime : classTime.replace(' ', 'T') + ':00'
}

async function submitConsume() {
  if (!sessions.value.length) {
    ElMessage.warning('请至少添加一次消课')
    return
  }
  for (let i = 0; i < sessions.value.length; i++) {
    const s = sessions.value[i]
    if (!s.classTime || !s.classEndTime) {
      ElMessage.warning(`第 ${i + 1} 次请填写完整上课时间段`)
      return
    }
    if (dayjs(s.classTime).minute() % 15 !== 0 || dayjs(s.classEndTime).minute() % 15 !== 0) {
      ElMessage.warning(`第 ${i + 1} 次时间须为15分钟的整数倍`)
      return
    }
    if (!dayjs(s.classEndTime).isAfter(dayjs(s.classTime))) {
      ElMessage.warning(`第 ${i + 1} 次结课时间必须在开始时间之后`)
      return
    }
  }
  submitting.value = true
  try {
    await request.post('/consumptions', {
      orderId: currentOrderId.value,
      sessions: sessions.value.map(s => ({
        teacherId: s.teacherId || null,
        classTime: toClassTimePayload(s.classTime),
        classEndTime: toClassTimePayload(s.classEndTime),
        consumedAmount: orderContext.value.consumptionMode === 'AMOUNT' ? s.consumedAmount : null,
        consumedHours: orderContext.value.consumptionMode === 'HOURS' ? s.consumedHours : null
      }))
    })
    showSuccess()
    consumeVisible.value = false
    loadPending()
    loadCompleted()
  } finally {
    submitting.value = false
  }
}

async function openEdit(row) {
  currentRecordId.value = row.id
  editTeachers.value = await request.get('/employees/active', { params: { campusId: row.campusId } })
  Object.assign(editForm, {
    teacherId: row.teacherId || null,
    classTime: row.classTime ? dayjs(row.classTime).format('YYYY-MM-DD HH:mm') : '',
    classEndTime: row.classEndTime
      ? dayjs(row.classEndTime).format('YYYY-MM-DD HH:mm')
      : (row.classTime && row.sessionMinutes
        ? dayjs(row.classTime).add(row.sessionMinutes, 'minute').format('YYYY-MM-DD HH:mm')
        : ''),
    consumptionMode: row.consumptionMode,
    consumedAmount: Number(row.consumedAmount),
    consumedHours: Number(row.consumedHours),
    sessionMinutes: row.sessionMinutes || 60,
    remark: row.remark || ''
  })
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.classTime || !editForm.classEndTime) {
    ElMessage.warning('请填写完整上课时间段')
    return
  }
  if (!dayjs(editForm.classEndTime).isAfter(dayjs(editForm.classTime))) {
    ElMessage.warning('结课时间必须在开始时间之后')
    return
  }
  await request.put(`/consumptions/${currentRecordId.value}`, {
    teacherId: editForm.teacherId || null,
    classTime: toClassTimePayload(editForm.classTime),
    classEndTime: toClassTimePayload(editForm.classEndTime),
    consumedAmount: editForm.consumptionMode === 'AMOUNT' ? editForm.consumedAmount : null,
    consumedHours: editForm.consumptionMode === 'HOURS' ? editForm.consumedHours : null,
    remark: editForm.remark
  })
  showSuccess()
  editVisible.value = false
  loadCompleted()
  loadPending()
}

onMounted(() => {
  loadPending()
  loadCompleted()
})
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.history-block { margin-top: 16px; }
.history-title { font-weight: 600; margin-bottom: 8px; color: #303133; }
</style>
