package fr.hozakan.flysightcompanion

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail.ConfigDetailMenuActions
import fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail.ConfigDetailScreen
import fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files.ListConfigFileMenuActions
import fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files.ListConfigFilesScreen
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightCompanionTheme
import fr.hozakan.flysightcompanion.designsystem.theme.TextConfiguration
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.dialogmodule.DialogHandler
import fr.hozakan.flysightcompanion.dialogmodule.LocalDialogService
import fr.hozakan.flysightcompanion.framework.compose.LocalMenuState
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.dagger.Injectable
import fr.hozakan.flysightcompanion.framework.menu.rememberActionBarMenuState
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config.DeviceConfigurationMenuActions
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config.DeviceConfigurationScreen
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail.DeviceDetailMenuActions
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail.DeviceDetailScreen
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.file.DeviceFileScreen
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDevicesMenuActions
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDevicesScreen
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.recordsmodule.ui.detail.RecordDetailMenuActions
import fr.hozakan.flysightcompanion.recordsmodule.ui.detail.RecordDetailScreen
import fr.hozakan.flysightcompanion.recordsmodule.ui.list.ListRecordsScreen
import fr.hozakan.flysightcompanion.recordsmodule.ui.plot.PlotSettingsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import fr.hozakan.flysightcompanion.R as LocalR

class MainActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dialogService: fr.hozakan.flysightcompanion.dialogmodule.MutableDialogService

    @Inject
    lateinit var json: Gson

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlySightCompanionTheme {

                val menuState = rememberActionBarMenuState()

                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory,
                    LocalMenuState provides menuState,
                    LocalDialogService provides dialogService
                ) {
                    val navController = rememberNavController()
                    val currentBackStack = navController.currentBackStackEntryAsState()

                    DialogHandler()

                    Scaffold(
                        topBar = {
                            val currentRoute = currentBackStack.value?.destination?.route
                            val title = when (currentRoute) {
                                AppScreen.DeviceTab.DeviceList.route -> {
                                    stringResource(LocalR.string.app_name)
                                }

                                AppScreen.DeviceTab.DeviceDetail.route -> {
                                    stringResource(R.string.screen_title_device_detail)
                                }

                                AppScreen.ConfigTab.ConfigList.route -> {
                                    stringResource(R.string.screen_title_config_list)
                                }

                                AppScreen.RecordTab.PlotSettings.route -> {
                                    stringResource(R.string.screen_title_config_list)
                                }

                                AppScreen.DeviceTab.DeviceFile.route -> {
                                    val filePath =
                                        currentBackStack.value?.arguments?.getString("filePath")
                                            ?.split(";")
                                    stringResource(
                                        R.string.screen_title_file,
                                        filePath?.lastOrNull()
                                            ?: stringResource(R.string.misc_unknown)
                                    )
                                }

                                else -> {
                                    stringResource(LocalR.string.app_name)
                                }
                            }
                            TopAppBar(
                                title = { Text(title) },
                                navigationIcon = {
                                    when (currentRoute) {

                                        AppScreen.RecordTab.RecordDetail.route,
                                        AppScreen.RecordTab.PlotSettings.route,
                                        AppScreen.DeviceTab.DeviceDetail.route -> {
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.misc_navigate_up)
                                                )
                                            }
                                        }

                                        AppScreen.DeviceTab.DeviceFile.route -> {
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.misc_navigate_up)
                                                )
                                            }
                                        }

                                        AppScreen.ConfigTab.ConfigDetail.route -> {
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.misc_navigate_up)
                                                )
                                            }
                                        }

                                        AppScreen.DeviceTab.DeviceConfig.route -> {
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.misc_navigate_up)
                                                )
                                            }
                                        }

                                        else -> {
                                            Icon(
                                                modifier = Modifier.requiredSize(24.dp),
                                                painter = painterResource(LocalR.drawable.deprecated_flysight_icon),
                                                contentDescription = stringResource(R.string.misc_home)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    when (currentRoute) {
                                        AppScreen.ConfigTab.ConfigList.route -> {
                                            ListConfigFileMenuActions {
                                                navController.navigate(
                                                    AppScreen.ConfigTab.ConfigDetail.buildRoute(
                                                        ""
                                                    )
                                                )
                                            }
                                        }

                                        AppScreen.ConfigTab.ConfigDetail.route -> {
                                            ConfigDetailMenuActions()
                                        }

                                        AppScreen.DeviceTab.DeviceDetail.route -> {
                                            val deviceId =
                                                currentBackStack.value?.arguments?.getString("deviceId")
                                            if (deviceId != null) {
                                                DeviceDetailMenuActions(
                                                    deviceId = deviceId
                                                ) {
                                                    navController.navigate(
                                                        AppScreen.DeviceTab.DeviceConfig.buildRoute(
                                                            json.toJson(it)
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        AppScreen.DeviceTab.DeviceConfig.route -> {
                                            val config =
                                                currentBackStack.value?.arguments?.getString("config")
                                            if (config != null) {
                                                DeviceConfigurationMenuActions(
                                                    conf = json.fromJson(
                                                        config,
                                                        ConfigFile::class.java
                                                    ),
                                                )
                                            }
                                        }

                                        AppScreen.DeviceTab.DeviceList.route -> {
                                            ListFlySightDevicesMenuActions()
                                        }

                                        AppScreen.RecordTab.RecordDetail.route -> {
                                            RecordDetailMenuActions {
                                                navController.navigate(AppScreen.RecordTab.PlotSettings.route)
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (currentBackStack.value?.destination?.route == AppScreen.DeviceTab.DeviceList.route ||
                                currentBackStack.value?.destination?.route == AppScreen.ConfigTab.ConfigList.route ||
                                currentBackStack.value?.destination?.route == AppScreen.RecordTab.RecordList.route
                            ) {
                                val selectedTab =
                                    updateTransition(targetState = currentBackStack.value?.destination?.route)

                                val deviceScale by selectedTab.animateFloat { if (it == AppScreen.DeviceTab.DeviceList.route) 1.2f else 1f }
                                val configScale by selectedTab.animateFloat { if (it == AppScreen.ConfigTab.ConfigList.route) 1.2f else 1f }
                                val recordScale by selectedTab.animateFloat { if (it == AppScreen.RecordTab.RecordList.route) 1.2f else 1f }
                                BottomAppBar(
                                    actions = {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .graphicsLayer {
                                                    scaleX = deviceScale
                                                    scaleY = deviceScale
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    modifier = Modifier.requiredSize(24.dp),
                                                    painter = painterResource(LocalR.drawable.flysight_logo_only),
                                                    contentDescription = stringResource(R.string.misc_devices)
                                                )
                                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                                FText(
                                                    text = stringResource(R.string.misc_devices),
                                                    configuration = TextConfiguration.TabTitle
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        navController.navigate(AppScreen.DeviceTab.route)
                                                    }
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .graphicsLayer {
                                                    scaleX = configScale
                                                    scaleY = configScale
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Engineering,
                                                    contentDescription = stringResource(R.string.screen_title_config_list)
                                                )
                                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                                FText(
                                                    text = stringResource(R.string.screen_title_config_list),
                                                    configuration = TextConfiguration.TabTitle
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        navController.navigate(AppScreen.ConfigTab.route)
                                                    }
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .graphicsLayer {
                                                    scaleX = recordScale
                                                    scaleY = recordScale
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AreaChart,
                                                    contentDescription = stringResource(R.string.screen_title_record_list)
                                                )
                                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                                FText(
                                                    text = stringResource(R.string.screen_title_record_list),
                                                    configuration = TextConfiguration.TabTitle
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        navController.navigate(AppScreen.RecordTab.route)
                                                    }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            NavHost(
                                navController = navController,
                                startDestination = AppScreen.DeviceTab.route
                            ) {
                                navigation(
                                    route = AppScreen.DeviceTab.route,
                                    startDestination = AppScreen.DeviceTab.DeviceList.route
                                ) {
                                    composable(route = AppScreen.DeviceTab.DeviceList.route) {
                                        ListFlySightDevicesScreen(
                                            onDeviceSelected = {
                                                navController.navigate(
                                                    AppScreen.DeviceTab.DeviceDetail.buildRoute(
                                                        it.uuid
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    composable(route = AppScreen.DeviceTab.DeviceDetail.route) { backStackEntry ->
                                        val deviceId =
                                            backStackEntry.arguments?.getString("deviceId")
                                        if (deviceId != null) {
                                            DeviceDetailScreen(
                                                deviceId = deviceId,
                                                onFileClicked = {
                                                    navController.navigate(
                                                        AppScreen.DeviceTab.DeviceFile.buildRoute(
                                                            deviceId,
                                                            it
                                                        )
                                                    )
                                                },
                                                onNavigateUp = {
                                                    navController.popBackStack()
                                                }
                                            )
                                        }
                                    }
                                    composable(route = AppScreen.DeviceTab.DeviceFile.route) { backStackEntry ->
                                        val deviceId =
                                            backStackEntry.arguments?.getString("deviceId")
                                        val filePath =
                                            backStackEntry.arguments?.getString("filePath")
                                                ?.split(";")
                                        if (deviceId != null && filePath != null) {
                                            DeviceFileScreen(
                                                deviceId = deviceId,
                                                filePath = "/" + filePath.joinToString("/"),
                                                onNavigateUp = {
                                                    navController.popBackStack()
                                                }
                                            )
                                        }

                                    }
                                    composable(route = AppScreen.DeviceTab.DeviceConfig.route) { backStackEntry ->
                                        val config =
                                            backStackEntry.arguments?.getString("config")
                                        if (config != null) {
                                            DeviceConfigurationScreen(
                                                conf = json.fromJson(
                                                    config,
                                                    ConfigFile::class.java
                                                ),
                                            )
                                        }
                                    }
                                }
                                navigation(
                                    route = AppScreen.ConfigTab.route,
                                    startDestination = AppScreen.ConfigTab.ConfigList.route
                                ) {
                                    composable(route = AppScreen.ConfigTab.ConfigList.route) {
                                        ListConfigFilesScreen(
                                            onConfigSelected = {
                                                navController.navigate(
                                                    AppScreen.ConfigTab.ConfigDetail.buildRoute(
                                                        it.name
                                                    )
                                                )
                                            },
                                            onCreateConfigFile = {
                                                navController.navigate(
                                                    AppScreen.ConfigTab.ConfigDetail.buildRoute(
                                                        ""
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    composable(route = AppScreen.ConfigTab.ConfigDetail.route) { backStackEntry ->
                                        val configName =
                                            backStackEntry.arguments?.getString("configName")
                                                ?: return@composable
                                        ConfigDetailScreen(
                                            configName = configName,
                                            onNavigateUp = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }
                                navigation(
                                    route = AppScreen.RecordTab.route,
                                    startDestination = AppScreen.RecordTab.RecordList.route
                                ) {
                                    composable(route = AppScreen.RecordTab.RecordList.route) {
                                        ListRecordsScreen { selectedRecord ->
                                            navController.navigate(
                                                AppScreen.RecordTab.RecordDetail.buildRoute(
                                                    selectedRecord.phoneFilePath
                                                )
                                            )
                                        }
                                    }
                                    composable(route = AppScreen.RecordTab.RecordDetail.route) { backStackEntry ->
                                        val recordName =
                                            backStackEntry.arguments?.getString("recordName")
                                                ?: return@composable
                                        RecordDetailScreen(
                                            recordName = recordName
                                        )
                                    }
                                    composable(route = AppScreen.RecordTab.PlotSettings.route) {
                                        PlotSettingsScreen()
                                    }
                                }
                            }

                            BackHandler {
                                val currentRoute = currentBackStack.value?.destination?.route
                                if (currentRoute == AppScreen.DeviceTab.DeviceList.route || currentRoute == AppScreen.ConfigTab.ConfigList.route) {
                                    finish()
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}