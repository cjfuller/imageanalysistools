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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents an integer-valued coordinate in a 5D image (X, Y, Z, color, time).
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
	
	private static java.util.ArrayList<ImageCoordinate> availableStaticCoords;
	private static java.util.Hashtable<ImageCoordinate, Boolean> inUse;
	
	private int x;
	private int y;
	private int z;
	private int c;
	private int t;
	
	private final int dimensionality = 5;
			
	static {

        synchronized(ImageCoordinate.class) {

            availableStaticCoords = new java.util.ArrayList<ImageCoordinate>();
            inUse = new java.util.Hashtable<ImageCoordinate, Boolean>();

            for (int i = 0; i < initialStaticCoordCount; i++) {
                ImageCoordinate c = new ImageCoordinate();
                inUse.put(c, false);
                availableStaticCoords.add(c);

            }

        }
	}
	
	private ImageCoordinate(){}

    /**
     * Gets the x-component of this ImageCoordinate.
     * @return  The x-component as an integer.
     */
	public int getX(){return this.x;}

    /**
     * Gets the y-component of this ImageCoordinate.
     * @return  The y-component as an integer.
     */
	public int getY(){return this.y;}

    /**
     * Gets the z-component of this ImageCoordinate.
     * @return  The z-component as an integer.
     */
	public int getZ(){return this.z;}

    /**
     * Gets the c-component of this ImageCoordinate.
     * @return  The c-component as an integer.
     */
	public int getC(){return this.c;}

    /**
     * Gets the t-component of this ImageCoordinate.
     * @return  The t-component as an integer.
     */
	public int getT(){return this.t;}

    /**
     * Factory method that creates an ImageCoordinate with the specified coordinates.
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
	public static synchronized ImageCoordinate createCoord(int x, int y, int z, int c, int t) {
		
		ImageCoordinate staticCoord = null;

		if (! availableStaticCoords.isEmpty()) {
			
			staticCoord = availableStaticCoords.remove(0);
			
//			if (inUse.get(staticCoord)) {
//				Logger.getLogger("edu.stanford.cfuller.imageanalysistools").warning("WARNING! Image coordinate was likely recycled twice: " + staticCoord.toString() + "  This can only result from a coding bug.");
//				Logger.getLogger("edu.stanford.cfuller.imageanalysistools").warning("at:");
//                try {
//                    throw new IllegalStateException();
//                } catch (IllegalStateException e) {
//                    e.printStackTrace();
//                }
//                staticCoord = new ImageCoordinate();
//			} 

		} else {
			staticCoord = new ImageCoordinate();
		}
		
		staticCoord.x = x;
		staticCoord.y = y;
		staticCoord.z = z;
		staticCoord.c = c;
		staticCoord.t = t;
//		inUse.put(staticCoord, true);
		return staticCoord;
		
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

        return createCoord(toClone.getX(), toClone.getY(), toClone.getZ(), toClone.getC(), toClone.getT());

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

            /*if (availableStaticCoords.contains(this)) {
                Logger.getLogger("edu.stanford.cfuller.imageanalysistools").warning("WARNING! Possible second recycle of: " + this.toString() + "  This can only result from a coding bug.");
                Logger.getLogger("edu.stanford.cfuller.imageanalysistools").warning("at:");
                try {
                    throw new IllegalStateException();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            } else {
            */
            availableStaticCoords.add(this);
            //}
//          inUse.put(this, false);
        }
	}

    /**
     * Converts the ImageCoordinate to a string representation.
     * <p>
     * The returned string will be "(x,y,z,c,t) = (a,b,c,d,e)", where a through e are placeholders for the actual coordinates of the ImageCoordinate.
     * @return  The string representation of the ImageCoordinate.
     */
	public String toString() {
		String result = "(x,y,z,c,t) = (" + Integer.toString(this.x)+ ", "+ Integer.toString(this.y) + ", " + Integer.toString(this.z)+ ", " + Integer.toString(this.c) + ", " + Integer.toString(this.t) + ")";
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
	public void setCoord(int x, int y, int z, int c, int t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.c = c;
		this.t = t;
	}

    /**
     * Sets the components of the ImageCoordinate to the values of the components of another ImageCoordinate.
     * @param other     The ImageCoordinate whose component values will be copied.
     */
    public void setCoord(ImageCoordinate other) {
        this.setCoord(other.getX(), other.getY(), other.getZ(), other.getC(), other.getT());
    }

    /**
     * Sets the x-component of the ImageCoordinate to the specified value.
     * @param x The new x-component of the ImageCoordinate.
     */
	public void setX(int x) {
		this.x = x;
	}

    /**
     * Sets the y-component of the ImageCoordinate to the specified value.
     * @param y The new y-component of the ImageCoordinate.
     */
	public void setY(int y) {
		this.y = y;
	}

    /**
     * Sets the z-component of the ImageCoordinate to the specified value.
     * @param z The new z-component of the ImageCoordinate.
     */
	public void setZ(int z) {
		this.z = z;
	}

    /**
     * Sets the c-component of the ImageCoordinate to the specified value.
     * @param c The new c-component of the ImageCoordinate.
     */
	public void setC(int c) {
		this.c = c;
	}

    /**
     * Sets the t-component of the ImageCoordinate to the specified value.
     * @param t The new t-component of the ImageCoordinate.
     */
	public void setT(int t) {
		this.t = t;
	}
	
	/**
	 * Gets the coordinate specifies by the supplied index, ordered in the same
	 * way as when iterating over the coordinates.
	 * @param index		The index of the coordinate to get.
	 * @return			The coordinate at the specified index.
	 * @throws ArrayIndexOutOfBoundsException if the index specified is not >= 0 and less than the dimensionality of the ImageCoordiante.
	 */
	public Integer get(int index) {
		switch (index) {
		case 0:	return getX();
		case 1: return getY();
		case 2: return getZ();
		case 3: return getC();
		case 4: return getT();
		default: break;
		}
		throw new ArrayIndexOutOfBoundsException("Invalid coordinate index.  Dimensionality: " + dimensionality);
	}
	
	
	/**
	 * Sets the coordinate specifies by the supplied index, ordered in the same
	 * way as when iterating over the coordinates.
	 * @param index	The index of the coordinate to get.
	 * @param value	The value to which the specified coordinate will be set.
	 * @throws ArrayIndexOutOfBoundsException if the index specified is not >= 0 and less than the dimensionality of the ImageCoordiante.
	 */
	public void set(int index, int value) {
		switch (index) {
		case 0:	setX(value); return;
		case 1: setY(value); return;
		case 2: setZ(value); return;
		case 3: setC(value); return;
		case 4: setT(value); return;
		default: break;
		}
		throw new ArrayIndexOutOfBoundsException("Invalid coordinate index.  Dimensionality: " + dimensionality + ".  Index: " + index);
	}
	
	/**
	 * Gets the dimensionality of the ImageCoordinate (that is, the number of dimensions with coordinates stored).
	 * @return	The dimensionality, as an int.
	 */
	public int getDimensionality() {
		return this.dimensionality;
	}

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
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
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
		return dimensionality;
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
			if (this.currentIndex < dimensionality) {
				return true;
			}
			return false;
		}
		
		public Integer next() {
			switch (currentIndex++) {
			case 0:	return getX();
			case 1: return getY();
			case 2: return getZ();
			case 3: return getC();
			case 4: return getT();
			default: break;
			}
			
			throw new NoSuchElementException("No more elements in ImageCoordinate.");
			
		}
		
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported for ImageCoordinate.");
		}
		
		
	}
	
}
