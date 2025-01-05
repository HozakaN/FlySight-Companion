package fr.hozakan.flysightcompanion.tools.di

import fr.hozakan.flysightcompanion.tools.di.modules.ActivityBuilder
import fr.hozakan.flysightcompanion.tools.di.modules.BaseModule
import fr.hozakan.flysightcompanion.tools.di.modules.ServiceModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import fr.hozakan.flysightcompanion.BaseApplication
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Singleton

@InternalCoroutinesApi
@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ActivityBuilder::class,
        BaseModule::class,
        ServiceModule::class
    ]
)
interface BaseComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: BaseApplication): Builder

        fun build(): BaseComponent
    }

    fun inject(application: BaseApplication)
}
