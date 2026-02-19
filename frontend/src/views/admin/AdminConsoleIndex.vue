<template>
  <div class="admin-page">
    <aside class="sidebar">
      <div>
        <div class="sidebar-brand">Admin Console</div>
      </div>
      <el-menu :default-active="activeMenu" class="menu" @select="activeMenu = $event">
        <el-menu-item index="overview">总览</el-menu-item>
        <el-menu-item index="users">用户管理</el-menu-item>
        <el-menu-item index="links">链接管理</el-menu-item>
        <el-menu-item index="settings">系统设置</el-menu-item>
      </el-menu>
      <div class="sidebar-actions">
        <el-button @click="goDashboard">用户控制台</el-button>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </div>
    </aside>

    <main class="content">
      <section v-if="activeMenu === 'overview'" class="panel-grid">
        <div class="stat-card">
          <span>总短链接</span>
          <strong>{{ overview.totalLinks || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>总点击次数</span>
          <strong>{{ overview.totalClicks || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>总点击人数</span>
          <strong>{{ overview.totalVisitors || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>用户人数</span>
          <strong>{{ overview.totalUsers || 0 }}</strong>
        </div>
      </section>

      <section v-if="activeMenu === 'users'" class="panel-card">
        <div class="panel-header">
          <div>
            <h2>用户管理</h2>
            <p>内置保留账号：admin、guest</p>
          </div>
          <el-button type="primary" @click="openCreateUser">新增用户</el-button>
        </div>

        <div class="toolbar">
          <el-input v-model="userQuery.keyword" placeholder="按用户名/姓名/邮箱搜索" clearable />
          <el-select v-model="userQuery.role" clearable placeholder="角色">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="访客" value="guest" />
          </el-select>
          <el-select v-model="userQuery.status" clearable placeholder="状态">
            <el-option label="启用" :value="0" />
            <el-option label="封禁" :value="1" />
          </el-select>
          <el-button @click="loadUsers">查询</el-button>
        </div>

        <el-table :data="users" height="520">
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="姓名" min-width="120" />
          <el-table-column prop="mail" label="邮箱" min-width="180" />
          <el-table-column prop="role" label="角色" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                {{ scope.row.status === 0 ? '启用' : '封禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="scope">
              <div class="table-actions">
                <el-button size="small" @click="openEditUser(scope.row)">编辑</el-button>
                <el-button
                  size="small"
                  :type="scope.row.status === 0 ? 'warning' : 'success'"
                  @click="toggleUserStatus(scope.row)"
                >
                  {{ scope.row.status === 0 ? '封禁' : '启用' }}
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :disabled="['admin', 'guest'].includes(scope.row.username)"
                  @click="removeUser(scope.row.username)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="userQuery.current"
          v-model:page-size="userQuery.size"
          layout="total, prev, pager, next"
          :total="userTotal"
          @current-change="loadUsers"
        />
      </section>

      <section v-if="activeMenu === 'links'" class="panel-card">
        <div class="panel-header">
          <div>
            <h2>链接管理</h2>
            <p>支持模糊搜索、按创建者过滤、按点击人数排序</p>
          </div>
        </div>

        <div class="toolbar toolbar-wide">
          <el-input v-model="linkQuery.keyword" placeholder="短链 / 原始链接 / 描述" clearable />
          <el-input v-model="linkQuery.username" placeholder="创建者" clearable />
          <el-select v-model="linkQuery.sortField" placeholder="排序字段">
            <el-option label="创建时间" value="createTime" />
            <el-option label="点击次数" value="totalPv" />
            <el-option label="访问人数" value="totalUv" />
            <el-option label="IP 数" value="totalUip" />
          </el-select>
          <el-select v-model="linkQuery.sortOrder" placeholder="排序方向">
            <el-option label="降序" value="desc" />
            <el-option label="升序" value="asc" />
          </el-select>
          <el-button @click="loadLinks">查询</el-button>
        </div>

        <el-table :data="links" height="520">
          <el-table-column prop="username" label="创建者" width="140" />
          <el-table-column label="短链" min-width="240">
            <template #default="scope">
              <a :href="scope.row.fullShortUrl" target="_blank" rel="noreferrer">{{ scope.row.fullShortUrl }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="originUrl" label="原始链接" min-width="260" show-overflow-tooltip />
          <el-table-column prop="describe" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="totalPv" label="累计点击" width="100" />
          <el-table-column prop="totalUv" label="累计人数" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
        </el-table>

        <el-pagination
          v-model:current-page="linkQuery.current"
          v-model:page-size="linkQuery.size"
          layout="total, prev, pager, next"
          :total="linkTotal"
          @current-change="loadLinks"
        />
      </section>

      <section v-if="activeMenu === 'settings'" class="panel-card settings-card">
        <div class="panel-header">
          <div>
            <h2>系统设置</h2>
            <p>注册、访客开关与黑名单配置</p>
          </div>
        </div>

        <el-form label-width="120px" class="settings-form">
          <el-form-item label="开放注册">
            <el-switch v-model="settings.allowRegister" />
          </el-form-item>
          <el-form-item label="开启访客">
            <el-switch v-model="settings.allowGuest" />
          </el-form-item>
          <el-form-item label="短链黑名单">
            <el-input
              v-model="settings.blackList"
              type="textarea"
              :rows="8"
              placeholder="每行一个关键字或域名，例如 badsite.com"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </section>
    </main>

    <el-dialog v-model="userDialog.visible" :title="userDialog.mode === 'create' ? '新增用户' : '编辑用户'" width="520px">
      <el-form label-width="92px" class="dialog-form">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="userDialog.mode === 'edit'" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.mail" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="访客" value="guest" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status">
            <el-option label="启用" :value="0" />
            <el-option label="封禁" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, getUsername, removeKey, removeRole, removeUsername } from '@/core/auth'

const { proxy } = getCurrentInstance()
const API = proxy.$API
const router = useRouter()

const activeMenu = ref('overview')
const overview = reactive({ totalLinks: 0, totalClicks: 0, totalVisitors: 0, totalUsers: 0, enabledUsers: 0 })

const userQuery = reactive({ current: 1, size: 10, keyword: '', role: '', status: null })
const users = ref([])
const userTotal = ref(0)

const linkQuery = reactive({ current: 1, size: 10, keyword: '', username: '', sortField: 'createTime', sortOrder: 'desc' })
const links = ref([])
const linkTotal = ref(0)

const settings = reactive({ allowRegister: true, allowGuest: true, blackList: '' })

const userDialog = reactive({ visible: false, mode: 'create' })
const userForm = reactive({ username: '', password: '', realName: '', phone: '', mail: '', role: 'user', status: 0 })

const fillUserForm = (row = {}) => {
  userForm.username = row.username || ''
  userForm.password = ''
  userForm.realName = row.realName || ''
  userForm.phone = row.phone || ''
  userForm.mail = row.mail || ''
  userForm.role = row.role || 'user'
  userForm.status = row.status ?? 0
}

const loadOverview = async () => {
  const res = await API.adminConsole.getOverview()
  Object.assign(overview, res?.data?.data || {})
}

const loadUsers = async () => {
  const params = { ...userQuery }
  const res = await API.user.pageUsers(params)
  users.value = res?.data?.data?.records || []
  userTotal.value = Number(res?.data?.data?.total || 0)
}

const loadLinks = async () => {
  const res = await API.adminConsole.getLinksPage({ ...linkQuery })
  links.value = res?.data?.data?.records || []
  linkTotal.value = Number(res?.data?.data?.total || 0)
}

const loadSettings = async () => {
  const res = await API.adminConsole.getSettings()
  Object.assign(settings, res?.data?.data || {})
}

const saveSettings = async () => {
  await API.adminConsole.updateSettings({ ...settings })
  ElMessage.success('系统设置已保存')
}

const openCreateUser = () => {
  userDialog.mode = 'create'
  userDialog.visible = true
  fillUserForm()
}

const openEditUser = (row) => {
  userDialog.mode = 'edit'
  userDialog.visible = true
  fillUserForm(row)
}

const saveUser = async () => {
  if (!userForm.username || !userForm.password && userDialog.mode === 'create') {
    ElMessage.warning('请先填写用户名和密码')
    return
  }
  if (userDialog.mode === 'create') {
    await API.user.adminCreateUser({ ...userForm })
    ElMessage.success('用户创建成功')
  } else {
    await API.user.adminUpdate({ ...userForm })
    ElMessage.success('用户更新成功')
  }
  userDialog.visible = false
  loadUsers()
  loadOverview()
}

const toggleUserStatus = async (row) => {
  await API.user.updateUserStatus({ username: row.username, status: row.status === 0 ? 1 : 0 })
  ElMessage.success('用户状态已更新')
  loadUsers()
}

const removeUser = async (username) => {
  await ElMessageBox.confirm(`确认删除用户 ${username} 吗？`, '删除用户', { type: 'warning' })
  await API.user.deleteUser(username)
  ElMessage.success('用户已删除')
  loadUsers()
  loadOverview()
}

const logout = async () => {
  const token = getToken()
  const username = getUsername()
  await API.user.logout({ token, username })
  removeUsername()
  removeKey()
  removeRole()
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  router.push('/login')
}

const goDashboard = () => router.push('/dashboard')

onMounted(async () => {
  await Promise.all([loadOverview(), loadUsers(), loadLinks(), loadSettings()])
})
</script>

<style scoped lang="scss">
.admin-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background: linear-gradient(180deg, #f8fafc, #eef2f7);
}

.sidebar {
  padding: 28px 20px;
  border-right: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.96);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.sidebar-brand {
  font-size: 1.3rem;
  font-weight: 600;
}

.sidebar-note {
  margin-top: 8px;
  color: rgba(226, 232, 240, 0.72);
}

.menu {
  margin-top: 24px;
  border-right: 0;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: rgba(226, 232, 240, 0.88);
  border-radius: 14px;
  margin-bottom: 8px;
}

.menu :deep(.is-active) {
  background: rgba(59, 130, 246, 0.22);
  color: #fff;
}

.sidebar-actions {
  display: grid;
  gap: 10px;
}

.content {
  padding: 28px;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.stat-card,
.panel-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
}

.stat-card {
  padding: 22px;
}

.stat-card span {
  display: block;
  color: #64748b;
  margin-bottom: 12px;
}

.stat-card strong {
  font-size: 2rem;
  color: #0f172a;
}

.panel-card {
  padding: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.panel-header h2 {
  font-size: 1.35rem;
  color: #0f172a;
}

.panel-header p {
  color: #64748b;
  margin-top: 4px;
}

.toolbar {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr auto;
  gap: 12px;
  margin-bottom: 18px;
}

.toolbar-wide {
  grid-template-columns: 2fr 1fr 1fr 1fr auto;
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.settings-card {
  max-width: 880px;
}

.settings-form {
  margin-top: 10px;
}

.dialog-form :deep(.el-select) {
  width: 100%;
}

@media (max-width: 1100px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .panel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }

  .panel-grid,
  .toolbar,
  .toolbar-wide {
    grid-template-columns: 1fr;
  }
}
</style>