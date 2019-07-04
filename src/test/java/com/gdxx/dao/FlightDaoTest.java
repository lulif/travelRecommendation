package com.gdxx.dao;

import java.util.List;

import com.gdxx.service.RouteSelectionStrategyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.gdxx.entity.Flight;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlightDaoTest {
    @Autowired
    private RouteSelectionStrategyService routeSelectionStrategyService;

    @Test
    public void test() {
        routeSelectionStrategyService.updateFlightScore("bj");
        routeSelectionStrategyService.updateFlightScore("sjz");
        routeSelectionStrategyService.updateRailScore("bj");
        System.out.println("OK");
    }

}
