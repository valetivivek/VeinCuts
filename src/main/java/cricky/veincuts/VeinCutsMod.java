package cricky.veincuts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinCutsMod implements ModInitializer {
    private static final String CONFIG_FILE = "config/veincuts.json";
    private int yLevelLimit = 0;
    private boolean veining = false;
    private final Gson gson = new Gson();

    @Override
    public void onInitialize() {
        loadConfig();

        // Use the BEFORE event, which returns boolean
        PlayerBlockBreakEvents.BEFORE.register((World world,
                                                PlayerEntity player,
                                                BlockPos pos,
                                                BlockState state,
                                                BlockEntity blockEntity) -> {
            // Only on the server side
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
            // Return true to let the vanilla break continue
            return true;
        });
    }

    private void loadConfig() {
        try {
            Path cfg = Path.of(CONFIG_FILE);
            if (!Files.exists(cfg.getParent())) {
                Files.createDirectories(cfg.getParent());
            }
            if (!Files.exists(cfg)) {
                JsonObject defaults = new JsonObject();
                defaults.addProperty("yLevelLimit", 0);
                try (FileWriter writer = new FileWriter(cfg.toFile())) {
                    gson.toJson(defaults, writer);
                }
            }
            try (Reader reader = Files.newBufferedReader(cfg)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                yLevelLimit = json.has("yLevelLimit")
                    ? json.get("yLevelLimit").getAsInt()
                    : 0;
            }
        } catch (IOException e) {
            System.err.println("[VeinCuts] Failed to load config: " + e);
        }
    }
}
