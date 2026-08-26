import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-how-to-play',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="how-to-play" class="py-24 px-6 bg-rm-bg">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-4 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">¿Cómo se juega Carioca?</h2>
        <p class="text-rm-text-muted text-lg text-center max-w-[600px] mx-auto mb-16 animate-on-scroll">
          9 rondas. Cada una más desafiante. ¿Listo para la Escala Real?
        </p>

        <div class="max-w-[700px] mx-auto">
          <div *ngFor="let step of steps; let i = index; let last = last"
               class="relative animate-on-scroll"
               [style.transition-delay]="(i * 0.15) + 's'">
            <div class="grid gap-5 items-center py-6"
                 [style.grid-template-columns]="'60px 1fr 60px'">
              <!-- Number -->
              <div class="w-14 h-14 bg-gradient-to-br from-rm-gold to-rm-gold-dark rounded-full flex items-center justify-center font-display font-extrabold text-[1.4rem] text-rm-black shadow-[0_4px_20px_rgba(249,168,37,0.3)] relative z-10">
                {{ i + 1 }}
              </div>
              <!-- Content -->
              <div>
                <h3 class="font-display font-bold text-white text-xl mb-1.5">{{ step.title }}</h3>
                <p class="text-rm-text-muted text-[0.95rem] leading-relaxed">{{ step.description }}</p>
              </div>
              <!-- Icon -->
              <div class="text-3xl text-center">{{ step.icon }}</div>
            </div>
            <!-- Connector line -->
            <div *ngIf="!last"
                 class="absolute left-[28px] top-[80px] w-0.5 bg-gradient-to-b from-rm-gold to-rm-border"
                 [style.height]="'calc(100% - 56px)'"></div>
          </div>
        </div>

        <div class="text-center mt-12 animate-on-scroll">
          <a href="#" class="text-rm-gold font-semibold hover:text-rm-gold-light transition-colors">Ver reglas completas →</a>
        </div>
      </div>
    </section>
  `
})
export class HowToPlayComponent implements AfterViewInit {
  steps = [
    { title: 'Roba una carta', description: 'Al inicio de tu turno, toma una carta del mazo o del pozo.', icon: '📥' },
    { title: 'Forma combinaciones', description: 'Arma escaleras (misma palo, seguidas) o grupos (mismo rango, distintos palos) en tu mano.', icon: '🃏' },
    { title: 'Bájate a la mesa', description: 'Cuando tengas las combinaciones requeridas, juega todas en la mesa.', icon: '📤' },
    { title: 'Descarta y repite', description: 'Termina tu turno descartando una carta. Sigue hasta vaciar tu mano.', icon: '🔄' }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
