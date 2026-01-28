import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { startMockTelemetry } from './services/telemetry.mock'
import App from './App.vue'

// Syncfusion License
import { registerLicense } from '@syncfusion/ej2-base'
registerLicense('Ngo9BigBOggjHTQxAR8/V1JGaF5cXGpCfEx0QHxbf1x2ZFZMY1VbRXVPMyBoS35RcEViW3lec3BVR2VbU0R/VEFf')

// Syncfusion Vue plugin
import { CircularGaugePlugin } from '@syncfusion/ej2-vue-circulargauge'

// Syncfusion CSS
import '@syncfusion/ej2-base/styles/material.css'

// Bootstrap the Vue application
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

// Global CSS
import './assets/css/styles.css'

const app = createApp(App)
app.use(createPinia())
app.use(CircularGaugePlugin)
app.mount('#app')

// ⚠️ DEV ONLY
startMockTelemetry()
