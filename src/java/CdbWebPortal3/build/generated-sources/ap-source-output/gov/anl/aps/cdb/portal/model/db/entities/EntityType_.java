package gov.anl.aps.cdb.portal.model.db.entities;

import gov.anl.aps.cdb.portal.model.db.entities.EntityType;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2016-05-12T14:58:28")
@StaticMetamodel(EntityType.class)
public class EntityType_ { 

    public static volatile ListAttribute<EntityType, EntityType> entityTypeList1;
    public static volatile ListAttribute<EntityType, PropertyType> propertyTypeList;
    public static volatile SingularAttribute<EntityType, String> name;
    public static volatile SingularAttribute<EntityType, String> description;
    public static volatile ListAttribute<EntityType, Item> itemList;
    public static volatile SingularAttribute<EntityType, Integer> id;
    public static volatile ListAttribute<EntityType, EntityType> entityTypeList;

}