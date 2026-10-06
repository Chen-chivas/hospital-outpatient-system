# database

数据库脚本目录（建表、初始化数据、迁移脚本等）。

建议按模块拆分脚本，便于团队协作与后续扩展。模块建议见：`docs/项目结构与模块划分.md`。

已提供参考脚本：

- `database/schema/001_init.sql`：建表脚本（与后端 Flyway migration 保持一致）
- `database/schema/002_seed.sql`：初始化数据脚本占位（默认由后端启动自动初始化）
