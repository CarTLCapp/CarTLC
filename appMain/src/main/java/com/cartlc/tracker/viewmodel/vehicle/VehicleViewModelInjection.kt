package com.cartlc.tracker.viewmodel.vehicle

import com.cartlc.tracker.ui.act.VehicleActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VehicleViewModelModule(
        private val vehicleViewModel: VehicleViewModel
) {

    @Provides
    @Singleton
    fun providesVehicleViewModel(): VehicleViewModel = vehicleViewModel
}

@Singleton
@Component(modules = [VehicleViewModelModule::class])
interface VehicleViewModelComponent {
    fun inject(act: VehicleActivity)
}
