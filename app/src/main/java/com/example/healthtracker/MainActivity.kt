package com.example.healthtracker

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.healthtracker.StepTracker
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import androidx.room.*
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.serialization.json.*
import java.util.Locale



val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "userData")

val db_params = mapOf(
    "host" to "healthtracker.c1k2agywgaxv.us-east-2.rds.amazonaws.com",
    "database" to "postgres",
    "user" to "postgres",
    "password" to "12345678"  // Replace with your actual password
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthTrackerTheme {
                ScreenNavigator()
            }
        }
    }
}

@Composable
fun ScreenNavigator() {
    val context = LocalContext.current
    val userIDKey = intPreferencesKey("id")
    val userID by context.userDataStore.data
        .map { preferences ->
            preferences[userIDKey] ?: -42
        }.collectAsState(initial = -42) as State<Int>
    val userEmailKey = stringPreferencesKey("email")
    val userEmail by context.userDataStore.data
        .map { preferences ->
            preferences[userEmailKey] ?: ""
        }.collectAsState(initial = "") as State<String>
    val userPasswordKey = stringPreferencesKey("password")
    val userPassword by context.userDataStore.data
        .map { preferences ->
            preferences[userPasswordKey] ?: ""
        }.collectAsState(initial = "") as State<String>
    val userUsernameKey = stringPreferencesKey("password")
    val userUsername by context.userDataStore.data
        .map { preferences ->
            preferences[userUsernameKey] ?: ""
        }.collectAsState(initial = "") as State<String>

    if (userUsername == "" && userID == -42
        && userPassword =="") {

        RegistrationScreen(context, userIDKey, userEmailKey,
            userPasswordKey, userUsernameKey)
        Text("Hello")

    } else {
        MainScreen(context, userIDKey, userEmailKey,
            userPasswordKey, userUsernameKey)
    }
}


@Composable
fun NonAdjustableQuantity(achievementText:String,context: Context,
                        backgroundColor: Color, innerColor: Color, quantityName: String) {
    val currentQuantityKey = intPreferencesKey(achievementText)
    val currentQuantity by context.userDataStore.data
        .map { preferences ->
            preferences[currentQuantityKey] ?: 0
        }.collectAsState(initial = 0) as State<Int>
    Row (verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)) {
        Text("${currentQuantity} $quantityName",
            color = innerColor,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(10.dp))
    }
}


@Composable
fun AdjustableQuantity(achievementText:String,context: Context, coroutineScope: CoroutineScope,
                       backgroundColor: Color, innerColor: Color, quantityName: String, delta: Int) {
    val currentQuantityKey = intPreferencesKey(achievementText)
    val currentQuantity by context.userDataStore.data
        .map { preferences ->
            preferences[currentQuantityKey] ?: 0
        }.collectAsState(initial = 0) as State<Int>
    Row (verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)) {
        Button(onClick = {
            coroutineScope.launch {
                context.userDataStore.edit { preferences ->
                    preferences[currentQuantityKey] = currentQuantity - delta
                }
            }
        },
            enabled = currentQuantity!=0,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
            modifier = Modifier
                .size(30.dp)
        ) {
            Icon(imageVector = Icons.Default.Remove,
                contentDescription = "remove",
                tint = innerColor,
                modifier = Modifier.fillMaxSize())
        }
        Text("${currentQuantity} $quantityName",
            color = innerColor,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(10.dp))
        Button(onClick = { coroutineScope.launch {
            context.userDataStore.edit { preferences ->
                preferences[currentQuantityKey] = currentQuantity + delta
            }
        } },
            contentPadding = PaddingValues(0.dp),
            enabled = currentQuantity<4499,
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
            modifier = Modifier
                .size(30.dp)
        ) {
            Icon(imageVector = Icons.Default.Add,
                contentDescription = "remove",
                tint = innerColor,
                modifier = Modifier
                    .fillMaxSize())
        }
    }
}

private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

    if (runningServices != null) {
        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    }

    return false
}


