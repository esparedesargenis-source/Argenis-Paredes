package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ServiceRequest
import com.example.data.model.WorkOrder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.DarkSlateNavy
import com.example.viewmodel.AppTab
import com.example.viewmodel.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ServiceViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val workOrders by viewModel.workOrders.collectAsStateWithLifecycle()
    val serviceRequests by viewModel.serviceRequests.collectAsStateWithLifecycle()
    val selectedWorkOrder by viewModel.selectedWorkOrder.collectAsStateWithLifecycle()
    val scannedWorkOrder by viewModel.scannedWorkOrder.collectAsStateWithLifecycle()
    val isScanningActive by viewModel.isScanningActive.collectAsStateWithLifecycle()
    val showRequestForm by viewModel.showRequestForm.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display messages via Snackbar
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUiMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Brand Logo Icon matching AP custom dynamic identity - Ruby Hexagon & Carbon Frame
                        ArgenisParedesLogo(
                            modifier = Modifier
                                .size(38.dp)
                                .padding(vertical = 2.dp)
                        )
                        Column {
                            Text(
                                text = "Argenis Paredes",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Servicios en construcción en general.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary, // Dynamic bronze copper highlight
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleRequestForm(true) },
                        modifier = Modifier.testTag("new_request_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva Solicitud",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.INICIO,
                    onClick = { viewModel.selectTab(AppTab.INICIO) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.PROYECTOS,
                    onClick = { viewModel.selectTab(AppTab.PROYECTOS) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Proyectos") },
                    label = { Text("Proyectos", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_projects")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.ESCANER_QR,
                    onClick = { viewModel.selectTab(AppTab.ESCANER_QR) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Escanear QR") },
                    label = { Text("Lector QR", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_qr")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content tabs switcher
            when (selectedTab) {
                AppTab.INICIO -> HomeScreen(
                    viewModel = viewModel,
                    requests = serviceRequests
                )
                AppTab.PROYECTOS -> ProjectsScreen(
                    orders = workOrders,
                    onSelectOrder = { viewModel.selectWorkOrder(it) }
                )
                AppTab.ESCANER_QR -> QrScannerScreen(
                    scannedWorkOrder = scannedWorkOrder,
                    isScanning = isScanningActive,
                    onSimulateScan = { viewModel.scanQrCode(it) },
                    onPayOrder = { viewModel.payScannedOrder() },
                    onReset = { viewModel.resetScanner() },
                    activeOrders = workOrders
                )
            }

            // Custom Dialog sheet for service request creation
            if (showRequestForm) {
                CustomRequestDialog(
                    onDismiss = { viewModel.toggleRequestForm(false) },
                    viewModel = viewModel
                )
            }

            // Project/WorkOrder detailed invoice sheet
            selectedWorkOrder?.let { order ->
                WorkOrderDetailDialog(
                    order = order,
                    onDismiss = { viewModel.selectWorkOrder(null) },
                    onPayNow = {
                        viewModel.selectWorkOrder(null)
                        viewModel.selectTab(AppTab.ESCANER_QR)
                        viewModel.scanQrCode(order.qrCodeData)
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN SECTION
// ==========================================
@Composable
fun HomeScreen(
    viewModel: ServiceViewModel,
    requests: List<ServiceRequest>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Upper premium slide banner with brand values
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Garantía",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SOLIDEZ • COMPROMISO • TRANSPARENCIA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        ArgenisParedesLogo(
                            modifier = Modifier.size(32.dp),
                            gemSizeFraction = 0.55f
                        )
                    }
                    Text(
                        text = "Construcción en General y Mantenimiento Profesional",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Unificamos la solidez técnica con herramientas de gestión digital premium para darte control, confianza y cuentas claras en tus órdenes de trabajo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.toggleRequestForm(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cta_request_service")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Herramienta",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solicitar Presupuesto Especializado", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats row for visual impact
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "+15 Años",
                    subtitle = "De Trayectoria"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "100%",
                    subtitle = "Obras y Firmas"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Garantía",
                    subtitle = "Post-Mano de Obra"
                )
            }
        }

        // Service Catalog Header
        item {
            Text(
                text = "Catálogo de Soluciones Técnicas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Catalog Categories Grid Items
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CatalogItemCard(
                    title = "Albañilería y Remodelaciones",
                    description = "Acabados finos y rústicos, revoque, lozas, nivelado de contramuros, vaciados estructurales y reformas comerciales integrales.",
                    serviceType = "Albañilería",
                    icon = Icons.Default.Build,
                    onSelect = {
                        viewModel.updateFormServiceType("Albañilería")
                        viewModel.toggleRequestForm(true)
                    }
                )
                CatalogItemCard(
                    title = "Electricidad Certificada",
                    description = "Estudios de carga, instalaciones homologadas, balanceo de tableros, iluminación arquitectónica LED y reparaciones críticas de líneas.",
                    serviceType = "Electricidad",
                    icon = Icons.Default.Warning,
                    onSelect = {
                        viewModel.updateFormServiceType("Electricidad")
                        viewModel.toggleRequestForm(true)
                    }
                )
                CatalogItemCard(
                    title = "Pintura y Recubrimientos",
                    description = "Impermeabilización hidrófuga tropical extrema, masillas elásticas para fisuras, pintura residencial, comercial y de naves industriales.",
                    serviceType = "Pintura",
                    icon = Icons.Default.Check,
                    onSelect = {
                        viewModel.updateFormServiceType("Pintura")
                        viewModel.toggleRequestForm(true)
                    }
                )
                CatalogItemCard(
                    title = "Fontanería e Hidráulica",
                    description = "Distribución por termofusión, reparaciones de fugas ocultas, instalaciones sanitarias, griferías empotrables y optimizadores de caudal.",
                    serviceType = "Fontanería",
                    icon = Icons.Default.Add,
                    onSelect = {
                        viewModel.updateFormServiceType("Fontanería")
                        viewModel.toggleRequestForm(true)
                    }
                )
            }
        }

        // User Requests Section if available
        if (requests.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tus Solicitudes de Cotización Recientes (${requests.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(requests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Solicitud #${req.id}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Urgencia: ${req.urgency}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Servicio: " + req.serviceType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = req.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estado: " + req.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = req.preferredDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun CatalogItemCard(
    title: String,
    description: String,
    serviceType: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Icono catálogo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quiero cotizar " + serviceType + " →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


// ==========================================
// 2. PROJECTS TRACKER SCREEN
// ==========================================
@Composable
fun ProjectsScreen(
    orders: List<WorkOrder>,
    onSelectOrder: (WorkOrder) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }

    val filteredOrders = remember(orders, selectedFilter) {
        if (selectedFilter == "Todos") orders
        else orders.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Seguimiento de Proyectos",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Valide el desglose, los presupuestos por hitos y realice el escaneo del QR para saldar deudas de obras.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle filters segment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Todos", "Pendiente", "En Progreso", "Completado").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No hay proyectos",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se encontraron órdenes en esta categoría.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredOrders) { order ->
                    WorkOrderCard(
                        order = order,
                        onClick = { onSelectOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkOrderCard(
    order: WorkOrder,
    onClick: () -> Unit
) {
    val statusColor = when (order.status) {
        "Completado" -> Color(0xFF2E7D32)
        "En Progreso" -> Color(0xFF1565C0)
        else -> Color(0xFFD84315)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("work_order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = order.serviceType.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = order.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CLIENTE: ${order.clientName}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "CÓDIGO: ${order.qrCodeData}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL PRESUPUESTO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", order.totalPrice)} USD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


// ==========================================
// 3. QR SCANNER & TRANSACTION COMPONENT
// ==========================================
@Composable
fun QrScannerScreen(
    scannedWorkOrder: WorkOrder?,
    isScanning: Boolean,
    onSimulateScan: (String) -> Unit,
    onPayOrder: () -> Unit,
    onReset: () -> Unit,
    activeOrders: List<WorkOrder>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserMover"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Escanear",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Escáner QR Interno",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Simulador táctico de escaneo instantáneo de órdenes Argenis Paredes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Camera frame view simulator
        if (scannedWorkOrder == null) {
            item {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Corner targets simulation
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 5.dp.toPx()
                        val length = 32.dp.toPx()

                        // Top-Left corner
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(0f, 0f),
                            end = Offset(length, 0f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(0f, 0f),
                            end = Offset(0f, length),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )

                        // Top-Right corner
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width - length, 0f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, length),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )

                        // Bottom-Left corner
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(0f, size.height),
                            end = Offset(length, size.height),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(0f, size.height),
                            end = Offset(0f, size.height - length),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )

                        // Bottom-Right corner
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(size.width, size.height),
                            end = Offset(size.width - length, size.height),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = SafetyOrange,
                            start = Offset(size.width, size.height),
                            end = Offset(size.width, size.height - length),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    if (isScanning) {
                        // Flashing scan screen or line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = 218.dp * laserOffsetY)
                                .background(SafetyOrange)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33FF6D00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PROCESANDO...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Camera target",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Alinee el Código QR",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Nuestra app buscará los presupuestos certificados",
                                color = Color.LightGray.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Selector to simulate scanning particular QR code labels on startup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Simulador: Selecciona orden activa para Escaneo QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )

                        activeOrders.forEach { order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { onSimulateScan(order.qrCodeData) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Código: " + order.qrCodeData,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (order.paymentStatus == "Pagado") Color(0xFFE8F5E9)
                                            else Color(0xFFFBE9E7)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (order.paymentStatus == "Pagado") "Saldado" else "Escanear",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.paymentStatus == "Pagado") Color(0xFF2E7D32) else Color(0xFFD84315)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Instant digital bill found
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Invoice Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FACTURA DIGITAL INSTANTÁNEA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "ORDEN: ${scannedWorkOrder.qrCodeData}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = onReset) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar Escaneada")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                        // Client Information
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            InvoiceTextRow(label = "Propietario / Cliente:", valText = scannedWorkOrder.clientName)
                            InvoiceTextRow(label = "Dirección de Obra:", valText = scannedWorkOrder.address)
                            InvoiceTextRow(label = "Especialidad Técnica:", valText = scannedWorkOrder.serviceType)
                            InvoiceTextRow(label = "Estado de Proyecto:", valText = scannedWorkOrder.status)
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                        // Granular Breakdowns - Materials and Labor
                        Text(
                            text = "DESGLOSE DE MATERIALES Y RECURSOS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = scannedWorkOrder.materialsDetail,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }

                        Text(
                            text = "MANO DE OBRA Y CERTIFICACIONES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = scannedWorkOrder.laborDetail,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Monetary calculation Box
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(14.dp)
                        ) {
                            InvoiceCostRow(label = "Subtotal Materiales:", cost = scannedWorkOrder.subtotalMaterials)
                            InvoiceCostRow(label = "Subtotal Mano de Obra:", cost = scannedWorkOrder.subtotalLabor)
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 0.6.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "MONTO TOTAL LIQUIDABLE:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "$${String.format("%.2f", scannedWorkOrder.totalPrice)} USD",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Safe Native Payment action block
                        if (scannedWorkOrder.paymentStatus == "Pendiente") {
                            Button(
                                onClick = onPayOrder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("pay_invoice_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Pay", tint = Color.White)
                                    Text(
                                        text = "Aprobar y Liquidar Presupuesto",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // Paid Success Stamp Seal
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, Color(0xFF2E7D32), RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Pagado",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "TRANSACCIÓN APROBADA Y LIQUIDADA NATIVAMENTE",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color(0xFF2E7D32),
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Actualización completada en tiempo real",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32).copy(alpha = 0.8f)
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
fun InvoiceTextRow(label: String, valText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(text = valText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InvoiceCostRow(label: String, cost: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "$${String.format("%.2f", cost)} USD", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}


// ==========================================
// 4. CUSTOM DIALOG FOR CREATING SERVICE REQS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRequestDialog(
    onDismiss: () -> Unit,
    viewModel: ServiceViewModel
) {
    val formServiceType by viewModel.formServiceType.collectAsStateWithLifecycle()
    val formDescription by viewModel.formDescription.collectAsStateWithLifecycle()
    val formClientName by viewModel.formClientName.collectAsStateWithLifecycle()
    val formClientPhone by viewModel.formClientPhone.collectAsStateWithLifecycle()
    val formClientEmail by viewModel.formClientEmail.collectAsStateWithLifecycle()
    val formUrgency by viewModel.formUrgency.collectAsStateWithLifecycle()
    val formPreferredDate by viewModel.formPreferredDate.collectAsStateWithLifecycle()
    val formAddress by viewModel.formAddress.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("request_form_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solicitar Presupuesto Técnico",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Categoría o Especialidad Requerida:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Albañilería", "Electricidad", "Pintura", "Fontanería").forEach { cat ->
                                val isSelected = formServiceType == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.updateFormServiceType(cat) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = formClientName,
                            onValueChange = { viewModel.updateFormClientName(it) },
                            label = { Text("Tu Nombre Completo *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_client_name"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = formAddress,
                            onValueChange = { viewModel.updateFormAddress(it) },
                            label = { Text("Dirección de la Obra *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_address"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = formDescription,
                            onValueChange = { viewModel.updateFormDescription(it) },
                            label = { Text("Detalle de Trabajos Deseados *") },
                            placeholder = { Text("Ej: Remodelación completa de baño social con tubería de agua...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .testTag("form_description"),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = formClientPhone,
                                onValueChange = { viewModel.updateFormClientPhone(it) },
                                label = { Text("Teléfono") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = formPreferredDate,
                                onValueChange = { viewModel.updateFormPreferredDate(it) },
                                label = { Text("Fecha Pref.") },
                                placeholder = { Text("Pronto") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Prioridad o Urgencia:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Baja", "Media", "Alta").forEach { level ->
                                val isSelected = formUrgency == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.updateFormUrgency(level) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { viewModel.submitServiceRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_request_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Registrar Solicitud en la Plataforma", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}


// ==========================================
// 5. WORK ORDER BREAKDOWN MODAL DIALOG
// ==========================================
@Composable
fun WorkOrderDetailDialog(
    order: WorkOrder,
    onDismiss: () -> Unit,
    onPayNow: () -> Unit
) {
    val statusColor = when (order.status) {
        "Completado" -> Color(0xFF2E7D32)
        "En Progreso" -> Color(0xFF1565C0)
        else -> Color(0xFFD84315)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_details_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Diagonal Close header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(order.status.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = statusColor, fontFamily = FontFamily.Monospace)
                        }
                        Text(text = "Ficha de Obra Técnica", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Detalles")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 380.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(text = "PROYECTO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = order.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = order.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }

                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(2.dp))
                        InvoiceTextRow(label = "Cliente Propietario:", valText = order.clientName)
                        InvoiceTextRow(label = "Ubicación de Trabajo:", valText = order.address)
                        InvoiceTextRow(label = "Fecha de Orden:", valText = "Confirmado en Sistema")
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "DESGLOSE DE MATERIALES:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(text = order.materialsDetail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                        }
                    }

                    item {
                        Text(text = "DETALLE DE OPERACIONES TÉCNICAS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(text = order.laborDetail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Dash Ticket line or calculations
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            InvoiceCostRow(label = "Mano de Obra Autorizada:", cost = order.subtotalLabor)
                            InvoiceCostRow(label = "Materiales Catalogados:", cost = order.subtotalMaterials)
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "PRESUPUESTO NETO:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "$${String.format("%.2f", order.totalPrice)} USD", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Dynamic barcode element matching order values
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CÓDIGO QR PARA ESCANEO DE ORDEN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Draw dynamic vector QR code representation using custom Canvas
                            QrCodeComponent(
                                data = order.qrCodeData,
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = order.qrCodeData,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom CTA action based on paymentStatus
                if (order.paymentStatus == "Pendiente") {
                    Button(
                        onClick = onPayNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("pay_from_details_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Scan", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Iniciar Escaneo QR de Orden", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ESTE PROYECTO SE ENCUENTRA SALDADO",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cerrar Ficha de Proyecto")
                    }
                }
            }
        }
    }
}


// ==========================================
// 6. DETAILED VECTOR QR COMPONENT WITH CANVAS
// ==========================================
@Composable
fun QrCodeComponent(
    data: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = size.width
        val squareCount = 13 // 13x13 grid squares representation

        val moduleSize = sizePx / squareCount

        // 1. Draw Corner Finder Patterns (Three big bounding squares: Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(0f, 0f, moduleSize, 4)
        drawFinderPattern((squareCount - 4) * moduleSize, 0f, moduleSize, 4)
        drawFinderPattern(0f, (squareCount - 4) * moduleSize, moduleSize, 4)

        // 2. Deterministic pseudo-random generation of data modules based on data string hash
        val seed = data.hashCode().toLong()
        val random = java.util.Random(seed)

        for (row in 0 until squareCount) {
            for (col in 0 until squareCount) {
                // Skip finder pattern zones to avoid overlapping
                val isTopLeftFinder = row < 4 && col < 4
                val isTopRightFinder = row < 4 && col >= squareCount - 4
                val isBottomLeftFinder = row >= squareCount - 4 && col < 4

                if (!isTopLeftFinder && !isTopRightFinder && !isBottomLeftFinder) {
                    val deservesBlackPixel = random.nextBoolean()
                    if (deservesBlackPixel) {
                        drawRect(
                            color = Color(0xFF0F172A), // Slate Navy for premium look instead of pure black
                            topLeft = Offset(col * moduleSize, row * moduleSize),
                            size = Size(moduleSize + 0.5f, moduleSize + 0.5f) // Minor bleed prevention overlap
                        )
                    }
                }
            }
        }
    }
}

// Draw a QR concentric finder pattern
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    moduleSize: Float,
    modules: Int
) {
    val totalSize = modules * moduleSize

    // Outer solid box
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(x, y),
        size = Size(totalSize, totalSize)
    )

    // Inner white hollow square
    val hollowOffset = moduleSize
    val hollowSize = totalSize - (2 * moduleSize)
    drawRect(
        color = Color.White,
        topLeft = Offset(x + hollowOffset, y + hollowOffset),
        size = Size(hollowSize, hollowSize)
    )

    // Center solid box
    val centerOffset = 1.3f * moduleSize
    val centerSize = totalSize - (2.6f * moduleSize)
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(x + centerOffset, y + centerOffset),
        size = Size(centerSize, centerSize)
    )
}

// ==========================================
// 7. BRAND LOGO COMPOSABLE (CARBON FRAME & FACETED RUBY GEM)
// ==========================================
@Composable
fun ArgenisParedesLogo(
    modifier: Modifier = Modifier,
    gemSizeFraction: Float = 0.5f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = kotlin.math.min(w, h) / 2f

        // 1. Draw architectural steel metallic framework forming stylized house / pillars
        val metalPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy - r) // Top apex
            lineTo(cx + r * 0.9f, cy - r * 0.4f)
            lineTo(cx + r * 0.9f, cy + r * 0.7f)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.9f, cy + r * 0.7f)
            lineTo(cx - r * 0.9f, cy - r * 0.4f)
            close()
        }
        drawPath(
            path = metalPath,
            color = Color(0xFF1E2530),
            style = Stroke(width = r * 0.22f, cap = StrokeCap.Round)
        )
        // Accent edge shine on the metal frame mimicking refined copper/bronze glow
        drawPath(
            path = metalPath,
            color = Color(0xFFC6996D).copy(alpha = 0.45f),
            style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
        )

        // 2. Beautiful Central Glowing Faceted Ruby Hex Gem (3D crystal geometry)
        val gemRadius = r * gemSizeFraction
        val vertices = List(6) { index ->
            val angleRad = Math.toRadians((index * 60.0 - 30.0))
            Offset(
                (cx + gemRadius * kotlin.math.cos(angleRad)).toFloat(),
                (cy + gemRadius * kotlin.math.sin(angleRad)).toFloat()
            )
        }

        // 6 Facets forming a 3D gemstone layout
        // Facet 0 (top/right)
        val f0 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[0].x, vertices[0].y)
            lineTo(vertices[1].x, vertices[1].y)
            close()
        }
        drawPath(f0, color = Color(0xFFFF4149))

        // Facet 1 (right/bottom)
        val f1 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[1].x, vertices[1].y)
            lineTo(vertices[2].x, vertices[2].y)
            close()
        }
        drawPath(f1, color = Color(0xFFE51D24))

        // Facet 2 (bottom)
        val f2 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[2].x, vertices[2].y)
            lineTo(vertices[3].x, vertices[3].y)
            close()
        }
        drawPath(f2, color = Color(0xFF910505))

        // Facet 3 (bottom/left)
        val f3 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[3].x, vertices[3].y)
            lineTo(vertices[4].x, vertices[4].y)
            close()
        }
        drawPath(f3, color = Color(0xFF7A0303))

        // Facet 4 (left/top)
        val f4 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[4].x, vertices[4].y)
            lineTo(vertices[5].x, vertices[5].y)
            close()
        }
        drawPath(f4, color = Color(0xFFC40E15))

        // Facet 5 (top-left)
        val f5 = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(vertices[5].x, vertices[5].y)
            lineTo(vertices[0].x, vertices[0].y)
            close()
        }
        drawPath(f5, color = Color(0xFFFF525A))

        // Shimmer glint highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = gemRadius * 0.12f,
            center = Offset(cx - gemRadius * 0.25f, cy - gemRadius * 0.25f)
        )
    }
}

