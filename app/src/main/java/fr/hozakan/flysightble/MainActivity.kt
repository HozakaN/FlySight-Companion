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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
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
import fr.hozakan.flysightble.configfilesmodule.ui.config_detail.ConfigDetailScreen
import fr.hozakan.flysightble.configfilesmodule.ui.list_files.ListConfigFilesScreen
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.dagger.Injectable
import fr.hozakan.flysightble.fsdevicemodule.ui.device_config.DeviceConfigurationScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.device_detail.DeviceDetailScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.file.DeviceFileScreen
import fr.hozakan.flysightble.fsdevicemodule.ui.list_fs.ListFlySightDevicesScreen
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.ui.theme.FlySightBLETheme
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var json: Gson

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlySightBLETheme {
                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory
                ) {
                    val navController = rememberNavController()
                    val currentBackStack = navController.currentBackStackEntryAsState()

                    Scaffold(
                        topBar = {
                            val currentRoute = currentBackStack.value?.destination?.route
                            val title = when (currentRoute) {
                                AppScreen.Device.DeviceList.route -> {
                                    "FlySight BLE"
                                }

                                AppScreen.Device.DeviceDetail.route -> {
                                    "Device Detail"
                                }

                                AppScreen.ConfigFiles.ConfigFileList.route -> {
                                    "Config files"
                                }

                                AppScreen.Device.DeviceFile.route -> {
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
                                        AppScreen.Device.DeviceList.route -> {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = "Home"
                                            )
                                        }

                                        AppScreen.Device.DeviceDetail.route -> {
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

                                        AppScreen.Device.DeviceFile.route -> {
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

                                        AppScreen.ConfigFiles.ConfigFileDetail.route -> {
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

                                        AppScreen.Device.DeviceConfig.route -> {
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
                                    if (currentRoute == AppScreen.ConfigFiles.ConfigFileList.route) {
                                        IconButton(
                                            onClick = {
                                                navController.navigate(
                                                    AppScreen.ConfigFiles.ConfigFileDetail.buildRoute(
                                                        ""
                                                    )
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                                contentDescription = "New config file"
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (currentBackStack.value?.destination?.route == AppScreen.Device.DeviceList.route ||
                                currentBackStack.value?.destination?.route == AppScreen.ConfigFiles.ConfigFileList.route
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
                                                        navController.navigate(AppScreen.Device.route)
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
                                                        navController.navigate(AppScreen.ConfigFiles.route)
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
                                startDestination = AppScreen.Device.route
                            ) {
                                navigation(
                                    route = AppScreen.Device.route,
                                    startDestination = AppScreen.Device.DeviceList.route
                                ) {
                                    composable(route = AppScreen.Device.DeviceList.route) {
                                        ListFlySightDevicesScreen(
                                            onDeviceSelected = {
                                                navController.navigate(
                                                    AppScreen.Device.DeviceDetail.buildRoute(
                                                        it.uuid
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    composable(route = AppScreen.Device.DeviceDetail.route) { backStackEntry ->
                                        val deviceId =
                                            backStackEntry.arguments?.getString("deviceId")
                                        if (deviceId != null) {
                                            DeviceDetailScreen(
                                                deviceId = deviceId,
                                                onFileClicked = {
                                                    navController.navigate(
                                                        AppScreen.Device.DeviceFile.buildRoute(
                                                            deviceId,
                                                            it
                                                        )
                                                    )
                                                },
                                                onShowDeviceConfigClicked = {
                                                    navController.navigate(
                                                        AppScreen.Device.DeviceConfig.buildRoute(
//                                                            json.encodeToString(it)
                                                            json.toJson(it)
                                                        )
                                                    )
                                                },
                                                onNavigateUp = {
                                                    navController.popBackStack()
                                                }
                                            )
                                        }
                                    }
                                    composable(route = AppScreen.Device.DeviceFile.route) { backStackEntry ->
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
                                    composable(route = AppScreen.Device.DeviceConfig.route) { backStackEntry ->
                                        val config =
                                            backStackEntry.arguments?.getString("config")
                                        if (config != null) {
                                            DeviceConfigurationScreen(
//                                                conf = Json.decodeFromString<ConfigFile>(
//                                                    config
//                                                )
                                                conf = json.fromJson(config, ConfigFile::class.java),
                                            )
                                        }
                                    }
                                }
                                navigation(
                                    route = AppScreen.ConfigFiles.route,
                                    startDestination = AppScreen.ConfigFiles.ConfigFileList.route
                                ) {
                                    composable(route = AppScreen.ConfigFiles.ConfigFileList.route) {
                                        ListConfigFilesScreen(
                                            onConfigSelected = {
                                                navController.navigate(
                                                    AppScreen.ConfigFiles.ConfigFileDetail.buildRoute(
                                                        it.name
                                                    )
                                                )
                                            },
                                            onCreateConfigFile = {
                                                navController.navigate(
                                                    AppScreen.ConfigFiles.ConfigFileDetail.buildRoute(
                                                        ""
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    composable(route = AppScreen.ConfigFiles.ConfigFileDetail.route) { backStackEntry ->
                                        val configFileName =
                                            backStackEntry.arguments?.getString("configFileName")
                                                ?: return@composable
                                        ConfigDetailScreen(
                                            configFileName = configFileName,
                                            onNavigateUp = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }
                            }

                            BackHandler {
                                val currentRoute = currentBackStack.value?.destination?.route
                                if (currentRoute == AppScreen.Device.DeviceList.route || currentRoute == AppScreen.ConfigFiles.ConfigFileList.route) {
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