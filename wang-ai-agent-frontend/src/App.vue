<template>
  <div class="layout">
    <header>
      <div class="logo">AI Agent</div>
      <nav>
        <router-link to="/">主页</router-link>
        <router-link to="/love">AI 恋爱军师</router-link>
        <router-link to="/manus">AI 超级智能体</router-link>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
// 无需脚本逻辑
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}
header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: #222;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.logo {
  font-size: 1.5rem;
  font-weight: bold;
  letter-spacing: 2px;
}
nav {
  display: flex;
  gap: 18px;
}
nav a {
  color: #fff;
  text-decoration: none;
  font-size: 1rem;
  padding: 6px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}
nav a.router-link-active {
  background: #42b983;
  color: #fff;
}
main {
  flex: 1;
  padding: 24px 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
@media (max-width: 900px) {
  main {
    padding: 12px 0;
  }
  header {
    flex-direction: column;
    height: auto;
    padding: 12px 0;
    gap: 8px;
  }
}
@media (max-width: 600px) {
  .logo {
    font-size: 1.1rem;
  }
  nav a {
    font-size: 0.95rem;
    padding: 4px 8px;
  }
}
</style> 