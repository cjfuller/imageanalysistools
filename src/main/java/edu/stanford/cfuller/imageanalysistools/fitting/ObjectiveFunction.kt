package edu.stanford.cfuller.imageanalysistools.fitting

/**

 * Describes a function that returns a (real) scalar and takes a vector of parameters.

 * @author Colin J. Fuller
 */
interface ObjectiveFunction {
    /**
     * Evaluates the function at the specified point.
     * @param parameters    A RealVector containing the values of all the independent variables and/or parameters.
     * *
     * @return              The function value at this point.
     */
    fun evaluate(parameters: org.apache.commons.math3.linear.RealVector): Double
}
