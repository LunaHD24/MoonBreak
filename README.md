**MoonBreak is an easy-to-use, high-performance and lightweight Custom Tool and Block API**

## Feature Overview
- Complete vanilla-like Breaking Simulation
- Custom Blocks with their own Hardness values
- Custom Tools with own speeds, durability & more
- 100% pure Paper API, no NMS
- Event Hook Callback Integration for Custom Tools
- Ability to disable vanilla Behaviors for specific Tools
- Incredibly lightweight <70KB (API [standalone]: ~12KB; Plugin [includes API]: ~69KB)
- Low impact on server resources

## Installation
Detailed information on how to install the plugin and integrate the API using Gradle or Maven can be found in the [Wiki](https://github.com/LunaHD24/MoonBreak/wiki).

## Getting Started

### Creating a Custom Tool
(Mining Speed/Level are not limited to vanilla values)
```java
CustomToolType HAMMER = CustomToolType.builder()
        .name(Component.text("Hammer"))
        .material(Material.IRON_PICKAXE)
        .speed(CustomToolType.MiningSpeed.GOLD)
        .miningLevel(CustomToolType.MiningLevel.IRON)
        .maxDurability(1000)
        .affectedByWrongTool(true)
        .affectedUnderwater(true)
        .affectedByFloating(false)
        .build();

private void init() {
    BuiltinRegistries.TOOL_TYPE.register(Key.key("example:hammer"), HAMMER);
}
```
Obtaining the itemstack is as simple as:
```java
CustomTool tool = CustomTool.of(HAMMER);
ItemStack toolItem = tool.itemStack();
```

### Creating a Custom Block
(Block Hardness is not limited to vanilla values)
```java
CustomBlockType LIGHT_OBSIDIAN = CustomBlockType.of(Material.OBSIDIAN, Material.STONE); // Copies the Hardness of Stone (=1.5). Not limited to vanilla values

private void init() {
    BuiltinRegistries.BLOCK_TYPE.register(Key.key("example:light_obsidian"), LIGHT_OBSIDIAN);
}
```
Placing the block is as simple as:
```java
CustomBlockManager.manager().place(yourLocation, LIGHT_OBSIDIAN);
```

## Documentation & License
Documentation can be found in the wiki [here](https://github.com/LunaHD24/MoonBreak/wiki).<br>
JavaDocs in the web can be found [here](https://lunaa.dev/repository/maven/dev/lunaa/moonbreak/moonbreak-api/1.1.0/javadoc).<br>
MoonBreak is licensed under the GNU General Public License v3.0
