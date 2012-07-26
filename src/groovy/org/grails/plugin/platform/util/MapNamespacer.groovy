package org.grails.plugin.platform.util

class MapNamespacer {
    private Map delegateMap
    private String keyPrefix
    
    MapNamespacer(String keyPrefix, Map delegateMap) {
        this.delegateMap = delegateMap
        this.keyPrefix = keyPrefix
    }
    
    Object get(Object key) {
        delegateMap[keyPrefix + key.toString()]
    }

    Object put(Object key, Object value) {
        delegateMap[keyPrefix + key.toString()] = value
    }
}