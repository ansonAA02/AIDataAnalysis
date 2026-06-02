/// <reference types="vitest/config" />

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

const config = {
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 700,
    rollupOptions: {
      output: {
        manualChunks: {
          echarts: ['echarts/core', 'echarts/components', 'echarts/charts', 'echarts/renderers'],
        },
      },
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    // OneDrive + Docker bind mount on Windows blocks inotify events.
    // Use polling so Vite picks up newly created/edited files inside the container.
    watch: {
      usePolling: true,
      interval: 300,
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
  },
};

export default defineConfig(config);
