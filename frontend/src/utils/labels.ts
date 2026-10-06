export type ElTagType = 'success' | 'warning' | 'info' | 'danger' | ''

export function timePeriodLabel(tp?: string | null) {
  return tp === 'AM' ? '上午' : tp === 'PM' ? '下午' : tp ?? '-'
}

export function scheduleStatusLabel(status?: string | null) {
  return status === 'OPEN' ? '可挂号' : status === 'CLOSED' ? '已关闭' : status ?? '-'
}

export function scheduleStatusTag(status?: string | null): ElTagType {
  return status === 'OPEN' ? 'success' : 'info'
}

export function billStatusLabel(status?: string | null) {
  return status === 'UNPAID'
    ? '未支付'
    : status === 'PAID'
      ? '已支付'
      : status === 'VOIDED'
        ? '已作废'
        : status === 'REFUNDED'
          ? '已退款'
          : status ?? '-'
}

export function billStatusTag(status?: string | null): ElTagType {
  return status === 'PAID'
    ? 'success'
    : status === 'UNPAID'
      ? 'warning'
      : status === 'REFUNDED'
        ? 'info'
        : status === 'VOIDED'
          ? 'info'
          : 'info'
}

export function billSourceTypeLabel(sourceType?: string | null) {
  return sourceType === 'REGISTRATION' ? '挂号' : sourceType === 'PRESCRIPTION' ? '处方' : sourceType ?? '-'
}

export function paymentMethodLabel(method?: string | null) {
  return method === 'CASH'
    ? '现金'
    : method === 'WECHAT'
      ? '微信'
      : method === 'ALIPAY'
        ? '支付宝'
        : method === 'CARD'
          ? '银行卡'
          : method === 'INSURANCE'
            ? '医保'
            : method === 'OTHER'
              ? '其他'
              : method === 'UNKNOWN'
                ? '未知'
                : method ?? '-'
}

export function registrationStatusLabel(status?: string | null) {
  return status === 'CREATED'
    ? '待支付'
    : status === 'PAID'
      ? '已支付'
      : status === 'CANCELED'
        ? '已取消'
        : status === 'NO_SHOW'
          ? '爽约'
          : status === 'COMPLETED'
            ? '已完成'
            : status ?? '-'
}

export function registrationStatusTag(status?: string | null): ElTagType {
  return status === 'PAID'
    ? 'success'
    : status === 'CREATED'
      ? 'warning'
      : status === 'CANCELED'
        ? 'info'
        : status === 'NO_SHOW'
          ? 'danger'
          : 'info'
}

export function userStatusLabel(status?: string | null) {
  return status === 'ACTIVE' ? '启用' : status === 'DISABLED' ? '禁用' : status ?? '-'
}

export function userStatusTag(status?: string | null): ElTagType {
  return status === 'ACTIVE' ? 'success' : status === 'DISABLED' ? 'info' : 'info'
}

export function roleLabel(roleCode?: string | null) {
  return roleCode === 'ADMIN'
    ? '管理员'
    : roleCode === 'DOCTOR'
      ? '医生'
      : roleCode === 'NURSE'
        ? '护士'
        : roleCode === 'CASHIER'
          ? '收费员'
          : roleCode === 'PHARMACIST'
            ? '药师'
            : roleCode === 'PATIENT'
              ? '患者'
              : roleCode ?? '-'
}

export function patientTypeLabel(type?: string | null) {
  return type === 'SELF_PAY'
    ? '自费'
    : type === 'INSURANCE'
      ? '医保'
      : type === 'COMMERCIAL'
        ? '商保'
        : type === 'OTHER'
          ? '其他'
          : type === 'UNKNOWN'
            ? '未知'
            : type ?? '-'
}

export function visitStatusLabel(status?: string | null) {
  return status === 'IN_PROGRESS' ? '就诊中' : status === 'DONE' ? '已结束' : status ?? '-'
}

export function visitStatusTag(status?: string | null): ElTagType {
  return status === 'IN_PROGRESS' ? 'warning' : status === 'DONE' ? 'success' : 'info'
}

export function prescriptionStatusLabel(status?: string | null) {
  return status === 'ISSUED' ? '已开立' : status === 'DISPENSED' ? '已发药' : status ?? '-'
}

export function prescriptionStatusTag(status?: string | null): ElTagType {
  return status === 'ISSUED' ? 'warning' : status === 'DISPENSED' ? 'success' : 'info'
}

export function stopClinicStatusLabel(status?: string | null) {
  return status === 'PENDING' ? '待审核' : status === 'APPROVED' ? '已通过' : status === 'REJECTED' ? '已驳回' : status ?? '-'
}

export function stopClinicStatusTag(status?: string | null): ElTagType {
  return status === 'PENDING' ? 'warning' : status === 'APPROVED' ? 'success' : 'info'
}

export function auditActionLabel(action?: string | null) {
  return action === 'BOOK'
    ? '挂号'
    : action === 'CANCEL'
      ? '取消挂号'
      : action === 'RESCHEDULE'
        ? '改期'
        : action === 'NO_SHOW'
          ? '标记爽约'
          : action === 'PAY'
            ? '支付'
            : action === 'CREATE'
              ? '创建'
              : action === 'UPDATE'
                ? '更新'
                : action === 'UPDATE_STATUS'
                  ? '更新状态'
                  : action === 'RESET_PASSWORD'
                    ? '重置密码'
                    : action === 'START'
                      ? '开始'
                      : action === 'ISSUE'
                        ? '开立'
                        : action ?? '-'
}

export function auditModuleLabel(module?: string | null) {
  return module === 'billing'
    ? '收费'
    : module === 'registration'
      ? '挂号'
      : module === 'visit'
        ? '就诊'
        : module === 'user'
          ? '用户'
          : module === 'pharmacy'
            ? '药房'
            : module ?? '-'
}
