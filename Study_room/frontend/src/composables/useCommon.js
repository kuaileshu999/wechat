import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

export function useCampuses() {
  const campuses = ref([])
  const campusMap = ref({})

  async function loadCampuses() {
    const authorized = await request.get('/auth/campuses')
    campuses.value = authorized.filter(c => c.status !== 0)
    campusMap.value = Object.fromEntries(
      authorized.map(c => [c.id, c.status === 0 ? `${c.name}（已停用）` : c.name])
    )
  }

  onMounted(loadCampuses)
  return { campuses, campusMap, loadCampuses }
}

export function usePagination(fetchFn) {
  const list = ref([])
  const total = ref(0)
  const page = ref(1)
  const size = ref(10)
  const loading = ref(false)

  async function load(extra = {}) {
    loading.value = true
    try {
      const data = await fetchFn({ page: page.value, size: size.value, ...extra })
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  function onPageChange(p) {
    page.value = p
    load()
  }

  return { list, total, page, size, loading, load, onPageChange }
}

export function showSuccess(msg = '操作成功') {
  ElMessage.success(msg)
}
