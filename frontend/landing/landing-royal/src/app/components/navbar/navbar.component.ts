import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
         [class]="isScrolled
           ? 'bg-rm-bg/95 backdrop-blur-md border-b border-rm-border py-2.5'
           : 'bg-transparent py-4'">
      <div class="max-w-[1200px] mx-auto px-6 flex items-center justify-between">
        <a href="#" class="flex items-center gap-2.5">
          <span class="w-9 h-9 bg-gradient-to-br from-rm-blue to-rm-blue-dark rounded-lg flex items-center justify-center font-display font-extrabold text-white text-base border-2 border-rm-gold">R</span>
          <span class="font-display font-bold text-xl text-white">Royal Meld</span>
        </a>

        <button class="md:hidden flex flex-col gap-1.5 p-1" (click)="menuOpen = !menuOpen">
          <span class="block w-6 h-0.5 bg-rm-text transition-all duration-300"
                [class]="menuOpen ? 'rotate-45 translate-y-[6px]' : ''"></span>
          <span class="block w-6 h-0.5 bg-rm-text transition-all duration-300"
                [class]="menuOpen ? 'opacity-0' : ''"></span>
          <span class="block w-6 h-0.5 bg-rm-text transition-all duration-300"
                [class]="menuOpen ? '-rotate-45 -translate-y-[6px]' : ''"></span>
        </button>

        <ul class="flex items-center gap-8 list-none"
            [class]="menuOpen
              ? 'fixed top-0 right-0 w-7/12 h-screen bg-rm-bg flex-col justify-center gap-6 p-6 border-l border-rm-border md:relative md:flex-row md:h-auto md:w-auto md:bg-transparent md:border-l-0'
              : 'hidden md:flex'">
          <li><a href="#about" class="text-rm-text-muted text-sm font-medium hover:text-white transition-colors" (click)="menuOpen = false">Cómo funciona</a></li>
          <li><a href="#gameplay" class="text-rm-text-muted text-sm font-medium hover:text-white transition-colors" (click)="menuOpen = false">Gameplay</a></li>
          <li><a href="#features" class="text-rm-text-muted text-sm font-medium hover:text-white transition-colors" (click)="menuOpen = false">Características</a></li>
          <li><a href="#how-to-play" class="text-rm-text-muted text-sm font-medium hover:text-white transition-colors" (click)="menuOpen = false">Cómo jugar</a></li>
          <li><a href="#download" class="bg-gradient-to-r from-rm-gold to-rm-gold-dark text-rm-black px-5 py-2 rounded-md font-semibold text-sm hover:shadow-[0_4px_16px_rgba(249,168,37,0.4)] transition-shadow" (click)="menuOpen = false">Descargar</a></li>
        </ul>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  isScrolled = false;
  menuOpen = false;

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled = window.scrollY > 50;
  }
}
