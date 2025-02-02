/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.anl.aps.cdb.common.constants.ItemMetadataFieldType;
import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.constants.ItemElementRelationshipTypeNames;
import gov.anl.aps.cdb.portal.controllers.utilities.ItemDomainCableDesignControllerUtility;
import gov.anl.aps.cdb.portal.controllers.utilities.RelationshipTypeControllerUtility;
import gov.anl.aps.cdb.portal.model.db.beans.RelationshipTypeFacade;
import gov.anl.aps.cdb.portal.view.objects.ItemMetadataPropertyInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cmcchesney
 */
@Entity
@DiscriminatorValue(value = ItemDomainName.CABLE_DESIGN_ID + "")
public class ItemDomainCableDesign extends Item {

    private static final Logger LOGGER = LogManager.getLogger(ItemDomainCableDesign.class.getName());

    private transient String externalCableName = null;
    private transient String importCableId = null;
    private transient String alternateCableId = null;
    private transient String legacyQrId = null;
    private transient String laying = null;
    private transient String voltage = null;
    private transient String endpoint1Description = null;
    private transient String endpoint2Description = null;
    private transient String endpoint1Route = null;
    private transient String endpoint2Route = null;
    private transient String endpoint1Pinlist = null;
    private transient String endpoint2Pinlist = null;
    
    private transient List<ItemElementRelationship> deletedRelationshipList = null;
    
    public final static String CABLE_DESIGN_INTERNAL_PROPERTY_TYPE = "cable_design_internal_property_type";
    public final static String CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY = "externalCableName";
    public final static String CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY = "importCableId";
    public final static String CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY = "alternateCableId";
    public final static String CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY = "legacyQrId";
    public final static String CABLE_DESIGN_PROPERTY_LAYING_KEY = "laying";
    public final static String CABLE_DESIGN_PROPERTY_VOLTAGE_KEY = "voltage";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT1_DESC_KEY = "endpoint1Description";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT2_DESC_KEY = "endpoint2Description";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT1_ROUTE_KEY = "endpoint1Route";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT2_ROUTE_KEY = "endpoint2Route";
    
    private static ItemMetadataPropertyInfo connectionMetadataPropertyInfo = null;
    public final static String CABLE_DESIGN_CONNECTION_PROPERTY_TYPE = "cable_design_connection_property_type";
    public final static String CONNECTION_PROPERTY_DESCRIPTION_KEY = "description";
    public final static String CONNECTION_PROPERTY_ROUTE_KEY = "route";
    public final static String CONNECTION_PROPERTY_PINLIST_KEY = "pinlist";

    private static final String endpointsSeparator = " | ";

    @Override
    public Item createInstance() {
        return new ItemDomainCableDesign();
    } 

    @Override
    public ItemDomainCableDesignControllerUtility getItemControllerUtility() {
        return new ItemDomainCableDesignControllerUtility();
    }
    
    public List<ItemElementRelationship> getDeletedRelationshipList() {
        if (deletedRelationshipList == null) {
            deletedRelationshipList = new ArrayList<>();
        }
        return deletedRelationshipList;
    }
    
    public void clearDeletedRelationshipList() {
        if (deletedRelationshipList != null) {
            deletedRelationshipList.clear();
        }
    }

    private RelationshipType getCableConnectionRelationshipType(UserInfo userInfo) {
        RelationshipType relationshipType
                = RelationshipTypeFacade.getInstance().findByName(
                        ItemElementRelationshipTypeNames.itemCableConnection.getValue());
        if (relationshipType == null) {
            RelationshipTypeControllerUtility rtcu = new RelationshipTypeControllerUtility();             
            String name = ItemElementRelationshipTypeNames.itemCableConnection.getValue();
            try {
                relationshipType = rtcu.createRelationshipTypeWithName(name, userInfo);
            } catch (CdbException ex) {
                LOGGER.error(ex);
                return null; 
            }
        }
        return relationshipType;
    }

