package brightspark.sparkz.util;

import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.Map;

public class OtherMods
{
    public static final String RF = "redstoneflux";
    public static final String TESLA = "tesla";

    private static final Map<String, Boolean> LOADED = new HashMap<>();

    public static boolean isLoaded(String modId)
    {
        Boolean isLoaded = LOADED.get(modId);
        if(isLoaded == null)
        {
            isLoaded = Loader.isModLoaded(modId);
            LOADED.put(modId, isLoaded);
        }
        return isLoaded;
    }
}
