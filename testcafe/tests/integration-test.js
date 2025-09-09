import { Selector, Role } from 'testcafe';

// Integration test with different naming convention
fixture('End-to-End Integration Tests')
    .page('https://integration.example.com');

test('Full user journey workflow', async t => {
    await t
        .click('#start-journey')
        .typeText('#user-input', 'integration test user')
        .click('#next-step')
        .expect(Selector('.step-2').exists).ok()
        .click('#complete-journey')
        .expect(Selector('.success-page').exists).ok();
});

// Multiple fixtures with various patterns
fixture `Template Literal Integration`
    .page `https://template.example.com`;

test `Template literal test name`, async t => {
    console.log('Template literal test executed');
};

fixture("Double Quote Integration")
    .page("https://doublequote.example.com");

test("Double quote test name", async t => {
    console.log("Double quote test executed");
});

// Fixture with complex multiline setup
fixture('Complex Multiline Integration')
    .page(
        process.env.NODE_ENV === 'production' 
            ? 'https://prod.example.com'
            : 'https://staging.example.com'
    )
    .before(async ctx => {
        ctx.sharedData = {
            testRun: new Date().toISOString(),
            environment: process.env.NODE_ENV || 'development'
        };
    })
    .beforeEach(async t => {
        console.log(`Running test at: ${t.fixtureCtx.sharedData.testRun}`);
    });

test('Multiline test with environment detection', async t => {
    const env = t.fixtureCtx.sharedData.environment;
    console.log(`Testing in environment: ${env}`);
    
    await t.expect(Selector('body').exists).ok();
});