    /**
     * Creates ItemElementRelationship for the 2 specified items.
     *
     * @param item Machine design item for cable endpoint.
     * @return New instance of ItemElementRelationshipo for specified items.
     */
    private ItemElementRelationship createRelationship(Item item, float sortOrder, UserInfo userInfo) {
        ItemElementRelationship itemElementRelationship = new ItemElementRelationship();
        itemElementRelationship.setFirstItemElement(item.getSelfElement());
        itemElementRelationship.setSecondItemElement(this.getSelfElement());
        itemElementRelationship.setSecondSortOrder(sortOrder);

        RelationshipType cableConnectionRelationshipType = getCableConnectionRelationshipType(userInfo);
        itemElementRelationship.setRelationshipType(cableConnectionRelationshipType);

        return itemElementRelationship;
    }

    /**
     * Adds specified relationship for specified item.
     *
     * @param item Item to add relationship for.
     * @param ier Relationship to add.
     * @param secondItem True if the item is the second item in the
     * relationship.
     */
    private void addItemElementRelationshipToItem(Item item, ItemElementRelationship ier, boolean secondItem) {
        ItemElement selfElement = item.getSelfElement();
        List<ItemElementRelationship> ierList;
        if (secondItem) {
            ierList = selfElement.getItemElementRelationshipList1();
        } else {
            ierList = selfElement.getItemElementRelationshipList();
        }
        ierList.add(ier);
    }

    public ItemElementRelationship addCableRelationship(Item endpoint, Float sortOrder) {
        
        if (endpoint == null) {
            return null;
        }
        
        // calculate sortOrder if not provided
        if (sortOrder == null) {
            float maxSortOrder = this.getMaxRelationshipSortOrder();
            sortOrder = maxSortOrder + 1;
        }
        
        EntityInfo entityInfo = this.getEntityInfo();
        UserInfo ownerUser = entityInfo.getOwnerUser();
        
        // create relationships from cable to endpoints
        ItemElementRelationship relationship = createRelationship(endpoint, sortOrder, ownerUser);

        // Create list for cable's relationships. 
        ItemElement selfElement = this.getSelfElement();
        if (selfElement.getItemElementRelationshipList1() == null) {
            selfElement.setItemElementRelationshipList1(new ArrayList<>());
        }

        // Add appropriate item relationships to model.
        addItemElementRelationshipToItem(endpoint, relationship, false);
        addItemElementRelationshipToItem(this, relationship, true);
        
        return relationship;
    }
    
    private void removeCableRelationship(
            ItemElementRelationship relationship,
            boolean isImport) {
        
        this.getSelfElement().getItemElementRelationshipList1().remove(relationship);
        // remove relationship from old endpoint's relationship list
        relationship.getFirstItemElement().getItemElementRelationshipList().remove(relationship);
        if (isImport) {
            getDeletedRelationshipList().add(relationship);
        }
    }
    
    private void updateCableRelationshipToEndpoint(
            Item itemEndpoint,
            ItemElementRelationship cableRelationship,
            boolean isImport) {
        
        if (itemEndpoint == null) {
            // remove relationship from cable's relationship list
            removeCableRelationship(cableRelationship, isImport);
        } else {
            // update existing relationship with new endpoint
            updateCableRelationship(cableRelationship, itemEndpoint, null, null);
        }
    }
    
    public void updateCableRelationship(
            ItemElementRelationship cableRelationship, 
            Item itemEndpoint,
            ItemConnector endpointConnector,
            ItemConnector cableConnector) {
        
        cableRelationship.setFirstItemElement(itemEndpoint.getSelfElement());
        cableRelationship.setFirstItemConnector(endpointConnector);
        cableRelationship.setSecondItemConnector(cableConnector);
    }

    public void setEndpoint(Item itemEndpoint, float sortOrder, boolean isImport) {
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(sortOrder);
        if (cableRelationship != null) {
            updateCableRelationshipToEndpoint(itemEndpoint, cableRelationship, isImport);
        } else {
            if (itemEndpoint != null) {
                this.addCableRelationship(itemEndpoint, sortOrder);
            }
        }
    }

    public void setEndpoint1(Item itemEndpoint) {
        setEndpoint(itemEndpoint, 1.0f, false);
    }

    public void setEndpoint1Import(Item itemEndpoint) {
        setEndpoint(itemEndpoint, 1.0f, true);
    }

    public void setEndpoint1Id(String id) {
        ItemDomainMachineDesign itemEndpoint1 = (ItemDomainMachineDesign)(getEntityById(id));
        if (itemEndpoint1 != null) {
            setEndpoint1(itemEndpoint1);
        } else {
            LOGGER.error("setEndpoint1Id() unknown machine design item id " + id);
        }
    }

