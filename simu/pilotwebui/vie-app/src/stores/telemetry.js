import { defineStore } from 'pinia'

export const useTelemetryStore = defineStore('telemetry', {
  state: () => ({
    vehicle: {
      speed: 0
    }
  }),

  actions: {
    updateSpeed(speed) {
      this.vehicle.speed = speed
    }
  }
})
