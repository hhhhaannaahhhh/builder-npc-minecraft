package NPC;

import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Citizens trait behaviour for the npc
 * has the npc search for the closest 5x5 bit of flat land
 *
 * @author haf31
 */

public class Builder extends Trait {
    private int radius = 250; // search radius
    private int tolerance= 0; // tolerance is so the land can be perfectly flat
    private int step = 1;

    private FlatLand flatLandSearch;
    private Location buildSpot;
    private String schematicFile = "building_1.txt";

    public Builder(){
        super("builder"); // trait name for citizens
    }
    public void setSchematic(String name){
        String n = name.trim();
        if (!n.toLowerCase().endsWith(".txt")) n = n + ".txt";
        this.schematicFile = n;
    }

    // start searching for the closest bit of flat land
    public void startSearch(Plugin plugin, Location spawn, Consumer<Location> foundLand){
        if (flatLandSearch != null) flatLandSearch.stop();
        flatLandSearch = null;
        buildSpot = null;

        // create flat land search
        LoadedSchematic schematic = loadSchematic(plugin);
        int width = schematic.width();
        int length = schematic.length();

        // start the search
        flatLandSearch = new FlatLand(plugin, npc, spawn, width, length, radius, tolerance, step);
        flatLandSearch.startSearch(foundSpot ->
                {
                    buildSpot = foundSpot.clone();
                    foundLand.accept(foundSpot);
                });

    }

    public void build(Plugin plugin){
        if (buildSpot == null) {
            return;
        }
        // building put where flat land is
        World world = buildSpot.getWorld();
        // load and read schematic file
        File file = new File(plugin.getDataFolder(), "schematics/" + schematicFile);
       final String text;
       try {
           text = Files.readString(file.toPath());
       } catch (Exception error){
           return;
       }
       // where the building will be built
        //schematic is written (dx, dy, dz) material
       int startX = buildSpot.getBlockX();
       int startY = buildSpot.getBlockY();
       int startZ = buildSpot.getBlockZ();
        Location safeSpot = buildSpot.clone().add(-3, 1, -3);
        npc.getNavigator().cancelNavigation();

        if (npc.isSpawned() && npc.getEntity() != null) {
            npc.getEntity().teleport(safeSpot);
        }


//split into lines then sections
       for (String line1 : text.split("\\R")){
           String line = line1.trim();
           if (line.isEmpty() || line.startsWith("#")) continue;
           String[] sections = line.split("\\s+", 5);
           if (sections.length < 4) continue;
           int dx = Integer.parseInt(sections[0]);
           int dy = Integer.parseInt(sections[1]);
           int dz = Integer.parseInt(sections[2]);
           Material material = Material.matchMaterial(sections[3]);
           int x = startX + dx;
           int y = startY + dy;
           int z = startZ + dz;
            if (sections.length == 5) {
                String blockDataString = "minecraft:" + material.name().toLowerCase() + "[" + sections[4] + "]";
                BlockData blockData = Bukkit.createBlockData(blockDataString);
                world.getBlockAt(x, y, z).setBlockData(blockData, false);
            } else {
                world.getBlockAt(x, y, z).setType(material, false);
            }
       }
    }
    private LoadedSchematic loadSchematic(Plugin plugin){
        File file = new File(plugin.getDataFolder(), "schematics/" + schematicFile);
        int minDx = Integer.MAX_VALUE, maxDx = Integer.MIN_VALUE;
        int minDz = Integer.MAX_VALUE, maxDz = Integer.MIN_VALUE;
        try (BufferedReader bufferedReader =  new BufferedReader(new FileReader(file))){
            String raw;
            while ((raw = bufferedReader.readLine()) != null) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] sections = line.split("\\s+");
                if (sections.length < 4) continue;
                int dx, dz;
                try {
                    dx = Integer.parseInt(sections[0]);
                    dz = Integer.parseInt(sections[2]);
                } catch (NumberFormatException ignored){
                    continue;
                }
                minDx = Math.min(minDx, dx);
                maxDx = Math.max(maxDx, dx);
                minDz = Math.min(minDz, dz);
                maxDz = Math.max(maxDz, dz);
            }
        } catch (Exception ignored){
            return null;
        }
        if (minDx == Integer.MAX_VALUE || minDz == Integer.MAX_VALUE) return null;
        return new LoadedSchematic(minDx, maxDx, minDz, maxDz);
    }

}
