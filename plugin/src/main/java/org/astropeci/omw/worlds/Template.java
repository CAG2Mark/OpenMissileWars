package org.astropeci.omw.worlds;

import lombok.RequiredArgsConstructor;
import org.astropeci.omw.settings.ArenaSettings;
import org.astropeci.omw.settings.GlobalSettings;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class Template {

    private static final String TEMPLATE_WORLD_NAME = "template";
    private static final ChunkGenerator VOID_GENERATOR = new VoidChunkGenerator();

    private final GlobalTeamManager globalTeamManager;
    private final WorldManager worldManager;
    private final Hub hub;
    private final Plugin plugin;

    public void sendPlayer(Player player) {
        worldManager.send(player, getWorld());
    }

    public Arena createArena(ArenaSettings settings) {
        String worldName = "omw-arena-" + UUID.randomUUID();
        Bukkit.getLogger().info("Creating arena " + worldName);

        World templateWorld = getWorld();
        Path source = templateWorld.getWorldFolder().toPath().resolve("region");

        Path worldContainer = Bukkit.getWorldContainer().toPath();
        Path dest = worldContainer.resolve(worldName).resolve("region");

        Bukkit.getLogger().info("Copying template files to " + worldName);

        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                    Path destFile = dest.resolve(source.relativize(sourceFile));
                    Files.createDirectories(destFile.getParent());
                    Files.copy(sourceFile, destFile);
                    return super.visitFile(sourceFile, attrs);
                }
            });
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to clone region files from " + source + " to " + dest, e);
        }

        WorldCreator creator = new WorldCreator(worldName)
                .generator(VOID_GENERATOR)
                .copy(templateWorld);

        Bukkit.getLogger().info("Loading " + worldName);

        World world = creator.createWorld();
        worldManager.configureWorld(world);

        Bukkit.getLogger().info("Completed creating arena " + worldName);

        return new Arena(settings, world, globalTeamManager, worldManager, hub, plugin);
    }

    private World getWorld() {
        World world = Bukkit.getWorld(TEMPLATE_WORLD_NAME);

        if (world == null) {
            Bukkit.getLogger().info("No template world found, generating one");

            WorldCreator creator = new WorldCreator(TEMPLATE_WORLD_NAME)
                    .generator(VOID_GENERATOR);

            World newWorld = creator.createWorld();
            worldManager.configureWorld(newWorld);

            return newWorld;
        } else {
            return world;
        }
    }

    public void createWorldIfMissing() {
        getWorld();
    }
}
