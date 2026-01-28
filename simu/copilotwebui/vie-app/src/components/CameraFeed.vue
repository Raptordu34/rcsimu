<template>
  <div class="card h-100">
    <div class="card-header">
      <h5 class="card-title">{{ cameraName }}</h5>
    </div>
    <div class="card-body">
      <img :src="feedUrl" class="img-fluid" :alt="cameraName + ' feed'">
    </div>
  </div>
</template>

<script>
export default {
  name: 'CameraFeed',
  props: {
    cameraName: {
      type: String,
      required: true
    },
    feedUrl: {
      type: String,
      default: 'https://via.placeholder.com/640x480.png?text=Camera+Feed'
    }
  }
}
</script>

<style scoped>
.card-body {
  padding: 0;
}
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
