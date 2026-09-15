package com.aft.api.agent.tool;

/** Sunucu araclarin yalnizca semasini bilir; yurutme tamamen istemcidedir. */
public interface ToolSpec {

    String name();

    String description();

    String inputSchema();

    /** Yazma etkisi olan araclar kullanici onayi ister. */
    default boolean writeEffect() {
        return false;
    }
}
