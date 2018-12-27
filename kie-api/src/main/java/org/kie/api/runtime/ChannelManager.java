package org.kie.api.runtime;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    private Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void registerChannel(String name, Channel channel) {
        channels.put(name, channel);
    }

    public void unregisterChannel(String name) {
        channels.remove(name);
    }

    public Map<String, Channel> getChannels() {
        return Collections.unmodifiableMap(channels);
    }
}
