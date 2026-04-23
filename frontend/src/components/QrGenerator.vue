<template>
  <div class="grid">

    <!-- Linker kolom: form -->
    <div class="card">
      <h2>⚙️ Configuratie</h2>

      <div class="form-group">
        <label>Contactmoment</label>
        <select v-model="form.contactmomentId">
          <option value="cm_001">Hoorcollege – Webbeveiliging</option>
          <option value="cm_002">Werkgroep – GDPR Praktijk</option>
          <option value="cm_003">Stagegesprek – Mentorgesprek</option>
        </select>
      </div>

      <div class="form-group">
        <label>Geldigheid</label>
        <select v-model="form.ttlSeconds">
          <option :value="60">1 minuut</option>
          <option :value="300">5 minuten</option>
          <option :value="900">15 minuten</option>
        </select>
      </div>

      <button
          class="btn btn-primary"
          :disabled="loading"
          @click="generate"
      >
        {{ loading ? '⏳ Genereren...' : '⚡ Genereer QR-code' }}
      </button>

      <div v-if="error" class="alert danger">{{ error }}</div>

      <!-- Token inhoud (wat de QR-code NIET bevat) -->
      <template v-if="result">
        <div style="margin-top:1.25rem;">
          <p style="font-size:0.78rem;font-weight:600;color:#64748b;text-transform:uppercase;letter-spacing:0.04em;margin-bottom:0.5rem;">
            Inhoud van de QR-code
          </p>
          <div class="code-block">
            <div class="code-line">
              <span class="code-key">"token":</span>
              <span class="code-val">"{{ result.tokenPreview }}"</span>
            </div>
            <div class="code-line">
              <span class="code-key">"naam":</span>
              <span class="code-val dim">// niet aanwezig ✓</span>
            </div>
            <div class="code-line">
              <span class="code-key">"email":</span>
              <span class="code-val dim">// niet aanwezig ✓</span>
            </div>
            <div class="code-line">
              <span class="code-key">"user_id":</span>
              <span class="code-val dim">// niet aanwezig ✓</span>
            </div>
          </div>
          <div class="alert info" style="margin-top:0.5rem;font-size:0.78rem;">
            ℹ️ De QR bevat enkel een anoniem token. Persoonsgegevens zijn uitsluitend server-side beschikbaar via RBAC.
          </div>
        </div>
      </template>
    </div>

    <!-- Rechter kolom: QR + timer -->
    <div class="card" style="display:flex;flex-direction:column;align-items:center;gap:1rem;">
      <h2 style="align-self:flex-start;">📱 QR-code</h2>

      <!-- Placeholder -->
      <div v-if="!result" class="qr-placeholder">
        <span>Genereer een token<br>om de QR te zien</span>
      </div>

      <!-- QR image -->
      <template v-else>
        <div class="qr-wrapper" :class="{ expired: isExpired, used: isUsed }">
          <img
              :src="`data:image/png;base64,${result.qrCodeBase64}`"
              alt="QR Code"
              width="200"
              height="200"
          />
          <div v-if="isExpired" class="qr-overlay">VERLOPEN</div>
          <div v-else-if="isUsed" class="qr-overlay used-overlay">GEBRUIKT</div>
        </div>

        <!-- Countdown timer -->
        <div class="timer" :class="timerClass">
          <div class="timer-bar-wrap">
            <div class="timer-bar" :style="{ width: timerPercent + '%' }"></div>
          </div>
          <div class="timer-label">
            <span v-if="isExpired">⏰ Token verlopen</span>
            <span v-else>⏱ Nog geldig: {{ secondsLeft }}s</span>
            <span class="timer-cm">{{ result.contactmomentLabel }}</span>
          </div>
        </div>

        <div class="alert" :class="isExpired || isUsed ? 'danger' : 'success'" style="width:100%;font-size:0.78rem;">
          <span v-if="isExpired">🚫 QR-code verlopen — genereer een nieuwe.</span>
          <span v-else-if="isUsed">✅ Token gebruikt — kan niet opnieuw gescand worden.</span>
          <span v-else>✅ QR-code actief en geldig.</span>
        </div>

        <!-- Raw token voor scanner-tab (copy helper) -->
        <details style="width:100%;font-size:0.75rem;">
          <summary style="cursor:pointer;color:#64748b;user-select:none;">
            🔍 Raw token (voor scanner-tab)
          </summary>
          <div class="code-block" style="margin-top:0.5rem;">
            <span style="color:#a6e3a1;word-break:break-all;">{{ rawToken }}</span>
          </div>
          <button
              class="btn btn-primary"
              style="margin-top:0.5rem;padding:0.35rem;font-size:0.75rem;"
              @click="copyToken"
          >
            {{ copied ? '✅ Gekopieerd!' : '📋 Kopieer token' }}
          </button>
        </details>
      </template>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import axios from 'axios'

