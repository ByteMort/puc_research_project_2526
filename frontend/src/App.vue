<template>
  <div class="app">

    <nav class="tabs">
      <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="['tab-btn', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </nav>

    <main>
      <QrGenerator v-if="activeTab === 'generator'" />
      <QrScanner   v-if="activeTab === 'scanner'"   />
    </main>

    <footer>
      <a href="https://github.com/ByteMort/poc_research_project_2526" target="_blank">
        GitHub Repository
      </a>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import QrGenerator from './components/QrGenerator.vue'
import QrScanner   from './components/QrScanner.vue'

const activeTab = ref('generator')
const tabs = [
  { id: 'generator', label: '① QR Genereren' },
  { id: 'scanner',   label: '② QR Scannen'   },
]
</script>

<style>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Segoe UI', system-ui, sans-serif;
  background: #f8fafc;
  color: #1e293b;
  min-height: 100vh;
}

.app {
  max-width: 860px;
  margin: 0 auto;
  padding: 2rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

header {
  text-align: center;
}

.badge {
  display: inline-block;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 0.2rem 0.7rem;
  border-radius: 99px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 0.6rem;
}

header h1 { font-size: 1.9rem; font-weight: 800; }
header p  { color: #64748b; margin-top: 0.3rem; font-size: 0.95rem; }

.tabs {
  display: flex;
  gap: 0.4rem;
  border-bottom: 2px solid #e2e8f0;
}

.tab-btn {
  padding: 0.55rem 1.1rem;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  color: #64748b;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: color 0.15s, border-color 0.15s;
}

.tab-btn.active { color: #4f46e5; border-bottom-color: #4f46e5; }
.tab-btn:hover  { color: #4f46e5; }

footer {
  text-align: center;
  font-size: 0.78rem;
  color: #94a3b8;
}

footer a { color: #4f46e5; text-decoration: none; }
footer a:hover { text-decoration: underline; }

/* Shared component styles */
.card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
}

.card h2 {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.form-group { margin-bottom: 0.9rem; }

label {
  display: block;
  font-size: 0.78rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.3rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

select, input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.15s;
  background: white;
  color: #1e293b;
}

select:focus, input:focus { border-color: #4f46e5; }

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  border: none;
  width: 100%;
  transition: opacity 0.15s, transform 0.1s;
}

.btn:hover   { opacity: 0.88; }
.btn:active  { transform: scale(0.97); }
.btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-primary { background: #4f46e5; color: white; }
.btn-success { background: #16a34a; color: white; }
.btn-danger  { background: #dc2626; color: white; }

.alert {
  border-radius: 8px;
  padding: 0.7rem 1rem;
  font-size: 0.82rem;
  margin-top: 0.75rem;
}

.alert.info    { background: #eef2ff; color: #4f46e5; }
.alert.success { background: #dcfce7; color: #16a34a; }
.alert.danger  { background: #fee2e2; color: #dc2626; }
.alert.warn    { background: #fef3c7; color: #d97706; }

.code-block {
  background: #1e1e2e;
  border-radius: 8px;
  padding: 1rem 1.25rem;
  font-family: monospace;
  font-size: 0.78rem;
  color: #cdd6f4;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-top: 0.75rem;
  word-break: break-all;
}

.code-line { display: flex; gap: 0.5rem; }
.code-key     { color: #89b4fa; flex-shrink: 0; }
.code-val     { color: #a6e3a1; }
.code-val.red { color: #f38ba8; }
.code-val.dim { color: #6c7086; font-style: italic; }
</style>