#--
# /* ***** BEGIN LICENSE BLOCK *****
#  * 
#  * Copyright (c) 2012 Colin J. Fuller
#  * 
#  * Permission is hereby granted, free of charge, to any person obtaining a copy
#  * of this software and associated documentation files (the Software), to deal
#  * in the Software without restriction, including without limitation the rights
#  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  * copies of the Software, and to permit persons to whom the Software is
#  * furnished to do so, subject to the following conditions:
#  * 
#  * The above copyright notice and this permission notice shall be included in
#  * all copies or substantial portions of the Software.
#  * 
#  * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#  * SOFTWARE.
#  * 
#  * ***** END LICENSE BLOCK ***** */
#++

module IATScripting

  ##
  # Gets an instance of a Filter by its class name.
  # @param  filter_name an object that supplies the name of the filter (relative to the ...filter java package;
  #         for morphological filters, supply, e.g. :morph.OpeningFilter)
  # @return an instance of the requested Filter object
  # 
  def get_filter(filter_name)
  
    java_import (Filter_package_name + filter_name.to_s)

    const_get(filter_name).new
    
  end

  ##
  # Creates a read-only (deep) copy of an Image object.
  # @param  another_image an object that implements the Image interface.
  # @return an object that implements the Image interface and is a deep copy of the supplied Image.
  # 
  def image_copy(another_image)

    java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageFactory

    ImageFactory.create(another_image)

  end

  ##
  # Creates a shallow read-only copy of an Image object.
  # @param  an another_image object that implements the Image interface.
  # @return an object that implements the Image interface and shares the same 
  #         pixeldata and metadata as the supplied Image but can be boxed and 
  #         iterated separately.
  # 
  def image_shallow_copy(another_image)

    java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageFactory

    ImageFactory.createShallow(another_image)

  end

  ##
  # Creates a writable (deep) copy of an Image object.
  # @param  another_image  an object that implements the Image interface that wil be copied
  # @return an object that implements the WritableImage interface and is a 
  #         deep copy of the supplied Image.
  # 
  def writable_image_copy(another_image)

    java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageFactory

    ImageFactory.createWritable(another_image)

  end

  ##
  # Defines a parameter in the parameter dictionary used for the analysis.
  # Any existing value will be overwritten.
  # 
  # @param [Symbol] key   a symbol that is the name of the parameter
  # @param value          the value to be associated with the key
  # @return               the value of the parameter; nil if it could not be set
  # 
  def set_parameter(key, value)

    return nil unless @__parameter_dictionary 

    @__parameter_dictionary.setValueForKey(key.to_s, value.to_s)

  end

  ##
  # Gets a parameter from the parameter dictionary used for the analysis.
  # Any existing value will be overwritten.
  # 
  # @param [Symbol] key   a symbol that is the name of the parameter
  # @return [String]      the value of that parameter or nil if it does not exist
  #
  def parameter(key)

    unless @__parameter_dictionary and @__parameter_dictionary.hasKey(key.to_s) then 
      return nil
    end

    @__parameter_dictionary.getValueForKey(key.to_s)

  end

end