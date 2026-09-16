# 前端 AI 开发约束

技术栈：Vue 3 Composition API + Vite 5 + TypeScript + Ant Design Vue 4 + Vue Router 4 + Pinia + Axios + ECharts + Electron 29+

---

# 一、P0 稳定性与安全（违反 = 线上事故，零妥协）

## 1. XSS 防御
禁止 v-html 渲染任何用户输入、URL 参数、接口返回的富文本字段。必须先用 DOMPurify 预清洗，设置白名单标签（b/i/br/p），再渲染。DOMPurify 封装为工具函数 sanitizeHtml，统一调用。

## 2. Token 过期 401 全局拦截
Axios 响应拦截器统一处理 401 状态码：清除 Pinia user store 中的用户信息和 token，清除 localStorage 中的 token，然后 router.push 跳转到 /login 页面，并在 URL 上携带当前页面路径作为 redirect 参数。禁止在业务代码中单独 try-catch 处理 401，所有 401 走统一的拦截器。

## 3. 权限路由守卫
router.beforeEach 按以下顺序校验：第一步，白名单路由（/login、/404、/403）直接放行；第二步，无 token 时跳转 /login；第三步，有 token 但无用户信息时，先调用获取当前用户接口，再用后端返回的权限菜单动态调用 router.addRoute 挂载路由，最后用 replace: true 重新进入当前目标路由；第四步，有 token 且有用户信息时直接放行。

## 4. 防重复提交
所有表单提交按钮必须同时设置 loading 和 disabled 属性，两个属性绑定同一个 ref（submitting）。提交方法内先判断 submitting 是否为 true，为 true 直接 return。请求发出前将 submitting 置为 true，请求完成后在 finally 块中将 submitting 置为 false，确保无论成功或失败都能恢复按钮状态。

## 5. 敏感数据脱敏
手机号、身份证号、银行卡号在列表展示时自动脱敏处理。手机号保留前三位和后四位、中间四位用星号替代；身份证保留前四位和后四位、中间十位用星号替代。脱敏函数封装在 utils/mask.ts 中。表格列定义使用 customRender 调用脱敏函数。详情页可提供"查看完整"开关，点击后调用接口获取完整数据。

## 6. 请求超时与取消重复请求
Axios 实例创建时设置 timeout 为 15000 毫秒。在请求拦截器中维护一个 pendingMap，以 URL + method + params 序列化为 key，存储 AbortController。每次新请求到达时，检查 map 中是否存在相同 key 的请求，如果存在则调用其 abort() 取消旧请求，然后创建新的 AbortController 存入 map。在响应拦截器成功回调中从 map 删除对应 key，失败回调中判断是否为 Cancel 类型的错误，如果是则返回特殊标记对象。

## 7. 全局错误兜底
App.vue 根组件中使用 Vue 的 onErrorCaptured 生命周期钩子捕获所有未被处理的子组件异常。捕获到异常后，设置一个 ref 控制界面显示友好的错误提示组件而非白屏。错误提示组件包含"页面异常，请刷新重试"的文案和一个重试按钮。onErrorCaptured 回调返回 false，阻止异常继续向上传播。

## 8. Electron 安全约束
主进程 BrowserWindow 创建时必须配置 webPreferences 参数：nodeIntegration 设置为 false（禁止渲染进程直接访问 Node API），contextIsolation 设置为 true（启用上下文隔离），sandbox 设置为 true（沙箱模式），preload 指向正确的 preload.js 路径。CSP 头在生产环境 HTML 的 meta 标签中配置：默认源和脚本源限制为 'self'，样式源允许 'unsafe-inline'，图片源允许 data: 和 blob:，连接源白名单 API 域名。preload.js 中通过 contextBridge.exposeInMainWorld 暴露白名单 API，禁止暴露 Node 核心模块如 fs、child_process、shell，仅允许暴露打印通道、文件保存、剪贴板、shell.openExternal 这四个 API。

