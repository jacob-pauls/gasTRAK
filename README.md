# gasTRAK-App
**Gas Station Tracking/Pricing Information Project for CSIS 3175**

**DISCLAIMER: This was an extremely long project that I am still working on and have ideas for. Test it out for yourself! Simply because this documentation does if very little justice.**

**Documentation for gasTRAK used as a final project**
- *Douglas College*
- *Course: CSIS 3175 (Introduction to Mobile App Development)*
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
  - External Database for Pricing Data:
    - Would be a permanent database to hold record for each gas stations price (trip and vehicle data could stay local to the device)
  - Other:
    - Create new vehicle profile FROM Fuel Efficiency Calculator using generated number
    - Use the price submitted at the end of a trip upload to the database as the the map price. This would reduce the necessity for users to explicitly upload prices through the app.
    
## REFERENCES:

  - Google's own documentation/Github is brilliant in helping to understand some of the intricacies and complexities of their API:
    https://developers.google.com/maps/documentation/
  - CodingWithMitch has an excellent set of tutorials as to how to start using Google Maps, Google Places, etc:
    https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
  - TechAcademy has excellent tutorials on Google Places and nearby places searches:
    https://www.youtube.com/watch?v=_Oljjn1fIAc
