<template>
  <div class="page-card tutors-page">
    <div class="toolbar">
      <el-select v-model="groupId" placeholder="全部教研组" clearable style="width: 200px" @change="loadData">
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="tutors" border stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="teachingGroupName" label="教研组" width="120" />
      <el-table-column prop="accountCount" label="企微账号数" width="100" align="center" />
      <el-table-column label="企微账号" min-width="320">
        <template #default="{ row }">
          <div v-for="acc in row.accounts" :key="acc.id" class="account-line">
            <el-tag size="small">{{ acc.accountName }}</el-tag>
            <span class="acc-meta">{{ acc.subject }} · {{ acc.grade }} · {{ acc.studentCount }} 学生</span>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { teachingGroupApi, tutorApi } from '../api'

const loading = ref(false)
const tutors = ref([])
const groups = ref([])
const groupId = ref(null)

async function loadData() {
  loading.value = true
  try {
    tutors.value = await tutorApi.list(groupId.value ? { teachingGroupId: groupId.value } : undefined)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  groups.value = await teachingGroupApi.list()
  await loadData()
})
</script>

<style scoped>
.tutors-page {
  height: calc(100vh - 56px - 40px);
  overflow: auto;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.account-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.acc-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
