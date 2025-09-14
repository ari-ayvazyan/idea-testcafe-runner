import { Selector, ClientFunction } from 'testcafe';

// Basic fixture with simple formatting
fixture('Sample Test')
    .page('https://example.com');

test('Simple test with console log', async t => {
    console.log('Hello from TestCafe!');
});

test('Another test', async t => {
    console.log('Another test');
});

// Fixture with template literal formatting
fixture`Authentication Tests`
    .page`https://devexpress.github.io/testcafe/example`;

test('Login test', async t => {
    await t
        .click('#login-button')
        .typeText('#username', 'testuser')
        .typeText('#password', 'password')
        .click('#submit');
});

// Multi-line fixture declaration with hooks and metadata
fixture('Complex E2E Tests')
    .page('https://example.com/dashboard')
    .beforeEach(async t => {
        await t.click('#accept-cookies');
    })
    .afterEach(async t => {
        await t.eval(() => localStorage.clear());
    })
    .meta({
        group: 'e2e',
        priority: 'high',
        author: 'test-team'
    });

test('Dashboard interaction test', async t => {
    const selector = Selector('#main-content');
    await t.expect(selector.exists).ok();
});

// Fixture with parentheses formatting
fixture("API Integration Tests").page("https://api.example.com");

test("Fetch user data", async t => {
    const getUserData = ClientFunction(() => {
        return fetch('/api/users/1').then(r => r.json());
    });
    
    const userData = await getUserData();
    console.log('User data:', userData);
});

// Single-line fixture
fixture`Quick Tests`.page`https://google.com`;

test(`Search functionality`, async t => {
    await t.typeText('[name="q"]', 'testcafe').pressKey('enter');
});

// Fixture with multiline page setup
fixture('Form Testing Suite')
    .page(
        'https://very-long-example-url.com/with/many/path/segments/for/testing/purposes'
    );

test('Form validation test', async t => {
    await t
        .typeText('#email', 'invalid-email')
        .click('#submit')
        .expect(Selector('.error-message').innerText)
        .contains('Invalid email format');
});

// Fixture with no page (inherits from previous or uses default)
fixture('Utility Tests');

test('Browser info test', async t => {
    const getBrowserInfo = ClientFunction(() => navigator.userAgent);
    const userAgent = await getBrowserInfo();
    console.log('User Agent:', userAgent);
});

// Commented fixture (should not show play button)
// fixture('Disabled Tests')
//     .page('https://example.com');

// test('This test is commented out', async t => {
//     console.log('This should not run');
// });

/* Multi-line comment fixture
fixture('Block Commented Tests')
    .page('https://example.com');

test('Another commented test', async t => {
    console.log('This should also not run');
});
*/

// Fixture with hooks using different formatting styles
fixture("Payment Flow Tests")
    .page("https://checkout.example.com")
    .before(async ctx => {
        ctx.testData = {
            cardNumber: '4111111111111111',
            expiryDate: '12/25',
            cvv: '123'
        };
    })
    .beforeEach(async t => {
        await t.navigateTo('/checkout');
    });

test("Credit card payment", async t => {
    const { cardNumber, expiryDate, cvv } = t.fixtureCtx.testData;
    
    await t
        .typeText('#card-number', cardNumber)
        .typeText('#expiry-date', expiryDate)
        .typeText('#cvv', cvv)
        .click('#pay-button');
});

// Fixture with skip and only modifiers
fixture.only('Priority Tests')
    .page('https://example.com/important');

test('Critical functionality test', async t => {
    await t.click('#critical-button');
});

fixture.skip('Temporarily Disabled Tests')
    .page('https://example.com/disabled');

test('Flaky test', async t => {
    console.log('This test is skipped');
});

// Fixture with deeply nested structure
fixture('Mobile Testing Suite')
    .page('https://m.example.com')
    .meta({
        device: 'mobile',
        viewport: {
            width: 375,
            height: 667
        }
    });

test('Mobile navigation test', async t => {
    await t
        .click('#hamburger-menu')
        .expect(Selector('#mobile-nav').visible)
        .ok();
});

test.only('Run only this test', async t => {
    console.log('Only this test will run in the fixture');
});

test.skip('Skip this specific test', async t => {
    console.log('This test will be skipped');
});

// Fixture with RequestHooks
import { RequestMock } from 'testcafe';

const mockUsers = RequestMock()
    .onRequestTo(/.*\/api\/users.*/)
    .respond([
        { id: 1, name: 'John Doe' },
        { id: 2, name: 'Jane Smith' }
    ], 200, {
        'Access-Control-Allow-Origin': '*'
    });

fixture('API Mock Tests')
    .page('https://app.example.com')
    .requestHooks(mockUsers);

test('Test with mocked API', async t => {
    await t
        .click('#load-users')
        .expect(Selector('.user-item').count)
        .eql(2);
});

// Edge case: fixture with special characters in name
fixture('Tests with "Quotes" and \'Apostrophes\' & Symbols!')
    .page('https://example.com');

test('Test with unicode characters: 测试 🧪', async t => {
    console.log('Testing unicode support');
});

// Second Fixture (from original)
fixture('Second Fixture')
    .page('https://example.com/other');

test('Test in second fixture', async t => {
    console.log('Test in second fixture');
});