## 9. 环境变量隔离
所有环境相关的配置统一放在 .env.* 文件中，变量名必须以 VITE_ 为前缀。生产环境配置文件 .env.production 中定义 API 地址等变量。Vite 构建配置中开启 terser 压缩选项的 drop_console 和 drop_debugger，在生产构建时自动移除所有 console.log 和 debugger 语句。

## 10. 图片与文件上传前端预检
文件上传前必须在 beforeUpload 回调中校验两个条件：文件类型必须在白名单内（JPG/PNG/WebP/PDF），文件大小不能超过上限（单文件 10MB），不符合条件时调用 message.error 提示用户并返回 false 阻止上传。视频和压缩包等大文件走分片上传策略。

## 11. watch 异步副作用竞态处理
watch 回调函数内如果发起异步请求，必须使用 onCleanup 参数来处理竞态。在回调内部声明一个 cancelled 布尔变量，在 onCleanup 中将其设为 true。请求返回后先判断 cancelled 是否为 false，只有当请求未被取消时才更新响应式数据。

## 12. 统一错误码体系
前端定义错误码枚举常量，覆盖 401（token 过期）、403（无权限）、404（资源不存在）等通用错误码，以及 30001（重复提交）、30002（库存不足）等业务错误码。Axios 响应拦截器中统一判断接口返回的 code 字段，不等于 200 时调用 message.error 提示并返回 rejected Promise，等于 200 时直接解包 data 字段返回。


# 二、P0 性能（违反 = 页面卡死或白屏）

## 1. 虚拟滚动
列表数据超过 500 条时，必须使用虚拟滚动方案（vue-virtual-scroller 或 vxe-table 内置虚拟滚动），禁止一次性渲染全部 DOM 节点。使用 antd 表格时必须配置分页 pageSize 和滚动高度 scroll.y，避免全量渲染。

## 2. 路由懒加载
所有页面级组件必须使用动态 import 语法进行路由懒加载，即 component 属性值使用箭头函数返回 import() 表达式，禁止在路由配置顶部静态 import 页面组件。

## 3. 大型第三方组件异步加载
非首屏渲染的富文本编辑器、图表组件、PDF 预览等大型组件，使用 defineAsyncComponent 进行异步加载，并同时配置 loading 和 error 状态的处理。

## 4. computed 优先于 methods
模板中需要计算的表达式必须使用 computed 属性，禁止在模板中直接调用 methods 中的函数。因为 methods 函数在每次组件重渲染时都会重新执行，而 computed 会在依赖不变时返回缓存值。

## 5. 禁止 v-if 与 v-for 同元素使用
v-if 和 v-for 不能写在同一个 DOM 元素上。如果需要同时使用，必须将 v-for 写在 template 标签上，将 v-if 写在 template 内部的子元素上。

## 6. shallowRef 优化大对象
纯展示型的数据列表使用 shallowRef 包装，因为它只追踪 .value 的引用变化，不深度追踪内部属性，性能开销远小于 ref。表单数据因为需要深度响应每个字段的变化，继续使用 ref 包装。

## 7. 第三方 UI 组件按需引入
使用 unplugin-vue-components 插件配合 AntDesignVueResolver 实现自动按需引入，禁止在 main.ts 中全量注册 Ant Design Vue 的所有组件。

## 8. ECharts 按需引入
禁止使用全量引入的方式（import * as echarts）。必须从 echarts/core 引入核心，按需引入需要用到的图表类型（如 BarChart、LineChart）、组件（如 GridComponent、TooltipComponent）和渲染器（CanvasRenderer），然后通过 echarts.use() 注册。

## 9. 图片懒加载与 WebP
列表中的图片使用 v-lazy 指令或原生 loading="lazy" 属性进行懒加载。图片资源优先提供 WebP 格式，使用 picture 标签或 srcset 属性进行响应式适配。

## 10. 打包体积优化
Vite 构建配置的 rollupOptions.output.manualChunks 中将 vue 家族依赖拆分为 vendor-vue、Ant Design Vue 拆分为 vendor-antd、ECharts 拆分为 vendor-echarts，实现按依赖拆分 chunk。同时使用 vite-plugin-compression 插件开启 gzip 压缩。

