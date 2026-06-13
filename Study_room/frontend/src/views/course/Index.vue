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
      <el-table-column prop="unitAmount" label="每课时金额/扣费金额" />
      <el-table-column prop="unitHours" label="每次扣课时" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" title="新建课程" width="520px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%" @change="onCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型" required>
          <el-select v-model="form.courseTypeId" style="width: 100%">
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
        <el-form-item label="每课时金额" required>
          <el-input-number v-model="form.unitAmount" :min="1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每次扣课时">
          <el-input-number v-model="form.unitHours" :min="0.5" :step="0.5" :precision="1" style="width: 100%" />
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
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { SUBJECTS, CONSUMPTION_MODES, labelOf } from '@/constants'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const dialogVisible = ref(false)
const courseTypes = ref([])
const form = reactive({
  campusId: null, courseTypeId: null, name: '', subject: 'MATH',
  consumptionMode: 'HOURS', unitAmount: 100, unitHours: 1, status: 1
})

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/courses', { params })
)

async function loadCourseTypes(campusId) {
  if (!campusId) return
  courseTypes.value = await request.get(`/course-types/enabled/${campusId}`)
}

function openDialog() {
  Object.assign(form, {
    campusId: campuses.value[0]?.id || null,
    courseTypeId: null, name: '', subject: 'MATH',
    consumptionMode: 'HOURS', unitAmount: 100, unitHours: 1, status: 1
  })
  loadCourseTypes(form.campusId)
  dialogVisible.value = true
}

function onCampusChange(campusId) {
  form.courseTypeId = null
  loadCourseTypes(campusId)
}

async function submit() {
  await request.post('/courses', form)
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
</style>
