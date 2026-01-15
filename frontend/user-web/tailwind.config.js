/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // 主色调 - 灰白色系
        'primary': '#2c3e50',
        'primary-light': '#34495e',
        'primary-dark': '#1a252f',
        
        // 辅助色
        'secondary': '#7f8c8d',
        'accent': '#3498db',
        'success': '#27ae60',
        'warning': '#f39c12',
        'danger': '#e74c3c',

        // 背景色
        'bg-primary': '#f5f6f7',
        'bg-secondary': '#ffffff',
        'bg-tertiary': '#ecf0f1',

        // 文字色
        'text-primary': '#2c3e50',
        'text-secondary': '#7f8c8d',
        'text-tertiary': '#95a5a6',
        'text-inverse': '#ffffff',
        
        // 边框色
        'border-light': '#e1e8ed',
        'border-default': '#d5dce0',
        'border-dark': '#bdc3c7',
      },
      boxShadow: {
        'card': '0 2px 8px rgba(0, 0, 0, 0.08)',
        'card-hover': '0 4px 16px rgba(0, 0, 0, 0.12)',
        'modal': '0 8px 32px rgba(0, 0, 0, 0.16)',
      },
      fontSize: {
        xs: ['12px', { lineHeight: '1rem' }],
        sm: ['14px', { lineHeight: '1.25rem' }],
        base: ['16px', { lineHeight: '1.4' }],
        lg: ['18px', { lineHeight: '1.4' }],
        xl: ['20px', { lineHeight: '1.4' }],
      },
    },
  },
  plugins: [],
}
