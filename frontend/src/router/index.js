import { createRouter, createWebHistory } from 'vue-router'
import { isNotEmpty } from '@/utils/plugins'
import { getRole, getToken, setRole, setToken, setUsername } from '@/core/auth' // 验权

const publicPaths = ['/', '/login']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'PublicCreateIndex',
      component: () => import('@/views/public/PublicCreateIndex.vue')
    },
    {
      path: '/login',
      name: 'LoginIndex',
      component: () => import('@/views/login/LoginIndex.vue')
    },
    {
      path: '/dashboard',
      name: 'DashboardLayout',
      redirect: '/dashboard/space',
      component: () => import('@/views/home/HomeIndex.vue'),
      children: [
        {
          path: 'space',
          name: 'MySpace',
          component: () => import('@/views/mySpace/MySpaceIndex.vue'),
          meta: { title: '我的空间' }
        },
        {
          path: 'recycleBin',
          name: 'RecycleBin',
          component: () => import('@/views/recycleBin/RecycleBinIndex.vue'),
          meta: { title: '账户设置' }
        },
        {
          path: 'account',
          name: 'Mine',
          component: () => import('@/views/mine/MineIndex.vue'),
          meta: { title: '个人中心' }
        }
      ]
    },
    {
      path: '/admin',
      name: 'AdminConsoleIndex',
      component: () => import('@/views/admin/AdminConsoleIndex.vue')
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  setToken(localStorage.getItem('token'))
  setUsername(localStorage.getItem('username'))
  setRole(localStorage.getItem('role'))
  const token = getToken()
  const role = getRole()

  if (to.path === '/login' && isNotEmpty(token)) {
    next(role === 'admin' ? '/admin' : '/dashboard')
    return
  }

  if (publicPaths.includes(to.path)) {
    next()
    return
  }

  if (!isNotEmpty(token)) {
    next('/login')
    return
  }

  if (to.path.startsWith('/admin') && role !== 'admin') {
    next('/dashboard')
    return
  }

  next()
})

export default router
