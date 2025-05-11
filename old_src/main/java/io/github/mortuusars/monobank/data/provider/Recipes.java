package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generator) {
        super(generator.getPackOutput());
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> recipeConsumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registry.Items.MONOBANK.get())
                .pattern("BIB")
                .pattern("L I")
                .pattern("BIB")
                .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('L', Registry.Items.REPLACEMENT_LOCK.get())
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(recipeConsumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registry.Items.REPLACEMENT_LOCK.get())
                .pattern(" I ")
                .pattern("ITI")
                .pattern("IBI")
                .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('T', Items.TRIPWIRE_HOOK)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(recipeConsumer);
    }
}
