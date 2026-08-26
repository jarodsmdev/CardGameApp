import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="min-h-screen flex items-center relative overflow-hidden"
             style="background: radial-gradient(ellipse at 30% 50%, rgba(30,136,229,0.15), transparent 60%),
                    radial-gradient(ellipse at 70% 80%, rgba(249,168,37,0.08), transparent 50%),
                    #0D1117;">

      <!-- Particles -->
      <div class="absolute inset-0 pointer-events-none">
        <span *ngFor="let p of particles" class="absolute w-1 h-1 bg-rm-gold rounded-full opacity-0"
              [style]="p"></span>
      </div>

      <div class="max-w-[1200px] mx-auto px-6 grid md:grid-cols-2 gap-16 items-center pt-20 w-full">
        <!-- Text -->
        <div>
          <h1 class="font-display font-extrabold text-white leading-[1.1] mb-6 animate-fade-in-up"
              style="font-size: clamp(2.5rem, 6vw, 4.5rem);">
            Reúne, desafía<br>
            <span class="text-rm-gold">y reina.</span>
          </h1>
          <p class="text-rm-text-muted text-lg leading-7 mb-9 max-w-[500px] animate-fade-in-up"
             style="animation-delay: 0.2s;">
            El juego de cartas multijugador favorito de Latinoamérica, ahora online.
            Juega Carioca con amigos en salas privadas, con reglas oficiales y sin trampas.
          </p>
          <div class="flex flex-wrap gap-4 animate-fade-in-up" style="animation-delay: 0.4s;">
            <a href="#download"
               class="inline-flex items-center gap-2 px-8 py-3.5 rounded-lg font-body font-semibold text-base bg-gradient-to-r from-rm-gold to-rm-gold-dark text-rm-black shadow-[0_4px_20px_rgba(249,168,37,0.3)] hover:-translate-y-0.5 hover:shadow-[0_8px_30px_rgba(249,168,37,0.5)] transition-all">
              <span class="material-icons-outlined text-xl">phone_android</span>
              Descargar gratis
            </a>
            <a href="#gameplay"
               class="inline-flex items-center gap-2 px-8 py-3.5 rounded-lg font-body font-semibold text-base border border-rm-border text-rm-text hover:border-rm-gold hover:text-rm-gold hover:bg-rm-gold/5 transition-all">
              <span class="material-icons-outlined text-xl">play_circle</span>
              Ver gameplay
            </a>
          </div>
        </div>

        <!-- Phone mockup -->
        <div class="flex justify-center relative animate-fade-in-up" style="animation-delay: 0.6s;">
          <div class="w-[280px] h-[560px] bg-[#111] rounded-[36px] p-3 border-[3px] border-[#333] shadow-[0_20px_60px_rgba(0,0,0,0.5),0_0_80px_rgba(30,136,229,0.1)] animate-float relative z-10">
            <div class="w-full h-full bg-[#1a472a] rounded-[24px] overflow-hidden flex flex-col p-3">
              <!-- Screen header -->
              <div class="flex justify-between items-center pb-2 border-b border-white/15 mb-3">
                <span class="text-[0.65rem] text-white/80 font-semibold">Carioca — Ronda 5</span>
                <span class="text-[0.65rem] text-rm-gold font-bold">2,450 pts</span>
              </div>

              <!-- Board -->
              <div class="flex-1 flex flex-col gap-3">
                <!-- Combos -->
                <div class="flex flex-col gap-1.5 items-center">
                  <div class="flex gap-1">
                    <span *ngFor="let c of combo1" class="w-8 h-11 bg-white rounded flex items-center justify-center text-[0.6rem] font-semibold border border-gray-300"
                          [class]="c.color">{{ c.value }}</span>
                  </div>
                  <div class="flex gap-1">
                    <span *ngFor="let c of combo2" class="w-8 h-11 bg-white rounded flex items-center justify-center text-[0.6rem] font-semibold border border-gray-300"
                          [class]="c.color">{{ c.value }}</span>
                  </div>
                </div>

                <!-- Piles -->
                <div class="flex justify-center gap-6 mt-2">
                  <div class="flex flex-col items-center gap-1">
                    <span class="text-3xl text-rm-gold">🂠</span>
                    <small class="text-[0.55rem] text-white/60">Mazo</small>
                  </div>
                  <div class="flex flex-col items-center gap-1">
                    <span class="w-11 h-[60px] bg-white rounded border border-gray-300 flex items-center justify-center text-rm-red text-sm font-semibold">Q♦</span>
                    <small class="text-[0.55rem] text-white/60">Pozo</small>
                  </div>
                </div>
              </div>

              <!-- Hand -->
              <div class="flex justify-center gap-0.5 pt-2 mt-auto border-t border-white/10">
                <span *ngFor="let c of handCards"
                      class="w-[26px] h-[36px] bg-white rounded-[3px] flex items-center justify-center text-[0.5rem] font-semibold border border-gray-300"
                      [class]="c.color">{{ c.value }}</span>
              </div>
            </div>
          </div>
          <!-- Glow -->
          <div class="absolute w-[400px] h-[400px] rounded-full z-[1] animate-pulse-glow"
               style="background: radial-gradient(circle, rgba(249,168,37,0.15), transparent 70%);"></div>
        </div>
      </div>

      <!-- Scroll indicator -->
      <div class="absolute bottom-8 left-1/2 animate-bounce-down">
        <span class="material-icons-outlined text-3xl text-rm-text-muted">expand_more</span>
      </div>
    </section>
  `
})
export class HeroComponent {
  combo1 = [
    { value: '7♦', color: 'text-rm-red' },
    { value: '8♦', color: 'text-rm-red' },
    { value: '9♦', color: 'text-rm-red' },
  ];
  combo2 = [
    { value: 'K♠', color: 'text-rm-black' },
    { value: 'K♣', color: 'text-rm-black' },
    { value: 'K♥', color: 'text-rm-red' },
  ];
  handCards = [
    { value: '2♥', color: 'text-rm-red' },
    { value: '5♠', color: 'text-rm-black' },
    { value: '8♦', color: 'text-rm-red' },
    { value: 'J♣', color: 'text-rm-black' },
    { value: 'Q♥', color: 'text-rm-red' },
    { value: 'K♠', color: 'text-rm-black' },
    { value: 'A♦', color: 'text-rm-red' },
  ];

  particles = Array.from({ length: 15 }, (_, i) =>
    `left:${Math.random()*100}%;animation-delay:${Math.random()*6}s;animation-duration:${4+Math.random()*4}s;`
  );
}
