package com.example.healthtracker

import android.content.Context
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch



val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "userData")


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthTrackerTheme {
                MainScreen()
            }
        }
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
            }
        }
    }
}

@Composable
fun MainScreen() {
    val backgroundColor = Color(0xFFb2e1f9)
    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
    ) {
        Column (modifier = Modifier.padding(top = 30.dp)) {
            Text(text = "Daily Wellness Indices",
                fontWeight = FontWeight(1000),
                fontSize = 30.sp,
                color = Color(0xFF7E57C2),
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 10.dp, start = 30.dp, end = 20.dp)
                    .drawBehind {
                        drawLine(
                            color = Color(0xFF7E57C2),
                            start = Offset(0f-100, size.height),
                            end = Offset(size.width+300, size.height),
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

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
fun Preview() {
    HealthTrackerTheme {
        MainScreen()
    }
}