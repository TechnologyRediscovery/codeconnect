# Configuring the database for initialization on OccBeta

In order to login to codeNforce, a series of entries must be made in database tables to properly bootstrap the User and Municipality objects. This page will walk you through these configurations. To allow the proper connection of tables with keys, this sequence will first address the utility tables on which the main object tables depend and work its way inward to municipality.

## Background on the munilogin table
This table stores highly detailed information about the permissions allowed to each user in each municipality in which they work by declaring a start and stop date that each level of permissions. The table itself looks like a beast since it stores two dates and a few boolean flags for each user role but the pattern is simple: make sure today's date is included inside the start and end date bounds of the user rank you want to be granted upon login. 

`supportdatestart` and `supportdatestop` correspond with developer permissions--so for most testing purposes, it's wise to have today's date included in these bounds.

The initialization system embedded in the UserCoordinator uses the UserAccessRecord object (which stores the start and stop dates for each permitted role) to calculate the highest rank allowed for each user it is asked to configure. 

## Table configuration steps 

11. Populate the table `muniprofile` with an arbitrary entry. You could call this the default muni profile. MuniProfiles allow us to group municipality configuration settings under a name that is separate from the municipality itself, facilitating reuse of setting groupings across municipalities. The default of 0 on `continuousoccupancybufferdays` will be fine. Set `minimumuserranktodeclarerentalintent` and `minimumuserrankforinspectionoverrides` to 3 for now (which corresponds with code officers)

11. Update your municipality record for COGLand (or any other muni). With the occbeta updates, the municipality table now stores much more system-config info that must be set properly for a Municipality object to be loaded, whcih must occur for a User login. `activeinprogram` must be true. Default code set is keyed to any codeset record. `occpermitissuingsource_sourceid` can be any codesource record. Now that we have a muniprofile created, we can key to it in the `profile_profileid` foreign key field in our municipality table. Flip all the enable flags to true. `munimanager_userid` should point to yourself for testing. `office_propertyid` is the default property used on login and will generally point to the parcel on which the borough or township office exists. The system does not care which property this is, however. It will be the default property loaded when you visit properties.xhtml. The other colums are keyed as indiciated by checking PGAdmin on them.

11. Populate the `munilogin` table with an entry keyed to a record in the login for your test user. `defaultmuni` should be set to true. `accessgranteddatestart` and `accessgranteddatestop` must include today's date  and can be set to an arbitrarily far away date. As mentioned above the `supportstartdate` and `supportstopdate` will be mapped to developer permissions, so be sure to have today in this range, too. The `bypassXXX` flags are not wired up yet and can stay false. Stamp `recorddeactivatedts` with now()--this stamp can be set to NULL to inactivate a record. `userrole` is deprecated given the addition of the start/stop ranges, but set this to `Developer` to be safe. `muniloginrecordid` is the table's PK accidentally included later in the default tablec column order. `recordcreatedts` defaults to now(). `defaultcecase_caseid` must be keyed to a record in the cecase table and will be used on first load.

## Attempt a login and check key methods
Since there are so many configurations that occur on login, a key may be missing or field incorrectly configured so be patient as you check for errors. The method `configureUserMuniAccess` in `UserCoordinator` implements logic associated with the `AccessRecord` objects and may be useful to read through. Additionally, most of the heavy work of the login happens in the `SessionInitializer` object's `initiateInternalSession` method and `populateSessionObjectQueues` method. Running a debugger session with a breakpoint in each of these methods and stepping through the lines may be a useful exercise to internalize how many different elements of the system are called on just to load the dashboard. Please call Eric if you're ramming your head against the wall. 


