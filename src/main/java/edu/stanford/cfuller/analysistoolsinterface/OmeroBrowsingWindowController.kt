package edu.stanford.cfuller.analysistoolsinterface

import java.util.ArrayList
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.DefaultMutableTreeNode
import Glacier2.CannotCreateSessionException
import Glacier2.PermissionDeniedException
import omero.ServerError
import omero.api.GatewayPrx
import omero.api.IQueryPrx
import omero.api.ServiceFactoryPrx
import omero.model.Dataset
import omero.model.Project
import omero.sys.ParametersI
import omero.rtypes.*


/**

 * @author cfuller
 */
class OmeroBrowsingWindowController {

    internal var obw: OmeroBrowsingWindow = OmeroBrowsingWindow(this)
    internal var selectedImageIds: MutableList<Long> = ArrayList<Long>()
    internal var client: omero.client? = null
    internal var serviceFactory: ServiceFactoryPrx? = null
    internal var queryService: IQueryPrx? = null
    internal var gateway: GatewayPrx? = null
    internal var lastDatasetAccessed: Dataset? = null

    internal var listener: OmeroListener? = null

    var username: String = ""
        internal set
    var hostname: String = ""
        internal set
    internal var password: CharArray = CharArray(0)

    fun openNewBrowsingWindow(l: OmeroListener) {
        this.listener = l
        this.obw = OmeroBrowsingWindow(this)
        this.obw.omeroBrowsingTree!!.model = DefaultTreeModel(null)
        this.obw.isVisible = true
        this.selectedImageIds = ArrayList<Long>()
        this.client = null
        this.serviceFactory = null
        this.queryService = null
        this.gateway = null
        this.lastDatasetAccessed = null
    }

    fun doneButtonPressed() {
        val selectedNode = this.obw.omeroBrowsingTree!!.selectionPath.lastPathComponent as DefaultMutableTreeNode
        val selected = selectedNode.userObject as Holder
        if (selected.isAnImage) {
            this.selectedImageIds.add((selected as ImageHolder).image.id.value)
        }
        if (selected.isADataset) {
            this.loadImages()
            val children = selectedNode.children()
            while (children.hasMoreElements()) {
                val child = children.nextElement() as DefaultMutableTreeNode
                val ih = child.userObject as ImageHolder
                this.selectedImageIds.add(ih.image.id.value)
            }
        }
        this.obw.dispose()
        this.listener?.imageIdsHaveBeenSelected(this.selectedImageIds)
        this.client!!.closeSession()
    }

    fun getSelectedImageIds(): List<Long> {
        return this.selectedImageIds
    }

    fun loginButtonPressed() {
        this.hostname = obw.host
        this.username = obw.username
        this.password = obw.password

        if (this.client != null) {
            this.client!!.closeSession()
        }

        this.client = omero.client(hostname)
        try {
            this.serviceFactory = this.client!!.createSession(username, String(password))
            this.gateway = this.serviceFactory!!.createGateway()
        } catch (ex: CannotCreateSessionException) {
            LoggingUtilities.severe(ex.message ?: "Cannot create session.")
        } catch (ex: PermissionDeniedException) {
            LoggingUtilities.severe(ex.message ?: "Permission denied.")
        } catch (ex: ServerError) {
            LoggingUtilities.severe(ex.toString())
        }

        val projectList = this.projectList
        java.util.Collections.sort(projectList, ProjectComparator())
        val root = DefaultMutableTreeNode(this.username)

        for (p in projectList) {
            val pNode = DefaultMutableTreeNode(ProjectHolder(p))
            val pdLinks = p.copyDatasetLinks()
            val datasets = ArrayList<Dataset>()

            for (pdl in pdLinks) {
                datasets.add(pdl.child)
            }

            java.util.Collections.sort(datasets, DatasetComparator())

            for (d in datasets) {
                val dNode = DefaultMutableTreeNode(DatasetHolder(d))
                pNode.add(dNode)

            }
            root.add(pNode)
        }
        this.obw.omeroBrowsingTree!!.model = DefaultTreeModel(root)
    }


