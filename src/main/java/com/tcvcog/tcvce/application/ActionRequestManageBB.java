/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class ActionRequestManageBB extends BackingBeanUtils {
    
    private CEActionRequest currentRequest;
    
    private List<CEActionRequestStatus> statusList;
    private CEActionRequestStatus selectedStatus;
    
    private List<CEActionRequest> requestList;
    private int ceCaseIDForConnection;
    
    private SearchParamsCEActionRequests searchParams;
    
    // search stuff
    
    public void updateRequestList(ActionEvent ev){
        requestList = null;
        System.out.println("ActionRequestManagebb.updateRequestList");
        
    }
    
    
    public String manageActionRequest(CEActionRequest req){
        getSessionBean().setActionRequest(req);
        return "actionRequestManage";
        
    }

    /**
     * @return the ceCaseIDForConnection
     */
    public int getCeCaseIDForConnection() {
        return ceCaseIDForConnection;
    }

    /**
     * @param ceCaseIDForConnection the ceCaseIDForConnection to set
     */
    public void setCeCaseIDForConnection(int ceCaseIDForConnection) {
        this.ceCaseIDForConnection = ceCaseIDForConnection;
    }

    /**
     * @return the requestList
     */
    public List<CEActionRequest> getRequestList() {
        System.out.println("ActionRequestManageBB.getRequestList");
        
        CEActionRequestIntegrator ari = getcEActionRequestIntegrator();
        
        if(requestList == null || requestList.isEmpty()){
            System.out.println("CeActionRequestsBB.getUnlinkedRequestList | unlinkedrequests is null");
            try {
                requestList = ari.getCEActionRequestList(searchParams);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Unable to load action requests due to an error in the Integration Module", ""));
            }
        }
        return requestList;
    }

    /**
     * @param requestList the requestList to set
     */
    public void setRequestList(List<CEActionRequest> requestList) {
        this.requestList = requestList;
    }

   
    
    
    
    public void connectActionRequestToCECase(){
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        // TODO: create event if the CEAR is connected to a CECASE
        EventIntegrator ei = getEventIntegrator();
        
        try {
            ceari.connectActionRequestToCECase(currentRequest.getRequestID(), 
                    ceCaseIDForConnection, 
                    getFacesUser().getUserID());
            
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully linked action request ID: " 
                        + currentRequest.getRequestID() 
                        + " to code enforcement case ID: " + ceCaseIDForConnection
                        + "\n REFRESH your page to see the changes reflected in the action list", ""));
            // force a list reset
            requestList = null;
            
            // create case sevent
            
        } catch (IntegrationException ex) {
            // thrown if the integrator cannot find a CECase to link
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not connect action request to case-- database integration error", ""));
        } catch (CaseLifecyleException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }
    

    /**
     * Creates a new instance of ActionRequestManageBB
     */
    public ActionRequestManageBB() {
    
    }
    
    public String updateActionRequest(){
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        try {
            ceari.updateActionRequest(currentRequest);
        } catch (IntegrationException ex) {
            
            
        }
        return "missionControl";
        
    }
    
    public void changeActionRequestStatus(ActionEvent ev){
        
        
        
    }
    
    public void searchForCEActionRequests(ActionEvent ev){
        
        
    }
    
    

    /**
     * @return the currentRequest
     */
    public CEActionRequest getCurrentRequest() {
        currentRequest = getSessionBean().getActionRequest();
        return currentRequest;
    }

    /**
     * @param currentRequest the currentRequest to set
     */
    public void setCurrentRequest(CEActionRequest currentRequest) {
        this.currentRequest = currentRequest;
    }

    /**
     * @return the statusList
     */
    public List<CEActionRequestStatus> getStatusList() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if(statusList == null){
            try {
                statusList = ceari.getRequestStatusList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return statusList;
    }

    /**
     * @return the selectedStatus
     */
    public CEActionRequestStatus getSelectedStatus() {
        return selectedStatus;
    }

    /**
     * @param statusList the statusList to set
     */
    public void setStatusList(List<CEActionRequestStatus> statusList) {
        this.statusList = statusList;
    }

    /**
     * @param selectedStatus the selectedStatus to set
     */
    public void setSelectedStatus(CEActionRequestStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCEActionRequests getSearchParams() {
        System.out.println("ActionRequestManageBB.getSearchparams");
        if(searchParams == null){
            System.out.println("ActionRequestManageBB.getSearchparams | params is null");
            SearchCoordinator sc = getSearchCoordinator();
            searchParams = sc.getDefaultSearchParamsCEActionRequests();
        }
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEActionRequests searchParams) {
        this.searchParams = searchParams;
    }
    
}
