import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 개발 중 /api 요청을 Spring Boot(8080)로 넘겨서 CORS 설정 없이 호출
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
