package com.the9grounds.aeadditions.api

import appeng.api.storage.ICellWorkbenchItem
import appeng.api.storage.IStorageChannel
import appeng.api.storage.data.IAEStack
import net.minecraft.item.ItemStack

interface IAEAdditionsStorageCell<T : IAEStack<T>?> : ICellWorkbenchItem {
    abstract fun getBytes(itemStack: ItemStack): Int

    abstract fun getBytesPerType(itemStack: ItemStack): Int

    abstract fun getTotalTypes(itemStack: ItemStack): Int

    abstract fun isBlackListed(itemStack: ItemStack, var2: T): Boolean

    abstract fun storableInStorageCell(): Boolean

    abstract fun isStorageCell(itemStack: ItemStack): Boolean

    abstract fun getIdleDrain(): Double

    abstract fun getChannel(): IStorageChannel<T>
}