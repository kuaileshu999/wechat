export const SUBJECTS = [
  { label: '语文', value: 'CHINESE' },
  { label: '数学', value: 'MATH' },
  { label: '英语', value: 'ENGLISH' },
  { label: '物理', value: 'PHYSICS' },
  { label: '历史', value: 'HISTORY' },
  { label: '地理', value: 'GEOGRAPHY' }
]

export const PAYMENT_METHODS = [
  { label: '支付宝', value: 'ALIPAY' },
  { label: '微信', value: 'WECHAT' },
  { label: '现金', value: 'CASH' }
]

export const CONSUMPTION_MODES = [
  { label: '每次扣金额', value: 'AMOUNT' },
  { label: '每次扣课时', value: 'HOURS' }
]

export const EMPLOYMENT_STATUS = [
  { label: '在职', value: 'ACTIVE' },
  { label: '离职', value: 'RESIGNED' }
]

export const ORDER_STATUS = [
  { label: '正常', value: 'ACTIVE' },
  { label: '部分退款', value: 'PARTIAL_REFUND' },
  { label: '已退款', value: 'REFUNDED' }
]

export const SCHEDULE_STATUS = [
  { label: '待上课', value: 'PENDING' },
  { label: '已上课', value: 'COMPLETED' }
]

export function labelOf(list, value) {
  return list.find(item => item.value === value)?.label || value
}
