<h1 align="center">Extended ItemView (Eiv)</h1>

<p align="center">
  <img width="500" height="500" src="https://i.ibb.co/fYrqVKdC/EIV-Extended-Item-View.png">
</p>

# Items, Recipes & more

A mod all around simplifying your life by showing you the recipes you need! :D

Currently supported functions are:

- **recipe viewing**
- **bookmarking items**
- **hiding/showing overlay**

**NOTE: _Must be installed on both the client and server_**


# Developer Guide (fabric)

## Creating your mod's integration

Before you can implement your own recipes you first have to create an eiv-integration for your Mod.
This is done by creating a class implementing `IExtendedItemViewIntegration`:

```java
public class EivIntegration implements IExtendedItemViewIntegration {
    
    @Override
    public void onIntegrationInitialize() {
        
    }
    
}
```

Don't forget to add it as an entrypoint to your `fabric.mod.json`:

```json
{
...,
	"entrypoints": {
    ...,
		"eiv": [
			"de.you.modid.eiv.EivIntegration"
		]
	},
...
}
```

## Adding a new recipe type

Since you want to add a complete new way of crafting, you first need to create your viewtype.
Simply create a class implementing `IEivRecipeViewType` and override the required methods:

```java
public class YourCustomViewType implements IEivRecipeViewType {

    //Create an instance of your viewtype here
    //Relevant for next steps
    protected static final YourCustomViewType INSTANCE = new YourCustomViewType();
    
    
    @Override
    public Component getDisplayName() {
        return Component.literal("YourCustomViewType"); //This is the name of your viewtype displayed later in the recipe-view
    }

    @Override
    public int getDisplayWidth() {
        return 0; //The width of your type's gui texture
    }

    @Override
    public int getDisplayHeight() {
        return 0; //The height of your type's gui texture
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("yourmodid", "path/to/your/texture"); //Your type's gui texture
    }

    @Override
    public int getSlotCount() {
        return 0; //The amount of slots one of your type's recipes requires (all slots including results)
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        //Tell EIV where your slots are located by calling slotDefinition.addItemSlot();
        //NOTE: Slot position is relative to your gui texture

        slotDefinition.addItemSlot(0, 10, 20);
        slotDefinition.addItemSlot(1, 40, 20);

    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("yourmodid", "your_type_id"); //A unique id for your viewtype
    }

    @Override
    public ItemStack getIcon() {
        return null; //The icon displayed in the recipe-view
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(); //Return a list of blocks/items that can be used to process your recipes (e.g. for Smelting it would be the furnace)
    }
}
```

## Adding your recipe blueprint

Now you need to add your recipe's class to tell EIV about things like rendering & items.
Just create a class implementing `IEivViewRecipe` and override the required methods.

```java
public class YourCustomViewRecipe implements IEivViewRecipe {

    private final SlotContent input, output;


    //You can design your constructor to suit your needs
    public YourCustomViewRecipe(ItemStack input, ItemStack output) {

        //Define your inputs and outputs here

        this.input = SlotContent.of(input);
        this.output = SlotContent.of(output);

    }

    @Override
    public IEivRecipeViewType getViewType() {
        return YourCustomViewType.INSTANCE; //Here you need your type's instance you created before
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {

        //Tell EIV which SlotContent belongs to which of your previously defined slots
        slotFillContext.bindSlot(0, this.input);
        slotFillContext.bindSlot(1, this.output);

        //When you want to add custom information to some of your items simply add a stack modifier to the corresponding slots
        slotFillContext.addAdditionalStackModifier(0, (stack, tooltip) -> {
            tooltip.add(Component.literal("A cool item"));
        });

    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(this.input); //Return all of your inputs here
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.output); //Return all of your outputs here
    }
}
```
### The SlotContent

In EIV everything concerning recipe content is handled via a class called `SlotContent`.
It is a representation of all itemstacks a slot holds. The content is constantly ticked while a the player is looking at a recipe to achieve an overview over the possible in- & outputs.
To wrap your ingredients and results (items, itemstacks, list of items, ...) just call `SlotContent.of();`

