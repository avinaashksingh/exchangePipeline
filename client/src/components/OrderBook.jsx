function LevelRow({ side, price, volume, maxVolume, isTouch }) {
  const pct = Math.min(100, (volume / maxVolume) * 100);
  return (
    <div className={`level ${side} ${isTouch ? 'touch' : ''}`}>
      <div
        className="depth"
        style={{ width: `${pct}%`, opacity: side === 'bid' ? 0.35 : 0.3 }}
        aria-hidden
      />
      <span className="price">{price.toFixed(2)}</span>
      <span className="vol">{volume.toLocaleString()}</span>
    </div>
  );
}

export function OrderBook({ bids, asks, maxVolume, spread }) {
  const askRows = [...asks].sort((a, b) => a.price - b.price).slice(0, 12).reverse();
  const bidRows = [...bids].sort((a, b) => b.price - a.price).slice(0, 12);
  const mid =
    spread.bestBid != null && spread.bestAsk != null
      ? (spread.bestBid + spread.bestAsk) / 2
      : null;

  return (
    <section className="panel orderbook">
      <h2>Order book</h2>
      <div className="book-head">
        <span>Price</span>
        <span>Size</span>
      </div>

      <div className="asks-block">
        {askRows.length === 0 && <p className="empty">No asks</p>}
        {askRows.map((row, i) => (
          <LevelRow
            key={`a-${row.price}`}
            side="ask"
            price={row.price}
            volume={row.volume}
            maxVolume={maxVolume}
            isTouch={i === askRows.length - 1}
          />
        ))}
      </div>

      <div className="spread-row" role="separator">
        {mid != null ? (
          <>
            <span>Mid {mid.toFixed(2)}</span>
            <span className="spread-inline">
              Spread {spread.spread != null ? spread.spread.toFixed(2) : '—'}
            </span>
          </>
        ) : (
          <span>Waiting for quotes…</span>
        )}
      </div>

      <div className="bids-block">
        {bidRows.length === 0 && <p className="empty">No bids</p>}
        {bidRows.map((row, i) => (
          <LevelRow
            key={`b-${row.price}`}
            side="bid"
            price={row.price}
            volume={row.volume}
            maxVolume={maxVolume}
            isTouch={i === 0}
          />
        ))}
      </div>
    </section>
  );
}
