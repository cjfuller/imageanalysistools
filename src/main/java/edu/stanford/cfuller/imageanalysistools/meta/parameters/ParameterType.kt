/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.imageanalysistools.meta.parameters

/**
 * Parameters for storage in a ParameterDictionary can be one of four types:
 * boolean, integer, floating-point, or String.  ParameterType specifies these
 * and handles converting their names to strings.

 * @author Colin J. Fuller
 */
enum class ParameterType private constructor(private val stringRep: String) {

    BOOLEAN_T("boolean"),
    INTEGER_T("integer"),
    FLOATING_T("floating"),
    STRING_T("string");

    /**
     * Gets the name of the ParameterType as a String
     * @return the String naming the parameter type: one of boolean, integer, floating, or string.
     */
    override fun toString(): String {
        return this.stringRep
    }

}