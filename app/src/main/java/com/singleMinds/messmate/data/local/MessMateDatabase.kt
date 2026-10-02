package com.singleminds.messmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.singleminds.messmate.data.local.dao.*
import com.singleminds.messmate.data.local.entity.*

@Database(
    entities = [
        Flat::class,
        Member::class,
        MealEntry::class,
        Expense::class,
        ExpenseShare::class,
        Payment::class,
        Month::class,
        AuditLog::class,
        RecurringBill::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MessMateDatabase : RoomDatabase() {

    abstract fun flatDao(): FlatDao
    abstract fun memberDao(): MemberDao
    abstract fun mealDao(): MealDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun monthDao(): MonthDao
    abstract fun auditDao(): AuditDao
    abstract fun recurringBillDao(): RecurringBillDao

    companion object {
        @Volatile
        private var INSTANCE: MessMateDatabase? = null

        fun getInstance(context: Context): MessMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessMateDatabase::class.java,
                    "messmate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
