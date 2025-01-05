package fr.hozakan.flysightcompanion.framework.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun rememberActionBarMenuState(): ActionBarMenuState = remember {
    ActionBarMenuState()
}

@Stable
class ActionBarMenuState(
    private val scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main)
) {

    private val _menuActionFlow = MutableSharedFlow<MenuAction>(replay = 0)
    val menuActionFlow: SharedFlow<MenuAction>
        get() = _menuActionFlow.asSharedFlow()

    fun emitAction(menuAction: MenuAction) {
        scope.launch {
            _menuActionFlow.emit(menuAction)
        }
    }

}
