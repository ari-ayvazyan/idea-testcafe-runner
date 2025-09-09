// TypeScript test file with API integration patterns
import { Selector, RequestLogger, RequestMock, ClientFunction } from 'testcafe';

// API request logging setup
const apiLogger = RequestLogger(/https:\/\/api\.example\.com\/.*/, {
    logRequestHeaders: true,
    logResponseHeaders: true,
    logRequestBody: true,
    logResponseBody: true
});

// Mock API responses
const userMock = RequestMock()
    .onRequestTo('https://api.example.com/users')
    .respond([
        { id: 1, name: 'John Doe', email: 'john@example.com' },
        { id: 2, name: 'Jane Smith', email: 'jane@example.com' }
    ], 200, {
        'content-type': 'application/json',
        'access-control-allow-origin': '*'
    });

const errorMock = RequestMock()
    .onRequestTo('https://api.example.com/error')
    .respond(null, 500, {
        'content-type': 'application/json'
    });

// Fixture with request hooks
fixture('API Integration Tests')
    .page('https://frontend.example.com')
    .requestHooks(apiLogger, userMock)
    .meta({
        type: 'api-integration',
        tags: ['api', 'mock', 'typescript']
    });

interface ApiResponse {
    data: any[];
    status: number;
    message: string;
}

test('Fetch and display user data', async (t: TestController) => {
    // Trigger API call
    await t.click('#load-users-btn');
    
    // Wait for data to load
    await t.wait(1000);
    
    // Verify UI shows the mocked data
    await t
        .expect(Selector('.user-item').count).eql(2)
        .expect(Selector('.user-item').nth(0).innerText).contains('John Doe')
        .expect(Selector('.user-item').nth(1).innerText).contains('Jane Smith');
    
    // Check that API was called
    await t.expect(apiLogger.count(record => 
        record.request.url.includes('/users')
    )).eql(1);
});

test('API request headers validation', async t => {
    await t.click('#authenticated-request-btn');
    
    await t.wait(500);
    
    // Validate request headers
    const authRequest = apiLogger.requests.find(req => 
        req.request.url.includes('/protected')
    );
    
    if (authRequest) {
        console.log('Authorization header:', authRequest.request.headers.authorization);
        await t.expect(authRequest.request.headers.authorization).ok();
    }
});

// Fixture with error handling
fixture('API Error Handling')
    .page('https://frontend.example.com/error-test')
    .requestHooks(errorMock);

test('Handle API server errors gracefully', async t => {
    await t
        .click('#trigger-error-btn')
        .wait(1000)
        .expect(Selector('.error-message').exists).ok()
        .expect(Selector('.error-message').innerText).contains('Server error');
});

// Real API testing (without mocks)
fixture('Live API Tests')
    .page('https://frontend.example.com/live')
    .requestHooks(apiLogger);

test('Test against real API endpoint', async t => {
    const makeApiCall = ClientFunction(async () => {
        try {
            const response = await fetch('https://jsonplaceholder.typicode.com/posts/1');
            const data = await response.json();
            return { success: true, data };
        } catch (error) {
            return { success: false, error: error.message };
        }
    });
    
    const result = await makeApiCall();
    
    await t
        .expect(result.success).ok('API call should succeed')
        .expect(result.data.id).eql(1)
        .expect(result.data.title).ok();
});

// GraphQL API testing
const graphqlMock = RequestMock()
    .onRequestTo('https://api.example.com/graphql')
    .respond({
        data: {
            users: [
                { id: '1', name: 'Alice', posts: [{ title: 'Hello World' }] },
                { id: '2', name: 'Bob', posts: [{ title: 'GraphQL Rocks' }] }
            ]
        }
    }, 200, {
        'content-type': 'application/json'
    });

fixture('GraphQL API Tests')
    .page('https://frontend.example.com/graphql')
    .requestHooks(graphqlMock);

test('GraphQL query with nested data', async t => {
    const executeGraphQL = ClientFunction(async () => {
        const query = `
            query GetUsersWithPosts {
                users {
                    id
                    name
                    posts {
                        title
                    }
                }
            }
        `;
        
        const response = await fetch('https://api.example.com/graphql', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query })
        });
        
        return await response.json();
    });
    
    const result = await executeGraphQL();
    
    await t
        .expect(result.data.users.length).eql(2)
        .expect(result.data.users[0].name).eql('Alice')
        .expect(result.data.users[0].posts[0].title).eql('Hello World');
});

// File upload testing with API
fixture('File Upload API Tests')
    .page('https://frontend.example.com/upload');

test('Upload file via API', async t => {
    const fileContent = 'Test file content for upload';
    
    const uploadFile = ClientFunction((content: string) => {
        const blob = new Blob([content], { type: 'text/plain' });
        const formData = new FormData();
        formData.append('file', blob, 'test.txt');
        
        return fetch('https://api.example.com/upload', {
            method: 'POST',
            body: formData
        }).then(response => response.json());
    });
    
    // Simulate file selection and upload
    await t.setFilesToUpload('#file-input', './test-files/sample.txt');
    
    const uploadResult = await uploadFile(fileContent);
    console.log('Upload result:', uploadResult);
    
    await t
        .click('#upload-btn')
        .expect(Selector('.upload-success').exists).ok();
});

// Pagination API testing
fixture('Pagination API Tests')
    .page('https://frontend.example.com/pagination');

test('Navigate through paginated API results', async t => {
    // Load first page
    await t
        .click('#load-page-1')
        .wait(500)
        .expect(Selector('.item').count).eql(10);
    
    // Load second page
    await t
        .click('#load-page-2')
        .wait(500)
        .expect(Selector('.item').count).eql(10)
        .expect(Selector('.page-info').innerText).contains('Page 2');
    
    // Verify API calls were made
    const pageRequests = apiLogger.requests.filter(req => 
        req.request.url.includes('page=')
    );
    
    console.log(`Made ${pageRequests.length} pagination requests`);
    await t.expect(pageRequests.length).gte(2);
});

// Authentication API flow
fixture('Authentication API Flow')
    .page('https://frontend.example.com/auth')
    .beforeEach(async t => {
        // Clear any existing tokens
        const clearAuth = ClientFunction(() => {
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('authToken');
        });
        await clearAuth();
    });

test('Complete authentication flow', async t => {
    // Login
    await t
        .typeText('#login-email', 'test@example.com')
        .typeText('#login-password', 'password123')
        .click('#login-submit');
    
    // Wait for token to be stored
    await t.wait(1000);
    
    // Verify token exists
    const getToken = ClientFunction(() => localStorage.getItem('authToken'));
    const token = await getToken();
    
    await t.expect(token).ok('Auth token should be stored');
    
    // Make authenticated request
    await t
        .click('#profile-button')
        .wait(500)
        .expect(Selector('.user-profile').exists).ok();
    
    // Logout
    await t
        .click('#logout-button')
        .wait(500);
    
    // Verify token is cleared
    const tokenAfterLogout = await getToken();
    await t.expect(tokenAfterLogout).notOk('Auth token should be cleared after logout');
});