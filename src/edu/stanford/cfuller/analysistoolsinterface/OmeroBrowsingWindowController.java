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

import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.ArrayList;
import omero.model.DatasetImageLink;
import javax.swing.tree.DefaultTreeModel;
import omero.model.ProjectDatasetLink;
import javax.swing.tree.DefaultMutableTreeNode;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import omero.ServerError;
import omero.api.GatewayPrx;
import omero.api.IQueryPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.Project;
import omero.sys.ParametersI;
import static omero.rtypes.*;

/**
 *
 * @author cfuller
 */
public class OmeroBrowsingWindowController {

    OmeroBrowsingWindow obw;

    List<Long> selectedImageIds;

    omero.client client;
    ServiceFactoryPrx serviceFactory;
    IQueryPrx queryService;
    GatewayPrx gateway;
    Dataset lastDatasetAccessed;

    OmeroListener listener;

    String username;
    String hostname;
    char[] password;
  

    public OmeroBrowsingWindowController() {
    }


    public void openNewBrowsingWindow(OmeroListener l) {

        this.listener = l;

        this.obw = new OmeroBrowsingWindow(this);

        this.obw.getOmeroBrowsingTree().setModel(new DefaultTreeModel(null));

        this.obw.setVisible(true);

        this.selectedImageIds = new ArrayList<Long>();

        this.client = null;
        this.serviceFactory = null;
        this.queryService = null;
        this.gateway = null;
        this.lastDatasetAccessed = null;

    }


