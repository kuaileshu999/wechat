<template>
  <div class="page-container">
    <div class="page-header">
      <h2>年级管理</h2>
      <el-button v-if="userStore.hasPermission('grade:create')" type="primary" @click="openDialog()">新建年级</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="年级名称" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPermission('grade:create')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-drawer v-model="dialogVisible" :title="editingId ? '编辑年级' : '新建年级'"
               direction="rtl" size="70%" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="年级名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { usePagination, showSuccess } from '@/composables/useCommon'

const userStore = useUserStore()
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', status: 1 })

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/grades', { params })
)

function openDialog() {
  editingId.value = null
  Object.assign(form, { name: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { name: row.name, status: row.status })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name?.trim()) {
    ElMessage.warning('请填写年级名称')
    return
  }
  if (editingId.value) {
    await request.put(`/grades/${editingId.value}`, { name: form.name.trim() })
  } else {
    await request.post('/grades', { name: form.name.trim(), status: 1 })
  }
  showSuccess()
  dialogVisible.value = false
  load()
}

async function toggleStatus(row) {
  await request.put(`/grades/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  showSuccess()
  load()
}

onMounted(load)
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
</style>
