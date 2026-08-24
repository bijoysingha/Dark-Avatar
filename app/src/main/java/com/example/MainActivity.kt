package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MatrixPulseDot
import com.example.ui.screens.AiWorkstationScreen
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.CodeAuditScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DefenseScreen
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.MalwareScreen
import com.example.ui.screens.NetworkScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSky
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CyberViewModel
import kotlinx.coroutines.launch

data class NavItem(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val isPrimaryBottom: Boolean = false
)

class MainActivity : ComponentActivity() {
    private val viewModel: CyberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: CyberViewModel) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navItems = listOf(
        NavItem(0, "Dashboard", Icons.Default.Dashboard, isPrimaryBottom = true),
        NavItem(1, "AI Copilot", Icons.Default.Psychology, isPrimaryBottom = true),
        NavItem(2, "Defense", Icons.Default.Shield, isPrimaryBottom = false),
        NavItem(3, "Code Engine", Icons.Default.Code, isPrimaryBottom = true),
        NavItem(4, "Analyze", Icons.Default.Security, isPrimaryBottom = false),
        NavItem(5, "Network", Icons.Default.Lan, isPrimaryBottom = false),
        NavItem(6, "Malware", Icons.Default.BugReport, isPrimaryBottom = false),
        NavItem(7, "Incidents", Icons.Default.ReportProblem, isPrimaryBottom = false),
        NavItem(8, "Terminal", Icons.Default.Terminal, isPrimaryBottom = true),
        NavItem(9, "Reports", Icons.Default.Description, isPrimaryBottom = true),
        NavItem(10, "Settings", Icons.Default.Settings, isPrimaryBottom = false)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CyberDarkSurface,
                drawerContentColor = CyberTextPrimary,
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MatrixPulseDot()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "DARK AVATAR",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CYBERSECURITY AI WORKSTATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                }

                HorizontalDivider(color = CyberBorder, thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    navItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (selectedIndex == item.index) CyberCyan else CyberTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedIndex == item.index) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 1.sp
                                )
                            },
                            selected = selectedIndex == item.index,
                            onClick = {
                                selectedIndex = item.index
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.12f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = CyberCyan,
                                unselectedTextColor = CyberTextPrimary
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(vertical = 3.dp)
                                .testTag("nav_drawer_${item.title.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DARK AVATAR",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                color = CyberSurfaceVariant,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, CyberBorder)
                            ) {
                                Text(
                                    text = navItems.find { it.index == selectedIndex }?.title?.uppercase() ?: "",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = CyberCyan
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { selectedIndex = 10 }, // Settings
                            modifier = Modifier.testTag("top_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (selectedIndex == 10) CyberCyan else CyberTextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberDarkSurface,
                        titleContentColor = CyberCyan
                    )
                )
            },
            bottomBar = {
                val bottomNavItems = navItems.filter { it.isPrimaryBottom }
                Surface(
                    color = CyberBlack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyberSurfaceVariant,
                            border = BorderStroke(1.dp, CyberBorder),
                            shadowElevation = 12.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = selectedIndex == item.index
                                    Surface(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { selectedIndex = item.index }
                                            .testTag("bottom_nav_${item.title.lowercase().replace(" ", "_")}"),
                                        shape = CircleShape,
                                        color = if (isSelected) CyberCyan else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (isSelected) Color(0xFF000000) else CyberTextMuted
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.title,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF000000)
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
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(CyberBlack)
            ) {
                AnimatedContent(
                    targetState = selectedIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> DashboardScreen(viewModel = viewModel, onNavigateTab = { selectedIndex = it })
                        1 -> AiWorkstationScreen(viewModel = viewModel)
                        2 -> DefenseScreen()
                        3 -> CodeAuditScreen(viewModel = viewModel)
                        4 -> AnalyzeScreen(viewModel = viewModel)
                        5 -> NetworkScreen(viewModel = viewModel)
                        6 -> MalwareScreen(viewModel = viewModel)
                        7 -> IncidentsScreen(viewModel = viewModel)
                        8 -> TerminalScreen(viewModel = viewModel)
                        9 -> ReportsScreen(viewModel = viewModel)
                        10 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
