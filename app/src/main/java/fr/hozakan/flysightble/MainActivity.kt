package fr.hozakan.flysightble

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import fr.hozakan.flusightble.dialog.DialogHandler
import fr.hozakan.flusightble.dialog.LocalDialogService
import fr.hozakan.flusightble.dialog.MutableDialogService
import fr.hozakan.flysightble.configfilesmodule.ui.config_detail.ConfigDetailMenuActions
import fr.hozakan.flysightble.configfilesmodule.ui.config_detail.ConfigDetailScreen
import fr.hozakan.flysightble.configfilesmodule.ui.list_files.ListConfigFileMenuActions
import fr.hozakan.flysightble.configfilesmodule.ui.list_files.ListConfigFilesScreen
import fr.hozakan.flysightble.framework.compose.LocalMenuState
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.dagger.Injectable
import fr.hozakan.flysightble.framework.menu.rememberActionBarMenuState
import fr.hozakan.flysightble.fsdevicemodule.ui.device_config.DeviceConfigurationMenuActions
import fr.hozakan.flysightble.fsdevicemodule.ui.device_config.DeviceConfigurationScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.device_detail.DeviceDetailMenuActions
import fr.hozakan.flysightble.fsdevicemodule.ui.device_detail.DeviceDetailScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.file.DeviceFileScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.list_fs.ListFlySightDevicesScreen
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.composablecommons.theme.FlySightBLETheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dialogService: MutableDialogService

    @Inject
    lateinit var json: Gson

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlySightBLETheme {

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
                                    "FlySight BLE"
                                }

                                AppScreen.DeviceTab.DeviceDetail.route -> {
                                    "Device Detail"
                                }

                                AppScreen.ConfigTab.ConfigList.route -> {
                                    "Config files"
                                }

                                AppScreen.DeviceTab.DeviceFile.route -> {
                                    val filePath =
                                        currentBackStack.value?.arguments?.getString("filePath")
                                            ?.split(";")
                                    "File ${filePath?.lastOrNull()}"
                                }

                                else -> {
                                    "FlySight BLE"
                                }
                            }
                            TopAppBar(
                                title = { Text(title) },
                                navigationIcon = {
                                    when (currentRoute) {
                                        AppScreen.DeviceTab.DeviceList.route -> {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = "Home"
                                            )
                                        }

                                        AppScreen.DeviceTab.DeviceDetail.route -> {
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Navigate up"
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
                                                    contentDescription = "Navigate up"
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
                                                    contentDescription = "Navigate up"
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
                                                    contentDescription = "Navigate up"
                                                )
                                            }
                                        }

                                        else -> {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = "Home"
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
//                                                            json.encodeToString(it)
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

                                        else -> {}
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (currentBackStack.value?.destination?.route == AppScreen.DeviceTab.DeviceList.route ||
                                currentBackStack.value?.destination?.route == AppScreen.ConfigTab.ConfigList.route
                            ) {
                                BottomAppBar(
                                    actions = {
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.flysight_icon),
                                                    contentDescription = "Devices"
                                                )
                                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                                Text("Devices")
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
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Engineering,
                                                    contentDescription = "Config files"
                                                )
                                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                                Text("Config files")
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
//                                                conf = Json.decodeFromString<ConfigFile>(
//                                                    config
//                                                )
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