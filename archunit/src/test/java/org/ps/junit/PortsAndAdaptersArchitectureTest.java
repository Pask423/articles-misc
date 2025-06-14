package org.ps.junit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class PortsAndAdaptersArchitectureTest {

    /**
     * Let's assume following structure
     * org.ps
     * ├─ domain
     * │   └─ ... (domain models and services)
     * ├─ application
     * │   ├─ port
     * │   │   ├─ in
     * │   │   │   └─ ... (interfaces for incoming incoming requests and messages)
     * │   │   └─ out
     * │   │       └─ ... (interfaces for outgoing requests and messages)
     * │   └─ ... (application services, use case implementations)
     * ├─ adapters
     * │   ├─ in (incoming requests and messages)
     * │   └─ out (outgoing requests and messages)
     * ├─ infrastructure
     * │   └─ ... (external setups - DB connections, queues, metrics)
     * └─ config
     * └─ ... (configurations classes for all other packages)
     */
    @Test
    public void hexagonArchTest() {
        // Given
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.ps.hexagon");
        LayeredArchitecture portsAndAdaptersLayers = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                // Define each “layer” by its package
                .layer("Adapters").definedBy("..adapters..")
                .layer("Application").definedBy("..application..")
                .layer("Config").definedBy("..config..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                // Domain may not access any layer but can be access by Application and Adapters layers.
                .whereLayer("Domain").mayNotAccessAnyLayer()
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapters")
                // Application may access the Config and Domain layers but can be access by Adapters layer.
                .whereLayer("Application").mayOnlyAccessLayers("Config", "Domain")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapters")
                // Adapters may access Application, Adapters, Domain and Infrastructure but cannot be access by other layers.
                .whereLayer("Adapters").mayOnlyAccessLayers("Infrastructure", "Config", "Application", "Domain")
                .whereLayer("Adapters").mayNotBeAccessedByAnyLayer()
                // Infrastructure can only access Config layer but can be access only by Adapters.
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Config")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Adapters")
                // Config may not be access any layer but can be access by Application, Adapters, Domain and Infrastructure.
                .whereLayer("Config").mayNotAccessAnyLayer()
                .whereLayer("Config").mayOnlyBeAccessedByLayers("Application", "Adapters", "Domain", "Infrastructure");

        // Then
        portsAndAdaptersLayers.check(importedClasses);
    }
}
