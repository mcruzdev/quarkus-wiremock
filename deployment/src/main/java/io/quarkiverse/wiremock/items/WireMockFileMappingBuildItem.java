package io.quarkiverse.wiremock.items;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Represents a Wiremock file mapping containing stub definitions used for HTTP request/response simulation.
 * This build item encapsulates the content of a Wiremock stub file to be processed during the build phase.
 */
public final class WireMockFileMappingBuildItem extends MultiBuildItem {

    private final String content;
    private final String filename;

    public WireMockFileMappingBuildItem(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String filename() {
        return this.filename;
    }

    public String content() {
        return this.content;
    }
}
