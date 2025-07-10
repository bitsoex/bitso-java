package com.bitso;

/**
 * The target environment to connect to.
 */
public enum Target {

    production("https://api.bitso.com"),
    stage("https://api-stage.bitso.com"),
    development("https://api-dev.bitso.com");

    private final String uri;

    Target(String uri) {
        this.uri = uri;
    }

    /** Returns the URI for this environment. */
    public String uri() {
        return uri;
    }
}
