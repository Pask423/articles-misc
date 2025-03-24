package org.ps.junit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

class OverallArchitectureJUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter().importPackages("org.ps");

    /**
     * 1) Classes in package X should only depend on classes in package Y.
     * (Also allow some core Java / framework packages as needed.)
     */
    @Test
    void testClassesInXShouldOnlyDependOnClassesInY() {
        // Given
        ArchRule rule = classes()
            .that().resideInAPackage("..x..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..y..",
                "java.."
            );

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 2) Classes in the service layer should not access (depend on) controller layer classes.
     */
    @Test
    void testServiceLayerShouldNotAccessControllers() {
        // Given
        ArchRule rule = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 3) No cyclic dependencies should exist among these packages.
     *    This example checks for cycles in sub-packages under "org.ps".
     */
    @Test
    void testNoCyclicDependencies() {
        // Given
        ArchRule rule = SlicesRuleDefinition.slices()
            .matching("org.ps.(*)..")
            .should()
            .beFreeOfCycles();

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 4) Prevent field and setter-based injection (e.g., @Autowired on fields/setters).
     */
    @Test
    void testNoFieldInjection() {
        // Given
        ArchRule noFieldInjectionRule = noFields()
            .should().beAnnotatedWith(Autowired.class)
            .because("Use constructor injection instead of field injection.");

        ArchRule noSetterInjectionRule = noMethods()
            .that().haveNameMatching("set[A-Z].*")
            .should().beAnnotatedWith(Autowired.class)
            .because("Use constructor injection instead of setter injection.");

        // When
        ArchRule compositeRule = CompositeArchRule.of(noFieldInjectionRule).and(noSetterInjectionRule);

        // Then
        compositeRule.check(IMPORTED_CLASSES);
    }

    /**
     * 5) Ensure @Transactional annotation is only used in the service layer.
     *    (Check both class-level and method-level usage.)
     */
    @Test
    void testTransactionalAnnotationOnlyInService() {
        // Given
        ArchRule classLevelTransactional = classes()
            .that().areAnnotatedWith(Transactional.class)
            .should().resideInAPackage("..service..")
            .because("Class-level @Transactional belongs in the service layer only.");

        ArchRule methodLevelTransactional = methods()
            .that().areAnnotatedWith(Transactional.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..service..")
            .because("Method-level @Transactional belongs in the service layer only.");

        // When
        ArchRule compositeRule = CompositeArchRule.of(classLevelTransactional).and(methodLevelTransactional);

        // Then
        compositeRule.check(IMPORTED_CLASSES);
    }

    /**
     * 6) Enforce @Repository and @Service annotation usage in specific packages:
     *    - @Repository only in ..repository..
     *    - @Service only in ..service..
     */
    @Test
    void testRepositoryAnnotationInRepositoryPackage() {
        // Given
        ArchRule rule = classes()
            .that().areAnnotatedWith(Repository.class)
            .should().resideInAPackage("..repository..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    @Test
    void testServiceAnnotationInServicePackage() {
        // Given
        ArchRule rule = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..service..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }
}
