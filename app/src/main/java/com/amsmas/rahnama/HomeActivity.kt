package com.amsmas.rahnama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amsmas.rahnama.data.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        setContent { MaterialTheme { RahnamaWorkingApp() } }
    }
}

private data class RahnamaNav(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RahnamaWorkingApp() {
    val context = LocalContext.current
    val dao = remember { RahnamaDatabase.getInstance(context).dao() }
    val scope = rememberCoroutineScope()
    val people by dao.observePersons().collectAsState(initial = emptyList())
    val properties by dao.observeProperties().collectAsState(initial = emptyList())
    val requests by dao.observeActiveRequests().collectAsState(initial = emptyList())
    var tab by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf("") }
    var alert by remember { mutableStateOf<String?>(null) }
    val nav = listOf(
        RahnamaNav("خانه", Icons.Default.Home),
        RahnamaNav("املاک", Icons.Default.Apartment),
        RahnamaNav("تقاضاها", Icons.Default.Assignment),
        RahnamaNav("اشخاص", Icons.Default.People),
        RahnamaNav("تطبیق", Icons.Default.Search),
        RahnamaNav("تنظیمات", Icons.Default.Settings)
    )
    val propertyFields = listOf(
        "نام صاحب خانه/زمین *", "شماره تماس اول مالک *", "شماره تماس دوم مالک",
        "نوع ملک: تعمیر/آپارتمان/زمین/دکان/مارکیت/باغ", "نوع معامله: فروشی/کرایی/گروی",
        "ولایت *", "شهر *", "ناحیه/گذر", "آدرس تشریحی",
        "شماره موقعیت روی نقشه (اختیاری)", "چپه/راسته/نامشخص",
        "قیمت فروش یا مبلغ گروی", "مقدار کرایه ماهانه", "شرایط گروی: اهل سنت/اهل تشیع/نامشخص",
        "تعداد اتاق", "آشپزخانه: دارد/ندارد", "برق: دارد/ندارد", "برمه: دارد/ندارد",
        "مهمان‌خانه: دارد/ندارد", "ورودی مهمان‌خانه: جداگانه/از داخل تعمیر",
        "زمین: خالی/چهار دیواری", "دیوار از خود دارد؟ دارد/ندارد",
        "چند طرف دیوار همسایه است؟ (۰ تا ۳)", "مساحت", "توضیحات",
        "نوع تعمیر/ساختمان (اختیاری)"
    )
    val requestFields = listOf(
        "نام مشتری *", "شماره تماس اول مشتری *", "نوع تقاضا: خرید/کرایه/گروی",
        "نوع ملک: تعمیر/آپارتمان/زمین/دکان/مارکیت/باغ", "حداقل بودجه", "حداکثر بودجه",
        "حداکثر کرایه ماهانه", "مبلغ گروی", "شرایط گروی: اهل سنت/اهل تشیع/هرکدام",
        "ولایت *", "شهر", "ناحیه", "حداقل تعداد اتاق", "توضیحات",
        "آشپزخانه مورد نیاز: دارد/ندارد/مهم نیست", "برق مورد نیاز: دارد/ندارد/مهم نیست",
        "برمه مورد نیاز: دارد/ندارد/مهم نیست", "ورودی جداگانه مهمان‌خانه: ضروری/ضروری نیست/مهم نیست",
        "شماره موقعیت روی نقشه (اختیاری)", "آدرس مورد نظر: خانه/زمین/هرکدام",
        "شماره تماس دوم مشتری"
    )
    val personFields = listOf("نام کامل *", "شماره تماس *", "شماره دوم", "ولایت", "شهر", "آدرس", "نقش: مالک/خریدار/مستأجر/متقاضی گروی")

    Scaffold(
        topBar = { TopAppBar(title = {}, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        bottomBar = { NavigationBar { nav.forEachIndexed { index, item ->
            NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(item.icon, contentDescription = item.title) }, label = { Text(item.title) })
        } } }
    ) { padding ->
        when (tab) {
            0 -> HomeDashboard(Modifier.padding(padding), properties.size, requests.size, people.size, properties.count { it.status == "sold" }, { tab = 1 }, { tab = 2 }, { tab = 4 })
            1 -> HomeRecords(Modifier.padding(padding), "مدیریت املاک", properties.map {
                listOf(
                    it.propertyCode,
                    homePropertyType(it.propertyType) + " • " + homeListingType(it.listingType),
                    it.province + "، " + it.city + (it.district?.let { district -> "، " + district } ?: ""),
                    it.address?.let { address -> "آدرس: " + address } ?: "",
                    it.mapLocationNumber?.let { number -> "شماره نقشه: " + number } ?: "",
                    it.directionPosition?.let { direction -> "موقعیت: " + direction } ?: "",
                    "قیمت: " + (it.priceAmount?.toLong()?.toString() ?: "ثبت نشده") + " AFN",
                    it.monthlyRent?.let { rent -> "کرایه ماهانه: " + rent.toLong() + " AFN" } ?: "",
                    it.bedrooms?.let { rooms -> "تعداد اتاق: " + rooms } ?: "",
                    it.kitchenAvailable?.let { value -> "آشپزخانه: " + homeYesNoText(value) } ?: "",
                    it.electricityAvailable?.let { value -> "برق: " + homeYesNoText(value) } ?: "",
                    it.borewellAvailable?.let { value -> "برمه: " + homeYesNoText(value) } ?: "",
                    it.guestHouseAvailable?.let { value -> "مهمان‌خانه: " + homeYesNoText(value) } ?: "",
                    it.guestHouseSeparateEntrance?.let { value -> "ورودی مهمان‌خانه: " + if (value) "جداگانه" else "از داخل تعمیر" } ?: "",
                    it.kotahAvailable?.let { value -> "کوته: " + homeYesNoText(value) } ?: "",
                    it.landBoundaryType?.let { type -> "وضعیت زمین: " + type } ?: "",
                    it.hasOwnWall?.let { value -> "دیوار از خود: " + homeYesNoText(value) } ?: "",
                    it.neighborWallSides?.let { sides -> "طرف دیوار همسایه: " + sides } ?: "",
                    it.constructionType?.let { type -> "نوع تعمیر: " + type } ?: "",
                    it.propertyTypeDetails?.let { details -> "مشخصات: " + details } ?: "",
                    it.areaValue?.let { area -> "مساحت: " + area } ?: "",
                    it.description?.let { description -> "توضیحات: " + description } ?: ""
                )
            }, "＋ ثبت ملک") { form = "property" }
            2 -> HomeRecords(Modifier.padding(padding), "تقاضاهای مشتریان", requests.map {
                listOf(
                    homePropertyType(it.propertyType) + " • " + homeListingType(it.listingType),
                    it.province + (it.city?.let { city -> "، " + city } ?: ""),
                    "بودجه: " + (it.budgetMin?.toLong()?.toString() ?: "—") + " تا " + (it.budgetMax?.toLong()?.toString() ?: "—"),
                    it.maxMonthlyRent?.let { rent -> "حداکثر کرایه ماهانه: " + rent.toLong() } ?: "",
                    it.bedroomsMin?.let { rooms -> "حداقل اتاق: " + rooms } ?: "",
                    it.kitchenRequired?.let { value -> "آشپزخانه: " + homeYesNoText(value) } ?: "",
                    it.electricityRequired?.let { value -> "برق: " + homeYesNoText(value) } ?: "",
                    it.borewellRequired?.let { value -> "برمه: " + homeYesNoText(value) } ?: "",
                    it.separateGuestEntranceRequired?.let { value -> "ورودی جداگانه مهمان‌خانه: " + if (value) "ضروری" else "ضروری نیست" } ?: "",
                    it.mapLocationNumber?.let { number -> "شماره موقعیت نقشه: " + number } ?: "",
                    it.requestedAddressType?.let { addressType -> "آدرس مورد نظر: " + addressType } ?: "",
                    it.notes ?: ""
                )
            }, "＋ ثبت تقاضا") { form = "request" }
            3 -> HomeRecords(Modifier.padding(padding), "اشخاص و مشتریان", people.map {
                listOf(it.fullName, "شماره اول: " + it.phoneNumber, it.alternatePhone?.let { phone -> "شماره دوم: " + phone } ?: "", (it.province ?: "") + " " + (it.city ?: ""), "نقش: " + it.roles)
            }, "＋ ثبت شخص") { form = "person" }
            4 -> HomeMatching(Modifier.padding(padding), properties, requests)
            else -> HomeSettings(Modifier.padding(padding))
        }
    }

    if (form.isNotBlank()) {
        if (form == "property") {
            PropertyEntryDialog(
                onDismiss = { form = "" },
                onSave = { v ->
                    if (v[0].isBlank() || v[1].isBlank() || v[5].isBlank() || v[6].isBlank()) {
                        alert = "نام صاحب ملک، شماره تماس، ولایت و شهر ضروری است."
                    } else scope.launch {
                        try {
                            val existingOwner = dao.findPersonByPhone(v[1].trim())
                            val ownerId = existingOwner?.id ?: dao.insertPerson(PersonEntity(
                                fullName = v[0].trim(), phoneNumber = v[1].trim(),
                                alternatePhone = v[2].trim().ifBlank { null },
                                province = v[5].trim(), city = v[6].trim(),
                                address = v[8].trim().ifBlank { null }, roles = "owner"
                            ))
                            if (existingOwner != null && v[2].isNotBlank()) {
                                dao.updatePersonContactDetails(existingOwner.id, v[2].trim(), v[8].trim().ifBlank { null })
                            }
                            val propertyCode = PropertyCodeGenerator.candidate(dao.countProperties() + 1, homeJalaliYear())
                            dao.insertProperty(PropertyEntity(
                                propertyCode = propertyCode, ownerId = ownerId,
                                propertyType = homeMapProperty(v[3]), listingType = homeMapListing(v[4]),
                                province = v[5].trim(), city = v[6].trim(),
                                district = v[7].trim().ifBlank { null }, address = v[8].trim().ifBlank { null },
                                mapLocationNumber = v[9].trim().ifBlank { null }, directionPosition = v[10].trim().ifBlank { null },
                                priceAmount = v[11].trim().toDoubleOrNull(), monthlyRent = v[12].trim().toDoubleOrNull(),
                                growyCondition = homeMapGrowy(v[13]), bedrooms = v[14].trim().toIntOrNull(),
                                kitchenAvailable = homeNullableYesNo(v[15]), electricityAvailable = homeNullableYesNo(v[16]),
                                borewellAvailable = homeNullableYesNo(v[17]), guestHouseAvailable = homeNullableYesNo(v[18]),
                                guestHouseSeparateEntrance = when {
                                    v[19].contains("جدا") -> true
                                    v[19].contains("داخل") -> false
                                    else -> null
                                },
                                landBoundaryType = v[20].trim().ifBlank { null }, hasOwnWall = homeNullableYesNo(v[21]),
                                neighborWallSides = v[22].trim().toIntOrNull(), areaValue = v[23].trim().toDoubleOrNull(),
                                description = v[24].trim().ifBlank { null }, constructionType = null,
                                propertyTypeDetails = v[25].trim().ifBlank { null }, kotahAvailable = homeNullableYesNo(v[26])
                            ))
                            form = ""
                            alert = "ملک ذخیره شد. کد ملک: " + propertyCode
                        } catch (e: Exception) { alert = "ذخیره ملک ناموفق بود؛ اطلاعات را بررسی کنید." }
                    }
                }
            )
        } else {
            val fields = if (form == "request") requestFields else personFields
            HomeEntryDialog(if (form == "request") "ثبت تقاضای مشتری" else "ثبت شخص", fields,
                onDismiss = { form = "" },
                onSave = { v ->
                    if (form == "request") {
                        if (v[0].isBlank() || v[1].isBlank() || v[9].isBlank()) alert = "نام، شماره تماس و ولایت ضروری است."
                        else scope.launch {
                            try {
                                val existingClient = dao.findPersonByPhone(v[1].trim())
                                val clientId = existingClient?.id ?: dao.insertPerson(PersonEntity(
                                    fullName = v[0].trim(), phoneNumber = v[1].trim(),
                                    alternatePhone = v[20].trim().ifBlank { null },
                                    province = v[9].trim(), city = v[10].trim().ifBlank { null }, roles = "applicant"
                                ))
                                if (existingClient != null && v[20].isNotBlank()) dao.updatePersonContactDetails(existingClient.id, v[20].trim(), null)
                                dao.insertRequest(ClientRequestEntity(
                                    clientId = clientId, listingType = homeMapListing(v[2]), propertyType = homeMapProperty(v[3]),
                                    budgetMin = v[4].trim().toDoubleOrNull(), budgetMax = v[5].trim().toDoubleOrNull(),
                                    maxMonthlyRent = v[6].trim().toDoubleOrNull(), desiredGrowyAmount = v[7].trim().toDoubleOrNull(),
                                    growyCondition = homeMapRequestGrowy(v[8]), province = v[9].trim(),
                                    city = v[10].trim().ifBlank { null }, district = v[11].trim().ifBlank { null },
                                    bedroomsMin = v[12].trim().toIntOrNull(), notes = v[13].trim(),
                                    kitchenRequired = homeNullableYesNo(v[14]), electricityRequired = homeNullableYesNo(v[15]),
                                    borewellRequired = homeNullableYesNo(v[16]),
                                    separateGuestEntranceRequired = when {
                                        v[17].contains("مهم نیست") || v[17].isBlank() -> null
                                        v[17].contains("ضروری نیست") -> false
                                        v[17].contains("ضروری") -> true
                                        else -> null
                                    },
                                    mapLocationNumber = v[18].trim().ifBlank { null }, requestedAddressType = v[19].trim().ifBlank { null }
                                ))
                                form = ""; alert = "تقاضا ذخیره شد."
                            } catch (e: Exception) { alert = "ذخیره تقاضا ناموفق بود؛ اطلاعات را بررسی کنید." }
                        }
                    } else {
                        if (v[0].isBlank() || v[1].isBlank()) alert = "نام و شماره تماس ضروری است."
                        else scope.launch {
                            try {
                                dao.insertPerson(PersonEntity(
                                    fullName = v[0].trim(), phoneNumber = v[1].trim(),
                                    alternatePhone = v[2].trim().ifBlank { null },
                                    province = v[3].trim().ifBlank { null }, city = v[4].trim().ifBlank { null },
                                    address = v[5].trim().ifBlank { null }, roles = homeMapRole(v[6])
                                ))
                                form = ""; alert = "شخص ذخیره شد."
                            } catch (e: Exception) { alert = "ذخیره شخص ناموفق بود؛ شماره تماس را بررسی کنید." }
                        }
                    }
                })
        }
    }
    alert?.let { AlertDialog(onDismissRequest = { alert = null }, title = { Text("رهنمای معاملات") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { alert = null }) { Text("تأیید") } }) }
}

