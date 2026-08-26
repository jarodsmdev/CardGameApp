import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="about" class="py-24 px-6"
             style="background: linear-gradient(180deg, #0D1117 0%, #161B22 50%, #0D1117 100%);">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-4 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">¿Qué es Royal Meld?</h2>
        <p class="text-rm-text-muted text-lg text-center max-w-[600px] mx-auto mb-16 animate-on-scroll">
          La plataforma definitiva para jugar Carioca con tus amigos,
          sin trampas y con reglas que tú controlas.
        </p>

        <div class="grid md:grid-cols-3 gap-8">
          <div *ngFor="let f of features; let i = index"
               class="bg-rm-bg-card border border-rm-border rounded-2xl p-9 text-center hover:border-rm-gold hover:-translate-y-1.5 transition-all duration-300 animate-on-scroll"
               [style.transition-delay]="(i * 0.1) + 's'">
            <span class="text-4xl block mb-4">{{ f.icon }}</span>
            <h3 class="font-display font-bold text-white text-xl mb-3">{{ f.title }}</h3>
            <p class="text-rm-text-muted text-[0.95rem] leading-relaxed">{{ f.description }}</p>
          </div>
        </div>
      </div>
    </section>
  `
})
export class AboutComponent implements AfterViewInit {
  features = [
    { icon: '🃏', title: 'Cartas Clásicas', description: 'Juega Carioca con las reglas oficiales, 108 cartas y hasta 4 jugadores en partidas de verdad.' },
    { icon: '🌐', title: 'Multijugador Real', description: 'Enfrenta a amigos o rivales en salas privadas con código. Sin randoms, sin sorpresas.' },
    { icon: '🏆', title: 'Compite en Vivo', description: 'Sube de nivel con cada partida y demuestra quién es el rey de la mesa.' }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
