package com.amsmas.rahnama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        setContent { MaterialTheme { RahnamaApp() } }
    }
}

private data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RahnamaApp() {
    val items = listOf(
        NavItem("داشبورد", Icons.Default.Home),
        NavItem("املاک", Icons.Default.Apartment),
        NavItem("تقاضاها", Icons.Default.Assignment),
        NavItem("اشخاص", Icons.Default.People)
    )
    var tab by remember { mutableIntStateOf(0) }
    var showPropertyForm by remember { mutableStateOf(false) }
    var showRequestForm by remember { mutableStateOf(false) }
    var showPersonForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("رهنمای معاملات", fontWeight = FontWeight.Bold)
                    Text("مدیریت املاک افغانستان", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            })
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (tab) {
            0 -> Dashboard(Modifier.padding(padding), onOpenProperties = { tab = 1 }, onOpenRequests = { tab = 2 })
            1 -> PropertyPage(Modifier.padding(padding), onAdd = { showPropertyForm = true })
            2 -> RequestPage(Modifier.padding(padding), onAdd = { showRequestForm = true })
            else -> PersonPage(Modifier.padding(padding), onAdd = { showPersonForm = true })
        }

        if (showPropertyForm) EntryDialog("ثبت ملک", listOf(
            "نام مالک", "نوع ملک", "نوع معامله: فروشی / کرایی / گروی",
            "ولایت", "شهر", "ناحیه", "قیمت یا مبلغ گروی", "کرایه ماهانه", "توضیحات"
        )) { showPropertyForm = false }
        if (showRequestForm) EntryDialog("ثبت تقاضای مشتری", listOf(
            "نام مشتری", "نوع تقاضا: خرید / کرایه / گروی", "نوع ملک",
            "حداقل بودجه", "حداکثر بودجه", "حداکثر کرایه ماهانه", "ولایت", "شهر", "توضیحات"
        )) { showRequestForm = false }
        if (showPersonForm) EntryDialog("ثبت شخص", listOf(
            "نام کامل", "شماره تماس", "شماره دوم", "ولایت", "شهر", "آدرس",
            "نقش: مالک / خریدار / مستأجر / متقاضی گروی"
        )) { showPersonForm = false }
    }
}

@Composable
private fun Dashboard(modifier: Modifier = Modifier, onOpenProperties: () -> Unit, onOpenRequests: () -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("خلاصه وضعیت", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("به سیستم مدیریت املاک خوش آمدید")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("املاک فعال", "۰", Modifier.weight(1f))
                SummaryCard("تقاضاهای فعال", "۰", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("فروخته‌شده", "۰", Modifier.weight(1f))
                SummaryCard("کرایه داده‌شده", "۰", Modifier.weight(1f))
            }
        }
        item {
            Card(onClick = onOpenProperties, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Apartment, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("مدیریت املاک", fontWeight = FontWeight.Bold)
                        Text("ثبت و مرور املاک فروشی، کرایی و گروی")
                    }
                }
            }
        }
        item {
            Card(onClick = onOpenRequests, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("تطبیق هوشمند", fontWeight = FontWeight.Bold)
                        Text("تطبیق تقاضای مشتریان با املاک موجود")
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("یادآوری", fontWeight = FontWeight.Bold)
                    Text("این نسخه برای ثبت اطلاعات طراحی می‌شود؛ پرداخت آنلاین وجود ندارد.")
                }
            }
        }
    }
}

@Composable
private fun PropertyPage(modifier: Modifier, onAdd: () -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("مدیریت املاک", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("برای هر ملک، کد اختصاصی سیستم، مالک، موقعیت و شرایط مالی ثبت می‌شود.")
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("＋ ثبت ملک جدید") }
        EmptyState("هنوز ملکی ثبت نشده است")
    }
}

@Composable
private fun RequestPage(modifier: Modifier, onAdd: () -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("تقاضاهای مشتریان", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("بودجه و کرایه ماهانه در صورت نیاز به‌صورت جداگانه بررسی می‌شوند.")
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("＋ ثبت تقاضای جدید") }
        EmptyState("هنوز تقاضایی ثبت نشده است")
    }
}

@Composable
private fun PersonPage(modifier: Modifier, onAdd: () -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("اشخاص و مشتریان", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("یک شخص می‌تواند هم‌زمان مالک، خریدار یا مستأجر باشد.")
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("＋ ثبت شخص جدید") }
        EmptyState("هنوز شخصی ثبت نشده است")
    }
}

@Composable
private fun EmptyState(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(count, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EntryDialog(title: String, fields: List<String>, onDismiss: () -> Unit) {
    val values = remember(title) { mutableStateListOf<String>().also { list -> fields.forEach { list.add("") } } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 440.dp)) {
                items(fields.size) { index ->
                    OutlinedTextField(
                        value = values[index],
                        onValueChange = { values[index] = it },
                        label = { Text(fields[index]) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}
