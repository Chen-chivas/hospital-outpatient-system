import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const LoginView = () => import('../views/LoginView.vue')
const MainLayout = () => import('../layouts/MainLayout.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const UsersView = () => import('../views/UsersView.vue')
const SchedulesView = () => import('../views/SchedulesView.vue')
const PatientRegistrationView = () => import('../views/PatientRegistrationView.vue')
const DoctorRegistrationView = () => import('../views/DoctorRegistrationView.vue')
const StopClinicReviewView = () => import('../views/StopClinicReviewView.vue')
const BillsView = () => import('../views/BillsView.vue')
const CashierBillsView = () => import('../views/CashierBillsView.vue')
const PharmacyView = () => import('../views/PharmacyView.vue')
const VisitsView = () => import('../views/VisitsView.vue')
const AuditView = () => import('../views/AuditView.vue')
const IntegrationView = () => import('../views/IntegrationView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: LoginView, meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '概览' } },
      { path: 'users', name: 'users', component: UsersView, meta: { roles: ['ADMIN'], title: '用户管理' } },
      { path: 'schedules', name: 'schedules', component: SchedulesView, meta: { roles: ['ADMIN'], title: '排班管理' } },
      { path: 'stop-clinic', name: 'stopClinicReview', component: StopClinicReviewView, meta: { roles: ['ADMIN'], title: '停诊审核' } },
      {
        path: 'patient/registrations',
        name: 'patientRegistrations',
        component: PatientRegistrationView,
        meta: { roles: ['PATIENT'], title: '挂号（患者）' },
      },
      {
        path: 'doctor/registrations',
        name: 'doctorRegistrations',
        component: DoctorRegistrationView,
        meta: { roles: ['DOCTOR'], title: '挂号列表（医生）' },
      },
      { path: 'visits', name: 'visits', component: VisitsView, meta: { roles: ['DOCTOR'], title: '就诊管理' } },
      { path: 'bills', name: 'bills', component: BillsView, meta: { roles: ['PATIENT'], title: '我的账单' } },
      {
        path: 'cashier/bills',
        name: 'cashierBills',
        component: CashierBillsView,
        meta: { roles: ['CASHIER', 'ADMIN'], title: '收费管理' },
      },
      { path: 'pharmacy', name: 'pharmacy', component: PharmacyView, meta: { roles: ['PHARMACIST', 'ADMIN'], title: '药房管理' } },
      { path: 'audit', name: 'audit', component: AuditView, meta: { roles: ['ADMIN'], title: '审计日志' } },
      { path: 'integrations', name: 'integrations', component: IntegrationView, meta: { title: '集成模块' } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'notFound', component: NotFoundView, meta: { public: true, title: '404' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return true
  }
  if (!auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!auth.meLoaded) {
    await auth.fetchMe()
  }
  if (!auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const requiredRoles = (to.meta.roles as string[] | undefined) ?? []
  if (requiredRoles.length > 0 && !auth.hasAnyRole(requiredRoles)) {
    return { path: '/dashboard' }
  }
  return true
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? '医院门诊管理系统'
  document.title = `${title} - 医院门诊管理系统`
})

export default router
