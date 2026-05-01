# Builder NPC in Minecraft
**Author**: Hannah Frick (haf31)  
**Supervisor**: Jim Finnis(jcf12)  
**Degree Scheme**:Computer Science    
**Degree Code**:G400  
**Module Code**: CS39440

# Projet Overview 
This project is a Minecraft server plugin written in Java. It uses Bukkit API
and Citizens2 API to create an NPC builder that can search for flat land and build structures from stored
schematic files.  
The player can type the name of a schematic in chat and the NPC will search for a suitable flat area. Once a valid location is found the player
can type build to make the NPC place the building.


# Main Features
- Creates or reuses an NPC 
- Uses chat commands to select a building schematic
- Lists available schematics files
- Calculates the width and length of each building
- Searches for a flat area that is large enough for the selected building.
- Moves the NPC to the selected build location.
- Builds the selected structure using block data from a text file.

# Commands 
shematics : lists the schematic files  
building_* : schematic name without .txt starts searching for flat land
build : builds the selected schematic at the location found

## Technologies Used

- Java
- Paper Minecraft server
- Bukkit API
- Citizens2 API
- Minecraft Java Edition
- Text based schematic files