<template>
  <div class="welcome">
    <!-- Atmospheric background -->
    <div class="bg">
      <div class="bg__orb bg__orb--1" />
      <div class="bg__orb bg__orb--2" />
      <div class="bg__orb bg__orb--3" />
      <div class="bg__orb bg__orb--4" />
      <div class="bg__grain" />
    </div>

    <div class="inner">
      <!-- Top bar -->
      <header class="head" :class="{ _on: ready }">
        <div class="head__left">
          <time class="head__date">{{ dateStr }}</time>
        </div>
        <div class="head__live">
          <span class="head__pulse">
            <span class="head__dot" />
          </span>
          在线
        </div>
      </header>

      <!-- Hero -->
      <section class="hero" :class="{ _on: ready }">
        <div class="hero__left">
          <div class="avatar">
            <div class="avatar__ring" />
            <div class="avatar__ring2" />
            <img
              v-if="avatar"
              :src="avatar + '?imageView2/1/w/240/h/240'"
              :alt="displayName"
              class="avatar__img"
            >
            <div v-else class="avatar__ph">
              <span>{{ initials }}</span>
            </div>
          </div>
        </div>
        <div class="hero__right">
          <p class="hero__hi">{{ greeting }}</p>
          <h1 class="hero__name">{{ displayName }}</h1>
          <p class="hero__at">@{{ userName }}</p>
          <div v-if="roles.length" class="hero__tags">
            <span
              v-for="(r, i) in roles"
              :key="i"
              class="tag"
              :style="tagStyle(i)"
            >{{ r }}</span>
          </div>
        </div>
      </section>

      <!-- Stats row -->
      <div class="nums" :class="{ _on: ready }">
        <div class="num">
          <div class="num__icon num__icon--amber">
            <i class="el-icon-s-check" />
          </div>
          <div class="num__info">
            <b class="num__v">{{ roles.length }}</b>
            <span class="num__l">个角色</span>
          </div>
        </div>
        <div class="num__sep" />
        <div class="num">
          <div class="num__icon num__icon--green">
            <i class="el-icon-office-building" />
          </div>
          <div class="num__info">
            <b class="num__v">{{ departments.length }}</b>
            <span class="num__l">个部门</span>
          </div>
        </div>
        <div class="num__sep" />
        <div class="num">
          <div class="num__icon num__icon--blue">
            <i class="el-icon-office-building" />
          </div>
          <div class="num__info">
            <b class="num__v" :class="{ 'num__v--live': currentDepartment }">
              {{ currentDepartment ? currentDepartment.departmentName : '未选择' }}
            </b>
            <span class="num__l">当前部门</span>
          </div>
        </div>
      </div>

      <!-- Panels -->
      <div class="grid" :class="{ _on: ready }">
        <!-- Roles -->
        <div class="pane">
          <h3 class="pane__hd">
            <span class="pane__ic pane__ic--amber">
              <i class="el-icon-s-check" />
            </span>
            角色权限
          </h3>
          <ul v-if="roles.length" class="pane__ul">
            <li v-for="(r, i) in roles" :key="i" class="pane__li">
              <span class="pane__dot" :style="{ background: PAL[i % PAL.length] }" />
              <span class="pane__txt">{{ r }}</span>
            </li>
          </ul>
          <p v-else class="pane__emp">
            <i class="el-icon-lock" />
            暂无角色
          </p>
        </div>

        <!-- Departments -->
        <div class="pane">
          <h3 class="pane__hd">
            <span class="pane__ic pane__ic--green">
              <i class="el-icon-office-building" />
            </span>
            所属部门
            <span v-if="currentDepartment" class="pane__badge">当前</span>
          </h3>
          <ul v-if="departments.length" class="pane__ul">
            <li
              v-for="(d, i) in departments"
              :key="i"
              class="pane__li pane__li--dept pane__li--clickable"
              :class="{ 'pane__li--active': isCurrentDept(d) }"
              @click="onSwitchDept(d)"
            >
              <span class="pane__dot" :style="{ background: isCurrentDept(d) ? '#30B879' : PAL[i % PAL.length] }" />
              <div class="pane__dept">
                <span class="pane__txt">{{ d.departmentName }}</span>
                <small v-if="d.companyName">{{ d.companyName }}</small>
              </div>
              <span v-if="isCurrentDept(d)" class="pane__check">
                <i class="el-icon-check" />
              </span>
            </li>
          </ul>
          <p v-else class="pane__emp">
            <i class="el-icon-office-building" />
            暂无部门信息
          </p>
        </div>

        <!-- Account -->
        <div class="pane">
          <h3 class="pane__hd">
            <span class="pane__ic pane__ic--blue">
              <i class="el-icon-user" />
            </span>
            账户信息
          </h3>
          <ul class="pane__ul">
            <li class="pane__li">
              <span class="pane__dot" style="background:#D97706" />
              <div class="pane__kv">
                <small>用户名</small>
                <span>{{ userName }}</span>
              </div>
            </li>
            <li class="pane__li">
              <span class="pane__dot" style="background:#059669" />
              <div class="pane__kv">
                <small>显示名</small>
                <span>{{ displayName }}</span>
              </div>
            </li>
            <li class="pane__li">
              <span class="pane__dot" style="background:#0071e3" />
              <div class="pane__kv">
                <small>角色数</small>
                <span>{{ roles.length }} 个</span>
              </div>
            </li>
            <li class="pane__li">
              <span class="pane__dot" style="background:#7C5CFC" />
              <div class="pane__kv">
                <small>部门数</small>
                <span>{{ departments.length }} 个</span>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- Footer -->
      <footer class="foot" :class="{ _on: ready }">
        <span class="foot__line" />
        <span class="foot__txt">欢迎回来，祝你工作愉快</span>
        <span class="foot__line" />
      </footer>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

