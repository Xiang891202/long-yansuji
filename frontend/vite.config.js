import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url' // 💡 確保引入這兩個工具

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 💡 設定 @ 指向 src 目錄
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
