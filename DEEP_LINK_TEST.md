# Deep Link Testing Guide

## Test Deep Links

You can test the deep link implementation using ADB commands:

### Test Order Screen Deep Link (Custom Scheme)
```bash
adb shell am start -W -a android.intent.action.VIEW -d "swadratna://order?tableNumber=1&orderId=123" com.swadratna.swadratna_staff
```

### Test Order Screen Deep Link (HTTPS)
```bash
adb shell am start -W -a android.intent.action.VIEW -d "https://swadratna.com/order?tableNumber=1&orderId=123" com.swadratna.swadratna_staff
```

### Test Notification Deep Link
The notification service will automatically include deep links for order notifications. When you receive a notification with:
- `notification_type`: "new_order" or "payment_completed"
- `order_id`: valid order ID
- `table_number`: valid table number

The notification will include a deep link that opens the order screen directly.

## Deep Link Parameters

- `tableNumber`: The table number (integer)
- `orderId`: The order ID (integer)

## How It Works

1. **Deep Link Intent Filter**: Added to MainActivity in AndroidManifest.xml
2. **Intent Handling**: MainActivity handles both notification intents and deep link intents
3. **Navigation**: Deep link data is stored in SharedPreferences and used after login
4. **Order Screen**: NavigationComponent navigates to OrderTakingScreen with the provided parameters

## Example Deep Link URLs

- `swadratna://order?tableNumber=5&orderId=456`
- `https://swadratna.com/order?tableNumber=12&orderId=789`