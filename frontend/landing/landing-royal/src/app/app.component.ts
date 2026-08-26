import { Component } from '@angular/core';
import { NavbarComponent } from './components/navbar/navbar.component';
import { HeroComponent } from './components/hero/hero.component';
import { AboutComponent } from './components/about/about.component';
import { GameplayComponent } from './components/gameplay/gameplay.component';
import { FeaturesComponent } from './components/features/features.component';
import { HowToPlayComponent } from './components/how-to-play/how-to-play.component';
import { CardShowcaseComponent } from './components/card-showcase/card-showcase.component';
import { CtaComponent } from './components/cta/cta.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroComponent,
    AboutComponent,
    GameplayComponent,
    FeaturesComponent,
    HowToPlayComponent,
    CardShowcaseComponent,
    CtaComponent,
    FooterComponent
  ],
  template: `
    <app-navbar />
    <app-hero />
    <app-about />
    <app-gameplay />
    <app-features />
    <app-how-to-play />
    <app-card-showcase />
    <app-cta />
    <app-footer />
  `,
  styles: [`:host { display: block; }`]
})
export class AppComponent {}
