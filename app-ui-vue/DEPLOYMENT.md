# 前端项目部署指南

## 1. 构建生产版本

```bash
cd /Users/abstergo/IdeaProjects/abstergo_solution_p/app-ui-vue
npm run build:prod
```

构建完成后，`dist` 目录将包含以下内容：
- `index.html` - 主页面
- `favicon.ico` - 网站图标
- `static/` - 静态资源目录
  - `css/` - CSS样式文件
  - `js/` - JavaScript文件
  - `fonts/` - 字体文件
  - `img/` - 图片文件

## 2. Nginx配置说明

### 基本配置

```nginx
server {
    listen       80;
    server_name  localhost;  # 替换为实际域名或IP
}
```

### 静态文件服务

```nginx
location / {
    root /usr/share/nginx/html;  # 替换为dist目录的实际路径
    index index.html;
    try_files $uri $uri/ /index.html;  # 支持Vue Router的history模式
}
```

### API反向代理

```nginx
location /api {
    proxy_pass http://127.0.0.1:8082;  # 后端服务器地址
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 公开下载码短链（nginx 层 + 前端默认）

前端 `buildPublicDownloadUrl` 生成 `<当前站点域名>/d/{code}` 短链，下载码面板复制/展示的就是它，可直接对外分享；
nginx 把短链映射到后端长链 `GET /public/oss/download-code/download?code={code}`，不改后端代码：

```nginx
location ^~ /d/ {
    rewrite ^/d/(.*)$ /public/oss/download-code/download?code=$1 break;
    proxy_pass http://127.0.0.1:8082;  # 与 /api 反向代理同一后端地址
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_buffering off;               # 文件直出，不缓冲到磁盘
}
```

使用边界：

- **下载码面板复制/展示的是带域名的短链**（如 `http://localhost:8866/d/ABC123`），长链 `/api/public/oss/download-code/download?code=` 仍保留兼容，两者指向同一个后端接口。
- 前端短链不依赖 `VUE_APP_BASE_API`，staging 等环境前缀不同也不会生成失效链接。
- 短链前缀 `/d/` 变更时，需同步修改 `vue.config.js` 的 devServer 代理、前端 `buildPublicDownloadUrl` 与 nginx 的 `location`/`rewrite`。
- 前缀不要与前端路由撞车：`/d/` 只匹配 `/d/` 开头路径，`/dashboard` 等不受影响。
- 必须用 `^~` 前缀匹配，否则会被上面的静态资源正则 `~* \.(js|css|…)$` 抢走。
- 下载码只含 `[A-Za-z0-9_-]`，放进路径段安全；码不存在/已过期仍由后端返回业务错误。

## 3. 部署步骤

### 方式一：直接部署到Nginx

1. 将 `dist` 目录内容复制到Nginx的html目录：
   ```bash
   sudo cp -r dist/* /usr/share/nginx/html/
   ```

2. 将 `nginx.conf` 复制到Nginx配置目录：
   ```bash
   sudo cp nginx.conf /etc/nginx/conf.d/vue-app.conf
   ```

3. 重启Nginx服务：
   ```bash
   sudo systemctl restart nginx
   ```

### 方式二：Docker部署

1. 创建Dockerfile：
   ```dockerfile
   FROM nginx:alpine
   COPY dist /usr/share/nginx/html
   COPY nginx.conf /etc/nginx/conf.d/default.conf
   EXPOSE 80
   CMD ["nginx", "-g", "daemon off;"]
   ```

2. 构建并运行：
   ```bash
   docker build -t vue-app .
   docker run -p 80:80 vue-app
   ```

## 4. 配置调整

### 修改后端API地址

如果后端服务器地址发生变化，需要修改以下位置：

1. `nginx.conf` 中的 `proxy_pass` 配置
2. `src/settings.js` 中的 `backend` 配置（开发环境）
3. `.env.production` 中的 `VUE_APP_BASE_API`（如果API路径变化）

若启用了公开下载码短链，后端地址变更时 `location ^~ /d/` 里的 `proxy_pass` 需与 `/api/` 反向代理一并修改；
短链前缀变更时需同步修改 nginx 的 `location`/`rewrite`、前端 `buildPublicDownloadUrl` 与 `vue.config.js` 的 devServer 代理。

### 修改域名

```nginx
server_name  your-domain.com;  # 替换为实际域名
```

### 启用HTTPS

```nginx
server {
    listen       443 ssl;
    server_name  your-domain.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    # 其他配置...
}
```

## 5. 常见问题

### 1. 页面刷新后404

确保 `try_files` 配置正确：
```nginx
try_files $uri $uri/ /index.html;
```

### 2. API请求失败

检查 `proxy_pass` 配置和后端服务是否正常运行。

### 3. 静态资源加载失败

检查 `root` 路径是否正确指向 `dist` 目录。

## 6. 性能优化建议

1. 启用gzip压缩（已在配置中）
2. 设置静态资源缓存（已在配置中）
3. 使用CDN加速静态资源
4. 启用HTTP/2
5. 配置SSL证书启用HTTPS