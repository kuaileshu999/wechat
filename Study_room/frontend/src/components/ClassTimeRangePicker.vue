<template>
  <div class="class-time-range" :class="{ 'is-small': size === 'small' }">
    <div class="date-field">
      <el-date-picker
        v-model="datePart"
        type="date"
        :size="size"
        value-format="YYYY-MM-DD"
        format="YYYY-MM-DD"
        placeholder="日期"
        @change="onDateChange"
      />
    </div>
    <div class="time-field">
      <el-time-select
        v-model="startTimePart"
        :size="size"
        start="00:00"
        :step="step"
        end="23:45"
        placeholder="开始"
        @change="onStartChange"
      />
    </div>
    <span class="sep">至</span>
    <div class="time-field">
      <el-time-select
        v-model="endTimePart"
        :size="size"
        :start="minEndTime"
        :step="step"
        end="23:59"
        placeholder="结课"
        @change="onEndChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  start: { type: String, default: '' },
  end: { type: String, default: '' },
  size: { type: String, default: 'default' },
  step: { type: String, default: '00:15' },
  defaultDurationMinutes: { type: Number, default: 60 }
})
const emit = defineEmits(['update:start', 'update:end'])

const datePart = ref('')
const startTimePart = ref('')
const endTimePart = ref('')

const stepMinutes = computed(() => parseInt(props.step.split(':')[1], 10) || 15)

const minEndTime = computed(() => {
  if (!startTimePart.value) return '00:15'
  const [h, m] = startTimePart.value.split(':').map(Number)
  const next = h * 60 + m + stepMinutes.value
  const nh = Math.floor(next / 60) % 24
  const nm = next % 60
  return `${String(nh).padStart(2, '0')}:${String(nm).padStart(2, '0')}`
})

function roundToStepTime(value) {
  const total = dayjs(value).hour() * 60 + dayjs(value).minute()
  const rounded = Math.round(total / stepMinutes.value) * stepMinutes.value
  return dayjs(value).startOf('day').add(rounded, 'minute').format('HH:mm')
}

function addMinutesToTime(timeStr, minutes) {
  const [h, m] = timeStr.split(':').map(Number)
  const total = h * 60 + m + minutes
  const nh = Math.floor(total / 60) % 24
  const nm = total % 60
  return `${String(nh).padStart(2, '0')}:${String(nm).padStart(2, '0')}`
}

function isEndAfterStart(start, end) {
  if (!start || !end) return false
  const [sh, sm] = start.split(':').map(Number)
  const [eh, em] = end.split(':').map(Number)
  return eh * 60 + em > sh * 60 + sm
}

function emitStart() {
  if (!datePart.value || !startTimePart.value) {
    emit('update:start', '')
    return
  }
  emit('update:start', `${datePart.value} ${startTimePart.value}`)
}

function emitEnd() {
  if (!datePart.value || !endTimePart.value) {
    emit('update:end', '')
    return
  }
  emit('update:end', `${datePart.value} ${endTimePart.value}`)
}

function syncFromProps() {
  if (!props.start) {
    datePart.value = ''
    startTimePart.value = ''
    endTimePart.value = ''
    return
  }
  const start = dayjs(props.start)
  datePart.value = start.format('YYYY-MM-DD')
  startTimePart.value = roundToStepTime(props.start)
  if (props.end) {
    endTimePart.value = roundToStepTime(props.end)
  } else if (startTimePart.value) {
    endTimePart.value = addMinutesToTime(startTimePart.value, props.defaultDurationMinutes)
  }
}

function onDateChange() {
  emitStart()
  emitEnd()
}

function onStartChange() {
  if (startTimePart.value && (!endTimePart.value || !isEndAfterStart(startTimePart.value, endTimePart.value))) {
    endTimePart.value = addMinutesToTime(startTimePart.value, props.defaultDurationMinutes)
  }
  emitStart()
  emitEnd()
}

function onEndChange() {
  if (startTimePart.value && endTimePart.value && !isEndAfterStart(startTimePart.value, endTimePart.value)) {
    endTimePart.value = addMinutesToTime(startTimePart.value, stepMinutes.value)
  }
  emitEnd()
}

watch(() => [props.start, props.end], syncFromProps, { immediate: true })
</script>

<style scoped>
.class-time-range {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.date-field {
  width: 128px;
  flex-shrink: 0;
}

.time-field {
  width: 92px;
  flex-shrink: 0;
}

.date-field :deep(.el-date-editor),
.time-field :deep(.el-select) {
  width: 100% !important;
}

.class-time-range.is-small .date-field {
  width: 118px;
}

.class-time-range.is-small .time-field {
  width: 86px;
}

.sep {
  flex-shrink: 0;
  color: #909399;
  font-size: 13px;
  padding: 0 2px;
}
</style>
