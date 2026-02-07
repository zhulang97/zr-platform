import type { App, DirectiveBinding } from 'vue'
import { useAuthStore } from './stores/auth'

export function installPermDirective(app: App) {
  app.directive('perm', {
    mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
      const auth = useAuthStore()
      if (!auth.hasPerm(binding.value)) {
        el.style.display = 'none'
      }
    }
  })
}
