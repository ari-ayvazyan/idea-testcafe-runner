import { Selector } from 'testcafe';

// Mobile-specific testing patterns
fixture('Mobile Device Testing')
    .page('https://mobile.example.com')
    .beforeEach(async t => {
        // Set mobile viewport
        await t.resizeWindow(375, 667); // iPhone SE dimensions
    });

test('Mobile navigation menu', async t => {
    await t
        .click('.hamburger-menu')
        .expect(Selector('.mobile-nav').visible).ok()
        .click('.mobile-nav a:first-child')
        .expect(Selector('.mobile-nav').visible).notOk();
});

test('Touch gestures simulation', async t => {
    const swipeableElement = Selector('.swipeable-carousel');
    
    await t
        .drag(swipeableElement, -100, 0, { speed: 0.1 })  // Swipe left
        .expect(Selector('.carousel-item:nth-child(2)').hasClass('active')).ok();
});

fixture('Responsive Design Tests')
    .page('https://responsive.example.com');

const viewports = [
    { name: 'Mobile', width: 320, height: 568 },
    { name: 'Tablet', width: 768, height: 1024 },
    { name: 'Desktop', width: 1920, height: 1080 }
];

viewports.forEach(viewport => {
    test(`Layout at ${viewport.name} resolution`, async t => {
        await t.resizeWindow(viewport.width, viewport.height);
        
        const mainContent = Selector('#main-content');
        await t.expect(mainContent.exists).ok();
        
        if (viewport.width < 768) {
            await t.expect(Selector('.mobile-only').visible).ok();
        } else {
            await t.expect(Selector('.desktop-only').visible).ok();
        }
    });
});

fixture('PWA Testing')
    .page('https://pwa.example.com');

test('Service worker registration', async t => {
    const checkServiceWorker = ClientFunction(() => {
        return 'serviceWorker' in navigator;
    });
    
    const hasServiceWorker = await checkServiceWorker();
    await t.expect(hasServiceWorker).ok('Service Worker should be supported');
});

test('Offline functionality', async t => {
    // Simulate offline mode
    const goOffline = ClientFunction(() => {
        Object.defineProperty(navigator, 'onLine', {
            writable: true,
            value: false
        });
        window.dispatchEvent(new Event('offline'));
    });
    
    await goOffline();
    
    await t
        .click('#offline-test-button')
        .expect(Selector('.offline-message').exists).ok();
});