package net.pixeldreamstudios.perplayerquests.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UniqueClaimStorage {
    private UniqueClaimStorage() {}

    private static final LevelResource ROOT = LevelResource.ROOT;
    private static final String FILE = "perplayerquests_unique_claims.dat";

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(ROOT).resolve(FILE);
    }

    public static void load(MinecraftServer server) {
        Path path = file(server);
        if (!Files.exists(path)) {
            UniqueClaims.clearAll();
            return;
        }
        try {
            CompoundTag root = NbtIo.read(path);
            UniqueClaims.load(root);
        } catch (IOException e) {
            UniqueClaims.clearAll();
        }
    }

    public static void save(MinecraftServer server) {
        Path path = file(server);
        try {
            NbtIo.write(UniqueClaims.save(), path);
        } catch (IOException ignored) {
        }
    }
}
