import { Selector, ClientFunction } from 'testcafe';

// User flow testing with realistic scenarios
fixture('User Registration Flow')
    .page('https://signup.example.com')
    .beforeEach(async t => {
        // Clear any existing session data
        const clearLocalStorage = ClientFunction(() => {
            localStorage.clear();
            sessionStorage.clear();
        });
        await clearLocalStorage();
    });

test('Successful user registration', async t => {
    const timestamp = Date.now();
    const email = `test.user.${timestamp}@example.com`;
    
    await t
        .typeText('#first-name', 'John')
        .typeText('#last-name', 'Doe')
        .typeText('#email', email)
        .typeText('#password', 'SecurePass123!')
        .typeText('#confirm-password', 'SecurePass123!')
        .click('#terms-checkbox')
        .click('#register-button')
        .expect(Selector('.success-message').innerText)
        .contains('Registration successful');
});

test('Password validation errors', async t => {
    await t
        .typeText('#email', 'test@example.com')
        .typeText('#password', '123')  // Weak password
        .typeText('#confirm-password', '456')  // Different password
        .click('#register-button')
        .expect(Selector('.password-error').exists).ok()
        .expect(Selector('.confirm-password-error').exists).ok();
});

fixture('E-commerce Shopping Cart')
    .page('https://shop.example.com/products')
    .meta({
        feature: 'shopping-cart',
        priority: 'high'
    });

test('Add items to cart and checkout', async t => {
    await t
        // Add first item
        .click('.product-item:nth-child(1) .add-to-cart')
        .expect(Selector('.cart-count').innerText).eql('1')
        
        // Add second item
        .click('.product-item:nth-child(2) .add-to-cart')
        .expect(Selector('.cart-count').innerText).eql('2')
        
        // Go to cart
        .click('.cart-icon')
        .expect(Selector('.cart-item').count).eql(2)
        
        // Proceed to checkout
        .click('#checkout-button')
        .expect(Selector('.checkout-form').exists).ok();
});

test('Update item quantities in cart', async t => {
    // Add item first
    await t.click('.product-item:first-child .add-to-cart');
    
    // Go to cart and update quantity
    await t
        .click('.cart-icon')
        .selectText('.quantity-input')
        .typeText('.quantity-input', '3')
        .click('.update-quantity')
        .expect(Selector('.item-total').innerText).contains('$');
});

// Multi-step workflow fixture
fixture('Admin Dashboard Workflow')
    .page('https://admin.example.com/login')
    .before(async ctx => {
        // Setup test data
        ctx.adminCredentials = {
            username: 'admin@example.com',
            password: 'AdminPass123!'
        };
    });

test('Admin login and user management', async t => {
    const { username, password } = t.fixtureCtx.adminCredentials;
    
    // Login
    await t
        .typeText('#admin-email', username)
        .typeText('#admin-password', password)
        .click('#login-submit')
        .expect(Selector('.admin-dashboard').exists).ok();
    
    // Navigate to user management
    await t
        .click('#users-menu')
        .click('#manage-users')
        .expect(Selector('.users-table').exists).ok();
    
    // Search for user
    await t
        .typeText('#user-search', 'john.doe')
        .pressKey('enter')
        .expect(Selector('.user-row').count).gte(1);
    
    // Edit user
    await t
        .click('.user-row:first-child .edit-button')
        .selectText('#user-status')
        .pressKey('delete')
        .typeText('#user-status', 'active')
        .click('#save-user')
        .expect(Selector('.success-notification').exists).ok();
});

// Cross-browser compatibility fixture
fixture('Cross-browser Compatibility Tests')
    .page('https://compatibility.example.com')
    .meta({
        browsers: ['chrome', 'firefox', 'safari', 'edge'],
        responsive: true
    });