## 11. 事件监听和定时器清理
onMounted 生命周期中添加的 window 事件监听（如 resize、scroll）和 setInterval/setTimeout 定时器，必须在 onUnmounted 生命周期中通过 removeEventListener 和 clearInterval/clearTimeout 移除，防止内存泄漏。

## 12. 非首屏任务延迟执行
日报统计、日志上报、用户行为埋点等非首屏核心任务，使用 requestIdleCallback 包裹，在浏览器空闲时执行，不影响首屏渲染和用户交互性能。


# 三、P1 代码规范（违反 = 代码腐化，维护灾难）

## 3.1 目录结构
项目 src 目录下按以下方式组织：
- api 目录：存放 Axios 请求函数，按业务模块拆分文件（inventory.ts、purchase.ts、auth.ts），每个文件引入封装好的 http 实例
- assets 目录：存放静态资源和全局样式，styles 子目录下放置 Design Token 变量文件、全局样式重置文件、Ant Design Vue 主题覆盖文件
- components 目录：存放全局公用组件，如指标卡片 StatCard、骨架屏 SkeletonTable、错误兜底 ErrorFallback、打印模板 PrintTemplate
- composables 目录：存放可复用的组合式函数，命名规范为 useXxx.ts，如 useDebounce、usePagination、usePrint、usePermission
- constants 目录：存放枚举和常量，如错误码、订单状态、缓存键名
- hooks 目录：存放全局钩子，与 composables 的区别是 hooks 包含状态和生命周期副作用
- layouts 目录：存放布局组件，如基础布局 BaseLayout、登录页布局 AuthLayout
- router 目录：存放路由配置，routes 子目录下按模块拆分路由配置文件
- stores 目录：存放 Pinia store，按模块拆分文件（user.ts、app.ts、permission.ts），使用 setup 语法定义
- types 目录：存放全局 TypeScript 类型定义，包括接口返回体类型、全局类型声明、业务模型文件
- utils 目录：存放纯函数工具，如脱敏函数、格式化函数、校验函数、安全清洗函数
- views 目录：存放页面级组件，按业务模块分文件夹，每个模块内可以有 components 子目录存放本模块私有的子组件

## 3.2 组件规范
单个组件不得超过 400 行，超出时必须拆分为子组件或抽取 composables。template 模板中禁止写复杂表达式（如多运算符运算），必须抽取为计算属性或格式化方法。defineExpose 仅暴露必要的方法（如 resetForm、focusInput），禁止暴露内部 ref 或数据对象。

## 3.3 TypeScript 严格类型
Props 必须使用 defineProps 的泛型语法定义完整类型，包括每个属性的类型、是否可选。Emits 必须使用 defineEmits 的泛型语法定义完整类型，包括事件名、参数类型、参数名。禁止使用 any 类型，不确定的类型用 unknown 替代，接口返回数据必须定义具体的 interface 或 type。v-model 双向绑定统一使用 defineModel（Vue 3.4 以上版本），或手写 computed 的 get/set 实现。

## 3.4 响应式 API 使用规则
禁止直接修改 props 中的属性值，必须通过 emit 通知父组件来修改，或使用 v-model 双向绑定模式。computed 内部禁止执行任何副作用操作（修改其他 ref 的值、调用 API 接口、操作 DOM 元素），副作用必须移到 watch 或方法中。watch 同时监听三个以上响应式源时，必须合并为数组形式一次性 watch，或多用 watchEffect 自动收集依赖。禁止在 script setup 顶层执行副作用代码（如 localStorage 读写、DOM 操作），必须放在 onMounted 等生命周期钩子中。

## 3.5 Pinia 规范
禁止在一个 store 的 action 中直接修改另一个 store 的 state，必须调用对方 store 暴露的 action 方法来完成修改。Store 使用 setup 语法定义，按 state（ref）、getters（computed）、actions（普通函数）三层结构组织，最后 return 导出。Store 按业务模块拆分文件，不放在一个全局 store 中。

