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

java_import Java::edu.stanford.cfuller.imageanalysistools.metric.AreaAndPerimeterMetric
java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageFactory
java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
java_import Java::edu.stanford.cfuller.imageanalysistools.image.ImageSet
java_import Java::edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

describe AreaAndPerimeterMetric do

  before :each do 

    ic = ImageCoordinate.createCoordXYZCT(8,8,1,1,1)
    im = ImageFactory.createWritable(ic, 0)
    
    #set up a 2x2 region, upper left
    1.upto(2) do |x|
      1.upto(2) do |y|
        ic.setCoordXYZCT(x,y,0,0,0)
        im.setValue(ic, 1)
      end
    end

    #set up a 3x3 region, lower right
    4.upto(6) do |x|
      4.upto(6) do |y|
        ic.setCoordXYZCT(x,y,0,0,0)
        im.setValue(ic, 2)
      end
    end

    m = AreaAndPerimeterMetric.new
    is = ImageSet.new(ParameterDictionary.emptyDictionary())

    is.addImageWithImageAndName(im, "test image")

    @q = m.quantify(im, is)

  end

  it "should correctly calculate the area and perimeter of a 2x2 region" do

    meas = @q.getAllMeasurementsForRegion(1)

    meas.each do |m|
      if m.getMeasurementName() == "perimeter" then
        m.getMeasurement().should eq 4.0
      end

      if m.getMeasurementName() == "area" then
        m.getMeasurement().should eq 4.0
      end
    end
  end

  it "should correctly calculate the area and perimeter of a 3x3 region" do
    
    meas = @q.getAllMeasurementsForRegion(2)
    meas.each do |m|
      if m.getMeasurementName() == "perimeter" then
        m.getMeasurement().should eq 8.0
      end

      if m.getMeasurementName() == "area" then
        m.getMeasurement().should eq 9.0
      end
    end
  end

end

