<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";

const STORAGE_KEY = "daily-desk-state";

const defaultTasks = [
  { id: 1, title: "梳理今天最重要的一件事", done: false, tag: "计划" },
  { id: 2, title: "完成一次 25 分钟专注", done: false, tag: "专注" },
  { id: 3, title: "收尾前记录一个小进展", done: false, tag: "复盘" },
];

const tasks = ref(defaultTasks);
const newTask = ref("");
const note = ref("把散乱的想法先放在这里，等它们慢慢排队。");
const energy = ref(72);
const mood = ref("steady");
const focusMinutes = ref(25);
const remainingSeconds = ref(25 * 60);
const timerRunning = ref(false);
let timerId = null;

const moods = [
  { id: "steady", label: "稳定", color: "#26765f" },
  { id: "sharp", label: "清醒", color: "#2563a8" },
  { id: "warm", label: "松弛", color: "#c28620" },
];

const completedCount = computed(() => tasks.value.filter((task) => task.done).length);
const completionRate = computed(() => {
  if (!tasks.value.length) return 0;
  return Math.round((completedCount.value / tasks.value.length) * 100);
});

const activeTasks = computed(() => tasks.value.filter((task) => !task.done));
const timerLabel = computed(() => {
  const minutes = String(Math.floor(remainingSeconds.value / 60)).padStart(2, "0");
  const seconds = String(remainingSeconds.value % 60).padStart(2, "0");
  return `${minutes}:${seconds}`;
});

const headline = computed(() => {
  if (completionRate.value === 100) return "今天的清单已经收束。";
  if (timerRunning.value) return "专注中，先把这一段走完。";
  if (activeTasks.value.length <= 1) return "只剩一点尾巴了。";
  return "先抓住一个清晰的下一步。";
});

watch([tasks, note, energy, mood, focusMinutes, remainingSeconds], saveState, { deep: true });

onMounted(() => {
  loadState();
});

onUnmounted(() => {
  stopTimer();
});

function loadState() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return;

  try {
    const saved = JSON.parse(raw);
    tasks.value = Array.isArray(saved.tasks) && saved.tasks.length ? saved.tasks : defaultTasks;
    note.value = typeof saved.note === "string" ? saved.note : note.value;
    energy.value = Number.isFinite(saved.energy) ? saved.energy : energy.value;
    mood.value = saved.mood || mood.value;
    focusMinutes.value = Number.isFinite(saved.focusMinutes) ? saved.focusMinutes : focusMinutes.value;
    remainingSeconds.value = Number.isFinite(saved.remainingSeconds) ? saved.remainingSeconds : focusMinutes.value * 60;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
  }
}

function saveState() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      tasks: tasks.value,
      note: note.value,
      energy: energy.value,
      mood: mood.value,
      focusMinutes: focusMinutes.value,
      remainingSeconds: remainingSeconds.value,
    }),
  );
}

function addTask() {
  const title = newTask.value.trim();
  if (!title) return;

  tasks.value.unshift({
    id: Date.now(),
    title,
    done: false,
    tag: "新任务",
  });
  newTask.value = "";
}

function removeTask(id) {
  tasks.value = tasks.value.filter((task) => task.id !== id);
}

function resetTasks() {
  tasks.value = defaultTasks.map((task) => ({ ...task, done: false }));
}

function toggleTimer() {
  timerRunning.value ? stopTimer() : startTimer();
}

function startTimer() {
  if (timerRunning.value) return;
  timerRunning.value = true;
  timerId = window.setInterval(() => {
    if (remainingSeconds.value <= 1) {
      remainingSeconds.value = 0;
      stopTimer();
      markFocusTaskDone();
      return;
    }
    remainingSeconds.value -= 1;
  }, 1000);
}

function stopTimer() {
  timerRunning.value = false;
  if (timerId) window.clearInterval(timerId);
  timerId = null;
}

function resetTimer() {
  stopTimer();
  remainingSeconds.value = focusMinutes.value * 60;
}

function updateFocusMinutes(value) {
  focusMinutes.value = Number(value);
  resetTimer();
}

function markFocusTaskDone() {
  const focusTask = tasks.value.find((task) => task.tag === "专注");
  if (focusTask) focusTask.done = true;
}
</script>

<template>
  <main class="app-shell">
    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Daily Desk</p>
          <h1>今日工作台</h1>
          <p class="subtitle">{{ headline }}</p>
        </div>
        <div class="progress-card">
          <span>完成度</span>
          <strong>{{ completionRate }}%</strong>
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: completionRate + '%' }"></div>
          </div>
        </div>
      </header>

      <section class="task-panel">
        <div class="panel-heading">
          <div>
            <h2>任务清单</h2>
            <p>{{ activeTasks.length }} 个待处理，{{ completedCount }} 个已完成</p>
          </div>
          <button class="ghost-button" type="button" @click="resetTasks">重置</button>
        </div>

        <form class="task-form" @submit.prevent="addTask">
          <input v-model="newTask" type="text" placeholder="添加一个明确的小任务">
          <button type="submit">添加</button>
        </form>

        <div class="task-list">
          <article v-for="task in tasks" :key="task.id" class="task-item" :class="{ done: task.done }">
            <label>
              <input v-model="task.done" type="checkbox">
              <span>{{ task.title }}</span>
            </label>
            <div class="task-actions">
              <em>{{ task.tag }}</em>
              <button type="button" aria-label="删除任务" @click="removeTask(task.id)">×</button>
            </div>
          </article>
        </div>
      </section>
    </section>

    <aside class="side">
      <section class="timer-card">
        <div class="panel-heading compact">
          <div>
            <h2>专注计时</h2>
            <p>{{ focusMinutes }} 分钟一轮</p>
          </div>
          <strong>{{ timerLabel }}</strong>
        </div>

        <input
          class="range"
          type="range"
          min="5"
          max="60"
          step="5"
          :value="focusMinutes"
          @input="updateFocusMinutes($event.target.value)"
        >

        <div class="timer-actions">
          <button type="button" @click="toggleTimer">{{ timerRunning ? "暂停" : "开始" }}</button>
          <button class="secondary-button" type="button" @click="resetTimer">归零</button>
        </div>
      </section>

      <section class="note-card">
        <div class="panel-heading compact">
          <div>
            <h2>便签</h2>
            <p>自动保存在本机</p>
          </div>
        </div>
        <textarea v-model="note" rows="7"></textarea>
      </section>

      <section class="state-card">
        <div class="panel-heading compact">
          <div>
            <h2>今日状态</h2>
            <p>能量 {{ energy }}%</p>
          </div>
        </div>

        <input v-model="energy" class="range" type="range" min="0" max="100">

        <div class="mood-grid">
          <button
            v-for="item in moods"
            :key="item.id"
            type="button"
            :class="{ active: mood === item.id }"
            :style="{ '--mood-color': item.color }"
            @click="mood = item.id"
          >
            {{ item.label }}
          </button>
        </div>
      </section>
    </aside>
  </main>
</template>
