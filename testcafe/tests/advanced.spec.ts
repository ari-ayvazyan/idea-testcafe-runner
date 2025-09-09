import { Selector, t } from 'testcafe';
import { RequestLogger, RequestHook } from 'testcafe';

// TypeScript fixture with type annotations
fixture('TypeScript Advanced Tests')
    .page('https://typescript.example.com')
    .meta({
        type: 'integration',
        tags: ['typescript', 'advanced']
    });

interface UserData {
    id: number;
    name: string;
    email: string;
}

test('TypeScript test with interfaces', async (t: TestController) => {
    const userData: UserData = {
        id: 1,
        name: 'John Doe',
        email: 'john@example.com'
    };
    
    await t
        .typeText('#user-name', userData.name)
        .typeText('#user-email', userData.email)
        .click('#save-user');
});

// Fixture with RequestLogger
const logger = RequestLogger(/.*\/api\/.*/, {
    logRequestHeaders: true,
    logResponseHeaders: true
});

fixture('API Logging Tests')
    .page('https://api.example.com')
    .requestHooks(logger);

test('Test API request logging', async t => {
    await t.click('#trigger-api-call');
    
    console.log('API requests logged:', logger.requests.length);
    
    for (const request of logger.requests) {
        console.log(`${request.request.method} ${request.request.url}`);
    }
});

// Multiline fixture with complex beforeEach
fixture('Complex Setup Tests')
    .page('https://complex.example.com')
    .beforeEach(async t => {
        // Multi-step setup
        await t
            .navigateTo('/login')
            .typeText('#username', 'testuser')
            .typeText('#password', 'testpass')
            .click('#login-btn')
            .wait(2000)
            .navigateTo('/dashboard');
    })
    .afterEach(async t => {
        await t
            .click('#user-menu')
            .click('#logout');
    });

test('Dashboard functionality after login', async t => {
    const welcomeMessage = Selector('.welcome-message');
    
    await t
        .expect(welcomeMessage.exists).ok()
        .expect(welcomeMessage.innerText).contains('Welcome');
});

// Fixture with custom hooks
class CustomHook extends RequestHook {
    constructor() {
        super();
    }
    
    async onRequest() {
        console.log('Custom hook: Request intercepted');
    }
    
    async onResponse() {
        console.log('Custom hook: Response intercepted');
    }
}

fixture('Custom Hook Tests')
    .page('https://hooks.example.com')
    .requestHooks(new CustomHook());

test('Test with custom request hook', async t => {
    await t.click('#api-button');
});

// Fixture with roles
import { Role } from 'testcafe';

const adminRole = Role('https://admin.example.com/login', async t => {
    await t
        .typeText('#admin-username', 'admin')
        .typeText('#admin-password', 'adminpass')
        .click('#admin-login');
});

const userRole = Role('https://example.com/login', async t => {
    await t
        .typeText('#username', 'user')
        .typeText('#password', 'userpass')
        .click('#login');
});

fixture('Role-based Tests')
    .page('https://example.com/protected');

test('Admin access test', async t => {
    await t
        .useRole(adminRole)
        .expect(Selector('.admin-panel').exists).ok();
});

test('User access test', async t => {
    await t
        .useRole(userRole)
        .expect(Selector('.user-dashboard').exists).ok();
});

// Fixture with parametrized tests using meta
const testData = [
    { browser: 'chrome', viewport: '1920x1080' },
    { browser: 'firefox', viewport: '1366x768' },
    { browser: 'safari', viewport: '1440x900' }
];

testData.forEach(data => {
    fixture(`Cross-browser Tests - ${data.browser}`)
        .page('https://responsive.example.com')
        .meta({
            browser: data.browser,
            viewport: data.viewport
        });
    
    test(`Test layout on ${data.browser}`, async t => {
        await t.resizeWindow(
            parseInt(data.viewport.split('x')[0]),
            parseInt(data.viewport.split('x')[1])
        );
        
        const mainContent = Selector('#main-content');
        await t.expect(mainContent.visible).ok();
    });
});

// Error handling fixture
fixture('Error Handling Tests')
    .page('https://errors.example.com')
    .skipJsErrors(false); // Enable JS error detection

test('Test JavaScript error handling', async t => {
    try {
        await t.click('#error-trigger-button');
    } catch (error) {
        console.log('Caught error:', error.message);
    }
});
