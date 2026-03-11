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

/**
 * Bukkit Plugin class
 * Creates the npc, registers player chat events, attaches the builder trait to the npc
 * @author haf31
 */

public class Plugin extends JavaPlugin implements Listener {
    private NPC npc;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        //makes sure event methods can run
        Bukkit.getPluginManager().registerEvents(this, this);

    World world = Bukkit.getWorlds().get(0); // get world
    Location spawn = world.getSpawnLocation().clone().add(0, 1,0 ); // get spawn
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
@EventHandler
//method runs when a player sends a chat message
public void playerChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();
        //when player types search or Search it'll still run
        if(!message.equalsIgnoreCase("search") && !message.equalsIgnoreCase("build")) return;
        Bukkit.getScheduler().runTask(this, () -> {
            event.getPlayer().sendMessage("Starting search");

            //have the search center be the spawn
            Location spawnCenter = Bukkit.getWorlds().get(0).getSpawnLocation().clone().add(0,1,0);

            // set the npc navigator biggger for a larger radius
            npc.getNavigator().getLocalParameters().range(400);
            //attach trait to npc and run search
            Builder trait = npc.getOrAddTrait(Builder.class);
            trait.startSearch(this, spawnCenter, buildLocation -> {
                event.getPlayer().sendMessage("Flat land found at: "+ buildLocation.getBlockX() + " " + buildLocation.getBlockY() + " " + buildLocation.getBlockZ() );
            });
            trait.build(this);
        });

}
 }