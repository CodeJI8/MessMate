package com.singleminds.messmate.engine

import com.singleminds.messmate.data.local.entity.*
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SettleUpEngineTest {

    @Test
    fun testMealCostSplit() {
        val engine = SettleUpEngine()

        val member1 = Member("m1", "Alice", 0, 0L, null, true)
        val member2 = Member("m2", "Bob", 1, 0L, null, true)
        
        val startMillis = 1672531200000L // Jan 1 2023
        val endMillis = 1675209599000L   // Jan 31 2023

        val mealEntries = listOf(
            MealEntry("m1", startMillis, 1, 1, 1), // 3 meals
            MealEntry("m2", startMillis, 0, 1, 1)  // 2 meals
        )

        val expenses = listOf(
            Expense("e1", "Market", 500L, startMillis, "m1", ExpenseType.MEAL_POOL.name, "Food", SplitRule.SHARES.name, "")
        )

        val result = engine.calculateSummary(
            "mon1", startMillis, endMillis, listOf(member1, member2), mealEntries, expenses, emptyList(), emptyList()
        )

        assertEquals(300L, result["m1"]?.mealCost)
        assertEquals(200L, result["m2"]?.mealCost)
        assertEquals(200L, result["m1"]?.netBalance)
        assertEquals(-200L, result["m2"]?.netBalance)
    }

    @Test
    fun testLargestRemainderSplit() {
        val res = SplitLogic.splitWeighted(100L, listOf("A", "B", "C"), listOf(1.0, 1.0, 1.0))
        assertEquals(100L, res.values.sum())
        assertTrue(res.values.containsAll(listOf(34L, 33L, 33L)))
    }
    
    @Test
    fun testFixedBillProration() {
        val engine = SettleUpEngine()
        
        val zone = ZoneId.systemDefault()
        val start = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, zone)
        val end = ZonedDateTime.of(2023, 1, 31, 23, 59, 59, 0, zone)
        val midMonth = ZonedDateTime.of(2023, 1, 16, 0, 0, 0, 0, zone)

        val member1 = Member("m1", "Alice", 0, start.toInstant().toEpochMilli(), null, true)
        val member2 = Member("m2", "Bob", 1, midMonth.toInstant().toEpochMilli(), null, true)
        
        val startMillis = start.toInstant().toEpochMilli()
        val endMillis = end.toInstant().toEpochMilli()

        val expenses = listOf(
            Expense("e1", "Wifi", 1000L, startMillis, "m1", ExpenseType.FIXED_BILL.name, "Internet", SplitRule.SHARES.name, "")
        )

        val result = engine.calculateSummary(
            "mon1", startMillis, endMillis, listOf(member1, member2), emptyList(), expenses, emptyList(), emptyList()
        )

        assertEquals(660L, result["m1"]?.billShares)
        assertEquals(340L, result["m2"]?.billShares)
    }

    @Test
    fun testSettlement2Members() {
        val engine = SettleUpEngine()
        val balances = mapOf("m1" to 500L, "m2" to -500L)
        val settlements = engine.calculateSettlements(balances)

        assertEquals(1, settlements.size)
        assertEquals("m2", settlements[0].fromMemberId)
        assertEquals("m1", settlements[0].toMemberId)
        assertEquals(500L, settlements[0].amountMinor)
    }

    @Test
    fun testSettlement3Members() {
        val engine = SettleUpEngine()
        // m1 owed 1000, m2 owes 600, m3 owes 400
        val balances = mapOf("m1" to 1000L, "m2" to -600L, "m3" to -400L)
        val settlements = engine.calculateSettlements(balances)

        assertEquals(2, settlements.size)
        val totalTransferred = settlements.sumOf { it.amountMinor }
        assertEquals(1000L, totalTransferred)
        assertTrue(settlements.all { it.toMemberId == "m1" })
    }

    @Test
    fun testSettlement6Members() {
        val engine = SettleUpEngine()
        // m1 +500, m2 +300, m3 +200, m4 -400, m5 -400, m6 -200
        val balances = mapOf(
            "m1" to 500L,
            "m2" to 300L,
            "m3" to 200L,
            "m4" to -400L,
            "m5" to -400L,
            "m6" to -200L
        )
        val settlements = engine.calculateSettlements(balances)

        val sumTransferred = settlements.sumOf { it.amountMinor }
        assertEquals(1000L, sumTransferred)
        // Verify no member transfers more than they owe/are owed
        val netMap = mutableMapOf<String, Long>()
        settlements.forEach {
            netMap[it.fromMemberId] = (netMap[it.fromMemberId] ?: 0L) - it.amountMinor
            netMap[it.toMemberId] = (netMap[it.toMemberId] ?: 0L) + it.amountMinor
        }
        assertEquals(-400L, netMap["m4"])
        assertEquals(-400L, netMap["m5"])
        assertEquals(-200L, netMap["m6"])
        assertEquals(500L, netMap["m1"])
        assertEquals(300L, netMap["m2"])
        assertEquals(200L, netMap["m3"])
    }

    @Test
    fun testZeroMealsGuard() {
        val engine = SettleUpEngine()
        val m1 = Member("m1", "Alice", 0, 0L, null, true)
        val expenses = listOf(
            Expense("e1", "Market", 500L, 0L, "m1", ExpenseType.MEAL_POOL.name, "Food", SplitRule.EQUAL.name, "")
        )
        val result = engine.calculateSummary("mon1", 0L, 100000L, listOf(m1), emptyList(), expenses, emptyList())
        assertEquals(0L, result["m1"]?.mealCost)
        assertEquals(500L, result["m1"]?.paidOut)
        assertEquals(500L, result["m1"]?.netBalance)
    }
}
