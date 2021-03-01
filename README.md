# Campsite Reservation

- Update the application.yaml with the Oracle DB connection, and update the server port of needed.
- Run the DB migration script `/src/main/resources/db/migration/V0__init.sql` to create the required DB objects
- Start the server from root directory using `./gradlew bootRun`
---
## Entity Descriptons

CAMP_BOOKING - Pre populated table with all the available dates for the camp.
BOOKING - Store the booking details

---
## API

---

### Get Campsite availability
`GET /campsite/availability`

Query Params (Optional or both are required)

    from - From date (dd-MMM-yyyy)
    to  - To date (dd-MMM-yyyy)
Response

List of dates the campsite is available within the date range

    [
        "01-Mar-2021".......
    ]

    
### Book Campsite
`POST /campsite/booking`

Request

    {
        "email": "test@user.com",
        "fullName": "Test User",
        "checkIn": "01-Mar-2021",
        "checkOut": "03-Mar-2021",
    }

Response

    {
        "bookingId": <generatedUniqueId>
        "email": "test@user.com",
        "fullName": "Test User",
        "checkIn": "01-Mar-2021",
        "checkOut": "03-Mar-2021",
    }


### Update Campsite booking
`PATCH /campsite/booking/{bookingId}`

Request

    {
        "checkIn": "01-Mar-2021",
        "checkOut": "03-Mar-2021",
    }

Response

    {
        "bookingId": <bookingId>
        "email": "test@user.com",
        "fullName": "Test User",
        "checkIn": "01-Mar-2021",
        "checkOut": "03-Mar-2021",
    }


### Cancel Campsite booking
`DELETE /campsite/booking/{bookingId}`

Response - 200

    {
        "message":"Booking has been cancelled successfully"
    }

---
### Test Cases to show handling of concurrent requests for the same/overlapping dates
While booking the reservations a PESSIMISTIC WRITE lock is acquired on the CAMP dates (on the CAMP_BOOKING table) for which the booking is requested for so that no other request can book the sames dates.
If the reservation is successful the dates are marked as unavailable and any other request(s) trying to book same dates will find them as unavailable or get a LockTimeOutException.
If the reservation is unsuccessful, the dates would be still be available and any other request waiting to book (for the lock) will acquire the lock on those dates and continue with the reservation.

Above scenarios can simulated and tested by uncommenting the line 54 in `ReservationService.java` (so that we can have a particular date locked by a request while other comes in)
and changing the lock timeout (`CampBookingRepo.java`) values.

#### Test Case 1
- Set Lock Timeout - 2 sec
- Set Wait - 10 sec
- Fire two requests to book the camp for same available dates
- Since lock timeout is less than the wait - the second request trying to book the same dates will fail with LockTimeOutException and return error `Unable to reserve camp for requested date` to the user.

#### Test Case 2
- Lock Timeout - 10 sec
- Wait - 5 sec
- Fire two requests to book the camp for same available dates
- First Request that acquired the lock is able to complete the reservation.
- Second request waiting to get the lock will eventually find the dates are not available and return `Campsite is not available for the requested dates`

#### Test Case 3
- Lock Timeout - 10 sec
- Wait - 5 sec
- Fire two requests to book the camp for with overlapping dates. 
- First request should overlap with one date which is not available so that its not able to complete the reservation
- Second request should have valid available date range
- First Request that acquired the lock is not able to complete the reservation.
- Second Request waiting for the lock gets the lock on those dates and continues with the reservation
