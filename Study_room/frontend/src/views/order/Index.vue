<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="header-actions">
        <el-button v-if="userStore.hasPermission('order:export')" @click="exportOrders">导出</el-button>
        <el-button v-if="userStore.hasPermission('order:create')" type="primary" @click="openDialog()">新建订单</el-button>
      </div>
    </div>
    <div class="search-bar">
      <el-select v-model="filters.campusId" placeholder="校区" clearable style="width: 160px">
        <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="学员姓名或手机号" clearable style="width: 180px" />
      <el-input v-model="filters.orderNo" placeholder="订单号" clearable style="width: 180px" />
      <el-input v-model="filters.unionPayOrderNo" placeholder="银联单号" clearable style="width: 180px" />
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                      start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      <el-button type="primary" @click="load()">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="unionPayOrderNo" label="银联单号" width="160">
        <template #default="{ row }">{{ row.unionPayOrderNo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="studentName" label="学员姓名" width="100" />
      <el-table-column prop="courseName" label="课程名称" min-width="120" />
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

    <el-drawer v-model="dialogVisible" :title="editingId ? '编辑订单' : '新建订单'"
               direction="rtl" size="70%" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%" :disabled="!!editingId" @change="onCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员" required>
          <el-select v-model="form.studentId" filterable remote reserve-keyword
                     :remote-method="searchStudents" placeholder="请选择或搜索姓名/手机号"
                     style="width: 100%" :disabled="!form.campusId" @visible-change="onStudentDropdown">
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
        <el-form-item v-if="form.paymentMethod === 'UNION_PAY'" label="银联订单号" required>
          <el-input v-model="form.unionPayOrderNo" placeholder="请输入银联订单号" maxlength="64" />
        </el-form-item>
        <el-form-item label="收款日期" required>
          <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售人" required>
          <el-select v-model="form.salespersonIds" filterable multiple
                     class="order-multi-select" style="width: 100%" :disabled="!form.campusId"
                     placeholder="可多选">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主讲老师">
          <el-select v-model="form.teacherIds" filterable clearable multiple
                     class="order-multi-select" style="width: 100%" :disabled="!form.campusId"
                     placeholder="选填，可多选">
            <el-option v-for="e in employees" :key="'t-' + e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit
                    :autosize="{ minRows: 1, maxRows: 5 }" placeholder="选填，最多500字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">确定</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="refundVisible" title="发起退款" direction="rtl" size="70%" destroy-on-close>
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
          <el-select v-model="refundForm.refundMethod" style="width: 100%" disabled>
            <el-option v-for="m in REFUND_METHODS" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="refundForm.remark" type="textarea" maxlength="200" show-word-limit
                    :autosize="{ minRows: 1, maxRows: 4 }" placeholder="选填，最多200字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="refundVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRefund">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { PAYMENT_METHODS, REFUND_METHODS, ORDER_STATUS, labelOf } from '@/constants'
import { downloadExport } from '@/utils/export'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const dialogVisible = ref(false)
const refundVisible = ref(false)
const editingId = ref(null)
const dateRange = ref([])
const filters = reactive({ campusId: null, keyword: '', orderNo: '', unionPayOrderNo: '' })
const students = ref([])
const courses = ref([])
const employees = ref([])
const form = reactive({
  campusId: null, studentId: null, courseId: null, totalHours: 1,
  paidAmount: 1000, paymentMethod: 'UNION_PAY', unionPayOrderNo: '',
  paymentDate: dayjs().format('YYYY-MM-DD'), salespersonIds: [], teacherIds: [], remark: ''
})
const refundForm = reactive({
  orderId: null, orderNo: '', paidAmount: 0, consumedAmount: 0, refundedAmount: 0,
  refundableAmount: 0, maxRefund: 0, refundAmount: 0, refundReason: '', refundMethod: 'BANK_CARD', remark: ''
})

const { list, total, page, loading, load, onPageChange } = usePagination(params => {
  const [startDate, endDate] = dateRange.value || []
  return request.get('/orders', { params: { ...params, ...filters, startDate, endDate } })
})

async function loadStudents(keyword = '') {
  if (!form.campusId) {
    students.value = []
    return
  }
  students.value = await request.get('/students/options', {
    params: { campusId: form.campusId, keyword: keyword || undefined }
  })
}

async function searchStudents(keyword) {
  await loadStudents(keyword)
}

function onStudentDropdown(visible) {
  if (visible && form.campusId) {
    loadStudents()
  }
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
  form.salespersonIds = []
  form.teacherIds = []
  students.value = []
  employees.value = []
  if (campusId) {
    employees.value = await request.get('/employees/active', { params: { campusId } })
    await loadEnabledCourses()
    await loadStudents()
  } else {
    courses.value = []
  }
}

async function openDialog() {
  editingId.value = null
  Object.assign(form, {
    campusId: campuses.value[0]?.id || null,
    studentId: null, courseId: null, totalHours: 1, paidAmount: 1000,
    paymentMethod: 'UNION_PAY', unionPayOrderNo: '', paymentDate: dayjs().format('YYYY-MM-DD'),
    salespersonIds: [], teacherIds: [], remark: ''
  })
  await onCampusChange(form.campusId)
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
    unionPayOrderNo: order.unionPayOrderNo || '',
    paymentDate: order.paymentDate,
    salespersonIds: detail.salespersonIds?.length ? [...detail.salespersonIds]
      : (order.salespersonId ? [order.salespersonId] : []),
    teacherIds: detail.teacherIds?.length ? [...detail.teacherIds]
      : (order.teacherId ? [order.teacherId] : []),
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
    unionPayOrderNo: form.paymentMethod === 'UNION_PAY' ? form.unionPayOrderNo?.trim() : null,
    paymentDate: form.paymentDate,
    salespersonIds: form.salespersonIds?.length ? form.salespersonIds : [],
    teacherIds: form.teacherIds?.length ? form.teacherIds : [],
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
    refundMethod: 'BANK_CARD',
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

async function exportOrders() {
  const [startDate, endDate] = dateRange.value || []
  await downloadExport('/orders/export', {
    ...filters,
    startDate,
    endDate,
    keyword: filters.keyword || undefined,
    orderNo: filters.orderNo || undefined,
    unionPayOrderNo: filters.unionPayOrderNo || undefined
  }, '订单导出.xlsx')
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.header-actions { display: flex; gap: 8px; }
.page-header { display: flex; align-items: center; justify-content: space-between; }
.refundable { color: #e6a23c; font-weight: 600; }
.tip { margin-left: 8px; font-size: 12px; color: #909399; }
.order-multi-select :deep(.el-select__wrapper) {
  height: auto;
  padding-top: 4px;
  padding-bottom: 4px;
}
.order-multi-select :deep(.el-select__selected-item) {
  max-width: 100%;
}
</style>
