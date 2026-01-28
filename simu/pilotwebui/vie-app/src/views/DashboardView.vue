<script setup>
import { computed } from 'vue'
import { useTelemetryStore } from '../stores/telemetry'
import Camera from '../components/Camera.vue'
import DrivingInfo from '../components/DrivingInfo.vue'
import Speedometer from '../components/Speedometer.vue'

const telemetry = useTelemetryStore()
const speed = computed(() => telemetry.vehicle.speed)
</script>

<template>
  <div class="container-fluid p-3">
    <div class="row">
      <div class="col-12 col-lg-9">
        <Camera />
      </div>

      <div class="col-12 col-lg-3">
        <DrivingInfo />
        <Speedometer :value="speed" />
      </div>
    </div>
  </div>
</template>
