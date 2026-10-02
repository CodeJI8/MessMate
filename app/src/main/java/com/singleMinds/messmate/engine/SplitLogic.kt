package com.singleminds.messmate.engine

import kotlin.math.floor

object SplitLogic {
    fun splitWeighted(
        totalMinor: Long,
        memberIds: List<String>,
        weights: List<Double>
    ): Map<String, Long> {
        if (memberIds.isEmpty() || totalMinor == 0L) return emptyMap()
        if (memberIds.size == 1) return mapOf(memberIds[0] to totalMinor)

        val totalWeight = weights.sum()
        val effectiveWeights = if (totalWeight <= 0.0) List(memberIds.size) { 1.0 } else weights
        val sumWeight = effectiveWeights.sum()

        data class ItemShare(
            val memberId: String,
            val index: Int,
            val exact: Double,
            val floor: Long,
            val remainder: Double
        )

        val shares = memberIds.indices.map { i ->
            val w = effectiveWeights[i]
            val exact = totalMinor.toDouble() * (w / sumWeight)
            val fl = floor(exact).toLong()
            val rem = exact - fl
            ItemShare(memberIds[i], i, exact, fl, rem)
        }

        val allocated = shares.sumOf { it.floor }
        var remainderMinor = (totalMinor - allocated).toInt()

        val sortedByRemainder = shares.sortedWith(
            compareByDescending<ItemShare> { it.remainder }
                .thenBy { it.index }
        )

        val result = mutableMapOf<String, Long>()
        sortedByRemainder.forEach { item ->
            result[item.memberId] = item.floor
        }

        var i = 0
        while (remainderMinor > 0 && sortedByRemainder.isNotEmpty()) {
            val targetId = sortedByRemainder[i % sortedByRemainder.size].memberId
            result[targetId] = (result[targetId] ?: 0L) + 1L
            remainderMinor--
            i++
        }

        return result
    }
}
