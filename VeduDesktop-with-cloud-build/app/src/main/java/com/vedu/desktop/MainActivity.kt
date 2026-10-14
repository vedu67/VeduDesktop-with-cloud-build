package com.vedu.desktop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val Ink = Color(0xFF101827)
private val Panel = Color(0xFF182337)
private val Panel2 = Color(0xFF22314A)
private val Cyan = Color(0xFF68E1FD)
private val White = Color(0xFFF4F7FB)
private val Muted = Color(0xFFB5C1D3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VeduDesktopApp() }
    }
}

private enum class AppKind(val title: String, val icon: String) {
    NOTES("Notes", "📝"),
    CALCULATOR("Calculator", "🧮"),
    FILES("Files", "📁"),
    ABOUT("About Vedu", "💻")
}

private data class DesktopWindow(
    val id: Int,
    val kind: AppKind,
    val x: Float,
    val y: Float,
    val minimized: Boolean = false
)

@Composable
private fun VeduDesktopApp() {
    var windows by remember { mutableStateOf(listOf<DesktopWindow>()) }
    var nextId by remember { mutableIntStateOf(1) }
    var startMenu by remember { mutableStateOf(false) }
    var quickPanel by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("Welcome to Vedu Desktop!\\n\\nTap here and start writing.") }
    var calcInput by remember { mutableStateOf("0") }

    fun openApp(kind: AppKind) {
        val existing = windows.firstOrNull { it.kind == kind }
        windows = if (existing != null) {
            windows.map { if (it.id == existing.id) it.copy(minimized = false) else it }
        } else {
            windows + DesktopWindow(nextId, kind, 28f + (windows.size * 18f), 35f + (windows.size * 18f))
        }
        if (existing == null) nextId++
        startMenu = false
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF101827), Color(0xFF173B58), Color(0xFF315C78)))
        )
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Decorative desktop wallpaper
        Box(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 58.dp, end = 22.dp)
                .size(170.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x5568E1FD), Color.Transparent)))
        )
        Column(Modifier.align(Alignment.TopStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopShortcut("📝", "Notes") { openApp(AppKind.NOTES) }
            DesktopShortcut("🧮", "Calculator") { openApp(AppKind.CALCULATOR) }
            DesktopShortcut("📁", "Files") { openApp(AppKind.FILES) }
            DesktopShortcut("💻", "About") { openApp(AppKind.ABOUT) }
        }

        windows.filter { !it.minimized }.forEach { win ->
            DesktopAppWindow(
                window = win,
                screenWidth = screenW,
                screenHeight = screenH,
                noteText = noteText,
                onNoteChange = { noteText = it },
                calcInput = calcInput,
                onCalcInputChange = { calcInput = it },
                onMove = { id, dx, dy ->
                    windows = windows.map { if (it.id == id) it.copy(x = (it.x + dx).coerceAtLeast(0f), y = (it.y + dy).coerceAtLeast(0f)) else it }
                },
                onMinimize = { id -> windows = windows.map { if (it.id == id) it.copy(minimized = true) else it } },
                onClose = { id -> windows = windows.filterNot { it.id == id } }
            )
        }

        if (startMenu) {
            Column(
                Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 66.dp)
                    .widthIn(max = 300.dp).fillMaxWidth(0.82f)
                    .clip(RoundedCornerShape(20.dp)).background(Color(0xF21A2639))
                    .border(1.dp, Color(0x4468E1FD), RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Vedu Desktop", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Your apps", color = Muted, fontSize = 12.sp)
                AppKind.values().forEach { kind ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable { openApp(kind) }.background(Panel).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(kind.icon, fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(kind.title, color = White, fontSize = 15.sp)
                    }
                }
            }
        }

        if (quickPanel) {
            Column(
                Modifier.align(Alignment.TopEnd).padding(12.dp).width(220.dp)
                    .clip(RoundedCornerShape(18.dp)).background(Color(0xF21A2639))
                    .border(1.dp, Color(0x4468E1FD), RoundedCornerShape(18.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Quick panel", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Desktop session", color = Muted, fontSize = 13.sp)
                Text("• Touch controls enabled", color = White, fontSize = 13.sp)
                Text("• Windows run inside this app", color = White, fontSize = 13.sp)
                Button(onClick = { quickPanel = false }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
            }
        }

        // Taskbar
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color(0xEE0B1220)).padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF21445B), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(42.dp).clickable { startMenu = !startMenu; quickPanel = false }
            ) {
                Box(contentAlignment = Alignment.Center) { Text("V", color = Cyan, fontWeight = FontWeight.Black, fontSize = 22.sp) }
            }
            Spacer(Modifier.width(8.dp))
            windows.forEach { win ->
                Surface(
                    color = if (!win.minimized) Panel2 else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(end = 4.dp).size(42.dp).clickable {
                        windows = windows.map { if (it.id == win.id) it.copy(minimized = !it.minimized) else it }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(win.kind.icon, fontSize = 20.sp) }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.clickable { quickPanel = !quickPanel; startMenu = false }.padding(horizontal = 6.dp)) {
                Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()), color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()), color = Muted, fontSize = 10.sp)
            }
            IconTextButton("⚙") { quickPanel = !quickPanel; startMenu = false }
        }
    }
}

