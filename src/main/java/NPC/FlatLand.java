package NPC;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/**
 * Flat land finer for the trait to use
 * creates a list of sutible spots, checks the spots to see if they have the correct size,
 * lets the npc move to that location
 * @author haf31
 */
public class FlatLand {
    private NPC npc;
    private Location spawn; //search start
    private Plugin npcPlugin; // needed for tasks
    private BukkitTask task; // repeating task for search

    private int width; //width of the flat land
    private int length; //length of the flat land
    private int radius; //search radius
    private int tolerance; // how flat the ground is
    private int step; // how many blocks it searches to find the flat land

    // queue of sutible locations to test
    private ArrayDeque<Location> flatSpot = new ArrayDeque<>();
    private Location target = null;
    private boolean search = false;
    private int targetTicks = 0;

    /**
     * Creates a flat land search object
     * @param npcPlugin the plugin instance, used to run scheduled Buk
     * @param npc the Citizens NPC that will move to the selected flat area
     * @param spawn the center location where move to the selected flat area
     * @param width the width of the building
     * @param length the length of the building
     * @param radius the maximum distance from the center location
     * @param tolerance the height difference across the area 0 means flat
     * @param step number of blocks to move between search checks
     */


    public FlatLand(Plugin npcPlugin,
            NPC npc,
            Location spawn,
            int width,
            int length,
            int radius,
            int tolerance,
            int step){
        this.npcPlugin = npcPlugin;
        this.npc = npc;
        this.spawn = spawn;
        this.width = width;
        this.length = length;
        this.radius = radius;
        this.tolerance = tolerance;
        this.step = Math.max(1, step); // cannot have 0



    }
    /**
     *Builds a queue of possible flat locations around the center
     */
    //nearest first search using a grid scan
    private void buildSpot() {
        World world = spawn.getWorld();
        if (world == null) return;

        int centerX = spawn.getBlockX();
        int centerZ = spawn.getBlockZ();

        // r is distance from spawn
        for (int r = 0; r <= radius; r += step) {
            // look through closest first
            if (r == 0) {
                int y = actualGround(world, centerX, centerZ);
                if (y > world.getMinHeight()) {
                    //y+1 so npc isnt in the ground
                    flatSpot.add(new Location(world, centerX + 0.5, y + 1, centerZ + 0.5));
                }
                continue;
            }
            // search top to bottom first
            for (int x = centerX - r; x <= centerX + r; x += step) {
                int zTop = centerZ - r;
                int zBottom = centerZ + r;

                int yTop = actualGround(world, x, zTop);
                if (yTop > world.getMinHeight()) {
                    flatSpot.add(new Location(world, x + 0.5, yTop + 1, zTop + 0.5));
                }

                int yBottom = actualGround(world, x, zBottom);
                if (yBottom > world.getMinHeight()) {
                    flatSpot.add(new Location(world, x + 0.5, yBottom + 1, zBottom + 0.5));
                }
            }
            // search left to right
            for (int z = centerZ - r + step; z <= centerZ + r - step; z += step) {
                int xLeft = centerX - r;
                int xRight = centerX + r;

                int yLeft = actualGround(world, xLeft, z);
                if (yLeft > world.getMinHeight()) {
                    flatSpot.add(new Location(world, xLeft + 0.5, yLeft + 1, z + 0.5));
                }

                int yRight = actualGround(world, xRight, z);
                if (yRight > world.getMinHeight()) {
                    flatSpot.add(new Location(world, xRight + 0.5, yRight + 1, z + 0.5));
                }
            }
        }
    }

    /**
     * Start the flat land search
     * @param landFound callback that receives the final build location
     */
    // starts search loop
    public void startSearch(Consumer<Location> landFound){
        search = true;
        flatSpot.clear();
        target = null;
        // generate spots
        buildSpot();
       // runs every 10 ticks
        task = Bukkit.getScheduler().runTaskTimer(npcPlugin, ()->{
            Location npcLocation = npc.getEntity().getLocation();
            // if no target pick closest spot before walking there
            if (target == null) {
                while (!flatSpot.isEmpty()) {
                    Location candidate = flatSpot.removeFirst();
                    // valid spot to avoid npc confusion
                    if (flatArea(candidate)) {
                        target = candidate;
                        targetTicks= 0;
                        npc.getNavigator().setTarget(target);
                        return;
                    }
                }
                // no more spots
                search = false;
                task.cancel();
                return;
            }

            targetTicks++;

            if (npcLocation.distance(target) > 1.0) {
                if (targetTicks > 120) { // about 60 seconds because task runs every 10 ticks
                    npc.getNavigator().cancelNavigation();
                    target = null;
                    targetTicks = 0;
                }
                return;
            }
            if (flatArea(target)) {
                npc.getNavigator().cancelNavigation();
                search = false;

                Location buildOrigin = getBuildSpot(target);
                landFound.accept(buildOrigin);

                task.cancel();
                task = null;
                return;
            }
            target = null;

        }, 1L, 10L);
    }

    /**
     * finds the highest solid block at a x and z coordinate
     * @param world the world being searched
     * @param x x coordinate to check
     * @param z z coordinate to check
     * @return y coordinate of the highest solid block or the world min height
     */
    //find y of top solid block
    private int actualGround(World world, int x, int z){
        int y = world.getHighestBlockYAt(x, z);
        int minY = world.getMinHeight();

       while (y > minY){
           Block block = world.getBlockAt(x, y, z);
           Material type = block.getType();
           //solid is the ground
           if (type.isSolid()){
               return y;
           }
           y--;
       }
        return minY;
        }

    /**
     * heks if a location has enough flat land for the building
     * @param location center location of the area
     * @return true if the area is flat and suitable for building
     */
// makes sure the area is perfectly flat
    private boolean flatArea(Location location) {
        World world = location.getWorld();
        // center of flat bit
        int startX = location.getBlockX();
        int startZ = location.getBlockZ();
        // find the corner to build
        int halfWidth = width / 2;
        int halfLength = length / 2;
        int minX = startX - halfWidth;
        int maxX = startX + halfWidth;
        int minZ = startZ - halfLength;
        int maxZ = startZ + halfLength;

        // only if width and length are even
        if (width % 2 == 0){
            maxX = minX + width - 1;
        }
        if (length % 2 == 0){
            maxZ = minZ + length - 1;
        }
        // center of the flat area is what it should be for surrounding
        int startY = actualGround(world, startX, startZ);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int y = actualGround(world,x, z);

                // cannot have these
                Material ground = world.getBlockAt(x, y, z).getType();
                if (ground == Material.WATER || ground == Material.LAVA) {
                    return false;
                }
                // cant be above tolerance (0)
                if (Math.abs(y - startY) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts the accepted center loation into the corner where the schemiatic starts
     * @param location the accepted center location of the flat area
     * @return the block location where the schematic should begin
     */
    // finds the corner of the flat land so there can be a building
    private Location getBuildSpot(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        int halfWidth = width / 2;
        int halfLength = length / 2;

        int cornerX = x - halfWidth;
        int cornerZ = z - halfLength;

        int y = actualGround(world, cornerX, cornerZ);

        return new Location(world, cornerX + 0.5, y + 1, cornerZ + 0.5);
    }

    /**
     * Stops the current search and clears all stored search data
     */
    public void stop(){
        search = false;
        target = null;
        flatSpot.clear();

        if (task != null){
            task.cancel();
            task = null;
        }
        if (npc != null){
            npc.getNavigator().cancelNavigation();
        }
    }

}
