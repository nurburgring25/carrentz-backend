package dev.burikk.carrentz.engine.common;

import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 22/06/2020 08.03
 */
public class SimpleUriInfo implements UriInfo {
    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getPath(boolean decode) {
        return null;
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return null;
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        return null;
    }

    @Override
    public URI getRequestUri() {
        return null;
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return null;
    }

    @Override
    public URI getAbsolutePath() {
        return null;
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return null;
    }

    @Override
    public URI getBaseUri() {
        return null;
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();

        multivaluedHashMap.putSingle("pageSize", "1000");

        return multivaluedHashMap;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return this.getQueryParameters();
    }

    @Override
    public List<String> getMatchedURIs() {
        return null;
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        return null;
    }

    @Override
    public List<Object> getMatchedResources() {
        return null;
    }

    @Override
    public URI resolve(URI uri) {
        return null;
    }

    @Override
    public URI relativize(URI uri) {
        return null;
    }
}
