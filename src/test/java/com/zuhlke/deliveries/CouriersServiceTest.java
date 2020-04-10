package com.zuhlke.deliveries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CouriersServiceTest {
    CouriersService couriersService = new CouriersService();

    @BeforeEach
    void setUp() {
        couriersService.wipeAllData();
    }

    @Test
    void creatingSomeCouriers() {
        Long courier1 = couriersService.registerAsCourier(1L, "Will deliver groceries");
        Long courier2 = couriersService.registerAsCourier(2L, "Will deliver cleaning products");

        List<Courier> couriers = couriersService.getCouriers(Arrays.asList(courier1, courier2));
        Assertions.assertThat(couriers).extracting(c -> c.userId).containsExactly(1L, 2L);
    }

    @Test
    void creatingCourierTwice() {
        couriersService.registerAsCourier(1L, "Will deliver groceries");
        assertThrows(Exception.class, () -> couriersService.registerAsCourier(1L, "Will deliver cleaning products"));
    }
}