@Composable
fun HealthStat(achievementText: String, backgroundColor: Color,
               iconResId: Int, innerColor: Color, context: Context
) {
    val icon = ImageVector.vectorResource(id = iconResId)
    val coroutineScope = rememberCoroutineScope()

    //var quantity by rememberSaveable { mutableStateOf(0) }
    Surface(
        Modifier
            .height(180.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 15.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 5.dp

    ) {
        Row (verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()) {
            Icon(imageVector = icon,
                contentDescription = null,
                Modifier.size(size = 65.dp),
                tint = innerColor)
            Text(achievementText,
                color = innerColor,
                fontSize = 23.sp,
                fontWeight = FontWeight(1000),
                modifier = Modifier.padding(start = 35.dp)
            )
            if (achievementText=="Water") {
                AdjustableQuantity(achievementText, context, coroutineScope,
                    backgroundColor, innerColor, "ml", 250)

            } else if (achievementText=="Calories") {
                AdjustableQuantity(achievementText, context, coroutineScope,
                    backgroundColor, innerColor, "cal", 250)

            } else if (achievementText=="Exercise") {
                AdjustableQuantity(achievementText, context, coroutineScope,
                    backgroundColor, innerColor, "min", 15)
            } else if (achievementText == "Steps") {
                val context = LocalContext.current
                var stepCount by remember { mutableStateOf(0) }
                var hasStepCounter by remember { mutableStateOf(false) }

                // Check for step counter sensor
                LaunchedEffect(Unit) {
                    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                    hasStepCounter = (stepCounter != null)
                }

                if (hasStepCounter) {
                    // Register broadcast receiver
                    val broadcastReceiver = remember {
                        object : BroadcastReceiver() {
                            override fun onReceive(context: Context?, intent: Intent?) {
                                if (intent?.action == StepCounterService.ACTION_STEP_COUNT_UPDATE) {
                                    stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0)
                                }
                            }
                        }
                    }

                    // Start service and register receiver
                    DisposableEffect(key1 = context) {
                        val filter = IntentFilter(StepCounterService.ACTION_STEP_COUNT_UPDATE)
                        context.registerReceiver(
                            broadcastReceiver, filter,
                            Context.RECEIVER_NOT_EXPORTED
                        )

                        if (!isServiceRunning(context, StepCounterService::class.java)) {
                            val intent = Intent(context, StepCounterService::class.java)
                            context.startService(intent)
                        }

                        onDispose {
                            context.unregisterReceiver(broadcastReceiver)
                        }
                    }
                    NonAdjustableQuantity(achievementText, context,
                        backgroundColor, innerColor, "$stepCount")
                } else {
                    // Alternative for devices without step counter
                    Text(
                        "Step counter not available",
                        color = innerColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun TabBar(navController: NavController, containerColor: Color) {
    NavigationBar(containerColor = containerColor) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        listOf(
            "home" to Icons.Default.Home,
            "profile" to Icons.Default.Person
        ).forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(route.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                }) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(context: Context, userIDKey: Preferences.Key<Int>,
               userEmailKey: Preferences.Key<String>,
               userPasswordKey: Preferences.Key<String>,
               userUsernameKey: Preferences.Key<String>) {
    val navController = rememberNavController()
    val backgroundColor = Color(0xFFb2e1f9)

    Scaffold(
        containerColor = backgroundColor, // Set the background color for the entire Scaffold
        bottomBar = { TabBar(navController = navController, containerColor = backgroundColor) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Only apply bottom padding
        ) {
            composable("home") {
                HomeScreen(backgroundColor)
            }
            composable("profile") {
                ProfileScreen(context, userIDKey, userEmailKey,
                    userPasswordKey, userUsernameKey)
            }
            // Add other destinations as needed
        }
    }
}

@Composable
fun HomeScreen(backgroundColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(top = 30.dp)) {
            Text(
                text = "Daily Wellness Indices",
                fontWeight = FontWeight(1000),
                fontSize = 30.sp,
                color = Color(0xFF7E57C2),
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 10.dp, start = 30.dp, end = 20.dp)
                    .drawBehind {
                        drawLine(
                            color = Color(0xFF7E57C2),
                            start = Offset(0f - 100, size.height),
                            end = Offset(size.width + 300, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            )
            HealthStat("Water", Color(0xFF083042),
                R.drawable.water, backgroundColor, LocalContext.current)
            HealthStat("Steps", Color(0xFF00796B),
                R.drawable.steps, backgroundColor, LocalContext.current)
            HealthStat("Calories", Color(0xFF4285f4),
                R.drawable.calories, backgroundColor, LocalContext.current)
            HealthStat("Exercise", Color(0xFFFF7043),
                R.drawable.exercise, backgroundColor, LocalContext.current)
        }
    }
}


@Composable
fun ProfileButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // Set a specific height for the button
            colors = ButtonDefaults.buttonColors(contentColor=Color(0xFFb2e1f9),
                containerColor = Color(0xFF4285f4)),
            shape = RoundedCornerShape(20.dp), // This creates square corners
            contentPadding = PaddingValues(0.dp) // This removes default padding
        ) {
            Text(text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight(1000))
        }
    }
}


fun APIRequest(endpoint: String, params: Map<String, String>, requestMethod: String): Map<String, Any?> {
    val baseUrl = "https://trbdbmpwgt.us-east-2.awsapprunner.com"

    val query = params.map { (k, v) ->
        "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
    }.joinToString("&")

    val urlString = "$baseUrl/$endpoint?$query"

    try {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = requestMethod
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Json.parseToJsonElement(response).jsonObject.toMap()
        } else {
            val errorStream = connection.errorStream
            val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available"
            mapOf("error" to "HTTP $responseCode", "message" to errorResponse)
        }
    } catch (e: Exception) {
        return mapOf("error" to "Exception", "message" to e.message)
    }
}


@Composable
fun ProfileScreen(context: Context, userIDKey: Preferences.Key<Int>,
                  userEmailKey: Preferences.Key<String>,
                  userPasswordKey: Preferences.Key<String>,
                  userUsernameKey: Preferences.Key<String>) {
    val coroutineScope = rememberCoroutineScope()
    // Your profile screen content
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Blue)
        ) {
            Text(
                text = "AVG daily index",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column {
                Row (modifier = Modifier.padding(top=50.dp)) {}

                // Example functions to be called on button click
                fun logout() {
                    coroutineScope.launch {
                        context.userDataStore.edit { preferences ->
                            preferences[userIDKey] = -42
                            preferences[userEmailKey] = ""
                            preferences[userPasswordKey] = ""
                            preferences[userUsernameKey] = ""
                        }
                    }
                }

                fun deleteAccount() {
                    coroutineScope.launch(Dispatchers.IO) {
                        context.userDataStore.edit { preferences ->
                            val params = mapOf(
                                "id" to preferences[userIDKey].toString()
                            )
                            val result = APIRequest("deleteUSERS", params, "DELETE")
                            println(result)
                        }


                    }
                }

                ProfileButton("Logout") {
                    logout()
                }
                ProfileButton("Delete account") {
                    deleteAccount()
                    logout()
                }

            }
        }
    }
}


