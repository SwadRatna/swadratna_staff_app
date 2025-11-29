package com.swadratna.swadratna_staff.data.remote.services

import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import retrofit2.Response
import retrofit2.http.*

// 1. POST /auth/login
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

// 5. POST /kot
data class KotItem(val menuItemId: String, val quantity: Int)

// 7. PATCH /approveBill/:orderId
data class ApproveBillResponse(val message: String)

// 8. PATCH /menuItemAvailability/:location/:menu
data class AvailabilityRequest(val is_available: Boolean)
data class AvailabilityResponse(val message: String)

// 9. PATCH /staff/bill/{billId}/payment
data class RecordPaymentRequest(
    val payment_mode: String,
    val transaction_id: String?,
    val notes: String?,
    val amount_paid: Double
)

data class PaymentDetails(
    val amount_paid: Double,
    val collected_by: Int?,
    val notes: String?,
    val paid_at: String?,
    val payment_mode: String,
    val status: String,
    val transaction_id: String?
)

data class PaymentInvoice(
    val id: Int,
    val tenant_id: Int?,
    val bill_id: Int,
    val invoice_number: String,
    val invoice_date: String?,
    val customer_name: String?,
    val customer_phone: String?,
    val customer_email: String?,
    val payment_details: PaymentDetails
)

data class PaidBill(
    val id: Int,
    val bill_number: String?,
    val total_amount: Double,
    val status: String,
    val payment_mode: String?
)

data class RecordPaymentResponse(
    val bill: PaidBill,
    val invoice: PaymentInvoice?,
    val message: String
)
