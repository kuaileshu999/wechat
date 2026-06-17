<template>
  <div class="page-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button v-if="userStore.hasPermission('course:create')" type="primary" @click="openDialog()">新建课程</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="校区">
        <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
      </el-table-column>
      <el-table-column prop="name" label="课程名称" />
      <el-table-column label="学科">
        <template #default="{ row }">{{ labelOf(SUBJECTS, row.subject) }}</template>
      </el-table-column>
      <el-table-column label="消课方式">
        <template #default="{ row }">{{ labelOf(CONSUMPTION_MODES, row.consumptionMode) }}</template>
      </el-table-column>
      <el-table-column prop="unitAmount" label="每次消课金额" />
      <el-table-column prop="unitHours" label="每次消课课时" />
      <el-table-column prop="sessionMinutes" label="每次消课时长(分钟)" width="150" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPermission('course:create')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新建课程'" width="520px">
      <el-form :model="form" label-width="130px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%" :disabled="!!editingId" @change="onCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型" required>
          <el-select v-model="form.courseTypeId" style="width: 100%" :disabled="!!editingId">
            <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="学科" required>
          <el-select v-model="form.subject" style="width: 100%">
            <el-option v-for="s in SUBJECTS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="消课方式" required>
          <el-select v-model="form.consumptionMode" style="width: 100%">
            <el-option v-for="m in CONSUMPTION_MODES" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.consumptionMode === 'HOURS'" label="每次消课课时" required>
          <el-input-number v-model="form.unitHours" :min="0.5" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.consumptionMode === 'AMOUNT'" label="每次消课金额" required>
          <el-input-number v-model="form.unitAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每次消课时长" required>
          <el-input-number v-model="form.sessionMinutes" :min="1" :precision="0" :step="1" style="width: 100%" />
          <span class="unit-tip">分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { SUBJECTS, CONSUMPTION_MODES, labelOf } from '@/constants'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const dialogVisible = ref(false)
const editingId = ref(null)
const courseTypes = ref([])
const form = reactive({
  campusId: null, courseTypeId: null, name: '', subject: 'MATH',
  consumptionMode: 'HOURS', unitAmount: 100, unitHours: 1, sessionMinutes: 60, status: 1
})

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/courses', { params })
)

async function loadCourseTypes(campusId) {
  if (!campusId) {
    courseTypes.value = []
    return
  }
  courseTypes.value = await request.get(`/course-types/enabled/${campusId}`)
}

function openDialog() {
  editingId.value = null
  Object.assign(form, {
    campusId: campuses.value[0]?.id || null,
    courseTypeId: null, name: '', subject: 'MATH',
    consumptionMode: 'HOURS', unitAmount: 100, unitHours: 1, sessionMinutes: 60, status: 1
  })
  loadCourseTypes(form.campusId)
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    campusId: row.campusId,
    courseTypeId: row.courseTypeId,
    name: row.name,
    subject: row.subject,
    consumptionMode: row.consumptionMode,
    unitAmount: Number(row.unitAmount),
    unitHours: Number(row.unitHours),
    sessionMinutes: row.sessionMinutes || 60,
    status: row.status
  })
  await loadCourseTypes(row.campusId)
  dialogVisible.value = true
}

function onCampusChange(campusId) {
  form.courseTypeId = null
  loadCourseTypes(campusId)
}

function validateForm() {
  if (!form.campusId || !form.courseTypeId || !form.name?.trim()) {
    ElMessage.warning('请填写必填项')
    return false
  }
  if (!form.sessionMinutes || form.sessionMinutes < 1 || !Number.isInteger(form.sessionMinutes)) {
    ElMessage.warning('每次消课时长必须为正整数')
    return false
  }
  if (form.consumptionMode === 'HOURS') {
    if (!form.unitHours || form.unitHours <= 0) {
      ElMessage.warning('请填写每次消课课时')
      return false
    }
  } else if (form.consumptionMode === 'AMOUNT') {
    if (!form.unitAmount || form.unitAmount <= 0) {
      ElMessage.warning('请填写每次消课金额')
      return false
    }
  }
  return true
}

function buildPayload() {
  const payload = { ...form, name: form.name.trim() }
  if (payload.consumptionMode === 'HOURS') {
    payload.unitAmount = 0
  } else {
    payload.unitHours = 0
  }
  return payload
}

async function submit() {
  if (!validateForm()) return
  if (editingId.value) {
    const { name, subject, consumptionMode, unitAmount, unitHours, sessionMinutes } = buildPayload()
    await request.put(`/courses/${editingId.value}`, {
      name, subject, consumptionMode, unitAmount, unitHours, sessionMinutes
    })
  } else {
    await request.post('/courses', buildPayload())
  }
  showSuccess()
  dialogVisible.value = false
  load()
}

async function toggleStatus(row) {
  await request.put(`/courses/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  showSuccess()
  load()
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.unit-tip { margin-left: 8px; color: #909399; font-size: 13px; }
</style>
