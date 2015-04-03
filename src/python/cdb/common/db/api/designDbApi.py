#!/usr/bin/env python

from cdb.common.exceptions.cdbException import CdbException
from cdb.common.db.api.cdbDbApi import CdbDbApi
from cdb.common.db.impl.designHandler import DesignHandler
from cdb.common.db.impl.designElementHandler import DesignElementHandler
from cdb.common.db.impl.componentHandler import ComponentHandler
from cdb.common.db.impl.locationHandler import LocationHandler
from cdb.common.db.impl.userInfoHandler import UserInfoHandler
from cdb.common.db.impl.userGroupHandler import UserGroupHandler

class DesignDbApi(CdbDbApi):

    def __init__(self):
        CdbDbApi.__init__(self)
        self.designHandler = DesignHandler()
        self.designElementHandler = DesignElementHandler()
        self.componentHandler = ComponentHandler()
        self.locationHandler = LocationHandler()
        self.userInfoHandler = UserInfoHandler()
        self.userGroupHandler = UserGroupHandler()

    @CdbDbApi.executeQuery
    def getDesigns(self, **kwargs):
        session = kwargs['session']
        dbDesigns = self.designHandler.getDesigns(session)
        return self.toCdbObjectList(dbDesigns)

    @CdbDbApi.executeQuery
    def getDesignById(self, id, **kwargs):
        session = kwargs['session']
        dbDesign = self.designHandler.getDesignById(session, id)
        return dbDesign.getCdbObject()

    @CdbDbApi.executeQuery
    def getDesignByName(self, name, **kwargs):
        session = kwargs['session']
        dbDesign = self.designHandler.getDesignByName(session, name)
        return dbDesign.getCdbObject()

    @CdbDbApi.executeTransaction
    def addDesign(self, name, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, description, **kwargs):
        session = kwargs['session']
        dbDesign = self.designHandler.addDesign(session, name, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, description)
        return dbDesign.getCdbObject()

    # This method is meant for adding designs via spreadsheets
    @CdbDbApi.executeTransaction
    def loadDesign(self, name, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, description, designElementList, **kwargs):
        session = kwargs['session']
        dbDesign = self.designHandler.addDesign(session, name, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, description)

        # Verify users/group.
        self.userInfoHandler.findUserInfoById(session, createdByUserId)
        self.userInfoHandler.findUserInfoById(session, ownerUserId)
        dbUserGroup = self.userGroupHandler.findUserGroupById(session, ownerGroupId)

        # Go over all design elements
        for designElementDict in designElementList: 
            designElementName = designElementDict.get('name')
            description = designElementDict.get('description')
            sortOrder = designElementDict.get('sortOrder')
            parentDesignId = dbDesign.id

            # Add child design if possible
            childDesignId = designElementDict.get('childDesignId')
            childDesignName = designElementDict.get('childDesignName')
            childDesignId = self.designHandler.findDesignIdByIdOrName(session, childDesignId, childDesignName)
                
            # Add component if possible
            componentId = None
            if childDesignId is None:
                componentId = designElementDict.get('componentId')
                componentName = designElementDict.get('componentName')
                componentId = self.componentHandler.findComponentIdByIdOrName(session, componentId, componentName)

            # Add location if possible
            locationId = designElementDict.get('locationId')
            locationName = designElementDict.get('locationName')
            locationId = self.locationHandler.findLocationIdByIdOrName(session, locationId, locationName)

            # Add design element
            dbDesignElement = self.designElementHandler.addUnverifiedDesignElement(session, designElementName, parentDesignId, childDesignId, componentId, locationId, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, sortOrder, description)

            # Go over design element properties 
            designElementPropertyList = designElementDict.get('propertyList', [])
            designElementId = dbDesignElement.id
            for designElementPropertyDict in designElementPropertyList: 
                propertyTypeName = designElementPropertyDict.get('name') 
                tag = designElementPropertyDict.get('tag')
                value = designElementPropertyDict.get('value')
                units = designElementPropertyDict.get('units')
                description = designElementPropertyDict.get('description')
                enteredByUserId = createdByUserId 
                dbDesignElementProperty = self.designElementHandler.addUnverifiedDesignElementProperty(session, designElementId, propertyTypeName, tag, value, units, description, enteredByUserId)
                        
        # Done
        return dbDesign.getCdbObject()

    @CdbDbApi.executeQuery
    def getDesignElements(self, designId, **kwargs):
        session = kwargs['session']
        dbDesignElements = self.designElementHandler.getDesignElements(session, designId)
        return self.toCdbObjectList(dbDesignElements)

    @CdbDbApi.executeTransaction
    def addDesignElement(self, name, parentDesignId, childDesignId, componentId, locationId, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, sortOrder, description, **kwargs):
        session = kwargs['session']
        dbDesignElement = self.designElementHandler.addDesignElement(session, name, parentDesignId, childDesignId, componentId, locationId, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, sortOrder, description)
        return dbDesignElement.getCdbObject()

    @CdbDbApi.executeTransaction
    def addDesignElementProperty(self, designElementId, propertyTypeName, tag, value, units, description, enteredByUserId, **kwargs):
        session = kwargs['session']
        dbDesignElementProperty = self.designElementHandler.addDesignElementProperty(session, designElementId, propertyTypeName, tag, value, units, description, enteredByUserId)
        return dbDesignElementProperty.getCdbObject()

