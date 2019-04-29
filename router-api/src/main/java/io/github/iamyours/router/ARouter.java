package io.github.iamyours.router;

import java.util.HashMap;
import java.util.Map;

public class ARouter {
    private static final ARouter instance = new ARouter();
    private Map<String, String> routeMap = new HashMap<>();
    private boolean loaded;

    private ARouter() {
    }

    public static ARouter getInstance() {
        if (!instance.loaded) instance.init();
        return instance;
    }

    public void init() {
        if (loaded) return;
        new RouteMap().loadInto(routeMap);
        loaded = true;
    }

    public Map<String, String> getMap() {
        return routeMap;
    }

    public Postcard build(String path) {
        String component = routeMap.get(path);
        if (component == null) throw new RuntimeException("could not find route with " + path);
        return new Postcard(component);
    }
}
