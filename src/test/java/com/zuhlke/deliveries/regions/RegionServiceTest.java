package com.zuhlke.deliveries.regions;

import com.zuhlke.deliveries.regions.RegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RegionServiceTest {
    RegionService regionService = new RegionService();

    @BeforeEach
    void setUp() {
        regionService.wipeAllData();
    }

    @Test
    void creatingSomeRegions() {
        Long crnaGora = regionService.createRootRegion("Crna Gora");
        regionService.createRegion(crnaGora, "Andrijevica");
        Long bar = regionService.createRegion(crnaGora, "Bar");
        regionService.createRegion(crnaGora, "Budva");

        regionService.createRegion(bar, "Topolica");
        regionService.createRegion(bar, "Polje");

        assertThat(regionService.getRootRegions()).extracting("name").containsExactly("Crna Gora");
        assertThat(regionService.getRegionsOf(crnaGora)).extracting("name").containsExactly("Andrijevica", "Bar", "Budva");
        assertThat(regionService.getRegionsOf(bar)).extracting("name").containsExactly("Topolica", "Polje");
    }

    @Test
    void creatingSameRootRegion() {
        regionService.createRootRegion("Ireland");
        Exception e = assertThrows(Exception.class, () -> regionService.createRootRegion("Ireland"));
        assertThat(e.getMessage()).contains("already exists");
    }

    @Test
    void creatingSameRegionInSameRoot() {
        Long germany = regionService.createRootRegion("Germany");
        regionService.createRegion(germany, "Berlin");
        Exception e = assertThrows(Exception.class, () -> regionService.createRegion(germany, "Berlin"));
        assertThat(e.getMessage()).contains("already exists");
    }

    @Test
    void creatingSameRegionInDifferentRoots() {
        Long eastGermany = regionService.createRootRegion("East Germany");
        Long westGermany = regionService.createRootRegion("West Germany");
        regionService.createRegion(eastGermany, "Berlin");
        regionService.createRegion(westGermany, "Berlin");
    }

    @Test
    void addingCouriersToRegion() {
        Long crnaGora = regionService.createRootRegion("Crna Gora");
        Long bar = regionService.createRegion(crnaGora, "Bar");
        Long budva = regionService.createRegion(crnaGora, "Budva");
        regionService.assignRegion(1L, bar);
        regionService.assignRegion(2L, bar);
        regionService.assignRegion(3L, budva);

        assertThat(regionService.getCouriersIn(bar)).containsExactly(1L, 2L);
        assertThat(regionService.getCouriersIn(budva)).containsExactly(3L);
    }

    @Test
    void addingCouriersToParentRegion() {
        Long crnaGora = regionService.createRootRegion("Crna Gora");
        Long bar = regionService.createRegion(crnaGora, "Bar");
        Long budva = regionService.createRegion(crnaGora, "Budva");
        regionService.assignRegion(1L, bar);
        regionService.assignRegion(2L, bar);
        regionService.assignRegion(3L, budva);
        regionService.assignRegion(4L, crnaGora);

        assertThat(regionService.getAllCouriersIn(bar)).containsExactly(1L, 2L, 4L);
        assertThat(regionService.getAllCouriersIn(budva)).containsExactly(3L, 4L);
    }
}