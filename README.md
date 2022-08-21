# UrbanMissionMap

UrbanMissionMap is an Android client including UAV parameter configuration management and real-time telemetry and remote control functions. It is installed on an Android tablet to control the formation of UAVs.

## System composition

| module name              | description                                                                                                    | development status |
| ------------------------ | -------------------------------------------------------------------------------------------------------------- | ------------------ |
| UAV management           | Register and manage the uav information that needs to be controlled                                            | New                |
| Formation management     | Configure the parameters of the formation locally and upload the on-board equipment through the ad hoc network | New                |
| System management        | Configure system parameters, such as default formation parameters, map coordinates, etc.                       | New                |
| Real-time telemetry      | Real-time display of registered UAV status, such as position, altitude, speed, attitude angle, etc.            | New                |
| Manual route planning    | Manually plan the flight route of the uav formation on the map and upload it through the ad hoc network        | New                |
| Automatic route planning | Plan the UAV flight route automatically avoiding the no-fly zone through the algorithm                         | New                |

## Sub-module description of UrbanMissionMap

### Start interface

![boot](img\boot.jpg) 

The startup interface is the first interface to enter UrbanMissionMap. The top center of the interface is the name and LOGO of the software; the gear-shaped button in the upper right corner, click to enter the formation and UAV management interface; Select the drop-down box to select the initial formation to enter the main control interface.

### UAV management

UAV management mainly manages the relevant information of the registered UAV, including the UAV's serial number, network address, payload video address, aircraft type, belonging formation and position in the team, etc., and stores it in the local database for inspection. After the information is correct, the formation information can be uploaded through the ad hoc network.

![UAVsetting](img\UAVsetting.jpg)

![newuav](img\newuav.jpg)

### Formation management

Formation management interface displays and configures UAV formation-related information, including serial number, name, scale, type, spacing, launch delay, escape delay, leader address, backup leader address, etc., and store it in the local database. After the information is checked, the formation information can be uploaded through the ad hoc network.

![swarmsetting](img\swarmsetting.jpg)

![newswarm](img\newswarm.jpg)

### System management

The system management interface is used to set the default parameters and system coordinate system of the newly created formation on the main control interface, including the formation default spacing, launch default delay, departure default delay and system coordinate.

![syssetting](img\syssetting.jpg)

### Real-time telemetry

The main control interface of the UAV swarm is mainly used to display the real-time position of the UAV, the real-time parameters of the UAV swarm, airborne video, etc.,  Support route planning, automatic route generation, attack point setting and generation, formation assembly, etc.

![flight](img\flight.jpg)

### Manual route planning

The manual route planning function is the most basic collaborative planning function, that is, manually click the waypoint on the map, upload the route information to the mission computer, and click to take off the UAV formation to automatically complete the route flight.

![waypoint](img\waypoint.jpg)

### Automatic route planning

The automatic route planning function is to manually set the no-fly zone on the map, manually click the take-off point and the end point of the route, and automatically plan a route that bypasses the no-fly zone through the path planning function, upload the route information to the mission computer, and click take off. The UAV formation automatically completes the route flight.
