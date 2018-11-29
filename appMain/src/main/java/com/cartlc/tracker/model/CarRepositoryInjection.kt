package com.cartlc.tracker.model

import com.cartlc.tracker.model.server.DCPing
import com.cartlc.tracker.model.server.DCService
import com.cartlc.tracker.ui.act.ListEntryActivity
import com.cartlc.tracker.ui.list.ListEntryAdapter
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel
import com.cartlc.tracker.viewmodel.frag.ConfirmationViewModel
import com.cartlc.tracker.viewmodel.frag.LoginViewModel
import com.cartlc.tracker.viewmodel.frag.TitleViewModel
import com.cartlc.tracker.viewmodel.frag.MainListViewModel
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
    fun inject(obj: DCPing)
    fun inject(obj: DCService)
    fun inject(vm: MainListViewModel)
    fun inject(vm: LoginViewModel)
    fun inject(vm: ConfirmationViewModel)
    fun inject(vm: TitleViewModel)
    fun inject(vm: ButtonsViewModel)
}
