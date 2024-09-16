package fr.hozakan.flysightble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.dagger.Injectable
import fr.hozakan.flysightble.fsdevicemodule.ui.device_detail.DeviceDetailComposables
import fr.hozakan.flysightble.fsdevicemodule.ui.list_fs.ListFlySightDevicesScreen
import fr.hozakan.flysightble.ui.theme.FlySightBLETheme
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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
//                            val currentRoute = navController.currentDestination?.route
                            val currentRoute = currentBackStack.value?.destination?.route
                            val title = when (currentRoute) {
                                AppScreen.DeviceList.route -> {
                                    "FlySight BLE"
                                }

                                AppScreen.DeviceDetail.route -> {
                                    "Device Detail"
                                }

                                else -> {
                                    "FlySight BLE"
                                }
                            }
                            TopAppBar(
                                title = { Text(title) },
                                navigationIcon = {
                                    when (currentRoute) {
                                        AppScreen.DeviceList.route -> {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = "Home"
                                            )
                                        }
                                        AppScreen.DeviceDetail.route -> {
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
                                }
                            )
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            NavHost(
                                navController = navController,
                                startDestination = AppScreen.DeviceList.route
                            ) {
                                composable(route = AppScreen.DeviceList.route) {
                                    ListFlySightDevicesScreen(
                                        onDeviceSelected = {
                                            navController.navigate(
                                                AppScreen.DeviceDetail.buildRoute(
                                                    it.uuid
                                                )
                                            )
                                        }
                                    )
                                }
                                composable(route = AppScreen.DeviceDetail.route) { backStackEntry ->
                                    val deviceId = backStackEntry.arguments?.getString("deviceId")
                                    if (deviceId != null) {
                                        DeviceDetailComposables(deviceId = deviceId)
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