<template>
  <div class="login-page">
    <div class="aurora aurora-left"></div>
    <div class="aurora aurora-right"></div>
    <div class="noise"></div>

    <section class="login-shell">
      <section class="auth-panel">
        <div class="auth-header">
          <div>
            <p class="panel-kicker">{{ isLogin ? 'Welcome back' : 'Create account' }}</p>
            <h2>{{ isLogin ? '登录到 Rlink' : '创建新账号' }}</h2>
            <p class="panel-description">统一管理短链接、访问数据与成员空间。</p>
          </div>
          <div class="segment-control">
            <button
              type="button"
              class="segment-button"
              :class="{ active: isLogin }"
              @click="switchAuthMode(true)"
            >
              登录
            </button>
            <button
              type="button"
              class="segment-button"
              :class="{ active: !isLogin }"
              @click="switchAuthMode(false)"
            >
              注册
            </button>
          </div>
        </div>

        <transition name="panel-fade" mode="out-in">
          <div v-if="isLogin" key="login" class="form-panel">
            <el-form ref="loginFormRef1" :model="loginForm" :rules="loginFormRule" @keyup.enter="login(loginFormRef1)">
              <div class="form-grid compact">
                <el-form-item prop="username">
                  <label class="field-label">用户名</label>
                  <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable size="large" />
                </el-form-item>

                <el-form-item prop="password">
                  <label class="field-label">密码</label>
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    clearable
                    placeholder="请输入密码"
                    show-password
                    size="large"
                  />
                </el-form-item>
              </div>

              <div class="action-row">
                <el-checkbox v-model="checked">记住密码</el-checkbox>
              </div>

              <el-button :loading="loading" type="primary" class="primary-button" @click="login(loginFormRef1)">
                登录
              </el-button>
            </el-form>
          </div>

          <div v-else key="register" class="form-panel">
            <el-form ref="loginFormRef2" :model="addForm" :rules="addFormRule" @keyup.enter="addUser(loginFormRef2)">
              <div class="form-grid">
                <el-form-item prop="username">
                  <label class="field-label">用户名</label>
                  <el-input v-model="addForm.username" placeholder="请输入用户名" clearable size="large" />
                </el-form-item>

                <el-form-item prop="realName">
                  <label class="field-label">姓名</label>
                  <el-input v-model="addForm.realName" placeholder="请输入姓名" clearable size="large" />
                </el-form-item>

                <el-form-item prop="mail">
                  <label class="field-label">邮箱</label>
                  <el-input v-model="addForm.mail" placeholder="请输入邮箱" clearable size="large" />
                </el-form-item>

                <el-form-item prop="phone">
                  <label class="field-label">手机号</label>
                  <el-input v-model="addForm.phone" placeholder="请输入手机号" clearable size="large" />
                </el-form-item>

                <el-form-item prop="password" class="span-2">
                  <label class="field-label">密码</label>
                  <el-input
                    v-model="addForm.password"
                    type="password"
                    clearable
                    placeholder="请输入密码"
                    show-password
                    size="large"
                  />
                </el-form-item>
              </div>

              <p class="form-footnote">创建后将自动登录并进入个人工作台。</p>

              <el-button :loading="loading" type="primary" class="primary-button" @click="addUser(loginFormRef2)">
                注册并进入
              </el-button>
            </el-form>
          </div>
        </transition>

        <p class="bottom-tip">
          {{ isLogin ? '没有账号？' : '已经有账号？' }}
          <button type="button" class="inline-switch" @click="changeLogin">
            {{ isLogin ? '立即注册' : '返回登录' }}
          </button>
        </p>
      </section>
    </section>
  </div>
  <el-dialog v-model="isWC" title="访问验证" width="40%" :before-close="handleClose">
    <div class="verification-flex">
      <span>请输入管理员提供的访问验证码以继续登录。</span>
      <el-form class="form" :model="verification" :rules="verificationRule" ref="verificationRef">
        <el-form-item prop="code" label="验证码">
          <el-input v-model="verification.code" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="isWC = false">取消</el-button>
        <el-button type="primary" @click="verificationLogin(verificationRef)"> 确认 </el-button>
      </span>
    </template>
  </el-dialog>
  <!-- </template> -->
</template>

<script setup>
import { setRole, setToken, setUsername, getUsername } from '@/core/auth.js'
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const { proxy } = getCurrentInstance()
const API = proxy.$API
const loginFormRef1 = ref()
const loginFormRef2 = ref()
const router = useRouter()
const loginForm = reactive({
  username: 'admin',
  password: 'admin123456'
})
const addForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  mail: ''
})

