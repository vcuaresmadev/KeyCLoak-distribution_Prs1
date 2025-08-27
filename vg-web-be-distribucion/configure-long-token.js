const https = require('https');
const http = require('http');

// Disable SSL verification for local development
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

// Step 1: Get admin token
function getAdminToken() {
  return new Promise((resolve, reject) => {
    const postData = 'grant_type=password&client_id=admin-cli&username=admin&password=admin';
    
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: '/realms/master/protocol/openid-connect/token',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(postData)
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          if (response.access_token) {
            resolve(response.access_token);
          } else {
            reject(new Error('Failed to get admin token: ' + JSON.stringify(response)));
          }
        } catch (error) {
          reject(new Error('Failed to parse admin token response: ' + error.message));
        }
      });
    });

    req.on('error', reject);
    req.write(postData);
    req.end();
  });
}

// Step 2: Configure backend client for long-lived tokens
function configureBackendClient(adminToken) {
  return new Promise((resolve, reject) => {
    const clientConfig = {
      "id": "ffb01f71-2dd9-4f67-af84-78e302711f7c",
      "clientId": "backend",
      "enabled": true,
      "clientAuthenticatorType": "client-secret",
      "secret": "your-secret-here",
      "redirectUris": [],
      "webOrigins": ["*"],
      "standardFlowEnabled": false,
      "implicitFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": true,
      "publicClient": false,
      "protocol": "openid-connect",
      "attributes": {
        "access.token.lifespan": "10368000" // 4 months in seconds (120 days * 24 * 60 * 60)
      },
      "fullScopeAllowed": true,
      "defaultClientScopes": ["web-origins", "acr", "profile", "roles", "email"],
      "optionalClientScopes": ["address", "phone", "offline_access", "microprofile-jwt"]
    };

    const options = {
      hostname: 'localhost',
      port: 8080,
      path: '/admin/realms/master/clients/ffb01f71-2dd9-4f67-af84-78e302711f7c',
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ' + adminToken,
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(JSON.stringify(clientConfig))
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        if (res.statusCode === 204) {
          resolve();
        } else {
          reject(new Error(`Failed to configure client: ${res.statusCode} - ${data}`));
        }
      });
    });

    req.on('error', reject);
    req.write(JSON.stringify(clientConfig));
    req.end();
  });
}

// Step 3: Generate long-lived token
function generateLongToken() {
  return new Promise((resolve, reject) => {
    const postData = 'grant_type=client_credentials&client_id=backend&client_secret=your-secret-here';
    
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: '/realms/master/protocol/openid-connect/token',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(postData)
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          if (response.access_token) {
            resolve(response.access_token);
          } else {
            reject(new Error('Failed to generate token: ' + JSON.stringify(response)));
          }
        } catch (error) {
          reject(new Error('Failed to parse token response: ' + error.message));
        }
      });
    });

    req.on('error', reject);
    req.write(postData);
    req.end();
  });
}

// Main execution
async function main() {
  try {
    console.log('🔧 Configurando cliente backend para tokens de 4 meses...');
    
    const adminToken = await getAdminToken();
    console.log('✅ Token de administrador obtenido');
    
    await configureBackendClient(adminToken);
    console.log('✅ Cliente backend configurado para tokens de 4 meses');
    
    // Wait a moment for changes to take effect
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    const longToken = await generateLongToken();
    console.log('\n🎉 Token de 4 meses generado exitosamente!');
    console.log('\n📋 Copia este token para usar en Angular:');
    console.log('Bearer ' + longToken);
    
    // Decode token to show expiration
    const tokenParts = longToken.split('.');
    if (tokenParts.length === 3) {
      const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64').toString());
      const expirationDate = new Date(payload.exp * 1000);
      console.log(`\n📅 Token expira: ${expirationDate.toLocaleString()}`);
    }
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

main();
