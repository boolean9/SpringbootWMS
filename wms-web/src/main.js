import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router, { registerDynamicRoutes } from './router'
import store from './store'
import AppIcon from './components/common/AppIcon.vue'
import './assets/global.css'

registerDynamicRoutes(store.state.menu)

const app = createApp(App)

app.component('AppIcon', AppIcon)
app.use(store)
app.use(router)
app.use(ElementPlus, {
  size: 'default',
})
app.mount('#app')
