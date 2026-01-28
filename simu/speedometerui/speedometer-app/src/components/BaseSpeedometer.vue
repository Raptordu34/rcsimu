<template>
  <div class="circular-gauge-wrapper">
    <ejs-circulargauge ref="gauge" :background="background">
      <e-axes>
        <e-axis :minimum="min" :maximum="max" :ranges="ranges">
          <e-pointers>
            <e-pointer :value="value" enableAnimation="true" :ranges="ranges" type="Needle"></e-pointer>
          </e-pointers>
        </e-axis>
      </e-axes>
    </ejs-circulargauge>
    <div class="speed-display" :style="{ color: currentSpeedColor }">
      {{ value }} {{ unit }}
    </div>
    <button v-if="slip" class="slip-button">Glissement!</button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    required: true,
  },
  slip: {
    type: Boolean,
    required: true,
  },
  min: {
    type: Number,
    default: 0,
  },
  max: {
    type: Number,
    default: 180,
  },
  unit: {
    type: String,
    default: 'km/h',
  },
})

const background = 'transparent'

const ranges = computed(() => [
  { start: props.min, end: props.max * 0.3, color: '#30B32D' },
  { start: props.max * 0.3, end: props.max * 0.65, color: '#FFDD00' },
  { start: props.max * 0.65, end: props.max * 0.9, color: '#F03E3E' },
  { start: props.max * 0.9, end: props.max, color: '#000000' },
])

const currentSpeedColor = computed(() => {
  const speed = props.value
  for (const range of ranges.value) {
    if (speed >= range.start && speed <= range.end) {
      return range.color
    }
  }
  return '#000000' // Default color if no range matches
})

const gauge = ref(null)

watch(
  () => props.value,
  (newValue) => {
    if (gauge.value?.ej2Instances) {
      gauge.value.ej2Instances.setPointerValue(0, 0, newValue)
    }
  },
)
</script>

<style scoped>
.circular-gauge-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.speed-display {
  font-size: 26px;
  font-weight: bold;
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
}

.slip-button {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background-color: red;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  font-weight: bold;
  animation: blink 3s linear infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>
