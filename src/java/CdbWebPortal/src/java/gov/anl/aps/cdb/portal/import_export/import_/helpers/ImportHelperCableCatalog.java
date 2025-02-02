/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.helpers;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.ItemDomainCableCatalogController;
import gov.anl.aps.cdb.portal.controllers.SourceController;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ColumnModeOptions;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.ColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.CreateInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.IdOrNameRefColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.StringColumnSpec;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCableCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.Source;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author craig
 */
public class ImportHelperCableCatalog extends ImportHelperCatalogBase<ItemDomainCableCatalog, ItemDomainCableCatalogController> {

    @Override
    protected List<ColumnSpec> getColumnSpecs() {
        
        List<ColumnSpec> specs = new ArrayList<>();
        
        specs.add(new StringColumnSpec(
                "Name", 
                "name", 
                "setName", 
                "Cable type name, uniquely identifies cable type.", 
                null,
                ColumnModeOptions.rCREATErUPDATE(),
                128));
        
        specs.add(new StringColumnSpec(
                "Alt Name", 
                "alternateName", 
                "setAlternateName", 
                "Alternate cable type name.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                128));
        
        specs.add(new StringColumnSpec(
                "Description", 
                "description", 
                "setDescription", 
                "Textual description of cable type.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Documentation URL", 
                "urlDisplay", 
                "setUrl", 
                "Raw URL for documentation pdf file, e.g., http://www.example.com/documentation.pdf", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Image URL", 
                "imageUrlDisplay", 
                "setImageUrl", 
                "Raw URL for image file, e.g., http://www.example.com/image.jpg", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new IdOrNameRefColumnSpec(
                "Manufacturer", 
                ImportHelperCatalogBase.KEY_MFR, 
                "", 
                "ID or name of CDB source for manufacturer. Name must be unique and prefixed with '#'.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                SourceController.getInstance(), 
                Source.class, 
                ""));
        
        specs.add(new StringColumnSpec(
                "Part Number", 
                ImportHelperCatalogBase.KEY_PART_NUM, 
                "setPartNumber", 
                "Manufacturer's part number.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                32));
        
        specs.add(new StringColumnSpec(
                "Alt Part Num", 
                "altPartNumber", 
                "setAltPartNumber", 
                "Manufacturer's alternate part number, e.g., 760152413", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Diameter", 
                "diameter", 
                "setDiameter", 
                "Diameter in inches (max).", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Weight", 
                "weight", 
                "setWeight", 
                "Nominal weight in lbs/1000 feet.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Conductors", 
                "conductors", 
                "setConductors", 
                "Number of conductors/fibers", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Insulation", 
                "insulation", 
                "setInsulation", 
                "Description of cable insulation.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Jacket Color", 
                "jacketColor", 
                "setJacketColor", 
                "Jacket color.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Voltage Rating", 
                "voltageRating", 
                "setVoltageRating", 
                "Voltage rating (VRMS).", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Fire Load", 
                "fireLoad", 
                "setFireLoad", 
                "Fire load rating.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Heat Limit", 
                "heatLimit", 
                "setHeatLimit", 
                "Heat limit.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Bend Radius", 
                "bendRadius", 
                "setBendRadius", 
                "Bend radius in inches.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Rad Tolerance", 
                "radTolerance", 
                "setRadTolerance", 
                "Radiation tolerance rating.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Total Length", 
                "totalLength", 
                "setTotalLength", 
                "Total cable length required.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Reel Length", 
                "reelLength", 
                "setReelLength", 
                "Standard reel length for this type of cable.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Reel Quantity", 
                "reelQuantity", 
                "setReelQuantity", 
                "Number of standard reels required for total length.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Lead Time", 
                "leadTime", 
                "setLeadTime", 
                "Standard procurement lead time for this type of cable.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(new StringColumnSpec(
                "Procurement Status", 
                "procurementStatus", 
                "setProcurementStatus", 
                "Procurement status.", 
                null,
                ColumnModeOptions.oCREATEoUPDATE(), 
                256));
        
        specs.add(projectListColumnSpec());
        specs.add(technicalSystemListColumnSpec(ItemDomainName.cableCatalog.getValue()));
        specs.add(ownerUserColumnSpec());
        specs.add(ownerGroupColumnSpec());

        return specs;
    }
    
    @Override
    public ItemDomainCableCatalogController getEntityController() {
        return ItemDomainCableCatalogController.getInstance();
    }

    @Override
    public String getFilenameBase() {
        return "Cable Type Catalog";
    }
    
    @Override 
    protected ValidInfo preImport() {
        getEntityController().migrateCoreMetadataPropertyType();
        return new ValidInfo(true, "");
    }
    
    @Override
    protected CreateInfo createEntityInstance(Map<String, Object> rowMap) {
        return super.createEntityInstance(rowMap);
    }  
}
