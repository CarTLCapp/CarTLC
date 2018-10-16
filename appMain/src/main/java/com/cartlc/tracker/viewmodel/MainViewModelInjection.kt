package com.cartlc.tracker.viewmodel

import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.server.DCPing
import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.ui.app.TBApplication
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MainViewModelModule(
        private val repo: CarRepository
) {
    @Provides
    @Singleton
    fun providesMainViewModel(): MainViewModel = MainViewModel(repo)

    @Provides
    @Singleton
    fun providesCarRepository(): CarRepository = repo
}

@Singleton
@Component(modules = [MainViewModelModule::class])
interface MainViewModelComponent {
    fun inject(app: TBApplication)
    fun inject(obj: MainActivity)
    fun inject(obj: DCPing)
    fun inject(vm: LoginViewModel)
    fun inject(vm: MainListViewModel)
    fun inject(vm: ConfirmationViewModel)

}
