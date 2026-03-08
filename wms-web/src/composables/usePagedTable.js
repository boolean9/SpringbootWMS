import { ref } from 'vue'

export function usePagedTable(loader, initialPageSize = 10) {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(initialPageSize)

  const loadData = async () => {
    loading.value = true
    try {
      const result = await loader({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
      })

      tableData.value = Array.isArray(result?.data) ? result.data : []
      total.value = Number(result?.total || 0)
      return result
    } finally {
      loading.value = false
    }
  }

  const handleSizeChange = async (value) => {
    pageSize.value = value
    pageNum.value = 1
    await loadData()
  }

  const handleCurrentChange = async (value) => {
    pageNum.value = value
    await loadData()
  }

  return {
    loading,
    tableData,
    total,
    pageNum,
    pageSize,
    loadData,
    handleSizeChange,
    handleCurrentChange,
  }
}
