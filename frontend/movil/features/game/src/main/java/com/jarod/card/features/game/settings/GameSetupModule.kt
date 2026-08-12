package com.jarod.card.features.game.settings

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameSetupModule {

    @Binds
    @Singleton
    abstract fun bindGameSetupStore(impl: SharedPrefsGameSetupStore): GameSetupStore
}