@Composable
private fun DesktopShortcut(icon: String, label: String, onClick: () -> Unit) {
    Column(
        Modifier.width(68.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x44202F45)),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 26.sp) }
        Text(label, color = White, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun IconTextButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.padding(start = 2.dp).size(36.dp).clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }.background(Panel),
        contentAlignment = Alignment.Center
    ) { Text(text, color = White, fontSize = 18.sp) }
}

@Composable
private fun DesktopAppWindow(
    window: DesktopWindow,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    noteText: String,
    onNoteChange: (String) -> Unit,
    calcInput: String,
    onCalcInputChange: (String) -> Unit,
    onMove: (Int, Float, Float) -> Unit,
    onMinimize: (Int) -> Unit,
    onClose: (Int) -> Unit
) {
    var expanded by remember(window.id) { mutableStateOf(false) }
    val width = if (expanded) (screenWidth - 20.dp).coerceAtLeast(250.dp) else screenWidth.coerceAtMost(350.dp)
    val height = if (expanded) (screenHeight - 82.dp).coerceAtLeast(220.dp) else (screenHeight * 0.54f).coerceIn(230.dp, 430.dp)
    val x = if (expanded) 10.dp else window.x.dp
    val y = if (expanded) 10.dp else window.y.dp

    Column(
        Modifier.offset { IntOffset(x.roundToPx(), y.roundToPx()) }
            .width(width).height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F9FC))
            .border(1.dp, Color(0xFF91A6BC), RoundedCornerShape(16.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().background(Panel).pointerInput(window.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (!expanded) onMove(window.id, dragAmount.x / density, dragAmount.y / density)
                }
            }.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(window.kind.icon, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(window.kind.title, color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text("—", color = Muted, modifier = Modifier.clickable { onMinimize(window.id) }.padding(horizontal = 7.dp), fontSize = 19.sp)
            Text(if (expanded) "❐" else "□", color = Muted, modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp), fontSize = 15.sp)
            Text("✕", color = Color(0xFFFF8B8B), modifier = Modifier.clickable { onClose(window.id) }.padding(start = 7.dp), fontSize = 15.sp)
        }
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            when (window.kind) {
                AppKind.NOTES -> {
                    Column {
                        Text("Quick Notes", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteText, onValueChange = onNoteChange,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = { Text("Write something…") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                AppKind.CALCULATOR -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Calculator", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(calcInput, color = Ink, fontSize = 28.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFE8EEF5), RoundedCornerShape(10.dp)).padding(12.dp))
                        val keys = listOf("7","8","9","÷","4","5","6","×","1","2","3","−","C","0","=","+")
                        keys.chunked(4).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                row.forEach { key ->
                                    Button(
                                        onClick = {
                                            when (key) {
                                                "C" -> onCalcInputChange("0")
                                                "=" -> onCalcInputChange(calculate(calcInput))
                                                else -> onCalcInputChange(if (calcInput == "0") key else calcInput + key)
                                            }
                                        },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (key in listOf("÷","×","−","+","=")) Color(0xFF24516B) else Color(0xFFE5ECF4), contentColor = if (key in listOf("÷","×","−","+","=")) White else Ink)
                                    ) { Text(key, fontSize = 17.sp) }
                                }
                            }
                        }
                    }
                }
                AppKind.FILES -> {
                    Column {
                        Text("Files", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("A simple file dashboard", color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        listOf("📷  Images", "🎬  Videos", "🎵  Audio", "📄  Documents", "⬇️  Downloads").forEach {
                            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(it, color = Ink, fontSize = 15.sp)
                            }
                            HorizontalDivider(color = Color(0xFFE4EAF1))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("This starter screen is a demo. Real file browsing can be added with Android's file picker.", color = Muted, fontSize = 11.sp)
                    }
                }
                AppKind.ABOUT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Vedu Desktop", color = Ink, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("A desktop-style workspace that runs inside your Android app.", color = Ink)
                        HorizontalDivider()
                        Text("Included", color = Ink, fontWeight = FontWeight.Bold)
                        Text("• Desktop wallpaper and shortcuts", color = Ink)
                        Text("• Taskbar and app menu", color = Ink)
                        Text("• Movable, minimizable and maximizable windows", color = Ink)
                        Text("• Notes and calculator demos", color = Ink)
                        Text("Note: this does not replace Android or enable Samsung DeX system-wide.", color = Color(0xFF9A4A22), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun calculate(input: String): String {
    return try {
        val normalized = input.replace("×", "*").replace("÷", "/").replace("−", "-").replace(" ", "")
        val match = Regex("^(-?\\d+(?:\\.\\d+)?)([+*/-])(-?\\d+(?:\\.\\d+)?)$").matchEntire(normalized)
            ?: return "Use: number operator number"
        val a = match.groupValues[1].toDouble()
        val op = match.groupValues[2]
        val b = match.groupValues[3].toDouble()
        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == 0.0) return "Cannot divide by zero" else a / b
            else -> return "Error"
        }
        if (!result.isFinite()) "Error" else if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
    } catch (_: Exception) {
        "Error"
    }
}