const addFormRule = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3|5|7|8|9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    },
    { min: 11, max: 11, message: '手机号必须是11位', trigger: 'blur' }
  ],
  username: [{ required: true, message: '请输入您的真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 15, message: '密码长度请在八位以上', trigger: 'blur' }
  ],
  mail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      pattern: /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/,
      message: '请输入正确的邮箱号',
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
})
const loginFormRule = reactive({
  username: [{ required: true, message: '请输入您的真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 15, message: '密码长度请在八位以上', trigger: 'blur' }
  ]
})
// 注册
const addUser = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // 检测用户名是否已经存在
      const res1 = await API.user.hasUsername({ username: addForm.username })
      if (res1.data.success !== false) {
        // 注册
        const res2 = await API.user.addUser(addForm)
        // console.log(res2)
        if (res2.data.success === false) {
          ElMessage.warning(res2.data.message)
        } else {
          const res3 = await API.user.login({
            username: addForm.username,
            password: addForm.password
          })
          const token = res3?.data?.data?.token
          // 将username和token保存到cookies中和localStorage中
          if (token) {
            setToken(token)
            setUsername(addForm.username)
            setRole(res3?.data?.data?.role || 'user')
            localStorage.setItem('token', token)
            localStorage.setItem('username', addForm.username)
            localStorage.setItem('role', res3?.data?.data?.role || 'user')
          }
          ElMessage.success('注册登录成功！')
          router.push('/dashboard')
        }
      } else {
        ElMessage.warning('用户名已存在！')
      }
    } else {
      return false
    }
  })
}
// 访问验证码
const isWC = ref(false)
const verificationRef = ref()
const verification = reactive({
  code: ''
})
const verificationRule = reactive({
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})
const verificationLogin = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      const tempPassword = loginForm.password
      loginForm.password = verification.code
      const res1 = await API.user.login(loginForm)
      if (res1.data.code === '0') {
        const token = res1?.data?.data?.token
        // 将username和token保存到cookies中和localStorage中
        if (token) {
          setToken(token)
          setUsername(loginForm.username)
          setRole(res1?.data?.data?.role || 'user')
          localStorage.setItem('token', token)
          localStorage.setItem('username', loginForm.username)
          localStorage.setItem('role', res1?.data?.data?.role || 'user')
        }
        ElMessage.success('登录成功！')
        router.push(res1?.data?.data?.role === 'admin' ? '/admin' : '/dashboard')
      } else if (res1.data.message === '用户已登录') {
        // 如果已经登录了，判断一下浏览器保存的登录信息是不是再次登录的信息，如果是就正常登录
        const cookiesUsername = getUsername()
        if (cookiesUsername === loginForm.username) {
          ElMessage.success('登录成功！')
          router.push('/dashboard')
        } else {
          ElMessage.warning('用户已在别处登录，请勿重复登录！')
        }
      } else {
        ElMessage.error('请输入正确的验证码!')
      }
      loginForm.password = tempPassword
    }
  })
}
// 登录
const login = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // 当启用额外校验时，可在此处切换为验证码登录流程
      // let domain = window.location.host
      // if (domain === 'shortlink.rlink.local' || domain === 'demo.rlink.local') {
      //   isWC.value = true
      //   return
      // }
      const res1 = await API.user.login(loginForm)
      if (res1.data.code === '0') {
        const token = res1?.data?.data?.token
        // 将username和token保存到cookies中和localStorage中
        if (token) {
          setToken(token)
          setUsername(loginForm.username)
          setRole(res1?.data?.data?.role || 'user')
          localStorage.setItem('token', token)
          localStorage.setItem('username', loginForm.username)
          localStorage.setItem('role', res1?.data?.data?.role || 'user')
        }
        ElMessage.success('登录成功！')
        router.push(res1?.data?.data?.role === 'admin' ? '/admin' : '/dashboard')
      } else if (res1.data.message === '用户已登录') {
        // 如果已经登录了，判断一下浏览器保存的登录信息是不是再次登录的信息，如果是就正常登录
        const cookiesUsername = getUsername()
        if (cookiesUsername === loginForm.username) {
          ElMessage.success('登录成功！')
          router.push('/dashboard')
        } else {
          ElMessage.warning('用户已在别处登录，请勿重复登录！')
        }
      } else if (res1.data.message === '用户不存在') {
        ElMessage.error('请输入正确的账号密码!')
      }
    } else {
      return false
    }
  })
}

const loading = ref(false)
// 是否记住密码
const checked = ref(true)
// 展示登录还是展示注册
const isLogin = ref(true)
const switchAuthMode = (nextLoginState) => {
  isLogin.value = nextLoginState
}
const changeLogin = () => {
  isLogin.value = !isLogin.value
}
</script>

