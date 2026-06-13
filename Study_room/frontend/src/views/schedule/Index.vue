<template>
  <div class="page-container">
    <div class="page-header">
      <h2>排课管理</h2>
      <div v-if="activeTab === 'manage'">
        <el-button v-if="userStore.hasPermission('schedule:delete') && selectedIds.length"
                   type="danger" @click="batchDelete">批量删除</el-button>
        <el-button v-if="userStore.hasPermission('schedule:import')" @click="importVisible = true">Excel导入</el-button>
        <el-button v-if="userStore.hasPermission('schedule:create')" @click="openBatchDialog()">批量排课</el-button>
        <el-button v-if="userStore.hasPermission('schedule:create')" type="primary" @click="openDialog()">
          新建排课
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="排课列表" name="manage" />
      <el-tab-pane label="我的课表" name="my" />
    </el-tabs>

    <div class="search-bar">
      <el-select v-if="activeTab === 'manage'" v-model="filters.campusId" placeholder="校区" clearable style="width: 140px">
        <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-if="activeTab === 'manage'" v-model="filters.teacherId" placeholder="授课老师" clearable filterable style="width: 140px">
        <el-option v-for="e in teachers" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-select v-model="filters.courseTypeId" placeholder="课程类型" clearable filterable style="width: 140px">
        <el-option v-for="t in filterCourseTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-if="activeTab === 'manage'" v-model="filters.status" placeholder="状态" clearable style="width: 110px">
        <el-option v-for="s in SCHEDULE_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                      start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      <el-button type="primary" @click="loadData()">查询</el-button>
    </div>

    <el-table v-if="activeTab === 'manage'" :data="list" v-loading="loading" border stripe
              @selection-change="onSelectionChange">
      <el-table-column v-if="userStore.hasPermission('schedule:delete')" type="selection" width="48"
                       :selectable="row => row.deletable" />
      <el-table-column label="校区" width="100">
        <template #default="{ row }">{{ row.campusName }}</template>
      </el-table-column>
      <el-table-column prop="teacherName" label="授课老师" width="100" />
      <el-table-column label="课程类型" width="90">
        <template #default="{ row }">{{ row.courseTypeName }}</template>
      </el-table-column>
      <el-table-column label="学员" width="100">
        <template #default="{ row }">{{ row.studentName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="上课时间" width="170">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="classroom" label="教室" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
            {{ labelOf(SCHEDULE_STATUS, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPermission('schedule:update') && row.editable"
                     link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="userStore.hasPermission('schedule:delete') && row.deletable"
                     link type="danger" @click="remove(row)">删除</el-button>
          <el-button v-if="row.status === 'PENDING'" link type="success" @click="markComplete(row)">已上课</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="activeTab === 'manage'" class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-table v-if="activeTab === 'my'" :data="myList" v-loading="myLoading" border stripe>
      <el-table-column label="校区" width="100">
        <template #default="{ row }">{{ row.campusName }}</template>
      </el-table-column>
      <el-table-column label="课程类型" width="90">
        <template #default="{ row }">{{ row.courseTypeName }}</template>
      </el-table-column>
      <el-table-column label="学员" width="100">
        <template #default="{ row }">{{ row.studentName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="上课时间" width="170">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="classroom" label="教室" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
            {{ labelOf(SCHEDULE_STATUS, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑排课' : '新建排课'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%" @change="onCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课老师" required>
          <el-select v-model="form.teacherId" filterable style="width: 100%">
            <el-option v-for="e in campusTeachers" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型" required>
          <el-select v-model="form.courseTypeId" style="width: 100%">
            <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员">
          <el-select v-model="form.studentId" filterable remote clearable :remote-method="searchStudents"
                     placeholder="搜索学员姓名或手机号" style="width: 100%">
            <el-option v-for="s in students" :key="s.id"
                       :label="`${s.name} (${s.phone})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="如：数学提高班" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <ScheduleDateTimePicker v-model="form.startTime" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <ScheduleDateTimePicker v-model="form.endTime" />
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.classroom" />
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

    <el-dialog v-model="batchVisible" title="批量排课" width="1040px">
      <el-form label-width="90px">
        <el-form-item label="校区" required>
          <el-select v-model="batchForm.campusId" style="width: 240px" @change="onBatchCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button class="ml-8" @click="addBatchRow">添加一行</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="batchForm.rows" border stripe max-height="420">
        <el-table-column label="授课老师" width="130">
          <template #default="{ row }">
            <el-select v-model="row.teacherId" filterable size="small" style="width: 100%">
              <el-option v-for="e in batchTeachers" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="课程类型" width="110">
          <template #default="{ row }">
            <el-select v-model="row.courseTypeId" size="small" style="width: 100%">
              <el-option v-for="t in batchCourseTypes" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="学员" width="150">
          <template #default="{ row }">
            <el-input v-model="row.studentKeyword" size="small" placeholder="姓名或手机号" />
          </template>
        </el-table-column>
        <el-table-column label="标题" width="120">
          <template #default="{ row }">
            <el-input v-model="row.title" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="240">
          <template #default="{ row }">
            <ScheduleDateTimePicker v-model="row.startTime" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="240">
          <template #default="{ row }">
            <ScheduleDateTimePicker v-model="row.endTime" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="教室" width="100">
          <template #default="{ row }">
            <el-input v-model="row.classroom" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="batchForm.rows.length <= 1"
                       @click="removeBatchRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="submitBatch">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="Excel导入排课" width="520px">
      <el-form label-width="90px">
        <el-form-item label="校区" required>
          <el-select v-model="importCampusId" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Excel文件">
          <input type="file" accept=".xlsx,.xls" @change="onImportFileChange" />
          <p class="tip">第一列：授课老师，第二列：课程类型，第三列：学员，第四列：标题，第五列：开始时间，第六列：结束时间，第七列：备注</p>
          <p class="tip">时间须为5分钟的整数倍；课程类型填已在「课程类型管理」中创建且启用的名称；学员可填姓名或11位手机号</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { SCHEDULE_STATUS, labelOf } from '@/constants'
import ScheduleDateTimePicker from '@/components/ScheduleDateTimePicker.vue'

const userStore = useUserStore()
const { campuses } = useCampuses()
const activeTab = ref('manage')
const dialogVisible = ref(false)
const batchVisible = ref(false)
const importVisible = ref(false)
const batchSubmitting = ref(false)
const importing = ref(false)
const importCampusId = ref(null)
const importFile = ref(null)
const batchTeachers = ref([])
const courseTypes = ref([])
const batchCourseTypes = ref([])
const filterCourseTypes = ref([])
const editingId = ref(null)
const dateRange = ref([])
const teachers = ref([])
const campusTeachers = ref([])
const students = ref([])
const myList = ref([])
const myLoading = ref(false)
const selectedIds = ref([])
const filters = reactive({ campusId: null, teacherId: null, courseTypeId: null, status: null })
const form = reactive({
  campusId: null, teacherId: null, courseTypeId: null, studentId: null,
  title: '', startTime: '', endTime: '', classroom: '', remark: ''
})
const batchForm = reactive({
  campusId: null,
  rows: []
})

const { list, total, page, loading, load, onPageChange } = usePagination(params => {
  const [startDate, endDate] = dateRange.value || []
  return request.get('/schedules', {
    params: { ...params, ...filters, startDate, endDate }
  })
})

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : ''
}

function roundToFiveMinutes(d) {
  const total = d.hour() * 60 + d.minute()
  const rounded = Math.round(total / 5) * 5
  return d.startOf('day').add(rounded, 'minute').format('YYYY-MM-DD HH:mm')
}

function validateScheduleTime(time, label) {
  if (!time) return true
  if (dayjs(time).minute() % 5 !== 0) {
    ElMessage.warning(`${label}必须为5分钟的整数倍`)
    return false
  }
  return true
}

async function loadCourseTypes(campusId) {
  if (!campusId) {
    courseTypes.value = []
    return
  }
  courseTypes.value = await request.get(`/course-types/enabled/${campusId}`)
}

async function loadBatchCourseTypes(campusId) {
  if (!campusId) {
    batchCourseTypes.value = []
    return
  }
  batchCourseTypes.value = await request.get(`/course-types/enabled/${campusId}`)
}

async function loadFilterCourseTypes() {
  const all = []
  for (const c of campuses.value) {
    const types = await request.get(`/course-types/enabled/${c.id}`)
    all.push(...types)
  }
  filterCourseTypes.value = all
}

async function loadTeachers() {
  teachers.value = await request.get('/employees/active')
}

async function loadCampusTeachers(campusId) {
  if (!campusId) {
    campusTeachers.value = []
    return
  }
  campusTeachers.value = await request.get('/employees/active', { params: { campusId } })
}

async function searchStudents(keyword) {
  if (!keyword) return
  students.value = await request.get('/students/search', { params: { keyword } })
}

function onCampusChange(campusId) {
  form.teacherId = null
  form.studentId = null
  form.courseTypeId = null
  students.value = []
  loadCampusTeachers(campusId)
  loadCourseTypes(campusId)
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function loadMy() {
  myLoading.value = true
  try {
    const [startDate, endDate] = dateRange.value || []
    myList.value = await request.get('/schedules/my', { params: { startDate, endDate, courseTypeId: filters.courseTypeId } })
  } finally {
    myLoading.value = false
  }
}

function loadData() {
  if (activeTab.value === 'my') {
    loadMy()
  } else {
    load()
  }
}

function onTabChange() {
  selectedIds.value = []
  loadData()
}

function createEmptyBatchRow() {
  const now = dayjs()
  return {
    teacherId: null,
    courseTypeId: batchCourseTypes.value[0]?.id || null,
    studentKeyword: '',
    title: '',
    startTime: roundToFiveMinutes(now),
    endTime: roundToFiveMinutes(now.add(1, 'hour')),
    classroom: '',
    remark: ''
  }
}

async function onBatchCampusChange(campusId) {
  batchTeachers.value = campusId
    ? await request.get('/employees/active', { params: { campusId } })
    : []
  await loadBatchCourseTypes(campusId)
  batchForm.rows.forEach(row => {
    row.teacherId = null
    row.studentKeyword = ''
    row.courseTypeId = batchCourseTypes.value[0]?.id || null
  })
}

function openBatchDialog() {
  batchForm.campusId = campuses.value[0]?.id || null
  onBatchCampusChange(batchForm.campusId).then(() => {
    batchForm.rows = [createEmptyBatchRow(), createEmptyBatchRow(), createEmptyBatchRow()]
    batchVisible.value = true
  })
}

function addBatchRow() {
  batchForm.rows.push(createEmptyBatchRow())
}

function removeBatchRow(index) {
  batchForm.rows.splice(index, 1)
}

async function resolveStudentId(campusId, keyword) {
  if (!keyword?.trim()) {
    return null
  }
  const list = await request.get('/students/search', { params: { keyword: keyword.trim() } })
  const matched = list.filter(s => s.campusId === campusId)
  if (matched.length === 0) {
    throw new Error(`未找到学员: ${keyword}`)
  }
  if (matched.length > 1) {
    throw new Error(`学员不唯一: ${keyword}`)
  }
  return matched[0].id
}

async function submitBatch() {
  if (!batchForm.campusId) {
    ElMessage.warning('请选择校区')
    return
  }
  batchSubmitting.value = true
  try {
    const items = []
    for (let i = 0; i < batchForm.rows.length; i++) {
      const row = batchForm.rows[i]
      if (!row.teacherId || !row.courseTypeId || !row.startTime || !row.endTime) {
        ElMessage.warning(`第 ${i + 1} 行请填写老师、课程类型和时间`)
        return
      }
      if (!validateScheduleTime(row.startTime, `第 ${i + 1} 行开始时间`) ||
          !validateScheduleTime(row.endTime, `第 ${i + 1} 行结束时间`)) {
        return
      }
      let studentId = null
      try {
        studentId = await resolveStudentId(batchForm.campusId, row.studentKeyword)
      } catch (e) {
        ElMessage.warning(`第 ${i + 1} 行: ${e.message}`)
        return
      }
      items.push({
        campusId: batchForm.campusId,
        teacherId: row.teacherId,
        courseTypeId: row.courseTypeId,
        studentId,
        title: row.title || null,
        startTime: row.startTime,
        endTime: row.endTime,
        classroom: row.classroom || null,
        remark: row.remark || null
      })
    }
    const count = await request.post('/schedules/batch', { items })
    showSuccess(`成功排课 ${count} 条`)
    batchVisible.value = false
    loadData()
  } finally {
    batchSubmitting.value = false
  }
}

function onImportFileChange(e) {
  importFile.value = e.target.files[0]
}

async function doImport() {
  if (!importCampusId.value || !importFile.value) {
    ElMessage.warning('请选择校区和文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const count = await request.post(`/schedules/import?campusId=${importCampusId.value}`, formData)
    showSuccess(`成功导入 ${count} 条`)
    importVisible.value = false
    loadData()
  } finally {
    importing.value = false
  }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, {
      campusId: row.campusId,
      teacherId: row.teacherId,
      courseTypeId: row.courseTypeId,
      studentId: row.studentId,
      title: row.title || '',
      startTime: row.startTime ? dayjs(row.startTime).format('YYYY-MM-DD HH:mm') : '',
      endTime: row.endTime ? dayjs(row.endTime).format('YYYY-MM-DD HH:mm') : '',
      classroom: row.classroom || '',
      remark: row.remark || ''
    })
    loadCampusTeachers(row.campusId)
    loadCourseTypes(row.campusId)
    if (row.studentId) {
      students.value = [{ id: row.studentId, name: row.studentName, phone: '' }]
    }
  } else {
    editingId.value = null
    const now = dayjs()
    Object.assign(form, {
      campusId: campuses.value[0]?.id || null,
      teacherId: null,
      courseTypeId: null,
      studentId: null,
      title: '',
      startTime: roundToFiveMinutes(now),
      endTime: roundToFiveMinutes(now.add(1, 'hour')),
      classroom: '',
      remark: ''
    })
    onCampusChange(form.campusId)
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.campusId || !form.teacherId || !form.courseTypeId || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (!validateScheduleTime(form.startTime, '开始时间') ||
      !validateScheduleTime(form.endTime, '结束时间')) {
    return
  }
  const payload = { ...form, studentId: form.studentId || null }
  if (editingId.value) {
    await request.put(`/schedules/${editingId.value}`, payload)
  } else {
    await request.post('/schedules', payload)
  }
  showSuccess()
  dialogVisible.value = false
  loadData()
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该排课？', '提示', { type: 'warning' })
  await request.delete(`/schedules/${row.id}`)
  showSuccess()
  loadData()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 条未上排课？`, '提示', { type: 'warning' })
  const count = await request.post('/schedules/batch-delete', { ids: selectedIds.value })
  showSuccess(`已删除 ${count} 条`)
  selectedIds.value = []
  loadData()
}

async function markComplete(row) {
  await ElMessageBox.confirm('确认标记为已上课？标记后将不可编辑或删除。', '提示', { type: 'warning' })
  await request.put(`/schedules/${row.id}/complete`)
  showSuccess()
  loadData()
}

onMounted(async () => {
  loadTeachers()
  importCampusId.value = campuses.value[0]?.id
  await loadFilterCourseTypes()
  loadData()
})
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.ml-8 { margin-left: 8px; }
.tip { font-size: 12px; color: #909399; margin-top: 8px; line-height: 1.6; }
</style>
