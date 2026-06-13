<template>
  <div class="schedule-datetime">
    <el-date-picker
      v-model="datePart"
      type="date"
      :size="size"
      value-format="YYYY-MM-DD"
      format="YYYY-MM-DD"
      placeholder="日期"
      style="width: 100%"
      @change="emitValue"
    />
    <el-time-select
      v-model="timePart"
      :size="size"
      start="00:00"
      step="00:05"
      end="23:55"
      placeholder="时间"
      style="width: 100%"
      @change="emitValue"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: { type: String, default: '' },
  size: { type: String, default: 'default' }
})
const emit = defineEmits(['update:modelValue'])

const datePart = ref('')
const timePart = ref('')

function roundToFiveMinuteTime(value) {
  const total = dayjs(value).hour() * 60 + dayjs(value).minute()
  const rounded = Math.round(total / 5) * 5
  return dayjs(value).startOf('day').add(rounded, 'minute').format('HH:mm')
}

function syncFromModel(value) {
  if (!value) {
    datePart.value = ''
    timePart.value = ''
    return
  }
  const d = dayjs(value)
  datePart.value = d.format('YYYY-MM-DD')
  timePart.value = roundToFiveMinuteTime(value)
}

function emitValue() {
  if (!datePart.value || !timePart.value) {
    emit('update:modelValue', '')
    return
  }
  emit('update:modelValue', `${datePart.value} ${timePart.value}`)
}

watch(() => props.modelValue, syncFromModel, { immediate: true })
</script>

<style scoped>
.schedule-datetime {
  display: flex;
  gap: 8px;
  width: 100%;
}

.schedule-datetime > * {
  flex: 1;
  min-width: 0;
}
</style>
