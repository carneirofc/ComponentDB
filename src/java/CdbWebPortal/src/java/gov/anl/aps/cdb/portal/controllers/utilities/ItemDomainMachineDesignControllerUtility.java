/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.utilities;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.model.db.beans.EntityTypeFacade;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainMachineDesignFacade;
import gov.anl.aps.cdb.portal.model.db.entities.CdbEntity;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainInventory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.utilities.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author darek
 */
public class ItemDomainMachineDesignControllerUtility extends ItemControllerUtility<ItemDomainMachineDesign, ItemDomainMachineDesignFacade> {
    
    private static final Logger logger = LogManager.getLogger(ItemDomainMachineDesignControllerUtility.class.getName());
    
    EntityTypeFacade entityTypeFacade; 

    public ItemDomainMachineDesignControllerUtility() {
        super();
        entityTypeFacade = EntityTypeFacade.getInstance(); 
    }
    
    @Override    
    protected boolean verifyItemNameCombinationUniqueness(Item item) {
        boolean unique = super.verifyItemNameCombinationUniqueness(item);

        // Ensure all machine designs are unique
        if (!unique) {
            String viewUUID = item.getViewUUID();
            item.setItemIdentifier2(viewUUID);
            unique = true;
        }

        return unique;
    }
    
    @Override
    public void checkItem(ItemDomainMachineDesign item) throws CdbException {
        super.checkItem(item);

        if (item.getIsItemTemplate()) {
            List<ItemElement> itemElementMemberList = item.getItemElementMemberList();
            if (itemElementMemberList == null || itemElementMemberList.isEmpty()) {
                // Item is not a child of another item. 
                if (!verifyValidTemplateName(item.getName())) {
                    throw new CdbException("Place parements within {} in template name. Example: 'templateName {paramName}'");
                }
            }
        }

        Item newAssignedItem = item.getAssignedItem();
        if (newAssignedItem != null) {
            if ((newAssignedItem instanceof ItemDomainCatalog || newAssignedItem instanceof ItemDomainInventory) == false) {
                throw new CdbException("The new assigned item must be either catalog or inventory item.");
            }

            Integer itemId = item.getId();
            if (itemId != null) {                
                ItemDomainMachineDesign originalItem = findById(itemId);

                Item origAssignedItem = originalItem.getAssignedItem();

                if (origAssignedItem != null) {
                    ItemDomainCatalog catItem = null;
                    if (origAssignedItem instanceof ItemDomainInventory) {
                        catItem = ((ItemDomainInventory) origAssignedItem).getCatalogItem();
                    } else if (origAssignedItem instanceof ItemDomainCatalog) {
                        catItem = (ItemDomainCatalog) origAssignedItem;
                    }

                    if (newAssignedItem instanceof ItemDomainInventory) {
                        List<ItemDomainInventory> inventoryItemList = catItem.getInventoryItemList();
                        if (inventoryItemList.contains(newAssignedItem) == false) {
                            throw new CdbException("The new assigned inventory item must be of catalog item: " + catItem.getName() + ".");
                        }
                    }
                }
            }
        }
    }   
    
    private boolean verifyValidTemplateName(String templateName) {
        boolean validTitle = false;
        if (templateName.contains("{")) {
            int openBraceIndex = templateName.indexOf("{");
            int closeBraceIndex = templateName.indexOf("}");
            if (openBraceIndex < closeBraceIndex) {
                validTitle = true;
            }
        }
        
        return validTitle;
    }

    /**
     * Used by import framework.  Looks up entity by path.
     */
    @Override
    public ItemDomainMachineDesign findByPath(String path) throws CdbException {
        return findByPath_(path, ItemDomainMachineDesign::getParentMachineDesign);
    }
        
    @Override
    public boolean isEntityHasItemIdentifier2() {
        return false;
    }

    @Override
    public boolean isEntityHasQrId() {
        //TODO add a machine design template and inventory and override with false; 
        return true; 
    }

    @Override
    public boolean isEntityHasName() {
        return true; 
    }

    @Override
    public boolean isEntityHasProject() {
        return true; 
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.machineDesign.getValue(); 
    }

    @Override
    protected ItemDomainMachineDesignFacade getItemFacadeInstance() {
        return ItemDomainMachineDesignFacade.getInstance(); 
    }       

    @Override
    public String getDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    @Override
    public String getEntityTypeName() {
        return "itemMachineDesign";
    }

    @Override
    public String getDisplayEntityTypeName() {
        return "Machine Design Item";
    }     
    
    @Override
    protected ItemDomainMachineDesign instenciateNewItemDomainEntity() {
        return new ItemDomainMachineDesign();
    }
    
    public TreeNode getSearchResults(String searchString, boolean caseInsensitive) {
        LinkedList<SearchResult> searchResultList = this.performEntitySearch(searchString, caseInsensitive);
        return getHierarchicalSearchResults(searchResultList);
    }

    public TreeNode getHierarchicalSearchResults(LinkedList<SearchResult> searchResultList) {
        TreeNode searchResultsTreeNode; 
                
        TreeNode rootTreeNode = new DefaultTreeNode();
        if (searchResultList != null) {
            for (SearchResult result : searchResultList) {
                result.setRowStyle(SearchResult.SEARCH_RESULT_ROW_STYLE);

                ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) result.getCdbEntity();

                ItemDomainMachineDesign parent = mdItem.getParentMachineDesign();

                TreeNode resultNode = new DefaultTreeNode(result);

                List<ItemDomainMachineDesign> parents = new ArrayList<>();

                while (parent != null) {
                    parents.add(parent);
                    parent = parent.getParentMachineDesign();
                }

                TreeNode currentRoot = rootTreeNode;

                // Combine common parents 
                parentSearch:
                for (int i = parents.size() - 1; i >= 0; i--) {
                    ItemDomainMachineDesign currentParent = parents.get(i);

                    for (TreeNode node : currentRoot.getChildren()) {
                        Object data = node.getData();
                        SearchResult searchResult = (SearchResult) data;
                        CdbEntity cdbEntity = searchResult.getCdbEntity();
                        ItemDomainMachineDesign itemResult = (ItemDomainMachineDesign) cdbEntity;

                        if (itemResult.equals(currentParent)) {
                            currentRoot = node;
                            continue parentSearch;
                        }
                    }

                    // Need to create parentNode
                    SearchResult parentResult = new SearchResult(currentParent, currentParent.getId(), currentParent.getName());
                    parentResult.addAttributeMatch("Reason", "Parent of Result");

                    TreeNode newRoot = new DefaultTreeNode(parentResult);
                    newRoot.setExpanded(true);
                    currentRoot.getChildren().add(newRoot);
                    currentRoot = newRoot;
                }

                currentRoot.getChildren().add(resultNode);

                List<ItemElement> childElements = mdItem.getItemElementDisplayList();

                for (ItemElement childElement : childElements) {
                    Item mdChild = childElement.getContainedItem();
                    SearchResult childResult = new SearchResult(mdChild, mdChild.getId(), mdChild.getName());
                    childResult.addAttributeMatch("Reason", "Child of result");

                    TreeNode resultChildNode = new DefaultTreeNode(childResult);
                    resultNode.getChildren().add(resultChildNode);
                }
            }
        }
        searchResultsTreeNode = rootTreeNode;
        return searchResultsTreeNode;
    }

}
