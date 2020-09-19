# gasTRAK-App
**Gas Station Tracking/Pricing Information Project for CSIS 3175/CSIS 4175**

**DISCLAIMER: This was an extremely long project that I worked on from June 2019 - December 2019. It was my first time using version control, as a result, the final commit with the Fall 2019 features was committed all at once. I still love this project with my whole heart, but definitely see fit to revamp this idea in a platform/framework for both iOS and Android. Thanks!**

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
  
**Added in Fall 2019:**
 
  - Worked on server-side/external databases:
    - All gas-related information is stored in an AWS EC2 server with a MySQL database.
    - All location/places/gas station information is stored in a Firebase.
  - Added autocomplete/location suggestions when searching for gas stations.
  - Fixed marker algorithm (sorting isn't as accurate as it could be in Summer 2019 release, sometimes colour mixups occurred)
  - Refined UI, only one search bar is present along with the drawer for easy navigation.
  - **Unfinished:** Started working on gas station notifications:
    - User can request to receive notifications if a gas station price drops below a certain threshold within a given timespan.
  - Miscellaneous bug fixes and detail improvements leftover from Summer 2019.
  
## POSSIBLE ADDITIONS:

  - Fuel Statistics:
    - The amount of useful data this application collects is significant. The inclusion of fuel statistics can help show ACTUAL value in downloading and consistently using the app. Figures to be considered would include Total Savings, most frequent gas station, cost per week/month, avg fuel efficiency, etc.  
    
## RETROSPECTIVE (as of Fall 2020)

**When I started gasTRAK, I had no idea that the idea would manifest as well as it did. Looking back on it in Fall 2020, there are two main - "general" - takeaways that I've taken and applied to my other projects:**

  - Code with maintainability in mind first. Focus less on the fact that a particular method, section of code, or algorithm "works" the first time creating it. Break your code down into efficient, effective, and sustainable code that promotes high-reuse (instead of using an enumerable amount of overloaded methods for each possible situation/use case).
  
  - Using version control from the start, using it consistently, and using it correctly. At the start, git and GitHub were simply used as places to store backups for this project. Even while working alone, the ability to see all feature changes and progress would have been extremely useful during the lifecycle of this project.
  
**As of Fall 2020, gasTRAK is still (what I would consider) to be a valuable idea. It's quite evident that the future development of gasTRAK would have to be using a framework such as React Native or Flutter in order to have availability for both Android and iOS.**

**However, if I were to ever "redo" or at least "re-engineer" gasTRAK, it would be with maintainability in mind from the start. It would involve a team, a planning phase, and would be executed with a specific vision in mind. The best part about a college project is that you can afford self discovery, remain constantly curious, and deviate from the features.** 

**As a result of my time spent being curious, I have a plethora of different ideas and features that may have been missed if this was a "serious" (business related) project from the start.**
	
## REFERENCES:

  - Google's own documentation/Github is brilliant in helping to understand some of the intricacies and complexities of their API:
    https://developers.google.com/maps/documentation/
  - CodingWithMitch has an excellent set of tutorials as to how to start using Google Maps, Google Places, etc:
    https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
  - TechAcademy has excellent tutorials on Google Places and nearby places searches:
    https://www.youtube.com/watch?v=_Oljjn1fIAc
