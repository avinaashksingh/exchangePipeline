package com.scetzhbook.exchangePipeline.model;

/**
 * Price/side identity for order-book maps. Must implement value semantics (unlike snakeyaml Tuple).
 */
public record BookLevelKey(double price, Side side) {}
