#!/bin/bash

echo "🔧 Generando token para Angular..."

# Generate token from inside the backend container
TOKEN=$(docker exec vg-web-be-distribucion-backend-1 curl -s -X POST http://keycloak:8080/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=backend&client_secret=your-secret-here" | jq -r '.access_token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo "✅ Token generado exitosamente!"
    echo ""
    echo "📋 Copia este token para usar en Angular:"
    echo "Bearer $TOKEN"
    echo ""
    echo "🔗 Token completo para el header Authorization:"
    echo "Authorization: Bearer $TOKEN"
    echo ""
    
    # Decode token to show expiration
    PAYLOAD=$(echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null)
    if [ -n "$PAYLOAD" ]; then
        EXP=$(echo $PAYLOAD | jq -r '.exp')
        if [ "$EXP" != "null" ]; then
            EXP_DATE=$(date -d @$EXP)
            echo "📅 Token expira: $EXP_DATE"
        fi
        
        ISS=$(echo $PAYLOAD | jq -r '.iss')
        echo "🔗 Issuer: $ISS"
    fi
else
    echo "❌ Error al generar el token"
    exit 1
fi
