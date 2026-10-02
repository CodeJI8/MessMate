package com.singleminds.messmate.data.repository

import com.singleminds.messmate.data.local.MessMateDatabase
import com.singleminds.messmate.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

class MessMateRepository(private val db: MessMateDatabase) {

    fun getFlatFlow(): Flow<Flat?> = db.flatDao().getFlatFlow()
    suspend fun getFlat(): Flat? = withContext(Dispatchers.IO) { db.flatDao().getFlat() }
    suspend fun saveFlat(flat: Flat) = withContext(Dispatchers.IO) { db.flatDao().insertOrUpdate(flat) }

    fun getAllMembersFlow(): Flow<List<Member>> = db.memberDao().getAllMembersFlow()
    fun getActiveMembersFlow(): Flow<List<Member>> = db.memberDao().getActiveMembersFlow()
    suspend fun addMember(member: Member) = withContext(Dispatchers.IO) { db.memberDao().insert(member) }
    suspend fun updateMember(member: Member) = withContext(Dispatchers.IO) { db.memberDao().update(member) }
    suspend fun deleteMember(member: Member) = withContext(Dispatchers.IO) { db.memberDao().delete(member) }

    fun getAllMonthsFlow(): Flow<List<Month>> = db.monthDao().getAllMonthsFlow()
    fun getCurrentMonthFlow(): Flow<Month?> = db.monthDao().getCurrentMonthFlow()

    suspend fun getOrCreateCurrentMonth(): Month = withContext(Dispatchers.IO) {
        val existing = db.monthDao().getCurrentMonth()
        if (existing != null) return@withContext existing

        val now = ZonedDateTime.now()
        val yearMonth = YearMonth.of(now.year, now.month)
        val zone = ZoneId.systemDefault()
        val startMillis = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

        val label = "${yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${yearMonth.year}"
        val id = yearMonth.toString()

        val month = Month(
            id = id,
            label = label,
            startTimestamp = startMillis,
            endTimestamp = endMillis,
            isClosed = false
        )
        db.monthDao().insertOrUpdate(month)
        month
    }

    suspend fun insertOrUpdateMonth(month: Month) = withContext(Dispatchers.IO) { db.monthDao().insertOrUpdate(month) }

    fun getMealsForMonthFlow(start: Long, end: Long): Flow<List<MealEntry>> =
        db.mealDao().getMealsForMonthFlow(start, end)

    suspend fun getMealsForMonth(start: Long, end: Long): List<MealEntry> =
        withContext(Dispatchers.IO) { db.mealDao().getMealsForMonth(start, end) }

    suspend fun saveMealEntry(mealEntry: MealEntry) =
        withContext(Dispatchers.IO) { db.mealDao().insertOrUpdate(mealEntry) }

    suspend fun saveAllMealEntries(mealEntries: List<MealEntry>) =
        withContext(Dispatchers.IO) { db.mealDao().insertOrUpdateAll(mealEntries) }

    fun getExpensesForMonthFlow(start: Long, end: Long): Flow<List<Expense>> =
        db.expenseDao().getExpensesForMonthFlow(start, end)

    suspend fun getExpensesForMonth(start: Long, end: Long): List<Expense> =
        withContext(Dispatchers.IO) { db.expenseDao().getExpensesForMonth(start, end) }

    suspend fun addExpense(expense: Expense, shares: List<ExpenseShare> = emptyList()) = withContext(Dispatchers.IO) {
        db.expenseDao().insertExpense(expense)
        if (shares.isNotEmpty()) {
            db.expenseDao().insertShares(shares)
        }
        db.auditDao().insertLog(
            AuditLog(
                entity = "EXPENSE",
                entityId = expense.id,
                action = "CREATE",
                newValJson = "${expense.title}: ${expense.amountMinor} (Payer: ${expense.payerId})"
            )
        )
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        db.expenseDao().deleteExpense(expense)
        db.expenseDao().deleteSharesForExpense(expense.id)
        db.auditDao().insertLog(
            AuditLog(
                entity = "EXPENSE",
                entityId = expense.id,
                action = "DELETE",
                oldValJson = "${expense.title}: ${expense.amountMinor}"
            )
        )
    }

