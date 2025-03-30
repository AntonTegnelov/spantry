package com.spantry.cli;

import com.spantry.inventory.service.InventoryService;
import picocli.CommandLine;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * A custom Picocli factory responsible for injecting dependencies (like InventoryService)
 * into command instances during their creation.
 */
public class DependencyFactory implements CommandLine.IFactory {

    private final InventoryService inventoryService;

    /**
     * Creates a factory that can inject the provided InventoryService.
     * @param inventoryService The service instance to inject (must not be null).
     */
    public DependencyFactory(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "InventoryService cannot be null for factory");
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            // Try to find a constructor that accepts InventoryService
            Constructor<K> constructor = cls.getDeclaredConstructor(InventoryService.class);
            // If found, instantiate the command using that constructor and the service
            return constructor.newInstance(inventoryService);
        } catch (NoSuchMethodException e) {
            // If no constructor accepting InventoryService exists, fall back to the default
            // factory, which typically uses the no-argument constructor.
            return CommandLine.defaultFactory().create(cls);
        }
        // Other exceptions (InstantiationException, IllegalAccessException, InvocationTargetException)
        // will propagate up, indicating a problem creating the command instance.
    }
} 