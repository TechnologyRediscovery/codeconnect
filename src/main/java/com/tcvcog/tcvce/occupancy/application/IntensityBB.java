/*
 * Copyright (C) 2019 Nathan Dietz
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Intensity;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Nathan Dietz
 */
public class IntensityBB extends BackingBeanUtils implements Serializable {
    
    private Intensity workingIntensityClass;
    private Intensity selectedIntensityClass;
    private ArrayList<Intensity> existingIntensityList;
    private IntensitySchema selectedSchema;
    private ArrayList<IntensitySchema> schemaList;
    
    private boolean editing;
    
    public IntensityBB() {
        
    }
    
    @PostConstruct
    public void initBean() {
        
        if (workingIntensityClass == null) {
            
            workingIntensityClass = new Intensity();

            
        }
        
    }

    public void editIntensity(ActionEvent e){
        if(selectedIntensityClass != null){
            workingIntensityClass.setClassID(selectedIntensityClass.getClassID());
            workingIntensityClass.setTitle(selectedIntensityClass.getTitle());
            workingIntensityClass.setMuni(selectedIntensityClass.getMuni());
            workingIntensityClass.setNumericRating(selectedIntensityClass.getNumericRating());
            workingIntensityClass.setSchema(selectedIntensityClass.getSchema());
            workingIntensityClass.setActive(selectedIntensityClass.isActive());
            workingIntensityClass.setIcon(selectedIntensityClass.getIcon());
            
            editing = true;
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an Intensity Class to edit", ""));
        }
    }
    
    public void commitEdits() {
        
        SystemIntegrator si = new SystemIntegrator();
        
        try{
            si.updateIntensityClass(workingIntensityClass);
            
            workingIntensityClass = new Intensity();
            
            queryIntensityClasses();
            
            editing = false;
        } catch (IntegrationException ex){
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update Intensity Classes.",
                    "This must be corrected by the System Administrator"));
        }
        
    }
    
    public void queryIntensityClasses(){
        
        System.out.println("Intensity Classes Queried!");
        
        if(selectedSchema != null){
            
            SystemIntegrator si = new SystemIntegrator();
            
            try{
            existingIntensityList = (ArrayList<Intensity>) si.getIntensityClassList(selectedSchema);
        } catch (IntegrationException ex){
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to get Intensity Classes.",
                    "This must be corrected by the System Administrator"));
        }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an Intensity Schema", ""));
        }
        
    }
    
    public Intensity getWorkingIntensityClass() {
        return workingIntensityClass;
    }

    public void setWorkingIntensityClass(Intensity workingIntensityClass) {
        this.workingIntensityClass = workingIntensityClass;
    }

    public ArrayList<Intensity> getExistingIntensityList() {
        return existingIntensityList;
    }

    public void setExistingIntensityList(ArrayList<Intensity> existingIntensityList) {
        this.existingIntensityList = existingIntensityList;
    }

    public IntensitySchema getSelectedSchema() {
        return selectedSchema;
    }

    public void setSelectedSchema(IntensitySchema selectedSchema) {
        this.selectedSchema = selectedSchema;
    }

    public ArrayList<IntensitySchema> getSchemaList() {
        
        if(schemaList != null) {
        return schemaList;
        }
        else {
            
            SystemIntegrator si = new SystemIntegrator();
            
            try {
            schemaList = (ArrayList<IntensitySchema>) si.getIntensitySchemaList();
            } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,  
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load schema list",
                        "This must be corrected by the system administrator"));
        }
            return schemaList;
            
        }
        
    }

    public void setSchemaList(ArrayList<IntensitySchema> schemaList) {
        this.schemaList = schemaList;
    }

    public Intensity getSelectedIntensityClass() {
        return selectedIntensityClass;
    }

    public void setSelectedIntensityClass(Intensity selectedIntensityClass) {
        this.selectedIntensityClass = selectedIntensityClass;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
            
    
    
    
}