    /** Gets the list of projects associated with the current user from the server.

     * Will close the connection to the server and return on exception.

     * @return        a `List` of `Project` objects or null on unexpected failure.
     */
    //return new Vector<Project>();
    val projectList: List<Project>
        get() {
            var rv: List<*>? = null

            try {
                if (this.queryService == null)
                    this.queryService = this.serviceFactory!!.queryService

                val query_string = "select p from Project p left outer join fetch p.datasetLinks dsl left outer join fetch dsl.child ds where p.details.owner.omeName = :name"
                val p = ParametersI()
                p.add("id", rlong(1L))
                rv = queryService!!.findAllByQuery(
                        query_string,
                        ParametersI().add("name", rstring(this.username)))

            } catch (e: Exception) {
                LoggingUtilities.severe("encountered exception while reading datasets")
                this.client!!.closeSession()
            }

            val toReturn = java.util.ArrayList<Project>()
            for (prj in rv!!) {
                toReturn.add(prj as Project)
            }
            return toReturn
        }

    fun loadImages() {
        try {
            val n = this.obw.omeroBrowsingTree!!.selectionPath.lastPathComponent as DefaultMutableTreeNode
            val h = n.userObject as Holder
            if (!h.isADataset) {
                return
            }
            var d = (h as DatasetHolder).dataset
            d = this.gateway!!.getDataset(d.id.value, true)
            val images = d.linkedImageList()
            java.util.Collections.sort(images, ImageComparator())
            images.forEach {
                n.add(DefaultMutableTreeNode(ImageHolder(it)))
            }
            this.obw.omeroBrowsingTree!!.repaint()
        } catch (ex: ServerError) {
            LoggingUtilities.severe(ex.toString())
        }
    }

    fun getPassword(): String {
        val pw = String(this.password)
        nullifyPassword(this.password)
        return pw
    }

    /** A comparator for omero images, allowing sorting for display.

     */
    private class ImageComparator : java.util.Comparator<omero.model.Image>, java.io.Serializable {
        override fun compare(a: omero.model.Image, b: omero.model.Image): Int {
            return a.name.value.compareTo(b.name.value)
        }

        companion object {
            internal const val serialVersionUID = 1L
        }
    }

    /** A comparator for omero projects, allowing sorting for display.

     */
    private class ProjectComparator : java.util.Comparator<Project>, java.io.Serializable {
        override fun compare(a: Project, b: Project): Int {
            return a.name.value.compareTo(b.name.value)
        }

        companion object {
            internal const val serialVersionUID = 1L
        }
    }

    /** A comparator for omero datasets, allowing sorting for display.

     */
    private class DatasetComparator : java.util.Comparator<Dataset>, java.io.Serializable {
        override fun compare(a: Dataset, b: Dataset): Int {
            return a.name.value.compareTo(b.name.value)
        }

        companion object {
            internal const val serialVersionUID = 1L
        }
    }


    private open class Holder {
        var isADataset: Boolean = false
            internal set
        var isAnImage: Boolean = false
            internal set
        var isAProject: Boolean = false
            internal set
    }

    /** A class that holds a dataset and adds a toString method so these can be
     * used to populate the tree for display.
     */
    private class DatasetHolder(var dataset: Dataset) : Holder() {
        init {
            isADataset = true
            isAnImage = false
            isAProject = false
        }

        override fun toString(): String {
            return this.dataset.name.value
        }

    }

    private class ImageHolder(i_in: omero.model.Image) : Holder() {
        var image: omero.model.Image
            internal set

        init {
            this.image = i_in
            isADataset = false
            isAnImage = true
            isAProject = false
        }
        override fun toString(): String {
            return this.image.name.value
        }
    }

    private class ProjectHolder(p_in: Project) : Holder() {
        var project: Project
            internal set

        init {
            this.project = p_in
            isADataset = false
            isAnImage = false
            isAProject = true
        }

        override fun toString(): String {
            return this.project.name.value
        }
    }

    companion object {
        fun nullifyPassword(password: CharArray) {
            for (i in password.indices) {
                password[i] = 0.toChar()
            }
        }
    }
}
