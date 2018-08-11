
import com.rameses.rcp.annotations.*
import com.rameses.rcp.common.*
import com.rameses.osiris2.client.*
import com.rameses.osiris2.common.*
import com.rameses.annotations.Env
import com.rameses.common.*;
import com.rameses.seti2.models.*;
import com.rameses.util.*;

class FarmerModel extends CrudFormModel{
    @Binding
    def binding;
    
    @Service('DateService')
    def dtSvc
    
    @Service('SequenceService')
    def seqSvc
    
    @Service("PersistenceService")
    def persistenceSvc;
  
    @Service("FarmerProfileService")
    def svc;
    
    
    @Service('QueryService') 
    def querySvc; 

    
    boolean isAllowApprove() {
        return ( mode=='read' && entity.state.toString().matches('DRAFT|ACTIVE') ); 
    }
    
    def acquisition = ['OWNED', 'RENTED', 'LEASED', 'TENANT', 'OTHERS'];
    
    def selectedFarmLocation;
    def selectedFarmFacility;
    def selectedCommodity;
    
    public void afterCreate(){
        entity = svc.initCreate()
    }
    
    public void afterOpen(){
        if(entity.farmlocations){
            entity.farmlocations.each{
                it.putAll(persistenceSvc.read([_schemaname:'agri_farmerprofile_location',objid:it.objid]))
            }
        }
    }
   
    public void beforeSave(o){
        if(mode == 'create' ) {
            if(validateBeforeSave(entity)) throw new Exception("Farmer already exists");
        }
    }
  
