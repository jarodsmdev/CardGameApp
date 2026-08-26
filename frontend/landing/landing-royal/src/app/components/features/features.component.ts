import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="features" class="py-24 px-6"
             style="background: linear-gradient(180deg, #0D1117 0%, rgba(30,136,229,0.05) 50%, #0D1117 100%);">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-4 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">¿Por qué Royal Meld?</h2>
        <p class="text-rm-text-muted text-lg text-center max-w-[600px] mx-auto mb-16 animate-on-scroll">
          Diseñado para jugadores de verdad. Sin atajos, sin trampas, sin excusas.
        </p>

        <div class="grid md:grid-cols-3 gap-6">
          <div *ngFor="let f of features; let i = index"
               class="bg-rm-bg-card border border-rm-border rounded-2xl p-8 hover:border-rm-gold hover:-translate-y-1 transition-all duration-300 animate-on-scroll"
               [style.transition-delay]="(i * 0.08) + 's'">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4"
                 [style.background]="f.bg">
              <span class="material-icons-outlined text-2xl" [style.color]="f.color">{{ f.icon }}</span>
            </div>
            <h3 class="font-display font-bold text-white text-lg mb-2.5">{{ f.title }}</h3>
            <p class="text-rm-text-muted text-sm leading-relaxed">{{ f.description }}</p>
          </div>
        </div>
      </div>
    </section>
  `
})
export class FeaturesComponent implements AfterViewInit {
  features = [
    { icon: 'security', title: 'Sin Trampas', description: 'El servidor valida cada jugada. No hay forma de hacer trampa.', bg: 'rgba(30,136,229,0.15)', color: '#42A5F5' },
    { icon: 'tune', title: 'Reglas Configurables', description: 'Cada sala define sus rondas, cantidad de rondas y variantes.', bg: 'rgba(249,168,37,0.15)', color: '#F9A825' },
    { icon: 'group', title: 'Amigos y Salas Privadas', description: 'Comparte un código y juega solo con quien tú quieras.', bg: 'rgba(46,125,50,0.15)', color: '#66BB6A' },
    { icon: 'palette', title: 'Baraja Personalizable', description: 'Elige el diseño de tus cartas: backs, fronts y jokers.', bg: 'rgba(106,27,154,0.15)', color: '#AB47BC' },
    { icon: 'sync', title: 'Reconexión Automática', description: 'Se cortó la conexión? Vuelve sin perder tu turno.', bg: 'rgba(0,150,136,0.15)', color: '#26A69A' },
    { icon: 'bar_chart', title: 'Estadísticas e Historial', description: 'Revisa tus partidas, puntajes y progreso.', bg: 'rgba(198,40,40,0.15)', color: '#EF5350' }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
