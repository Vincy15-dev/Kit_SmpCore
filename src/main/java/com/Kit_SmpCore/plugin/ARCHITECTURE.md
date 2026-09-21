# Kit_SmpCore - Plugin Structure Plan

## File Tree
src/main/java/com/Kit_SmpCore/plugin/
├── MainClass.java (existing, to be modified)
├── kit/
│   ├── Kit.java
│   ├── KitItem.java
│   ├── KitContainer.java
│   ├── KitLoader.java
│   ├── KitManager.java
│   ├── CooldownService.java
│   ├── KitGiver.java
│   └── storage/
│       ├── KitStorage.java (interface)
│       └── YamlKitStorage.java
├── player/
│   └── PlayerData.java
├── gui/
│   ├── CosmosGUI.java (InventoryHolder for main menu)
│   ├── PreviewGUI.java (InventoryHolder for preview)
│   ├── GUIRenderer.java
│   └── GuiRefreshTask.java
├── listener/
│   ├── KitListener.java (inventory events, click handling)
│   └── PlayerListener.java (join/quit events)
├── command/
│   ├── KitCommand.java
│   └── KitTabCompleter.java
└── util/
    ├── ColorUtil.java
    ├── ItemBuilder.java
    ├── InventoryUtil.java
    ├── TimeUtil.java
    └── RegistryUtil.java

resources/
├── plugin.yml
├── config.yml
├── kits.yml
└── messages.yml

## Data Flow
1. onEnable -> Load configs -> Register commands/listeners -> Load kits -> Start storage
2. AsyncPlayerPreLoginEvent -> Load PlayerData async -> Cache in ConcurrentHashMap
3. Player joins -> Send notification if kits ready
4. /kit command -> Open CosmosGUI (main menu)
5. Click on kit -> Check permissions/cooldown -> Build items -> Check space -> Give items -> Set cooldown -> Save async -> Feedback
6. GuiRefreshTask (20 ticks) -> Update countdowns/stars only if GUIs open
7. Quit -> Save PlayerData async -> Remove from cache -> Close GUIs
8. onDisable -> Cancel tasks -> Save all data -> Close all GUIs
