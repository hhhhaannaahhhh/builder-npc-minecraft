package NPC;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Main Bukkit Plugin class
 * Creates the npc named Jeff, registers player chat events,
 * attaches the Citizens2 builder trait to the npc,and passes the schematics
 *
 * @author haf31
 */

public class Plugin extends JavaPlugin implements Listener {
    private NPC npc;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs(); // create plugin folder
        //makes sure event methods can run
        Bukkit.getPluginManager().registerEvents(this, this);

        // get world n put jeff at spawn
    World world = Bukkit.getWorlds().get(0);
    Location spawn = world.getSpawnLocation().clone().add(0, 1,0 );
        //register citizens trait
        CitizensAPI.getTraitFactory().registerTrait(
                TraitInfo.create(Builder.class).withName("builder")
        );
// use an old jeff if its there
        npc = null;
        for(NPC npc1 : CitizensAPI.getNPCRegistry()){
            if (npc1.getName() != null && npc1.getName().equals("Jeff")){
                npc = npc1;
                break;
            }
        }
        //create jeff if its not there
        if (npc == null) {
            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Jeff");
        }
        if (!npc.isSpawned()){
            npc.spawn(spawn);
        }
        // make sure npc has the trait
        npc.getOrAddTrait(Builder.class);

}

    /**
     * Handles player chats and uses them as commands
     * The player can type schematics to list all avalible,
     * The schematic name to start the flat land search,
     * then build to build the schematic
     *
     * @param event the chat event for the players messages
     */
    @EventHandler
//method runs when a player sends a chat message
public void playerChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();
        //when player types search or Search it'll still run

        Bukkit.getScheduler().runTask(this, () -> {
            //attach trait to npc and run search
            Builder trait = npc.getOrAddTrait(Builder.class);

            // list schematics
            if (message.equalsIgnoreCase("schematics")){
                event.getPlayer().sendMessage(listSchematics());
                return;
            }
            if (message.equalsIgnoreCase("build")){
                trait.build(this);
                return;
            }

            //have the search center be the spawn
            Location spawnCenter = Bukkit.getWorlds().get(0).getSpawnLocation().clone().add(0,1,0);

            // set the npc navigator biggger for a larger radius
            npc.getNavigator().getLocalParameters().range(400);
            npc.getNavigator().cancelNavigation();
            trait.setSchematic(message);
            trait.startSearch(this, spawnCenter, buildLocation -> {
                event.getPlayer().sendMessage("Flat land found at: "+ buildLocation.getBlockX() + " " + buildLocation.getBlockY() + " " + buildLocation.getBlockZ() );
            });
        });

}
        private String listSchematics(){
        // get schematic folder
            File dir = new File(getDataFolder(), "schematics");
            // get all files ending with .txt
            File [] file = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
            // turn file list into the schematic names
            String names = Arrays.stream(file).map(File::getName).sorted().collect(Collectors.joining(", "));
        // Send the schematic list back to the player
            return "Schematics:" + names + " Type a name without .txt to search, then type build";
        }
 }