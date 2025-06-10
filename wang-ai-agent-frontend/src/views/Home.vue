<template>
  <div class="home-container">
    <h1>AI Agent 前端主页</h1>
    <p>请选择上方导航栏切换不同的 AI 应用。</p>
  </div>
</template>

<script setup>
// 无需脚本
</script>

<style scoped>
.home-container {
  width: 100%;
  max-width: 500px;
  margin: 80px auto 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 48px 24px;
  text-align: center;
}
@media (max-width: 600px) {
  .home-container {
    margin-top: 32px;
    padding: 24px 8px;
  }
}
</style> 