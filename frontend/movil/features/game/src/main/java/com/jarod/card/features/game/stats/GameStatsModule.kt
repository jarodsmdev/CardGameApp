package com.jarod.card.features.game.stats

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameStatsModule {

    @Binds
    @Singleton
    abstract fun bindGameStatsStore(impl: SharedPrefsGameStatsStore): GameStatsStore
}
