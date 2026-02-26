import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  { path: '/novels', name: 'NovelList', component: () => import('../views/NovelList.vue'), meta: { title: '我的小说' } },
  { path: '/novels/new', name: 'NovelNew', component: () => import('../views/NovelForm.vue'), meta: { title: '创建小说' } },
  { path: '/novels/:id/edit', name: 'NovelEdit', component: () => import('../views/NovelForm.vue'), meta: { title: '编辑小说' } },
  { path: '/novels/:id', name: 'NovelDetail', component: () => import('../views/NovelDetail.vue'), meta: { title: '小说详情' } },
  { path: '/library', name: 'Library', component: () => import('../views/Library.vue'), meta: { title: '资料库' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - AI 小说生成器` : 'AI 小说生成器'
})

export default router
