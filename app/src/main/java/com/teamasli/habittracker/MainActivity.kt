package com.teamasli.habittracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as HabitApp
        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T = HabitViewModel(app.repository) as T
            }
        )[HabitViewModel::class.java]

        setContent {
            MaterialTheme {
                HabitTrackerApp(viewModel)
            }
        }
    }
}

class HabitApp : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            this,
            HabitDatabase::class.java,
            "habit-tracker.db"
        ).build()
    }

    val repository by lazy { HabitRepository(database.habitDao()) }
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val reason: String,
    val streakDays: Int = 0,
    val bestStreakDays: Int = 0
)

@Entity(tableName = "urges")
data class UrgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val triggerName: String,
    val intensity: Int,
    val note: String
)

@Entity(tableName = "relapses")
data class RelapseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val triggerName: String,
    val note: String
)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getHabits(): Flow<List<HabitEntity>>

    @Insert
    suspend fun addHabit(habit: HabitEntity)

    @Insert
    suspend fun addUrge(urge: UrgeEntity)

    @Insert
    suspend fun addRelapse(relapse: RelapseEntity)

    @Query("SELECT COUNT(*) FROM urges")
    fun urgesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM relapses")
    fun relapsesCount(): Flow<Int>

    @Query("UPDATE habits SET streakDays = streakDays + 1, bestStreakDays = MAX(bestStreakDays, streakDays + 1) WHERE id = :habitId")
    suspend fun incrementStreak(habitId: Int)

    @Query("UPDATE habits SET streakDays = 0 WHERE id = :habitId")
    suspend fun resetStreak(habitId: Int)
}

@Database(entities = [HabitEntity::class, UrgeEntity::class, RelapseEntity::class], version = 1)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

data class DashboardState(
    val habits: List<HabitEntity> = emptyList(),
    val urges: Int = 0,
    val relapses: Int = 0
)

class HabitRepository(private val dao: HabitDao) {
    fun dashboard(): Flow<DashboardState> = combine(
        dao.getHabits(),
        dao.urgesCount(),
        dao.relapsesCount()
    ) { habits, urges, relapses ->
        DashboardState(habits, urges, relapses)
    }

    suspend fun addHabit(name: String, reason: String) = dao.addHabit(HabitEntity(name = name, reason = reason))

    suspend fun logUrge(habitId: Int, trigger: String, intensity: Int, note: String) {
        dao.addUrge(UrgeEntity(habitId = habitId, triggerName = trigger, intensity = intensity, note = note))
        dao.incrementStreak(habitId)
    }

    suspend fun logRelapse(habitId: Int, trigger: String, note: String) {
        dao.addRelapse(RelapseEntity(habitId = habitId, triggerName = trigger, note = note))
        dao.resetStreak(habitId)
    }
}

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {
    val dashboard = repository.dashboard().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun addHabit(name: String, reason: String) {
        viewModelScope.launch { repository.addHabit(name, reason) }
    }

    fun logUrge(habitId: Int, trigger: String, intensity: Int, note: String) {
        viewModelScope.launch { repository.logUrge(habitId, trigger, intensity, note) }
    }

    fun logRelapse(habitId: Int, trigger: String, note: String) {
        viewModelScope.launch { repository.logRelapse(habitId, trigger, note) }
    }
}

private data class NavItem(val route: String, val label: String)

@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    val items = listOf(NavItem("home", "Home"), NavItem("insights", "Insights"), NavItem("history", "History"))

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination?.route
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = current == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = { Text(item.label.first().toString()) }
                    )
                }
            }
        }
    ) { padding ->
        AppNavHost(navController, viewModel, padding)
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, viewModel: HabitViewModel, padding: PaddingValues) {
    NavHost(navController = navController, startDestination = "welcome", modifier = Modifier.padding(padding)) {
        composable("welcome") {
            WelcomeScreen(onStart = {
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                viewModel,
                onAddHabit = { navController.navigate("addHabit") },
                onLogUrge = { navController.navigate("logUrge") },
                onLogRelapse = { navController.navigate("logRelapse") },
                onTools = { navController.navigate("tools") }
            )
        }
        composable("addHabit") { AddHabitScreen(viewModel) { navController.popBackStack() } }
        composable("logUrge") { LogUrgeScreen(viewModel) { navController.popBackStack() } }
        composable("logRelapse") { LogRelapseScreen(viewModel) { navController.popBackStack() } }
        composable("tools") { CopingToolsScreen { navController.popBackStack() } }
        composable("insights") { InsightsScreen(viewModel) }
        composable("history") { HistoryScreen(viewModel) }
    }
}

@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("IRON Will Lite", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("No login. No notifications. Just progress.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start Tracking") }
    }
}

