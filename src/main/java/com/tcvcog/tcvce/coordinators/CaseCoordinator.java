/*
 * Copyright (C) 2017 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CasePhaseChangeRule;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseCoordinator extends BackingBeanUtils implements Serializable{

    final CasePhase initialCECasePphase = CasePhase.PrelimInvestigationPending;
    
    /**
     * Creates a new instance of CaseCoordinator
     */
    public CaseCoordinator() {
        
        
    
    }
    
    /**
     * Called at the very end of the CECase creation process by the CaseIntegrator
     * and simply checks for events that have a required eventcategory attached
     * and places a copy of the event in the Case's member variable.
     * 
     * This means that every time we refresh the case, the list is automatically
     * updated.
     * 
     * DESIGN NOTE: A competing possible location for this method would be on the
     * CECase object itself--in its getEventListActionRequest method
     * 
     * @param c the CECase with a populated set of Events
     * @return the CECase with the action request list ready to roll
     */
    public CECase configureCECase(CECase c){
        List<EventCECase> evList = new ArrayList();
        // transfer any events with requests to a separate list for display at
        // the head of the case profile
        if(c.getEventList() !=  null && c.getEventList().size() >= 1){
            for(EventCECase ev: c.getEventList()){
                if(ev.getRequestedEventCat()!= null && !ev.isResponseComplete()){
                    evList.add(ev);
                }
            }
        }
        c.setEventListActionRequests(evList);
        
        Collections.sort(c.getEventListActionRequests());
        
        Collections.sort(c.getEventList());
        Collections.reverse(c.getEventList()); 
        
        // check to make sure we have empty lists on all of our list objects
        if(c.getViolationList() == null){
            c.setViolationList(new ArrayList<CodeViolation>());
        }
        
        if(c.getEventListActionRequests() == null){
            c.setEventListActionRequests(new ArrayList<EventCECase>());
        }
        
        if(c.getCitationList() == null){
            c.setCitationList(new ArrayList<Citation>());
        }
        
        if(c.getNoticeList() == null){
            c.setNoticeList(new ArrayList<NoticeOfViolation>());
        }
        
        if(c.getRequestList() == null){
            c.setRequestList(new ArrayList<CEActionRequest>());
        }
        
        
        
        return c;
    }
    
    public Icon getIconByCasePhase(CasePhase phase) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        return si.getIcon(phase);
        
    }
    
    
    /**
     * The temporarily hard-coded values for default search parameters for various
     * types of search Param objects
     * 
     * @param m
     * @return an search params object for CEAction requests with default values
     * which amount to requests that aren't attached to a case and were submitted
     * within the past 10 years
     */
    public SearchParamsCEActionRequests getDefaultSearchParamsCEActionRequests(Municipality m){
        
            System.out.println("CaseCoordinator.configureDefaultSearchParams "
                    + "| found actionrequest param object");
            
            SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

            sps.setMuni(m);
            LocalDateTime pastTenYears = LocalDateTime.now().minusYears(10);
            sps.setStartDate(pastTenYears);
            
            // action requests cannot have a time stamp past the current datetime
            sps.setEndDate(LocalDateTime.now());

            sps.setUseAttachedToCase(true);
            sps.setAttachedToCase(false);
            sps.setUseMarkedUrgent(false);
            sps.setUseNotAtAddress(false);
            sps.setUseRequestStatus(false);
        
        return sps;
    }
    
     /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param m
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECases getDefaultSearchParamsCECase(Municipality m){
        SearchParamsCECases params = new SearchParamsCECases();
        
        // superclass 
        params.setFilterByMuni(true);
        params.setMuni(m);
        params.setFilterByObjectID(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific
        params.setUseIsOpen(true);
        params.setIsOpen(true);
        
        params.setDateToSearchCECases("Opening date of record");
        params.setUseCaseManager(false);
        
        params.setUseCasePhase(false);
        params.setUseCaseStage(false);
        params.setUseProperty(false);
        params.setUsePropertyInfoCase(false);
        params.setUseCaseManager(false);
        
        return params;
    }
    
    /**
     * Existed before queryCases() became the goto way to retrieve lists of cases
     * @deprecated 
     * @param m
     * @return
     * @throws IntegrationException 
     */
    public List<CECase> getOpenCECaseList(Municipality m) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        List<CECase> cList = ci.queryCECases(getDefaultSearchParamsCECase(m));
        return cList;
    }
    
    /**
     * Front door for querying cases in the DB
     * 
     * @param params pre-configured search parameters
     * @return
     * @throws IntegrationException 
     */
    public List<CECase> queryCECases(SearchParamsCECases params) throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        return ci.queryCECases(params);
        
    }
    
    
    public CECase getInitializedCECase(Property p, User u){
        CECase newCase = new CECase();
        
        int casePCC = getControlCodeFromTime();
        // caseID set by postgres sequence
        // timestamp set by postgres
        // no closing date, by design of case flow
        newCase.setPublicControlCode(casePCC);
        newCase.setProperty(p);
        newCase.setCaseManager(u);
        
        return newCase;
    }
    
    /**
     * Primary entry point for code enf cases. Two major pathways exist through this method:
     * - creating cases as a result of an action request submission
     * - creating cases from some other source than an action request
     * Depending on the source, an appropriately note-ified case origination event
     * is built and attached to the case that was just created.
     * 
     * @param newCase
     * @param u
     * @param cear
     * @throws IntegrationException
     * @throws CaseLifecyleException
     * @throws ViolationException 
     */
    public void insertNewCECase(CECase newCase, User u, CEActionRequest cear) throws IntegrationException, CaseLifecyleException, ViolationException{
        
        CECase insertedCase;
        
        CaseIntegrator ci = getCaseIntegrator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCategory originationCategory;
        EventCECase originationEvent;
        
        // set default status to prelim investigation pending
        newCase.setCasePhase(initialCECasePphase);
        
        // the integrator returns to us a CECase with the correct ID after it has
        // been written into the DB
        insertedCase = ci.insertNewCECase(newCase);
        newCase.setCaseID(insertedCase.getCaseID());

        // If we were passed in an action request, connect it to the new case we just made
        if(cear != null){
            ceari.connectActionRequestToCECase(cear.getRequestID(), insertedCase.getCaseID(), u.getUserID());
            originationCategory = ec.getInitiatlizedEventCategory(
                    Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByActionRequest")));
            originationEvent = ec.getInitializedEvent(newCase, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case generated from the submission of a Code Enforcement Action Request");
            sb.append("<br/>");
            sb.append("ID#:");
            sb.append(cear.getRequestID());
            sb.append(" submitted by ");
            sb.append(cear.getActionRequestorPerson().getFirstName());
            sb.append(" ");
            sb.append(cear.getActionRequestorPerson().getLastName());
            sb.append(" on ");
            sb.append(getPrettyDate(cear.getDateOfRecord()));
            sb.append(" with a database timestamp of ");
            sb.append(getPrettyDate(cear.getSubmittedTimeStamp()));
            originationEvent.setNotes(sb.toString());
            
            
        } else {
            // since there's no action request, the assumed method is called "observation"
            originationCategory = ec.getInitiatlizedEventCategory(
                    Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("originiationByObservation")));
            originationEvent = ec.getInitializedEvent(newCase, originationCategory);
            StringBuilder sb = new StringBuilder();
            sb.append("Case opened directly on property by code officer assigned to this event");
            originationEvent.setNotes(sb.toString());
            
        }
            originationEvent.setOwner(u);
            originationEvent.setCaseID(insertedCase.getCaseID());
            originationEvent.setDateOfRecord(LocalDateTime.now());
            attachNewEventToCECase(newCase, originationEvent, null);
    }
    
    /**
     * Called by the PIBCECaseBB when a public user wishes to add an event
     * to the case they are viewing online. This method stitches together the
     * message text, messenger name, and messenger phone number before
     * passing the info back to the EventCoordinator
     * @param caseID can be extracted from the public info bundle
     * @param msg the text of the message the user wants to add to the case 
     * @param messagerName the first and last name of the person submitting the message
     * Note that this submission info is not YET wired into the actual Person objects
     * in the system.
     * @param messagerPhone a simple String rendering of whatever the user types in. Length validation only.
     */
    public void attachPublicMessage(int caseID, String msg, String messagerName, String messagerPhone) throws IntegrationException{
        StringBuilder sb = new StringBuilder();
        sb.append("Case note added by ");
        sb.append(messagerName);
        sb.append(" with contact number: ");
        sb.append(messagerPhone);
        sb.append(": ");
        sb.append("<br/><br/>");
        sb.append(msg);
        
        EventCoordinator ec = getEventCoordinator();
        ec.attachPublicMessagToCECase(caseID, sb.toString());
        
        
    }
    
    /**
     * Called by the CaseManageBB when the user requests to change the case phase manually.
     * Note that this method is responsible for storing the case's phase before the change, 
     * updating the case Object itself, and then passing the updated CECase object
     * and the phase phase to the EventCoordinator which will take care of logging the event
     * 
     * @param c the case before its phase has been changed
     * @param newPhase the CasePhase to which we to change the case
     * @throws IntegrationException
     * @throws CaseLifecyleException 
     */
    public void manuallyChangeCasePhase(CECase c, CasePhase newPhase) throws IntegrationException, CaseLifecyleException, ViolationException{
        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase pastPhase = c.getCasePhase();
        // this call to changeCasePhase requires that the case we pass in already has
        // its phase changed
        c.setCasePhase(newPhase);
        ci.changeCECasePhase(c);
        
        ec.generateAndInsertManualCasePhaseOverrideEvent(c, pastPhase);
        
    }
    
    
    /**
     * Primary event life cycle control method which is called
     * each time an event is added to a code enf case. The primary business
     * logic related to which events can be attached to a case at any
     * given case phase is implemented in this coordinator method.
     * 
     * Its core operation is to check case and event related qualities
     * and delegate further processing to event-type specific methods
     * also found in this coordinator
     * 
     * @param c the case to which the event should be added
     * @param e the event to add to the case also included in this call
     * @param viol the CodeViolation object associated with this event, can be null
     * @return 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public int attachNewEventToCECase(CECase c, EventCECase e, CodeViolation viol) 
            throws CaseLifecyleException, IntegrationException, ViolationException{
        EventType eventType = e.getCategory().getEventType();
        EventIntegrator ei = getEventIntegrator();
        int insertedEventID = 0;
        
        switch(eventType){
            case Action:
                System.out.println("CaseCoordinator.attachNewEventToCECase: action case");
                insertedEventID = ei.insertEvent(e);
                checkForAndCarryOutCasePhaseChange(c, e);
                break;
            case Compliance:
                if(viol != null){
                    System.out.println("CaseCoordinator.attachNewEventToCECase: compliance inside if");
                    viol.setActualComplianceDate(e.getDateOfRecord());
                    insertedEventID = ei.insertEvent(e);
                    checkForFullComplianceAndCloseCaseIfTriggered(c);
                } else {
                    throw new CaseLifecyleException("no violation was included with this compliance event");
                }
                break;
            case Closing:
                System.out.println("CaseCoordinator.attachNewEventToCECase: closing case");
                insertedEventID = processClosingEvent(c, e);
                break;
            default:
                System.out.println("CaseCoordinator.attachNewEventToCECase: default case");
                e.setCaseID(c.getCaseID());
                insertedEventID = ei.insertEvent(e);
        } // close switch
        return insertedEventID;
    } // close method
   
   
    
     /**
     * Implements business rules for determining which event types are allowed
     * to be attached to the given CECase based on the case's phase and the
     * user's permissions in the system.
     * 
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     * 
     * @param c the CECase on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> getPermittedEventTypesForCECase(CECase c, User u){
        List<EventType> typeList = new ArrayList<>();
        RoleType role = u.getRoleType();
        
        if(role == RoleType.EnforcementOfficial 
                || u.getRoleType() == RoleType.Developer){
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }
        
        if(role != RoleType.MuniReader){
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
        }
        return typeList;
    }
    
    
    
    /**
     * Iterates over all of a case's violations and checks for compliance. 
     * If all of the violations have a compliance date, the case is automatically
     * closed and a case closing event is generated and added to the case
     * 
     * @param c the case whose violations should be checked for compliance
     * @throws IntegrationException
     * @throws CaseLifecyleException
     * @throws ViolationException 
     */
    private void checkForFullComplianceAndCloseCaseIfTriggered(CECase c) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        List caseViolationList = cvi.getCodeViolations(c);
        boolean complianceWithAllViolations = false;
        ListIterator<CodeViolation> fullViolationLi = caseViolationList.listIterator();
        CodeViolation cv;
        
        while(fullViolationLi.hasNext())
        {
            cv = fullViolationLi.next();
            // if there are any outstanding code violations, toggle switch to 
            // false and exit the loop. Phase change will not occur
            if(cv.getActualComplianceDate() != null){
                complianceWithAllViolations = true;
            } else {
                complianceWithAllViolations = false;
                break;
            }
        } // close while
        
        EventCECase complianceClosingEvent;
        if (complianceWithAllViolations){
            complianceClosingEvent = ec.getInitializedEvent(c, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("closingAfterFullCompliance"))));
            attachNewEventToCECase(c, complianceClosingEvent, null);
            
        } // close if
        
    }
    
     
    private int processClosingEvent(CECase c, EventCECase e) throws IntegrationException, CaseLifecyleException{
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        CasePhase closedPhase = CasePhase.Closed;
        c.setCasePhase(closedPhase);
        ci.changeCECasePhase(c);
        
        c.setClosingDate(LocalDateTime.now());
        updateCoreCECaseData(c);
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down here in the Java
        e.setDateOfRecord(LocalDateTime.now());
        e.setOwner(getFacesUser());
        e.setDescription(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventDescription"));
        e.setNotes(getResourceBundle(Constants.MESSAGE_TEXT).getString("automaticClosingEventNotes"));
        e.setCaseID(c.getCaseID());
        return ei.insertEvent(e);
        
    }
    
    
    private void checkForAndCarryOutCasePhaseChange(CECase c, EventCECase e) throws CaseLifecyleException, IntegrationException, ViolationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase initialCasePhase = c.getCasePhase();
        EventCoordinator ec = getEventCoordinator();
        // this value is used to compare to the category IDs listed in the resource bundle
        int evCatID = e.getCategory().getCategoryID();
        
        // check to see if the event triggers a case phase chage. 
        
        if(
            (initialCasePhase == CasePhase.PrelimInvestigationPending 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToNoticeDelivery")))

            ||

            (initialCasePhase == CasePhase.NoticeDelivery 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.InitialComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.SecondaryComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToAwaitingHearingDate")))

            ||

            (initialCasePhase == CasePhase.AwaitingHearingDate 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToHearingPreparation")))

            ||

            (initialCasePhase == CasePhase.HearingPreparation 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialPostHearingComplianceTimeframe")))

            || 

            (initialCasePhase == CasePhase.InitialPostHearingComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryPostHearingComplianceTimeframe")))
        ){
            // write the phase change to the DB
            // we must ship the case to the integrator with the case phase updated
            // because the integrator does not implement any business logic
            c.setCasePhase(getNextCasePhase(c));
            ci.changeCECasePhase(c);
            
            // generate event for phase change and write
            ec.generateAndInsertPhaseChangeEvent(c, initialCasePhase); 

        } 
        
      
        
    }
    
    
    /**
     * Utility method for determining which CasePhase follows any given case's CasePhase. 
     * @param c the case whose set CasePhase will be read to determine the next CasePhase
     * @return the ONLY CasePhase to which any case can be changed to without a manual protocol
     * override request
     * @throws CaseLifecyleException thrown when no next CasePhase exists or the next
     * CasePhase cannot be determined
     */
    public CasePhase getNextCasePhase(CECase c) throws CaseLifecyleException{
        CasePhase currentPhase = c.getCasePhase();
        CasePhase nextPhaseInSequence = null;
        
        switch(currentPhase){
            
            case PrelimInvestigationPending:
                nextPhaseInSequence = CasePhase.NoticeDelivery;
                break;
                // conduct inital investigation
                // compose and deply notice of violation
            case NoticeDelivery:
                nextPhaseInSequence = CasePhase.InitialComplianceTimeframe;
                break;
                // Letter marked with a send date
            case InitialComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryComplianceTimeframe;
                break;
                // compliance inspection
            case SecondaryComplianceTimeframe:
                nextPhaseInSequence = CasePhase.AwaitingHearingDate;
                break;
                // Filing of citation
            case AwaitingHearingDate:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                // hearing date scheduled
            case HearingPreparation:
                nextPhaseInSequence = CasePhase.InitialPostHearingComplianceTimeframe;
                break;
                // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryPostHearingComplianceTimeframe;
                break;
            
            case SecondaryPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                
            case Closed:
                // TODO deal with this later
//                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                nextPhaseInSequence = CasePhase.InactiveHolding;
                break;
                
            default:
                nextPhaseInSequence = CasePhase.InactiveHolding;
        }
        
        return nextPhaseInSequence;
    }
    
    
    public List retrieveViolationList(CECase ceCase) throws IntegrationException{
        List<CodeViolation> al;
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
    public void noticeOfViolationResetMailing(CECase cs, NoticeOfViolation nov) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setRequestToSend(false);
        nov.setSentTS(null);
        nov.setReturnedTS(null);
        cvi.updateViolationLetter(nov);
        
    }
    
    
    public void noticeOfViolationLockAndQueue(CECase c, NoticeOfViolation nov) 
            throws CaseLifecyleException, IntegrationException, EventException, ViolationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        EventCoordinator evCoord = getEventCoordinator();
        
        // togglign this switch puts the notice in the queue for sending
        // flag violation letter as ready to send
        // this will also need to trigger a letter mailing process that hasn't been implemented as
        // of 2 March 2018
        if(nov.isRequestToSend() == false){
            nov.setRequestToSend(true);
        } else {
            throw new CaseLifecyleException("Notice is already queued for sending");
        }
        
        // new letters won't have a LocalDateTime object
        // so insert instead of update in this case
        if(nov.getCreationTS() == null){
            cvi.insertNoticeOfViolation(c, nov);
            
        } else {
            cvi.updateViolationLetter(nov);
            
        }
        
        EventCECase noticeEvent = evCoord.getInitializedEvent(c, evCoord.getInitiatlizedEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("noticeQueued"))));
        
        String queuedNoticeEventNotes = getResourceBundle(Constants.MESSAGE_TEXT).getString("noticeQueuedEventDesc");
        noticeEvent.setDescription(queuedNoticeEventNotes);
        
        noticeEvent.setOwner(getFacesUser());
        noticeEvent.setDiscloseToMunicipality(true);
        noticeEvent.setDiscloseToPublic(true);
        
        ArrayList<Person> al = new ArrayList();
        al.add(nov.getRecipient());
        noticeEvent.setPersonList(al);
        
        attachNewEventToCECase(c, noticeEvent, null);
    }
    
    public void refreshCase(CECase c) throws IntegrationException{
        System.out.println("CaseCoordinator.refreshCase");
        CaseIntegrator ci = getCaseIntegrator();
        
        getSessionBean().setcECase(ci.getCECase(c.getCaseID()));
    }
    
    public void noticeOfViolationMarkAsSent(CECase ceCase, NoticeOfViolation nov) throws CaseLifecyleException, EventException, IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setSentTS(LocalDateTime.now());
        nov.setSentTSPretty(getPrettyDate(LocalDateTime.now()));
        cvi.updateViolationLetter(nov);   
        //advanceToNextCasePhase(ceCase);
    }
    
    public void noticeOfViolationMarkAsReturned(CECase c, NoticeOfViolation nov) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setReturnedTS(LocalDateTime.now());
        cvi.updateViolationLetter(nov);
        refreshCase(c);
    } 
    
    public void noticeOfViolationDelete(NoticeOfViolation nov) throws CaseLifecyleException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();

        //cannot delete a letter that was already sent
        if(nov != null && nov.getSentTS() != null){
            throw new CaseLifecyleException("Cannot delete a letter that has been sent");
        } else {
            try {
                cvi.deleteViolationLetter(nov);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to delete notice of violation due to a database error", ""));
            }
        }
    }
    
   
   public Citation generateNewCitation(List<CodeViolation> violationList){
       Citation newCitation = new Citation();
       List<CodeViolation> al = new ArrayList<>();
       ListIterator<CodeViolation> li = violationList.listIterator();
       CodeViolation cv;
       
       StringBuilder notesBuilder = new StringBuilder();
       notesBuilder.append("Failure to comply with the following ordinances:\n");
       
       while(li.hasNext()){
           
           cv = li.next();
           System.out.println("CaseCoordinator.generateNewCitation | linked list item: " 
                   + cv.getDescription());
           
           // build a nice note section that lists the elements cited
           notesBuilder.append("* Chapter ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdchapterNo());
           notesBuilder.append(":");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdchapterTitle());
           notesBuilder.append(", Section ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSecNum());
           notesBuilder.append(":");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSecTitle());
           notesBuilder.append(", Subsection ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSubSecNum());
           notesBuilder.append(": ");
           notesBuilder.append(cv.getCodeViolated().getCodeElement().getOrdSubSecTitle());
           notesBuilder.append("\n\n");
           
           al.add(cv);
           
       }
       newCitation.setViolationList(al);
       newCitation.setNotes(notesBuilder.toString());
       newCitation.setIsActive(true);
       
       return newCitation;
   }
   
   /**
    * Implements business logic before updating a CECase's core data (opening date,
    * closing date, etc.). If all is well, pass to integrator.
    * @param c the CECase to be updated
    * @throws CaseLifecyleException
    * @throws IntegrationException 
    */
   public void updateCoreCECaseData(CECase c) throws CaseLifecyleException, IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       if(c.getClosingDate() != null){
            if(c.getClosingDate().isBefore(c.getOriginationDate())){
                throw new CaseLifecyleException("You cannot update a case's origination date to be after its closing date");
            }
       }
       ci.updateCECaseMetadata(c);
   }
   
   public void deleteCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.deleteCitation(c);
              
   }
   
   public void issueCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.insertCitation(c);
       
   }
   
   public void updateCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.updateCitation(c);
       
   }
   
   /**
    * Factory method for our CEActionRequests - initializes the date as well
    * @return The CEActionRequest ready for populating with user values
    */
   public CEActionRequest getInititalizedCEActionRequest(){
       System.out.println("CaseCoordinator.getNewActionRequest");
       CEActionRequest cear = new CEActionRequest();
       // start by writing in the current date
       cear.setDateOfRecordUtilDate(
               java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
       
       return new CEActionRequest();
       
   }
   
   /**
    * Utility method for determining whether or not the panel of Code Enforcement request
    * routing buttons can be pressed. Used by the view for setting disabled properties on buttons
    * requests 
    * @param req the current CE Request
    * @param u current user
    * @return True if the current user can route the given ce request
    */
   public boolean determineCEActionRequestRoutingActionEnabledStatus(
                                                        CEActionRequest req,
                                                        User u ){
       if(req != null && u.getKeyCard() != null){
            if((
                    req.getRequestStatus().getStatusID() == 
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("actionRequestInitialStatusCode")))
                    && 
                    u.getKeyCard().isHasEnfOfficialPermissions()
                ){
                return true;
            }
        }
       return false;
   }
   
   public ReportConfigCECase getDefaultReportConfigCECase(CECase c){
        ReportConfigCECase rpt = new ReportConfigCECase();
        
        // general
        rpt.setIncludeCaseName(false);
        
        // events
        rpt.setIncludeEventNotes(true);
        rpt.setIncludeHiddenEvents(false);
        rpt.setIncludeInactiveEvents(false);
        rpt.setIncludeRequestedActionFields(false);
        rpt.setIncludeMunicipalityDiclosedEvents(true);
        rpt.setIncludeOfficerOnlyEvents(false);
        
        // notices of violation
        rpt.setIncludeAllNotices(false);
        rpt.setIncludeNoticeFullText(true);
        // violations
        rpt.setIncludeFullOrdinanceText(true);
        rpt.setIncludeViolationPhotos(true);
        
       return rpt;
   }
   
   public ReportConfigCECaseList getDefaultReportConfigCECaseList(){
       ReportConfigCECaseList listRpt = new ReportConfigCECaseList();
       listRpt.setIncludeListSummaryFigures(true);
       listRpt.setIncludeCaseNames(true);
       listRpt.setIncludeFullOwnerContactInfo(true);
       listRpt.setIncludeViolationList(true);
       listRpt.setIncludeEventSummaryByCase(false);
       return listRpt;
       
       
   }
   
   
   /**
    * Primary configuration mechanism for customizing report data from the 
    * ceCases.xhtml display. Called by the CECasesBB.
    * 
    * @param rptCse
    * @return
    * @throws IntegrationException 
    */
   public ReportConfigCECase transformCECaseForReport(ReportConfigCECase rptCse) throws IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       // we actually get an entirely new object instead of editing the 
       // one we used throughout the ceCases.xhtml
       CECase c = ci.getCECase(rptCse.getCse().getCaseID());
       
       List<EventCECase> evList =  new ArrayList<>();
       Iterator<EventCECase> iter = c.getEventList().iterator();
       while(iter.hasNext()){
            EventCECase ev = iter.next();
            
            // toss out hidden events unless the user wants them
            if(ev.isHidden() && !rptCse.isIncludeHiddenEvents()) continue;
            // toss out inactive events unless user wants them
            if(!ev.isActive()&& !rptCse.isIncludeInactiveEvents()) continue;
            // toss out events only available internally to the muni users unless user wants them
            if(!ev.isDiscloseToMunicipality() && !rptCse.isIncludeMunicipalityDiclosedEvents()) continue;
            // toss out officer only events unless the user wants them
            if((!ev.isDiscloseToMunicipality() && !ev.isDiscloseToPublic()) 
                    && !rptCse.isIncludeOfficerOnlyEvents()) continue;
            evList.add(ev);
       }
       c.setEventList(evList);
       List<NoticeOfViolation> noticeList = new ArrayList<>();
       Iterator<NoticeOfViolation> iterNotice = c.getNoticeList().iterator();
       while(iterNotice.hasNext()){
           NoticeOfViolation nov = iterNotice.next();
           // skip unsent notices
           if(nov.getSentTS() == null) continue;
           // if the user dones't want all notices, skip returned notices
           if(!rptCse.isIncludeAllNotices() && nov.getReturnedTS() != null  ) continue;
           noticeList.add(nov);
       }
       c.setNoticeList(noticeList);
       return rptCse;
   }
   
   /**
    * Currently a pass-through method for object creation
    * @param ec
    * @return
    * @throws IntegrationException 
    */
   public CasePhaseChangeRule getCasePhaseChangeRule(EventCategory ec) throws IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       return ci.getPhaseChangeRule(ec.getCasePhaseChangeRule().getRuleID());
       
   }
   
   
    
    public CodeViolation generateNewCodeViolation(CECase c, EnforcableCodeElement ece){
        CodeViolation v = new CodeViolation();
        
        System.out.println("ViolationCoordinator.generateNewCodeViolation | enfCodeElID:" + ece.getCodeSetElementID());
        
        v.setViolatedEnfElement(ece);
        v.setStipulatedComplianceDate(LocalDateTime.now()
                .plusDays(ece.getNormDaysToComply()));
        v.setPenalty(ece.getNormPenalty());
        v.setDateOfRecord(LocalDateTime.now());
        v.setCeCaseID(c.getCaseID());
        // control is passed back to the violationAddBB which stores this 
        // generated violation under teh activeCodeViolation in the session
        // which the ViolationAddBB then picks up and edits
        
        return v;
    }
    
    /**
     * Standard coordinator method which calls the integration method after 
     * checking businses rules. 
     * ALSO creates a corresponding timeline event to match the stipulated compliance
     * date on the violation that's added.
     * @param v
     * @param c
     * @return the database key assigned to the inserted violation
     * @throws IntegrationException
     * @throws ViolationException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     */
    public int attachViolationToCaseAndInsertTimeFrameEvent(CodeViolation v, CECase c) throws IntegrationException, ViolationException, CaseLifecyleException{
        
        CodeViolationIntegrator vi = getCodeViolationIntegrator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        EventCECase tfEvent;
        int violationStoredDBKey;
        int eventID;
        StringBuilder sb = new StringBuilder();
        
        EventCategory eventCat = ec.getInitiatlizedEventCategory(
                                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                                .getString("complianceTimeframeExpiry")));
