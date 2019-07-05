/*
 * Copyright (C) 2018 Turtle Creek Valley
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
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonAddBB extends BackingBeanUtils implements Serializable {

    private Property activeProp;
    private Person person;
    
    //not in the add form! ID generated by DB's sequence
    //private int personid;
    private PersonType formPersonType;
    private Municipality formMuni;
    
    private String formFirstName;
    private String formLastName;
    private String formJobTitle;
    
    private String formPhoneCell;
    private String formPhoneHome;
    private String formPhoneWork;
    
    private String formEmail;
    private String formAddress_street;
    private String formAddress_city;
    
    private String formAddress_state;
    private String formAddress_zip;
    private String formNotes;
    
    // not needed on the add since DB will stamp with now()
    //private LocalDateTime lastUpdated;
    private java.util.Date formExpiryDate;
    private boolean formIsActive;
    
    private boolean formIsUnder18;
    
    private boolean formConnectToActiveProperty;
    /**
     * Creates a new instance of PersonAddBB
     */
    public PersonAddBB() {
    }
    /**
     * @deprecated 
     * @return 
     */
    public String oldAddPerson(){
        Person p = new Person();
        PersonIntegrator personInt = getPersonIntegrator();
        
        p.setPersonType(formPersonType);
        
        if (formMuni != null){
            p.setMuniCode(formMuni.getMuniCode());
        } else {
            p.setMuniCode(getSessionBean().getActiveMuni().getMuniCode());
        }
        
        
        p.setFirstName(formFirstName);
        p.setLastName(formLastName);
        p.setJobTitle(formJobTitle);
        
        p.setPhoneCell(formPhoneCell);
        p.setPhoneHome(formPhoneHome);
        p.setPhoneWork(formPhoneWork);
        
        p.setEmail(formEmail);
        p.setAddressStreet(formAddress_street);
        p.setAddressCity(formAddress_city);
        
        p.setAddressState(formAddress_state);
        p.setAddressZip(formAddress_zip);
        p.setNotes(formNotes);
        
        // placeholder for lastupdated
        if (formExpiryDate != null) {
            p.setExpiryDate(formExpiryDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        } else{
            p.setExpiryDate(LocalDateTime.MAX);
        }
        
        p.setActive(formIsActive);
        
        p.setUnder18(formIsUnder18);
        
        try {
            if(formConnectToActiveProperty){
                
                Property property = getSessionBean().getActiveProp();
                personInt.insertPersonAndConnectToProperty(p, property);
            
                getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO, 
                         "Successfully added " + p.getFirstName() + " to the Database!" 
                             + " and connected to " + property.getAddress(), ""));
                
            } else {
                personInt.insertPerson(p);
            
                getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO, 
                             "Successfully added " + p.getFirstName() + " to the Database!", ""));
                
            }

        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
               getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to add new person to the database, my apologies!", "Make sure your phone numbers have no separators"));
            return "";
        }
        
        return "personSearch";
    }

    public String addPerson() {
        
        Person temp = new Person();
        
        temp.setFirstName(formFirstName);
        
        temp.setLastName(formLastName);
        
        temp.setAddressStreet(formAddress_street);
        
        temp.setAddressCity(formAddress_city);
        
        temp.setAddressState(formAddress_state);
        
        temp.setAddressZip(formAddress_zip);
        
        temp.setPhoneHome(formPhoneHome);
        
        temp.setPhoneCell(formPhoneCell);
        
        temp.setPhoneWork(formPhoneWork);
        
        temp.setEmail(formEmail);
        
        getSessionBean().getOccPermitApplication().getAttachedPersons().add(temp);
        
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/personsRequirementManage.xhtml#currentStep");
            } catch (IOException ex) {
            }
        
        return "managePeople";
        
    }
    
    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

  
    /**
     * @return the formPersonType
     */
    public PersonType getFormPersonType() {
        return formPersonType;
    }

    

    /**
     * @return the formMuniCode
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

   
    /**
     * @return the formFirstName
     */
    public String getFormFirstName() {
        return formFirstName;
    }

    /**
     * @return the formLastName
     */
    public String getFormLastName() {
        return formLastName;
    }

    /**
     * @return the formJobTitle
     */
    public String getFormJobTitle() {
        return formJobTitle;
    }

    /**
     * @return the formPhoneCell
     */
    public String getFormPhoneCell() {
        return formPhoneCell;
    }

    /**
     * @return the formPhoneHome
     */
    public String getFormPhoneHome() {
        return formPhoneHome;
    }

    /**
     * @return the formPhoneWork
     */
    public String getFormPhoneWork() {
        return formPhoneWork;
    }

    /**
     * @return the formEmail
     */
    public String getFormEmail() {
        return formEmail;
    }

    /**
     * @return the formAddress_street
     */
    public String getFormAddress_street() {
        return formAddress_street;
    }

    /**
     * @return the formAddress_city
     */
    public String getFormAddress_city() {
        return formAddress_city;
    }

    /**
     * @return the formAddress_state
     */
    public String getFormAddress_state() {
        return formAddress_state;
    }

    /**
     * @return the formAddress_zip
     */
    public String getFormAddress_zip() {
        return formAddress_zip;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        return formNotes;
    }



    
    /**
     * @return the formExpiryDate
     */
    public java.util.Date getFormExpiryDate() {
        return formExpiryDate;
    }

    /**
     * @return the formIsActive
     */
    public boolean isFormIsActive() {
        return formIsActive;
    }

    /**
     * @return the formIsUnder18
     */
    public boolean isFormIsUnder18() {
        return formIsUnder18;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

  

    /**
     * @param formPersonType the formPersonType to set
     */
    public void setFormPersonType(PersonType formPersonType) {
        this.formPersonType = formPersonType;
    }

   

    /**
     * @param m
     */
    public void setFormMuniCode(Municipality m) {
        this.formMuni = m;
    }

   
    /**
     * @param formFirstName the formFirstName to set
     */
    public void setFormFirstName(String formFirstName) {
        this.formFirstName = formFirstName;
    }

    /**
     * @param formLastName the formLastName to set
     */
    public void setFormLastName(String formLastName) {
        this.formLastName = formLastName;
    }

    /**
     * @param formJobTitle the formJobTitle to set
     */
    public void setFormJobTitle(String formJobTitle) {
        this.formJobTitle = formJobTitle;
    }

    /**
     * @param formPhoneCell the formPhoneCell to set
     */
    public void setFormPhoneCell(String formPhoneCell) {
        this.formPhoneCell = formPhoneCell;
    }

    /**
     * @param formPhoneHome the formPhoneHome to set
     */
    public void setFormPhoneHome(String formPhoneHome) {
        this.formPhoneHome = formPhoneHome;
    }

    /**
     * @param formPhoneWork the formPhoneWork to set
     */
    public void setFormPhoneWork(String formPhoneWork) {
        this.formPhoneWork = formPhoneWork;
    }

    /**
     * @param formEmail the formEmail to set
     */
    public void setFormEmail(String formEmail) {
        this.formEmail = formEmail;
    }

    /**
     * @param formAddress_street the formAddress_street to set
     */
    public void setFormAddress_street(String formAddress_street) {
        this.formAddress_street = formAddress_street;
    }

    /**
     * @param formAddress_city the formAddress_city to set
     */
    public void setFormAddress_city(String formAddress_city) {
        this.formAddress_city = formAddress_city;
    }

    /**
     * @param formAddress_state the formAddress_state to set
     */
    public void setFormAddress_state(String formAddress_state) {
        this.formAddress_state = formAddress_state;
    }

    /**
     * @param formAddress_zip the formAddress_zip to set
     */
    public void setFormAddress_zip(String formAddress_zip) {
        this.formAddress_zip = formAddress_zip;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @param formExpiryDate the formExpiryDate to set
     */
    public void setFormExpiryDate(java.util.Date formExpiryDate) {
        this.formExpiryDate = formExpiryDate;
    }

    /**
     * @param formIsActive the formIsActive to set
     */
    public void setFormIsActive(boolean formIsActive) {
        this.formIsActive = formIsActive;
    }

    /**
     * @param formIsUnder18 the formIsUnder18 to set
     */
    public void setFormIsUnder18(boolean formIsUnder18) {
        this.formIsUnder18 = formIsUnder18;
    }

    /**
     * @return the formConnectToActiveProperty
     */
    public boolean isFormConnectToActiveProperty() {
        // default value
        formConnectToActiveProperty = false;
        return formConnectToActiveProperty;
    }

    /**
     * @param formConnectToActiveProperty the formConnectToActiveProperty to set
     */
    public void setFormConnectToActiveProperty(boolean formConnectToActiveProperty) {
        this.formConnectToActiveProperty = formConnectToActiveProperty;
    }

    /**
     * @return the activeProp
     */
    public Property getActiveProp() {
        
        activeProp = getSessionBean().getActiveProp();
        return activeProp;
    }

    /**
     * @param activeProp the activeProp to set
     */
    public void setActiveProp(Property activeProp) {
        this.activeProp = activeProp;
    }
    
    
    
}