@Composable
private fun HomeScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onLogUrge: () -> Unit,
    onLogRelapse: () -> Unit,
    onTools: () -> Unit
) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Track habits locally on this device.")
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Habits: ${state.habits.size}")
                    Text("Total urges: ${state.urges}")
                    Text("Total relapses: ${state.relapses}")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddHabit, modifier = Modifier.weight(1f)) { Text("Add Habit") }
                Button(onClick = onLogUrge, modifier = Modifier.weight(1f)) { Text("Log Urge") }
            }
        }
        item {
            Button(onClick = onLogRelapse, modifier = Modifier.fillMaxWidth()) { Text("I Relapsed") }
        }
        item {
            TextButton(onClick = onTools, modifier = Modifier.fillMaxWidth()) { Text("Open Coping Tools") }
        }
        items(state.habits) { habit ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(habit.name, fontWeight = FontWeight.SemiBold)
                    Text("Current streak: ${habit.streakDays}d • Best: ${habit.bestStreakDays}d")
                    if (habit.reason.isNotBlank()) Text("Why: ${habit.reason}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitScreen(viewModel: HabitViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Add Habit", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Habit name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Why quit?") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    viewModel.addHabit(name.trim(), reason.trim())
                    onDone()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Habit")
        }
    }
}

@Composable
private fun LogUrgeScreen(viewModel: HabitViewModel, onDone: () -> Unit) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    var trigger by remember { mutableStateOf("Stress") }
    var note by remember { mutableStateOf("") }
    var intensity by remember { mutableIntStateOf(5) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Log Urge", style = MaterialTheme.typography.headlineSmall)
        Text("Trigger")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Stress", "Boredom", "Social", "Other").forEach {
                TextButton(onClick = { trigger = it }) { Text(it) }
            }
        }
        Text("Intensity: $intensity")
        Slider(value = intensity.toFloat(), onValueChange = { intensity = it.toInt() }, valueRange = 1f..10f)
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            state.habits.firstOrNull()?.let {
                viewModel.logUrge(it.id, trigger, intensity, note)
                onDone()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Urge")
        }
    }
}

@Composable
private fun LogRelapseScreen(viewModel: HabitViewModel, onDone: () -> Unit) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    var trigger by remember { mutableStateOf("Stress") }
    var note by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Log Relapse", style = MaterialTheme.typography.headlineSmall)
        Text("Setbacks happen. Let's learn and move forward.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Stress", "Boredom", "Social", "Other").forEach {
                TextButton(onClick = { trigger = it }) { Text(it) }
            }
        }
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Reflection") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            state.habits.firstOrNull()?.let {
                viewModel.logRelapse(it.id, trigger, note)
                onDone()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save & Continue")
        }
    }
}

@Composable
private fun CopingToolsScreen(onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Coping Tools", style = MaterialTheme.typography.headlineSmall)
        listOf(
            "2-Minute Breathing",
            "Delay 10 Minutes",
            "Read My Reasons",
            "Grounding Exercise (5-4-3-2-1)"
        ).forEach { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(item, modifier = Modifier.padding(16.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("I'm okay now") }
    }
}

@Composable
private fun InsightsScreen(viewModel: HabitViewModel) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Insights", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Best streak: ${state.habits.maxOfOrNull { it.bestStreakDays } ?: 0} days")
                Text("Most active trigger logging: ${state.urges} urges captured")
                Text("Relapses tracked: ${state.relapses}")
            }
        }
    }
}

@Composable
private fun HistoryScreen(viewModel: HabitViewModel) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        Text("Summary timeline")
        Text("• Habits created: ${state.habits.size}")
        Text("• Urges logged: ${state.urges}")
        Text("• Relapses logged: ${state.relapses}")
        if (state.habits.isEmpty()) {
            Text("No events yet. Add your first habit to begin.")
        }
    }
}
