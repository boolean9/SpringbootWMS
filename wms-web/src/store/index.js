import { createStore } from 'vuex'
import { registerDynamicRoutes } from '@/router'
import { clearSessionState, getMenuList, setMenuList } from '@/utils/session'

const store = createStore({
  state() {
    return {
      menu: getMenuList(),
    }
  },
  mutations: {
    setMenu(state, menuList) {
      const nextMenu = Array.isArray(menuList) ? menuList : []
      state.menu = nextMenu
      setMenuList(nextMenu)
      registerDynamicRoutes(nextMenu)
    },
    clearSession(state) {
      state.menu = []
      clearSessionState()
    },
  },
  getters: {
    menu(state) {
      return state.menu
    },
  },
})

export default store
