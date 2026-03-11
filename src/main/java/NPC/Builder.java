package NPC;

import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;


import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Citizens trait behaviour for the npc
 * has the npc search for the closest 5x5 bit of flat land
 *
 * @author haf31
 */

public class Builder extends Trait {
    private int width = 5; //width of land in FlatLand class
    private int length = 5; // length of land
    private int radius = 50; // search radius
    private int tolerance= 0; // tolerance is so the land can be perfectly flat
    private int step = 1;

    private FlatLand flatLandSearch;
    private Location buildSpot;

    public Builder(){
        super("builder"); // trait name for citizens
    }
    // start searching for the closest bit of flat land
    public void startSearch(org.bukkit.plugin.Plugin plugin, Location spawn, Consumer<Location> foundLand){
        // create flat land search
        flatLandSearch = new FlatLand( plugin, npc, spawn, width, length, radius, tolerance, step);
        // start the search
        flatLandSearch.startSearch(foundSpot ->
                {
                    buildSpot = foundSpot.clone();
                    foundLand.accept(foundSpot);
                });

    }

    public void build(Plugin plugin){
        // building put where flat land is
        World world = buildSpot.getWorld();
        // load and read schematic file
        File file = new File(plugin.getDataFolder(), "schematics/building_1.txt");
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
//split into lines then sections
       for (String line1 : text.split("\\R")){
           String line = line1.trim();
           if (line.isEmpty() || line.startsWith("#")) continue;
           String[] sections = line.split("\\s+");
           if (sections.length != 4) continue;
           int dx = Integer.parseInt(sections[0]);
           int dy = Integer.parseInt(sections[1]);
           int dz = Integer.parseInt(sections[2]);
           Material material = Material.matchMaterial(sections[3]);
           //place blocks on the land
           world.getBlockAt(startX + dx, startY + dy, startZ + dz).setType(material, false);
       }
    }

}