@Composable
fun RegScreenButton(text: String, onClick: (String) -> Unit,
                    buttonColor: Color = Color(0xFF445e91),
                    textColor: Color = Color.White,
                    topStartCorner: Int = 0, bottomStartCorner: Int = 0,
                    topEndCorner: Int = 0, bottomEndCorner: Int = 0,
                    alpha: Float = 1.0f) {
    Button(
        onClick = { onClick(text) },
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = alpha)),
        shape = MaterialTheme.shapes.small.copy(
            topStart = CornerSize(topStartCorner.dp),
            bottomStart = CornerSize(bottomStartCorner.dp),
            topEnd = CornerSize(topEndCorner.dp),
            bottomEnd = CornerSize(bottomEndCorner.dp)),
        modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)
    ) {
        Text(text,
            Modifier.padding(0.dp),
            color = textColor.copy(alpha = alpha))
    }
}

@Composable
fun RegTextField(text: String, onTextChange: (String) -> Unit,
                 backgroundColor: Color, description: String, imageVector: ImageVector) {
    TextField(value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .width(220.dp)
            .border(
                width = 0.5.dp,
                color = Color.Black,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        label = { Text(description) },
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = imageVector,
                contentDescription = description)
        })
}

// Extension function to convert JsonObject to Map
fun JsonObject.toMap(): Map<String, Any?> = entries.associate { (key, value) ->
    key to when (value) {
        is JsonObject -> value.toMap()
        is JsonArray -> value.toList().map { if (it is JsonObject) it.toMap() else it.toString() }
        JsonNull -> null
        else -> value.toString()
    }
}


