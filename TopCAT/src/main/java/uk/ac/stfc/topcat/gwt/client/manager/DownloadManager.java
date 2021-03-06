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
package uk.ac.stfc.topcat.gwt.client.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.RootPanel;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.DownloadService;
import uk.ac.stfc.topcat.gwt.client.DownloadServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddMyDownloadEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.UpdateDownloadStatusEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.shared.DataSelectionType;
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;
import uk.ac.stfc.topcat.gwt.shared.Utils;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;

public class DownloadManager {
    private final DownloadServiceAsync downloadService = DownloadService.Util.getInstance();
    /** Items waiting to be downloaded. */
    private Set<DownloadModel> downloadQueue = new HashSet<DownloadModel>();
    private int downloadCount = 0;
    private boolean statusErrorVisible = false;
    private EventPipeLine eventPipeLine = EventPipeLine.getInstance();
    private EventBus eventBus = EventPipeLine.getEventBus();

    /** An instance of this class. */
    private static DownloadManager downloadManager = new DownloadManager();

    /**
     * Private constructor to make a singleton.
     */
    private DownloadManager() {
        // Create a new timer that calls getDownloadStatus() on the server.
        Timer t = getDownloadStatusTimer();
        // Schedule the timer to run every 30 seconds.
        t.scheduleRepeating(30000);

        createLoginHandler();
        createLogoutHandler();
    }

    /**
     * Get the DownloadManager.
     *
     * @return an instance of the DownloadManager
     */
    public static DownloadManager getInstance() {
        return downloadManager;
    }

    /**
     * Initiate a download in a separate window.
     *
     * @param facilityName
     *            a string containing the facility name
     * @param url
     *            a string containing the url to contact
     */
    public void download(String facilityName, final String url) {
        if (!eventPipeLine.getLoggedInFacilities().contains(facilityName)) {
            // session logged out so do not download
            return;
        }
        DOM.setElementAttribute(RootPanel.get("__download" + downloadCount).getElement(), "src", url);
        downloadCount = downloadCount + 1;
        if (downloadCount > (Constants.MAX_DOWNLOAD_FRAMES - 1)) {
            downloadCount = 0;
        }
    }

    /**
     * Download all the data files from the facility for the given ids.
     *
     * @param facilityName
     *            a string containing the facility name
     * @param datafileList
     *            a list containing data file ids
     * @param downloadName
     *            a string containing a user defined name to give the download
     *            file
     */
    /*
    public void downloadDatafiles(final String facilityName, final TopcatDataSelection dataSelection, final String downloadName, IdsFlag flag) {
        ArrayList<TFacility> facilities = eventPipeLine.getFacilities();
        for (TFacility facility : facilities) {
            if (facility.getName().equalsIgnoreCase(facilityName)) {
                if (facility.getDownloadPluginName() != null
                        && facility.getDownloadPluginName().equalsIgnoreCase("ids")) {
                    if (facility.getDownloadTypeName().equalsIgnoreCase("archived")) {
                        prepareDataObjectsForDownloadIDS(Constants.DATA_FILE, facility, dataSelection, downloadName);
                    } else if (facility.getDownloadTypeName().equalsIgnoreCase("local")) {
                        directDownloadFromIDS(Constants.DATA_FILE, facility, dataSelection, downloadName, flag);
                    } else {
                        //use prepared if download type not set. Shouldn't get here
                        prepareDataObjectsForDownloadIDS(Constants.DATA_FILE, facility, dataSelection, downloadName);
                    }
                }

            }
        }
    }
    */

