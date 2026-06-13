<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button v-if="userStore.hasPermission('system:role:create')" type="primary" @click="openDialog()">
        新建角色
      </el-button>
    </div>

    <el-table :data="roles" v-loading="loading" border stripe>
      <el-table-column prop="name" label="角色名称" width="160" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="权限" min-width="280">
        <template #default="{ row }">
          <el-tag v-for="name in (row.permissionNames || []).slice(0, 5)" :key="name"
                  size="small" class="perm-tag">{{ name }}</el-tag>
          <span v-if="(row.permissionNames || []).length > 5">等 {{ row.permissionNames.length }} 项</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑权限</el-button>
          <el-button v-if="row.code !== 'SUPER_ADMIN'" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新建角色'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="如：校区财务" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="菜单权限" required>
          <div class="tree-box">
            <el-tree
              ref="treeRef"
              :data="permissionTree"
              show-checkbox
              check-strictly
              node-key="id"
              default-expand-all
              :props="{ label: 'name', children: 'children' }"
              @check="onPermissionCheck"
            />
          </div>
          <p class="tip">勾选菜单将自动勾选其下所有按钮权限；也可单独勾选按钮</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { showSuccess } from '@/composables/useCommon'

const userStore = useUserStore()
const roles = ref([])
const permissionTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const treeRef = ref()
const form = reactive({
  id: null,
  name: '',
  description: '',
  permissionIds: []
})

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await request.get('/roles')
  } finally {
    loading.value = false
  }
}

async function loadPermissionTree() {
  permissionTree.value = await request.get('/permissions/tree')
}

function collectButtonIds(node) {
  const ids = []
  if (node.type === 2) ids.push(node.id)
  for (const child of node.children || []) {
    ids.push(...collectButtonIds(child))
  }
  return ids
}

function onPermissionCheck(node, { checkedKeys }) {
  if (node.type !== 1) return
  const buttonIds = collectButtonIds(node)
  const merged = new Set(checkedKeys)
  if (checkedKeys.includes(node.id)) {
    buttonIds.forEach(id => merged.add(id))
  } else {
    buttonIds.forEach(id => merged.delete(id))
  }
  treeRef.value?.setCheckedKeys([...merged])
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    description: row?.description || '',
    permissionIds: row?.permissionIds ? [...row.permissionIds] : []
  })
  dialogVisible.value = true
  nextTick(() => {
    treeRef.value?.setCheckedKeys(form.permissionIds)
  })
}

async function submit() {
  const checked = treeRef.value?.getCheckedKeys() || []
  const permissionIds = [...checked]
  if (!form.name) {
    return
  }
  if (permissionIds.length === 0) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description,
      permissionIds
    }
    if (form.id) {
      await request.put(`/roles/${form.id}`, payload)
    } else {
      await request.post('/roles', payload)
    }
    showSuccess()
    dialogVisible.value = false
    loadRoles()
  } finally {
    submitting.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.name}」？`, '提示', { type: 'warning' })
  await request.delete(`/roles/${row.id}`)
  showSuccess()
  loadRoles()
}

onMounted(() => {
  loadRoles()
  loadPermissionTree()
})
</script>

<style scoped>
.tree-box {
  width: 100%;
  max-height: 360px;
  overflow: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
}

.tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}

.perm-tag {
  margin: 0 4px 4px 0;
}
</style>