    fun getPaymentsForMonthFlow(start: Long, end: Long): Flow<List<Payment>> =
        db.paymentDao().getPaymentsForMonthFlow(start, end)

    suspend fun getPaymentsForMonth(start: Long, end: Long): List<Payment> =
        withContext(Dispatchers.IO) { db.paymentDao().getPaymentsForMonth(start, end) }

    suspend fun addPayment(payment: Payment) = withContext(Dispatchers.IO) {
        db.paymentDao().insertPayment(payment)
        db.auditDao().insertLog(
            AuditLog(
                entity = "PAYMENT",
                entityId = payment.id,
                action = "CREATE",
                newValJson = "Payment ${payment.amountMinor} from ${payment.fromMemberId} to ${payment.toMemberId}"
            )
        )
    }

    fun getAuditLogsFlow(): Flow<List<AuditLog>> = db.auditDao().getAllLogsFlow()

    fun getRecurringBillsFlow(): Flow<List<RecurringBill>> = db.recurringBillDao().getActiveRecurringBillsFlow()
    suspend fun addRecurringBill(bill: RecurringBill) = withContext(Dispatchers.IO) { db.recurringBillDao().insertOrUpdate(bill) }
    suspend fun deleteRecurringBill(bill: RecurringBill) = withContext(Dispatchers.IO) { db.recurringBillDao().delete(bill) }

    suspend fun loadSampleData() = withContext(Dispatchers.IO) {
        val flat = Flat(
            id = "default_flat",
            name = "Baker Street Kitchen",
            currencySymbol = "$",
            defaultAssumeEveryoneEats = true
        )
        saveFlat(flat)

        val m1 = Member(id = "m1", name = "Rafi", colorIndex = 0)
        val m2 = Member(id = "m2", name = "Anik", colorIndex = 1)
        val m3 = Member(id = "m3", name = "Saad", colorIndex = 2)
        val m4 = Member(id = "m4", name = "Nibir", colorIndex = 3)

        addMember(m1)
        addMember(m2)
        addMember(m3)
        addMember(m4)

        val month = getOrCreateCurrentMonth()

        val exp1 = Expense(
            title = "Weekly Fresh Grocery",
            amountMinor = 12000L,
            date = month.startTimestamp + 86400000L * 2,
            payerId = m1.id,
            type = ExpenseType.MEAL_POOL.name,
            category = "Grocery",
            splitRule = SplitRule.SHARES.name
        )
        val exp2 = Expense(
            title = "High Speed Fiber WiFi",
            amountMinor = 4000L,
            date = month.startTimestamp + 86400000L * 5,
            payerId = m2.id,
            type = ExpenseType.FIXED_BILL.name,
            category = "Utilities",
            splitRule = SplitRule.EQUAL.name
        )
        val exp3 = Expense(
            title = "Cooking Gas Cylinder",
            amountMinor = 3500L,
            date = month.startTimestamp + 86400000L * 8,
            payerId = m3.id,
            type = ExpenseType.MEAL_POOL.name,
            category = "Gas",
            splitRule = SplitRule.EQUAL.name
        )

        addExpense(exp1)
        addExpense(exp2)
        addExpense(exp3)

        val todayMillis = System.currentTimeMillis()
        saveMealEntry(MealEntry(memberId = m1.id, date = todayMillis, breakfast = 1, lunch = 1, dinner = 1))
        saveMealEntry(MealEntry(memberId = m2.id, date = todayMillis, breakfast = 1, lunch = 0, dinner = 1))
        saveMealEntry(MealEntry(memberId = m3.id, date = todayMillis, breakfast = 0, lunch = 1, dinner = 1))
        saveMealEntry(MealEntry(memberId = m4.id, date = todayMillis, breakfast = 1, lunch = 1, dinner = 2))
    }
}
