import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-gameplay',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="gameplay" class="py-24 px-6 bg-rm-bg">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-4 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">Mira cómo se juega</h2>
        <p class="text-rm-text-muted text-lg text-center max-w-[600px] mx-auto mb-16 animate-on-scroll">
          Roba cartas, forma combinaciones y descarta para terminar tu turno.
          Desliza para jugar, toca para elegir.
        </p>

        <!-- Main board -->
        <div class="mb-12 rounded-2xl overflow-hidden animate-on-scroll"
             style="background: linear-gradient(135deg, #1a472a, #0f2d1a); min-height: 400px;">
          <div class="p-8 max-w-[700px] mx-auto">
            <!-- Opponents -->
            <div class="flex justify-center gap-8 mb-6">
              <div *ngFor="let o of opponents" class="flex flex-col items-center gap-1">
                <div class="w-10 h-10 bg-rm-gray-800 rounded-full flex items-center justify-center text-xl border-2 border-rm-border">{{ o.avatar }}</div>
                <span class="text-[0.7rem] text-white/60">{{ o.cards }} cartas</span>
              </div>
            </div>

            <!-- Table -->
            <div class="bg-black/20 rounded-xl p-5 flex flex-col items-center gap-4">
              <div class="flex gap-4 flex-wrap justify-center">
                <div *ngFor="let combo of combos" class="flex gap-0.5">
                  <span *ngFor="let card of combo" class="w-9 h-[50px] bg-white rounded flex items-center justify-center text-[0.7rem] font-semibold border border-gray-300"
                        [class]="card.includes('♥') || card.includes('♦') ? 'text-rm-red' : 'text-rm-black'">{{ card }}</span>
                </div>
              </div>
              <div class="flex gap-8">
                <div class="flex flex-col items-center gap-1">
                  <span class="text-[2.5rem] text-rm-red">🂠</span>
                  <small class="text-[0.65rem] text-white/50">Mazo</small>
                </div>
                <div class="flex flex-col items-center gap-1">
                  <span class="w-11 h-[60px] bg-white rounded border border-gray-300 flex items-center justify-center text-rm-red text-sm font-semibold">Q♦</span>
                  <small class="text-[0.65rem] text-white/50">Pozo</small>
                </div>
              </div>
            </div>

            <!-- Hand -->
            <div class="flex justify-center gap-1 pt-3 mt-4 border-t border-white/10">
              <span *ngFor="let c of handCards"
                    class="w-10 h-14 bg-white rounded flex items-center justify-center text-[0.75rem] font-semibold border border-gray-300 hover:-translate-y-2 hover:shadow-lg transition-all cursor-pointer"
                    [class]="c.includes('♥') || c.includes('♦') ? 'text-rm-red' : 'text-rm-black'">{{ c }}</span>
            </div>
          </div>
        </div>

        <!-- Detail cards -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div *ngFor="let item of details; let i = index"
               class="text-center animate-on-scroll"
               [style.transition-delay]="(i * 0.1) + 's'">
            <div class="bg-rm-bg-card rounded-xl flex flex-col items-center justify-center gap-2 mb-4"
                 style="min-height: 180px;">
              <span class="text-4xl">{{ item.icon }}</span>
              <span class="text-[0.8rem] text-rm-text-muted font-medium">{{ item.label }}</span>
            </div>
            <h4 class="font-display font-bold text-white text-base mb-1.5">{{ item.title }}</h4>
            <p class="text-rm-text-muted text-sm leading-relaxed">{{ item.description }}</p>
          </div>
        </div>
      </div>
    </section>
  `
})
export class GameplayComponent implements AfterViewInit {
  opponents = [
    { avatar: '🤖', cards: 8 },
    { avatar: '👤', cards: 5 },
    { avatar: '🧑', cards: 10 }
  ];

  combos = [
    ['7♦', '8♦', '9♦', '10♦'],
    ['K♠', 'K♣', 'K♥'],
    ['A♠', 'A♣', 'A♥', 'A♦']
  ];

  handCards = ['2♥', '5♠', '8♦', 'J♣', 'Q♥', 'K♠', 'A♦', '3♣', '7♥'];

  details = [
    { icon: '📥', title: 'Robar', description: 'Toca el mazo o el pozo para tomar una carta.', label: 'ROBA' },
    { icon: '🃏', title: 'Combinar', description: 'Forma escaleras o grupos en la mesa.', label: 'COMBINA' },
    { icon: '📤', title: 'Bajarse', description: 'Juega tus combinaciones cuando tengas las cartas.', label: 'BÁJATE' },
    { icon: '🔄', title: 'Repetir', description: 'Descarta y repite hasta vaciar tu mano.', label: 'REPITE' }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
