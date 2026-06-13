<template>
  <div class="page-container">
    <div class="page-header">
      <h2>财务管理</h2>
    </div>
    <el-tabs v-model="activeTab" @tab-change="loadReport">
      <el-tab-pane label="按天统计" name="day">
        <div class="search-bar">
          <el-date-picker v-model="dayRange" type="daterange" range-separator="至"
                          start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
          <el-select v-model="campusId" placeholder="校区" clearable style="width: 160px">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="loadReport">查询</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="按月统计" name="month">
        <div class="search-bar">
          <el-date-picker v-model="month" type="month" value-format="YYYY-MM" placeholder="选择月份" />
          <el-select v-model="campusId" placeholder="校区" clearable style="width: 160px">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="loadReport">查询</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="按校区统计" name="campus">
        <div class="search-bar">
          <el-date-picker v-model="campusRange" type="daterange" range-separator="至"
                          start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="loadReport">查询</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-table :data="reportData" v-loading="loading" border stripe show-summary>
      <el-table-column v-if="activeTab === 'day'" prop="date" label="日期" />
      <el-table-column v-if="activeTab === 'month'" prop="month" label="月份" />
      <el-table-column v-if="activeTab === 'campus'" prop="campusName" label="校区" />
      <el-table-column prop="totalPaidAmount" label="收款金额" />
      <el-table-column prop="totalConsumedAmount" label="已消课金额" />
      <el-table-column prop="totalPendingAmount" label="待消课金额" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import { useCampuses } from '@/composables/useCommon'

const { campuses } = useCampuses()
const activeTab = ref('day')
const loading = ref(false)
const reportData = ref([])
const campusId = ref(null)
const dayRange = ref([dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const month = ref(dayjs().format('YYYY-MM'))
const campusRange = ref([dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])

async function loadReport() {
  loading.value = true
  try {
    if (activeTab.value === 'day') {
      const [startDate, endDate] = dayRange.value || []
      reportData.value = await request.get('/finance/by-day', { params: { startDate, endDate, campusId: campusId.value } })
    } else if (activeTab.value === 'month') {
      reportData.value = await request.get('/finance/by-month', { params: { month: month.value, campusId: campusId.value } })
    } else {
      const [startDate, endDate] = campusRange.value || []
      reportData.value = await request.get('/finance/by-campus', { params: { startDate, endDate } })
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadReport)
</script>
