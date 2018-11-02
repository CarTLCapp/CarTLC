package com.cartlc.tracker.viewmodel

import com.cartlc.tracker.ui.act.MainActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MainViewModelModule(
        private val mainViewModel: MainViewModel
) {

    @Provides
    @Singleton
    fun providesMainViewModel(): MainViewModel = mainViewModel
}

@Singleton
@Component(modules = [MainViewModelModule::class])
interface MainViewModelComponent {
    fun inject(act: MainActivity)
}
