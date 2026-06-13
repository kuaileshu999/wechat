<template>
  <div class="page-container">
    <div class="page-header">
      <h2>校区管理</h2>
      <el-button v-if="userStore.hasPermission('system:campus:create')" type="primary" @click="openDialog()">
        新建校区
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="校区名称" min-width="200" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑名称</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑校区' : '新建校区'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="校区名称" required>
          <el-input v-model="form.name" placeholder="请输入校区名称" maxlength="100" show-word-limit />
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
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { showSuccess } from '@/composables/useCommon'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '' })

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/auth/campuses', { params: { manage: true } })
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, { id: row?.id || null, name: row?.name || '' })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name?.trim()) return
  if (form.id) {
    await request.put(`/auth/campuses/${form.id}`, { name: form.name.trim() })
  } else {
    await request.post('/auth/campuses', { name: form.name.trim() })
  }
  showSuccess()
  dialogVisible.value = false
  load()
}

async function toggleStatus(row) {
  const action = row.status === 1 ? '停用' : '启用'
  await ElMessageBox.confirm(`确定${action}校区「${row.name}」？${row.status === 1 ? '停用后新建业务将无法选择该校区。' : ''}`, '提示', { type: 'warning' })
  await request.put(`/auth/campuses/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  showSuccess()
  load()
}

onMounted(load)
</script>
