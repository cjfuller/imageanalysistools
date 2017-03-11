package edu.stanford.cfuller.imageanalysistools.filter

/**
 * An exception thrown by a filter if a reference image is required for its operation and one has not been supplied.
 */
class ReferenceImageRequiredException: RuntimeException {
    constructor() : super() {}
    constructor(message: String) : super(message) {}
    constructor(message: String, cause: Throwable) : super(message, cause) {}
    constructor(cause: Throwable) : super(cause) {}
    companion object {
        internal val serialVersionUID = 1L
    }
}
