package sh.yannick.state;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class State implements Closeable {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean restricted;

    private Map<ResourceKey, Resource<?, ?>> resources;
    private Map<SpecKey, Class<? extends Resource<?, ?>>> baseClasses;
    private Map<SpecKey, Class<?>> specDefinitions;
    private Map<SpecKey, Class<?>> statusDefinitions;
    private Map<SpecKey, ResourceListener<?, ?, ?>> listeners;

    public static StateBuilder builder() {
        return new StateBuilder();
    }

    public State initializeListeners() {
        listeners.values().forEach(listener -> listener.onInit(this));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T, S, C extends Resource<T, S>> C addResource(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Resource<T, S> resource = addResource(mapper, mapper.readTree(file));

        SpecKey key = new SpecKey(resource.getApiVersion(), resource.getKind());
        Class<C> baseClass = (Class<C>) baseClasses.get(key); // Here, we don't know what class to cast to, so we have to use an unchecked cast
        if (baseClass == null) {
            throw new IllegalArgumentException("No base class found for apiVersion " + resource.getApiVersion() + " and kind " + resource.getKind());
        }
        return baseClass.cast(resource);
    }

    public <T, S, C extends Resource<T, S>> C addResource(File file, Class<C> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return clazz.cast(addResource(mapper, mapper.readTree(file)));
    }

    public <T, S, C extends Resource<T, S>> C addResource(File file, JsonFactory factory, Class<C> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(factory);
        return clazz.cast(addResource(mapper, mapper.readTree(file)));
    }

    public <T, S, C extends Resource<T, S>> C addResource(InputStream in, JsonFactory factory) throws IOException {
        ObjectMapper mapper = new ObjectMapper(factory);
        return addResource(mapper, mapper.readTree(in));
    }

    public <T, S, C extends Resource<T, S>> C addResource(String json) throws IOException {
        return addResource(json, true);
    }

    public <T, S, C extends Resource<T, S>> C addResource(String json, boolean triggerUpdate) throws IOException {
        return addResource(parseResource(json), triggerUpdate);
    }

    public <T, S, C extends Resource<T, S>> C addResource(ObjectMapper mapper, JsonNode node) throws IOException {
        return addResource(parseResource(mapper, node));
    }

    public <T, S, C extends Resource<T, S>> C addResource(C resource) {
        return addResource(resource, true);
    }

    public <T, S, C extends Resource<T, S>> C addResource(C resource, boolean triggerUpdates) {
        log.info("Received resource {}/{}/{}", resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName());

        ResourceKey key = new ResourceKey(resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName());
        if (triggerUpdates) {
            triggerUpdates(resource, key);
        } else {
            resources.put(key, resource);
        }

        return resource;
    }

    @SuppressWarnings("unchecked")
    public <T, S, C extends Resource<T, S>> C deleteResource(String apiVersion, String kind, String name, Class<C> clazz) {
        ResourceListener<T, S, C> listener = (ResourceListener<T, S, C>) listeners.get(new SpecKey(apiVersion, kind)); // Unchecked, but we know it's safe
        if (listener == null) {
            throw new IllegalArgumentException("No listener found for apiVersion " + apiVersion + " and kind " + kind);
        }

        C resource = getResource(apiVersion, kind, name, clazz).orElseThrow(() -> new IllegalArgumentException("No resource found for apiVersion " + apiVersion + ", kind " + kind + " and name " + name));
        listener.onDelete(resource);
        resources.remove(new ResourceKey(apiVersion, kind, name));
        return resource;
    }

    public <T, S, C extends Resource<T, S>> C parseResource(String yaml) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return parseResource(mapper, mapper.readTree(yaml));
    }

    public <T, S, C extends Resource<T, S>> C parseResource(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return parseResource(mapper, mapper.readTree(in));
    }

    public <T, S, C extends Resource<T, S>> C parseResource(ObjectMapper mapper, JsonNode node) throws IOException {
        JsonNode apiVersionNode = node.at("/apiVersion");
        JsonNode kindNode = node.at("/kind");

        if (apiVersionNode.isMissingNode() || kindNode.isMissingNode()) {
            throw new IllegalArgumentException("Missing resource apiVersion or kind");
        }

        SpecKey key = new SpecKey(apiVersionNode.asText(), kindNode.asText());
        Class<?> resourceClass = Objects.requireNonNullElse(baseClasses.get(key), Resource.class);
        Class<?> specClass = specDefinitions.get(key);
        Class<?> statusClass = statusDefinitions.get(key);
        if (specClass == null || statusClass == null) {
            throw new IllegalArgumentException("No spec or status class found for apiVersion " + apiVersionNode.asText() + " and kind " + kindNode.asText());
        }

        JavaType javaType;
        if (resourceClass == Resource.class) {
            javaType = mapper.getTypeFactory().constructParametricType(resourceClass, specClass, statusClass);
        } else {
            javaType = mapper.getTypeFactory().constructType(resourceClass);
        }

        return mapper.treeToValue(node, javaType);
    }

    // TODO: deep clone
    public <T, S, C extends Resource<T, S>> Optional<C> getResource(String apiVersion, String kind, String name, Class<C> clazz) {
        Resource<?, ?> resource = resources.get(new ResourceKey(apiVersion, kind, name));
        if (resource == null) {
            return Optional.empty();
        }

        Class<?> specClass = specDefinitions.get(new SpecKey(apiVersion, kind));
        Class<?> statusClass = statusDefinitions.get(new SpecKey(apiVersion, kind));
        if (specClass == null || statusClass == null) {
            throw new IllegalArgumentException("No spec or status class found for apiVersion " + apiVersion + " and kind " + kind);
        }

        return Optional.of(clazz.cast(resource));
    }

    @SuppressWarnings("unchecked")
    private <T, S, C extends Resource<T, S>> void triggerUpdates(C resource, ResourceKey key) {
        ResourceListener<T, S, C> listener = (ResourceListener<T, S, C>) listeners.get(new SpecKey(resource.getApiVersion(), resource.getKind())); // Unchecked, but we know it's safe
        if (listener == null && restricted) {
            throw new IllegalArgumentException("Resource %s/%s/%s does not have registered listener (state is in restricted mode)".formatted(resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName()));
        } else if (listener == null) {
            resources.put(key, resource); // Just store the resource and do nothing
        } else {
            // TODO: call those in threads
            try {
                if (!resources.containsKey(key)) {
                    resources.put(key, resource);
                    listener.onCreate(resource);
                } else {
                    // Preserve status and errors.
                    // If we don't do this, we lose the status and errors of the resource. This results in ugly NPEs.
                    resource.setStatus((S) resources.get(key).getStatus());
                    resource.setErrors(resources.get(key).getErrors());

                    resources.put(key, resource);
                    listener.onUpdate(resource);
                }
            } catch (NoSuchElementException e) {
                log.warn("Resource {}/{}/{} was requested but does not exist", resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName());
                resource.addError(e.getMessage());
            } catch (Exception e) {
                log.error("Error while processing resource {}/{}/{}", resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName(), e);
                resource.addError(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        log.info("Shutdown hook received for state {}: destroying all resources and listeners.", name);
        listeners.values().forEach(ResourceListener::onDestroy);

        resources.clear();
        listeners.clear();
        baseClasses.clear();
        specDefinitions.clear();
        statusDefinitions.clear();

        log.info("State {} closed.", name);
    }
}
