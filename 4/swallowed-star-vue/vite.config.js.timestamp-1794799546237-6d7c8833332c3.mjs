// vite.config.js
import { defineConfig } from "file:///D:/workSpace/java/4/swallowed-star-vue/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/workSpace/java/4/swallowed-star-vue/node_modules/@vitejs/plugin-vue/dist/index.mjs";
var vite_config_default = defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5173,
    open: true,
    proxy: {
      // 开发环境把 /api 请求转发到 Spring Boot 后端（默认 8081）
      "/api": {
        target: "http://192.168.68.71:8081",
        changeOrigin: true
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcuanMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFx3b3JrU3BhY2VcXFxcamF2YVxcXFw0XFxcXHN3YWxsb3dlZC1zdGFyLXZ1ZVwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRDpcXFxcd29ya1NwYWNlXFxcXGphdmFcXFxcNFxcXFxzd2FsbG93ZWQtc3Rhci12dWVcXFxcdml0ZS5jb25maWcuanNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Q6L3dvcmtTcGFjZS9qYXZhLzQvc3dhbGxvd2VkLXN0YXItdnVlL3ZpdGUuY29uZmlnLmpzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSdcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXHJcblxyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gIHBsdWdpbnM6IFt2dWUoKV0sXHJcbiAgc2VydmVyOiB7XHJcbiAgICBob3N0OiB0cnVlLFxyXG4gICAgcG9ydDogNTE3MyxcclxuICAgIG9wZW46IHRydWUsXHJcbiAgICBwcm94eToge1xyXG4gICAgICAvLyBcdTVGMDBcdTUzRDFcdTczQUZcdTU4ODNcdTYyOEEgL2FwaSBcdThCRjdcdTZDNDJcdThGNkNcdTUzRDFcdTUyMzAgU3ByaW5nIEJvb3QgXHU1NDBFXHU3QUVGXHVGRjA4XHU5RUQ4XHU4QkE0IDgwODFcdUZGMDlcclxuICAgICAgJy9hcGknOiB7XHJcbiAgICAgICAgdGFyZ2V0OiAnaHR0cDovLzE5Mi4xNjguNjguNzE6ODA4MScsXHJcbiAgICAgICAgY2hhbmdlT3JpZ2luOiB0cnVlXHJcbiAgICAgIH1cclxuICAgIH1cclxuICB9XHJcbn0pXHJcbiJdLAogICJtYXBwaW5ncyI6ICI7QUFBOFMsU0FBUyxvQkFBb0I7QUFDM1UsT0FBTyxTQUFTO0FBRWhCLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQzFCLFNBQVMsQ0FBQyxJQUFJLENBQUM7QUFBQSxFQUNmLFFBQVE7QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQTtBQUFBLE1BRUwsUUFBUTtBQUFBLFFBQ04sUUFBUTtBQUFBLFFBQ1IsY0FBYztBQUFBLE1BQ2hCO0FBQUEsSUFDRjtBQUFBLEVBQ0Y7QUFDRixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
