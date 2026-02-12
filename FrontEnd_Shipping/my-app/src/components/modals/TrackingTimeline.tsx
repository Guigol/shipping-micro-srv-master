interface Props {
  history: any[];
}

export default function TrackingTimeline({ history }: Props) {
  if (!history?.length) return <p>Aucun historique</p>;

  return (
    <ul className="border-l pl-4">
      {history.map((h, i) => (
        <li key={i} className="mb-3">
          <div className="font-bold">{h.status}</div>
          <div className="text-sm text-gray-600">
            {h.location} — {new Date(h.timestamp).toLocaleString()}
          </div>
          {h.note && <div className="text-sm">{h.note}</div>}
        </li>
      ))}
    </ul>
  );
}
