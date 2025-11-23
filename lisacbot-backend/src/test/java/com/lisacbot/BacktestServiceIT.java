package com.lisacbot;

import com.lisacbot.backtest.BacktestResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for backtesting trading strategies via REST API.
 * These tests launch the full Spring Boot application and test the backtest endpoints.
 *
 * NOTE: These tests call the CoinGecko API which has rate limits on the free tier.
 * Wait a few minutes between test runs if you hit rate limits.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BacktestServiceIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testBacktestEndpointWith30Days() throws InterruptedException {
        String url = "http://localhost:" + port + "/api/backtest/custom?days=30&balance=1000";

        BacktestResult result = restTemplate.postForObject(url, null, BacktestResult.class);

        assertNotNull(result);
        assertEquals(1000.0, result.getInitialBalance());
        assertEquals(30, result.getDays());
        assertTrue(result.getFinalBalance() > 0, "Final balance should be positive");
        assertTrue(result.getTotalTrades() >= 0, "Total trades should be non-negative");

        System.out.println("=== Backtest 30 Days (via REST) ===");
        System.out.println(result);

        // Wait to avoid rate limit
        Thread.sleep(2000);
    }

    @Test
    void testBacktestEndpointWith7Days() throws InterruptedException {
        String url = "http://localhost:" + port + "/api/backtest/custom?days=7&balance=1000";

        BacktestResult result = restTemplate.postForObject(url, null, BacktestResult.class);

        assertNotNull(result);
        assertEquals(1000.0, result.getInitialBalance());
        assertEquals(7, result.getDays());
        assertTrue(result.getFinalBalance() > 0);

        // Verify profit/loss calculations
        double expectedProfitLoss = result.getFinalBalance() - result.getInitialBalance();
        assertEquals(expectedProfitLoss, result.getProfitLoss(), 0.01);

        double expectedPercentage = (expectedProfitLoss / result.getInitialBalance()) * 100;
        assertEquals(expectedPercentage, result.getProfitLossPercentage(), 0.01);

        // Verify trade counts
        assertEquals(result.getBuyTrades() + result.getSellTrades(), result.getTotalTrades());

        System.out.println("=== Backtest 7 Days (via REST) ===");
        System.out.println(result);

        // Wait to avoid rate limit
        Thread.sleep(2000);
    }

    @Test
    void testBacktestEndpointDefault() {
        String url = "http://localhost:" + port + "/api/backtest";

        BacktestResult result = restTemplate.postForObject(url, null, BacktestResult.class);

        assertNotNull(result);
        assertTrue(result.getInitialBalance() > 0);
        assertTrue(result.getFinalBalance() > 0);
        assertTrue(result.getDays() > 0);

        System.out.println("=== Backtest Default Config (via REST) ===");
        System.out.println(result);
    }
}
