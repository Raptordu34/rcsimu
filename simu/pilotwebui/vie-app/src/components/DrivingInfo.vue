<script setup>
import { computed } from 'vue'
import { useTelemetryStore } from '../stores/telemetry'

const telemetry = useTelemetryStore()

const battery = computed(() => telemetry.systems?.battery ?? 0)
</script>

<template>
  <div class="card">
    <div class="card-header">Statut véhicule</div>

    <div class="card-body">
      <div class="d-flex justify-content-between">
        <span>Batterie</span>
        <strong>{{ battery }} %</strong>
        <span>Vitesse</span>
        <strong>{{  telemetry.vehicle.speed  }}km/h</strong>
      </div>
    </div>
  </div>
</template>
