package com.valise.mobile

import com.google.firebase.Timestamp
import com.valise.mobile.entities.City
import com.valise.mobile.entities.FSCity
import com.valise.mobile.entities.toCity
import com.valise.mobile.entities.toFSCity
import org.junit.Assert.assertNull
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CityTest {
    @Test
    fun `'test city initialization`() {
        // CITY 1
        val cityId1 =  UUID.fromString("49b85e74-9e5a-4552-962a-d16970dddd8b")
        val cityName1 = "Waterloo, ON, Canada"
        val startTime1 = 1730764800000
        val endTime1 = 1733356800000
        val placeId1 = "ChIJK2f-X1bxK4gRkB0jxyh7AwU"

        val city1 = City(cityId1, cityName1, startTime1, endTime1, placeId1)

        assertEquals(cityId1, city1.cityId)
        assertEquals(cityName1, city1.name)
        assertEquals(startTime1, city1.startTime)
        assertEquals(endTime1, city1.endTime)
        assertEquals(placeId1, city1.placeId)

        // CITY 2
        val cityId2 = UUID.randomUUID()
        val cityName2 = "Toronto, ON, Canada"
        val startTime2: Long? = 1730246400000
        val endTime2: Long? = 1730419200000
        val placeId2 = "ChIJpTvG15DL1IkRd8S0KlBVNTI"

        val city2 = City(cityId2, cityName2, startTime2, endTime2, placeId2)

        assertEquals(cityId2, city2.cityId)
        assertEquals(cityName2, city2.name)
        assertEquals(startTime2, city2.startTime)
        assertEquals(endTime2, city2.endTime)
        assertEquals(placeId2, city2.placeId)
    }

    @Test
    fun `test city equality`() {
        val cityId = UUID.randomUUID()
        val city1 = City(cityId, "Toronto, ON, Canada", 1730246400000, 1730419200000, "ChIJpTvG15DL1IkRd8S0KlBVNTI")
        val city2 = City(cityId, "Toronto, ON, Canada", 1730246400000, 1730419200000, "ChIJpTvG15DL1IkRd8S0KlBVNTI")

        assertEquals(city1, city2)
        assertEquals(city1.hashCode(), city2.hashCode())
    }

    @Test
    fun `test property update`() {
        val city = City(UUID.randomUUID(), "Waterloo, ON, Canada", null, 1733356800000, "ChIJK2f-X1bxK4gRkB0jxyh7AwU")

        city.name = "Toronto, ON, Canada"
        city.startTime = 1730246400000
        city.endTime = 1730419200000

        assertEquals("Toronto, ON, Canada", city.name)
        assertEquals(1730246400000, city.startTime)
        assertEquals(1730419200000, city.endTime)
    }

    @Test
    fun `test nullable`() {
        val city = City(UUID.randomUUID(), "Toronto, ON, Canada", null, null, "ChIJK2f-X1bxK4gRkB0jxyh7AwU")

        assertEquals(null, city.startTime)
        assertEquals(null, city.endTime)
    }

    @Test
    fun `convert test City to FSCity`() {
        val cityId = UUID.randomUUID()
        val city = City(
            cityId = cityId,
            name = "Toronto, ON, Canada",
            startTime = 1622512800000,
            endTime = 1622599200000,
            placeId = "ChIJK2f-X1bxK4gRkB0jxyh7AwU"
        )

        val fsCity = city.toFSCity()

        assertEquals("Toronto, ON, Canada", fsCity.name)
        assertEquals(Timestamp(1622512800, 0), fsCity.startTime)
        assertEquals(Timestamp(1622599200, 0), fsCity.endTime)
        assertEquals("ChIJK2f-X1bxK4gRkB0jxyh7AwU", fsCity.placeId)
    }

    @Test
    fun `convert FSCity to City`() {
        val fsCity = FSCity(
            name = "Toronto, ON, Canada",
            startTime = Timestamp(1622512800, 0),
            endTime = Timestamp(1622599200, 0),
            placeId = "ChIJK2f-X1bxK4gRkB0jxyh7AwU"
        )

        val cityId = UUID.randomUUID()
        val city = fsCity.toCity(cityId)

        assertEquals(cityId, city.cityId)
        assertEquals("Toronto, ON, Canada", city.name)
        assertEquals(1622512800000, city.startTime) // Back to milliseconds
        assertEquals(1622599200000, city.endTime)
        assertEquals("ChIJK2f-X1bxK4gRkB0jxyh7AwU", city.placeId)
    }

    @Test
    fun `convert City to FSCity with bad values`() {
        val city = City(
            cityId = UUID.randomUUID(),
            name = "",
            startTime = null,
            endTime = null,
            placeId = ""
        )

        val fsCity = city.toFSCity()

        assertEquals("", fsCity.name)
        assertNull(fsCity.startTime)
        assertNull(fsCity.endTime)
        assertEquals("", fsCity.placeId)
    }

    @Test
    fun `convert FSCity to City with bad values`() {
        val fsCity = FSCity(
            name = "",
            startTime = null,
            endTime = null,
            placeId = ""
        )

        val cityId = UUID.randomUUID()
        val city = fsCity.toCity(cityId)

        assertEquals(cityId, city.cityId)
        assertEquals("", city.name)
        assertNull(city.startTime)
        assertNull(city.endTime)
        assertEquals("", city.placeId)
    }
}