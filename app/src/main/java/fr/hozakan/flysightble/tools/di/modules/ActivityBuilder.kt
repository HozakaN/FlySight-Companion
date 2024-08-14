package fr.hozakan.flysightble.tools.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.hozakan.flysightble.MainActivity
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@Module
internal abstract class ActivityBuilder {

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

}
