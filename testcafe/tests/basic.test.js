import { Selector } from 'testcafe';

fixture('Demo Fixture')
    .page('https://example.com');

test('First test', async t => {
    await t.expect(Selector('body').exists).ok();
});

test('Second test', async t => {
    await t.expect(Selector('h1').exists).ok();
});

fixture('Another Fixture')
    .page('https://example.org');

test('Test in another fixture', async t => {
    await t.expect(Selector('body').exists).ok();
});