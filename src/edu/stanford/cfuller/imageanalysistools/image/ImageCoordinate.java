/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Represents an integer-valued coordinate in an n-dimensional image.
 *
 * <p>
 * To avoid unnecessary object allocation when iterating over large images, this class provides basic object pooling capabilities.
 * ImageCoordinates can only be created using this class's static factory methods ({@link #createCoord}, {@link #cloneCoord}), which will reuse ImageCoordinates if there are any
 * available or allocate more as necessary.
 *
 * <p> Users of this class are responsible for calling {@link #recycle} on any ImageCoordinate they explicitly create using one of the static factory methods
 * but not any coordinates that they obtain by any other means.  Calling recycle on an ImageCoordinate will return it to the pool of available coordinates.
 * ImageCoordinates are not guaranteed to retain their value after being recycled, so they should not be used after being recycled.
 *
 * <p> This class is partly thread-safe in the sense that multiple threads can use the same pool of ImageCoordinates safely.  However, ImageCoordinate objects themselves
 * are not thread-safe and should not be shared between threads.
 *
 *
 * @author Colin J. Fuller
 *
 */

public class ImageCoordinate implements java.io.Serializable, Collection<Integer> {

	static final long serialVersionUID = 1L;
	
	static final int initialStaticCoordCount = 8;
	
	private static java.util.Deque<ImageCoordinate> availableStaticCoords;
	
	private HashMap<String, Integer> dimensionCoordinates;
	private ArrayList<String> indexToStringMapping;
			
	static {

        synchronized(ImageCoordinate.class) {

            availableStaticCoords = new java.util.LinkedList<ImageCoordinate>();

            for (int i = 0; i < initialStaticCoordCount; i++) {
                ImageCoordinate c = new ImageCoordinate();
                availableStaticCoords.add(c);

            }

        }
	}
	
	private static ImageCoordinate getNextAvailableCoordinate() {
		ImageCoordinate coord = null;
		synchronized(ImageCoordinate.class) {
			if (! availableStaticCoords.isEmpty()) {
				coord = availableStaticCoords.removeFirst();
			}
		}
		if (coord == null) {
			coord = new ImageCoordinate();
		} else {
			coord.clear();
		}
		return coord;
	}
	
	private ImageCoordinate(){
		this.dimensionCoordinates = new HashMap<String, Integer>();
		this.indexToStringMapping = new ArrayList<String>();
	}

    /**
     * Gets the x-component of this ImageCoordinate.
     * @return  The x-component as an integer.
     * @deprecated 	Use {@link #get(String, int)} or {@link #get(int)} instead.
     */
	public int getX(){return this.get("x");}

    /**
     * Gets the y-component of this ImageCoordinate.
     * @return  The y-component as an integer.
     * @deprecated 	Use {@link #get(String, int)} or {@link #get(int)} instead.
     */
	public int getY(){return this.get("y");}

    /**
     * Gets the z-component of this ImageCoordinate.
     * @return  The z-component as an integer.
     * @deprecated 	Use {@link #get(String, int)} or {@link #get(int)} instead.
     */
	public int getZ(){return this.get("z");}

    /**
     * Gets the c-component of this ImageCoordinate.
     * @return  The c-component as an integer.
     * @deprecated 	Use {@link #get(String, int)} or {@link #get(int)} instead.
     */
	public int getC(){return this.get("c");}

    /**
     * Gets the t-component of this ImageCoordinate.
     * @return  The t-component as an integer.
     * @deprecated 	Use {@link #get(String, int)} or {@link #get(int)} instead.
     */
	public int getT(){return this.get("t");}
	
	/**
	 * Gets the specified named coordinate component of the ImageCoordinate.
	 * @param dimension		the name of the coordinate component to retrieve.
	 * @return				the component in the named direction, or 0 if the component has not been specified.
	 */
	public int get(String dimension) {
		if (this.dimensionCoordinates.containsKey(dimension)) {
			return this.dimensionCoordinates.get(dimension);
		}
		return 0;
	}
	
	/**
	 * Gets the specified coordinate by index.
	 * <p>
	 * The order of coordinates will be the order in which they were set if done manually,
	 * or the order from the coordinate this ImageCoordinate was cloned from, 
	 * or otherwise ordered unpredictably (though consistent between calls), probably
	 * according to hash key.
	 * <p>
	 * If the coordinate index is out of bounds, returns 0 instead of throwing an exception.
	 * 
	 * @param dimensionIndex	the index of the dimension whose value will be retrieved.
	 * @return					the value of the specified dimension component, or 0 if the index was out of bounds.
	 * 
	 */
	public int get(int dimensionIndex) {
		if (dimensionIndex >= 0 && dimensionIndex < this.indexToStringMapping.size()) {
			return this.get(this.indexToStringMapping.get(dimensionIndex));
		}
		return 0;
	}
	
	/**
	 * Sets the specified named coordinate component of the ImageCoordinate.
	 * <p>
	 * If the named component does not yet exist in the ImageCoordinate, it will be added.
	 * 
	 * @param dimension		the name of the coordinate component to set.
	 * @param value			the value to which the named component will be set.
	 */
	public void set(String dimension, int value) {
		if (! this.dimensionCoordinates.containsKey(dimension)) {
			this.indexToStringMapping.add(dimension);
		}
		this.dimensionCoordinates.put(dimension, value);
	}
	
	/**
	 * Sets the specified coordinate component of the ImageCoordinate by its index.
	 * 
	 * @param dimension		the index of the dimension to set.
	 * @param value			the value to which to set the coordinate.
	 * @deprecated	use {@link #set(String, int)} instead.
	 */
	public void set(int dimension, int value) {
		this.set(this.indexToStringMapping.get(dimension), value);
	}

    /**
     * Factory method that creates a 5D ImageCoordinate with the specified coordinates.
     *
     * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
     *
     * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
     * make the coordinate available for reuse on other calls of this method.
     *
     * @param x     The x-component of the ImageCoordinate to be created.
     * @param y     The y-component of the ImageCoordinate to be created.
     * @param z     The z-component of the ImageCoordinate to be created.
     * @param c     The c-component of the ImageCoordinate to be created.
     * @param t     The t-component of the ImageCoordinate to be created.
     * @return      An ImageCoordinate whose components have been set to the specified values.
     */
	public static ImageCoordinate createCoordXYZCT(int x, int y, int z, int c, int t) {
		
		ImageCoordinate staticCoord = ImageCoordinate.getNextAvailableCoordinate();
		
		staticCoord.set("x", x);
		staticCoord.set("y", y);
		staticCoord.set("z", z);
		staticCoord.set("c", c);
		staticCoord.set("t", t);

		return staticCoord;
		
	}
	
	/**
     * Factory method that creates a 5D ImageCoordinate with the specified coordinates.
     *
     * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
     *
     * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
     * make the coordinate available for reuse on other calls of this method.
     *
     * @param x     The x-component of the ImageCoordinate to be created.
     * @param y     The y-component of the ImageCoordinate to be created.
     * @param z     The z-component of the ImageCoordinate to be created.
     * @param c     The c-component of the ImageCoordinate to be created.
     * @param t     The t-component of the ImageCoordinate to be created.
     * @return      An ImageCoordinate whose components have been set to the specified values.
     * @deprecated	use {@link #createCoordXYZCT(int, int, int, int, int)} instead.
     */
	public static ImageCoordinate createCoord(int x, int y, int z, int c, int t) {
		
		return ImageCoordinate.createCoordXYZCT(x, y, z, c, t);
		
	}
	
	/**
     * Factory method that creates a zero-dimensional ImageCoordinate that can be manually extended.
     *
     * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
     *
     * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
     * make the coordinate available for reuse on other calls of this method.
     *
     * @return      An ImageCoordinate with no specified dimensions.
     */
	public static ImageCoordinate createCoord() {
		return ImageCoordinate.getNextAvailableCoordinate();
	}
	
	/**
     * Factory method that creates an n-dimensional ImageCoordinate whose dimensions and values
     * are specified by the supplied Map, where n is the count of keys in the Map.
     *
     * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
     *
     * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
     * make the coordinate available for reuse on other calls of this method.
     *
     * @param dimensionValues	A Map containing the name of each dimension as a String and each dimension's mapped value.
     * @return      An ImageCoordinate with the specified dimensions and values.
     */
	public static ImageCoordinate createCoord(Map<String, Integer> dimensionValues) {
		ImageCoordinate coord = ImageCoordinate.getNextAvailableCoordinate();
		
		for (String s : dimensionValues.keySet()) {
			coord.set(s, dimensionValues.get(s));
		}
		
		return coord;
	}


    /**
     * Factory method that creates and ImageCoordinate that represents the same position in coordinate space as the supplied coordinate.
     *
     * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
     * make the coordinate available for reuse on other calls of this method.
     *
     * @param toClone   The ImageCoordinate to clone.
     * @return          An ImageCoordinate whose components are set to the same values as the components of the supplied ImageCoordinate.
     */
    public static ImageCoordinate cloneCoord(ImageCoordinate toClone) {
    	
    	ImageCoordinate newCoord = ImageCoordinate.getNextAvailableCoordinate();
    	
    	newCoord.setCoord(toClone);
    	
    	return newCoord;
    	
    }

    /**
     * Returns an ImageCoordinate to the pool of ImageCoordinates available for reuse.
     *<p>
     * This method should be called on an ImageCoordinate if and only if that coordinate was obtained using one of the static factory methods
     * ({@link #createCoord} or {@link #cloneCoord}).
     *<p>
     * Each ImageCoordinate must only be recycled once, or else future calls to the factory methods may return the same coordinate multiple times with
     * different values, which will lead to unpredictable behavior.  No checking is done to ensure that each coordinate was only recycled once (this was found
     * to cause enough overhead that the benefits of reusing objects were cancelled out).
     *
     *
     */
	public void recycle() {

        synchronized(ImageCoordinate.class) {

            availableStaticCoords.add(this);

        }
	}
	
	/**
	 * Gets the dimension of the ImageCoordinate: that is, the number of defined named dimensions
	 * it contains.
	 * 
	 * @return		the dimension of the ImageCoordinate.
	 */
	public int getDimension() {
		return this.indexToStringMapping.size();
	}

    /**
     * Converts the ImageCoordinate to a string representation.
     * <p>
     * The returned string will be "(x,y,z,c,t) = (a,b,c,d,e)", where a through e 
     * are placeholders for the actual coordinates of the ImageCoordinate, and x
     * through t will be replaced by the defined named coordinates of the ImageCoordinate.
     * @return  The string representation of the ImageCoordinate.
     */
	public String toString() {
		if (this.getDimension() == 0) {
			return "() = ()";
		}
		String keyString = "(" + this.indexToStringMapping.get(0);
		String valueString = "(" + this.get(0);
		for (int i = 1; i < this.getDimension(); i++) {
			keyString+= "," + this.indexToStringMapping.get(i);
			valueString+= "," + this.get(i);
		}
		keyString += ")";
		valueString += ")";
		String result = keyString + " = " + valueString;
		return result;
	}

    /**
     * Sets all the components of the ImageCoordinate to the specified values.
     * 
     * @param x     The new x-component of the ImageCoordinate.
     * @param y     The new y-component of the ImageCoordinate.
     * @param z     The new z-component of the ImageCoordinate.
     * @param c     The new c-component of the ImageCoordinate.
     * @param t     The new t-component of the ImageCoordinate.
     */
	public void setCoordXYZCT(int x, int y, int z, int c, int t) {
		this.set("x", x);
		this.set("y", x);
		this.set("z", x);
		this.set("c", x);
		this.set("t", x);
	}
	
    /**
     * Sets all the components of the ImageCoordinate to the specified values.
     * 
     * @param x     The new x-component of the ImageCoordinate.
     * @param y     The new y-component of the ImageCoordinate.
     * @param z     The new z-component of the ImageCoordinate.
     * @param c     The new c-component of the ImageCoordinate.
     * @param t     The new t-component of the ImageCoordinate.
     * @deprecated	use {@link #setCoordXYZCT(int, int, int, int, int)} instead.
     */
	public void setCoord(int x, int y, int z, int c, int t) {
		this.set("x", x);
		this.set("y", x);
		this.set("z", x);
		this.set("c", x);
		this.set("t", x);
	}

    /**
     * Sets the components of the ImageCoordinate to the values of the components of another ImageCoordinate.
     * @param other     The ImageCoordinate whose component values will be copied.
     */
    public void setCoord(ImageCoordinate other) {
    	this.clear();
    	for (String s : other.indexToStringMapping) {
    		this.dimensionCoordinates.put(s, other.get(s));
    		this.indexToStringMapping.add(s);
    	}
    }
    
    /**
     * Clears all stored coordinate dimensions and values.
     * <p>
     * After calling this method, the ImageCoordinate will be zero-dimensional.
     * 
     */
    public void clear() {
    	this.dimensionCoordinates.clear();
    	this.indexToStringMapping.clear();
    }

    /**
     * Sets the x-component of the ImageCoordinate to the specified value.
     * @param x The new x-component of the ImageCoordinate.
     * @deprecated use {@link #set(String, int)} instead.
     */
	public void setX(int x) {
		this.set("x", x);
	}

    /**
     * Sets the y-component of the ImageCoordinate to the specified value.
     * @param y The new y-component of the ImageCoordinate.
     * @deprecated use {@link #set(String, int)} instead.
     */
	public void setY(int y) {
		this.set("y", y);
	}

    /**
     * Sets the z-component of the ImageCoordinate to the specified value.
     * @param z The new z-component of the ImageCoordinate.
     * @deprecated use {@link #set(String, int)} instead.
     */
	public void setZ(int z) {
		this.set("z", z);
	}

    /**
     * Sets the c-component of the ImageCoordinate to the specified value.
     * @param c The new c-component of the ImageCoordinate.
     * @deprecated use {@link #set(String, int)} instead.
     */
	public void setC(int c) {
		this.set("c", c);
	}

    /**
     * Sets the t-component of the ImageCoordinate to the specified value.
     * @param t The new t-component of the ImageCoordinate.
     * @deprecated use {@link #set(String, int)} instead.
     */
	public void setT(int t) {
		this.set("t", t);
	}
	
	/**
	 * Collection interface methods.
	 */

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(Integer arg0) {
		throw new UnsupportedOperationException("Add not supported for ImageCoordinates.");
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Integer> arg0) {
		throw new UnsupportedOperationException("Add not supported for ImageCoordinates.");
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object arg0) {
		Integer iArg = (Integer) arg0;
		for (Integer i : this) {
			if (i == iArg) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object o : arg0) {
			if (!this.contains(o)) return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new ImageCoordinateIterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException("Remove not supported for ImageCoordinates.");

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Remove not supported for ImageCoordinates.");

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Retain not supported for ImageCoordinates.");

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return this.getDimension();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return toArray(new Object[0]);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arg0) {
		if (arg0.length < this.size()) {
			int c = 0; 
			for (Integer i : this) {
				arg0[c++] = (T) i;
			}
			return arg0;
		} else {
			Integer[] toReturn = new Integer[this.size()];
			int c = 0;
			for (Integer i : this) {
				toReturn[c++] = i;
			}
			return (T[]) toReturn;
		}
	}
	
	protected class ImageCoordinateIterator implements Iterator<Integer> {
		
		int currentIndex;
		
		public ImageCoordinateIterator() {
			this.currentIndex = 0;
		}
		
		public boolean hasNext() {
			return (this.currentIndex < getDimension());
		}
		
		public Integer next() {
			if (this.hasNext()) return get(currentIndex);
			
			throw new NoSuchElementException("No more elements in ImageCoordinate.");
			
		}
		
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported for ImageCoordinate.");
		}
		
		
	}
	
}
