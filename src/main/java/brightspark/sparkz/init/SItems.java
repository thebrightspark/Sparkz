package brightspark.sparkz.init;

import brightspark.sparkz.items.ItemDebug;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SItems
{
    public static List<Item> ITEMS;

    public static ItemDebug debug;

    private static void init()
    {
        ITEMS = new ArrayList<>();

        addItem(debug = new ItemDebug());
    }

    private static void addItem(Item item)
    {
        ITEMS.add(item);
    }

    public static Item[] getItems()
    {
        if(ITEMS == null) init();
        return ITEMS.toArray(new Item[ITEMS.size()]);
    }

    public static void uninitLists()
    {
        ITEMS = null;
    }
}
