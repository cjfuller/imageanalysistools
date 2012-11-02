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

require 'edu/stanford/cfuller/imageanalysistools/resources/common_methods'

##
# A collection of methods to enhance the ruby scripting interface
# 
# @author Colin J. Fuller
# 
module IATScripting

  ##
  # Specifies a section of parameter defintions contained in an attached block
  #
  def parameter_definitions(p)
  
    @__parameter_dictionary = p
  
    yield
  
  end

  ##
  # Defines the method that will run on the images to be the attached block.
  # 
  # @param [Symbol] name  (optional) a name for the method.
  # @return               nil
  # 
  def method_definition(name = :ScriptMethod)
    nil
  end
      
end