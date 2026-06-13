<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-button v-if="userStore.hasPermission('order:create')" type="primary" @click="openDialog()">新建订单</el-button>
    </div>
    <div class="search-bar">
      <el-select v-model="filters.campusId" placeholder="校区" clearable style="width: 160px">
        <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                      start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      <el-button type="primary" @click="load()">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="校区">
        <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="收款金额" />
      <el-table-column prop="totalHours" label="课时数" />
      <el-table-column label="收款方式">
        <template #default="{ row }">{{ labelOf(PAYMENT_METHODS, row.paymentMethod) }}</template>
      </el-table-column>
      <el-table-column prop="paymentDate" label="收款日期" />
      <el-table-column label="状态">
        <template #default="{ row }">{{ labelOf(ORDER_STATUS, row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/order/${row.id}`)">详情</el-button>
          <el-button v-if="userStore.hasPermission('order:create') && row.status !== 'REFUNDED'"
                     link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="userStore.hasPermission('order:refund') && row.status !== 'REFUNDED'"
                     link type="warning" @click="openRefund(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑订单' : '新建订单'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%" :disabled="!!editingId" @change="onCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员" required>
          <el-select v-model="form.studentId" filterable remote :remote-method="searchStudents"
                     placeholder="搜索姓名或手机号" style="width: 100%">
            <el-option v-for="s in students" :key="s.id"
                       :label="`${s.name} (${s.phone})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" required>
          <el-select v-model="form.courseId" filterable placeholder="请选择或搜索已启用课程" style="width: 100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课时数" required>
          <el-input-number v-model="form.totalHours" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款金额" required>
          <el-input-number v-model="form.paidAmount" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款方式" required>
          <el-select v-model="form.paymentMethod" style="width: 100%">
            <el-option v-for="m in PAYMENT_METHODS" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款日期" required>
          <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售人" required>
          <el-select v-model="form.salespersonId" style="width: 100%" :disabled="!form.campusId">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="发起退款" width="520px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ refundForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="收款金额">
          <span>{{ refundForm.paidAmount }}</span>
        </el-form-item>
        <el-form-item label="消课金额">
          <span>{{ refundForm.consumedAmount }}</span>
        </el-form-item>
        <el-form-item label="可退金额">
          <span class="refundable">{{ refundForm.refundableAmount }}</span>
          <span class="tip">（收款金额 - 消课金额）</span>
        </el-form-item>
        <el-form-item v-if="refundForm.refundedAmount > 0" label="已退金额">
          <span>{{ refundForm.refundedAmount }}</span>
        </el-form-item>
        <el-form-item label="退款金额" required>
          <el-input-number v-model="refundForm.refundAmount" :min="0.01" :max="refundForm.maxRefund"
                           :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-input v-model="refundForm.refundReason" type="textarea" />
        </el-form-item>
        <el-form-item label="退款方式" required>
          <el-select v-model="refundForm.refundMethod" style="width: 100%">
            <el-option v-for="m in PAYMENT_METHODS" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="refundForm.remark" type="textarea" maxlength="200" show-word-limit
                    placeholder="选填，最多200字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { PAYMENT_METHODS, ORDER_STATUS, labelOf } from '@/constants'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const dialogVisible = ref(false)
const refundVisible = ref(false)
const editingId = ref(null)
const dateRange = ref([])
const filters = reactive({ campusId: null })
const students = ref([])
const courses = ref([])
const employees = ref([])
const form = reactive({
  campusId: null, studentId: null, courseId: null, totalHours: 1,
  paidAmount: 1000, paymentMethod: 'WECHAT',
  paymentDate: dayjs().format('YYYY-MM-DD'), salespersonId: null, remark: ''
})
const refundForm = reactive({
  orderId: null, orderNo: '', paidAmount: 0, consumedAmount: 0, refundedAmount: 0,
  refundableAmount: 0, maxRefund: 0, refundAmount: 0, refundReason: '', refundMethod: 'WECHAT', remark: ''
})

