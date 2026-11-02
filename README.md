# Minecraft NPC Builder Plugin (Final Uni Assignment)  
## Project Overview  
This project is a Minecraft server plugin developed in Java using the Paper and Bukkit API's and Citizens2. The player can choose a building schematic and have the 
NPC find a suitable flat area before building the schematic chosen.  
The plugin uses NPC navigation, .txt schematic files, terrain search, and automatic block placement. The aim is to make building easier on the player 
by having the NPC do all of the work.  
## Features
1. NPC Creation: Creates or reuses an NPC with the Citizens2 API.
2. Builder Trait: Attaches a custom trait to the NPC to control the building behaviour.
3. Schematic Selection: Allows players to view and select available building schematics through chat.
4. .txt File Schematic: Stores the building blueprints in .txt files.
5. Automatic Search: NPC searches for a suitable bit of flat land based on the dimensions of the selected schematic.
6. Terrain Validation: Checks that the land is actually flat and rejects blocks like water and lava.
7. NPC Navigation: Uses Citizens2 to move the NPC towards the building location.
8. Automatic Building: Reads the selected schematic and places the blocks into the Minecraft world.

## Technologies Used  
1. Java: Main programming language
2. Minecraft: Game environment
3. Paper: Minecraft server software
4. Bukkit API: Server Interaction, events, and block placement
5. Citizens2: NPC creation, traits, and navigation
6. Maven: Dependency and project management
7. IntelliJ IDEA: Development environment

## Installation and Setup  
### Requirements
1. Set up a Paper Minecraft server.
2. Start the server once to generate the folders.
3. Download and install Citizens2 by placing the .jar fille into the servers plugin folder.
4. Place the compiled NPC builder plugin .jar file into the same plugins folder.
5. Start or restart the server.
6. The plugin should load and create or reuse the NPC

## Building from Source  
The project uses Maven to manage its dependencies.  
1. Open the project in IntelliJ IDEA.
2. Allow Maven to load the project dependencies.
3. Build the project using Maven.
4. Find the generated plugin .jar file inside the target folder.
5. Copy the .jar file into the Minecraft server's plugins folder.

Make sure the Java runtime used to start the server is compatible with the Java version used to build Paper and the plugin.  

## How to use  
The plugin is controlled through the Minecraft chat.  
### Commands  
schematics: lists the building schematics.  
building_1, building_2, building_3: Selects the corresponding schematic and starts searching for suitable land.  
build: Builds the selected schematic at the saved location.  

## Building Process  
1. The player types schematics to see the list of buildings
2. The player types a schematic name
3. The plugin loads the selected schematic and calculates the required building dimensions
4. The flat land search checks possible locations around the search origin.
5. When a suitable location is found the NPC moves towards it using Citizens2 navigation.
6. Once the NPC reaches the location, the build coordinates are saved and returned back to the player in the chat.
7. The player types build to place the selected structure into the world.

## Schematic System  
Buildings are stored as text files rather then being hardcoded into the Java classes.  
This allows additional buildings to be added without creating code clutter.  
Schematic files are stored in:    
plugins/Plugin/schematics  
### Schematic File Format  
Each line represents one block and follows the format:  
X Y Z material  
The coordinates are relative to the building origin rather than fixed Minecraft world coordinates.  
X: East/West axis  
Y: Vertical  
Z: North/South axis  
Material: The Bukkit material name of the block  
Blank lines and lines beginning with # are ignored.  

### Schematic Dimensions  
When a schematic is selected, the plugin reads the coordinates and calculates the minimum and maximum X and Z values.  
These values determine the required width and length of the building area. This means the search automatically adapts to different 
building sizes rather then checking a fixed area.  

### Flat Land Search Algorithm  
The plugin uses a grid based search that expands outwards from the starting location. Each location is validated against the 
schematic's required width and length. The validation checks whether the area is large enough, if the ground blocks are the same height,
and if the area contains unsuitable blocks.  
The height tolerance is set to 0 meaning the land needs to be perfectly flat.  
When a valid location is found, Citizens2 handles the NPC's movement towards the spot.  

### Project Structure  
Plugin class: Plugin startup, NPC creation, and player chat events.  
Builder Trait: Stores the selected schematic, build location, and controls the NPC's building behaviour.  
FlatLand Class: Generates locations and validates terrain.  
Schematic Files: Stores the building coordinates and block materials.  

### Known Limitations  
1. The NPC does not physically place blocks one at a time. The entire building is placed when the player types build.
2. The plugin searches for natural flat land and doesn't flatten terrain.
3. The search requires perfectly flat land which can make larger buildings almost impossible to place.
4. A large search radius can increase the server workload and cause lag.
5. Directional blocks such as stairs, doors, and trapdoors have limited support and may not face the correct direction.
6. The player needs to type the schematic name correctly.
7. The search can fail if no location is found in the radius.

### Future Improvements  
1. Animated block building
2. Improved directional block support
3. More efficient search
4. Schematic rotation to have a building face different ways
5. More building schematics
6. NPC flattening terrain for the schematic
7. Improved and clearer player commands and error messages
8. Better feedback when a location cant be found

### Development  
The project was developed using an iterative methodology. Features were implemented and tested individually before being added into the plugin.  
Testing was done directly in Minecraft to find issues with NPC navigation, schematic loading, location validation, and block placement.  
Gitlab was used for repository storage and task management during term time development.  
During future development Github will be used.  
### Author  
Hannah Frick (haf31) Developed as a final year university project.


