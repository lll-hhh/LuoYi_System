# Flutter Mobile App Dockerfile
# 用于构建 Flutter Web 版本供部署

FROM debian:bookworm-slim AS build

# 安装依赖
RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    xz-utils \
    zip \
    libglu1-mesa \
    && rm -rf /var/lib/apt/lists/*

# 安装 Flutter
ENV FLUTTER_HOME=/opt/flutter
ENV PATH="$FLUTTER_HOME/bin:$PATH"

RUN git clone https://github.com/flutter/flutter.git -b stable $FLUTTER_HOME \
    && flutter precache \
    && flutter config --enable-web \
    && flutter doctor

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY pubspec.* ./
RUN flutter pub get

COPY . .

# 生成代码
RUN flutter pub run build_runner build --delete-conflicting-outputs

# 构建 Web 版本
RUN flutter build web --release

# 生产镜像
FROM nginx:alpine

# 复制构建产物
COPY --from=build /app/build/web /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
