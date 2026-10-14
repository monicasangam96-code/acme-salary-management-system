import { Injectable } from '@angular/core';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notifications: Notification[] = [];

  get notifications$() { return this.notifications; }

  success(message: string): void {
    this.add('success', message);
  }

  error(message: string): void {
    this.add('error', message);
    // Auto-dismiss after 5 seconds
    setTimeout(() => this.dismiss(this.notifications[this.notifications.length - 1]), 5000);
  }

  info(message: string): void {
    this.add('info', message);
  }

  warning(message: string): void {
    this.add('warning', message);
  }

  dismiss(notification: Notification): void {
    const idx = this.notifications.indexOf(notification);
    if (idx !== -1) {
      this.notifications.splice(idx, 1);
    }
  }

  private add(type: 'success' | 'error' | 'info' | 'warning', message: string): void {
    this.notifications.push({ id: Date.now().toString(), type, message });
  }
}
