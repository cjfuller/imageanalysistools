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

require 'spec_helper'

java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageFactory

skip_image_stage = [:VoronoiFilter, :ConvexHullByLabelFilter, :FillFilter, :GaussianFitFilter]

skip = [:Filter, :SeededFilter]

skip_with_input_required = [:RegionThresholdingFilter, :ConvolutionFilter, :ActiveContourFilter]

skip.concat(skip_with_input_required)

parameters = {min_size: 20, max_size: 40, bandpass_filter_low: 3, bandpass_filter_high: 10}

params = Java::edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary.emptyDictionary

parameters.each do |k, v|
	params.setValueForKey(k.to_s, v.to_s)
end

all_filters = filter_list_2d
im = read_image_from_url(PLANAR_IMAGE_URL)
mask = read_image_from_url(PLANAR_MASK_URL)

module FilterNamespace
	include_package "edu.stanford.cfuller.imageanalysistools.filter"
end

all_filters.each do |filt|

	f_const = FilterNamespace.const_get(filt)

	next if skip.include?(filt.to_sym)

	describe f_const do 

		before :all do 

			@filt = f_const.new
			@ref_im = ImageFactory.create(im)
			@filt.setReferenceImage(@ref_im)
			@filt.setParameters(params)
			@regression_hashes =  filter_regression_hashes

		end

		unless skip_image_stage.any? { |e| f_const.to_s.match(e.to_s) } then

			context "image processing" do 

				before :all do
					@im = ImageFactory.createWritable(im)
					@pre_hash_ref = hash_image_content(@ref_im)
					@filt.apply(@im)
				end

				it "should not regress on filter output on a single plane image" do
					h = hash_image_content(@im)
					h.should eq @regression_hashes[filt][:image]
				end

				it "should not change the reference image" do 
					post_hash = hash_image_content(@ref_im)
					post_hash.should eq @pre_hash_ref
				end

			end
		end

		context "mask processing" do

			before :all do
				@mask = ImageFactory.createWritable(mask)
				@pre_hash_ref = hash_image_content(@ref_im)
				@filt.apply(@mask)
			end

			it "should not regress on filter output on a single plane mask" do 
				h = hash_image_content(@mask)
				h.should eq @regression_hashes[filt][:mask]
			end

			it "should not change the reference image" do 
				post_hash = hash_image_content(@ref_im)
				post_hash.should eq @pre_hash_ref
			end

		end
	end
end


