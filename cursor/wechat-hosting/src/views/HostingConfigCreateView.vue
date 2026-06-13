<template>
  <div class="create-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">← 返回</el-button>
        <div>
          <h3>新建分配</h3>
          <p>按教研组、辅导老师、学生阶段配置筛选规则，匹配账号将纳入托管</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button v-if="currentStep < 2" type="primary" :disabled="!canNext" @click="currentStep++">
          下一步
        </el-button>
        <el-button v-else type="primary" :loading="saving" :disabled="!canSubmit" @click="handleSubmit">
          确认创建（{{ preview.availableCount }} 个可托管账号）
        </el-button>
      </div>
    </div>

    <el-steps :active="currentStep" align-center class="steps-bar">
      <el-step title="配置筛选条件" />
      <el-step title="选择接管者与生效时间" />
      <el-step title="确认并生效" />
    </el-steps>

    <div v-show="currentStep === 0" class="step-panel page-card">
      <div class="filter-section">
        <h4>按组织选择</h4>
        <p class="section-tip">从组织架构中勾选多个组；数据来自数据库，选中上级将包含其下全部辅导老师</p>
        <OrgCascadePicker v-model="selectedOrgIds" :tree="orgTree" />
      </div>

      <div class="filter-section">
        <h4>单独指定辅导老师</h4>
        <p class="section-tip">可搜索并添加任意辅导老师，无需属于上方已选教研组</p>
        <el-select
          v-model="pickerTutorId"
          filterable
          clearable
          placeholder="搜索辅导老师姓名"
          style="width: 320px"
          @change="addTutorByPicker"
        >
          <el-option
            v-for="t in tutorSearchOptions"
            :key="t.id"
            :label="`${t.name}（${t.teachingGroupName}）`"
            :value="t.id"
            :disabled="selectedTutorIds.includes(t.id)"
          />
        </el-select>
        <div v-if="selectedTutorTags.length" class="tag-row">
          <el-tag
            v-for="item in selectedTutorTags"
            :key="item.id"
            closable
            @close="removeTutor(item.id)"
          >
            {{ item.name }} · {{ item.teachingGroupName }}
          </el-tag>
        </div>
      </div>

      <div class="filter-section">
        <h4>账号阶段筛选</h4>
        <p class="section-tip">按学生所处阶段筛选企微账号；不选则包含全部阶段</p>
        <el-checkbox-group v-model="selectedStages">
          <el-checkbox :value="1" border>转化期</el-checkbox>
          <el-checkbox :value="2" border>承接期</el-checkbox>
        </el-checkbox-group>
      </div>

      <div class="filter-section">
        <h4>自动纳入</h4>
        <el-switch v-model="autoAssign" active-text="后续新增且匹配的账号自动纳入托管" />
      </div>

      <div v-loading="previewLoading" class="preview-panel">
        <div class="preview-stats">
          <div class="preview-stat">
            <span class="num">{{ preview.tutorCount }}</span>
            <span class="label">涉及辅导老师</span>
          </div>
          <div class="preview-stat">
            <span class="num">{{ preview.accountCount }}</span>
            <span class="label">匹配账号</span>
          </div>
          <div class="preview-stat ok">
            <span class="num">{{ preview.availableCount }}</span>
            <span class="label">可托管</span>
          </div>
          <div class="preview-stat warn">
            <span class="num">{{ preview.skippedHostedCount }}</span>
            <span class="label">已被托管（跳过）</span>
          </div>
        </div>
        <el-table v-if="preview.sampleAccounts?.length" :data="preview.sampleAccounts" border size="small" max-height="280">
          <el-table-column label="辅导老师" prop="tutorName" min-width="100" />
          <el-table-column label="教研组" prop="teachingGroupName" min-width="100" />
          <el-table-column label="企微账号" prop="accountName" min-width="140" />
          <el-table-column label="阶段" min-width="120">
            <template #default="{ row }">
              <el-tag v-for="s in row.stageLabels" :key="s" size="small" style="margin-right: 4px">{{ s }}</el-tag>
              <span v-if="!row.stageLabels?.length">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.hosted" type="warning" size="small">已托管</el-tag>
              <el-tag v-else type="success" size="small">可托管</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="hasFilter" description="当前筛选条件下无匹配账号" />
        <el-empty v-else description="请至少选择一个教研组或辅导老师" />
      </div>
    </div>

    <div v-show="currentStep === 1" class="step-panel page-card">
      <el-form label-width="100px" class="config-form">
        <el-form-item label="接管者" required>
          <el-select v-model="takeoverManagerId" placeholder="请选择接管者" filterable style="width: 100%">
            <el-option
              v-for="m in managers"
              :key="m.id"
              :label="`${m.name}（已接管 ${m.activeTutorCount}/${m.maxTutorCount}）`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生效时间" required>
          <el-radio-group v-model="effectiveType">
            <el-radio :value="1">立即生效</el-radio>
            <el-radio :value="2">定时生效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="effectiveType === 2" label="生效时间" required>
          <el-date-picker
            v-model="scheduledStartAt"
            type="datetime"
            placeholder="选择生效时间"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="description" type="textarea" :rows="4" placeholder="请输入接管说明（可选）" />
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="同一企微账号同时只能被一位接管者托管；已被他人托管的账号将自动跳过。"
      />
    </div>

    <div v-show="currentStep === 2" class="step-panel page-card">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="已选组织">
          {{ selectedOrgLabels.join('；') || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="指定辅导老师">
          {{ selectedTutorTags.map((t) => t.name).join('、') || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="阶段筛选">
          {{ stageSummary }}
        </el-descriptions-item>
        <el-descriptions-item label="自动纳入">
          {{ autoAssign ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="可托管账号">
          {{ preview.availableCount }} 个（共匹配 {{ preview.accountCount }} 个）
        </el-descriptions-item>
        <el-descriptions-item label="接管者">
          {{ selectedManagerName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="生效方式">
          {{ effectiveType === 1 ? '立即生效' : '定时生效' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="effectiveType === 2" label="生效时间">
          {{ scheduledStartAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="说明">
          {{ description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { hostingConfigApi, takeoverManagerApi, teachingGroupApi, tutorApi } from '../api'
import { getUser } from '../utils/auth'
import OrgCascadePicker from '../components/OrgCascadePicker.vue'

const router = useRouter()
const currentStep = ref(0)
const saving = ref(false)
const previewLoading = ref(false)
const tutors = ref([])
const managers = ref([])
const orgTree = ref([])
const selectedOrgIds = ref([])
const selectedTutorIds = ref([])
const selectedStages = ref([])
const autoAssign = ref(true)
const pickerTutorId = ref(null)
const takeoverManagerId = ref(null)
const effectiveType = ref(1)
const scheduledStartAt = ref(null)
const description = ref('')
const preview = ref({
  tutorCount: 0,
  accountCount: 0,
  availableCount: 0,
  skippedHostedCount: 0,
  sampleAccounts: [],
})

const hasFilter = computed(() => selectedOrgIds.value.length > 0 || selectedTutorIds.value.length > 0)

const tutorSearchOptions = computed(() => tutors.value)

const selectedOrgLabels = computed(() => {
  const nodeMap = new Map()
  const walk = (nodes, ancestors = []) => {
    ;(nodes || []).forEach((node) => {
      nodeMap.set(node.id, { name: node.name, ancestors })
      if (node.children?.length) walk(node.children, [...ancestors, node.id])
    })
  }
  walk(orgTree.value)
  return selectedOrgIds.value
    .map((id) => {
      const node = nodeMap.get(Number(id))
      if (!node) return null
      const path = [...node.ancestors.map((aid) => nodeMap.get(aid)?.name).filter(Boolean), node.name]
      return path.join(' / ')
    })
    .filter(Boolean)
})

const selectedTutorTags = computed(() =>
  selectedTutorIds.value
    .map((id) => tutors.value.find((t) => t.id === id))
    .filter(Boolean)
    .map((t) => ({ id: t.id, name: t.name, teachingGroupName: t.teachingGroupName }))
)

const stageSummary = computed(() => {
  if (!selectedStages.value.length) return '全部阶段'
  const map = { 1: '转化期', 2: '承接期' }
  return selectedStages.value.map((s) => map[s]).join('、')
})

const selectedManagerName = computed(() => {
  const manager = managers.value.find((m) => m.id === takeoverManagerId.value)
  return manager?.name || ''
})

const canNext = computed(() => {
  if (currentStep.value === 0) return hasFilter.value && preview.value.availableCount > 0
  if (currentStep.value === 1) {
    if (!takeoverManagerId.value) return false
    if (effectiveType.value === 2 && !scheduledStartAt.value) return false
    return true
  }
  return true
})

const canSubmit = computed(() => hasFilter.value && preview.value.availableCount > 0 && takeoverManagerId.value)

let previewTimer = null

function buildFilterPayload() {
  return {
    filterGroupIds: selectedOrgIds.value,
    filterTutorIds: selectedTutorIds.value,
    filterStages: selectedStages.value.length ? selectedStages.value : undefined,
  }
}

async function loadPreview() {
  if (!hasFilter.value) {
    preview.value = { tutorCount: 0, accountCount: 0, availableCount: 0, skippedHostedCount: 0, sampleAccounts: [] }
    return
  }
  previewLoading.value = true
  try {
    preview.value = await hostingConfigApi.preview(buildFilterPayload())
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    previewLoading.value = false
  }
}

function schedulePreview() {
  clearTimeout(previewTimer)
  previewTimer = setTimeout(loadPreview, 300)
}

watch([selectedOrgIds, selectedTutorIds, selectedStages], schedulePreview, { deep: true })

function addTutorByPicker(id) {
  if (id && !selectedTutorIds.value.includes(id)) {
    selectedTutorIds.value = [...selectedTutorIds.value, id]
  }
  pickerTutorId.value = null
}

function removeTutor(id) {
  selectedTutorIds.value = selectedTutorIds.value.filter((item) => item !== id)
}

function goBack() {
  router.push('/hosting-config')
}

async function handleSubmit() {
  saving.value = true
  try {
    await hostingConfigApi.create({
      takeoverManagerId: takeoverManagerId.value,
      effectiveType: effectiveType.value,
      scheduledStartAt: effectiveType.value === 2 ? scheduledStartAt.value : null,
      description: description.value || undefined,
      createdBy: getUser().userId,
      autoAssign: autoAssign.value,
      ...buildFilterPayload(),
    })
    ElMessage.success('创建成功')
    router.push('/hosting-config')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const [tree, tutorList, managerList] = await Promise.all([
    teachingGroupApi.tree(),
    tutorApi.list(),
    takeoverManagerApi.list(),
  ])
  orgTree.value = tree
  tutors.value = tutorList
  managers.value = managerList
})
</script>

<style scoped>
.create-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 56px - 40px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.header-left h3 {
  font-size: 18px;
  font-weight: 600;
}

.header-left p {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.steps-bar {
  background: var(--card);
  padding: 16px 24px;
  border-radius: 8px;
}

.step-panel {
  flex: 1;
  overflow: auto;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.section-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.group-grid {
  display: none;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.preview-panel {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.preview-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.preview-stat {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
}

.preview-stat .num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--primary);
}

.preview-stat.ok .num {
  color: #16a34a;
}

.preview-stat.warn .num {
  color: #d97706;
}

.preview-stat .label {
  font-size: 12px;
  color: var(--text-secondary);
}

.config-form {
  max-width: 560px;
  margin-bottom: 16px;
}
</style>