<style lang="less" scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(143, 211, 255, 0.48), transparent 34%),
    radial-gradient(circle at 85% 15%, rgba(255, 214, 166, 0.42), transparent 28%),
    linear-gradient(180deg, #f6f7fb 0%, #eef2f8 45%, #e8edf5 100%);
  font-family:
    'SF Pro Display',
    'PingFang SC',
    'Helvetica Neue',
    sans-serif;
  color: #1d1d1f;
}

.aurora,
.noise {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.aurora {
  filter: blur(30px);
  opacity: 0.75;
}

.aurora-left {
  inset: auto auto 5% 8%;
  width: 26rem;
  height: 26rem;
  border-radius: 50%;
  background: rgba(101, 198, 255, 0.35);
}

.aurora-right {
  inset: 8% 6% auto auto;
  width: 22rem;
  height: 22rem;
  border-radius: 50%;
  background: rgba(255, 197, 121, 0.3);
}

.noise {
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.18)),
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120' viewBox='0 0 120 120'%3E%3Cfilter id='n'%3E%3CfeTurbulence baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='120' height='120' filter='url(%23n)' opacity='0.06'/%3E%3C/svg%3E");
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(560px, 100%);
  min-height: auto;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: saturate(180%) blur(28px);
  box-shadow:
    0 32px 80px rgba(80, 96, 135, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  overflow: hidden;
}

.panel-kicker,
.hint-text,
.form-footnote,
.bottom-tip,
.panel-description {
  font-family:
    'SF Pro Text',
    'PingFang SC',
    'Helvetica Neue',
    sans-serif;
}

.panel-kicker {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #6e6e73;
}

.hint-text,
.form-footnote,
.bottom-tip,
.panel-description {
  color: #6e6e73;
}

.auth-panel {
  padding: 44px 40px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
}

.auth-header h2 {
  margin: 0;
  font-size: 34px;
  font-weight: 600;
  letter-spacing: -0.04em;
}

.panel-description {
  margin: 12px 0 0;
  font-size: 15px;
  line-height: 1.7;
}

.segment-control {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px;
  border-radius: 999px;
  background: rgba(120, 120, 128, 0.12);
}

.segment-button {
  border: 0;
  background: transparent;
  border-radius: 999px;
  padding: 9px 16px;
  font-size: 14px;
  color: #6e6e73;
  transition: all 0.25s ease;
}

.segment-button.active {
  background: rgba(255, 255, 255, 0.9);
  color: #1d1d1f;
  box-shadow: 0 8px 20px rgba(60, 75, 110, 0.12);
}

.form-panel {
  padding: 28px 0 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 16px;
}

.form-grid.compact {
  grid-template-columns: 1fr;
}

.span-2 {
  grid-column: span 2;
}

.field-label {
  display: block;
  margin: 0 0 8px;
  font-size: 13px;
  color: #6e6e73;
  letter-spacing: 0.01em;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 8px 0 20px;
}

.primary-button {
  width: 100%;
  height: 52px;
  border-radius: 18px;
  border: none;
  background: linear-gradient(180deg, #0a84ff 0%, #0071e3 100%);
  box-shadow: 0 20px 40px rgba(0, 113, 227, 0.24);
  font-size: 16px;
  font-weight: 600;
}

.primary-button:hover {
  opacity: 0.94;
}

.form-footnote {
  margin: 6px 0 20px;
  font-size: 13px;
}

.bottom-tip {
  margin: 24px 0 0;
  font-size: 14px;
}

.inline-switch {
  border: 0;
  padding: 0;
  background: transparent;
  color: #0071e3;
  font-weight: 600;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
}

:deep(.el-input__wrapper) {
  min-height: 52px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.9),
    0 10px 26px rgba(110, 128, 161, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px rgba(0, 113, 227, 0.22),
    0 10px 28px rgba(0, 113, 227, 0.16);
}

:deep(.el-checkbox__label) {
  color: #515154;
}

.verification-flex {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  .img {
    margin-top: 10px;
    align-self: center;
  }
  .form {
    transform: translateY(15px);
    width: 90%;
  }
}

@media (max-width: 980px) {
  .login-page {
    height: auto;
    min-height: 100vh;
  }

  .auth-panel {
    padding: 32px 28px 40px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    border-radius: 28px;
  }

  .form-grid,
  .auth-header {
    grid-template-columns: 1fr;
  }

  .auth-header {
    display: grid;
  }

  .segment-control {
    width: 100%;
    justify-content: space-between;
  }

  .segment-button {
    flex: 1;
  }

  .span-2 {
    grid-column: span 1;
  }

  .action-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
