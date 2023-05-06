package io.lsdconsulting.junit5

/**
 * Test that use the `LsdExtension` to generate LSD reports can use this annotation to flag a
 * method (can be private but must take no arguments) that should be invoked after each Test has run but before the
 * sequence diagram is generated.
 *
 *
 * This may be useful if it is necessary to add additional interactions or interesting givens to the TestState instance
 * before the sequence diagrams are generated.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class LsdPostTestProcessing  