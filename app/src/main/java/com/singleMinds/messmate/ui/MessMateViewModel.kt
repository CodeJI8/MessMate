package com.singleminds.messmate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.singleminds.messmate.data.local.entity.*
import com.singleminds.messmate.data.repository.MessMateRepository
import com.singleminds.messmate.engine.SettleUpEngine
import com.singleminds.messmate.engine.MemberSummary
import com.singleminds.messmate.engine.SettlementTransfer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import org.json.JSONArray
import org.json.JSONObject

data class MainUiState(
    val flat: Flat? = null,
    val isOnboarded: Boolean = false,
    val currentMonth: Month? = null,
    val availableMonths: List<Month> = emptyList(),
    val selectedMonth: Month? = null,
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val meals: List<MealEntry> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val summaryMap: Map<String, MemberSummary> = emptyMap(),
    val settlements: List<SettlementTransfer> = emptyList(),
    val totalSpent: Long = 0L,
    val mealRate: Double = 0.0,
    val auditLogs: List<AuditLog> = emptyList(),
    val recurringBills: List<RecurringBill> = emptyList(),
    val themeMode: String = "system"
)

@OptIn(ExperimentalCoroutinesApi::class)
class MessMateViewModel(private val repository: MessMateRepository) : ViewModel() {

    private val engine = SettleUpEngine()

    private val _selectedMonthId = MutableStateFlow<String?>(null)
    private val _themeMode = MutableStateFlow("system")

    private val flow1 = combine(
        repository.getFlatFlow(),
        repository.getAllMembersFlow(),
        repository.getAllMonthsFlow(),
        _selectedMonthId
    ) { flat, members, months, selectedMonthId ->
        val currentMonth = months.firstOrNull { !it.isClosed } ?: months.firstOrNull()
        val activeMonth = months.find { it.id == selectedMonthId } ?: currentMonth
        TupleBase(flat, members, months, activeMonth)
    }

    private val flow2 = combine(
        repository.getAuditLogsFlow(),
        repository.getRecurringBillsFlow(),
        _themeMode
    ) { auditLogs, recurringBills, themeMode ->
        TupleExtras(auditLogs, recurringBills, themeMode)
    }

