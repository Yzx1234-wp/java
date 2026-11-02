import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../../composables/useToast.js'
import { isAuthError } from '../../../api/admin.js'

/**
 * 后台列表页通用逻辑：查询条件、客户端分页、全选、统一的错误提示与登录失效跳转。
 *
 * @param {Object} opts
 * @param {Function} opts.list   列表接口（接收查询对象，返回 { data: [] }）
 * @param {number} [opts.pageSize]
 * @param {Array<{key: string}>} [opts.filters] 需要纳入查询条件的字段
 */
export function useCrudPage({ list, pageSize = 10, filters = [] }) {
  const router = useRouter()
  const { show } = useToast()

  const rows = ref([])
  const loading = ref(false)
  const page = ref(1)
  const selected = ref([])
  const query = reactive(Object.fromEntries(filters.map(f => [f.key, ''])))

  const total = computed(() => rows.value.length)
  const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
  const pageRows = computed(() => rows.value.slice((page.value - 1) * pageSize, page.value * pageSize))
  const selectedRows = computed(() => rows.value.filter(r => selected.value.includes(r.id)))

  /** 统一错误处理；返回 true 表示已跳转登录页 */
  function fail(e, fallback = '操作失败') {
    if (isAuthError(e)) {
      show(e.message || '登录已失效，请重新登录', 'err')
      router.push('/admin/login')
      return true
    }
    show((e && e.message) || fallback, 'err')
    return false
  }

  /**
   * 重新拉取列表
   * @param {boolean} keepPage true 时停留在当前页（增删改后刷新用）
   */
  async function load(keepPage = false) {
    loading.value = true
    try {
      const res = await list({ ...query })
      rows.value = res.data || []
      selected.value = []
      if (!keepPage) page.value = 1
      if (page.value > pageCount.value) page.value = pageCount.value
    } catch (e) {
      fail(e, '加载失败')
    } finally {
      loading.value = false
    }
  }

  function reset() {
    filters.forEach(f => { query[f.key] = '' })
    load()
  }

  function clearSelection() {
    selected.value = []
  }

  return {
    rows, loading, page, pageCount, pageRows, total,
    selected, query, selectedRows,
    load, reset, fail, clearSelection
  }
}
