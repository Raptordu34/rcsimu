<template>
  <div class="card h-100">
    <div class="card-header">
      <h5 class="card-title">Battery</h5>
    </div>
    <div class="card-body d-flex justify-content-center align-items-center">
      <div class="battery-container">
        <div class="battery-level" :style="{ width: batteryLevel + '%', backgroundColor: batteryColor }"></div>
        <span class="battery-percentage">{{ batteryLevel }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Battery',
  props: {
    batteryLevel: {
      type: Number,
      required: true,
      default: 100
    }
  },
  computed: {
    batteryColor() {
      if (this.batteryLevel > 50) {
        return '#4CAF50'; // Green
      } else if (this.batteryLevel > 20) {
        return '#FFC107'; // Orange
      } else {
        return '#F44336'; // Red
      }
    }
  }
}
</script>

<style scoped>
.battery-container {
  width: 150px;
  height: 60px;
  border: 3px solid #333;
  border-radius: 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.battery-container::after {
  content: '';
  position: absolute;
  top: 15px;
  right: -15px;
  width: 12px;
  height: 30px;
  background-color: #333;
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
}

.battery-level {
  height: 100%;
  border-radius: 5px;
  transition: width 0.5s ease-in-out;
}

.battery-percentage {
  position: absolute;
  color: white;
  text-shadow: 1px 1px 2px #333;
  font-weight: bold;
}
</style>
