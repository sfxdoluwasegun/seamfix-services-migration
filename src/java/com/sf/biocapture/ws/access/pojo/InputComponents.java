/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.access.pojo;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcel
 */
@XmlRootElement(name = "components")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputComponents implements Serializable {

    @XmlElement(name = "component")
    private List<InputComponent> inputComponents;

    public List<InputComponent> getInputComponents() {
        return inputComponents;
    }

    public void setInputComponents(List<InputComponent> inputComponents) {
        this.inputComponents = inputComponents;
    }

}
