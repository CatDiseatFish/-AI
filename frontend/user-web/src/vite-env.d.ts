/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

interface MessageApi {
  success(content: string): void
  error(content: string): void
  warning(content: string): void
  info(content: string): void
}

declare global {
  interface Window {
    $message?: MessageApi
  }
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

export {}
