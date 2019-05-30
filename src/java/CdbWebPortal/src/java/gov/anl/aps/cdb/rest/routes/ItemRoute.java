/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.rest.routes;

import gov.anl.aps.cdb.common.exceptions.InvalidArgument;
import gov.anl.aps.cdb.common.exceptions.ObjectNotFound;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.ItemController;
import gov.anl.aps.cdb.portal.model.db.beans.DomainFacade;
import gov.anl.aps.cdb.portal.model.db.beans.ItemFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Domain;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainInventory;
import gov.anl.aps.cdb.portal.model.db.entities.Log;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.model.db.utilities.PropertyValueUtility;
import gov.anl.aps.cdb.portal.utilities.AuthorizationUtility;
import gov.anl.aps.cdb.rest.authentication.Secured;
import gov.anl.aps.cdb.rest.authentication.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author djarosz
 */
@Path("/items")
@Tag(name = "Item")
public class ItemRoute extends BaseRoute {

    @EJB
    ItemFacade itemFacade;

    @EJB
    DomainFacade domainFacade;

    @GET
    @Path("/ById/{id}")
    @Produces(MediaType.APPLICATION_JSON)    
    public Item getItemById(@PathParam("id") int id) {
        Item findById = itemFacade.findById(id);
        return findById;
    }

    @GET
    @Path("/ById/{id}/Status")
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyValue getItemStatus(@PathParam("id") int id) {
        Item item = itemFacade.findById(id);

        if (item instanceof ItemDomainInventory) {
            return ((ItemDomainInventory) item).getInventoryStatusPropertyValue();
        }

        return null;
    }

    @GET
    @Path("/ById/{id}/Permission")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public boolean verifyUserPermissionForItem(@PathParam("id") int id) {
        Item itemById = getItemById(id);
        if (itemById != null) {
            UserInfo user = getCurrentRequestUserInfo();
            if (user != null) {
                if (isUserAdmin(user)) {
                    return true;
                }
                return AuthorizationUtility.isEntityWriteableByUser(itemById, user);           
            }

        }
        return false;
    }

    @POST
    @Path("/UpdateDetails")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public Item updateItemDetails(Item item) {
        int itemId = item.getId();
        Item dbItem = getItemById(itemId);

        dbItem.setName(item.getName());
        dbItem.setItemIdentifier1(item.getItemIdentifier1());
        dbItem.setItemIdentifier2(item.getItemIdentifier2());
        dbItem.setDescription(item.getDescriptionFromAPI());
        dbItem.setItemTypeList(item.getItemTypeList());
        dbItem.setItemCategoryList(item.getItemCategoryList());

        ItemController itemDomainControllerForApi = dbItem.getItemDomainController(); 

        itemDomainControllerForApi.setApiUser(getCurrentRequestUserInfo());
        itemDomainControllerForApi.setCurrent(dbItem);
        itemDomainControllerForApi.update();

        return dbItem;
    }
    
    @POST
    @Path("/UpdateProperty/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public PropertyValue updateItemPropertyValue(@PathParam("itemId") int itemId, PropertyValue propertyValue) throws InvalidArgument, ObjectNotFound {
        Item dbItem = getItemById(itemId);
        UserInfo updatedByUser = getCurrentRequestUserInfo();
        
        ItemController itemController = dbItem.getItemDomainController(); 
        itemController.setApiUser(getCurrentRequestUserInfo());
        PropertyValue dbPropertyValue = null; 
        
        int propIdx = -1; 
        
        if (propertyValue.getId() == null) {
            PropertyType propertyType = propertyValue.getPropertyType();
            if (propertyType == null) {
                throw new InvalidArgument("Property type must be assigned to new property value."); 
            }
            dbPropertyValue = itemController.preparePropertyTypeValueAdd(dbItem, propertyType, null, null, updatedByUser);             
        } else {
            // Property already exists for the particular item.             
            for (int i = 0; i < dbItem.getPropertyValueList().size(); i++) {
                PropertyValue propertyValueIttr = dbItem.getPropertyValueList().get(i);
                if (propertyValueIttr.getId().equals(propertyValue.getId())) {
                    dbPropertyValue = propertyValueIttr; 
                    propIdx = i; 
                    break;
                }
            }                                    
        }
        
        if (dbPropertyValue == null) {
            throw new ObjectNotFound("There was an error trying to load the property value.");
        }
                        
        // Set passed in property value to match db property value 
        dbPropertyValue.setValue(propertyValue.getValue());
        dbPropertyValue.setDisplayValue(propertyValue.getDisplayValue());
        dbPropertyValue.setTag(propertyValue.getTag());
        dbPropertyValue.setDescription(propertyValue.getDescription());
        dbPropertyValue.setUnits(propertyValue.getUnits());
        dbPropertyValue.setIsDynamic(propertyValue.getIsDynamic());
        dbPropertyValue.setIsUserWriteable(propertyValue.getIsUserWriteable());                
        
        itemController.setCurrent(dbItem);
        itemController.update();
        
        dbItem = (Item) itemController.getCurrent(); 
        
        List<PropertyValue> pvList = dbItem.getPropertyValueList(); 
        if (propIdx > 0) {
            dbPropertyValue = pvList.get(propIdx); 
        } else {
            propIdx = pvList.size() -1; 
            dbPropertyValue = pvList.get(propIdx); 
        }
        
        return dbPropertyValue; 
    }

    @GET
    @Path("/ByQrId/{qrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Item getItemByQrId(@PathParam("qrId") int qrId) {
        Item findByQrId = itemFacade.findByQrId(qrId);
        return findByQrId;
    }

    @GET
    @Path("/PropertiesForItem/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyValue> getPropertiesForItem(@PathParam("itemId") int itemId) {
        Item itemById = getItemById(itemId);
        return itemById.getPropertyValueList();
    }

    @GET
    @Path("/LogsForItem/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Log> getLogsForItem(@PathParam("itemId") int itemId) {
        Item itemById = getItemById(itemId);
        return itemById.getLogList();
    }

    @GET
    @Path("/ImagePropertiesForItem/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyValue> getImagePropertiesForItem(@PathParam("itemId") int itemId) {
        Item itemById = getItemById(itemId);
        List<PropertyValue> propertyValueList = itemById.getPropertyValueList();
        return PropertyValueUtility.prepareImagePropertyValueList(propertyValueList);
    }

    @GET
    @Path("/ByDomain/{domainName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItemsByDomain(@PathParam("domainName") String domainName) {
        return itemFacade.findByDomain(domainName);
    }

    @GET
    @Path("/Catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainCatalog> getCatalogItems() {
        return (List<ItemDomainCatalog>) (List<?>) getItemsByDomain(ItemDomainName.catalog.getValue());
    }

    @GET
    @Path("/Inventory")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainInventory> getInventoryItems() {
        return (List<ItemDomainInventory>) (List<?>) getItemsByDomain(ItemDomainName.inventory.getValue());
    }

    @GET
    @Path("/Domains")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Domain> getDomainList() {
        return domainFacade.findAll();
    }

    private UserInfo getCurrentRequestUserInfo() {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (userPrincipal instanceof User) {
            UserInfo user = ((User) userPrincipal).getUser();
            return user; 
        }
        return null; 
    }

}
