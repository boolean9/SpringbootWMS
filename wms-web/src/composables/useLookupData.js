import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

export function useLookupData() {
  const goodsOptions = ref([])
  const storageOptions = ref([])
  const goodstypeOptions = ref([])
  const supplierOptions = ref([])

  const loadGoods = async () => {
    try {
      const result = await request.get('/goods/list')
      goodsOptions.value = result.code === 200 ? result.data : []
      if (result.code !== 200) {
        ElMessage.error(result.msg || '获取货品列表失败')
      }
    } catch (error) {
      goodsOptions.value = []
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取货品列表失败')
    }
  }

  const loadStorages = async () => {
    try {
      const result = await request.get('/storage/list')
      storageOptions.value = result.code === 200 ? result.data : []
      if (result.code !== 200) {
        ElMessage.error(result.msg || '获取仓库列表失败')
      }
    } catch (error) {
      storageOptions.value = []
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取仓库列表失败')
    }
  }

  const loadGoodstypes = async () => {
    try {
      const result = await request.get('/goodstype/list')
      goodstypeOptions.value = result.code === 200 ? result.data : []
      if (result.code !== 200) {
        ElMessage.error(result.msg || '获取分类列表失败')
      }
    } catch (error) {
      goodstypeOptions.value = []
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取分类列表失败')
    }
  }

  const loadSuppliers = async () => {
    try {
      const result = await request.get('/supplier/list')
      supplierOptions.value = result.code === 200 ? result.data : []
      if (result.code !== 200) {
        ElMessage.error(result.msg || '获取供应商列表失败')
      }
    } catch (error) {
      supplierOptions.value = []
      ElMessage.error(error?.response?.data?.msg || error?.message || '获取供应商列表失败')
    }
  }

  const loadBaseLookups = async () => {
    await Promise.all([loadStorages(), loadGoodstypes(), loadSuppliers()])
  }

  const loadAllLookups = async () => {
    await Promise.all([loadGoods(), loadBaseLookups()])
  }

  return {
    goodsOptions,
    storageOptions,
    goodstypeOptions,
    supplierOptions,
    loadGoods,
    loadStorages,
    loadGoodstypes,
    loadSuppliers,
    loadBaseLookups,
    loadAllLookups,
  }
}