@Composable
fun RegistrationScreen(context: Context, userIDKey: Preferences.Key<Int>,
                       userEmailKey: Preferences.Key<String>,
                       userPasswordKey: Preferences.Key<String>,
                       userUsernameKey: Preferences.Key<String>) {

    val backgroundColor = Color(0xFFb2e1f9)
    val surfaceColor = Color(0xFF8ac7e6)
    val cardColor = Color(0xFF62aed2)

    var selectedButton by rememberSaveable { mutableStateOf("Register") }

    var emailText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var usernameText by rememberSaveable { mutableStateOf("") }


    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = backgroundColor)
    ) {

        // Login or Registration Field
        Surface (
            color = surfaceColor,
            shape = CutCornerShape(20.dp),
            shadowElevation = 7.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .then(
                    if (selectedButton == "Login") {
                        Modifier.size(300.dp)
                    } else {
                        Modifier
                            .width(300.dp)
                            .height(380.dp)
                    }
                )

        ) {

            // Content in surface
            Column (modifier = Modifier
                .fillMaxSize()) {

                // "Login / Register" Box
                Box (modifier = Modifier
                    .fillMaxWidth(),
                    contentAlignment = Alignment.Center) {

                    val roundCornerMeasure = 20

                    Card (
                        shape = RoundedCornerShape(roundCornerMeasure.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier
                            .padding(top = 20.dp)
                    ) {

                        // "Login / Register" text
                        Row (
                            Modifier
                                .height(IntrinsicSize.Min)
                        ) {
                            if (selectedButton == "Login") {
                                // Login button
                                RegScreenButton(
                                    "Login",
                                    { buttonText -> selectedButton = buttonText },
                                    topEndCorner = roundCornerMeasure,
                                    bottomEndCorner = roundCornerMeasure
                                )

                                // Divider
                                HorizontalDivider(Modifier.size(3.dp))

                                // Register button
                                RegScreenButton(
                                    "Register",
                                    { buttonText -> selectedButton = buttonText },
                                    //buttonColor = cardColor, textColor = cardColor,
                                    topStartCorner = roundCornerMeasure,
                                    bottomStartCorner = roundCornerMeasure,
                                    alpha = 0f
                                )
                            } else if (selectedButton == "Register") {
                                // Login button
                                RegScreenButton(
                                    "Login",
                                    { buttonText -> selectedButton = buttonText },
                                    buttonColor = cardColor, textColor = cardColor,
                                    topEndCorner = roundCornerMeasure,
                                    bottomEndCorner = roundCornerMeasure,
                                    alpha = 0f
                                )

                                // Divider
                                HorizontalDivider(Modifier.size(3.dp))

                                // Register button
                                RegScreenButton(
                                    "Register",
                                    { buttonText -> selectedButton = buttonText },
                                    topStartCorner = roundCornerMeasure,
                                    bottomStartCorner = roundCornerMeasure
                                )
                            }
                        }
                    }
                }
                // Data Fields
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                    contentAlignment = Alignment.Center) {

                    Column {

                        RegTextField(emailText, {newText -> emailText = newText},
                            cardColor, "email", Icons.Default.Mail)
                        VerticalDivider(Modifier.size(15.dp))
                        RegTextField(passwordText, {newText -> passwordText = newText},
                            cardColor, "password", Icons.Default.Lock)

                        if (selectedButton == "Register") {
                            VerticalDivider(Modifier.size(15.dp))
                            RegTextField(usernameText, {newText -> usernameText = newText},
                                cardColor, "username", Icons.Default.AccountCircle)
                        }

                        // Submit button
                        val coroutineScope = rememberCoroutineScope()

//                        val context = LocalContext.current - NO NEED
                        Box(modifier = Modifier
                            .padding(horizontal = 63.dp, vertical = 25.dp)
                        ) {
                            Button(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (selectedButton == "Register") {

                                        val params = mapOf(
                                            "username" to usernameText,
                                            "email" to emailText,
                                            "password" to passwordText
                                        )
                                        val result = APIRequest("createUSERS", params, "POST")
                                        println(result)
                                        val paramsLogin = mapOf(
                                            "email" to emailText
                                        )
                                        val resultLogin = APIRequest("readUSERS", paramsLogin, "GET")
                                        println(resultLogin)
                                        val data = resultLogin["data"] as Map<String, Any>
                                        println(data)

                                        context.userDataStore.edit { preferences ->
                                            preferences[userIDKey] = (data["id"] as String).toInt()
                                            preferences[userEmailKey] = data["email"] as String
                                            preferences[userPasswordKey] = data["password"] as String
                                            preferences[userUsernameKey] = data["username"] as String
                                        }

                                    } else if (selectedButton == "Login") {
                                        val params = mapOf(
                                            "email" to emailText
                                        )
                                        val result = APIRequest("readUSERS", params, "GET")
                                        println(result)
                                        if (result["data"]!=null) {
                                            val data = result["data"] as Map<String, Any>

                                            context.userDataStore.edit { preferences ->
                                                preferences[userIDKey] = (data["id"] as String).toInt()
                                                preferences[userEmailKey] = data["email"] as String
                                                preferences[userPasswordKey] = data["password"] as String
                                                preferences[userUsernameKey] = data["username"] as String
                                            }
                                            println("Problem is not here")
                                        } else {
                                            context.userDataStore.edit { preferences ->
                                                preferences[userIDKey] = -42
                                                preferences[userEmailKey] = ""
                                                preferences[userPasswordKey] = ""
                                                preferences[userUsernameKey] = ""
                                            }
                                        }
                                    }
                                }

                            })
                            {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
fun Preview() {
    HealthTrackerTheme {
        ScreenNavigator()
    }
}