## 3.6 Axios 封装标准
创建一个 http.ts 文件，用 axios.create 创建实例，设置 baseURL（从环境变量读取）和 timeout（15000 毫秒）。请求拦截器中从 localStorage 读取 token 并注入到请求头的 Authorization 字段。响应拦截器中统一判断返回数据的 code 字段：code 为 401 时执行 token 过期逻辑，code 不为 200 时提示错误信息并返回 rejected Promise，code 为 200 时直接解包 data 层返回。业务请求函数按模块拆分到不同文件中（如 api/inventory.ts），每个函数用 http.get 或 http.post 调用对应接口并声明返回类型。

## 3.7 常量管理
所有状态码、业务标识、缓存 key、固定配置值必须抽取到 constants 目录下的常量文件中，禁止在页面组件或业务逻辑中直接写魔法数字和魔法字符串。状态码使用 TypeScript 枚举定义并命名语义化（如 OrderStatus.PENDING_APPROVAL 而非数字 1）。缓存 key 使用 as const 断言确保类型不可变（如 CacheKeys.USER_INFO）。

## 3.8 三态覆盖
每个数据展示组件（列表、详情、图表）必须覆盖 loading（加载中）、error（加载失败，附重重试按钮）、empty（空数据）三种状态。使用 a-spin 组件展示加载态，a-result 组件展示错误态并内嵌重试按钮重新调用接口，a-empty 组件展示空数据提示。

## 3.9 CSS 规范
所有组件样式使用 scoped 属性避免全局污染。需要穿透子组件样式时使用 :deep() 伪类选择器。禁止写不带 scoped 的 style 标签来修改全局样式，全局样式覆盖统一放在 theme.less 文件中。

## 3.10 禁止行为
禁止空 catch 块，每个 catch 至少要做错误提示或日志记录。禁止将注释掉的代码留在代码库中。禁止 console.log 出现在生产代码中（Vite 构建配置已自动移除，但开发期也应避免）。禁止未使用的 import 语句、未使用的变量声明、未调用的方法函数。v-for 循环中有唯一 ID 时禁止用 index 作为 key。


# 四、P1 UI 美化（Design Token 化，组件级样式覆盖）

## 4.1 全局主题配置
App.vue 根组件中使用 Ant Design Vue 的 ConfigProvider 组件包裹 router-view，传入 themeConfig 配置对象。token 配置项中设置 colorPrimary 为低饱和哑光藏青（色值 #1e3a5f），colorSuccess 为柔和浅绿（#5b8c5a），colorWarning 为浅橙（#d4953a），colorError 为砖红（#b44a4a），borderRadius 为 4px，fontSize 为 14px，colorText 为中性灰（#4b5563），colorTextHeading 为深灰标题色（#1f2937）。

## 4.2 色彩体系
定义完整 Design Token Less 变量文件，主色调衍生出 hover 态和 active 态色值，以及超浅背景色。每种语义色（成功/警告/错误）都配套定义一个浅色背景版本。文字色分三级：标题用深灰、正文用中性灰、辅助说明用浅灰。背景色大面积用极浅灰白，卡片和容器用纯白。边框统一用弱浅灰细线。禁止在组件中硬编码色值，统一引用 Token 变量。

## 4.3 间距与阴影 Token
定义五个层级的间距变量：4px、8px、16px、24px、32px，全页面统一使用这四个间距值，禁止自定义任意间距。定义三个层级的阴影变量：微阴影用于卡片和行 hover、中等阴影用于弹窗、大阴影用于抽屉，所有阴影色值为黑色配合低透明度（0.04 到 0.08）。圆角统一为 4px，弹窗类可稍微放大到 8px。

## 4.4 表格样式
表格采用无边框模式，去除默认的表格外边框和列分割线。表头背景色使用页面的极浅灰白背景，文字加粗、色值为标题深灰，底部只保留一条细线。行与行之间用细线分隔。行 hover 时背景变为淡藏青色调（主色 3% 透明度）。奇偶行用极浅灰微差区分（偶数行背景加半透明浅灰）。分页组件精简小巧，页码按钮圆角为 4px。金额列使用等宽字体展示，字重加粗。状态列使用自定义标签样式：已审核用绿色背景和文字，待审核用橙色背景和文字，已驳回用红色背景和文字。