    /**
     * Download the data set from the facility.
     *
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @param downloadName
     *            a string containing a user defined name to give the download
     *            file
     */
    /*
    public void downloadDataset(final String facilityName, final TopcatDataSelection dataSelection, final String downloadName, IdsFlag flag) {

        ArrayList<TFacility> facilities = eventPipeLine.getFacilities();
        for (TFacility facility : facilities) {
            if (facility.getName().equalsIgnoreCase(facilityName)) {
                if (facility.getDownloadPluginName() != null
                        && facility.getDownloadPluginName().equalsIgnoreCase("ids")) {

                    if (facility.getDownloadTypeName().equalsIgnoreCase("prepared")) {
                        prepareDataObjectsForDownloadIDS(Constants.DATA_SET, facility, dataSelection, downloadName);
                    } else if (facility.getDownloadTypeName().equalsIgnoreCase("direct")) {
                        directDownloadFromIDS(Constants.DATA_SET, facility, dataSelection, downloadName, flag);
                    } else {
                        //use prepared if download type not set. Shouldn't get here
                        prepareDataObjectsForDownloadIDS(Constants.DATA_SET, facility, dataSelection, downloadName);
                    }
                }
            }
        }
    }
    */


    public void downloadData(final String facilityName, TopcatDataSelection dataSelection, final String downloadName, IdsFlag flag) {
        ArrayList<TFacility> facilities = eventPipeLine.getFacilities();

        for (TFacility facility : facilities) {
            if (facility.getName().equalsIgnoreCase(facilityName)) {
                if (facility.getDownloadPluginName() != null && facility.getDownloadPluginName().equalsIgnoreCase("ids")) {
                    if (facility.getDownloadTypeName().equalsIgnoreCase(Constants.DOWNLOAD_TYPE_ARCHIVE)) {
                        prepareDataObjectsForDownloadIDS(facility, dataSelection, downloadName, flag);
                    } else if (facility.getDownloadTypeName().equalsIgnoreCase(Constants.DOWNLOAD_TYPE_LOCAL)) {
                        directDownloadFromIDS(facility, dataSelection, downloadName, flag);
                    } else {
                        eventPipeLine.showErrorDialog("Unknown download type " + facility.getDownloadTypeName() + ". Please check ids configuration");
                    }
                }
            }
        }
    }


    /**
     * Call out to the server to get the list of downloads available for the
     * given facilities.
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>UpdateDownloadStatusEvent</code></dd>
     * </dl>
     *
     * @param facilities
     *            a list of facility names
     */
    public void getMyDownloads(Set<String> facilities) {
        if (facilities.size() == 0) {
            return;
        }
        eventPipeLine.showRetrievingData();
        downloadService.getMyDownloads(facilities, new AsyncCallback<List<DownloadModel>>() {
            @Override
            public void onFailure(Throwable caught) {
                eventPipeLine.hideRetrievingData();
                if (caught instanceof SessionException) {
                    eventPipeLine.checkStillLoggedIn();
                } else if (caught instanceof InternalException) {
                    showErrorDialog(caught.getMessage());
                } else {
                    showErrorDialog("Error retrieving download history from server. " + caught.getMessage());
                }
            }

            @Override
            public void onSuccess(List<DownloadModel> result) {
                eventPipeLine.hideRetrievingData();
                eventBus.fireEvent(new UpdateDownloadStatusEvent(result));
            }
        });
    }

