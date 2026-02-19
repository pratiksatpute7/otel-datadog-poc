#!/bin/bash

# Test API Requests for OpenTelemetry POC
# Run these commands to test the CRUD endpoints

BASE_URL="http://localhost:8080/api/v1/products"

echo "========================================"
echo "OpenTelemetry POC - API Test Suite"
echo "========================================"

# 1. Get all products
echo ""
echo "1. GET all products"
echo "   curl $BASE_URL"
curl -s "$BASE_URL" | python3 -m json.tool || curl -s "$BASE_URL"

# 2. Get specific product
echo ""
echo ""
echo "2. GET product by ID (id=1)"
echo "   curl $BASE_URL/1"
curl -s "$BASE_URL/1" | python3 -m json.tool || curl -s "$BASE_URL/1"

# 3. Create new product
echo ""
echo ""
echo "3. POST create new product"
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "price": 149.99,
    "description": "RGB mechanical keyboard with Cherry MX switches"
  }')
echo "   Request:"
echo '   {"name":"Mechanical Keyboard","price":149.99,"description":"RGB mechanical keyboard with Cherry MX switches"}'
echo "   Response:"
echo "$CREATE_RESPONSE" | python3 -m json.tool || echo "$CREATE_RESPONSE"

# Extract ID from response for update test
NEW_ID=$(echo "$CREATE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', 999))" 2>/dev/null || echo "4")

# 4. Update product
echo ""
echo ""
echo "4. PUT update product (id=$NEW_ID)"
echo "   Request:"
echo '   {"name":"Updated Mechanical Keyboard","price":159.99,"description":"Updated with wireless option"}'
curl -s -X PUT "$BASE_URL/$NEW_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Mechanical Keyboard",
    "price": 159.99,
    "description": "Updated with wireless option"
  }' | python3 -m json.tool || curl -s -X PUT "$BASE_URL/$NEW_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Mechanical Keyboard",
    "price": 159.99,
    "description": "Updated with wireless option"
  }'

# 5. Delete product
echo ""
echo ""
echo "5. DELETE product (id=$NEW_ID)"
echo "   curl -X DELETE $BASE_URL/$NEW_ID"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/$NEW_ID")
echo "   Response: HTTP $HTTP_CODE"

# 6. Verify deletion
echo ""
echo ""
echo "6. GET all products (after deletion)"
echo "   curl $BASE_URL"
curl -s "$BASE_URL" | python3 -m json.tool || curl -s "$BASE_URL"

echo ""
echo ""
echo "========================================"
echo "✓ API test suite completed"
echo "========================================"
