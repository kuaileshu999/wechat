<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <h2>订单详情</h2>
      <div>
        <el-button v-if="userStore.hasPermission('order:refund') && detail?.order?.status !== 'REFUNDED'"
                   type="warning" @click="openRefund">发起退款</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <el-descriptions v-if="detail" title="订单信息" :column="3" border>
      <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="校区">{{ detail.campusName }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ labelOf(ORDER_STATUS, detail.order.status) }}</el-descriptions-item>
      <el-descriptions-item label="学员">{{ detail.studentName }} ({{ detail.studentPhone }})</el-descriptions-item>
      <el-descriptions-item label="课程">{{ detail.courseName }}</el-descriptions-item>
      <el-descriptions-item label="课时数">{{ detail.order.totalHours }}</el-descriptions-item>
      <el-descriptions-item label="收款金额">{{ detail.order.paidAmount }}</el-descriptions-item>
      <el-descriptions-item label="已消课金额">{{ detail.order.consumedAmount }}</el-descriptions-item>
      <el-descriptions-item label="待消课金额">{{ detail.pendingAmount }}</el-descriptions-item>
      <el-descriptions-item label="待消课时">{{ detail.pendingHours }}</el-descriptions-item>
      <el-descriptions-item label="已退费">{{ detail.order.refundedAmount }}</el-descriptions-item>
      <el-descriptions-item label="销售人">{{ detail.salespersonName }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detail.order.remark }}</el-descriptions-item>
    </el-descriptions>

    <el-card v-if="detail?.refunds?.length" class="mt-16" header="退款记录">
      <el-table :data="detail.refunds" border>
        <el-table-column prop="refundAmount" label="退款金额" />
        <el-table-column prop="refundReason" label="退款原因" />
        <el-table-column label="退款方式">
          <template #default="{ row }">{{ labelOf(PAYMENT_METHODS, row.refundMethod) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="退款时间" />
      </el-table>
    </el-card>

    <el-card class="mt-16" header="修改记录">
      <el-table :data="detail?.auditLogs || []" border>
        <el-table-column prop="operatorName" label="修改人" />
        <el-table-column prop="createdAt" label="修改时间" />
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="content" label="修改内容" />
      </el-table>
    </el-card>

    <el-dialog v-model="refundVisible" title="发起退款" width="520px">
      <el-form :model="refundForm" label-width="100px">
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
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { showSuccess } from '@/composables/useCommon'
import { PAYMENT_METHODS, ORDER_STATUS, labelOf } from '@/constants'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const detail = ref(null)
const refundVisible = ref(false)
const refundForm = reactive({
  paidAmount: 0, consumedAmount: 0, refundedAmount: 0,
  refundableAmount: 0, maxRefund: 0, refundAmount: 0,
  refundReason: '', refundMethod: 'WECHAT', remark: ''
})

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await request.get(`/orders/${route.params.id}`)
  } finally {
    loading.value = false
  }
}

function openRefund() {
  const order = detail.value.order
  const paid = Number(order.paidAmount || 0)
  const consumed = Number(order.consumedAmount || 0)
  const refunded = Number(order.refundedAmount || 0)
  const refundableAmount = Math.max(0, paid - consumed)
  const maxRefund = Math.max(0, paid - consumed - refunded)
  Object.assign(refundForm, {
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
  await request.post(`/orders/${route.params.id}/refund`, {
    refundAmount: refundForm.refundAmount,
    refundReason: refundForm.refundReason,
    refundMethod: refundForm.refundMethod,
    remark: refundForm.remark || undefined
  })
  showSuccess()
  refundVisible.value = false
  loadDetail()
}

onMounted(loadDetail)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.refundable { color: #e6a23c; font-weight: 600; }
.tip { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
