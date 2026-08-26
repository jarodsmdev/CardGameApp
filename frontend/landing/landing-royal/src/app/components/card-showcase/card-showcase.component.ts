import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card-showcase',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="cards" class="py-24 px-6"
             style="background: linear-gradient(180deg, #0D1117 0%, rgba(249,168,37,0.03) 50%, #0D1117 100%);">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-4 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">Personaliza tu baraja</h2>
        <p class="text-rm-text-muted text-lg text-center max-w-[600px] mx-auto mb-16 animate-on-scroll">
          Elige entre 7 diseños de respaldo, 4 frentes y 3 estilos de Joker.
        </p>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-6">
          <div *ngFor="let card of cards; let i = index"
               class="text-center cursor-pointer animate-on-scroll group"
               [style.transition-delay]="(i * 0.08) + 's'">
            <!-- Card display -->
            <div class="aspect-[2.5/3.5] rounded-xl flex items-center justify-center mb-3 group-hover:-translate-y-2 group-hover:shadow-xl transition-all duration-300"
                 [style.background]="card.bg">
              <div class="w-[70%] aspect-[2.5/3.5] rounded-lg border-[3px] bg-white flex items-center justify-center transition-transform duration-300 group-hover:rotate-[-5deg] group-hover:rotate-x-[3deg]"
                   [style.border-color]="card.border">
                <div *ngIf="!card.isJoker" class="flex flex-col items-center gap-0.5">
                  <span class="text-3xl" [style.color]="card.suitColor">{{ card.suit }}</span>
                  <span class="font-display font-extrabold text-base" [style.color]="card.suitColor">{{ card.rank }}</span>
                </div>
                <div *ngIf="card.isJoker" class="flex flex-col items-center gap-1">
                  <span class="text-3xl">🃏</span>
                  <span class="text-[0.6rem] font-extrabold tracking-[2px] text-[#6A1B9A]">JOKER</span>
                </div>
              </div>
            </div>
            <h4 class="text-sm font-semibold text-white mb-0.5">{{ card.name }}</h4>
            <span class="text-xs text-rm-text-muted">{{ card.type }}</span>
          </div>
        </div>
      </div>
    </section>
  `
})
export class CardShowcaseComponent implements AfterViewInit {
  cards = [
    { name: 'Azul Clásico', type: 'Back', bg: 'linear-gradient(135deg, #1565C0, #1E88E5)', border: '#F9A825', suit: '♠', suitColor: '#1B1B1B', rank: 'A', isJoker: false },
    { name: 'Rojo Rombos', type: 'Back', bg: 'linear-gradient(135deg, #B71C1C, #C62828)', border: '#F9A825', suit: '♥', suitColor: '#C62828', rank: 'K', isJoker: false },
    { name: 'Negro Rayas', type: 'Back', bg: 'linear-gradient(135deg, #212121, #424242)', border: '#9E9E9E', suit: '♣', suitColor: '#1B1B1B', rank: 'Q', isJoker: false },
    { name: 'Verde Anillos', type: 'Back', bg: 'linear-gradient(135deg, #1B5E20, #2E7D32)', border: '#F9A825', suit: '♦', suitColor: '#C62828', rank: '10', isJoker: false },
    { name: 'Borde Dorado', type: 'Front', bg: 'linear-gradient(135deg, #333, #555)', border: '#F9A825', suit: '♥', suitColor: '#C62828', rank: 'A', isJoker: false },
    { name: 'Joker Dorado', type: 'Joker', bg: 'linear-gradient(135deg, #4A148C, #6A1B9A)', border: '#F9A825', suit: '', suitColor: '', rank: '', isJoker: true }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
