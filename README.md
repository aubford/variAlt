VariAlt is an Android flight computer app for hang gliding, paragliding, and other unpowered flight.  It's main feature is it's variometer, which gives the pilot information about their vertical velocity.  This is important because the primary goal in unpowered flight is to identify rising air that can either slow their descent or allow them to ascend.
The application also gives key data on altitude, including their heigh above an automatically or manually set landing zone.  The altitude is determined using the built-in barometer and is calibrated using a call to the Geonames API, which connects to the pilot's nearest weather reporting station.
After the pilot ends their flight, VariAlt gives a readout of various data points about the flight.  It uses the Google Maps API to show the pilot's flight path.  The pilot can then save this readout to his phone's local storing using a SQLite database to view later. 