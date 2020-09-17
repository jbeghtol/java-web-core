package com.ilsian.tomcat;

/**
 * TemplateInteraction - A hybrid interface for objects that can both prepare
 * a data model for a template AND service POST actions addressed to the template.
 * 
 * @author justin
 *
 */
public interface TemplateInteraction extends ActionHandler, TemplateDataFactory {

}
