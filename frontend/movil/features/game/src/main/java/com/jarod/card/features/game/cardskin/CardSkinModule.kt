package com.jarod.card.features.game.cardskin

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CardSkinModule {

    @Binds
    @Singleton
    abstract fun bindCardSkinStore(impl: SharedPrefsCardSkinStore): CardSkinStore
}
