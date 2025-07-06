# Store your JWT for easy reuse
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJSRVNUQVVSQU5UX09XTkVSIl0sInVzZXJJZCI6IjAwMGUyMTFjLTgzMGYtNDA0My1iNTJlLTU3NTVmYzVhNmZhOSIsImVtYWlsIjoia290aUBleGFtcG92ZS5jb20iLCJ1c2VybmFtZSI6ImtvZGFieXRlIiwic3ViIjoia29kYWJ5dGUiLCJpYXQiOjE3NTE3NDEzNzYsImV4cCI6MTc1MTc0NDk3Nn0.hrbg8X_uw_OKD77-JkGGe1I7-tSb70Cjr8YQdiJ00CY"
export AUTHENTICATED_USER_ID="000e211c-830f-4043-b52e-5755fc5a6fa9"
export AUTHENTICATED_ROLES="ROLE_RESTAURANT_OWNER" # As per your JWT payload
export API_GATEWAY_URL="http://localhost:8081"

# --- Test Case 1: Create an Order (Requires ROLE_CUSTOMER) ---
# Expected: This will likely fail with 403 Forbidden because the JWT user is ROLE_RESTAURANT_OWNER, not ROLE_CUSTOMER.
# The @PreAuthorize("hasRole('ROLE_CUSTOMER')") on @PostMapping should block this.
# If it passes @PreAuthorize, the service layer's business rule "You can only create an order for yourself"
# will still require the customerId in the body to match the authenticatedUserId.
echo "--- Attempting to Create Order (as RESTAURANT_OWNER for a random customer) ---"
curl -X POST "${API_GATEWAY_URL}/api/orders" \
     -H "Authorization: Bearer ${JWT_TOKEN}" \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": "8f9f0101-1111-2222-3333-444455556666",
           "restaurantId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
           "totalAmount": 25.50,
           "items": [
             { "menuItemId": "c1d2e3f4-a5b6-7890-1234-567890abcdef", "quantity": 2, "pricePerUnit": 10.00 },
             { "menuItemId": "d1e2f3a4-b5c6-7890-1234-567890abcdef", "quantity": 1, "pricePerUnit": 5.50 }
           ]
         }' -v


# To successfully create an order, you would need a JWT with ROLE_CUSTOMER
# And the customerId in the body should be the userId from that JWT.
# Example (if you had a CUSTOMER JWT and Customer ID from it):
# export CUSTOMER_JWT="<your_customer_jwt_token>"
# export CUSTOMER_ID="<your_customer_id_from_jwt>"
# curl -X POST "${API_GATEWAY_URL}/api/orders" \
#      -H "Authorization: Bearer ${CUSTOMER_JWT}" \
#      -H "Content-Type: application/json" \
#      -d "{
#            \"customerId\": \"${CUSTOMER_ID}\",
#            \"restaurantId\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\",
#            \"totalAmount\": 25.50,
#            \"items\": [
#              { \"menuItemId\": \"c1d2e3f4-a5b6-7890-1234-567890abcdef\", \"quantity\": 2, \"pricePerUnit\": 10.00 },
#              { \"menuItemId\": \"d1e2f3a4-b5c6-7890-1234-567890abcdef\", \"quantity\": 1, \"pricePerUnit\": 5.50 }
#            ]
#          }" -v

# --- Test Case 2: Get All Orders for a Specific Restaurant (Requires ROLE_RESTAURANT_OWNER or ROLE_ADMIN) ---
# The provided JWT is ROLE_RESTAURANT_OWNER, so this should pass @PreAuthorize.
# The service layer does not do further ownership checks for now, but in a real app,
# you'd check if AUTHENTICATED_USER_ID owns the RESTAURANT_ID.
echo -e "\n--- Get Orders by Restaurant ID (as RESTAURANT_OWNER) ---"
# Use a placeholder restaurant ID. In a real scenario, this would be a valid ID from your system.
export EXAMPLE_RESTAURANT_ID="b1c2d3e4-f5a6-7890-1234-567890abcdef"
curl -X GET "${API_GATEWAY_URL}/api/orders/restaurant/${EXAMPLE_RESTAURANT_ID}" \
     -H "Authorization: Bearer ${JWT_TOKEN}" -v

# --- Test Case 3: Update Order Status (Requires ROLE_RESTAURANT_OWNER, ROLE_ADMIN, or ROLE_CUSTOMER) ---
# As RESTAURANT_OWNER, you can update status. Try to confirm a PENDING order.
# You'll need an existing order ID here. Let's assume you've created one (maybe manually in DB for now).
# If you don't have one, this will return 404 or an error.
echo -e "\n--- Update Order Status (as RESTAURANT_OWNER) ---"
export EXISTING_ORDER_ID="<REPLACE_WITH_AN_EXISTING_ORDER_UUID>" # IMPORTANT: Replace this with a UUID from your database
if [ "$EXISTING_ORDER_ID" != "<REPLACE_WITH_AN_EXISTING_ORDER_UUID>" ]; then
    curl -X PATCH "${API_GATEWAY_URL}/api/orders/${EXISTING_ORDER_ID}/status?status=CONFIRMED" \
         -H "Authorization: Bearer ${JWT_TOKEN}" -v
else
    echo "Skipping updateOrderStatus test: Please provide an EXISTING_ORDER_ID."
fi


# --- Test Case 4: Get Order by ID (Requires ROLE_CUSTOMER, ROLE_RESTAURANT_OWNER, or ROLE_ADMIN) ---
# As RESTAURANT_OWNER, this should pass @PreAuthorize. The service layer doesn't perform role-specific checks beyond that for now.
echo -e "\n--- Get Order by ID (as RESTAURANT_OWNER) ---"
if [ "$EXISTING_ORDER_ID" != "<REPLACE_WITH_AN_EXISTING_ORDER_UUID>" ]; then
    curl -X GET "${API_GATEWAY_URL}/api/orders/${EXISTING_ORDER_ID}" \
         -H "Authorization: Bearer ${JWT_TOKEN}" -v
else
    echo "Skipping getOrderById test: Please provide an EXISTING_ORDER_ID."
fi

# --- Test Case 5: Get Orders by Customer ID (Requires ROLE_CUSTOMER or ROLE_ADMIN) ---
# Expected: This will likely fail with 403 Forbidden because the JWT user is ROLE_RESTAURANT_OWNER, not ROLE_CUSTOMER or ROLE_ADMIN.
# The @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')") on @GetMapping("/customer/{customerId}") should block this.
echo -e "\n--- Get Orders by Customer ID (as RESTAURANT_OWNER for a random customer) ---"
export EXAMPLE_CUSTOMER_ID="8f9f0101-1111-2222-3333-444455556666" # Random customer ID
curl -X GET "${API_GATEWAY_URL}/api/orders/customer/${EXAMPLE_CUSTOMER_ID}" \
     -H "Authorization: Bearer ${JWT_TOKEN}" -v
