import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-kpi-card',
  template: `
    <div class="kpi-card" [style.border-left-color]="color">
      <div class="kpi-title">{{ title }}</div>
      <div class="kpi-value">{{ value | number:'1.2-2' }}</div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .kpi-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      border-left: 4px solid #0d6efd;
    }
    .kpi-title { font-size: 14px; color: #6c757d; margin-bottom: 8px; }
    .kpi-value { font-size: 28px; font-weight: 700; color: #212529; }
  `]
})
export class KpiCardComponent {
  @Input() title = '';
  @Input() value = 0;
  @Input() color = '#0d6efd';
}
