package com.cartlc.tracker.model

import com.cartlc.tracker.model.server.DCPing
import com.cartlc.tracker.model.server.DCService
import com.cartlc.tracker.ui.act.ListEntryActivity
import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.ui.list.ListEntryAdapter
import com.cartlc.tracker.viewmodel.*
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
    fun inject(act: MainActivity)
    fun inject(act: ListEntryActivity)
    fun inject(view: ListEntryAdapter)
    fun inject(obj: DCPing)
    fun inject(obj: DCService)
    fun inject(vm: LoginViewModel)
    fun inject(vm: MainListViewModel)
    fun inject(vm: ConfirmationViewModel)
    fun inject(vm: TitleViewModel)
    fun inject(vm: ButtonsViewModel)
    fun inject(vm: EntrySimpleViewModel)
}
