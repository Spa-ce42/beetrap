package edu.rochester.beetrap.controller;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("game")) {
            switch(args.length) {
                case 1 -> {
                    return List.of("new", "destroy");
                }
            }
        }

        return List.of();
    }
}
