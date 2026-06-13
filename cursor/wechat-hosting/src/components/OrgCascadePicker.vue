<template>
  <div class="org-tree-select">
    <el-tree-select
      :model-value="modelValue"
      :data="treeData"
      :loading="loading"
      multiple
      show-checkbox
      check-strictly
      check-on-click-node
      filterable
      collapse-tags
      collapse-tags-tooltip
      :max-collapse-tags="3"
      placeholder="请选择组织架构（可多选）"
      style="width: 100%"
      node-key="value"
      popper-class="org-tree-select-popper"
      @update:model-value="onSelectChange"
    />

    <div v-if="selectedItems.length" class="selected-tags">
      <span class="selected-label">已选 {{ selectedItems.length }} 个组：</span>
      <el-tag
        v-for="item in selectedItems"
        :key="item.id"
        closable
        size="small"
        type="primary"
        @close="removeOrg(item.id)"
      >
        {{ item.label }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { teachingGroupApi } from '../api'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  /** 可选：外部传入组织树；不传则从接口读取 */
  tree: { type: Array, default: null },
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const orgTree = ref([])
const nodeMap = ref(new Map())

const treeData = computed(() => toTreeSelectData(orgTree.value))

watch(
  () => props.tree,
  (tree) => {
    if (tree && tree.length) {
      orgTree.value = tree
      buildNodeMap(tree)
    }
  },
  { immediate: true }
)

watch(orgTree, (tree) => buildNodeMap(tree), { immediate: true })

onMounted(async () => {
  if (props.tree && props.tree.length) return
  loading.value = true
  try {
    orgTree.value = await teachingGroupApi.tree()
  } catch (e) {
    ElMessage.error(e.message || '加载组织架构失败')
  } finally {
    loading.value = false
  }
})

function buildNodeMap(tree) {
  const map = new Map()
  const walk = (nodes, ancestors = []) => {
    ;(nodes || []).forEach((node) => {
      map.set(node.id, { ...node, ancestors })
      if (node.children?.length) walk(node.children, [...ancestors, node.id])
    })
  }
  walk(tree || [])
  nodeMap.value = map
}

function toTreeSelectData(nodes) {
  return (nodes || []).map((node) => ({
    value: node.id,
    label: node.tutorCount != null ? `${node.name}（${node.tutorCount}人）` : node.name,
    children: node.children?.length ? toTreeSelectData(node.children) : undefined,
  }))
}

function onSelectChange(val) {
  const ids = (val || []).map((id) => Number(id)).filter((id) => !Number.isNaN(id))
  emit('update:modelValue', ids)
}

function removeOrg(id) {
  emit(
    'update:modelValue',
    props.modelValue.filter((item) => Number(item) !== Number(id))
  )
}

const selectedItems = computed(() =>
  props.modelValue
    .map((id) => {
      const node = nodeMap.value.get(Number(id))
      if (!node) return null
      const path = [...node.ancestors.map((aid) => nodeMap.value.get(aid)?.name).filter(Boolean), node.name]
      return { id: Number(id), label: path.join(' / ') }
    })
    .filter(Boolean)
)
</script>

<style scoped>
.org-tree-select {
  max-width: 640px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
}

.selected-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>

<style>
.org-tree-select-popper {
  min-width: 360px !important;
}

.org-tree-select-popper .el-select-dropdown__wrap {
  max-height: 400px;
}
</style>
