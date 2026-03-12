

# 1. Auth/Profile

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/change-password`
- `GET /api/users/me`
- `PUT /api/users/me`
- `GET /api/users/me/addresses`
- `POST /api/users/me/addresses`
- `PUT /api/users/me/addresses/{id}`
- `DELETE /api/users/me/addresses/{id}`

# 2. Customer catalog/search/cart/order

- `GET /api/catalog/restaurants/nearby?lat=&lng=&keyword=`
- `GET /api/catalog/products/search?q=&context=`
- `GET /api/catalog/restaurants/{id}/products`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`
- `POST /api/vouchers/validate`
- `POST /api/orders/checkout`
- `GET /api/orders/my`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/cancel`
- `POST /api/orders/{id}/review`
- `GET /api/orders/{id}/review`

# 3. Merchant

- `POST /api/merchant/register`
- `GET /api/merchant/profile`
- `PUT /api/merchant/profile`
- `GET /api/merchant/categories`
- `POST /api/merchant/categories`
- `PUT /api/merchant/categories/{id}`
- `DELETE /api/merchant/categories/{id}`
- `GET /api/merchant/products`
- `POST /api/merchant/products`
- `PUT /api/merchant/products/{id}`
- `PATCH /api/merchant/products/{id}/availability`
- `GET /api/merchant/orders/new`
- `PATCH /api/merchant/orders/{id}/confirm`
- `PATCH /api/merchant/orders/{id}/preparing`
- `PATCH /api/merchant/orders/{id}/ready`
- `GET /api/merchant/reviews?from=&to=&rating=`
- `GET /api/merchant/reports/revenue?from=&to=`
- `POST /api/merchant/payout-requests`

# 4. Shipper

- `POST /api/shipper/register`
- `GET /api/shipper/orders/available`
- `PATCH /api/shipper/orders/{id}/accept`
- `PATCH /api/shipper/orders/{id}/picked-up`
- `PATCH /api/shipper/orders/{id}/delivered`
- `GET /api/shipper/orders/history`
- `GET /api/shipper/income?from=&to=`
- `POST /api/shipper/payout-requests`

# 5. Payment

- `POST /api/payments/vnpay/create-url`
- `GET /api/payments/vnpay/callback`
- `GET /api/payments/orders/{orderId}/status`

# 6. Admin

- `GET /api/admin/merchants/pending`
- `PATCH /api/admin/merchants/{id}/approve`
- `PATCH /api/admin/merchants/{id}/reject`
- `GET /api/admin/shippers/pending`
- `PATCH /api/admin/shippers/{id}/approve`
- `PATCH /api/admin/shippers/{id}/reject`
- `GET /api/admin/vouchers`
- `POST /api/admin/vouchers`
- `PUT /api/admin/vouchers/{id}`
- `DELETE /api/admin/vouchers/{id}`
- `GET /api/admin/shipping-configs`
- `POST /api/admin/shipping-configs`
- `PUT /api/admin/shipping-configs/{id}`
- `GET /api/admin/payout-requests`
- `PATCH /api/admin/payout-requests/{id}/approve`
- `PATCH /api/admin/payout-requests/{id}/reject`
- `GET /api/admin/reports/overview?from=&to=`

# 7. API standards

- Auth header: `Authorization: Bearer <token>`
- Response success:

```json
{
  "success": true,
  "data": {},
  "message": "OK"
}
```

- Response error:

```json
{
  "success": false,
  "errorCode": "ORDER_INVALID_STATE",
  "message": "Order cannot transition to requested state"
}
```

- Bắt buộc idempotency cho callback payment và các action có nguy cơ click lặp.
