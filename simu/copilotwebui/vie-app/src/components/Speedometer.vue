<template>
  <div class="card h-100">
    <div class="card-header">
      <h5 class="card-title">Speed</h5>
    </div>
    <div class="card-body d-flex justify-content-center align-items-center">
      <svg :viewBox="viewBox" class="w-100 h-100">
        <path :d="backgroundArc" fill="none" :stroke="color" stroke-width="8" />
        <path :d="speedArc" fill="none" :stroke="color" stroke-width="8" />
        <text :x="cx" :y="cy + 10" text-anchor="middle" font-size="20" :fill="color">{{ speed }}</text>
        <text :x="cx" :y="cy + 25" text-anchor="middle" font-size="10" :fill="color">km/h</text>
      </svg>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Speedometer',
  props: {
    speed: {
      type: Number,
      required: true,
      default: 0
    },
    maxSpeed: {
      type: Number,
      default: 200
    },
    color: {
      type: String,
      default: 'black'
    }
  },
  data() {
    return {
      width: 150,
      height: 150,
    };
  },
  computed: {
    cx() {
      return this.width / 2;
    },
    cy() {
      return this.height / 2;
    },
    radius() {
      return Math.min(this.width, this.height) / 2 - 20;
    },
    viewBox() {
      return `0 0 ${this.width} ${this.height}`;
    },
    backgroundArc() {
      return this.describeArc(this.cx, this.cy, this.radius, -120, 120);
    },
    speedArc() {
      const angle = -120 + (this.speed / this.maxSpeed) * 240;
      return this.describeArc(this.cx, this.cy, this.radius, -120, angle);
    }
  },
  methods: {
    polarToCartesian(centerX, centerY, radius, angleInDegrees) {
      const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;
      return {
        x: centerX + (radius * Math.cos(angleInRadians)),
        y: centerY + (radius * Math.sin(angleInRadians))
      };
    },
    describeArc(x, y, radius, startAngle, endAngle) {
      const start = this.polarToCartesian(x, y, radius, endAngle);
      const end = this.polarToCartesian(x, y, radius, startAngle);
      const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";
      const d = [
        "M", start.x, start.y,
        "A", radius, radius, 0, largeArcFlag, 0, end.x, end.y
      ].join(" ");
      return d;
    }
  }
}
</script>