    val uiState: StateFlow<MainUiState> = combine(flow1, flow2) { base, extras ->
        TupleData(
            flat = base.flat,
            members = base.members,
            months = base.months,
            activeMonth = base.activeMonth,
            auditLogs = extras.auditLogs,
            recurringBills = extras.recurringBills,
            themeMode = extras.themeMode
        )
    }.flatMapLatest { tuple ->
        val activeMonth = tuple.activeMonth
        if (activeMonth == null) {
            flowOf(
                MainUiState(
                    flat = tuple.flat,
                    isOnboarded = tuple.flat != null,
                    members = tuple.members,
                    availableMonths = tuple.months,
                    auditLogs = tuple.auditLogs,
                    recurringBills = tuple.recurringBills,
                    themeMode = tuple.themeMode
                )
            )
        } else {
            combine(
                repository.getExpensesForMonthFlow(activeMonth.startTimestamp, activeMonth.endTimestamp),
                repository.getMealsForMonthFlow(activeMonth.startTimestamp, activeMonth.endTimestamp),
                repository.getPaymentsForMonthFlow(activeMonth.startTimestamp, activeMonth.endTimestamp)
            ) { expenses, meals, payments ->

                val summaryMap = engine.calculateSummary(
                    monthId = activeMonth.id,
                    startTimestamp = activeMonth.startTimestamp,
                    endTimestamp = activeMonth.endTimestamp,
                    members = tuple.members,
                    mealEntries = meals,
                    expenses = expenses,
                    payments = payments
                )

                val netBalances = summaryMap.mapValues { it.value.netBalance }
                val settlements = engine.calculateSettlements(netBalances)

                val totalSpent = expenses.sumOf { it.amountMinor }
                val mealPoolSpent = expenses.filter { it.type == ExpenseType.MEAL_POOL.name }.sumOf { it.amountMinor }
                val totalMealsCount = meals.sumOf { it.breakfast + it.lunch + it.dinner }
                val mealRate = if (totalMealsCount > 0) mealPoolSpent.toDouble() / totalMealsCount.toDouble() else 0.0

                MainUiState(
                    flat = tuple.flat,
                    isOnboarded = tuple.flat != null,
                    currentMonth = tuple.months.firstOrNull { !it.isClosed },
                    availableMonths = tuple.months,
                    selectedMonth = activeMonth,
                    members = tuple.members,
                    expenses = expenses,
                    meals = meals,
                    payments = payments,
                    summaryMap = summaryMap,
                    settlements = settlements,
                    totalSpent = totalSpent,
                    mealRate = mealRate,
                    auditLogs = tuple.auditLogs,
                    recurringBills = tuple.recurringBills,
                    themeMode = tuple.themeMode
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.getOrCreateCurrentMonth()
        }
    }

    fun selectMonth(monthId: String) {
        _selectedMonthId.value = monthId
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun setupFlat(
        name: String,
        currencySymbol: String,
        memberNames: List<String>,
        loadSample: Boolean = false
    ) {
        viewModelScope.launch {
            if (loadSample) {
                repository.loadSampleData()
            } else {
                val flat = Flat(name = name, currencySymbol = currencySymbol)
                repository.saveFlat(flat)
                memberNames.filter { it.isNotBlank() }.forEachIndexed { idx, mName ->
                    repository.addMember(Member(name = mName.trim(), colorIndex = idx % 8))
                }
                repository.getOrCreateCurrentMonth()
            }
        }
    }

    fun addMember(name: String, colorIndex: Int) {
        viewModelScope.launch {
            repository.addMember(Member(name = name.trim(), colorIndex = colorIndex % 8))
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun addExpense(
        title: String,
        amountMinor: Long,
        payerId: String,
        type: ExpenseType,
        category: String,
        splitRule: SplitRule,
        note: String = "",
        receiptImagePath: String? = null
    ) {
        viewModelScope.launch {
            val month = uiState.value.selectedMonth ?: repository.getOrCreateCurrentMonth()
            val expense = Expense(
                title = title.trim(),
                amountMinor = amountMinor,
                date = System.currentTimeMillis(),
                payerId = payerId,
                type = type.name,
                category = category,
                splitRule = splitRule.name,
                note = note,
                receiptImagePath = receiptImagePath,
                monthId = month.id
            )
            repository.addExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun saveMealEntry(memberId: String, date: Long, breakfast: Int, lunch: Int, dinner: Int) {
        viewModelScope.launch {
            val entry = MealEntry(
                memberId = memberId,
                date = date,
                breakfast = breakfast,
                lunch = lunch,
                dinner = dinner
            )
            repository.saveMealEntry(entry)
        }
    }

    fun batchEveryoneEats(date: Long) {
        viewModelScope.launch {
            val activeMembers = uiState.value.members.filter { it.isActive }
            val entries = activeMembers.map { member ->
                MealEntry(memberId = member.id, date = date, breakfast = 1, lunch = 1, dinner = 1)
            }
            repository.saveAllMealEntries(entries)
        }
    }

    fun recordPayment(fromMemberId: String, toMemberId: String, amountMinor: Long, note: String = "") {
        viewModelScope.launch {
            val month = uiState.value.selectedMonth ?: repository.getOrCreateCurrentMonth()
            val payment = Payment(
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amountMinor = amountMinor,
                date = System.currentTimeMillis(),
                note = note,
                monthId = month.id
            )
            repository.addPayment(payment)
        }
    }

    fun closeMonth(carryForward: Boolean = true) {
        viewModelScope.launch {
            val current = uiState.value.currentMonth ?: return@launch
            val closedMonth = current.copy(isClosed = true, carryForward = carryForward)
            repository.insertOrUpdateMonth(closedMonth)
            
            repository.getOrCreateCurrentMonth()
        }
    }

    fun reopenMonth(monthId: String) {
        viewModelScope.launch {
            val month = uiState.value.availableMonths.find { it.id == monthId } ?: return@launch
            repository.insertOrUpdateMonth(month.copy(isClosed = false))
        }
    }

    fun addRecurringBill(title: String, amountMinor: Long, category: String, payerId: String) {
        viewModelScope.launch {
            val bill = RecurringBill(
                title = title.trim(),
                amountMinor = amountMinor,
                category = category,
                payerId = payerId
            )
            repository.addRecurringBill(bill)
        }
    }

    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
        }
    }

    fun confirmRecurringBill(bill: RecurringBill) {
        addExpense(
            title = bill.title,
            amountMinor = bill.amountMinor,
            payerId = bill.payerId,
            type = ExpenseType.FIXED_BILL,
            category = bill.category,
            splitRule = SplitRule.EQUAL,
            note = "Recurring Bill Confirmed"
        )
    }

    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())

            val state = uiState.value
            val membersArray = JSONArray()
            state.members.forEach { m ->
                val obj = JSONObject()
                obj.put("id", m.id)
                obj.put("name", m.name)
                obj.put("colorIndex", m.colorIndex)
                obj.put("joinDate", m.joinDate)
                obj.put("leaveDate", m.leaveDate ?: JSONObject.NULL)
                obj.put("isActive", m.isActive)
                membersArray.put(obj)
            }
            root.put("members", membersArray)

            val expensesArray = JSONArray()
            state.expenses.forEach { e ->
                val obj = JSONObject()
                obj.put("id", e.id)
                obj.put("title", e.title)
                obj.put("amountMinor", e.amountMinor)
                obj.put("date", e.date)
                obj.put("payerId", e.payerId)
                obj.put("type", e.type)
                obj.put("category", e.category)
                obj.put("splitRule", e.splitRule)
                obj.put("note", e.note)
                expensesArray.put(obj)
            }
            root.put("expenses", expensesArray)

            outputStream.bufferedWriter().use { it.write(root.toString(2)) }
        }
    }

    private data class TupleBase(
        val flat: Flat?,
        val members: List<Member>,
        val months: List<Month>,
        val activeMonth: Month?
    )

    private data class TupleExtras(
        val auditLogs: List<AuditLog>,
        val recurringBills: List<RecurringBill>,
        val themeMode: String
    )

    private data class TupleData(
        val flat: Flat?,
        val members: List<Member>,
        val months: List<Month>,
        val activeMonth: Month?,
        val auditLogs: List<AuditLog>,
        val recurringBills: List<RecurringBill>,
        val themeMode: String
    )
}

class MessMateViewModelFactory(private val repository: MessMateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessMateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessMateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
