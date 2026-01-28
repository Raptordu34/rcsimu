<template>
  <div class="speedometer-container">
    <ejs-circulargauge title="" :titleStyle="titleStyle" :axes="axes" background="">
    </ejs-circulargauge>
  </div>
</template>

<script>
export default {
  name: 'Speedometer',
  props: {
    value: {
      type: Number,
      required: true
    },
    min: {
      type: Number,
      default: 0
    },
    max: {
      type: Number,
      default: 100
    },
    unit: {
      type: String,
      default: 'km/h'
    }
  },
  data() {
    return {
      titleStyle: {
        size: '18px',
        color: 'black'
      },
      axes: [
        {
          lineStyle: { width: 10 },
          radius: '90%',
          minimum: this.min,
          maximum: this.max,
          startAngle: 210,
          endAngle: 150,
          labelStyle: {
            font: {
              size: '12px',
              color: 'black'
            }
          },
          ranges: [
            { start: this.min, end: this.max * 0.6, color: '#30B32D' },
            { start: this.max * 0.6, end: this.max * 0.8, color: '#FFDD00' },
            { start: this.max * 0.8, end: this.max, color: '#F03E3E' }
          ],
          pointers: [
            {
              value: this.value,
              radius: '60%',
              pointerWidth: 8,
              cap: { radius: 7, color: 'black' },
              needleTail: { length: '18%' },
              color: '#F03E3E'
            }
          ],
          annotations: [
            {
              content: `<div style="font-size:20px; color: black;">${Math.round(this.value)}</div>`,
              angle: 180, // Center annotation
              radius: '0%', // Center annotation
              zIndex: '1'
            }
          ]
        }
      ]
    };
  },
  watch: {
    value(newValue) {
      const updatedAxes = JSON.parse(JSON.stringify(this.axes));
      updatedAxes[0].pointers[0].value = newValue;
      updatedAxes[0].annotations[0].content = `<div style="font-size:20px; color: black;">${Math.round(newValue)}</div>`;
      // The angle and radius for annotation remain fixed at center
      this.axes = updatedAxes;
    }
  }
};
</script>

<style scoped>
.speedometer-container {
  width: 300px;
  height: 150px;
}
</style>