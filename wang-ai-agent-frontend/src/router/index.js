import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import LoveChat from '../views/LoveChat.vue';
import ManusChat from '../views/ManusChat.vue';

const routes = [
  { path: '/', component: Home },
  { path: '/love', component: LoveChat },
  { path: '/manus', component: ManusChat },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router; 