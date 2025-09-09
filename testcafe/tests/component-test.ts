// Component-level testing patterns
import { Selector, ClientFunction } from 'testcafe';

// Component isolation testing
fixture('Button Component Tests')
    .page('https://components.example.com/button');

test('Button click events', async t => {
    const button = Selector('.test-button');
    const clickCounter = Selector('#click-count');
    
    await t
        .expect(clickCounter.innerText).eql('0')
        .click(button)
        .expect(clickCounter.innerText).eql('1')
        .click(button)
        .expect(clickCounter.innerText).eql('2');
});

test('Button disabled state', async t => {
    const button = Selector('#toggle-button');
    const disableBtn = Selector('#disable-button');
    
    await t
        .expect(button.hasAttribute('disabled')).notOk()
        .click(disableBtn)
        .expect(button.hasAttribute('disabled')).ok();
});

fixture('Form Component Tests')
    .page('https://components.example.com/form');

test('Input validation', async t => {
    const emailInput = Selector('#email-input');
    const errorMessage = Selector('.email-error');
    
    await t
        .typeText(emailInput, 'invalid-email')
        .click('#validate-button')
        .expect(errorMessage.visible).ok()
        .expect(errorMessage.innerText).contains('Invalid email format')
        
        .selectText(emailInput)
        .typeText(emailInput, 'valid@example.com')
        .click('#validate-button')
        .expect(errorMessage.visible).notOk();
});

test('Form submission', async t => {
    await t
        .typeText('#name-input', 'John Doe')
        .typeText('#email-input', 'john@example.com')
        .typeText('#message-input', 'Test message')
        .click('#submit-form')
        .expect(Selector('.success-message').exists).ok()
        .expect(Selector('.success-message').innerText).contains('Form submitted');
});

fixture('Modal Component Tests')
    .page('https://components.example.com/modal');

test('Modal open and close', async t => {
    const modal = Selector('.modal');
    const openBtn = Selector('#open-modal');
    const closeBtn = Selector('.modal-close');
    
    await t
        .expect(modal.visible).notOk()
        .click(openBtn)
        .expect(modal.visible).ok()
        .click(closeBtn)
        .expect(modal.visible).notOk();
});

test('Modal keyboard navigation', async t => {
    const modal = Selector('.modal');
    
    await t
        .click('#open-modal')
        .expect(modal.visible).ok()
        .pressKey('escape')
        .expect(modal.visible).notOk();
});

fixture('Dropdown Component Tests')
    .page('https://components.example.com/dropdown');

test('Dropdown selection', async t => {
    const dropdown = Selector('.dropdown-select');
    const option = Selector('.dropdown-option[data-value="option2"]');
    const selectedValue = Selector('.selected-value');
    
    await t
        .click(dropdown)
        .click(option)
        .expect(selectedValue.innerText).eql('Option 2')
        .expect(dropdown.hasClass('open')).notOk();
});

test('Dropdown search functionality', async t => {
    const searchInput = Selector('.dropdown-search');
    const options = Selector('.dropdown-option');
    
    await t
        .click('.dropdown-select')
        .typeText(searchInput, 'opt')
        .expect(options.count).gte(1)
        .selectText(searchInput)
        .typeText(searchInput, 'xyz')
        .expect(options.count).eql(0);
});

fixture('Data Table Component Tests')
    .page('https://components.example.com/table');

test('Table sorting', async t => {
    const nameHeader = Selector('th[data-sort="name"]');
    const firstRowName = Selector('tbody tr:first-child td:first-child');
    
    await t
        .click(nameHeader)  // Sort ascending
        .expect(nameHeader.hasClass('sort-asc')).ok()
        .click(nameHeader)  // Sort descending
        .expect(nameHeader.hasClass('sort-desc')).ok();
});

test('Table pagination', async t => {
    const rows = Selector('tbody tr');
    const nextBtn = Selector('.pagination-next');
    const pageInfo = Selector('.page-info');
    
    await t
        .expect(rows.count).lte(10)  // Assuming 10 per page
        .expect(pageInfo.innerText).contains('Page 1')
        .click(nextBtn)
        .expect(pageInfo.innerText).contains('Page 2');
});

fixture('Chart Component Tests')
    .page('https://components.example.com/chart');

test('Chart data visualization', async t => {
    const chart = Selector('.chart-container');
    const dataPoints = Selector('.data-point');
    
    await t
        .expect(chart.exists).ok()
        .expect(dataPoints.count).gte(1);
    
    // Test chart interactions
    await t
        .hover(dataPoints.nth(0))
        .expect(Selector('.tooltip').visible).ok()
        .expect(Selector('.tooltip').innerText).contains('Value:');
});

test('Chart legend interactions', async t => {
    const legendItem = Selector('.legend-item:first-child');
    const seriesLine = Selector('.chart-series:first-child');
    
    await t
        .click(legendItem)
        .expect(seriesLine.hasClass('hidden')).ok()
        .click(legendItem)
        .expect(seriesLine.hasClass('hidden')).notOk();
});

// Custom component with complex state
fixture('State Management Component Tests')
    .page('https://components.example.com/counter');

test('Counter state synchronization', async t => {
    const counter1 = Selector('#counter-1 .count');
    const counter2 = Selector('#counter-2 .count');
    const incrementBtn = Selector('#counter-1 .increment');
    const decrementBtn = Selector('#counter-2 .decrement');
    
    await t
        .expect(counter1.innerText).eql('0')
        .expect(counter2.innerText).eql('0')
        .click(incrementBtn)
        .expect(counter1.innerText).eql('1')
        .expect(counter2.innerText).eql('1')  // Should sync
        .click(decrementBtn)
        .expect(counter1.innerText).eql('0')
        .expect(counter2.innerText).eql('0'); // Should sync
});

fixture('Animation Component Tests')
    .page('https://components.example.com/animation');

test('CSS animation completion', async t => {
    const animatedElement = Selector('.animated-element');
    const triggerBtn = Selector('#trigger-animation');
    
    await t
        .click(triggerBtn)
        .expect(animatedElement.hasClass('animating')).ok();
    
    // Wait for animation to complete
    await t
        .wait(2000)
        .expect(animatedElement.hasClass('animation-complete')).ok();
});

test('Animation performance', async t => {
    const performanceCheck = ClientFunction(() => {
        return new Promise(resolve => {
            const startTime = performance.now();
            
            // Trigger animation
            document.querySelector('#performance-animation').classList.add('animate');
            
            requestAnimationFrame(() => {
                const endTime = performance.now();
                resolve(endTime - startTime < 16); // Should complete in one frame
            });
        });
    });
    
    const isPerformant = await performanceCheck();
    await t.expect(isPerformant).ok('Animation should be performant');
});