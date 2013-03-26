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
java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
java_import Java::java.awt.image.BufferedImage
java_import Java::ij.ImagePlus

describe ImageFactory do

	it "should be able to initialize an image given dimension sizes and an initial value" do

		im = ImageFactory.createWritable(ImageCoordinate.createCoordXYZCT(2,2,2,2,2), 3.0)

		im.getDimensionSizes.each { |c| im.getDimensionSizes.get(c).should eq 2 }

		im.each { |ic| im.getValue(ic).should eq 3.0 }

	end

	it "should be able to initialize an image from a BufferedImage" do

		buf_im = BufferedImage.new(10, 10, BufferedImage::TYPE_USHORT_GRAY)

		buf_im.getRaster.setSample(3,3,0, 2)

		im = ImageFactory.java_send :create, [BufferedImage], buf_im

		im.getValue(ImageCoordinate.createCoordXYZCT(3,3,0,0,0)).should eq 2

	end

	it "should be able to initialize an image from an ImagePlus" do 
	
		imp = ImageFactory.create(ImageCoordinate.createCoordXYZCT(10, 10, 10, 10, 10), 3.0).toImagePlus

		im = ImageFactory.java_send :create, [ImagePlus], imp

		im.getValue(ImageCoordinate.createCoordXYZCT(1, 1, 1, 1, 1)).should eq 3.0

	end

	it "should be able to initialize an image given pixel data and metadata" do 

		im = ImageFactory.create(ImageCoordinate.createCoordXYZCT(10, 10, 10, 10, 10), 3.0)

		im_2 = ImageFactory.create(im.getMetadata, im.getPixelData)

		im_2.getValue(ImageCoordinate.createCoordXYZCT(1,1,1,1,1)).should eq 3.0

	end

	context "copying operations" do 

		before :each do 

			@im = ImageFactory.createWritable(ImageCoordinate.createCoordXYZCT(64,64,10,3,10), 0.0)
			@coord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0)

		end

		it "should be able to deep copy an image" do
			im_cp = ImageFactory.create(@im)
			@im.setValue(@coord, 1.0)

			im_cp.getValue(@coord).should eq 0.0

			lambda { im_cp.setValue(@coord, 1.0) }.should raise_error
		end

		it "should be able to deep copy an image as a writable image" do 
			im_cp = ImageFactory.createWritable(@im)
			im_cp.setValue(@coord, 1.0)

			@im.getValue(@coord).should eq 0.0
		end

		it "should be able to shallow copy an image" do 
			im_cp = ImageFactory.createShallow(@im)
			@im.setValue(@coord, 1.0)

			im_cp.getValue(@coord).should eq 1.0
		end

		it "should copy a non-writable image when getting a writable instance" do
			im_ro = ImageFactory.create(@im)
			im_writ = ImageFactory.writableImageInstance(im_ro)
			im_writ.setValue(@coord, 1.0)

			im_ro.getValue(@coord).should eq 0.0
		end

	end


end

