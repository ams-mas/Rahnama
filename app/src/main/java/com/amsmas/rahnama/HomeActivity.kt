package com.amsmas.rahnama

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.amsmas.rahnama.data.*
import kotlinx.coroutines.launch
import java.util.Calendar

private val RahnamaFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        setContent {
            var showOpeningLogo by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1_500)
                showOpeningLogo = false
            }
            MaterialTheme(
                typography = Typography().let { typography ->
                    typography.copy(
                        displayLarge = typography.displayLarge.copy(fontFamily = RahnamaFontFamily),
                        displayMedium = typography.displayMedium.copy(fontFamily = RahnamaFontFamily),
                        displaySmall = typography.displaySmall.copy(fontFamily = RahnamaFontFamily),
                        headlineLarge = typography.headlineLarge.copy(fontFamily = RahnamaFontFamily),
                        headlineMedium = typography.headlineMedium.copy(fontFamily = RahnamaFontFamily),
                        headlineSmall = typography.headlineSmall.copy(fontFamily = RahnamaFontFamily),
                        titleLarge = typography.titleLarge.copy(fontFamily = RahnamaFontFamily),
                        titleMedium = typography.titleMedium.copy(fontFamily = RahnamaFontFamily),
                        titleSmall = typography.titleSmall.copy(fontFamily = RahnamaFontFamily),
                        bodyLarge = typography.bodyLarge.copy(fontFamily = RahnamaFontFamily),
                        bodyMedium = typography.bodyMedium.copy(fontFamily = RahnamaFontFamily),
                        bodySmall = typography.bodySmall.copy(fontFamily = RahnamaFontFamily),
                        labelLarge = typography.labelLarge.copy(fontFamily = RahnamaFontFamily),
                        labelMedium = typography.labelMedium.copy(fontFamily = RahnamaFontFamily),
                        labelSmall = typography.labelSmall.copy(fontFamily = RahnamaFontFamily)
                    )
                },
                shapes = Shapes(
                    small = RoundedCornerShape(12.dp),
                    medium = RoundedCornerShape(16.dp),
                    large = RoundedCornerShape(22.dp)
                )
            ) {
                if (showOpeningLogo) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rahnama_logo),
                            contentDescription = "Rahnama logo",
                            modifier = Modifier.size(180.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                } else {
                    RahnamaWorkingApp()
                }
            }
        }
    }
}

