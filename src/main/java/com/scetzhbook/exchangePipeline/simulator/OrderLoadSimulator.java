package com.scetzhbook.exchangePipeline.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.scetzhbook.exchangePipeline.model.Side;

/**
 * Standalone load generator: places orders against the running API at human-like
 * intervals. Roughly 40% of orders are priced to cross the book and produce trades.
 *
 * Run (app + Kafka up): ./mvnw -q exec:java -Dexec.mainClass=com.scetzhbook.exchangePipeline.simulator.OrderLoadSimulator
 */
public final class OrderLoadSimulator {

    private static final String BASE_URL = System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");
    private static final double MID = 100.0;
    private static final double TICK = 0.05;
    private static final double SPREAD_HALF = 0.25;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final HttpClient http = HTTP;
    private final Random random = ThreadLocalRandom.current();
    private final String symbol;
    private final String token;

    private double bestBid = MID - SPREAD_HALF;
    private double bestAsk = MID + SPREAD_HALF;

    private OrderLoadSimulator(String symbol, String token) {
        this.symbol = symbol;
        this.token = token;
    }

    public static void main(String[] args) throws Exception {
        String symbol = randomSymbol();
        System.out.println("OrderLoadSimulator symbol=" + symbol + " api=" + BASE_URL);
        OrderLoadSimulator sim = new OrderLoadSimulator(symbol, fetchToken());
        sim.seedBook();
        sim.runLoop();
    }

    private static String randomSymbol() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder("TST");
        Random r = ThreadLocalRandom.current();
        for (int i = 0; i < 4; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private static String fetchToken() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/token?user=simulator&role=TRADER"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("Token request failed: " + res.statusCode() + " " + res.body());
        }
        return res.body().trim();
    }

    private void seedBook() throws Exception {
        for (int i = 0; i < 5; i++) {
            place(Side.BUY, bestBid - i * TICK, quantity());
            place(Side.SELL, bestAsk + i * TICK, quantity());
        }
        System.out.println("Seeded book around " + bestBid + " / " + bestAsk);
    }

    private void runLoop() throws Exception {
        while (true) {
            boolean aggressive = random.nextDouble() < 0.40;
            Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
            long qty = quantity();
            double price;

            if (aggressive) {
                price = side == Side.BUY ? bestAsk : bestBid;
            } else {
                if (side == Side.BUY) {
                    price = round(bestBid - TICK - random.nextDouble() * 0.15);
                    bestBid = Math.min(bestBid, price);
                } else {
                    price = round(bestAsk + TICK + random.nextDouble() * 0.15);
                    bestAsk = Math.max(bestAsk, price);
                }
            }

            String body = place(side, price, qty);
            if (body.contains("tradeId") || body.contains("buyOrderId")) {
                refreshTouchingPrices(side, price);
            } else if (!aggressive) {
                if (side == Side.BUY) {
                    bestBid = Math.max(bestBid, price);
                } else {
                    bestAsk = Math.min(bestAsk, price);
                }
            }

            long delayMs = (long) (random.nextDouble() * 500);
            Thread.sleep(delayMs);
        }
    }

    private void refreshTouchingPrices(Side takerSide, double tradePrice) {
        if (takerSide == Side.BUY) {
            bestAsk = round(tradePrice + TICK + random.nextDouble() * 0.1);
            bestBid = round(tradePrice - SPREAD_HALF);
        } else {
            bestBid = round(tradePrice - TICK - random.nextDouble() * 0.1);
            bestAsk = round(tradePrice + SPREAD_HALF);
        }
    }

    private String place(Side side, double price, long quantity) throws Exception {
        String json = """
                {"symbol":"%s","side":"%s","price":%.2f,"quantity":%d}
                """.formatted(symbol, side.name(), price, quantity).trim();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/orders"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            System.err.println("Order failed " + res.statusCode() + ": " + res.body());
        }
        return res.body();
    }

    private long quantity() {
        return 5 + random.nextInt(46);
    }

    private static double round(double v) {
        return Math.round(v / TICK) * TICK;
    }
}
