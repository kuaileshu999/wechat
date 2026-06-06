<template>
  <div class="page-card logs-page">
    <div class="toolbar">
      <el-select v-model="tutorId" placeholder="全部辅导老师" clearable style="width: 200px" @change="loadData">
        <el-option v-for="t in tutors" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="logs" border stripe>
      <el-table-column prop="createdAt" label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="actionTypeLabel" label="操作类型" width="100" />
      <el-table-column prop="tutorName" label="辅导老师" width="100" />
      <el-table-column prop="conversationTitle" label="会话" width="120">
        <template #default="{ row }">{{ row.conversationTitle || '-' }}</template>
      </el-table-column>
      <el-table-column label="处理人变更" min-width="200">
        <template #default="{ row }">
          <span v-if="row.fromHandlerName">{{ row.fromHandlerName }}</span>
          <span v-else>-</span>
          <span v-if="row.toHandlerName"> → {{ row.toHandlerName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { transferLogApi, tutorApi } from '../api'

const loading = ref(false)
const logs = ref([])
const tutors = ref([])
const tutorId = ref(null)

async function loadData() {
  loading.value = true
  try {
    logs.value = await transferLogApi.list(tutorId.value ? { tutorId: tutorId.value } : undefined)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(async () => {
  tutors.value = await tutorApi.list()
  await loadData()
})
</script>

<style scoped>
.logs-page {
  height: calc(100vh - 56px - 40px);
  overflow: auto;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
