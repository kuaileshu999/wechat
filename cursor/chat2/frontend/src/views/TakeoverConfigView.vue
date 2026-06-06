<template>
  <div class="config-workbench">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="config-alert"
      title="按组织架构批量选择被接管的辅导；可进一步按年级、学科筛选企微账号。不选账号时默认接管该辅导的全部账号。"
    />

    <div class="config-body">
      <section class="batch-panel">
        <div class="panel-title">批量配置</div>

        <div class="org-block">
          <div class="block-label">组织架构 · 选择辅导</div>
          <el-tree
            ref="orgTreeRef"
            :data="orgTreeData"
            show-checkbox
            node-key="nodeKey"
            default-expand-all
            :props="{ label: 'label', children: 'children' }"
            class="org-tree"
            @check="onOrgCheck"
          />
          <div class="selected-tip">已选 {{ selectedTutorIds.length }} 名辅导</div>
        </div>

        <div v-if="selectedTutorIds.length" class="account-block">
          <div class="block-label">企微账号（年级 / 学科）</div>
          <div class="account-filters">
            <el-select v-model="filterGrade" placeholder="年级" clearable size="small" @change="loadAccounts">
              <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
            </el-select>
            <el-select v-model="filterSubject" placeholder="学科" clearable size="small" @change="loadAccounts">
              <el-option v-for="s in subjectOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </div>
          <el-checkbox v-model="assignAllAccounts" class="all-acc-check">
            不勾选下表时，默认接管已选辅导的全部账号
          </el-checkbox>
          <el-table
            ref="accountTableRef"
            :data="accountList"
            max-height="220"
            size="small"
            border
            @selection-change="onAccountSelectionChange"
          >
            <el-table-column type="selection" width="42" />
            <el-table-column prop="tutorName" label="辅导" width="88" />
            <el-table-column prop="accountName" label="账号" min-width="100" />
            <el-table-column prop="gradeLevel" label="年级" width="72" />
            <el-table-column prop="subject" label="学科" width="72" />
          </el-table>
        </div>

        <el-form label-width="88px" class="takeover-form">
          <el-form-item label="接管辅导" required>
            <el-select v-model="takeoverTutorId" placeholder="选择接管人" filterable style="width: 100%">
              <el-option v-for="t in takeoverTutors" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="enabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" :disabled="!selectedTutorIds.length" @click="handleBatchSave">
              保存批量配置
            </el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="list-panel">
        <div class="panel-title">
          已配置列表
          <span class="count">共 {{ assignments.length }} 条</span>
        </div>
        <el-table v-loading="loading" :data="assignments" border stripe height="100%" size="small">
          <el-table-column prop="sourceTutorName" label="被接管辅导" width="100" />
          <el-table-column prop="orgPath" label="组织" min-width="140" show-overflow-tooltip />
          <el-table-column label="企微账号" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="row.allAccounts" type="success" size="small">全部账号</el-tag>
              <span v-else>{{ row.wecomAccountName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="gradeLevel" label="年级" width="72" />
          <el-table-column prop="subject" label="学科" width="72" />
          <el-table-column prop="takeoverTutorName" label="接管人" width="88" />
          <el-table-column label="状态" width="72" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="72" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orgApi, takeoverApi, tutorApi, wecomApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const assignments = ref([])
const tutors = ref([])
const orgTreeRaw = ref([])
const orgTreeRef = ref(null)
const accountTableRef = ref(null)

const selectedTutorIds = ref([])
const selectedAccountIds = ref([])
const accountList = ref([])
const gradeOptions = ref([])
const subjectOptions = ref([])
const filterGrade = ref('')
const filterSubject = ref('')
const assignAllAccounts = ref(true)

const takeoverTutorId = ref(null)
const enabled = ref(true)

const takeoverTutors = computed(() => tutors.value.filter((t) => t.takeoverRole))

const orgTreeData = computed(() => transformOrgTree(orgTreeRaw.value))

function transformOrgTree(nodes) {
  if (!nodes?.length) return []
  return nodes.map((node) => {
    const deptChildren = transformOrgTree(node.children || [])
    const tutorChildren = (node.tutors || []).map((t) => ({
      nodeKey: `tutor-${t.id}`,
      label: `${t.name}`,
      deptType: 'TUTOR',
      tutorId: t.id,
      children: [],
    }))
    return {
      nodeKey: `dept-${node.id}`,
      label: node.name,
      deptType: node.deptType,
      deptId: node.id,
      children: [...deptChildren, ...tutorChildren],
    }
  })
}

function onOrgCheck() {
  const nodes = orgTreeRef.value?.getCheckedNodes(false, true) || []
  const ids = new Set()
  for (const n of nodes) {
    if (n.tutorId) ids.add(n.tutorId)
  }
  selectedTutorIds.value = [...ids]
}

async function loadAccounts() {
  if (!selectedTutorIds.value.length) {
    accountList.value = []
    return
  }
  try {
    const [accounts, options] = await Promise.all([
      wecomApi.query(selectedTutorIds.value, {
        gradeLevel: filterGrade.value || undefined,
        subject: filterSubject.value || undefined,
      }),
      wecomApi.filterOptions(selectedTutorIds.value),
    ])
    accountList.value = accounts
    gradeOptions.value = options.gradeLevels || []
    subjectOptions.value = options.subjects || []
    selectedAccountIds.value = []
    accountTableRef.value?.clearSelection()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onAccountSelectionChange(rows) {
  selectedAccountIds.value = rows.map((r) => r.id)
  if (selectedAccountIds.value.length > 0) {
    assignAllAccounts.value = false
  }
}

watch(selectedTutorIds, () => {
  filterGrade.value = ''
  filterSubject.value = ''
  loadAccounts()
})

async function loadAssignments() {
  loading.value = true
  try {
    assignments.value = await takeoverApi.list()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function handleBatchSave() {
  if (!selectedTutorIds.value.length || !takeoverTutorId.value) {
    ElMessage.warning('请选择辅导与接管人')
    return
  }
  saving.value = true
  try {
    const useAll = assignAllAccounts.value || selectedAccountIds.value.length === 0
    const result = await takeoverApi.batchSave({
      takeoverTutorId: takeoverTutorId.value,
      sourceTutorIds: selectedTutorIds.value,
      wecomAccountIds: useAll ? [] : selectedAccountIds.value,
      assignAllAccounts: useAll,
      enabled: enabled.value,
    })
    ElMessage.success(`已保存 ${result.total} 条（新增 ${result.created}，更新 ${result.updated}）`)
    await loadAssignments()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该接管配置？', '提示', { type: 'warning' })
    await takeoverApi.remove(id)
    ElMessage.success('已删除')
    await loadAssignments()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(async () => {
  tutors.value = await tutorApi.list()
  takeoverTutorId.value = takeoverTutors.value[0]?.id ?? null
  orgTreeRaw.value = await orgApi.tree()
  await loadAssignments()
})
</script>

<style scoped>
.config-workbench {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px - 40px);
  min-height: 520px;
  background: var(--card);
  border-radius: 8px;
  border: 1px solid var(--border);
  padding: 16px 20px;
  overflow: hidden;
}

.config-alert {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.config-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.batch-panel {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 8px;
  border-right: 1px solid var(--border);
}

.list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.panel-title .count {
  font-size: 13px;
  font-weight: normal;
  color: var(--text-secondary);
  margin-left: 8px;
}

.block-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.org-tree {
  max-height: 240px;
  overflow-y: auto;
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 8px;
}

.selected-tip {
  font-size: 12px;
  color: var(--primary);
  margin-top: 6px;
}

.account-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.account-filters .el-select {
  flex: 1;
}

.all-acc-check {
  margin-bottom: 8px;
}

.takeover-form {
  margin-top: 8px;
}
</style>
