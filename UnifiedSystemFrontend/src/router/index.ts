import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '工作台', icon: 'DashboardOutlined' }
      },
      {
        path: '/system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'SettingOutlined' },
        children: [
          {
            path: '/system/user',
            name: 'UserManage',
            component: () => import('@/views/system/user/Index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: '/system/role',
            name: 'RoleManage',
            component: () => import('@/views/system/role/Index.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: '/system/department',
            name: 'DeptManage',
            component: () => import('@/views/system/department/Index.vue'),
            meta: { title: '部门管理' }
          },
          {
            path: '/system/resource',
            name: 'ResourceManage',
            component: () => import('@/views/system/resource/Index.vue'),
            meta: { title: '权限管理' }
          },
          {
            path: '/system/message',
            name: 'MessageCenter',
            component: () => import('@/views/system/message/Index.vue'),
            meta: { title: '消息中心' }
          },
          {
            path: '/system/dict',
            name: 'DictManage',
            component: () => import('@/views/system/dict/Index.vue'),
            meta: { title: '字典管理' }
          },
          {
            path: '/system/audit',
            name: 'AuditLog',
            component: () => import('@/views/system/audit/Index.vue'),
            meta: { title: '审计日志' }
          },
          {
            path: '/system/attendance',
            name: 'Attendance',
            component: () => import('@/views/system/attendance/Index.vue'),
            meta: { title: '考勤管理' }
          },
          {
            path: '/system/workflow',
            name: 'WorkflowConsole',
            component: () => import('@/views/system/workflow/Index.vue'),
            meta: { title: '审批工作台' }
          }
        ]
      },
      {
        path: '/production',
        name: 'Production',
        redirect: '/production/product',
        meta: { title: '生产管理', icon: 'ToolOutlined' },
        children: [
          { path: '/production/product', name: 'ProductManage', component: () => import('@/views/production/product/Index.vue'), meta: { title: '产品与BOM' } },
          { path: '/production/plan', name: 'ProductionPlan', component: () => import('@/views/production/plan/Index.vue'), meta: { title: '计划与排期' } },
          { path: '/production/order', name: 'WorkOrder', component: () => import('@/views/production/order/Index.vue'), meta: { title: '工单管理' } },
          { path: '/production/purchase', name: 'PurchaseRequest', component: () => import('@/views/production/purchase/Index.vue'), meta: { title: '采购申请' } },
          { path: '/production/warehouse', name: 'ProdWarehouse', component: () => import('@/views/production/warehouse/Index.vue'), meta: { title: '生产仓库' } },
          { path: '/production/quality', name: 'ProdQuality', component: () => import('@/views/production/quality/Index.vue'), meta: { title: '质检管理' } }
        ]
      },
      {
        path: '/logistics',
        name: 'Logistics',
        redirect: '/logistics/warehouse',
        meta: { title: '物流管理', icon: 'CarOutlined' },
        children: [
          { path: '/logistics/warehouse', name: 'LogiWarehouse', component: () => import('@/views/logistics/warehouse/Index.vue'), meta: { title: '物流仓库' } },
          { path: '/logistics/transfer', name: 'Transfer', component: () => import('@/views/logistics/transfer/Index.vue'), meta: { title: '调拨管理' } },
          { path: '/logistics/picking', name: 'Picking', component: () => import('@/views/logistics/picking/Index.vue'), meta: { title: '拣货管理' } },
          { path: '/logistics/shipping', name: 'Shipping', component: () => import('@/views/logistics/shipping/Index.vue'), meta: { title: '发货管理' } },
          { path: '/logistics/quality', name: 'LogiQuality', component: () => import('@/views/logistics/quality/Index.vue'), meta: { title: '物流质检' } }
        ]
      },
      {
        path: '/sales',
        name: 'Sales',
        redirect: '/sales/customer',
        meta: { title: '销售管理', icon: 'ShoppingCartOutlined' },
        children: [
          { path: '/sales/customer', name: 'Customer', component: () => import('@/views/sales/customer/Index.vue'), meta: { title: '客户管理' } },
          { path: '/sales/order', name: 'SalesOrder', component: () => import('@/views/sales/order/Index.vue'), meta: { title: '销售报单' } },
          { path: '/sales/price', name: 'PriceManage', component: () => import('@/views/sales/price/Index.vue'), meta: { title: '价格管理' } },
          { path: '/sales/contract', name: 'Contract', component: () => import('@/views/sales/contract/Index.vue'), meta: { title: '合同管理' } },
          { path: '/sales/aftersale', name: 'AfterSale', component: () => import('@/views/sales/aftersale/Index.vue'), meta: { title: '售后服务' } }
        ]
      },
      {
        path: '/finance',
        name: 'Finance',
        redirect: '/finance/account',
        meta: { title: '财务管理', icon: 'DollarOutlined' },
        children: [
          { path: '/finance/account', name: 'Account', component: () => import('@/views/finance/account/Index.vue'), meta: { title: '账户与科目' } },
          { path: '/finance/voucher', name: 'Voucher', component: () => import('@/views/finance/voucher/Index.vue'), meta: { title: '记账凭证' } },
          { path: '/finance/receivable', name: 'Receivable', component: () => import('@/views/finance/receivable/Index.vue'), meta: { title: '应收账款' } },
          { path: '/finance/payable', name: 'Payable', component: () => import('@/views/finance/payable/Index.vue'), meta: { title: '应付账款' } },
          { path: '/finance/expense', name: 'Expense', component: () => import('@/views/finance/expense/Index.vue'), meta: { title: '费用报销' } },
          { path: '/finance/budget', name: 'Budget', component: () => import('@/views/finance/budget/Index.vue'), meta: { title: '预算管理' } },
          { path: '/finance/salary', name: 'Salary', component: () => import('@/views/finance/salary/Index.vue'), meta: { title: '工资管理' } },
          { path: '/finance/report', name: 'FinancialReport', component: () => import('@/views/finance/report/Index.vue'), meta: { title: '财务报表' } }
        ]
      }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/system/login/Index.vue'), meta: { title: '登录' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

const whiteList = ['/login', '/404', '/403']

router.beforeEach(async (to, _from, next) => {
  if (whiteList.includes(to.path)) {
    return next()
  }
  const token = localStorage.getItem('token')
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
      next({ ...to, replace: true })
    } catch {
      userStore.setToken('')
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  } else {
    next()
  }
})

export default router
