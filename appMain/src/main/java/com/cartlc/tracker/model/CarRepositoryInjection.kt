package com.cartlc.tracker.model

import com.cartlc.tracker.model.server.DCService
import com.cartlc.tracker.ui.act.ListEntryActivity
import com.cartlc.tracker.ui.frag.*
import com.cartlc.tracker.ui.list.ListEntryAdapter
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CarRepositoryModule(
        private val repo: CarRepository
) {

    @Provides
    @Singleton
    fun providesCarRepository(): CarRepository = repo
}

@Singleton
@Component(modules = [CarRepositoryModule::class])
interface CarRepositoryComponent {
    fun inject(act: ListEntryActivity)
    fun inject(view: ListEntryAdapter)
    fun inject(obj: DCService)
    fun inject(vm: MainListFragment)
    fun inject(vm: LoginFragment)
    fun inject(vm: ConfirmationFragment)
    fun inject(vm: TitleFragment)
    fun inject(vm: ButtonsFragment)
}
