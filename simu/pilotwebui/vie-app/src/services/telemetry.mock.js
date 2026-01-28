import { useTelemetryStore } from '../stores/telemetry'

let intervalId = null

export function startMockTelemetry() {
  if (intervalId) {
    // To prevent multiple intervals from being created by HMR
    clearInterval(intervalId)
  }

  const telemetry = useTelemetryStore()
  let speed = 0
  let direction = 1 // 1 for increasing, -1 for decreasing

  intervalId = setInterval(() => {
    speed += direction * 0.5 // Adjust step for speed of change

    if (speed > 100) {
      speed = 100
      direction = -1
    } else if (speed < 0) {
      speed = 0
      direction = 1
    }

    telemetry.updateSpeed(speed)
  }, 200) // Update every 200ms
}
