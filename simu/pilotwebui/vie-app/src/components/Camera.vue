<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// === Constantes image ===
const IMAGE_WIDTH = 640
const IMAGE_HEIGHT = 480

// === Refs Vue ===
const canvasRef = ref(null)

let ws = null
let ctx = null
let resizeObserver = null

// Queue d’images
const imageQueue = []
let drawing = false

// FPS
let frameCount = 0;
let fps = 0;
let lastFpsTime = 0;

function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const rect = canvas.getBoundingClientRect()
  canvas.width = rect.width
  canvas.height = rect.height
}

function updateFPS() {
  frameCount++;
  const now = performance.now();
  const delta = now - lastFpsTime;

  if (delta >= 1000) {
    fps = (frameCount * 1000) / delta;
    frameCount = 0;
    lastFpsTime = now;
  }
}

function drawFPS() {
  ctx.fillStyle = "rgba(0, 0, 0, 0.6)";
  ctx.fillRect(10, 10, 90, 30);
  ctx.fillStyle = "#00ff00";
  ctx.font = "16px Arial";
  ctx.fillText(`FPS: ${fps.toFixed(1)}`, 20, 32);
}

function drawNextImage() {
  if (drawing || imageQueue.length === 0) return

  drawing = true
  const blob = imageQueue.shift()
  const img = new Image()

  img.onload = () => {
    ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)

    const x = (canvasRef.value.width - IMAGE_WIDTH) / 2
    const y = (canvasRef.value.height - IMAGE_HEIGHT) / 2

    ctx.drawImage(img, x, y, IMAGE_WIDTH, IMAGE_HEIGHT)

    updateFPS();
    drawFPS();

    URL.revokeObjectURL(img.src)
    drawing = false
    drawNextImage()
  }

  img.onerror = () => {
    console.error('Erreur de chargement image caméra')
    drawing = false
    drawNextImage()
  }

  img.src = URL.createObjectURL(blob)
}

function connectWebSocket() {
  ws = new WebSocket('wss://rcsimu-ia.ensma.fr/webcamdriverstream/receiver')
  ws.binaryType = 'blob'

  ws.onopen = () => {
    console.log('🟢 WebSocket caméra connecté')
  }

  ws.onmessage = (event) => {
    imageQueue.push(event.data)
    drawNextImage()
  }

  ws.onerror = (err) => {
    console.error('WebSocket caméra erreur:', err)
  }

  ws.onclose = () => {
    console.log('🔴 WebSocket caméra fermé')
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')

  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
  
  lastFpsTime = performance.now();
  connectWebSocket()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  if (ws) ws.close()
})
</script>

<template>
  <div class="camera-container">
    <!-- Canvas caméra -->
    <canvas ref="canvasRef" class="camera-canvas"></canvas>
  </div>
</template>

<style scoped>
.camera-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: black;
  overflow: hidden;
}

.camera-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
