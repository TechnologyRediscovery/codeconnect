/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOB;
import java.util.List;

/**
 *
 * @author sylvia
 */
public interface Reportable<E> {

    /**
     * `
     * @return
     */
    public abstract List<E> retrieveBOBList();
    
}