## 4.5 表单样式
输入框、数字输入框、日期选择器、下拉框的边框统一为圆角 4px、边框颜色为弱浅灰。鼠标悬停时边框变为主色，聚焦时边框变为主色并加上主色淡色外发光（box-shadow 2px 扩散）。必填字段的红色星标弱化处理，字号缩小为 12px，颜色改为主色。长表单按业务分组用卡片包裹，每张卡片有简约小标题，标题底部用一条细线分隔，卡片本身有极浅微阴影。

## 4.6 按钮样式
主按钮填充低饱和藏青主色，hover 时略为提亮。次要按钮使用线框样式，边框颜色与输入框一致，hover 时边框和文字同时变为主色。危险按钮不使用实心红色填充，改为透明背景、红色边框和文字，hover 时背景出现红色淡色。所有按钮统一高度 32px、左右内边距 16px、圆角 4px、字号 14px，禁止使用超大按钮。

## 4.7 弹窗样式
弹窗内容区圆角统一为 8px，不采用过大圆角。外层阴影使用大阴影层级、透明度稍高（0.12）。弹窗头部分割线为弱灰细线，内边距适中，标题字号 16px、颜色为标题深灰。弹窗内容区内边距统一。弹窗底部分割线与头部一致。

## 4.8 指标卡片
卡片无厚重边框，依靠留白和细微阴影区分层级。卡片内部采用左侧图标 + 右侧内容的两列布局。左侧图标区域为 48px 正方形、带淡色背景圆角。右侧内容区从上到下依次排列：辅助说明文字（小号浅灰）、核心数据（大号加粗深灰，28px 字号）、趋势箭头（小号彩色）。数据字号与辅助文字形成明显层级落差。

## 4.9 图表主题
ECharts 全局配色数组跟随系统主色调体系，包含藏青、浅绿、浅橙、砖红及其变体共六色。网格配置允许标签溢出、边距统一。tooltip 提示框使用白色背景、细灰边框、阴影，文字色为中性灰。图例文字色为中性灰，图例图标缩小。X 轴只显示轴线（弱灰细线）、隐藏刻度线、隐藏网格线。Y 轴只显示虚线网格（极浅灰）、隐藏轴线、隐藏刻度线。所有图表配置抽取为 useChartsTheme composable，统一复用。

## 4.10 页面布局模板
每个业务页面采用统一的四段式布局。顶部操作栏：左侧面包屑导航、右侧操作按钮区（新建、导出等）。筛选查询区：浅灰背景的折叠面板，包含筛选表单。主体区：白色背景的卡片包裹表格或表单，通过阴影区分层级。底部汇总栏：显示总数、合计金额等汇总信息，文字色为浅灰辅助色。


# 五、P2 单据打印适配（Electron 原生打印 + A4 套打）

## 5.1 打印媒体查询
在全局样式文件中定义 @media print 媒体查询。@page 规则设置纸张为 A4 竖版，页边距 12mm（上下）和 15mm（左右）。隐藏所有非打印元素：侧边栏、面包屑、顶部操作栏、筛选区、分页组件、所有按钮、滚动条，这些元素统一添加 .no-print 类名或直接选择器隐藏。强制打印背景色和图片，使用 print-color-adjust: exact 和 -webkit-print-color-adjust: exact。页面容器去除所有内边距和阴影，背景强制为白色。表格表头设置 display: table-header-group 确保跨页时表头重复。表格的每一行设置 page-break-inside: avoid 防止行内断页。定义 .print-only 类在屏幕显示时隐藏、打印时显示。打印标题区域居中显示，字号 20px、加粗。HTML 和 body 标签去除默认边距。

## 5.2 打印模板
单据套打模板组件使用 HTML 原生的 table 标签而非 antd 组件，因为打印时需要精确控制表格结构。模板包含三部分：打印标题、信息表（单据编号、日期、供应商、仓库、经手人、状态等字段用两列或三列布局）、明细表（物料编码、名称、规格、单位、数量、单价、金额、批次号等列，尾部有合计行）。字体统一使用宋体，颜色纯黑。明细表单元格有 1px 实线黑色边框，表头灰色背景加粗。

