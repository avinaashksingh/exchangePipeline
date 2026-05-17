export function SpreadBar({ bestBid, bestAsk, spread, spreadBps }) {
  const hasBook = bestBid != null && bestAsk != null;

  return (
    <section className="spread-bar" aria-label="Market spread">
      <div className="spread-side bid">
        <span className="label">Best bid</span>
        <span className="value">{bestBid != null ? bestBid.toFixed(2) : '—'}</span>
      </div>
      <div className="spread-center">
        <span className="spread-label">Spread</span>
        <span className="spread-value">
          {hasBook ? spread.toFixed(2) : '—'}
        </span>
        {spreadBps != null && (
          <span className="spread-bps">{spreadBps.toFixed(1)} bps</span>
        )}
        {hasBook && spread <= 0 && (
          <span className="spread-locked">Locked / crossed</span>
        )}
      </div>
      <div className="spread-side ask">
        <span className="label">Best ask</span>
        <span className="value">{bestAsk != null ? bestAsk.toFixed(2) : '—'}</span>
      </div>
    </section>
  );
}
