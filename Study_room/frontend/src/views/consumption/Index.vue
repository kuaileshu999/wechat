<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消课管理</h2>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待消课列表" name="pending">
        <el-table :data="pendingOrders" v-loading="pendingLoading" border stripe>
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column label="校区">
            <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="收款金额" />
          <el-table-column prop="consumedAmount" label="已消金额" />
          <el-table-column prop="totalHours" label="总课时" />
          <el-table-column prop="consumedHours" label="已消课时" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="userStore.hasPermission('consumption:execute')" link type="primary"
                         @click="openConsume(row)">消课</el-button>
              <el-checkbox v-model="selectedIds" :label="row.id">批量</el-checkbox>
            </template>
          </el-table-column>
        </el-table>
        <div class="batch-bar" v-if="userStore.hasPermission('consumption:execute')">
          <el-button type="primary" :disabled="selectedIds.length < 2" @click="batchConsume">批量消课（同课程）</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="已消课列表" name="completed">
        <el-table :data="completedList" v-loading="completedLoading" border stripe>
          <el-table-column prop="orderId" label="订单ID" />
          <el-table-column label="校区">
            <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
          </el-table-column>
          <el-table-column prop="consumedAmount" label="消课金额" />
          <el-table-column prop="consumedHours" label="消课课时" />
          <el-table-column label="消课方式">
            <template #default="{ row }">{{ labelOf(CONSUMPTION_MODES, row.consumptionMode) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="消课时间" />
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

    <el-dialog v-model="consumeVisible" title="执行消课" width="480px">
      <el-form :model="consumeForm" label-width="100px">
        <el-form-item label="消课方式">
          <el-select v-model="consumeForm.consumptionMode" style="width: 100%">
            <el-option v-for="m in CONSUMPTION_MODES" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="消课金额">
          <el-input-number v-model="consumeForm.consumedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消课课时">
          <el-input-number v-model="consumeForm.consumedHours" :min="0" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="consumeForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConsume">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="修改消课记录" width="480px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="消课金额">
          <el-input-number v-model="editForm.consumedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消课课时">
          <el-input-number v-model="editForm.consumedHours" :min="0" :step="0.5" :precision="1" style="width: 100%" />
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
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, showSuccess } from '@/composables/useCommon'
import { CONSUMPTION_MODES, labelOf } from '@/constants'

const userStore = useUserStore()
const { campusMap } = useCampuses()
const activeTab = ref('pending')
const pendingOrders = ref([])
const pendingLoading = ref(false)
const completedList = ref([])
const completedLoading = ref(false)
const completedTotal = ref(0)
const completedPage = ref(1)
const selectedIds = ref([])
const consumeVisible = ref(false)
const editVisible = ref(false)
const currentOrderId = ref(null)
const currentRecordId = ref(null)
const consumeForm = reactive({ consumptionMode: 'HOURS', consumedAmount: null, consumedHours: 1, remark: '' })
const editForm = reactive({ consumedAmount: 0, consumedHours: 0, remark: '' })

async function loadPending() {
  pendingLoading.value = true
  try {
    pendingOrders.value = await request.get('/consumptions/pending-orders')
  } finally {
    pendingLoading.value = false
  }
}

async function loadCompleted() {
  completedLoading.value = true
  try {
    const data = await request.get('/consumptions/completed', { params: { page: completedPage.value, size: 10 } })
    completedList.value = data.list
    completedTotal.value = data.total
  } finally {
    completedLoading.value = false
  }
}

function openConsume(row) {
  currentOrderId.value = row.id
  Object.assign(consumeForm, { consumptionMode: 'HOURS', consumedAmount: null, consumedHours: 1, remark: '' })
  consumeVisible.value = true
}

async function submitConsume() {
  await request.post('/consumptions', { orderId: currentOrderId.value, ...consumeForm })
  showSuccess()
  consumeVisible.value = false
  loadPending()
  loadCompleted()
}

async function batchConsume() {
  await ElMessageBox.confirm(`确认对 ${selectedIds.value.length} 个订单批量消课？`, '提示')
  await request.post('/consumptions/batch', {
    orderIds: selectedIds.value,
    consumptionMode: consumeForm.consumptionMode,
    consumedAmount: consumeForm.consumedAmount,
    consumedHours: consumeForm.consumedHours
  })
  showSuccess()
  selectedIds.value = []
  loadPending()
  loadCompleted()
}

function openEdit(row) {
  currentRecordId.value = row.id
  Object.assign(editForm, {
    consumedAmount: row.consumedAmount,
    consumedHours: row.consumedHours,
    remark: row.remark
  })
  editVisible.value = true
}

async function submitEdit() {
  await request.put(`/consumptions/${currentRecordId.value}`, editForm)
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
.batch-bar { margin-top: 16px; }
</style>
