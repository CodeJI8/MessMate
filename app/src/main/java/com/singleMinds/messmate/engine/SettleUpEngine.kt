package com.singleminds.messmate.engine

import com.singleminds.messmate.data.local.entity.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.min

data class MemberSummary(
    val memberId: String,
    val totalMeals: Int = 0,
    val mealCost: Long = 0L,
    val billShares: Long = 0L,
    val paidOut: Long = 0L,
    val paymentsMade: Long = 0L,
    val paymentsReceived: Long = 0L,
    val netBalance: Long = 0L
)

data class SettlementTransfer(
    val fromMemberId: String,
    val toMemberId: String,
    val amountMinor: Long
)

class SettleUpEngine {

    fun calculateSummary(
        monthId: String,
        startTimestamp: Long,
        endTimestamp: Long,
        members: List<Member>,
        mealEntries: List<MealEntry>,
        expenses: List<Expense>,
        payments: List<Payment>,
        shares: List<ExpenseShare> = emptyList()
    ): Map<String, MemberSummary> {
        // 1. Meal count per member
        val mealCountMap = mutableMapOf<String, Int>()
        members.forEach { mealCountMap[it.id] = 0 }
        mealEntries.forEach { entry ->
            val totalForEntry = entry.breakfast + entry.lunch + entry.dinner
            mealCountMap[entry.memberId] = (mealCountMap[entry.memberId] ?: 0) + totalForEntry
        }

        val totalMealsInMonth = mealCountMap.values.sum()

        // 2. Meal pool expenses
        val mealPoolExpenses = expenses.filter { 
            it.type == ExpenseType.MEAL_POOL.name || it.type == "MEAL_POOL" 
        }
        val totalMealPoolSpend = mealPoolExpenses.sumOf { it.amountMinor }

        // Meal costs using splitWeighted if totalMeals > 0
        val mealCostMap = if (totalMealsInMonth > 0 && totalMealPoolSpend > 0L) {
            val activeMealMembers = members.filter { (mealCountMap[it.id] ?: 0) > 0 }
            val memberIds = activeMealMembers.map { it.id }
            val weights = activeMealMembers.map { (mealCountMap[it.id] ?: 0).toDouble() }
            SplitLogic.splitWeighted(totalMealPoolSpend, memberIds, weights)
        } else {
            emptyMap()
        }

        // 3. Fixed bill expenses
        val fixedBillExpenses = expenses.filter { 
            it.type == ExpenseType.FIXED_BILL.name || it.type == "FIXED_BILL" 
        }

        val billSharesMap = mutableMapOf<String, Long>()
        members.forEach { billSharesMap[it.id] = 0L }

        val monthTotalDays = calculateDaysInRange(startTimestamp, endTimestamp)

        fixedBillExpenses.forEach { expense ->
            val expenseShares = shares.filter { it.expenseId == expense.id }

            val memberWeights = mutableListOf<Pair<String, Double>>()

            members.forEach { member ->
                val memberStart = maxOf(member.joinDate, startTimestamp)
                val memberEnd = minOf(member.leaveDate ?: endTimestamp, endTimestamp)
                val activeDays = if (memberEnd >= memberStart) calculateDaysInRange(memberStart, memberEnd) else 0

                val explicitShare = expenseShares.find { it.memberId == member.id }
                val baseWeight = explicitShare?.weightOrValue ?: 1.0

                if (activeDays > 0) {
                    val prorationFactor = if (monthTotalDays > 0) activeDays.toDouble() / monthTotalDays.toDouble() else 1.0
                    memberWeights.add(member.id to (baseWeight * prorationFactor))
                }
            }

            if (memberWeights.isNotEmpty()) {
                val memberIds = memberWeights.map { it.first }
                val weights = memberWeights.map { it.second }
                val splitResult = SplitLogic.splitWeighted(expense.amountMinor, memberIds, weights)
                splitResult.forEach { (mId, share) ->
                    billSharesMap[mId] = (billSharesMap[mId] ?: 0L) + share
                }
            }
        }

        // 4. Paid Out per member
        val paidOutMap = mutableMapOf<String, Long>()
        members.forEach { paidOutMap[it.id] = 0L }
        expenses.forEach { expense ->
            paidOutMap[expense.payerId] = (paidOutMap[expense.payerId] ?: 0L) + expense.amountMinor
        }

        // 5. Payments Made and Received
        val paymentsMadeMap = mutableMapOf<String, Long>()
        val paymentsReceivedMap = mutableMapOf<String, Long>()
        members.forEach { 
            paymentsMadeMap[it.id] = 0L 
            paymentsReceivedMap[it.id] = 0L
        }
        payments.forEach { payment ->
            paymentsMadeMap[payment.fromMemberId] = (paymentsMadeMap[payment.fromMemberId] ?: 0L) + payment.amountMinor
            paymentsReceivedMap[payment.toMemberId] = (paymentsReceivedMap[payment.toMemberId] ?: 0L) + payment.amountMinor
        }

        // 6. Summary map
        val summaryMap = mutableMapOf<String, MemberSummary>()
        members.forEach { member ->
            val mId = member.id
            val mCount = mealCountMap[mId] ?: 0
            val mCost = mealCostMap[mId] ?: 0L
            val bShare = billSharesMap[mId] ?: 0L
            val pOut = paidOutMap[mId] ?: 0L
            val pMade = paymentsMadeMap[mId] ?: 0L
            val pRec = paymentsReceivedMap[mId] ?: 0L

            val netBalance = pOut + pMade - (mCost + bShare + pRec)

            summaryMap[mId] = MemberSummary(
                memberId = mId,
                totalMeals = mCount,
                mealCost = mCost,
                billShares = bShare,
                paidOut = pOut,
                paymentsMade = pMade,
                paymentsReceived = pRec,
                netBalance = netBalance
            )
        }

        return summaryMap
    }

    fun calculateSettlements(netBalances: Map<String, Long>): List<SettlementTransfer> {
        data class BalanceNode(val memberId: String, var amount: Long)

        val debtors = mutableListOf<BalanceNode>()
        val creditors = mutableListOf<BalanceNode>()

        netBalances.forEach { (mId, balance) ->
            if (balance < 0L) {
                debtors.add(BalanceNode(mId, abs(balance)))
            } else if (balance > 0L) {
                creditors.add(BalanceNode(mId, balance))
            }
        }

        debtors.sortByDescending { it.amount }
        creditors.sortByDescending { it.amount }

        val settlements = mutableListOf<SettlementTransfer>()

        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]

            val transfer = min(debtor.amount, creditor.amount)
            if (transfer > 0L) {
                settlements.add(SettlementTransfer(debtor.memberId, creditor.memberId, transfer))
                debtor.amount -= transfer
                creditor.amount -= transfer
            }

            if (debtor.amount == 0L) dIdx++
            if (creditor.amount == 0L) cIdx++
        }

        return settlements
    }

    private fun calculateDaysInRange(startMillis: Long, endMillis: Long): Int {
        val zone = ZoneId.systemDefault()
        val startDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zone).toLocalDate()
        val endDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        return maxOf(1, days.toInt())
    }
}
