package org.orbisgis.orbisprocess.geoclimate.bdtopo_v2

import groovy.transform.BaseScript
import org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource
import org.orbisgis.orbisdata.processmanager.api.IProcess

@BaseScript BDTopo_V2_Utils bdTopo_v2_utils

/** The processing chain creates the Table from the Abstract model and feed them with the BDTopo data according to some
 * defined rules (i.e. certain types of buildings or vegetation are transformed into a generic type in the abstract
 * model). Then default values are set (or values are corrected) and the quality of the input data is assessed and
 * returned into a statistic table for each layer. For further information, cf. each of the four processes used in the
 * mapper.
 *
 * @param datasource A connexion to a database (H2GIS, PostGIS, ...), in which the data to process will be stored
 * @param distBuffer The distance (exprimed in meter) used to compute the buffer area around the ZONE
 * @param distance The distance (exprimed in meter) used to compute the extended area around the ZONE
 * @param idZone The Zone id
 * @param tableIrisName The table name in which the IRIS are stored
 * @param tableBuildIndifName The table name in which the undifferentiated ("Indifférencié" in french) buildings are stored
 * @param tableBuildIndusName The table name in which the industrial buildings are stored
 * @param tableBuildRemarqName The table name in which the remarkable ("Remarquable" in french) buildings are stored
 * @param tableRoadName The table name in which the roads are stored
 * @param tableRailName The table name in which the rail ways are stored
 * @param tableHydroName The table name in which the hydrographic areas are stored
 * @param tableVegetName The table name in which the vegetation areas are stored
 * @param tableImperviousSportName The table name in which the impervious sport areas are stored
 * @param tableImperviousBuildSurfName The table name in which the impervious surfacic buildings are stored
 * @param tableImperviousRoadSurfName The table name in which the impervious road areas are stored
 * @param tableImperviousActivSurfName The table name in which the impervious activities areas are stored
 * @param hLevMin Minimum building level height
 * @param hLevMax Maximum building level height
 * @param hThresholdLev2 Threshold on the building height, used to determine the number of levels
 *
 * @return outputBuilding Table name in which the (ready to be used in the GeoIndicators part) buildings are stored
 * @return outputRoad Table name in which the (ready to be used in the GeoIndicators part) roads are stored
 * @return outputRail Table name in which the (ready to be used in the GeoIndicators part) rail ways are stored
 * @return outputHydro Table name in which the (ready to be used in the GeoIndicators part) hydrographic areas are stored
 * @return outputVeget Table name in which the (ready to be used in the GeoIndicators part) vegetation areas are stored
 * @return outputImpervious Table name in which the (ready to be used in the GeoIndicators part) impervious areas are stored
 * @return outputZone Table name in which the (ready to be used in the GeoIndicators part) zone is stored
 *
 */
