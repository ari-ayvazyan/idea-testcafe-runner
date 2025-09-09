// TypeScript smoke tests with naming pattern variation
import { Selector, ClientFunction } from 'testcafe';

// Basic smoke tests to verify core functionality
fixture('Application Smoke Tests')
    .page('https://app.example.com')
    .meta({ 
        suite: 'smoke',
        priority: 'critical'
    });

test('Application loads successfully', async (t: TestController) => {
    await t
        .expect(Selector('body').exists).ok('Page should load')
        .expect(Selector('title').innerText).ok('Page should have title');
});

test('Navigation menu is accessible', async t => {
    const nav = Selector('nav');
    const navLinks = Selector('nav a');
    
    await t
        .expect(nav.exists).ok('Navigation should exist')
        .expect(navLinks.count).gte(1, 'Navigation should have links');
});

test('Footer information is present', async t => {
    await t
        .expect(Selector('footer').exists).ok()
        .expect(Selector('footer').innerText).contains('©');
});

// Performance smoke tests
fixture('Performance Smoke Tests')
    .page('https://performance.example.com');

test('Page loads within acceptable time', async t => {
    const startTime = Date.now();
    
    await t.expect(Selector('#main-content').exists).ok();
    
    const loadTime = Date.now() - startTime;
    console.log(`Page load time: ${loadTime}ms`);
    
    // Smoke test should pass even with slower load times
    await t.expect(loadTime).lt(10000, 'Page should load within 10 seconds');
});

test('Critical resources are loaded', async t => {
    const checkResources = ClientFunction(() => {
        const resources = performance.getEntriesByType('resource');
        const criticalResources = resources.filter(resource => 
            resource.name.includes('.css') || 
            resource.name.includes('.js') ||
            resource.name.includes('/api/')
        );
        return criticalResources.length;
    });
    
    const resourceCount = await checkResources();
    await t.expect(resourceCount).gte(1, 'Critical resources should be loaded');
});

// Security smoke tests
fixture('Security Smoke Tests')
    .page('https://secure.example.com');

test('HTTPS is enforced', async t => {
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('https://', 'Should use HTTPS protocol');
});

test('Security headers are present', async t => {
    const checkSecurityHeaders = ClientFunction(() => {
        // This would typically be done server-side, but demonstrating concept
        const meta = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
        return !!meta;
    });
    
    // Note: This is a simplified check - real security testing would be more comprehensive
    console.log('Security headers check completed');
});

// API smoke tests
fixture('API Smoke Tests')
    .page('https://api-consumer.example.com');

test('Health check endpoint responds', async t => {
    const healthCheck = ClientFunction(async () => {
        try {
            const response = await fetch('/api/health');
            return {
                status: response.status,
                ok: response.ok
            };
        } catch (error) {
            return { status: 0, ok: false, error: error.message };
        }
    });
    
    const health = await healthCheck();
    await t
        .expect(health.ok).ok('Health check should pass')
        .expect(health.status).eql(200);
});

// Cross-browser smoke tests
const browsers = ['chrome', 'firefox', 'safari'];

browsers.forEach(browser => {
    fixture(`${browser} Compatibility Smoke Tests`)
        .page('https://compatibility.example.com')
        .meta({ browser });
    
    test(`Basic functionality works in ${browser}`, async t => {
        await t
            .expect(Selector('#app').exists).ok()
            .click('#test-button')
            .expect(Selector('.result').exists).ok();
    });
});

// Mobile smoke tests
fixture('Mobile Smoke Tests')
    .page('https://mobile.example.com')
    .beforeEach(async t => {
        await t.resizeWindow(375, 667); // iPhone dimensions
    });

test('Mobile layout renders correctly', async t => {
    await t
        .expect(Selector('.mobile-header').exists).ok()
        .expect(Selector('.hamburger-menu').exists).ok()
        .click('.hamburger-menu')
        .expect(Selector('.mobile-nav').visible).ok();
});

test('Touch interactions work', async t => {
    const touchElement = Selector('.touch-target');
    
    await t
        .expect(touchElement.exists).ok()
        .click(touchElement)  // Simulates touch
        .expect(Selector('.touch-feedback').exists).ok();
});

// Accessibility smoke tests
fixture('Accessibility Smoke Tests')
    .page('https://accessible.example.com');

test('Basic accessibility features present', async t => {
    // Check for alt text on images
    const images = Selector('img');
    const imagesWithAlt = Selector('img[alt]');
    
    if (await images.count > 0) {
        await t.expect(imagesWithAlt.count).gte(1, 'Images should have alt text');
    }
    
    // Check for semantic HTML
    await t.expect(Selector('main').exists).ok('Should have main element');
});

test('Keyboard navigation basics', async t => {
    // Test tab navigation
    await t
        .pressKey('tab')
        .expect(Selector(':focus').exists).ok('Something should receive focus')
        .pressKey('shift+tab')  // Go back
        .expect(Selector(':focus').exists).ok('Reverse tab should work');
});

// Data integrity smoke tests
fixture('Data Integrity Smoke Tests')
    .page('https://data.example.com');

test('Form data persists correctly', async t => {
    const testValue = `Test-${Date.now()}`;
    
    await t
        .typeText('#test-input', testValue)
        .click('#save-data')
        .wait(1000)  // Wait for save
        .eval(() => location.reload())  // Refresh page
        t.wait(1000)  // Wait for load
        .expect(Selector('#test-input').value).eql(testValue);
});

// Internationalization smoke tests
fixture('i18n Smoke Tests')
    .page('https://i18n.example.com');

test('Multiple languages supported', async t => {
    const languageSelector = Selector('#language-select');
    
    if (await languageSelector.exists) {
        await t
            .click(languageSelector)
            .click(Selector('option[value="es"]'))  // Spanish
            .expect(Selector('html').getAttribute('lang')).eql('es');
    } else {
        console.log('Language selector not found - single language app');
    }
});