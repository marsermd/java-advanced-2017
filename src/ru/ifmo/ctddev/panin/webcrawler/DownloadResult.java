package ru.ifmo.ctddev.panin.webcrawler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DownloadResult
{
    private final Map<String, IOException> errors = new ConcurrentHashMap<>();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();

    /**
     * @return unmodifiable set of visited elements
     */
    public Set<String> getVisited()
    {
        return Collections.unmodifiableSet(visited);
    }

    /**
     * @return unmodifiable map url to errors
     */
    public Map<String, IOException> getErrors()
    {
        return Collections.unmodifiableMap(errors);
    }

    public Stream<String> getVisitedWithoutError()
    {
        return visited.stream().filter((url) ->
        {
            return !errors.containsKey(url);
        });
    }

    public boolean isVisited(String url)
    {
        return visited.contains(url);
    }

    public void markAsVisited(String url)
    {
        visited.add(url);
    }

    public void addError(String url, IOException error)
    {
        errors.put(url, error);
    }
}
