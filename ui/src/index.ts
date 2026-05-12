import { definePlugin } from '@halo-dev/ui-shared'
import { IconPlug } from '@halo-dev/components'
import { markRaw } from 'vue'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'Root',
      route: {
        path: "/todos", // 前端路由 path
        name: 'TodoList', // 菜单标识名
        component: () => import(/* webpackChunkName: "HomeView" */ './views/HomeView.vue'),
        meta: {
          title: "Todo List", // 菜单页的浏览器 tab 标题
          searchable: true,
          menu: {
            name: "Todo List", // 菜单显示名称
            group: "工具", // 菜单所在组名
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})