const form = ref({ contactmomentId: 'cm_001', ttlSeconds: 300 })
const result   = ref(null)
const rawToken = ref('')
const loading  = ref(false)
const error    = ref(null)
const secondsLeft = ref(0)
const isExpired   = ref(false)
const isUsed      = ref(false)
const copied      = ref(false)
let timer = null

const timerPercent = computed(() => {
  if (!result.value) return 100
  return Math.max(0, (secondsLeft.value / result.value.ttlSeconds) * 100)
})

const timerClass = computed(() => {
  if (isExpired.value) return 'timer-expired'
  if (timerPercent.value < 25) return 'timer-urgent'
  if (timerPercent.value < 50) return 'timer-warn'
  return 'timer-ok'
})

async function generate() {
  loading.value = true
  error.value   = null
  isExpired.value = false
  isUsed.value    = false
  clearInterval(timer)

  try {
    const { data } = await axios.post('/api/tokens/generate', {
      contactmomentId: form.value.contactmomentId,
      ttlSeconds: form.value.ttlSeconds
    })
    result.value = data

    // The raw token is NOT returned by the API for security.
    // For this PoC demo we store it separately so the scanner tab can test it.
    // In productie zou dit niet bestaan — de scanner gebruikt de camera.
    rawToken.value = data.token   // volledig token uit response

    // Fetch raw token from status endpoint for demo purposes
    const { data: statuses } = await axios.get('/api/tokens/status')
    // We can't get the full token from status (only preview) — this is by design.
    // For demo: we re-generate a full token client-side is not possible.
    // Instead we hint the user to use the scanner's "demo scan" button.

    startCountdown()
  } catch (e) {
    error.value = 'Fout bij genereren: ' + (e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

function startCountdown() {
  const exp = result.value.expiresAt
  function tick() {
    const diff = exp - Math.floor(Date.now() / 1000)
    secondsLeft.value = Math.max(0, diff)
    if (diff <= 0) {
      isExpired.value = true
      clearInterval(timer)
    }
  }
  tick()
  timer = setInterval(tick, 1000)
}

async function copyToken() {
  await navigator.clipboard.writeText(rawToken.value)
  copied.value = true
  setTimeout(() => copied.value = false, 2000)
}

// Allow scanner to mark token as used (cross-component event via localStorage)
window.addEventListener('storage', (e) => {
  if (e.key === 'handshake_last_scan' && e.newValue === 'used') {
    isUsed.value = true
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

@media (max-width: 640px) {
  .grid { grid-template-columns: 1fr; }
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #e2e8f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #94a3b8;
  font-size: 0.82rem;
}

.qr-wrapper {
  position: relative;
  border: 3px solid #16a34a;
  border-radius: 8px;
  padding: 8px;
  background: white;
  transition: border-color 0.3s;
}

.qr-wrapper.expired { border-color: #dc2626; }
.qr-wrapper.used    { border-color: #d97706; }

.qr-overlay {
  position: absolute;
  inset: 0;
  background: rgba(220, 38, 38, 0.75);
  color: white;
  font-weight: 900;
  font-size: 1.4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  letter-spacing: 0.1em;
}

.used-overlay { background: rgba(217, 119, 6, 0.75); }

.timer {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.timer-bar-wrap {
  height: 6px;
  background: #e2e8f0;
  border-radius: 99px;
  overflow: hidden;
}

.timer-bar {
  height: 100%;
  border-radius: 99px;
  transition: width 1s linear, background 0.5s;
}

.timer-ok     .timer-bar { background: #16a34a; }
.timer-warn   .timer-bar { background: #d97706; }
.timer-urgent .timer-bar { background: #dc2626; }
.timer-expired .timer-bar { background: #dc2626; width: 0 !important; }

.timer-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.78rem;
  font-weight: 600;
}

.timer-ok     .timer-label { color: #16a34a; }
.timer-warn   .timer-label { color: #d97706; }
.timer-urgent .timer-label { color: #dc2626; }
.timer-expired .timer-label { color: #dc2626; }
.timer-cm { color: #64748b; font-weight: 400; }
</style>