    public void setEndpoint2(Item itemEndpoint) {
        setEndpoint(itemEndpoint, 2.0f, false);
    }

    public void setEndpoint2Import(Item itemEndpoint) {
        setEndpoint(itemEndpoint, 2.0f, true);
    }

    public void setEndpoint2Id(String id) {
        ItemDomainMachineDesign itemEndpoint2 = (ItemDomainMachineDesign)(getEntityById(id));
        if (itemEndpoint2 != null) {
            setEndpoint2(itemEndpoint2);
        } else {
            LOGGER.error("setEndpoint2Id() unknown machine design item id " + id);
        }
    }

    public List<Item> getEndpointList() {
        ItemElement selfElement = this.getSelfElement();
        List<ItemElementRelationship> ierList
                = selfElement.getItemElementRelationshipList1();
        if (ierList != null) {
            // find just the cable relationship items
            RelationshipType cableIerType
                    = RelationshipTypeFacade.getInstance().findByName(
                            ItemElementRelationshipTypeNames.itemCableConnection.getValue());
            if (cableIerType != null) {
                return ierList.stream().
                        filter(ier -> ier.getRelationshipType().getName().equals(cableIerType.getName())).
                        sorted((ier1, ier2) -> (ier1.getSecondSortOrder() == null || ier2.getSecondSortOrder() == null) ? 0 : ier1.getSecondSortOrder().compareTo(ier2.getSecondSortOrder())).
                        map(ier -> ier.getFirstItemElement().getParentItem()).
                        collect(Collectors.toList());
            }
        }

        return null;
    }
    
    public ItemElementRelationship getCableConnectionBySortOrder(float sortOrder) {
        ItemElement selfElement = this.getSelfElement();
        List<ItemElementRelationship> ierList
                = selfElement.getItemElementRelationshipList1();
        if (ierList != null) {
            // find just the cable relationship items
            RelationshipType cableIerType
                    = RelationshipTypeFacade.getInstance().findByName(
                            ItemElementRelationshipTypeNames.itemCableConnection.getValue());
            if (cableIerType != null) {
                for (ItemElementRelationship rel : ierList) {
                    if ((rel.getRelationshipType().getName().equals(cableIerType.getName()))
                            && (rel.getSecondSortOrder() == sortOrder)) {
                        return rel;
                    }
                }
            }
        }
        return null;
    }

    public float getMaxRelationshipSortOrder() {
        float maxSortOrder = 0;
        ItemElement selfElement = this.getSelfElement();
        List<ItemElementRelationship> ierList
                = selfElement.getItemElementRelationshipList1();
        if (ierList != null) {
            // find just the cable relationship items
            RelationshipType cableIerType
                    = RelationshipTypeFacade.getInstance().findByName(
                            ItemElementRelationshipTypeNames.itemCableConnection.getValue());
            if (cableIerType != null) {
                for (ItemElementRelationship rel : ierList) {
                    if ((rel.getRelationshipType().getName().equals(cableIerType.getName()))
                            && (rel.getSecondSortOrder() > maxSortOrder)) {
                        maxSortOrder = rel.getSecondSortOrder();
                    }
                }
            }
        }
        return maxSortOrder;
    }

    /**
     * Returns a string containing the cables endpoints for display.
     */
    public String getEndpointsString() {
        String result = "";
        int count = 0;
        List<Item> iList = this.getEndpointList();
        for (Item endpoint : iList) {
            count = count + 1;
            result = result + endpoint.getName();
            if (count != iList.size()) {
                result = result + endpointsSeparator;
            }
        }
        return result;
    }

    public Item getEndpoint1() {
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(1.0f);
        if (cableRelationship != null) {
            return cableRelationship.getFirstItemElement().getParentItem();
        } else {
            return null;
        }
    }

    public String getEndpoint1String() {
        Item iEndpoint1 = this.getEndpoint1();
        if (iEndpoint1 != null) {
            return iEndpoint1.getName();
        } else {
            return "";
        }
    }

    public Item getEndpoint2() {
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
        if (cableRelationship != null) {
            return cableRelationship.getFirstItemElement().getParentItem();
        } else {
            return null;
        }
    }

    public String getEndpoint2String() {
        Item iEndpoint2 = this.getEndpoint2();
        if (iEndpoint2 != null) {
            return iEndpoint2.getName();
        } else {
            return "";
        }
    }

