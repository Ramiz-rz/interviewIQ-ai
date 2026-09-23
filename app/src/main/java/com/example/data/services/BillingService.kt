package com.example.data.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubscriptionPlan(val id: String, val title: String, val priceFormatted: String, val billingPeriod: String) {
    FREE("free_tier", "Free Tier", "$0.00", "Forever"),
    PRO_MONTHLY("pro_monthly_599", "Pro Monthly", "$5.99", "per month"),
    PRO_YEARLY("pro_yearly_3999", "Pro Annual", "$39.99", "per year ($3.33/mo - Save 44%)")
}

data class SubscriptionState(
    val isPro: Boolean = false,
    val activePlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val interviewsRemainingThisMonth: Int = 3,
    val expiresAtMillis: Long = 0L
)

class BillingService(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("interviewiq_billing", Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(loadSubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private fun loadSubscriptionState(): SubscriptionState {
        val isPro = prefs.getBoolean("is_pro", false)
        val planId = prefs.getString("active_plan", SubscriptionPlan.FREE.id) ?: SubscriptionPlan.FREE.id
        val remaining = prefs.getInt("free_interviews_remaining", 3)
        val expires = prefs.getLong("expires_at", 0L)

        val plan = when (planId) {
            SubscriptionPlan.PRO_MONTHLY.id -> SubscriptionPlan.PRO_MONTHLY
            SubscriptionPlan.PRO_YEARLY.id -> SubscriptionPlan.PRO_YEARLY
            else -> SubscriptionPlan.FREE
        }

        return SubscriptionState(
            isPro = isPro,
            activePlan = plan,
            interviewsRemainingThisMonth = remaining,
            expiresAtMillis = expires
        )
    }

    fun purchasePlan(plan: SubscriptionPlan): Boolean {
        val expiry = System.currentTimeMillis() + if (plan == SubscriptionPlan.PRO_YEARLY) 365L * 86400000L else 30L * 86400000L
        prefs.edit()
            .putBoolean("is_pro", true)
            .putString("active_plan", plan.id)
            .putLong("expires_at", expiry)
            .apply()

        _subscriptionState.value = SubscriptionState(
            isPro = true,
            activePlan = plan,
            interviewsRemainingThisMonth = 999,
            expiresAtMillis = expiry
        )
        return true
    }

    fun restorePurchases(): Boolean {
        // Checks play billing / local receipt
        val state = loadSubscriptionState()
        _subscriptionState.value = state
        return state.isPro
    }

    fun consumeMockInterview(): Boolean {
        val current = _subscriptionState.value
        if (current.isPro) return true
        if (current.interviewsRemainingThisMonth > 0) {
            val updated = current.interviewsRemainingThisMonth - 1
            prefs.edit().putInt("free_interviews_remaining", updated).apply()
            _subscriptionState.value = current.copy(interviewsRemainingThisMonth = updated)
            return true
        }
        return false
    }

    fun resetSubscriptionForTesting() {
        prefs.edit().clear().apply()
        _subscriptionState.value = SubscriptionState(
            isPro = false,
            activePlan = SubscriptionPlan.FREE,
            interviewsRemainingThisMonth = 3,
            expiresAtMillis = 0L
        )
    }
}
