<template>
  <UserCrudPage page-title="用户选择" :role-id="2" select-mode @do-select-user="handleSelect" />
</template>

<script setup>
import UserCrudPage from './UserCrudPage.vue'

const emit = defineEmits(['doSelectUser'])

const handleSelect = (user) => {
  emit('doSelectUser', user)
}
</script>
