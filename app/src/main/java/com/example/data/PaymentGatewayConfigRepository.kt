package com.example.data

import com.example.model.PaymentGatewayConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository for managing PaymentGatewayConfig.
 * Provides real-time reactive state updates for Admin Panel live preview and user screens.
 */
object PaymentGatewayConfigRepository {
    private val _config = MutableStateFlow(PaymentGatewayConfig.DEFAULT)
    val config: StateFlow<PaymentGatewayConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: PaymentGatewayConfig) {
        _config.value = newConfig
    }

    fun resetToDefault() {
        _config.value = PaymentGatewayConfig.DEFAULT
    }
}