    /**
     * Call out to the server to delete a download
     *
     * @param facility
     *            name of the facility
     *        downloadModel
     *            the downlod to delete
     */
    public void deleteDownload(String facilityName, DownloadModel downloadModel) {
        eventPipeLine.showRetrievingData();
        downloadService.deleteDownload(facilityName, downloadModel, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                eventPipeLine.hideRetrievingData();
                EventPipeLine.getInstance().showErrorDialog("Error deleting download: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean result) {
                eventPipeLine.hideRetrievingData();
                if (result) {
                    eventPipeLine.getMainWindow().getMainPanel().getMyDownloadPanel().refresh();
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Failed to delete download");
                }
            }
        });
    }


    public void getDataSelectionSize(String facilityName, TopcatDataSelection topcatDataSelection, final DataSelectionType dataSelectionType) {
        eventPipeLine.showRetrievingData();

        TFacility facility = EventPipeLine.getInstance().getFacility(facilityName);

        downloadService.getDataSelectionSize(facility, topcatDataSelection, new AsyncCallback<Long>() {
            @Override
            public void onFailure(Throwable caught) {
                eventPipeLine.hideRetrievingData();
                EventPipeLine.getInstance().showErrorDialog(caught.getMessage());
            }

            @Override
            public void onSuccess(Long size) {
                eventPipeLine.hideRetrievingData();
                String message = null;

                if (dataSelectionType == DataSelectionType.MIXED) {
                    message = "Selection is " + Utils.byteCountToDisplaySizeForClient(size, false) + ".";
                } else if (dataSelectionType == DataSelectionType.INVESTIGATION) {
                    message = "Investigation is " + Utils.byteCountToDisplaySizeForClient(size, false) + ".";
                } else if (dataSelectionType == DataSelectionType.DATASET) {
                    message = "Data Set is " + Utils.byteCountToDisplaySizeForClient(size, false) + ".";
                } else if (dataSelectionType == DataSelectionType.DATAFILE) {
                    message = "Data File is " + Utils.byteCountToDisplaySizeForClient(size, false) + ".";
                }

                EventPipeLine.getInstance().showMessageDialog(message);
            }
        });


    }


    /**
     * Contact the I.D.S. and prepare the download of the given data objects.
     *
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>AddMyDownloadEvent</code></dd>
     * </dl>
     *
     * @param dataType
     *            the type of the data object to be downloaded
     * @param facility
     *            the facility data
     * @param dataObjectList
     *            a list of data object ids
     * @param downloadName
     *            the name to give the download file
     */
    private void directDownloadFromIDS(final TFacility facility, final TopcatDataSelection dataSelection, final String downloadName, IdsFlag flag) {
        downloadService.directDownloadFromIDS(facility, dataSelection, downloadName, flag, new AsyncCallback<DownloadModel>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof SessionException) {
                    eventPipeLine.checkStillLoggedIn();
                } else if (caught instanceof InternalException) {
                    showErrorDialog(caught.getMessage());
                } else {
                    showErrorDialog("Error requesting the download. " + caught.getMessage());
                }
            }

            @Override
            public void onSuccess(DownloadModel result) {
                //trigger the download in __directdownload iframe
                if(!result.getUrl().isEmpty()) {
                    DOM.setElementAttribute(RootPanel.get("__directdownload").getElement(), "src", result.getUrl());
                    //Window.alert(result.getUrl());
                } else {
                    showErrorDialog("Error retrieving download url from the ids");
                }
            }
        });
    }


    /**
     * Contact the I.D.S. and prepare the download of the given data objects.
     *
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>AddMyDownloadEvent</code></dd>
     * </dl>
     *
     * @param dataType
     *            the type of the data object to be downloaded
     * @param facility
     *            the facility data
     * @param dataObjectList
     *            a list of data object ids
     * @param downloadName
     *            the name to give the download file
     */
    private void prepareDataObjectsForDownloadIDS(final TFacility facility,
            final TopcatDataSelection dataSelection, final String downloadName, IdsFlag flag) {
        downloadService.prepareDataObjectsForDownload(facility, dataSelection, downloadName, flag,
                new AsyncCallback<DownloadModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof SessionException) {
                            eventPipeLine.checkStillLoggedIn();
                        } else if (caught instanceof InternalException) {
                            showErrorDialog(caught.getMessage());
                        } else {
                            showErrorDialog("Error requesting the download. " + caught.getMessage());
                        }
                    }

                    @Override
                    public void onSuccess(DownloadModel result) {
                        ArrayList<DownloadModel> downloadModels = new ArrayList<DownloadModel>();
                        downloadModels.add(result);
                        eventBus.fireEventFromSource(new AddMyDownloadEvent(facility.getName(), downloadModels),
                                facility.getName());
                        downloadQueue.add(result);
                    }
                });
    }




    /**
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>AddMyDownloadEvent</code></dd>
     * </dl>
     *
     * @param facilityName
     * @param datafileList
     * @param downloadName
     */
    /*
    @Deprecated
    private void downloadDatafilesRDS(final String facilityName, final List<Long> datafileList,
            final String downloadName) {
        downloadService.getDatafilesDownloadURL(facilityName, (ArrayList<Long>) datafileList, downloadName,
                new AsyncCallback<DownloadModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof SessionException) {
                            eventPipeLine.checkStillLoggedIn();
                        } else if (caught instanceof InternalException) {
                            showErrorDialog(caught.getMessage());
                        } else {
                            showErrorDialog("Error retrieving data from server. " + caught.getMessage());
                        }
                    }

                    @Override
                    public void onSuccess(DownloadModel result) {
                        ArrayList<DownloadModel> downloadModels = new ArrayList<DownloadModel>();
                        downloadModels.add(result);
                        eventBus.fireEventFromSource(new AddMyDownloadEvent(facilityName, downloadModels), facilityName);
                        if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                            downloadQueue.add(result);
                        } else {
                            download(facilityName, result.getUrl());
                        }
                    }
                });
    }
    */

    /**
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>AddMyDownloadEvent</code></dd>
     * </dl>
     *
     * @param facilityName
     * @param datasetId
     * @param downloadName
     */
    /*
    @Deprecated
    private void downloadDatasetsRDS(final String facilityName, Long datasetId, String downloadName) {
        downloadService.getDatasetDownloadURL(facilityName, datasetId, downloadName,
                new AsyncCallback<DownloadModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof SessionException) {
                            eventPipeLine.checkStillLoggedIn();
                        } else if (caught instanceof InternalException) {
                            showErrorDialog(caught.getMessage());
                        } else {
                            showErrorDialog("Error retrieving data from server. " + caught.getMessage());
                        }
                    }

                    @Override
                    public void onSuccess(DownloadModel result) {
                        ArrayList<DownloadModel> downloadModels = new ArrayList<DownloadModel>();
                        downloadModels.add(result);
                        eventBus.fireEventFromSource(new AddMyDownloadEvent(facilityName, downloadModels), facilityName);
                        if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                            downloadQueue.add(result);
                        } else {
                            download(facilityName, result.getUrl());
                        }
                    }
                });
    }
    */

    /**
     * Create a new timer that calls getDownloadStatus() on the server.
     *
     * @return a timer that call getDownloadStatus on the server
     */
    private Timer getDownloadStatusTimer() {
        Timer t = new Timer() {
            @Override
            public void run() {
                for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                    DownloadModel download = it.next();

                    // remove items if we have logged out of the facility
                    if (!eventPipeLine.getLoggedInFacilities().contains(download.getFacilityName())) {
                        it.remove();
                    }

                    //remove item with error or expired status
                    if (download.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR) || download.getStatus().equalsIgnoreCase(Constants.STATUS_EXPIRED) || download.getStatus().equalsIgnoreCase(Constants.STATUS_IDS_ERROR)) {
                        it.remove();
                    }
                }

                if (downloadQueue.size() == 0) {
                    // nothing to check
                    return;
                }

                // call the server if there's something in the queue and check updates for items in the queue
                downloadService.checkForFinalStatus(downloadQueue, new AsyncCallback<List<DownloadModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof StatusCodeException) {
                            // There were problems communicating with the
                            // server, 200 was not returned. As this is on a
                            // timer we do not want to keep on spamming the user
                            // with error messages every n seconds.
                            return;
                        }
                        if (caught instanceof InternalException) {
                            // There were problems communicating with the
                            // server. As this is on a timer we do not want to
                            // keep on spamming the user with error messages
                            // every n seconds.
                            return;
                        }

                        for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                            DownloadModel download = it.next();

                            // remove items if we have logged out of the facility
                            if (!eventPipeLine.getLoggedInFacilities().contains(download.getFacilityName())) {
                                it.remove();
                            }
                        }

                        if (downloadQueue.size() > 0) {
                            // Only show error message if a message is not already being displayed
                            if (statusErrorVisible != true) {
                                statusErrorVisible = true;
                                String msg = "Error retrieving data from server while trying to get download status: " + caught.toString();

                                MessageBox.alert("Error", msg, new Listener<MessageBoxEvent>() {
                                    public void handleEvent(MessageBoxEvent be) {
                                        statusErrorVisible = false;
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onSuccess(List<DownloadModel> finalStateReached) {
                        boolean refresh = false;

                        for (DownloadModel download : finalStateReached) {
                            // remove done item from the list
                            downloadQueue.remove(download);

                            if (!eventPipeLine.getLoggedInFacilities().contains(download.getFacilityName())) {
                                // session logged out so do not process this download
                                continue;
                            }
                            if (download.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)) {
                                download(download.getFacilityName(), download.getUrl());
                            }

                            refresh = true;
                        }

                        if (refresh) {
                            eventPipeLine.getMainWindow().getMainPanel().getMyDownloadPanel().refresh();
                        }
                    }
                });
            }
        };
        return t;
    }

    /**
     * Show an alert dialog box.
     *
     * @param msg
     *            message in the dialog box
     */
    private void showErrorDialog(String msg) {
        MessageBox.alert("Error", msg, null);
    }

    /**
     * Call out to the server to get the list of downloads belonging to the user
     * for the given facility. If any are in a state of in progress, add them to
     * the downloadQueue.
     * <dl>
     * <dt>Fires:</dt>
     * <dd> <code>AddMyDownloadEvent</code></dd>
     * </dl>
     *
     * @param facilityName
     *            a string containing the facility name
     */
    private void initDownloadData(final String facilityName) {
        eventPipeLine.showRetrievingData();
        Set<String> facilityNames = new HashSet<String>();
        facilityNames.add(facilityName);
        downloadService.getMyDownloads(facilityNames, new AsyncCallback<List<DownloadModel>>() {
            @Override
            public void onFailure(Throwable caught) {
                eventPipeLine.hideRetrievingData();
                if (caught instanceof SessionException) {
                    eventPipeLine.checkStillLoggedIn();
                } else if (caught instanceof InternalException) {
                    showErrorDialog(caught.getMessage());
                } else {
                    showErrorDialog("Error retrieving download history from server for " + facilityName);
                }
            }

            @Override
            public void onSuccess(List<DownloadModel> result) {
                eventBus.fireEventFromSource(new AddMyDownloadEvent(facilityName, result), facilityName);
                eventPipeLine.hideRetrievingData();
                // Check for downloads that are 'in progress' as we need to add
                // them to the download queue
                for (Iterator<DownloadModel> it = result.iterator(); it.hasNext();) {
                    if (!it.next().getStatus().equals(Constants.STATUS_IN_PROGRESS)) {
                        it.remove();
                    }
                }
                if (result.size() > 0) {
                    downloadQueue.addAll(result);
                }
            }
        });
    }

    /**
     * Setup a handler to react to login events for the facility.
     */
    private void createLoginHandler() {
        // react to user logging into a facility
        LoginEvent.register(eventBus, new LoginEventHandler() {
            @Override
            public void login(final LoginEvent event) {
                initDownloadData(event.getFacilityName());
            }
        });
    }

    /**
     * Setup a handler to react to logout events for the facility.
     */
    private void createLogoutHandler() {
        // react to user logging out of a facility
        LogoutEvent.register(eventBus, new LogoutEventHandler() {
            @Override
            public void logout(final LogoutEvent event) {
                // remove items from the download queue
                for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                    if (it.next().getFacilityName().equals(event.getFacilityName())) {
                        it.remove();
                    }
                }
            }
        });
    }

}