    public void doneButtonPressed() {

        DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode) this.obw.getOmeroBrowsingTree().getSelectionPath().getLastPathComponent());

        Holder selected = (Holder) selectedNode.getUserObject();

        if (selected.isAnImage()) {
            this.selectedImageIds.add(((ImageHolder) selected).getImage().getId().getValue());
        }
        if (selected.isADataset()) {
            this.loadImages();
            
            Enumeration<DefaultMutableTreeNode> children = selectedNode.children();

            while(children.hasMoreElements()) {
                DefaultMutableTreeNode child = children.nextElement();

                ImageHolder ih = (ImageHolder) child.getUserObject();

                this.selectedImageIds.add(ih.getImage().getId().getValue());

            }


        }

        this.obw.dispose();

        this.listener.imageIdsHaveBeenSelected(this.selectedImageIds);

        this.client.closeSession();

    }

  

    public List<Long> getSelectedImageIds() {
        return this.selectedImageIds;
    }

    public void loginButtonPressed() {

        this.hostname = obw.getHost();
        this.username = obw.getUsername();
        this.password = obw.getPassword();

        if (this.client != null) {
            this.client.closeSession();
        }

        this.client = new omero.client(hostname);
        try {
            this.serviceFactory = this.client.createSession(username, String.valueOf(password));
            this.gateway = this.serviceFactory.createGateway();
        } catch (CannotCreateSessionException ex) {
            LoggingUtilities.severe(ex.getMessage());
        } catch (PermissionDeniedException ex) {
            LoggingUtilities.severe(ex.getMessage());
        } catch (ServerError ex) {
            LoggingUtilities.severe(ex.getMessage());
        }

        List<Project> projectList = this.getProjectList();

        java.util.Collections.sort(projectList, new ProjectComparator());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.username);

        for (Project p : projectList) {
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(new ProjectHolder(p));

            List<ProjectDatasetLink> pdLinks = p.copyDatasetLinks();

            List<Dataset> datasets = new ArrayList<Dataset>();

            for (ProjectDatasetLink pdl : pdLinks) {
                datasets.add(pdl.getChild());
            }

            java.util.Collections.sort(datasets, new DatasetComparator());

            for (Dataset d : datasets) {
                    DefaultMutableTreeNode dNode = new DefaultMutableTreeNode(new DatasetHolder(d));
                    pNode.add(dNode);
                    
            }

            root.add(pNode);

        }

        this.obw.getOmeroBrowsingTree().setModel(new DefaultTreeModel(root));


    }


    /** Gets the list of projects associated with the current user from the server.
     *
     * Will close the connection to the server and return on exception.
     *
     * @return	    a <code>List</code> of <code>Project</code> objects or null on unexpected failure.
     */
    public List<Project> getProjectList() {

	//return new Vector<Project>();

	List rv = null;

	try {

	    if (this.queryService == null)
		this.queryService = this.serviceFactory.getQueryService();

	    String query_string = "select p from Project p left outer join fetch p.datasetLinks dsl left outer join fetch dsl.child ds where p.details.owner.omeName = :name";
	    ParametersI p = new ParametersI();
	    p.add("id", rlong(1L));

	    rv = queryService.findAllByQuery(
		    query_string,
		    new ParametersI().add("name", rstring(this.username)));

	} catch (Exception e) {
            LoggingUtilities.severe("encountered exception while reading datasets");
	    this.client.closeSession();
	}


	return (List<Project>) rv;

    }

    public void loadImages() {
        try {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) (this.obw.getOmeroBrowsingTree().getSelectionPath().getLastPathComponent());
            Holder h = (Holder) (n.getUserObject());
            if (!h.isADataset()) {
                return;
            }
            Dataset d = ((DatasetHolder) h).getDataset();
            d = this.gateway.getDataset(d.getId().getValue(), true);
            List<omero.model.Image> images = d.linkedImageList();
            java.util.Collections.sort(images, new ImageComparator());
            for (omero.model.Image i : images) {
                n.add(new DefaultMutableTreeNode(new ImageHolder(i)));
            }

            this.obw.getOmeroBrowsingTree().repaint();

        } catch (ServerError ex) {
            LoggingUtilities.severe(ex.getMessage());
        }

    }

    public static void nullifyPassword(char[] password) {
        for (int i =0; i < password.length; i++) {
            password[i] = 0;
        }
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        String pw =  String.valueOf(this.password);
        nullifyPassword(this.password);
        return pw;
    }
    

    /** A comparator for omero images, allowing sorting for display.
     *
     */
    private class ImageComparator implements java.util.Comparator<omero.model.Image>, java.io.Serializable {

	public int compare(omero.model.Image a, omero.model.Image b) {

	    return a.getName().getValue().compareTo(b.getName().getValue());

	}

    }

    /** A comparator for omero projects, allowing sorting for display.
     *
     */
    private class ProjectComparator implements java.util.Comparator<Project>, java.io.Serializable {

	public int compare(Project a, Project b) {

	    return a.getName().getValue().compareTo(b.getName().getValue());

	}

    }

    /** A comparator for omero datasets, allowing sorting for display.
     *
     */
    private class DatasetComparator implements java.util.Comparator<Dataset>, java.io.Serializable {

	public int compare(Dataset a, Dataset b) {

	    return a.getName().getValue().compareTo(b.getName().getValue());

	}

    }


    protected static class Holder {

        boolean isDataset;
        boolean isImage;
        boolean isProject;

        public boolean isADataset() {return isDataset;}
        public boolean isAProject() {return isProject;}
        public boolean isAnImage() {return isImage;}
        
    }

    /** A class that holds a dataset and adds a toString method so these can be
     * used to populate the tree for display.
     */
    protected static class DatasetHolder extends Holder {

        Dataset d;

        

        public DatasetHolder(Dataset d_in) {this.d = d_in; isDataset = true; isImage = false; isProject = false;}

        public void setDataset(Dataset d) {this.d = d;}

        public Dataset getDataset() {return this.d;}

        @Override
        public String toString() {return this.d.getName().getValue();}

    }

    protected static class ImageHolder extends Holder{

    
        
        omero.model.Image i;

        public ImageHolder(omero.model.Image i_in) {this.i = i_in; isDataset = false; isImage = true; isProject = false;}
        public omero.model.Image getImage() {return this.i;}

        @Override
        public String toString() {return this.i.getName().getValue();}

    }

    protected static class ProjectHolder extends Holder {


        Project p;

        public ProjectHolder(Project p_in) {this.p = p_in; isDataset = false; isImage = false; isProject = true;}
        public Project getProject() {return this.p;}

        @Override
        public String toString() {return this.p.getName().getValue();}

    }
}
