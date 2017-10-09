package brightspark.sparkz.init;

import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SItems
{
    public static List<Item> ITEMS = new ArrayList<>();

    static
    {

    }

    private static void addItem(Item item)
    {
        ITEMS.add(item);
    }

    public static Item[] getItems()
    {
        return ITEMS.toArray(new Item[ITEMS.size()]);
    }

    public static void uninitLists(){
        ITEMS = null;
    }
}
