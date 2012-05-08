#!/usr/bin/ruby

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


# generate_filter_list.rb
# 
# Makes the auto-generated xml list of available filters for the gui method
# constructor.
# 
# Execution: 
# cd <scriptdir>
# ruby generate_filter_list.rb
# If you are generating filters from a nonstandard source location, then add 
# the path to the paths_to_src variable as well as the package path to the 
# filter directory to filter_paths


header = <<-HEADER
<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : filters.xml
    Author     : auto-generated
-->


HEADER

Open_filter_string = "<filter "

Close_filter_string = "></filter>"

Display_string = "displayname="

Class_string = "class="

def make_entry(name, class_name)
  
  Open_filter_string + Display_string + "\"" + name + "\" " + Class_string + "\"" + class_name + "\"" + Close_filter_string
  
end

paths_to_src = ['../src/', '../src/']

filter_paths = ['edu/stanford/cfuller/imageanalysistools/filter/', 'edu/stanford/cfuller/imageanalysistools/filter/morph/']

gui_resources_path = '../src/edu/stanford/cfuller/analysistoolsinterface/resources/'

filters_xml_filename=  'filters.xml'

filename = gui_resources_path + filters_xml_filename

f = File.open(filename, "w")

f.puts(header)
f.puts("<root>")



paths_to_src.each_with_index do |path, index|
  
  Dir.foreach(path + filter_paths[index]) do |potential_filter|
    if potential_filter.match(".*Filter.java") then
      name = potential_filter.gsub(".java", "")
      sf = File.open(path + filter_paths[index] + File::Separator + potential_filter)
      no_impl = false
      sf.each_line do |line|
        if /public abstract class #{name}/.match(line) or /public interface #{name}/.match(line) then
          no_impl = true
          break
        end
      end
      if (no_impl) then
        next
      end
      
      java_path = filter_paths[index].gsub("/", ".")
      
      class_name = java_path + name
      
      f.puts(make_entry(name, class_name))
      
    end
  end
  
end

f.puts("</root>")

