import { useMemo } from 'react';

function formatTime(ts) {
  if (!ts) return '—';
  const d = new Date(ts);
  return d.toLocaleTimeString(undefined, {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3,
  });
}

export function TradeTape({ trades }) {
  const sortedTrades = useMemo(() => {
    return [...trades].sort((a, b) => b.timestamp - a.timestamp);
  }, [trades]);

  return (
    <section className="panel trades">
      <h2>Trades</h2>
      <div className="tape-head">
        <span>Time</span>
        <span>Price</span>
        <span>Qty</span>
      </div>
      <ul className="tape-list">
        {sortedTrades.length === 0 && (
          <li className="empty">No trades yet — run the simulator</li>
        )}
        {sortedTrades.map((t) => (
          <li key={t.tradeId ?? `${t.timestamp}-${t.price}`} className="tape-row">
            <span className="time">{formatTime(t.timestamp)}</span>
            <span className="price">{Number(t.price).toFixed(2)}</span>
            <span className="qty">{Number(t.quantity).toLocaleString()}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
