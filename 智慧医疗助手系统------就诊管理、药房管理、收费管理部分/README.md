# Legacy Smart Medical Module

This directory is kept as the original standalone smart medical module and SQL reference.

The integrated project no longer starts this module directly and no longer uses this module's MySQL `smart_medical` configuration at runtime. The active runtime entry is:

```text
../backend
```

Use the repository root startup script:

```bat
..\start-integrated.cmd
```

The integrated backend uses the unified H2 database configured in `backend/src/main/resources/application.yml`.
