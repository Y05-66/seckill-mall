<template>
  <div class="not-found-page container">
    <div class="not-found-content">
      <div class="not-found-icon">🔍</div>
      <h1>404</h1>
      <p class="not-found-text">抱歉，您访问的页面不存在</p>
      <p class="not-found-hint">可能的原因：链接错误、页面已删除或您没有访问权限</p>
      <div class="not-found-actions">
        <button class="btn-home" @click="$router.push('/')">
          🏠 返回首页
        </button>
        <button class="btn-back" @click="$router.back()">
          ← 返回上一页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  text-align: center;
}

.not-found-content {
  max-width: 480px;
  padding: 40px 20px;
}

.not-found-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.8;
}

.not-found-content h1 {
  font-size: 72px;
  font-weight: 800;
  color: var(--red);
  margin-bottom: 16px;
  line-height: 1;
}

.not-found-text {
  font-size: 18px;
  color: var(--text-main);
  margin-bottom: 8px;
  font-weight: 500;
}

.not-found-hint {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 32px;
}

.not-found-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-home, .btn-back {
  padding: 12px 24px;
  border-radius: var(--radius);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-home {
  background: var(--red);
  color: white;
  border: none;
}

.btn-home:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-back {
  background: white;
  color: var(--text-main);
  border: 1px solid var(--border);
}

.btn-back:hover {
  border-color: var(--red);
  color: var(--red);
}
</style>
