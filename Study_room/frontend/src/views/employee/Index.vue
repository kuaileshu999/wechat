<template>
  <div class="page-container">
    <div class="page-header">
      <h2>员工管理</h2>
      <el-button v-if="userStore.hasPermission('employee:create')" type="primary" @click="openDialog()">新建员工</el-button>
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="员工姓名" clearable style="width: 200px" @keyup.enter="load()" />
      <el-button type="primary" @click="load()">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="员工ID" width="100" />
      <el-table-column prop="name" label="员工姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="所属校区">
        <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
      </el-table-column>
      <el-table-column label="任职状态">
        <template #default="{ row }">
          <el-tag :type="row.employmentStatus === 'ACTIVE' ? 'success' : 'info'">
            {{ labelOf(EMPLOYMENT_STATUS, row.employmentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.employmentStatus === 'ACTIVE'" link type="warning"
                     @click="updateStatus(row.id, 'RESIGNED')">设为离职</el-button>
          <el-button v-else link type="success" @click="updateStatus(row.id, 'ACTIVE')">恢复在职</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" title="新建员工" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="员工姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="所属校区" required>
          <el-select v-model="form.campusId" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'
import { EMPLOYMENT_STATUS, labelOf } from '@/constants'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const keyword = ref('')
const dialogVisible = ref(false)
const form = reactive({ name: '', phone: '', campusId: null })

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/employees', { params: { ...params, name: keyword.value || undefined } })
)

function openDialog() {
  Object.assign(form, { name: '', phone: '', campusId: campuses.value[0]?.id || null })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入员工姓名')
    return
  }
  if (!form.campusId) {
    ElMessage.warning('请选择所属校区')
    return
  }
  await request.post('/employees', form)
  showSuccess()
  dialogVisible.value = false
  load()
}

async function updateStatus(id, status) {
  await request.put(`/employees/${id}/status`, null, { params: { status } })
  showSuccess()
  load()
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
</style>
