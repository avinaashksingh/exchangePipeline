import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

const WS_URL = import.meta.env.VITE_WS_URL ?? '/ws';
const MAX_TRADES = 80;

export function useMarketData() {
  const [connected, setConnected] = useState(false);
  const [orderBook, setOrderBook] = useState({ bids: [], asks: [] });
  const [trades, setTrades] = useState([]);
  const clientRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/orderbook', (msg) => {
          try {
            setOrderBook(JSON.parse(msg.body));
          } catch {
            /* ignore malformed */
          }
        });
        client.subscribe('/topic/trades', (msg) => {
          try {
            const trade = JSON.parse(msg.body);
            setTrades((prev) => [trade, ...prev].slice(0, MAX_TRADES));
          } catch {
            /* ignore malformed */
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  return { connected, orderBook, trades };
}
