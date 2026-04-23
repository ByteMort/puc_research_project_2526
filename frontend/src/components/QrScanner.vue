<template>
  <div style="display:flex;flex-direction:column;gap:1.25rem;">

    <div class="card">
      <h2>📱 Token scannen</h2>
      <p style="font-size:0.82rem;color:#64748b;margin-bottom:1rem;">
        In productie scant de app de QR-code met de camera. In deze PoC voer je het token handmatig in om de server-validatie te demonstreren.
      </p>

      <div class="form-group">
        <label>Token</label>
        <input
            v-model="token"
            placeholder="Plak hier het UUID-token..."
            @keyup.enter="scan"
        />
      </div>

      <div style="display:flex;gap:0.5rem;flex-wrap:wrap;">
        <button class="btn btn-success" :disabled="!token || loading" @click="scan" style="flex:1;">
          {{ loading ? '⏳ Valideren...' : '✅ Valideer token' }}
        </button>
        <button class="btn btn-danger" :disabled="loading" @click="simulateReplay" style="flex:1;">
          ♻️ Replay-aanval
        </button>
      </div>
    </div>

    <!-- Server response -->
    <div v-if="response" class="card">
      <h2>⚙️ Server response</h2>

      <div class="alert" :class="response.success ? 'success' : 'danger'">
        {{ response.success ? '✅ Scan succesvol gevalideerd' : '🚫 Scan geweigerd: ' + response.error }}
      </div>

      <div class="code-block">
        <div class="code-line">
          <span class="code-key">HTTP status:</span>
          <span class="code-val" :class="response.success ? '' : 'red'">
            {{ response.success ? '200 OK' : '403 Forbidden' }}
          </span>
        </div>
        <template v-if="response.success">
          <div class="code-line">
            <span class="code-key">contactmoment_id:</span>
            <span class="code-val">"{{ response.contactmomentId }}"</span>
          </div>
          <div class="code-line">
            <span class="code-key">label:</span>
            <span class="code-val">"{{ response.contactmomentLabel }}"</span>
          </div>
        </template>
        <template v-else>
          <div class="code-line">
            <span class="code-key">error:</span>
            <span class="code-val red">"{{ response.error }}"</span>
          </div>
        </template>
        <!-- Bewust GEEN PII in response -->
        <div class="code-line"><span class="code-key">naam:</span><span class="code-val dim">// niet teruggegeven ✓</span></div>
        <div class="code-line"><span class="code-key">email:</span><span class="code-val dim">// niet teruggegeven ✓</span></div>
        <div class="code-line"><span class="code-key">user_id:</span><span class="code-val dim">// niet teruggegeven ✓</span></div>
      </div>
    </div>

    <!-- Scan log -->
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.75rem;">
        <h2 style="margin:0;">📋 Scan log</h2>
        <button class="btn btn-primary" style="width:auto;padding:0.3rem 0.75rem;font-size:0.75rem;" @click="log = []">
          🗑 Wis
        </button>
      </div>
      <div class="scan-log">
        <div v-if="log.length === 0" style="color:#64748b;font-style:italic;font-size:0.78rem;">
          Nog geen scans uitgevoerd.
        </div>
        <div v-for="(entry, i) in log" :key="i" class="log-entry" :class="entry.ok ? 'ok' : 'err'">
          <span class="log-time">{{ entry.time }}</span>
          <span>{{ entry.ok ? '✅' : '🚫' }}</span>
          <span>{{ entry.message }}</span>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const token   = ref('')
const loading = ref(false)
const response = ref(null)
const log = ref([])
let lastToken = ''

async function scan() {
  if (!token.value.trim()) return
  lastToken = token.value.trim()
  loading.value = true
  response.value = null

  try {
    const { data } = await axios.post('/api/tokens/scan', { token: lastToken })
    response.value = data
    addLog(
        data.success,
        data.success
            ? `Geregistreerd: ${data.contactmomentLabel}`
            : `Geweigerd: ${data.error}`
    )
    if (data.success) {
      // Notify generator tab via localStorage
      localStorage.setItem('handshake_last_scan', 'used')
      setTimeout(() => localStorage.removeItem('handshake_last_scan'), 100)
    }
  } catch (e) {
    const msg = e.response?.data?.error || e.message
    response.value = { success: false, error: msg }
    addLog(false, 'Fout: ' + msg)
  } finally {
    loading.value = false
  }
}

async function simulateReplay() {
  if (!lastToken) {
    addLog(false, 'Geen vorig token beschikbaar voor replay-aanval')
    return
  }
  token.value = lastToken
  addLog(false, '⚔️ Replay-aanval: zelfde token opnieuw ingediend...')
  await scan()
}

function addLog(ok, message) {
  log.value.unshift({
    ok,
    message,
    time: new Date().toLocaleTimeString('nl-BE')
  })
}
</script>

<style scoped>
.scan-log {
  background: #1e1e2e;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.log-entry {
  display: flex;
  gap: 0.5rem;
  font-family: monospace;
  font-size: 0.76rem;
}

.log-entry.ok  { color: #a6e3a1; }
.log-entry.err { color: #f38ba8; }

.log-time { color: #6c7086; flex-shrink: 0; }
</style>