private data class RahnamaNav(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RahnamaWorkingApp() {
    val context = LocalContext.current
    val viewModel: RahnamaViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val people by viewModel.people.collectAsState(initial = emptyList())
    val properties by viewModel.properties.collectAsState(initial = emptyList())
    val requests by viewModel.activeRequests.collectAsState(initial = emptyList())
    val allRequests by viewModel.allRequests.collectAsState(initial = emptyList())
    var tab by remember { mutableIntStateOf(0) }
    var propertyListingFilter by remember { mutableStateOf<String?>(null) }
    var requestListingFilter by remember { mutableStateOf<String?>(null) }
    var showPropertyArchive by remember { mutableStateOf(false) }
    var showRequestArchive by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf("") }
    var selectedPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var editingPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var selectedPersonForCall by remember { mutableStateOf<PersonEntity?>(null) }
    var alert by remember { mutableStateOf<String?>(null) }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.sqlite3")
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                DatabaseBackup.export(context, uri)
                alert = "پشتیبان دیتابیس با موفقیت ذخیره شد."
            } catch (e: Exception) {
                alert = "ساخت پشتیبان ناموفق بود. فضای ذخیره‌سازی و دسترسی فایل را بررسی کنید."
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                DatabaseBackup.restore(context, uri)
                alert = "بازیابی انجام شد. اطلاعات از فایل انتخاب‌شده بارگذاری شد."
            } catch (e: Exception) {
                alert = "بازیابی ناموفق بود. فایل پشتیبان معتبر را انتخاب کنید؛ اطلاعات قبلی تا حد امکان حفظ می‌شود."
            }
        }
    }
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

    val personFields = listOf("نام کامل *", "شماره تماس *", "شماره دوم", "ولایت", "شهر", "آدرس", "نقش: فروشنده/خریدار/متقاضی مستأجر/متقاضی گروی/متقاضی کرایی")

    Scaffold(
        topBar = { TopAppBar(title = {}, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        bottomBar = { NavigationBar { nav.forEachIndexed { index, item ->
            NavigationBarItem(selected = tab == index, onClick = {
                tab = index
                if (index != 1) propertyListingFilter = null
                if (index != 2) requestListingFilter = null
            }, icon = { Icon(item.icon, contentDescription = item.title) }, label = { Text(item.title) })
        } } }
    ) { padding ->
        when (tab) {
            0 -> HomeDashboard(
                Modifier.padding(padding), properties, requests,
                onProperties = { type -> propertyListingFilter = type.ifBlank { null }; tab = 1 },
                onRequests = { type -> requestListingFilter = type.ifBlank { null }; tab = 2 },
                onMatch = { propertyListingFilter = null; requestListingFilter = null; tab = 4 },
                onRegisterProperty = { form = "property" },
                onRegisterRequest = { form = "request" }
            )
            1 -> HomeRecords(Modifier.padding(padding),
                if (propertyListingFilter == null) "مدیریت املاک" else "املاک ${homeListingType(propertyListingFilter!!)}",
                properties.filter { (propertyListingFilter == null || it.listingType == propertyListingFilter) && ((it.status != "available") == showPropertyArchive) }.map {
                listOf(
                    "__RAHNAMA_RECORD__:P:${it.id}:${it.status}",
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
                    it.propertyTypeDetails?.let { details -> homeFormatPropertyDetails(details) } ?: "",
                    it.areaValue?.let { area ->
                        val areaText = if (area % 1.0 == 0.0) area.toLong().toString() else area.toString()
                        "مساحت: " + homeToPersianDigits(areaText) +
                            (it.areaUnit?.takeIf { unit -> unit.isNotBlank() }?.let { unit -> " " + unit } ?: "")
                    } ?: "",
                    it.description?.let { description -> "توضیحات: " + description } ?: ""
                )
            }, "＋ ثبت ملک", onAdd = { form = "property" }, archiveMode = showPropertyArchive, onArchiveModeChange = { showPropertyArchive = it }, onStatusChange = { type, id, status -> viewModel.updatePropertyStatus(id, status) { result -> result.onFailure { alert = "تغییر وضعیت ملک انجام نشد." } } })
            2 -> HomeRecords(Modifier.padding(padding),
                if (requestListingFilter == null) "تقاضاهای مشتریان" else "تقاضای ${homeListingType(requestListingFilter!!)}",
                allRequests.filter { (requestListingFilter == null || it.listingType == requestListingFilter) && ((it.requestStatus != "active") == showRequestArchive) }.map {
                listOf(
                    "__RAHNAMA_RECORD__:R:${it.id}:${it.requestStatus}",
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
            }, "＋ ثبت تقاضا", onAdd = { form = "request" }, archiveMode = showRequestArchive, onArchiveModeChange = { showRequestArchive = it }, onStatusChange = { type, id, status -> viewModel.updateRequestStatus(id, status) { result -> result.onFailure { alert = "تغییر وضعیت تقاضا انجام نشد." } } })
            3 -> HomePeople(
                Modifier.padding(padding),
                people = people,
                properties = properties,
                requests = requests,
                onAdd = { editingPerson = null; form = "person" },
                onSelect = { selectedPerson = it },
                onCall = { person ->
                    if (person.alternatePhone.isNullOrBlank()) dialPersonNumber(context, person.phoneNumber) { alert = it }
                    else selectedPersonForCall = person
                }
            )
            4 -> HomeMatching(Modifier.padding(padding), properties, requests, people)
            else -> HomeSettings(Modifier.padding(padding), onBackup = { backupLauncher.launch("rahnama-backup.db") }, onRestore = { restoreLauncher.launch(arrayOf("*/*")) })
        }
    }

    if (form == "person") {
        PersonEntryDialog(
            initialPerson = editingPerson,
            onDismiss = { form = ""; editingPerson = null },
            onSave = { person ->
                if (editingPerson == null) {
                    viewModel.savePerson(person) { result ->
                        result.fold(
                            onSuccess = { form = ""; alert = "شخص با موفقیت ثبت شد." },
                            onFailure = { alert = it.message ?: "ثبت شخص ناموفق بود؛ شماره تماس را بررسی کنید." }
                        )
                    }
                } else {
                    viewModel.updatePerson(person) { result ->
                        result.fold(
                            onSuccess = { form = ""; editingPerson = null; alert = "اطلاعات شخص به‌روزرسانی شد." },
                            onFailure = { alert = it.message ?: "ویرایش شخص ناموفق بود؛ اطلاعات را بررسی کنید." }
                        )
                    }
                }
            }
        )
    } else if (form == "property") {
        PropertyEntryDialog(
            onDismiss = { form = "" },
            onSave = { v ->
                val validationError = validatePropertyForm(v)
                if (v[0].isBlank() || v[1].isBlank() || v[3].isBlank() || v[4].isBlank() || v[5].isBlank() || v[6].isBlank()) {
                    alert = "نام مالک، شماره تماس، نوع ملک، نوع معامله، ولایت و شهر ضروری است."
                } else if (validationError != null) {
                    alert = validationError
                } else {
                    val ownerDraft = PersonEntity(
                        fullName = v[0].trim(), phoneNumber = homeToEnglishDigits(v[1]),
                        alternatePhone = homeToEnglishDigits(v[2]).ifBlank { null },
                        province = v[5].trim(), city = v[6].trim(),
                        address = v[8].trim().ifBlank { null }, roles = "owner"
                    )
                    val propertyDraft = PropertyEntity(
                        propertyCode = "", ownerId = 0,
                        propertyType = homeMapProperty(v[3]), listingType = homeMapListing(v[4]),
                        province = v[5].trim(), city = v[6].trim(),
                        district = v[7].trim().ifBlank { null }, address = v[8].trim().ifBlank { null },
                        mapLocationNumber = v[9].trim().ifBlank { null }, directionPosition = v[10].trim().ifBlank { null },
                        priceAmount = homeToEnglishDigits(v[11]).trim().toDoubleOrNull(), monthlyRent = if (v[4] == "گروی" && v[34] == "بلی") homeToEnglishDigits(v[35]).trim().toDoubleOrNull() else homeToEnglishDigits(v[12]).trim().toDoubleOrNull(),
                        growyCondition = homeMapGrowy(v[13]), bedrooms = v[14].trim().toIntOrNull(),
                        kitchenAvailable = homeNullableYesNo(v[15]), electricityAvailable = homeNullableYesNo(v[16]),
                        borewellAvailable = homeNullableYesNo(v[17]), guestHouseAvailable = homeNullableYesNo(v[18]),
                        guestHouseSeparateEntrance = when {
                            v[19].contains("جدا") -> true
                            v[19].contains("داخل") -> false
                            else -> null
                        },
                        landBoundaryType = v[20].trim().ifBlank { null }, hasOwnWall = homeNullableYesNo(v[21]),
                        neighborWallSides = v[22].trim().toIntOrNull(), areaValue = homeToEnglishDigits(when (v[3]) { "باغ", "زمین", "تعمیر" -> v[38]; "آپارتمان", "دکان" -> v[39]; else -> "" }).trim().toDoubleOrNull(), areaUnit = v[44].ifBlank { null },
                        description = v[24].trim().ifBlank { null }, constructionType = null,
                        propertyTypeDetails = homeBuildPropertyExtraDetails(v), kotahAvailable = homeNullableYesNo(v[26])
                    )
                    viewModel.saveProperty(ownerDraft, propertyDraft, homeJalaliYear()) { result ->
                        result.fold(
                            onSuccess = { code -> form = ""; alert = "ملک ذخیره شد. کد ملک: " + code },
                            onFailure = { alert = "ذخیره ملک ناموفق بود؛ اطلاعات را بررسی کنید." }
                        )
                    }
                }
            }
        )
    } else if (form == "request") {
        HomeRequestEntryDialog(
            onDismiss = { form = "" },
            onSave = { v ->
                val validationError = validateSimpleRequestForm(v)
                if (v[1].isBlank() || v[2].isBlank() || v[0].isBlank()) {
                    alert = "نوع تقاضا، نام و شماره تماس اول ضروری است."
                } else if (validationError != null) {
                    alert = validationError
                } else {
                    val clientDraft = PersonEntity(
                        fullName = v[1].trim(),
                        phoneNumber = homeToEnglishDigits(v[2]),
                        alternatePhone = homeToEnglishDigits(v[3]).ifBlank { null },
                        roles = "applicant"
                    )
                    val requestDraft = ClientRequestEntity(
                        clientId = 0,
                        listingType = homeMapListing(v[0]),
                        propertyType = "other",
                        budgetMin = homeToEnglishDigits(v[10]).trim().toDoubleOrNull(),
                        budgetMax = homeToEnglishDigits(v[11]).trim().toDoubleOrNull(),
                        maxMonthlyRent = homeToEnglishDigits(v[12]).trim().toDoubleOrNull(),
                        desiredGrowyAmount = homeToEnglishDigits(v[13]).trim().toDoubleOrNull(),
                        province = "",
                        bedroomsMin = homeToEnglishDigits(v[4]).trim().toIntOrNull(),
                        kitchenRequired = homeNullableYesNo(v[5]),
                        guestHouseRequired = homeNullableYesNo(v[6]),
                        kotahRequired = homeNullableYesNo(v[7]),
                        electricityRequired = homeNullableYesNo(v[8]),
                        borewellRequired = homeNullableYesNo(v[9]),
                        notes = v[14].trim().ifBlank { null }
                    )
                    viewModel.saveRequest(clientDraft, requestDraft) { result ->
                        result.fold(
                            onSuccess = { form = ""; alert = "تقاضا ذخیره شد." },
                            onFailure = { alert = "ذخیره تقاضا ناموفق بود؛ اطلاعات را بررسی کنید." }
                        )
                    }
                }
            }
        )
    }
    selectedPerson?.let { person ->
        PersonDetailDialog(
            person = people.firstOrNull { it.id == person.id } ?: person,
            properties = properties.filter { it.ownerId == person.id },
            requests = allRequests.filter { it.clientId == person.id },
            onDismiss = { selectedPerson = null },
            onCall = { p ->
                if (p.alternatePhone.isNullOrBlank()) dialPersonNumber(context, p.phoneNumber) { alert = it }
                else selectedPersonForCall = p
            },
            onSms = { p -> openSms(context, p.phoneNumber) { alert = it } },
            onWhatsApp = { p -> openWhatsApp(context, p.phoneNumber) { alert = it } },
            onEdit = { p -> selectedPerson = null; editingPerson = p; form = "person" }
        )
    }
    selectedPersonForCall?.let { person ->
        AlertDialog(
            onDismissRequest = { selectedPersonForCall = null },
            title = { Text("با کدام شماره تماس می‌گیرید؟") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val phone = person.phoneNumber
                        selectedPersonForCall = null
                        dialPersonNumber(context, phone) { alert = it }
                    }, modifier = Modifier.fillMaxWidth()) { Text("شماره اصلی: " + homeToPersianDigits(person.phoneNumber)) }
                    person.alternatePhone?.takeIf { it.isNotBlank() }?.let { alternate ->
                        OutlinedButton(onClick = {
                            selectedPersonForCall = null
                            dialPersonNumber(context, alternate) { alert = it }
                        }, modifier = Modifier.fillMaxWidth()) { Text("شماره دوم: " + homeToPersianDigits(alternate)) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedPersonForCall = null }) { Text("لغو") } }
        )
    }
    alert?.let { AlertDialog(onDismissRequest = { alert = null }, title = { Text("رهنمای معاملات") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { alert = null }) { Text("تأیید") } }) }
}

