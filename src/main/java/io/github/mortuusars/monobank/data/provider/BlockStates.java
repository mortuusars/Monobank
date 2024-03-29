package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlock;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider {
    public BlockStates(DataGenerator dataGenerator, ExistingFileHelper exFileHelper) {
        super(dataGenerator.getPackOutput(), Monobank.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        ModelFile model = models().getExistingFile(modLoc("block/monobank"));

        getVariantBuilder(Registry.Blocks.MONOBANK.get())
                .forAllStates(blockState -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(((int) blockState.getValue(MonobankBlock.FACING).toYRot()))
                        .build());
    }
}