IProcess prepareData() {
    return create {
        title "Extract and transform BD Topo data to Geoclimate model"
        id "prepareData"
        inputs datasource: JdbcDataSource,
                distBuffer: 500,
                distance: 1000,
                idZone: String,
                tableIrisName: String,
                tableBuildIndifName: String,
                tableBuildIndusName: String,
                tableBuildRemarqName: String,
                tableRoadName: String,
                tableRailName: String,
                tableHydroName: String,
                tableVegetName: String,
                tableImperviousSportName: String,
                tableImperviousBuildSurfName: String,
                tableImperviousRoadSurfName: String,
                tableImperviousActivSurfName: String,
                tablePiste_AerodromeName: String,
                tableReservoirName: String,
                hLevMin: 3,
                hLevMax: 15,
                hThresholdLev2: 10
        outputs outputBuilding: String, outputRoad: String, outputRail: String, outputHydro: String, outputVeget: String, outputImpervious: String, outputZone: String
        run { datasource, distBuffer, distance, idZone, tableIrisName, tableBuildIndifName, tableBuildIndusName, tableBuildRemarqName, tableRoadName, tableRailName,
              tableHydroName, tableVegetName, tableImperviousSportName, tableImperviousBuildSurfName, tableImperviousRoadSurfName, tableImperviousActivSurfName,
              tablePiste_AerodromeName, tableReservoirName,hLevMin, hLevMax, hThresholdLev2 ->

            if(!hLevMin){
                hLevMin=3
            }
            if(!hLevMax){
                hLevMax = 15
            }
            if(!hThresholdLev2){
                hThresholdLev2 = 10
            }
            if (!datasource) {
                error "The database to store the BD Topo data doesn't exist"
                return
            }

            //Init model
            def initParametersAbstract = BDTopo_V2.initParametersAbstract
            if (!initParametersAbstract(datasource: datasource)) {
                error "Cannot initialize the geoclimate data model."
                return
            }
            def abstractTables = initParametersAbstract.results

            //Init BD Topo parameters
            def initTypes = BDTopo_V2.initTypes
            if (!initTypes([datasource             : datasource,
                            buildingAbstractUseType: abstractTables.outputBuildingAbstractUseType,
                            roadAbstractType       : abstractTables.outputRoadAbstractType, roadAbstractCrossing: abstractTables.outputRoadAbstractCrossing,
                            railAbstractType       : abstractTables.outputRailAbstractType, railAbstractCrossing: abstractTables.outputRailAbstractCrossing,
                            vegetAbstractType      : abstractTables.outputVegetAbstractType])) {
                error "Cannot initialize the BD Topo parameters."
                return
            }
            def initTables = initTypes.results

            //Import preprocess
            def importPreprocess = BDTopo_V2.importPreprocess
            if (!importPreprocess([datasource                  : datasource,
                                   tableIrisName               : tableIrisName,
                                   tableBuildIndifName         : tableBuildIndifName,
                                   tableBuildIndusName         : tableBuildIndusName,
                                   tableBuildRemarqName        : tableBuildRemarqName,
                                   tableRoadName               : tableRoadName,
                                   tableRailName               : tableRailName,
                                   tableHydroName              : tableHydroName,
                                   tableVegetName              : tableVegetName,
                                   tableImperviousSportName    : tableImperviousSportName,
                                   tableImperviousBuildSurfName: tableImperviousBuildSurfName,
                                   tableImperviousRoadSurfName : tableImperviousRoadSurfName,
                                   tableImperviousActivSurfName: tableImperviousActivSurfName,
                                   tablePiste_AerodromeName    : tablePiste_AerodromeName,
                                   tableReservoirName          : tableReservoirName,
                                   distBuffer                  : distBuffer, distance: distance, idZone: idZone,
                                   building_bd_topo_use_type   : initTables.outputBuildingBDTopoUseType,
                                   building_abstract_use_type  : abstractTables.outputBuildingAbstractUseType,
                                   road_bd_topo_type           : initTables.outputroadBDTopoType,
                                   road_abstract_type          : abstractTables.outputRoadAbstractType,
                                   road_bd_topo_crossing       : initTables.outputroadBDTopoCrossing,
                                   road_abstract_crossing      : abstractTables.outputRoadAbstractCrossing,
                                   rail_bd_topo_type           : initTables.outputrailBDTopoType,
                                   rail_abstract_type          : abstractTables.outputRailAbstractType,
                                   rail_bd_topo_crossing       : initTables.outputrailBDTopoCrossing,
                                   rail_abstract_crossing      : abstractTables.outputRailAbstractCrossing,
                                   veget_bd_topo_type          : initTables.outputvegetBDTopoType,
                                   veget_abstract_type         : abstractTables.outputVegetAbstractType])) {
                error "Cannot import preprocess."
                return
            }
            def preprocessTables = importPreprocess.results

            // Input data formatting and statistics
            def inputDataFormatting = BDTopo_V2.formatInputData
            if (!inputDataFormatting([datasource                : datasource,
                                      inputBuilding             : preprocessTables.outputBuildingName,
                                      inputRoad                 : preprocessTables.outputRoadName,
                                      inputRail                 : preprocessTables.outputRailName,
                                      inputHydro                : preprocessTables.outputHydroName,
                                      inputVeget                : preprocessTables.outputVegetName,
                                      inputImpervious           : preprocessTables.outputImperviousName,
                                      inputZone                 : preprocessTables.outputZoneName,
                                      //inputZoneNeighbors: preprocessTables.outputZoneNeighborsName,
                                      hLevMin                   : hLevMin, hLevMax: hLevMax, hThresholdLev2: hThresholdLev2, idZone: idZone,
                                      buildingAbstractParameters: abstractTables.outputBuildingAbstractParameters,
                                      roadAbstractParameters    : abstractTables.outputRoadAbstractParameters,
                                      vegetAbstractParameters   : abstractTables.outputVegetAbstractParameters])) {
                error "Cannot format data and compute statistics."
                return
            }

            debug "End of the BD Topo extract transform process."

            def finalBuildings = inputDataFormatting.results.outputBuilding
            def finalRoads = inputDataFormatting.results.outputRoad
            def finalRails = inputDataFormatting.results.outputRail
            def finalHydro = inputDataFormatting.results.outputHydro
            def finalVeget = inputDataFormatting.results.outputVeget
            def finalImpervious = inputDataFormatting.results.outputImpervious
            def finalZone = inputDataFormatting.results.outputZone

            [outputBuilding: finalBuildings, outputRoad: finalRoads, outputRail: finalRails, outputHydro: finalHydro,
             outputVeget   : finalVeget, outputImpervious: finalImpervious, outputZone: finalZone]

        }
    }
}