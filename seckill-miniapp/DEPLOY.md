# 微信小程序部署指南

## 前置条件

1. **微信开发者工具**：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html
2. **Node.js**：v16+ 
3. **微信小程序 AppID**：在 https://mp.weixin.qq.com 注册小程序后获取

## 第一步：配置 AppID

编辑 `src/manifest.json`，将 `mp-weixin.appid` 改为你的小程序 AppID：

```json
"mp-weixin": {
  "appid": "wx1234567890abcdef",  // ← 改为你的 AppID
  ...
}
```

同时编辑 `src/project.config.json`，将 `appid` 改为同样的值。

## 第二步：配置服务器域名

登录 [微信公众平台](https://mp.weixin.qq.com) → 开发管理 → 服务器域名：

- **request 合法域名**：`https://你的域名.com`（必须 HTTPS）

开发阶段可在微信开发者工具中勾选「不校验合法域名」。

## 第三步：配置 API 地址

编辑 `src/utils/request.js`，将 `API_HOST` 改为你的服务器地址：

```javascript
// 开发阶段可以用 HTTP
const API_HOST = 'http://192.168.1.100:8080'

// 正式发布必须用 HTTPS
const API_HOST = 'https://api.yourdomain.com'
```

## 第四步：安装依赖并编译

```bash
cd seckill-miniapp
npm install
npm run dev:mp-weixin    # 开发模式（热更新）
# 或
npm run build:mp-weixin  # 生产构建
```

编译产物在 `dist/dev/mp-weixin` 或 `dist/build/mp-weixin` 目录。

## 第五步：微信开发者工具预览

1. 打开微信开发者工具
2. 选择「导入项目」
3. 项目目录选择 `dist/dev/mp-weixin`（或 `dist/build/mp-weixin`）
4. AppID 填写你的小程序 AppID
5. 点击「预览」生成二维码，用手机微信扫码预览

## 第六步：上传发布

1. 在微信开发者工具中点击「上传」
2. 填写版本号（如 1.0.0）和项目备注
3. 登录微信公众平台 → 版本管理 → 开发版本
4. 点击「提交审核」→ 审核通过后「发布」

## 常见问题

### Q: 真机预览时接口报错？
A: 检查以下几点：
- `request.js` 中的 `API_HOST` 是否为真机可访问的 IP/域名
- 微信开发者工具是否勾选了「不校验合法域名」
- 正式版必须配置 HTTPS 域名

### Q: TabBar 图标不显示？
A: 确认 `src/static/tab/` 目录下有 8 个 PNG 文件（81x81px）

### Q: 页面空白？
A: 检查控制台报错，常见原因：
- Pinia store 未正确初始化
- API 接口返回格式不是 `{ code: 200, msg: "...", data: ... }`

### Q: 如何调试？
A: 微信开发者工具自带调试器：
- Console：查看日志和错误
- Network：查看请求
- AppData：查看页面数据
- Wxml：查看元素结构
