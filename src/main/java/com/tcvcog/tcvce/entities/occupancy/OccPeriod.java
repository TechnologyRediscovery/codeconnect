/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleOccPeriod;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccPeriod;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tcvcog.tcvce.entities.Openable;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.util.Collections;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_ProposalDriven;

/**
 * Primary Business Object BOB for holding data about Occupancy Periods
 * @author Ellen Baskem
 */
public class OccPeriod 
        extends EntityUtils 
        implements  Serializable,
                    Openable,
                    IFace_EventRuleGoverned,
                    IFace_ProposalDriven{
    
    private int periodID;
    private int propertyUnitID;
    private OccPeriodType type;
    private OccPeriodStatusEnum status;
    
    private boolean readyForPeriodAuthorization;
    
    private List<OccPermitApplication> applicationList;
    private List<PersonOccPeriod> personList;
    
    private List<Event> eventList;
    private List<Proposal> proposalList;
    private List<EventRuleImplementation> eventRuleList;
    
    private OccInspection governingInspection;
    private List<OccInspection> inspectionList;
    private List<OccPermit> permitList;
    private List<Integer> blobIDList;
    
    private List<Payment> paymentList;
    
    private User manager;
     
    private User periodTypeCertifiedBy;
    private LocalDateTime periodTypeCertifiedTS;
    
    private BOBSource source;
    private User createdBy;
    private LocalDateTime createdTS;
    
    private LocalDateTime startDate;
    private java.util.Date startDateUtilDate;
    private LocalDateTime startDateCertifiedTS;
    private User startDateCertifiedBy;
    
    private LocalDateTime endDate;
    private java.util.Date endDateUtilDate;
    private LocalDateTime endDateCertifiedTS;
    private User endDateCertifiedBy;
    
    private LocalDateTime authorizedTS;
    private User authorizedBy;
    
    private boolean overrideTypeConfig;
    
    private String notes;
    

    @Override
    public void setEventList(List<Event> lst) {
        eventList = lst;
    }

    
     @Override
    public boolean isOpen() {
        return status.isOpenPeriod();
    }

    @Override
    public boolean isAllRulesPassed() {
        boolean allPassed = true;
        for(EventRuleImplementation er: eventRuleList){
            if(er.getPassedRuleTS() == null){
                allPassed = false;
                break;
            }
        }
        return allPassed;
    }

    @Override
    public void setEventRuleList(List<EventRuleImplementation> lst) {
        eventRuleList = lst;
    }

    @Override
    public List<Event> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle) {
        List<Event> visEventList = new ArrayList<>();
        if(eventList != null){
            for (Event ev : eventList) {
                switch(voahle){
                    case VIEW_ACTIVE_HIDDEN:
                        if (ev.isActive()
                                && ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (ev.isActive()
                                && !ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ALL:
                        visEventList.add(ev);
                        break;
                    case VIEW_INACTIVE:
                        if (!ev.isActive()) {
                            visEventList.add(ev);
                        }
                        break;
                    default:
                        visEventList.add(ev);
                } // close switch
            } // close for   
        } // close null check
        return visEventList;
    }

    @Override
    public List<EventRuleImplementation> assembleEventRuleList(ViewOptionsEventRulesEnum voere) {
        List<EventRuleImplementation> evRuleList = new ArrayList<>();
        if(eventRuleList != null){
            for(EventRuleImplementation eri: eventRuleList){
                switch(voere){
                    case VIEW_ACTIVE_NOT_PASSED:
                        if(eri.isActiveRuleImp()
                                && eri.getPassedRuleTS() == null){
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ACTIVE_PASSED:
                        if(eri.isActiveRuleImp()
                                && eri.getPassedRuleTS() != null){
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ALL:
                        evRuleList.add(eri);
                        break;
                    case VIEW_INACTIVE:
                        if(!eri.isActiveRuleImp()){
                            evRuleList.add(eri);
                        }
                        break;
                    default:
                        evRuleList.add(eri);
                } // close switch
            } // close loop
        } // close null check
        return evRuleList;
    }
    

    @Override
    public List<Proposal> assembleProposalList(ViewOptionsProposalsEnum vope) {
        List<Proposal> proposalListVisible = new ArrayList<>();
        if(proposalList != null && !proposalList.isEmpty()){
            for(Proposal p: proposalList){
                switch(vope){
                    case VIEW_ALL:
                        proposalListVisible.add(p);
                        break;
                    case VIEW_ACTIVE_HIDDEN:
                        if(p.isActive() 
                                && p.isHidden()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if(p.isActive() 
                                && !p.isHidden()
                                && !p.getDirective().isRefuseToBeHidden()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_EVALUATED:
                        if(p.getResponseTS() != null){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_INACTIVE:
                        if(!p.isActive()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_NOT_EVALUATED:
                        if(p.getResponseTS() == null){
                            proposalListVisible.add(p);
                        }
                        break;
                    default:
                        proposalListVisible.add(p);
                } // switch
            } // for
        } // if
        return proposalListVisible;
    }

    
    /**
     * @return the periodID
     */
    public int getPeriodID() {
        return periodID;
    }

    /**
     * @return the propertyUnitID
     */
    public int getPropertyUnitID() {
        return propertyUnitID;
    }

    /**
     * @return the applicationList
     */
    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }

    /**
     * @return the personList
     */
    public List<PersonOccPeriod> getPersonList() {
        return personList;
    }


    /**
     * @return the proposalList
     */
    public List<Proposal> getProposalList() {
        return proposalList;
    }

    /**
     * @return the inspectionList
     */
    public List<OccInspection> getInspectionList() {
        return inspectionList;
    }

    /**
     * @return the permitList
     */
    public List<OccPermit> getPermitList() {
        return permitList;
    }

    /**
     * @return the blobIDList
     */
    public List<Integer> getBlobIDList() {
        return blobIDList;
    }

    /**
     * @return the manager
     */
    public User getManager() {
        return manager;
    }

    /**
     * @return the type
     */
    public OccPeriodType getType() {
        return type;
    }

    /**
     * @return the periodTypeCertifiedBy
     */
    public User getPeriodTypeCertifiedBy() {
        return periodTypeCertifiedBy;
    }

    /**
     * @return the periodTypeCertifiedTS
     */
    public LocalDateTime getPeriodTypeCertifiedTS() {
        return periodTypeCertifiedTS;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the startDate
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * @return the startDateCertifiedTS
     */
    public LocalDateTime getStartDateCertifiedTS() {
        return startDateCertifiedTS;
    }

    /**
     * @return the startDateCertifiedBy
     */
    public User getStartDateCertifiedBy() {
        return startDateCertifiedBy;
    }

    /**
     * @return the endDate
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * @return the endDateCertifiedTS
     */
    public LocalDateTime getEndDateCertifiedTS() {
        return endDateCertifiedTS;
    }

    /**
     * @return the endDateCertifiedBy
     */
    public User getEndDateCertifiedBy() {
        return endDateCertifiedBy;
    }

    /**
     * @return the authorizedTS
     */
    public LocalDateTime getAuthorizedTS() {
        return authorizedTS;
    }

    /**
     * @return the authorizedBy
     */
    public User getAuthorizedBy() {
        return authorizedBy;
    }

    /**
     * @return the overrideTypeConfig
     */
    public boolean isOverrideTypeConfig() {
        return overrideTypeConfig;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param periodID the periodID to set
     */
    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

    /**
     * @param propertyUnitID the propertyUnitID to set
     */
    public void setPropertyUnitID(int propertyUnitID) {
        this.propertyUnitID = propertyUnitID;
    }

    /**
     * @param applicationList the applicationList to set
     */
    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<PersonOccPeriod> personList) {
        this.personList = personList;
    }


    /**
     * @param proposalList the proposalList to set
     */
    public void setProposalList(List<Proposal> proposalList) {
        this.proposalList = proposalList;
    }

    /**
     * @param inspectionList the inspectionList to set
     */
    public void setInspectionList(List<OccInspection> inspectionList) {
        this.inspectionList = inspectionList;
    }

    /**
     * @param permitList the permitList to set
     */
    public void setPermitList(List<OccPermit> permitList) {
        this.permitList = permitList;
    }

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(User manager) {
        this.manager = manager;
    }

    /**
     * @param type the type to set
     */
    public void setType(OccPeriodType type) {
        this.type = type;
    }

    /**
     * @param periodTypeCertifiedBy the periodTypeCertifiedBy to set
     */
    public void setPeriodTypeCertifiedBy(User periodTypeCertifiedBy) {
        this.periodTypeCertifiedBy = periodTypeCertifiedBy;
    }

    /**
     * @param periodTypeCertifiedTS the periodTypeCertifiedTS to set
     */
    public void setPeriodTypeCertifiedTS(LocalDateTime periodTypeCertifiedTS) {
        this.periodTypeCertifiedTS = periodTypeCertifiedTS;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @param startDateCertifiedTS the startDateCertifiedTS to set
     */
    public void setStartDateCertifiedTS(LocalDateTime startDateCertifiedTS) {
        this.startDateCertifiedTS = startDateCertifiedTS;
    }

    /**
     * @param startDateCertifiedBy the startDateCertifiedBy to set
     */
    public void setStartDateCertifiedBy(User startDateCertifiedBy) {
        this.startDateCertifiedBy = startDateCertifiedBy;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * @param endDateCertifiedTS the endDateCertifiedTS to set
     */
    public void setEndDateCertifiedTS(LocalDateTime endDateCertifiedTS) {
        this.endDateCertifiedTS = endDateCertifiedTS;
    }

    /**
     * @param endDateCertifiedBy the endDateCertifiedBy to set
     */
    public void setEndDateCertifiedBy(User endDateCertifiedBy) {
        this.endDateCertifiedBy = endDateCertifiedBy;
    }

    /**
     * @param authorizedTS the authorizedTS to set
     */
    public void setAuthorizedTS(LocalDateTime authorizedTS) {
        this.authorizedTS = authorizedTS;
    }

    /**
     * @param authorizedBy the authorizedBy to set
     */
    public void setAuthorizedBy(User authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    /**
     * @param overrideTypeConfig the overrideTypeConfig to set
     */
    public void setOverrideTypeConfig(boolean overrideTypeConfig) {
        this.overrideTypeConfig = overrideTypeConfig;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

   

   
    /**
     * @return the startDateUtilDate
     */
    public java.util.Date getStartDateUtilDate() {
         if(startDate != null){
            startDateUtilDate = java.util.Date.from(getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        }
         return startDateUtilDate;
    }

    /**
     * @return the endDateUtilDate
     */
    public java.util.Date getEndDateUtilDate() {
        if(endDate != null){
            endDateUtilDate = java.util.Date.from(getEndDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDateUtilDate;
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        this.startDateUtilDate = startDateUtilDate;
        if(startDateUtilDate != null){
            startDate = startDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
        this.startDateUtilDate = startDateUtilDate;
    }

    /**
     * @param endDateUtilDate the endDateUtilDate to set
     */
    public void setEndDateUtilDate(java.util.Date endDateUtilDate) {
        this.endDateUtilDate = endDateUtilDate;
        if(endDateUtilDate != null){
            endDate = endDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        this.endDateUtilDate = endDateUtilDate;
    }

    /**
     * @return the status
     */
    public OccPeriodStatusEnum getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccPeriodStatusEnum status) {
        this.status = status;
    }

    /**
     * @return the readyForPeriodAuthorization
     */
    public boolean isReadyForPeriodAuthorization() {
        return readyForPeriodAuthorization;
    }

    /**
     * @param readyForPeriodAuthorization the readyForPeriodAuthorization to set
     */
    public void setReadyForPeriodAuthorization(boolean readyForPeriodAuthorization) {
        this.readyForPeriodAuthorization = readyForPeriodAuthorization;
    }

    /**
     * @return the paymentList
     */
    public List<Payment> getPaymentList() {
        return paymentList;
    }

    /**
     * @param paymentList the paymentList to set
     */
    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    /**
     * @return the governingInspection
     */
    public OccInspection getGoverningInspection() {
        return governingInspection;
    }

    /**
     * @param governingInspection the governingInspection to set
     */
    public void setGoverningInspection(OccInspection governingInspection) {
        this.governingInspection = governingInspection;
    }
}