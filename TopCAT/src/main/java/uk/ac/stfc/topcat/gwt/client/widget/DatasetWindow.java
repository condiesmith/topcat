/**
 *
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddDatasetEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.WindowLogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddDatasetEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.DownloadManager;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;
import uk.ac.stfc.topcat.gwt.shared.DataSelectionType;
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.BufferView;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * This is a floating window widget, It shows list of datasets for a given
 * investigation.
 *
 * <p>
 *
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DatasetWindow extends Window {
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    private CheckBoxSelectionModel<DatasetModel> datasetSelectionModel = null;
    private ListStore<DatasetModel> datasetStore;
    private PagingModelMemoryProxy pageProxy = null;
    private PagingLoader<PagingLoadResult<DatasetModel>> loader = null;
    private PagingToolBar pageBar = null;
    private ToolBar toolBar;
    private int selectionCount = 0;
    private String facilityName;
    private String investigationId;
    private String investigationName;
    private TopcatInvestigation topcatInvestigation;
    boolean historyVerified;
    private boolean awaitingLogin;
    private boolean loadingData = false;
    private static final String SOURCE = "DatasetWindow";
    private Menu contextMenu;
    private MenuItem addDatafileMenuItem;
    private MenuItem downloadDatasetMenuItem;

    private Button btnShowAddDataset;

    /** Number of rows of data. */
    private static final int PAGE_SIZE = 20;

    public DatasetWindow() {
        // Listener called when the datafile window is closed.
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                // Go to page one and de-select everything in case we reuse this
                // window to display this data again
                loader.load(0, PAGE_SIZE);
                datasetSelectionModel.deselectAll();
                // Update the history to notify the close of dataset window
                EventPipeLine.getInstance().getHistoryManager().updateHistory();
            }
        });

        datasetSelectionModel = createDatasetSelectionModel();

        topcatInvestigation = new TopcatInvestigation();

        setHeadingText("");
        setLayout(new FillLayout(Orientation.HORIZONTAL));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        configs.add(datasetSelectionModel.getColumn());
        ColumnConfig clmncnfgName = new ColumnConfig("datasetName", "Data Set Name", 150);

        clmncnfgName.setAlignment(HorizontalAlignment.LEFT);
        configs.add(clmncnfgName);

        ColumnConfig clmncnfgStatus = new ColumnConfig("datasetStatus", "Status", 150);
        configs.add(clmncnfgStatus);

        ColumnConfig clmncnfgType = new ColumnConfig("datasetType", "Type", 150);
        configs.add(clmncnfgType);

        ColumnConfig clmncnfgDescription = new ColumnConfig("datasetDescription", "Description", 200);
        configs.add(clmncnfgDescription);

        // Pagination
        pageProxy = new PagingModelMemoryProxy(datasetStore);
        loader = new BasePagingLoader<PagingLoadResult<DatasetModel>>(pageProxy);
        loader.setRemoteSort(true);
        datasetStore = new ListStore<DatasetModel>(loader);
        datasetStore.sort("datasetName", Style.SortDir.ASC);

        // Grid
        final Grid<DatasetModel> grid = new Grid<DatasetModel>(datasetStore, new ColumnModel(configs));
        add(grid);
        grid.setBorders(true);
        grid.setToolTip("\"Double Click\" row to show data files, right click for more options");
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DatasetModel>>() {
            @Override
            public void handleEvent(GridEvent<DatasetModel> e) {
                ArrayList<DatasetModel> dsmList = new ArrayList<DatasetModel>();
                dsmList.add(e.getModel());
                EventPipeLine.getInstance().showDatafileWindowWithHistory(dsmList);
            }
        });

        grid.addPlugin(datasetSelectionModel);
        grid.setSelectionModel(datasetSelectionModel);

        // Context Menu
        contextMenu = new Menu();
        contextMenu.setWidth(200);
        contextMenu.addStyleName("context-menu");
        MenuItem showDS = new MenuItem();
        showDS.setText("Show Data Set Parameters");
        showDS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconShowDatasetParameter()));
        contextMenu.add(showDS);
        showDS.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (grid.getSelectionModel().getSelectedItem() != null) {
                    DatasetModel dsm = (DatasetModel) grid.getSelectionModel().getSelectedItem();
                    EventPipeLine.getInstance().showParameterWindowWithHistory(dsm.getFacilityName(), Constants.DATA_SET,
                            dsm.getId(), dsm.getName());
                }
            }
        });


        MenuItem showSize = new MenuItem();
        showSize.setText("Show Data Set Size");
        showSize.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));

        contextMenu.add(showSize);
        showSize.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (grid.getSelectionModel().getSelectedItem() != null) {
                    DatasetModel dsm = (DatasetModel) grid.getSelectionModel().getSelectedItem();
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                    topcatDataSelection.addDataset(new Long(dsm.getId()));

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(dsm.getFacilityName(), topcatDataSelection, DataSelectionType.DATASET);
                }
            }
        });


        MenuItem showFS = new MenuItem();
        showFS.setText("Show Data Files");
        showFS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconOpenDatafile()));
        contextMenu.add(showFS);
        showFS.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                ArrayList<DatasetModel> dsmList = new ArrayList<DatasetModel>();
                dsmList.add((DatasetModel) grid.getSelectionModel().getSelectedItem());
                EventPipeLine.getInstance().showDatafileWindowWithHistory(dsmList);
            }
        });


        downloadDatasetMenuItem = new MenuItem();
        downloadDatasetMenuItem.setText("Download this Data Set");
        downloadDatasetMenuItem.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconDownloadDataset()));
        downloadDatasetMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                DatasetModel dsm = (DatasetModel) grid.getSelectionModel().getSelectedItem();
                TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                topcatDataSelection.addDataset(new Long(dsm.getId()));

                DownloadManager.getInstance().downloadData(dsm.getFacilityName(), topcatDataSelection, dsm.getName(), IdsFlag.ZIP_AND_COMPRESS);
            }
        });


        //create add datafile content menu. Note: menu item is not added to the menu until setDataset is called as facility
        //name is not available until then
        addDatafileMenuItem = new MenuItem();
        addDatafileMenuItem.setText("Add Data File");
        addDatafileMenuItem.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconAddDatafile()));
        addDatafileMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                DatasetModel dsm = (DatasetModel) grid.getSelectionModel().getSelectedItem();

                EventPipeLine.getInstance().showUploadDatasetWindow(dsm.getFacilityName(),
                        dsm.getId(),
                        dsm,
                        SOURCE);
            }
        });

        grid.setContextMenu(contextMenu);

        BufferView view = new BufferView();
        view.setRowHeight(32);
        view.setForceFit(true);
        grid.setView(view);
        setSize(600, 400);


        toolBar = new ToolBar();

        Button btnDownloadInvestigation = new DownloadButton("Download this Investigation", AbstractImagePrototype.create(Resource.ICONS.iconDownloadInvestigation()),
                topcatInvestigation);
        btnDownloadInvestigation.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                downloadInvestigation(((DownloadButtonEvent) ce).getDownloadName());
            }
        });
        toolBar.add(btnDownloadInvestigation);

        Button btnDownloadDataset = new DownloadButton("Download Selected Data Sets", AbstractImagePrototype.create(Resource.ICONS.iconDownloadDataset()), ICATNodeType.DATASET, datasetSelectionModel);
        btnDownloadDataset.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                downloadDataset(((DownloadButtonEvent) ce).getDownloadName());
            }
        });
        toolBar.add(btnDownloadDataset);

        Button btnCheckSelectedSize = new Button("Check Selected Size", AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));
        btnCheckSelectedSize.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<DatasetModel> selectedModels = (ArrayList<DatasetModel>) getSelectedModels(pageProxy);

                if (selectedModels.size() > 0) {
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();

                    for(DatasetModel datasetModel : selectedModels){
                        topcatDataSelection.addDataset(new Long(datasetModel.getId()));
                    }

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(facilityName, topcatDataSelection, DataSelectionType.MIXED);
                } else {
                    EventPipeLine.getInstance().showMessageDialog("Nothing selected to check.");
                }
            }
        });

        toolBar.add(btnCheckSelectedSize);


        btnShowAddDataset = new Button("Add Data Set", AbstractImagePrototype.create(Resource.ICONS.iconAddDataset()));
        btnShowAddDataset.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                //we need a topcat investigation to be passed to the event.
                //TopcatInvestigation topcatInvestigation = new TopcatInvestigation(facilityName, facilityName, investigationId, investigationName, null, null, null, null);

                EventPipeLine.getInstance().showAddNewDatasetWindow(facilityName, investigationId, investigationName, topcatInvestigation);
            }
        });


        toolBar.add(new SeparatorToolItem());
        setTopComponent(toolBar);

        // Pagination Bar
        pageBar = new PagingToolBar(PAGE_SIZE) {
            @Override
            public void refresh() {
                super.refresh();
                loadData();
            }
        };
        pageBar.bind(loader);
        setBottomComponent(pageBar);
        awaitingLogin = false;
        createLoginHandler();
        createLogoutHandler();
        createAddDatasetHandler();
    }


    private void addUploadContextMenu(String facilityName, Menu contextMenu, MenuItem menuItem) {
        if (facilityName != null) {
            if (EventPipeLine.getInstance().hasUploadSupport(facilityName)) {
                contextMenu.add(menuItem);
            }
        }
    }


    private void addDownloadContextMenu(String facilityName, Menu contextMenu, MenuItem menuItem) {
        if (facilityName != null) {
            contextMenu.add(menuItem);
        }
    }


    private void addCreateDatasetButton(final String facilityName, ToolBar toolbar, Button btnShowAddDatasetWindow){
        //add add data set button if has create dataset support
        if (EventPipeLine.getInstance().hasCreateDatasetSupport(facilityName)) {
            toolBar.add(btnShowAddDatasetWindow);
        }
    }





    /**
     * This method sets the facility name and investigation id for this window.
     * using this information it contacts the server using GWT-RPC to get the
     * dataset list.
     *
     * @param facilityName
     * @param investigationId
     */

    public void setDataset(String facilityName, String investigationId) {
        this.facilityName = facilityName;
        this.investigationId = investigationId;

        topcatInvestigation.setFacilityName(facilityName);
        topcatInvestigation.setInvestigationId(investigationId);

        if (EventPipeLine.getInstance().getLoggedInFacilities().contains(facilityName)) {
            awaitingLogin = false;
            loadData();
        } else {
            awaitingLogin = true;
        }

        //add the upload file context menu as facilityName is set
        addUploadContextMenu(facilityName, getContextMenu(), getAddDatafileMenuItem());

        //add the download dataset context menu as facilityName is set
        addDownloadContextMenu(facilityName, getContextMenu(), getDownloadDatasetMenuItem());

        //add the add dataset button as facilityName is set
        addCreateDatasetButton(facilityName, getToolBar(), getBtnShowAddDataset());
    }


    public ToolBar getToolBar() {
        return toolBar;
    }


    public Menu getContextMenu() {
        return contextMenu;
    }


    public MenuItem getAddDatafileMenuItem() {
        return addDatafileMenuItem;
    }

    public MenuItem getDownloadDatasetMenuItem() {
        return downloadDatasetMenuItem;
    }


    public Button getBtnShowAddDataset() {
        return btnShowAddDataset;
    }

    /**
     * @return the facility name
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * @return the investigation id
     */
    public String getInvestigationId() {
        return investigationId;
    }

    /**
     * @return the investigation title information
     */
    public String getInvestigationTitle() {
        return investigationName;
    }

    /**
     * This method sets the investigation title of the window (Windows Header
     * information)
     *
     * @param investigationTitle
     */
    public void setInvestigationTitle(String investigationTitle) {
        investigationName = investigationTitle;
        topcatInvestigation.setInvestigationName(investigationTitle);
        setHeadingText("Investigation: " + investigationTitle);
    }

    /**
     * @return the history string for this window
     */
    public String getHistoryString() {
        String history = "";
        history += HistoryManager.seperatorModel + HistoryManager.seperatorToken + "Model"
                + HistoryManager.seperatorKeyValues + "Investigation";
        history += HistoryManager.seperatorToken + "ServerName" + HistoryManager.seperatorKeyValues + facilityName;
        history += HistoryManager.seperatorToken + "InvestigationId" + HistoryManager.seperatorKeyValues
                + investigationId;
        return history;
    }

    /**
     * Checks whether the given input information (facility name and
     * investigation id) matches with the window's information.
     *
     * @param FacilityName
     * @param InvestigationId
     * @return
     */
    public boolean isSameModel(String FacilityName, String InvestigationId) {
        if (facilityName.compareTo(FacilityName) == 0 && investigationId.compareTo(InvestigationId) == 0)
            return true;
        return false;
    }

    /**
     * @return whether the history is verified or not
     */
    public boolean isHistoryVerified() {
        return historyVerified;
    }

    /**
     * Set the window history verified status
     *
     * @param historyVerified
     */
    public void setHistoryVerified(boolean historyVerified) {
        this.historyVerified = historyVerified;
    }

    @Override
    public void show() {
        if (awaitingLogin) {
            return;
        }
        if (facilityName != null && !EventPipeLine.getInstance().getLoggedInFacilities().contains(facilityName)) {
            // trying to use/reuse window but we are not logged in
            awaitingLogin = true;
            return;
        }
        super.show();
    }

    /**
     * Check if the widget is in use by the given facility, i.e. waiting for the
     * user to log in or widget already visible.
     *
     * @param facilitName
     * @return true if the widget is in use
     */
    public boolean isInUse(String facilitName) {
        if (!facilityName.equals(facilitName)) {
            return false;
        } else {
            return isInUse();
        }
    }

    /**
     * Check if the widget is in use, i.e. waiting for the user to log in or
     * widget already visible.
     *
     * @return true if the widget is in use
     */
    public boolean isInUse() {
        if (awaitingLogin) {
            return true;
        }
        return isVisible();
    }

    /**
     * Clear out data ready for window reuse.
     */
    public void reset() {
        facilityName = "";
        investigationId = "";
        datasetStore.removeAll();
        selectionCount = 0;
        datasetSelectionModel.refresh();
        awaitingLogin = false;
    }

    /**
     * Get a customised CheckBoxSelectionModel
     *
     * @return a customised CheckBoxSelectionModel
     */
    private CheckBoxSelectionModel<DatasetModel> createDatasetSelectionModel() {
        CheckBoxSelectionModel<DatasetModel> dsSelectionModel = new CheckBoxSelectionModel<DatasetModel>() {
            private boolean allSelected = false;

            @SuppressWarnings("unchecked")
            @Override
            public void deselectAll() {
                super.deselectAll();
                if (pageProxy.getData() != null) {
                    for (DatasetModel m : (List<DatasetModel>) pageProxy.getData()) {
                        m.setSelected(false);
                    }
                }
                selectionCount = 0;
                allSelected = false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void selectAll() {
                super.selectAll();
                if (allSelected) {
                    // no need to loop through every item again
                    return;
                }
                for (DatasetModel m : (List<DatasetModel>) pageProxy.getData()) {
                    m.setSelected(true);
                }
                selectionCount = ((List<DatasetModel>) pageProxy.getData()).size();
                allSelected = true;
                setChecked(allSelected);
            }

            @Override
            protected void doDeselect(List<DatasetModel> models, boolean supressEvent) {
                super.doDeselect(models, supressEvent);
                if (supressEvent) {
                    return;
                }
                for (DatasetModel m : models) {
                    m.setSelected(false);
                    selectionCount = selectionCount - 1;
                }
                allSelected = false;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void doSelect(List<DatasetModel> models, boolean keepExisting, boolean supressEvent) {
                super.doSelect(models, keepExisting, supressEvent);
                if (supressEvent) {
                    return;
                }
                for (DatasetModel m : models) {
                    m.setSelected(true);
                    selectionCount = selectionCount + 1;
                }
                if (selectionCount == ((List<DatasetModel>) pageProxy.getData()).size()) {
                    allSelected = true;
                } else {
                    allSelected = false;
                }
                setChecked(allSelected);
            }

            @Override
            public void refresh() {
                super.refresh();
                List<DatasetModel> previouslySelected = new ArrayList<DatasetModel>();
                for (DatasetModel m : listStore.getModels()) {
                    if (m.getSelected()) {
                        previouslySelected.add(m);
                    } else {
                        allSelected = false;
                    }
                }
                if (previouslySelected.size() > 0) {
                    doSelect(previouslySelected, true, false);
                }
            }

            private void setChecked(boolean checked) {
                if (grid.isViewReady()) {
                    El hd = El.fly(grid.getView().getHeader().getElement()).selectNode("div.x-grid3-hd-checker");
                    if (hd != null) {
                        hd.getParent().setStyleName("x-grid3-hd-checker-on", checked);
                    }
                }
            }
        };

        return dsSelectionModel;
    }

    /**
     * This method shows the datafile window for the selected datasets.
     */
    private void viewDatafileWindow() {
        ArrayList<DatasetModel> selectedModels = (ArrayList<DatasetModel>) getSelectedModels(pageProxy);
        if (selectedModels.size() == 0) {
            EventPipeLine.getInstance().showMessageDialog("No datasets selected for viewing");
            return;
        }

        // Get all the datasets selected and show the datafile window
        EventPipeLine.getInstance().showDatafileWindowWithHistory(selectedModels);
    }


    @SuppressWarnings("unchecked")
    private List<DatasetModel> getSelectedModels(PagingModelMemoryProxy pageProxy) {
        List<DatasetModel> selectedModels = new ArrayList<DatasetModel>();
        for (DatasetModel model : (List<DatasetModel>) pageProxy.getData()) {
            if (model.getSelected()) {
                selectedModels.add(model);
            }
        }

        return selectedModels;
    }


    /**
     * Download investigation.
     *
     * @param downloadName
     *            the display name for the download
     */
    private void downloadInvestigation(String downloadName) {
        TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
        topcatDataSelection.addInvestigation(new Long(investigationId));

        DownloadManager.getInstance().downloadData(facilityName, topcatDataSelection, downloadName, IdsFlag.ZIP_AND_COMPRESS);
    }



    /**
     * Download datasets.
     *
     * @param downloadName
     *            the display name for the download
     */
    private void downloadDataset(String downloadName) {
        List<DatasetModel> selectedModels = (ArrayList<DatasetModel>) getSelectedModels(pageProxy);

        TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
        for(DatasetModel dataset : selectedModels) {
            topcatDataSelection.addDataset(new Long(dataset.getId()));
        }

        DownloadManager.getInstance().downloadData(facilityName, topcatDataSelection, downloadName, IdsFlag.ZIP_AND_COMPRESS);
    }



    /**
     * Call the server to get fresh data.
     */
    private void loadData() {
        if (loadingData) {
            return;
        }
        loadingData = true;
        EventPipeLine.getInstance().showRetrievingData();
        utilityService.getDatasetsInInvestigations(facilityName, investigationId,
                new AsyncCallback<ArrayList<DatasetModel>>() {
                    @Override
                    public void onSuccess(ArrayList<DatasetModel> result) {
                        EventPipeLine.getInstance().hideRetrievingData();
                        if (result.size() > 0) {
                            setDatasetList(result);
                            show();
                            EventPipeLine.getInstance().getHistoryManager().updateHistory();
                        } else {
                            EventPipeLine.getInstance().showMessageDialog("No datasets returned");
                            hide();
                            reset();
                        }
                        loadingData = false;
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        EventPipeLine.getInstance().hideRetrievingData();
                        hide();
                        reset();
                        loadingData = false;
                        if (caught instanceof SessionException) {
                            // session has probably expired, check all sessions
                            // to be safe
                            EventPipeLine.getInstance().checkStillLoggedIn();
                        } else {
                            EventPipeLine.getInstance().showErrorDialog("Error retrieving dataset");
                        }
                    }
                });
    }

    /**
     * This method sets the dataset list in the window
     *
     * @param datasetsList
     */
    private void setDatasetList(ArrayList<DatasetModel> datasetsList) {
        datasetStore.removeAll();
        selectionCount = 0;
        pageProxy.setData(datasetsList);
        loader.load(0, PAGE_SIZE);
        pageBar.refresh();
        if (datasetsList.size() == 1) {
            datasetSelectionModel.selectAll();
            viewDatafileWindow();
        }
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                if (awaitingLogin && event.getFacilityName().equals(facilityName)) {
                    awaitingLogin = false;
                    loadData();
                }
            }
        });
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                if (isInUse() && facilityName.equals(event.getFacilityName())) {
                    // When we open a web page with a url a status check is done
                    // on all facilities. We do not want this to remove this
                    // window. However when the user presses the cancel button
                    // on the login widget we do want to remove this window.
                    if (!event.isStatusCheck() || isVisible()) {
                        reset();
                    }
                    hide();
                    EventPipeLine.getEventBus().fireEventFromSource(new WindowLogoutEvent(event.getFacilityName()),
                            event.getFacilityName());
                }
            }
        });
    }


    /**
     *  Setup handler for addd data file
     */
    private void createAddDatasetHandler() {
        AddDatasetEvent.register(EventPipeLine.getEventBus(), new AddDatasetEventHandler() {

            @Override
            public void addDataset(AddDatasetEvent event) {
                if (event.getNode() != null) {
                    TopcatInvestigation investigation = (TopcatInvestigation) event.getNode();

                    if (investigation.getInvestigationId().equals(investigationId)) {
                        datasetSelectionModel.refresh();
                        loadData();
                    }
                }
            }
        });
    }



}
