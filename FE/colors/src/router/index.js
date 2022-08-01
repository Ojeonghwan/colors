import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import MyPageView from "../views/MyPageView.vue";
import enterPageView from "../views/EnterView.vue";
import signUp from "../components/user/customSignUp.vue";
import logIn from "../components/user/customLogIn.vue";
import modifyUser from "../components/user/customUpdateUser.vue";
import deleteUser from "../components/user/customDeleteUser.vue";
import modifyPW from "../components/user/customUpdatePW.vue";
import idPwFind from "../components/user/idPwFind.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },

  {
    path: "/mypage",
    name: "mypage",
    component: MyPageView,
  },
  {
    path: "/signup",
    name: "signUp",
    component: signUp,
  },
  {
    path: "/login",
    name: "logIn",
    component: logIn,
  },
  {
    path: "/modifyuser",
    name: "modifyUser",
    component: modifyUser,
  },
  {
    path: "/enterPage",
    name: "enterPage",
    component: enterPageView,
  },
  {
    path: "/deleteuser",
    name: "deleteUser",
    component: deleteUser,
  },
  {
    path: "/modifypw",
    name: "modifyPW",
    component: modifyPW,
  },
  {
    path: "/findidpw",
    name: "findidpw",
    component: idPwFind,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
