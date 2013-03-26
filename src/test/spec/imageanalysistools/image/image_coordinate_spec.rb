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

import Java::edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

describe ImageCoordinate do 

	before :each do
		@coord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0)
		@dims = [ImageCoordinate::X, ImageCoordinate::Y, ImageCoordinate::Z, ImageCoordinate::C, ImageCoordinate::T]
		@other_coord = ImageCoordinate.createCoordXYZCT(1,2,3,4,5)
	end

	def each_dim
		@dims.each do |c|
			yield c
		end
	end

	it "should provide get access to x,y,z,c,t dimensions" do
		each_dim do |c|
			@coord.get(c).should eq 0
		end
	end

	it "should provide set access to x,y,z,c,t dimensions" do 
		each_dim do |c|
			@coord.set(c, 1)
		end

		each_dim do |c|
			@coord.get(c).should eq 1
		end
	end

	it "should be able to clone another coordinate" do
		cloned = ImageCoordinate.cloneCoord(@other_coord)
		cloned.should be_located_at @other_coord
	end

	it "should be able to copy another coordinate into itself" do
		@coord.setCoord(@other_coord)
		@coord.should be_located_at @other_coord
	end

end