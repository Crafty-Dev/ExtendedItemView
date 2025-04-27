package de.crafty.eiv.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EivCommand {


    private static int reloadRecipes(CommandContext<CommandSourceStack> context) {
        ServerRecipeManager.INSTANCE.reload(context.getSource().getServer().getRecipeManager());
        context.getSource().sendSuccess(() -> Component.translatable("commands.eiv.reloaded"), true);
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                Commands.literal("eiv")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                        .then(Commands.literal("reloadRecipes").executes(EivCommand::reloadRecipes))
        );
    }

}
