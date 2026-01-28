<template>
  <div class="container-fluid vh-100 d-flex flex-column">
    <header class="bg-dark text-light text-center py-3">
      <h1>Copilot Dashboard</h1>
    </header>
    
    <main class="flex-grow-1 p-3">
      <div class="row h-100">
        <div class="col-md-6 h-50 pb-3">
          <camera-feed cameraName="Front Camera"></camera-feed>
        </div>
        <div class="col-md-6 h-50 pb-3">
          <camera-feed cameraName="Rear Camera"></camera-feed>
        </div>
        <div class="col-md-6 h-50">
          <Speedometer :speed="75" />
        </div>
        <div class="col-md-6 h-50">
          <Battery :batteryLevel="80" />
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import CameraFeed from '../components/CameraFeed.vue';
import Speedometer from '../components/Speedometer.vue';
import Battery from '../components/Battery.vue';


export default {
  name: 'DashboardView',
  components: {
    CameraFeed,
    Speedometer,
    Battery,
  }
}
</script>

<style>
/* Ensure body and html take full height */
html, body {
  height: 100%;
  margin: 0;
}
#app {
  height: 100%;
}
</style>