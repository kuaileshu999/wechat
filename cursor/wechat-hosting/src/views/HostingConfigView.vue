<template>
  <div class="hosting-list-page">
    <div class="stats-row">
      <div v-for="item in statCards" :key="item.label" class="stat-card page-card">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="list-panel page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="搜索辅导老师或接管者"
            clearable
            style="width: 240px"
            @keyup.enter="loadList"
            @clear="loadList"
          />
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="loadList">
            <el-option label="接管中" :value="1" />
            <el-option label="已解除" :value="2" />
          </el-select>
          <el-select
            v-model="accountFilter"
            placeholder="全部账号"
            clearable
            filterable
            style="width: 180px"
            @change="loadList"
          >
            <el-option
              v-for="acc in accountOptions"
              :key="acc.id"
              :label="acc.accountName"
              :value="acc.id"
            />
          </el-select>
          <el-button @click="loadList">查询</el-button>
        </div>
        <el-button type="primary" @click="goCreate">+ 新建分配</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="辅导老师" min-width="160">
          <template #default="{ row }">
            <div class="cell-main">{{ row.tutorName }}</div>
            <div class="cell-sub">{{ row.teachingGroupName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="企微账号" min-width="180">
          <template #default="{ row }">
            <div class="cell-main">{{ row.accountName }}</div>
            <div class="cell-sub">{{ row.wechatUserid }}</div>
          </template>
        </el-table-column>
        <el-table-column label="接管者" min-width="120">
          <template #default="{ row }">
            <div class="cell-main">{{ row.takeoverManagerName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '接管中' : '已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="primary" size="small" @click="handleRelease(row)">
              解除接管
            </el-button>
            <el-button v-else link type="primary" size="small" @click="handleReactivate(row)">
              重新接管
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hostingAssignmentApi, tutorApi } from '../api'
import { getUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref(null)
const accountFilter = ref(null)
const accountOptions = ref([])
const stats = ref({ activeCount: 0, tutorCount: 0, managerCount: 0, todayMessageCount: 0 })

const statCards = computed(() => [
  { label: '当前接管中', value: stats.value.activeCount },
  { label: '辅导老师', value: stats.value.tutorCount },
  { label: '接管者', value: stats.value.managerCount },
  { label: '今日接管消息', value: stats.value.todayMessageCount },
])

async function loadStats() {
  stats.value = await hostingAssignmentApi.stats()
}

async function loadAccountOptions() {
  const tutors = await tutorApi.list()
  accountOptions.value = tutors.flatMap((t) => t.accounts || [])
}

async function loadList() {
  loading.value = true
  try {
    const data = await hostingAssignmentApi.list({
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined,
      accountId: accountFilter.value ?? undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = data.list
    total.value = data.total
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/hosting-config/create')
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

async function handleRelease(row) {
  try {
    await ElMessageBox.confirm(`确认解除「${row.accountName}」的接管？`, '提示', { type: 'warning' })
    await hostingAssignmentApi.release(row.id, getUser().userId)
    ElMessage.success('已解除接管')
    await Promise.all([loadList(), loadStats()])
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleReactivate(row) {
  try {
    await hostingAssignmentApi.reactivate(row.id, getUser().userId)
    ElMessage.success('已重新接管')
    await Promise.all([loadList(), loadStats()])
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadAccountOptions(), loadList()])
})
</script>

<style scoped>
.hosting-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 56px - 40px);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  padding: 20px 24px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary);
  line-height: 1.2;
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.cell-main {
  font-weight: 500;
}

.cell-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
