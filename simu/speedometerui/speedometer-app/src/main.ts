import './assets/main.css'
import '@syncfusion/ej2-base/styles/material.css'

import { createApp } from 'vue'

// Registering Syncfusion license key
import { registerLicense } from '@syncfusion/ej2-base'
registerLicense(
  'Ngo9BigBOggjHTQxAR8/V1JGaF5cXGpCfExyWmFZfVhgd19CYFZSQGYuP1ZhSXxVdkRhUX9ddHRWQmJbUkd9XEA=',
)

import App from './App.vue'

import { CircularGaugePlugin } from '@syncfusion/ej2-vue-circulargauge'

const app = createApp(App)

app.use(CircularGaugePlugin)

app.mount('#app')
