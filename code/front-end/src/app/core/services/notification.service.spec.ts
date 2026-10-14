import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    service = new NotificationService();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('success()', () => {
    it('should add a success notification', () => {
      service.success('Operation complete');
      expect(service.notifications$.length).toBe(1);
      expect(service.notifications$[0].type).toBe('success');
      expect(service.notifications$[0].message).toBe('Operation complete');
    });
  });

  describe('error()', () => {
    it('should add an error notification', () => {
      service.error('Something went wrong');
      expect(service.notifications$.length).toBe(1);
      expect(service.notifications$[0].type).toBe('error');
      expect(service.notifications$[0].message).toBe('Something went wrong');
    });
  });

  describe('info()', () => {
    it('should add an info notification', () => {
      service.info('Information message');
      expect(service.notifications$[0].type).toBe('info');
      expect(service.notifications$[0].message).toBe('Information message');
    });
  });

  describe('warning()', () => {
    it('should add a warning notification', () => {
      service.warning('Low disk space');
      expect(service.notifications$[0].type).toBe('warning');
      expect(service.notifications$[0].message).toBe('Low disk space');
    });
  });

  describe('dismiss()', () => {
    it('should remove a notification from the list', () => {
      service.success('First message');
      service.info('Second message');
      const target = service.notifications$[0];
      service.dismiss(target);
      expect(service.notifications$.length).toBe(1);
      expect(service.notifications$).not.toContain(target);
    });

    it('should do nothing if notification is not in list', () => {
      const phantom: NotificationService['notifications$'][number] = { id: '999', type: 'success', message: '' };
      service.dismiss(phantom);
      expect(service.notifications$.length).toBe(0);
    });
  });

  describe('multiple notifications', () => {
    it('should accumulate notifications in order', () => {
      service.success('One');
      service.warning('Two');
      service.error('Three');
      expect(service.notifications$.length).toBe(3);
      expect(service.notifications$[0].message).toBe('One');
      expect(service.notifications$[1].message).toBe('Two');
      expect(service.notifications$[2].message).toBe('Three');
    });

    it('should have unique ids for each notification', () => {
      service.success('A');
      service.success('B');
      expect(service.notifications$[0].id).not.toBe(service.notifications$[1].id);
    });
  });
});