## 5.3 Electron 静默打印
preload.ts 中通过 contextBridge.exposeInMainWorld 暴露一个 print 方法，该方法通过 ipcRenderer.invoke 调用主进程的 print:silent 通道。主进程中用 ipcMain.handle 处理 print:silent 事件，创建一个不可见的 BrowserWindow，通过 loadURL 加载 data URI 编码的 HTML 字符串，然后调用 webContents.print 方法，参数设置 silent: true（静默打印）、printBackground: true（打印背景色）。前端封装 usePrint composable，提供 printBill 方法，接收标题和 HTML 内容，组装完整 HTML 文档（包含 @page 规则和内联样式），调用 window.electronAPI.print 触发打印。

## 5.4 页面打印入口
页面中添加打印按钮，带有 .no-print 类名确保打印时隐藏。点击按钮后调用 window.print() 触发浏览器或 Electron 原生打印，打印前可以调用 nextTick 确保 DOM 更新完成。


# 六、P2 交互细节

## 6.1 搜索输入框防抖
封装 useDebounce composable，接收一个回调函数和延迟时间（默认 300 毫秒），返回一个防抖后的函数。防抖函数内部使用 setTimeout 和 clearTimeout 实现，每次调用先清除上一次的定时器，再创建新的定时器。搜索输入框的 input 事件绑定防抖后的处理函数，每次输入 300 毫秒后触发搜索请求，避免每次按键都发送请求。

## 6.2 批量操作二次确认
批量删除或批量审批等操作，点击按钮后不直接执行，而是弹出 Modal.confirm 确认框。确认框标题为操作名称，内容明确提示选中的记录数量，操作不可恢复等警告信息，确认按钮使用危险样式。用户点击确认后再调用批量操作接口，成功后弹出成功提示（含操作条数）并刷新列表。

## 6.3 表单校验自动滚动到错误字段
表单提交时，调用 formRef.value.validate() 进行校验。校验失败时 catch 到 error 对象，从中提取 errorFields 数组，取第一个错误字段的 name 属性，在 nextTick 中调用 formRef.value.scrollToField() 滚动到该字段位置，让用户立即看到错误提示。

## 6.4 列表操作后保持滚动位置
删除或更新列表中某条记录前，先记录当前表格滚动容器的 scrollTop 值。操作完成后等待列表刷新，在 nextTick 中将滚动容器的 scrollTop 恢复为之前记录的值，避免列表刷新后跳回顶部造成用户操作中断。

## 6.5 ResizeObserver 替代 resize 事件
图表容器需要响应窗口尺寸变化时，不使用 window.resize 事件（频率高、性能差），改用 ResizeObserver API 监听容器元素的尺寸变化。在 onMounted 中创建 ResizeObserver 实例，observe 图表容器 DOM 元素，回调中执行图表实例的 resize 方法。在 onUnmounted 中调用 observer.disconnect() 断开监听。


# 附：开发自检清单

每个组件提交前逐一确认以下事项：

- 列表数据超过 500 条是否使用了虚拟滚动或分页？
- 所有页面组件是否使用了动态 import 懒加载？
- 是否有 v-if 和 v-for 写在同一个元素上的情况？
- watch 中如果有异步请求，是否使用了 onCleanup 处理竞态？
- onMounted 中添加的事件监听或定时器是否在 onUnmounted 中移除？
- 表单提交按钮是否有 loading 和 disabled 双重锁保护？
- 代码中是否有 any 类型？是否有 console.log？
- 敏感字段（手机号、身份证）在列表展示中是否做了脱敏处理？
- 每个数据展示区域是否覆盖了 loading、error、empty 三种状态？
- 页面中是否有硬编码的魔法数字或魔法字符串？
- 打印页面的操作按钮、侧边栏、分页组件是否添加了 .no-print 类？
- 单据打印模板是否正确设置了页边距和页眉页脚隐藏？