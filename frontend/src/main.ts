// Bootstrap the Vue application with router and Pinia state container.
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './styles/theme.css';
import './styles.css';
// Load global print stylesheet so all views can be exported as PDF via browser print.
import './styles/print.css';

const app = createApp(App);
// Register Pinia first so stores are available to router-loaded views.
app.use(createPinia());
app.use(router);
app.mount('#app');