@Composable
private fun HomeDashboard(modifier: Modifier, propertyCount: Int, requestCount: Int, peopleCount: Int, soldCount: Int, onProperties: () -> Unit, onRequests: () -> Unit, onMatch: () -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("داشبورد", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("آمار از داده‌های ذخیره‌شده در موبایل") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { HomeCount("کل املاک", propertyCount.toString(), Modifier.weight(1f)); HomeCount("تقاضاهای فعال", requestCount.toString(), Modifier.weight(1f)) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { HomeCount("اشخاص", peopleCount.toString(), Modifier.weight(1f)); HomeCount("فروخته‌شده", soldCount.toString(), Modifier.weight(1f)) } }
        item { Button(onClick = onProperties, modifier = Modifier.fillMaxWidth()) { Text("مدیریت املاک") } }
        item { OutlinedButton(onClick = onRequests, modifier = Modifier.fillMaxWidth()) { Text("مدیریت تقاضاها") } }
        item { OutlinedButton(onClick = onMatch, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("جستجو و تطبیق هوشمند") } }
        item { Text("پرداخت آنلاین در این برنامه فعال نیست.") }
    }
}
@Composable
private fun HomeCount(title: String, count: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(count, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(title) } }
}
@Composable
private fun HomeRecords(modifier: Modifier, title: String, rows: List<List<String>>, action: String, onAdd: () -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text(action) }
        if (rows.isEmpty()) Text("هنوز موردی ثبت نشده است.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { row -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                row.filter { it.isNotBlank() }.forEachIndexed { index, text -> Text(text, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal) }
            } } }
        }
    }
}
@Composable
private fun HomeSettings(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("تنظیمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("این بخش برای تنظیمات آینده برنامه آماده شده است.")
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Column {
                    Text("تنظیمات برنامه", fontWeight = FontWeight.SemiBold)
                    Text("گزینه‌های تنظیمات بعداً در این صفحه اضافه خواهند شد.")
                }
            }
        }
    }
}

