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
        // Given: Define a rule that restricts classes in package '..x..'
        // to depend only on classes in '..y..' or standard Java packages.
        ArchRule rule = classes()
                .that().resideInAPackage("..x..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..y..",   // Allow dependency on package '..y..'
                        "java.."   // Allow dependency on Java standard library
                );

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 2) Classes in the service layer should not access (depend on) controller layer classes.
     */
    @Test
    void testServiceLayerShouldNotAccessControllers() {
        // Given: Define a rule that prevents the service layer
        // from depending on classes in the controller layer.
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 3) No cyclic dependencies should exist among these packages.
     * This example checks for cycles in sub-packages under "org.ps".
     */
    @Test
    void testNoCyclicDependencies() {
        // Given: Define a rule to ensure there are no cyclic dependencies
        // between modules grouped by their first-level sub-packages under 'org.ps'.
        ArchRule rule = SlicesRuleDefinition.slices()
                .matching("org.ps.(*)..")  // Define slices by sub-packages under 'org.ps'
                .should()
                .beFreeOfCycles();         // Ensure there's no cyclic dependency between them

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * 4) Prevent field and setter-based injection (e.g., @Autowired on fields/setters).
     */
    @Test
    void testNoFieldInjection() {
        // Given: Define a rule that disallows field injection using @Autowired.
        ArchRule noFieldInjectionRule = noFields()
                .should().beAnnotatedWith(Autowired.class)
                .because("Use constructor injection instead of field injection.");

        // Also define a rule that disallows setter injection using @Autowired.
        ArchRule noSetterInjectionRule = noMethods()
                .that().haveNameMatching("set[A-Z].*")
                .should().beAnnotatedWith(Autowired.class)
                .because("Use constructor injection instead of setter injection.");

        // When: Combine both rules into one composite rule.
        ArchRule compositeRule = CompositeArchRule.of(noFieldInjectionRule).and(noSetterInjectionRule);

        // Then
        compositeRule.check(IMPORTED_CLASSES);
    }

    /**
     * 5) Ensure @Transactional annotation is only used in the service layer.
     * (Check both class-level and method-level usage.)
     */
    @Test
    void testTransactionalAnnotationOnlyInService() {
        // Given: Define a rule that ensures classes annotated with @Transactional
        // are located in the service layer.
        ArchRule classLevelTransactional = classes()
                .that().areAnnotatedWith(Transactional.class)
                .should().resideInAPackage("..service..")
                .because("Class-level @Transactional belongs in the service layer only.");

        // Also define a rule for methods annotated with @Transactional
        // to be declared only in service layer classes.
        ArchRule methodLevelTransactional = methods()
                .that().areAnnotatedWith(Transactional.class)
                .should().beDeclaredInClassesThat().resideInAPackage("..service..")
                .because("Method-level @Transactional belongs in the service layer only.");

        // When: Combine both rules into one composite rule.
        ArchRule compositeRule = CompositeArchRule.of(classLevelTransactional).and(methodLevelTransactional);

        // Then
        compositeRule.check(IMPORTED_CLASSES);
    }

    /**
     * 6) Enforce @Repository and @Service annotation usage in specific packages:
     * - @Repository only in ..repository..
     * - @Service only in ..service..
     */
    @Test
    void testRepositoryAnnotationInRepositoryPackage() {
        // Given: Define a rule that ensures @Repository-annotated classes
        // are only located in the repository package.
        ArchRule rule = classes()
                .that().areAnnotatedWith(Repository.class)
                .should().resideInAPackage("..repository..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }

    @Test
    void testServiceAnnotationInServicePackage() {
        // Given: Define a rule that ensures @Service-annotated classes
        // are only located in the service package.
        ArchRule rule = classes()
                .that().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..service..");

        // Then
        rule.check(IMPORTED_CLASSES);
    }
}
