<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学员管理</h2>
      <div>
        <el-button v-if="userStore.hasPermission('student:import')" @click="importVisible = true">批量导入</el-button>
        <el-button v-if="userStore.hasPermission('student:create')" type="primary" @click="openDialog()">新建学员</el-button>
      </div>
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="学员姓名" clearable style="width: 200px" @keyup.enter="load()" />
      <el-button type="primary" @click="load()">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="学员姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="报名校区">
        <template #default="{ row }">{{ campusMap[row.campusId] }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPermission('student:update')" link type="primary"
                     @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑学员' : '新建学员'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学员姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" maxlength="11" placeholder="11位数字" />
        </el-form-item>
        <el-form-item label="报名校区" required>
          <el-select v-model="form.campusId" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingId" label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入学员" width="480px">
      <el-form label-width="90px">
        <el-form-item label="校区" required>
          <el-select v-model="importCampusId" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Excel文件">
          <input type="file" accept=".xlsx,.xls" @change="onFileChange" />
          <p class="tip">第一列：姓名，第二列：手机号（11位数字），第三列：备注</p>
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
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCampuses, usePagination, showSuccess } from '@/composables/useCommon'

const userStore = useUserStore()
const { campuses, campusMap } = useCampuses()
const keyword = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const importVisible = ref(false)
const importCampusId = ref(null)
const importFile = ref(null)
const importing = ref(false)
const form = reactive({ name: '', phone: '', campusId: null, remark: '' })

const PHONE_PATTERN = /^\d{11}$/

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/students', { params: { ...params, name: keyword.value || undefined } })
)

function validatePhone(phone) {
  if (!PHONE_PATTERN.test(phone)) {
    ElMessage.warning('手机号必须是11位数字')
    return false
  }
  return true
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, { name: row.name, phone: row.phone, campusId: row.campusId, remark: row.remark })
  } else {
    editingId.value = null
    Object.assign(form, { name: '', phone: '', campusId: campuses.value[0]?.id || null, remark: '' })
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入学员姓名')
    return
  }
  if (!validatePhone(form.phone?.trim())) {
    return
  }
  if (editingId.value) {
    if (!form.campusId) {
      ElMessage.warning('请选择报名校区')
      return
    }
    await request.put(`/students/${editingId.value}`, {
      name: form.name.trim(),
      phone: form.phone.trim(),
      campusId: form.campusId
    })
  } else {
    if (!form.campusId) {
      ElMessage.warning('请选择报名校区')
      return
    }
    await request.post('/students', {
      ...form,
      name: form.name.trim(),
      phone: form.phone.trim()
    })
  }
  showSuccess()
  dialogVisible.value = false
  load()
}

function onFileChange(e) {
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
    const count = await request.post(`/students/import?campusId=${importCampusId.value}`, formData)
    showSuccess(`成功导入 ${count} 条`)
    importVisible.value = false
    load()
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  load()
  importCampusId.value = campuses.value[0]?.id
})
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.tip { font-size: 12px; color: #909399; margin-top: 8px; }
</style>