@Composable
private fun HomeMatching(modifier: Modifier, properties: List<PropertyEntity>, requests: List<ClientRequestEntity>) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("جستجو و تطبیق هوشمند", fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("تطبیق نوع معامله، ملک، موقعیت و بودجه") }
        if (requests.isEmpty()) item { Text("ابتدا یک تقاضای فعال ثبت کنید.") }
        items(requests, key = { it.id }) { request ->
            val matches = MatchingEngine.matchPropertiesToRequest(request, properties)
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(homeMapDisplayProperty(request.propertyType) + " • " + homeMapDisplayListing(request.listingType), fontWeight = FontWeight.Bold)
                Text(request.province + (request.city?.let { "، " + it } ?: ""))
                if (matches.isEmpty()) Text("ملک مطابق پیدا نشد.")
                else matches.take(5).forEach { match ->
                    HorizontalDivider()
                    Text(match.property.propertyCode + " — " + match.score + "%", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(match.property.city + " • " + (match.property.priceAmount?.toLong()?.toString() ?: "قیمت نامشخص"))
                }
            } }
        }
    }
}
private data class PropertyFormField(val index: Int, val label: String, val options: List<String>? = null)

@Composable
private fun PropertyEntryDialog(onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val values = remember { mutableStateListOf<String>().also { state -> repeat(27) { state.add("") } } }
    val propertyType = values[3]
    val listingType = values[4]
    val yesNo = listOf("دارد", "ندارد", "نامشخص")
    val fields = buildList {
        add(PropertyFormField(0, "نام صاحب ملک *"))
        add(PropertyFormField(1, "شماره تماس اول *"))
        add(PropertyFormField(2, "شماره تماس دوم"))
        add(PropertyFormField(3, "نوع ملک *", listOf("تعمیر", "آپارتمان", "زمین", "دکان", "مارکیت", "باغ")))
        add(PropertyFormField(4, "نوع معامله *", listOf("فروشی", "کرایی", "گروی")))
        add(PropertyFormField(5, "ولایت *"))
        add(PropertyFormField(6, "شهر *"))
        add(PropertyFormField(7, "ناحیه / گذر"))
        add(PropertyFormField(8, "آدرس"))
        add(PropertyFormField(9, "شماره ملک / شماره موقعیت"))
        add(PropertyFormField(10, "چپه یا راسته", listOf("چپه", "راسته", "نامشخص")))
        if (listingType != "کرایی") add(PropertyFormField(11, "قیمت"))
        if (listingType == "کرایی" || listingType == "گروی") add(PropertyFormField(12, "کرایه ماهانه"))
        if (listingType == "گروی") add(PropertyFormField(13, "شرایط گروی", listOf("اهل سنت", "اهل تشیع", "نامشخص")))
        when (propertyType) {
            "زمین" -> {
                add(PropertyFormField(20, "نوع زمین", listOf("زمین خالی", "چهار دیواری")))
                add(PropertyFormField(21, "دیوار از خود دارد؟", yesNo))
                add(PropertyFormField(22, "تعداد طرف دیوار همسایه", listOf("0", "1", "2", "3")))
                add(PropertyFormField(23, "مساحت زمین"))
                add(PropertyFormField(25, "مشخصات بیشتر زمین"))
            }
            "تعمیر" -> {
                add(PropertyFormField(14, "تعداد اتاق"))
                add(PropertyFormField(15, "آشپزخانه", yesNo))
                add(PropertyFormField(16, "برق", yesNo))
                add(PropertyFormField(17, "برمه آب", yesNo))
                add(PropertyFormField(18, "مهمان خانه", yesNo))
                add(PropertyFormField(19, "ورودی مهمان خانه", listOf("جداگانه", "از داخل تعمیر", "نامشخص")))
                add(PropertyFormField(26, "کوته", yesNo))
                add(PropertyFormField(25, "مشخصات بیشتر تعمیر"))
            }
            "آپارتمان" -> {
                add(PropertyFormField(14, "تعداد اتاق"))
                add(PropertyFormField(15, "آشپزخانه", yesNo))
                add(PropertyFormField(16, "برق", yesNo))
                add(PropertyFormField(17, "برمه آب", yesNo))
                add(PropertyFormField(18, "مهمان خانه", yesNo))
                add(PropertyFormField(19, "ورودی مهمان خانه", listOf("جداگانه", "از داخل تعمیر", "نامشخص")))
                add(PropertyFormField(26, "کوته", yesNo))
                add(PropertyFormField(23, "مساحت"))
                add(PropertyFormField(25, "مشخصات بیشتر آپارتمان"))
            }
            "باغ" -> {
                add(PropertyFormField(20, "وضعیت زمین", listOf("زمین خالی", "چهار دیواری", "باغ دارای تعمیر")))
                add(PropertyFormField(21, "دیوار از خود دارد؟", yesNo))
                add(PropertyFormField(22, "تعداد طرف دیوار همسایه", listOf("0", "1", "2", "3")))
                add(PropertyFormField(23, "مساحت زمین"))
                add(PropertyFormField(16, "برق", yesNo))
                add(PropertyFormField(17, "برمه آب", yesNo))
                add(PropertyFormField(25, "مشخصات بیشتر باغ"))
            }
            "دکان", "مارکیت" -> {
                add(PropertyFormField(16, "برق", yesNo))
                add(PropertyFormField(17, "برمه آب", yesNo))
                add(PropertyFormField(23, "مساحت"))
                add(PropertyFormField(25, "مشخصات بیشتر ملک"))
            }
            else -> add(PropertyFormField(25, "مشخصات بیشتر ملک"))
        }
        add(PropertyFormField(24, "توضیحات بیشتر"))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت ملک", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("مشخصات ملک", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                fields.forEach { field ->
                    if (field.options != null) {
                        HomeDropdownField(field.label, values[field.index], field.options) { values[field.index] = it }
                    } else {
                        OutlinedTextField(values[field.index], { values[field.index] = it },
                            label = { Text(field.label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("ذخیره ملک") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}

@Composable
private fun HomeDropdownField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember(label) { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }, singleLine = true)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun HomeEntryDialog(title: String, fields: List<String>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val values = remember(title) { mutableStateListOf<String>().also { state -> fields.forEach { state.add("") } } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { LazyColumn(Modifier.heightIn(max = 450.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fields.size) { i -> OutlinedTextField(value = values[i], onValueChange = { values[i] = it }, label = { Text(fields[i]) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        } },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } })
}
private fun homeMapProperty(value: String) = when {
    value.contains("اپارتمان") || value.contains("آپارتمان") -> "apartment"
    value.contains("تعمیر") || value.contains("ساختمان") -> "building"
    value.contains("زمین") -> "land"
    value.contains("دکان") -> "shop"
    value.contains("مارکیت") -> "market"
    value.contains("باغ") -> "garden"
    value.contains("خانه") -> "house"
    else -> "other"
}
private fun homeNullableYesNo(value: String): Boolean? = when {
    value.contains("مهم نیست") || value.isBlank() -> null
    value.contains("ندارد") || value.contains("نیست") || value.contains("خیر") || value == "نه" -> false
    value.contains("دارد") || value.contains("بلی") || value.contains("بله") || value.contains("ضروری") -> true
    else -> null
}
private fun homeYesNoText(value: Boolean) = if (value) "دارد" else "ندارد"
private fun homeMapListing(value: String) = when { value.contains("کرایه") || value.contains("کرایی") -> "rent"; value.contains("گروی") -> "growy"; else -> "sale" }
private fun homeMapGrowy(value: String) = when { value.contains("سنت") -> "sunni"; value.contains("تشیع") || value.contains("شیعه") -> "shia"; else -> "unspecified" }
private fun homeMapRequestGrowy(value: String) = when { value.contains("هرکدام") -> "either"; value.contains("سنت") -> "sunni"; value.contains("تشیع") || value.contains("شیعه") -> "shia"; else -> "unspecified" }
private fun homeMapRole(value: String) = when { value.contains("مالک") -> "owner"; value.contains("خریدار") -> "buyer"; value.contains("مستأجر") || value.contains("مستاجر") -> "tenant"; value.contains("گروی") -> "growy_applicant"; else -> "other" }
private fun homePropertyType(value: String) = when (value) { "house" -> "خانه"; "building" -> "تعمیر"; "apartment" -> "آپارتمان"; "land" -> "زمین"; "shop" -> "دکان"; "market" -> "مارکیت"; "garden" -> "باغ"; else -> "سایر" }
private fun homeListingType(value: String) = when (value) { "rent" -> "کرایی"; "growy" -> "گروی"; else -> "فروشی" }
private fun homeMapDisplayProperty(value: String) = homePropertyType(value)
private fun homeMapDisplayListing(value: String) = homeListingType(value)
private fun homeJalaliYear(): Int { val c = Calendar.getInstance(); return c.get(Calendar.YEAR) - if (c.get(Calendar.MONTH) + 1 >= 3) 621 else 622 }