//        EventCategory eventCat = ec.getInitiatlizedEventCategory(113);
        tfEvent = ec.getInitializedEvent(c, eventCat);
        tfEvent.setDateOfRecord(v.getStipulatedComplianceDate());
        tfEvent.setOwner(c.getCaseManager());
        tfEvent.setRequestActionByDefaultMuniCEO(true);
        eventCat = ec.getInitiatlizedEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
                .getString("propertyInspection")));
        tfEvent.setRequestedEventCat(eventCat);
        tfEvent.setActionRequestedBy(uc.getCogBotUser());
        
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT)
                        .getString("complianceTimeframeEndEventDesc"));
        sb.append("Case: ");
        sb.append(c.getCaseName());
        sb.append(" at ");
        sb.append(c.getProperty().getAddress());
        sb.append("(");
        sb.append(c.getProperty().getMuni().getMuniName());
        sb.append(")");
        sb.append("; Violation: ");
        sb.append(v.getViolatedEnfElement().getCodeElement().getHeaderString());
        tfEvent.setDescription(sb.toString());
        
        if(verifyCodeViolationAttributes(v)){
            eventID = cc.attachNewEventToCECase(c, tfEvent, v);
            v.setComplianceTimeframeEventID(eventID);
            violationStoredDBKey = vi.insertCodeViolation(v);
        } else {
            throw new ViolationException("Failed violation verification");
        }
        
        
        return violationStoredDBKey;
        
    }
    
    /**
     * Uses date fields on the populated CodeViolation to determine
     * a status string and icon for UI
     * Called by the integrator when creating a code violation
     * @param cv
     * @return the CodeViolation with correct icon and statusString
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation configureCodeViolation(CodeViolation cv) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        if(cv.getActualComplianceDate() == null){
            // violation still within compliance timeframe
            if(cv.getDaysUntilStipulatedComplianceDate() >= 0){
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_withincomptimeframe_ageleadtext"));
                
            } else if(cv.getCitationIDList().isEmpty()) {
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_overdue_ageleadtext"));
            } else {
                cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_statusstring"));
                cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_iconid"))));
                cv.setAgeLeadText(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                        .getString("codeviolation_unresolved_citation_ageleadtext"));
            }
        } else {
            cv.setStatusString(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString("codeviolation_resolved_statusstring"));
            cv.setIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.VIOLATIONS_BUNDLE)
                    .getString("codeviolation_resolved_iconid"))));
        }
        return cv;
    }
    
    
    private boolean verifyCodeViolationAttributes(CodeViolation cv) throws ViolationException{
        
        // this is no good--when we get past stip comp date, we'll have a coordinator issue
//        if(cv.getStipulatedComplianceDate().isBefore(LocalDateTime.now())){
//            throw new ViolationException("Stipulated Complicance must be in the future!");
//        }
        
        return true;
    }
    
    public void updateCodeViolation(CodeViolation cv) throws ViolationException, IntegrationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        if(verifyCodeViolationAttributes(cv)){
            cvi.updateCodeViolation(cv);
        }
    }
    
    /**
     * CodeViolation should have the actual compliance date set from the user's 
     * event date of record
     * @param cv
     * @param u
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void recordCompliance(CodeViolation cv, User u) throws IntegrationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        EventIntegrator ei = getEventIntegrator();
        // update violation record for compliance
        cv.setComplianceUser(u);
        cvi.recordCompliance(cv);
                
        // inactivate timeframe expiry event
        if(cv.getCompTimeFrameComplianceEvent() != null || cv.getComplianceTimeframeEventID() != 0){
            int vev;
            // cope with the condition that incoming code violations may only have the id
            // of the assocaited event and not the entire object
            if(cv.getCompTimeFrameComplianceEvent() != null){
                 vev = cv.getCompTimeFrameComplianceEvent().getEventID();
            } else {
                vev = cv.getComplianceTimeframeEventID();
            }
            System.out.println("ViolationCoordinator.recordCompliance | invalidating event id: " + vev);
            ei.inactivateEvent(vev);
        }
    }
    
    public NoticeOfViolation getNewNoticeOfViolation(){
        NoticeOfViolation nov = new NoticeOfViolation();
        nov.setDateOfRecord(LocalDateTime.now());
        return nov;
        
    }
    
    public void deleteViolation(CodeViolation cv) throws IntegrationException{
        //TODO: delete photos and photo links
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        cvi.deleteCodeViolation(cv);
    }
    
    public List getCodeViolations(CECase ceCase) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        List al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
   
} // close class
