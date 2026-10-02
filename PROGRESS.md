# Messmate Project Progress

## DONE
- Theme setup (`Color.kt`, `Type.kt`, `Theme.kt`) with Warm Kitchen Ledger design system (CharcoalBase, WarmCream, WarmPaper, Saffron, ChiliCoral, HerbGreen, Fraunces font, Manrope font).
- Core UI components:
  - `PlateVisual.kt`: Custom Canvas donut plate visualization with spring animation and slice tap handling.
  - `TugBarBalance.kt`: Custom tug-bar balance component with spring animation.
  - `FloatingPillDock.kt`: Floating bottom navigation bar with raised center '+' action button.
  - `Layouts.kt`: Custom `MessMateBackground` and `LedgerCard` surface containers.
- Shell UI:
  - `HomeScreen.kt`: Hero screen layout with real data from `MainUiState`.
  - `MainActivity.kt`: Edge-to-edge Scaffold wiring `HomeScreen`, `MealsScreen`, `LedgerScreen`, `MembersScreen`, `SettleScreen`, `MonthCloseScreen`, `SettingsScreen`, and `AddExpenseSheet`.
- Data Layer (Room Database & Entities):
  - Entities: `Flat`, `Member`, `MealEntry`, `Expense`, `ExpenseShare`, `Payment`, `Month`, `AuditLog`, `RecurringBill`, `Enums`.
  - DAOs: `FlatDao`, `MemberDao`, `MealDao`, `ExpenseDao`, `PaymentDao`, `MonthDao`, `AuditDao`, `RecurringBillDao`.
  - `MessMateDatabase`: Room database configuration.
  - `MessMateRepository`: Central repository managing Room flows, IO threading, and sample flat generation.
- Pure Kotlin Business Logic & Engines (100% unit tested, 8 passing tests):
  - `SplitLogic.kt`: Largest-remainder rounding algorithm (`splitWeighted`).
  - `SettleUpEngine.kt`:
    - Meal rate calculation & meal cost allocation.
    - Fixed bill splits with proration by active member days.
    - Net balance formula (`paidOut + paymentsMade - (mealCost + billShares + paymentsReceived)`).
    - Minimum transfer settlement greedy algorithm (debtors -> creditors matching).
    - Tests for 2, 3, and 6 member scenarios and zero-meal guard.
- Screens & Features:
  - Onboarding Screen (Flat name, currency symbol selector, member addition, "Load sample flat").
  - Home Screen ("The Table", Plate donut, month selector, total spent, tug-bar balances).
  - Add Expense Bottom Sheet (direct major unit currency input converted to minor units, payer chips, Meal Pool / Bill switch, category chips).
  - Meals Screen (day strip calendar, B/L/D pill chips with long-press guest counter, "Everyone ate" batch button).
  - Ledger Screen (day-grouped feed with swipe/click delete & audit logging).
  - Members Screen (active/inactive members, add member, personal statement breakdown).
  - Settle Screen (animated flow cards: debtor -> amount -> creditor with "Mark as paid" partial/full payment generation).
  - Month Close Screen (review, freeze month, carry forward toggle, month history, reopen month).
  - Settings Screen (currency selector, theme mode switcher, recurring bills, JSON SAF export, summary image sharing).

## PARTIAL/BROKEN
- None. Project compiles cleanly, builds debug APK successfully, and passes all unit tests (`8 passed, 0 failed`).

## TODO
- [ ] Final release verification (AAB build steps).
