#--
# Copyright (c) 2013 Colin J. Fuller
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the Software), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#++

require 'digest'
require 'open-uri'
require 'tempfile'
require 'yaml'

Dir[File.dirname(__FILE__) + "/support/**/*.rb"].each {|f| require f}

PLANAR_IMAGE_URL = "https://s3-us-west-1.amazonaws.com/imageanalysistools/planar_test_image_smaller.ome.tif"

PLANAR_MASK_URL = "https://s3-us-west-1.amazonaws.com/imageanalysistools/planar_test_mask_smaller.ome.tif"

FILTER_REGRESSION_HASHES_FN = File.expand_path('resources/filter_regression_hashes.yml', File.dirname(__FILE__))


def filter_list_2d

	filters = []

	Dir[File.dirname(__FILE__) + "/../../main/java/edu/stanford/cfuller/imageanalysistools/filter/*Filter.java"].each do |f|

		unless /3D/.match(f) then

			filters << File.basename(f).gsub(".java", "")

		end

	end

	filters

end

def filter_regression_hashes

	YAML.load(File.read(FILTER_REGRESSION_HASHES_FN))

end


def read_image_from_url(url)

	data = nil
	im = nil

	open(url) do |f|
		data = f.read
	end

	tf = Tempfile.new('url_image_reader')

	begin

		tf.write(data)
		im = Java::edu.stanford.cfuller.imageanalysistools.image.io.ImageReader.new.read(tf.path)

	ensure

		tf.close
		tf.unlink

	end

	im

end


def hash_image_content(im)

	dims = im.getDimensionSizes

	total_pixels = dims.reduce(1) { |a,e| a * dims.get(e) }

	all_pixels = Array.new(total_pixels + dims.getDimension , 0.0)

	dims.each_with_index { |d, i| all_pixels[i] = dims.get(d) }

	counter = dims.getDimension

	im.each do |ic|

		all_pixels[counter] = im.getValue(ic)
		counter += 1

	end

	byte_str = all_pixels.pack("g*")

	digest = Digest::SHA2.new

	digest << byte_str

	Digest.hexencode(digest.digest).encode("UTF-8")

end



