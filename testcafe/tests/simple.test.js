import { Selector } from 'testcafe';

fixture('Sample Test')
    .page('https://example.com');

test('Simple test with console log', async t => {
    console.log('Hello from TestCafe!');
});

test('Simple test 2 with console log', async t => {
    console.log('Hello from TestCafe!');
});