    def farmLocationHandler  = [
        fetchList: { 
            return entity?.farmlocations;
        },
        onAddItem : {
            entity.farmlocations << it;
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')) {
                persistenceSvc.removeEntity([_schemaname:'agri_farmerprofile_location',objid:it.objid])
                entity.farmlocations.remove(it)
                farmLocationHandler.reload();
                return true;
            }
            return false;
        },
        onColumnUpdate: { o,col-> 
            o.dateoflastupdate = dtSvc.getServerDate();
            o.lastupdatedbyuser = OsirisContext.env.FULLNAME;
            o.lastupdatedbyuserid = OsirisContext.env.USERID;
            
            //kani para ma kumpleto ang address pag local
            if (col == 'location') { 
                if(o.location.type == 'local'){
                    o.location.text = svc.formatAddress(o.location,"\n")
                }
            } 
            
        },
    ] as EditorListModel;
    
    def getFarmLocationLookup(){
        def h = { o->
            selectedFarmLocation?.location = o;
        };
        def m = selectedFarmLocation?.location;
        if(!m) m = [:];
        return Inv.lookupOpener( "address:editor", [handler:h, entity:m] );
    }
    
    def farmCommodityHandler  = [
        fetchList: { 
            selectedFarmLocation?.commodities;
        },
        onAddItem : {
            selectedFarmLocation.commodities.add(it);
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')) {
                persistenceSvc.removeEntity([_schemaname:'master_commodity_variety',objid:it.objid])
                selectedFarmLocation.commodities.remove(it)
                farmCommodityHandler.reload();
                return true;
            }
            return false;
        }
    ] as EditorListModel;
    
    
    def farmFacilityHandler  = [
        fetchList: { o->
            return entity?.farmfacilities;
        },
        onAddItem : {
            entity.farmfacilities << it;
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')) {
                persistenceSvc.removeEntity([_schemaname:'agri_farmerprofile_facility',objid:it.objid])
                entity.farmfacilities.remove(it)
                farmFacilityHandler.reload();
                return true;
            }
            return false;
        },
        onColumnUpdate: { o,col-> 
            o.dateoflastupdate = dtSvc.getServerDate();
            o.lastupdatedbyuser = OsirisContext.env.FULLNAME;
            o.lastupdatedbyuserid = OsirisContext.env.USERID;
            
        },
    ] as EditorListModel;
    //test pani
   
    // farmerCommodityGroupItemHandler
    // farmerLivestockeGroupItemHandler
 
    
    //    def farmerItemHandler = [
    //        fetchList: { o->
    //            def p = [_schemaname: 'agri_pagrifarmer'];
    //            p.findBy = [ 'farmer_objid': entity.objid];
    //            p.select = "objid,farmer_objid,address_text,commodity_objid,commodity_name,commoditytype_objid,commoditytype_name,commoditysubtype_objid,commoditysubtype_name,tenurialstatus,qty,maintainer,remarks";
    //            if(!entity.farmerItems){
    //                entity.farmerItems = queryService.getList( p );
    //            }
    //            return entity.farmerItems;
    //        },
    //        createItem : {
    //            return[
    //                objid : 'FI' + new java.rmi.server.UID(),
    //                newitem : false,
    //            ]
    //        },
    //        onAddItem : {
    //            println it
    //            entity.farmerItems << it;
    //        },onColumnUpdate: {item,colname-> 
    //            if (colname == 'commodity') { 
    //                item.commoditytype = null;
    //                item.commoditysubtype = null;
    //            } 
    //            if (colname == 'commoditytype') { 
    //                item.commoditysubtype = null;
    //            }
    //            
    //        },
    //        onRemoveItem : {
    //            if (MsgBox.confirm('Delete item?')) {
    //                //service.deleteFarmerItems(it)
    //                entity.farmerItems.remove(it)
    //                farmerItemHandler.reload();
    //                return true;
    //            }
    //            return false;
    //        }
    //            
    //        
    //    ] as EditorListModel;
    //    
    
    /*
    def selectedFacilityItem;
        
    def facilityItemHandler = [
    fetchList: { o->
    def p = [_schemaname: 'test_pagrifarmerphf'];
    p.findBy = [ 'parentid': entity.objid];
    p.select = "objid,parentid,phf_objid,phf_name,phfnumber";
    if(!entity.facilityItems){
    entity.facilityItems = queryService.getList( p );
    }
    return entity.facilityItems;
    },
    createItem : {
    return[
    objid : 'FF' + new java.rmi.server.UID()
    ]
    },
    onAddItem : {
    entity.facilityItems << it;
    },
    onRemoveItem : {
    if (MsgBox.confirm('Delete item?')){                
    entity.facilityItems.remove(it);
    facilityItemHandler.reload();
    return true;
    }
    return false;
    }              
                         
    ] as EditorListModel;
     */
    //    
    //    ========== Lookup Location =========
    //    def getLookupAddress(){
    //        if(!selectedFarmerItem.address?.objid) {
    //            def h = { o->
    //                selectedFarmerItem.address = o;
    //            };
    //            def m = selectedFarmerItem.address;
    //            if(!m) m = [:];
    //            return Inv.lookupOpener( "address:editor", [handler:h, entity:m] );
    //        }
    //        else {
    //            def h = { o->
    //                o._schemaname = "entity_address";
    //                persistenceSvc.update( o );
    //                selectedFarmerItem.address = o;
    //            };
    //            def m = persistenceSvc.read( [_schemaname:'entity_address', objid:selectedFarmerItem.address.objid] );
    //            return Inv.lookupOpener( "address:editor", [handler:h, entity:m] );
    //            //binding.refresh();
    //           
    //        }
    //        
    //    }
    //    ========== Lookup Commodity =========
    def getLookupCommodity(){
        return InvokerUtil.lookupOpener('pagricommodity:lookup');
    }
   
    //         ========== Lookup type =========
    def getLookupPagritype(){
        return InvokerUtil.lookupOpener('test_pagricommodity_type:lookup', [parentid:selectedFarmerItem.commodity.objid])
    }

    
    //         ========== Lookup Subtype =========
    def getLookupPagrisubtype(){
        return Inv.lookupOpener('test_pagricommodity_subtype:lookup', [parentid:selectedFarmerItem.commoditytype.objid])
    }
    //       
    //       ========== Lookup Facility ========= 
    def getLookupPagriphf(){
        return Inv.lookupOpener('phf:lookup')
    }
        
    
    //    void changestate(){
    //        switch(entity.state) {
    //        case 'DRAFT':
    //            entity.state = 'APPROVED';
    //            break;
    //            
    ////        case 'FORREVIEW':
    ////            entity.state = 'FORAPPROVAL';
    ////            break;
    ////            
    ////        case 'FORAPPROVAL':
    ////            entity.state = 'APPROVE';
    ////            break;
    //        default:
    //            break;
    //        }
    //        fasSvc.changestate(entity)
    //   
    //    }

    //    void approve() { 
    //        if ( MsgBox.confirm('You are about to approve this information. Proceed?')) { 
    //            getPersistenceService().update([ 
    //                    _schemaname: 'agri_pagrifarmer', 
    //                    objid : entity.objid, 
    //                    state : 'APPROVED' 
    //                ]); 
    //            loadData(); 
    //        }
    //    }
    
    def validateBeforeSave(o){
        def p = [_schemaname: 'agri_farmerprofile'];
        def farmer = [:]
        farmer.objid = o.farmer.objid
        p.findBy = [farmer];
        return querySvc.findFirst(p);
    }
    /// LOOKUP COMMODITY FARMER MAIN PAGE
    def getLookupCommodityVariey(){
        return Inv.lookupOpener('master_commodity_variety:lookup')
    }
    

}