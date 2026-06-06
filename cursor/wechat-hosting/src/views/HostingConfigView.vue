<template>
  <div class="config-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="选择辅导老师及接管者，支持立即生效或定时生效。已被他人托管的老师将自动跳过。托管包含该老师全部企微账号（私聊+群聊）。"
      class="config-alert"
    />

    <div class="config-body">
      <section class="form-panel page-card">
        <div class="panel-title">新建接管配置</div>

        <el-form label-width="96px" class="config-form">
          <el-form-item label="教研组">
            <el-select v-model="filterGroupId" placeholder="全部教研组" clearable style="width: 100%" @change="loadTutors">
              <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="辅导老师" required>
            <el-checkbox-group v-model="selectedTutorIds" class="tutor-check-group">
              <el-checkbox v-for="t in tutors" :key="t.id" :label="t.id" border>
                <div class="tutor-card-inner">
                  <div class="tutor-name">{{ t.name }}</div>
                  <div class="tutor-sub">{{ t.teachingGroupName }} · {{ t.accountCount }} 个账号</div>
                </div>
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="!tutors.length" class="empty-tip">暂无辅导老师</div>
            <div v-else class="selected-tip">已选 {{ selectedTutorIds.length }} 位辅导老师</div>
          </el-form-item>

          <el-form-item label="接管者" required>
            <el-select v-model="takeoverManagerId" placeholder="选择接管者" filterable style="width: 100%">
              <el-option
                v-for="m in managers"
                :key="m.id"
                :label="`${m.name}（已接管 ${m.activeTutorCount}/${m.maxTutorCount}）`"
                :value="m.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="生效方式" required>
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
            <el-input v-model="description" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              :disabled="!selectedTutorIds.length || !takeoverManagerId"
              @click="handleCreate"
            >
              创建配置
            </el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="list-panel page-card">
        <div class="panel-title">
          配置列表
          <span class="count">共 {{ configs.length }} 条</span>
        </div>
        <el-table v-loading="loading" :data="configs" border stripe height="100%" size="small">
          <el-table-column prop="id" label="ID" width="56" />
          <el-table-column prop="takeoverManagerName" label="接管者" width="88" />
          <el-table-column label="辅导老师" min-width="140">
            <template #default="{ row }">
              <el-tag v-for="t in row.tutors" :key="t.tutorId" size="small" class="mr-tag">
                {{ t.tutorName }}
                <span v-if="t.status === 0" class="skip">(跳过)</span>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="生效方式" width="88">
            <template #default="{ row }">
              {{ row.effectiveType === 1 ? '立即' : '定时' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 1"
                link
                type="primary"
                size="small"
                @click="handleActivate(row.id)"
              >
                激活
              </el-button>
              <el-button
                v-if="row.status === 2"
                link
                type="danger"
                size="small"
                @click="handleEnd(row.id)"
              >
                结束
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hostingConfigApi, takeoverManagerApi, teachingGroupApi, tutorApi } from '../api'
import { getUser } from '../utils/auth'

const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const tutors = ref([])
const managers = ref([])
const groups = ref([])
const filterGroupId = ref(null)
const selectedTutorIds = ref([])
const takeoverManagerId = ref(null)
const effectiveType = ref(1)
const scheduledStartAt = ref(null)
const description = ref('')

async function loadGroups() {
  groups.value = await teachingGroupApi.list()
}

async function loadTutors() {
  tutors.value = await tutorApi.list(
    filterGroupId.value ? { teachingGroupId: filterGroupId.value } : undefined
  )
}

async function loadManagers() {
  managers.value = await takeoverManagerApi.list()
}

async function loadConfigs() {
  loading.value = true
  try {
    configs.value = await hostingConfigApi.list()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  saving.value = true
  try {
    await hostingConfigApi.create({
      takeoverManagerId: takeoverManagerId.value,
      effectiveType: effectiveType.value,
      scheduledStartAt: effectiveType.value === 2 ? scheduledStartAt.value : null,
      description: description.value || undefined,
      createdBy: getUser().userId,
      tutorIds: selectedTutorIds.value,
    })
    ElMessage.success('创建成功')
    selectedTutorIds.value = []
    description.value = ''
    await loadConfigs()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleActivate(id) {
  try {
    await hostingConfigApi.activate(id, getUser().userId)
    ElMessage.success('已激活')
    await loadConfigs()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function handleEnd(id) {
  try {
    await ElMessageBox.confirm('确认结束该托管配置？', '提示', { type: 'warning' })
    await hostingConfigApi.end(id, getUser().userId)
    ElMessage.success('已结束托管')
    await loadConfigs()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

function statusLabel(status) {
  return ['草稿', '待生效', '生效中', '已结束', '已取消'][status] || '未知'
}

function statusTagType(status) {
  if (status === 2) return 'success'
  if (status === 3 || status === 4) return 'info'
  return 'warning'
}

onMounted(async () => {
  await Promise.all([loadGroups(), loadTutors(), loadManagers(), loadConfigs()])
})
</script>

<style scoped>
.config-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 56px - 40px);
}

.config-body {
  flex: 1;
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 16px;
  min-height: 0;
}

.form-panel,
.list-panel {
  overflow: auto;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
}

.panel-title .count {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 400;
  margin-left: 8px;
}

.tutor-check-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.tutor-check-group :deep(.el-checkbox) {
  margin-right: 0;
  height: auto;
  padding: 8px 12px;
}

.tutor-name {
  font-weight: 600;
}

.tutor-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

.selected-tip,
.empty-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.mr-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.skip {
  color: #ff4d4f;
}
</style>
