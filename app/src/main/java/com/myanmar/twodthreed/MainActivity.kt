package com.myanmar.twodthreed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

private val Navy = Color(0xFF08285F)
private val Blue = Color(0xFF1557B8)
private val Gold = Color(0xFFFFD34D)
private val Pale = Color(0xFFB8D0FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MyanmarResultsApp() } }
    }
}

@Composable
private fun MyanmarResultsApp() {
    var tab by remember { mutableStateOf("Live") }
    var live by remember { mutableStateOf<JsonObject?>(null) }
    var history by remember { mutableStateOf<JsonObject?>(null) }
    var latest3d by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                when (tab) {
                    "Live" -> live = ApiClient.api.live2d()
                    "2D" -> history = ApiClient.api.history2d()
                    "3D" -> latest3d = ApiClient.api.latest3d()
                }
            } catch (e: Exception) {
                error = "ဒေတာရယူလို့မရပါ။ အင်တာနက်နဲ့ API server ကို စစ်ဆေးပါ။"
            } finally { loading = false }
        }
    }

    LaunchedEffect(tab) { refresh() }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy, Blue, Color(0xFF174EAB))))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Myanmar 2D", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("LIVE", color = Gold, fontWeight = FontWeight.Bold)
            }
            if (error != null) Text(error!!, color = Color(0xFFFFD5D5), modifier = Modifier.padding(vertical = 8.dp))
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold)
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (tab) {
                    "Live" -> item {
                        val d = live?.obj("data")
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(d?.str("live") ?: "--", color = Color.White, fontSize = 112.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Live 2D ရလဒ်", color = Pale, fontSize = 17.sp)
                            Spacer(Modifier.height(20.dp))
                            ResultCard("12:01 PM", "MORNING", d?.str("result_1100") ?: "--", d?.str("live_set") ?: "--", d?.str("live_val") ?: "--")
                            ResultCard("4:30 PM", "EVENING", d?.str("live") ?: "--", d?.str("live_set") ?: "--", d?.str("live_val") ?: "--")
                            Text("ရက်စွဲ  ${d?.str("date") ?: "--"}", color = Pale, modifier = Modifier.padding(12.dp))
                        }
                    }
                    "2D" -> {
                        val rows = history?.array("data")
                        item { Text("2D ရလဒ်မှတ်တမ်း", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                        if (rows == null || rows.size() == 0) item { Text("မှတ်တမ်း မရရှိသေးပါ", color = Pale) }
                        else items(rows.size()) { index ->
                            val row = rows.get(index).asJsonObject
                            Surface(color = Color(0x332D69C1), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(18.dp)) {
                                    Text(row.str("date") ?: "", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Text("11:00  ${row.str("result_1100") ?: "--"}     12:00  ${row.str("result_1200") ?: "--"}", color = Color.White, fontSize = 18.sp)
                                    Text("3:00    ${row.str("result_300") ?: "--"}     4:30    ${row.str("result_430") ?: "--"}", color = Color.White, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                    else -> item {
                        val d = latest3d?.obj("data")
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("3D နောက်ဆုံးရလဒ်", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(32.dp))
                            Text(d?.str("threed_number") ?: "---", color = Gold, fontSize = 92.sp, fontWeight = FontWeight.ExtraBold)
                            Text("ပထမဆု  ${d?.str("first_prize_number") ?: "------"}", color = Color.White, fontSize = 22.sp)
                            Text("ထွက်ရက်  ${d?.str("draw_date") ?: "--"}", color = Pale, modifier = Modifier.padding(10.dp))
                            val sched = latest3d?.obj("schedule")
                            if (sched != null) Text("နောက်တစ်ကြိမ်  ${sched.str("next_draw_date") ?: "--"}   ${sched.str("phase") ?: ""}", color = Pale)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).background(Color(0xFF071F4F), RoundedCornerShape(36.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Live", "2D", "3D").forEach { label ->
                    val selected = tab == label
                    TextButton(onClick = { tab = label }, colors = ButtonDefaults.textButtonColors(contentColor = if (selected) Navy else Pale), modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp)) {
                        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = if (selected) Modifier.background(Gold, RoundedCornerShape(24.dp)).padding(horizontal = 14.dp, vertical = 7.dp) else Modifier)
                    }
                }
                TextButton(onClick = { refresh() }) { Text("↻", color = Gold, fontSize = 24.sp) }
            }
        }
    }
}

@Composable
private fun ResultCard(time: String, badge: String, result: String, set: String, value: String) {
    Surface(color = Color(0x332D69C1), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(color = Gold, shape = RoundedCornerShape(20.dp)) { Text(badge, color = Navy, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)) }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Pale.copy(alpha = .35f))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) { Text("SET", color = Pale); Text(set, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) { Text("VALUE", color = Pale); Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) { Text("2D", color = Pale); Text(result, color = Gold, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

private fun JsonObject?.obj(key: String): JsonObject? = this?.getAsJsonObject(key)
private fun JsonObject?.str(key: String): String? = this?.get(key)?.takeUnless { it.isJsonNull }?.asString
private fun JsonObject?.array(key: String) = this?.getAsJsonArray(key)
