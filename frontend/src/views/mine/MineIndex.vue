<template>
  <div class="account-shell">
    <aside class="account-rail">
      <span class="eyebrow">Account</span>
      <h2>账户</h2>
      <p>登录与资料管理</p>
    </aside>
    <section class="account-content">
      <div class="account-hero glass-card">
        <div class="hero-identity">
          <div class="identity-mark">{{ profileInitial }}</div>
          <div>
            <span class="eyebrow">Profile</span>
            <h1>{{ userInfo?.data?.data?.username || '账户' }}</h1>
            <p>用于登录、通知与找回</p>
          </div>
        </div>
        <el-button class="hero-button" type="primary" @click="dialogVisible = true">编辑资料</el-button>
      </div>

      <div class="info-grid">
        <article class="glass-card stat-card">
          <span>邮箱</span>
          <strong>{{ userInfo?.data?.data?.mail || '未设置' }}</strong>
        </article>
        <article class="glass-card stat-card">
          <span>手机</span>
          <strong>{{ userInfo?.data?.data?.phone || '未设置' }}</strong>
        </article>
        <article class="glass-card stat-card">
          <span>姓名</span>
          <strong>{{ userInfo?.data?.data?.realName || '未设置' }}</strong>
        </article>
      </div>

      <div class="glass-card detail-card">
        <div class="detail-head">
          <div>
            <span class="eyebrow">Details</span>
            <h3>个人信息</h3>
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span>用户名</span>
            <strong>{{ userInfo?.data?.data?.username || '--' }}</strong>
          </div>
          <div class="detail-item">
            <span>手机号</span>
            <strong>{{ userInfo?.data?.data?.phone || '--' }}</strong>
          </div>
          <div class="detail-item">
            <span>姓名</span>
            <strong>{{ userInfo?.data?.data?.realName || '--' }}</strong>
          </div>
          <div class="detail-item">
            <span>邮箱</span>
            <strong>{{ userInfo?.data?.data?.mail || '--' }}</strong>
          </div>
        </div>
      </div>
    </section>
  </div>

  <el-dialog v-model="dialogVisible" title="编辑资料" width="560px" :before-close="handleClose">
    <div class="register">
      <el-form ref="loginFormRef" :model="userInfoForm" class="form-container" :rules="formRule">
        <el-form-item prop="username">
          <el-input v-model="userInfoForm.username" placeholder="用户名" maxlength="11" show-word-limit disabled>
            <template #prepend>用户名</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="mail">
          <el-input v-model="userInfoForm.mail" placeholder="邮箱" show-word-limit clearable>
            <template #prepend>邮箱</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="userInfoForm.phone" placeholder="手机号" show-word-limit clearable>
            <template #prepend>手机</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="userInfoForm.realName" placeholder="姓名" show-word-limit clearable>
            <template #prepend>姓名</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="userInfoForm.password" placeholder="如需修改密码请填写" show-word-limit clearable>
            <template #prepend>密码</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <div class="form-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="changeUserInfo(loginFormRef)">保存</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, getCurrentInstance, ref, reactive } from 'vue'
import { getUsername } from '@/core/auth'
import { cloneDeep } from 'lodash'
import { ElMessage } from 'element-plus'
const loginFormRef = ref()
const { proxy } = getCurrentInstance()
// eslint-disable-next-line no-unused-vars
const API = proxy.$API
const userInfo = ref()
const userInfoForm = ref({}) // 修改信息
const getUserInfo = async () => {
  const username = getUsername()
  userInfo.value = await API.user.queryUserInfo(username)
  userInfoForm.value = cloneDeep(userInfo.value.data?.data)
}
getUserInfo()
const dialogVisible = ref(false)
const handleClose = (done) => {
  done()
}
const profileInitial = computed(() => {
  return userInfo.value?.data?.data?.username?.slice(0, 1)?.toUpperCase() || 'U'
})
const formRule = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3|5|7|8|9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    },
    { min: 11, max: 11, message: '手机号必须是11位', trigger: 'blur' }
  ],
  username: [{ required: true, message: '请输入您的用户名', trigger: 'blur' }],
  mail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      pattern: /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/,
      message: '请输入正确的邮箱号',
      trigger: 'blur'
    }
  ],
  password: [
    { required: false, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 15, message: '密码长度请在八位以上', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
})
const changeUserInfo = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      await API.user.editUser(userInfoForm.value).then((res) => {
        if (res?.data?.code !== '0') {
          ElMessage.error(res.data.message)
        } else {
          getUserInfo()
          dialogVisible.value = false
          ElMessage.success('修改成功!')
        }
      })
    } else {
      return false
    }
  })
}
</script>

<style lang="scss" scoped>
.account-shell {
  display: flex;
  min-height: 100%;
  color: #111827;
  font-family: 'SF Pro Text', 'PingFang SC', sans-serif;
}

.account-rail {
  width: 220px;
  padding: 28px 22px;
  border-right: 1px solid rgba(255, 255, 255, 0.74);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.52), rgba(244, 247, 250, 0.7));
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-content {
  flex: 1;
  padding: 24px;
  background: linear-gradient(180deg, rgba(245, 247, 250, 0.86), rgba(236, 240, 244, 0.96));
}

.glass-card {
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
}

.eyebrow {
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #9ca3af;
}

.account-rail h2,
.account-hero h1,
.detail-head h3 {
  margin: 0;
  font-weight: 600;
  letter-spacing: -0.04em;
}

.account-rail h2 {
  font-size: 32px;
}

.account-rail p,
.account-hero p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.account-hero {
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.hero-identity {
  display: flex;
  align-items: center;
  gap: 18px;
}

.identity-mark {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: linear-gradient(135deg, #0f172a, #334155);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
}

.account-hero h1 {
  font-size: 40px;
}

.hero-button {
  min-width: 120px;
  height: 46px;
  border-radius: 999px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.stat-card {
  padding: 20px 22px;
  min-height: 124px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.stat-card span,
.detail-item span {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9ca3af;
}

.stat-card strong,
.detail-item strong {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  word-break: break-word;
}

.detail-card {
  margin-top: 16px;
  padding: 24px;
}

.detail-head {
  margin-bottom: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-item {
  min-height: 120px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.register {
  padding-right: 6px;
}

.form-actions {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-dialog) {
  border-radius: 28px;
}

:deep(.el-dialog__body) {
  padding-top: 14px;
}

:deep(.el-input-group__prepend) {
  min-width: 64px;
  justify-content: center;
}

@media (max-width: 980px) {
  .account-shell {
    flex-direction: column;
  }

  .account-rail {
    width: 100%;
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.74);
  }

  .info-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .account-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .account-hero h1 {
    font-size: 32px;
  }
}
</style>
