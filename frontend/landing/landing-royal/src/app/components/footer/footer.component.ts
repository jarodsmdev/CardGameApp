import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="border-t border-rm-border pt-16 pb-8 bg-rm-bg">
      <div class="max-w-[1200px] mx-auto px-6">
        <div class="grid md:grid-cols-[1fr_2fr] gap-16 mb-10">
          <!-- Brand -->
          <div>
            <a href="#" class="flex items-center gap-2.5 mb-4">
              <span class="w-8 h-8 bg-gradient-to-br from-rm-blue to-rm-blue-dark rounded-lg flex items-center justify-center font-display font-extrabold text-[0.9rem] text-white border-2 border-rm-gold">R</span>
              <span class="font-display font-bold text-lg text-white">Royal Meld</span>
            </a>
            <p class="text-rm-text-muted text-sm leading-relaxed max-w-[280px]">
              El juego de cartas multijugador para Android.
              Juega Carioca con amigos, sin trampas.
            </p>
          </div>

          <!-- Links -->
          <div class="grid grid-cols-3 gap-8">
            <div>
              <h4 class="text-sm font-semibold text-white mb-4">Producto</h4>
              <a href="#features" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Características</a>
              <a href="#gameplay" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Gameplay</a>
              <a href="#how-to-play" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Cómo jugar</a>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-white mb-4">Legal</h4>
              <a href="#" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Términos</a>
              <a href="#" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Privacidad</a>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-white mb-4">Contacto</h4>
              <a href="#" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Soporte</a>
              <a href="#" class="block text-rm-text-muted text-sm mb-2.5 hover:text-rm-gold transition-colors">Feedback</a>
            </div>
          </div>
        </div>

        <!-- Bottom -->
        <div class="flex flex-col sm:flex-row justify-between items-center pt-6 border-t border-rm-border text-sm text-rm-text-muted">
          <div class="flex flex-col sm:flex-row gap-1 sm:gap-3">
            <span>© 2026 Royal Meld.</span>
            <span class="text-rm-text-muted">Desarrollado por <a href="https://leonel-briones.netlify.app/" target="_blank" rel="noopener noreferrer" class="text-rm-gold hover:text-rm-gold-light transition-colors">Leonel Briones Palacios</a></span>
          </div>
          <div class="flex gap-4 mt-4 sm:mt-0">
            <a href="https://www.linkedin.com/in/leonel-briones-palacios/" target="_blank" rel="noopener noreferrer" class="text-rm-text-muted hover:text-rm-gold transition-colors" aria-label="LinkedIn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/></svg>
            </a>
            <a href="https://github.com/jarodsmdev" target="_blank" rel="noopener noreferrer" class="text-rm-text-muted hover:text-rm-gold transition-colors" aria-label="GitHub">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
            </a>
            <a href="https://leonel-briones.netlify.app/" target="_blank" rel="noopener noreferrer" class="text-rm-text-muted hover:text-rm-gold transition-colors" aria-label="Portafolio">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
            </a>
          </div>
        </div>
      </div>
    </footer>
  `
})
export class FooterComponent {}
