package fr.redstonneur1256.omega.messages;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;

public class Serialization {

    public static final Genson GENSON = new GensonBuilder()
            .setConstructorFilter(VisibilityFilter.ALL)
            .useClassMetadata(true)
            .create();

}
