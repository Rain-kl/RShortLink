<template>
  <div class="public-page">
    <div class="ambient ambient-left"></div>
    <div class="ambient ambient-right"></div>

    <header class="topbar">
      <div class="brand">RShortLink</div>
      <div class="top-actions">
        <el-button v-if="!isLoggedIn" text @click="goLogin">登录</el-button>
        <el-button v-if="isLoggedIn" class="ghost-button" @click="goDashboard">进入控制台</el-button>
        <el-button v-if="role === 'admin'" class="ghost-button" @click="goAdmin">管理员</el-button>
      </div>
    </header>

    <main class="hero-card">
      <div class="eyebrow">公开短链创建</div>
      <h1>Rlink短链系统</h1>

      <div class="input-shell">
        <textarea
          v-model="originUrl"
          placeholder="粘贴 http:// 或 https:// 开头的链接"
          rows="3"
        />
        <div class="input-actions">
          <span class="hint">7 天有效</span>
          <el-button class="create-button" type="primary" :loading="submitting" @click="createLink">
            生成短链
          </el-button>
        </div>
      </div>

      <transition name="fade-up">
        <div v-if="createdLink" class="result-card">
          <div>
            <div class="result-label">生成结果</div>
            <a :href="createdLink" target="_blank" rel="noreferrer">{{ createdLink }}</a>
          </div>
          <div class="result-actions">
            <el-button class="ghost-button" @click="copyLink">复制到剪切板</el-button>
          </div>
        </div>
      </transition>
    </main>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRole, getToken } from '@/core/auth'

const { proxy } = getCurrentInstance()
const API = proxy.$API
const router = useRouter()

const originUrl = ref('')
const createdLink = ref('')
const submitting = ref(false)

const isLoggedIn = computed(() => Boolean(getToken()))
const role = computed(() => getRole())

const createLink = async () => {
  if (!originUrl.value) {
    ElMessage.warning('请输入目标链接')
    return
  }
  submitting.value = true
  try {
    const res = await API.publicLink.create({
      originUrl: originUrl.value.trim(),
      describe: '',
      validDateType: 1,
      createdType: 0
    })
    if (res?.data?.success) {
      createdLink.value = res.data.data.fullShortUrl
      ElMessage.success('短链生成成功')
      return
    }
    ElMessage.error(res?.data?.message || '短链生成失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '短链生成失败')
  } finally {
    submitting.value = false
  }
}

const copyLink = async () => {
  if (!createdLink.value) return
  try {
    await navigator.clipboard.writeText(createdLink.value)
    ElMessage.success('已复制到剪切板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const openLink = () => {
  if (createdLink.value) {
    window.open(createdLink.value, '_blank')
  }
}

const goLogin = () => router.push('/login')
const goDashboard = () => router.push('/dashboard')
const goAdmin = () => router.push('/admin')
</script>

<style scoped lang="scss">
.public-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(203, 225, 255, 0.95), transparent 30%),
    radial-gradient(circle at bottom right, rgba(244, 234, 223, 0.75), transparent 32%),
    linear-gradient(180deg, #f6f2ec 0%, #fdfcf9 48%, #f5f7fb 100%);
  color: #1f2937;
  font-family: 'SF Pro Display', 'PingFang SC', 'Helvetica Neue', 'Avenir Next', sans-serif;
}

.ambient {
  position: absolute;
  width: 28rem;
  height: 28rem;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.45;
}

.ambient-left {
  background: #dbeafe;
  top: -8rem;
  left: -10rem;
}

.ambient-right {
  background: #fde7d8;
  right: -8rem;
  bottom: -10rem;
}

.topbar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 32px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.brand {
  font-size: 1rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #111827;
}

.top-actions {
  display: flex;
  gap: 12px;
}

.hero-card {
  position: relative;
  z-index: 1;
  width: min(920px, calc(100% - 32px));
  margin: 8vh auto 0;
  padding: 48px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(24px);
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.65);
}

.eyebrow {
  font-size: 0.82rem;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: #64748b;
  margin-bottom: 18px;
}

h1 {
  font-size: clamp(2.8rem, 6vw, 4.7rem);
  line-height: 0.95;
  font-weight: 600;
  color: #111827;
  max-width: 760px;
}

p {
  margin-top: 22px;
  max-width: 700px;
  color: #475569;
  font-size: 1rem;
}

.input-shell {
  margin-top: 32px;
  padding: 20px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

textarea {
  width: 100%;
  border: 0;
  resize: none;
  background: transparent;
  font-size: 1.35rem;
  color: #0f172a;
  line-height: 1.5;
  font-family: inherit;
}

textarea::placeholder {
  color: #94a3b8;
}

.input-actions {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hint {
  color: #64748b;
  font-size: 0.95rem;
}

.create-button,
.ghost-button {
  min-width: 130px;
  border-radius: 999px;
}

.create-button {
  height: 48px;
  padding: 0 22px;
  background: linear-gradient(135deg, #111827, #1e293b);
  border: 0;
}

.ghost-button {
  height: 44px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  background: rgba(255, 255, 255, 0.7);
}

.result-card {
  margin-top: 22px;
  padding: 22px 24px;
  border-radius: 24px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 0.9));
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.result-label {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: #64748b;
  margin-bottom: 8px;
}

.result-card a {
  font-size: 1.15rem;
  color: #0f172a;
  word-break: break-all;
}

.result-actions {
  display: flex;
  gap: 12px;
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.28s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 768px) {
  .topbar {
    padding: 20px 16px 0;
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .hero-card {
    width: calc(100% - 24px);
    margin-top: 28px;
    padding: 28px 20px;
    border-radius: 24px;
  }

  .input-actions,
  .result-card,
  .result-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>