<template>
  <div class="page-card managers-page">
    <div class="toolbar">
      <el-button @click="loadData">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="managers" border stripe>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="maxTutorCount" label="最大接管数" width="110" align="center" />
      <el-table-column prop="activeTutorCount" label="当前接管数" width="110" align="center" />
      <el-table-column label="负载" min-width="200">
        <template #default="{ row }">
          <el-progress
            :percentage="Math.round((row.activeTutorCount / row.maxTutorCount) * 100)"
            :status="row.activeTutorCount >= row.maxTutorCount ? 'exception' : ''"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { takeoverManagerApi } from '../api'

const loading = ref(false)
const managers = ref([])

async function loadData() {
  loading.value = true
  try {
    managers.value = await takeoverManagerApi.list()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.managers-page {
  height: calc(100vh - 56px - 40px);
  overflow: auto;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
