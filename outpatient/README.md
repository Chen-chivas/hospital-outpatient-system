# Legacy Outpatient Module

This directory is kept as the original standalone outpatient module for reference.

The integrated project no longer starts this module directly and no longer uses this module's MySQL `hospital` configuration at runtime. The active runtime entry is:

```text
../backend
```

Use the repository root startup script:

```bat
..\start-integrated.cmd
```

The integrated backend uses the unified H2 database configured in `backend/src/main/resources/application.yml`.
