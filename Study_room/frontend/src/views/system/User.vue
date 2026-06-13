<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button v-if="userStore.hasPermission('system:user:create')" type="primary" @click="openCreate()">
        新建用户
      </el-button>
    </div>
    <el-alert title="为员工创建系统账号并分配角色与校区。用户只能查看和操作已授权校区的数据；密码至少6位且不能全是数字。"
              type="info" show-icon :closable="false" class="mb-16" />

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="用户姓名" clearable style="width: 200px" @keyup.enter="load()" />
      <el-button type="primary" @click="load()">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="employeeName" label="关联员工" width="120" />
      <el-table-column label="授权校区" min-width="160">
        <template #default="{ row }">{{ (row.campusNames || []).join('、') }}</template>
      </el-table-column>
      <el-table-column label="角色" min-width="140">
        <template #default="{ row }">{{ (row.roleNames || []).join('、') }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
          <el-tag v-if="row.locked === 1" type="danger" class="ml-4">锁定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openResetPwd(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-16" background layout="total, prev, pager, next"
                   :total="total" :current-page="page" @current-change="onPageChange" />

    <el-dialog v-model="createVisible" title="新建用户" width="520px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="关联员工" required>
          <el-select v-model="createForm.employeeId" filterable style="width: 100%" @change="onEmployeeSelect">
            <el-option v-for="e in employeesWithoutAccount" :key="e.id"
                       :label="`${e.name} (${campusMap[e.campusId] || ''})`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="createForm.username" placeholder="默认使用员工手机号" />
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input v-model="createForm.password" type="password" show-password placeholder="至少6位，不能全是数字" />
        </el-form-item>
        <el-form-item label="授权校区" required>
          <el-select v-model="createForm.campusIds" multiple style="width: 100%">
            <el-option v-for="c in allCampuses" :key="c.id" :label="c.name" :value="c.id"
                       :disabled="c.status === 0" />
          </el-select>
        </el-form-item>
        <el-form-item label="授权角色" required>
          <el-select v-model="createForm.roleIds" multiple style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑用户" width="520px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户">
          <span>{{ editForm.realName }}（{{ editForm.username }}）</span>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-switch v-model="editForm.enabled" :active-value="1" :inactive-value="0"
                     active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="授权校区" required>
          <el-select v-model="editForm.campusIds" multiple style="width: 100%">
            <el-option v-for="c in allCampuses" :key="c.id" :label="c.name" :value="c.id"
                       :disabled="c.status === 0" />
          </el-select>
        </el-form-item>
        <el-form-item label="授权角色" required>
          <el-select v-model="editForm.roleIds" multiple style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="重置密码" width="480px">
      <el-form :model="pwdForm" label-width="100px">
        <el-form-item label="用户">
          <span>{{ pwdForm.realName }}（{{ pwdForm.username }}）</span>
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位，不能全是数字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="warning" @click="submitResetPwd">重置并解锁</el-button>
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
const { campusMap } = useCampuses()
const keyword = ref('')
const roles = ref([])
const allCampuses = ref([])
const employeesWithoutAccount = ref([])
const createVisible = ref(false)
const editVisible = ref(false)
const pwdVisible = ref(false)

const createForm = reactive({
  employeeId: null, username: '', password: '', campusIds: [], roleIds: []
})
const editForm = reactive({
  id: null, username: '', realName: '', enabled: 1, campusIds: [], roleIds: []
})
const pwdForm = reactive({ userId: null, username: '', realName: '', newPassword: '' })

const { list, total, page, loading, load, onPageChange } = usePagination(
  params => request.get('/users', { params: { ...params, keyword: keyword.value || undefined } })
)

async function loadRoles() {
  roles.value = await request.get('/auth/roles')
}

async function loadAllCampuses() {
  allCampuses.value = await request.get('/auth/campuses', { params: { manage: true } })
}

async function loadEmployeesWithoutAccount() {
  employeesWithoutAccount.value = await request.get('/employees/without-account')
}

function onEmployeeSelect(employeeId) {
  const employee = employeesWithoutAccount.value.find(e => e.id === employeeId)
  if (!employee) return
  createForm.username = employee.phone || ''
  if (createForm.campusIds.length === 0 && employee.campusId) {
    createForm.campusIds = [employee.campusId]
  }
}

function openCreate() {
  Object.assign(createForm, { employeeId: null, username: '', password: '', campusIds: [], roleIds: [] })
  loadEmployeesWithoutAccount()
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.employeeId || !createForm.username || !createForm.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!createForm.campusIds.length || !createForm.roleIds.length) {
    ElMessage.warning('请选择校区和角色')
    return
  }
  await request.post('/users', createForm)
  showSuccess()
  createVisible.value = false
  load()
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    enabled: row.enabled,
    campusIds: [...(row.campusIds || [])],
    roleIds: [...(row.roleIds || [])]
  })
  editVisible.value = true
}

async function submitEdit() {
  await request.put(`/users/${editForm.id}`, {
    enabled: editForm.enabled,
    campusIds: editForm.campusIds,
    roleIds: editForm.roleIds
  })
  showSuccess()
  editVisible.value = false
  load()
}

function openResetPwd(row) {
  Object.assign(pwdForm, {
    userId: row.id,
    username: row.username,
    realName: row.realName,
    newPassword: ''
  })
  pwdVisible.value = true
}

async function submitResetPwd() {
  if (!pwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  await request.post(`/auth/reset-password/${pwdForm.userId}`, { newPassword: pwdForm.newPassword })
  showSuccess('密码已重置，账号已解锁')
  pwdVisible.value = false
  load()
}

onMounted(() => {
  load()
  loadRoles()
  loadAllCampuses()
})
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }
.ml-4 { margin-left: 4px; }
</style>