const PAL = ['#D97706', '#059669', '#0071e3', '#7C5CFC', '#DC2626']

export default {
  name: 'Dashboard',
  data() {
    return {
      ready: false,
      PAL
    }
  },
  computed: {
    ...mapGetters(['name', 'userName', 'avatar', 'roles', 'departments', 'currentDepartment']),
    displayName() {
      return this.name || this.userName || '用户'
    },
    initials() {
      return this.displayName.charAt(0).toUpperCase()
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了，注意休息'
      if (h < 9) return '早上好'
      if (h < 12) return '上午好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      if (h < 22) return '晚上好'
      return '夜深了，注意休息'
    },
    dateStr() {
      const d = new Date()
      const days = ['日', '一', '二', '三', '四', '五', '六']
      return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 星期${days[d.getDay()]}`
    }
  },
  mounted() {
    requestAnimationFrame(() => {
      setTimeout(() => { this.ready = true }, 60)
    })
  },
  methods: {
    tagStyle(i) {
      const c = PAL[i % PAL.length]
      return {
        background: c + '12',
        color: c,
        borderColor: c + '25'
      }
    },
    isCurrentDept(dept) {
      return this.currentDepartment && this.currentDepartment.departmentId === dept.departmentId
    },
    async onSwitchDept(dept) {
      if (this.isCurrentDept(dept) || this._switching) return
      this._switching = true
      try {
        await this.$store.dispatch('user/switchDepartment', dept.departmentId)
        this.$message.success('已切换到 ' + dept.departmentName)
      } catch (e) {
        this.$message.error('切换失败: ' + (e.message || '未知错误'))
      } finally {
        this._switching = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ═══════════════════════════════════════════════════════════
   Dashboard — Refined Glassmorphism + Editorial Aesthetic
   ═══════════════════════════════════════════════════════════ */

@import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&family=Outfit:wght@300;400;500;600;700;800&display=swap');

/* ── Palette ── */
$bg:          #f0f2f5;
$surface:     rgba(255, 255, 255, 0.72);
$surface-h:   rgba(255, 255, 255, 0.85);
$surface-s:   rgba(255, 255, 255, 0.45);
$border:      rgba(0, 0, 0, 0.06);
$border-h:    rgba(0, 0, 0, 0.1);
$primary:     #0A84FF;
$primary-l:   #409CFF;
$primary-bg:  rgba(10, 132, 255, 0.06);
$amber:       #E8960C;
$amber-l:     #F5B731;
$amber-bg:    rgba(232, 150, 12, 0.07);
$amber-bd:    rgba(232, 150, 12, 0.18);
$green:       #30B879;
$green-bg:    rgba(48, 184, 121, 0.07);
$green-bd:    rgba(48, 184, 121, 0.18);
$purple:      #8B5CF6;
$text-1:      #1a1d23;
$text-2:      #5a6072;
$text-3:      #8e95a9;
$radius:      18px;
$radius-sm:   12px;
$ease:        cubic-bezier(0.22, 1, 0.36, 1);
$ease-out:    cubic-bezier(0.16, 1, 0.3, 1);
$font-display: 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;
$font-body:    'DM Sans', -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
$font-mono:    'SF Mono', 'Fira Code', 'JetBrains Mono', Consolas, monospace;

/* ── Base ── */
.welcome {
  position: relative;
  min-height: 100%;
  background: $bg;
  font-family: $font-body;
  color: $text-1;
  overflow: hidden;
}

/* ── Atmospheric Background ── */
.bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  will-change: transform;
}

.bg__orb--1 {
  width: 600px;
  height: 600px;
  top: -12%;
  left: -8%;
  background: radial-gradient(circle, rgba(10, 132, 255, 0.12) 0%, transparent 70%);
  animation: orb-drift 20s ease-in-out infinite;
}

.bg__orb--2 {
  width: 500px;
  height: 500px;
  top: 20%;
  right: -10%;
  background: radial-gradient(circle, rgba(232, 150, 12, 0.10) 0%, transparent 70%);
  animation: orb-drift 25s ease-in-out infinite reverse;
}

.bg__orb--3 {
  width: 400px;
  height: 400px;
  bottom: -5%;
  left: 30%;
  background: radial-gradient(circle, rgba(48, 184, 121, 0.10) 0%, transparent 70%);
  animation: orb-drift 22s ease-in-out infinite 3s;
}

.bg__orb--4 {
  width: 350px;
  height: 350px;
  top: 50%;
  left: -5%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.08) 0%, transparent 70%);
  animation: orb-drift 18s ease-in-out infinite 5s;
}

.bg__grain {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  background-size: 200px 200px;
}

/* ── Inner ── */
.inner {
  position: relative;
  z-index: 1;
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 32px 28px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  box-sizing: border-box;

  @media (max-width: 768px) {
    padding: 16px 16px 24px;
  }
}

/* ── Top Bar ── */
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  opacity: 0;
  transform: translateY(-10px);
  transition: all 0.6s $ease;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.head__date {
  font-size: 13px;
  color: $text-3;
  letter-spacing: 0.8px;
  font-weight: 500;
  text-transform: uppercase;
}

.head__live {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: $green;
  font-weight: 600;
  letter-spacing: 0.4px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 6px 16px;
  border-radius: 100px;
  border: 1px solid $green-bd;
}

.head__pulse {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
}

.head__pulse::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(48, 184, 121, 0.2);
  animation: pulse-bg 2.5s ease-in-out infinite;
}

.head__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $green;
  position: relative;
  z-index: 1;
}

/* ── Hero ── */
.hero {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 14px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s $ease-out 0.1s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }

  @media (max-width: 640px) {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
}

.hero__left {
  flex-shrink: 0;
}

/* ── Avatar ── */
.avatar {
  position: relative;
  width: 92px;
  height: 92px;
}

.avatar__ring {
  position: absolute;
  inset: -5px;
  border-radius: 22px;
  border: 2px solid transparent;
  background: linear-gradient(135deg, $primary 0%, $purple 50%, $amber 100%) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: ring-rotate 6s linear infinite;
  opacity: 0.6;
}

.avatar__ring2 {
  position: absolute;
  inset: -10px;
  border-radius: 26px;
  border: 1px solid rgba(10, 132, 255, 0.08);
}

.avatar__img {
  width: 100%;
  height: 100%;
  border-radius: $radius-sm;
  object-fit: cover;
  display: block;
  position: relative;
  z-index: 1;
}

.avatar__ph {
  width: 100%;
  height: 100%;
  border-radius: $radius-sm;
  background: linear-gradient(145deg, $primary 0%, #4F46E5 60%, $purple 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 32px rgba(10, 132, 255, 0.25);

  span {
    font-family: $font-display;
    font-size: 32px;
    font-weight: 700;
    color: #ffffff;
    letter-spacing: -1px;
  }
}

/* ── Hero Text ── */
.hero__right {
  flex: 1;
  min-width: 0;
}

.hero__hi {
  font-size: 14px;
  color: $text-3;
  margin: 0 0 6px 0;
  letter-spacing: 0.3px;
  font-weight: 500;
}

.hero__name {
  font-family: $font-display;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.15;
  margin: 0 0 4px 0;
  color: $text-1;
  letter-spacing: -0.8px;
  background: linear-gradient(135deg, $text-1 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero__at {
  font-size: 13px;
  color: $text-3;
  margin: 0 0 10px 0;
  font-family: $font-mono;
  letter-spacing: 0.2px;
  font-weight: 400;
}

.hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  @media (max-width: 640px) {
    justify-content: center;
  }
}

.tag {
  padding: 5px 16px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.2px;
  border: 1px solid;
  transition: all 0.3s $ease;
  cursor: default;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

/* ── Stats Row ── */
.nums {
  display: flex;
  align-items: center;
  gap: 0;
  margin-bottom: 10px;
  padding: 10px 24px;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
  opacity: 0;
  transform: translateY(14px);
  transition: all 0.6s $ease 0.3s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.num {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 4px 0;
}

.num__sep {
  width: 1px;
  height: 40px;
  background: $border;
  flex-shrink: 0;
}

.num__icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  flex-shrink: 0;

  &--amber {
    background: $amber-bg;
    color: $amber;
    border: 1px solid $amber-bd;
  }
  &--green {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }
  &--blue {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }
}

.num__info {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.num__v {
  font-family: $font-display;
  font-size: 26px;
  font-weight: 700;
  color: $text-1;
  line-height: 1;
  letter-spacing: -0.5px;

  &--live {
    font-size: 14px;
    font-weight: 700;
    color: $green;
    letter-spacing: 0;
  }
}

.num__l {
  font-size: 13px;
  color: $text-3;
  letter-spacing: 0.3px;
  font-weight: 400;
  white-space: nowrap;
}

/* ── Panels Grid ── */
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1fr;
  gap: 14px;
  margin-bottom: 0;
  flex: 1;
  min-height: 0;

  @media (max-width: 800px) {
    grid-template-columns: 1fr;
  }
}

.pane {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid $border;
  border-radius: $radius;
  padding: 16px 20px;
  transition: all 0.35s $ease;
  opacity: 0;
  transform: translateY(14px);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;

  /* Subtle inner highlight */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.8) 50%, transparent 100%);
  }

  .grid._on & {
    opacity: 1;
    transform: translateY(0);
    transition: all 0.6s $ease-out;
    transition-delay: 0.35s;

    &:nth-child(2) { transition-delay: 0.45s; }
    &:nth-child(3) { transition-delay: 0.55s; }
  }

  &:hover {
    border-color: $border-h;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
    transform: translateY(-3px);
    background: $surface-h;
  }
}

.pane__hd {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 12px 0;
  font-family: $font-display;
  font-size: 15px;
  font-weight: 700;
  color: $text-1;
  letter-spacing: -0.2px;
}

.pane__ic {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;

  &--amber {
    background: $amber-bg;
    color: $amber;
    border: 1px solid $amber-bd;
  }
  &--green {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }
  &--blue {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }
}

.pane__ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pane__li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: $text-2;
  line-height: 1.5;
  padding: 4px 0;
  border-radius: $radius-sm;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.pane__li--dept {
  align-items: flex-start;
}

.pane__li--clickable {
  cursor: pointer;
  transition: all 0.25s $ease;
  border: 1px solid transparent;
  margin: -2px 0;
  padding: 6px 8px;

  &:hover {
    background: rgba(48, 184, 121, 0.04);
    border-color: rgba(48, 184, 121, 0.1);
  }
}

.pane__li--active {
  background: rgba(48, 184, 121, 0.06);
  border-color: rgba(48, 184, 121, 0.15);

  &:hover {
    background: rgba(48, 184, 121, 0.08);
  }

  .pane__txt {
    color: #059669;
    font-weight: 600;
  }
}

.pane__check {
  margin-left: auto;
  color: $green;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.pane__badge {
  margin-left: auto;
  font-size: 10px;
  font-weight: 600;
  color: $green;
  background: $green-bg;
  border: 1px solid $green-bd;
  padding: 2px 8px;
  border-radius: 100px;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

.pane__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 5px;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.02);
  transition: background 0.3s ease;
}

.pane__txt {
  font-weight: 500;
  color: $text-1;
}

.pane__dept {
  display: flex;
  flex-direction: column;
  gap: 2px;

  small {
    font-size: 12px;
    color: $text-3;
    letter-spacing: 0.2px;
  }
}

.pane__kv {
  display: flex;
  flex-direction: column;
  gap: 2px;

  small {
    font-size: 11px;
    color: $text-3;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    font-weight: 500;
  }

  span {
    font-weight: 600;
    color: $text-1;
    letter-spacing: -0.1px;
  }
}

.pane__emp {
  display: flex;
  align-items: center;
  gap: 10px;
  color: $text-3;
  font-size: 13px;
  padding: 10px 0;

  i {
    font-size: 18px;
    opacity: 0.5;
  }
}

/* ── Footer ── */
.foot {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding-top: 20px;
  opacity: 0;
  transition: opacity 0.6s $ease 0.6s;

  &._on {
    opacity: 1;
  }
}

.foot__line {
  width: 48px;
  height: 1px;
  background: linear-gradient(90deg, transparent, $border-h, transparent);
}

.foot__txt {
  font-size: 13px;
  color: $text-3;
  letter-spacing: 1px;
  font-weight: 500;
}

/* ── Keyframes ── */
@keyframes orb-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -20px) scale(1.05); }
  50% { transform: translate(-10px, 20px) scale(0.95); }
  75% { transform: translate(-30px, -10px) scale(1.02); }
}

@keyframes pulse-bg {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.6); opacity: 0; }
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ── Responsive ── */
@media (max-width: 640px) {
  .hero__name {
    font-size: 28px;
  }

  .nums {
    flex-direction: column;
    gap: 0;
    padding: 20px;
  }

  .num {
    width: 100%;
    padding: 12px 0;
  }

  .num__sep {
    width: 100%;
    height: 1px;
  }

  .head__date {
    font-size: 11px;
  }
}
</style>