#######################################################################
# Testing.
if __name__ == '__main__':
    api = DesignDbApi()
    designs = api.getDesigns()
    for design in designs:
        print
        print "********************"
        print design
        print "TEXT"
        print design.getTextRep()
        print "DICT"
        print design.getDictRep()
        print "JSON"
        print design.getJsonRep()

    print 'Getting design'
    design = api.getDesignById(1)
    print design.getDictRep()

    #print 'Adding design'
    #design = api.addDesign(name='ab5', createdByUserId=4, ownerUserId=4, ownerGroupId=3, isGroupWriteable=True, description='Test Design')
    #print "Added Design"
    #print design

    #print 'Getting design elements'
    #parentDesignId = 1
    #designElements = api.getDesignElements(parentDesignId)
    #for designElement in designElements:
    #    print
    #    print "********************"
    #    print designElement

    #print 'Adding design element'
    #designElement = api.addDesignElement(name='e7', parentDesignId=1, childDesignId=2, componentId=None, locationId=None, createdByUserId=4, ownerUserId=4, ownerGroupId=3, isGroupWriteable=True, sortOrder=111.123, description='Test Design Element')
    #print "Added Design Element"
    #print designElement

    #print 'Adding design element property'
    #designElementProperty = api.addDesignElementProperty(designElement['id'], propertyTypeName='length', tag='Test property', value='133', units='mm', description='Test desc', enteredByUserId=4)
    #print "Added design element property"
    #print designElementProperty

    print 'Loading design'
    designElementList = [
        { 'name' : 'e1',
          'componentId' : 3,
          'locationId' : 3,
          'description' : 'element 1',
          'sortOrder' : 1.0,
          'propertyList' : [
              {
                  'propertyTypeName' : 'alphax',
                  'value' : '1.123',
              },
              {
                  'propertyTypeName' : 'alphay',
                  'value' : '3.234',
              },
          ]
        },
        { 'name' : 'e2',
          'componentId' : 4,
          'locationId' : 4,
          'description' : 'element 2',
          'sortOrder' : 2.0,
          'propertyList' : [
              {
                  'propertyTypeName' : 'alphax',
                  'value' : '7.234',
              },
              {
                  'propertyTypeName' : 'alphay',
                  'value' : '8.235',
              },
          ]
        },
    ]
    design = api.loadDesign(name='sv2', createdByUserId=4, ownerUserId=4, ownerGroupId=3, isGroupWriteable=True, description='Loaded Design', designElementList=designElementList)
    print "Added Design"
    print design


