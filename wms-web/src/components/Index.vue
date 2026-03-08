<template>
  <div class="index-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapse }">
      <Aside :is-collapse="isCollapse" />
    </aside>

    <div class="main-container">
      <header class="header">
        <Header :icon="collapseIcon" @toggle-aside="toggleAside" />
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import Aside from './Aside.vue'
import Header from './Header.vue'

const isCollapse = ref(false)

const collapseIcon = computed(() => (isCollapse.value ? 'el-icon-s-unfold' : 'el-icon-s-fold'))

const toggleAside = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped>
.index-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: var(--color-bg);
  overflow: hidden;
}

.sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  background: #fff;
  box-shadow: var(--shadow-md);
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
  z-index: 100;
}

.sidebar.collapsed {
  width: 72px;
  min-width: 72px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.header {
  height: 64px;
  min-height: 64px;
  background: #fff;
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--color-border-light);
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--color-bg);
}

@media (max-width: 960px) {
  .sidebar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
  }
}
</style>
