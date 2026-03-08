import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentUser } from '@/utils/session'

const dynamicViews = {
  ...import.meta.glob('../components/Main.vue'),
  ...import.meta.glob('../components/admin/AdminManage.vue'),
  ...import.meta.glob('../components/goods/GoodsManage.vue'),
  ...import.meta.glob('../components/goodstype/GoodstypeManage.vue'),
  ...import.meta.glob('../components/record/RecordManage.vue'),
  ...import.meta.glob('../components/storage/StorageManage.vue'),
  ...import.meta.glob('../components/user/UserManage.vue'),
  ...import.meta.glob('../components/advanced/*.vue'),
}

const dynamicRouteNames = new Set()
const builtinMenuClicks = new Set(['Home', 'Profile', 'Alerts', 'Reports', 'Transfers', 'Batches', 'Scanner', 'Suppliers', 'AuditLogs'])

const routes = [
  {
    path: '/',
    name: 'login',
    component: () => import('../components/Login.vue'),
  },
  {
    path: '/Index',
    name: 'index',
    component: () => import('../components/Index.vue'),
    redirect: '/Home',
    children: [
      {
        path: '/Home',
        name: 'home',
        meta: { title: '首页' },
        component: () => import('../components/Home.vue'),
      },
      {
        path: '/Profile',
        name: 'profile',
        meta: { title: '个人中心' },
        component: () => import('../components/Profile.vue'),
      },
      {
        path: '/Alerts',
        name: 'alerts',
        meta: { title: '库存预警' },
        component: () => import('../components/advanced/InventoryAlert.vue'),
      },
      {
        path: '/Reports',
        name: 'reports',
        meta: { title: '报表中心' },
        component: () => import('../components/advanced/ReportCenter.vue'),
      },
      {
        path: '/Transfers',
        name: 'transfers',
        meta: { title: '仓库调拨' },
        component: () => import('../components/advanced/WarehouseTransfer.vue'),
      },
      {
        path: '/Batches',
        name: 'batches',
        meta: { title: '批次管理' },
        component: () => import('../components/advanced/BatchManage.vue'),
      },
      {
        path: '/Scanner',
        name: 'scanner',
        meta: { title: '条码扫描' },
        component: () => import('../components/advanced/ScanCenter.vue'),
      },
      {
        path: '/Suppliers',
        name: 'suppliers',
        meta: { title: '供应商管理' },
        component: () => import('../components/advanced/SupplierManage.vue'),
      },
      {
        path: '/AuditLogs',
        name: 'auditLogs',
        meta: { title: '操作日志' },
        component: () => import('../components/advanced/OperationLog.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: () => (getCurrentUser() ? '/Home' : '/'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

function normalizeComponentPath(componentPath) {
  if (!componentPath) {
    return null
  }

  const normalized = componentPath
    .replace(/^\/+/, '')
    .replace(/^components\//, '')
    .replace(/^\.\/+/, '')

  return normalized.endsWith('.vue') ? normalized : `${normalized}.vue`
}

function resolveMenuComponent(componentPath) {
  const normalizedPath = normalizeComponentPath(componentPath)
  if (!normalizedPath) {
    return null
  }

  return dynamicViews[`../components/${normalizedPath}`] || null
}

export function resetDynamicRoutes() {
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })

  dynamicRouteNames.clear()
}

export function registerDynamicRoutes(menuList = []) {
  resetDynamicRoutes()

  menuList.forEach((menu) => {
    const routeName = `menu-${menu.menuclick}`
    const component = resolveMenuComponent(menu.menucomponent)

    if (!component || router.hasRoute(routeName) || builtinMenuClicks.has(menu.menuclick)) {
      return
    }

    router.addRoute('index', {
      path: `/${menu.menuclick}`,
      name: routeName,
      meta: {
        title: menu.menuname,
      },
      component,
    })

    dynamicRouteNames.add(routeName)
  })
}

router.beforeEach((to, from, next) => {
  const currentUser = getCurrentUser()

  if (!currentUser && to.path !== '/') {
    next('/')
    return
  }

  if (currentUser && to.path === '/') {
    next('/Home')
    return
  }

  next()
})

export default router
