package NPC;

/**
 * Stores the size information for a loaded schematic file
 * @author haf31
 */
public class LoadedSchematic {
    final int minDx; // smallest x
     final int maxDx; // largest x
     final int minDz; // smallest z
     final int maxDz; // largest z

    /**
     * Createst a loadedschematic object using the min and max schematic coordinates
     * @param minDx the smallest x coordinate
     * @param maxDx the largest x coordinate
     * @param minDz the smallest z coordinate
     * @param maxDz the largest z coordinate
     */
    LoadedSchematic(int minDx, int maxDx, int minDz, int maxDz){
        this.minDx = minDx;
        this.maxDx = maxDx;
        this.minDz = minDz;
        this.maxDz = maxDz;
    }
    // calculates the width of the schematic
    int width(){
        return (maxDx - minDx) + 1;

    }
    // calculates the length of the schematic
    int length(){
        return (maxDz - minDz) + 1;
    }
}
