package com.badoo.reaktive.samplemppmodule

class KittenStoreBuilderImpl : KittenStoreBuilder {

    override fun build(): KittenStore = KittenStoreImpl(loader = KittenLoaderImpl(), parser = KittenParser())
}