import { useMemo } from 'react';
import { useMarketData } from './hooks/useMarketData';
import { SpreadBar } from './components/SpreadBar';
import { OrderBook } from './components/OrderBook';
import { TradeTape } from './components/TradeTape';
import './App.css';
import './components/components.css';

function computeSpread(bids, asks) {
  const bestBid = bids.length ? bids[0].price : null;
  const bestAsk = asks.length ? asks[0].price : null;
  if (bestBid == null || bestAsk == null) {
    return { bestBid, bestAsk, spread: null, spreadBps: null };
  }
  const spread = bestAsk - bestBid;
  const mid = (bestBid + bestAsk) / 2;
  const spreadBps = mid > 0 ? (spread / mid) * 10000 : null;
  return { bestBid, bestAsk, spread, spreadBps };
}

export default function App() {
  const { connected, orderBook, trades } = useMarketData();
  const { bids, asks } = orderBook;
  const spreadInfo = useMemo(() => computeSpread(bids, asks), [bids, asks]);
  const maxVolume = useMemo(() => {
    const levels = [...bids, ...asks];
    return levels.reduce((m, l) => Math.max(m, l.volume ?? 0), 1);
  }, [bids, asks]);

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Exchange Live View</h1>
          <p className="subtitle">Order book &amp; trade tape via STOMP</p>
        </div>
        <span className={`status ${connected ? 'on' : 'off'}`}>
          {connected ? 'Connected' : 'Disconnected'}
        </span>
      </header>

      <SpreadBar {...spreadInfo} />

      <main className="grid">
        <OrderBook bids={bids} asks={asks} maxVolume={maxVolume} spread={spreadInfo} />
        <TradeTape trades={trades} />
      </main>
    </div>
  );
}
