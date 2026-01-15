# 资源文件夹 (Assets)

此文件夹用于存放项目所需的静态资源文件。

## 目录结构

```
assets/
├── images/     # 图片资源（PNG、JPG、WebP等）
├── icons/      # 图标资源（SVG、ICO等）
└── README.md   # 本说明文件
```

## 使用说明

- `images/` - 存放背景图、插图、Logo等图片
- `icons/` - 存放SVG图标、favicon等

## 引用方式

在 Vue 组件中引用：
```vue
<img src="@/assets/images/logo.png" alt="Logo" />
```

在 CSS 中引用：
```css
background-image: url('@/assets/images/bg.png');
```
