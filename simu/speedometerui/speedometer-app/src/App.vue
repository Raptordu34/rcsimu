<template>
  <div id="app">
    <div class="speedometer-container">
      <div class="speedometer-wrapper">
        <BaseSpeedometer :value="leftWheel" unit="km/h" :max="110" :slip="slipping" />
      </div>
      <div class="speedometer-wrapper">
        <BaseSpeedometer :value="rightWheel" unit="km/h" :max="110" :slip="slipping" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseSpeedometer from './components/BaseSpeedometer.vue'

const leftWheel = ref(0)
const rightWheel = ref(0)
const slipping = ref(false)

onMounted(() => {
  const ws = new WebSocket('ws://192.168.196.15:8080/speedmessage/speedmessage')

  ws.onopen = () => {
    console.log('WebSocket connection established')
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (typeof data.leftWheel === 'number') {
        leftWheel.value = data.leftWheel
      }
      if (typeof data.rightWheel === 'number') {
        rightWheel.value = data.rightWheel
      }
      if (typeof data.slip === 'boolean') {
        slipping.value = data.slip
      }
    } catch (error) {
      console.error('Error parsing WebSocket message:', error)
    }
  }

  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }

  ws.onclose = () => {
    console.log('WebSocket connection closed')
  }
})
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  margin-top: 60px;
}

.speedometer-container {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.speedometer-wrapper {
  width: 500px;
}
</style>
