// Edge cases and unusual formatting patterns for TestCafe parser testing

import { Selector } from 'testcafe';

// Standard fixture
fixture('Edge Case Tests').page('https://example.com');

// Fixture with no space before parentheses
fixture('NoSpaceFixture').page('https://nospace.com');

// Fixture with extra spaces
fixture   (   'Extra Spaces Fixture'   )   .   page   (   'https://spaces.com'   );

// Fixture with tabs
fixture	('Tab Fixture')	.	page	('https://tabs.com');

// Multiline fixture definition
fixture(
    'Multiline Fixture'
).page(
    'https://multiline.com'
);

// Fixture with line breaks in different positions
fixture('Line Break Fixture')
    .page(
        'https://linebreak.com'
    );

// Chained methods on separate lines
fixture('Chained Methods')
    .page('https://chained.com')
    .beforeEach(async t => {
        console.log('Before each');
    })
    .afterEach(async t => {
        console.log('After each');
    })
    .meta({
        category: 'edge-case'
    });

// Fixture with very long name
fixture('This is a very long fixture name that might cause issues with parsing or display in IDE because it exceeds normal length expectations')
    .page('https://longname.com');

// Fixture name with escaped quotes
fixture('Fixture with "quoted" strings and \'apostrophes\'')
    .page('https://quotes.com');

// Fixture with regex-like strings
fixture('Regex patterns: /test/gi and [a-z]+ patterns')
    .page('https://regex.com');

// Nested fixture definitions (unusual but possible)
if (process.env.NODE_ENV === 'test') {
    fixture('Conditional Fixture')
        .page('https://conditional.com');
    
    test('Conditional test', async t => {
        console.log('Running conditional test');
    });
}

// Fixture defined using variables
const fixtureName = 'Variable Fixture';
const pageUrl = 'https://variable.com';

fixture(fixtureName)
    .page(pageUrl);

// Dynamic fixture creation
['chrome', 'firefox', 'safari'].forEach(browser => {
    fixture(`Dynamic ${browser} Tests`)
        .page(`https://${browser}.example.com`);
    
    test(`Test for ${browser}`, async t => {
        console.log(`Running test for ${browser}`);
    });
});

// Fixture with template literals and expressions
const environment = 'staging';
fixture`Environment Tests - ${environment}`
    .page`https://${environment}.example.com/test`;

test`Test for ${environment} environment`, async t => {
    console.log(`Testing ${environment}`);
}

// Mixed quote styles
fixture("Double Quote Fixture").page('https://mixed.com');
fixture('Single Quote Fixture').page("https://mixed2.com");
fixture`Template Literal Fixture`.page`https://template.com`;

// Fixture with IIFE
(function() {
    fixture('IIFE Fixture')
        .page('https://iife.com');
    
    test('IIFE test', async t => {
        console.log('IIFE test');
    });
})();

// Arrow function fixture (unusual pattern)
const createFixture = () => {
    fixture('Arrow Function Fixture')
        .page('https://arrow.com');
};
createFixture();

// Fixture with comments inline
fixture('Fixture with /* inline comment */ name')
    .page('https://comment.com');

// Multiple fixtures with same name (should be avoided but possible)
fixture('Duplicate Name')
    .page('https://duplicate1.com');

fixture('Duplicate Name')  // Second one with same name
    .page('https://duplicate2.com');

// Fixture with Unicode characters
fixture('Тест с русскими буквами 🚀')
    .page('https://unicode.com');

fixture('测试中文 والعربية')
    .page('https://multilang.com');

// Test definitions with various patterns

// Standard test
test('Standard test', async t => {
    console.log('Standard test');
});

// Test with no space
test('NoSpaceTest', async t=>{
    console.log('No space test');
});

// Multiline test
test(
    'Multiline test name',
    async t => {
        console.log('Multiline test');
    }
);

// Test with long name
test('This is an extremely long test name that might cause display or parsing issues in the IDE due to its excessive length and verbosity', async t => {
    console.log('Long name test');
});

// Test with special characters
test('Test with "quotes" and \'apostrophes\' & symbols!', async t => {
    console.log('Special chars test');
});

// Test with template literal
test`Template literal test with ${environment} variable`, async t => {
    console.log('Template test');
};

// Test with unicode
test('Unicode test: 🧪 测试 テスト', async t => {
    console.log('Unicode test');
});

// Async arrow function test (alternative syntax)
const myTest = test('Arrow function test', async (t) => {
    console.log('Arrow function test');
});

// Test with destructured parameter (uncommon)
test('Destructured test', async ({ click, typeText }) => {
    // This would cause error but tests parser robustness
    console.log('Destructured test');
});

// Nested test (should not work but tests parser)
if (true) {
    test('Nested test', async t => {
        console.log('Nested test');
    });
}

// Test with try-catch
test('Error handling test', async t => {
    try {
        await t.click('#non-existent');
    } catch (error) {
        console.log('Caught error');
    }
});

// Multiple tests on one line (bad practice but possible)
test('Test 1', async t => { console.log('Test 1'); }); test('Test 2', async t => { console.log('Test 2'); });

// Test with very complex async function
test('Complex async test', async function(t) {
    const result = await (async () => {
        return new Promise(resolve => {
            setTimeout(() => resolve('done'), 100);
        });
    })();
    console.log(result);
});

// Test with default parameters
test('Default params test', async (t = {}) => {
    console.log('Default params test');
});

// Test with JSDoc comments
/**
 * This is a test with JSDoc documentation
 * @param {TestController} t - The test controller
 */
test('JSDoc test', async t => {
    console.log('JSDoc test');
});

// Empty test
test('Empty test', async t => {
    // Empty test body
});

// Test with only comment
test('Comment only test', async t => {
    // TODO: Implement this test
});

// Single line test with semicolon variations
test('Semicolon test', async t => { console.log('test'); });
test('No semicolon test', async t => { console.log('test') })

// Test followed by another fixture
test('Last test before new fixture', async t => {
    console.log('Before new fixture');
});

fixture('Final Edge Case Fixture')
    .page('https://final.com');

test('Final test', async t => {
    console.log('Final test');
});