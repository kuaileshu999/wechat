<template>
  <div class="page-container">
    <div class="page-header">
      <h2>课程类型管理</h2>
      <el-button v-if="userStore.hasPermission('course-type:create')" type="primary" @click="openDialog()">新建课程类型</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="校区">
        <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
      </el-table-column>
      <el-table-column prop="name" label="课程类型名称" />
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

    <el-dialog v-model="dialogVisible" title="新建课程类型" width="480px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="校区" required>
          <el-select v-model="form.campusId" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型名称" required>
          <el-input v-model="form.name" />
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

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const dialogVisible = ref(false)
const form = reactive({ campusId: null, name: '', status: 1 })

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/course-types', { params })
)

function openDialog() {
  Object.assign(form, { campusId: campuses.value[0]?.id || null, name: '', status: 1 })
  dialogVisible.value = true
}

async function submit() {
  await request.post('/course-types', form)
  showSuccess()
  dialogVisible.value = false
  load()
}

async function toggleStatus(row) {
  await request.put(`/course-types/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  showSuccess()
  load()
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
</style>