const { list, total, page, loading, load, onPageChange } = usePagination(params => {
  const [startDate, endDate] = dateRange.value || []
  return request.get('/orders', { params: { ...params, ...filters, startDate, endDate } })
})

async function searchStudents(keyword) {
  if (!keyword) return
  students.value = await request.get('/students/search', { params: { keyword } })
}

async function loadEnabledCourses() {
  if (!form.campusId) {
    courses.value = []
    return
  }
  courses.value = await request.get(`/courses/enabled/${form.campusId}`)
}

async function onCampusChange(campusId) {
  form.studentId = null
  form.courseId = null
  form.salespersonId = null
  students.value = []
  employees.value = []
  if (campusId) {
    employees.value = await request.get('/employees/active', { params: { campusId } })
    await loadEnabledCourses()
  } else {
    courses.value = []
  }
}

function openDialog() {
  editingId.value = null
  Object.assign(form, {
    campusId: campuses.value[0]?.id || null,
    studentId: null, courseId: null, totalHours: 1, paidAmount: 1000,
    paymentMethod: 'WECHAT', paymentDate: dayjs().format('YYYY-MM-DD'),
    salespersonId: null, remark: ''
  })
  onCampusChange(form.campusId)
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  const detail = await request.get(`/orders/${row.id}`)
  const order = detail.order
  Object.assign(form, {
    campusId: order.campusId,
    studentId: order.studentId,
    courseId: order.courseId,
    totalHours: order.totalHours,
    paidAmount: Number(order.paidAmount),
    paymentMethod: order.paymentMethod,
    paymentDate: order.paymentDate,
    salespersonId: order.salespersonId,
    remark: order.remark || ''
  })
  students.value = [{
    id: order.studentId,
    name: detail.studentName,
    phone: detail.studentPhone
  }]
  employees.value = await request.get('/employees/active', { params: { campusId: order.campusId } })
  await loadEnabledCourses()
  if (!courses.value.some(c => c.id === order.courseId)) {
    courses.value = [{ id: order.courseId, name: detail.courseName }, ...courses.value]
  }
  dialogVisible.value = true
}

async function submit() {
  const payload = {
    studentId: form.studentId,
    courseId: form.courseId,
    totalHours: form.totalHours,
    paidAmount: form.paidAmount,
    paymentMethod: form.paymentMethod,
    paymentDate: form.paymentDate,
    salespersonId: form.salespersonId,
    remark: form.remark
  }
  if (editingId.value) {
    await request.put(`/orders/${editingId.value}`, payload)
  } else {
    await request.post('/orders', { ...payload, campusId: form.campusId })
  }
  showSuccess()
  dialogVisible.value = false
  load()
}

function openRefund(row) {
  const paid = Number(row.paidAmount || 0)
  const consumed = Number(row.consumedAmount || 0)
  const refunded = Number(row.refundedAmount || 0)
  const refundableAmount = Math.max(0, paid - consumed)
  const maxRefund = Math.max(0, paid - consumed - refunded)
  Object.assign(refundForm, {
    orderId: row.id,
    orderNo: row.orderNo,
    paidAmount: paid,
    consumedAmount: consumed,
    refundedAmount: refunded,
    refundableAmount,
    maxRefund,
    refundAmount: maxRefund || refundableAmount,
    refundReason: '',
    refundMethod: 'WECHAT',
    remark: ''
  })
  refundVisible.value = true
}

async function submitRefund() {
  await request.post(`/orders/${refundForm.orderId}/refund`, {
    refundAmount: refundForm.refundAmount,
    refundReason: refundForm.refundReason,
    refundMethod: refundForm.refundMethod,
    remark: refundForm.remark || undefined
  })
  showSuccess()
  refundVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.refundable { color: #e6a23c; font-weight: 600; }
.tip { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
