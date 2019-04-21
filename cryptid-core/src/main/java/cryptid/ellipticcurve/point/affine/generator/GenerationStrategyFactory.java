package cryptid.ellipticcurve.point.affine.generator;

import cryptid.ellipticcurve.TypeOneEllipticCurve;

/**
 * Interface for factory classes that can produce {@link AffinePointGenerationStrategy} instances.
 */
@FunctionalInterface
public interface GenerationStrategyFactory<T extends AffinePointGenerationStrategy> {
    /**
     * Creates a new generator which generates points on the specified curve.
     * @param ellipticCurve the curve on which points should be generated
     * @return a new generator instance
     */
    T newInstance(TypeOneEllipticCurve ellipticCurve);
}
