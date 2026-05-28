package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ServiceRequest
import com.example.data.model.WorkOrder
import com.example.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    INICIO,
    PROYECTOS,
    ESCANER_QR
}

class ServiceViewModel(private val repository: ServiceRepository) : ViewModel() {

    init {
        // Pre-populate with beautiful, professional default work orders immediately on launch
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    // Navigation and tab flow definitions
    private val _selectedTab = MutableStateFlow(AppTab.INICIO)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Database Flows
    val workOrders: StateFlow<List<WorkOrder>> = repository.allWorkOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val serviceRequests: StateFlow<List<ServiceRequest>> = repository.allServiceRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection Details
    private val _selectedWorkOrder = MutableStateFlow<WorkOrder?>(null)
    val selectedWorkOrder: StateFlow<WorkOrder?> = _selectedWorkOrder.asStateFlow()

    // QR Scanning state
    private val _scannedWorkOrder = MutableStateFlow<WorkOrder?>(null)
    val scannedWorkOrder: StateFlow<WorkOrder?> = _scannedWorkOrder.asStateFlow()

    private val _isScanningActive = MutableStateFlow(false)
    val isScanningActive: StateFlow<Boolean> = _isScanningActive.asStateFlow()

    // New Request Form management
    private val _showRequestForm = MutableStateFlow(false)
    val showRequestForm: StateFlow<Boolean> = _showRequestForm.asStateFlow()

    private val _formServiceType = MutableStateFlow("Albañilería")
    val formServiceType: StateFlow<String> = _formServiceType.asStateFlow()

    private val _formDescription = MutableStateFlow("")
    val formDescription: StateFlow<String> = _formDescription.asStateFlow()

    private val _formClientName = MutableStateFlow("")
    val formClientName: StateFlow<String> = _formClientName.asStateFlow()

    private val _formClientPhone = MutableStateFlow("")
    val formClientPhone: StateFlow<String> = _formClientPhone.asStateFlow()

    private val _formClientEmail = MutableStateFlow("")
    val formClientEmail: StateFlow<String> = _formClientEmail.asStateFlow()

    private val _formUrgency = MutableStateFlow("Media")
    val formUrgency: StateFlow<String> = _formUrgency.asStateFlow()

    private val _formPreferredDate = MutableStateFlow("")
    val formPreferredDate: StateFlow<String> = _formPreferredDate.asStateFlow()

    private val _formAddress = MutableStateFlow("")
    val formAddress: StateFlow<String> = _formAddress.asStateFlow()

    // Feedback messages
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Tab Navigation
    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
        // Reset specific UI states when changing tabs
        if (tab != AppTab.ESCANER_QR) {
            _scannedWorkOrder.value = null
            _isScanningActive.value = false
        }
    }

    fun selectWorkOrder(order: WorkOrder?) {
        _selectedWorkOrder.value = order
    }

    fun toggleRequestForm(show: Boolean) {
        _showRequestForm.value = show
    }

    // Input mutations
    fun updateFormServiceType(type: String) { _formServiceType.value = type }
    fun updateFormDescription(desc: String) { _formDescription.value = desc }
    fun updateFormClientName(name: String) { _formClientName.value = name }
    fun updateFormClientPhone(phone: String) { _formClientPhone.value = phone }
    fun updateFormClientEmail(email: String) { _formClientEmail.value = email }
    fun updateFormUrgency(urgency: String) { _formUrgency.value = urgency }
    fun updateFormPreferredDate(date: String) { _formPreferredDate.value = date }
    fun updateFormAddress(address: String) { _formAddress.value = address }

    fun dismissUiMessage() {
        _uiMessage.value = null
    }

    // Actively scan mock QR code (lookup database matching the key)
    fun scanQrCode(qrData: String) {
        viewModelScope.launch {
            _isScanningActive.value = true
            // Support small delay for a real tech look and feel
            kotlinx.coroutines.delay(1200)
            val matchedOrder = repository.getWorkOrderByQr(qrData)
            if (matchedOrder != null) {
                _scannedWorkOrder.value = matchedOrder
                _uiMessage.value = "Hoguera y desglose cargados exitosamente de la orden ${matchedOrder.qrCodeData}"
            } else {
                _scannedWorkOrder.value = null
                _uiMessage.value = "Código QR no reconocido. Comprueba que sea de Argenis Paredes."
            }
            _isScanningActive.value = false
        }
    }

    fun resetScanner() {
        _scannedWorkOrder.value = null
        _isScanningActive.value = false
    }

    // Simulating secure transactional payment using coroutines
    fun payScannedOrder() {
        val order = _scannedWorkOrder.value ?: return
        if (order.paymentStatus == "Pagado") {
            _uiMessage.value = "Esta orden ya se encuentra liquidada anteriormente."
            return
        }

        viewModelScope.launch {
            // Update order in database to "Completado" and payment to "Pagado"
            val updatedOrder = order.copy(
                status = "Completado",
                paymentStatus = "Pagado"
            )
            repository.updateWorkOrder(updatedOrder)
            // Sync current state
            _scannedWorkOrder.value = updatedOrder
            // If the same order is selected in details modal, update it as well
            if (_selectedWorkOrder.value?.id == order.id) {
                _selectedWorkOrder.value = updatedOrder
            }
            _uiMessage.value = "¡Pago realizado con éxito! Proyecto actualizado en tiempo real."
        }
    }

    // Submitting a custom construction/maintenance quote
    fun submitServiceRequest() {
        if (_formDescription.value.isBlank() || _formClientName.value.isBlank() || _formAddress.value.isBlank()) {
            _uiMessage.value = "Por favor completa los campos principales (Descripción, Nombre y Dirección)."
            return
        }

        viewModelScope.launch {
            val newRequest = ServiceRequest(
                serviceType = _formServiceType.value,
                description = _formDescription.value,
                clientName = _formClientName.value,
                clientPhone = _formClientPhone.value,
                clientEmail = _formClientEmail.value,
                urgency = _formUrgency.value,
                preferredDate = _formPreferredDate.value.ifBlank { "A convenir" },
                address = _formAddress.value
            )

            repository.insertServiceRequest(newRequest)

            // Clear inputs
            _formDescription.value = ""
            _formClientName.value = ""
            _formClientPhone.value = ""
            _formClientEmail.value = ""
            _formPreferredDate.value = ""
            _formAddress.value = ""
            _showRequestForm.value = false

            _uiMessage.value = "¡Solicitud registrada con éxito! Analizaremos el presupuesto a la brevedad."
        }
    }
}

class ServiceViewModelFactory(private val repository: ServiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
