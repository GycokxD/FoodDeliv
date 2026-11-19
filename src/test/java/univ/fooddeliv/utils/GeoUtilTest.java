package univ.fooddeliv.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilTest {

    @Test
    void distanceBetweenBishkekPointsIsRealistic() {
        double meters = GeoUtil.distanceMeters(42.8754957, 74.6026276, 42.8723565, 74.5979695);
        assertEquals(520, meters, 30);
    }

    @Test
    void pointAlongWalksByLengthNotBySegmentCount() {
        List<double[]> route = List.of(
                new double[]{0, 0},
                new double[]{0, 0.001},
                new double[]{0, 0.004}
        );
        assertArrayEquals(new double[]{0, 0}, GeoUtil.pointAlong(route, 0), 1e-9);
        assertArrayEquals(new double[]{0, 0.002}, GeoUtil.pointAlong(route, 0.5), 1e-6);
        assertArrayEquals(new double[]{0, 0.004}, GeoUtil.pointAlong(route, 1), 1e-9);
    }

    @Test
    void rejectsInvalidCoordinates() {
        assertTrue(GeoUtil.isValid(42.87, 74.61));
        assertFalse(GeoUtil.isValid(null, 74.61));
        assertFalse(GeoUtil.isValid(91.0, 74.61));
        assertFalse(GeoUtil.isValid(42.87, Double.NaN));
    }
}
