import { Selector } from 'testcafe';

fixture('Demo Fixture 1')
    .page('https://testcafe.io/');

test('Check page title', async t => {
    const title = await Selector('title').innerText;
    await t.expect(title).contains('TestCafe');
});

test('Check navigation links', async t => {
    const navLink = Selector('nav a').withText('Getting Started');
    await t.expect(navLink.exists).ok();
});

fixture('Demo Fixture 2')
    .page('https://www.google.com');

test('Google search exists', async t => {
    const searchBox = Selector('input[name="q"]');
    await t.expect(searchBox.exists).ok();
});

test('Google logo exists', async t => {
    const logo = Selector('img[alt*="Google"]');
    await t.expect(logo.exists).ok();
});