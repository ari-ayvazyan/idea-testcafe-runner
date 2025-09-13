import { Selector } from 'testcafe';

fixture('Sample Test')
    .page('https://example.com');

test('Simple test with console log', async t => {
    console.log('This message belongs to Simple test 1!');
});

test('Simple test 2 with console log', async t => {
    console.log('This message belongs to Simple test 2!');
});

test('Simple test 3 with err', async t => {
  console.log('This message belongs to Simple test 3!');
  t.expect(false).ok()
});

test('Simple test 4 with err', async t => {
  console.log('This message belongs to Simple test 4!');
  await t.expect(true).ok()
});

test('Simple test 5 with err', async t => {
  console.log('This message belongs to Simple test 5!');
  await t.expect(false).ok()
});