    public void setCatalogItem(Item itemCableCatalog) {
        // "assign" catalog item to cable design
        ItemElement selfElement = this.getSelfElement();
        selfElement.setContainedItem2(itemCableCatalog);
    }

    public void setCatalogItemId(String catalogItemId) {
        ItemDomainCableCatalog catalogItem = (ItemDomainCableCatalog) (getEntityById(catalogItemId));

        if (catalogItem != null) {
            setCatalogItem(catalogItem);
        } else {
            LOGGER.error("setCatalogItemId() unknown cable catalog item id " + catalogItemId);
        }
     }

    public Item getCatalogItem() {
        ItemElement selfElementCable = this.getSelfElement();
        return selfElementCable.getContainedItem2();
    }

    public String getCatalogItemString() {
        Item iCatalog = this.getCatalogItem();
        if (iCatalog != null) {
            return iCatalog.getName();
        } else {
            return "";
        }
    }

    private PropertyValue getInternalCableDesignPropertyValue() {
        List<PropertyValue> propertyValueList = getPropertyValueList();
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getPropertyType().getName().equals(CABLE_DESIGN_INTERNAL_PROPERTY_TYPE)) {
                return propertyValue;
            }
        }
        return null;
    }

    public String getAlternateName() {
        return getItemIdentifier1();
    }

    public void setAlternateName(String n) {
        setItemIdentifier1(n);
    }

    public void setTechnicalSystemList(List<ItemCategory> technicalSystemList) {
        setItemCategoryList(technicalSystemList);
    }
    @JsonIgnore
    public String getExternalCableName() throws CdbException {
        if (externalCableName == null) {
            externalCableName = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY);
        }
        return externalCableName;
    }

    public void setExternalCableName(String n) throws CdbException {
        externalCableName = n;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY, n);
    }

    @JsonIgnore
    public String getImportCableId() throws CdbException {
        if (importCableId == null) {
            importCableId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY);
        }
        return importCableId;
    }

    public void setImportCableId(String id) throws CdbException {
        importCableId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY, id);
    }

    @JsonIgnore
    public String getAlternateCableId() throws CdbException {
        if (alternateCableId == null) {
            alternateCableId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY);
        }
        return alternateCableId;
    }

    public void setAlternateCableId(String id) throws CdbException {
        alternateCableId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY, id);
    }

    @JsonIgnore
    public String getLegacyQrId() throws CdbException {
        if (legacyQrId == null) {
            legacyQrId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY);
        }
        return legacyQrId;
    }

    public void setLegacyQrId(String id) throws CdbException {
        legacyQrId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY, id);
    }

    @JsonIgnore
    public String getLaying() throws CdbException {
        if (laying == null) {
            laying = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LAYING_KEY);
        }
        return laying;
    }

    public void setLaying(String l) throws CdbException {
        laying = l;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LAYING_KEY, l);
    }

    @JsonIgnore
    public String getVoltage() throws CdbException {
        if (voltage == null) {
            voltage = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_VOLTAGE_KEY);
        }
        return voltage;
    }

    public void setVoltage(String v) throws CdbException {
        voltage = v;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_VOLTAGE_KEY, v);
    }

    @JsonIgnore
    public String getEndpoint1Description() throws CdbException {
        if (endpoint1Description == null) {
            ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(1.0f);
            if (cableRelationship != null) {
                endpoint1Description = getConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_DESCRIPTION_KEY);
            }
        }
        return endpoint1Description;
    }

    public void setEndpoint1Description(String description) throws CdbException {
        endpoint1Description = description;
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(1.0f);
        if (cableRelationship != null) {
            setConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_DESCRIPTION_KEY, description);
        }
    }
    
    @JsonIgnore
    public String getEndpoint2Description() throws CdbException {
        if (endpoint2Description == null) {
            ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
            if (cableRelationship != null) {
                endpoint2Description = getConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_DESCRIPTION_KEY);
            }
        }
        return endpoint2Description;
    }

    public void setEndpoint2Description(String description) throws CdbException {
        endpoint2Description = description;
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
        if (cableRelationship != null) {
            setConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_DESCRIPTION_KEY, description);
        }
    }
    
    @JsonIgnore
    public String getEndpoint1Route() throws CdbException {
        if (endpoint1Route == null) {
            ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(1.0f);
            if (cableRelationship != null) {
                endpoint1Route = getConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_ROUTE_KEY);
            }
        }
        return endpoint1Route;
    }

    public void setEndpoint1Route(String route) throws CdbException {
        endpoint1Route = route;
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(1.0f);
        if (cableRelationship != null) {
            setConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_ROUTE_KEY, route);
        }
    }
    
    @JsonIgnore
    public String getEndpoint2Route() throws CdbException {
        if (endpoint2Route == null) {
            ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
            if (cableRelationship != null) {
                endpoint2Route = getConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_ROUTE_KEY);
            }
        }
        return endpoint2Route;
    }

    public void setEndpoint2Route(String route) throws CdbException {
        endpoint2Route = route;
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
        if (cableRelationship != null) {
            setConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_ROUTE_KEY, route);
        }
    }
    
    
    @JsonIgnore
    public String getEndpoint2Pinlist() throws CdbException {
        if (endpoint2Pinlist == null) {
            ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
            if (cableRelationship != null) {
                endpoint2Pinlist = getConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_PINLIST_KEY);
            }
        }
        return endpoint2Pinlist;
    }

    public void setEndpoint2Pinlist(String pinlist) throws CdbException {
        endpoint2Pinlist = pinlist;
        ItemElementRelationship cableRelationship = getCableConnectionBySortOrder(2.0f);
        if (cableRelationship != null) {
            setConnectionPropertyFieldValue(cableRelationship, CONNECTION_PROPERTY_PINLIST_KEY, pinlist);
        }
    }
    
    private ItemMetadataPropertyInfo getConnectionPropertyInfo() {
        if (connectionMetadataPropertyInfo == null) {
            connectionMetadataPropertyInfo = new ItemMetadataPropertyInfo("Cable Connection Metadata", CABLE_DESIGN_CONNECTION_PROPERTY_TYPE);
            connectionMetadataPropertyInfo.addField(CONNECTION_PROPERTY_DESCRIPTION_KEY, "Description", "Connection description.", ItemMetadataFieldType.STRING, "", null);
            connectionMetadataPropertyInfo.addField(CONNECTION_PROPERTY_ROUTE_KEY, "Route", "Routing waypoint for connection.", ItemMetadataFieldType.STRING, "", null);
            connectionMetadataPropertyInfo.addField(CONNECTION_PROPERTY_PINLIST_KEY, "Pinlist", "Pin mapping details for connection.", ItemMetadataFieldType.STRING, "", null);
        }
        return connectionMetadataPropertyInfo;
    }

    public PropertyValue getConnectionPropertyValue(ItemElementRelationship ier) {

        ItemMetadataPropertyInfo info = getConnectionPropertyInfo();

        if (info != null) {
            List<PropertyValue> propertyValueList = ier.getPropertyValueList();
            if (propertyValueList == null) {
                return null;
            }
            for (PropertyValue propertyValue : propertyValueList) {
                if (propertyValue.getPropertyType().getName().equals(info.getPropertyName())) {
                    return propertyValue;
                }
            }
        }

        return null;
    }

    private void setConnectionPropertyFieldValue(
            ItemElementRelationship ier, String key, String value) throws CdbException {

        validateConnectionPropertyFieldKey(key);

        PropertyValue propertyValue = getConnectionPropertyValue(ier);

        if (propertyValue == null) {
            propertyValue = 
                    getItemControllerUtility().prepareConnectionPropertyValue(ier, getConnectionPropertyInfo());
        }
        
        if (value == null) {
            value = "";
        }
        
        propertyValue.setPropertyMetadataValue(key, value);
    }

    private void validateConnectionPropertyFieldKey(String key) throws CdbException {
        ItemMetadataPropertyInfo info = getConnectionPropertyInfo();
        if (!info.hasKey(key)) {
            throw new CdbException("Invalid metadata key used to get/set connection property field value: " + key);
        }
    }
    
    protected String getConnectionPropertyFieldValue(ItemElementRelationship ier, String key) throws CdbException {

        validateConnectionPropertyFieldKey(key);

        PropertyValue propertyValue = getConnectionPropertyValue(ier);
        if (propertyValue != null) {
            return propertyValue.getPropertyMetadataValueForKey(key);
        } else {
            return "";
        }
    }

}
