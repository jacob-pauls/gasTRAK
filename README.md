# gasTRAK-App
**Gas Station Tracking/Pricing Information Project for CSIS 3175**

**DISCLAIMER: This was an extremely long project that I worked on from January 2019 - August 2019. It was my first time using version control. So expect the git configuration to be a little messy or dated. As of Spring 2020, I still need to commit the changes from August 2019. I still love this project with my whole heart, but definitely see fit to revamp this idea in a platform/framework for both iOS and Android. Doing this would also make code cleanup/code revamp a test of foresight as opposed to taking days to modify an Android specific app that I could make better elsewhere. Thanks!**

**Documentation for gasTRAK used as a final project**
- *Douglas College*
- *Course: CSIS 3175 (Introduction to Mobile App Development) & CSIS 4175 (Mobile App Development II)*
- *Created By: Jacob Pauls*

## OVERVIEW & FEATURES:

  - Programmed in Java using the Android Studio framework
  - Utilizes Google Maps, Google Places, and location services on android devices to perform functions
  
  - Trips and Gas Pricing
    - Users can set a trip and view gas prices that are uploaded by users to a database
    - After each trip, they enter in the price they paid and the amount of litres they purchased
  
  - Vehicle Profiles
    - User must create a vehicle profile to be inserted into the internal database. This information is used when calculating/finding the best gas station in proximity.
  - Fuel Efficiency Calculator
    - User can enter in the amount of kilometers they travel on a full tank, and the number of litres in their tank to find the fuel efficiency of their vehicle.
  - Previous Trips
    - All trips committed by the user are stored in a database. The user can see all previous trips and the various information stored in the database for each trip.
  - Upload Fuel Prices
    - Users can enter in the name, address, and city of a gas station to upload and commit the fuel price to the database. The gas station must presently be in the database for any prices to commit.
  - Nearby Search Button (Arrow Icon)
    - Google Places performs a nearby search and uploads any nearby gas stations that are not in the databse to the database. Then, gas stations are placed in a List of HashMap values (MarkerOptions, TotalCost).
    - These HashMaps are compared to each other based on the calculated TotalCost to arrive at the gas station and are sorted.
    - The hue of the marker is updated within each HashMap's MarkerOptions in accordance to its place in the sorted list of HashMaps. In this way, there will always be the WORST gas station (hue 0 - red) and there will always be the BEST gas station (hue 120 - green).
    
## POSSIBLE ADDITIONS:

  - Fuel Statistics:
    - The amount of useful data this application collects is significant. The inclusion of fuel statistics can help show ACTUAL value in downloading and consistently using the app. Figures to be considered would include Total Savings, most frequent gas station, cost per week/month, avg fuel efficiency, etc.
    
## ADDED IN SUMMER 2019 (NOT COMMITTED):
  
  - Worked on server-side/external databases:
    - All gas-related information is stored in an AWS EC2 server with a MySQL database.
    - All location/places/gas station information is stored in a Firebase.
  - Added autocomplete/location suggestions when searching for gas stations.
  - Fixed marker algorithm (sorting isn't as accurate as it could be in Winter 2020 release, sometimes colour mixups occurred)
  - Refined UI, only one search bar is present along with the drawer for easy navigation.
  - **Unfinished:** Started working on gas station notifications:
    - User can request to receive notifications if a gas station price drops below a certain threshold within a given timespan.
  - Fixed various bugs/small detail work present from Winter 2020 release.   
    
## REFERENCES:

  - Google's own documentation/Github is brilliant in helping to understand some of the intricacies and complexities of their API:
    https://developers.google.com/maps/documentation/
  - CodingWithMitch has an excellent set of tutorials as to how to start using Google Maps, Google Places, etc:
    https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
  - TechAcademy has excellent tutorials on Google Places and nearby places searches:
    https://www.youtube.com/watch?v=_Oljjn1fIAc