@Composable
private fun HomeDashboard(
    modifier: Modifier,
    properties: List<PropertyEntity>,
    requests: List<ClientRequestEntity>,
    onProperties: (String) -> Unit,
    onRequests: (String) -> Unit,
    onMatch: () -> Unit,
    onRegisterProperty: () -> Unit,
    onRegisterRequest: () -> Unit
) {
    var showRegisterOptions by remember { mutableStateOf(false) }
    val availableProperties = properties.filter { it.status == "available" }
    val saleCount = availableProperties.count { it.listingType == "sale" }
    val growyCount = availableProperties.count { it.listingType == "growy" }
    val rentCount = availableProperties.count { it.listingType == "rent" }
    val buyRequestCount = requests.count { it.listingType == "sale" }
    val growyRequestCount = requests.count { it.listingType == "growy" }
    val rentRequestCount = requests.count { it.listingType == "rent" }
    var currentDateTime by remember {
        mutableStateOf(java.util.Date())
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = java.util.Date()
            kotlinx.coroutines.delay(60_000)
        }
    }
    val dateLabel = remember(currentDateTime) {
        java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.ENGLISH).format(currentDateTime)
    }
    val timeLabel = remember(currentDateTime) {
        java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(currentDateTime)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF6950A1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("رهنمای معاملات", color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .background(
                                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(dateLabel, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.width(1.dp).height(16.dp).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.45f)))
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(timeLabel, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(7.dp))
                    Text("املاک موجود", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeStatCard("فروشی", saleCount, Icons.Default.Home, Modifier.weight(1f), { onProperties("sale") })
                    HomeStatCard("گروی", growyCount, Icons.Default.Key, Modifier.weight(1f), { onProperties("growy") })
                    HomeStatCard("کرایی", rentCount, Icons.Default.Apartment, Modifier.weight(1f), { onProperties("rent") })
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(7.dp))
                    Text("تقاضاهای مشتریان", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeStatCard("خرید", buyRequestCount, Icons.Default.ShoppingCart, Modifier.weight(1f), { onRequests("sale") })
                    HomeStatCard("گروی", growyRequestCount, Icons.Default.Key, Modifier.weight(1f), { onRequests("growy") })
                    HomeStatCard("کرایه", rentRequestCount, Icons.Default.Search, Modifier.weight(1f), { onRequests("rent") })
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showRegisterOptions = !showRegisterOptions },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF6950A1))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ثبت")
                    Spacer(Modifier.width(5.dp))
                    Icon(if (showRegisterOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }
                if (showRegisterOptions) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showRegisterOptions = false; onRegisterProperty() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.HomeWork, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text("ثبت ملک")
                        }
                        OutlinedButton(onClick = { showRegisterOptions = false; onRegisterRequest() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Assignment, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text("ثبت تقاضا")
                        }
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = onMatch, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("جستجو و تطبیق هوشمند")
            }
        }
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
            Text(homeToPersianDigits(count.toString()), fontSize = 23.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Medium)
        }
    }
}
@Composable
private fun HomeRecords(
    modifier: Modifier,
    title: String,
    rows: List<List<String>>,
    action: String,
    onAdd: () -> Unit,
    archiveMode: Boolean,
    onArchiveModeChange: (Boolean) -> Unit,
    onStatusChange: (String, Long, String) -> Unit
) {
    var expandedStatusRecord by remember { mutableStateOf<String?>(null) }
    var expandedPropertyRecord by remember { mutableStateOf<Long?>(null) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !archiveMode, onClick = { onArchiveModeChange(false) }, label = { Text("فعال") }, modifier = Modifier.weight(1f))
            FilterChip(selected = archiveMode, onClick = { onArchiveModeChange(true) }, label = { Text("بایگانی / غیرفعال") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text(action) }
        if (rows.isEmpty()) {
            Text(if (archiveMode) "در بایگانی موردی ثبت نشده است." else "هنوز مورد فعالی ثبت نشده است.")
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { row ->
                val marker = row.firstOrNull()?.takeIf { it.startsWith("__RAHNAMA_RECORD__:") }
                val parts = marker?.removePrefix("__RAHNAMA_RECORD__:")?.split(":").orEmpty()
                val recordType = parts.getOrNull(0).orEmpty()
                val recordId = parts.getOrNull(1)?.toLongOrNull()
                val currentStatus = parts.getOrNull(2).orEmpty()
                val statusKey = "$recordType:$recordId"
                val statuses = if (recordType == "P") listOf("available", "sold", "rented", "mortgaged", "inactive") else listOf("active", "completed", "cancelled")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        val visibleLines = row.drop(1).filter { it.isNotBlank() }
                        val isExpanded = recordType == "P" && expandedPropertyRecord == recordId
                        val propertyPriceLines = visibleLines.filter {
                            it.startsWith("قیمت:") || it.startsWith("کرایه ماهانه:") || it.startsWith("مبلغ گروی:")
                        }
                        val summaryLines = if (recordType == "P") {
                            (visibleLines.take(3) + propertyPriceLines).distinct()
                        } else visibleLines
                        Column(
                            Modifier.fillMaxWidth().then(
                                if (recordType == "P") Modifier.clickable {
                                    expandedPropertyRecord = if (isExpanded) null else recordId
                                } else Modifier
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            summaryLines.forEachIndexed { index, text ->
                                Text(text, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                            if (recordType == "P") {
                                Text(
                                    if (isExpanded) "نمایش خلاصه" else "نمایش جزئیات بیشتر",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                if (isExpanded) {
                                    visibleLines.filterNot { it in summaryLines }.forEach { detail ->
                                        Text(detail, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        if (recordId != null && recordType in listOf("P", "R")) {
                            Text(
                                if (recordType == "P") "وضعیت ملک: ${homeStatusDisplay(currentStatus)}" else "وضعیت تقاضا: ${homeRequestStatusDisplay(currentStatus)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box {
                                OutlinedButton(onClick = { expandedStatusRecord = statusKey }) {
                                    Icon(Icons.Default.SyncAlt, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("تغییر وضعیت")
                                }
                                DropdownMenu(expanded = expandedStatusRecord == statusKey, onDismissRequest = { expandedStatusRecord = null }) {
                                    statuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(if (recordType == "P") homeStatusDisplay(status) else homeRequestStatusDisplay(status)) },
                                            onClick = {
                                                onStatusChange(recordType, recordId, status)
                                                expandedStatusRecord = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun HomePeople(
    modifier: Modifier,
    people: List<PersonEntity>,
    properties: List<PropertyEntity>,
    requests: List<ClientRequestEntity>,
    onAdd: () -> Unit,
    onSelect: (PersonEntity) -> Unit,
    onCall: (PersonEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedPersonId by remember { mutableStateOf<Long?>(null) }
    val filteredPeople = remember(people, query) {
        val term = query.trim()
        val normalizedTerm = homeToEnglishDigits(term)
        if (term.isBlank()) people else people.filter {
            it.fullName.contains(term, ignoreCase = true) ||
                homeToEnglishDigits(it.phoneNumber).contains(normalizedTerm, ignoreCase = true) ||
                (it.alternatePhone?.let { phone -> homeToEnglishDigits(phone).contains(normalizedTerm, ignoreCase = true) } == true)
        }
    }
    Column(
        modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("اشخاص و مشتریان", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
            label = { Text("جستجو بر اساس نام یا شماره تلفن") },
            trailingIcon = {
                if (query.isNotBlank()) IconButton(onClick = { query = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "پاک‌کردن جستجو")
                }
            }
        )
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("ثبت شخص جدید")
        }
        Text("تعداد اشخاص: ${people.size}", style = MaterialTheme.typography.bodySmall)
        if (filteredPeople.isEmpty()) {
            Text(if (query.isBlank()) "هنوز شخصی ثبت نشده است." else "شخصی با این نام یا شماره پیدا نشد.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPeople, key = { it.id }) { person ->
                    val isExpanded = expandedPersonId == person.id
                    val propertyCount = properties.count { it.ownerId == person.id }
                    val requestCount = requests.count { it.clientId == person.id }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            expandedPersonId = if (isExpanded) null else person.id
                        }
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(person.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    val phoneLine = listOfNotNull(
                                        homeToPersianDigits(person.phoneNumber).takeIf { it.isNotBlank() },
                                        person.alternatePhone?.takeIf { it.isNotBlank() }
                                    ).joinToString(" - ")
                                    Text(phoneLine, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { onCall(person) }) {
                                    Icon(Icons.Default.Call, contentDescription = "تماس با ${person.fullName}")
                                }
                                if (isExpanded) {
                                    Icon(Icons.Default.ExpandLess, contentDescription = "بستن جزئیات")
                                } else {
                                    Icon(Icons.Default.ExpandMore, contentDescription = "نمایش جزئیات")
                                }
                            }
                            if (isExpanded) {
                                HorizontalDivider()
                                Text(homeRoleDisplay(person.roles), color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium)
                                val location = listOfNotNull(person.province, person.city)
                                    .filter { it.isNotBlank() }.joinToString("، ")
                                if (location.isNotBlank()) Text("موقعیت: $location", style = MaterialTheme.typography.bodySmall)
                                person.address?.takeIf { it.isNotBlank() }?.let {
                                    Text("آدرس: $it", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(onClick = { onSelect(person) }, label = { Text("املاک: $propertyCount") })
                                    AssistChip(onClick = { onSelect(person) }, label = { Text("تقاضاها: $requestCount") })
                                }
                                TextButton(onClick = { onSelect(person) }) {
                                    Text("مشاهده پروفایل و سوابق")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PersonHistoryEntry(
    val timestamp: Long,
    val category: String,
    val title: String,
    val location: String,
    val amount: String,
    val status: String
)

private fun homeHistoryDate(timestamp: Long): String =
    java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonDetailDialog(
    person: PersonEntity,
    properties: List<PropertyEntity>,
    requests: List<ClientRequestEntity>,
    onDismiss: () -> Unit,
    onCall: (PersonEntity) -> Unit,
    onSms: (PersonEntity) -> Unit,
    onWhatsApp: (PersonEntity) -> Unit,
    onEdit: (PersonEntity) -> Unit
) {
    var selectedTab by remember(person.id) { mutableIntStateOf(0) }
    val activeProperties = properties.filter { it.status == "available" }
    val activeRequests = requests.filter { it.requestStatus == "active" }
    val history = (
        properties.map { property ->
            val amount = when (property.listingType) {
                "rent" -> property.monthlyRent?.let { "کرایه ماهانه: ${it.toLong()} افغانی" } ?: "کرایه ثبت نشده"
                "growy" -> buildString {
                    append("مبلغ گروی: ")
                    append(property.priceAmount?.toLong()?.toString() ?: "ثبت نشده")
                    append(" افغانی")
                    property.monthlyRent?.let { append(" • کرایه ماهانه: ${it.toLong()} افغانی") }
                }
                else -> property.priceAmount?.let { "قیمت فروش: ${it.toLong()} افغانی" } ?: "قیمت ثبت نشده"
            }
            PersonHistoryEntry(
                timestamp = property.createdAt,
                category = "ملک ثبت‌شده",
                title = "${property.propertyCode} • ${homePropertyType(property.propertyType)} • ${homeListingType(property.listingType)}",
                location = listOfNotNull(property.province, property.city, property.district).filter { it.isNotBlank() }.joinToString("، "),
                amount = amount,
                status = homeStatusDisplay(property.status)
            )
        } + requests.map { request ->
            val amount = when (request.listingType) {
                "rent" -> request.maxMonthlyRent?.let { "حداکثر کرایه: ${it.toLong()} افغانی" } ?: "بودجه کرایه ثبت نشده"
                "growy" -> request.desiredGrowyAmount?.let { "مبلغ گروی موردنظر: ${it.toLong()} افغانی" } ?: "مبلغ گروی ثبت نشده"
                else -> {
                    if (request.budgetMin != null || request.budgetMax != null)
                        "بودجه: ${request.budgetMin?.toLong()?.toString() ?: "—"} تا ${request.budgetMax?.toLong()?.toString() ?: "—"} افغانی"
                    else "بودجه ثبت نشده"
                }
            }
            PersonHistoryEntry(
                timestamp = request.createdAt,
                category = "تقاضای مشتری",
                title = "${homePropertyType(request.propertyType)} • ${homeListingType(request.listingType)}",
                location = listOfNotNull(request.province, request.city, request.district).filter { it.isNotBlank() }.joinToString("، "),
                amount = amount,
                status = if (request.requestStatus == "active") "فعال" else request.requestStatus
            )
        }
    ).sortedByDescending { it.timestamp }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).widthIn(max = 560.dp).heightIn(min = 300.dp, max = 760.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("پروفایل شخص", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(person.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(homeRoleDisplay(person.roles), color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "بستن پروفایل") }
                }

                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )) {
                    Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("شماره اصلی: ${homeToPersianDigits(person.phoneNumber)}", style = MaterialTheme.typography.bodyLarge)
                        person.alternatePhone?.takeIf { it.isNotBlank() }?.let { Text("شماره دوم: ${homeToPersianDigits(it)}") }
                        val location = listOfNotNull(person.province, person.city).filter { it.isNotBlank() }.joinToString("، ")
                        if (location.isNotBlank()) Text("موقعیت: $location")
                        person.address?.takeIf { it.isNotBlank() }?.let { Text("آدرس: $it") }
                        person.notes?.takeIf { it.isNotBlank() }?.let { Text("یادداشت: $it") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCall(person) },
                        modifier = Modifier.weight(1f).heightIn(min = 68.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("تماس", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    OutlinedButton(
                        onClick = { onSms(person) },
                        modifier = Modifier.weight(1f).heightIn(min = 68.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("پیامک", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    OutlinedButton(
                        onClick = { onWhatsApp(person) },
                        modifier = Modifier.weight(1f).heightIn(min = 68.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("واتساپ", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
                OutlinedButton(onClick = { onEdit(person) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("ویرایش اطلاعات شخص")
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text("فعال (${activeProperties.size + activeRequests.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = { Text("تاریخچه (${history.size})") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                        text = { Text("جزئیات") })
                }

                Column(Modifier.fillMaxWidth().weight(1f, fill = true).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (selectedTab) {
                        0 -> {
                            Text("ملک‌های فعال برای فروش، کرایه یا گروی", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            if (activeProperties.isEmpty()) {
                                Text("این شخص فعلاً ملک فعالی برای عرضه ندارد.")
                            } else activeProperties.sortedByDescending { it.createdAt }.forEach { property ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${property.propertyCode} • ${homePropertyType(property.propertyType)}",
                                            fontWeight = FontWeight.Bold)
                                        Text("نوع معامله: ${homeListingType(property.listingType)}")
                                        Text("موقعیت: " + listOfNotNull(property.province, property.city, property.district)
                                            .filter { it.isNotBlank() }.joinToString("، "))
                                        property.address?.takeIf { it.isNotBlank() }?.let { Text("آدرس: $it") }
                                        when (property.listingType) {
                                            "rent" -> property.monthlyRent?.let { Text("کرایه ماهانه: ${it.toLong()} افغانی") }
                                            "growy" -> {
                                                property.priceAmount?.let { Text("مبلغ گروی: ${it.toLong()} افغانی") }
                                                property.monthlyRent?.let { Text("کرایه ماهانه: ${it.toLong()} افغانی") }
                                                property.growyCondition?.let { Text("شرایط گروی: ${homeGrowyDisplay(it)}") }
                                            }
                                            else -> property.priceAmount?.let { Text("قیمت فروش: ${it.toLong()} افغانی") }
                                        }
                                        property.bedrooms?.let { Text("تعداد اتاق: $it") }
                                        property.areaValue?.let { Text("مساحت: $it ${property.areaUnit.orEmpty()}") }
                                        Text("تاریخ ثبت: ${homeHistoryDate(property.createdAt)}")
                                    }
                                }
                            }

                            HorizontalDivider()
                            Text("تقاضاهای فعال شخص", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            if (activeRequests.isEmpty()) {
                                Text("این شخص فعلاً تقاضای فعال خرید، کرایه یا گروی ندارد.")
                            } else activeRequests.sortedByDescending { it.createdAt }.forEach { request ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${homePropertyType(request.propertyType)} • ${homeListingType(request.listingType)}",
                                            fontWeight = FontWeight.Bold)
                                        Text("موقعیت موردنظر: " + listOfNotNull(request.province, request.city, request.district)
                                            .filter { it.isNotBlank() }.joinToString("، "))
                                        when (request.listingType) {
                                            "rent" -> request.maxMonthlyRent?.let { Text("حداکثر کرایه ماهانه: ${it.toLong()} افغانی") }
                                            "growy" -> {
                                                request.desiredGrowyAmount?.let { Text("مبلغ گروی موردنظر: ${it.toLong()} افغانی") }
                                                Text("شرایط گروی: ${homeGrowyDisplay(request.growyCondition)}")
                                            }
                                            else -> if (request.budgetMin != null || request.budgetMax != null)
                                                Text("بودجه: ${request.budgetMin?.toLong()?.toString() ?: "—"} تا ${request.budgetMax?.toLong()?.toString() ?: "—"} افغانی")
                                        }
                                        request.bedroomsMin?.let { Text("حداقل تعداد اتاق: $it") }
                                        Text("تاریخ ثبت: ${homeHistoryDate(request.createdAt)}")
                                    }
                                }
                            }
                        }
                        1 -> {
                            Text("سوابق به ترتیب تاریخ ثبت، از جدید به قدیم", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (history.isEmpty()) Text("هنوز سابقه‌ای برای این شخص ثبت نشده است.")
                            history.forEach { item ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text(item.title, fontWeight = FontWeight.Bold)
                                                Text(item.category, style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(homeHistoryDate(item.timestamp), style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (item.location.isNotBlank()) Text("موقعیت: ${item.location}")
                                        Text(item.amount)
                                        Text("وضعیت فعلی: ${item.status}")
                                    }
                                }
                            }
                        }
                        else -> {
                            Text("تاریخ ثبت شخص: ${homeHistoryDate(person.createdAt)}")
                            Text("شماره اصلی: ${person.phoneNumber}")
                            person.alternatePhone?.takeIf { it.isNotBlank() }?.let { Text("شماره دوم: $it") }
                            person.province?.takeIf { it.isNotBlank() }?.let { Text("ولایت: $it") }
                            person.city?.takeIf { it.isNotBlank() }?.let { Text("شهر/ناحیه: $it") }
                            person.address?.takeIf { it.isNotBlank() }?.let { Text("آدرس دقیق: $it") }
                            person.notes?.takeIf { it.isNotBlank() }?.let { Text("یادداشت: $it") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonEntryDialog(
    initialPerson: PersonEntity?,
    onDismiss: () -> Unit,
    onSave: (PersonEntity) -> Unit
) {
    val roles = listOf("فروشنده", "خریدار", "متقاضی مستأجر", "متقاضی گروی", "متقاضی کرایی")
    val values = remember(initialPerson?.id) {
        mutableStateListOf(
            initialPerson?.fullName.orEmpty(),
            initialPerson?.phoneNumber.orEmpty(),
            initialPerson?.alternatePhone.orEmpty(),
            initialPerson?.province.orEmpty(),
            initialPerson?.city.orEmpty(),
            initialPerson?.address.orEmpty(),
            initialPerson?.notes.orEmpty()
        )
    }
    var role by remember(initialPerson?.id) { mutableStateOf(homeRoleDisplay(initialPerson?.roles ?: "other")) }
    var error by remember(initialPerson?.id) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialPerson == null) "ثبت شخص جدید" else "ویرایش شخص", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val labels = listOf("نام و نام خانوادگی *", "شماره تلفن اصلی *", "شماره دوم (واتساپ/کاری/خانه)", "ولایت", "شهر/ناحیه", "آدرس دقیق", "توضیحات")
                labels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = values[index],
                        onValueChange = { values[index] = if (index == 1 || index == 2) homeFilterPhoneInput(it) else it; error = null },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = index < 6
                    )
                }
                HomeDropdownField("نقش شخص", role, roles) { role = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (values[0].isBlank() || values[1].isBlank()) {
                    error = "نام و شماره تلفن اصلی الزامی است."
                } else {
                    val mappedRole = when (role) {
                        "فروشنده" -> "seller"
                        "خریدار" -> "buyer"
                        "متقاضی مستأجر" -> "tenant_applicant"
                        "متقاضی گروی" -> "growy_applicant"
                        "متقاضی کرایی" -> "rent_applicant"
                        else -> "seller"
                    }
                    onSave(PersonEntity(
                        id = initialPerson?.id ?: 0,
                        fullName = values[0].trim(),
                        phoneNumber = homeToEnglishDigits(values[1]),
                        alternatePhone = homeToEnglishDigits(values[2]).ifBlank { null },
                        province = values[3].trim().ifBlank { null },
                        city = values[4].trim().ifBlank { null },
                        address = values[5].trim().ifBlank { null },
                        notes = values[6].trim().ifBlank { null },
                        roles = mappedRole,
                        createdAt = initialPerson?.createdAt ?: System.currentTimeMillis()
                    ))
                }
            }) { Text(if (initialPerson == null) "ثبت شخص" else "ذخیره تغییرات") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}


private fun homeToEnglishDigits(value: String): String = buildString {
    value.forEach { ch ->
        append(when (ch) {
            in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
            in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
            else -> ch
        })
    }
}

private fun homeToPersianDigits(value: String): String = buildString {
    value.forEach { ch ->
        append(if (ch in '0'..'9') ('۰'.code + (ch.code - '0'.code)).toChar() else ch)
    }
}

private fun homeFilterPhoneInput(value: String): String =
    value.filter { it in '0'..'9' || it in '۰'..'۹' || it in '٠'..'٩' || it == '+' || it == '-' || it == ' ' || it == '(' || it == ')' }

private fun dialPersonNumber(context: android.content.Context, phone: String, onError: (String) -> Unit) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone.trim(), null)))
    } catch (_: Exception) {
        onError("بازکردن شماره‌گیر ممکن نشد.")
    }
}

private fun openSms(context: android.content.Context, phone: String, onError: (String) -> Unit) {
    try {
        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phone.trim(), null)))
    } catch (_: Exception) {
        onError("بازکردن برنامهٔ پیامک ممکن نشد.")
    }
}

private fun openWhatsApp(context: android.content.Context, phone: String, onError: (String) -> Unit) {
    var digits = phone.filter { it.isDigit() }
    if (digits.startsWith("00")) digits = digits.drop(2)
    if (digits.startsWith("0")) digits = "93" + digits.drop(1)
    else if (digits.length == 9) digits = "93$digits"
    if (digits.isBlank() || digits.length < 10) {
        onError("شماره تلفن معتبر نیست.")
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")))
    } catch (_: Exception) {
        onError("بازکردن واتساپ ممکن نشد. واتساپ و کد کشور شماره را بررسی کنید.")
    }
}

private fun homeRoleDisplay(role: String): String = when (role) {
    "seller", "owner" -> "فروشنده"
    "buyer" -> "خریدار"
    "tenant_applicant", "tenant" -> "متقاضی مستأجر"
    "growy_applicant" -> "متقاضی گروی"
    "rent_applicant", "applicant" -> "متقاضی کرایی"
    else -> "فروشنده"
}

private fun homeGrowyDisplay(value: String): String = when (value) {
    "sunni" -> "اهل سنت"
    "shia" -> "اهل تشیع"
    "either" -> "هرکدام"
    else -> "نامشخص"
}

private fun homeStatusDisplay(status: String): String = when (status) {
    "available" -> "موجود و فعال"
    "sold" -> "فروخته‌شده"
    "rented" -> "کرایه داده‌شده"
    "mortgaged" -> "گروی داده‌شده"
    "inactive" -> "لغوشده / پس‌گرفته‌شده"
    else -> status
}

private fun homeRequestStatusDisplay(status: String): String = when (status) {
    "active" -> "فعال"
    "completed" -> "تکمیل‌شده"
    "cancelled" -> "لغوشده"
    else -> status
}

@Composable
private fun HomeSettings(
    modifier: Modifier,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    var showRestoreWarning by remember { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("تنظیمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("پشتیبان‌گیری روی دستگاه یا حافظه‌ای که خودتان انتخاب می‌کنید انجام می‌شود.")
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("پشتیبان‌گیری و بازیابی", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("یک نسخه از دیتابیس شامل اشخاص، املاک و تقاضاها ذخیره کنید. برای انتقال اطلاعات به موبایل دیگر نیز می‌توانید فایل را منتقل کنید.")
                Button(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ساخت فایل پشتیبان")
                }
                OutlinedButton(onClick = { showRestoreWarning = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("بازیابی از فایل پشتیبان")
                }
                Text(
                    "هشدار: بازیابی، اطلاعات فعلی برنامه را با اطلاعات فایل پشتیبان جایگزین می‌کند.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (showRestoreWarning) {
            AlertDialog(
                onDismissRequest = { showRestoreWarning = false },
                title = { Text("تأیید بازیابی") },
                text = { Text("اطلاعات فعلی جایگزین می‌شود. پیش از ادامه، اگر اطلاعات فعلی برایتان مهم است، ابتدا از آن پشتیبان بگیرید. آیا فایل پشتیبان را انتخاب می‌کنید؟") },
                confirmButton = {
                    Button(onClick = {
                        showRestoreWarning = false
                        onRestore()
                    }) { Text("انتخاب فایل") }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreWarning = false }) { Text("لغو") }
                }
            )
        }
    }
}

@Composable
private fun HomeMatching(
    modifier: Modifier,
    properties: List<PropertyEntity>,
    requests: List<ClientRequestEntity>,
    people: List<PersonEntity>
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("تطبیق هوشمند املاک", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("املاک مناسب هر تقاضا بر اساس میزان مطابقت مرتب شده‌اند.")
        }
        if (requests.isEmpty()) {
            item { Text("برای شروع، یک تقاضای فعال ثبت کنید.") }
        }
        items(requests, key = { it.id }) { request ->
            val matches = MatchingEngine.matchPropertiesToRequest(request, properties)
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        homeMapDisplayProperty(request.propertyType) + " • " + homeMapDisplayListing(request.listingType),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text("مشتری: " + (people.firstOrNull { it.id == request.clientId }?.fullName ?: "نامشخص"))
                    Text("موقعیت: " + request.province + (request.city?.let { "، " + it } ?: ""))
                    Text("نتیجه: " + matches.size + " ملک مناسب", color = MaterialTheme.colorScheme.primary)
                    if (matches.isEmpty()) {
                        Text("فعلاً ملک مطابق پیدا نشد. مشخصات ملک‌ها یا تقاضا را بررسی کنید.")
                    } else {
                        matches.take(5).forEachIndexed { index, match ->
                            val property = match.property
                            val owner = people.firstOrNull { it.id == property.ownerId }
                            HorizontalDivider()
                            Text(
                                (index + 1).toString() + ". " + property.propertyCode + " — " + match.score + "%",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(homeMapDisplayProperty(property.propertyType) + " • " + homeMapDisplayListing(property.listingType))
                            Text("موقعیت: " + property.province + "، " + property.city +
                                (property.district?.let { "، " + it } ?: ""))
                            when (property.listingType) {
                                "rent" -> Text("کرایه ماهانه: " + (property.monthlyRent?.toLong()?.toString() ?: "ثبت نشده") + " افغانی")
                                "growy" -> {
                                    Text("مبلغ گروی: " + (property.priceAmount?.toLong()?.toString() ?: "ثبت نشده") + " افغانی")
                                    property.monthlyRent?.let { Text("کرایه ماهانه: " + it.toLong() + " افغانی") }
                                }
                                else -> Text("قیمت فروش: " + (property.priceAmount?.toLong()?.toString() ?: "ثبت نشده") + " افغانی")
                            }
                            property.bedrooms?.let { Text("تعداد اتاق: " + it) }
                            if (owner != null) {
                                Text("مالک: " + owner.fullName)
                                Text("شماره تماس مالک: " + owner.phoneNumber)
                            }
                            Text("دلایل تطبیق: " + match.reasons.joinToString("، "), style = MaterialTheme.typography.bodySmall)
                        }
                        if (matches.size > 5) {
                            Text("۵ نتیجهٔ برتر از " + matches.size + " مورد نمایش داده شد.")
                        }
                    }
                }
            }
        }
    }
}
private data class PropertyFormField(val index: Int, val label: String, val options: List<String>? = null)

@Composable
private fun PropertyEntryDialog(onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    // Common fields retain their existing indexes; type-specific details are stored as JSON in propertyTypeDetails.
    val values = remember { mutableStateListOf<String>().also { state -> repeat(45) { state.add("") } } }
    val propertyType = values[3]
    val listingType = values[4]
    val yesNo = listOf("بلی", "نخیر", "نامشخص")
    val direction = listOf("چپه", "راسته", "نامشخص")
    val fields = buildList {
        add(PropertyFormField(0, "اسم مالک *"))
        add(PropertyFormField(1, "شماره تماس ۱ *"))
        add(PropertyFormField(2, "شماره تماس ۲"))
        add(PropertyFormField(3, "نوع ملک *", listOf("باغ", "تعمیر", "آپارتمان", "زمین", "دکان", "مارکیت")))
        add(PropertyFormField(4, "نوع معامله *", listOf("فروشی", "کرایی", "گروی")))
        add(PropertyFormField(5, "ولایت *"))
        add(PropertyFormField(6, "شهر *"))
        add(PropertyFormField(7, "ناحیه / گذر"))
        add(PropertyFormField(8, "آدرس عمومی"))
        when (propertyType) {
            "باغ" -> {
                add(PropertyFormField(37, "آدرس باغ"))
                add(PropertyFormField(38, "مقدار مساحت باغ"))
                add(PropertyFormField(44, "واحد مساحت باغ", listOf("بسوه", "جریب", "متر مربع")))
                add(PropertyFormField(39, "نوع سند ملکیت", listOf("شرعی", "عرفی", "قباله", "بدون سند", "نامشخص")))
                add(PropertyFormField(40, "تعداد درخت‌ها"))
                add(PropertyFormField(41, "نوع درخت‌ها"))
                add(PropertyFormField(42, "نیاز به آب برای آبیاری؟", yesNo))
                if (values[42] == "بلی") add(PropertyFormField(43, "منبع آب"))
                add(PropertyFormField(21, "دیوار دارد؟", yesNo))
                if (values[21] == "بلی") add(PropertyFormField(22, "تعداد دیوارهای همسایه", listOf("0", "1", "2", "3")))
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(10, "آفتاب‌رخ", direction))
                add(PropertyFormField(25, "مشخصات بیشتر باغ"))
            }
            "تعمیر" -> {
                add(PropertyFormField(9, "شماره نمره"))
                add(PropertyFormField(10, "آفتاب‌رخ", direction))
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(14, "تعداد اتاق‌ها"))
                add(PropertyFormField(15, "آشپزخانه دارد؟", yesNo))
                add(PropertyFormField(18, "مهمان‌خانه دارد؟", yesNo))
                add(PropertyFormField(26, "کوته دارد؟", yesNo))
                add(PropertyFormField(38, "مقدار متراژ زمین"))
                add(PropertyFormField(44, "واحد متراژ زمین", listOf("بسوه", "جریب", "متر مربع")))
                add(PropertyFormField(39, "متراژ ساختمان"))
                add(PropertyFormField(21, "دیوار دارد؟", yesNo))
                if (values[21] == "بلی") {
                    add(PropertyFormField(40, "مالکیت دیوار چپ", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(41, "مالکیت دیوار راست", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(42, "مالکیت دیوار پیش‌رو", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(43, "مالکیت دیوار پشت‌سر", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                }
                add(PropertyFormField(25, "مشخصات بیشتر تعمیر"))
            }
            "آپارتمان" -> {
                add(PropertyFormField(9, "شماره نمره"))
                add(PropertyFormField(37, "شماره واحد"))
                add(PropertyFormField(38, "طبقه"))
                add(PropertyFormField(14, "تعداد اتاق‌ها"))
                add(PropertyFormField(15, "آشپزخانه دارد؟", yesNo))
                add(PropertyFormField(18, "مهمان‌خانه دارد؟", yesNo))
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(10, "آفتاب‌رخ", direction))
                add(PropertyFormField(39, "مساحت آپارتمان"))
                add(PropertyFormField(25, "مشخصات بیشتر آپارتمان"))
            }
            "زمین" -> {
                add(PropertyFormField(37, "نوع زمین", listOf("زراعتی", "مسکونی")))
                add(PropertyFormField(38, "مقدار مساحت زمین"))
                add(PropertyFormField(44, "واحد مساحت زمین", listOf("بسوه", "جریب", "متر مربع")))
                add(PropertyFormField(9, "شماره زمین"))
                add(PropertyFormField(21, "دیوار دارد؟", yesNo))
                if (values[21] == "بلی") {
                    add(PropertyFormField(40, "مالکیت دیوار چپ", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(41, "مالکیت دیوار راست", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(42, "مالکیت دیوار پیش‌رو", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                    add(PropertyFormField(43, "مالکیت دیوار پشت‌سر", listOf("از مالک", "از همسایه", "مشترک", "نامشخص")))
                }
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(17, "برمه آب دارد؟", yesNo))
                add(PropertyFormField(25, "مشخصات بیشتر زمین"))
            }
            "دکان" -> {
                add(PropertyFormField(37, "آدرس دکان"))
                add(PropertyFormField(38, "شماره دکان"))
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(39, "متراژ دکان"))
                add(PropertyFormField(25, "مشخصات بیشتر دکان"))
            }
            "مارکیت" -> {
                add(PropertyFormField(37, "تعداد دکان‌ها"))
                add(PropertyFormField(17, "برمه دارد؟", yesNo))
                add(PropertyFormField(16, "برق دارد؟", yesNo))
                add(PropertyFormField(38, "ساختمان دارد؟", yesNo))
                if (values[38] == "بلی") add(PropertyFormField(39, "توضیحات ساختمان"))
                add(PropertyFormField(10, "آفتاب‌رخ", direction))
                add(PropertyFormField(25, "مشخصات بیشتر مارکیت"))
            }
        }
        when (listingType) {
            "فروشی" -> {
                add(PropertyFormField(11, "قیمت پیشنهادی فروش *"))
                add(PropertyFormField(27, "تخفیف‌پذیر است؟", yesNo))
                if (values[27] == "بلی") add(PropertyFormField(28, "حداقل قیمت قابل قبول *"))
            }
            "کرایی" -> {
                add(PropertyFormField(12, "قیمت کرایه ماهانه *"))
                add(PropertyFormField(29, "تخفیف‌پذیر است؟", yesNo))
                if (values[29] == "بلی") add(PropertyFormField(30, "حداقل کرایه قابل قبول *"))
                add(PropertyFormField(36, "مشخصات بیشتر معامله"))
            }
            "گروی" -> {
                add(PropertyFormField(11, "مبلغ گروی *"))
                add(PropertyFormField(31, "مدت گروی"))
                add(PropertyFormField(32, "تخفیف‌پذیر است؟", yesNo))
                if (values[32] == "بلی") add(PropertyFormField(33, "حداقل مبلغ قابل قبول گروی *"))
                add(PropertyFormField(34, "ملک کرایه ماهانه هم دارد؟", yesNo))
                if (values[34] == "بلی") add(PropertyFormField(35, "مبلغ کرایه ماهانه"))
                add(PropertyFormField(13, "شرایط گروی", listOf("اهل سنت", "اهل تشیع", "نامشخص")))
                add(PropertyFormField(36, "مشخصات بیشتر معامله"))
            }
        }
        add(PropertyFormField(24, "توضیحات عمومی"))
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
                        HomeDropdownField(field.label, values[field.index], field.options) { selected ->
                            values[field.index] = selected
                            if (field.index == 3) {
                                for (index in 9..10) values[index] = ""
                                for (index in 14..26) values[index] = ""
                                for (index in 37..44) values[index] = ""
                            }
                            if (field.index == 4) {
                                if (selected != "فروشی") { values[27] = ""; values[28] = "" }
                                if (selected != "کرایی") { values[29] = ""; values[30] = "" }
                                if (selected != "گروی") { for (index in 31..36) values[index] = "" }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            values[field.index],
                            { values[field.index] = if (field.label.contains("شماره تماس")) homeFilterPhoneInput(it) else it },
                            label = { Text(field.label) }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("ذخیره ملک") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDropdownField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember(label) { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeRequestEntryDialog(onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val values = remember { mutableStateListOf("خرید", "", "", "", "", "مهم نیست", "مهم نیست", "مهم نیست", "مهم نیست", "مهم نیست", "", "", "", "", "") }
    val requestType = values[0]
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت تقاضای مشتری", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(Modifier.heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                item { RequestDropdownField("نوع تقاضا", values[0], listOf("خریدار", "کرایه", "گروی")) { values[0] = it } }
                item { OutlinedTextField(values[1], { values[1] = it }, label = { Text("نام *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(values[2], { values[2] = homeFilterPhoneInput(it) }, label = { Text("شماره اول *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(values[3], { values[3] = homeFilterPhoneInput(it) }, label = { Text("شماره دوم") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(values[4], { values[4] = it }, label = { Text("تعداد اتاق") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { RequestDropdownField("آشپزخانه", values[5], listOf("مهم نیست", "دارد", "ندارد")) { values[5] = it } }
                item { RequestDropdownField("مهمان‌خانه", values[6], listOf("مهم نیست", "دارد", "ندارد")) { values[6] = it } }
                item { RequestDropdownField("کوته", values[7], listOf("مهم نیست", "دارد", "ندارد")) { values[7] = it } }
                item { RequestDropdownField("برق", values[8], listOf("مهم نیست", "دارد", "ندارد")) { values[8] = it } }
                item { RequestDropdownField("برمه", values[9], listOf("مهم نیست", "دارد", "ندارد")) { values[9] = it } }
                if (requestType == "خریدار") {
                    item { OutlinedTextField(values[10], { values[10] = it }, label = { Text("حداقل بودجه خرید") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(values[11], { values[11] = it }, label = { Text("حداکثر بودجه خرید") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                }
                if (requestType == "کرایه") {
                    item { OutlinedTextField(values[12], { values[12] = it }, label = { Text("حداکثر کرایه ماهانه قابل پرداخت") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                }
                if (requestType == "گروی") {
                    item { OutlinedTextField(values[13], { values[13] = it }, label = { Text("مبلغ گروی موجود") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                }
                item { OutlinedTextField(values[14], { values[14] = it }, label = { Text("توضیحات بیشتر") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
            }
        },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestDropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}

private fun validateSimpleRequestForm(v: List<String>): String? {
    fun number(index: Int, label: String): Double? {
        val raw = homeToEnglishDigits(v[index]).trim()
        if (raw.isBlank()) return null
        val parsed = raw.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite() || parsed < 0) {
            throw IllegalArgumentException("$label باید عدد صفر یا بزرگ‌تر باشد.")
        }
        return parsed
    }
    return try {
        val type = v[0]
        val rooms = homeToEnglishDigits(v[4]).trim()
        if (rooms.isNotBlank() && (rooms.toIntOrNull() == null || rooms.toInt() < 0)) {
            return "تعداد اتاق باید عدد صحیح صفر یا بزرگ‌تر باشد."
        }
        when (type) {
            "خریدار", "خرید" -> {
                val min = number(10, "حداقل بودجه خرید")
                val max = number(11, "حداکثر بودجه خرید")
                if (min != null && max != null && min > max) return "حداقل بودجه خرید نمی‌تواند از حداکثر بودجه بیشتر باشد."
            }
            "کرایه" -> number(12, "حداکثر کرایه ماهانه")
            "گروی" -> number(13, "مبلغ گروی موجود")
        }
        null
    } catch (e: IllegalArgumentException) {
        e.message ?: "مقادیر عددی تقاضا را بررسی کنید."
    }
}

@Composable
fun HomeEntryDialog(title: String, fields: List<String>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val values = remember(title) { mutableStateListOf<String>().also { state -> fields.forEach { state.add("") } } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { LazyColumn(Modifier.heightIn(max = 450.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fields.size) { i ->
                val isPhoneField = fields[i].contains("شماره تماس")
                OutlinedTextField(value = values[i], onValueChange = {
                    values[i] = if (isPhoneField) homeFilterPhoneInput(it) else it
                }, label = { Text(fields[i]) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        } },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } })
}

private fun homeFormatPropertyDetails(raw: String): String {
    val labels = linkedMapOf(
        "saleDiscountAllowed" to "تخفیف در فروش", "saleMinimumPrice" to "حداقل قیمت فروش",
        "rentDiscountAllowed" to "تخفیف در کرایه", "rentMinimumPrice" to "حداقل کرایه",
        "growyDuration" to "مدت گروی", "growyDiscountAllowed" to "تخفیف در گروی",
        "growyMinimumAmount" to "حداقل مبلغ گروی", "growyHasMonthlyRent" to "کرایه ماهانه همراه گروی",
        "growyMonthlyRent" to "مبلغ کرایه ماهانه", "dealNotes" to "یادداشت معامله",
        "typeField1" to "مشخصات ملک", "typeField2" to "جزئیات ملک", "typeField3" to "سایر مشخصات",
        "wallLeftOwnership" to "مالکیت دیوار چپ", "wallRightOwnership" to "مالکیت دیوار راست",
        "wallFrontOwnership" to "مالکیت دیوار پیش‌رو", "wallBackOwnership" to "مالکیت دیوار عقب",
        "landAreaUnit" to "واحد مساحت", "propertySpecificNotes" to "توضیحات اختصاصی ملک",
        "propertyNumber" to "شماره ملک", "sunExposure" to "آفتاب‌گیری",
        "allLeftOwnership" to "دیوار چپ", "allRightOwnership" to "دیوار راست",
        "allFrontOwnership" to "دیوار پیش‌رو", "allBackOwnership" to "دیوار عقب"
    )
    return try {
        val json = org.json.JSONObject(raw)
        labels.mapNotNull { (key, label) ->
            if (!json.has(key) || json.isNull(key)) null
            else json.optString(key).trim().takeIf { it.isNotBlank() }?.let { value -> "$label: $value" }
        }.joinToString("\n")
    } catch (_: Exception) {
        raw.takeIf { it.isNotBlank() }?.let { "توضیحات اضافی: $it" }.orEmpty()
    }
}
private fun homeBuildPropertyExtraDetails(v: List<String>): String? {
    val labels = mapOf(
        27 to "saleDiscountAllowed", 28 to "saleMinimumPrice",
        29 to "rentDiscountAllowed", 30 to "rentMinimumPrice",
        31 to "growyDuration", 32 to "growyDiscountAllowed", 33 to "growyMinimumAmount",
        34 to "growyHasMonthlyRent", 35 to "growyMonthlyRent", 36 to "dealNotes",
        37 to "typeField1", 38 to "typeField2", 39 to "typeField3",
        40 to "wallLeftOwnership", 41 to "wallRightOwnership",
        42 to "wallFrontOwnership", 43 to "wallBackOwnership", 44 to "landAreaUnit"
    )
    val json = org.json.JSONObject()
    labels.forEach { (index, key) -> if (v.getOrNull(index).orEmpty().isNotBlank()) json.put(key, v[index].trim()) }
    if (v.getOrNull(25).orEmpty().isNotBlank()) json.put("propertySpecificNotes", v[25].trim())
    if (v.getOrNull(10).orEmpty().isNotBlank()) json.put("sunExposure", v[10].trim())
    if (v.getOrNull(9).orEmpty().isNotBlank()) json.put("propertyNumber", v[9].trim())
    return json.toString().takeIf { json.length() > 0 }
}

private fun validatePropertyForm(v: List<String>): String? {
    val listingType = v[4]
    if (listingType == "فروشی" && v[11].isBlank()) return "قیمت پیشنهادی فروش ضروری است."
    if (listingType == "کرایی" && v[12].isBlank()) return "قیمت کرایه ماهانه ضروری است."
    if (listingType == "گروی" && v[11].isBlank()) return "مبلغ گروی ضروری است."
    if (listingType == "فروشی" && v[27] == "بلی" && v[28].isBlank()) return "حداقل قیمت قابل قبول ضروری است."
    if (listingType == "کرایی" && v[29] == "بلی" && v[30].isBlank()) return "حداقل کرایه قابل قبول ضروری است."
    if (listingType == "گروی" && v[32] == "بلی" && v[33].isBlank()) return "حداقل مبلغ گروی قابل قبول ضروری است."
    if (listingType == "گروی" && v[34] == "بلی" && v[35].isBlank()) return "مبلغ کرایه ماهانه ضروری است."

    fun number(index: Int, label: String): Double? {
        val raw = homeToEnglishDigits(v.getOrElse(index) { "" }).trim()
        if (raw.isBlank()) return null
        val value = raw.toDoubleOrNull()
        if (value == null || !value.isFinite() || value < 0) {
            throw IllegalArgumentException("${label} باید عدد صفر یا بزرگ‌تر باشد.")
        }
        return value
    }

    return try {
        listOf(11 to "قیمت / مبلغ گروی", 12 to "کرایه ماهانه", 28 to "حداقل قیمت فروش",
            30 to "حداقل کرایه", 33 to "حداقل مبلغ گروی", 35 to "کرایه ماهانه گروی").forEach { number(it.first, it.second) }
        when (v[3]) {
            "باغ" -> { number(38, "مساحت باغ"); number(40, "تعداد درخت‌ها") }
            "تعمیر" -> { number(38, "متراژ زمین"); number(39, "متراژ ساختمان") }
            "آپارتمان" -> { number(38, "طبقه"); number(39, "مساحت آپارتمان") }
            "زمین" -> number(38, "مساحت زمین")
            "دکان" -> number(39, "متراژ دکان")
            "مارکیت" -> number(37, "تعداد دکان‌ها")
        }
        val bedrooms = homeToEnglishDigits(v[14]).trim()
        if (bedrooms.isNotBlank() && (bedrooms.toIntOrNull() == null || bedrooms.toInt() < 0)) {
            return "تعداد اتاق باید عدد صحیح صفر یا بزرگ‌تر باشد."
        }
        val areaIndex = when (v[3]) { "باغ", "زمین", "تعمیر" -> 38; "آپارتمان", "دکان" -> 39; else -> -1 }
        if (areaIndex >= 0) {
            val area = number(areaIndex, "مساحت")
            if (area != null && area <= 0) return "مساحت باید بیشتر از صفر باشد."
        }
        null
    } catch (e: IllegalArgumentException) {
        e.message ?: "مقادیر عددی ملک را بررسی کنید."
    }
}
private fun validateRequestForm(v: List<String>): String? {
    fun optionalNonNegative(index: Int, label: String): Double? {
        val raw = v[index].trim()
        if (raw.isBlank()) return null
        val value = raw.toDoubleOrNull()
        if (value == null || !value.isFinite() || value < 0) {
            throw IllegalArgumentException("$label باید عدد صفر یا بزرگ‌تر باشد.")
        }
        return value
    }

    return try {
        val minBudget = optionalNonNegative(4, "حداقل بودجه")
        val maxBudget = optionalNonNegative(5, "حداکثر بودجه")
        optionalNonNegative(6, "حداکثر کرایه ماهانه")
        optionalNonNegative(7, "مبلغ گروی")
        val bedrooms = v[12].trim()
        if (bedrooms.isNotBlank() && (bedrooms.toIntOrNull() == null || bedrooms.toInt() < 0)) {
            return "حداقل تعداد اتاق باید عدد صحیح صفر یا بزرگ‌تر باشد."
        }
        if (minBudget != null && maxBudget != null && minBudget > maxBudget) {
            return "حداقل بودجه نمی‌تواند از حداکثر بودجه بیشتر باشد."
        }
        null
    } catch (e: IllegalArgumentException) {
        e.message ?: "مقادیر عددی تقاضا را بررسی کنید."
    }
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
private fun homeMapRole(value: String) = when { value.contains("فروشنده") || value.contains("مالک") -> "seller"; value.contains("خریدار") -> "buyer"; value.contains("مستأجر") || value.contains("مستاجر") -> "tenant_applicant"; value.contains("گروی") -> "growy_applicant"; value.contains("کرایی") || value.contains("کرایه") -> "rent_applicant"; else -> "seller" }
private fun homePropertyType(value: String) = when (value) { "house" -> "خانه"; "building" -> "تعمیر"; "apartment" -> "آپارتمان"; "land" -> "زمین"; "shop" -> "دکان"; "market" -> "مارکیت"; "garden" -> "باغ"; else -> "سایر" }
private fun homeListingType(value: String) = when (value) { "rent" -> "کرایی"; "growy" -> "گروی"; else -> "فروشی" }
private fun homeMapDisplayProperty(value: String) = homePropertyType(value)
private fun homeMapDisplayListing(value: String) = homeListingType(value)
private fun homeJalaliYear(): Int { val c = Calendar.getInstance(); return c.get(Calendar.YEAR) - if (c.get(Calendar.MONTH) + 1 >= 3) 621 else 622 }
