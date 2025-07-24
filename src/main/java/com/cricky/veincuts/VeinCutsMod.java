package com.cricky.veincuts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinCutsMod implements ModInitializer {
    // Use FabricLoader to get the real config directory
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("veincuts.json");

    private int yLevelLimit = 0;
    private boolean veining = false;
    private final Gson gson = new Gson();

    @Override
    public void onInitialize() {
        System.out.println("[VeinCuts] onInitialize() called");
        loadConfig();
        System.out.println("[VeinCuts] yLevelLimit = " + yLevelLimit);

        PlayerBlockBreakEvents.BEFORE.register((World world,
                                                PlayerEntity player,
                                                BlockPos pos,
                                                BlockState state,
                                                BlockEntity blockEntity) -> {
            // Only on the logical server
            if (!(world instanceof ServerWorld) || !(player instanceof ServerPlayerEntity)) {
                return true;
            }
            // Only when holding a shovel
            if (!(player.getMainHandStack().getItem() instanceof ShovelItem)) {
                return true;
            }
            // Enforce Y‑level cutoff
            if (pos.getY() < yLevelLimit) {
                return true;
            }
            // Prevent recursion
            if (veining) {
                return true;
            }

            veining = true;
            Set<BlockPos> visited = new HashSet<>();
            Deque<BlockPos> toCheck = new ArrayDeque<>();
            BlockState targetState = state;
            toCheck.add(pos);

            while (!toCheck.isEmpty()) {
                BlockPos current = toCheck.removeFirst();
                if (visited.contains(current)) continue;
                visited.add(current);

                if (current.getY() < yLevelLimit) continue;
                if (!world.getBlockState(current).equals(targetState)) continue;

                world.breakBlock(current, true, player);

                for (BlockPos offset : List.of(
                    new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
                    new BlockPos(0, 1, 0), new BlockPos(0, -1, 0),
                    new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
                )) {
                    BlockPos neigh = current.add(offset);
                    if (!visited.contains(neigh)
                       && world.getBlockState(neigh).equals(targetState)) {
                        toCheck.add(neigh);
                    }
                }
            }

            veining = false;
            // Return true to let Minecraft break the original block normally
            return true;
        });
    }

    private void loadConfig() {
        try {
            System.out.println("[VeinCuts] Loading config at " + CONFIG_PATH.toAbsolutePath());
            Path parent = CONFIG_PATH.getParent();
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(CONFIG_PATH)) {
                System.out.println("[VeinCuts] Config not found, creating default");
                JsonObject defaults = new JsonObject();
                defaults.addProperty("yLevelLimit", 0);
                try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                    new GsonBuilder().setPrettyPrinting().create().toJson(defaults, writer);
                }
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                yLevelLimit = json.has("yLevelLimit")
                    ? json.get("yLevelLimit").getAsInt()
                    : 0;
                System.out.println("[VeinCuts] Loaded yLevelLimit=" + yLevelLimit);
            }
        } catch (IOException e) {
            System.err.println("[VeinCuts] Failed to load config:");
            e.printStackTrace();
        }
    }
}
