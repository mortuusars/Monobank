package io.github.mortuusars.monobank.content.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.registry.ModBlockEntityTypes;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

public class MonobankBlockEntity extends BlockEntity implements MenuProvider {
    private Component name;

    private @Nullable Item storedItem = null;
    private @Nullable CompoundTag storedItemTag = null;
    private int storedItemCount = 0;
    private boolean locked = false;

    public MonobankBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.MONOBANK.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        if (storedItem != null && storedItem != Items.AIR && storedItemCount > 0) {
            tag.putString("Item", ForgeRegistries.ITEMS.getKey(storedItem).toString());
            tag.putInt("Count", storedItemCount);

            if (storedItemTag != null)
                tag.put("ItemTag", storedItemTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag == null)
            tag = new CompoundTag();
        super.load(tag);
        if (tag.contains("CustomName", 8))
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"));

        try {
            if (tag.contains("Item")) {
                storedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("Item")));

                if (tag.contains("Count"))
                    storedItemCount = tag.getInt("Count");
                if (tag.contains("ItemTag"))
                    storedItemTag = (CompoundTag) tag.get("ItemTag");
            }
        }
        catch (Exception e) {
            LogUtils.getLogger().error("Failed to load MonobankBlockEntity data from tag: {}", e);
        }
    }

    public int getCapacity() {
        // TODO: config for capacity
        return Integer.MAX_VALUE;
    }

    public boolean isEmpty() {
        return storedItem == null || storedItem == Items.AIR || storedItemCount <= 0;
    }

    public boolean canDeposit(ItemStack stackToDeposit) {
        if (isEmpty())
            return true;

        if (storedItemCount == Integer.MAX_VALUE)
            return false;

        ItemStack existing = new ItemStack(storedItem, 1, storedItemTag);
        return ItemStack.isSameItemSameTags(existing, stackToDeposit);
    }


    public boolean tryDepositItemStack(ItemStack stackToAdd) {

        if (!canDeposit(stackToAdd))
            return false;

        if (isEmpty()) {
            storedItem = stackToAdd.getItem();
            storedItemTag = stackToAdd.getTag();
            storedItemCount = stackToAdd.split(getCapacity()).getCount();
        }
        else {
            storedItemCount += stackToAdd.split(getCapacity() - storedItemCount).getCount();
        }

        inventoryChanged();
        return true;
    }

    /**
     * Creates new ItemStack from stored item with count up to maxStackSize or stored amount (whichever is smaller). If bank is empty - ItemStack.EMPTY is returned.
     */
    public ItemStack withdrawStack() {
        return withdraw(Item.MAX_STACK_SIZE);
    }

    /**
     * Creates new ItemStack from stored item with specified count but not larger than maxStackSize or stored amount (whichever is smaller). If bank is empty - ItemStack.EMPTY is returned.
     */
    public ItemStack withdraw(int count) {
        if (count <= 0)
            throw new IllegalArgumentException("'count' cannot be less than 1. Count: '" + count + "'.");

        if (isEmpty())
            return ItemStack.EMPTY;

        ItemStack itemStack = new ItemStack(getStoredItem());
        count = Math.min(count, itemStack.getMaxStackSize());
        itemStack.setCount(Math.min(getStoredItemCount(), count));
        itemStack.setTag(storedItemTag);

        this.storedItemCount -= itemStack.getCount();
        if (storedItemCount <= 0)
            clearStoredItem();

        inventoryChanged();

        return itemStack;
    }

    public void clearStoredItem() {
        this.storedItem = null;
        this.storedItemTag = null;
        this.storedItemCount = 0;
    }


    public Item getStoredItem() {
        return this.storedItem;
    }

    public CompoundTag getStoredItemTag() {
        return this.storedItemTag;
    }

    public int getStoredItemCount() {
        return this.storedItemCount;
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
        return new MonobankMenu(containerID, playerInventory, this, createContainerData());
    }

    private ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? MonobankBlockEntity.this.storedItemCount : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0)
                    MonobankBlockEntity.this.storedItemCount = value;
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    // Name
    public Component getDisplayName() {
        return this.getName();
    }
    @javax.annotation.Nullable
    public Component getCustomName() {
        return this.name;
    }
    public void setCustomName(Component pName) {
        this.name = pName;
    }
    public Component getName() {
        return this.name != null ? this.name : this.getDefaultName();
    }
    protected Component getDefaultName() {
        return TextUtil.translate("monobank");
    }

    // Sync
    @Override
    @javax.annotation.Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        boolean a = true;
        load(pkt.getTag());
    }

    protected void inventoryChanged() {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