### Slot dependencies

If there is a slot that should not tick independantly you can bind it as a dependant slot:

```java
    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {

        //...

        slotFillContext.bindDepedantSlot(0, this.input::index, this.input2);

    }
```

The method requires an integer supplier which tells the `SlotContent` at which position it is currently ticking.
In this case we are using the current index of `this.input` as the the index for `this.input2`. So they are always synchronized.

## Server side recipe representation

Since the significant change of Minecraft's recipe system with the 1.21.3, they do most of the recipe work server side and do not tell the client which recipes exist.
Unfortunately, we need the recipes client side so we have to synch the recipes between server and client.
To keep a consistent concept EIV requires a server side representation of all recipes regardless whether it's a mod or vanilla recipe.

Creating a server side representation for your mod recipes is quite easy, simply create a class that implements `IEivServerModRecipe` and override the methods:

```java
public class YourServerRecipe implements IEivServerModRecipe {


    //Create a server recipe type (the id does not have to match your client side viewtype id)
    public static final ModRecipeType<YourServerRecipe> TYPE = ModRecipeType.register(
            ResourceLocation.fromNamespaceAndPath("yourmodid", "your_recipe_id"),
            () -> new YourServerRecipe()
    );

    @Override
    public void writeToTag(CompoundTag tag) {

    }

    @Override
    public void loadFromTag(CompoundTag tag) {

    }

    @Override
    public ModRecipeType<? extends IEivServerModRecipe> getRecipeType() {
        return TYPE;
    }
}
```

## Register your recipes

Registering your recipes requires you to call 2 methods in your `onIntegrationInitialize();` method:

- `ItemViewRecipes.INSTANCE.addModRecipeProvider();`
- `ItemViewRecipes.INSTANCE.registerModRecipeWrapper();`

```java
public class EivIntegration implements IExtendedItemViewIntegration {

    @Override
    public void onIntegrationInitialize() {

        //For the server 
        ItemViewRecipes.INSTANCE.addModRecipeProvider(list -> {
            //Here you can add all your server recipes
        });

        //For the client
        ItemViewRecipes.INSTANCE.registerModRecipeWrapper(YourServerRecipe.TYPE, modRecipe -> {
            
            //Here you tell EIV how to process incoming server recipes
            //Requires you to return a list of client-side view-recipes (IEivViewRecipe)
            
            return List.of();
        });

    }

}
```

Mod recipe providers registered by `ItemViewRecipes.INSTANCE.addModRecipeProvider();` are used by the server recipe manager to maintain and update the recipe cache.
Whenever there is an update the client is informed about the update and the mod recipe wrappers registered by `ItemViewRecipes.INSTANCE.registerModRecipeWrapper();` are used to convert incoming server recipes into displayable view-recipes.

## Conclusion

And there you go! Just reproduce these steps for each of your recipe types and you'll be fine.
If you now want to create item-transfer functionality, read the section below.

## Adding recipe-transfer functionality

To be able to shift items from the players inventory into it's crafting gui you have to override a few more methods of your class that implements `IEivViewRecipe`:

```java

    @Override
    public boolean supportsItemTransfer() {
        return true; //Enable item transfer
    }

    @Override
    public Class<? extends AbstractContainerScreen<?>> getTransferClass() {
        return CraftingScreen.class; //Tell which screen is the corresponding crafting gui
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap map) {

	//Link your recipe slots to the corresponding slots in the destination inventory (the crafting inventory)

        map.linkSlots(0, 1);
        map.linkSlots(1, 2);
        map.linkSlots(2, 3);
        map.linkSlots(3, 4);
        map.linkSlots(4, 5);
        map.linkSlots(5, 6);
        map.linkSlots(6, 7);
        map.linkSlots(7, 8);
        map.linkSlots(8, 9);

    }
```
