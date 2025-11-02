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

## In-App Notifications (New Feature)

When the app is already open and running in the foreground, notifications will now appear as in-app notification cards at the top of the screen instead of system notifications. Multiple notifications are queued and shown sequentially. This provides a better user experience with:

### Features:
- **Non-intrusive**: Appears as a card in the center of the current screen
- **Auto-dismiss**: Automatically disappears after 5 seconds
- **Manual dismiss**: Can be dismissed by tapping the X button
- **Action button**: "Show Details" button for order-related notifications
- **Visual feedback**: Different colors for different notification types

### Supported Notification Types:
- **New Order**: Green card with "View Order" button
- **Payment Completed**: Blue card with "View Order" button  
- **Order Ready**: Orange card (no action button)
- **General**: Primary color card (no action button)

### Behavior:
- **App in Background**: Uses deep link navigation (opens specific screen)
- **App in Foreground**: Shows in-app notification card on current screen (NO system notification built)
-### Click Action**: "Show Details" button uses the deep link provided by the API, falling back to manual route construction if no deep link is provided
- **No System Notification**: When app is in foreground, no system notification is created - only the in-app card appears
- **Multiple Notifications**: When multiple notifications arrive while one is already displayed, they are queued and shown sequentially after the current notification is dismissed

## Deep Link Parameters

- `tableNumber`: The table number (integer)
- `orderId`: The order ID (integer)

## Deep Link Usage in Notifications

### API Deep Link Priority
The system prioritizes using the deep link provided by the API over manually constructed routes:

1. **API Provided Deep Link**: If the notification payload includes a `deep_link` field, it will be used for navigation
2. **Fallback Construction**: If no deep link is provided, the system constructs a route using `tableNumber` and `orderId`

### Benefits of Using API Deep Links
- **Flexibility**: API can specify any valid deep link, not just order-related ones
- **Consistency**: Same deep link works for both system notifications and in-app notifications
- **Future-proofing**: API can change navigation behavior without app updates
- **Complex Navigation**: Supports navigation to screens that require more than just table/order parameters

### Implementation Details
- **Firebase Service**: Passes the API's `deep_link` as an extra when sending intent to MainActivity
- **MainActivity**: Extracts and passes the deep link to the notification manager
- **NavigationComponent**: Parses the deep link URI to extract parameters, then constructs the appropriate navigation route
- **Error Handling**: If deep link parsing fails, falls back to manual route construction using orderId and tableNumber

### Deep Link Parsing
The system now properly parses deep link URIs like `swadratna://order?tableNumber=1000059&orderId=1000022` to extract the query parameters and use them for navigation, rather than trying to navigate directly with the URI string.

## How It Works

### Technical Implementation:
1. **Firebase Messaging Service**: Checks if app is in foreground using ProcessLifecycleOwner
2. **Foreground Handling**: If app is in foreground, sends intent to MainActivity with `in_app_notification=true` flag
3. **No System Notification**: When app is in foreground, NO system notification is created at all
4. **Background Handling**: If app is in background, creates normal system notification with deep link
5. **MainActivity Processing**: Handles the in-app notification intent and shows the card directly
6. **Deep Link Intent Filter**: Added to MainActivity in AndroidManifest.xml
7. **Intent Handling**: MainActivity handles both notification intents and deep link intents
8. **Navigation**: Deep link data is stored in SharedPreferences and used after login
9. **Order Screen**: NavigationComponent navigates to OrderTakingScreen with the provided parameters

## Example Deep Link URLs

- `swadratna://order?tableNumber=5&orderId=456`
- `https://swadratna.com/order?tableNumber=12&orderId=789`