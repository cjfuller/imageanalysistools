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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author cfuller
 *
 */
public class FilterSelectionFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	static List<String> filters;
	static Map<String, String> filterLookupByName;
	
	ParameterSetupController psc;
	JList parameterList;
	
	static final String FILTERS_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/filters.xml";
	static final String FILTER_TAG_NAME = "filter";
	static final String NAME_ATTR = "displayname";
	static final String CLASS_ATTR = "class";
	
	static {
		
		filters = new ArrayList<String>();
		filterLookupByName = new HashMap<String, String>();
		
		populateFilterList(FILTERS_XML_FILENAME, true);
		
	}
	
	protected void addAdditionalFilters() {
		JFileChooser jfc = new JFileChooser(Preferences.userNodeForPackage(this.getClass()).get("additional_filters_filename", ""));
		jfc.setMultiSelectionEnabled(false);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = jfc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			String filename = jfc.getSelectedFile().getAbsolutePath();
			Preferences.userNodeForPackage(this.getClass()).put("additional_filters_filename", filename);
			populateFilterList(filename, false);
			parameterList.setListData(filters.toArray());
		}
	}
	
	public FilterSelectionFrame(ParameterSetupController pscIn) {
		setPreferredSize(new Dimension(400, 300));
		setBounds(new Rectangle(0, 22, 400, 300));
				
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.psc = pscIn;
		
		JScrollPane scrollPane = new JScrollPane();
		
		JLabel lblLoadAdditionalFilters = new JLabel("Load additional filters from an XML file:");
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addAdditionalFilters();
			}
		});
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblLoadAdditionalFilters)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLoad)
					.addContainerGap(64, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblLoadAdditionalFilters)
						.addComponent(btnLoad))
					.addContainerGap(11, Short.MAX_VALUE))
		);
		
		parameterList = new JList();
		parameterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String selected = (String) parameterList.getSelectedValue();
				psc.addSelectedFilter(selected, filterLookupByName.get(selected));
				setVisible(false);
				dispose();
			}
		});
		scrollPane.setViewportView(parameterList);
		getContentPane().setLayout(groupLayout);
		parameterList.setListData(filters.toArray());
	}
	
	protected static void populateFilterList(String filename, boolean isResource) {
		
        Document taskDoc = null;

        String taskURLString = null;
        
        if (isResource) {
	        if (ij.IJ.getInstance() != null) {
	        	taskURLString = ij.IJ.getClassLoader().getResource(filename).toString();
	        } else {
	        	taskURLString = ClassLoader.getSystemClassLoader().getResource(filename).toString();
	        }
        } else {
        	taskURLString = filename;
        }
        
        try {
            taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskURLString);
        } catch (SAXException e) {
            LoggingUtilities.severe("Encountered exception while parsing filters xml file.");
            e.printStackTrace();
            return;
        } catch (ParserConfigurationException e) {
            LoggingUtilities.severe("Incorrectly configured xml parser.");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            LoggingUtilities.severe("Exception while reading xml file.");
            e.printStackTrace();
            return;
        }

        NodeList tasks = taskDoc.getElementsByTagName(FILTER_TAG_NAME);

        for (int i = 0; i < tasks.getLength(); i++) {
            
            Node n = tasks.item(i);

            String filterName = n.getAttributes().getNamedItem(NAME_ATTR).getNodeValue();

            String filterClass = n.getAttributes().getNamedItem(CLASS_ATTR).getNodeValue();

            filterLookupByName.put(filterName, filterClass);
            
            filters.add(filterName);


        }
    }
}
