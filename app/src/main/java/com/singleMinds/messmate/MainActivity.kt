package com.singleminds.messmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.singleminds.messmate.ui.MessMateViewModel
import com.singleminds.messmate.ui.MessMateViewModelFactory
import com.singleminds.messmate.ui.components.FloatingPillDock
import com.singleminds.messmate.ui.components.NavDestination
import com.singleminds.messmate.ui.screens.*
import com.singleminds.messmate.ui.theme.MessMateTheme
import com.singleminds.messmate.utils.SummaryExporter

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MessMateViewModel

    private val exportJsonLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            contentResolver.openOutputStream(it)?.use { stream ->
                viewModel.exportBackup(stream)
            }
        }
    }

    private val importJsonLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // Could implement restore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MessMateApplication
        viewModel = ViewModelProvider(this, MessMateViewModelFactory(app.repository))[MessMateViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            var isDarkTheme by remember { mutableStateOf(false) }
            val darkTheme = when (uiState.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isDarkTheme
            }

            MessMateTheme(darkTheme = darkTheme) {
                if (!uiState.isOnboarded) {
                    OnboardingScreen(
                        onStartFlat = { name, currency, members ->
                            viewModel.setupFlat(name, currency, members, loadSample = false)
                        },
                        onLoadSample = {
                            viewModel.setupFlat("", "", emptyList(), loadSample = true)
                        }
                    )
                } else {
                    var currentDestination by remember { mutableStateOf(NavDestination.Home) }
                    var showAddExpenseSheet by remember { mutableStateOf(false) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settle",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (currentDestination == NavDestination.Settle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { currentDestination = NavDestination.Settle }.padding(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Close Month",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (currentDestination == NavDestination.MonthClose) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { currentDestination = NavDestination.MonthClose }.padding(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (currentDestination == NavDestination.Settings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { currentDestination = NavDestination.Settings }.padding(8.dp)
                                )
                            }
                        },
                        bottomBar = {
                            FloatingPillDock(
                                currentDestination = currentDestination,
                                onNavigate = { currentDestination = it },
                                onAddClick = { showAddExpenseSheet = true }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                            val currency = uiState.flat?.currencySymbol ?: "$"
                            val flatName = uiState.flat?.name ?: "Flat"
                            val monthLabel = uiState.selectedMonth?.label ?: "Current Month"

                            when (currentDestination) {
                                NavDestination.Home -> {
                                    HomeScreen(
                                        flatName = flatName,
                                        monthLabel = monthLabel,
                                        availableMonths = uiState.availableMonths,
                                        mealRate = uiState.mealRate,
                                        totalSpent = uiState.totalSpent,
                                        currencySymbol = currency,
                                        members = uiState.members,
                                        summaryMap = uiState.summaryMap,
                                        onSelectMonth = { viewModel.selectMonth(it) }
                                    )
                                }
                                NavDestination.Meals -> {
                                    MealsScreen(
                                        members = uiState.members,
                                        mealEntries = uiState.meals,
                                        onSaveMeal = { mId, date, b, l, d ->
                                            viewModel.saveMealEntry(mId, date, b, l, d)
                                        },
                                        onEveryoneAte = { date ->
                                            viewModel.batchEveryoneEats(date)
                                        }
                                    )
                                }
                                NavDestination.Ledger -> {
                                    LedgerScreen(
                                        expenses = uiState.expenses,
                                        members = uiState.members,
                                        currencySymbol = currency,
                                        onDeleteExpense = { viewModel.deleteExpense(it) }
                                    )
                                }
                                NavDestination.Members -> {
                                    MembersScreen(
                                        members = uiState.members,
                                        summaryMap = uiState.summaryMap,
                                        currencySymbol = currency,
                                        onAddMember = { name, colorIdx ->
                                            viewModel.addMember(name, colorIdx)
                                        },
                                        onUpdateMember = { viewModel.updateMember(it) }
                                    )
                                }
                                NavDestination.Settle -> {
                                    SettleScreen(
                                        settlements = uiState.settlements,
                                        members = uiState.members,
                                        currencySymbol = currency,
                                        onRecordPayment = { from, to, amount, note ->
                                            viewModel.recordPayment(from, to, amount, note)
                                        }
                                    )
                                }
                                NavDestination.MonthClose -> {
                                    MonthCloseScreen(
                                        currentMonth = uiState.currentMonth,
                                        availableMonths = uiState.availableMonths,
                                        totalSpent = uiState.totalSpent,
                                        mealRate = uiState.mealRate,
                                        currencySymbol = currency,
                                        onCloseMonth = { carryForward ->
                                            viewModel.closeMonth(carryForward)
                                        },
                                        onReopenMonth = { viewModel.reopenMonth(it) }
                                    )
                                }
                                NavDestination.Settings -> {
                                    SettingsScreen(
                                        currentCurrency = currency,
                                        themeMode = uiState.themeMode,
                                        recurringBills = uiState.recurringBills,
                                        auditLogs = uiState.auditLogs,
                                        onCurrencyChanged = { newSym ->
                                            uiState.flat?.let { f ->
                                                viewModel.setupFlat(f.name, newSym, emptyList())
                                            }
                                        },
                                        onThemeModeChanged = { viewModel.setThemeMode(it) },
                                        onExportBackup = { exportJsonLauncher.launch("messmate_backup.json") },
                                        onImportRestore = { importJsonLauncher.launch(arrayOf("application/json")) },
                                        onShareSummaryImage = { SummaryExporter.shareSummaryImage(context, uiState) },
                                        onConfirmRecurringBill = { viewModel.confirmRecurringBill(it) }
                                    )
                                }
                            }
                        }

                        if (showAddExpenseSheet) {
                            AddExpenseSheet(
                                members = uiState.members.filter { it.isActive },
                                currencySymbol = uiState.flat?.currencySymbol ?: "$",
                                onSaveExpense = { title, amount, payerId, type, category, splitRule, note ->
                                    viewModel.addExpense(title, amount, payerId, type, category, splitRule, note)
                                },
                                onDismiss = { showAddExpenseSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
