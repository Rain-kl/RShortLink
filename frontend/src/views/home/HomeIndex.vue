<template>
  <div class="dashboard-layout">
    <el-container>
      <el-header height="72px" class="dashboard-header-wrap">
        <div class="dashboard-header">
          <div class="brand-cluster">
            <button class="logo" @click="toMySpace">RLink</button>
            <span class="surface-tag">Dashboard</span>
          </div>
          <div class="header-actions">
            <button class="nav-chip" @click="toMySpace">空间</button>
            <el-dropdown>
              <div class="account-chip">
                <span class="account-label">Account</span>
                <span class="name-span">{{ username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toMine">个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main class="dashboard-main">
        <div class="content-box">
          <RouterView class="content-space" />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { removeKey, removeRole, removeUsername, getToken, getUsername, setRole } from '@/core/auth.js'
import { ElMessage } from 'element-plus'
const { proxy } = getCurrentInstance()
const API = proxy.$API
const router = useRouter()
const toMine = () => {
  router.push('/dashboard/account')
}
// 登出
const logout = async () => {
  const token = getToken()
  const username = getUsername()
  // 请求登出的接口
  await API.user.logout({ token, username })
  // 删除cookies中的token和username
  removeUsername()
  removeKey()
  removeRole()
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  router.push('/login')
  ElMessage.success('成功退出！')
}
// 点击左上方的图片跳转到我的空间
const toMySpace = () => {
  router.push('/dashboard/space')
}
const username = ref('')
onMounted(async () => {
  const actualUsername = getUsername()
  const res = await API.user.queryUserInfo(actualUsername)
  setRole(res?.data?.data?.role || 'user')
  localStorage.setItem('role', res?.data?.data?.role || 'user')
  username.value = truncateText(actualUsername, 8)
})
const truncateText = (text, maxLength) => {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}
</script>

<style lang="scss" scoped>
.dashboard-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.92), rgba(244, 246, 248, 0.82) 34%, rgba(235, 239, 243, 0.95) 100%),
    linear-gradient(180deg, #f6f7f9 0%, #e9edf2 100%);
}

.el-container {
  height: 100vh;
}

.dashboard-header-wrap {
  padding: 16px 22px 0;
}

.dashboard-main {
  padding: 10px 22px 22px;
}

.dashboard-header {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px 0 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
}

.content-box {
  height: calc(100vh - 104px);
  overflow: hidden;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.brand-cluster,
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.el-tooltip__trigger:focus-visible) {
  outline: unset;
}

.logo {
  border: 0;
  background: transparent;
  padding: 0;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.04em;
  color: #111827;
  font-family:
    'SF Pro Display', 'SF Pro Text', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  cursor: pointer;
}

.logo:hover {
  color: #4b5563;
}

.surface-tag,
.nav-chip,
.account-chip {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  background: rgba(248, 250, 252, 0.82);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.surface-tag {
  padding: 8px 14px;
  font-size: 12px;
  color: #6b7280;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.nav-chip {
  border: 0;
  padding: 10px 16px;
  font-size: 13px;
  color: #111827;
  font-family: 'SF Pro Text', 'PingFang SC', sans-serif;
  cursor: pointer;
}

.nav-chip:hover {
  background: #ffffff;
}

.account-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px 8px 14px;
  cursor: pointer;
}

.account-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9ca3af;
}

.name-span {
  max-width: 96px;
  font-size: 13px;
  color: #111827;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

@media (max-width: 768px) {
  .dashboard-header-wrap {
    padding: 12px 12px 0;
  }

  .dashboard-main {
    padding: 10px 12px 12px;
  }

  .dashboard-header {
    padding: 0 12px 0 16px;
  }

  .surface-tag,
  .account-label {
    display: none;
  }

  .content-box {
    height: calc(100vh - 94px);
    border-radius: 22px;
  }
}
</style>
