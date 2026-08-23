package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ContainerEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.ContainerRepository
import com.example.data.repository.UserRepository
import com.example.model.RentCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class ContainerFilter {
    ALL,
    IN_YARD,
    ON_VEHICLE,
    AT_MILL,
    PENDING_RENT,
    GATED_OUT,
    DAMAGED
}

data class YardStats(
    val totalContainers: Int = 0,
    val inYardCount: Int = 0,
    val onVehicleCount: Int = 0,
    val atMillCount: Int = 0,
    val gatedOutCount: Int = 0,
    val totalPendingReceivables: Double = 0.0,
    val totalRevenueCollected: Double = 0.0
)

data class CompanySettings(
    val companyName: String = "KARACHI INTERNATIONAL CONTAINER DEPOT",
    val yardAddress: String = "Plot 42-B, Marine Logistics Hub, West Wharf",
    val contactInfo: String = "+92 321 9876543 | ops@kicd-depot.com",
    val currencySymbol: String = "Rs.",
    val defaultDaily20ft: Double = 0.0,
    val defaultDaily40ft: Double = 0.0,
    val defaultHandling: Double = 0.0,
    val defaultFreeDays: Int = 0
)

class ContainerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContainerRepository
    private val userRepository: UserRepository

    // User Authentication & Local Storage Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage = _authSuccessMessage.asStateFlow()

    private val _lastSavedUsername = MutableStateFlow("")
    val lastSavedUsername = _lastSavedUsername.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ContainerFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedContainerId = MutableStateFlow<Long?>(null)
    val selectedContainerId = _selectedContainerId.asStateFlow()

    private val _settings = MutableStateFlow(CompanySettings())
    val settings = _settings.asStateFlow()

    val allContainers: StateFlow<List<ContainerEntity>>
    val filteredContainers: StateFlow<List<ContainerEntity>>
    val yardStats: StateFlow<YardStats>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ContainerRepository(db.containerDao(), db.paymentDao())
        userRepository = UserRepository(db.userDao(), application)

        allUsers = userRepository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default local users and check saved session
        viewModelScope.launch {
            userRepository.seedDefaultUsersIfEmpty()
            _lastSavedUsername.value = userRepository.getLastSavedUsername()

            val savedId = userRepository.getSavedUserId()
            if (savedId != null) {
                val savedUser = userRepository.getUserById(savedId)
                if (savedUser != null) {
                    _currentUser.value = savedUser
                }
            }
        }

        allContainers = repository.allContainers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredContainers = combine(
            allContainers,
            _searchQuery,
            _selectedFilter
        ) { containers, query, filter ->
            containers.filter { container ->
                val matchesQuery = query.isBlank() ||
                        container.containerNo.contains(query, ignoreCase = true) ||
                        container.line.contains(query, ignoreCase = true) ||
                        container.transporter.contains(query, ignoreCase = true) ||
                        container.millName.contains(query, ignoreCase = true) ||
                        container.millLocation.contains(query, ignoreCase = true) ||
                        container.shipper.contains(query, ignoreCase = true) ||
                        container.consignee.contains(query, ignoreCase = true) ||
                        container.bookingNo.contains(query, ignoreCase = true) ||
                        container.nocNo.contains(query, ignoreCase = true) ||
                        container.driverCnic.contains(query, ignoreCase = true) ||
                        container.driverCell.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    ContainerFilter.ALL -> true
                    ContainerFilter.IN_YARD -> container.status == "IN_YARD"
                    ContainerFilter.ON_VEHICLE -> container.status == "ON_VEHICLE"
                    ContainerFilter.AT_MILL -> container.status == "AT_MILL"
                    ContainerFilter.GATED_OUT -> container.status == "GATED_OUT"
                    ContainerFilter.PENDING_RENT -> {
                        val calc = RentCalculation.calculate(container)
                        calc.pendingAmount > 0.01 && container.status != "GATED_OUT"
                    }
                    ContainerFilter.DAMAGED -> container.condition.contains("Damaged", ignoreCase = true) ||
                            container.condition.contains("Repair", ignoreCase = true)
                }

                matchesQuery && matchesFilter
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        yardStats = allContainers.combine(_settings) { list, _ ->
            var inYard = 0
            var onVehicle = 0
            var atMill = 0
            var gatedOut = 0
            var pendingTotal = 0.0
            var revenue = 0.0

            for (c in list) {
                revenue += c.paidAmount
                when (c.status) {
                    "IN_YARD" -> {
                        inYard++
                        val calc = RentCalculation.calculate(c)
                        pendingTotal += calc.pendingAmount
                    }
                    "ON_VEHICLE" -> {
                        onVehicle++
                        val calc = RentCalculation.calculate(c)
                        pendingTotal += calc.pendingAmount
                    }
                    "AT_MILL" -> {
                        atMill++
                        val calc = RentCalculation.calculate(c)
                        pendingTotal += calc.pendingAmount
                    }
                    "GATED_OUT" -> {
                        gatedOut++
                    }
                }
            }

            YardStats(
                totalContainers = list.size,
                inYardCount = inYard,
                onVehicleCount = onVehicle,
                atMillCount = atMill,
                gatedOutCount = gatedOut,
                totalPendingReceivables = pendingTotal,
                totalRevenueCollected = revenue
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = YardStats()
        )

        // Clean start: clear old dummy containers if any and don't load dummy sample data
        viewModelScope.launch {
            repository.deleteAllContainers()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ContainerFilter) {
        _selectedFilter.value = filter
    }

    fun selectContainer(id: Long?) {
        _selectedContainerId.value = id
    }

    fun updateSettings(newSettings: CompanySettings) {
        _settings.value = newSettings
    }

    fun addContainer(container: ContainerEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertContainer(container)
            if (container.paidAmount > 0) {
                repository.recordPayment(
                    containerId = id,
                    amount = container.paidAmount,
                    paymentMethod = "CASH",
                    referenceNo = "INIT-${System.currentTimeMillis() % 10000}",
                    receivedBy = "Yard Gate Cashier",
                    notes = "Initial Gate-In Advance / Handling"
                )
            }
            onComplete(id)
        }
    }

    fun updateContainer(container: ContainerEntity) {
        viewModelScope.launch {
            repository.updateContainer(container)
        }
    }

    fun deleteContainer(id: Long) {
        viewModelScope.launch {
            repository.deleteContainer(id)
        }
    }

    fun recordPayment(
        containerId: Long,
        amount: Double,
        method: String,
        ref: String,
        receivedBy: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(containerId, amount, method, ref, receivedBy, notes)
        }
    }

    fun gateOutAndClear(
        containerId: Long,
        outDate: Long,
        destinationType: String = "FINAL_EXIT",
        millName: String = "",
        millLocation: String = "",
        paymentAmount: Double = 0.0,
        paymentMethod: String = "CASH",
        clearedBy: String = "Yard Master",
        driverName: String? = null,
        driverCnic: String? = null,
        driverCell: String? = null,
        transporter: String? = null,
        truckNo: String? = null,
        remarks: String? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.gateOutContainer(
                containerId = containerId,
                outDate = outDate,
                destinationType = destinationType,
                millName = millName,
                millLocation = millLocation,
                settlePayment = paymentAmount > 0,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                clearedBy = clearedBy,
                driverName = driverName,
                driverCnic = driverCnic,
                driverCell = driverCell,
                transporter = transporter,
                truckNo = truckNo,
                remarks = remarks
            )
            onComplete()
        }
    }

    fun returnToYard(
        containerId: Long,
        yardBay: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.returnToYard(
                containerId = containerId,
                yardBay = yardBay
            )
            onComplete()
        }
    }

    // --- Authentication Actions with Local Storage DB ---

    fun login(
        credential: String,
        pass: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val result = userRepository.authenticate(credential, pass)
            _isAuthLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                _authSuccessMessage.value = "Welcome back, ${user.fullName}! / خوش آمدید"
                userRepository.saveSession(user.id, rememberMe, user.username)
                _lastSavedUsername.value = user.username
                onSuccess()
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Authentication failed / لاگ ان میں ناکامی"
            }
        }
    }

    fun signUp(
        username: String,
        fullName: String,
        email: String,
        pass: String,
        role: String,
        phone: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val result = userRepository.registerUser(
                username = username,
                fullName = fullName,
                email = email,
                password = pass,
                role = role,
                phone = phone
            )
            _isAuthLoading.value = false

            result.onSuccess { newUser ->
                _currentUser.value = newUser
                _authSuccessMessage.value = "Account created successfully for ${newUser.fullName}!"
                userRepository.saveSession(newUser.id, true, newUser.username)
                _lastSavedUsername.value = newUser.username
                onSuccess()
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Sign up failed / سائن اپ میں خرابی"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearSession()
            _currentUser.value = null
            _authSuccessMessage.value = null
            _authErrorMessage.value = null
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    fun deleteAllContainers(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllContainers()
            onComplete()
        }
    }
}
