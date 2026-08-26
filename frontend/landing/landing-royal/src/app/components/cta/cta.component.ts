import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cta',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="download" class="py-24 px-6"
             style="background: linear-gradient(180deg, #0D1117 0%, rgba(249,168,37,0.05) 50%, #0D1117 100%);">
      <div class="max-w-[1200px] mx-auto">
        <h2 class="font-display font-bold text-white text-center mb-12 animate-on-scroll"
            style="font-size: clamp(2rem, 5vw, 3rem);">Empezar a jugar es fácil</h2>

        <!-- Steps -->
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 sm:gap-6 mb-10 animate-on-scroll">
          <div *ngFor="let step of steps; let i = index; let last = last" class="flex items-center gap-3">
            <div class="w-11 h-11 min-w-[44px] bg-gradient-to-br from-rm-gold to-rm-gold-dark rounded-full flex items-center justify-center font-display font-extrabold text-lg text-rm-black">
              {{ i + 1 }}
            </div>
            <div>
              <h4 class="text-sm font-semibold text-white">{{ step.title }}</h4>
              <p class="text-xs text-rm-text-muted">{{ step.description }}</p>
            </div>
            <span *ngIf="!last" class="text-rm-gold text-lg mx-2 hidden sm:inline">→</span>
            <span *ngIf="!last" class="text-rm-gold text-lg rotate-90 sm:hidden">→</span>
          </div>
        </div>

        <!-- CTA button -->
        <div class="text-center mb-16 animate-on-scroll">
          <a href="#"
             class="inline-flex items-center gap-2 px-12 py-[18px] rounded-lg font-body font-semibold text-xl bg-gradient-to-r from-rm-gold to-rm-gold-dark text-rm-black shadow-[0_4px_20px_rgba(249,168,37,0.3)] hover:-translate-y-0.5 hover:shadow-[0_8px_30px_rgba(249,168,37,0.5)] transition-all">
            <span class="material-icons-outlined text-2xl">phone_android</span>
            Descargar gratis
          </a>
        </div>

        <!-- Phone mockups -->
        <div class="animate-on-scroll">
          <div class="flex justify-center items-end gap-5">
            <!-- Left phone -->
            <div class="w-[140px] h-[260px] bg-[#111] rounded-[20px] p-2 border-2 border-[#333]"
                 style="transform: perspective(800px) rotateY(8deg);">
              <div class="w-full h-full bg-[#1a1a2e] rounded-[14px] overflow-hidden p-3 flex flex-col items-center justify-center gap-4">
                <div class="w-12 h-12 bg-gradient-to-br from-rm-blue to-rm-blue-dark rounded-xl flex items-center justify-center font-display font-extrabold text-[1.4rem] text-white border-2 border-rm-gold">R</div>
                <div class="text-[0.6rem] bg-white text-gray-700 px-3 py-1.5 rounded font-medium">Iniciar con Google</div>
              </div>
            </div>

            <!-- Center phone -->
            <div class="w-[170px] h-[320px] bg-[#111] rounded-[20px] p-2 border-2 border-[#333]">
              <div class="w-full h-full bg-[#1a1a2e] rounded-[14px] overflow-hidden p-3 flex flex-col gap-2">
                <div class="text-[0.7rem] font-bold text-white pb-1.5 border-b border-white/10">Salas</div>
                <div *ngFor="let r of rooms" class="flex items-center gap-1.5 text-[0.6rem] text-white/80 p-1.5 bg-white/5 rounded-md">
                  <span>{{ r.icon }}</span>
                  <span>{{ r.name }}</span>
                </div>
                <div class="text-[0.6rem] text-rm-gold text-center py-1.5 border border-dashed border-rm-gold rounded-md mt-1">+ Crear sala</div>
              </div>
            </div>

            <!-- Right phone -->
            <div class="w-[140px] h-[260px] bg-[#111] rounded-[20px] p-2 border-2 border-[#333]"
                 style="transform: perspective(800px) rotateY(-8deg);">
              <div class="w-full h-full bg-[#1a1a2e] rounded-[14px] overflow-hidden p-3 flex flex-col gap-2">
                <div class="text-[0.7rem] font-bold text-white pb-1.5 border-b border-white/10">Mi perfil</div>
                <div class="grid grid-cols-3 gap-2 mt-2">
                  <div class="flex flex-col items-center gap-0.5">
                    <span class="font-display font-extrabold text-rm-gold text-base">47</span>
                    <span class="text-[0.5rem] text-white/50">Partidas</span>
                  </div>
                  <div class="flex flex-col items-center gap-0.5">
                    <span class="font-display font-extrabold text-rm-gold text-base">32</span>
                    <span class="text-[0.5rem] text-white/50">Victorias</span>
                  </div>
                  <div class="flex flex-col items-center gap-0.5">
                    <span class="font-display font-extrabold text-rm-gold text-base">12</span>
                    <span class="text-[0.5rem] text-white/50">Nivel</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  `
})
export class CtaComponent implements AfterViewInit {
  rooms = [
    { icon: '🏠', name: 'Sala de Juan' },
    { icon: '🎮', name: 'Partida rápida' },
    { icon: '👥', name: 'Amigos online: 3' }
  ];

  steps = [
    { title: 'Descarga la app', description: 'Gratis en Google Play' },
    { title: 'Inicia sesión', description: 'Con tu cuenta de Google' },
    { title: 'Crea una sala', description: 'Y juega con amigos' }
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
  }
}
