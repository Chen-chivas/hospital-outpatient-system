# frontend（Vue 3 + Vite）

本目录为前端工程，按业务模块提供基础页面：登录、用户管理、排班、挂号、就诊、收费、药房、审计。

## 本地启动

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

Vite 已配置代理：`/api` → `http://127.0.0.1:8080`（见 `frontend/vite.config.ts`），因此前端开发期不需要额外配置 CORS。
