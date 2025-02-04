package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.service.GardenService;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandTabCompleter implements TabCompleter {

    private final GardenService gs;

    public CommandTabCompleter(GardenService gs) {
        this.gs = gs;
    }

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

        if(command.getName().equalsIgnoreCase("garden")) {
            switch(args.length) {
                case 1 -> {
                    return List.of("create", "list", "list_gardens_in_detail", "destroy",
                            "destroy_all",
                            "generate_flowers", "save", "load", "draw_flowers", "clear_flowers",
                            "remove_flowers", "spawn_bee_nest");
                }

                case 2 -> {
                    if(args[0].equalsIgnoreCase("destroy")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("generate_flowers")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("draw_flowers")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("clear_flowers")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("remove_flowers")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("spawn_bee_nest")) {
                        return this.gs.getGardenNames();
                    }

                    if(args[0].equalsIgnoreCase("destroy_bee_nest")) {
                        return this.gs.getGardenNames();
                    }
                }
            }
        }

        return List.of();
    }
}
