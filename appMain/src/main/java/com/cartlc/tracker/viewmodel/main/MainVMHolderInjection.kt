package com.cartlc.tracker.viewmodel.main

import com.cartlc.tracker.ui.act.MainActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MainViewModelModule(
        private val mainVMHolder: MainVMHolder
) {

    @Provides
    @Singleton
    fun providesMainViewModel(): MainVMHolder = mainVMHolder
}

@Singleton
@Component(modules = [MainViewModelModule::class])
interface MainViewModelComponent {
    fun inject(act: MainActivity)
}
