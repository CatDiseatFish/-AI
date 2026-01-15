import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { layout: 'auth', requiresAuth: false },
  },
  {
    path: '/invite/:code',
    name: 'InviteLogin',
    component: () => import('@/views/auth/Login.vue'),
    meta: { layout: 'auth', requiresAuth: false },
    props: true,
  },
  {
    path: '/',
    redirect: '/home',
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/HomePage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/toolbox',
    name: 'Toolbox',
    component: () => import('@/views/toolbox/ToolboxPage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/invite',
    name: 'Invite',
    component: () => import('@/views/invite/InvitePage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/wallet/WalletPage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/recharge',
    name: 'Recharge',
    component: () => import('@/views/recharge/RechargePage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/character-library',
    name: 'CharacterLibrary',
    component: () => import('@/views/character-library/CharacterLibraryPage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/scene-library',
    name: 'SceneLibrary',
    component: () => import('@/views/scene-library/SceneLibraryPage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/prop-library',
    name: 'PropLibrary',
    component: () => import('@/views/prop-library/PropLibraryPage.vue'),
    meta: { layout: 'main', requiresAuth: true },
  },
  {
    path: '/editor/:id',
    name: 'Editor',
    component: () => import('@/views/editor/EditorPage.vue'),
    meta: { layout: 'none', requiresAuth: true },
    props: true,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && userStore.isAuthenticated) {
    next('/home')
  } else {
    next()
  }
})

export default router
