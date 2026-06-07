<template>
  <div class="create-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">← 返回</el-button>
        <div>
          <h3>新建接管配置</h3>
          <p>批量选择辅导老师及企微账号，统一配置接管者</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button
          v-if="currentStep < 2"
          type="primary"
          :disabled="!canNext"
          @click="currentStep++"
        >
          下一步
        </el-button>
        <el-button
          v-else
          type="primary"
          :loading="saving"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          确认创建（{{ selectedAccountIds.length }} 个账号）
        </el-button>
      </div>
    </div>

    <el-steps :active="currentStep" align-center class="steps-bar">
      <el-step title="选择辅导老师及账号" />
      <el-step title="选择接管者与生效时间" />
      <el-step title="确认并生效" />
    </el-steps>

    <div v-show="currentStep === 0" class="step-panel page-card">
      <div class="step-toolbar">
        <el-input v-model="searchName" placeholder="搜索辅导老师姓名" clearable style="width: 220px" />
        <el-select v-model="filterGroupId" placeholder="全部教研组" clearable style="width: 180px">
          <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
        <span class="selected-summary">已选 {{ selectedTutorCount }} 位老师，共 {{ selectedAccountIds.length }} 个账号</span>
      </div>

      <div class="tutor-grid">
        <div v-for="tutor in filteredTutors" :key="tutor.id" class="tutor-card">
          <div class="tutor-card-head">
            <el-checkbox
              :model-value="isTutorFullySelected(tutor)"
              :indeterminate="isTutorPartialSelected(tutor)"
              @change="(val) => toggleTutor(tutor, val)"
            >
              <span class="tutor-name">{{ tutor.name }}</span>
              <span class="tutor-group">{{ tutor.teachingGroupName }}</span>
            </el-checkbox>
          </div>
          <div class="account-list">
            <label v-for="acc in tutor.accounts" :key="acc.id" class="account-item">
              <el-checkbox
                :model-value="selectedAccountIds.includes(acc.id)"
                :disabled="acc.hosted"
                @change="(val) => toggleAccount(acc.id, val)"
              />
              <span class="acc-name">{{ acc.accountName }}</span>
              <span class="acc-meta">{{ acc.subject }} · {{ acc.grade }} · {{ acc.studentCount }} 学生</span>
              <el-tag v-if="acc.hosted" size="small" type="warning">已托管</el-tag>
            </label>
          </div>
        </div>
      </div>
      <el-empty v-if="!filteredTutors.length" description="暂无辅导老师" />
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
        <el-form-item label="接管说明">
          <el-input v-model="description" type="textarea" :rows="4" placeholder="请输入接管说明（可选）" />
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="批量接管提示：所选账号将统一由一位接管者处理消息；已被他人托管的账号将自动跳过。"
      />
    </div>

    <div v-show="currentStep === 2" class="step-panel page-card">
      <div class="confirm-block">
        <h4>已选账号（{{ selectedAccountIds.length }}）</h4>
        <div class="confirm-tags">
          <el-tag v-for="item in selectedAccountSummary" :key="item.id" class="confirm-tag">
            {{ item.tutorName }} · {{ item.accountName }}
          </el-tag>
        </div>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="接管者">
          {{ selectedManagerName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="生效方式">
          {{ effectiveType === 1 ? '立即生效' : '定时生效' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="effectiveType === 2" label="生效时间">
          {{ scheduledStartAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="接管说明">
          {{ description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { hostingConfigApi, takeoverManagerApi, teachingGroupApi, tutorApi } from '../api'
import { getUser } from '../utils/auth'

const router = useRouter()
const currentStep = ref(0)
const saving = ref(false)
const tutors = ref([])
const managers = ref([])
const groups = ref([])
const searchName = ref('')
const filterGroupId = ref(null)
const selectedAccountIds = ref([])
const takeoverManagerId = ref(null)
const effectiveType = ref(1)
const scheduledStartAt = ref(null)
const description = ref('')

const filteredTutors = computed(() => {
  return tutors.value.filter((t) => {
    if (filterGroupId.value && t.teachingGroupId !== filterGroupId.value) return false
    if (searchName.value && !t.name.includes(searchName.value.trim())) return false
    return true
  })
})

const selectedTutorCount = computed(() => {
  const tutorIds = new Set()
  tutors.value.forEach((tutor) => {
    const selectable = (tutor.accounts || []).filter((a) => !a.hosted).map((a) => a.id)
    if (selectable.some((id) => selectedAccountIds.value.includes(id))) {
      tutorIds.add(tutor.id)
    }
  })
  return tutorIds.size
})

const selectedAccountSummary = computed(() => {
  const result = []
  tutors.value.forEach((tutor) => {
    ;(tutor.accounts || []).forEach((acc) => {
      if (selectedAccountIds.value.includes(acc.id)) {
        result.push({ id: acc.id, tutorName: tutor.name, accountName: acc.accountName })
      }
    })
  })
  return result
})

const selectedManagerName = computed(() => {
  const manager = managers.value.find((m) => m.id === takeoverManagerId.value)
  return manager?.name || ''
})

const canNext = computed(() => {
  if (currentStep.value === 0) return selectedAccountIds.value.length > 0
  if (currentStep.value === 1) {
    if (!takeoverManagerId.value) return false
    if (effectiveType.value === 2 && !scheduledStartAt.value) return false
    return true
  }
  return true
})

const canSubmit = computed(() => selectedAccountIds.value.length > 0 && takeoverManagerId.value)

function selectableAccountIds(tutor) {
  return (tutor.accounts || []).filter((a) => !a.hosted).map((a) => a.id)
}

function isTutorFullySelected(tutor) {
  const ids = selectableAccountIds(tutor)
  return ids.length > 0 && ids.every((id) => selectedAccountIds.value.includes(id))
}

function isTutorPartialSelected(tutor) {
  const ids = selectableAccountIds(tutor)
  const selected = ids.filter((id) => selectedAccountIds.value.includes(id))
  return selected.length > 0 && selected.length < ids.length
}

function toggleTutor(tutor, checked) {
  const ids = selectableAccountIds(tutor)
  if (checked) {
    selectedAccountIds.value = [...new Set([...selectedAccountIds.value, ...ids])]
  } else {
    selectedAccountIds.value = selectedAccountIds.value.filter((id) => !ids.includes(id))
  }
}

function toggleAccount(accountId, checked) {
  if (checked) {
    if (!selectedAccountIds.value.includes(accountId)) {
      selectedAccountIds.value = [...selectedAccountIds.value, accountId]
    }
  } else {
    selectedAccountIds.value = selectedAccountIds.value.filter((id) => id !== accountId)
  }
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
      accountIds: selectedAccountIds.value,
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
  const [groupList, tutorList, managerList] = await Promise.all([
    teachingGroupApi.list(),
    tutorApi.list(),
    takeoverManagerApi.list(),
  ])
  groups.value = groupList
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

.step-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.selected-summary {
  margin-left: auto;
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
}

.tutor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.tutor-card {
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}

.tutor-card-head {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid var(--border);
}

.tutor-name {
  font-weight: 600;
  margin-right: 8px;
}

.tutor-group {
  font-size: 12px;
  color: var(--text-secondary);
}

.account-list {
  padding: 8px 0;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

.acc-name {
  font-size: 14px;
}

.acc-meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: auto;
}

.config-form {
  max-width: 560px;
  margin-bottom: 16px;
}

.confirm-block {
  margin-bottom: 16px;
}

.confirm-block h4 {
  margin-bottom: 12px;
}

.confirm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.confirm-tag {
  margin: 0;
}
</style>
