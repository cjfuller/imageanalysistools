/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.analysistoolsinterface;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import edu.stanford.cfuller.imageanalysistools.parameters.Parameter;

/**
 * Transfer handler to coordinate drag and drop of parameters into the parameter
 * setup list in the GUI.
 * 
 * @author Colin J. Fuller
 *
 */
public class ParameterTransferHandler extends TransferHandler {
	

	protected class ParameterTransferable implements Transferable {
		

		Parameter p;
		
		public ParameterTransferable(Parameter p) {
			this.p = p;
		}
		

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public Object getTransferData(DataFlavor arg0)
				throws UnsupportedFlavorException, IOException {
			
			
			if (! isDataFlavorSupported(arg0)) {throw new UnsupportedFlavorException(arg0);}
			
			return this.p;

		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			String flavorString = DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Object.class.getName();
			DataFlavor[] flavors = new DataFlavor[1];
			try {
				flavors[0] = new DataFlavor(flavorString);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			return flavors;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public boolean isDataFlavorSupported(DataFlavor arg0) {
			
			
			if (! (arg0.getMimeType().equals( DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Object.class.getName()))) {
				return false;
			}
			
			return true;
		}
		
		
		
		
		
	}
	
	private static final long serialVersionUID = -6116086270413180899L;
	
	ParameterSetupController controller;
	
	public ParameterTransferHandler(ParameterSetupController p) {
		this.setParameterController(p);
	}
	
	public void setParameterController(ParameterSetupController p) {
		this.controller = p;
	}
	
	//export
	
	public int getSourceActions(JComponent c) {
		return COPY;
	}
	
	public Transferable createTransferable(JComponent c) {
		try {
			
			JList l = (JList) c;
						
			Parameter p = (Parameter) l.getSelectedValue();
						
			return new ParameterTransferable(p);

		} catch (ClassCastException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public boolean canImport(TransferHandler.TransferSupport t) {
		
		
		try {
			
			
			ij.IJ.getClassLoader().loadClass(Parameter.class.getName());
			
			DataFlavor objectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Object.class.getName());

			
			if (! t.isDataFlavorSupported(objectFlavor)) {return false;}
			
			
			Parameter p = (Parameter) t.getTransferable().getTransferData(objectFlavor);
			
			if (p != null) return true;
			
			return false;
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean importData(TransferHandler.TransferSupport t) {
				
		if (this.canImport(t)) {
			
		
			try {
				
				DataFlavor objectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Object.class.getName());

				if (! t.isDataFlavorSupported(objectFlavor)) {return false;}
				
				Parameter p = (Parameter) t.getTransferable().getTransferData(objectFlavor);

				this.controller.useParameter(p);
				
				return true;
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (ClassCastException e) {
				e.printStackTrace();
				return false;
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		
		}
		
		return false;
	}
	
	
}