test('CSS Grid layout compatibility', async t => {
    const gridContainer = Selector('.grid-container');
    
    await t
        .expect(gridContainer.exists).ok()
        .expect(gridContainer.getStyleProperty('display')).eql('grid');
    
    // Test responsive behavior
    await t
        .resizeWindow(768, 1024)  // Tablet
        .expect(gridContainer.getStyleProperty('grid-template-columns')).contains('1fr')
        
        .resizeWindow(320, 568)   // Mobile
        .expect(gridContainer.getStyleProperty('grid-template-columns')).eql('1fr');
});

test('Modern JavaScript features support', async t => {
    const checkModernJS = ClientFunction(() => {
        try {
            // Test async/await, arrow functions, template literals
            const testFunction = async () => {
                const result = await Promise.resolve(`Modern JS works!`);
                return result;
            };
            
            return testFunction().then(result => result === 'Modern JS works!');
        } catch (error) {
            return false;
        }
    });
    
    const isSupported = await checkModernJS();
    await t.expect(isSupported).ok('Modern JavaScript features should be supported');
});

// Performance testing fixture
fixture('Performance Tests')
    .page('https://performance.example.com')
    .beforeEach(async t => {
        // Clear cache and reset performance metrics
        const resetPerformance = ClientFunction(() => {
            if (window.performance && window.performance.clearMarks) {
                window.performance.clearMarks();
                window.performance.clearMeasures();
            }
        });
        await resetPerformance();
    });

test('Page load performance', async t => {
    const measurePageLoad = ClientFunction(() => {
        return new Promise((resolve) => {
            window.addEventListener('load', () => {
                const perfData = window.performance.getEntriesByType('navigation')[0];
                resolve({
                    domContentLoaded: perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart,
                    loadComplete: perfData.loadEventEnd - perfData.loadEventStart,
                    totalTime: perfData.loadEventEnd - perfData.fetchStart
                });
            });
        });
    });
    
    const metrics = await measurePageLoad();
    console.log('Performance metrics:', metrics);
    
    // Assert performance thresholds
    await t
        .expect(metrics.totalTime).lt(5000, 'Page should load in under 5 seconds')
        .expect(metrics.domContentLoaded).lt(2000, 'DOM should be ready in under 2 seconds');
});

// Accessibility testing fixture
fixture('Accessibility Tests')
    .page('https://accessibility.example.com')
    .meta({
        a11y: true,
        wcag: '2.1'
    });

test('Keyboard navigation', async t => {
    // Test tab navigation
    await t
        .pressKey('tab')
        .expect(Selector(':focus').tagName).eql('button')
        .pressKey('tab')
        .expect(Selector(':focus').tagName).eql('input')
        .pressKey('shift+tab')
        .expect(Selector(':focus').tagName).eql('button');
});

test('ARIA attributes', async t => {
    const mainContent = Selector('[role="main"]');
    const navigation = Selector('[role="navigation"]');
    
    await t
        .expect(mainContent.exists).ok('Main content should have role="main"')
        .expect(navigation.exists).ok('Navigation should have role="navigation"')
        .expect(Selector('button[aria-label]').count).gte(1, 'Buttons should have aria-label');
});

// Error boundary testing
fixture('Error Handling and Recovery')
    .page('https://errors.example.com')
    .skipJsErrors(false); // We want to test error handling

test('Graceful error recovery', async t => {
    // Trigger an error intentionally
    await t.click('#trigger-error-button');
    
    // Check that error boundary catches it
    await t
        .expect(Selector('.error-boundary').exists).ok()
        .expect(Selector('.error-message').innerText).contains('Something went wrong');
    
    // Test recovery mechanism
    await t
        .click('#retry-button')
        .expect(Selector('.error-boundary').exists).notOk()
        .expect(Selector('.main-content').exists).ok();
});

test('Network error handling', async t => {
    // Simulate network failure
    const simulateNetworkError = ClientFunction(() => {
        window.fetch = () => Promise.reject(new Error('Network error'));
    });
    
    await simulateNetworkError();
    
    // Trigger network request
    await t
        .click('#load-data-button')
        .expect(Selector('.error-message').innerText).contains('Failed to load data')
        .expect(Selector('.retry-button').exists).ok();
});