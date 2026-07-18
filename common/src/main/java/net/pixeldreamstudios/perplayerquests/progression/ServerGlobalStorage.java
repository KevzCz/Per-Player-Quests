package net.pixeldreamstudios.perplayerquests.progression;

import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerGlobalStorage {
    private ServerGlobalStorage() {}

    private static final LevelResource ROOT = LevelResource.ROOT;
    private static final String FILE = "perplayerquests_server_global.dat";

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(ROOT).resolve(FILE);
    }

    public static void load(MinecraftServer server) {
        Path path = file(server);
        if (!Files.exists(path)) {
            ServerGlobalProgress.clearAll();
            return;
        }
        try {
            ServerGlobalProgress.load(NbtIo.read(path));
        } catch (IOException e) {
            ServerGlobalProgress.clearAll();
        }
    }

    public static void save(MinecraftServer server) {
        try {
            NbtIo.write(ServerGlobalProgress.save(), file(server));
        } catch (IOException ignored) {
        }
    }
}
