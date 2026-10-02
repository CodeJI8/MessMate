# Messmate Project Progress

## DONE
- Theme setup (`Color.kt`, `Type.kt`, `Theme.kt`) with Warm Kitchen Ledger design system (CharcoalBase, WarmCream, WarmPaper, Saffron, ChiliCoral, HerbGreen, Fraunces font, Manrope font).
- Core UI components:
  - `PlateVisual.kt`: Custom Canvas donut plate visualization with spring animation and slice tap handling.
  - `TugBarBalance.kt`: Custom tug-bar balance component with spring animation.
  - `FloatingPillDock.kt`: Floating bottom navigation bar with raised center '+' action button.
  - `Layouts.kt`: Custom `MessMateBackground` and `LedgerCard` surface containers.
- Shell UI:
  - `HomeScreen.kt`: Static/dummy preview of "The Table" hero screen.
  - `MainActivity.kt`: Edge-to-edge Scaffold wiring `HomeScreen` and `FloatingPillDock`.
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

## PARTIAL/BROKEN
- None. Project compiles cleanly and passes all unit tests (`8 passed, 0 failed`).

## TODO
- [ ] ViewModels & Architecture:
  - `MessMateApplication.kt`: Manual DI container providing `MessMateRepository` and `MessMateDatabase`.
  - ViewModels for Onboarding, Home, Add Expense, Meals, Ledger, Members, Settle, Month Close, Settings.
- [ ] Screens:
  - Onboarding Screen (Flat setup, currency symbol, add members, "Load sample flat").
  - Home Screen ("The Table", real-time Plate donut, month selector, total spent, tug-bar balances).
  - Add Expense Bottom Sheet (keypad first, payer chips, Meal Pool / Bill toggle, category chips, split selector).
  - Meals Screen (day strip calendar, B/L/D pill chips with long-press guest counter, "Everyone ate" batch button).
  - Ledger Screen (day-grouped transaction feed, swipe to edit/delete with audit logging).
  - Members Screen (add/edit members, mid-month leave date, personal statement view).
  - Settle Screen (animated flow cards: debtor -> amount -> creditor with "Mark as paid" partial/full payment generation).
  - Month Close Screen (review, freeze month, carry forward balances, month history, explicit reopen).
  - Settings Screen (currency, theme toggle, default "assume everyone eats" preference).
- [ ] Extras & Features:
  - Month Summary Image / PDF generation & Android FileProvider sharing.
  - SAF Backup/Restore (single JSON file with schema version & validation).
  - Recurring bill templates & one-tap due confirm card.
  - Receipt photo picking (PickVisualMedia) and app-private storage copy.
  - Audit Log / Expense edit history timeline.
  - UI polish: empty state canvas illustrations, haptics, TalkBack content descriptions, adaptive + monochrome launcher icons, release readiness (R8 rules verified, 0 permissions manifest).
