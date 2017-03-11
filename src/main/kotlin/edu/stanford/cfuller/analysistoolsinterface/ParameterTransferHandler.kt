package edu.stanford.cfuller.analysistoolsinterface

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

import javax.swing.JComponent
import javax.swing.JList
import javax.swing.TransferHandler

import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter

/**
 * Transfer handler to coordinate drag and drop of parameters into the parameter
 * setup list in the GUI.

 * @author Colin J. Fuller
 */
class ParameterTransferHandler(p: ParameterSetupController) : TransferHandler() {
    private inner class ParameterTransferable(internal var p: Parameter) : Transferable {
        /* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
        @Throws(UnsupportedFlavorException::class, IOException::class)
        override fun getTransferData(arg0: DataFlavor): Any {
            if (!isDataFlavorSupported(arg0)) {
                throw UnsupportedFlavorException(arg0)
            }
            return this.p
        }

        /* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
        override fun getTransferDataFlavors(): Array<DataFlavor>? {
            val flavorString = DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Any::class.java.name
            try {
                return arrayOf(DataFlavor(flavorString))
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return null
            }
        }

        /* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
        override fun isDataFlavorSupported(arg0: DataFlavor): Boolean {
            if (arg0.mimeType != DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Any::class.java.name) {
                return false
            }
            return true
        }
    }

    internal var controller: ParameterSetupController = p

    fun setParameterController(p: ParameterSetupController) {
        this.controller = p
    }

    //export

    override fun getSourceActions(c: JComponent): Int {
        return TransferHandler.COPY
    }

    public override fun createTransferable(c: JComponent): Transferable? {
        try {
            val l = c as JList<*>
            val p = l.selectedValue as Parameter
            return ParameterTransferable(p)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return null
        }
    }

    override fun canImport(t: TransferHandler.TransferSupport): Boolean {
        try {
            ij.IJ.getClassLoader().loadClass(Parameter::class.java.name)
            val objectFlavor = DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Any::class.java.name)
            if (!t.isDataFlavorSupported(objectFlavor)) {
                return false
            }
            val p = t.transferable.getTransferData(objectFlavor)
            return p != null
        } catch (e: ClassNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return false
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return false
        } catch (e: UnsupportedFlavorException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return false
        }
    }

    override fun importData(t: TransferHandler.TransferSupport): Boolean {
        if (this.canImport(t)) {
            try {
                val objectFlavor = DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Any::class.java.name)
                if (!t.isDataFlavorSupported(objectFlavor)) {
                    return false
                }
                val p = t.transferable.getTransferData(objectFlavor) as Parameter
                this.controller.useParameter(p)
                return true
            } catch (e: ClassNotFoundException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                return false
            } catch (e: ClassCastException) {
                e.printStackTrace()
                return false
            } catch (e: UnsupportedFlavorException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    companion object {
        private val serialVersionUID = -6116086270413180899L
    }
}
